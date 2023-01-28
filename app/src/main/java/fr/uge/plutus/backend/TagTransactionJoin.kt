package fr.uge.plutus.backend

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
data class TagTransactionJoin (
    val transactionId: UUID,

    val tagId: UUID
)

@Dao
abstract class TagTransactionJoinDao {

    fun insert(transaction: Transaction, tag: Tag) {
        require(transaction.bookId == tag.bookId) { "Transaction and tag must belong to the same book" }
        val tagTransactionJoin = TagTransactionJoin(transaction.transactionId, tag.tagId)
        _insert(tagTransactionJoin)
    }

    @Insert
    abstract fun _insert(tagTransactionJoin: TagTransactionJoin)

    @Delete
    abstract fun delete(transaction: Transaction, tag: Tag)

    @androidx.room.Transaction
    @Query("SELECT * FROM tag JOIN tag_transaction_join ON tag.tagId = tag_transaction_join.tagId WHERE tag_transaction_join.transactionId = :transactionId")
    abstract fun findTagsByTransactionId(transactionId: UUID): List<Tag>

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions JOIN tag_transaction_join ON transactions.transactionId = tag_transaction_join.transactionId WHERE tag_transaction_join.tagId = :tagId")
    abstract fun findTransactionByTagId(tagId: UUID): List<Transaction>

    @Query("SELECT * FROM tag_transaction_join")
    abstract fun findAll(): List<TagTransactionJoin>
}
