package fr.uge.plutus.frontend.view.transaction

import android.database.sqlite.SQLiteConstraintException
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.R
import fr.uge.plutus.MainActivity
import fr.uge.plutus.backend.*
import fr.uge.plutus.backend.Currency
import fr.uge.plutus.util.getLocation
import fr.uge.plutus.frontend.component.form.InputDate
import fr.uge.plutus.frontend.component.form.InputSelectEnum
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.View
import fr.uge.plutus.frontend.view.attachment.AttachmentCreationView
import fr.uge.plutus.util.toDateOrNull
import kotlinx.coroutines.*
import java.util.*

enum class Field {
    DESCRIPTION,
    DATE,
    AMOUNT,
    CURRENCY,
    LATITUDE,
    LONGITUDE
}


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
    var askLocation by rememberSaveable { mutableStateOf(false) }

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

    val descriptionInvalidMessage = stringResource(R.string.description_cannot_be_empty)
    val dateFormatInvalidMessage = stringResource(R.string.date_format_invalid)
    val amountInvalidMessage = stringResource(R.string.amount_invalid)
    val latitudeAndLongitudeInvalidMessage = stringResource(R.string.latitude_and_longitude_invalid)
    val latitudeInvalidMessage = stringResource(R.string.latitude_invalid)
    val longitudeInvalidMessage = stringResource(R.string.longitude_invalid)
    val transactionCreatedMessage = stringResource(R.string.transaction_created)
    val transactionErrorMessage = stringResource(R.string.error_while_creating_transaction)

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

    LaunchedEffect(globalState.locationPermission, askLocation) {
        if (!askLocation) return@LaunchedEffect

        if (!globalState.locationPermission) {
            MainActivity.requestLocationPermission()
        } else {
            getLocation(
                context = context,
                onError = {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); askLocation = false
                }) {
                latitude = it.latitude.toString()
                longitude = it.longitude.toString()
                askLocation = false
            }
        }
    }

    LaunchedEffect(creating) {
        if (!creating) return@LaunchedEffect

        errors.clear()
        if (description.isBlank()) {
            errors[Field.DESCRIPTION] = descriptionInvalidMessage
        }
        val actualDate = date.toDateOrNull()
        if (actualDate == null) {
            errors[Field.DATE] = dateFormatInvalidMessage
        }
        val actualAmount = amount.toDoubleOrNull()
        if (actualAmount == null) {
            errors[Field.AMOUNT] = amountInvalidMessage
        }
        if ((longitude.isBlank()) xor (latitude.isBlank())) {
            errors[Field.LATITUDE] = latitudeAndLongitudeInvalidMessage
            errors[Field.LONGITUDE] = latitudeAndLongitudeInvalidMessage
        }
        val actualLatitude = latitude.toDoubleOrNull()
        if (actualLatitude == null && latitude.isNotBlank()) {
            errors[Field.LATITUDE] = latitudeInvalidMessage
        }
        val actualLongitude = longitude.toDoubleOrNull()
        if (actualLongitude == null && longitude.isNotBlank()) {
            errors[Field.LONGITUDE] = longitudeInvalidMessage
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
                val scope = coroutineContext
                attachments.forEach { (id, new) -> // for each attachment we have to check if it has been added, updated or deleted
                    initialAttachments.compute(id) { _, old ->
                        val toUpsert = new.copy(transactionId = transaction.transactionId)
                        CoroutineScope(scope).launch {
                            if (old == null) {
                                Database.attachments()._insert(toUpsert)
                            } else {
                                Database.attachments().update(toUpsert)
                            }
                        }.start()
                        null
                    }
                }
                initialAttachments.forEach { (_, old) -> // delete the rest
                    Database.attachments().delete(old)
                }
                if (transaction.date > Date()) {
                    val tags = Database.tags()
                    val todoTag = tags
                        .findByName("@todo", currentBook.uuid)
                        .firstOrNull { it.type == TagType.INFO }
                        ?: tags.insert("@todo", currentBook.uuid, null)
                    Database.tagTransactionJoin().insert(transaction, todoTag)
                }
            }
            Toast.makeText(context, transactionCreatedMessage, Toast.LENGTH_SHORT).show()

            globalState.currentView = View.TRANSACTION_LIST
            globalState.currentTransaction = null
        } catch (e: SQLiteConstraintException) {
            Toast.makeText(context, transactionErrorMessage, Toast.LENGTH_SHORT).show()
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
            stringResource(R.string.description),
            description,
            singleLine = false,
            errorMessage = errors[Field.DESCRIPTION]
        ) {
            description = it
            errors.clear()
        }
        InputDate(stringResource(R.string.date), errors[Field.DATE]) {
            date = it
            errors.clear()
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(3f / 5f)) {
                InputText(stringResource(R.string.amount), amount, errorMessage = errors[Field.AMOUNT]) {
                    amount = it
                    errors.clear()
                }
            }
            Box(Modifier.weight(2f / 5f)) {
                InputSelectEnum(
                    label = stringResource(R.string.currency),
                    options = Currency.values().toList(),
                    initial = currency,
                    mapper = { Currency.valueOf(it) },
                    onSelected = {
                        currency = it
                        errors.clear()
                    },
                    enabled = false,
                )
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f / 2f)) {
                InputText(stringResource(R.string.latitude), latitude, errorMessage = errors[Field.LATITUDE]) {
                    latitude = it
                    errors.clear()
                }
            }
            Box(modifier = Modifier.weight(1f / 2f)) {
                InputText(stringResource(R.string.longitude), longitude, errorMessage = errors[Field.LONGITUDE]) {
                    longitude = it
                    errors.clear()
                }
            }
            Button(
                modifier = Modifier
                    .weight(1f / 5f)
                    .padding(top = 0.dp),
                onClick = {
                    askLocation = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.my_location)
                )
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
                text = if (initialTransaction == null) stringResource(R.string.create) else stringResource(R.string.save),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
