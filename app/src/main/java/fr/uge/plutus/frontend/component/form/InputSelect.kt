package fr.uge.plutus.frontend.component.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.toSize
import fr.uge.plutus.backend.Currency

@Composable
fun <T : Enum<T>> InputSelectEnum(
    label: String,
    options: List<T>,
    initial: T,
    mapper: (String) -> T,
    onSelected: (T) -> Unit
) {
    var actualWidth by rememberSaveable { mutableStateOf(0f) }
    var selected by rememberSaveable { mutableStateOf(initial) }
    var open by rememberSaveable { mutableStateOf(false) }
    val arrow = if (open) {
        Icons.Default.KeyboardArrowUp
    } else {
        Icons.Default.KeyboardArrowDown
    }

    Column {
        OutlinedTextField(
            selected.toString(),
            {
                selected = try {
                    mapper(it)
                } catch (e: IllegalArgumentException) {
                    selected
                }
            },
            Modifier
                .fillMaxWidth()
                .onGloballyPositioned { actualWidth = it.size.toSize().width },
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    arrow,
                    contentDescription = null,
                    modifier = Modifier.clickable { open = !open }
                )
            },
        )

        DropdownMenu(
            open,
            { open = false },
            Modifier.width(with(LocalDensity.current) {
                actualWidth.toDp()
            })
        ) {
            options.forEach { option ->
                DropdownMenuItem({
                    selected = option
                    onSelected(option)
                    open = false
                }) {
                    Text(option.toString())
                }
            }
        }
    }
}

@Composable
fun <T : Any> InputSelectCollection(
    label: String,
    options: Collection<T>,
    initial: T?,
    mapFromString: (String) -> T,
    mapToString: (T) -> String = Any::toString,
    onSelected: (T) -> Unit
) {
    var actualWidth by rememberSaveable { mutableStateOf(0f) }
    var selected by rememberSaveable { mutableStateOf(initial) }
    var open by rememberSaveable { mutableStateOf(false) }
    val arrow = if (open) {
        Icons.Default.KeyboardArrowUp
    } else {
        Icons.Default.KeyboardArrowDown
    }

    Column {
        OutlinedTextField(
            selected.toString(),
            {
                selected = try {
                    mapFromString(it)
                } catch (e: IllegalArgumentException) {
                    selected
                }

            },
            Modifier
                .fillMaxWidth()
                .onGloballyPositioned { actualWidth = it.size.toSize().width },
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    arrow,
                    contentDescription = null,
                    modifier = Modifier.clickable { open = !open }
                )
            },
        )

        DropdownMenu(
            open,
            { open = false },
            Modifier.width(with(LocalDensity.current) {
                actualWidth.toDp()
            })
        ) {
            options.forEach { option ->
                DropdownMenuItem({
                    selected = option
                    onSelected(option)
                    open = false
                }) {
                    Text(mapToString(option))
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        InputSelectEnum(
            label = "Currency",
            options = Currency.values().toList(),
            initial = Currency.USD,
            mapper = { Currency.valueOf(it) },
            onSelected = { }
        )
    }
}
