package fr.uge.plutus.frontend.component.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun InputText(
    label: String,
    value: String,
    placeholder: String = label,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value,
        onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = singleLine
    )
}