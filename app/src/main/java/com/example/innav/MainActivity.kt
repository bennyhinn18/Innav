package com.example.innav

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPathMode: MaterialButton = findViewById(R.id.btnPathMode)
        val btnLandmarkMode: MaterialButton = findViewById(R.id.btnLandmarkMode)
        val btnARNavigate: MaterialButton = findViewById(R.id.btnARNavigate)
        val btnNavigate:MaterialButton = findViewById(R.id.btnNavigate)

        btnPathMode.setOnClickListener { safeStartActivity(PathCollectionActivity::class.java) }
        btnLandmarkMode.setOnClickListener { safeStartActivity(LandmarkCollectionActivity::class.java) }
        btnARNavigate.setOnClickListener { safeStartActivity(ARNavigationActivity::class.java) }
        btnNavigate.setOnClickListener { safeStartActivity(NavigationActivity::class.java) }
    }

    private fun safeStartActivity(activityClass: Class<*>) {
        try {
            startActivity(Intent(this, activityClass))
        } catch (e: Exception) {
            Toast.makeText(this, "Error: Unable to start activity", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}
