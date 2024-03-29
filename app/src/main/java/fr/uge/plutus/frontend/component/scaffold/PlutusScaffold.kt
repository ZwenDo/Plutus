package fr.uge.plutus.frontend.component.scaffold

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.store.initGlobalState
import fr.uge.plutus.frontend.view.View


@Composable
fun PlutusScaffold() {
    val globalState = globalState()

    Scaffold(
        scaffoldState = globalState.scaffoldState,
        topBar = globalState.currentView.headerComponent,
        floatingActionButton = globalState.currentView.fabComponent,
        bottomBar = { Navbar() },
        content = { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp)
            ) {
                globalState.currentView.contentComponent(padding)
            }
        },
        drawerContent = globalState.currentView.drawerComponent,
        drawerGesturesEnabled = true,
    )
}

@Preview(showBackground = true)
@Composable
fun ScaffoldPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        initGlobalState().currentView = View.BOOK_SELECTION
        PlutusScaffold()
    }
}