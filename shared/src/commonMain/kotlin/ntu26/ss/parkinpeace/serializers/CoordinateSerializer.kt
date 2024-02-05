package ntu26.ss.parkinpeace.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ntu26.ss.parkinpeace.models.Coordinate

class CoordinateSerializer : KSerializer<Coordinate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("coordinate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Coordinate) {
        encoder.encodeString(value.asIso6709())
    }

    override fun deserialize(decoder: Decoder): Coordinate {
        val string = decoder.decodeString()
        return Coordinate.parse(string)
    }
}