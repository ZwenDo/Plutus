@file:UseSerializers(UUIDSerializer::class)
package fr.uge.plutus.backend.serialization

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
    val latitude: Double?,
    val longitude: Double?
)
@Serializable
data class TagDTO(
    val tagId: UUID,
    val name: String,
    val type: TagType,
)

@Serializable
data class BookDTO(
    val uuid: UUID,
    val name: String,
    val transactions: List<TransactionDTO>
)