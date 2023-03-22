package fr.uge.plutus.frontend.view.search

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.uge.plutus.R
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.Tag
import fr.uge.plutus.backend.TagType
import fr.uge.plutus.frontend.component.form.InputDate
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.store.GlobalFilters
import fr.uge.plutus.frontend.store.globalState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TagSelector(
    open: Boolean,
    tags: List<Tag>,
    selectedTags: Set<UUID> = emptySet(),
    onClose: (List<Tag>) -> Unit = {},
) {
    val selection = remember { mutableStateMapOf(*selectedTags.map { it to Unit }.toTypedArray()) }

    LaunchedEffect(selectedTags) {
        selection.clear()
        selection.putAll(selectedTags.map { it to Unit })
    }

    fun toggleTag(uuid: UUID) {
        if (selection.contains(uuid)) {
            selection.remove(uuid)
        } else {
            selection[uuid] = Unit
        }
    }

    if (open) {
        Dialog(
            onDismissRequest = { onClose(emptyList()) }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    //verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .height(64.dp)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select tags", style = MaterialTheme.typography.h6
                        )
                    }
                    Divider()
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .scrollable(rememberScrollState(), orientation = Orientation.Vertical),
                    ) {
                        items(tags) { tag ->
                            Surface(onClick = {
                                toggleTag(tag.tagId)
                            }) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp, 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selection.contains(tag.tagId),
                                        onCheckedChange = { toggleTag(tag.tagId) },
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tag.name,
                                        style = MaterialTheme.typography.body1,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Divider()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colors.secondary
                            ),
                            onClick = { onClose(tags.filter { it.tagId in selection.keys }) }
                        ) {
                            Text(text = "OK")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TagSelectorPreview() {
    var open by remember { mutableStateOf(false) }
    val tags = listOf(
        Tag("Test tag 1", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 2", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 3", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 4", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 5", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 5", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 5", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 5", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 5", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 5", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 5", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 5", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 5", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 5", TagType.EXPENSE, UUID.randomUUID()),
        Tag("Test tag 5", TagType.EXPENSE, UUID.randomUUID()),
    )
    Button(onClick = { open = true }) {
        Text(text = "Open tag selector")
    }
    TagSelector(open, tags) {
        open = false
    }
}

@Composable
fun SearchFilters(
    globalFilters: GlobalFilters,
    onResetFilter: () -> Unit = {},
    onSaveFilter: () -> Unit = {},
    onLoadFilter: () -> Unit = {},
    onOpenTagSelector: () -> Unit = {},
    onFilterUpdate: (GlobalFilters) -> Unit = {},
) {
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .weight(1f)
                .scrollable(rememberScrollState(), orientation = Orientation.Vertical)
        ) {
            item {
                Column {
                    Text(
                        text = "Filters",
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.padding(16.dp)
                    )
                    Divider()
                }
            }
            item {
                Column {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Transaction details", style = MaterialTheme.typography.h6)
                        InputText(
                            label = "Description",
                            value = globalFilters.description,
                            onValueChange = {
                                onFilterUpdate(globalFilters.copy(description = it))
                            }
                        )
                    }
                    Divider()
                }
            }
            item {
                Column {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Date range", style = MaterialTheme.typography.h6)
                        InputDate(label = "From", onValueChange = { })
                        InputDate(label = "To", onValueChange = { })
                    }
                    Divider()
                }
            }
            item {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Tags", style = MaterialTheme.typography.h6)
                        TextButton(onClick = { onOpenTagSelector() }) {
                            Text(text = "${globalFilters.tags.size} selected ")
                        }
                    }
                    Divider()
                }
            }
        }
        Surface(elevation = 12.dp) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onSaveFilter() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.save),
                                contentDescription = null
                            )
                            Text(text = "Save filters")
                        }
                    }
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onLoadFilter() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.folder),
                                contentDescription = null
                            )
                            Text(text = "Load filters")
                        }
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onResetFilter() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.refresh),
                            contentDescription = null
                        )
                        Text(text = "Reset filters")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchFiltersView() {
    var openTagSelector by remember { mutableStateOf(false) }

    val globalState = globalState()
    var availableTags by remember { mutableStateOf(emptyList<Tag>()) }

    LaunchedEffect(Unit) {
        globalState.globalFilters = GlobalFilters()
        withContext(Dispatchers.IO) {
            availableTags = Database.tags().findByBookId(globalState.currentBook!!.uuid)
        }
    }

    TagSelector(
        open = openTagSelector,
        tags = availableTags,
        selectedTags = globalState.globalFilters.tags
    ) { tags ->
        openTagSelector = false
        globalState.globalFilters = globalState.globalFilters.copy(
            tags = tags.map { it.tagId }.toSet()
        )
    }

    SearchFilters(
        globalFilters = globalState.globalFilters,
        onOpenTagSelector = { openTagSelector = true },
        onResetFilter = { globalState.globalFilters = GlobalFilters() },
    ) {
        globalState.globalFilters = it
    }
}

@Preview(showBackground = true)
@Composable
fun SearchFiltersViewPreview() {
    val globalFilters = GlobalFilters()
    SearchFilters(globalFilters)
}
