package com.oslab.cpuscheduler.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oslab.cpuscheduler.domain.model.ProcessInput
import com.oslab.cpuscheduler.domain.model.SchedulingAlgorithm
import com.oslab.cpuscheduler.ui.screens.ComparisonScreen
import com.oslab.cpuscheduler.ui.screens.VisualizationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    // Shared State elevated to MainScreen to retain changes across tab navigation
    var processes by remember { mutableStateOf<List<ProcessInput>>(ProcessInput.generateIdleTimePreset()) }
    var timeQuantumText by remember { mutableStateOf("2") }
    var selectedAlgorithm by remember { mutableStateOf(SchedulingAlgorithm.FCFS) }
    var selectedComparisonAlgorithms by remember {
        mutableStateOf(
            setOf(
                SchedulingAlgorithm.FCFS,
                SchedulingAlgorithm.SJF,
                SchedulingAlgorithm.SRTF,
                SchedulingAlgorithm.ROUND_ROBIN
            )
        )
    }

    val handleAddProcess: (ProcessInput) -> Unit = { newProc ->
        processes = processes + newProc
    }

    val handleUpdateProcess: (ProcessInput) -> Unit = { updatedProc ->
        processes = processes.map { if (it.id == updatedProc.id) updatedProc else it }
    }

    val handleDeleteProcess: (Int) -> Unit = { idToDelete ->
        processes = processes.filter { it.id != idToDelete }
    }

    val handleClearAll: () -> Unit = {
        processes = emptyList()
    }

    val handleLoadPreset: (String) -> Unit = { presetType ->
        processes = if (presetType == "idle") {
            ProcessInput.generateIdleTimePreset()
        } else {
            ProcessInput.generateRandomProcesses()
        }
        timeQuantumText = "${kotlin.random.Random.nextInt(1, 6)}"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "CPU Scheduler Visualizer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row Navigation
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Module 1: Visualization", fontWeight = FontWeight.Bold)
                        }
                    }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Module 2: Comparative Analysis", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Screen Content (Retains State & UI across tab switches)
            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedTab == 0) {
                    VisualizationScreen(
                        processes = processes,
                        selectedAlgorithm = selectedAlgorithm,
                        onAlgorithmSelected = { selectedAlgorithm = it },
                        timeQuantumText = timeQuantumText,
                        onTimeQuantumChanged = { timeQuantumText = it },
                        onAddProcess = handleAddProcess,
                        onUpdateProcess = handleUpdateProcess,
                        onDeleteProcess = handleDeleteProcess,
                        onLoadPreset = handleLoadPreset,
                        onClearAll = handleClearAll
                    )
                }

                if (selectedTab == 1) {
                    ComparisonScreen(
                        processes = processes,
                        selectedAlgorithms = selectedComparisonAlgorithms,
                        onAlgorithmsChanged = { selectedComparisonAlgorithms = it },
                        timeQuantumText = timeQuantumText,
                        onTimeQuantumChanged = { timeQuantumText = it },
                        onAddProcess = handleAddProcess,
                        onUpdateProcess = handleUpdateProcess,
                        onDeleteProcess = handleDeleteProcess,
                        onLoadPreset = handleLoadPreset,
                        onClearAll = handleClearAll
                    )
                }
            }
        }
    }
}
