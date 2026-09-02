package com.oslab.cpuscheduler.domain.model

data class GanttBlock(
    val processId: Int?,
    val processName: String,
    val startTime: Int,
    val endTime: Int,
    val isIdle: Boolean = false,
    val colorHex: String = "#9E9E9E" // Default grey for idle
) {
    val duration: Int get() = endTime - startTime
}
