package dj.music.mixer.sound.effe.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dj.music.mixer.sound.effe.R
import dj.music.mixer.sound.effe.ui.componets.SquareButton
import dj.music.mixer.sound.effe.ui.theme.GameFont

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_vertical),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 16.dp, start = 24.dp, end = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SquareButton(
                    btnRes = R.drawable.back,
                    btnMaxWidth = 0.14f,
                    btnClickable = onBack
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "How To Play",
                fontFamily = GameFont,
                fontSize = 32.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                HelpSection(
                    title = "Goal",
                    body = "Fish fall from the top of the screen. Sort each fish into the correct color zone at the bottom before it reaches the ground!"
                )
                HelpSection(
                    title = "How to Sort",
                    body = "1. Tap a falling fish to select it — it will glow and grow slightly.\n2. Tap the matching color zone at the bottom to send it there.\n\nCorrect sort = +1 point. Wrong zone = lose 1 life."
                )
                HelpSection(
                    title = "Zones",
                    body = "There are 3 sorting zones at the bottom of the screen:\n\u2022 Red zone (left) — for red fish\n\u2022 Orange zone (center) — for orange fish\n\u2022 Blue zone (right) — for blue fish"
                )
                HelpSection(
                    title = "Lives",
                    body = "You start with 3 lives. You lose a life when:\n\u2022 You sort a fish into the wrong zone\n\u2022 A fish reaches the bottom without being sorted\n\nIf all lives are lost, the round is over."
                )
                HelpSection(
                    title = "Scoring",
                    body = "Each correctly sorted fish gives you 1 point. Reach the target score to win the level and unlock the next one."
                )
                HelpSection(
                    title = "Tips",
                    body = "Act fast — fish fall faster in higher levels and more appear at once. Prioritize the fish closest to the bottom. You can tap a different fish to change your selection at any time."
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun HelpSection(title: String, body: String) {
    Text(
        text = title,
        fontFamily = GameFont,
        fontSize = 22.sp,
        color = Color(0xFFFFD54F)
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = body,
        fontFamily = GameFont,
        fontSize = 17.sp,
        color = Color.White,
        lineHeight = 22.sp
    )
    Spacer(modifier = Modifier.height(20.dp))
}