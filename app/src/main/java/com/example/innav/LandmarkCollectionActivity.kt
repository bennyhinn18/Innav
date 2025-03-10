package com.example.innav

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.innav.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LandmarkCollectionActivity : AppCompatActivity() {

    private lateinit var saveLandmarkButton: Button
    private lateinit var landmarkNameInput: EditText
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landmark_collection)

        saveLandmarkButton = findViewById(R.id.saveLandmarkButton)
        landmarkNameInput = findViewById(R.id.landmarkNameInput)

        database = FirebaseDatabase.getInstance().getReference("landmarks")

        saveLandmarkButton.setOnClickListener {
            val landmarkName = landmarkNameInput.text.toString().trim()
            if (landmarkName.isEmpty()) {
                Toast.makeText(this, "Please enter a landmark name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveLandmark(landmarkName)
        }
    }

    private fun saveLandmark(name: String) {
        val landmarkData = mapOf(
            "latitude" to 12.9716,    // Replace with GPS data
            "longitude" to 77.5946,   // Replace with GPS data
            "magX" to 0.0,
            "magY" to 0.0,
            "magZ" to 0.0
        )

        database.child(name).setValue(landmarkData)
            .addOnSuccessListener {
                Toast.makeText(this, "Landmark saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save landmark data", Toast.LENGTH_SHORT).show()
            }
    }
}
