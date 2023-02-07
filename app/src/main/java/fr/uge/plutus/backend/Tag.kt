package fr.uge.plutus.backend

import androidx.room.*
import java.util.*

enum class TagType(val code: String) {
    INFO(""),
    INCOME("+"),
    EXPENSE("-"),
    TRANSFER("="),
    ;

    companion object {
        fun tagFromString(tag: String): Pair<TagType, String> {
            require(tag.isNotBlank()) { "Tag name cannot be blank" }
            val type = when (tag[0]) {
                '-' -> EXPENSE
                '+' -> INCOME
                '=' -> TRANSFER
                else -> INFO
            }
            val name = if (type != INFO) {
                tag.drop(1).also { require(it.isNotBlank()) { "Tag name cannot be blank" } }
            } else {
                tag
            }
            return type to name
        }
    }
}

@Entity(
    tableName = "tag",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["uuid"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )],
    indices = [
        Index(
            value = ["name", "bookId", "type"], unique = true
        )]
)
data class Tag(
    val name: String?,
    val type: TagType?,
    val bookId: UUID?,
    @PrimaryKey val tagId: UUID = UUID.randomUUID()
)

@Dao
interface TagDao {

    @Insert
    fun _insert(tag: Tag)

    @Delete
    fun delete(tag: Tag)

    @Query("SELECT * FROM tag WHERE name = :name AND bookId = :bookId")
    fun findByName(name: String, bookId: UUID): List<Tag>

    @Query("SELECT * FROM tag WHERE bookId = :bookId")
    fun findByBookId(bookId: UUID): List<Tag>

    fun insert(tag: String, bookId: UUID): Tag {
        val (type, name) = TagType.tagFromString(tag)
        val tagEntity = Tag(name, type, bookId)
        _insert(tagEntity)
        return tagEntity
    }

    @Query("SELECT * FROM tag")
    fun findAll(): List<Tag>
}
