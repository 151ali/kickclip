package com.kickclip.app.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickclip.app.model.SharedTikTok
import com.kickclip.app.parser.TikTokLinkParser
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
     * Transitions: "Send" -> "Sending…" -> "Sent ✓" -> dismiss after ~700ms.
     *
     * PRODUCTION SWAP POINT:
     * Replace this simulated delay and Log.d call with a Retrofit / Ktor / Room client
     * to persist or broadcast { username, videoId, note }.
     */
    fun onSendClicked() {
        val currentParsed = (_parsingState.value as? ParsingState.Parsed)?.data ?: return
        if (_sendState.value != SendState.Idle) return

        viewModelScope.launch {
            _sendState.value = SendState.Sending

            val username = when (currentParsed) {
                is SharedTikTok.Profile -> currentParsed.username
                is SharedTikTok.Video -> currentParsed.username
            }
            val videoId = (currentParsed as? SharedTikTok.Video)?.videoId
            val noteContent = _note.value.trim()

            // -------------------------------------------------------------
            // Simulated network delay (900ms) as specified in requirements
            // -------------------------------------------------------------
            delay(900)

            // Log final payload
            Log.d(TAG, "Successfully submitted Kickclip payload:")
            Log.d(TAG, "Payload -> username: $username, videoId: $videoId, note: \"$noteContent\"")

            // Transition to "Sent ✓"
            _sendState.value = SendState.Sent

            // Auto-dismiss the Activity after ~700ms
            delay(700)
            _uiEvents.emit(UiEvent.DismissActivity)
        }
    }
}
