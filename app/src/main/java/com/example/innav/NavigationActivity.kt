package com.example.innav

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.firestore.FirebaseFirestore

class NavigationActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var arFragment: ArFragment
    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null
    private var lastMagneticValues = FloatArray(3)
    private val firestore = FirebaseFirestore.getInstance()
    private var arRenderable: ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        arFragment = supportFragmentManager.findFragmentById(R.id.arNavigationFragment) as ArFragment
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        loadModel()  // Load AR Model
        startNavigation()
    }

    private fun loadModel() {
        ModelRenderable.builder()
            .setSource(this, Uri.parse("marker.glb"))

            .build()
            .thenAccept { renderable -> arRenderable = renderable }
            .exceptionally { throwable ->
                Toast.makeText(this, "Failed to load AR model", Toast.LENGTH_SHORT).show()
                null
            }
    }


    private fun startNavigation() {
        firestore.collection("locations")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val x = document.getDouble("x")?.toFloat() ?: 0f
                    val y = document.getDouble("y")?.toFloat() ?: 0f
                    val z = document.getDouble("z")?.toFloat() ?: 0f
                    val mxStored = document.getDouble("mx")?.toFloat() ?: 0f
                    val myStored = document.getDouble("my")?.toFloat() ?: 0f
                    val mzStored = document.getDouble("mz")?.toFloat() ?: 0f

                    val (mxLive, myLive, mzLive) = lastMagneticValues

                    if (isMatchingMagneticField(mxStored, myStored, mzStored, mxLive, myLive, mzLive)) {
                        placeARMarker(x, y, z)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching locations", Toast.LENGTH_SHORT).show()
            }
    }

    private fun placeARMarker(x: Float, y: Float, z: Float) {
        val pose = Pose.makeTranslation(x, y, z)
        val session = arFragment.arSceneView.session ?: return

        val anchor = session.createAnchor(pose)
        val anchorNode = AnchorNode(anchor).apply {
            setParent(arFragment.arSceneView.scene)
        }

        val modelNode = TransformableNode(arFragment.transformationSystem).apply {
            setParent(anchorNode)
            renderable = arRenderable
        }

        arFragment.arSceneView.scene.addChild(anchorNode)
    }

    private fun isMatchingMagneticField(
        mxStored: Float, myStored: Float, mzStored: Float,
        mxLive: Float, myLive: Float, mzLive: Float
    ): Boolean {
        val threshold = 7.0f  // Adjusted for flexibility
        return (Math.abs(mxStored - mxLive) < threshold &&
                Math.abs(myStored - myLive) < threshold &&
                Math.abs(mzStored - mzLive) < threshold)
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
