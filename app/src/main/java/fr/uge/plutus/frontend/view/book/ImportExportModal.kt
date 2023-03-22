package fr.uge.plutus.frontend.view.book

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import fr.uge.plutus.backend.serialization.ExportBook
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.ui.theme.Gray

@Composable
fun ImportExportModal(
    isImport: Boolean = false,
    onDismiss: () -> Unit
) {
    var exportName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isCloud by rememberSaveable { mutableStateOf(false) }
    var submit by rememberSaveable { mutableStateOf(false) }
    var cloudKey by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(submit) {
        if (!submit) return@LaunchedEffect

        if (isImport) {
            if (isCloud) {

            } else {
//                ExportBook(password = , book = , name = )
            }
        } else {
            if (isCloud) {

            } else {

            }
        }
        onDismiss()
    }

    Popup(
        alignment = Alignment.CenterStart,
        onDismissRequest = { onDismiss() },
        properties = PopupProperties(focusable = true)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .border(1.dp, color = Gray, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(checked = isCloud, onCheckedChange = { isCloud = it })
                Text(text = "Cloud ${if (isImport) "import" else "export"}")
                if (isCloud && isImport) {
                    InputText(
                        label = "File key",
                        value = cloudKey,
                    ) {
                        cloudKey = it
                    }
                }
            }
            if (!isImport) {
                InputText(
                    label = "Export name",
                    value = exportName,
                ) {
                    exportName = it
                }
            }
            InputText(
                label = "Password",
                value = password,
                isPassword = true
            ) {
                password = it
            }
            Button(onClick = { submit = true }) {
                Text(text = if (isImport) "Import" else "Export")
            }
        }
    }

}