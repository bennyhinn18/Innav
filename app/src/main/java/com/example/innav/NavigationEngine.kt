package com.example.innav

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class NavigationEngine(
    private val context: Context,
    private val updateInterval: Long = 1000L
) : SensorEventListener {
    private val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    private val particleFilter = ParticleFilter(1000)
    private var lastUpdate = 0L

    init {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun startTracking() {
        particleFilter.initialize()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val currentMag = MagneticSnapshot(
                0, 0, // Grid position will be updated by particle filter
                event.values[0],
                event.values[1],
                event.values[2]
            )

            if (System.currentTimeMillis() - lastUpdate > updateInterval) {
                particleFilter.update(currentMag)
                lastUpdate = System.currentTimeMillis()
            }
        }
    }

    fun getCurrentPosition(): Pair<Int, Int> {
        return particleFilter.bestEstimate()
    }

    fun shutdown() {
        sensorManager.unregisterListener(this)
    }
}