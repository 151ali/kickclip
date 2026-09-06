package com.kickclip.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.kickclip.app.ui.theme.KickclipTheme
import com.kickclip.app.ui.theme.SheetSurface
import com.kickclip.app.ui.theme.TextPrimary
import com.kickclip.app.ui.theme.TextSecondary
import com.kickclip.app.ui.theme.Typography

/** TikTok's standard and "Lite" package names, tried in order. */
private val TIKTOK_PACKAGES = listOf(
    "com.zhiliaoapp.musically",
    "com.ss.android.ugc.trill"
)

/**
 * Main launcher Activity for Kickclip.
 *
 * Shows a short "how it works" explainer for the native share-sheet target
 * (see ShareActivity) and a button to jump straight into TikTok.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KickclipTheme {
                HomeScreen(onOpenTikTok = { openTikTok() })
            }
        }
    }

    private fun openTikTok() {
        val pm = packageManager
        val installedPackage = TIKTOK_PACKAGES.firstOrNull { pkg ->
            pm.getLaunchIntentForPackage(pkg) != null
        }

        val launchIntent = installedPackage?.let { pm.getLaunchIntentForPackage(it) }

        try {
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                // TikTok isn't installed — send the user to the Play Store listing.
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${TIKTOK_PACKAGES[0]}")
                    )
                )
            }
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${TIKTOK_PACKAGES[0]}")
                )
            )
        }
    }
}

@Composable
private fun HomeScreen(onOpenTikTok: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackdrop)
            .statusBarsPadding()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with brand accent
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

        // How it works
        Card(
            colors = CardDefaults.cardColors(containerColor = SheetSurface),
            border = BorderStroke(1.dp, CardBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "HOW IT WORKS",
                    style = Typography.labelSmall,
                    color = AccentCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Open TikTok and find a video or profile you want to save.\n" +
                        "2. Tap Share, then choose Kickclip from the share sheet.\n" +
                        "3. Kickclip opens as a floating sheet over TikTok — add a note and send.\n" +
                        "4. Your clip shows up here, organized and ready whenever you need it.",
                    style = Typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            }
        }

        // First-run tip: new share targets take a couple of uses to be promoted
        // into Android's direct-share row, so we call that out up front.
        Card(
            colors = CardDefaults.cardColors(containerColor = SheetSurface),
            border = BorderStroke(1.dp, CardBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TIP",
                    style = Typography.labelSmall,
                    color = AccentCoral
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Don't see Kickclip in the top row of the share sheet yet? Android " +
                        "ranks that row by how often you've used each app there. Pick " +
                        "\"Kickclip\" from the full list a couple of times and it'll move up.",
                    style = Typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onOpenTikTok,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
        ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text("Open TikTok", style = Typography.labelLarge)
        }
    }
}
