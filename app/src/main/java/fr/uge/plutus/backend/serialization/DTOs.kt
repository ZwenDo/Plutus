package fr.uge.plutus.backend.serialization

import java.util.UUID

data class TransactionDTO(
    val transactionId: UUID,
    val bookId: String,
    val description: String,
    val date: Long,
    val amount: Double,
    val currency: String,
    val latitude: Double?,
    val longitude: Double?
)

data class BookDTO(
    val uuid: UUID,
    val name: String,
    val transactions: List<TransactionDTO>
)