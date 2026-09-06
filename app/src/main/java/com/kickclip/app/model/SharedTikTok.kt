package com.kickclip.app.model

/**
 * Represents the parsed TikTok target extracted from a shared link.
 */
sealed class SharedTikTok {
    /**
     * Represents a shared TikTok user profile (e.g. tiktok.com/@username)
     */
    data class Profile(val username: String) : SharedTikTok()

    /**
     * Represents a shared TikTok video (e.g. tiktok.com/@username/video/1234567890)
     */
    data class Video(val username: String, val videoId: String) : SharedTikTok()
}
