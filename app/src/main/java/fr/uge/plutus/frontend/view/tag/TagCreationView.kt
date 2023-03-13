package fr.uge.plutus.frontend.view.tag

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.*
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.Tag
import fr.uge.plutus.backend.Transaction
import fr.uge.plutus.frontend.component.common.Loading
import fr.uge.plutus.frontend.component.form.InputSelectCollection
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.store.GlobalState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private suspend fun removeTags(transaction: Transaction, tag: Tag) =
    withContext(Dispatchers.IO) {
        Database.tagTransactionJoin().delete(transaction, tag)
    }

private suspend fun updateTagMap(book: Book): Map<String, Tag> =
    withContext(Dispatchers.IO) {
        val tagsValue = Database.tags().findByBookId(book.uuid)
        return@withContext tagsValue.associateBy { it.name!! }
    }

private suspend fun createTag(book: Book, name: String, transaction: Transaction) =
    withContext(Dispatchers.IO) {
        val tag = Database.tags().insert(name, book.uuid)
        Database.tagTransactionJoin().insert(transaction, tag)
    }

@Composable
fun TagCreationView(onExit: () -> Unit) {
    val currentBook = GlobalState.currentBook!!
    val currentTransaction = GlobalState.currentTransaction!!
    val context = LocalContext.current
    var isOpen by rememberSaveable { mutableStateOf(false) }
    var creatingTag by rememberSaveable { mutableStateOf("") }
    var creating by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var tagMap by rememberSaveable { mutableStateOf(emptyMap<String, Tag>()) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    val tagToAdd = rememberSaveable { mutableStateListOf<Tag>() }

    LaunchedEffect(creating) {
        if (!creating) return@LaunchedEffect

        if (tagToAdd.isEmpty()) {
            errorMessage = "You must select at least one tag"
            creating = false
            return@LaunchedEffect
        }

        if (creatingTag.isNotBlank()) {
            try {
                createTag(currentBook, creatingTag, currentTransaction)
                Toast.makeText(context, "Tag created", Toast.LENGTH_SHORT).show()
                onExit()
            } catch (e: SQLiteConstraintException) {
                errorMessage = "Tag name already exist"
            }
        }

        for (tag in tagToAdd) {
            withContext(Dispatchers.IO) {
                Database.tagTransactionJoin().insert(currentTransaction, tag)
            }
        }
        Toast.makeText(context, "Tag added", Toast.LENGTH_SHORT).show()
        tagToAdd.clear()
        creating = false
    }

    if (isOpen) {

        if (!loaded) {
            Loading {
                tagMap = updateTagMap(currentBook)
                loaded = true
            }
        }

        Box(
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(32.dp),
                elevation = 8.dp,
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    InputText(
                        label = "Create a new tag",
                        value = creatingTag
                    ) { creatingTag = it }
                    Box(modifier = Modifier.wrapContentSize()) {
                        InputSelectCollection(
                            label = "Select an existing tag",
                            options = tagMap.values.map { it.name!! },
                            initial = "",
                            mapFromString = { tagMap[it]!! },
                            mapToString = { it.toString() },
                            onSelected = { tagMap[it]?.let { tag -> tagToAdd += tag } }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {
                        Button(onClick = {
                            creating = true
                            loaded = false
                        }) {
                            Text(text = "ADD", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                isOpen = false
                                loaded = false
                            }) {
                            Text(text = "CLOSE", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    } else {
        Button(onClick = { isOpen = true }) {
            Text(text = "Create a tag")
        }
    }
}


