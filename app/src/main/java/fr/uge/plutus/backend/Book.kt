package fr.uge.plutus.backend

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.*
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
abstract class BookDao {

    @Query("SELECT * FROM books")
    abstract suspend fun findAll(): List<Book>

    @Query("SELECT * FROM books WHERE uuid = :bookId LIMIT 1")
    abstract suspend fun findById(bookId: UUID): Book?

    @Query("SELECT * FROM books WHERE name LIKE :name LIMIT 1")
    abstract suspend fun findByName(name: String): Book?

    @Insert
    abstract suspend fun insert(vararg books: Book)

    @Delete
    abstract suspend fun delete(vararg book: Book)

    @Update
    abstract suspend fun update(book: Book)

    suspend fun upsert(book: Book) = try {
        insert(book)
        Log.d("YEP", "inserted: $book")
    } catch (e: SQLiteConstraintException) {
        Log.d("YEP", "updated: $book")
        update(book)
    }

    suspend fun copy(book: Book, newName: String, database: Database? = null): Book {
        val newBook = book.copy(uuid = UUID.randomUUID(), name = newName)
        insert(newBook) // insert book copy

        val transactionDao = database?.transactions() ?: Database.transactions()
        val tagsPerTransactionDao = database?.tagTransactionJoin()
            ?: Database.tagTransactionJoin()
        val tagDao = database?.tags() ?: Database.tags()

        val tagsMap = mutableMapOf<Pair<String, TagType>, Tag>()
        tagDao.findByBookId(book.uuid)
            .forEach { tag ->
                val tagCopy = tag.copy(bookId = newBook.uuid, tagId = UUID.randomUUID())
                tagDao._insert(tagCopy)
                tagsMap[tag.name to tag.type] = tagCopy
            }

        transactionDao
            .findAllByBookId(book.uuid)
            .forEach { transaction ->
                val newTransaction = transaction.copy(
                    transactionId = UUID.randomUUID(),
                    bookId = newBook.uuid
                )
                transactionDao.insert(newTransaction) // insert transaction copy
                tagsPerTransactionDao
                    .findTagsByTransactionId(transaction.transactionId)
                    .forEach {
                        val tagCopy = tagsMap[it.name to it.type]!!
                        tagsPerTransactionDao.insert(newTransaction, tagCopy)
                    }
            }
        return newBook
    }

}
