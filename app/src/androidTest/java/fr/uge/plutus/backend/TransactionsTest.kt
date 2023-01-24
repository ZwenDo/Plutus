package fr.uge.plutus.backend

import android.content.Context
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

    @Before
    fun init() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            Database::class.java
        ).build()
        transactionDao = db.transactions()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldCreateATransactionWithoutFailing() = runTest {
        val bookId = UUID.randomUUID()
        val transaction = Transaction("First Transaction", Date(0), 10.0, bookId)
        transactionDao.insert(transaction)

        val transactionFromDb = transactionDao.findById(transaction.uuid)
        assertEquals(transaction, transactionFromDb)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldGetAllBookTransactions() = runTest {
        val bookId1 = UUID.randomUUID()
        val bookId2 = UUID.randomUUID()
        val transaction1 = Transaction("First Transaction", Date(0), 10.0, bookId1)
        val transaction2 = Transaction("Second Transaction", Date(10), 12.2, bookId1)
        val transaction3 = Transaction("First Transaction of another book", Date(5), 7.85, bookId2)

        transactionDao.insert(transaction1)
        transactionDao.insert(transaction2)
        transactionDao.insert(transaction3)

        val transactions = transactionDao.findAllByBookId(bookId1)
        assertEquals(2, transactions.size)
        assertTrue(transaction1 in transactions)
        assertTrue(transaction2 in transactions)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldUpdateATransaction() = runTest {
        val bookId = UUID.randomUUID()
        val transaction = Transaction("Second Transaction", Date(0), 10.0, bookId)
        transactionDao.insert(transaction)
        val transactionFromDb = transactionDao.findById(transaction.uuid)!!
        assertEquals(transaction, transactionFromDb)

        val updatedTransaction = transactionFromDb.copy(description = "First transaction")
        transactionDao.update(updatedTransaction)
        val updatedTransactionFromDb = transactionDao.findById(transaction.uuid)
        assertEquals(updatedTransaction, updatedTransactionFromDb)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldDeleteATransaction() = runTest {
        val bookId = UUID.randomUUID()
        val transaction = Transaction("First Transaction", Date(0), 10.0, bookId)
        transactionDao.insert(transaction)

        val transactionFromDB = transactionDao.findById(transaction.uuid)!!
        assertEquals(transaction, transactionFromDB)

        transactionDao.delete(transactionFromDB)
        val transactionDeleted = transactionDao.findById(transaction.uuid)
        assertNull(transactionDeleted)
    }

    @After
    fun close() {
        db.close()
    }
}