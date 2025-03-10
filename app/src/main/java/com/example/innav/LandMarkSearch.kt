import com.google.firebase.firestore.FirebaseFirestore

class LandmarkSearch {

    private val firestore = FirebaseFirestore.getInstance()

    fun searchLandmark(query: String, callback: (List<String>) -> Unit) {
        firestore.collection("landmarks")
            .whereGreaterThanOrEqualTo("landmarkName", query)
            .get()
            .addOnSuccessListener { documents ->
                val results = documents.map { it.getString("landmarkName") ?: "" }
                callback(results)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
}
