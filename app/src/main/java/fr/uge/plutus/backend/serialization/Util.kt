@file:OptIn(ExperimentalSerializationApi::class)
package fr.uge.plutus.backend.serialization

import android.net.Uri
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

@Serializer(forClass = UUID::class)
object UUIDSerializer : KSerializer<UUID> {

    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID =
        UUID.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: UUID): Unit =
        encoder.encodeString(value.toString())

}
@Serializer(forClass = Uri::class)
object UriSerializer : KSerializer<Uri> {

    override val descriptor = PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Uri =
        Uri.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Uri): Unit =
        encoder.encodeString(value.toString())

}