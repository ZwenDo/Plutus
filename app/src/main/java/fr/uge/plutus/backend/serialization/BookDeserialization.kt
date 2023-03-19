package fr.uge.plutus.backend.serialization

import com.kamelia.sprinkler.binary.decoder.*
import com.kamelia.sprinkler.binary.decoder.composer.composedDecoder
import com.kamelia.sprinkler.binary.decoder.core.Decoder

private fun TransactionDecoder(): Decoder<TransactionDTO> {
    val uuidDecoder = UUIDDecoderString()
    val stringDecoder = UTF8StringDecoder()
    val longDecoder = LongDecoder()
    val doubleDecoder = DoubleDecoder()
    val optionalDoubleDecoder = doubleDecoder.toOptional()

    return composedDecoder {
        beginWith(uuidDecoder)
            .then(stringDecoder)
            .then(stringDecoder)
            .then(longDecoder)
            .then(doubleDecoder)
            .then(stringDecoder)
            .then(optionalDoubleDecoder)
            .then(optionalDoubleDecoder)
            .reduce(::TransactionDTO)
    }
}


fun BookDecoder(): Decoder<BookDTO> {
    val uuidDecoder = UUIDDecoderString()
    val stringDecoder = UTF8StringDecoder()

    return composedDecoder {
        beginWith(uuidDecoder)
            .then(stringDecoder)
            .then(TransactionDecoder().toList())
            .reduce(::BookDTO)
    }
}
