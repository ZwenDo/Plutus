package fr.uge.plutus.util

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
@SuppressLint("ComposableNaming")
fun useDoOnceOnMounted(block: () -> Unit) {
    var mounted by rememberSaveable { mutableStateOf(false) }
    if (!mounted) {
        mounted = true
        block()
    }
}
