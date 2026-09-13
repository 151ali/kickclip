package com.kickclip.app.data

import com.kickclip.app.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Single shared Supabase client for the app.
 *
 * SUPABASE_URL / SUPABASE_ANON_KEY come from BuildConfig, which in turn are
 * generated from local.properties at build time (see app/build.gradle.kts).
 * The anon key is meant to be public -- it is safe to ship inside the app
 * ONLY because every table it can touch has Row Level Security policies
 * that key access off auth.uid() (see supabase/schema.sql). It is NOT the
 * same thing as a database password and must never be swapped for the
 * project's service_role key, which bypasses RLS entirely.
 */
object SupabaseClientProvider {

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    /**
     * Ensures there's a signed-in session before any Postgrest call that
     * relies on auth.uid() in an RLS policy. Uses anonymous sign-in, so
     * there's no login screen -- each install gets its own stable user id
     * (persisted by the Auth plugin's session storage) and RLS scopes all
     * of that install's rows to it.
     */
    suspend fun ensureSignedIn() {
        if (client.auth.currentSessionOrNull() == null) {
            client.auth.signInAnonymously()
        }
    }
}

/**
 * Row shape for public.clips, matching supabase/schema.sql.
 * user_id is intentionally omitted -- Postgrest/Postgres fills it from
 * auth.uid() by default if you set a column default, or you can pass it
 * explicitly from client.auth.currentUserOrNull()?.id.
 */
@Serializable
data class ClipInsert(
    @SerialName("user_id") val userId: String,
    val username: String,
    @SerialName("video_id") val videoId: String? = null,
    val note: String
)

suspend fun insertClip(clip: ClipInsert) {
    SupabaseClientProvider.client.postgrest["clips"].insert(clip)
}
