package fr.uge.plutus.backend

import androidx.room.*
import java.util.*

@Entity(
    tableName = "transactions"
)
data class Transaction(
    val description: String?,
    val date: Date?,
    val amount: Double?,
    val bookId: UUID?,

    @PrimaryKey val uuid: UUID = UUID.randomUUID()
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

    @Query("SELECT * FROM transactions WHERE uuid = :id LIMIT 1")
    suspend fun findById(id: UUID): Transaction?
}