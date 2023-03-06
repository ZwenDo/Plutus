package fr.uge.plutus.frontend.component.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.runBlocking

@Composable
fun Loading(animation: Boolean = true, callBack: suspend () -> Unit) {
    LaunchedEffect(true) {
        callBack()
    }

    if (animation) {
        Box(
            Modifier.fillMaxSize(),
            Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.Black)
        }
    }
}
