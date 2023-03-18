package fr.uge.plutus.backend

import androidx.room.*
import java.util.*
import java.io.Serializable

enum class Currency {
    EUR,
    USD
}

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = Book::class,
        parentColumns = ["uuid"],
        childColumns = ["bookId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Transaction (
    val description: String,
    val date: Date,
    val amount: Double,
    val bookId: UUID,
    val currency: Currency = Currency.USD,

    @PrimaryKey val transactionId: UUID = UUID.randomUUID()
) : Serializable {

    fun attachments(database: Database? = null): List<Attachment> {
        val dao = database?.attachments() ?: Database.attachments()
        return dao.findAllByTransactionId(transactionId)
    }

}

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

    @Query("SELECT * FROM transactions WHERE transactionId = :id LIMIT 1")
    suspend fun findById(id: UUID): Transaction?
}
