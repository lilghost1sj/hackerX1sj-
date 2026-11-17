package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.NetworkStatus
import com.example.data.VisualStyle
import com.example.data.XboxGame
import com.example.ui.theme.GhostBackground
import com.example.ui.theme.GhostBlue
import com.example.ui.theme.GhostBorder
import com.example.ui.theme.GhostCyan
import com.example.ui.theme.GhostDiagonalBrush
import com.example.ui.theme.GhostHorizontalBrush
import com.example.ui.theme.GhostRed
import com.example.ui.theme.GhostSurfaceElevated
import com.example.ui.theme.GhostTextPrimary
import com.example.ui.theme.GhostTextSecondary
import com.example.ui.theme.GhostVoid
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.PremiumGoldContainer
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxGreenDark

@Composable
fun GhostGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(
                if (enabled) GhostDiagonalBrush
                else Brush.horizontalGradient(listOf(GhostBorder, GhostSurfaceElevated))
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = GhostTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GhostBrandMark(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GhostDiagonalBrush)
                .padding(2.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GhostVoid),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.SportsEsports,
                contentDescription = stringResource(R.string.app_name),
                tint = GhostTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = stringResource(R.string.brand_tag),
                style = MaterialTheme.typography.titleMedium,
                color = GhostTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.brand_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = GhostCyan
            )
        }
    }
}

@Composable
fun FreePassBadge(
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.free_pass_badge)
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = XboxGreenDark,
        border = BorderStroke(1.dp, XboxGreen.copy(alpha = 0.65f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = XboxGreen,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PremiumFreeBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = PremiumGoldContainer,
        border = BorderStroke(1.dp, PremiumGold.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = PremiumGold,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "PREMIUM • FREE",
                style = MaterialTheme.typography.labelSmall,
                color = PremiumGold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NetworkTelemetryChip(
    networkStatus: NetworkStatus,
    modifier: Modifier = Modifier
) {
    val accent = if (networkStatus.connected) GhostCyan else GhostRed
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = GhostSurfaceElevated.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (networkStatus.connected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                contentDescription = stringResource(R.string.cd_network_status),
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "${networkStatus.transportLabel} • ${networkStatus.speedTier}",
                style = MaterialTheme.typography.labelSmall,
                color = GhostTextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun PlatformPill(label: String, modifier: Modifier = Modifier) {
    val accent = when (label.uppercase()) {
        "XBOX" -> XboxGreen
        "CLOUD" -> GhostCyan
        "PC" -> GhostBlue
        "PS" -> GhostRed
        else -> GhostTextSecondary
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun FeatureCapabilityRow(
    crossPlatform: Boolean,
    cloudSync: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (crossPlatform) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Public,
                    contentDescription = null,
                    tint = GhostCyan,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Cross-Play",
                    style = MaterialTheme.typography.labelSmall,
                    color = GhostTextSecondary
                )
            }
        }
        if (cloudSync) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudDone,
                    contentDescription = null,
                    tint = XboxGreen,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Cloud Sync",
                    style = MaterialTheme.typography.labelSmall,
                    color = GhostTextSecondary
                )
            }
        }
    }
}

