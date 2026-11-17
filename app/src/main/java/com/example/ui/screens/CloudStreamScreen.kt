package com.example.ui.screens

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameCategory
import com.example.data.NetworkStatus
import com.example.data.StreamQuality
import com.example.data.findActivity
import com.example.ui.components.GameCoverArtwork
import com.example.ui.components.GhostGradientButton
import com.example.ui.components.NetworkTelemetryChip
import com.example.ui.theme.GhostBlue
import com.example.ui.theme.GhostBorder
import com.example.ui.theme.GhostCyan
import com.example.ui.theme.GhostDiagonalBrush
import com.example.ui.theme.GhostRed
import com.example.ui.theme.GhostSurface
import com.example.ui.theme.GhostTextPrimary
import com.example.ui.theme.GhostTextSecondary
import com.example.ui.theme.GhostVoid
import com.example.ui.theme.XboxButtonA
import com.example.ui.theme.XboxButtonB
import com.example.ui.theme.XboxButtonX
import com.example.ui.theme.XboxButtonY
import com.example.ui.theme.XboxGreen
import com.example.viewmodel.ActiveCloudSession
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun CloudStreamScreen(
    session: ActiveCloudSession,
    streamQuality: StreamQuality,
    networkStatus: NetworkStatus,
    onEndSession: (elapsedMinutes: Long, syncedProgressDelta: Int) -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var landscapeLocked by remember { mutableStateOf(false) }
    var showController by remember { mutableStateOf(true) }
    var bootProgress by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var actionsTriggered by remember { mutableIntStateOf(0) }
    var lastActionFeedback by remember { mutableStateOf("Ready • Xbox Virtual Gamepad Active") }
    var leftStickOffset by remember { mutableStateOf(Offset.Zero) }

    // Restore sensor orientation when leaving cloud session
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Smooth cloud rig handshake & session timer
    LaunchedEffect(session.game.id) {
        for (step in 1..10) {
            bootProgress = step * 10
            delay(90L)
        }
        while (true) {
            delay(1000L)
            elapsedSeconds += 1
        }
    }

    val finishAndSync: () -> Unit = {
        val elapsedMins = ((elapsedSeconds / 60) + 1).toLong()
        val progressBoost = (actionsTriggered / 3).coerceIn(1, 10)
        onEndSession(elapsedMins, progressBoost)
    }

    BackHandler(onBack = finishAndSync)

    val infiniteTransition = rememberInfiniteTransition(label = "hudPulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hudSweep"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostVoid)
    ) {
        // Full-bleed Game Stream Backdrop
        GameCoverArtwork(
            game = session.game,
            modifier = Modifier.fillMaxSize()
        )

        // Live Genre-Aware Interactive HUD Overlay Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val sweepY = h * pulsePhase

            // Subtle scanline sweep
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, GhostCyan.copy(alpha = 0.25f), Color.Transparent)
                ),
                start = Offset(0f, sweepY),
                end = Offset(w, sweepY),
                strokeWidth = 2f
            )

            // Center interactive reticle / speed arc responding to thumbstick
            val center = Offset(w * 0.5f + leftStickOffset.x * 1.4f, h * 0.44f + leftStickOffset.y * 1.4f)
            when (session.game.category) {
                GameCategory.RACING -> {
                    drawCircle(
                        brush = Brush.sweepGradient(listOf(GhostRed, GhostBlue, GhostCyan, GhostRed)),
                        center = center,
                        radius = 74f,
                        style = Stroke(width = 4f)
                    )
                }
                GameCategory.SHOOTER -> {
                    val r = 34f
                    drawCircle(color = GhostRed.copy(alpha = 0.7f), radius = r, center = center, style = Stroke(2.5f))
                    drawLine(GhostCyan, Offset(center.x - r - 14f, center.y), Offset(center.x - 10f, center.y), 2.5f)
                    drawLine(GhostCyan, Offset(center.x + 10f, center.y), Offset(center.x + r + 14f, center.y), 2.5f)
                    drawLine(GhostCyan, Offset(center.x, center.y - r - 14f), Offset(center.x, center.y - 10f), 2.5f)
                    drawLine(GhostCyan, Offset(center.x, center.y + 10f), Offset(center.x, center.y + r + 14f), 2.5f)
                }
                else -> {
                    drawCircle(
                        color = GhostCyan.copy(alpha = 0.55f),
                        radius = 48f + pulsePhase * 16f,
                        center = center,
                        style = Stroke(2f)
                    )
                }
            }
        }

        // Top Telemetry Bar & Controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = finishAndSync,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GhostVoid.copy(alpha = 0.72f))
                            .testTag("stream_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = GhostTextPrimary
                        )
                    }

                    Column {
                        Text(
                            text = session.game.title.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = GhostTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        val minStr = (elapsedSeconds / 60).toString().padStart(2, '0')
                        val secStr = (elapsedSeconds % 60).toString().padStart(2, '0')
                        Text(
                            text = "${streamQuality.label} • $minStr:$secStr • FREE CLOUD",
                            style = MaterialTheme.typography.labelSmall,
                            color = GhostCyan
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NetworkTelemetryChip(networkStatus = networkStatus)

                    IconButton(
                        onClick = { showController = !showController },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GhostVoid.copy(alpha = 0.72f))
                            .testTag("toggle_controller_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Gamepad,
                            contentDescription = stringResource(R.string.cd_toggle_controller),
                            tint = if (showController) XboxGreen else GhostTextSecondary
                        )
                    }

                    IconButton(
                        onClick = {
                            landscapeLocked = !landscapeLocked
                            activity?.requestedOrientation = if (landscapeLocked) {
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GhostVoid.copy(alpha = 0.72f))
                            .testTag("toggle_landscape_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ScreenRotation,
                            contentDescription = stringResource(R.string.cd_rotate_screen),
                            tint = if (landscapeLocked) GhostCyan else GhostTextPrimary
                        )
                    }
                }
            }

            // Center Bootloader OR Live Action Feed
            if (bootProgress < 100) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GhostSurface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, GhostDiagonalBrush),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.85f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "INITIALIZING GHOST CLOUD RIG…",
                            style = MaterialTheme.typography.titleMedium,
                            color = GhostTextPrimary
                        )
                        LinearProgressIndicator(
                            progress = { bootProgress / 100f },
                            color = GhostCyan,
                            trackColor = GhostVoid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = when {
                                bootProgress < 40 -> "Allocating Free Xbox Cloud Blade…"
                                bootProgress < 75 -> "Mounting Cloud Save & Cross-Play Profile…"
                                else -> "Locking 60 FPS Video Stream…"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = GhostTextSecondary
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GhostVoid.copy(alpha = 0.68f),
                    border = BorderStroke(1.dp, GhostBorder),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudDone,
                            contentDescription = null,
                            tint = XboxGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = buildString {
                                append(lastActionFeedback)
                                session.activeSave?.let { append(" • Slot: ${it.slotName}") }
                                session.activeLobby?.let { append(" • Squad: ${it.memberUids.size}/${it.maxPlayers}") }
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = GhostTextPrimary
                        )
                    }
                }
            }

            // Bottom Virtual Xbox Controller Overlay + Sync & Exit
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(visible = showController && bootProgress >= 100) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Analog Thumbstick + LB
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ShoulderTriggerButton("LB") {
                                actionsTriggered++
                                lastActionFeedback = "LB • Tactical Brake / Focus"
                            }
                            Box(
                                modifier = Modifier
                                    .size(112.dp)
                                    .clip(CircleShape)
                                    .background(GhostVoid.copy(alpha = 0.56f))
                                    .border(1.5.dp, GhostBlue.copy(alpha = 0.65f), CircleShape)
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragEnd = { leftStickOffset = Offset.Zero },
                                            onDragCancel = { leftStickOffset = Offset.Zero },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val next = leftStickOffset + dragAmount
                                                val dist = next.getDistance()
                                                leftStickOffset = if (dist > 34f) next * (34f / dist) else next
                                                lastActionFeedback = "LS • Steering / Movement"
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                leftStickOffset.x.roundToInt(),
                                                leftStickOffset.y.roundToInt()
                                            )
                                        }
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(GhostDiagonalBrush)
                                        .border(2.dp, GhostTextPrimary.copy(alpha = 0.8f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("LS", style = MaterialTheme.typography.labelSmall, color = GhostTextPrimary)
                                }
                            }
                        }

                        // Right Xbox A/B/X/Y Cluster + RB
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ShoulderTriggerButton("RB") {
                                actionsTriggered++
                                lastActionFeedback = "RB • Nitro Boost / Ability"
                            }
                            Box(
                                modifier = Modifier.size(126.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                XboxFaceButton(
                                    label = "Y",
                                    accent = XboxButtonY,
                                    modifier = Modifier.align(Alignment.TopCenter),
                                    onClick = {
                                        actionsTriggered++
                                        lastActionFeedback = "Y • Camera / Swap Weapon"
                                    }
                                )
                                XboxFaceButton(
                                    label = "X",
                                    accent = XboxButtonX,
                                    modifier = Modifier.align(Alignment.CenterStart),
                                    onClick = {
                                        actionsTriggered++
                                        lastActionFeedback = "X • Reload / Hack / Power-Up"
                                    }
                                )
                                XboxFaceButton(
                                    label = "B",
                                    accent = XboxButtonB,
                                    modifier = Modifier.align(Alignment.CenterEnd),
                                    onClick = {
                                        actionsTriggered++
                                        lastActionFeedback = "B • Handbrake Drift / Crouch"
                                    }
                                )
                                XboxFaceButton(
                                    label = "A",
                                    accent = XboxButtonA,
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                    onClick = {
                                        actionsTriggered++
                                        lastActionFeedback = "A • Accelerate / Sprint / Confirm"
                                    }
                                )
                            }
                        }
                    }
                }

                // Bottom Sync & Exit Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    GhostGradientButton(
                        text = stringResource(R.string.action_save_sync_exit),
                        onClick = finishAndSync,
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .testTag("stream_sync_exit_button"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.CloudDone,
                                contentDescription = null,
                                tint = GhostTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoulderTriggerButton(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = GhostVoid.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, GhostBorder),
        modifier = Modifier
            .width(72.dp)
            .height(34.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = GhostTextPrimary)
        }
    }
}

@Composable
private fun XboxFaceButton(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(GhostVoid.copy(alpha = 0.76f))
            .border(2.dp, accent, CircleShape)
            .clickable(onClick = onClick)
            .testTag("xbox_button_${label.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = accent,
            fontWeight = FontWeight.Bold
        )
    }
}
