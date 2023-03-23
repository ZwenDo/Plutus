package fr.uge.plutus.backend.serialization.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

suspend fun sendData(data: ByteArray): String {
    val client = HttpClient(CIO)

    val response: HttpResponse = client.submitFormWithBinaryData(
        url = "https://plutus.slama.io/api/store/book",
        formData = formData {
            append("file", data, Headers.build {
                append(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                append(
                    HttpHeaders.ContentDisposition,
                    """form-data; name="file"; filename="template.book.plutus""""
                )
            })
        }
    )
    return response.body()
}

suspend fun getData(token: String): ByteArray {
    val client = HttpClient(CIO)

    val response: HttpResponse = client.get("https://plutus.slama.io/api/store/book") {
        bearerAuth(token)
    }
    return response.body()
}
