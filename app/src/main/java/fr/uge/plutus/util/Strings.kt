package fr.uge.plutus.util

inline fun String.ifNotBlank(block: (String) -> Unit) {
    if (isNotBlank()) block(this)
}