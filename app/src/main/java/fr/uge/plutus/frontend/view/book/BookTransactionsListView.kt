package fr.uge.plutus.frontend.view.book

import android.widget.Toast
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Database
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.View
import fr.uge.plutus.frontend.view.transaction.TransactionListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private suspend fun deleteBook(book: Book) = withContext(Dispatchers.IO) {
    Database.books().delete(book)
}

@Composable
fun BookTransactionsListView() {
    val context = LocalContext.current
    val globalState = globalState()
    val coroutineScope = rememberCoroutineScope()
    val book = globalState.currentBook!!

    TransactionListView()

    val importExportState = globalState.importExportState
    if (importExportState.isNotNone) {
        ImportExportModal(
            globalState.currentBook!!,
            isImport = importExportState.isImport
        ) {
            globalState.importExportState = ImportExportState.NONE
        }
    }

    fun delete() {
        globalState.currentView = View.BOOK_SELECTION
        globalState.deletingBook = false
        coroutineScope.launch {
            deleteBook(book)
            Toast.makeText(context, "Book “${book.name}” deleted", Toast.LENGTH_SHORT).show()
            globalState.currentBook = null
        }
    }

    if (globalState.deletingBook) {
        AlertDialog(
            onDismissRequest = { globalState.deletingBook = false },
            title = {
                Text(
                    "Delete book “${book.name}”",
                    style = MaterialTheme.typography.h6
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete this book? This action cannot be undone.",
                    style = MaterialTheme.typography.body1
                )
            },
            confirmButton = {
                TextButton(onClick = { delete() }) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    globalState.deletingBook = false
                }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
