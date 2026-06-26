package com.harnie.app.domain.repository

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signOut()
    suspend fun isAuthenticated(): Boolean
    fun getCurrentUserId(): String?
}