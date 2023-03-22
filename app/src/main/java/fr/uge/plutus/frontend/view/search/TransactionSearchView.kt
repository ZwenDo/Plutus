package fr.uge.plutus.frontend.view.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.plutus.R
import fr.uge.plutus.backend.Tag
import fr.uge.plutus.backend.TagType
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.component.common.DisplayPill
import java.util.*


@Composable
fun TransactionSearchView() {
    var searchQuery by remember { mutableStateOf("") }

    // TODO : Replace with actual data
    val transactions = listOf(
        Transaction("Super U", Date(), 100.0, UUID.randomUUID()),
        Transaction("Vérification de carte bancaire", Date(), 0.00, UUID.randomUUID()),
        Transaction("Test transaction 4", Date(), 9.99, UUID.randomUUID()),
    )
    val tags = listOf(
        Tag("Test tag 1", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 2", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 3", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 4", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 5", TagType.EXPENSE, UUID.randomUUID()),
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search query") },
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    IconButton(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onClick = { /*TODO*/ }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.filter),
                            contentDescription = "Filter",
                        )
                    }
                }
            )
            LazyColumn() {
                items(transactions) { transaction ->
                    TransactionSearchResult(transaction, tags)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionSearchResult(transaction: Transaction, tags: List<Tag> = emptyList()) {
    Surface(onClick = {
        // TODO: Navigate to transaction details
    }) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    transaction.description,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    "${transaction.amount} ${transaction.currency}",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                )
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 16.dp)
            ) {
                items(tags) { tag ->
                    DisplayPill(tag.name)
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
