package com.oslab.cpuscheduler.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oslab.cpuscheduler.domain.model.ProcessInput
import com.oslab.cpuscheduler.domain.model.ProcessResult

@Composable
fun ProcessInputSection(
    processes: List<ProcessInput>,
    showPriority: Boolean,
    onAddProcess: (ProcessInput) -> Unit,
    onDeleteProcess: (Int) -> Unit,
    onLoadPreset: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var arrivalTimeText by remember { mutableStateOf("0") }
    var burstTimeText by remember { mutableStateOf("0") }
    var priorityText by remember { mutableStateOf("1") }

    Column(
        modifier = modifier
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
                text = "Process Input Parameters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { onLoadPreset("standard") }) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Preset Sample", fontSize = 12.sp)
                }
                IconButton(onClick = onClearAll) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Clear All",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = arrivalTimeText,
                onValueChange = { arrivalTimeText = it.filter { char -> char.isDigit() } },
                label = { Text("Arrival Time", fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = burstTimeText,
                onValueChange = { burstTimeText = it.filter { char -> char.isDigit() } },
                label = { Text("Burst Time", fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            AnimatedVisibility(visible = showPriority, modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = priorityText,
                    onValueChange = { priorityText = it.filter { char -> char.isDigit() } },
                    label = { Text("Priority", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Button(
                onClick = {
                    val at = arrivalTimeText.toIntOrNull() ?: 0
                    val bt = maxOf(1, burstTimeText.toIntOrNull() ?: 1)
                    val pri = maxOf(1, priorityText.toIntOrNull() ?: 1)
                    val nextId = (processes.maxOfOrNull { it.id } ?: 0) + 1

                    onAddProcess(
                        ProcessInput(
                            id = nextId,
                            name = "P$nextId",
                            arrivalTime = at,
                            burstTime = bt,
                            priority = pri
                        )
                    )
                },
                modifier = Modifier.padding(top = 6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Process")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Process list chips/items
        Text(
            text = "Active Processes (${processes.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (processes.isEmpty()) {
            Text(
                text = "No processes added yet. Enter AT/BT above or load sample preset.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                processes.forEach { proc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val color = try {
                                Color(android.graphics.Color.parseColor(proc.colorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = proc.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.width(16.dp))
                            Text("AT: ${proc.arrivalTime}", fontSize = 12.sp)
                            Spacer(Modifier.width(12.dp))
                            Text("BT: ${proc.burstTime}", fontSize = 12.sp)
                            if (showPriority) {
                                Spacer(Modifier.width(12.dp))
                                Text("Priority: ${proc.priority}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        IconButton(
                            onClick = { onDeleteProcess(proc.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessResultsTable(
    results: List<ProcessResult>,
    showPriority: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Tabulated Execution Results",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Process", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("AT", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
            Text("BT", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
            if (showPriority) {
                Text("Pri", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
            }
            Text("CT", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
            Text("TAT", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
            Text("WT", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Table Rows
        results.forEach { res ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(res.process.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text("${res.process.arrivalTime}", fontSize = 13.sp, modifier = Modifier.weight(0.8f))
                Text("${res.process.burstTime}", fontSize = 13.sp, modifier = Modifier.weight(0.8f))
                if (showPriority) {
                    Text("${res.process.priority}", fontSize = 13.sp, modifier = Modifier.weight(0.8f))
                }
                Text("${res.completionTime}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.8f))
                Text("${res.turnaroundTime}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(0.8f))
                Text("${res.waitingTime}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(0.8f))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}
