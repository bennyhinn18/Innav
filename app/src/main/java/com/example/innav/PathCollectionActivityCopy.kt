package com.example.innav
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
//class PathCollectionActivityCopy : AppCompatActivity(), LocationListener, SensorEventListener {
//
//    private lateinit var locationManager: LocationManager
//    private lateinit var sensorManager: SensorManager
//    private var magnetometer: Sensor? = null
//    private val pathData = mutableListOf<Map<String, Any>>()
//    private var isRecording = false
//
//    private lateinit var startButton: Button
//    private lateinit var stopButton: Button
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_path_collection)
//
//        startButton = findViewById(R.id.startButton)
//        stopButton = findViewById(R.id.stopButton)
//
//        // Initialize Location and Sensor Managers
//        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
//        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
//
//        startButton.setOnClickListener { startRecording() }
//        stopButton.setOnClickListener { stopRecording() }
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
//    }
//
//    private fun startRecording() {
//        isRecording = true
//        startButton.isEnabled = false
//        stopButton.isEnabled = true
//
//        // Start GPS Tracking
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
//    private fun stopRecording() {
//        isRecording = false
//        startButton.isEnabled = true
//        stopButton.isEnabled = false
//
//        // Stop GPS and Sensor Updates
//        locationManager.removeUpdates(this)
//        sensorManager.unregisterListener(this)
//
//        // Ask for path name
//        val pathNameController = EditText(this)
//        AlertDialog.Builder(this)
//            .setTitle("Save Path")
//            .setView(pathNameController)
//            .setPositiveButton("Save") { _, _ ->
//                val pathName = pathNameController.text.toString().trim()
//                if (pathName.isNotEmpty()) {
//                    savePathToFirebase(pathName)
//                }
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//
//    private fun savePathToFirebase(pathName: String) {
//        val db = FirebaseFirestore.getInstance()
//        db.collection("paths").document(pathName)
//            .set(mapOf("name" to pathName, "data" to pathData))
//            .addOnSuccessListener {
//                Snackbar.make(
//                    findViewById(android.R.id.content),
//                    "Path \"$pathName\" saved successfully!",
//                    Snackbar.LENGTH_SHORT
//                ).show()
//                pathData.clear()
//            }
//            .addOnFailureListener { e ->
//                Snackbar.make(
//                    findViewById(android.R.id.content),
//                    "Failed to save path: ${e.message}",
//                    Snackbar.LENGTH_SHORT
//                ).show()
//            }
//    }
//
//    override fun onLocationChanged(location: Location) {
//        if (isRecording) {
//            pathData.add(
//                mapOf(
//                    "latitude" to location.latitude,
//                    "longitude" to location.longitude,
//                    "altitude" to location.altitude,
//                    "timestamp" to Date().time
//                )
//            )
//        }
//    }
//
//    override fun onSensorChanged(event: SensorEvent?) {
//        if (isRecording && event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD && pathData.isNotEmpty()) {
//            (pathData.last() as MutableMap<String, Any>).putAll(
//                mapOf(
//                    "magnetic_x" to event.values[0],
//                    "magnetic_y" to event.values[1],
//                    "magnetic_z" to event.values[2]
//                )
//            )
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