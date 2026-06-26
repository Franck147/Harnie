package com.harnie.app.core.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {

    // TODO: Mover a BuildConfig o variables de entorno
    private const val SUPABASE_URL = "https://rqeqyfdxecycgrqabndo.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJxZXF5ZmR4ZWN5Y2dycWFibmRvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODIzMzI1NTgsImV4cCI6MjA5NzkwODU1OH0.qPzdmhfAMpb9iOk-Jtj8HaZN2qYtZfzJ6uUBjS7N0BM"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }
}