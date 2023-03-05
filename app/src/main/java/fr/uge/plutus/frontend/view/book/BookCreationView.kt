package fr.uge.plutus.frontend.view.book

import android.database.sqlite.SQLiteConstraintException
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Database
import fr.uge.plutus.frontend.component.form.InputText

@Preview(showBackground = true)
@Composable
fun BookCreatorPreview() {
    BookCreationView()
}

@Composable
fun BookCreationView(onExit: () -> Unit = {}) {
    val context = LocalContext.current

    var bookName by rememberSaveable { mutableStateOf("") }
    var creating by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(creating) {
        if (!creating) return@LaunchedEffect

        if (bookName.isBlank()) {
            errorMessage = "Book name cannot be empty"
            creating = false
            return@LaunchedEffect
        }

        val book = Book(bookName)
        try {
            Database.books().insert(book)
            Toast.makeText(context, "Book “${book.name}” created", Toast.LENGTH_SHORT).show()
            onExit()
        } catch (e: SQLiteConstraintException) {
            errorMessage = "Book already exists"
        }

        creating = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Create a book", style = MaterialTheme.typography.h5)
        InputText(label = "Book name", value = bookName, errorMessage = errorMessage) {
            bookName = it
            errorMessage = null
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = { creating = true }) {
            Text(text = "CREATE", fontWeight = FontWeight.SemiBold)
        }
    }
}
