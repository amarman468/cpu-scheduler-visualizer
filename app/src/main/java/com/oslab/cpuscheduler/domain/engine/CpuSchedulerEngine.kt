package com.oslab.cpuscheduler.domain.engine

import com.oslab.cpuscheduler.domain.model.*
import java.util.LinkedList
import java.util.Queue
import kotlin.math.round

object CpuSchedulerEngine {

    fun solve(
        algorithm: SchedulingAlgorithm,
        processes: List<ProcessInput>,
        timeQuantum: Int = 2
    ): AlgorithmResult {
        if (processes.isEmpty()) {
            return AlgorithmResult(
                algorithm = algorithm,
                timeQuantum = timeQuantum,
                processResults = emptyList(),
                ganttBlocks = emptyList(),
                averageWaitingTime = 0.0,
                averageTurnaroundTime = 0.0,
                totalIdleTime = 0,
                totalExecutionTime = 0,
                cpuUtilization = 0.0
            )
        }

        return when (algorithm) {
            SchedulingAlgorithm.FCFS -> solveFCFS(processes)
            SchedulingAlgorithm.SJF -> solveNonPreemptive(
                processes, algorithm,
                compareBy({ it.burstTime }, { it.arrivalTime }, { it.id })
            )
            SchedulingAlgorithm.SRTF -> solvePreemptive(
                processes, algorithm
            ) { rt -> compareBy({ rt.getValue(it.id) }, { it.arrivalTime }, { it.id }) }
            SchedulingAlgorithm.PRIORITY_NP -> solveNonPreemptive(
                processes, algorithm,
                compareBy({ it.priority }, { it.arrivalTime }, { it.id })
            )
            SchedulingAlgorithm.PRIORITY_P -> solvePreemptive(
                processes, algorithm
            ) { _ -> compareBy({ it.priority }, { it.arrivalTime }, { it.id }) }
            SchedulingAlgorithm.ROUND_ROBIN -> solveRoundRobin(processes, maxOf(1, timeQuantum))
        }
    }

    // 1. First-Come, First-Served (FCFS)
    private fun solveFCFS(processes: List<ProcessInput>): AlgorithmResult {
        val sorted = processes.sortedWith(compareBy({ it.arrivalTime }, { it.id }))
        var currentTime = 0
        val ganttBlocks = mutableListOf<GanttBlock>()
        val results = mutableListOf<ProcessResult>()

        for (proc in sorted) {
            if (currentTime < proc.arrivalTime) {
                ganttBlocks.add(
                    GanttBlock(
                        processId = null,
                        processName = "Idle",
                        startTime = currentTime,
                        endTime = proc.arrivalTime,
                        isIdle = true
                    )
                )
                currentTime = proc.arrivalTime
            }
            val startTime = currentTime
            currentTime += proc.burstTime
            val completionTime = currentTime
            val turnaroundTime = completionTime - proc.arrivalTime
            val waitingTime = turnaroundTime - proc.burstTime

            ganttBlocks.add(
                GanttBlock(
                    processId = proc.id,
                    processName = proc.name,
                    startTime = startTime,
                    endTime = completionTime,
                    isIdle = false,
                    colorHex = proc.colorHex
                )
            )

            results.add(
                ProcessResult(
                    process = proc,
                    completionTime = completionTime,
                    turnaroundTime = turnaroundTime,
                    waitingTime = waitingTime
                )
            )
        }

        return buildResult(SchedulingAlgorithm.FCFS, 0, processes, results, ganttBlocks)
    }

    // Unified Non-Preemptive Solver (SJF, Priority NP)
    private fun solveNonPreemptive(
        processes: List<ProcessInput>,
        algorithm: SchedulingAlgorithm,
        comparator: Comparator<ProcessInput>
    ): AlgorithmResult {
        val uncompleted = processes.toMutableList()
        var currentTime = 0
        val ganttBlocks = mutableListOf<GanttBlock>()
        val results = mutableListOf<ProcessResult>()

        while (uncompleted.isNotEmpty()) {
            val ready = uncompleted.filter { it.arrivalTime <= currentTime }
            if (ready.isEmpty()) {
                val nextArrival = uncompleted.minOf { it.arrivalTime }
                ganttBlocks.add(
                    GanttBlock(
                        processId = null,
                        processName = "Idle",
                        startTime = currentTime,
                        endTime = nextArrival,
                        isIdle = true
                    )
                )
                currentTime = nextArrival
                continue
            }

            val nextProc = ready.minWithOrNull(comparator)!!

            uncompleted.remove(nextProc)
            val startTime = currentTime
            currentTime += nextProc.burstTime
            val completionTime = currentTime
            val turnaroundTime = completionTime - nextProc.arrivalTime
            val waitingTime = turnaroundTime - nextProc.burstTime

            ganttBlocks.add(
                GanttBlock(
                    processId = nextProc.id,
                    processName = nextProc.name,
                    startTime = startTime,
                    endTime = completionTime,
                    isIdle = false,
                    colorHex = nextProc.colorHex
                )
            )

            results.add(
                ProcessResult(
                    process = nextProc,
                    completionTime = completionTime,
                    turnaroundTime = turnaroundTime,
                    waitingTime = waitingTime
                )
            )
        }

        return buildResult(algorithm, 0, processes, results, ganttBlocks)
    }

