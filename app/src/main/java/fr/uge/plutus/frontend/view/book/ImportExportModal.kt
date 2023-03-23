package fr.uge.plutus.frontend.view.book

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.R
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.serialization.ExportBook
import fr.uge.plutus.backend.serialization.importBook
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.component.scaffold.Dialog
import fr.uge.plutus.frontend.store.globalState
import java.util.*


data class ImportDTO(
    val isCloud: Boolean,
    val fileKey: String,
    val password: String,
)

data class ExportDTO(
    val isCloud: Boolean,
    val fileName: String,
    val password: String,
)

@Composable
fun ImportBookModal(
    book: Book,
    onClose: (ImportDTO?) -> Unit = {},
) {
    var isCloud by rememberSaveable { mutableStateOf(false) }
    var fileKey by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Dialog(
        open = true,
        title = stringResource(R.string.import_in_book_name).format(book.name),
        submitButtonText = stringResource(R.string.import_button),
        onClose = { submit ->
            if (submit) {
                onClose(ImportDTO(isCloud, fileKey, password))
            } else {
                onClose(null)
            }
        }
    ) {
        Column(
            Modifier.padding(24.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Checkbox(checked = isCloud, onCheckedChange = { isCloud = it })
                Text(text = stringResource(R.string.import_from_cloud))
            }
            InputText(
                label = stringResource(R.string.file_key),
                value = fileKey,
                onValueChange = { fileKey = it },
                enabled = isCloud,
            )
        }
        Divider()
        Column(
            Modifier.padding(24.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = stringResource(R.string.please_enter_the_password_to_decrypt))
            InputText(
                label = stringResource(R.string.password_optional),
                value = password,
                onValueChange = { password = it },
                isPassword = true,
            )
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
    onClose: (ExportDTO?) -> Unit = {},
) {
    var isCloud by rememberSaveable { mutableStateOf(false) }
    var fileName by rememberSaveable { mutableStateOf(book.name) }
    var password by rememberSaveable { mutableStateOf("") }

    Dialog(
        open = true,
        title = stringResource(R.string.export_book).format(book.name),
        submitButtonText = stringResource(R.string.export_maj),
        onClose = { submit ->
            if (submit) {
                onClose(ExportDTO(isCloud, fileName, password))
            } else {
                onClose(null)
            }
        }
    ) {
        Column(
            Modifier.padding(24.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Checkbox(checked = isCloud, onCheckedChange = { isCloud = it })
                Text(text = stringResource(R.string.export_to_cloud))
            }
            Text(text = stringResource(R.string.unique_access_key_will_be_generated))
        }
        Divider()
        Column(
            Modifier.padding(24.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = stringResource(R.string.only_the_currently_visible_transaction_will_be_exported))
            InputText(
                label = stringResource(R.string.output_file_name),
                value = fileName,
                onValueChange = { fileName = it },
            )
            InputText(
                label = stringResource(R.string.password_optional),
                value = password,
                onValueChange = { password = it },
                isPassword = true,
            )
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

    val clipboardCopyTag = stringResource(R.string.clipboard_copy_tag)

    if (!isImport && submit) {
        ExportBook(
            list = globalState.currentTransactions,
            password.ifEmpty { null },
            book = target,
            name = exportName.ifBlank { target.name },
            isCloud = isCloud,
        ) {
            if (it == null) {
                Toast.makeText(currentContext, R.string.invalid_password, Toast.LENGTH_SHORT).show()
                return@ExportBook
            }
            val clipboard = currentContext.getSystemService(
                Context.CLIPBOARD_SERVICE
            ) as ClipboardManager
            val clip = ClipData.newPlainText(clipboardCopyTag, it)
            clipboard.setPrimaryClip(clip)
            submit = false
            if (isCloud) {
                Toast.makeText(currentContext, R.string.clipboard_copy_toast, Toast.LENGTH_LONG)
                    .show()
            } else {
                Toast.makeText(currentContext, R.string.export_successful, Toast.LENGTH_LONG)
                    .show()
            }
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
            Toast.makeText(currentContext, R.string.invalid_password, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(currentContext, R.string.import_successful, Toast.LENGTH_SHORT).show()
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

    if (isImport) {
        ImportBookModal(target) {
            if (it != null) {
                isCloud = it.isCloud
                cloudKey = it.fileKey
                password = it.password
                submit = true
            } else {
                onDismiss()
            }
        }
    } else {
        ExportBookModal(target) {
            if (it != null) {
                isCloud = it.isCloud
                exportName = it.fileName
                password = it.password
                submit = true
            } else {
                onDismiss()
            }
        }
    }
}
