package fr.uge.plutus.backend

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.json.JSONObject
import java.util.*

@androidx.room.Database(
    entities = [
        Book::class,
        Tag::class,
        Transaction::class,
        TagTransactionJoin::class,
        Filter::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    UUIDConverter::class,
    DateConverter::class,
    MapConverter::class,
    SetConverter::class
)
abstract class Database : RoomDatabase() {

    abstract fun books(): BookDao

    abstract fun tags(): TagDao

    abstract fun transactions(): TransactionDao

    abstract fun tagTransactionJoin(): TagTransactionJoinDao

    abstract fun filters(): FilterDao

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

        fun tags(): TagDao {
            require(::INSTANCE.isInitialized) { "Database not initialized" }

            return INSTANCE.tags()
        }

        fun transactions(): TransactionDao {
            require(::INSTANCE.isInitialized) { "Database not initialized" }

            return INSTANCE.transactions()
        }

        fun tagTransactionJoin(): TagTransactionJoinDao {
            require(::INSTANCE.isInitialized) { "Database not initialized" }

            return INSTANCE.tagTransactionJoin()
        }

        fun filters(): FilterDao {
            require(::INSTANCE.isInitialized) { "Database not initialized" }

            return INSTANCE.filters()
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

private class MapConverter {
    @TypeConverter
    fun fromMap(map: Map<String, String>): String {
        val json = JSONObject()
        map.forEach { (key, value) ->
            json.put(key, value)
        }
        return json.toString()
    }

    @TypeConverter
    fun toMap(map: String): Map<String, String> {
        val json = JSONObject(map)
        val map = mutableMapOf<String, String>()
        json.keys().forEach { key ->
            map[key] = json.getString(key)
        }
        return map
    }
}

private class SetConverter {
    @TypeConverter
    fun fromSet(set: Set<String>): String = set.toString()

    @TypeConverter
    fun toSet(set: String): Set<String> {
        if (set.length > 2) {
            return set.substring(1, set.length - 1).split(", ").toMutableSet()
        } else {
            return mutableSetOf<String>()
        }
    }
}
