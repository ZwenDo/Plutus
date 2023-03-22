package fr.uge.plutus.backend

import android.database.sqlite.SQLiteConstraintException
import androidx.room.*
import java.util.*

@Entity(
    tableName = "tag_transaction_join",
    primaryKeys = ["transactionId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["transactionId"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["tagId"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TagTransactionJoin(
    val transactionId: UUID,

    val tagId: UUID
)

@Dao
abstract class TagTransactionJoinDao {

    suspend fun insert(transaction: Transaction, tag: Tag) {
        require(transaction.bookId == tag.bookId) { "Transaction and tag must belong to the same book" }
        val tagTransactionJoin = TagTransactionJoin(transaction.transactionId, tag.tagId)
        _insert(tagTransactionJoin)
    }

    @Insert
    abstract suspend fun _insert(tagTransactionJoin: TagTransactionJoin)

    @Update
    abstract suspend fun update(tagTransactionJoin: TagTransactionJoin)

    suspend fun delete(transaction: Transaction, tag: Tag) {
        require(transaction.bookId == tag.bookId) { "Transaction and tag must belong to the same book" }
        val tagTransactionJoin = TagTransactionJoin(transaction.transactionId, tag.tagId)
        _delete(tagTransactionJoin)
    }

    @Delete
    abstract suspend fun _delete(tagTransactionJoin: TagTransactionJoin)

    @Query("SELECT * FROM tag JOIN tag_transaction_join ON tag.tagId = tag_transaction_join.tagId WHERE tag_transaction_join.transactionId = :transactionId")
    abstract suspend fun findTagsByTransactionId(transactionId: UUID): List<Tag>

    @Query("SELECT tag.tagId FROM tag_transaction_join JOIN tag ON tag.tagId = tag_transaction_join.tagId WHERE tag_transaction_join.transactionId = :transactionId")
    abstract suspend fun findTagIdsByTransactionId(transactionId: UUID): List<UUID>


    @Query("SELECT * FROM transactions JOIN tag_transaction_join ON transactions.transactionId = tag_transaction_join.transactionId WHERE tag_transaction_join.tagId = :tagId")
    abstract suspend fun findTransactionByTagId(tagId: UUID): List<Transaction>

    @Query("SELECT * FROM tag_transaction_join")
    abstract suspend fun findAll(): List<TagTransactionJoin>

    suspend fun upsert(tagTransactionJoin: TagTransactionJoin) = try {
        _insert(tagTransactionJoin)
    } catch (e: SQLiteConstraintException) {
        update(tagTransactionJoin)
    }

}
