package com.example.innav

import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore

class PathCollectionActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var tvStatus: TextView
    private lateinit var db: FirebaseFirestore

    private var isRecording = false
    private val pathData = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_path_collection)

        btnStart = findViewById(R.id.btnStartRecording)
        btnStop = findViewById(R.id.btnStopRecording)
        tvStatus = findViewById(R.id.tvRecordingStatus)
        db = FirebaseFirestore.getInstance()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        btnStart.setOnClickListener {
            startRecording()
        }

        btnStop.setOnClickListener {
            stopRecording()
        }
    }

    private fun startRecording() {
        if (checkPermissions()) {
            isRecording = true
            btnStart.isEnabled = false
            btnStop.isEnabled = true
            tvStatus.text = "Recording..."
            registerSensors()
        }
    }

    private fun stopRecording() {
        isRecording = false
        btnStart.isEnabled = true
        btnStop.isEnabled = false
        tvStatus.text = "Ready to record"
        unregisterSensors()
        savePathToFirestore()
    }

    private fun checkPermissions(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            false
        }
    }

    private fun registerSensors() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (isRecording && event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val dataPoint = mapOf(
                "timestamp" to System.currentTimeMillis(),
                "magX" to event.values[0],
                "magY" to event.values[1],
                "magZ" to event.values[2]
            )
            pathData.add(dataPoint)
        }
    }

    private fun savePathToFirestore() {
        val pathName = "path_${System.currentTimeMillis()}"
        db.collection("paths").document(pathName)
            .set(mapOf("name" to pathName, "data" to pathData))
            .addOnSuccessListener {
                Toast.makeText(this, "Path saved!", Toast.LENGTH_SHORT).show()
                pathData.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Save failed!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startRecording()
        }
    }
}