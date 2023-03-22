package fr.uge.plutus.backend

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class BooksTest {

    private lateinit var db: Database
    private lateinit var bookDao: BookDao
    private lateinit var tagDao: TagDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var tagTransactionsDao: TagTransactionJoinDao


    @Before
    fun init() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            Database::class.java
        ).build()
        bookDao = db.books()
        tagDao = db.tags()
        transactionDao = db.transactions()
        tagTransactionsDao = db.tagTransactionJoin()
    }

    @Test
    fun shouldCreateABookWithoutFailing() = runTest {
        val book = Book("Android For Dummies")
        bookDao.insert(book)

        val bookFromDb = bookDao.findByName("Android For Dummies")
        assertEquals(book, bookFromDb)
    }

    @Test
    fun shouldGetAllBooks() = runTest {
        val book = Book("Android For Dummies")
        bookDao.insert(book)
        val book2 = Book("Kotlin For Dummies")
        bookDao.insert(book2)

        val books = bookDao.findAll()
        assertEquals(2, books.size)
        assertTrue(book in books)
        assertTrue(book2 in books)
    }

    @Test
    fun shouldFailBecauseOfUniqueConstraint() = runTest {
        val book = Book("Android For Dummies")
        bookDao.insert(book)
        runCatching {
            val book2 = Book("Android For Dummies")
            bookDao.insert(book2)
        }.onSuccess {
            error("Should have failed")
        }.onFailure {
            assertTrue(it is SQLiteConstraintException)
        }
    }

    @Test
    fun shouldUpdateABook() = runTest {
        val book = Book("Android For Dummies")
        bookDao.insert(book)
        val bookFromDb = bookDao.findByName("Android For Dummies")!!
        assertEquals(book, bookFromDb)

        val updatedBook = bookFromDb.copy(name = "Android For Dummies 2nd Edition")
        bookDao.update(updatedBook)
        val updatedBookFromDb = bookDao.findByName("Android For Dummies 2nd Edition")
        assertEquals(updatedBook, updatedBookFromDb)
    }

    @Test
    fun shouldFailBecauseOfUniqueConstraintOnUpdate() = runTest {
        val book = Book("Android For Dummies")
        bookDao.insert(book)
        val book2 = Book("Android For Dummies 2nd Edition")
        bookDao.insert(book2)
        val bookFromDb = bookDao.findByName("Android For Dummies")!!
        assertEquals(book, bookFromDb)

        val updatedBook = bookFromDb.copy(name = "Android For Dummies 2nd Edition")
        runCatching {
            bookDao.update(updatedBook)
        }.onSuccess {
            error("Should have failed")
        }.onFailure {
            assertTrue(it is SQLiteConstraintException)
        }
    }

    @Test
    fun shouldDeleteABook() = runTest {
        val book = Book("Android For Dummies")
        bookDao.insert(book)
        val bookFromDb = bookDao.findByName("Android For Dummies")!!
        assertEquals(book, bookFromDb)

        bookDao.delete(bookFromDb)
        val bookFromDb2 = bookDao.findByName("Android For Dummies")
        assertNull(bookFromDb2)
    }

    @Test
    fun canCopyABook() = runTest {
        val book = Book("My Book")
        bookDao.insert(book)

        val copyName = "My Other Book"
        val copy = bookDao.copy(book, copyName, db)

        assertEquals(copy, bookDao.findByName(copyName))
    }

    @Test
    fun copiedBookHasHisTransactionsCopied() = runTest {
        val book = Book("My Book")
        bookDao.insert(book)
        val transaction = Transaction("My transaction", Date(), 10.0, book.uuid)
        transactionDao.insert(transaction)

        val copyName = "My Other Book"
        val copy = bookDao.copy(book, copyName, db)

        val transactionCopies = transactionDao.findAllByBookId(copy.uuid)
        assertEquals(1, transactionCopies.size)
        val transactionCopy = transactionCopies.first()

        assertEquals(transaction.description, transactionCopy.description)
        assertEquals(transaction.date, transactionCopy.date)
        assertEquals(transaction.amount, transactionCopy.amount, .0)
        assertEquals(transaction.currency, transactionCopy.currency)
    }

    @Test
    fun copiedBooksTransactionsHaveTheirTagCopied() = runTest {
        val book = Book("My Book")
        bookDao.insert(book)
        val transaction = Transaction("My transaction", Date(), 10.0, book.uuid)
        transactionDao.insert(transaction)
        val tag = tagDao.insert("My tag", book.uuid, null)
        tagTransactionsDao.insert(transaction, tag)

        val copyName = "My Other Book"
        val copy = bookDao.copy(book, copyName, db)

        val transactionCopy = transactionDao.findAllByBookId(copy.uuid).first()
        val tags = tagTransactionsDao.findTagsByTransactionId(transactionCopy.transactionId)

        assertEquals(1, tags.size)
        val tagCopy = tags.first()
        assertEquals(tag.name, tagCopy.name)
        assertEquals(tag.type, tagCopy.type)
        assertEquals(copy.uuid, tagCopy.bookId)
    }


    @After
    fun close() {
        db.close()
    }
}
