package fr.uge.plutus.backend.serialization

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import fr.uge.plutus.MainActivity
import fr.uge.plutus.backend.*
import fr.uge.plutus.frontend.store.globalState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

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


    val transactions = transactionDao
        .run {
            if (exportTags.isNotEmpty()) { // filter transactions by tags if there is at least one tag selected
                val tagIds = exportTags.mapTo(HashSet(), Tag::tagId)
                findAllByBookIdWithTags(uuid, tagIds)
            } else {
                findAllByBookId(uuid)
            }
        }
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

private suspend fun BookDTO.loadToDB(database: Database? = null, bookId: UUID) {
    val bookDao = database?.books() ?: Database.books()
    val tagDao = database?.tags() ?: Database.tags()
    val transactionDao = database?.transactions() ?: Database.transactions()
    val tagTransactionJoinDao = database?.tagTransactionJoin() ?: Database.tagTransactionJoin()
    val attachmentDao = database?.attachments() ?: Database.attachments()

    val tagMap = mutableMapOf<UUID, Tag>()
    tags.forEach {
        // if the book is imported as a new book, we need to generate new ids for the tags
        val tag = it.toTag(bookId)
        tagDao.upsert(tag)
        tagMap[tag.tagId] = tag
    }

    transactions.forEach {
        // if the book is imported as a new book, we need to generate new ids for the transactions
        val transaction = it.toTransaction(bookId)
        transactionDao.upsert(transaction)
        it.tags.forEach { tagId ->
            tagTransactionJoinDao.upsert(TagTransactionJoin(tagId, transaction.transactionId))
        }

        it.attachments.forEach { attachmentDTO ->
            // if the book is imported as a new book, we need to generate new ids for the attachments
            val attachment = attachmentDTO.toAttachment(transaction.transactionId)
            attachmentDao.upsert(attachment)
        }
    }
}


@Composable
fun ExportBook(
    book: Book,
    name: String,
    exportTags: Set<Tag> = emptySet()
) {
    val globalState = globalState()

    LaunchedEffect(globalState.writeExternalStoragePermission) {
        if (!globalState.writeExternalStoragePermission) {
            MainActivity.requestWriteExternalStoragePermission()
            return@LaunchedEffect
        }
        export(book, name, exportTags)
    }
}

private suspend fun export(
    book: Book,
    name: String,
    exportTags: Set<Tag>
) {
    require("/" !in name && "\\" !in name) { "Invalid name: $name" }
    val json = Json.encodeToString(book.toDTO(exportTags))

    val folder = Environment
        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    // create a file with a unique name
    lateinit var outputFile: File
    var index = 0
    do {
        val fileName = if (index == 0) name else "$name ($index)"
        outputFile = folder.resolve("$fileName.book.plutus")
        index++
    } while (outputFile.exists())

    val didCreate = withContext(Dispatchers.IO) {
        outputFile.createNewFile()
    }
    if (!didCreate) throw IllegalStateException("Cannot create file: $outputFile")

    outputFile.writeText(json)
}

suspend fun importBook(
    fileUri: Uri,
    context: Context,
    mergeDestinationBook: UUID,
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

