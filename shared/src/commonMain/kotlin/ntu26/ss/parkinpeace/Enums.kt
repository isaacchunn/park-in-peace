package ntu26.ss.parkinpeace

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified E : Enum<E>> E.getSerialName(): String =
    serializer<E>().descriptor.getElementName(ordinal)

inline fun <reified E> decodeEnum(string: String) = Json.decodeFromString<E>(string.quoted)

val String.quoted get() = "\"$this\""