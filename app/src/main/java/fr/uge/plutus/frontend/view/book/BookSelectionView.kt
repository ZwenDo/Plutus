package fr.uge.plutus.frontend.view.book

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.R
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.serialization.ExportBook
import fr.uge.plutus.backend.serialization.importBook
import fr.uge.plutus.frontend.component.common.Loading
import fr.uge.plutus.ui.theme.PlutusTheme
import fr.uge.plutus.util.plus
import androidx.compose.foundation.shape.CircleShape

@Composable
fun BookSelectionView(onChosen: (Book) -> Unit) {
    var loaded by rememberSaveable { mutableStateOf(false) }
    var books by rememberSaveable { mutableStateOf(emptyList<Book>()) }

    if (!loaded) {
        Loading(false) {
            books = Database.books().findAll()
            loaded = true
        }
        return
    }

    Scaffold(
        scaffoldState = rememberScaffoldState(),
        topBar = { TopAppBar(title = { Text("Books") }) }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding + 8.dp)
                .scrollable(rememberScrollState(), orientation = Orientation.Vertical),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = true
        ) {
            items(books) { book ->
                BookSelectionItem(book, onChosen)
            }
        }
    }
}

@Composable
fun BookSelectionItem(book: Book, onChosen: (Book) -> Unit) {
    var exporting by rememberSaveable { mutableStateOf(false) }
    var importing: Uri? by rememberSaveable { mutableStateOf(null) }
    val currentContext = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        it.data?.data?.let { uri ->
            importing = uri
        }
    }

    if (exporting) {
        ImportExportModal(
            isImport = true
        ) {
            exporting = false
        }
//        ExportBook("baba", book, book.name) {
//            Toast.makeText(currentContext, "Export Completed", Toast.LENGTH_SHORT).show()
//            exporting = false
//        }
    }

    LaunchedEffect(importing) {
        if (importing == null) return@LaunchedEffect

        if (!importBook("baba", importing!!, currentContext, book.uuid)) {
            Toast.makeText(currentContext, "Invalid password", Toast.LENGTH_SHORT).show()
        }
        importing = null
    }


    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier
                .scale(0.8f)
                .border(1.dp, Color.Black, CircleShape),
            onClick = {
                exporting = true
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.file_upload),
                contentDescription = "Export"
            )
        }
        IconButton(
            modifier = Modifier
                .scale(0.8f)
                .border(1.dp, Color.Black, CircleShape),
            onClick = {
            val intent = Intent(
                Intent.ACTION_OPEN_DOCUMENT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
                .apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
            launcher.launch(intent)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.file_download),
                contentDescription = "Import"
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChosen(book) }
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                .padding(8.dp),
        ) {
            Text(book.name)
        }
    }

}

@Composable
@Preview(showBackground = true)
fun BookSelectionViewPreview() {
    Database.init(LocalContext.current)
    PlutusTheme {
        BookSelectionView {
            Log.d("BookSelectionView", "Chosen: $it")
        }
    }
}
