package com.example.innav

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class MapView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint()
    private val pathPoints = mutableListOf<DataPoint>()

    data class DataPoint(val x: Float, val y: Float, val intensity: Float)

    fun addPoint(x: Float, y: Float, intensity: Float) {
        pathPoints.add(DataPoint(x, y, intensity))
        invalidate()  // Refresh UI
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)

        for (point in pathPoints) {
            paint.color = getColorForIntensity(point.intensity)
            paint.style = Paint.Style.FILL
            canvas.drawCircle(point.x * 50 + width / 2, point.y * 50 + height / 2, 10f, paint)
        }
    }

    private fun getColorForIntensity(value: Float): Int {
        return when {
            value < 10 -> Color.BLUE
            value < 20 -> Color.GREEN
            value < 30 -> Color.YELLOW
            value < 40 -> Color.RED
            else -> Color.BLACK
        }
    }
}
