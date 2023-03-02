package fr.uge.plutus.util

import java.text.SimpleDateFormat
import java.util.*

class DateFormatter {
    companion object {
        private val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE)

        fun format(date: Date): String {
            return formatter.format(date)
        }
    }
}
