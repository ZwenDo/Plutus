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

@RunWith(AndroidJUnit4::class)
class BooksTest {

    private lateinit var db: Database
    private lateinit var bookDao: BookDao

    @Before
    fun init() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            Database::class.java
        ).build()
        bookDao = db.books()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldCreateABookWithoutFailing() = runTest {
        val book = Book("Android For Dummies")
        bookDao.insert(book)

        val bookFromDb = bookDao.findByName("Android For Dummies")
        assertEquals(book, bookFromDb)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldGetAllBooks() = runTest {
        val book = Book("Android For Dummies")
        bookDao.insert(book)
        val book2 = Book("Kotlin For Dummies")
        bookDao.insert(book2)

        val books = bookDao.getAll()
        assertEquals(2, books.size)
        assertTrue(book in books)
        assertTrue(book2 in books)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
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
    @OptIn(ExperimentalCoroutinesApi::class)
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
    @OptIn(ExperimentalCoroutinesApi::class)
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
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldDeleteABook() = runTest {
        val book = Book("Android For Dummies")
        bookDao.insert(book)
        val bookFromDb = bookDao.findByName("Android For Dummies")!!
        assertEquals(book, bookFromDb)

        bookDao.delete(bookFromDb)
        val bookFromDb2 = bookDao.findByName("Android For Dummies")
        assertNull(bookFromDb2)
    }

    @After
    fun close() {
        db.close()
    }
}
