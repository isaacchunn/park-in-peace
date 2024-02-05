package ntu26.ss.parkinpeace

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform