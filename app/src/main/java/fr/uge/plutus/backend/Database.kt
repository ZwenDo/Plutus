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
        Book::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(UUIDConverter::class, DateConverter::class)
abstract class Database : RoomDatabase() {

    abstract fun books(): BookDao

    companion object {

        private lateinit var INSTANCE: Database

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
