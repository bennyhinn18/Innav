package com.example.innav

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.math.*

class NavigationActivity : AppCompatActivity(), LocationListener, SensorEventListener {

    // UI Components
    private lateinit var landmarkSpinner: Spinner
    private lateinit var distanceText: TextView
    private lateinit var startNavigationButton: Button

    // AR Components
    private lateinit var arFragment: ArFragment
    private var arrowRenderable: ModelRenderable? = null

    // Data
    private lateinit var landmarks: List<Landmark>
    private lateinit var paths: List<Path>
    private lateinit var selectedPath: Path

    // Sensors
    private lateinit var locationManager: LocationManager
    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null
    private var currentLocation: Location? = null
    private var currentMagneticData: FloatArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        // Initialize UI
        landmarkSpinner = findViewById(R.id.landmarkSpinner)
        distanceText = findViewById(R.id.distanceText)
        startNavigationButton = findViewById(R.id.startNavigationButton)

        // Initialize AR Fragment
        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        // Initialize sensors
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Request Location Permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        // Start Location Updates
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                this
            )
        }

        // Start Magnetometer Data Collection
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Fetch landmarks and paths from Firebase
        fetchLandmarks()
        fetchPaths()

        // Set up navigation button
        startNavigationButton.setOnClickListener {
            val selectedLandmarkId = landmarks[landmarkSpinner.selectedItemPosition].id
            Log.d("NavigationActivity", "Selected Landmark ID: $selectedLandmarkId")

            if (selectedLandmarkId.isNotEmpty()) {
                // Find the path for the selected landmark
                selectedPath = paths.find { it.name == selectedLandmarkId } ?: return@setOnClickListener
                startNavigation()
            } else {
                Toast.makeText(this, "Please select a landmark first!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchLandmarks() {
        FirebaseFirestore.getInstance().collection("landmarks")
            .get()
            .addOnSuccessListener { documents ->
                landmarks = documents.map { document ->
                    Landmark(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        latitude = document.getDouble("latitude") ?: 0.0,
                        longitude = document.getDouble("longitude") ?: 0.0,
                        altitude = document.getDouble("altitude") ?: 0.0
                    )
                }
                // Populate the spinner with landmark names
                val landmarkNames = landmarks.map { it.name }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, landmarkNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                landmarkSpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch landmarks: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchPaths() {
        FirebaseFirestore.getInstance().collection("paths")
            .get()
            .addOnSuccessListener { documents ->
                paths = documents.map { document ->
                    val points = (document.get("data") as List<Map<String, Any>>).map { point ->
                        PathPoint(
                            latitude = point["latitude"] as Double,
                            longitude = point["longitude"] as Double,
                            altitude = point["altitude"] as Double,
                            magnetic_x = point["magnetic_x"] as? Float ?: 0f,
                            magnetic_y = point["magnetic_y"] as? Float ?: 0f,
                            magnetic_z = point["magnetic_z"] as? Float ?: 0f,
                            timestamp = point["timestamp"] as Long
                        )
                    }
                    Path(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        points = points
                    )
                }
                Log.d("NavigationActivity", "Fetched ${paths.size} paths")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch paths: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startNavigation() {
        // Load the arrow model
        loadArrowModel { arrowRenderable ->
            this.arrowRenderable = arrowRenderable
            // Place arrows along the path
            placeArrows(selectedPath, arrowRenderable)
        }

        // Start a timer to update navigation every 2 seconds
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    updateNavigation()
                }
            }
        }, 0, 2000)
    }

    private fun loadArrowModel(onSuccess: (ModelRenderable) -> Unit) {
        ModelRenderable.builder()
            .setSource(this, Uri.parse("arrow.glb")) // Ensure the model is in assets
            .build()
            .thenAccept(onSuccess)
            .exceptionally { throwable ->
                Toast.makeText(this, "Failed to load AR model: ${throwable.message}", Toast.LENGTH_SHORT).show()
                null
            }
    }

    private fun placeArrows(path: Path, arrowRenderable: ModelRenderable) {
        val scene = arFragment.arSceneView.scene

        for (point in path.points) {
            // Create an anchor at the path point
            val anchor = arFragment.arSceneView.session?.createAnchor(
                Pose.makeTranslation(
                    point.latitude.toFloat(),
                    point.altitude.toFloat(),
                    point.longitude.toFloat()
                )
            ) ?: continue

            // Create an AnchorNode
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(scene)

            // Create a TransformableNode for the arrow
            val arrowNode = TransformableNode(arFragment.transformationSystem)
            arrowNode.renderable = arrowRenderable
            arrowNode.setParent(anchorNode)

            // Position the arrow
            arrowNode.localPosition = Vector3(0f, 0f, -1f) // Adjust as needed
            arrowNode.localRotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 90f) // Rotate to face the correct direction
        }
    }

    private fun updateNavigation() {
        if (currentLocation == null || currentMagneticData == null) {
            Toast.makeText(this, "Waiting for sensor data...", Toast.LENGTH_SHORT).show()
            return
        }

        // Find the closest point in the selected path
        val closestPoint = findClosestPathPoint(
            currentLocation!!.latitude,
            currentLocation!!.longitude,
            currentLocation!!.altitude,
            currentMagneticData!![0],
            currentMagneticData!![1],
            currentMagneticData!![2],
            selectedPath
        )

        if (closestPoint != null) {
            // Update distance display
            val distance = haversine(
                currentLocation!!.latitude,
                currentLocation!!.longitude,
                closestPoint.latitude,
                closestPoint.longitude
            )
            distanceText.text = "Distance: ${"%.1f".format(distance)}m"
        } else {
            Toast.makeText(this, "No matching point found!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findClosestPathPoint(
        currentLat: Double,
        currentLon: Double,
        currentAlt: Double,
        currentMagX: Float,
        currentMagY: Float,
        currentMagZ: Float,
        path: Path
    ): PathPoint? {
        var minDistance = Double.MAX_VALUE
        var closestPoint: PathPoint? = null

        for (point in path.points) {
            // Weighted distance calculation
            val latDiff = (currentLat - point.latitude) * 1000000
            val lonDiff = (currentLon - point.longitude) * 1000000
            val altDiff = (currentAlt - point.altitude)
            val magXDiff = (currentMagX - point.magnetic_x).toDouble()
            val magYDiff = (currentMagY - point.magnetic_y).toDouble()
            val magZDiff = (currentMagZ - point.magnetic_z).toDouble()

            val distance = sqrt(
                latDiff.pow(2) + lonDiff.pow(2) + altDiff.pow(2) +
                        magXDiff.pow(2) + magYDiff.pow(2) + magZDiff.pow(2)
            )

            if (distance < minDistance) {
                minDistance = distance
                closestPoint = point
            }
        }

        return closestPoint
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
        Log.d("NavigationActivity", "Current Location: ${location.latitude}, ${location.longitude}")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            currentMagneticData = event.values.copyOf()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
        sensorManager.unregisterListener(this)
    }
}
data class Landmark(
    val id: String = "", // Firestore document ID
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val magnetic_x: Float = 0f,
    val magnetic_y: Float = 0f,
    val magnetic_z: Float = 0f,
    val timestamp: Long = 0
)
data class Path(
    val id: String = "",
    val name: String = "",
    val points: List<PathPoint> = emptyList(),


)

data class PathPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val magnetic_x: Float, // Ensure this matches Firestore
    val magnetic_y: Float, // Ensure this matches Firestore
    val magnetic_z: Float, // Ensure this matches Firestore
    val timestamp: Long
)
