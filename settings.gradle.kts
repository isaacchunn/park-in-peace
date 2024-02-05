enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
val secrets = File(rootDir, "SECRETS.properties").let {
    java.util.Properties().apply { load(it.inputStream()) }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                // Do not change the username below.
                // This should always be `mapbox` (not your username).
                username = "mapbox"
                // Use the secret token you stored in gradle.properties as the password
                password = secrets.getProperty("ANDROID_MAPBOX_SECRET_TOKEN")
            }
        }
    }
}

rootProject.name = "ParkInPeace"
include(":androidApp")
include(":shared")
include(":server")
include(":libs:svy21")
