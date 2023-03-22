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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
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
import fr.uge.plutus.frontend.store.GlobalFiltersWrapper
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
    globalFilters: GlobalFiltersWrapper,
    onResetFilter: () -> Unit = {},
    onSaveFilter: () -> Unit = {},
    onLoadFilter: () -> Unit = {},
    onOpenTagSelector: () -> Unit = {},
    onFilterUpdate: (GlobalFiltersWrapper) -> Unit = {},
) {
    var filterDetails by rememberSaveable { mutableStateOf(false) }
    var filterDates by rememberSaveable { mutableStateOf(false) }
    var filterAmount by rememberSaveable { mutableStateOf(false) }
    var filterTags by rememberSaveable { mutableStateOf(false) }
    var filterArea by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Surface(elevation = 12.dp) {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f),
                        text = "Filters",
                        style = MaterialTheme.typography.h5
                    )
                    TextButton(onClick = { onResetFilter() }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.refresh),
                                contentDescription = null
                            )
                            Text(text = "RESET")
                        }
                    }
                }
            }
        }
        LazyColumn(
            Modifier
                .fillMaxSize()
                .weight(1f)
                .scrollable(rememberScrollState(), orientation = Orientation.Vertical)
        ) {
            item {
                Column {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = filterDetails, onCheckedChange = {
                                filterDetails = it
                            })
                            Text("Transaction details", style = MaterialTheme.typography.h6)
                        }
                        InputText(
                            label = "Description",
                            value = globalFilters.filters.description ?: "",
                            enabled = filterDetails
                        ) {
                            onFilterUpdate(globalFilters.copy { description = it })
                        }
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = filterDates, onCheckedChange = {
                                filterDates = it
                            })
                            Text("Date range", style = MaterialTheme.typography.h6)
                        }
                        InputDate(label = "From", enabled = filterDates) {
                            onFilterUpdate(globalFilters.copy { fromDate = it })
                        }
                        InputDate(label = "To", enabled = filterDates) {
                            onFilterUpdate(globalFilters.copy { toDate = it })
                        }
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = filterAmount, onCheckedChange = {
                                filterAmount = it
                            })
                            Text("Amount range", style = MaterialTheme.typography.h6)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f / 2f)) {
                                InputText(
                                    label = "Minimum",
                                    value = globalFilters.filters.fromAmount.toString(),
                                    keyboardType = KeyboardType.Number,
                                    enabled = filterAmount
                                ) {
                                    onFilterUpdate(globalFilters.copy { fromAmount = it })
                                }
                            }
                            Box(Modifier.weight(1f / 2f)) {
                                InputText(
                                    label = "Maximum",
                                    value = globalFilters.filters.toAmount.toString(),
                                    keyboardType = KeyboardType.Number,
                                    enabled = filterAmount
                                ) {
                                    onFilterUpdate(globalFilters.copy { toAmount = it })
                                }
                            }
                        }
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = filterTags, onCheckedChange = {
                                filterTags = it
                            })
                            Text("Tags", style = MaterialTheme.typography.h6)
                        }
                        TextButton(onClick = { onOpenTagSelector() }, enabled = filterTags) {
                            Text(text = "${globalFilters.filters.tags.size} selected ")
                        }
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = filterArea, onCheckedChange = {
                                filterArea = it
                            })
                            Text("Area range", style = MaterialTheme.typography.h6)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f / 2f)) {
                                InputText(
                                    label = "Latitude",
                                    value = globalFilters.filters.latitude ?: "0.0",
                                    keyboardType = KeyboardType.Number,
                                    enabled = filterArea
                                ) {
                                    onFilterUpdate(globalFilters.copy { latitude = it })
                                }
                            }
                            Box(Modifier.weight(1f / 2f)) {
                                InputText(
                                    label = "Longitude",
                                    value = globalFilters.filters.longitude ?: "0.0",
                                    keyboardType = KeyboardType.Number,
                                    enabled = filterArea
                                ) {
                                    onFilterUpdate(globalFilters.copy { longitude = it })
                                }
                            }
                        }
                        InputText(
                            label = "Radius",
                            value = globalFilters.filters.radius ?: "0.0",
                            keyboardType = KeyboardType.Number,
                            enabled = filterArea
                        ) {
                            onFilterUpdate(globalFilters.copy { radius = it })
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
                    onClick = { /* APPLY */ }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = null
                        )
                        Text(text = "Apply filters")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SearchFiltersPreview() {
    val globalFilters = GlobalFiltersWrapper()
    SearchFilters(globalFilters)
}


@Composable
fun SearchFiltersView() {
    var openTagSelector by remember { mutableStateOf(false) }

    val globalState = globalState()
    var availableTags by remember { mutableStateOf(emptyList<Tag>()) }

    LaunchedEffect(Unit) {
        globalState.globalFilters = GlobalFiltersWrapper()
        withContext(Dispatchers.IO) {
            availableTags = Database.tags().findByBookId(globalState.currentBook!!.uuid)
        }
    }

    TagSelector(
        open = openTagSelector,
        tags = availableTags,
        selectedTags = globalState.globalFilters.filters.tags
    ) { tags ->
        openTagSelector = false
        globalState.globalFilters = globalState.globalFilters.copy {
            this.tags = tags.mapTo(mutableSetOf()) { it.tagId }
        }
    }

    SearchFilters(
        globalFilters = globalState.globalFilters,
        onOpenTagSelector = { openTagSelector = true },
        onResetFilter = { globalState.globalFilters = GlobalFiltersWrapper() },
    ) {
        globalState.globalFilters = it
    }
}
