package com.example.innav

// DataModels.kt
data class MagneticSnapshot(
    val x: Int,
    val y: Int,
    val magX: Float,
    val magY: Float,
    val magZ: Float,
    val timestamp: Long = System.currentTimeMillis()
)

//data class Landmark(
//    val name: String,
//    val gridX: Int,
//    val gridY: Int,
//    val magSignature: List<Float>
//)

data class NavigationPath(
    val name: String,
    val pathPoints: List<MagneticSnapshot>,
    val connectedLandmarks: List<String>
)