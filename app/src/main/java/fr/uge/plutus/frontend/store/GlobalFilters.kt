package fr.uge.plutus.frontend.store

import java.util.Date
import java.util.UUID

interface GlobalFilters {

    var description: String
    var fromDate: String
    var toDate: String
    var fromAmount: String
    var toAmount: String
    var tags: Set<UUID>
    var latitude: String
    var longitude: String
    var radius: String

    var mustApply: Boolean

    fun copy(block: GlobalFilters.() -> Unit): GlobalFiltersWrapper

}

class GlobalFiltersWrapper private constructor(
    private val filters: GlobalFilters
) : GlobalFilters by filters {

    constructor() : this(GlobalFilterImpl())

    override fun copy(block: GlobalFilters.() -> Unit): GlobalFiltersWrapper =
        GlobalFiltersWrapper(filters.apply(block))

}

class GlobalFilterImpl : GlobalFilters {

    override var description: String = ""
    override var fromDate: String = ""
    override var toDate: String = ""
    override var fromAmount: String = ""
    override var toAmount: String = ""
    override var tags: Set<UUID> = emptySet()
    override var latitude: String = ""
    override var longitude: String = ""
    override var radius: String = ""
    override var mustApply: Boolean = true

    override fun copy(block: GlobalFilters.() -> Unit): GlobalFiltersWrapper {
        throw UnsupportedOperationException("Cannot copy a GlobalFilterImpl")
    }

}