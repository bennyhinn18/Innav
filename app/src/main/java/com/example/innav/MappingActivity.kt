//package com.example.innav
//
//// MainActivity.kt
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.*
//
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import android.webkit.WebView
//import android.webkit.WebViewClient
//
//import androidx.compose.ui.semantics.Role.Companion.Button
//import androidx.compose.ui.semantics.SemanticsProperties.Text
//import org.json.JSONArray
//import org.json.JSONObject
//import java.io.File
//
//class MappingActivity: AppCompatActivity(), SensorEventListener {
//    private lateinit var sensorManager: SensorManager
//    private var magnetometer: Sensor? = null
//    private var currentMagX = 0f
//    private var currentMagY = 0f
//    private var currentMagZ = 0f
//
//    // Grid configuration
//    private val gridSize = 1.0 // meters
//    private var selectedGridX by mutableStateOf(-1)
//    private var selectedGridY by mutableStateOf(-1)
//    private val collectedData = mutableStateListOf<Pair<Pair<Int, Int>, Triple<Float, Float, Float>>>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
//
//        setContent {
//            AppLayout()
//        }
//    }
//
//    @Composable
//    fun AppLayout() {
//        Column(modifier = Modifier.fillMaxSize()) {
//            // SVG Map with Grid Overlay
//            Box(modifier = Modifier.weight(1f)) {
//                SVGViewer("file:///android_asset/floor_plan.svg")
//                GridOverlay()
//            }
//
//            // Data Collection Controls
//            Column(modifier = Modifier.padding(16.dp)) {
//                Button(
//                    onClick = { saveCurrentReading() },
//                    enabled = selectedGridX != -1
//                ) {
//                    Text("Save Current Reading")
//                }
//
//                Text("Selected Grid: ${if(selectedGridX != -1) "($selectedGridX, $selectedGridY)" else "None"}")
//                Text("Magnetic: X=%.2f, Y=%.2f, Z=%.2f".format(currentMagX, currentMagY, currentMagZ))
//
//                Button(onClick = { exportData() }) {
//                    Text("Export Data")
//                }
//            }
//        }
//    }
//
//    @Composable
//    fun SVGViewer(svgUrl: String) {
//        AndroidView(
//            factory = { context ->
//                WebView(context).apply {
//                    webViewClient = WebViewClient()
//                    settings.javaScriptEnabled = true
//                    loadUrl(svgUrl)
//
//                    setOnTouchListener { v, event ->
//                        // Convert touch coordinates to grid coordinates
//                        val scale = (contentHeight * 1f) / height
//                        val svgX = event.x * scale
//                        val svgY = event.y * scale
//
//                        selectedGridX = (svgX / gridSize).toInt()
//                        selectedGridY = (svgY / gridSize).toInt()
//                        true
//                    }
//                }
//            },
//            modifier = Modifier.fillMaxSize()
//        )
//    }
//
//    @Composable
//    fun GridOverlay() {
//        // Implement custom grid drawing using Canvas
//        // (This would need actual implementation based on your SVG coordinate system)
//    }
//
//    private fun saveCurrentReading() {
//        collectedData.add(
//            Pair(selectedGridX to selectedGridY, Triple(currentMagX, currentMagY, currentMagZ))
//        )
//    }
//
//    private fun exportData() {
//        val jsonArray = JSONArray()
//        collectedData.forEach { (grid, mag) ->
//            jsonArray.put(JSONObject().apply {
//                put("x", grid.first)
//                put("y", grid.second)
//                put("mag_x", mag.first)
//                put("mag_y", mag.second)
//                put("mag_z", mag.third)
//            })
//        }
//
//        File(filesDir, "magnetic_data.json").writeText(jsonArray.toString())
//    }
//
//    override fun onResume() {
//        super.onResume()
//        magnetometer?.let {
//            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        sensorManager.unregisterListener(this)
//    }
//
//    override fun onSensorChanged(event: SensorEvent?) {
//        event?.let {
//            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
//                currentMagX = event.values[0]
//                currentMagY = event.values[1]
//                currentMagZ = event.values[2]
//            }
//        }
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//}