package fr.uge.plutus.backend

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date
import java.util.UUID

@androidx.room.Database(
    entities = [
        Book::class,
        Tag::class,
        Transaction::class,
        TagTransactionJoin::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(UUIDConverter::class, DateConverter::class)
abstract class Database : RoomDatabase() {

    abstract fun books(): BookDao

    abstract fun tags(): TagDao

    abstract fun transactions(): TransactionDao

    abstract fun tagTransactionJoin() : TagTransactionJoinDao

    companion object {

        lateinit var INSTANCE: Database
            private set

        fun init(context: Context) {
            require(!::INSTANCE.isInitialized) { "Database already initialized" }

            INSTANCE = Room.databaseBuilder(
                context,
                Database::class.java,
                "plutus.db"
            ).build()
        }

        fun close() {
            require(::INSTANCE.isInitialized) { "Database not initialized" }

            INSTANCE.close()
        }

        fun books(): BookDao {
            require(::INSTANCE.isInitialized) { "Database not initialized" }

            return INSTANCE.books()
        }

        fun tags() : TagDao {
            require(::INSTANCE.isInitialized) { "Database not initialized" }

            return INSTANCE.tags()
        }

        fun transactions() : TransactionDao {
            require(::INSTANCE.isInitialized) { "Database not initialized" }

            return INSTANCE.transactions()
        }

        fun tagTransactionJoin() : TagTransactionJoinDao {
            require(::INSTANCE.isInitialized) { "Database not initialized" }

            return INSTANCE.tagTransactionJoin()
        }
    }

}

private class UUIDConverter {

    @TypeConverter
    fun fromUUID(uuid: UUID): String = uuid.toString()

    @TypeConverter
    fun toUUID(uuid: String): UUID = UUID.fromString(uuid)

}

private class DateConverter {

    @TypeConverter
    fun fromDate(date: Date): Long = date.time

    @TypeConverter
    fun toDate(date: Long): Date = Date(date)

}
