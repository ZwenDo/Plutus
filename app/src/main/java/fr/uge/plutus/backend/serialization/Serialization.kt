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

private fun Tag.toDTO(): TagDTO = TagDTO(
    tagId,
    name,
    type
)

private fun Attachment.toDTO(): AttachmentDTO = AttachmentDTO(
    id,
    name,
    uri
)

private fun Filter.toDTO(): FilterDTO {
    return FilterDTO(
        filterId,
        name,
        criterias,
//        tags,
    )
}

suspend fun Book.toDTO(database: Database? = null): BookDTO {
    val transactionDao = database?.transactions() ?: Database.transactions()
    val tagDao = database?.tags() ?: Database.tags()
    val filterDao = database?.filters() ?: Database.filters()

    val transactions = transactionDao.findAllByBookId(uuid).map { it.toDTO(database) }
    val tags = tagDao.findByBookId(uuid).map { it.toDTO() }
    val filters = filterDao.findAllByBookId(uuid).map { it.toDTO() }

    return BookDTO(
        uuid,
        name,
        transactions,
        tags,
        filters
    )
}

suspend fun BookDTO.loadToDB(database: Database? = null) {
    val bookDao = database?.books() ?: Database.books()
    val tagDao = database?.tags() ?: Database.tags()
    val transactionDao = database?.transactions() ?: Database.transactions()

    val book = toBook()
    bookDao.upsert(book)

    tags.forEach {
        tagDao.upsert(it.toTag(book.uuid))
    }
}

