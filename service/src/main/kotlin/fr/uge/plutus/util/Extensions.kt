package fr.uge.plutus.util

import fr.uge.plutus.core.MissingHeaderException
import fr.uge.plutus.core.MultipartParseException
import fr.uge.plutus.plugins.BookFilePrincipal
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import java.io.File

fun ApplicationCall.getHeader(header: String) = request.headers[header] ?: throw MissingHeaderException(header)

val PipelineContext<*, ApplicationCall>.bookFile: BookFilePrincipal get() = call.principal() ?: throw AssertionError("Principal should not be null")

suspend fun ApplicationCall.respondFile(file: File, name: String, type: String) {
    response.header(
        HttpHeaders.ContentDisposition,
        ContentDisposition.Attachment
            .withParameter(ContentDisposition.Parameters.FileName, name)
            .toString()
    )
    response.header("Mime-Type", type)
    respondFile(file)
}

suspend fun ApplicationCall.doWithForm(
    onFields: Map<String, suspend (PartData.FormItem) -> Unit> = mapOf(),
    onFiles: Map<String, suspend (PartData.FileItem) -> Unit> = mapOf(),
    onMissing: suspend (field: String) -> Unit = {},
): Result<MultiPartData> {
    getHeader("Content-Type").let { contentType ->
        if (!contentType.startsWith("multipart/form-data")) {
            throw MissingHeaderException("Content-Type")
        }
    }
    return runCatching {
        receiveMultipart()
    }.onSuccess {
        val visitedFormItem = mutableSetOf<String>()
        val visitedFileItem = mutableSetOf<String>()
        it.forEachPart { part ->
            val field = part.name!!
            when (part) {
                is PartData.FormItem -> {
                    visitedFormItem.add(field)
                    onFields[field]?.invoke(part)
                }
                is PartData.FileItem -> {
                    visitedFileItem.add(field)
                    onFiles[field]?.invoke(part)
                }
                else -> {}
            }
            part.dispose()
        }
        onFields.keys.forEach { field ->
            if (field !in visitedFormItem) onMissing(field)
        }
        onFiles.keys.forEach { field ->
            if (field !in visitedFileItem) onMissing(field)
        }
    }.onFailure { throw MultipartParseException() }
}
