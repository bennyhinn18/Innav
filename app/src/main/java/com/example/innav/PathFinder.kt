package com.example.innav

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object PathFinder {
    fun findPath(start: Pair<Int, Int>, end: Pair<Int, Int>): List<Pair<Int, Int>> {
        // Simplified A* pathfinding implementation
        val openSet = mutableSetOf(start)
        val cameFrom = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>()
        val gScore = mutableMapOf(start to 0.0)
        val fScore = mutableMapOf(start to heuristic(start, end))

        while (openSet.isNotEmpty()) {
            val current = openSet.minByOrNull { fScore.getOrDefault(it, Double.MAX_VALUE) }!!
            if (current == end) return reconstructPath(cameFrom, current)

            openSet.remove(current)

            getNeighbors(current).forEach { neighbor ->
                val tentativeGScore = gScore[current]!! + 1.0
                if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom[neighbor] = current
                    gScore[neighbor] = tentativeGScore
                    fScore[neighbor] = tentativeGScore + heuristic(neighbor, end)
                    if (neighbor !in openSet) openSet.add(neighbor)
                }
            }
        }
        return emptyList()
    }

    private fun heuristic(a: Pair<Int, Int>, b: Pair<Int, Int>): Double {
        return sqrt((a.first - b.first).toDouble().pow(2) + (a.second - b.second).toDouble().pow(2))
    }

    private fun reconstructPath(
        cameFrom: Map<Pair<Int, Int>, Pair<Int, Int>>,
        current: Pair<Int, Int>
    ): List<Pair<Int, Int>> {
        val path = mutableListOf(current)
        var currentStep = current
        while (cameFrom.containsKey(currentStep)) {
            currentStep = cameFrom[currentStep]!!
            path.add(0, currentStep)
        }
        return path
    }

    private fun getNeighbors(pos: Pair<Int, Int>): List<Pair<Int, Int>> {
        return listOf(
            Pair(pos.first + 1, pos.second),
            Pair(pos.first - 1, pos.second),
            Pair(pos.first, pos.second + 1),
            Pair(pos.first, pos.second - 1)
        ).filter { it.first >= 0 && it.second >= 0 }
    }
}