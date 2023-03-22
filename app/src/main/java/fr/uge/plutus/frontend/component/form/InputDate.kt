package fr.uge.plutus.frontend.component.form

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.util.toStringFormatted
import fr.uge.plutus.util.useDoOnceOnMounted
import java.util.*

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

    useDoOnceOnMounted {
        onValueChange(date)
    }

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

    InputText(label, date, "dd/mm/yyyy",
        errorMessage = errorMessage,
        leadingIcon = {
            IconButton(
                modifier = Modifier.padding(start = 8.dp),
                onClick = { dialog.show() }
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
            }
        }
    ) {
        date = it
        onValueChange(date)
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
