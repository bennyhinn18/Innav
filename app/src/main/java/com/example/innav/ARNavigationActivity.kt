package com.example.innav

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.innav.R
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.firestore.FirebaseFirestore

class ARNavigationActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_navigation)

        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            val anchor = hitResult.createAnchor()
            loadPathData(anchor)
        }
    }

    private fun loadPathData(anchor: Anchor) {
        firestore.collection("example_path").get().addOnSuccessListener { snapshot ->
            val pathData = snapshot.documents.mapNotNull { it.data }
            for (point in pathData) {
                placeModel(anchor, "file:///android_asset/models/arrow_forward.glb")
            }
        }.addOnFailureListener {
            it.printStackTrace()
        }
    }

    private fun placeModel(anchor: Anchor, modelName: String) {
        ModelRenderable.builder()
            .setSource(this, Uri.parse(modelName))
            .build()
            .thenAccept { renderable ->
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment.arSceneView.scene)

                val modelNode = TransformableNode(arFragment.transformationSystem)
                modelNode.setParent(anchorNode)
                modelNode.renderable = renderable
                modelNode.select()
            }
            .exceptionally {
                runOnUiThread {
                    Toast.makeText(this, "Error loading model", Toast.LENGTH_SHORT).show()
                }
                it.printStackTrace()
                null
            }
    }
}
