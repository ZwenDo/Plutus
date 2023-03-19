package fr.uge.plutus.frontend.view.attachment

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import fr.uge.plutus.backend.Attachment
import fr.uge.plutus.ui.theme.Gray
import java.util.*


@Composable
fun AttachmentCreationView(
    attachments: MutableMap<UUID, Attachment>,
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        it.data?.data?.let { uri ->
            val attachment = Attachment(
                UUID.randomUUID(),
                uri,
                uri.lastPathSegment ?: uri.toString()
            )
            attachments[attachment.id] = attachment
        }
    }

    Row {
        // TODO : add picture/video/audio buttons
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val intent = Intent(
                    Intent.ACTION_OPEN_DOCUMENT,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                    .apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                launcher.launch(intent)
            },
        ) {
            Text(text = "Add attachment from storage")
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .border(1.dp, Gray, MaterialTheme.shapes.small)
    ) {
        LazyColumn(
            modifier = Modifier
                .scrollable(
                    rememberScrollState(),
                    orientation = Orientation.Vertical
                )
                .fillMaxWidth()
                .height(150.dp),
            content = {
                val iterator = attachments.iterator()
                items(attachments.size) { _ ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (id, attachment) = iterator.next()
                        Button(
                            onClick = { attachments -= id },
                            modifier = Modifier
                                .scale(0.5f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                        TextField(
                            value = attachment.name,
                            modifier = Modifier.fillMaxWidth(),
                            onValueChange = {
                                attachments[id] = attachment.copy(name = it.replace("\n", ""))
                            },
                            singleLine = true,
                        )
                    }
                }
            }
        )
    }
}