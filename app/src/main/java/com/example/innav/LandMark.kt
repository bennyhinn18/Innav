package com.example.innav


data class Landmark(
    val name: String = "",
    val gridX: Int = 0,
    val gridY: Int = 0,
    val magneticData: MagneticData = MagneticData(0f, 0f, 0f)
) {
    val gridPosition: Pair<Int, Int> get() = Pair(gridX, gridY)
}