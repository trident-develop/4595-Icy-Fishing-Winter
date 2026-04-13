package com.naturalmotion.customstreetrac.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.naturalmotion.customstreetrac.audio.SoundManager
import com.naturalmotion.customstreetrac.R
import com.naturalmotion.customstreetrac.storage.GamePreferences
import com.naturalmotion.customstreetrac.ui.componets.MenuButton
import com.naturalmotion.customstreetrac.ui.componets.SquareButton
import com.naturalmotion.customstreetrac.ui.theme.GameFont
import kotlin.math.roundToInt
import kotlin.random.Random

// ── Models ──────────────────────────────────────────────────────────────────

private enum class FishColorType(val drawableRes: Int, val tintColor: Color, val label: String) {
    RED(R.drawable.fish, Color(0xFFE53935), "Red"),
    ORANGE(R.drawable.fish_2, Color(0xFFFF9800), "Orange"),
    BLUE(R.drawable.fish_3, Color(0xFF1E88E5), "Blue")
}

private enum class GameOverlayState { None, Pause, Win, Lose }

private data class FallingFish(
    val id: Long,
    val type: FishColorType,
    var x: Float,          // 0f..1f fraction of screen width
    var y: Float,          // pixels from top of game area
    val speed: Float,      // pixels per second
    val drift: Float       // horizontal drift pixels per second
)

