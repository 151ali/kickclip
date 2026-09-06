package com.kickclip.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kickclip.app.model.SharedTikTok
import com.kickclip.app.ui.theme.AccentCoral
import com.kickclip.app.ui.theme.AccentCyan
import com.kickclip.app.ui.theme.AccentGradient
import com.kickclip.app.ui.theme.CardBorder
import com.kickclip.app.ui.theme.InputSurface
import com.kickclip.app.ui.theme.SheetSurface
import com.kickclip.app.ui.theme.TextError
import com.kickclip.app.ui.theme.TextPlaceholder
import com.kickclip.app.ui.theme.TextPrimary
import com.kickclip.app.ui.theme.TextSecondary
import com.kickclip.app.ui.theme.Typography

@Composable
fun ShareBottomSheetContent(
    parsingState: ParsingState,
    sendState: SendState,
    note: String,
    onNoteChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Outer semi-transparent scrim container allowing tap-to-dismiss outside
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCancel
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bottom Sheet Surface
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Prevent clicks on the sheet from dismissing
                )
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(SheetSurface)
                .border(
                    width = 1.dp,
                    color = CardBorder,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .imePadding()
                .navigationBarsPadding()
        ) {
            // 1. Accent Gradient Handle Bar (Coral -> Cyan)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(AccentGradient)
                )
            }

            // 2. Header Row: "SEND TO KICKCLIP" + Close (X)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SEND TO KICKCLIP",
                    style = Typography.headlineSmall,
                    color = TextPrimary
                )

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 3. Preview Card or Inline States
            when (parsingState) {
                is ParsingState.Loading, is ParsingState.Idle -> {
                    LoadingPreviewCard()
                }
                is ParsingState.Error -> {
                    ErrorPreviewCard(message = parsingState.message)
                }
                is ParsingState.Parsed -> {
                    ParsedPreviewCard(data = parsingState.data)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Labeled Multi-line TextField
            Text(
                text = "ADD A NOTE",
                style = Typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(InputSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                if (note.isEmpty()) {
                    Text(
                        text = "What's the highlight here?",
                        color = TextPlaceholder,
                        style = Typography.bodyMedium
                    )
                }
                BasicTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(AccentCoral),
                    maxLines = 4
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Two Full-Width-Split Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel Button (Outlined, pill-shaped 999dp)
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(CardBorder)
                    )
                ) {
                    Text(
                        text = "Cancel",
                        style = Typography.labelLarge,
                        color = TextSecondary
                    )
                }

                // Send Button with 3 states: "Send" -> "Sending…" -> "Sent ✓"
                val isSendEnabled = parsingState is ParsingState.Parsed && sendState == SendState.Idle

                Button(
                    onClick = onSend,
                    enabled = isSendEnabled,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCoral,
                        disabledContainerColor = if (sendState == SendState.Sent) AccentCoral.copy(alpha = 0.85f) else AccentCoral.copy(alpha = 0.5f),
                        contentColor = TextPrimary,
                        disabledContentColor = TextPrimary
                    )
                ) {
                    when (sendState) {
                        is SendState.Idle -> {
                            Text(
                                text = "Send",
                                style = Typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        is SendState.Sending -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = TextPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Sending…",
                                    style = Typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        is SendState.Sent -> {
                            Text(
                                text = "Sent ✓",
                                style = Typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview card rendering the parsed @username, subtitle, thumbnail, and VIDEO/PROFILE badge.
 */
@Composable
private fun ParsedPreviewCard(data: SharedTikTok) {
    val isVideo = data is SharedTikTok.Video
    val username = when (data) {
        is SharedTikTok.Profile -> data.username
        is SharedTikTok.Video -> data.username
    }
    val subtitle = when (data) {
        is SharedTikTok.Profile -> "Profile"
        is SharedTikTok.Video -> {
            val truncatedId = if (data.videoId.length > 10) data.videoId.take(10) + "…" else data.videoId
            "Video · $truncatedId"
        }
    }
    val badgeText = if (isVideo) "VIDEO" else "PROFILE"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(InputSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Small square thumbnail placeholder (gradient background, centered play icon)
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Username and Subtitle
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "@$username",
                style = Typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = Typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Small pill status badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(CardBorder)
                .padding(horizontal = 9.dp, vertical = 4.dp)
        ) {
            Text(
                text = badgeText,
                style = Typography.labelSmall.copy(fontSize = 10.sp),
                color = AccentCyan
            )
        }
    }
}

@Composable
private fun LoadingPreviewCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(InputSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = AccentCoral,
            strokeWidth = 2.5.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Resolving TikTok link…",
                style = Typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = "Following redirect chain",
                style = Typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ErrorPreviewCard(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(InputSurface)
            .border(1.dp, TextError.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = TextError,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Parsing failed",
                style = Typography.titleMedium,
                color = TextError
            )
            Text(
                text = message,
                style = Typography.bodyMedium,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
