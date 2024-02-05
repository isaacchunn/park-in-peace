package ntu26.ss.parkinpeace.server.api

class ApiError : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}