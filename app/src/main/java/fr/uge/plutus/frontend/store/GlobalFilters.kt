package fr.uge.plutus.frontend.store

import java.util.*

data class GlobalFilters(
    val description: String? = "",
    val fromDate: Date? = Date(),
    val toDate: Date? = Date(),
    val fromAmount: Double? = 0.0,
    val toAmount: Double? = 0.0,
    val tags: Set<UUID> = mutableSetOf(),
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val radius: Double? = 0.0
)
