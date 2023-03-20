@file:UseSerializers(UUIDSerializer::class, UriSerializer::class)

package fr.uge.plutus.backend.serialization

import android.net.Uri
import fr.uge.plutus.backend.*
import fr.uge.plutus.backend.Currency
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

@Serializable
data class TransactionDTO(
    val transactionId: UUID,
    val description: String,
    val date: Long,
    val amount: Double,
    val currency: Currency,
    val location: Pair<Double, Double>?,
    val tags: List<UUID>,
    val attachments: List<AttachmentDTO>,
) {

    fun toTransaction(bookId: UUID): Transaction = Transaction(
        description,
        Date(date),
        amount,
        bookId,
        currency,
        location?.first,
        location?.second,
        transactionId,
    )

}

@Serializable
data class TagDTO(
    val tagId: UUID,
    val name: String,
    val type: TagType,
) {

    fun toTag(bookId: UUID): Tag = Tag(
        name,
        type,
        bookId,
        tagId,
    )

}

@Serializable
data class AttachmentDTO(
    val attachmentId: UUID,
    val name: String,
    val uri: Uri,
) {

    fun toAttachment(transactionId: UUID): Attachment = Attachment(
        transactionId,
        uri,
        name,
        attachmentId,
    )

}


@Serializable
data class FilterDTO(
    val filterId: UUID,
    val name: String,
    val criteria: Map<String, String>,
//    val tags: List<UUID>,
) {

    fun toFilter(): Filter = Filter(
        name,
        filterId,
        criteria,
    )

}

@Serializable
data class BookDTO(
    val uuid: UUID,
    val name: String,
    val transactions: List<TransactionDTO>,
    val tags: List<TagDTO>,
    val filters: List<FilterDTO>,
) {

    fun toBook(): Book = Book(
        name,
        uuid,
    )

}
