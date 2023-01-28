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
class TransactionsTest {

    private lateinit var db: Database
    private lateinit var transactionDao: TransactionDao
    private lateinit var bookDao: BookDao
    private lateinit var book: Book
    private lateinit var book2: Book

    @Before
    @OptIn(ExperimentalCoroutinesApi::class)
    fun init() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            Database::class.java
        ).build()
        transactionDao = db.transactions()
        bookDao = db.books()
        val b = Book("Android For Dummies")
        bookDao.insert(b)
        book = b
        val b2 = Book("Kotlin For Dummies")
        bookDao.insert(b2)
        book2 = b2
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldCreateATransactionWithoutFailing() = runTest {
        val transaction = Transaction("First Transaction", Date(0), 10.0, book.uuid)
        transactionDao.insert(transaction)

        val transactionFromDb = transactionDao.findById(transaction.transactionId)
        assertEquals(transaction, transactionFromDb)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldFailBecauseOfForeignKeyConstraint() = runTest {
        val unknownBookId = UUID.randomUUID()
        val transaction = Transaction("First Transaction", Date(0), 10.0, unknownBookId)

        runCatching {
            transactionDao.insert(transaction)
        }.onSuccess {
            error("Should have failed")
        }.onFailure {
            assertTrue(it is SQLiteConstraintException)
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldGetAllBookTransactions() = runTest {
        val transaction1 = Transaction("First Transaction", Date(0), 10.0, book.uuid)
        val transaction2 = Transaction("Second Transaction", Date(10), 12.2, book.uuid)
        val transaction3 = Transaction(
            "First Transaction of another book",
            Date(5),
            7.85,
            book2.uuid
        )

        transactionDao.insert(transaction1)
        transactionDao.insert(transaction2)
        transactionDao.insert(transaction3)

        val transactions = transactionDao.findAllByBookId(book.uuid)
        assertEquals(2, transactions.size)
        assertTrue(transaction1 in transactions)
        assertTrue(transaction2 in transactions)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldUpdateATransaction() = runTest {
        val transaction = Transaction("Second Transaction", Date(0), 10.0, book.uuid)
        transactionDao.insert(transaction)
        val transactionFromDb = transactionDao.findById(transaction.transactionId)!!
        assertEquals(transaction, transactionFromDb)

        val updatedTransaction = transactionFromDb.copy(description = "First transaction")
        transactionDao.update(updatedTransaction)
        val updatedTransactionFromDb = transactionDao.findById(transaction.transactionId)
        assertEquals(updatedTransaction, updatedTransactionFromDb)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldDeleteATransaction() = runTest {
        val transaction = Transaction("First Transaction", Date(0), 10.0, book.uuid)
        transactionDao.insert(transaction)
        val transactionFromDB = transactionDao.findById(transaction.transactionId)!!
        assertEquals(transaction, transactionFromDB)

        transactionDao.delete(transactionFromDB)
        val transactionDeleted = transactionDao.findById(transaction.transactionId)
        assertNull(transactionDeleted)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldDeleteTransactionOnCascade() = runTest {
        val transaction = Transaction("First Transaction", Date(0), 10.0, book.uuid)
        transactionDao.insert(transaction)

        val transactionFromDB = transactionDao.findById(transaction.transactionId)
        assertEquals(transaction, transactionFromDB)

        bookDao.delete(book)

        val bookFromDB2 = bookDao.findById(book.uuid)
        assertNull(bookFromDB2)
        val transactionFromDB2 = transactionDao.findById(transaction.transactionId)
        assertNull(transactionFromDB2)
    }

    @After
    fun close() {
        db.close()
    }
}
