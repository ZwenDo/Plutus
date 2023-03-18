package fr.uge.plutus.backend

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import java.util.UUID
import java.io.Serializable

@Entity(
    tableName = "books",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class Book(
    @ColumnInfo(name = "name") val name: String,

    @PrimaryKey val uuid: UUID = UUID.randomUUID()
) : Serializable

@Dao
interface BookDao {

    @Query("SELECT * FROM books")
    suspend fun getAll(): List<Book>

    @Query("SELECT * FROM books WHERE uuid = :bookId LIMIT 1")
    suspend fun findById(bookId: UUID): Book?

    @Query("SELECT * FROM books WHERE name LIKE :name LIMIT 1")
    suspend fun findByName(name: String): Book?

    @Insert
    suspend fun insert(vararg books: Book)

    @Delete
    suspend fun delete(vararg book: Book)

    @Update
    suspend fun update(book: Book)

}
