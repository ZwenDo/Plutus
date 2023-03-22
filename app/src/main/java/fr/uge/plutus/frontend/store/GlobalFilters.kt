package fr.uge.plutus.frontend.store

import java.util.*

class GlobalFiltersWrapper(
    var filters: GlobalFilters
) {

    inline fun copy(block: GlobalFilters.() -> Unit): GlobalFiltersWrapper =
        GlobalFiltersWrapper(filters.apply(block))

}

class GlobalFilters(
    var description: String? = null,
    var fromDate: String? = null,
    var toDate: String? = null,
    var fromAmount: String? = null,
    var toAmount: String? = null,
    val tags: MutableSet<UUID> = mutableSetOf(),
    var latitude: String? = null,
    var longitude: String? = null,
    var radius: String? = null
)
