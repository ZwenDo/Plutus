package fr.uge.plutus.backend.serialization

import com.kamelia.sprinkler.binary.encoder.EncoderBuilder
import java.util.*


val uuidEncoder = EncoderBuilder<UUID>()
    .encode(UUID::getMostSignificantBits)
    .encode(UUID::getLeastSignificantBits)
    .build()

val dateEncoder = EncoderBuilder<Date>()
    .encode(Date::getTime)
    .build()

val enumEncoder = EncoderBuilder<Enum<*>>()
    .encode { ordinal }
    .build()