// ── Main Screen ─────────────────────────────────────────────────────────────

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun GameScreen(
    level: Int,
    prefs: GamePreferences,
    soundManager: SoundManager,
    onHome: () -> Unit,
    onBack: () -> Unit,
    onNextLevel: (Int) -> Unit,
    onReplay: () -> Unit
) {
    // ── Difficulty scaling ───────────────────────────────────────────────
    val targetScore = (12 + level * 2).coerceAtMost(30)
    val maxLives = 3
    val baseSpeed = 140f + level * 12f          // px/sec
    val spawnIntervalMs = (1800L - level * 60L).coerceAtLeast(700L)
    val maxFishOnScreen = (3 + level / 4).coerceAtMost(7)

    // ── Game state ──────────────────────────────────────────────────────
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(maxLives) }
    var overlay by remember { mutableStateOf(GameOverlayState.None) }
    val fishList = remember { mutableStateListOf<FallingFish>() }
    var selectedFishId by remember { mutableStateOf<Long?>(null) }
    var nextId by remember { mutableLongStateOf(0L) }
    var lastSpawnTime by remember { mutableLongStateOf(0L) }

    // Screen dimensions
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val topBarHeightPx = with(density) { 72.dp.toPx() }
    val zoneHeightPx = with(density) { 110.dp.toPx() }
    val gameAreaHeight = screenHeightPx - topBarHeightPx - zoneHeightPx
    val fishSizePx = with(density) { 56.dp.toPx() }

    val isPlaying = overlay == GameOverlayState.None

    var isExitingScreen by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE && overlay != GameOverlayState.Win && overlay != GameOverlayState.Lose && !isExitingScreen)
                overlay = GameOverlayState.Pause
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    BackHandler(enabled = true) {
        isExitingScreen = true
        onBack()
    }

    // ── Spawn & update loop ─────────────────────────────────────────────
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var lastFrameTime = withFrameMillis { it }
        lastSpawnTime = lastFrameTime

        while (true) {
            val frameTime = withFrameMillis { it }
            val dt = ((frameTime - lastFrameTime) / 1000f).coerceAtMost(0.1f)
            lastFrameTime = frameTime

            // Spawn fish
            if (frameTime - lastSpawnTime >= spawnIntervalMs && fishList.size < maxFishOnScreen) {
                val type = FishColorType.entries[Random.nextInt(3)]
                val margin = fishSizePx / screenWidthPx
                val x = margin + Random.nextFloat() * (1f - 2 * margin)
                val speed = baseSpeed + Random.nextFloat() * 60f
                val drift = (Random.nextFloat() - 0.5f) * 40f
                fishList.add(
                    FallingFish(
                        id = nextId++,
                        type = type,
                        x = x,
                        y = fishSizePx * 0.5f,
                        speed = speed,
                        drift = drift
                    )
                )
                lastSpawnTime = frameTime
            }

            // Move fish
            val toRemove = mutableListOf<Long>()
            for (i in fishList.indices) {
                val fish = fishList[i]
                val newY = fish.y + fish.speed * dt
                val newX = (fish.x + fish.drift / screenWidthPx * dt).coerceIn(0.05f, 0.95f)
                fishList[i] = fish.copy(y = newY, x = newX)

                // Missed – reached bottom
                if (newY > gameAreaHeight) {
                    toRemove.add(fish.id)
                    lives = (lives - 1).coerceAtLeast(0)
                }
            }

            // Remove missed fish
            if (toRemove.isNotEmpty()) {
                fishList.removeAll { it.id in toRemove }
                if (selectedFishId in toRemove) selectedFishId = null
            }

            // Check lose
            if (lives <= 0) {
                overlay = GameOverlayState.Lose
                soundManager.playLoseSound()
                prefs.totalGamesPlayed++
            }
        }
    }

    // ── Sort handler ────────────────────────────────────────────────────
    fun onZoneTapped(zoneType: FishColorType) {
        if (!isPlaying) return
        val selId = selectedFishId ?: return
        val fish = fishList.firstOrNull { it.id == selId } ?: return

        if (fish.type == zoneType) {
            // Correct
            score++
            fishList.removeAll { it.id == selId }
            selectedFishId = null
            if (score >= targetScore) {
                overlay = GameOverlayState.Win
                soundManager.playWinSound()
                prefs.totalGamesPlayed++
                prefs.totalWins++
                if (score > prefs.bestScore) prefs.bestScore = score
                prefs.unlockNextLevel(level)
            }
        } else {
            // Wrong
            lives = (lives - 1).coerceAtLeast(0)
            fishList.removeAll { it.id == selId }
            selectedFishId = null
            if (lives <= 0) {
                overlay = GameOverlayState.Lose
                soundManager.playLoseSound()
                prefs.totalGamesPlayed++
            }
        }
    }

    // ── UI ──────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(R.drawable.bg_vertical),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize().padding(top = 40.dp)) {
            // ── Top bar ─────────────────────────────────────────────
            FishSortingTopBar(
                score = score,
                targetScore = targetScore,
                lives = lives,
                onBack = {
                    isExitingScreen = true
                    onBack()
                },
                onPause = {
                    if (isPlaying) overlay = GameOverlayState.Pause
                }
            )

            // ── Game area (fish) ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                fishList.forEach { fish ->
                    FishItem(
                        fish = fish,
                        isSelected = fish.id == selectedFishId,
                        fishSizeDp = 56,
                        onClick = {
                            if (isPlaying) {
                                selectedFishId = if (selectedFishId == fish.id) null else fish.id
                            }
                        }
                    )
                }
            }

            // ── Bottom zones ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FishColorType.entries.forEach { type ->
                    SortingZone(
                        type = type,
                        modifier = Modifier.weight(1f),
                        hasSelection = selectedFishId != null,
                        onClick = { onZoneTapped(type) }
                    )
                }
            }
        }

        // ── Overlays ────────────────────────────────────────────────
        when (overlay) {
            GameOverlayState.Pause -> GamePopup(
                onDismiss = { overlay = GameOverlayState.None }
            ) {
                MenuButton(text = "Resume", onClick = { overlay = GameOverlayState.None })
                MenuButton(text = "Replay", onClick = onReplay)
                MenuButton(text = "Home", onClick = {
                    isExitingScreen = true
                    onHome()
                })
            }

            GameOverlayState.Win -> GamePopup(onDismiss = {}) {
                Image(
                    painter = painterResource(R.drawable.you_win),
                    contentDescription = "You Win",
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.FillWidth
                )
                Text(
                    text = "Score: $score / $targetScore",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = GameFont,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                MenuButton(text = "Next", onClick = { onNextLevel(level + 1) })
                MenuButton(text = "Home", onClick = {
                    isExitingScreen = true
                    onHome()
                })
            }

            GameOverlayState.Lose -> GamePopup(onDismiss = {}) {
                Image(
                    painter = painterResource(R.drawable.game_over),
                    contentDescription = "Game Over",
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.FillWidth
                )
                Text(
                    text = "Score: $score / $targetScore",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = GameFont,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                MenuButton(text = "Replay", onClick = onReplay)
                MenuButton(text = "Home", onClick = {
                    isExitingScreen = true
                    onHome()
                })
            }

            GameOverlayState.None -> {}
        }
    }
}

