package com.kickclip.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kickclip.app.ui.ParsingState
import com.kickclip.app.ui.ShareBottomSheetContent
import com.kickclip.app.ui.ShareViewModel
import com.kickclip.app.ui.UiEvent
import com.kickclip.app.ui.theme.KickclipTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * Dialog-themed Activity that responds to ACTION_SEND with mime type text/plain.
 * When a user shares a TikTok link from the official TikTok or TikTok Lite app,
 * this floating dialog Activity renders directly on top of the calling app.
 *
 * Tapping outside the sheet, tapping the close button, or pressing the back key
 * dismisses this Activity and immediately returns the user to TikTok.
 */
class ShareActivity : ComponentActivity() {

    private val viewModel: ShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIncomingShareIntent(intent)

        setContent {
            KickclipTheme {
                val parsingState by viewModel.parsingState.collectAsState()
                val sendState by viewModel.sendState.collectAsState()
                val note by viewModel.note.collectAsState()

                // Observe dismiss events from ViewModel
                LaunchedEffect(Unit) {
                    viewModel.uiEvents.collectLatest { event ->
                        when (event) {
                            is UiEvent.DismissActivity -> finish()
                        }
                    }
                }

                ShareBottomSheetContent(
                    parsingState = parsingState,
                    sendState = sendState,
                    note = note,
                    onNoteChange = viewModel::onNoteChanged,
                    onCancel = { finish() },
                    onSend = viewModel::onSendClicked
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingShareIntent(intent)
    }

    /**
     * Inspects the intent for ACTION_SEND and extracts the shared text containing the TikTok link.
     */
    private fun handleIncomingShareIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("text/") == true) {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                        ?: intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
                    viewModel.processSharedText(sharedText)
                } else {
                    viewModel.processSharedText(null)
                }
            }
            else -> {
                // If opened directly without ACTION_SEND, show error state
                viewModel.processSharedText(null)
            }
        }
    }
}
