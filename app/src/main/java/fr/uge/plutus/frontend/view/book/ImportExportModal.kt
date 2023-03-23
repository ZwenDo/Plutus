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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.serialization.ExportBook
import fr.uge.plutus.backend.serialization.importBook
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.component.scaffold.Dialog
import fr.uge.plutus.frontend.store.globalState
import java.util.*
import androidx.compose.ui.window.Dialog as JCDialog


@Composable
fun ImportBookModal(
    book: Book,
) {
    Dialog(
        open = true,
        title = "Import in “${book.name}”",
        submitButtonText = "IMPORT",
        onClose = {}
    ) {
        Column(
            Modifier.padding(24.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "This book is password protected. Please enter the password to decrypt the file.")
            InputText(label = "Password", value = "", onValueChange = {})
        }
    }
}

@Preview
@Composable
fun ImportBookModalPreview() {
    val book = Book("Book", UUID.randomUUID())
    ImportBookModal(book)
}

@Composable
fun ExportBookModal(
    book: Book,
) {
    Dialog(
        open = true,
        title = "Export “${book.name}”",
        submitButtonText = "EXPORT",
        onClose = {}
    ) {
        Column(
            Modifier.padding(24.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InputText(label = "Output file name", value = "", onValueChange = {})
            InputText(label = "Password", value = "", onValueChange = {})
        }
    }
}

@Preview
@Composable
fun ExportBookModalPreview() {
    val book = Book("Book", UUID.randomUUID())
    ExportBookModal(book)
}

@Composable
fun ImportExportModal(
    target: Book,
    isImport: Boolean = false,
    onDismiss: () -> Unit
) {
    val globalState = globalState()
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
        ExportBook(
            password.ifEmpty { null },
            book = target,
            name = exportName.ifBlank { target.name },
            isCloud = isCloud,
        ) {
            if (it == null) {
                Toast.makeText(currentContext, "Invalid password", Toast.LENGTH_SHORT).show()
                return@ExportBook
            }
            // TODO it is the token
            submit = false
            Toast.makeText(currentContext, "Export successful", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    LaunchedEffect(importingUri, submit) {
        if (!isImport || (!isCloud && importingUri == null)) return@LaunchedEffect

        val importResult = importBook(
            password = password.ifEmpty { null },
            fileUri = if (isCloud) null else importingUri,
            context = currentContext,
            token = cloudKey.ifBlank { null },
            mergeDestinationBook = target.uuid
        )
        if (!importResult) {
            Toast.makeText(currentContext, "Invalid password", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(currentContext, "Import successful", Toast.LENGTH_SHORT).show()
            globalState.mustRefetchTransactions = true
        }
        importingUri = null
        onDismiss()
    }

    LaunchedEffect(submit) {
        if (!submit || !isImport) return@LaunchedEffect

        if (isCloud) {
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

    JCDialog(
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

@Preview
@Composable
fun ExportModalPreview() {
    val book = Book("Test", UUID.randomUUID())
    ImportExportModal(book, false) {}
}

@Preview
@Composable
fun ImportModalPreview() {
    val book = Book("Test", UUID.randomUUID())
    ImportExportModal(book, true) {}
}
