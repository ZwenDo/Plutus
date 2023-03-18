package fr.uge.plutus.frontend.view.transaction

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.Tag
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.component.common.DisplayPill
import fr.uge.plutus.frontend.component.common.Loading
import fr.uge.plutus.frontend.store.GlobalState
import fr.uge.plutus.ui.theme.PlutusTheme
import fr.uge.plutus.util.DateFormatter
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private suspend fun getTransactionsTags(transaction: Transaction): List<Tag> =
    withContext(Dispatchers.IO) {
        Database.tagTransactionJoin()
            .findTagsByTransactionId(transaction.transactionId)
    }

private suspend fun getTransactions(book: Book): List<Transaction> =
    withContext(Dispatchers.IO) {
        Database.transactions().findAllByBookId(book.uuid)
    }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DisplayTransaction(transaction: Transaction, clickHandler: () -> Unit) {
    var loaded by rememberSaveable { mutableStateOf(false) }
    var tags by rememberSaveable { mutableStateOf(emptyList<Tag>()) }

    if (!loaded) {
        Loading(false) {
            tags = getTransactionsTags(transaction)
            loaded = true
        }
    } else {
        Surface(
            modifier = Modifier.padding(8.dp),
            border = BorderStroke(1.dp, Color.Gray),
            elevation = 5.dp,
            shape = RoundedCornerShape(15.dp),
            onClick = clickHandler
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // Amount
                Text(
                    text = "${transaction.amount} ${transaction.currency}",
                    fontSize = 25.sp,
                    fontWeight = FontWeight(900)
                )

                // Description
                Text(text = transaction.description)

                // Pills
                LazyRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    userScrollEnabled = false
                ) {
                    items(tags) {
                        DisplayPill(caption = it.name)
                    }
                }
            }
        }
    }
}

enum class TransactionView {
    LIST, DETAILS
}

@Composable
fun TransactionList(
    padding: PaddingValues,
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit
) {
    var oldDate by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(padding)
    ) {
        items(transactions.sortedByDescending { it.date }) {
            val currentDate = DateFormatter.format(it.date)
            if (oldDate != currentDate) {
                Text(
                    text = currentDate,
                    Modifier.padding(horizontal = 10.dp),
                    color = Color.DarkGray
                )
                oldDate = currentDate
            }
            DisplayTransaction(it) {
                onTransactionClick(it)
            }
        }
    }
}

@Composable
fun DisplayTransactions(onNewTransactionRequest: () -> Unit) {
    val book = GlobalState.currentBook!!
    var transactions by rememberSaveable { mutableStateOf(emptyList<Transaction>()) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()

    var transactionView by rememberSaveable { mutableStateOf(TransactionView.LIST) }
    var currentTransaction by rememberSaveable { mutableStateOf<Transaction?>(null) }

    if (!loaded) {
        Loading {
            transactions = getTransactions(book)
            loaded = true
        }
    } else {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(title = { Text("Transactions") })
            },
            floatingActionButton =
            {
                FloatingActionButton(onClick = onNewTransactionRequest) {
                    Icon(Icons.Filled.Add, "New transaction")
                }
            }
        ) { padding ->
            when (transactionView) {
                TransactionView.LIST -> {
                    TransactionList(padding, transactions) {
                        currentTransaction = it
                        transactionView = TransactionView.DETAILS
                    }
                }
                TransactionView.DETAILS -> {
                    DisplayTransactionDetail(currentTransaction!!) {
                        transactionView = TransactionView.LIST
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionListPreview() {
    var book by rememberSaveable { mutableStateOf<Book?>(null) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    if (!loaded) {
        LaunchedEffect(false) {
            Database.init(context)
            val bookDb = Database.books().findByName("book")
            if (null != bookDb) {
                book = bookDb
                loaded = true
            }
        }
    }

    if (loaded) {
        PlutusTheme {
            DisplayTransactions {
                Log.d("TransactionView", "New transaction request")
            }
        }
    }
}
