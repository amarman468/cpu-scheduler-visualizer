package com.oslab.cpuscheduler

import com.oslab.cpuscheduler.domain.engine.CpuSchedulerEngine
import com.oslab.cpuscheduler.domain.model.ProcessInput
import com.oslab.cpuscheduler.domain.model.SchedulingAlgorithm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CpuSchedulerEngineTest {

    private val sampleProcesses = listOf(
        ProcessInput(id = 1, name = "P1", arrivalTime = 0, burstTime = 7, priority = 3),
        ProcessInput(id = 2, name = "P2", arrivalTime = 2, burstTime = 4, priority = 1),
        ProcessInput(id = 3, name = "P3", arrivalTime = 4, burstTime = 1, priority = 4),
        ProcessInput(id = 4, name = "P4", arrivalTime = 5, burstTime = 4, priority = 2)
    )

    @Test
    fun testFCFS() {
        val result = CpuSchedulerEngine.solve(SchedulingAlgorithm.FCFS, sampleProcesses)
        
        assertEquals(4, result.processResults.size)
        // CTs: P1=7, P2=11, P3=12, P4=16
        val p1 = result.processResults.first { it.process.id == 1 }
        val p2 = result.processResults.first { it.process.id == 2 }
        val p3 = result.processResults.first { it.process.id == 3 }
        val p4 = result.processResults.first { it.process.id == 4 }

        assertEquals(7, p1.completionTime)
        assertEquals(11, p2.completionTime)
        assertEquals(12, p3.completionTime)
        assertEquals(16, p4.completionTime)

        assertEquals(0, p1.waitingTime)
        assertEquals(5, p2.waitingTime)
        assertEquals(7, p3.waitingTime)
        assertEquals(7, p4.waitingTime)

        assertEquals(4.75, result.averageWaitingTime, 0.01)
        assertEquals(8.75, result.averageTurnaroundTime, 0.01)
        assertEquals(0, result.totalIdleTime)
        assertEquals(100.0, result.cpuUtilization, 0.01)
    }

    @Test
    fun testSJFNonPreemptive() {
        val result = CpuSchedulerEngine.solve(SchedulingAlgorithm.SJF, sampleProcesses)

        val p1 = result.processResults.first { it.process.id == 1 }
        val p2 = result.processResults.first { it.process.id == 2 }
        val p3 = result.processResults.first { it.process.id == 3 }
        val p4 = result.processResults.first { it.process.id == 4 }

        assertEquals(7, p1.completionTime)
        assertEquals(8, p3.completionTime)
        assertEquals(12, p2.completionTime)
        assertEquals(16, p4.completionTime)

        assertEquals(4.0, result.averageWaitingTime, 0.01)
        assertEquals(8.0, result.averageTurnaroundTime, 0.01)
    }

    @Test
    fun testSRTFPreemptive() {
        val result = CpuSchedulerEngine.solve(SchedulingAlgorithm.SRTF, sampleProcesses)

        val p1 = result.processResults.first { it.process.id == 1 }
        val p2 = result.processResults.first { it.process.id == 2 }
        val p3 = result.processResults.first { it.process.id == 3 }
        val p4 = result.processResults.first { it.process.id == 4 }

        assertEquals(16, p1.completionTime)
        assertEquals(7, p2.completionTime)
        assertEquals(5, p3.completionTime)
        assertEquals(11, p4.completionTime)

        assertEquals(3.0, result.averageWaitingTime, 0.01)
        assertEquals(7.0, result.averageTurnaroundTime, 0.01)
    }

    @Test
    fun testPriorityNonPreemptive() {
        val result = CpuSchedulerEngine.solve(SchedulingAlgorithm.PRIORITY_NP, sampleProcesses)

        val p1 = result.processResults.first { it.process.id == 1 }
        val p2 = result.processResults.first { it.process.id == 2 }
        val p3 = result.processResults.first { it.process.id == 3 }
        val p4 = result.processResults.first { it.process.id == 4 }

        assertEquals(7, p1.completionTime)
        assertEquals(11, p2.completionTime)
        assertEquals(15, p4.completionTime)
        assertEquals(16, p3.completionTime)
    }

    @Test
    fun testPriorityPreemptive() {
        val result = CpuSchedulerEngine.solve(SchedulingAlgorithm.PRIORITY_P, sampleProcesses)

        val p1 = result.processResults.first { it.process.id == 1 }
        val p2 = result.processResults.first { it.process.id == 2 }

        // P2 has priority 1, so it preempts P1 at t=2
        assertEquals(6, p2.completionTime)
        assertTrue(p1.completionTime > p2.completionTime)
    }

    @Test
    fun testRoundRobin() {
        val result = CpuSchedulerEngine.solve(SchedulingAlgorithm.ROUND_ROBIN, sampleProcesses, timeQuantum = 2)

        assertEquals(16, result.totalExecutionTime)
        assertEquals(0, result.totalIdleTime)
        assertTrue(result.ganttBlocks.size > 4) // Context switching creates multiple Gantt blocks
    }

    @Test
    fun testCPUIdleTimeHandling() {
        val processesWithGap = listOf(
            ProcessInput(id = 1, name = "P1", arrivalTime = 2, burstTime = 3)
        )
        val result = CpuSchedulerEngine.solve(SchedulingAlgorithm.FCFS, processesWithGap)

        assertEquals(2, result.totalIdleTime)
        assertEquals(5, result.totalExecutionTime)
        assertEquals(1, result.ganttBlocks.size) // 1 idle + 1 process block = idle block plus process block
        assertTrue(result.ganttBlocks.any { it.isIdle && it.duration == 2 })
    }
}
