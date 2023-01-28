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
class TagTest {


    private lateinit var db: Database
    private lateinit var transactionDao: TransactionDao
    private lateinit var bookDao: BookDao
    private lateinit var book: Book
    private lateinit var book2: Book
    private lateinit var tagDao: TagDao
    private lateinit var tagTransactionJoinDao: TagTransactionJoinDao

    @Before
    fun init() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            Database::class.java
        ).build()
        transactionDao = db.transactions()
        tagDao = db.tags()
        tagTransactionJoinDao = db.tagTransactionJoin()
        bookDao = db.books()
        val b = Book("Android For Dummies")
        bookDao.insert(b)
        book = b
        val b2 = Book("Kotlin For Dummies")
        bookDao.insert(b2)
        book2 = b2
    }

    @Test
    fun shouldCreateATagWithoutFailing() = runTest {

        val tag = tagDao.insert("+First Tag", book.uuid)
        val tagFromDb = tagDao.findByName("First Tag", book.uuid)

        val tag2 = tagDao.insert("-test", book.uuid)
        val tagFromDb2 = tagDao.findByName("test", book.uuid)

        val tag3 = tagDao.insert("=testTag", book.uuid)
        val tagFromDb3 = tagDao.findByName("testTag", book.uuid)

        val tag4 = tagDao.insert("tests", book.uuid)
        val tagFromDb4 = tagDao.findByName("tests", book.uuid)

        assertEquals(tag, tagFromDb[0])
        assertEquals(tag2, tagFromDb2[0])
        assertEquals(tag3, tagFromDb3[0])
        assertEquals(tag4, tagFromDb4[0])
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldFailBecauseOfForeignKeyConstraint() = runTest {
        val unknownBookId = UUID.randomUUID()

        runCatching {
            tagDao.insert("secondTest", unknownBookId)
        }.onSuccess {
            error("Should have failed")
        }.onFailure {
            assertTrue(it is SQLiteConstraintException)
        }
    }

    @Test
    fun shouldGetTagWithBookId() = runTest {
        val tag = tagDao.insert("3rd", book.uuid)
        val tagFromDb = tagDao.findByBookId(book.uuid)

        assertEquals(tag, tagFromDb[0])
    }

    @Test
    fun shouldDeleteATag() = runTest {
        val tag = tagDao.insert("3rd", book.uuid)
        val tagFromDb = tagDao.findByBookId(book.uuid)
        assertEquals(tag, tagFromDb[0])
        tagDao.delete(tag)
        val tagNotFound = tagDao.findByBookId(book.uuid)
        assertEquals(emptyList<Tag>(), tagNotFound)
    }

    @Test
    fun shouldFailBecauseOfTagName() = runTest {

        assertThrows(IllegalArgumentException::class.java) {
            tagDao.insert("=", book.uuid)
        }
        assertThrows(IllegalArgumentException::class.java) {
            tagDao.insert(" ", book.uuid)
        }

    }

    @Test
    fun shouldFailBecauseTransactionAndTagMustBeInTheSameBook() = runTest {
        val transaction = Transaction("First", Date(0), 10.0, book.uuid)
        transactionDao.insert(transaction)
        val transaction2 = Transaction("First", Date(0), 10.0, book2.uuid)
        transactionDao.insert(transaction2)

        val tag = tagDao.insert("4th", book.uuid)
        tagTransactionJoinDao.insert(transaction, tag)
        assertThrows(IllegalArgumentException::class.java) {
            tagTransactionJoinDao.insert(transaction2, tag)
        }
    }

    @Test
    fun shouldFailBecauseTransactionIsNotInDb() = runTest {
        val transaction = Transaction("First", Date(0), 10.0, book.uuid)

        val tag = tagDao.insert("4th", book.uuid)
        assertThrows(SQLiteConstraintException::class.java) {
            tagTransactionJoinDao.insert(transaction, tag)
        }
    }

    @Test
    fun shouldBeAbleToFindTagByTransactionId() = runTest {
        val transaction = Transaction("First", Date(0), 10.0, book.uuid)
        transactionDao.insert(transaction)

        val tag = tagDao.insert("4th", book.uuid)
        val tag2 = tagDao.insert("3rd", book.uuid)
        tagTransactionJoinDao.insert(transaction, tag)
        tagTransactionJoinDao.insert(transaction, tag2)
        val findTags = tagTransactionJoinDao.findTagsByTransactionId(transaction.transactionId)
        val list = listOf(tag2, tag)
        assertTrue(findTags.containsAll(list))
    }

    @Test
    fun shouldBeAbleToFindTransactionByTagId() = runTest {
        val transaction = Transaction("First", Date(0), 10.0, book.uuid)
        transactionDao.insert(transaction)
        val transaction2 = Transaction("Second", Date(0), 10.0, book.uuid)
        transactionDao.insert(transaction2)

        val tag = tagDao.insert("4th", book.uuid)
        tagTransactionJoinDao.insert(transaction, tag)
        tagTransactionJoinDao.insert(transaction2, tag)
        val findTransactions = tagTransactionJoinDao.findTransactionByTagId(tag.tagId)
        val list = listOf(transaction, transaction2)
        assertTrue(findTransactions.containsAll(list))
    }

    @Test
    fun shouldBeAbleToDeleteATagTransactionJoin() = runTest {
        val transaction = Transaction("First", Date(0), 10.0, book.uuid)
        transactionDao.insert(transaction)
        val transaction2 = Transaction("Second", Date(0), 10.0, book.uuid)
        transactionDao.insert(transaction2)

        val tag = tagDao.insert("4th", book.uuid)
        tagTransactionJoinDao.insert(transaction, tag)
        tagTransactionJoinDao.insert(transaction2, tag)
        val findTransactions = tagTransactionJoinDao.findTransactionByTagId(tag.tagId)
        val list = listOf(transaction, transaction2)
        assertTrue(findTransactions.containsAll(list))
        tagTransactionJoinDao.delete(transaction, tag)
        val findTransactions2 = tagTransactionJoinDao.findTransactionByTagId(tag.tagId)
        assertFalse(findTransactions2.containsAll(list))
    }

    @Test
    fun shouldDeleteTagOnCascade() = runTest {
        val tag = tagDao.insert("+1st", book.uuid)

        val findTag = tagDao.findByBookId(book.uuid)
        assertEquals(findTag[0], tag)

        bookDao.delete(book)

        val bookFromDB2 = bookDao.findById(book.uuid)
        assertNull(bookFromDB2)
        val findTag2 = tagDao.findByBookId(book.uuid)
        assertEquals(emptyList<Tag>(), findTag2)
    }

    @Test
    fun shouldDeleteTagTransactionJoinOnCascade() = runTest {
        val tag = tagDao.insert("+1st", book.uuid)
        val transaction = Transaction("First", Date(0), 10.0, book.uuid)
        transactionDao.insert(transaction)

        val findTag = tagDao.findByBookId(book.uuid)
        assertEquals(findTag[0], tag)
        val transactionFromDB = transactionDao.findById(transaction.transactionId)
        assertEquals(transaction, transactionFromDB)

        tagTransactionJoinDao.insert(transaction, tag)
        val findTagTransactionJoin = tagTransactionJoinDao.findTransactionByTagId(tag.tagId)
        assertEquals(findTagTransactionJoin[0], transaction)
        val findTransactionTagJoin = tagTransactionJoinDao.findTagsByTransactionId(transaction.transactionId)
        assertEquals(findTransactionTagJoin[0], tag)

        tagDao.delete(tag)

        val findTag2 = tagDao.findByBookId(book.uuid)
        assertEquals(emptyList<Tag>(), findTag2)
        val tagTransaction = tagTransactionJoinDao.findTransactionByTagId(tag.tagId)
        assertEquals(emptyList<Transaction>(), tagTransaction)
        val tagTransaction2 = tagTransactionJoinDao.findTagsByTransactionId(transaction.transactionId)
        assertEquals(emptyList<Tag>(), tagTransaction2)
    }

    @Test
    fun shouldDeleteTagTransactionJoinOnCascade2() = runTest {
        val tag = tagDao.insert("+1st", book.uuid)
        val transaction = Transaction("First", Date(0), 10.0, book.uuid)
        transactionDao.insert(transaction)

        val findTag = tagDao.findByBookId(book.uuid)
        assertEquals(findTag[0], tag)
        val transactionFromDB = transactionDao.findById(transaction.transactionId)
        assertEquals(transaction, transactionFromDB)

        tagTransactionJoinDao.insert(transaction, tag)
        val findTagTransactionJoin = tagTransactionJoinDao.findTransactionByTagId(tag.tagId)
        assertEquals(findTagTransactionJoin[0], transaction)
        val findTransactionTagJoin = tagTransactionJoinDao.findTagsByTransactionId(transaction.transactionId)
        assertEquals(findTransactionTagJoin[0], tag)

        transactionDao.delete(transaction)

        val findTransaction = transactionDao.findById(transaction.transactionId)
        assertNull(findTransaction)
        val tagTransaction = tagTransactionJoinDao.findTransactionByTagId(tag.tagId)
        assertEquals(emptyList<Transaction>(), tagTransaction)
        val tagTransaction2 = tagTransactionJoinDao.findTagsByTransactionId(transaction.transactionId)
        assertEquals(emptyList<Tag>(), tagTransaction2)
    }


    @After
    fun close() {
        db.close()
    }
}
