package fr.uge.plutus.frontend.component.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputText(
    label: String,
    value: String,
    placeholder: String = label,
    singleLine: Boolean = true,
    errorMessage: String? = null,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
) {
    val (focusRequester) = FocusRequester.createRefs()
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value,
            onValueChange = {
                val actual = if (singleLine) {
                    it.replace("\n", "")
                } else {
                    it
                }
                onValueChange(actual)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester = focusRequester),
            label = { Text(label) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            placeholder = { Text(placeholder) },
            singleLine = singleLine,
            isError = errorMessage != null,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                autoCorrect = !isPassword,
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusRequester.requestFocus() }
            ),
            leadingIcon = leadingIcon
        )
        if (errorMessage != null) {
            Text(
                errorMessage,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }
    }
}
