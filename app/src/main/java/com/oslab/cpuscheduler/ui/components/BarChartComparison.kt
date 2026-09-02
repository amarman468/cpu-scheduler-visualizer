package com.oslab.cpuscheduler.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oslab.cpuscheduler.domain.model.AlgorithmResult

@Composable
fun BarChartComparisonView(
    results: List<AlgorithmResult>,
    modifier: Modifier = Modifier
) {
    if (results.isEmpty()) return

    val maxVal = maxOf(
        1.0,
        results.maxOf { maxOf(it.averageWaitingTime, it.averageTurnaroundTime) }
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val labelColor = MaterialTheme.colorScheme.onSurface

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
                text = "Performance Metric Comparison",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(primaryColor))
                    Spacer(Modifier.width(4.dp))
                    Text("Avg WT", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(secondaryColor))
                    Spacer(Modifier.width(4.dp))
                    Text("Avg TAT", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val barGroupWidth = width / results.size
                val singleBarWidth = (barGroupWidth * 0.3f).coerceAtMost(36f)

                results.forEachIndexed { index, res ->
                    val groupStartX = index * barGroupWidth + (barGroupWidth - singleBarWidth * 2 - 8f) / 2

                    // Avg WT Bar
                    val wtHeight = ((res.averageWaitingTime / maxVal) * (height - 30f)).toFloat()
                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(groupStartX, height - wtHeight - 20f),
                        size = Size(singleBarWidth, wtHeight)
                    )

                    // Avg TAT Bar
                    val tatHeight = ((res.averageTurnaroundTime / maxVal) * (height - 30f)).toFloat()
                    drawRect(
                        color = secondaryColor,
                        topLeft = Offset(groupStartX + singleBarWidth + 8f, height - tatHeight - 20f),
                        size = Size(singleBarWidth, tatHeight)
                    )

                    // Baseline
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, height - 20f),
                        end = Offset(width, height - 20f),
                        strokeWidth = 2f
                    )
                }
            }
        }

        // X-Axis Algorithm Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            results.forEach { res ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = res.algorithm.shortName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = labelColor
                    )
                    Text(
                        text = "WT: ${res.averageWaitingTime} | TAT: ${res.averageTurnaroundTime}",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
