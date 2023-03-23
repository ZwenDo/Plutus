package fr.uge.plutus.frontend.view.transaction

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import createNotificationChannel
import fr.uge.plutus.R
import fr.uge.plutus.backend.*
import fr.uge.plutus.backend.Currency
import fr.uge.plutus.frontend.component.common.DisplayPill
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.View
import fr.uge.plutus.frontend.view.tag.TagCreator
import fr.uge.plutus.frontend.view.tag.TagDTO
import fr.uge.plutus.frontend.view.tag.TagSelector
import fr.uge.plutus.util.DateFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import showSimpleNotification
import java.util.*


private suspend fun deleteTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
    Database.transactions().delete(transaction)
}


private suspend fun getTransactionsTags(transaction: Transaction): List<Tag> =
    withContext(Dispatchers.IO) {
        Database.tagTransactionJoin()
            .findTagsByTransactionId(transaction.transactionId)
    }

private suspend fun getBookTags(bookId: UUID): List<Tag> =
    withContext(Dispatchers.IO) {
        Database.tags().findByBookId(bookId)
    }

private suspend fun checkTagTarget(tag: Tag, transaction: Transaction, context: Context) =
    withContext(Dispatchers.Main) {
        val channelId = "Budget Plutus"
        val notificationId = 0
        val textTitle = "Budget exceeded"
        val textContent = "You have exceeded your budget for the tag ${tag.name}"
        createNotificationChannel(channelId, context)
        if (tag.budgetTarget == null) {
            return@withContext
        }

        val (from, to) = tag.budgetTarget.timePeriod.toDateRange(transaction.date)
        val bookId = transaction.bookId
        val transactions = Database.transactions()
            .findByBookIdAndDateRangeAndTagId(bookId, from, to, tag.tagId)
        val total = transactions.sumOf { it.amount }
        if (total > tag.budgetTarget.value) {
            showSimpleNotification(context, channelId, notificationId, textTitle, textContent)
        }
    }


private suspend fun updateTransactionTags(
    transaction: Transaction,
    transactionTags: List<Tag>,
    tags: List<Tag>,
    context: Context
) = withContext(Dispatchers.IO) {
    // Adding tags
    tags.filter {
        it !in transactionTags
    }.forEach {
        Database.tagTransactionJoin().insert(transaction, it)
        checkTagTarget(it, transaction, context)
    }
    // Removing tags
    transactionTags.filter {
        it !in tags
    }.forEach {
        Database.tagTransactionJoin().delete(transaction, it)
    }
}


@Composable
private fun createMapWithLocation(latitude: Double, longitude: Double): ImageBitmap {
    val context = LocalContext.current
    val mapBitmap = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.equirectangular_world_map,
        BitmapFactory.Options().also { it.inMutable = true })
    val canvas = Canvas(mapBitmap)
    val radius = minOf(canvas.width, canvas.height) / 50f
    val x = (longitude + 180.0) / 360.0 * canvas.width
    val y = (latitude + 90.0) / 180.0 * canvas.height
    canvas.drawCircle(
        x.toFloat(),
        y.toFloat(),
        radius,
        Paint().apply { color = Color.Red.toArgb() })
    return mapBitmap.asImageBitmap()
}

@Composable
fun TransactionHeader(
    transaction: Transaction,
    backgroundColor: Color = MaterialTheme.colors.primary,
    fontColor: Color = MaterialTheme.colors.onPrimary,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(bottom = 20.dp)
    ) {
        Text(
            text = "${transaction.amount} ${transaction.currency}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = fontColor,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )

        val date = DateFormatter.format(transaction.date)
        Text(
            text = date,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = fontColor,
            fontSize = 20.sp,
            fontWeight = FontWeight(500)
        )
    }
}

@Composable
private fun DescriptionSection(transaction: Transaction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Description",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = transaction.description,
            style = MaterialTheme.typography.body1,
        )
    }
}

