package com.nextstepai.paperless.documents.data
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
actual fun createHttpClient(): HttpClient = HttpClient(OkHttp)
