package ntu26.ss.parkinpeace.server.serializers

import ntu26.ss.parkinpeace.server.ApplicationError

open class SerializationError(message: String): ApplicationError(message)