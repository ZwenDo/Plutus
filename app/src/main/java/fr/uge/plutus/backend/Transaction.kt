package fr.uge.plutus.backend

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import fr.uge.plutus.frontend.store.GlobalFilters
import fr.uge.plutus.util.ifNotBlank
import fr.uge.plutus.util.toDateOrNull
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

    @RawQuery
    suspend fun findFiltered(query: SupportSQLiteQuery): List<Transaction>

}


suspend fun TransactionDao.findWithGlobalFilters(
    bookId: UUID,
    filters: GlobalFilters,
    database: Database? = null
): List<Transaction> {
    val dao = database?.transactions() ?: Database.transactions()

    val args = mutableListOf<Any>()
    val query = buildString {
        append("SELECT * FROM transactions WHERE bookId = ?")
        args += bookId.toString()

        filters.description.ifNotBlank {
            append(" AND description LIKE ?")
            args += "%$it%"
        }

        filters.fromDate.ifNotBlank {
            append(" AND date >= ?")
            args += it.toDateOrNull()!!.time
        }

        filters.toDate.ifNotBlank {
            append(" AND date <= ?")
            args += it.toDateOrNull()!!.time
        }

        filters.latitude.ifNotBlank {
            append(" AND latitude BETWEEN ? AND ?")
            args += it.toDouble() - filters.radius.toDouble()
            args += it.toDouble() + filters.radius.toDouble()
        }

        filters.longitude.ifNotBlank {
            append(" AND longitude BETWEEN ? AND ?")
            args += it.toDouble() - filters.radius.toDouble()
            args += it.toDouble() + filters.radius.toDouble()
        }

        filters.fromAmount.ifNotBlank {
            append(" AND amount >= ?")
            args += it.toDouble()
        }

        filters.toAmount.ifNotBlank {
            append(" AND amount <= ?")
            args += it.toDouble()
        }

        filters.tags.forEach {
            append(" AND transactionId IN (SELECT transactionId FROM tag_transaction_join WHERE tagId = ?)")
            args += it.toString()
        }
    }

    val finalQuery = SimpleSQLiteQuery(query, args.toTypedArray())
    return dao.findFiltered(finalQuery)
}
