package com.kickclip.app.parser

import com.kickclip.app.model.SharedTikTok
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Handles the 5-step TikTok link parsing pipeline:
 * 1. Extract short URL from raw share text (regex: https:\/\/vm\.tiktok\.com\/[a-zA-Z0-9]+\/?)
 * 2. Resolve short link redirect chain using OkHttp on Dispatchers.IO
 * 3. Extract username and optional video ID (regex: tiktok\.com\/@([a-zA-Z0-9_.]+)(?:\/video\/(\d+))?)
 * 4. Construct SharedTikTok sealed class representation
 * 5. Handle all failure modes gracefully without crashing
 */
class TikTokLinkParser(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {

    companion object {
        // Step 1: Match TikTok short links like https://vm.tiktok.com/ZMBxxxx/ or https://vm.tiktok.com/ABC123
        // Also supports vt.tiktok.com or direct links gracefully as a fallback
        private val SHORT_LINK_REGEX = Regex("""https://vm\.tiktok\.com/[a-zA-Z0-9]+/?|https://vt\.tiktok\.com/[a-zA-Z0-9]+/?""")

        // Step 3: Match username and video ID from canonical resolved URL
        // Group 1: Username (e.g. charlidamelio)
        // Group 2: Video ID (e.g. 7123456789012345678)
        private val RESOLVED_URL_REGEX = Regex("""tiktok\.com/@([a-zA-Z0-9_.]+)(?:/video/(\d+))?""")
    }

    /**
     * Extracts the TikTok short URL from raw shared text, ignoring any trailing promotional
     * text appended by TikTok (e.g. "Download TikTok Lite to earn points...").
     */
    fun extractShortUrl(rawText: String): String? {
        val match = SHORT_LINK_REGEX.find(rawText)
        return match?.value?.trim()
    }

    /**
     * Resolves the full URL redirect chain using OkHttp and parses the TikTok metadata.
     *
     * PRODUCTION SWAP POINT:
     * In a production environment with strict network constraints, you can swap this OkHttp
     * redirect resolver with a lightweight server-side resolver or proxy endpoint
     * if TikTok requires specific User-Agent or anti-bot verification headers.
     */
    suspend fun parse(rawText: String): Result<SharedTikTok> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Extract URL from raw text
            val shortUrl = extractShortUrl(rawText)
                ?: return@withContext Result.failure(
                    IllegalArgumentException("No valid TikTok link found in shared content.")
                )

            // Step 2: Resolve short link redirect chain automatically
            val resolvedUrl = resolveRedirects(shortUrl)

            // Step 3: Extract data from resolved URL
            val match = RESOLVED_URL_REGEX.find(resolvedUrl)
                ?: return@withContext Result.failure(
                    IllegalArgumentException("Could not extract TikTok profile or video from: $resolvedUrl")
                )

            val username = match.groupValues.getOrNull(1)
                ?: return@withContext Result.failure(
                    IllegalArgumentException("Missing TikTok username in resolved URL.")
                )

            val videoId = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }

            // Step 4: Represent parsed result
            val sharedTikTok = if (videoId != null) {
                SharedTikTok.Video(username = username, videoId = videoId)
            } else {
                SharedTikTok.Profile(username = username)
            }

            Result.success(sharedTikTok)
        } catch (e: Exception) {
            // Step 5: Graceful error handling
            Result.failure(e)
        }
    }

    /**
     * Executes an HTTP HEAD or GET request following redirects automatically to obtain
     * the final destination URL.
     */
    @Throws(IOException::class)
    private fun resolveRedirects(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/109.0 Firefox/109.0")
            .head()
            .build()

        client.newCall(request).execute().use { response ->
            // OkHttp updates response.request.url with the final redirected URL
            return response.request.url.toString()
        }
    }
}
