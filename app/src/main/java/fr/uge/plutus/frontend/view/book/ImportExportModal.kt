package fr.uge.plutus.frontend.view.book

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.uge.plutus.R
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.serialization.ExportBook
import fr.uge.plutus.backend.serialization.importBook
import fr.uge.plutus.frontend.component.form.InputText

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

    val exportSuccessfulMessage = stringResource(R.string.export_successful)
    val passwordInvalidMessage = stringResource(R.string.invalid_password)
    val importSuccessfulMessage = stringResource(R.string.import_successful)

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
                Toast.makeText(currentContext, exportSuccessfulMessage, Toast.LENGTH_SHORT).show()
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
            Toast.makeText(currentContext, passwordInvalidMessage, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(currentContext, importSuccessfulMessage, Toast.LENGTH_SHORT).show()
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
                        stringResource(id = R.string.import_in_book).format(target.name)
                    } else {
                        stringResource(id = R.string.export_book).format(target.name)
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
                        Text(text = stringResource(id = R.string.cloud_import_export).format(if (isImport) "import" else "export"))
                        Checkbox(checked = isCloud, onCheckedChange = { isCloud = it })
                    }
                    InputText(
                        label = stringResource(R.string.file_key),
                        value = cloudKey,
                        enabled = isCloud
                    ) {
                        cloudKey = it
                    }
                }
                if (!isImport) {
                    InputText(
                        label = stringResource(R.string.export_name),
                        value = exportName,
                    ) {
                        exportName = it
                    }
                }
                InputText(
                    label = stringResource(R.string.password),
                    value = password,
                    isPassword = true
                ) {
                    password = it
                }
                Button(onClick = { submit = true }) {
                    Text(text = if (isImport) stringResource(id = R.string.import_book) else stringResource(id = R.string.export))
                }
            }
        }
    }

}