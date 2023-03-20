package fr.uge.plutus.frontend.view.book

import androidx.compose.runtime.Composable
import fr.uge.plutus.frontend.store.globalState


@Composable
fun BookOverview() {
    val globalState = globalState()
    val currentBook = globalState.currentBook

    assert(currentBook != null) { "No book selected" }


}