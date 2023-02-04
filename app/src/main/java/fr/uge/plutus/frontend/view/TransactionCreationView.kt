package fr.uge.plutus.frontend.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.backend.Currency
import fr.uge.plutus.frontend.component.form.InputDate
import fr.uge.plutus.frontend.component.form.InputSelect
import fr.uge.plutus.frontend.component.form.InputText

@Composable
fun TransactionCreationView() {
    var description by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf(Currency.USD) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(30.dp), Arrangement.Center, Alignment.CenterHorizontally
    ) {
        Text(text = "Create a new transaction", style = MaterialTheme.typography.h5)
        InputText("Description", description, singleLine = false) { description = it }
        InputDate("Date") { date = it }
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(3f/5f)) {
                InputText("Amount", amount) { amount = it }
            }
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(2f/5f)) {
                InputSelect(
                    label = "Currency",
                    options = Currency.values().toList(),
                    initial = Currency.USD,
                    mapper = { Currency.valueOf(it) },
                    onSelected = { currency = it }
                )
            }
        }
        Button(onClick = { /*TODO*/ }) {
            Text("Create")
        }
    }
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