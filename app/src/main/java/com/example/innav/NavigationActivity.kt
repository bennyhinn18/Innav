package com.example.innav

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.innav.databinding.ActivityNavigationBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ortiz.touchview.TouchImageView
import kotlin.math.sqrt

class NavigationActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityNavigationBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var particleFilter: ParticleFilter
    private lateinit var currentPath: List<Pair<Int, Int>>
    private var landmarks = mutableListOf<Landmark>()
    private var selectedLandmark: Landmark? = null

    // Visualization parameters
    private val gridSizePx = 100 // 100 pixels per grid cell
    private val positionRadius = 20f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSensors()
        setupFloorPlan()
        setupLandmarkSelector()
        initializeNavigationSystem()
    }

    private fun setupSensors() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    private fun setupFloorPlan() {
        binding.floorPlanView.setImageResource(R.drawable.floor_plan)
        binding.floorPlanContainer.addView(NavigationOverlayView(this))
    }


    private fun setupLandmarkSelector() {
        FirebaseDatabase.getInstance().getReference("landmarks")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    landmarks = snapshot.children.mapNotNull { it.getValue(Landmark::class.java) }.toMutableList()
                    binding.spinnerLandmarks.adapter = ArrayAdapter(
                        this@NavigationActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        landmarks.map { it.name }
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@NavigationActivity, "Failed to load landmarks", Toast.LENGTH_SHORT).show()
                }
            })

        binding.spinnerLandmarks.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedLandmark = landmarks[position]
                updateNavigationPath()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedLandmark = null
            }
        }
    }

    private fun initializeNavigationSystem() {
        particleFilter = ParticleFilter(1000).apply {
            loadMagneticMapFromFirebase()
            initialize()
        }
    }

    private fun updateNavigationPath() {
        selectedLandmark?.let { landmark ->
            val currentPosition = particleFilter.currentEstimate()
            currentPath = PathFinder.findPath(currentPosition, landmark.gridPosition)
            binding.floorPlanContainer.getChildAt(0).invalidate()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.takeIf { it.sensor.type == Sensor.TYPE_MAGNETIC_FIELD }?.let {
            val magneticData = MagneticData(it.values[0], it.values[1], it.values[2])
            particleFilter.update(magneticData)
            updatePositionDisplay()
        }
    }

    private fun updatePositionDisplay() {
        binding.floorPlanContainer.getChildAt(0).invalidate()
        updateNavigationPath()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    inner class NavigationOverlayView(context: Context) : View(context) {
        private val gridPaint = Paint().apply {
            color = Color.GRAY
            strokeWidth = 1f
        }

        private val pathPaint = Paint().apply {
            color = Color.BLUE
            strokeWidth = 8f
            style = Paint.Style.STROKE
        }

        private val positionPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            drawGrid(canvas)
            drawCurrentPosition(canvas)
            drawNavigationPath(canvas)
        }

        private fun drawGrid(canvas: Canvas) {
            val width = width.toFloat()
            val height = height.toFloat()

            // Vertical lines
            var x = 0f
            while (x < width) {
                canvas.drawLine(x, 0f, x, height, gridPaint)
                x += gridSizePx
            }

            // Horizontal lines
            var y = 0f
            while (y < height) {
                canvas.drawLine(0f, y, width, y, gridPaint)
                y += gridSizePx
            }
        }

        private fun drawCurrentPosition(canvas: Canvas) {
            val (gridX, gridY) = particleFilter.currentEstimate()
            val posX = gridX * gridSizePx + gridSizePx / 2f
            val posY = gridY * gridSizePx + gridSizePx / 2f
            canvas.drawCircle(posX, posY, positionRadius, positionPaint)
        }

        private fun drawNavigationPath(canvas: Canvas) {
            if (currentPath.isEmpty()) return

            val path = Path().apply {
                val first = currentPath.first()
                moveTo(
                    first.first * gridSizePx + gridSizePx / 2f, // Convert to Float
                    first.second * gridSizePx + gridSizePx / 2f  // Convert to Float
                )
                currentPath.drop(1).forEach { (x, y) ->
                    lineTo(
                        x * gridSizePx + gridSizePx / 2f, // Convert to Float
                        y * gridSizePx + gridSizePx / 2f  // Convert to Float
                    )
                }
            }
            canvas.drawPath(path, pathPaint)
        }
    }
}