package com.oslab.cpuscheduler.domain.model

enum class SchedulingAlgorithm(
    val displayName: String,
    val shortName: String,
    val requiresPriority: Boolean = false,
    val requiresQuantum: Boolean = false
) {
    FCFS("First-Come, First-Served", "FCFS"),
    SJF("Shortest Job First (Non-Preemptive)", "SJF"),
    SRTF("Shortest Remaining Time First (Preemptive)", "SRTF"),
    PRIORITY_NP("Priority Scheduling (Non-Preemptive)", "Priority (NP)", requiresPriority = true),
    PRIORITY_P("Priority Scheduling (Preemptive)", "Priority (P)", requiresPriority = true),
    ROUND_ROBIN("Round Robin", "Round Robin", requiresQuantum = true)
}
