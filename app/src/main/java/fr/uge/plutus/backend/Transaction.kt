package fr.uge.plutus.backend

import androidx.room.*
import java.util.*

enum class Currency {
    EUR,
    USD
}

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(entity = Book::class, parentColumns = ["uuid"], childColumns = ["bookId"], onDelete = ForeignKey.CASCADE)]
)
data class Transaction(
    val description: String?,
    val date: Date?,
    val amount: Double?,
    val bookId: UUID?,
    val currency: Currency? = Currency.USD,

    @PrimaryKey val id: UUID = UUID.randomUUID()
)

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(vararg transactions: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Update
    suspend fun update(vararg transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE bookId = :bookId")
    suspend fun findAllByBookId(bookId: UUID): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun findById(id: UUID): Transaction?
}