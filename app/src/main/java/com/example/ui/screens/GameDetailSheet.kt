package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CloudSave
import com.example.data.MultiplayerLobby
import com.example.data.XboxGame
import com.example.ui.components.FeatureCapabilityRow
import com.example.ui.components.FreePassBadge
import com.example.ui.components.GameCoverArtwork
import com.example.ui.components.GhostGradientButton
import com.example.ui.components.PlatformPill
import com.example.ui.components.PremiumFreeBadge
import com.example.ui.theme.GhostBorder
import com.example.ui.theme.GhostCyan
import com.example.ui.theme.GhostSurface
import com.example.ui.theme.GhostSurfaceElevated
import com.example.ui.theme.GhostTextPrimary
import com.example.ui.theme.GhostTextSecondary
import com.example.ui.theme.XboxGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailSheet(
    game: XboxGame,
    pinned: Boolean,
    gameSaves: List<CloudSave>,
    gameLobbies: List<MultiplayerLobby>,
    onDismiss: () -> Unit,
    onLaunchGame: (XboxGame, CloudSave?) -> Unit,
    onTogglePin: () -> Unit,
    onCreateQuickSave: () -> Unit,
    onOpenMultiplayer: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GhostSurface,
        contentColor = GhostTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                GameCoverArtwork(game = game, modifier = Modifier.matchParentSize())
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (game.premiumShowcase) PremiumFreeBadge() else FreePassBadge(label = "FREE CLOUD")
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = GhostTextPrimary
                    )
                    Text(
                        text = "${game.subtitle} • ${game.resolutionSpec}",
                        style = MaterialTheme.typography.labelMedium,
                        color = GhostCyan
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    game.supportedPlatforms.forEach { PlatformPill(label = it) }
                }
                FeatureCapabilityRow(
                    crossPlatform = game.crossPlatform,
                    cloudSync = game.cloudSync
                )
            }

            Text(
                text = game.description,
                style = MaterialTheme.typography.bodyMedium,
                color = GhostTextSecondary
            )

            // Primary Play & Library Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GhostGradientButton(
                    text = stringResource(R.string.action_play_free),
                    onClick = { onLaunchGame(game, gameSaves.firstOrNull()) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("detail_play_button"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = GhostTextPrimary
                        )
                    }
                )
                OutlinedButton(
                    onClick = onTogglePin,
                    border = BorderStroke(1.dp, GhostBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("detail_pin_button")
                ) {
                    Icon(
                        imageVector = if (pinned) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = stringResource(R.string.cd_pin_game),
                        tint = if (pinned) GhostCyan else GhostTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Cloud Saves Section
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = GhostSurfaceElevated,
                border = BorderStroke(1.dp, GhostBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudDone,
                                contentDescription = null,
                                tint = XboxGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "CLOUD SAVES (${gameSaves.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = GhostTextPrimary
                            )
                        }
                        Text(
                            text = "+ CREATE SAVE",
                            style = MaterialTheme.typography.labelLarge,
                            color = GhostCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onCreateQuickSave)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("detail_create_save_button")
                        )
                    }

                    if (gameSaves.isEmpty()) {
                        Text(
                            text = "No cloud saves yet for ${game.title}. Create a slot to sync your progress across Xbox, PC, and Android.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GhostTextSecondary
                        )
                    } else {
                        gameSaves.take(3).forEach { save ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GhostSurface)
                                    .clickable { onLaunchGame(game, save) }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = save.slotName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = GhostTextPrimary
                                    )
                                    Text(
                                        text = "${save.chapterOrRank} • ${save.progressPercent}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GhostTextSecondary
                                    )
                                }
                                Text(
                                    text = "RESUME",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = XboxGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Cross-Platform Lobbies summary
            if (game.crossPlatform) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GhostSurfaceElevated,
                    border = BorderStroke(1.dp, GhostBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenMultiplayer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Groups,
                                contentDescription = null,
                                tint = GhostCyan
                            )
                            Column {
                                Text(
                                    text = "CROSS-PLATFORM SQUADS",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = GhostTextPrimary
                                )
                                Text(
                                    text = "${gameLobbies.size} active lobby${if (gameLobbies.size == 1) "" else "ies"} • Up to ${game.maxSquadSize} players",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GhostTextSecondary
                                )
                            }
                        }
                        Text(
                            text = "OPEN",
                            style = MaterialTheme.typography.labelLarge,
                            color = GhostCyan
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}
