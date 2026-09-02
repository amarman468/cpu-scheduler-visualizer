package com.oslab.cpuscheduler.domain.model

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
            val count = kotlin.random.Random.nextInt(3, 7) // 3 to 6 processes
            val list = mutableListOf<ProcessInput>()
            var currentArrival = 0
            for (i in 1..count) {
                val arrival = if (i == 1) 0 else currentArrival + kotlin.random.Random.nextInt(0, 3)
                currentArrival = arrival
                val burst = kotlin.random.Random.nextInt(1, 11) // 1 to 10
                val priority = kotlin.random.Random.nextInt(1, 6) // 1 to 5
                list.add(
                    ProcessInput(
                        id = i,
                        name = "P$i",
                        arrivalTime = arrival,
                        burstTime = burst,
                        priority = priority
                    )
                )
            }
            return list
        }
    }
}

data class ProcessResult(
    val process: ProcessInput,
    val completionTime: Int,
    val turnaroundTime: Int, // CT - AT
    val waitingTime: Int     // TAT - BT
)
