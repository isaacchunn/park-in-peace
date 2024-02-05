import java.util.*

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinxSerialization)
    id("com.google.devtools.ksp")
}

val secrets = rootProject.file("SECRETS.properties").let {
    Properties().apply { load(it.inputStream()) }
}

val envFile = rootProject.file("android.ENV.properties").let {
    Properties().apply { load(it.inputStream()) }
}

android {
    namespace = "ntu26.ss.parkinpeace.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "ntu26.ss.parkinpeace.android"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // List all relevant secrets here
        buildConfigField(
            "String",
            "ANDROID_URA_ACCESS_TOKEN",
            secrets.getProperty("ANDROID_URA_ACCESS_TOKEN")
        )
        buildConfigField("String", "ANDROID_PIP_SERVER_IP", envFile.getProperty("pip.server"))
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(projects.shared)
    implementation(projects.libs.svy21)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)

    // Viewmodel compose
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    // Kotlin serialization
    implementation(libs.kotlinx.serialization)
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.okhttp)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation(libs.logging.interceptor)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // MapBox
    implementation("com.mapbox.extension:maps-compose:11.0.0-rc.1")
    implementation("com.mapbox.maps:android:11.0.0-rc.1")
    // Room
    implementation("androidx.room:room-ktx:2.6.0")
    ksp("androidx.room:room-compiler:2.6.0")

    debugImplementation(libs.compose.ui.tooling)

    // testing
    testImplementation(kotlin("test"))
}