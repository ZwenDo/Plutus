package fr.uge.plutus.backend.serialization

import fr.uge.plutus.backend.*

private suspend fun Transaction.toDTO(
    database: Database? = null,
): TransactionDTO {
    val tagDao = database?.tagTransactionJoin() ?: Database.tagTransactionJoin()
    val attachmentDao = database?.attachments() ?: Database.attachments()

    val tags = tagDao.findTagIdsByTransactionId(transactionId)
    val attachments = attachmentDao.findAllByTransactionId(transactionId).map { it.toDTO() }
    return TransactionDTO(
        transactionId,
        description,
        date.time,
        amount,
        currency,
        latitude?.let { it to longitude!! }, // TODO no.
        tags,
        attachments
    )
}

private fun Tag.toDTO(database: Database? = null): TagDTO = TagDTO(
    tagId,
    name,
    type
)

private fun Attachment.toDTO(): AttachmentDTO = AttachmentDTO(
    id,
    name,
    uri
)

suspend fun Book.toDTO(database: Database? = null): BookDTO {
    val transactionDao = database?.transactions() ?: Database.transactions()
    val tagDao = database?.tags() ?: Database.tags()

    val transactions = transactionDao.findAllByBookId(uuid).map { it.toDTO(database) }
    val tags = tagDao.findByBookId(uuid).map { it.toDTO(database) }

    return BookDTO(
        uuid,
        name,
        transactions,
        tags
    )
}