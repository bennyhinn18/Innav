package com.example.innav

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Pose
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.AnchorNode
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity(), SensorEventListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

          // Initialize Firebase
        firestore = FirebaseFirestore.getInstance() // Initialize Firestore after FirebaseApp

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        mapView = findViewById(R.id.mapView)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        loadMarkersFromFirestore()  // Now firestore is initialized before calling this function
        load3DModel()
    }
    private lateinit var arFragment: ArFragment
    private lateinit var sensorManager: SensorManager
    private lateinit var mapView: MapView
    private var magnetometer: Sensor? = null
    private lateinit var arRenderable: ModelRenderable
    private lateinit var firestore: FirebaseFirestore




    override fun onResume() {
        super.onResume()
        magnetometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val mx = event.values[0]
            val my = event.values[1]
            val mz = event.values[2]
            val position = getCurrentARPosition()

            // Add point to the live map
            mapView.addPoint(position.first, position.second, mz)

            // Upload data to Firestore
            saveLocationData(position.first, position.second, position.third, mz)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getCurrentARPosition(): Triple<Float, Float, Float> {
        val cameraPose: Pose? = arFragment.arSceneView.arFrame?.camera?.pose
        return Triple(cameraPose?.tx() ?: 0f, cameraPose?.ty() ?: 0f, cameraPose?.tz() ?: 0f)
    }

    private fun loadMarkersFromFirestore() {
        firestore.collection("indoor_location_data")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val x = document.getDouble("x")?.toFloat() ?: 0f
                    val y = document.getDouble("y")?.toFloat() ?: 0f
                    val z = document.getDouble("z")?.toFloat() ?: 0f
                    placeARMarker(x, y, z)
                }
            }
    }

    private fun placeARMarker(x: Float, y: Float, z: Float) {
        val pose = Pose.makeTranslation(x, y, z)
        val anchor = arFragment.arSceneView.session?.createAnchor(pose) ?: return
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment.arSceneView.scene)

        val modelNode = TransformableNode(arFragment.transformationSystem)
        modelNode.setParent(anchorNode)
        modelNode.renderable = arRenderable
    }

    private fun load3DModel() {
        ModelRenderable.builder()
            .setSource(this, Uri.parse("marker.sfb"))  // Ensure the model file exists in assets
            .build()
            .thenAccept { renderable -> arRenderable = renderable }
            .exceptionally { throwable ->
                println("⚠️ Model loading failed: ${throwable.message}")
                null
            }
    }

    private fun saveLocationData(x: Float, y: Float, z: Float, mz: Float) {
        val locationData = hashMapOf(
            "x" to x,
            "y" to y,
            "z" to z,
            "mz" to mz
        )

        firestore.collection("indoor_location_data")
            .add(locationData)
            .addOnSuccessListener {
                println("✅ Location data saved successfully")
            }
            .addOnFailureListener { e ->
                println("❌ Failed to save location data: ${e.message}")
            }
    }
}
