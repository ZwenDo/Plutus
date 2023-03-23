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
    val location: Pair<Double, Double>? = null,
    val tags: List<UUID> = emptyList(),
    val attachments: List<AttachmentDTO> = emptyList(),
) {

    fun toTransaction(originalBookId: UUID, bookId: UUID): Transaction = Transaction(
        description,
        Date(date),
        amount,
        bookId,
        currency,
        location?.first,
        location?.second,
        // if the book is imported, we need to generate a new id
        if (bookId != originalBookId) UUID.randomUUID() else transactionId,
    )

}

@Serializable
data class TagDTO(
    val tagId: UUID,
    val name: String,
    val type: TagType,
) {

    fun toTag(originalBookId: UUID, bookId: UUID): Tag = Tag(
        name,
        type,
        bookId,
        null,
        // if the book is imported, we need to generate a new id
        if (bookId != originalBookId) UUID.randomUUID() else tagId,
    )

}

@Serializable
data class AttachmentDTO(
    val attachmentId: UUID,
    val name: String,
    val uri: Uri,
) {

    fun toAttachment(originalTransactionId: UUID, transactionId: UUID): Attachment = Attachment(
        transactionId,
        uri,
        name,
        // if the transaction is imported, we need to generate a new id
        if (transactionId != originalTransactionId) UUID.randomUUID() else attachmentId,
    )

}


@Serializable
data class FilterDTO(
    val filterId: UUID,
    val name: String,
    val criteria: Map<String, String>,
    val tags: List<UUID>,
) {

    fun toFilter(originalBookId: UUID, bookId: UUID): Filter = Filter(
        name,
        bookId,
        criteria,
        if (bookId != originalBookId) UUID.randomUUID() else filterId,
    )

}

@Serializable
data class BookDTO(
    val uuid: UUID,
    val name: String,
    val transactions: List<TransactionDTO> = emptyList(),
    val tags: List<TagDTO> = emptyList(),
    val filters: List<FilterDTO> = emptyList()
)
