package fr.uge.plutus.frontend.component.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun PillText(caption: String) {
    Text(
        text = caption,
        modifier = Modifier.padding(10.dp, 2.dp),
        fontSize = 16.sp
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DisplayPill(
    caption: String,
    clickHandler: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        onClick = clickHandler,
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
    ) {
        PillText(caption)
    }
}

@Composable
fun DisplayPill(
    caption: String,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
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
