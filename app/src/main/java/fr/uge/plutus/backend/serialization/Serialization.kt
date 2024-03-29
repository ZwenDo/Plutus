package fr.uge.plutus.backend.serialization

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import fr.uge.plutus.MainActivity
import fr.uge.plutus.backend.*
import fr.uge.plutus.backend.serialization.http.getData
import fr.uge.plutus.backend.serialization.http.sendData
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
    list: List<Transaction>,
    password: String?,
    book: Book,
    name: String,
    isCloud: Boolean,
    onExportCompleted: (String?) -> Unit = {}
) {
    val globalState = globalState()

    LaunchedEffect(globalState.writeExternalStoragePermission) {
        if (!globalState.writeExternalStoragePermission && !isCloud) {
            MainActivity.requestWriteExternalStoragePermission()
            return@LaunchedEffect
        }
        val result = export(list, book, name, password, isCloud)
        onExportCompleted(result)
    }
}

suspend fun importBook(
    password: String?,
    fileUri: Uri?,
    context: Context,
    mergeDestinationBook: UUID,
    token: String?,
    database: Database? = null
): Boolean {
    require(fileUri == null || fileUri.scheme == "content") {
        "Invalid file: $fileUri"
    }

    val array = if (token != null) {
        getData(token)
    } else {
        context.contentResolver
            .openInputStream(fileUri!!)!!
            .use { it.readBytes() }
    }

    val content = array.let {
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

private fun Filter.toDTO(tags: List<UUID>): FilterDTO = FilterDTO(
    filterId,
    name,
    criterias,
    tags,
)

private suspend fun Book.toDTO(
    list: List<Transaction>,
    database: Database? = null
): BookDTO {
    val tagDao = database?.tags() ?: Database.tags()
    val filterDao = database?.filters() ?: Database.filters()
    val tagFilterDao = database?.tagFilterJoin() ?: Database.tagFilterJoin()

    val transactions = list
        .map { it.toDTO(database) }
    val tags = tagDao.findByBookId(uuid).map { it.toDTO() }
    val filters = filterDao.findAllByBookId(uuid).map {
        val filterTags = tagFilterDao.findTagsByFilter(it.filterId).map { tag -> tag.tagId }
        it.toDTO(filterTags)
    }
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
    val filterDao = database?.filters() ?: Database.filters()
    val tagFilterDao = database?.tagFilterJoin() ?: Database.tagFilterJoin()

    val tagMap = mutableMapOf<UUID, UUID>()
    tags.forEach {
        val tag = it.toTag(uuid, bookId)
        tagDao.upsert(tag)
        tagMap[it.tagId] = tag.tagId
    }

    filters.forEach {
        val filter = it.toFilter(uuid, bookId)
        filterDao.upsert(filter)
        it.tags.forEach { id ->
            tagFilterDao.upsert(
                TagFilterJoin(
                    tagMap[id]!!,
                    filter.filterId,
                    bookId
                )
            )
        }
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
    list: List<Transaction>,
    book: Book,
    name: String,
    password: String?,
    isCloud: Boolean,
): String? {
    require("/" !in name && "\\" !in name) { "Invalid name: $name" }
    val json = json.encodeToString(book.toDTO(list))

    Log.d("YEP", "Salam")

    try {
        val bytes = if (password != null) {
            encrypt(password, json.toByteArray())
        } else {
            json.toByteArray()
        }

        if (!isCloud) {
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

            outputFile.writeBytes(bytes)
        } else {
            Log.d("YEP", "Salam")
            return sendData(bytes).also {
                Log.d("YEP", it)
            }
        }
    } catch (e: IOException) {
        Log.d("YEP", e.toString())
        return null
    }
    return ""
}
