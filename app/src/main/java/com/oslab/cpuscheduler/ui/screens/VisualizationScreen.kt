package com.oslab.cpuscheduler.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oslab.cpuscheduler.domain.engine.CpuSchedulerEngine
import com.oslab.cpuscheduler.domain.model.ProcessInput
import com.oslab.cpuscheduler.domain.model.SchedulingAlgorithm
import com.oslab.cpuscheduler.ui.components.*
import kotlinx.coroutines.delay

@Composable
fun VisualizationScreen(
    processes: List<ProcessInput>,
    selectedAlgorithm: SchedulingAlgorithm,
    onAlgorithmSelected: (SchedulingAlgorithm) -> Unit,
    timeQuantumText: String,
    onTimeQuantumChanged: (String) -> Unit,
    onAddProcess: (ProcessInput) -> Unit,
    onDeleteProcess: (Int) -> Unit,
    onLoadPreset: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeQuantum = timeQuantumText.toIntOrNull()?.coerceAtLeast(1) ?: 2

    // Playback state
    var isPlaying by remember { mutableStateOf(false) }
    var playbackStep by remember { mutableStateOf(0) }
    var playbackSpeedMs by remember { mutableStateOf(600L) }

    val result = remember(selectedAlgorithm, processes, timeQuantum) {
        CpuSchedulerEngine.solve(selectedAlgorithm, processes, timeQuantum)
    }

    val maxTime = result.totalExecutionTime

    // Playback coroutine loop
    LaunchedEffect(isPlaying, playbackStep, playbackSpeedMs, maxTime) {
        if (isPlaying) {
            if (playbackStep >= maxTime) {
                isPlaying = false
            } else {
                delay(playbackSpeedMs)
                playbackStep++
            }
        }
    }

    val verticalScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(verticalScroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Algorithm Picker Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Select Scheduling Algorithm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SchedulingAlgorithm.values().forEach { algo ->
                    FilterChip(
                        selected = selectedAlgorithm == algo,
                        onClick = {
                            onAlgorithmSelected(algo)
                            playbackStep = 0
                            isPlaying = false
                        },
                        label = { Text(algo.shortName, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            AnimatedVisibility(visible = selectedAlgorithm.requiresQuantum) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Time Quantum:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = timeQuantumText,
                        onValueChange = {
                            onTimeQuantumChanged(it.filter { c -> c.isDigit() })
                        },
                        modifier = Modifier.width(100.dp),
                        singleLine = true
                    )
                }
            }
        }

        // Process Input Section
        ProcessInputSection(
            processes = processes,
            showPriority = selectedAlgorithm.requiresPriority,
            onAddProcess = onAddProcess,
            onDeleteProcess = onDeleteProcess,
            onLoadPreset = onLoadPreset,
            onClearAll = onClearAll
        )

        // Step-by-Step Playback Controls
        if (result.ganttBlocks.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step-by-Step Execution Playback",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Time: ${playbackStep}s / ${maxTime}s",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (playbackStep >= maxTime) playbackStep = 0
                                isPlaying = !isPlaying
                            }
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play"
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (isPlaying) "Pause" else "Play Timeline")
                        }

                        IconButton(
                            onClick = {
                                isPlaying = false
                                playbackStep = 0
                            }
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = "Reset Timeline")
                        }
                    }

                    // Speed Toggles
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(600L to "1x", 300L to "2x", 150L to "4x").forEach { (speed, label) ->
                            FilterChip(
                                selected = playbackSpeedMs == speed,
                                onClick = { playbackSpeedMs = speed },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Gantt Chart View
        GanttChartView(
            ganttBlocks = result.ganttBlocks,
            activeTimeProgress = if (isPlaying || playbackStep > 0) playbackStep else null
        )

        // Summary Performance Metrics
        SummaryMetricsView(result = result)

        // Tabulated Process Results
        ProcessResultsTable(
            results = result.processResults,
            showPriority = selectedAlgorithm.requiresPriority
        )
    }
}
