package fr.uge.plutus.frontend.store

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.view.View

private lateinit var globalState: GlobalState

interface GlobalState {
    var currentBook: Book?
    var currentTransaction: Transaction?
    var currentView: View
    var writeExternalStoragePermission: Boolean
    var locationPermission: Boolean
}

@Composable
fun initGlobalState(): GlobalState {
    val context = LocalContext.current
    globalState = object : GlobalState {
        override var currentBook: Book? by rememberSaveable { mutableStateOf(null) }
        override var currentTransaction: Transaction? by rememberSaveable { mutableStateOf(null) }
        override var currentView: View by rememberSaveable { mutableStateOf(View.BOOK_SELECTION) }
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
    }

    return globalState
}

@Composable
fun globalState(): GlobalState = globalState
