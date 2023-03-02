package fr.uge.plutus.frontend.view.book

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import fr.uge.plutus.ui.theme.PlutusTheme

@Composable
fun BookSelectionView(onChosen: (Book) -> Unit) {
    var loaded by rememberSaveable { mutableStateOf(false) }
    var books by rememberSaveable { mutableStateOf(emptyList<Book>()) }

    if (!loaded) {
        Loading(false) {
            books = Database.books().getAll()
            loaded = true
        }
        return
    }

    LazyColumn {
        items(books) { book ->
            BookSelectionItem(book, onChosen)
        }
    }
}

@Composable
fun BookSelectionItem(book: Book, onChosen: (Book) -> Unit) {
    Row(
        modifier = Modifier
            .border(1.dp, Color.Black)
            .padding(8.dp)
            .clickable { onChosen(book) },
        horizontalArrangement = Arrangement.Center
    ) {
        Text(book.name!!)
    }
}

@Composable
@Preview(showBackground = true)
fun BookSelectionViewPreview() {
    Database.init(LocalContext.current)
    PlutusTheme {
        BookSelectionView {
            Log.d("BookSelectionView", "Chosen: $it")
        }
    }
}