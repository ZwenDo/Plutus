package fr.uge.plutus.frontend.view.book

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Database
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.View
import java.util.*


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookListItem(
    book: Book,
    transactions: Int,
    onClick: () -> Unit = {}
) {
    Surface(onClick = onClick) {
        Column(Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = book.name,
                    style = MaterialTheme.typography.body1,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
                Text(
                    text = "$transactions transactions",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Divider()
        }
    }
}

@Preview
@Composable
fun BookListItemPreview() {
    BookListItem(
        book = Book(
            name = "Book name",
            UUID.randomUUID()
        ),
        transactions = 5
    )
}

@Composable
fun BookList(
    books: List<Pair<Book, Int>>,
    onClick: (Book) -> Unit = {}
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .scrollable(rememberScrollState(), orientation = Orientation.Vertical),
    ) {
        items(books) { (book, transactions) ->
            BookListItem(book, transactions) { onClick(book) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookListPreview() {
    val book = Book(
        name = "Book name",
        UUID.randomUUID()
    )
    BookList(books = List(5) { book to it })
}

@Composable
fun EmptyBookListPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No book found",
            style = MaterialTheme.typography.h5
        )
        Text(
            text = "Click on the button below to create a new one",
            style = MaterialTheme.typography.body1,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyBookListPlaceholderPreview() {
    EmptyBookListPlaceholder()
}

@Composable
fun BookSelectionView() {
    val globalState = globalState()
    var books by rememberSaveable { mutableStateOf(emptyList<Pair<Book, Int>>()) }

    LaunchedEffect(Unit) {
        books = Database.books().findAllAndCountTransactions().map { it.toPair() }
    }

    if (books.isNotEmpty()) {
        BookList(books) {
            globalState.currentBook = it
            globalState.currentView = View.TRANSACTION_LIST
        }
    } else {
        EmptyBookListPlaceholder()
    }
}

/*
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
                contentDescription = "Export"
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
                contentDescription = "Import"
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
 */

enum class ImportExportState {
    NONE,
    IMPORT,
    EXPORT,
    ;

    val isImport
        get() = this === IMPORT
    val isNotNone
        get() = this !== NONE
}