package com.example.innav

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.firestore.FirebaseFirestore
import com.ortiz.touchview.TouchImageView

class LandmarkCollectionActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var magnetometer: Sensor
    private lateinit var database: FirebaseFirestore
    private lateinit var btnSave: Button
    private lateinit var etLandmarkName: EditText
    private lateinit var tvMagneticData: TextView
    private lateinit var tvGridCoordinates: TextView

    private var currentMagX = 0f
    private var currentMagY = 0f
    private var currentMagZ = 0f
    private var currentGridX = 0
    private var currentGridY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landmark_collection)

        // Initialize UI components
        btnSave = findViewById(R.id.btnSaveLandmark)
        etLandmarkName = findViewById(R.id.etLandmarkName)
        tvMagneticData = findViewById(R.id.tvMagneticData)
        tvGridCoordinates = findViewById(R.id.tvGridCoordinates)

        // Initialize Firebase
        database = FirebaseFirestore.getInstance()

        // Initialize sensors
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!

        // Set click listener
        btnSave.setOnClickListener {
            saveLandmarkToDatabase()
        }

        // Initialize grid selection
        setupGridSelection()
    }

    private fun setupGridSelection() {
        val floorPlanView = findViewById<TouchImageView>(R.id.floorPlanView)
        floorPlanView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                handleGridSelection(event.x, event.y)
            }
            true
        }
    }

    private fun handleGridSelection(touchX: Float, touchY: Float) {
        // Convert touch coordinates to grid coordinates
        currentGridX = (touchX / 100).toInt() // Adjust 100px per grid cell
        currentGridY = (touchY / 100).toInt()
        tvGridCoordinates.text = "Grid: ($currentGridX, $currentGridY)"
    }

    private fun saveLandmarkToDatabase() {
        val name = etLandmarkName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter landmark name", Toast.LENGTH_SHORT).show()
            return
        }

        val landmarkData = hashMapOf(
            "name" to name,
            "gridX" to currentGridX,
            "gridY" to currentGridY,
            "magX" to currentMagX,
            "magY" to currentMagY,
            "magZ" to currentMagZ
        )

        database.collection("landmarks").add(landmarkData)
            .addOnSuccessListener {
                Toast.makeText(this, "Landmark saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Save failed!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                currentMagX = event.values[0]
                currentMagY = event.values[1]
                currentMagZ = event.values[2]
                updateMagneticDisplay()
            }
        }
    }

    private fun updateMagneticDisplay() {
        tvMagneticData.text = String.format(
            "Magnetic: X: %.2f, Y: %.2f, Z: %.2f",
            currentMagX,
            currentMagY,
            currentMagZ
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            magnetometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}