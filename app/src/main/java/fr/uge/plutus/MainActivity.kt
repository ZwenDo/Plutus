package fr.uge.plutus

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.android.gms.location.LocationServices
import createNotificationChannel
import fr.uge.plutus.backend.Database
import fr.uge.plutus.frontend.component.scaffold.PlutusScaffold
import fr.uge.plutus.frontend.store.GlobalState
import fr.uge.plutus.frontend.store.initGlobalState
import fr.uge.plutus.ui.theme.PlutusTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import showSimpleNotification
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var globalState: GlobalState
    private var isInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isInitialized) {
            init()
        }
        setContent {
            PlutusTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    if (!isInitialized) {
                        globalState = initGlobalState()
                    }
                    LaunchedEffect(Unit) {
                        todoNotification(this@MainActivity)
                    }
                    PlutusScaffold()
                }
            }
        }
    }

    private suspend fun todoNotification(context: Context) {
        withContext(Dispatchers.IO) {
            val channelId = "Todo Plutus"
            val notificationId = 0
            val textTitle = "Transaction to do"
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val end = calendar.time
            createNotificationChannel(channelId, context)
            val transactions = Database.transactions().findAllTodoByDate(start, end)
            transactions.forEach {
                showSimpleNotification(context, channelId, notificationId, textTitle, it)
            }
        }
    }

    private fun init() {
        if (!Database.isInitialized) {
            Database.init(this)
        }

        writeExternalStoragePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    globalState.writeExternalStoragePermission = true
                }
            }

        locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        // Precise location access granted.
                        globalState.locationPermission = true
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        // Only approximate location access granted.
                        globalState.locationPermission = true
                    }
                    else -> {
                        // No location access granted.
                    }
                }
            }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    companion object {

        private lateinit var writeExternalStoragePermissionLauncher: ActivityResultLauncher<String>
        private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
        private lateinit var fusedLocationClient: FusedLocationProviderClient

        fun requestWriteExternalStoragePermission() =
            writeExternalStoragePermissionLauncher.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

        fun requestLocationPermission() =
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

        fun locationProvider() = fusedLocationClient
    }

}
