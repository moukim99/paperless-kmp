plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }
    jvmToolchain(17)
    androidTarget()
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            api("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
        }
        androidMain.dependencies {
            implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
        }
    }
}

android {
    namespace = "com.nextstepai.paperless.domain"
    compileSdk = 37
}
