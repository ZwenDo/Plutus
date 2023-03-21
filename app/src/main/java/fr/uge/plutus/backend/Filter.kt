package fr.uge.plutus.backend

import androidx.room.*
import java.util.*
import java.io.Serializable

enum class Criteria(val value: String) {
    MIN_AMOUNT("minAmount"),
    MAX_AMOUNT("maxAmount"),
    CURRENCY("currency"),
    MIN_DATE("minDate"),
    MAX_DATE("maxDate"),
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

    data class Builder(
        val name: String,
        val bookId: UUID
    ) {
        private var minAmount: Double? = null
        private var maxAmount: Double? = null
        private var currency: Currency? = null
        private var minDate: Date? = null
        private var maxDate: Date? = null

        fun minAmount(minAmount: Double) = apply { this.minAmount = minAmount }
        fun maxAmount(maxAmount: Double) = apply { this.maxAmount = maxAmount }
        fun currency(currency: Currency) = apply { this.currency = currency }
        fun minDate(minDate: Date) = apply { this.minDate = minDate }
        fun maxDate(maxDate: Date) = apply { this.maxDate = maxDate }

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
}
