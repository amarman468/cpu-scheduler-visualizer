package com.oslab.cpuscheduler.domain.model

data class AlgorithmResult(
    val algorithm: SchedulingAlgorithm,
    val timeQuantum: Int = 0,
    val processResults: List<ProcessResult>,
    val ganttBlocks: List<GanttBlock>,
    val averageWaitingTime: Double,
    val averageTurnaroundTime: Double,
    val totalIdleTime: Int,
    val totalExecutionTime: Int,
    val cpuUtilization: Double // Percentage ((Total Time - Idle Time) / Total Time) * 100
)
