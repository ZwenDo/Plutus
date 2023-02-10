package fr.uge.plutus.frontend.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainView() {
    TransactionCreationView()
}
