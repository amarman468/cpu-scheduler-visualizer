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
import androidx.compose.material.icons.filled.EmojiEvents
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

@Composable
fun ComparisonScreen(
    processes: List<ProcessInput>,
    selectedAlgorithms: Set<SchedulingAlgorithm>,
    onAlgorithmsChanged: (Set<SchedulingAlgorithm>) -> Unit,
    timeQuantumText: String,
    onTimeQuantumChanged: (String) -> Unit,
    onAddProcess: (ProcessInput) -> Unit,
    onUpdateProcess: (ProcessInput) -> Unit = {},
    onDeleteProcess: (Int) -> Unit,
    onLoadPreset: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeQuantum = timeQuantumText.toIntOrNull()?.coerceAtLeast(1) ?: 2

    val results = remember(selectedAlgorithms, processes, timeQuantum) {
        selectedAlgorithms.map { algo ->
            CpuSchedulerEngine.solve(algo, processes, timeQuantum)
        }
    }

    val bestAlgorithm = remember(results) {
        if (results.isEmpty() || processes.isEmpty()) null
        else results.minByOrNull { it.averageWaitingTime }
    }

    val verticalScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(verticalScroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Multi-Algorithm Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Select Algorithms for Comparison",
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
                    val isSelected = selectedAlgorithms.contains(algo)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newSet = if (isSelected) {
                                if (selectedAlgorithms.size > 1) selectedAlgorithms - algo else selectedAlgorithms
                            } else {
                                selectedAlgorithms + algo
                            }
                            onAlgorithmsChanged(newSet)
                        },
                        label = { Text(algo.shortName, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            AnimatedVisibility(visible = selectedAlgorithms.any { it.requiresQuantum }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Time Quantum (Round Robin):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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

        // Shared Process Input Form
        ProcessInputSection(
            processes = processes,
            showPriority = selectedAlgorithms.any { it.requiresPriority },
            onAddProcess = onAddProcess,
            onUpdateProcess = onUpdateProcess,
            onDeleteProcess = onDeleteProcess,
            onLoadPreset = onLoadPreset,
            onClearAll = onClearAll
        )

        // Best Algorithm Winner Banner
        if (bestAlgorithm != null && processes.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Winner",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            text = "Best Performing Algorithm: ${bestAlgorithm.algorithm.displayName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Lowest Avg Waiting Time: ${bestAlgorithm.averageWaitingTime} ms | CPU Utilization: ${bestAlgorithm.cpuUtilization}%",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Comparative Bar Chart
        if (processes.isNotEmpty()) {
            BarChartComparisonView(results = results)
        }

        // Comparative Summary Table
        if (processes.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Comparative Metrics Summary Table",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Algorithm", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                    Text("Avg WT", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.9f))
                    Text("Avg TAT", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.9f))
                    Text("Idle Time", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.9f))
                    Text("CPU Util", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.9f))
                }

                Spacer(modifier = Modifier.height(4.dp))

                results.forEach { res ->
                    val isBest = bestAlgorithm?.algorithm == res.algorithm
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isBest) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            res.algorithm.shortName,
                            fontWeight = if (isBest) FontWeight.ExtraBold else FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text("${res.averageWaitingTime} ms", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(0.9f))
                        Text("${res.averageTurnaroundTime} ms", fontSize = 13.sp, modifier = Modifier.weight(0.9f))
                        Text("${res.totalIdleTime} ms", fontSize = 13.sp, modifier = Modifier.weight(0.9f))
                        Text("${res.cpuUtilization}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f))
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }

        // Comparative Stacked Gantt Charts
        if (processes.isNotEmpty()) {
            Text(
                text = "Stacked Gantt Chart Comparisons",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            results.forEach { res ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = res.algorithm.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    GanttChartView(ganttBlocks = res.ganttBlocks)
                }
            }
        }
    }
}
