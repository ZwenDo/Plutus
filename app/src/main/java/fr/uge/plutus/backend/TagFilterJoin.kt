package fr.uge.plutus.backend

import androidx.room.*
import java.util.*

@Entity(
    tableName = "tag_filter_join",
    primaryKeys = ["tagId", "filterId"],
    foreignKeys = [
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["tagId"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Filter::class,
            parentColumns = ["filterId"],
            childColumns = ["filterId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class TagFilterJoin(
    val tagId: UUID,
    val filterId: UUID,
    val bookId: UUID
)

@Dao
interface TagFilterJoinDao {
    fun insert(filter: Filter, tag: Tag) {
        require(tag.bookId == filter.bookId) { "Tag and filter must belong to the same book" }

        val tagFilterJoin = TagFilterJoin(tag.tagId, filter.filterId, tag.bookId)
        _insert(tagFilterJoin)
    }

    @Insert
    abstract fun _insert(tagFilterJoin: TagFilterJoin)

    fun delete(filter: Filter, tag: Tag) {
        require(tag.bookId == filter.bookId) { "Tag and filter must belong to the same book" }

        val tagFilterJoin = TagFilterJoin(tag.tagId, filter.filterId, tag.bookId)
        _delete(tagFilterJoin)
    }

    @Delete
    abstract fun _delete(tagFilterJoin: TagFilterJoin)

    @Query("SELECT * FROM tag JOIN tag_filter_join ON tag.tagId = tag_filter_join.tagId WHERE tag_filter_join.filterId = :filterId")
    abstract fun findTagsByFilter(filterId: UUID): List<Tag>

    @Query("SELECT * FROM filters JOIN tag_filter_join ON filters.filterId = tag_filter_join.filterId WHERE tag_filter_join.tagId = :tagId")
    abstract fun findFiltersByTag(tagId: UUID): List<Filter>

    @Query("SELECT * FROM tag_filter_join WHERE bookId = :bookId")
    abstract fun findAllByBookId(bookId: UUID): List<TagFilterJoin>
}
