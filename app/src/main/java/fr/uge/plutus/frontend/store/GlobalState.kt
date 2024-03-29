package fr.uge.plutus.frontend.store

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.view.View
import fr.uge.plutus.frontend.view.book.ImportExportState

private lateinit var globalState: GlobalState

interface GlobalState {
    var currentBook: Book?
    var currentTransaction: Transaction?
    var currentView: View
    var writeExternalStoragePermission: Boolean
    var scaffoldState: ScaffoldState
    var globalFilters: GlobalFilters
    var globalSorting: GlobalSorting?
    var importExportState: ImportExportState
    var deletingBook: Boolean
    var duplicatingBook: Boolean
    var deletingTransaction: Boolean
    var displaySorting: Boolean
    var locationPermission: Boolean
    var mustRefetchTransactions: Boolean
    var duplicatingTransaction: Boolean
    var currentTransactions: List<Transaction>
}

@Composable
fun initGlobalState(): GlobalState {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    globalState = object : GlobalState {
        override var currentBook: Book? by rememberSaveable { mutableStateOf(null) }
        override var currentTransaction: Transaction? by rememberSaveable { mutableStateOf(null) }
        override var currentView: View by rememberSaveable { mutableStateOf(View.BOOK_SELECTION) }
        override var scaffoldState: ScaffoldState by remember { mutableStateOf(scaffoldState) }
        override var globalFilters: GlobalFilters by remember { mutableStateOf(GlobalFilters.new()) }
        override var writeExternalStoragePermission: Boolean by rememberSaveable {
            val permission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
            mutableStateOf(permission == PackageManager.PERMISSION_GRANTED)
        }
        override var locationPermission: Boolean by rememberSaveable {
            val permission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
            mutableStateOf(permission == PackageManager.PERMISSION_GRANTED)
        }
        override var importExportState by rememberSaveable { mutableStateOf(ImportExportState.NONE) }
        override var deletingBook by rememberSaveable { mutableStateOf(false) }
        override var duplicatingBook by rememberSaveable { mutableStateOf(false) }
        override var deletingTransaction by rememberSaveable { mutableStateOf(false) }
        override var displaySorting by rememberSaveable { mutableStateOf(false) }
        override var globalSorting: GlobalSorting? by rememberSaveable { mutableStateOf(null) }
        override var mustRefetchTransactions by rememberSaveable { mutableStateOf(false) }
        override var duplicatingTransaction by rememberSaveable { mutableStateOf(false) }
        override var currentTransactions: List<Transaction> by rememberSaveable { mutableStateOf(emptyList()) }
    }

    return globalState
}

@Composable
fun globalState(): GlobalState = globalState
