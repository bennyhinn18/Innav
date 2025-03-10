package com.example.innav

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MarkLocationsActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var arFragment: ArFragment
    private lateinit var sensorManager: SensorManager
    private lateinit var magnetometer: Sensor
    private var lastMagneticValues = FloatArray(3)
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mark_locations)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!

        // Handle AR Tap Event
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            val pose = hitResult.createAnchor().pose
            val mz = lastMagneticValues[2]  // Focus on 'mz' for ORIENTER

            // Unique marker label
            val label = "Location-${System.currentTimeMillis()}"

            // Save to Firestore
            saveLocation(pose.tx(), pose.ty(), pose.tz(), mz, label)

            // Visualize marker
            val anchorNode = AnchorNode(hitResult.createAnchor())
            arFragment.arSceneView.scene.addChild(anchorNode)
        }
    }

    private fun saveLocation(x: Float, y: Float, z: Float, mz: Float, label: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        val locationData = hashMapOf(
            "x" to x, "y" to y, "z" to z,
            "mz" to mz,
            "label" to label,
            "timestamp" to timestamp
        )
        // Print location data to Logcat
        println("Location Data: x=$x, y=$y, z=$z, mz=$mz, label=$label, timestamp=$timestamp")
        Log.d("MarkLocationsActivity", "Saving Location Data: x=$x, y=$y, z=$z, mz=$mz")

        firestore.collection("locations")
            .add(locationData)
            .addOnSuccessListener { println("✅ Location Saved: $label") }
            .addOnFailureListener { e -> println("❌ Error: ${e.message}") }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            lastMagneticValues = event.values.clone()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
