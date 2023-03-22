package fr.uge.plutus.frontend.view.transaction

import android.os.Build
import androidx.annotation.RequiresApi
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
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.Tag
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.component.common.DisplayPill
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.View
import fr.uge.plutus.util.toStringFormatted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionSearchView() {
    val globalState = globalState()

    var transactions by rememberSaveable {
        mutableStateOf(emptyMap<Transaction, Set<UUID>>())
    }
    var tags by rememberSaveable { mutableStateOf(emptyList<Tag>()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            tags = Database.tags().findByBookId(globalState.currentBook!!.uuid)
            val bookTransactionsAndTags = Database.transactions()
                .findAllByBookId(globalState.currentBook!!.uuid)
                .map {
                    it to Database.tagTransactionJoin().findTagsByTransactionId(it.transactionId)
                        .map { tag -> tag.tagId }.toSet()
                }
            transactions = mapOf(*bookTransactionsAndTags.toTypedArray())
        }
    }

    TransactionList(transactions, tags)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionList(
    transactions: Map<Transaction, Set<UUID>>,
    tags: List<Tag>,
) {
    //var searchQuery by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            /*
            //TextField(
            //    modifier = Modifier.fillMaxWidth(),
            //    singleLine = true,
            //    placeholder = { Text("Search query") },
            //    value = searchQuery,
            //    onValueChange = { searchQuery = it },
            //    //leadingIcon = {
            //    //    IconButton(
            //    //        modifier = Modifier.padding(horizontal = 8.dp),
            //    //        onClick = {
            //    //            coroutineScope.launch {
            //    //                drawerState.open()
            //    //            }
            //    //        }
            //    //    ) {
            //    //        Icon(
            //    //            painter = painterResource(id = R.drawable.filter),
            //    //            contentDescription = "Filter",
            //    //        )
            //    //    }
            //    //}
            //)
             */
            LazyColumn {
                items(transactions.entries.toList()) { (transaction, tagIds) ->
                    TransactionSearchResult(transaction, tags.filter { tagIds.contains(it.tagId) })
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun TransactionSearchViewPreview() {
    TransactionSearchView()
}
