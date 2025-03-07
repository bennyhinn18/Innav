package com.example.innav

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Pose
import com.google.ar.sceneform.ux.ArFragment
import java.io.File
import java.io.FileWriter
import java.io.IOException

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var arFragment: ArFragment
    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null
    private lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize AR Fragment
        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        // Initialize Magnetometer Sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Create CSV File
        file = File(getExternalFilesDir(null), "sensor_data.csv")
        writeToFile("Time, Mx (µT), My (µT), Mz (µT), X (m), Y (m), Z (m)\n")
    }

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

            // Get current AR position
            val position = getCurrentARPosition()
            val timestamp = System.currentTimeMillis()

            // Save data
            val data = "$timestamp, $mx, $my, $mz, ${position.first}, ${position.second}, ${position.third}\n"
            writeToFile(data)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getCurrentARPosition(): Triple<Float, Float, Float> {
        val cameraPose: Pose? = arFragment.arSceneView.arFrame?.camera?.pose
        return Triple(cameraPose?.tx() ?: 0f, cameraPose?.ty() ?: 0f, cameraPose?.tz() ?: 0f)
    }

    private fun writeToFile(data: String) {
        try {
            val writer = FileWriter(file, true)
            writer.append(data)
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
