package fr.uge.plutus.frontend.view.book

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.serialization.ExportBook
import fr.uge.plutus.backend.serialization.importBook
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.store.globalState

@Composable
fun ImportExportModal(
    target: Book,
    isImport: Boolean = false,
    onDismiss: () -> Unit
) {
    var exportName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isCloud by rememberSaveable { mutableStateOf(false) }
    var submit by rememberSaveable { mutableStateOf(false) }
    var cloudKey by rememberSaveable { mutableStateOf("") }
    var importingUri: Uri? by rememberSaveable { mutableStateOf(null) }
    val currentContext = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        it.data?.data?.let { uri ->
            importingUri = uri
        }
    }

    if (!isImport && submit) {
        if (isCloud) {
            Toast.makeText(
                currentContext,
                "Cloud import not implemented yet",
                Toast.LENGTH_SHORT
            ).show()
            submit = false
        } else {
            ExportBook(
                password.ifEmpty { null },
                book = target,
                name = exportName.ifBlank { target.name },
            ) {
                submit = false
                Toast.makeText(currentContext, "Export successful", Toast.LENGTH_SHORT).show()
                onDismiss()
            }
        }
    }

    LaunchedEffect(importingUri) {
        if (importingUri == null) return@LaunchedEffect

        val importResult = importBook(
            password = password.ifEmpty { null },
            fileUri = importingUri!!,
            context = currentContext,
            target.uuid
        )
        if (!importResult) {
            Toast.makeText(currentContext, "Invalid password", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(currentContext, "Import successful", Toast.LENGTH_SHORT).show()
        }
        importingUri = null
        onDismiss()
    }

    LaunchedEffect(submit) {
        if (!submit || !isImport) return@LaunchedEffect

        if (isCloud) {
            Toast.makeText(
                currentContext,
                "Cloud import not implemented yet",
                Toast.LENGTH_SHORT
            ).show()
            submit = false
            return@LaunchedEffect
        }

        // if not cloud, we need to import a file from the device
        val intent = Intent(
            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
            .apply {
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        launcher.launch(intent)
        submit = false
    }

    Dialog(
        onDismissRequest = { onDismiss() },
    ) {
        Surface(shape = RoundedCornerShape(8.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (isImport) {
                        "Import in ${target.name}"
                    } else {
                        "Export ${target.name}"
                    },
                    style = MaterialTheme.typography.h6,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = "Cloud ${if (isImport) "import" else "export"}")
                        Checkbox(checked = isCloud, onCheckedChange = { isCloud = it })
                    }
                    InputText(
                        label = "File key",
                        value = cloudKey,
                        enabled = isCloud
                    ) {
                        cloudKey = it
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

}