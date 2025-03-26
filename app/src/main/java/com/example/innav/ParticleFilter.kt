package com.example.innav

import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class ParticleFilter(private val particleCount: Int) {
    private var particles = listOf<Particle>()
    private lateinit var magneticMap: Map<Pair<Int, Int>, MagneticData>

    fun initialize() {
        particles = List(particleCount) {
            Particle(
                x = Random.nextInt(0, 100),
                y = Random.nextInt(0, 100),
                weight = 1.0 / particleCount
            )
        }
    }

    fun loadMagneticMapFromFirebase() {
        // Implementation to load magnetic map from Firebase
        // This would typically use Firebase Realtime Database or Firestore
    }

    fun update(currentReading: MagneticData) {
        particles = particles.map { particle ->
            val movedParticle = particle.move()
            val weight = calculateWeight(movedParticle, currentReading)
            movedParticle.copy(weight = weight)
        }.normalizeWeights().resample()
    }

    private fun calculateWeight(particle: Particle, reading: MagneticData): Double {
        val mapData = magneticMap[particle.position] ?: return 0.0
        val distance = sqrt(
            (mapData.x.toDouble() - reading.x.toDouble()).pow(2) +
                    (mapData.y.toDouble() - reading.y.toDouble()).pow(2) +
                    (mapData.z.toDouble() - reading.z.toDouble()).pow(2)
        )
        return 1.0 / (1.0 + distance)
    }


    fun currentEstimate(): Pair<Int, Int> {
        return particles.maxByOrNull { it.weight }?.position ?: Pair(0, 0)
    }

    private fun List<Particle>.normalizeWeights(): List<Particle> {
        val totalWeight = sumOf { it.weight }
        return map { it.copy(weight = it.weight / totalWeight) }
    }

    private fun List<Particle>.resample(): List<Particle> {
        return List(particleCount) {
            var cumulativeWeight = 0.0
            val randomValue = Random.nextDouble()
            for (particle in this) {
                cumulativeWeight += particle.weight
                if (cumulativeWeight >= randomValue) {
                    return@List particle.copy(weight = 1.0 / particleCount)
                }
            }
            last().copy(weight = 1.0 / particleCount)
        }
    }
}

data class Particle(
    val x: Int,
    val y: Int,
    val weight: Double
) {
    val position: Pair<Int, Int> get() = Pair(x, y)

    fun move(): Particle = copy(
        x = (x + Random.nextInt(-1, 2)).coerceAtLeast(0),
        y = (y + Random.nextInt(-1, 2)).coerceAtLeast(0)
    )
}

data class MagneticData(
    val x: Float,
    val y: Float,
    val z: Float
)