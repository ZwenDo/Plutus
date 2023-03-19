package fr.uge.plutus.frontend.component.scaffold

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import fr.uge.plutus.frontend.store.initGlobalState
import fr.uge.plutus.frontend.view.View


@Composable
fun PlutusScaffold(content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        bottomBar = { Navbar() },
        content = content
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
        PlutusScaffold {
            Column {
                Text(text = "Hello World!")
            }
        }
    }
}