package fr.uge.plutus.frontend.store

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.view.View

private lateinit var globalState: GlobalState

interface GlobalState {
    var currentBook: Book?
    var currentTransaction: Transaction?
    var currentView: View
}

@Composable
fun initGlobalState(): GlobalState {
    @RequiresApi(Build.VERSION_CODES.O)
    globalState = object : GlobalState {
        override var currentBook: Book? by rememberSaveable { mutableStateOf(null) }
        override var currentTransaction: Transaction? by rememberSaveable { mutableStateOf(null) }
        override var currentView: View by rememberSaveable { mutableStateOf(View.TRANSACTION_SEARCH) }
    }

    return globalState
}

@Composable
fun globalState(): GlobalState = globalState
