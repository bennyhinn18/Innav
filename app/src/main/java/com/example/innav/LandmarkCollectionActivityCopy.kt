//package com.example.innav
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import android.location.Location
//import android.location.LocationListener
//import android.location.LocationManager
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import com.google.android.material.snackbar.Snackbar
//import com.google.firebase.firestore.FirebaseFirestore
//import java.util.*
//
//class LandmarkCollectionActivityCopy : AppCompatActivity(), LocationListener, SensorEventListener {
//
//    private lateinit var locationManager: LocationManager
//    private lateinit var sensorManager: SensorManager
//    private var magnetometer: Sensor? = null
//    private var currentLocation: Location? = null
//    private var magnetometerData: FloatArray? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_landmark_collection)
//
//        val addLandmarkButton: Button = findViewById(R.id.addLandmarkButton)
//        addLandmarkButton.setOnClickListener { addLandmark() }
//
//        // Initialize Location and Sensor Managers
//        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
//        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
//
//
//        // Request Location Permissions
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                1
//            )
//        }
//
//        // Start Location Updates
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            locationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER,
//                1000L,
//                1f,
//                this
//            )
//        }
//
//        // Start Magnetometer Data Collection
//        magnetometer?.let {
//            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
//        }
//    }
//
//    private fun addLandmark() {
//        if (currentLocation == null || magnetometerData == null) {
//            Snackbar.make(
//                findViewById(android.R.id.content),
//                "Location or magnetometer data not available!",
//                Snackbar.LENGTH_SHORT
//            ).show()
//            return
//        }
//
//        val landmarkNameController = EditText(this)
//        AlertDialog.Builder(this)
//            .setTitle("Add Landmark")
//            .setView(landmarkNameController)
//            .setPositiveButton("Add") { _, _ ->
//                val landmarkName = landmarkNameController.text.toString().trim()
//                if (landmarkName.isNotEmpty()) {
//                    saveLandmarkToFirebase(landmarkName)
//                }
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//
//    private fun saveLandmarkToFirebase(landmarkName: String) {
//        val db = FirebaseFirestore.getInstance()
//        db.collection("landmarks")
//            .add(
//                mapOf(
//                    "name" to landmarkName,
//                    "latitude" to currentLocation?.latitude,
//                    "longitude" to currentLocation?.longitude,
//                    "altitude" to currentLocation?.altitude,
//                    "magnetic_x" to magnetometerData?.get(0),
//                    "magnetic_y" to magnetometerData?.get(1),
//                    "magnetic_z" to magnetometerData?.get(2),
//                    "timestamp" to Date().time
//                )
//            )
//            .addOnSuccessListener {
//                Snackbar.make(
//                    findViewById(android.R.id.content),
//                    "Landmark \"$landmarkName\" added successfully!",
//                    Snackbar.LENGTH_SHORT
//                ).show()
//            }
//            .addOnFailureListener { e ->
//                Snackbar.make(
//                    findViewById(android.R.id.content),
//                    "Failed to add landmark: ${e.message}",
//                    Snackbar.LENGTH_SHORT
//                ).show()
//            }
//    }
//
//    override fun onLocationChanged(location: Location) {
//        currentLocation = location
//    }
//
//    override fun onSensorChanged(event: SensorEvent?) {
//        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
//            magnetometerData = event.values
//        }
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//
//    override fun onDestroy() {
//        super.onDestroy()
//        locationManager.removeUpdates(this)
//        sensorManager.unregisterListener(this)
//    }
//}