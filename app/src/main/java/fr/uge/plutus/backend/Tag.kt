package fr.uge.plutus.backend

import androidx.room.*
import java.util.*

enum class TagType {
    INFO,
    INCOME,
    EXPENSE,
    TRANSFER,
}

@Entity(
    tableName = "tag",
    foreignKeys = [
        ForeignKey(entity = Book::class, parentColumns = ["uuid"], childColumns = ["bookId"], onDelete = ForeignKey.CASCADE
        )],
    indices = [
        Index(value = ["name", "bookId", "type"], unique = true
        )]
)

data class Tag(
    val name: String?,
    val type: TagType?,
    val bookId: UUID?,
    @PrimaryKey val id: UUID = UUID.randomUUID()
)
/*
fun addTag(tagName: String, book: Book) {
    when {
        tagName.startsWith("-") -> {
            Tag(tagName, TagType.EXPENSE, book.uuid)
        }
        tagName.startsWith("+") -> {
            Tag(tagName, TagType.INCOME, book.uuid)
        }
        tagName.startsWith("=") -> {
            Tag(tagName, TagType.TRANSFER, book.uuid)
        }
        else -> {
            Tag(tagName, TagType.INFO, book.uuid)
        }
    }
}
 */

@Dao
interface TagDao {

    @Insert
    fun insert(tag: Tag)

    @Delete
    fun delete(tag: Tag)

    @Query("SELECT * FROM tag WHERE name = :name AND bookId = :bookId")
    fun findByName(name: String): List<Tag>

    @Query("SELECT * FROM tag WHERE bookId = :bookId")
    fun findByBookId(bookId: Int): List<Tag>
}