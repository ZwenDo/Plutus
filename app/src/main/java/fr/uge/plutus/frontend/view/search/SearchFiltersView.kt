package fr.uge.plutus.frontend.view.search

import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.uge.plutus.R
import fr.uge.plutus.backend.*
import fr.uge.plutus.frontend.component.form.InputDate
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.store.GlobalFilters
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.ui.theme.Gray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

private enum class FilterFields {
    DESCRIPTION,
    FROM_DATE,
    TO_DATE,
    FROM_AMOUNT,
    TO_AMOUNT,
    AREA_LATITUDE,
    AREA_LONGITUDE,
    AREA_RADIUS,
}

val DATE_REGEX = Regex("""\d{1,2}/\d{1,2}/\d{4}""")

private fun checkFilters(filters: GlobalFilters): Map<FilterFields, String> {
    val errors = mutableMapOf<FilterFields, String>()
    if (filters.fromDate.isNotEmpty() && filters.toDate.isNotEmpty() && filters.fromDate > filters.toDate) {
        errors[FilterFields.FROM_DATE] = "From date must be before to date"
        errors[FilterFields.TO_DATE] = "To date must be after from date"
    }
    if (filters.fromDate.isNotEmpty() && !filters.fromDate.matches(DATE_REGEX)) {
        errors[FilterFields.FROM_DATE] = "From date must be valid"
    }
    if (filters.toDate.isNotEmpty() && !filters.toDate.matches(DATE_REGEX)) {
        errors[FilterFields.TO_DATE] = "To date must be valid"
    }
    // check if amount is a valid number
    if (filters.fromAmount.isNotEmpty() && filters.fromAmount.toDoubleOrNull() == null) {
        errors[FilterFields.FROM_AMOUNT] = "From amount must be a valid number"
    }
    if (filters.toAmount.isNotEmpty() && filters.toAmount.toDoubleOrNull() == null) {
        errors[FilterFields.TO_AMOUNT] = "To amount must be a valid number"
    }
    if (filters.fromAmount.isNotEmpty() && filters.toAmount.isNotEmpty() && filters.fromAmount.toDouble() > filters.toAmount.toDouble()) {
        errors[FilterFields.FROM_AMOUNT] = "From amount must be before to amount"
        errors[FilterFields.TO_AMOUNT] = "To amount must be after from amount"
    }
    if (filters.latitude.isNotEmpty() != filters.longitude.isNotEmpty()) {
        errors[FilterFields.AREA_LATITUDE] =
            "Latitude and longitude must be either both set of both unset"
        errors[FilterFields.AREA_LONGITUDE] =
            "Latitude and longitude must be either both set of both unset"
    }
    if (filters.latitude.isNotEmpty() && filters.longitude.isNotEmpty() && filters.radius.isEmpty()) {
        errors[FilterFields.AREA_RADIUS] = "Radius must be set if latitude and longitude are set"
    }
    if (filters.latitude.isNotEmpty() && filters.longitude.isNotEmpty() && filters.radius.isNotEmpty()) {
        val lat = filters.latitude.toDoubleOrNull()
        val lon = filters.longitude.toDoubleOrNull()
        val radius = filters.radius.toDoubleOrNull()
        if (lat == null || lat < -90 || lat > 90) {
            errors[FilterFields.AREA_LATITUDE] =
                "Latitude must be a valid number between -90 and 90"
        }
        if (lon == null || lon < -180 || lon > 180) {
            errors[FilterFields.AREA_LONGITUDE] =
                "Longitude must be a valid number between -180 and 180"
        }
        if (radius == null || radius < 0) {
            errors[FilterFields.AREA_RADIUS] = "Radius must be a valid number greater than 0"
        }
    }
    return errors
}

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
    val globalState = globalState()
    val coroutineScope = rememberCoroutineScope()
    var errors by remember { mutableStateOf(emptyMap<FilterFields, String>()) }

    fun updateFilters(globalFilters: GlobalFilters) {
        errors = emptyMap()
        onFilterUpdate(globalFilters)
    }

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
                        Text("Transaction details", style = MaterialTheme.typography.h6)
                        InputText(
                            label = "Description",
                            value = globalFilters.description,
                            errorMessage = errors[FilterFields.DESCRIPTION],
                        ) {
                            updateFilters(globalFilters.copy { description = it })
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
                        Text("Date range", style = MaterialTheme.typography.h6)
                        InputDate(label = "From", errorMessage = errors[FilterFields.FROM_DATE]) {
                            updateFilters(globalFilters.copy { fromDate = it })
                        }
                        InputDate(label = "To", errorMessage = errors[FilterFields.TO_DATE]) {
                            updateFilters(globalFilters.copy { toDate = it })
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
                        Text("Amount range", style = MaterialTheme.typography.h6)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f / 2f)) {
                                InputText(
                                    label = "Minimum",
                                    value = globalFilters.fromAmount,
                                    keyboardType = KeyboardType.Number,
                                    errorMessage = errors[FilterFields.FROM_AMOUNT],
                                ) {
                                    updateFilters(globalFilters.copy { fromAmount = it })
                                }
                            }
                            Box(Modifier.weight(1f / 2f)) {
                                InputText(
                                    label = "Maximum",
                                    value = globalFilters.toAmount,
                                    keyboardType = KeyboardType.Number,
                                    errorMessage = errors[FilterFields.TO_AMOUNT],
                                ) {
                                    updateFilters(globalFilters.copy { toAmount = it })
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
                        Text("Tags", style = MaterialTheme.typography.h6)
                        TextButton(onClick = { onOpenTagSelector() }) {
                            Text(text = "${globalFilters.tags.size} selected ")
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
                        Text("Area range", style = MaterialTheme.typography.h6)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f / 2f)) {
                                InputText(
                                    label = "Latitude",
                                    value = globalFilters.latitude,
                                    keyboardType = KeyboardType.Number,
                                    errorMessage = errors[FilterFields.AREA_LATITUDE],
                                ) {
                                    updateFilters(globalFilters.copy { latitude = it })
                                }
                            }
                            Box(Modifier.weight(1f / 2f)) {
                                InputText(
                                    label = "Longitude",
                                    value = globalFilters.longitude,
                                    keyboardType = KeyboardType.Number,
                                    errorMessage = errors[FilterFields.AREA_LONGITUDE],
                                ) {
                                    updateFilters(globalFilters.copy { longitude = it })
                                }
                            }
                        }
                        InputText(
                            label = "Radius",
                            value = globalFilters.radius,
                            keyboardType = KeyboardType.Number,
                            errorMessage = errors[FilterFields.AREA_RADIUS],
                        ) {
                            updateFilters(globalFilters.copy { radius = it })
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
                    onClick = {
                        errors = checkFilters(globalFilters)
                        if (errors.isEmpty()) {
                            coroutineScope.launch {
                                globalState.scaffoldState.drawerState.close()
                            }
                            onFilterUpdate(globalFilters.copy { mustApply = true })
                        }
                    }
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
    val globalFilters = GlobalFilters.new()
    SearchFilters(globalFilters)
}

