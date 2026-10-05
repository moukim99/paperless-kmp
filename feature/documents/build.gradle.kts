plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
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
            api(project(":core:domain"))
            api(project(":core:database"))
            implementation("androidx.room:room-runtime:2.8.5")
            implementation("androidx.sqlite:sqlite-bundled:2.6.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
            api("io.ktor:ktor-client-core:3.6.0")
            implementation("io.ktor:ktor-client-content-negotiation:3.6.0")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.6.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:3.6.0")
            implementation("androidx.work:work-runtime-ktx:2.10.0")
        }
        val desktopMain by getting
        desktopMain.dependencies {
            implementation("io.ktor:ktor-client-cio:3.6.0")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
    }
}

android {
    namespace = "com.nextstepai.paperless.documents"
    compileSdk = 37
    defaultConfig {
        minSdk = 24
    }
}
