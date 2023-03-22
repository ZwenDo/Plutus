package fr.uge.plutus.frontend.view.tag

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.plutus.backend.BudgetTarget
import fr.uge.plutus.backend.Currency
import fr.uge.plutus.backend.TimePeriod
import fr.uge.plutus.frontend.component.form.InputSelectEnum
import fr.uge.plutus.frontend.component.form.InputText
import fr.uge.plutus.frontend.component.scaffold.Dialog

data class TagDTO(
    val name: String,
    val budgetTarget: BudgetTarget?,
)

@Composable
fun TagCreator(
    open: Boolean,
    onClose: (TagDTO?) -> Unit = {},
) {
    var name by rememberSaveable { mutableStateOf("") }
    var addBudgetTarget by rememberSaveable { mutableStateOf(false) }
    var budgetTarget by rememberSaveable { mutableStateOf(0.0) }
    var currency by rememberSaveable { mutableStateOf(Currency.USD) }
    var period by rememberSaveable { mutableStateOf(TimePeriod.DAILY) }

    fun reset() {
        name = ""
        addBudgetTarget = false
        budgetTarget = 0.0
        currency = Currency.USD
        period = TimePeriod.DAILY
    }

    Dialog(
        open = open,
        title = "Create a new tag",
        submitButtonText = "CREATE",
        onClose = { submit ->
            if (submit) {
                if (addBudgetTarget) {
                    onClose(TagDTO(name, BudgetTarget(budgetTarget, currency, period)))
                } else {
                    onClose(TagDTO(name, null))
                }
            } else {
                onClose(null)
            }
            reset()
        }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                Box(Modifier.padding(horizontal = 8.dp)) {
                    InputText(
                        label = "Name",
                        value = name,
                        onValueChange = { name = it },
                    )
                }
            }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(checked = addBudgetTarget, onCheckedChange = { addBudgetTarget = it })
                    Text(
                        text = "Budget target",
                        fontSize = 16.sp,
                    )
                }
            }
            item {
                Row(
                    Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.weight(1f / 2f)) {
                        InputText(
                            label = "Budget",
                            value = budgetTarget.toString(),
                            onValueChange = { budgetTarget = it.toDouble() },
                            enabled = addBudgetTarget,
                        )
                    }
                    Box(Modifier.weight(1f / 2f)) {
                        InputSelectEnum(
                            label = "Currency",
                            options = Currency.values().toList(),
                            initial = Currency.USD,
                            mapper = { Currency.valueOf(it) },
                            onSelected = { currency = it },
                            enabled = addBudgetTarget,
                        )
                    }
                }
            }
            item {
                Box(Modifier.padding(horizontal = 8.dp)) {
                    InputSelectEnum(
                        label = "Period",
                        options = TimePeriod.values().toList(),
                        initial = TimePeriod.DAILY,
                        mapper = { TimePeriod.valueOf(it) },
                        onSelected = { period = it },
                        enabled = addBudgetTarget,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun TagCreatorPreview() {
    TagCreator(open = true)
}
