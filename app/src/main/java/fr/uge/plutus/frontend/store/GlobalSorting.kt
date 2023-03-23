package fr.uge.plutus.frontend.store

import androidx.compose.ui.res.stringResource
import fr.uge.plutus.R
import fr.uge.plutus.backend.Transaction

enum class SortField(
    val displayName: Int,
    val comparator: Comparator<Transaction>,
) {
    DATE(R.string.date, { t1, t2 -> t1.date.compareTo(t2.date) }),
    AMOUNT(R.string.amount, { t1, t2 -> t1.amount.compareTo(t2.amount) }),
}

class GlobalSorting(
    val field: SortField,
    val ascending: Boolean,
)

val GlobalSorting?.comparator: Comparator<Transaction>
    get() = if (this == null) {
        Comparator { _, _ -> 0 }
    } else {
        if (ascending) {
            field.comparator
        } else {
            field.comparator.reversed()
        }
    }

fun GlobalSorting?.replace(newField: SortField): GlobalSorting? = when {
    this == null -> GlobalSorting(newField, false) // Add sorting by this with descending order
    field == newField && ascending -> null // Remove sorting
    field == newField -> GlobalSorting(
        newField,
        true
    ) // Replace sorting by this with ascending order
    else -> GlobalSorting(newField, false) // Replace sorting by this with descending order
}