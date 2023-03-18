package fr.uge.plutus.backend

import androidx.room.*
import java.net.URI
import java.util.UUID

@Entity(
    tableName = "attachment",
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["transactionId"],
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
)

@Dao
interface AttachmentDao {

    @Insert
    fun _insert(attachment: Attachment)

    @Delete
    fun delete(attachment: Attachment)

    fun insert(transaction: Transaction, uri: URI, name: String): Attachment =
        Attachment(transaction.transactionId, uri, name).also(::_insert)

    @Query("SELECT * FROM attachment WHERE transactionId = :transactionId")
    fun findAllByTransactionId(transactionId: UUID): List<Attachment>

}