@Composable
fun GameCoverArtwork(
    game: XboxGame,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.background(GhostVoid)) {
        if (game.coverArtRes != null) {
            Image(
                painter = painterResource(id = game.coverArtRes),
                contentDescription = "${game.title} ${stringResource(R.string.cd_game_artwork)}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            ProceduralGameArtwork(
                style = game.visualStyle,
                title = game.title,
                modifier = Modifier.fillMaxSize()
            )
        }
        // Subtle atmospheric vignette for crisp text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GhostVoid.copy(alpha = 0.28f),
                            GhostVoid.copy(alpha = 0.88f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun ProceduralGameArtwork(
    style: VisualStyle,
    title: String,
    modifier: Modifier = Modifier
) {
    val primaryAccent = when (style) {
        VisualStyle.BLUR_NEON_POWERUP -> Color(0xFFFF1E78)
        VisualStyle.NFS_UNDERGROUND -> Color(0xFFFF4500)
        VisualStyle.HALO_PLASMA -> Color(0xFF00E5FF)
        VisualStyle.CYBERPUNK_NIGHT -> Color(0xFFFFD600)
        else -> GhostRed
    }
    val secondaryAccent = when (style) {
        VisualStyle.BLUR_NEON_POWERUP -> Color(0xFF00C2FF)
        VisualStyle.NFS_UNDERGROUND -> Color(0xFF0088FF)
        VisualStyle.HALO_PLASMA -> Color(0xFF19C358)
        VisualStyle.CYBERPUNK_NIGHT -> Color(0xFFFF1E3C)
        else -> GhostBlue
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Rich diagonal backdrop
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        GhostVoid,
                        primaryAccent.copy(alpha = 0.30f),
                        secondaryAccent.copy(alpha = 0.32f),
                        GhostBackground
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
            )

            // Perspective horizon grid
            val horizonY = h * 0.52f
            for (i in 1..6) {
                val y = horizonY + (h - horizonY) * (i / 6f) * (i / 6f)
                drawLine(
                    color = secondaryAccent.copy(alpha = 0.28f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.6f
                )
            }
            for (i in -4..4) {
                val bottomX = w * 0.5f + i * (w * 0.24f)
                drawLine(
                    color = primaryAccent.copy(alpha = 0.22f),
                    start = Offset(w * 0.5f, horizonY),
                    end = Offset(bottomX, h),
                    strokeWidth = 1.6f
                )
            }

            // Signature speed streaks or orbital geometry
            when (style) {
                VisualStyle.BLUR_NEON_POWERUP, VisualStyle.NFS_UNDERGROUND -> {
                    for (i in 0..5) {
                        val yStart = h * (0.20f + i * 0.11f)
                        val streakPath = Path().apply {
                            moveTo(0f, yStart + h * 0.12f)
                            quadraticTo(w * 0.45f, yStart - h * 0.06f, w, yStart)
                        }
                        drawPath(
                            path = streakPath,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    if (i % 2 == 0) primaryAccent else secondaryAccent,
                                    Color.White.copy(alpha = 0.85f)
                                )
                            ),
                            style = Stroke(width = if (i % 2 == 0) 4.5f else 2.5f)
                        )
                    }
                    // Speed glow orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryAccent.copy(alpha = 0.45f), Color.Transparent),
                            center = Offset(w * 0.72f, h * 0.36f),
                            radius = w * 0.42f
                        ),
                        center = Offset(w * 0.72f, h * 0.36f),
                        radius = w * 0.42f
                    )
                }

                VisualStyle.HALO_PLASMA -> {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(primaryAccent, secondaryAccent, Color.Transparent, primaryAccent)
                        ),
                        center = Offset(w * 0.5f, h * 0.42f),
                        radius = minOf(w, h) * 0.34f,
                        style = Stroke(width = 8f)
                    )
                    drawCircle(
                        color = primaryAccent.copy(alpha = 0.24f),
                        center = Offset(w * 0.5f, h * 0.42f),
                        radius = minOf(w, h) * 0.22f
                    )
                }

                else -> {
                    // Cyberpunk neon skyscrapers silhouette
                    val widths = listOf(0.12f, 0.16f, 0.11f, 0.19f, 0.14f, 0.15f)
                    val heights = listOf(0.35f, 0.58f, 0.42f, 0.66f, 0.48f, 0.52f)
                    var xCursor = w * 0.05f
                    widths.forEachIndexed { idx, wf ->
                        val bw = w * wf
                        val bh = h * heights[idx]
                        drawRect(
                            color = GhostVoid.copy(alpha = 0.8f),
                            topLeft = Offset(xCursor, h - bh),
                            size = androidx.compose.ui.geometry.Size(bw * 0.85f, bh)
                        )
                        drawLine(
                            color = if (idx % 2 == 0) primaryAccent else secondaryAccent,
                            start = Offset(xCursor, h - bh),
                            end = Offset(xCursor + bw * 0.85f, h - bh),
                            strokeWidth = 3f
                        )
                        xCursor += bw
                    }
                }
            }
        }

        // Stylized emblem watermark in upper right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(GhostVoid.copy(alpha = 0.65f))
                .border(1.dp, GhostHorizontalBrush, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.take(1).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = GhostTextPrimary
            )
        }
    }
}
