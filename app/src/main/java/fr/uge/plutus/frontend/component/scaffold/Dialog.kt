package fr.uge.plutus.frontend.component.scaffold

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Dialog(
    open: Boolean,
    title: String,
    submitButtonText: String = "OK",
    cancelButtonText: String = "CANCEL",
    onClose: (Boolean) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    if (open) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { onClose(false) }
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .height(64.dp)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.h6
                        )
                    }
                    Divider()
                    content()
                    Divider()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp, 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(
                            onClick = { onClose(false) }
                        ) {
                            Text(text = cancelButtonText)
                        }
                        TextButton(
                            onClick = { onClose(true) }
                        ) {
                            Text(text = submitButtonText)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun DialogPreview() {
    Dialog(open = true, title = "Test dialog") {
        Text(
            modifier = Modifier.padding(24.dp, 16.dp),
            text = "Test content"
        )
    }
}