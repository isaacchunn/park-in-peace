package ntu26.ss.parkinpeace.server.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * ONE-WAY SERIALIZER:
 *  Only supports serialization by retrieving the value and calling the toString() method
 *  Deserialization is NOT supported.
 *  Any class that accesses/reads the value returned by the deserializer will encounter an IllegalStateException
 */
class LazyPropertySerializer<T> : KSerializer<Lazy<T>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("lazyProp", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Lazy<T>) {
        encoder.encodeString(value.value.toString())
    }

    override fun deserialize(decoder: Decoder): Lazy<T> {
        decoder.decodeString()
        return lazy { throw IllegalStateException("Cannot reconstruct a lazy property") }
    }
}