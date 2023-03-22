package fr.uge.plutus.frontend.view.transaction

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import fr.uge.plutus.R
import fr.uge.plutus.backend.Currency
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.Tag
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.component.common.DisplayPill
import fr.uge.plutus.util.DateFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


private suspend fun getTransactionsTags(transaction: Transaction): List<Tag> =
    withContext(Dispatchers.IO) {
        Database.tagTransactionJoin()
            .findTagsByTransactionId(transaction.transactionId)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    )
    {
        items(tags) {
            var caption = it.stringRepresentation
            it.budgetTarget?.let { target -> caption += " (${target.value} ${target.currency} ${target.timePeriod.displayName})" }
            DisplayPill(caption) { /* TODO: Display tag's details */ }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TagsSection(transaction: Transaction) {
    var loaded by rememberSaveable { mutableStateOf(false) }
    var tags by rememberSaveable { mutableStateOf(listOf<Tag>()) }
    var viewId by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(viewId) {
        tags = getTransactionsTags(transaction)
    }

    Surface(onClick = { /*TODO*/ }) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Tags",
                fontSize = 14.sp,
                color = Color.Gray
            )
            if (tags.isEmpty()) {
                Text(
                    text = "No tags",
                    style = MaterialTheme.typography.body1,
                )
            } else {
                DisplayTags(tags = tags)
            }
        }
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionDetails(transaction: Transaction) {
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
}

@RequiresApi(Build.VERSION_CODES.O)
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
