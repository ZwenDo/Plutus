package fr.uge.plutus.frontend.view.transaction

import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.backend.Currency
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.TagType
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.component.form.InputDate
import fr.uge.plutus.frontend.component.form.InputSelectEnum
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.store.GlobalState
import fr.uge.plutus.ui.theme.Gray
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.util.toDateOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

enum class Field {
    DESCRIPTION,
    DATE,
    AMOUNT,
    CURRENCY
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionCreationView(onExit: () -> Unit = {}) {
    val currentBook = globalState().currentBook
    require(currentBook != null) { "No book selected" }

    val context = LocalContext.current
    var creating by rememberSaveable { mutableStateOf(false) }
    val errors = remember { mutableStateMapOf<Field, String>() }

    var description by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf(Currency.USD) }
    val attachments = remember { mutableStateMapOf<Uri, String>() }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        it.data?.data?.let { uri ->
            attachments[uri] = uri.lastPathSegment ?: uri.toString()
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
        if (errors.isNotEmpty()) {
            creating = false
            return@LaunchedEffect
        }

        try {
            val transaction = Transaction(
                description = description,
                date = actualDate!!,
                amount = actualAmount!!,
                currency = currency,
                bookId = currentBook.uuid
            )
            withContext(Dispatchers.IO) {
                Database.transactions().insert(transaction)
                attachments.forEach { (uri, name) ->
                    Database.attachments().insert(transaction, uri, name)
                }
            }
            Database.transactions().insert(transaction)

            withContext(Dispatchers.IO) {
                if (transaction.date > Date()) {
                    val tags = Database.tags()
                    val todoTag = tags
                        .findByName("@todo", currentBook.uuid)
                        .firstOrNull { it.type == TagType.INFO }
                        ?: tags.insert("@todo", currentBook.uuid)
                    Database.tagTransactionJoin().insert(transaction, todoTag)
                }
            }
            Toast.makeText(context, "Transaction created", Toast.LENGTH_SHORT).show()
            onExit()
        } catch (e: SQLiteConstraintException) {
            Toast.makeText(context, "Error while creating transaction", Toast.LENGTH_SHORT).show()
        }
        creating = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Create a transaction", style = MaterialTheme.typography.h5)
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                            initial = Currency.USD,
                            mapper = { Currency.valueOf(it) },
                            onSelected = {
                                currency = it
                                errors.clear()
                            }
                        )
                    }
                }

                Spacer(
                    modifier = Modifier
                        .height(8.dp)
                        .fillMaxWidth()
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_OPEN_DOCUMENT,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                            .apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                            }
                        launcher.launch(intent)
                    },
                ) {
                    Text(text = "Add attachment")
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .border(1.dp, Gray, MaterialTheme.shapes.small)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .scrollable(
                                rememberScrollState(),
                                orientation = Orientation.Vertical
                            )
                            .fillMaxWidth()
                            .height(150.dp),
                        content = {
                            val iterator = attachments.iterator()
                            items(attachments.size) { index ->
                                val actualIndex = attachments.size - index - 1
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val entry = iterator.next()
                                    Button(
                                        onClick = { attachments -= entry.key },
                                        modifier = Modifier
                                            .scale(0.5f)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null)
                                    }
                                    TextField(
                                        value = entry.value,
                                        modifier = Modifier.fillMaxWidth(),
                                        onValueChange = {
                                            attachments[entry.key] = it.replace("\n", "")
                                        },
                                        singleLine = true,
                                    )
                                }
                            }
                        }
                    )
                }

            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = { creating = true }) {
                Text(text = "CREATE", fontWeight = FontWeight.SemiBold)
            }
        }
    }
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
