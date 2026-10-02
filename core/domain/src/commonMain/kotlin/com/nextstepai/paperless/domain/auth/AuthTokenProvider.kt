package com.nextstepai.paperless.domain.auth

interface AuthTokenProvider {
    suspend fun accessToken(): String?
}
