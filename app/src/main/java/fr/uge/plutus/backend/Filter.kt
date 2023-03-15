package fr.uge.plutus.backend

import androidx.room.*
import java.util.*
import java.io.Serializable

enum class Criteria(val value: String) {
    MINAMOUNT("minAmount"),
    MAXAMOUNT("maxAmount"),
    CURRENCY("currency"),
    MINDATE("minDate"),
    MAXDATE("maxDate"),
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
    val tags: Set<String> = emptySet(),

    @PrimaryKey val filterId: UUID = UUID.randomUUID()
) : Serializable {

    fun getCriteriaValue(criteria: Criteria): String {
        return criterias[criteria.value] ?: ""
    }

    data class Builder(
        var name: String,
        var bookId: UUID,
        var minAmount: Double? = null,
        var maxAmount: Double? = null,
        var currency: Currency? = null,
        var minDate: Date? = null,
        var maxDate: Date? = null,
        var tags: Set<Tag>? = null
    ) {

        fun minAmount(minAmount: Double) = apply { this.minAmount = minAmount }
        fun maxAmount(maxAmount: Double) = apply { this.maxAmount = maxAmount }
        fun currency(currency: Currency) = apply { this.currency = currency }
        fun minDate(minDate: Date) = apply { this.minDate = minDate }
        fun maxDate(maxDate: Date) = apply { this.maxDate = maxDate }
        fun tags(tags: Set<Tag>) = apply { this.tags = tags }

        fun build(): Filter {
            val vMinAmount = minAmount
            val vMaxAmount = maxAmount
            val vCurrency = currency
            val vMinDate = minDate
            val vMaxDate = maxDate

            val criterias = HashMap<String, String>()

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
                criterias[Criteria.MINAMOUNT.value] = vMinAmount.toString()
            }
            if (vMaxAmount != null) {
                criterias[Criteria.MAXAMOUNT.value] = vMaxAmount.toString()
            }
            if (vCurrency != null) {
                criterias[Criteria.CURRENCY.value] = vCurrency.toString()
            }
            if (vMinDate != null) {
                criterias[Criteria.MINDATE.value] = vMinDate.time.toString()
            }
            if (vMaxDate != null) {
                criterias[Criteria.MAXDATE.value] = vMaxDate.time.toString()
            }

            val t = tags?.map { it.name!! }?.toSet() ?: emptySet()

            if (t.isEmpty() && criterias.isEmpty()) {
                throw IllegalArgumentException("Filter must have at least one criteria or one tag")
            }

            return Filter(name, bookId, criterias, t)
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
}
