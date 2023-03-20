@file:UseSerializers(UUIDSerializer::class, UriSerializer::class)
package fr.uge.plutus.backend.serialization

import android.net.Uri
import fr.uge.plutus.backend.*
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class TransactionDTO(
    val transactionId: UUID,
    val description: String,
    val date: Long,
    val amount: Double,
    val currency: Currency,
    val location: Pair<Double, Double>?,
    val tags: List<UUID>,
    val attachments: List<AttachmentDTO>
)
@Serializable
data class TagDTO(
    val tagId: UUID,
    val name: String,
    val type: TagType,
)

@Serializable
data class AttachmentDTO(
    val attachmentId: UUID,
    val name: String,
    val uri: Uri,
)

@Serializable
data class BookDTO(
    val uuid: UUID,
    val name: String,
    val transactions: List<TransactionDTO>,
    val tags: List<TagDTO>,
)
