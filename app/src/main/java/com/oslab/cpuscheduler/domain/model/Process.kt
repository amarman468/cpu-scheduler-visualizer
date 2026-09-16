package com.oslab.cpuscheduler.domain.model

import kotlin.random.Random

data class ProcessInput(
    val id: Int,
    val name: String = "P$id",
    val arrivalTime: Int,
    val burstTime: Int,
    val priority: Int = 1,
    val colorHex: String = DEFAULT_COLORS[(id - 1) % DEFAULT_COLORS.size]
) {
    companion object {
        val DEFAULT_COLORS = listOf(
            "#4CAF50", // Green
            "#2196F3", // Blue
            "#FF9800", // Orange
            "#E91E63", // Pink
            "#9C27B0", // Purple
            "#00BCD4", // Cyan
            "#FFC107", // Amber
            "#795548", // Brown
            "#607D8B", // Blue Grey
            "#8BC34A"  // Light Green
        )

        fun generateRandomProcesses(): List<ProcessInput> {
            val count = Random.nextInt(3, 11) // 3 to 10 processes
            val arrivals = MutableList(count) { Random.nextInt(0, 16) }
            // Ensure at least one process arrives at t=0
            arrivals[Random.nextInt(count)] = 0

            return (1..count).map { i ->
                ProcessInput(
                    id = i,
                    name = "P$i",
                    arrivalTime = arrivals[i - 1],
                    burstTime = Random.nextInt(1, 11),
                    priority = Random.nextInt(1, 6)
                )
            }
        }

        fun generateIdleTimePreset(): List<ProcessInput> {
            val count = Random.nextInt(3, 11) // 3 to 10 processes
            val arrivalPool = mutableListOf<Int>()
            
            // Build timeline with clusters and idle gaps
            var currentTime = if (Random.nextBoolean()) Random.nextInt(1, 4) else 0
            var remaining = count
            
            while (remaining > 0) {
                val clusterSize = minOf(remaining, Random.nextInt(1, 4))
                var lastBurstSum = 0
                for (k in 0 until clusterSize) {
                    val offset = if (k == 0) 0 else Random.nextInt(0, 3)
                    currentTime += offset
                    arrivalPool.add(currentTime)
                    lastBurstSum += Random.nextInt(2, 6)
                }
                remaining -= clusterSize
                if (remaining > 0) {
                    // Force an idle gap between clusters
                    currentTime += lastBurstSum + Random.nextInt(3, 7)
                }
            }
            
            // Shuffle arrival times across P1..PN
            arrivalPool.shuffle()
            
            return (1..count).map { i ->
                ProcessInput(
                    id = i,
                    name = "P$i",
                    arrivalTime = arrivalPool[i - 1],
                    burstTime = Random.nextInt(2, 8),
                    priority = Random.nextInt(1, 6)
                )
            }
        }
    }
}

data class ProcessResult(
    val process: ProcessInput,
    val completionTime: Int,
    val turnaroundTime: Int, // CT - AT
    val waitingTime: Int     // TAT - BT
)
