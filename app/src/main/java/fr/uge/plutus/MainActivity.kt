package fr.uge.plutus

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import fr.uge.plutus.backend.Database
import fr.uge.plutus.frontend.store.GlobalState
import fr.uge.plutus.frontend.store.initGlobalState
import fr.uge.plutus.frontend.view.MainView
import fr.uge.plutus.ui.theme.PlutusTheme

class MainActivity : ComponentActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var globalState: GlobalState


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Database.isInitialized) {
            Database.init(this)
        }

        writeExternalStoragePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    globalState.writeExternalStoragePermission = true
                }
            }

        setContent {
            PlutusTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    globalState = initGlobalState()
                    MainView()
                }
            }
        }
    }

    companion object {

        private lateinit var writeExternalStoragePermissionLauncher: ActivityResultLauncher<String>

        fun requestWriteExternalStoragePermission() =
            writeExternalStoragePermissionLauncher.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

    }

}
