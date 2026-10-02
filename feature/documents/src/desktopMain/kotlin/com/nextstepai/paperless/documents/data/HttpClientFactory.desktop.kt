package com.nextstepai.paperless.documents.data
import io.ktor.client.*
import io.ktor.client.engine.cio.*
actual fun createHttpClient(): HttpClient = HttpClient(CIO)
