package ntu26.ss.parkinpeace.server.services

object Secrets : SecretManager {
    override val MAPBOX_TOKEN: String get() = System.getProperty("MAPBOX_TOKEN")
    override val URA_TOKEN: String get() = System.getProperty("URA_TOKEN")
    override val ONEMAP_USER: String get() = System.getProperty("ONEMAP_USER")
    override val ONEMAP_PASS: String get() = System.getProperty("ONEMAP_PASS")
    override val LTA_KEY: String get() = System.getProperty("LTA_KEY")
}