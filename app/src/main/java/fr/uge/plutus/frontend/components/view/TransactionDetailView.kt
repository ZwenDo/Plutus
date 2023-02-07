package fr.uge.plutus.frontend.components.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.plutus.backend.*
import java.text.SimpleDateFormat
import java.util.*


private fun getTagTypeColor(tag: Tag): Color {
    return when(tag.type) {
        TagType.INCOME -> Color.hsl(105f, 1f, 0.75f)    // green
        TagType.EXPENSE -> Color.hsl(1f, 1f, 0.75f)     // red
        TagType.TRANSFER -> Color.hsl(60f, 1f, 0.75f)   // yellow
        else -> Color.hsl(181f, 1f, 0.75f)              // cyan
    }
}

private fun getTransactionsTags(transaction: Transaction): List<Tag> {
    val book = Book("Book")

    val t1 = getTagFromString("+Quotidiens", book)
    val t2 = getTagFromString("-sorties", book)
    val t3 = getTagFromString("=anniversaries", book)
    val t4 = getTagFromString("repas", book)
    val t5 = getTagFromString("animaux", book)

    //return Database.tagTransactionJoin().findTagsByTransactionId(transactionId = transaction.transactionId)

    return listOf(t1, t2, t3, t4, t5)
}

@Composable
fun DisplayHeader(transaction: Transaction, backgroundColor: Color = Color.Cyan, fontColor: Color = Color.White) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(bottom = 20.dp)
    ) {
        // return button
        IconButton(onClick = { /*TODO*/ }) {
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
            text = transaction.description ?: "",
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
            DisplayTag(
                caption = caption,
                clickHandler = { /*TODO*/ }
            )
        }
    }
}

@Composable
fun DisplayTagsSection(transaction: Transaction) {
    val tags = getTransactionsTags(transaction)

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

@Composable
fun DisplayBody(transaction: Transaction) {
    Column(Modifier.fillMaxSize()) {
        DisplayDescriptionSection(transaction = transaction)
        Divider(color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(10.dp).width(1.dp))
        DisplayTagsSection(transaction = transaction)
    }
}

@Composable
fun DisplayTransaction(transaction: Transaction) {
    val color = Color.hsl(186f, 0.76f, 0.39f)
    val fontColor = Color.White

    Column(Modifier.fillMaxSize()) {
        DisplayHeader(
            transaction = transaction,
            backgroundColor = color,
            fontColor = fontColor
        )
        DisplayBody(transaction = transaction)
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    val book = Book("Book")
    val transaction = Transaction("First Transaction", Date(0), 1542.15, book.uuid)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        DisplayTransaction(transaction)
    }
}