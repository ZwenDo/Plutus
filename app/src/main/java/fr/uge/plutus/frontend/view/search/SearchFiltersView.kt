package fr.uge.plutus.frontend.view.search

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.R
import fr.uge.plutus.frontend.component.form.InputDate
import fr.uge.plutus.frontend.component.form.InputText

@Composable
fun SearchFiltersView(
    onApplyFilter: () -> Unit = {},
    onResetFilter: () -> Unit = {},
    onSaveFilter: () -> Unit = {},
    onLoadFilter: () -> Unit = {}
) {
    var descriptionFilter by remember { mutableStateOf("") }
    var fromFilter by remember { mutableStateOf("") }
    var toFilter by remember { mutableStateOf("") }

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
                    Column(Modifier.padding(16.dp)) {
                        Text("Transaction details", style = MaterialTheme.typography.h6)
                        Spacer(modifier = Modifier.height(8.dp))
                        InputText(
                            label = "Description",
                            value = descriptionFilter,
                            onValueChange = { descriptionFilter = it }
                        )
                    }
                    Divider()
                }
            }
            item {
                Column {
                    Column(Modifier.padding(16.dp)) {
                        Text("Date range", style = MaterialTheme.typography.h6)
                        Spacer(modifier = Modifier.height(8.dp))
                        InputDate(label = "From", onValueChange = { fromFilter = it })
                        InputDate(label = "To", onValueChange = { toFilter = it })
                    }
                    Divider()
                }
            }
            item {
                Column {
                    Column(Modifier.padding(16.dp)) {
                        Text("Tags", style = MaterialTheme.typography.h6)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Coming soon...", style = MaterialTheme.typography.caption)
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

@Preview(showBackground = true)
@Composable
fun SearchFiltersViewPreview() {
    SearchFiltersView()
}
