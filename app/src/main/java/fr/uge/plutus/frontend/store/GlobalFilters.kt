package fr.uge.plutus.frontend.store

import fr.uge.plutus.backend.Filter
import fr.uge.plutus.util.ifNotBlank
import fr.uge.plutus.util.toDateOrNull
import java.util.*

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


fun GlobalFilters.toFilter(name: String, bookId: UUID): Filter = Filter.create(name, bookId) { b ->
    description.ifNotBlank {
        b.description = it
    }

    fromDate.ifNotBlank {
        b.minDate = it.toDateOrNull()
    }

    toDate.ifNotBlank {
        b.maxDate = it.toDateOrNull()
    }

    fromAmount.ifNotBlank {
        b.minAmount = it.toDouble()
    }

    toAmount.ifNotBlank {
        b.maxAmount = it.toDouble()
    }

    latitude.ifNotBlank {
        b.latitude = it.toDouble()
    }

    longitude.ifNotBlank {
        b.longitude = it.toDouble()
    }

    radius.ifNotBlank {
        b.areaRange = it.toDouble()
    }
}