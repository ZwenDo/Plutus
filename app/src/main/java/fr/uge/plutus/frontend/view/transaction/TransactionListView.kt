package fr.uge.plutus.frontend.view.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.sqlite.db.SimpleSQLiteQuery
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.Tag
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.backend.findWithGlobalFilters
import fr.uge.plutus.frontend.component.common.DisplayPill
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.View
import fr.uge.plutus.util.toStringFormatted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.sql.DataSource


@Composable
fun TransactionSearchView() {
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

    TransactionList(transactions, tags)
}


@Composable
fun TransactionList(
    transactions: List<Pair<Transaction, Set<UUID>>>,
    tags: List<Tag>,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(transactions) { (transaction, tagIds) ->
                TransactionSearchResult(transaction, tags.filter { tagIds.contains(it.tagId) })
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionSearchResult(transaction: Transaction, tags: List<Tag> = emptyList()) {
    val globalState = globalState()
    Surface(onClick = {
        globalState.currentTransaction = transaction
        globalState.currentView = View.TRANSACTION_DETAILS
    }) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        transaction.description,
                        fontSize = 16.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(
                        transaction.date.toStringFormatted(),
                        style = MaterialTheme.typography.caption,
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
fun TransactionSearchViewPreview() {
    TransactionSearchView()
}
