package ntu26.ss.parkinpeace.server

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform