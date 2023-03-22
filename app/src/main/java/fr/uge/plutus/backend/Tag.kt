package fr.uge.plutus.backend

import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.room.*
import fr.uge.plutus.util.toDate
import fr.uge.plutus.util.toLocalDate
import java.io.Serializable
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit.*
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

        fun getTagTypeColor(tag: Tag): Color {
            return when (tag.type) {
                TagType.INCOME -> Color.hsl(105f, 1f, 0.75f)    // green
                TagType.EXPENSE -> Color.hsl(1f, 1f, 0.75f)     // red
                TagType.TRANSFER -> Color.hsl(60f, 1f, 0.75f)   // yellow
                else -> Color.hsl(181f, 1f, 0.75f)              // cyan
            }
        }
    }
}

enum class TimePeriod(
    val displayName: String
) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");

    @RequiresApi(Build.VERSION_CODES.O)
    fun toDateRange(date: Date): Pair<Date, Date> {
        val localDate = date.toLocalDate()
        return when (this) {
            DAILY -> {
                val start = localDate.atStartOfDay()
                val end = start.plus(1, DAYS).minusNanos(1)
                start to end
            }
            WEEKLY -> {
                val start = localDate.with(ChronoField.DAY_OF_WEEK, 1).atStartOfDay()
                val end = start.plus(1, WEEKS).minusNanos(1)
                start to end
            }
            MONTHLY -> {
                val start = localDate.with(ChronoField.DAY_OF_MONTH, 1).atStartOfDay()
                val end = start.plus(1, MONTHS).minusNanos(1)
                start to end
            }
            YEARLY -> {
                val start = localDate.with(ChronoField.DAY_OF_YEAR, 1).atStartOfDay()
                val end = start.plus(1, YEARS).minusNanos(1)
                start to end
            }
        }.let { (start, end) ->
            start.toDate() to end.toDate()
        }
    }
}

data class BudgetTarget(
    @ColumnInfo(name = "value") val value: Double,
    @ColumnInfo(name = "currency") val currency: Currency,
    @ColumnInfo(name = "time_period") val timePeriod: TimePeriod,
)

@Entity(
    tableName = "tag",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["uuid"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["name", "bookId", "type"], unique = true
        )]
)
data class Tag(
    val name: String,
    val type: TagType,
    val bookId: UUID,
    @Embedded val budgetTarget: BudgetTarget? = null,
    @PrimaryKey val tagId: UUID = UUID.randomUUID()
) : Serializable {
    val stringRepresentation: String
        get() = "${type.code}$name"
}


@Dao
interface TagDao {

    @Insert
    suspend fun _insert(tag: Tag)

    @Delete
    suspend fun delete(tag: Tag)

    @Query("SELECT * FROM tag WHERE name = :name AND bookId = :bookId")
    suspend fun findByName(name: String, bookId: UUID): List<Tag>

    @Query("SELECT * FROM tag WHERE bookId = :bookId")
    suspend fun findByBookId(bookId: UUID): List<Tag>

    suspend fun insert(tag: String, bookId: UUID, budgetTarget: BudgetTarget?): Tag {
        val (type, name) = TagType.tagFromString(tag)
        val tagEntity = Tag(name, type, bookId, budgetTarget)
        _insert(tagEntity)
        return tagEntity
    }

    @Query("SELECT * FROM tag")
    suspend fun findAll(): List<Tag>

    @Update
    suspend fun update(tag: Tag)

    suspend fun upsert(tag: Tag) { // this shit doesn't work natively
        try {
            _insert(tag)
        } catch (e: SQLiteConstraintException) {
            update(tag)
        }
    }

}
