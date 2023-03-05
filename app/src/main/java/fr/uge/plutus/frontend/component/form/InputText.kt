package fr.uge.plutus.frontend.component.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.math.sin

@Composable
fun InputText(
    label: String,
    value: String,
    placeholder: String = label,
    singleLine: Boolean = true,
    errorMessage: String? = null,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value,
            onValueChange = {
                val actual = if (singleLine) {
                    it.replace("\n", "")
                } else {
                    it
                }.trim()
                onValueChange(actual)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = singleLine,
            isError = errorMessage != null,
        )
        if (errorMessage != null) {
            Text(
                errorMessage,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }
    }
}
