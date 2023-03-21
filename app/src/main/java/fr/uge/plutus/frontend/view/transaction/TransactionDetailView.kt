package fr.uge.plutus.frontend.view.transaction

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
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
import fr.uge.plutus.R
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.Tag
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.component.common.DisplayPill
import fr.uge.plutus.frontend.component.common.Loading
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.View
import fr.uge.plutus.frontend.view.tag.TagCreationView
import fr.uge.plutus.ui.theme.PlutusTheme
import fr.uge.plutus.util.DateFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private suspend fun getTransactionsTags(transaction: Transaction): List<Tag> =
    withContext(Dispatchers.IO) {
        Database.tagTransactionJoin()
            .findTagsByTransactionId(transaction.transactionId)
    }


@Composable
fun DisplayHeader(
    transaction: Transaction,
    backgroundColor: Color = MaterialTheme.colors.primary,
    fontColor: Color = MaterialTheme.colors.onPrimary,
    onBack: () -> Unit
) {
    val globalState = globalState()
    Column(
        Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(bottom = 20.dp)
    ) {
        // return button
        Row {

            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            IconButton(onClick = {
                globalState.currentTransaction = transaction
                globalState.currentView = View.TRANSACTION_CREATION
            }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit"
                )
            }
        }

        // Amount
        Text(
            text = "${transaction.amount} ${transaction.currency}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = fontColor,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )

        // Date

        val date = DateFormatter.format(transaction.date)
        Text(
            text = date,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = fontColor,
            fontSize = 20.sp,
            fontWeight = FontWeight(500)
        )

        // Edit button
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
            text = "Description de la transaction",
            fontSize = 15.sp,
            color = Color.Gray
        )
        Text(
            text = transaction.description,
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
            val caption = it.stringRepresentation
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
        Row {
            TagCreationView()
        }
    }
}

@Composable
fun DisplayBody(transaction: Transaction) {
    Column(
        Modifier
            .fillMaxSize()
    ) {
        DisplayDescriptionSection(transaction = transaction)
        Divider(
            color = Color.Gray, modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .width(1.dp)
        )
        DisplayTagsSection(transaction = transaction)
        if (transaction.latitude != null && transaction.longitude != null) {
            Divider(
                color = Color.Gray, modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .width(1.dp)
            )
            DisplayLocation(latitude = transaction.latitude, longitude = transaction.longitude)
        }
    }
}

@Composable
fun DisplayTransactionDetail(transaction: Transaction, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .scrollable(rememberScrollState(), orientation = Orientation.Vertical)) {
        DisplayHeader(transaction, onBack = onBack)
        DisplayBody(transaction)
    }
}

@Composable
fun DisplayLocation(latitude: Double, longitude: Double) {
    val map = createMapWithLocation(latitude, longitude)

    Box(Modifier.wrapContentSize()) {
        Image(
            modifier = Modifier.fillMaxWidth(),
            bitmap = map,
            contentDescription = "",
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
fun createMapWithLocation(latitude: Double, longitude: Double): ImageBitmap {
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

@Preview(showBackground = true)
@Composable
fun TransactionDetailsPreview() {
    val context = LocalContext.current
    var loaded by rememberSaveable { mutableStateOf(false) }
    var transaction by rememberSaveable { mutableStateOf<Transaction?>(null) }

    if (!loaded) {
        Database.init(context)
        Loading {
            val books = Database.books().findAll()
            transaction = Database.transactions().findAllByBookId(books[0].uuid)[0]
            loaded = true
        }
    } else {
        PlutusTheme {
            DisplayTransactionDetail(transaction!!) {
                Log.d("TransactionDetails", "Back")
            }
        }
    }
}
