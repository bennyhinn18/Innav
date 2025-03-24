// BaseCollectionActivity.kt
package com.example.innav

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import coil.decode.SvgDecoder
import coil.load
import kotlin.math.min



// BaseCollectionActivity.kt
abstract class BaseCollectionActivity : AppCompatActivity(), SensorEventListener {
    protected lateinit var svgMap: ImageView
    protected var svgWidth = 1000f  // Real-world width in meters (adjust to match your SVG)
    protected var svgHeight = 500f  // Real-world height in meters
    protected var magnetometerData: FloatArray? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_map)

        svgMap = findViewById(R.id.svgMap)
        loadSVGMap()
    }



    private fun loadSVGMap() {
        svgMap.load("file:///android_asset/campus_map.svg") {
            decoderFactory(SvgDecoder.Factory())  // Corrected decoder syntax
            size(2048, 2048)  // Corrected size setting
        }
    }
    // Implement empty accuracy changed handler
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Empty implementation
    }


    protected fun getRealWorldCoordinates(event: MotionEvent): Pair<Float, Float> {
        // Get image dimensions after scaling
        val drawable = svgMap.drawable ?: return Pair(0f, 0f)
        val imageWidth = drawable.intrinsicWidth.toFloat()
        val imageHeight = drawable.intrinsicHeight.toFloat()

        // Calculate scaling factors
        val viewWidth = svgMap.width.toFloat()
        val viewHeight = svgMap.height.toFloat()
        val scale = min(viewWidth / imageWidth, viewHeight / imageHeight)

        // Calculate touch position in SVG coordinates
        val touchX = (event.x - (viewWidth - imageWidth * scale) / 2) / scale
        val touchY = (event.y - (viewHeight - imageHeight * scale) / 2) / scale

        // Convert to real-world meters (adjust based on your SVG's scale)
        val realX = (touchX / imageWidth) * svgWidth
        val realY = (touchY / imageHeight) * svgHeight

        return Pair(realX, realY)
    }
}