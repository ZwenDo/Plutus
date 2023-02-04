package fr.uge.plutus.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Calendar
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
fun String.toDateOrNull(): Date? {
    val parts = split("/").map(String::toIntOrNull)
    if (parts.size != 3 || parts.any { it == null }) return null
    val (day, month, year) = parts.mapNotNull { it }
    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, day)
    return Date.from(calendar.toInstant())
}

fun Date.toStringFormatted(): String {
    val calendar = Calendar.getInstance()
    calendar.time = this
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)
    return "$day/$month/$year"
}
