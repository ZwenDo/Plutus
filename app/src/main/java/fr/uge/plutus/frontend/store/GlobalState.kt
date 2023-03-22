package fr.uge.plutus.frontend.store

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.view.View

private lateinit var globalState: GlobalState

interface GlobalState {
    var currentBook: Book?
    var currentTransaction: Transaction?
    var currentView: View
    var scaffoldState: ScaffoldState
}

@Composable
fun initGlobalState(): GlobalState {
    val scaffoldState = rememberScaffoldState()
    @RequiresApi(Build.VERSION_CODES.O)
    globalState = object : GlobalState {
        override var currentBook: Book? by rememberSaveable { mutableStateOf(null) }
        override var currentTransaction: Transaction? by rememberSaveable { mutableStateOf(null) }
        override var currentView: View by rememberSaveable { mutableStateOf(View.TRANSACTION_SEARCH) }
        override var scaffoldState: ScaffoldState by remember { mutableStateOf(scaffoldState) }
    }

    return globalState
}

@Composable
fun globalState(): GlobalState = globalState