    // Unified Preemptive Solver (SRTF, Priority P)
    private fun solvePreemptive(
        processes: List<ProcessInput>,
        algorithm: SchedulingAlgorithm,
        comparatorFactory: (Map<Int, Int>) -> Comparator<ProcessInput>
    ): AlgorithmResult {
        val remainingTime = processes.associate { it.id to it.burstTime }.toMutableMap()
        val completionTimes = mutableMapOf<Int, Int>()
        var currentTime = 0
        val totalProcesses = processes.size
        var completedCount = 0

        val timeline = mutableListOf<Pair<Int, ProcessInput?>>()

        val maxTimeLimit = processes.sumOf { it.burstTime } + processes.maxOf { it.arrivalTime } + 100

        while (completedCount < totalProcesses && currentTime < maxTimeLimit) {
            val ready = processes.filter { it.arrivalTime <= currentTime && remainingTime.getValue(it.id) > 0 }
            if (ready.isEmpty()) {
                timeline.add(currentTime to null)
                currentTime++
                continue
            }

            val currentProc = ready.minWithOrNull(comparatorFactory(remainingTime))!!

            timeline.add(currentTime to currentProc)
            remainingTime[currentProc.id] = remainingTime.getValue(currentProc.id) - 1
            currentTime++

            if (remainingTime.getValue(currentProc.id) == 0) {
                completedCount++
                completionTimes[currentProc.id] = currentTime
            }
        }

        val ganttBlocks = mergeTimelineToGantt(timeline)
        val results = processes.map { proc ->
            val ct = completionTimes.getValue(proc.id)
            val tat = ct - proc.arrivalTime
            val wt = tat - proc.burstTime
            ProcessResult(proc, ct, tat, wt)
        }

        return buildResult(algorithm, 0, processes, results, ganttBlocks)
    }

    // Round Robin (RR)
    private fun solveRoundRobin(
        processes: List<ProcessInput>,
        timeQuantum: Int
    ): AlgorithmResult {
        val sortedByArrival = processes.sortedWith(compareBy({ it.arrivalTime }, { it.id }))
        val remainingTime = processes.associate { it.id to it.burstTime }.toMutableMap()
        val completionTimes = mutableMapOf<Int, Int>()
        val ganttBlocks = mutableListOf<GanttBlock>()

        val readyQueue: Queue<ProcessInput> = LinkedList()
        val inQueue = mutableSetOf<Int>()

        var currentTime = 0
        var completedCount = 0
        val totalProcesses = processes.size
        val maxTimeLimit = processes.sumOf { it.burstTime } + processes.maxOf { it.arrivalTime } + 100

        fun checkAndEnqueue(time: Int) {
            for (proc in sortedByArrival) {
                if (proc.arrivalTime <= time && remainingTime.getValue(proc.id) > 0 && proc.id !in inQueue) {
                    readyQueue.add(proc)
                    inQueue.add(proc.id)
                }
            }
        }

        checkAndEnqueue(currentTime)

        while (completedCount < totalProcesses && currentTime < maxTimeLimit) {
            if (readyQueue.isEmpty()) {
                val uncompleted = processes.filter { remainingTime.getValue(it.id) > 0 }
                if (uncompleted.isEmpty()) break
                val nextArrival = uncompleted.minOf { it.arrivalTime }
                ganttBlocks.add(
                    GanttBlock(
                        processId = null,
                        processName = "Idle",
                        startTime = currentTime,
                        endTime = nextArrival,
                        isIdle = true
                    )
                )
                currentTime = nextArrival
                checkAndEnqueue(currentTime)
                continue
            }

            val currentProc = readyQueue.poll()!!
            inQueue.remove(currentProc.id)

            val rem = remainingTime.getValue(currentProc.id)
            val execTime = minOf(rem, timeQuantum)
            val startTime = currentTime
            currentTime += execTime
            remainingTime[currentProc.id] = rem - execTime

            ganttBlocks.add(
                GanttBlock(
                    processId = currentProc.id,
                    processName = currentProc.name,
                    startTime = startTime,
                    endTime = currentTime,
                    isIdle = false,
                    colorHex = currentProc.colorHex
                )
            )

            for (proc in sortedByArrival) {
                if (proc.id != currentProc.id && proc.arrivalTime <= currentTime && remainingTime.getValue(proc.id) > 0 && proc.id !in inQueue) {
                    readyQueue.add(proc)
                    inQueue.add(proc.id)
                }
            }

            if (remainingTime.getValue(currentProc.id) > 0) {
                readyQueue.add(currentProc)
                inQueue.add(currentProc.id)
            } else {
                completedCount++
                completionTimes[currentProc.id] = currentTime
            }
        }

        val results = processes.map { proc ->
            val ct = completionTimes.getValue(proc.id)
            val tat = ct - proc.arrivalTime
            val wt = tat - proc.burstTime
            ProcessResult(proc, ct, tat, wt)
        }

        return buildResult(SchedulingAlgorithm.ROUND_ROBIN, timeQuantum, processes, results, ganttBlocks)
    }

