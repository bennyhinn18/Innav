package com.example.innav

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.innav.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PathCollectionActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var pathNameInput: EditText
    private lateinit var database: DatabaseReference

    private val pathData = mutableListOf<Map<String, Any>>() // Stores collected path data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_path_collection)

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        pathNameInput = findViewById(R.id.pathNameInput)

        database = FirebaseDatabase.getInstance().getReference("paths")

        startButton.setOnClickListener {
            pathData.clear() // Clear previous data
            startCollectingData()
        }

        stopButton.setOnClickListener {
            val pathName = pathNameInput.text.toString().trim()
            if (pathName.isEmpty()) {
                Toast.makeText(this, "Please enter a path name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            savePathData(pathName)
        }
    }

    private fun startCollectingData() {
        // Simulate continuous GPS and magnetic data collection
        // Example data structure: { lat: x, lon: y, magX: z, magY: a, magZ: b }
        // Add code here to fetch data from sensors
    }

    private fun savePathData(pathName: String) {
        database.child(pathName).setValue(pathData)
            .addOnSuccessListener {
                Toast.makeText(this, "Path saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save path data", Toast.LENGTH_SHORT).show()
            }
    }
}
