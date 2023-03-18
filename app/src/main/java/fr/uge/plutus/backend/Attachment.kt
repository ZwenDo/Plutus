package fr.uge.plutus.backend

import androidx.room.*
import java.net.URI
import java.util.UUID

@Entity(
    tableName = "attachment",
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["uuid"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )],
    indices = [
        Index(
            value = ["transactionId"], unique = true
        )]
)
data class Attachment(
    val transactionId: UUID,
    val uri: URI,
    val name: String,
    @PrimaryKey
    val id: UUID = UUID.randomUUID()
) {


}

