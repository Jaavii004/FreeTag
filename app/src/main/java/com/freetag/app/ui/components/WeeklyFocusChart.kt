package com.freetag.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freetag.app.data.DayFocusStat
import com.freetag.app.ui.theme.EmeraldDark
import com.freetag.app.ui.theme.EmeraldLight
import com.freetag.app.ui.theme.EmeraldPrimary
import com.freetag.app.ui.theme.SurfaceBorder
import com.freetag.app.ui.theme.SurfaceDark
import com.freetag.app.ui.theme.SurfaceElevated
import com.freetag.app.ui.theme.TextPrimary
import com.freetag.app.ui.theme.TextSecondary
import com.freetag.app.ui.theme.TextTertiary

@Composable
fun WeeklyFocusChart(
    stats: List<DayFocusStat>,
    modifier: Modifier = Modifier
) {
    val maxMinutes = (stats.maxOfOrNull { it.minutes } ?: 60).coerceAtLeast(30)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.BarChart,
                        contentDescription = null,
                        tint = EmeraldLight,
                        modifier = Modifier.height(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tiempo de Enfoque Semanal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                val totalWeekMin = stats.sumOf { it.minutes }
                Text(
                    text = "${totalWeekMin}m total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmeraldLight,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 7 Bar columns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                stats.forEach { stat ->
                    val barFraction = if (maxMinutes > 0) {
                        (stat.minutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.08f, 1f)
                    } else {
                        0.08f
                    }

                    val animatedFraction by animateFloatAsState(
                        targetValue = barFraction,
                        animationSpec = tween(durationMillis = 500),
                        label = "bar_anim"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Minutes text above bar
                        if (stat.minutes > 0) {
                            Text(
                                text = "${stat.minutes}m",
                                fontSize = 10.sp,
                                color = if (stat.isToday) EmeraldLight else TextTertiary,
                                fontWeight = if (stat.isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        } else {
                            Text(
                                text = "-",
                                fontSize = 10.sp,
                                color = TextTertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bar
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(80.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Background track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceElevated)
                            )

                            // Filled bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(animatedFraction)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (stat.isToday) {
                                            Brush.verticalGradient(listOf(EmeraldLight, EmeraldPrimary))
                                        } else if (stat.minutes > 0) {
                                            Brush.verticalGradient(listOf(EmeraldPrimary, EmeraldDark))
                                        } else {
                                            Brush.verticalGradient(listOf(SurfaceElevated, SurfaceElevated))
                                        }
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day label below
                        Text(
                            text = stat.dayLabel,
                            fontSize = 11.sp,
                            fontWeight = if (stat.isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (stat.isToday) EmeraldLight else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

