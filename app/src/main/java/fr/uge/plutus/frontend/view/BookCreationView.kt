package fr.uge.plutus.frontend.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.frontend.component.form.InputText

@Preview(showBackground = true)
@Composable
fun BookCreatorPreview() {
    BookCreationView()
}

data class BookDTO(val name: String)

@Composable
fun BookCreationView(
    onBookCreated: (BookDTO) -> Unit = {}
) {
    var bookName by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Create a book", style = MaterialTheme.typography.h5)
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                InputText(label = "Book name", value = bookName, onValueChange = { bookName = it })
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                onBookCreated(BookDTO(bookName))
            }) {
                Text(text = "CREATE", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
