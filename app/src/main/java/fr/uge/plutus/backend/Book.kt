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

    suspend fun copy(book: Book, newName: String, database: Database = Database.INSTANCE): Book {
        val newBook = book.copy(uuid = UUID.randomUUID(), name = newName)
        insert(newBook) // insert book copy

        val transactionDao = database.transactions()
        val tagsPerTransactionDao = database.tagTransactionJoin()
        val tagDao = database.tags()

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
