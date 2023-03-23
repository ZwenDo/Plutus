package fr.uge.plutus.frontend.view.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.plutus.backend.*
import fr.uge.plutus.backend.Currency
import fr.uge.plutus.frontend.component.common.DisplayPill
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.View
import fr.uge.plutus.util.toStringFormatted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionListItem(
    transaction: Transaction,
    tags: List<Tag> = emptyList(),
    onClick: () -> Unit = {},
) {
    Surface(onClick = onClick) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        transaction.description,
                        style = MaterialTheme.typography.body1,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(
                        transaction.date.toStringFormatted(),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    "${transaction.amount} ${transaction.currency}",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (tags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 16.dp)
                ) {
                    items(tags) { tag ->
                        DisplayPill(tag.name)
                    }
                }
            }
            Divider()
        }
    }
}

@Preview
@Composable
fun TransactionListItemPreview() {
    TransactionListItem(
        Transaction(
            "Transaction 1",
            Date(),
            100.0,
            UUID.randomUUID(),
            Currency.EUR
        )
    )
}

@Composable
fun TransactionList(
    transactions: List<Pair<Transaction, Set<UUID>>>,
    tags: List<Tag>,
    onClick: (Transaction) -> Unit = {}
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(transactions) { (transaction, tagIds) ->
                TransactionListItem(transaction, tags.filter { tagIds.contains(it.tagId) }) {
                    onClick(transaction)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionListPreview() {
    val transaction = Transaction(
        "Transaction 1",
        Date(),
        100.0,
        UUID.randomUUID(),
        Currency.EUR
    )
    TransactionList(
        listOf(transaction to emptySet()),
        listOf(Tag("Tag 1", TagType.EXPENSE, UUID.randomUUID()))
    )
}

@Composable
fun EmptyTransactionListPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No transaction found",
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Click on the button below to create a new one",
            style = MaterialTheme.typography.body1,
            color = Color.Gray,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyTransactionListPlaceholderPreview() {
    EmptyTransactionListPlaceholder()
}

@Composable
fun TransactionListView() {
    val globalState = globalState()
    val tagTransactionJoinDao = Database.tagTransactionJoin()
    val transactionDao = Database.transactions()
    val tagDao = Database.tags()

    var transactions by rememberSaveable {
        mutableStateOf(listOf<Pair<Transaction, Set<UUID>>>())
    }
    var tags by rememberSaveable { mutableStateOf(emptyList<Tag>()) }

    LaunchedEffect(globalState.globalFilters) {
        if (!globalState.globalFilters.mustApply) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            tags = tagDao.findByBookId(globalState.currentBook!!.uuid)
            transactions = transactionDao
                .findWithGlobalFilters(globalState.currentBook!!.uuid, globalState.globalFilters)
                .map {
                    it to tagTransactionJoinDao
                        .findTagsByTransactionId(it.transactionId)
                        .mapTo(mutableSetOf()) { tag -> tag.tagId }
                }
            globalState.globalFilters = globalState.globalFilters.copy { mustApply = false }
        }
    }

    if (transactions.isNotEmpty()) {
        TransactionList(transactions, tags) {
            globalState.currentTransaction = it
            globalState.currentView = View.TRANSACTION_DETAILS
        }
    } else {
        EmptyTransactionListPlaceholder()
    }
}
