package fr.uge.plutus.frontend.view.tag

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.plutus.backend.BudgetTarget
import fr.uge.plutus.backend.Currency
import fr.uge.plutus.backend.TimePeriod
import fr.uge.plutus.frontend.component.form.InputSelectEnum
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.component.scaffold.Dialog

data class TagDTO(
    val name: String,
    val budgetTarget: BudgetTarget?,
)

@Composable
fun TagCreator(
    open: Boolean,
    onClose: (TagDTO?) -> Unit = {},
) {
    var name by rememberSaveable { mutableStateOf("") }
    var addBudgetTarget by rememberSaveable { mutableStateOf(false) }
    var budgetTarget by rememberSaveable { mutableStateOf(0.0) }
    var currency by rememberSaveable { mutableStateOf(Currency.USD) }
    var period by rememberSaveable { mutableStateOf(TimePeriod.DAILY) }

    fun reset() {
        name = ""
        addBudgetTarget = false
        budgetTarget = 0.0
        currency = Currency.USD
        period = TimePeriod.DAILY
    }

    Dialog(
        open = open,
        title = "Create a new tag",
        submitButtonText = "CREATE",
        onClose = { submit ->
            if (submit) {
                if (addBudgetTarget) {
                    onClose(TagDTO(name, BudgetTarget(budgetTarget, currency, period)))
                } else {
                    onClose(TagDTO(name, null))
                }
            } else {
                onClose(null)
            }
            reset()
        }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                Box(Modifier.padding(horizontal = 8.dp)) {
                    InputText(
                        label = "Name",
                        value = name,
                        onValueChange = { name = it },
                    )
                }
            }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(checked = addBudgetTarget, onCheckedChange = { addBudgetTarget = it })
                    Text(
                        text = "Budget target",
                        fontSize = 16.sp,
                    )
                }
            }
            item {
                Row(
                    Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.weight(1f / 2f)) {
                        InputText(
                            label = "Budget",
                            value = budgetTarget.toString(),
                            onValueChange = { budgetTarget = it.toDouble() },
                            enabled = addBudgetTarget,
                        )
                    }
                    Box(Modifier.weight(1f / 2f)) {
                        InputSelectEnum(
                            label = "Currency",
                            options = Currency.values().toList(),
                            initial = Currency.USD,
                            mapper = { Currency.valueOf(it) },
                            onSelected = { currency = it },
                            enabled = addBudgetTarget,
                        )
                    }
                }
            }
            item {
                Box(Modifier.padding(horizontal = 8.dp)) {
                    InputSelectEnum(
                        label = "Period",
                        options = TimePeriod.values().toList(),
                        initial = TimePeriod.DAILY,
                        mapper = { TimePeriod.valueOf(it) },
                        onSelected = { period = it },
                        enabled = addBudgetTarget,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun TagCreatorPreview() {
    TagCreator(open = true)
}

/*

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
 */
