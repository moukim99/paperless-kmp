@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.nextstepai.paperless.auth

import kotlinx.datetime.Instant

data class AuthUser(
    val id: String,
    val email: String,
    val role: UserRole,
)

enum class UserRole { ADMIN, USER }

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Instant,
    val user: AuthUser,
)

interface AuthTokenProvider {
    suspend fun accessToken(): String?
    suspend fun refresh(): String?
}
