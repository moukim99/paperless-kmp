plugins { kotlin("multiplatform"); id("com.android.library"); id("androidx.room"); id("com.google.devtools.ksp") }
kotlin { compilerOptions { optIn.add("kotlin.time.ExperimentalTime") }; jvmToolchain(17); androidTarget(); jvm("desktop")
    sourceSets { commonMain.dependencies { api(project(":core:domain")); api("androidx.room:room-runtime:2.8.5"); implementation("androidx.sqlite:sqlite-bundled:2.6.1"); implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2"); implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1") } }
}
android { namespace = "com.nextstepai.paperless.database"; compileSdk = 37; defaultConfig { minSdk = 23 } }
room { schemaDirectory("$projectDir/schemas") }
dependencies { add("kspAndroid", "androidx.room:room-compiler:2.8.5"); add("kspDesktop", "androidx.room:room-compiler:2.8.5") }
