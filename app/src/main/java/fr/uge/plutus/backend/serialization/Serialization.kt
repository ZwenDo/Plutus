package fr.uge.plutus.backend.serialization

import android.content.Context
import android.net.Uri
import android.os.Environment
import fr.uge.plutus.backend.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.collections.HashMap

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

private suspend fun Book.toDTO(exportTags: Set<Tag>, database: Database? = null): BookDTO {
    val transactionDao = database?.transactions() ?: Database.transactions()
    val tagDao = database?.tags() ?: Database.tags()
    val filterDao = database?.filters() ?: Database.filters()

    val tagIds = exportTags.mapTo(HashSet(), Tag::tagId)

    val transactions = transactionDao
        .findAllByBookIdWithTags(uuid, tagIds)
        .map { it.toDTO(database) }
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

private suspend fun BookDTO.loadToDB(database: Database? = null, bookId: UUID? = null) {
    val bookDao = database?.books() ?: Database.books()
    val tagDao = database?.tags() ?: Database.tags()
    val transactionDao = database?.transactions() ?: Database.transactions()
    val tagTransactionJoinDao = database?.tagTransactionJoin() ?: Database.tagTransactionJoin()
    val attachmentDao = database?.attachments() ?: Database.attachments()

    val book = toBook(bookId ?: uuid)
    bookDao.upsert(book)

    val tagMap = mutableMapOf<UUID, Tag>()
    tags.forEach {
        // if the book is imported as a new book, we need to generate new ids for the tags
        val newId = if (bookId != null) UUID.randomUUID() else null
        val tag = it.toTag(book.uuid, newId)
        tagDao.upsert(tag)
        tagMap[tag.tagId] = tag
    }

    transactions.forEach {
        // if the book is imported as a new book, we need to generate new ids for the transactions
        val newId = if (bookId != null) UUID.randomUUID() else null
        val transaction = it.toTransaction(book.uuid, newId)
        transactionDao.upsert(transaction)
        it.tags.forEach { tagId ->
            tagTransactionJoinDao.upsert(TagTransactionJoin(tagId, transaction.transactionId))
        }

        it.attachments.forEach { attachmentDTO ->
            // if the book is imported as a new book, we need to generate new ids for the attachments
            val newAttachmentId = if (bookId != null) UUID.randomUUID() else null
            val attachment = attachmentDTO.toAttachment(transaction.transactionId, newAttachmentId)
            attachmentDao.upsert(attachment)
        }
    }
}

suspend fun exportBook(book: Book, name: String, exportTags: Set<Tag> = emptySet()) {
    require("/" !in name && "\\" !in name) { "Invalid name: $name" }
    val json = Json.encodeToString(book.toDTO(exportTags))
    val outputFile = Environment
        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        .resolve("$name.book.plutus")
    outputFile.writeText(json)
}

suspend fun importBook(
    fileUri: Uri,
    context: Context,
    database: Database? = null
) = bookImporting(fileUri, context, null, database)

suspend fun mergeBook(
    fileUri: Uri,
    context: Context,
    mergeDestinationBook: UUID,
    database: Database? = null
) = bookImporting(fileUri, context, mergeDestinationBook, database)

private suspend fun bookImporting(
    fileUri: Uri,
    context: Context,
    mergeDestinationBook: UUID?,
    database: Database? = null
) {
    require(
        fileUri.scheme == "content" &&
                fileUri.lastPathSegment?.endsWith(".book.plutus") == true
    ) {
        "Invalid file: $fileUri"
    }

    val content = context
        .contentResolver
        .openInputStream(fileUri)!!
        .use {
            it.bufferedReader().use { b ->
                b.readText()
            }
        }
    val bookDTO = Json.decodeFromString(BookDTO.serializer(), content)
    bookDTO.loadToDB(database, mergeDestinationBook)
}

