package fr.uge.plutus.frontend.components.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.plutus.backend.*
import fr.uge.plutus.frontend.components.commons.DisplayPill
import fr.uge.plutus.ui.theme.PlutusTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


private suspend fun getTransactionsTags(transaction: Transaction): List<Tag> = withContext(Dispatchers.Default)  {
    return@withContext Database.tagTransactionJoin().findTagsByTransactionId(transaction.transactionId)
}

private fun getTagTypeColor(tag: Tag): Color {
    return when(tag.type) {
        TagType.INCOME -> Color.hsl(105f, 1f, 0.75f)    // green
        TagType.EXPENSE -> Color.hsl(1f, 1f, 0.75f)     // red
        TagType.TRANSFER -> Color.hsl(60f, 1f, 0.75f)   // yellow
        else -> Color.hsl(181f, 1f, 0.75f)              // cyan
    }
}

@Composable
fun DisplayHeader(
    transaction: Transaction,
    backgroundColor: Color = MaterialTheme.colors.primary,
    fontColor: Color = MaterialTheme.colors.onPrimary
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(bottom = 20.dp)
    ) {
        // return button
        IconButton(onClick = { /* TODO: Navigation go back*/ }) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        // Amount
        Text(
            text = transaction.amount.toString() + " " + transaction.currency.toString(),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = fontColor,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )

        // Date
        if (null != transaction.date) {
            val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val date = formatter.format(transaction.date).toString()
            Text(
                text = date,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = fontColor,
                fontSize = 20.sp,
                fontWeight = FontWeight(500)
            )
        }

        // Location
    }
}

@Composable
fun DisplayDescriptionSection(transaction: Transaction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Text(
            text = "Descritpion de la transaction",
            fontSize = 15.sp,
            color = Color.Gray
        )
        Text(
            text = transaction.description!!,
            fontSize = 20.sp
        )
    }
}

@Composable
fun DisplayTags(tags: List<Tag>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    )
    {
        items(tags) {
            val caption = it.type?.code + it.name
            DisplayPill(caption) { /* TODO: Display tag's details */ }
        }
    }
}

@Composable
fun DisplayTagsSection(transaction: Transaction) {
    var loaded by rememberSaveable { mutableStateOf(false) }
    var tags by rememberSaveable { mutableStateOf(listOf<Tag>()) }

    if (!loaded) {
        Loading {
            tags = getTransactionsTags(transaction)
            loaded = true
        }
    } else {
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = "Tags",
                modifier = Modifier.padding(5.dp),
                fontSize = 15.sp,
                color = Color.Gray
            )
            DisplayTags(tags = tags)
        }
    }
}

@Composable
fun DisplayBody(transaction: Transaction) {
    Column(
        Modifier
            .fillMaxSize()) {
        DisplayDescriptionSection(transaction = transaction)
        Divider(color = Color.Gray, modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .width(1.dp))
        DisplayTagsSection(transaction = transaction)
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DisplayTransactionDetail(transaction: Transaction) {
    Scaffold {
        Column(Modifier.fillMaxSize()) {
            DisplayHeader(
                transaction = transaction
            )
            DisplayBody(transaction = transaction)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionDetailsPreview() {
    val context = LocalContext.current
    var loaded by rememberSaveable { mutableStateOf(false) }
    var transaction by rememberSaveable { mutableStateOf<Transaction?>(null) }

    if(!loaded) {
        Database.init(context)
        Loading {
            val books = Database.books().getAll()
            transaction = Database.transactions().findAllByBookId(books[0].uuid)[0]
            loaded = true
        }
    } else {
        PlutusTheme {
            DisplayTransactionDetail(transaction!!)
        }
    }
}