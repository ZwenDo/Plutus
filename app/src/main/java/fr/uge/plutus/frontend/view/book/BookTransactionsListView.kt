package fr.uge.plutus.frontend.view.book

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.uge.plutus.R
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Database
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.component.scaffold.Dialog
import fr.uge.plutus.frontend.store.SortField
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.store.replace
import fr.uge.plutus.frontend.view.View
import fr.uge.plutus.frontend.view.transaction.TransactionListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private suspend fun deleteBook(book: Book) = withContext(Dispatchers.IO) {
    Database.books().delete(book)
}

@Composable
fun BookTransactionsListView() {
    val context = LocalContext.current
    val globalState = globalState()
    val book = globalState.currentBook!!

    var delete by remember { mutableStateOf(false) }

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

    LaunchedEffect(delete) {
        if (delete) {
            globalState.currentView = View.BOOK_SELECTION
            globalState.deletingBook = false
            globalState.currentBook = null
            deleteBook(book)
            Toast.makeText(context, "Book “${book.name}” deleted", Toast.LENGTH_SHORT).show()
            delete = false
        }
    }

    if (globalState.duplicatingBook) {
        BookDuplicationDialog {
            globalState.duplicatingBook = false
        }
    }

    if (globalState.displaySorting) {
        TransactionSortingDialog {
            globalState.displaySorting = false
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
                TextButton(onClick = { delete = true }) {
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionSortingDialog(onDismiss: () -> Unit = {}) {
    val globalState = globalState()
    var currentSorting by remember { mutableStateOf(globalState.globalSorting) }

    Dialog(
        open = true,
        displayCancelButton = false,
        title = "Sorting",
        onClose = {
            if (it) {
                globalState.globalSorting = currentSorting
            }
            onDismiss()
        }
    ) {
        SortField.values().forEachIndexed { index, it ->
            Column {
                Surface(onClick = {
                    currentSorting = currentSorting.replace(it)
                }) {
                    Row(
                        Modifier.padding(24.dp, 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = it.displayName,
                        )

                        val current = currentSorting
                        if (current?.field == it) {
                            val icon = if (current.ascending) {
                                R.drawable.ascending_arrow
                            } else {
                                R.drawable.descending_arrow
                            }
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = null,
                            )
                        }
                    }
                }
                if (index != SortField.values().size - 1) {
                    Divider()
                }
            }
        }
    }
}

@Composable
fun BookDuplicationDialog(
    onDismiss: () -> Unit = {}
) {
    val globalState = globalState()
    var bookName by remember {
        mutableStateOf("${globalState.currentBook!!.name} (copy)")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var duplicateInProgress by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(duplicateInProgress) {
        if (!duplicateInProgress) return@LaunchedEffect

        val copy = Database.books().copy(globalState.currentBook!!, bookName)
        globalState.currentBook = copy
        globalState.duplicatingBook = false
        Toast.makeText(
            context,
            "Book “${copy.name}” created",
            Toast.LENGTH_SHORT
        ).show()

        duplicateInProgress = false
        onDismiss()
    }

    Dialog(
        open = true,
        title = "Duplicate book",
        submitButtonText = "DUPLICATE",
        onClose = {
            if (!it) {
                onDismiss()
                return@Dialog
            }
            if (bookName.isBlank()) {
                errorMessage = "Book name cannot be blank"
                return@Dialog
            }
            duplicateInProgress = true
        }
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp, 16.dp)
        ) {
            InputText(
                label = "New Book Name",
                value = bookName,
                errorMessage = errorMessage
            ) {
                errorMessage = null
                bookName = it
            }
        }
    }
}