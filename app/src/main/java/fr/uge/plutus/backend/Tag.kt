package fr.uge.plutus.backend

import androidx.room.*
import java.util.*

enum class TagType {
    INFO,
    INCOME,
    EXPENSE,
    TRANSFER,
}

@Entity(tableName = "tags",
    foreignKeys = [ForeignKey(entity = Book::class, parentColumns = ["uuid"], childColumns = ["bookId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["name", "bookId", "type"], unique = true)]
)

data class Tag(
    @PrimaryKey val uuid: UUID,
    val name: String,
    val type: TagType,
    val bookId: UUID
)

fun addTag(transaction: Transaction, tagName: String, book: Book) {
    when {
        tagName.startsWith("-") -> {
            val newTag = Tag(UUID.randomUUID(), tagName, TagType.EXPENSE, book.uuid)
            transaction.tags += newTag
        }
        tagName.startsWith("+") -> {
            val newTag = Tag(UUID.randomUUID(), tagName, TagType.INCOME, book.uuid)
            transaction.tags += newTag
        }
        tagName.startsWith("=") -> {
            val newTag = Tag(UUID.randomUUID(), tagName, TagType.TRANSFER, book.uuid)
            transaction.tags += newTag
        }
        else -> {
            val newTag = Tag(UUID.randomUUID(), tagName, TagType.INFO, book.uuid)
            transaction.tags += newTag
        }
    }
}

@Dao
interface TagDao {
    @Insert
    fun insert(tag: Tag)

    @Update
    fun update(tag: Tag)

    @Delete
    fun delete(tag: Tag)

    @Query("SELECT * FROM tags WHERE name = :name")
    fun findByName(name: String): Tag?

    @Query("SELECT * FROM tags WHERE bookId = :bookId")
    fun findByBookId(bookId: Int): List<Tag>
}


//Temporaire pour tester
class Transaction {
    var tags: List<Tag> = listOf()
    // autres propriétés
}