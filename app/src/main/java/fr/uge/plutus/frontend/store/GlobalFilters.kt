package fr.uge.plutus.frontend.store

import java.util.*

data class GlobalFilters(
    val description: String? = "",
    val fromDate: String? = "",
    val toDate: String? = "",
    val fromAmount: String? = "",
    val toAmount: String? = "",
    val tags: Set<UUID> = mutableSetOf(),
    val latitude: String? = "",
    val longitude: String? = "",
    val radius: String? = ""
)