@Composable
private fun DisplayTags(tags: List<Tag>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    )
    {
        items(tags) {
            var caption = it.stringRepresentation
            it.budgetTarget?.let { target ->
                caption += " (${target.value} ${target.currency} ${target.timePeriod.displayName})"
            }
            DisplayPill(caption)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TagsSection(transaction: Transaction) {
    val context = LocalContext.current
    val globalState = globalState()
    val coroutineScope = rememberCoroutineScope()

    var tags by rememberSaveable { mutableStateOf(listOf<Tag>()) }
    var transactionTags by rememberSaveable { mutableStateOf(listOf<Tag>()) }
    var viewId by rememberSaveable { mutableStateOf(0) }
    var tagSelectorOpen by rememberSaveable { mutableStateOf(false) }
    var tagCreatorOpen by rememberSaveable { mutableStateOf(false) }
    var tagDto by rememberSaveable { mutableStateOf<TagDTO?>(null) }

    LaunchedEffect(Unit, tagDto) {
        if (tagDto != null) {
            return@LaunchedEffect
        }
        tags = getBookTags(transaction.bookId)
    }
    LaunchedEffect(viewId) {
        transactionTags = getTransactionsTags(transaction)
    }
    LaunchedEffect(tagDto) {
        val dto = tagDto ?: return@LaunchedEffect
        val newTag = withContext(Dispatchers.IO) {
            if (dto.budgetTarget == null) {
                Database.tags().insert(dto.name, globalState.currentBook!!.uuid)
            } else {
                Database.tags().insert(dto.name, globalState.currentBook!!.uuid, dto.budgetTarget)
            }
        }
        Toast.makeText(context, "Tag “${newTag.name}” created", Toast.LENGTH_SHORT).show()
        tagDto = null
    }

    Surface(onClick = { tagSelectorOpen = true }) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Tags",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                if (transactionTags.isEmpty()) {
                    Text(
                        text = "No tags",
                        style = MaterialTheme.typography.body1,
                    )
                } else {
                    Box(Modifier.padding(top = 8.dp)) {
                        DisplayTags(tags = transactionTags)
                    }
                }
            }
            TextButton(onClick = { tagCreatorOpen = true }) {
                Text(text = "NEW TAG")
            }
        }
    }

    TagSelector(
        open = tagSelectorOpen,
        tags = tags,
        selectedTags = transactionTags.map { it.tagId }.toSet(),
    ) {
        tagSelectorOpen = false
        if (it != null) { // null means the user clicked to cancel the dialog
            coroutineScope.launch {
                updateTransactionTags(transaction, transactionTags, it, context)
                viewId++
            }
        }
    }

    TagCreator(open = tagCreatorOpen) { tag ->
        tagCreatorOpen = false
        tagDto = tag
    }
}

@Composable
fun LocationSection(latitude: Double, longitude: Double) {
    val map = createMapWithLocation(latitude, longitude)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Location",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = "$latitude, $longitude",
            style = MaterialTheme.typography.body1,
        )
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(8.dp)),
            bitmap = map,
            contentDescription = "",
            contentScale = ContentScale.FillWidth
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LocationSectionPreview() {
    LocationSection(latitude = 48.8534, longitude = 2.3488)
}

@Composable
fun TransactionDetails(transaction: Transaction) {
    val context = LocalContext.current
    val globalState = globalState()
    val coroutineScope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        DescriptionSection(transaction)
        Divider()
        TagsSection(transaction)
        Divider()
        if (transaction.latitude != null && transaction.longitude != null) {
            LocationSection(latitude = transaction.latitude, longitude = transaction.longitude)
            Divider()
        }
    }

    fun delete() {
        coroutineScope.launch {
            globalState.currentView = View.TRANSACTION_LIST
            globalState.deletingTransaction = false
            deleteTransaction(transaction)
            Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
            globalState.currentTransaction = null
        }
    }

    if (globalState.deletingTransaction) {
        AlertDialog(
            onDismissRequest = { globalState.deletingTransaction = false },
            title = {
                Text(
                    "Delete transaction",
                    style = MaterialTheme.typography.h6
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete this transaction? This action cannot be undone.",
                    style = MaterialTheme.typography.body1
                )
            },
            confirmButton = {
                TextButton(onClick = { delete() }) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    globalState.deletingTransaction = false
                }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionDetailsPreview() {
    TransactionDetails(
        transaction = Transaction(
            transactionId = UUID.randomUUID(),
            amount = 100.0,
            currency = Currency.EUR,
            description = "Achats de fournitures scolaires",
            date = Date(),
            latitude = 48.8534,
            longitude = 2.3488,
            bookId = UUID.randomUUID()
        )
    )
}
