import android.content.Context
import android.location.Location
import com.google.firebase.firestore.FirebaseFirestore

class DataCollector(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()

    fun collectPathData(
        latitude: Double, 
        longitude: Double, 
        magneticX: Float, 
        magneticY: Float, 
        magneticZ: Float,
        pathName: String
    ) {
        val data = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "magneticX" to magneticX,
            "magneticY" to magneticY,
            "magneticZ" to magneticZ,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("paths").document(pathName)
            .set(data)
            .addOnSuccessListener { println("Path data saved!") }
            .addOnFailureListener { println("Failed to save path data") }
    }

    fun collectLandmarkData(
        latitude: Double,
        longitude: Double,
        landmarkName: String
    ) {
        val data = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "landmarkName" to landmarkName
        )

        firestore.collection("landmarks").document(landmarkName)
            .set(data)
            .addOnSuccessListener { println("Landmark saved!") }
            .addOnFailureListener { println("Failed to save landmark data") }
    }
}
