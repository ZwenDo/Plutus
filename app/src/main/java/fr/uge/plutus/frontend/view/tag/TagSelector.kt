package fr.uge.plutus.frontend.view.tag

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.uge.plutus.backend.Tag
import fr.uge.plutus.frontend.component.scaffold.Dialog
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TagSelector(
    open: Boolean,
    tags: List<Tag>,
    selectedTags: Set<UUID> = emptySet(),
    onClose: (List<Tag>?) -> Unit = {},
) {
    val selection = remember { mutableStateMapOf(*selectedTags.map { it to Unit }.toTypedArray()) }

    LaunchedEffect(selectedTags, open) {
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

    Dialog(open = open, title = "Select tags", onClose = { submit ->
        if (submit) {
            onClose(tags.filter { selection.contains(it.tagId) })
        } else {
            onClose(null)
        }
    }) {
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .scrollable(rememberScrollState(), orientation = Orientation.Vertical),
        ) {
            items(tags) { tag ->
                Surface(onClick = { toggleTag(tag.tagId) }) {
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
    }
}
