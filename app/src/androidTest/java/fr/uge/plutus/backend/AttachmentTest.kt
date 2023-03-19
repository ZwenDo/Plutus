package fr.uge.plutus.backend

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AttachmentTest {

    private lateinit var db: Database
    private lateinit var attachmentDao: AttachmentDao
    private lateinit var bookDao: BookDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun init() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            Database::class.java
        ).build()
        attachmentDao = db.attachments()
        bookDao = db.books()
        transactionDao = db.transactions()
    }

    @Test
    fun shouldCreateAnAttachmentWithoutFailing() = runTest {
        val book = Book("Android For Dummies")
        bookDao.insert(book)
        val transaction = Transaction("Android For Dummies", Date(), 10.0, book.uuid)
        transactionDao.insert(transaction)

        val attachment = attachmentDao.insert(
            transaction,
            Uri.parse("https://www.google.com/"),
            "test"
        )

        val attachmentFromDb = attachmentDao.findById(attachment.id)
        assertEquals(attachment, attachmentFromDb)
    }

    @Test
    fun shouldBeAbleToDelete() = runTest {
        val book = Book("Android For Dummies")
        bookDao.insert(book)
        val transaction = Transaction("Android For Dummies", Date(), 10.0, book.uuid)
        transactionDao.insert(transaction)

        val attachment = attachmentDao.insert(
            transaction,
            Uri.parse("https://www.google.com/"),
            "test"
        )

        var attachmentFromDb = attachmentDao.findById(attachment.id)
        assertNotNull(attachmentFromDb)
        attachmentDao.delete(attachment)
        attachmentFromDb = attachmentDao.findById(attachment.id)
        assertNull(attachmentFromDb)
    }


}