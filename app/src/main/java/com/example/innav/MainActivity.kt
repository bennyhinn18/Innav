package com.example.innav

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Button to go to Mark Locations Page
        val btnMarkLocations = findViewById<Button>(R.id.btnMarkLocations)
        btnMarkLocations.setOnClickListener {
            val intent = Intent(this, MarkLocationsActivity::class.java)
            startActivity(intent)
        }

        // Button to go to Navigation Page
        val btnNavigate = findViewById<Button>(R.id.btnNavigate)
        btnNavigate.setOnClickListener {
            val intent = Intent(this, NavigationActivity::class.java)
            startActivity(intent)
        }
    }
}
