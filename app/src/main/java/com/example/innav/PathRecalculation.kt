import kotlin.math.*

object PathRecalculation {

    private const val EARTH_RADIUS = 6371.0 // Radius in km

    // Calculates the distance between two GPS points
    fun calculateDistance(
        lat1: Double, lon1: Double, 
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        return 2 * atan2(sqrt(a), sqrt(1 - a)) * EARTH_RADIUS
    }

    // Finds the closest waypoint to recalculate the route
    fun findClosestWaypoint(currentLat: Double, currentLon: Double, waypoints: List<Pair<Double, Double>>): Pair<Double, Double>? {
        return waypoints.minByOrNull { (lat, lon) ->
            calculateDistance(currentLat, currentLon, lat, lon)
        }
    }
}
