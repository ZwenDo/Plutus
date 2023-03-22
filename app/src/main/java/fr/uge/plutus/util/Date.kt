package fr.uge.plutus.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun String.isValidDate(): Boolean {
    val parts = split("/")
    if (parts.size != 3) return false
    val (day, month, year) = parts.map(String::toIntOrNull)
    return day in 1..31 && month in 1..12 && year in 0..9999
}


fun String.toDateOrNull(): Date? {
    if (!isValidDate()) return null
    val parts = split("/").map(String::toInt)
    val (day, month, year) = parts.map { it }
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

@RequiresApi(Build.VERSION_CODES.O)
fun Date.toLocalDate(): LocalDate = Instant.ofEpochMilli(time)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.toDate(): Date = Date.from(atZone(ZoneId.systemDefault()).toInstant())
