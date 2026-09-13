package com.kickclip.app.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickclip.app.data.ClipInsert
import com.kickclip.app.data.SupabaseClientProvider
import com.kickclip.app.data.insertClip
import com.kickclip.app.model.SharedTikTok
import com.kickclip.app.parser.TikTokLinkParser
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Parsing lifecycle states for the shared TikTok link.
 */
sealed class ParsingState {
    object Idle : ParsingState()
    object Loading : ParsingState()
    data class Parsed(val data: SharedTikTok) : ParsingState()
    data class Error(val message: String) : ParsingState()
}

/**
 * Sending lifecycle states for saving the note and TikTok payload.
 */
sealed class SendState {
    object Idle : SendState()
    object Sending : SendState()
    object Sent : SendState()
    data class Error(val message: String) : SendState()
}

/**
 * One-off UI navigation/lifecycle events (e.g. dismissing the floating dialog Activity).
 */
sealed class UiEvent {
    object DismissActivity : UiEvent()
}

class ShareViewModel(
    private val linkParser: TikTokLinkParser = TikTokLinkParser()
) : ViewModel() {

    companion object {
        private const val TAG = "KickclipShare"
    }

    private val _parsingState = MutableStateFlow<ParsingState>(ParsingState.Idle)
    val parsingState: StateFlow<ParsingState> = _parsingState.asStateFlow()

    private val _sendState = MutableStateFlow<SendState>(SendState.Idle)
    val sendState: StateFlow<SendState> = _sendState.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    /**
     * Process incoming shared text from the Android Share Sheet ACTION_SEND intent.
     */
    fun processSharedText(sharedText: String?) {
        if (sharedText.isNullOrBlank()) {
            _parsingState.value = ParsingState.Error("No content received from share sheet.")
            return
        }

        _parsingState.value = ParsingState.Loading

        viewModelScope.launch {
            // Step 1 - 5 in link parsing pipeline
            val result = linkParser.parse(sharedText)
            result.onSuccess { data ->
                _parsingState.value = ParsingState.Parsed(data)
            }.onFailure { error ->
                Log.e(TAG, "Link parsing failed", error)
                _parsingState.value = ParsingState.Error(
                    error.localizedMessage ?: "Failed to resolve TikTok URL."
                )
            }
        }
    }

    fun onNoteChanged(newNote: String) {
        _note.value = newNote
    }

    fun onCancelClicked() {
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.DismissActivity)
        }
    }

    /**
     * Executes the send action.
     * Transitions: "Send" -> "Sending…" -> "Sent ✓" -> dismiss after ~700ms
     * (or -> "Error" on failure, leaving the sheet open so the user can retry).
     */
    fun onSendClicked() {
        val currentParsed = (_parsingState.value as? ParsingState.Parsed)?.data ?: return
        // Allow retrying from Error, but not while already Sending/Sent.
        if (_sendState.value is SendState.Sending || _sendState.value is SendState.Sent) return

        viewModelScope.launch {
            _sendState.value = SendState.Sending

            val username = when (currentParsed) {
                is SharedTikTok.Profile -> currentParsed.username
                is SharedTikTok.Video -> currentParsed.username
            }
            val videoId = (currentParsed as? SharedTikTok.Video)?.videoId
            val noteContent = _note.value.trim()

            try {
                SupabaseClientProvider.ensureSignedIn()
                val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
                    ?: error("No Supabase session after sign-in")

                insertClip(
                    ClipInsert(
                        userId = userId,
                        username = username,
                        videoId = videoId,
                        note = noteContent
                    )
                )

                _sendState.value = SendState.Sent
                delay(700)
                _uiEvents.emit(UiEvent.DismissActivity)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save clip to Supabase", e)
                _sendState.value = SendState.Error(
                    e.localizedMessage ?: "Failed to save clip. Check your connection and try again."
                )
            }
        }
    }

    /** Lets the UI return to Idle after showing an error, so Send can be retried. */
    fun onErrorAcknowledged() {
        if (_sendState.value is SendState.Error) {
            _sendState.value = SendState.Idle
        }
    }
}
