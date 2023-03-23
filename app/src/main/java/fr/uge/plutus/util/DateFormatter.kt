package fr.uge.plutus.util

import java.text.DateFormat
import java.util.*

class DateFormatter {
    companion object {
        private val formatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())

        fun format(date: Date): String {
            return formatter.format(date)
        }
    }
}
