package fr.uge.plutus.frontend.view.transaction

import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.backend.Currency
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.component.form.InputDate
import fr.uge.plutus.frontend.component.form.InputSelectEnum
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.store.GlobalState
import fr.uge.plutus.util.toDateOrNull

enum class Field {
    DESCRIPTION,
    DATE,
    AMOUNT,
    CURRENCY
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionCreationView(onExit: () -> Unit = {}) {
    val currentBook = GlobalState.currentBook
    require(currentBook != null) { "No book selected" }

    val context = LocalContext.current
    var creating by rememberSaveable { mutableStateOf(false) }
    val errors = remember { mutableStateMapOf<Field, String>() }

    var description by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf(Currency.USD) }

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
            Database.transactions().insert(
                Transaction(
                    description = description,
                    date = actualDate!!,
                    amount = actualAmount!!,
                    currency = currency,
                    bookId = currentBook.uuid
                )
            )
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
