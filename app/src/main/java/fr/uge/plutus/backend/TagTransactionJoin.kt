package fr.uge.plutus.backend

import androidx.room.*
import java.util.*

@Entity(
    tableName = "tagTransactionJoin",
    primaryKeys = ["transactionId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )])
data class TagTransactionJoin (
    val transactionId: UUID,
    val tagId: UUID
)

@Dao
abstract class TagTransactionJoinDao {

    fun insert(transaction: Transaction, tag: Tag) {
        if (transaction.bookId == tag.bookId) {
            val tagTransactionJoin = TagTransactionJoin(transaction.id, tag.id)
            _insert(tagTransactionJoin)
        } else {
            throw IllegalArgumentException("Transaction and Tag must be in the same book")
        }
    }

    @Insert
    abstract fun _insert(tagTransactionJoin: TagTransactionJoin)

    @Delete
    abstract fun delete(transactionTag: TagTransactionJoin)

    //@Query("SELECT * FROM tagTransactionJoin WHERE transactionId = :transactionId")
    //abstract fun findTagByTransactionId(transactionId: UUID): List<TagTransactionJoin>

    //@Query("SELECT * FROM tagTransactionJoin WHERE tagId = :tagId")
    //abstract fun findTransactionByTagId(tagId: UUID): List<TagTransactionJoin>

    @Query("SELECT * FROM tag WHERE transactionId = :transactionId")
    abstract fun findTagByTransactionId(transactionId: UUID): List<TagWithTransactions>

    @Query("SELECT * FROM transaction WHERE tagId = :tagId")
    abstract fun findTransactionByTagId(tagId: UUID): List<TransactionWithTags>
}

data class TransactionWithTags(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "id",
        entityColumn = "tagId",
        associateBy = Junction(TagTransactionJoin::class)
    )
    val tags: List<Tag>

)

data class TagWithTransactions(
    @Embedded val tag: Tag,
    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId",
        associateBy = Junction(TagTransactionJoin::class)
    )
    val transactions: List<Transaction>
)