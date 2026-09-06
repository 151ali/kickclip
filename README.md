# Kickclip - Android Share Target for TikTok

Kickclip is an Android application written in Kotlin and Jetpack Compose that acts as a native **Share Target** (`ACTION_SEND`, `mimeType="text/plain"`). When a user shares a TikTok link (video or profile) from TikTok or TikTok Lite, Kickclip intercepts the intent, opens a floating translucent bottom-sheet dialog Activity over TikTok, resolves the short link redirect chain, displays video/profile metadata, and lets the user attach a note before sending.

## Project Structure

```
android/
├── build.gradle.kts                   # Project-level Gradle build
├── settings.gradle.kts                # Subproject and repository settings
├── gradle.properties                  # JVM & AndroidX memory options
├── gradle/
│   └── libs.versions.toml             # Gradle Version Catalog (Compose, OkHttp, Coroutines)
└── app/
    ├── build.gradle.kts               # App module build with Compose & OkHttp dependencies
    └── src/
        └── main/
            ├── AndroidManifest.xml    # Intent-filter for ACTION_SEND + Dialog Activity config
            ├── res/
                └── values/
                    ├── colors.xml     # Design tokens (#050506, #141416, #1C1C20, #FF5A36, #34E4D0)
                    ├── strings.xml    # Localization strings
                    └── themes.xml     # Theme.Kickclip.Dialog (translucent floating dialog)
            └── java/com/kickclip/app/
                ├── MainActivity.kt    # Launcher and intent testbed
                ├── ShareActivity.kt   # Dialog Activity registered for ACTION_SEND
                ├── model/
                │   └── SharedTikTok.kt# Sealed class: Profile vs Video
                ├── parser/
                │   └── TikTokLinkParser.kt # Regex parsing + OkHttp redirect resolver
                └── ui/
                    ├── ShareBottomSheet.kt # Jetpack Compose UI with design tokens
                    ├── ShareViewModel.kt   # StateFlow parsing & send states
                    └── theme/
                        ├── Color.kt
                        ├── Theme.kt
                        └── Type.kt
```

## Core Features & Pipeline

1. **Native Share Target**:
   - Registered in `AndroidManifest.xml` with `android:theme="@style/Theme.Kickclip.Dialog"`
   - `android:mimeType="text/plain"`
   - Dialog style with translucent background so TikTok remains visible underneath.
2. **Regex URL Extraction**:
   - `https:\/\/vm\.tiktok\.com\/[a-zA-Z0-9]+\/?`
   - Strips trailing promotional text (such as TikTok Lite "Download TikTok Lite to earn...").
3. **OkHttp Redirect Resolution**:
   - Follows redirect chain automatically on `Dispatchers.IO` via `client.newCall(request).execute()`.
4. **Canonical URL Parsing**:
   - `tiktok\.com\/@([a-zA-Z0-9_.]+)(?:\/video\/(\d+))?`
   - Extracts `@username` and optional numeric `videoId`.
5. **Interactive Jetpack Compose UI**:
   - Accent-gradient handle bar (`#FF5A36` to `#34E4D0`)
   - Header with `SEND TO KICKCLIP` and dismiss button
   - Preview card with video/profile badge and play icon
   - Note input field
   - 3-state send button: `Send` -> `Sending…` (disabled spinner) -> `Sent ✓` -> auto-dismiss after ~700ms.

## Production Swap Points

- **Redirect Resolution**: In `TikTokLinkParser.kt`, replace the OkHttp direct HEAD request with a dedicated proxy backend if scraping or user-agent anti-bot challenges arise.
- **Send API**: In `ShareViewModel.kt`, replace the `delay(900)` and `Log.d` stub with a REST / GraphQL backend call (e.g. Retrofit or Ktor) submitting `{ username, videoId, note }`.

## Testing via ADB

```bash
adb shell am start -a android.intent.action.SEND \
  -t text/plain \
  -e android.intent.extra.TEXT "https://vm.tiktok.com/ZM8x9P123/"
```
