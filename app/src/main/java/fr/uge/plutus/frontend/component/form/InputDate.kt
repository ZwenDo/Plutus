package fr.uge.plutus.frontend.component.form

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.util.toStringFormatted
import java.util.Calendar
import java.util.Date

@Composable
fun InputDate(
    label: String,
    errorMessage: String? = null,
    onValueChange: (String) -> Unit,
) {
    val calendar = Calendar.getInstance()
    calendar.time = Date()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

    var date by rememberSaveable { mutableStateOf(Date().toStringFormatted()) }
    onValueChange(date)

    val dialog = DatePickerDialog(
        LocalContext.current,
        { _, year, month, day ->
            date = "$day/${month + 1}/$year"
            onValueChange(date)
        },
        currentYear,
        currentMonth,
        currentDay
    )

    Row(
        Modifier,
        Arrangement.Center,
        Alignment.CenterVertically
    ) {
        Button(onClick = { dialog.show() }) {
            Icon(Icons.Default.DateRange, contentDescription = null)
        }

        Spacer(Modifier.width(10.dp))

        InputText(label, date, "dd/mm/yyyy", errorMessage = errorMessage) {
            date = it
            onValueChange(date)
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
        InputDate("Date") {}
    }
}