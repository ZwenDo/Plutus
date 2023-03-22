package fr.uge.plutus.frontend.view.book

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.uge.plutus.R
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Database
import fr.uge.plutus.frontend.component.common.Loading
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.View

@Composable
fun BookSelectionView() {
    var loaded by rememberSaveable { mutableStateOf(false) }
    var books by rememberSaveable { mutableStateOf(emptyList<Book>()) }

    if (!loaded) {
        Loading(false) {
            books = Database.books().findAll()
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

@Composable
fun BookSelectionItem(book: Book) {
    val globalState = globalState()
    var exporting by rememberSaveable { mutableStateOf(ImportExportState.NONE) }

    if (exporting.isNotNone) {
        ImportExportModal(
            book,
            isImport = exporting.isImport
        ) {
            exporting = ImportExportState.NONE
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier
                .scale(0.8f)
                .border(1.dp, Color.Black, CircleShape),
            onClick = {
                exporting = ImportExportState.EXPORT
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.file_upload),
                contentDescription = stringResource(R.string.export)
            )
        }
        IconButton(
            modifier = Modifier
                .scale(0.8f)
                .border(1.dp, Color.Black, CircleShape),
            onClick = {
                exporting = ImportExportState.IMPORT
            }) {
            Icon(
                painter = painterResource(id = R.drawable.file_download),
                contentDescription = stringResource(R.string.import_book)
            )

        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    globalState.currentBook = book
                    globalState.currentView = View.TRANSACTION_LIST
                }
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                .padding(8.dp),
        ) {
            Text(book.name)
        }
    }
}

private enum class ImportExportState {
    NONE,
    IMPORT,
    EXPORT,
    ;

    val isImport
        get() = this === IMPORT
    val isNotNone
        get() = this !== NONE
}