    private fun mergeTimelineToGantt(timeline: List<Pair<Int, ProcessInput?>>): List<GanttBlock> {
        if (timeline.isEmpty()) return emptyList()

        val ganttBlocks = mutableListOf<GanttBlock>()
        var currentProc = timeline[0].second
        var startTime = timeline[0].first

        for (i in 1 until timeline.size) {
            val (time, proc) = timeline[i]
            if (proc?.id != currentProc?.id) {
                val endTime = time
                if (currentProc == null) {
                    ganttBlocks.add(
                        GanttBlock(
                            processId = null,
                            processName = "Idle",
                            startTime = startTime,
                            endTime = endTime,
                            isIdle = true
                        )
                    )
                } else {
                    ganttBlocks.add(
                        GanttBlock(
                            processId = currentProc.id,
                            processName = currentProc.name,
                            startTime = startTime,
                            endTime = endTime,
                            isIdle = false,
                            colorHex = currentProc.colorHex
                        )
                    )
                }
                currentProc = proc
                startTime = time
            }
        }

        val endTime = timeline.last().first + 1
        if (currentProc == null) {
            ganttBlocks.add(
                GanttBlock(
                    processId = null,
                    processName = "Idle",
                    startTime = startTime,
                    endTime = endTime,
                    isIdle = true
                )
            )
        } else {
            ganttBlocks.add(
                GanttBlock(
                    processId = currentProc.id,
                    processName = currentProc.name,
                    startTime = startTime,
                    endTime = endTime,
                    isIdle = false,
                    colorHex = currentProc.colorHex
                )
            )
        }

        return ganttBlocks
    }

    private fun buildResult(
        algorithm: SchedulingAlgorithm,
        timeQuantum: Int,
        originalProcesses: List<ProcessInput>,
        results: List<ProcessResult>,
        ganttBlocks: List<GanttBlock>
    ): AlgorithmResult {
        val totalIdle = ganttBlocks.filter { it.isIdle }.sumOf { it.duration }
        val totalExecutionTime = ganttBlocks.lastOrNull()?.endTime ?: 0
        val avgWT = if (results.isNotEmpty()) results.map { it.waitingTime }.average() else 0.0
        val avgTAT = if (results.isNotEmpty()) results.map { it.turnaroundTime }.average() else 0.0
        val cpuUtilization = if (totalExecutionTime > 0) {
            ((totalExecutionTime - totalIdle).toDouble() / totalExecutionTime.toDouble()) * 100.0
        } else 0.0

        val sortedResults = originalProcesses.map { input ->
            results.first { it.process.id == input.id }
        }

        return AlgorithmResult(
            algorithm = algorithm,
            timeQuantum = timeQuantum,
            processResults = sortedResults,
            ganttBlocks = ganttBlocks,
            averageWaitingTime = roundDouble(avgWT),
            averageTurnaroundTime = roundDouble(avgTAT),
            totalIdleTime = totalIdle,
            totalExecutionTime = totalExecutionTime,
            cpuUtilization = roundDouble(cpuUtilization)
        )
    }

    private fun roundDouble(value: Double): Double {
        return round(value * 100.0) / 100.0
    }
}