// ── Top Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun FishSortingTopBar(
    score: Int,
    targetScore: Int,
    lives: Int,
    onBack: () -> Unit,
    onPause: () -> Unit
) {
    val buttonSize = 48.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        Box(modifier = Modifier.size(buttonSize)) {
            SquareButton(
                btnRes = R.drawable.back,
                btnMaxWidth = 1f,
                btnClickable = onBack
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Score HUD
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.height(52.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.score_bg),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier.fillMaxHeight(0.75f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "$score / $targetScore",
                    color = Color.Blue,
                    fontSize = 18.sp,
                    fontFamily = GameFont
                )
                Spacer(modifier = Modifier.width(16.dp))

                repeat(3) { i ->
                    Text(
                        text = if (i < lives) "❤" else "♡",
                        fontSize = 16.sp,
                        color = if (i < lives) Color.Red else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Pause button
        Box(modifier = Modifier.size(buttonSize)) {
            SquareButton(
                btnRes = R.drawable.pause,
                btnMaxWidth = 1f,
                btnClickable = onPause
            )
        }
    }
}

// ── Fish Item ───────────────────────────────────────────────────────────────

@Composable
private fun FishItem(
    fish: FallingFish,
    isSelected: Boolean,
    fishSizeDp: Int,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val parentWidth = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val fishSizePx = with(density) { fishSizeDp.dp.toPx() }
    val offsetX = (fish.x * parentWidth - fishSizePx / 2f)
    val offsetY = fish.y

    // Selection pulse animation
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = tween(200),
        label = "fishSelect"
    )

    // Gentle wobble
    val wobble = remember { Animatable(0f) }
    LaunchedEffect(fish.id) {
        wobble.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600 + Random.nextInt(400), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    val wobbleAngle = (wobble.value - 0.5f) * 12f

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(fishSizeDp.dp)
            .graphicsLayer {
                scaleX = selectionScale
                scaleY = selectionScale
                rotationZ = wobbleAngle
            }
            .then(
                if (isSelected) Modifier.border(2.dp, Color.White, CircleShape) else Modifier
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(fish.type.drawableRes),
            contentDescription = fish.type.label,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(0.85f)
        )
    }
}

// ── Sorting Zone ────────────────────────────────────────────────────────────

@Composable
private fun SortingZone(
    type: FishColorType,
    modifier: Modifier = Modifier,
    hasSelection: Boolean,
    onClick: () -> Unit
) {
    val pulseAlpha by animateFloatAsState(
        targetValue = if (hasSelection) 0.85f else 0.55f,
        animationSpec = tween(300),
        label = "zonePulse"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(type.tintColor.copy(alpha = pulseAlpha))
            .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(type.drawableRes),
                contentDescription = type.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = type.label,
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = GameFont,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Popup ───────────────────────────────────────────────────────────────────

@Composable
private fun GamePopup(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.bg_vertical),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(initialScale = 0.7f, animationSpec = tween(300)) +
                    fadeIn(animationSpec = tween(300))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // consume clicks on popup body
                    )
            ) {
                Image(
                    painter = painterResource(R.drawable.popup_1),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(1.1f)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    content()
                }
            }
        }
    }
}