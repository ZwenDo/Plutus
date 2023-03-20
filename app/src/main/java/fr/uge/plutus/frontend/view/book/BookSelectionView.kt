package fr.uge.plutus.frontend.view.book

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Database
import fr.uge.plutus.frontend.component.common.Loading
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.View
import fr.uge.plutus.ui.theme.PlutusTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookSelectionView() {
    var loaded by rememberSaveable { mutableStateOf(false) }
    var books by rememberSaveable { mutableStateOf(emptyList<Book>()) }

    if (!loaded) {
        Loading(false) {
            books = Database.books().getAll()
            loaded = true
        }
        return
    }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(8.dp)
            .scrollable(rememberScrollState(), orientation = Orientation.Vertical),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = true
    ) {
        items(books) { book ->
            BookSelectionItem(book)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookSelectionItem(book: Book) {
    val globalState = globalState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                globalState.currentBook = book
                globalState.currentView = View.TRANSACTION_LIST
            }
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(book.name)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun BookSelectionViewPreview() {
    Database.init(LocalContext.current)
    PlutusTheme {
        BookSelectionView()
    }
}
