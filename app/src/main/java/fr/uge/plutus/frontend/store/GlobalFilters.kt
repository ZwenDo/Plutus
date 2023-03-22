package fr.uge.plutus.frontend.store

import java.util.*

data class GlobalFilters(
    val description: String = "",
    val fromDate: Date = Date(),
    val toDate: Date = Date(),
    val tags: Set<UUID> = mutableSetOf()
)
