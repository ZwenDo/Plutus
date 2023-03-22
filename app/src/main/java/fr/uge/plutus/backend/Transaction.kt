package fr.uge.plutus.backend

import android.database.sqlite.SQLiteConstraintException
import androidx.room.*
import java.io.Serializable
import java.util.*

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
data class Transaction(
    val description: String,
    val date: Date,
    val amount: Double,
    val bookId: UUID,
    val currency: Currency = Currency.USD,
    val latitude: Double? = null,
    val longitude: Double? = null,

    @PrimaryKey val transactionId: UUID = UUID.randomUUID()
) : Serializable {

    suspend fun attachments(database: Database? = null): List<Attachment> {
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

    @Query(
        """
        SELECT * 
        FROM transactions
        JOIN tag_transaction_join 
        ON transactions.transactionId = tag_transaction_join.transactionId
        WHERE bookId = :bookId
        AND tagId IN (:tags)
        """
    )
    suspend fun findAllByBookIdWithTags(bookId: UUID, tags: Set<UUID>): List<Transaction>

    @Query("SELECT * FROM transactions WHERE transactionId = :id LIMIT 1")
    suspend fun findById(id: UUID): Transaction?

    suspend fun upsert(transaction: Transaction) = try {
        insert(transaction)
    } catch (e: SQLiteConstraintException) {
        update(transaction)
    }

    @Query("SELECT * FROM transactions NATURAL JOIN tag_transaction_join WHERE date BETWEEN :start AND :end AND bookId = :bookId AND tagId = :tagId")
    suspend fun findByBookIdAndDateRangeAndTagId(
        bookId: UUID,
        start: Date,
        end: Date,
        tagId: UUID
    ): List<Transaction>
}
