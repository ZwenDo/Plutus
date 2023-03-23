package fr.uge.plutus.frontend.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun <K : Comparable<K>> BarChart(values: Map<K, Double>) {
    Box(Modifier.fillMaxWidth()) {
        LazyRow(
            Modifier
                .fillMaxWidth()
                .scrollable(rememberScrollState(), Orientation.Horizontal)
        ) {
            items(values.entries.sortedBy { it.key }.toList()) { (label, value) ->
                Spacer(modifier = Modifier.width(10.dp))
                BarChartBar(label.toString(), value, values.values.maxOf { abs(it) })
            }
        }
    }
}


const val MAX_BAR_HEIGHT = 200

@Composable
fun BarChartBar(label: String, value: Double, maxValue: Double) {
    val heightMultiplier = MAX_BAR_HEIGHT / maxValue
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(20.dp)
    ) {
        Column(Modifier.height(height = MAX_BAR_HEIGHT.dp)) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height((abs(value) * heightMultiplier).dp)
                    .background(if (value > 0) Color.Green else Color.Red)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = label)
    }
}


@Preview(showBackground = true)
@Composable
fun BarChartPreview() {
    BarChart(
        values = mapOf(
            "1" to 10.0,
            "2" to -20.0,
            "3" to 60.0,
            "4" to -40.0,
            "5" to 50.0,
            "6" to -60.0,
        )
    )
}

@Preview(showBackground = true)
@Composable
fun BarChartPreview2() {
    BarChart(
        values = mapOf(
            "1" to -10.0,
        )
    )
}

@Preview(showBackground = true)
@Composable
fun BarChartBarPreview() {
    BarChartBar(label = "1", value = 100.0, maxValue = 100.0)
}
