package fr.uge.plutus.frontend.view.book

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.component.common.Card
import fr.uge.plutus.frontend.component.common.Loading
import fr.uge.plutus.frontend.store.globalState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.abs


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

    val biggestIncome by rememberSaveable {
        mutableStateOf(transactionsOfThisMonth
            .filter { it.amount > 0 }.maxOfOrNull { it.amount } ?: 0.0)
    }
    val biggestExpense by rememberSaveable {
        mutableStateOf(transactionsOfThisMonth
            .filter { it.amount < 0 }.minOfOrNull { it.amount } ?: 0.0)
    }
    val biggestTransaction by rememberSaveable {
        mutableStateOf(abs(biggestExpense).coerceAtLeast(biggestIncome))
    }

    val barHeight = 200
    val heightMultiplier by rememberSaveable {
        mutableStateOf((barHeight - 35.0) / biggestTransaction)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val scrollState = rememberScrollState()
        Box(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .scrollable(scrollState, Orientation.Horizontal)
        ) {
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                items(transactionsByDay.entries.sortedBy { it.key }
                    .toList()) { (day, transactions) ->
                    val sum = transactions.sumOf { it.amount }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(20.dp)
                            .height(barHeight.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height((abs(sum) * heightMultiplier).dp)
                                .background(if (sum > 0) Color.Green else Color.Red)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "$day")
                    }
                }
            }
        }

        Text("(Scroll horizontally)")
        Text(
            text = "Summary of ${
                calendar.getDisplayName(
                    Calendar.MONTH,
                    Calendar.LONG,
                    Locale.getDefault()
                )
            } ${calendar.get(Calendar.YEAR)}",
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
            TextCard("N° of transactions this month", transactionsOfThisMonth.size.toString())
        }
        item {
            TextCard("Mean of this month", "%.2f".format(mean) + " USD")
        }
        item {
            TextCard("Total of this month", "$total USD")
        }
        item {
            if (biggestIncome != null) {
                TransactionCard("Biggest income of this month", biggestIncome!!)
            }
        }
        item {
            if (biggestOutcome != null) {
                TransactionCard("Biggest outcome of this month", biggestOutcome!!)
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
        if (transactions.isEmpty()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No transactions yet ; go to the transaction list to create one",
                    Modifier.padding(10.dp)
                )
            }
        } else {
            BookOverview(transactions)
        }
    }
}

private suspend fun getTransactions(book: Book): List<Transaction> =
    withContext(Dispatchers.IO) {
        Database.transactions().findAllByBookId(book.uuid)
    }