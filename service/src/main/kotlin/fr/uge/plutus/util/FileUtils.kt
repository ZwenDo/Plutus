package fr.uge.plutus.util

import fr.uge.plutus.core.Environment
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object FileUtils {

    private val UPLOAD_PATH = Path.of(Environment.uploadFolder)

    init {
        Files.createDirectories(UPLOAD_PATH)
    }

    fun getOrNull(id: String): File? {
        val file = UPLOAD_PATH.resolve(id).toFile()
        return if (file.exists()) file else null
    }

    suspend fun write(
        id: String,
        file: PartData.FileItem,
    ): Long = withContext(Dispatchers.IO) {
        val fileBytes = file.streamProvider().readBytes()
        val filePath = UPLOAD_PATH.resolve(id)
        Files.write(filePath, fileBytes)
        val fileSize = Files.size(filePath)
        fileSize
    }

}