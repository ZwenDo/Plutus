package fr.uge.plutus.frontend.component.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun PillText(caption: String) {
    Text(
        text = caption,
        modifier = Modifier.padding(7.dp),
        fontSize = 20.sp
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DisplayPill(
    caption: String,
    color: Color = MaterialTheme.colors.background,
    clickHandler: () -> Unit
) {
    Surface(
        border = BorderStroke(1.dp, Color.Gray),
        elevation = 3.dp,
        shape = RoundedCornerShape(20.dp),
        onClick = clickHandler,
        color = color
    ) {
        PillText(caption)
    }
}

@Composable
fun DisplayPill(
    caption: String,
    color: Color = MaterialTheme.colors.background
) {
    Surface(
        border = BorderStroke(1.dp, Color.Gray),
        elevation = 3.dp,
        shape = RoundedCornerShape(20.dp),
        color = color
    ) {
        PillText(caption)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val captions = listOf("Food", "Transport", "Sport", "Shopping", "Other")
    DisplayPill(captions[0])
}
