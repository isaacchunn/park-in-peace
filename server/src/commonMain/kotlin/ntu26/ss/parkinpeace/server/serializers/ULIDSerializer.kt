package ntu26.ss.parkinpeace.server.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ulid.ULID

class ULIDSerializer : KSerializer<ULID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ulid", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ULID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): ULID {
        val string = decoder.decodeString()
        return ULID.parseULID(string)
    }
}