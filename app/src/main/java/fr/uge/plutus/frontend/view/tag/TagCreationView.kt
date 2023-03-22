package fr.uge.plutus.frontend.view.tag

import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import fr.uge.plutus.backend.*
import fr.uge.plutus.frontend.component.common.Loading
import fr.uge.plutus.frontend.component.form.InputSelectCollection
import fr.uge.plutus.frontend.component.form.InputSelectEnum
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.store.globalState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private suspend fun updateTagMapToAdd(book: Book, transaction: Transaction): Map<String, Tag> =
    withContext(Dispatchers.IO) {
        val tagsValue = Database.tags().findByBookId(book.uuid)
        val alreadyExistTag =
            Database.tagTransactionJoin().findTagsByTransactionId(transaction.transactionId)
        val tags = tagsValue.filter { !alreadyExistTag.contains(it) }
        return@withContext tags.associateBy { it.name }
    }

private suspend fun updateTagMapToDelete(transaction: Transaction): Map<String, Tag> =
    withContext(Dispatchers.IO) {
        val tagsValue =
            Database.tagTransactionJoin().findTagsByTransactionId(transaction.transactionId)
        return@withContext tagsValue.associateBy { it.name }
    }

@RequiresApi(Build.VERSION_CODES.O)
private suspend fun checkTagTarget(tag: Tag, transaction: Transaction) =
    withContext(Dispatchers.Main) {
        if (tag.budgetTarget == null) {
            return@withContext
        }

        val (from, to) = tag.budgetTarget.timePeriod.toDateRange(transaction.date)
        val bookId = transaction.bookId
        val transactions = Database.transactions()
            .findByBookIdAndDateRangeAndTagId(bookId, from, to, tag.tagId)
        val total = transactions.sumOf { it.amount }
        if (total > tag.budgetTarget.value) {
            // TODO: Send notification
        }
    }

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TagCreationView(onClose: () -> Unit = {}) {
    val globalState = globalState()
    val currentBook = globalState.currentBook!!
    val currentTransaction = globalState.currentTransaction!!
    val context = LocalContext.current
    var isOpen by rememberSaveable { mutableStateOf(false) }
    var creatingTag by rememberSaveable { mutableStateOf("") }
    var addTarget by rememberSaveable { mutableStateOf(false) }
    var budgetTargetValue by rememberSaveable { mutableStateOf<Double?>(null) }
    var budgetTargetCurrency by rememberSaveable { mutableStateOf(Currency.USD) }
    var budgetTargetPeriod by rememberSaveable { mutableStateOf<TimePeriod?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var delete by rememberSaveable { mutableStateOf(false) }
    var update by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var tagMap by rememberSaveable { mutableStateOf(emptyMap<String, Tag>()) }
    var tagMapDelete by rememberSaveable { mutableStateOf(emptyMap<String, Tag>()) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var onSelectDelete by remember { mutableStateOf<Tag?>(null) }
    var onSelectAdd by remember { mutableStateOf<Tag?>(null) }

    LaunchedEffect(update) {
        if (creating) {
            if (creatingTag.isNotBlank()) {
                try {
                    withContext(Dispatchers.IO) {
                        val value = budgetTargetValue
                        val currency = budgetTargetCurrency
                        val period = budgetTargetPeriod
                        val tag = if (addTarget && value != null && period != null) {
                            val budgetTarget = BudgetTarget(value, currency, period)
                            Database.tags().insert(creatingTag, currentBook.uuid, budgetTarget)
                        } else {
                            Database.tags().insert(creatingTag, currentBook.uuid, null)
                        }
                        Database.tagTransactionJoin().insert(currentTransaction, tag)
                    }
                    Toast.makeText(context, "Tag created", Toast.LENGTH_SHORT).show()
                } catch (e: SQLiteConstraintException) {
                    errorMessage = "Tag name already exist"
                }
            }

            if (onSelectAdd != null) {
                try {
                    withContext(Dispatchers.IO) {
                        Database.tagTransactionJoin().insert(currentTransaction, onSelectAdd!!)
                        checkTagTarget(onSelectAdd!!, currentTransaction)
                    }
                    Toast.makeText(context, "Tag added", Toast.LENGTH_SHORT).show()
                } catch (e: SQLiteConstraintException) {
                    errorMessage = "Tag is already added"
                }
            }

            creating = false
        }

        if (delete) {
            if (onSelectDelete != null) {
                try {
                    withContext(Dispatchers.IO) {
                        Database.tagTransactionJoin().delete(currentTransaction, onSelectDelete!!)
                    }
                    Toast.makeText(context, "Tag deleted", Toast.LENGTH_SHORT).show()
                } catch (e: SQLiteConstraintException) {
                    errorMessage = "Tag is already deleted"
                }
            }
            delete = false
        }
        onSelectDelete = null
        onSelectAdd = null
        update = false
    }

    if (isOpen) {
        Popup(
            alignment = Alignment.CenterStart,
            onDismissRequest = {
                isOpen = false
                onClose()
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
                        Box(modifier = Modifier.wrapContentSize()) {
                            InputSelectCollection(
                                label = "Select an existing tag to add",
                                options = tagMap.values,
                                initial = null,
                                mapFromString = { tagMap[it]!! },
                                mapToString = Tag::stringRepresentation,
                                onSelected = { onSelectAdd = it }
                            )
                        }
                        Box(modifier = Modifier.wrapContentSize()) {
                            InputSelectCollection(
                                label = "Select an existing tag to remove",
                                options = tagMapDelete.values,
                                initial = null,
                                mapFromString = { tagMapDelete[it]!! },
                                mapToString = Tag::stringRepresentation,
                                onSelected = { onSelectDelete = it }
                            )
                        }
                        Divider(
                            color = Color.Gray, modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .width(1.dp)
                        )
                        InputText(
                            label = "Create a new tag",
                            value = creatingTag
                        ) { creatingTag = it }
                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = addTarget, onCheckedChange = { addTarget = it })
                                Text(text = "Set a budget target")
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f / 2f)) {
                                    InputText(
                                        label = "Amount",
                                        value = budgetTargetValue?.toString() ?: "",
                                        enabled = addTarget,
                                    ) {
                                        budgetTargetValue = it.toDoubleOrNull()
                                    }
                                }
                                Box(modifier = Modifier.weight(1f / 2f)) {
                                    InputSelectEnum(
                                        label = "Currency",
                                        options = Currency.values().toList(),
                                        initial = budgetTargetCurrency,
                                        mapper = { Currency.valueOf(it) },
                                        onSelected = { budgetTargetCurrency = it },
                                        enabled = addTarget,
                                    )
                                }
                            }
                            InputSelectCollection(
                                label = "Period",
                                options = TimePeriod.values().toList(),
                                initial = null,
                                mapFromString = { TimePeriod.valueOf(it) },
                                mapToString = TimePeriod::displayName,
                                onSelected = {
                                    budgetTargetPeriod = it
                                },
                                enabled = addTarget,
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
                                Text(text = "REMOVE", fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = {
                                    isOpen = false
                                    loaded = false
                                    onClose()
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
            Text(text = "Manage tags")
        }
    }
}


