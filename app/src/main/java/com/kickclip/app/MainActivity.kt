package com.kickclip.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kickclip.app.ui.theme.AccentCoral
import com.kickclip.app.ui.theme.AccentCyan
import com.kickclip.app.ui.theme.AccentGradient
import com.kickclip.app.ui.theme.CardBorder
import com.kickclip.app.ui.theme.DarkBackdrop
import com.kickclip.app.ui.theme.InputSurface
import com.kickclip.app.ui.theme.KickclipTheme
import com.kickclip.app.ui.theme.SheetSurface
import com.kickclip.app.ui.theme.TextPrimary
import com.kickclip.app.ui.theme.TextSecondary
import com.kickclip.app.ui.theme.Typography

/**
 * Main launcher Activity for Kickclip.
 * Explains how the native share target operates, and includes built-in test triggers
 * to fire ACTION_SEND intents simulating TikTok shares directly.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KickclipTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackdrop)
                        .statusBarsPadding()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with Brand Accent
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = DarkBackdrop,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "KICKCLIP",
                                style = Typography.headlineSmall,
                                color = TextPrimary
                            )
                            Text(
                                text = "TikTok Native Share Sheet Target",
                                style = Typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    // Explanation Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SheetSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "HOW TO USE",
                                style = Typography.labelSmall,
                                color = AccentCyan
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. Open TikTok or TikTok Lite.\n" +
                                       "2. Tap the Share button on any video or profile.\n" +
                                       "3. Choose 'Kickclip' from the Android share sheet.\n" +
                                       "4. Kickclip will open as a floating bottom sheet over TikTok to let you add a note and send.",
                                style = Typography.bodyMedium,
                                color = TextSecondary,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    // Test Actions Header
                    Text(
                        text = "TEST SHARE TARGET",
                        style = Typography.labelSmall,
                        color = TextSecondary
                    )

                    // Test 1: Video Link
                    Button(
                        onClick = {
                            simulateShare("Check out this dance! https://vm.tiktok.com/ZM8x9P123/")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Simulate TikTok Video Share", style = Typography.labelLarge)
                    }

                    // Test 2: TikTok Lite Share with trailing promo text
                    OutlinedButton(
                        onClick = {
                            simulateShare("https://vm.tiktok.com/ZM8x9P456/ Download TikTok Lite to earn cash rewards!")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(999.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Text("Simulate TikTok Lite Video (with promo text)", style = Typography.labelLarge, color = TextPrimary)
                    }

                    // Test 3: Profile Link
                    OutlinedButton(
                        onClick = {
                            simulateShare("Follow this creator: https://vm.tiktok.com/ZM8profile12/")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(999.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Text("Simulate TikTok Profile Share", style = Typography.labelLarge, color = TextPrimary)
                    }

                    // ADB Command snippet
                    Card(
                        colors = CardDefaults.cardColors(containerColor = InputSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "TEST VIA ADB SHELL",
                                style = Typography.labelSmall,
                                color = AccentCoral
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "adb shell am start -a android.intent.action.SEND -t text/plain -e android.intent.extra.TEXT 'https://vm.tiktok.com/ZMBxxxx/'",
                                style = Typography.bodyMedium.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    private fun simulateShare(content: String) {
        val intent = Intent(this, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
        }
        startActivity(intent)
    }
}
