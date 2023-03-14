package fr.uge.plutus.frontend.view.tag

import android.database.sqlite.SQLiteConstraintException
import android.provider.ContactsContract.Data
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
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
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

private suspend fun updateTagMapToAdd(book: Book, transaction: Transaction): Map<String, Tag> =
    withContext(Dispatchers.IO) {
        val tagsValue = Database.tags().findByBookId(book.uuid)
        val alreadyExistTag =
            Database.tagTransactionJoin().findTagsByTransactionId(transaction.transactionId)
        val tags = tagsValue.filter { !alreadyExistTag.contains(it) }
        return@withContext tags.associateBy { it.name!! }
    }

private suspend fun updateTagMapToDelete(transaction: Transaction): Map<String, Tag> =
    withContext(Dispatchers.IO) {
        val tagsValue =
            Database.tagTransactionJoin().findTagsByTransactionId(transaction.transactionId)
        return@withContext tagsValue.associateBy { it.name!! }
    }

@Composable
fun TagCreationView() {
    val currentBook = GlobalState.currentBook!!
    val currentTransaction = GlobalState.currentTransaction!!
    val context = LocalContext.current
    var isOpen by rememberSaveable { mutableStateOf(false) }
    var creatingTag by rememberSaveable { mutableStateOf("") }
    var creating by rememberSaveable { mutableStateOf(false) }
    var delete by rememberSaveable { mutableStateOf(false) }
    var update by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var tagMap by rememberSaveable { mutableStateOf(emptyMap<String, Tag>()) }
    var tagMapDelete by rememberSaveable { mutableStateOf(emptyMap<String, Tag>()) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    val tagToAdd = remember { mutableStateListOf<Tag>() }
    val tagToDelete = remember { mutableStateListOf<Tag>() }

    LaunchedEffect(update) {
        if (creating) {
            if (creatingTag.isNotBlank()) {
                try {
                    withContext(Dispatchers.IO) {
                        val tag = Database.tags().insert(creatingTag, currentBook.uuid)
                        Database.tagTransactionJoin().insert(currentTransaction, tag)
                    }
                    Toast.makeText(context, "Tag created", Toast.LENGTH_SHORT).show()
                } catch (e: SQLiteConstraintException) {
                    errorMessage = "Tag name already exist"
                }
            }

            for (tag in tagToAdd) {
                try {
                    withContext(Dispatchers.IO) {
                        Database.tagTransactionJoin().insert(currentTransaction, tag)
                    }
                } catch (e: SQLiteConstraintException) {
                    errorMessage = "Tag is already added"
                }
            }
            Toast.makeText(context, "Tag added", Toast.LENGTH_SHORT).show()
            tagToAdd.clear()
            creating = false
        }

        if (delete) {
            for (tag in tagToDelete) {
                try {
                    withContext(Dispatchers.IO) {
                        Database.tags().delete(tag)
                    }
                } catch (e: SQLiteConstraintException) {
                    errorMessage = "Tag is already deleted"
                }
            }
            Toast.makeText(context, "Tag deleted", Toast.LENGTH_SHORT).show()
            tagToDelete.clear()
            delete = false
        }
        update = false
    }

    if (isOpen) {
        Popup(
            alignment = Alignment.CenterStart,
            onDismissRequest = {
                isOpen = false
                tagToAdd.clear()
                tagToDelete.clear()
            },
            properties = PopupProperties(focusable = true)
        ) {
            if (!loaded) {
                Loading {
                    tagMap = updateTagMapToAdd(currentBook, currentTransaction)
                    tagMapDelete = updateTagMapToDelete(currentTransaction)
                    loaded = true
                }
            }

            Box(
                contentAlignment = Alignment.Center,
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
                                label = "Select an existing tag to add",
                                options = tagMap.values.map { it.name!! },
                                initial = "",
                                mapFromString = { tagMap[it]!! },
                                mapToString = { it.toString() },
                                onSelected = { tagMap[it]?.let { tag -> tagToAdd.add(tag) } }
                            )
                        }
                        Box(modifier = Modifier.wrapContentSize()) {
                            InputSelectCollection(
                                label = "Select an existing tag to delete",
                                options = tagMapDelete.values.map { it.name!! },
                                initial = "",
                                mapFromString = { tagMapDelete[it]!! },
                                mapToString = { it.toString() },
                                onSelected = { tagMapDelete[it]?.let { tag -> tagToDelete.add(tag) } }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween

                        ) {
                            Button(onClick = {
                                update = true
                                creating = true
                                loaded = false
                            }) {
                                Text(text = "ADD", fontWeight = FontWeight.SemiBold)
                            }
                            Button(onClick = {
                                update = true
                                delete = true
                                loaded = false
                            }) {
                                Text(text = "DELETE", fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = {
                                    isOpen = false
                                    loaded = false
                                    tagToAdd.clear()
                                    tagToDelete.clear()
                                }) {
                                Text(text = "CLOSE", fontWeight = FontWeight.SemiBold)
                            }
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