@Composable
fun SearchFiltersView() {
    var openTagSelector by remember { mutableStateOf(false) }

    val globalState = globalState()
    var availableTags by remember { mutableStateOf(emptyList<Tag>()) }
    var saveClicked by remember { mutableStateOf(false) }
    var loadClicked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        globalState.globalFilters = GlobalFilters.new()
        withContext(Dispatchers.IO) {
            availableTags = Database.tags().findByBookId(globalState.currentBook!!.uuid)
        }
    }

    if (saveClicked) {
        FilterSaveComponent {
            saveClicked = false
        }
    }

    if (loadClicked) {
        FilterLoadComponent {
            loadClicked = false
        }
    }

    TagSelector(
        open = openTagSelector,
        tags = availableTags,
        selectedTags = globalState.globalFilters.tags,
    ) { tags ->
        openTagSelector = false
        globalState.globalFilters = globalState.globalFilters.copy {
            this.tags = tags.mapTo(mutableSetOf()) { it.tagId }
        }
    }

    SearchFilters(
        globalFilters = globalState.globalFilters,
        onOpenTagSelector = { openTagSelector = true },
        onResetFilter = { globalState.globalFilters = GlobalFilters.new() },
        onSaveFilter = { saveClicked = true },
        onLoadFilter = { loadClicked = true },
    ) {
        globalState.globalFilters = it
    }
}

@Composable
fun FilterSaveComponent(onDismiss: () -> Unit) {
    var filterName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var buttonClicked by remember { mutableStateOf(false) }
    val globalState = globalState()

    LaunchedEffect(buttonClicked) {
        if (!buttonClicked) return@LaunchedEffect

        Database
            .filters()
            .insertFromGlobalFilters(
                filterName,
                globalState.currentBook!!.uuid,
                globalState.globalFilters
            )

        onDismiss()
    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(shape = RoundedCornerShape(8.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Save filter",
                    style = MaterialTheme.typography.h6,
                )
                InputText(
                    label = "Filter name",
                    value = filterName,
                    errorMessage = errorMessage,
                ) {
                    errorMessage = null
                    filterName = it
                }
                Button(
                    onClick = {
                        if (filterName.isNotBlank()) {
                            buttonClicked = true
                        } else {
                            errorMessage = "Filter name cannot be blank"
                        }
                    }
                ) {
                    Text(text = "Save")
                }
            }
        }
    }
}

@Composable
fun FilterLoadComponent(onDismiss: () -> Unit) {
    val globalState = globalState()
    var filters by remember { mutableStateOf(listOf<Filter>()) }
    var toImport by remember { mutableStateOf<Filter?>(null) }

    LaunchedEffect(Unit) {
        filters = Database
            .filters()
            .findAllByBookId(globalState.currentBook!!.uuid)
        Log.d("YEP", "Filters: $filters")
    }

    LaunchedEffect(toImport) {
        if (toImport == null) return@LaunchedEffect

        globalState.globalFilters = toImport!!.toGlobalFilters()
        onDismiss()
    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(shape = RoundedCornerShape(8.dp)) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Load filter",
                    style = MaterialTheme.typography.h6,
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .scrollable(rememberScrollState(), orientation = Orientation.Vertical),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(filters) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .border(1.dp, Gray, RoundedCornerShape(8.dp))
                                .padding(4.dp)
                                .clickable {
                                    toImport = it
                                },
                            textAlign = TextAlign.Center,
                            text = it.name
                        )
                    }
                }
            }
        }
    }
}