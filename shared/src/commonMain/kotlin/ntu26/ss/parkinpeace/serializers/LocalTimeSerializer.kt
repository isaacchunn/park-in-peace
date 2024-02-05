package ntu26.ss.parkinpeace.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ntu26.ss.parkinpeace.fromMinutesOfDay
import ntu26.ss.parkinpeace.toMinutesOfDay
import java.time.LocalTime

class LocalTimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("localtime", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeInt(value.toMinutesOfDay())
    }

    override fun deserialize(decoder: Decoder): LocalTime {
        val int = decoder.decodeInt()
        return fromMinutesOfDay(int)
    }
}