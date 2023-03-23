package fr.uge.plutus.frontend.view.search

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.R
import fr.uge.plutus.backend.*
import fr.uge.plutus.frontend.component.form.InputDate
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.component.scaffold.Dialog
import fr.uge.plutus.frontend.store.GlobalFilters
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.tag.TagSelector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    TextButton(onClick = {
                        onResetFilter()
                        errors = emptyMap()
                        coroutineScope.launch {
                            globalState.scaffoldState.drawerState.close()
                        }
                    }) {
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
                        InputDate(
                            label = "From",
                            value = globalFilters.fromDate,
                            errorMessage = errors[FilterFields.FROM_DATE]
                        ) {
                            updateFilters(globalFilters.copy { fromDate = it })
                        }
                        InputDate(
                            label = "To",
                            value = globalFilters.toDate,
                            errorMessage = errors[FilterFields.TO_DATE]
                        ) {
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
                        enabled = !globalFilters.isEmpty,
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
        if (tags != null) {
            globalState.globalFilters = globalState.globalFilters.copy {
                this.tags = tags.mapTo(mutableSetOf()) { it.tagId }
            }
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

    Dialog(
        title = "Save filter",
        onClose = { submit ->
            if (submit) {
                if (filterName.isNotBlank()) {
                    buttonClicked = true
                } else {
                    errorMessage = "Filter name cannot be blank"
                }
            } else {
                onDismiss()
            }
        },
        open = true,
        submitButtonText = "SAVE",
    ) {
        Column(
            modifier = Modifier.padding(24.dp, 16.dp),
        ) {
            InputText(
                label = "Filter name",
                value = filterName,
                errorMessage = errorMessage,
            ) {
                errorMessage = null
                filterName = it
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterLoadComponent(onDismiss: () -> Unit) {
    val globalState = globalState()
    val filters = remember { mutableStateListOf<Filter>() }
    var toImport by remember { mutableStateOf<Filter?>(null) }
    var toDelete by remember { mutableStateOf<Filter?>(null) }

    LaunchedEffect(Unit) {
        filters += Database
            .filters()
            .findAllByBookId(globalState.currentBook!!.uuid)
    }

    LaunchedEffect(toDelete) {
        if (toDelete == null) return@LaunchedEffect

        Database.filters().delete(toDelete!!)
        filters.remove(toDelete!!)
        toDelete = null
    }

    LaunchedEffect(toImport) {
        if (toImport == null) return@LaunchedEffect

        globalState.globalFilters = toImport!!.toGlobalFilters()
        onDismiss()
    }

    Dialog(
        title = "Load filter",
        onClose = { onDismiss() },
        open = true,
        displaySubmitButton = false,
        cancelButtonText = "CLOSE"
    ) {
        Box(Modifier.height(300.dp)) {
            if (filters.isEmpty()) {
                Text(
                    modifier = Modifier.padding(24.dp, 64.dp).fillMaxWidth(),
                    text = "No filters found",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center,
                )
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .scrollable(rememberScrollState(), orientation = Orientation.Vertical),
                ) {
                    items(filters) {
                        Surface(onClick = { toImport = it }) {
                            Column {
                                Row(
                                    Modifier.padding(24.dp, 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = it.name,
                                    )
                                    IconButton(onClick = { toDelete = it }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.close),
                                            contentDescription = null,
                                        )
                                    }
                                }
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}