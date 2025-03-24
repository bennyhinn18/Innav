package com.example.innav

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class ARNavigationActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private var arrowRenderable: ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_navigation)

        // Initialize AR Fragment
        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        // Load the arrow model with improved error handling
        ModelRenderable.builder()
            .setSource(this, Uri.parse("arrow.glb")) // Ensure model is in the 'assets' folder
            .build()
            .thenAccept { renderable ->
                arrowRenderable = renderable
            }
            .exceptionally { throwable ->
                Log.e("AR_NAV", "Model loading failed", throwable)
                null
            }

        // Set up AR scene click listener
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, _: MotionEvent ->
            if (arrowRenderable != null) {
                // Create an anchor at the tapped location
                val anchor = hitResult.createAnchor()
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment.arSceneView.scene)

                // Create a transformable node for the arrow
                val arrowNode = TransformableNode(arFragment.transformationSystem)
                arrowNode.renderable = arrowRenderable
                arrowNode.setParent(anchorNode)

                // Scale adjustments for better visibility
                arrowNode.scaleController.minScale = 0.1f
                arrowNode.scaleController.maxScale = 2.0f

                // Position offset for better visibility
                arrowNode.worldPosition = Vector3(
                    anchorNode.worldPosition.x,
                    anchorNode.worldPosition.y + 0.1f, // Slight elevation to ensure visibility
                    anchorNode.worldPosition.z
                )

                // Select the node to make it interactable
                arrowNode.select()
            }
        }
    }
}
