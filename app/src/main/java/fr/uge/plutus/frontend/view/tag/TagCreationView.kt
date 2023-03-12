package fr.uge.plutus.frontend.view.tag

import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.Tag
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.store.GlobalState


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TagCreationView(onExit: () -> Unit) {
    val currentBook = GlobalState.currentBook
    val context = LocalContext.current
    var isOpen by rememberSaveable { mutableStateOf(false) }
    var tagName by rememberSaveable { mutableStateOf("") }
    var creating by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val options = listOf("Tag 1 ", "Tag 2 ", "Tag 2")
    var selectedOptionIndex by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(creating) {
        if (!creating) return@LaunchedEffect

        if (tagName.isBlank()) {
            errorMessage = "Tag name cannot be empty"
            creating = false
            return@LaunchedEffect
        }

        try {
            Database.tags().insert(tagName, currentBook!!.uuid)
            Toast.makeText(context, "Tag created", Toast.LENGTH_SHORT).show()
            onExit()
        } catch (e: SQLiteConstraintException) {
            errorMessage = "Tag name already exist"
        }

        creating = false
    }

    if (isOpen) {
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
                    InputText(label = "Create a new tag", value = tagName) { tagName = it }
                    Box(modifier = Modifier.wrapContentSize()) {
                        Button(onClick = { expanded = true }) {
                            Text(text = "Select an existing tag")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            options.forEachIndexed() { index, option ->
                                DropdownMenuItem(onClick = {
                                    selectedOptionIndex = index
                                    expanded = false
                                }) {
                                    Text(text = option)
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {
                        Button(onClick = { creating = true }) {
                            Text(text = "CREATE", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { isOpen = false },
                        ) {
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

