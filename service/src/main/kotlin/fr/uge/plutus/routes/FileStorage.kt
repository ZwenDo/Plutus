package fr.uge.plutus.routes

import fr.uge.plutus.core.MultipartParseException
import fr.uge.plutus.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.fileStorageRoutes() = route("/store") {
    route("/book") {
        uploadFile()

        authenticate(Constants.AUTH_JWT) {
            downloadFile()
        }
    }
}

private fun Route.uploadFile() = post {
    call.doWithForm(
        onFiles = mapOf(
            "file" to {
                val id = UUID.randomUUID().toString()
                FileUtils.write(id, it)
                call.respondText(JWTFactory.from(id), status = HttpStatusCode.OK)
            }
        ),
        onMissing = {
            throw MultipartParseException("Missing file (key: file)")
        }
    )
}

private fun Route.downloadFile() = get {
    val (id) = bookFile

    val file = FileUtils.getOrNull(id)
    checkNotNull(file) { "Could not find file with id $id" }

    call.respondFile(file, "book.bin", "application/octet-stream")
}
