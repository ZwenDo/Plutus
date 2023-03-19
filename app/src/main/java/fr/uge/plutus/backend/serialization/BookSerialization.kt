package fr.uge.plutus.backend.serialization

import com.kamelia.sprinkler.binary.encoder.DoubleEncoder
import com.kamelia.sprinkler.binary.encoder.EncoderBuilder
import com.kamelia.sprinkler.binary.encoder.toCollection
import com.kamelia.sprinkler.binary.encoder.toOptional
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Database
import fr.uge.plutus.backend.Transaction
import kotlinx.coroutines.runBlocking


private val transactionEncoder = EncoderBuilder<Transaction>()
    .encodeWith(uuidEncoder, Transaction::transactionId)
    .encodeWith(uuidEncoder, Transaction::bookId)
    .encode(Transaction::description)
    .encodeWith(dateEncoder, Transaction::date)
    .encode(Transaction::amount)
    .encodeWith(enumEncoder, Transaction::currency)
    .encodeWith(DoubleEncoder.toOptional()) { latitude }
    .encodeWith(DoubleEncoder.toOptional()) { longitude }
    .build()

private val bookEncoder = EncoderBuilder<Book>()
    .encodeWith(uuidEncoder, Book::uuid)
    .encode(Book::name)
    .encodeWith(transactionEncoder.toCollection()) {
        runBlocking {
            Database.transactions().findAllByBookId(uuid)
        }
    }
    .build()

fun Book.serialize(): ByteArray = bookEncoder.encode(this)