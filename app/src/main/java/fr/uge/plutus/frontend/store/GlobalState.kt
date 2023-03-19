package fr.uge.plutus.frontend.store

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Transaction


private object GlobalContext {
    lateinit var globalState: GlobalState
}

interface GlobalState {
    var currentBook: Book?
    var currentTransaction: Transaction?
}

@Composable
fun initGlobalState(): GlobalState {
    GlobalContext.globalState = object : GlobalState {
        override var currentBook: Book? by rememberSaveable { mutableStateOf(null) }
        override var currentTransaction: Transaction? by rememberSaveable { mutableStateOf(null) }
    }

    return GlobalContext.globalState
}

@Composable
fun globalState(): GlobalState = GlobalContext.globalState