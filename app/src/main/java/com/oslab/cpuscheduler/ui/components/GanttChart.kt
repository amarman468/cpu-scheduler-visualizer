package com.oslab.cpuscheduler.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oslab.cpuscheduler.domain.model.GanttBlock

@Composable
fun GanttChartView(
    ganttBlocks: List<GanttBlock>,
    modifier: Modifier = Modifier,
    activeTimeProgress: Int? = null // For step-by-step playback simulation
) {
    if (ganttBlocks.isEmpty()) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Gantt chart data available.\nAdd processes to generate visualization.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val totalDuration = ganttBlocks.lastOrNull()?.endTime ?: 0
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Gantt Chart Execution Timeline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Scrollable Gantt Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 8.dp)
        ) {
            Column {
                // Blocks Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ganttBlocks.forEach { block ->
                        val blockWidth = maxOf(48, block.duration * 36).dp
                        val color = if (block.isIdle) {
                            Color(0xFF64748B) // Slate grey for idle
                        } else {
                            try {
                                Color(android.graphics.Color.parseColor(block.colorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                        }

                        val isActive = activeTimeProgress != null &&
                                activeTimeProgress >= block.startTime && activeTimeProgress < block.endTime

                        Box(
                            modifier = Modifier
                                .width(blockWidth)
                                .height(56.dp)
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isActive) color.copy(alpha = 0.9f) else color)
                                .border(
                                    width = if (isActive) 3.dp else 1.dp,
                                    color = if (isActive) MaterialTheme.colorScheme.tertiary else Color.White.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = block.processName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "(${block.duration}s)",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Time Ticks Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ganttBlocks.forEachIndexed { index, block ->
                        val blockWidth = maxOf(48, block.duration * 36).dp

                        if (index == 0) {
                            Text(
                                text = "${block.startTime}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier.width(blockWidth),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "${block.endTime}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
