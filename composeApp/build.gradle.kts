plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
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
            implementation(project(":feature:documents"))
            implementation(project(":core:domain"))
            implementation(project(":core:database"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation("androidx.compose.material3.adaptive:adaptive:1.3.0")
            implementation("androidx.compose.material3.adaptive:adaptive-layout:1.3.0")
            implementation(compose.ui)
            implementation(compose.components.resources)
        }
        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.11.0")
            implementation("androidx.work:work-runtime:2.12.0")
            implementation("androidx.core:core-ktx:1.17.0")
            implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0")
        }
        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

android {
    namespace = "com.nextstepai.paperless"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.nextstepai.paperless"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "API_BASE_URL", "\"https://example.invalid\"")
        buildConfigField("String", "API_TOKEN", "\"\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

compose.desktop {
    application {
        mainClass = "com.nextstepai.paperless.MainKt"
    }
}
