package fr.uge.plutus.backend.serialization.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

suspend fun sendData(data: ByteArray): String {
    val client = HttpClient(CIO)

    val response: HttpResponse = client.submitFormWithBinaryData(
        url = "http://localhost:8080/api/store/book",
        formData = formData {
            append("file", data, Headers.build {
                append(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                append(HttpHeaders.ContentDisposition, """form-data; name="file"; filename="template.book.plutus"""")
            })
        }
    )
    return response.body()
}

suspend fun getData(token: String): ByteArray {
    val client = HttpClient(CIO)

    val response: HttpResponse = client.get("http://localhost:8080/api/store/book") {
        bearerAuth(token)
    }
    return response.body()
}
