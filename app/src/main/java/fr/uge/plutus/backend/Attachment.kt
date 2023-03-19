package fr.uge.plutus.backend

import android.net.Uri
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
    val uri: Uri,
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

    @Update
    fun update(attachment: Attachment)

    fun insert(transaction: Transaction, uri: Uri, name: String): Attachment =
        Attachment(transaction.transactionId, uri, name).also(::_insert)

    @Query("SELECT * FROM attachment WHERE id = :id LIMIT 1")
    fun findById(id: UUID): Attachment?

    @Query("SELECT * FROM attachment WHERE transactionId = :transactionId")
    fun findAllByTransactionId(transactionId: UUID): List<Attachment>

}