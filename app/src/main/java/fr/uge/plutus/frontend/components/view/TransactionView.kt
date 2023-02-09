package fr.uge.plutus.frontend.components.view

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.ui.theme.PlutusTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DisplayTransaction(transaction: Transaction, clickHandler: () -> Unit) {
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
                .padding(10.dp)) {
            Text(
                text = transaction.amount.toString()!! + " " + transaction.currency,
                fontSize = 25.sp,
                fontWeight = FontWeight(900)
            )
            Text(text = transaction.description!!)
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DisplayTransactions(transactions: List<Transaction>) {
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    var oldDate = ""
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(title = { Text("Transactions") })
        },
        floatingActionButton =
        {
            FloatingActionButton(onClick = { /*TODO new Transaction*/ }) {
                Icon(Icons.Filled.Add, "New transaction")
            }
        }
    ) {
        LazyColumn(
            Modifier.fillMaxWidth()
        ) {
            items(transactions.sortedBy { it.date }) {
                val currentDate = formatter.format(it.date!!)
                if (oldDate != currentDate) {
                    Text(
                        text = currentDate,
                        Modifier.padding(horizontal = 10.dp),
                        color = Color.DarkGray
                    )
                    oldDate = currentDate
                }
                DisplayTransaction(transaction = it) {
                    //DisplayTransactionDetail(transaction = it)
                    println(it.amount)
                }
            }
        }
    }
}

private fun getTransactions(): List<Transaction> {
    val book = Book("Book")

    val t1 = Transaction("transaction 1", Date(1), 10.0, book.uuid, fr.uge.plutus.backend.Currency.EUR)
    val t2 = Transaction("transaction 2", Date(1531321214611L), 10351.53, book.uuid, fr.uge.plutus.backend.Currency.EUR)
    val t3 = Transaction("transaction 3", Date(1531321215611L), 123.0, book.uuid, fr.uge.plutus.backend.Currency.EUR)
    val t4 = Transaction("transaction 4", Date(1531321214611L), 0.0, book.uuid, fr.uge.plutus.backend.Currency.EUR)
    val t5 = Transaction("transaction 5", Date(1531321214611L), 1523.52, book.uuid, fr.uge.plutus.backend.Currency.EUR)
    val t6 = Transaction("transaction 6", Date(1531321214611L), 522.1, book.uuid, fr.uge.plutus.backend.Currency.EUR)
    val t7 = Transaction("transaction 7", Date(1531321214611L), -4102.0, book.uuid, fr.uge.plutus.backend.Currency.EUR)
    val t8 = Transaction("transaction 8", Date(1531371214611L), -45.0, book.uuid, fr.uge.plutus.backend.Currency.EUR)
    val t9 = Transaction("transaction 9", Date(1531321254611L), -78.56, book.uuid, fr.uge.plutus.backend.Currency.EUR)

    //return listOf(t1, t2, t3, t4, t5, t6, t7, t8, t9)
    return emptyList()
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    PlutusTheme {
        val transactions = getTransactions()
        DisplayTransactions(transactions)
    }
}