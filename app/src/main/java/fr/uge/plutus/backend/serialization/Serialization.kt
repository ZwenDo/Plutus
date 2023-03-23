package fr.uge.plutus.backend.serialization

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.*
import fr.uge.plutus.MainActivity
import fr.uge.plutus.backend.*
import fr.uge.plutus.frontend.store.globalState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

@Composable
fun ExportBook(
    password: String?,
    book: Book,
    name: String,
    exportTags: Set<Tag> = emptySet(),
    onExportCompleted: () -> Unit = {}
) {
    val globalState = globalState()

    LaunchedEffect(globalState.writeExternalStoragePermission) {
        if (!globalState.writeExternalStoragePermission) {
            MainActivity.requestWriteExternalStoragePermission()
            return@LaunchedEffect
        }
        export(book, name, password, exportTags)
        onExportCompleted()
    }
}

suspend fun importBook(
    password: String?,
    fileUri: Uri,
    context: Context,
    mergeDestinationBook: UUID,
    database: Database? = null
): Boolean {
    require(
        fileUri.scheme == "content" &&
                fileUri.lastPathSegment?.endsWith(".book.plutus") == true
    ) {
        "Invalid file: $fileUri"
    }

    val content = context
        .contentResolver
        .openInputStream(fileUri)!!
        .use { it.readBytes() }
        .let {
            if (password != null) {
                try {
                    decrypt(password, it)
                } catch (e: BadPaddingException) {
                    return false
                } catch (e: IllegalBlockSizeException) {
                    return false
                }
            } else {
                it
            }
        }

    val bookDTO = try {
        Json.decodeFromString(BookDTO.serializer(), String(content))
    } catch (e: SerializationException) {
        return false
    }
    bookDTO.loadToDB(database, mergeDestinationBook)
    return true
}


private suspend fun Transaction.toDTO(
    database: Database? = null,
): TransactionDTO {
    val tagDao = database?.tagTransactionJoin() ?: Database.tagTransactionJoin()
    val attachmentDao = database?.attachments() ?: Database.attachments()

    val tags = tagDao.findTagIdsByTransactionId(transactionId)
    Log.d("Export", tags.toString())
    val attachments = attachmentDao.findAllByTransactionId(transactionId).map { it.toDTO() }
    return TransactionDTO(
        transactionId,
        description,
        date.time,
        amount,
        currency,
        latitude?.let { it to longitude!! },
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
    val tagDao = database?.tags() ?: Database.tags()
    val transactionDao = database?.transactions() ?: Database.transactions()
    val tagTransactionJoinDao = database?.tagTransactionJoin() ?: Database.tagTransactionJoin()
    val attachmentDao = database?.attachments() ?: Database.attachments()

    val tagMap = mutableMapOf<UUID, UUID>()
    tags.forEach {
        val tag = it.toTag(uuid, bookId)
        tagDao.upsert(tag)
        tagMap[it.tagId] = tag.tagId
    }

    transactions.forEach {
        val transaction = it.toTransaction(uuid, bookId)
        transactionDao.upsert(transaction)
        it.tags.forEach { tagId ->
            tagTransactionJoinDao.upsert(
                TagTransactionJoin(
                    tagMap[tagId]!!,
                    transaction.transactionId
                )
            )
        }

        it.attachments.forEach { attachmentDTO ->
            val attachment = attachmentDTO.toAttachment(
                it.transactionId,
                transaction.transactionId
            )
            attachmentDao.upsert(attachment)
        }
    }
}

private val json = Json {
    prettyPrint = false
}

private suspend fun export(
    book: Book,
    name: String,
    password: String?,
    exportTags: Set<Tag>
): Boolean {
    require("/" !in name && "\\" !in name) { "Invalid name: $name" }
    val json = json.encodeToString(book.toDTO(exportTags))

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

    try {
        val didCreate =
            withContext(Dispatchers.IO) {
                outputFile.createNewFile()
            }

        if (!didCreate) throw IllegalStateException("Cannot create file: $outputFile")

        val bytes = if (password != null) {
            encrypt(password, json.toByteArray())
        } else {
            json.toByteArray()
        }
        outputFile.writeBytes(bytes)
    } catch (e: IOException) {
        return false
    }
    return true
}
