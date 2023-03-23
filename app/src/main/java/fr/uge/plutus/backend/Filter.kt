package fr.uge.plutus.backend

import androidx.room.*
import fr.uge.plutus.frontend.store.GlobalFilters
import fr.uge.plutus.util.ifNotBlank
import fr.uge.plutus.util.toDateOrNull
import java.io.Serializable
import java.util.*

enum class Criteria(val value: String) {
    MIN_AMOUNT("minAmount"),
    MAX_AMOUNT("maxAmount"),
    CURRENCY("currency"),
    MIN_DATE("minDate"),
    MAX_DATE("maxDate"),
    AREA_RANGE("areaRange"),
    LATITUDE("latitude"),
    LONGITUDE("longitude"),
    DESCRIPTION("description"),
}

@Entity(
    tableName = "filters",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["uuid"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )],
)
data class Filter(
    val name: String,
    val bookId: UUID,
    val criterias: Map<String, String> = emptyMap(),

    @PrimaryKey val filterId: UUID = UUID.randomUUID()
) : Serializable {

    fun getCriteriaValue(criteria: Criteria): String {
        return criterias[criteria.value] ?: ""
    }

    companion object {

        fun create(name: String, bookId: UUID, block: (Builder) -> Unit): Filter =
            Builder(name, bookId).apply(block).build()

    }

    class Builder(
        val name: String,
        val bookId: UUID
    ) {
        var description: String? = null
        var minAmount: Double? = null
        var maxAmount: Double? = null
        var currency: Currency? = null
        var minDate: Date? = null
        var maxDate: Date? = null
        var latitude: Double? = null
        var longitude: Double? = null
        var areaRange: Double? = null


        fun build(): Filter {
            val vMinAmount = minAmount
            val vMaxAmount = maxAmount
            val vCurrency = currency
            val vMinDate = minDate
            val vMaxDate = maxDate

            val criterias = HashMap<String, String>()

            description?.let {
                criterias[Criteria.DESCRIPTION.value] = it
            }

            if (vMinAmount != null && vMaxAmount != null) {
                require(vMinAmount <= vMaxAmount) { "Min amount must be lower than max amount" }
            }

            if (vMinAmount != null || vMaxAmount != null) {
                require(vCurrency != null) { "Currency must be specified" }
            }

            if (vMinDate != null && vMaxDate != null) {
                require(vMinDate <= vMaxDate) { "Min date must be lower than max date" }
            }

            if (vMinAmount != null) {
                criterias[Criteria.MIN_AMOUNT.value] = vMinAmount.toString()
            }
            if (vMaxAmount != null) {
                criterias[Criteria.MAX_AMOUNT.value] = vMaxAmount.toString()
            }
            if (vCurrency != null) {
                criterias[Criteria.CURRENCY.value] = vCurrency.toString()
            }
            if (vMinDate != null) {
                criterias[Criteria.MIN_DATE.value] = vMinDate.time.toString()
            }
            if (vMaxDate != null) {
                criterias[Criteria.MAX_DATE.value] = vMaxDate.time.toString()
            }
            latitude?.let {
                criterias[Criteria.LATITUDE.value] = it.toString()
            }
            longitude?.let {
                criterias[Criteria.LONGITUDE.value] = it.toString()
            }
            require((latitude == null && longitude == null) || areaRange != null) {
                "Area range must be specified if latitude or longitude is specified"
            }
            areaRange?.let {
                criterias[Criteria.AREA_RANGE.value] = it.toString()
            }

            if (criterias.isEmpty()) {
                throw IllegalArgumentException("Filter must have at least one criteria")
            }

            return Filter(name, bookId, criterias)
        }
    }
}

@Dao
interface FilterDao {
    @Insert
    suspend fun insert(vararg filters: Filter)

    @Delete
    suspend fun delete(filter: Filter)

    @Query("SELECT * FROM filters WHERE bookId = :bookId")
    suspend fun findAllByBookId(bookId: UUID): List<Filter>

    @Query("SELECT * FROM filters WHERE filterId = :id LIMIT 1")
    suspend fun findById(id: UUID): Filter?

    suspend fun insertFromGlobalFilters(
        name: String,
        bookId: UUID,
        globalFilters: GlobalFilters,
        database: Database? = null
    ) {
        val filter = globalFilters.toFilter(name, bookId)
        insert(filter)

        val tagFilterJoinDao = database?.tagFilterJoin() ?: Database.tagFilterJoin()

        globalFilters.tags.forEach { tag ->
            val tagFilterJoin = TagFilterJoin(tag, filter.filterId, bookId)
            tagFilterJoinDao._insert(tagFilterJoin)
        }
    }
}


private fun GlobalFilters.toFilter(name: String, bookId: UUID): Filter =
    Filter.create(name, bookId) { b ->
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

suspend fun Filter.toGlobalFilters(database: Database? = null): GlobalFilters {
    val tagFilterJoinDao = database?.tagFilterJoin() ?: Database.tagFilterJoin()
    val tagFilterJoins = tagFilterJoinDao.findTagsByFilter(filterId)

    return GlobalFilters.new {
        description = getCriteriaValue(Criteria.DESCRIPTION)

        fromDate = getCriteriaValue(Criteria.MIN_DATE)
        toDate = getCriteriaValue(Criteria.MAX_DATE)

        fromAmount = getCriteriaValue(Criteria.MIN_AMOUNT)
        toAmount = getCriteriaValue(Criteria.MAX_AMOUNT)

        latitude = getCriteriaValue(Criteria.LATITUDE)
        longitude = getCriteriaValue(Criteria.LONGITUDE)
        radius = getCriteriaValue(Criteria.AREA_RANGE)

        tags = tagFilterJoins.mapTo(mutableSetOf(), Tag::tagId)
    }
}