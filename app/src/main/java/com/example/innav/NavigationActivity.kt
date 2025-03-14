
package com.example.innav
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.innav.R
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode

class NavigationActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            val anchor = hitResult.createAnchor()
            placeModel(anchor, "models/arrow_forward.glb")
        }
    }

    private fun placeModel(anchor: Anchor, modelName: String) {
        ModelRenderable.builder()
            .setSource(this, Uri.parse(modelName))
             // Required for .glb files
            .build()
            .thenAccept { renderable ->
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment.arSceneView.scene)

                val modelNode = TransformableNode(arFragment.transformationSystem)
                modelNode.setParent(anchorNode)
                modelNode.renderable = renderable
                modelNode.select() // Focus the model immediately
            }
            .exceptionally {
                it.printStackTrace()
                null
            }
    }
}
