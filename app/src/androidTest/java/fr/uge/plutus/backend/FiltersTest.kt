package fr.uge.plutus.backend

import android.content.Context
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
class FiltersTest {
    private lateinit var db: Database
    private lateinit var filterDao: FilterDao
    private lateinit var bookDao: BookDao
    private lateinit var tagDao: TagDao
    private lateinit var book: Book
    private lateinit var book2: Book
    private lateinit var tag: Tag
    private lateinit var tag2: Tag
    private lateinit var tag3: Tag

    @Before
    @OptIn(ExperimentalCoroutinesApi::class)
    fun init() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            Database::class.java
        ).build()

        filterDao = db.filters()
        bookDao = db.books()
        tagDao = db.tags()

        val b = Book("Android For Dummies")
        bookDao.insert(b)
        book = b
        val b2 = Book("Kotlin For Dummies")
        bookDao.insert(b2)
        book2 = b2

        tag = tagDao.insert("+First Tag", book.uuid)
        tag2 = tagDao.insert("-test", book.uuid)
        tag3 = tagDao.insert("=testTag", book.uuid)
    }


    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldCreateFiltersWithoutFailing() = runTest {
        val tags = setOf(tag, tag2, tag3)
        val filter1 = Filter.Builder("test", book.uuid)
            .tags(tags)
            .minAmount(10.0)
            .maxAmount(100.0)
            .currency(Currency.EUR)
            .minDate(Date(1000000))
            .maxDate(Date(99999999999))
            .build()

        val filter2 = Filter.Builder("test2", book.uuid)
            .tags(tags)
            .build()

        val filter3 = Filter.Builder("test3", book.uuid)
            .minAmount(10.0)
            .currency(Currency.EUR)
            .build()

        filterDao.insert(filter1)
        filterDao.insert(filter2)
        filterDao.insert(filter3)

        val filtersDb = filterDao.findAllByBookId(book.uuid)
        assertEquals(3, filtersDb.size)
        assertTrue(filter1 in filtersDb)
        assertTrue(filter2 in filtersDb)
        assertTrue(filter3 in filtersDb)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldDeleteFilter() = runTest {
        val tags = setOf(tag, tag2, tag3)
        val filter1 = Filter.Builder("test", book.uuid)
            .tags(tags)
            .minAmount(10.0)
            .maxAmount(100.0)
            .currency(Currency.EUR)
            .minDate(Date(1000000))
            .maxDate(Date(99999999999))
            .build()

        val filter2 = Filter.Builder("test2", book.uuid)
            .tags(tags)
            .build()

        val filter3 = Filter.Builder("test3", book.uuid)
            .minAmount(10.0)
            .currency(Currency.EUR)
            .build()

        filterDao.insert(filter1)
        filterDao.insert(filter2)
        filterDao.insert(filter3)

        val filtersDb = filterDao.findAllByBookId(book.uuid)
        assertEquals(3, filtersDb.size)
        assertTrue(filter2 in filtersDb)

        filterDao.delete(filter2)
        val filtersDb2 = filterDao.findAllByBookId(book.uuid)
        assertEquals(2, filtersDb2.size)
        assertFalse(filter2 in filtersDb2)
    }

    @Test
    fun shouldFailedCreateFilterWithoutCurrency1() {
        runCatching {
            Filter.Builder("No Currency1", book.uuid)
                .minAmount(10.0)
                .build()
        }.onSuccess {
            error("Should have failed")
        }.onFailure {
            assertTrue(it is IllegalArgumentException)
        }
    }

    @Test
    fun shouldFailedCreateFilterWithoutCurrency2() {
        runCatching {
            Filter.Builder("No Currency 2", book.uuid)
                .maxAmount(10.0)
                .build()
        }.onSuccess {
            error("Should have failed")
        }.onFailure {
            assertTrue(it is IllegalArgumentException)
        }
    }

    @Test
    fun shouldFailedCreateFilterWithoutCurrency3() {
        runCatching {
            Filter.Builder("No currency 3", book.uuid)
                .minAmount(10.0)
                .maxAmount(100.0)
                .build()
        }.onSuccess {
            error("Should have failed")
        }.onFailure {
            assertTrue(it is IllegalArgumentException)
        }
    }

    @Test
    fun shouldFailedCreateFilterWithWrongDates() {
        runCatching {
            Filter.Builder("No min Date", book.uuid)
                .minDate(Date(1000000))
                .maxDate(Date(0))
                .build()
        }.onSuccess {
            error("Should have failed")
        }.onFailure {
            assertTrue(it is IllegalArgumentException)
        }
    }

    @Test
    fun shouldFailedCreateEmptyFilter() {
        runCatching {
            Filter.Builder("Empty", book.uuid)
                .build()
        }.onSuccess {
            error("Should have failed")
        }.onFailure {
            assertTrue(it is IllegalArgumentException)
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun shouldRetrieveFilterCriteria() = runTest {
        val tags = setOf(tag, tag2, tag3)
        val minAmount = 10.0
        val maxAmount = 100.0
        val currency = Currency.EUR
        val minDate = Date(1000000)
        val maxDate = Date(99999999999)

        val filter = Filter.Builder("test", book.uuid)
            .tags(tags)
            .minAmount(minAmount)
            .maxAmount(maxAmount)
            .currency(currency)
            .minDate(minDate)
            .maxDate(maxDate)
            .build()

        filterDao.insert(filter)

        val filterDb = filterDao.findById(filter.filterId)

        assertNotNull(filterDb)
        assertEquals(minAmount.toString(), filterDb!!.getCriteriaValue(Criteria.MIN_AMOUNT))
        assertEquals(maxAmount.toString(), filterDb!!.getCriteriaValue(Criteria.MAX_AMOUNT))
        assertEquals(currency.name, filterDb!!.getCriteriaValue(Criteria.CURRENCY))
        assertEquals(minDate.time.toString(), filterDb!!.getCriteriaValue(Criteria.MIN_DATE))
        assertEquals(maxDate.time.toString(), filterDb!!.getCriteriaValue(Criteria.MAX_DATE))
    }

    @Test
    fun shouldNotFailedRetrieveUnknownCriteria() {
        val filter = Filter.Builder("test", book.uuid)
            .minDate(Date(1000000))
            .build()

        assertEquals(1, filter.criterias.size)

        assertEquals("", filter.getCriteriaValue(Criteria.MIN_AMOUNT))
    }
}
