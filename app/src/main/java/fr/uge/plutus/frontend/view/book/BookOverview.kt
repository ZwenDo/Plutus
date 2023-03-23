package fr.uge.plutus.frontend.view.book

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.plutus.R
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.component.BarChart
import fr.uge.plutus.frontend.component.common.Card
import fr.uge.plutus.frontend.component.common.Loading
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


@Composable
fun TransactionCard(title: String, transaction: Transaction) {
    Card {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = transaction.description,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${transaction.amount} ${transaction.currency}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun TextCard(title: String, content: String) {
    Card {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = content,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun TransactionsMonthChart(transactionsOfThisMonth: List<Transaction>) {
    val calendar by rememberSaveable { mutableStateOf(Calendar.getInstance()) }

    val transactionsByDay by rememberSaveable {
        val m = transactionsOfThisMonth
            .groupBy { transaction ->
                calendar.time = transaction.date
                calendar.get(Calendar.DAY_OF_MONTH)
            }.toMutableMap()

        val firstDay = m.keys.minOrNull() ?: 1
        val lastDay = m.keys.maxOrNull() ?: 1
        for (day in firstDay..lastDay) {
            if (day !in m) {
                m[day] = emptyList()
            }
        }

        mutableStateOf(m.toMap())
    }
    val amountByDay by rememberSaveable {
        mutableStateOf(transactionsByDay.mapValues { (_, transactions) ->
            transactions.sumOf { it.amount }
        })
    }

    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        BarChart(values = amountByDay)
        Text(stringResource(R.string.scroll_horizontally))
        Text(
            text = stringResource(id = R.string.summary_of_calendar_name).format("${
                calendar.getDisplayName(
                    Calendar.MONTH,
                    Calendar.LONG,
                    Locale.getDefault()
                )
            } ${calendar.get(Calendar.YEAR)}"),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
    }
}


@Composable
fun BookOverview(transactions: List<Transaction>) {
    val globalState = globalState()
    val currentBook = globalState.currentBook

    assert(currentBook != null) { "No book selected" }

    val calendar by rememberSaveable { mutableStateOf(Calendar.getInstance()) }
    val transactionsOfThisMonth by rememberSaveable {
        mutableStateOf(transactions
            .filter { transaction ->
                calendar.time = transaction.date
                calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)
            })
    }
    val mean by rememberSaveable {
        mutableStateOf(transactionsOfThisMonth
            .map { it.amount }
            .average())
    }
    val total by rememberSaveable {
        mutableStateOf(transactionsOfThisMonth.sumOf { it.amount })
    }
    val biggestIncome by rememberSaveable {
        mutableStateOf(transactionsOfThisMonth
            .filter { it.amount > 0 }
            .maxByOrNull { it.amount })
    }
    val biggestOutcome by rememberSaveable {
        mutableStateOf(transactionsOfThisMonth
            .filter { it.amount < 0 }
            .minByOrNull { it.amount })
    }

    LazyColumn(
        Modifier
            .fillMaxWidth()
            .scrollable(rememberScrollState(), Orientation.Vertical),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            TransactionsMonthChart(transactionsOfThisMonth)
        }
        item {
            TextCard(stringResource(R.string.n_of_transactions_this_month), transactionsOfThisMonth.size.toString())
        }
        item {
            TextCard(stringResource(R.string.mean_of_this_month), "%.2f".format(mean) + " USD")
        }
        item {
            TextCard(stringResource(R.string.total_of_this_month), "$total USD")
        }
        item {
            if (biggestIncome != null) {
                TransactionCard(stringResource(R.string.biggest_income_of_this_month), biggestIncome!!)
            }
        }
        item {
            if (biggestOutcome != null) {
                TransactionCard(stringResource(R.string.biggest_outcome_of_this_month), biggestOutcome!!)
            }
        }
    }
}

@Composable
fun BookOverviewLoader() {
    val globalState = globalState()
    val book = globalState.currentBook!!
    var transactions by rememberSaveable { mutableStateOf(emptyList<Transaction>()) }
    var loaded by rememberSaveable { mutableStateOf(false) }

    if (!loaded) {
        Loading {
            transactions = getTransactions(book)
            loaded = true
        }
    } else {
        if (transactions.isNotEmpty()) {
            BookOverview(transactions)
        } else {
            EmptyTransactionListPlaceholder()
        }
    }
}

private suspend fun getTransactions(book: Book): List<Transaction> =
    withContext(Dispatchers.IO) {
        Database.transactions().findAllByBookId(book.uuid)
    }

@Composable
fun EmptyTransactionListPlaceholder() {
    val globalState = globalState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Empty book",
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
        )
        Text(
            //R.string.no_transactions_yet_visit
            text = "Your book doesn't have any transaction yet.\n Create one to get started.",
            style = MaterialTheme.typography.body1,
            color = Color.Gray,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(64.dp))
        TextButton(onClick = {
            globalState.currentView = View.TRANSACTION_CREATION
        }) {
            Text("CREATE A TRANSACTION")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyTransactionListPlaceholderPreview() {
    EmptyTransactionListPlaceholder()
}
