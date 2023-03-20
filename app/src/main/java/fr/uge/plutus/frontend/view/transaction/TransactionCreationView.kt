package fr.uge.plutus.frontend.view.transaction

import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.backend.*
import fr.uge.plutus.backend.Currency
import fr.uge.plutus.frontend.component.form.InputDate
import fr.uge.plutus.frontend.component.form.InputSelectEnum
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.View
import fr.uge.plutus.frontend.view.attachment.AttachmentCreationView
import fr.uge.plutus.util.toDateOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

enum class Field {
    DESCRIPTION,
    DATE,
    AMOUNT,
    CURRENCY,
    LATITUDE,
    LONGITUDE
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun Preview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        TransactionCreationView()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionCreationView() {
    val globalState = globalState()
    val currentBook = globalState.currentBook
    val initialTransaction = globalState.currentTransaction
    require(currentBook != null) { "No book selected" }

    val context = LocalContext.current
    var creating by rememberSaveable { mutableStateOf(false) }
    val errors = remember { mutableStateMapOf<Field, String>() }

    var description by rememberSaveable { mutableStateOf(initialTransaction?.description ?: "") }
    var date by rememberSaveable { mutableStateOf(initialTransaction?.date?.toString() ?: "") }
    var amount by rememberSaveable { mutableStateOf(initialTransaction?.amount?.toString() ?: "") }
    var latitude by rememberSaveable {
        mutableStateOf(
            initialTransaction?.latitude?.toString() ?: ""
        )
    }
    var longitude by rememberSaveable {
        mutableStateOf(
            initialTransaction?.longitude?.toString() ?: ""
        )
    }
    var currency by rememberSaveable {
        mutableStateOf(initialTransaction?.currency ?: Currency.USD)
    }

    val initialAttachments = remember { mutableStateMapOf<UUID, Attachment>() }
    val attachments = remember { mutableStateMapOf<UUID, Attachment>() }

    LaunchedEffect(Unit) {
        if (initialTransaction != null) {
            withContext(Dispatchers.IO) {
                val loaded = Database
                    .attachments()
                    .findAllByTransactionId(initialTransaction.transactionId)
                    .associateBy { it.id }
                initialAttachments += loaded
                attachments += loaded
            }
        }
    }

    LaunchedEffect(creating) {
        if (!creating) return@LaunchedEffect

        errors.clear()
        if (description.isBlank()) {
            errors[Field.DESCRIPTION] = "Description cannot be empty"
        }
        val actualDate = date.toDateOrNull()
        if (actualDate == null) {
            errors[Field.DATE] = "Date format is invalid"
        }
        val actualAmount = amount.toDoubleOrNull()
        if (actualAmount == null) {
            errors[Field.AMOUNT] = "Amount is invalid"
        }
        if ((longitude.isBlank()) xor (latitude.isBlank())) {
            errors[Field.LATITUDE] = "Latitude and longitude must be both set or both unset"
            errors[Field.LONGITUDE] = "Latitude and longitude must be both set or both unset"
        }
        val actualLatitude = latitude.toDoubleOrNull()
        if (actualLatitude == null && latitude.isNotBlank()) {
            errors[Field.LATITUDE] = "Latitude is invalid"
        }
        val actualLongitude = longitude.toDoubleOrNull()
        if (actualLongitude == null && longitude.isNotBlank()) {
            errors[Field.LONGITUDE] = "Longitude is invalid"
        }
        if (errors.isNotEmpty()) {
            creating = false
            return@LaunchedEffect
        }

        try {
            val transactionId = initialTransaction?.transactionId ?: UUID.randomUUID()
            val transaction = Transaction(
                description = description,
                date = actualDate!!,
                amount = actualAmount!!,
                currency = currency,
                bookId = currentBook.uuid,
                transactionId = transactionId,
                latitude = actualLatitude,
                longitude = actualLongitude
            )
            withContext(Dispatchers.IO) {
                if (initialTransaction != null) {
                    Database.transactions().update(transaction)
                } else {
                    Database.transactions().insert(transaction)
                }
                attachments.forEach { (id, new) -> // for each attachment we have to check if it has been added, updated or deleted
                    initialAttachments.compute(id) { _, old ->
                        val toUpsert = new.copy(transactionId = transaction.transactionId)
                        if (old == null) {
                            Database.attachments()._insert(toUpsert)
                        } else {
                            Database.attachments().update(toUpsert)
                        }
                        null
                    }
                }
                initialAttachments.forEach { (_, old) -> // delete the rest
                    Database.attachments().delete(old)
                }
                if (initialTransaction != null && transaction.date > Date()) {
                    val tags = Database.tags()
                    val todoTag = tags
                        .findByName("@todo", currentBook.uuid)
                        .firstOrNull { it.type == TagType.INFO }
                        ?: tags.insert("@todo", currentBook.uuid)
                    Database.tagTransactionJoin().insert(transaction, todoTag)
                }
            }
            Toast.makeText(context, "Transaction created", Toast.LENGTH_SHORT).show()

            globalState.currentView = View.TRANSACTION_LIST
            globalState.currentTransaction = null
        } catch (e: SQLiteConstraintException) {
            Toast.makeText(context, "Error while creating transaction", Toast.LENGTH_SHORT).show()
        }
        creating = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        InputText(
            "Description",
            description,
            singleLine = false,
            errorMessage = errors[Field.DESCRIPTION]
        ) {
            description = it
            errors.clear()
        }
        InputDate("Date", errors[Field.DATE]) {
            date = it
            errors.clear()
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(3f / 5f)) {
                InputText("Amount", amount, errorMessage = errors[Field.AMOUNT]) {
                    amount = it
                    errors.clear()
                }
            }
            Box(Modifier.weight(2f / 5f)) {
                InputSelectEnum(
                    label = "Currency",
                    options = Currency.values().toList(),
                    initial = currency,
                    mapper = { Currency.valueOf(it) },
                    onSelected = {
                        currency = it
                        errors.clear()
                    }
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f / 2f)) {
                InputText("Latitude", latitude, errorMessage = errors[Field.LATITUDE]) {
                    latitude = it
                    errors.clear()
                }
            }
            Box(modifier = Modifier.weight(1f / 2f)) {
                InputText("Longitude", longitude, errorMessage = errors[Field.LONGITUDE]) {
                    longitude = it
                    errors.clear()
                }
            }
        }

        Spacer(
            modifier = Modifier
                .height(8.dp)
                .fillMaxWidth()
        )
        AttachmentCreationView(attachments)

        Spacer(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth()
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = { creating = true }) {
            Text(
                text = if (initialTransaction == null) "CREATE" else "SAVE",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
