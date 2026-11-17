package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CloudSave
import com.example.data.GameCatalog
import com.example.data.GamingPlatform
import com.example.data.UiState
import com.example.data.XboxGame
import com.example.ui.components.GhostGradientButton
import com.example.ui.components.PlatformPill
import com.example.ui.theme.GhostBackground
import com.example.ui.theme.GhostBlue
import com.example.ui.theme.GhostBorder
import com.example.ui.theme.GhostCyan
import com.example.ui.theme.GhostRed
import com.example.ui.theme.GhostSurface
import com.example.ui.theme.GhostSurfaceElevated
import com.example.ui.theme.GhostTextPrimary
import com.example.ui.theme.GhostTextSecondary
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxGreenDark
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CloudSyncScreen(
    cloudSavesState: UiState<List<CloudSave>>,
    onCreateSave: (XboxGame, String, String, Int, Int, GamingPlatform) -> Unit,
    onQuickSync: (CloudSave) -> Unit,
    onResumeSave: (XboxGame, CloudSave) -> Unit,
    onDeleteSave: (CloudSave) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var presetGameForDialog by remember { mutableStateOf(GameCatalog.findById("need_for_speed") ?: GameCatalog.featuredHero) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GhostBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CLOUD SAVE SYNC",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GhostTextPrimary
                )
                Text(
                    text = "Cross-device sync for Need for Speed, Forza & all Xbox titles",
                    style = MaterialTheme.typography.bodySmall,
                    color = GhostTextSecondary
                )
            }
            GhostGradientButton(
                text = "NEW SAVE",
                onClick = {
                    presetGameForDialog = GameCatalog.findById("need_for_speed") ?: GameCatalog.featuredHero
                    showCreateDialog = true
                },
                modifier = Modifier.testTag("create_cloud_save_button"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = GhostTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        Spacer(Modifier.height(14.dp))

        // Quick-Sync Need for Speed Spotlight Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GhostSurfaceElevated,
            border = BorderStroke(1.dp, XboxGreen.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudSync,
                        contentDescription = null,
                        tint = XboxGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "NEED FOR SPEED • AUTO CLOUD GARAGE",
                            style = MaterialTheme.typography.titleSmall,
                            color = GhostTextPrimary
                        )
                        Text(
                            text = "Sync Rep Level, custom wraps & progress between Xbox, PC & Android Cloud.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GhostTextSecondary
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        presetGameForDialog = GameCatalog.findById("need_for_speed") ?: GameCatalog.featuredHero
                        showCreateDialog = true
                    },
                    border = BorderStroke(1.dp, XboxGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("nfs_quick_sync_button")
                ) {
                    Text("SYNC NFS", style = MaterialTheme.typography.labelLarge, color = XboxGreen)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        when (cloudSavesState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GhostBlue)
                }
            }

            is UiState.Error -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GhostRed.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, GhostRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = cloudSavesState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GhostTextPrimary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            is UiState.Success -> {
                val saves = cloudSavesState.data
                if (saves.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = GhostSurface,
                        border = BorderStroke(1.dp, GhostBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudDone,
                                contentDescription = null,
                                tint = XboxGreen,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "No cloud saves synced yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = GhostTextPrimary
                            )
                            Text(
                                text = "Create your first Need for Speed, Forza Horizon 6, GTA V, or Watch Dogs save slot to sync instantaneously across all devices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GhostTextSecondary
                            )
                            GhostGradientButton(
                                text = stringResource(R.string.action_new_save),
                                onClick = { showCreateDialog = true },
                                modifier = Modifier.testTag("empty_create_save_button")
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(saves, key = { it.id }) { save ->
                            CloudSaveCard(
                                save = save,
                                onQuickSync = { onQuickSync(save) },
                                onResume = {
                                    val game = GameCatalog.findById(save.gameId) ?: GameCatalog.featuredHero
                                    onResumeSave(game, save)
                                },
                                onDelete = { onDeleteSave(save) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCloudSaveDialog(
            initialGame = presetGameForDialog,
            onDismiss = { showCreateDialog = false },
            onConfirm = { game, slot, chapter, progress, minutes, platform ->
                showCreateDialog = false
                onCreateSave(game, slot, chapter, progress, minutes, platform)
            }
        )
    }
}

@Composable
private fun CloudSaveCard(
    save: CloudSave,
    onQuickSync: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedTime = remember(save.updatedAt, save.createdAt) {
        val ts = save.updatedAt ?: save.createdAt ?: Timestamp.now()
        SimpleDateFormat("MMM d • HH:mm", Locale.getDefault()).format(ts.toDate())
    }
    val platform = GamingPlatform.fromCode(save.platformOrigin)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GhostSurface,
        border = BorderStroke(1.dp, GhostBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cloud_save_card_${save.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = XboxGreenDark,
                        border = BorderStroke(1.dp, XboxGreen.copy(alpha = 0.65f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudDone,
                                contentDescription = null,
                                tint = XboxGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = save.syncStatus,
                                style = MaterialTheme.typography.labelSmall,
                                color = XboxGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = save.gameTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = GhostCyan
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("delete_save_${save.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete cloud save",
                        tint = GhostRed
                    )
                }
            }

            Text(
                text = save.slotName,
                style = MaterialTheme.typography.titleLarge,
                color = GhostTextPrimary
            )
            Text(
                text = save.chapterOrRank,
                style = MaterialTheme.typography.bodySmall,
                color = GhostTextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress ${save.progressPercent}% • ${save.playTimeMinutes}m played",
                    style = MaterialTheme.typography.labelMedium,
                    color = GhostTextPrimary
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = GhostTextSecondary
                )
            }

            LinearProgressIndicator(
                progress = { (save.progressPercent / 100f).coerceIn(0f, 1f) },
                color = XboxGreen,
                trackColor = GhostSurfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlatformPill(label = platform.label)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onQuickSync,
                        border = BorderStroke(1.dp, GhostCyan.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("sync_save_${save.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = null,
                            tint = GhostCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("SYNC", style = MaterialTheme.typography.labelLarge, color = GhostCyan)
                    }
                    GhostGradientButton(
                        text = "RESUME",
                        onClick = onResume,
                        modifier = Modifier.testTag("resume_save_${save.id}"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
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
private fun CreateCloudSaveDialog(
    initialGame: XboxGame,
    onDismiss: () -> Unit,
    onConfirm: (XboxGame, String, String, Int, Int, GamingPlatform) -> Unit
) {
    var selectedGame by remember { mutableStateOf(initialGame) }
    var slotName by remember { mutableStateOf(initialGame.defaultSavePreset) }
    var chapterOrRank by remember { mutableStateOf(initialGame.defaultChapterPreset) }
    var progressSlider by remember { mutableFloatStateOf(35f) }
    var platform by remember { mutableStateOf(GamingPlatform.XBOX_CLOUD) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GhostSurface,
        title = {
            Text(
                text = "CREATE CLOUD SAVE SLOT",
                style = MaterialTheme.typography.titleLarge,
                color = GhostTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Game Title", style = MaterialTheme.typography.labelLarge, color = GhostCyan)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GameCatalog.allGames, key = { it.id }) { game ->
                        FilterChip(
                            selected = selectedGame.id == game.id,
                            onClick = {
                                selectedGame = game
                                slotName = game.defaultSavePreset
                                chapterOrRank = game.defaultChapterPreset
                            },
                            label = { Text(game.title) }
                        )
                    }
                }

                OutlinedTextField(
                    value = slotName,
                    onValueChange = {
                        slotName = it.take(60)
                        errorText = null
                    },
                    label = { Text("Save Slot Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GhostBlue,
                        unfocusedBorderColor = GhostBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_slot_input")
                )

                OutlinedTextField(
                    value = chapterOrRank,
                    onValueChange = {
                        chapterOrRank = it.take(100)
                        errorText = null
                    },
                    label = { Text("Chapter / Garage / Rank Summary") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GhostBlue,
                        unfocusedBorderColor = GhostBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_chapter_input")
                )

                Column {
                    Text(
                        text = "Synced Progress: ${progressSlider.toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = GhostTextPrimary
                    )
                    Slider(
                        value = progressSlider,
                        onValueChange = { progressSlider = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = XboxGreen,
                            activeTrackColor = XboxGreen
                        )
                    )
                }

                Text("Origin Platform", style = MaterialTheme.typography.labelLarge, color = GhostCyan)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GamingPlatform.entries) { p ->
                        FilterChip(
                            selected = platform == p,
                            onClick = { platform = p },
                            label = { Text(p.label) }
                        )
                    }
                }

                if (errorText != null) {
                    Text(text = errorText!!, style = MaterialTheme.typography.bodySmall, color = GhostRed)
                }
            }
        },
        confirmButton = {
            GhostGradientButton(
                text = "SYNC TO CLOUD",
                onClick = {
                    if (slotName.trim().isEmpty() || chapterOrRank.trim().isEmpty()) {
                        errorText = "Please fill in both slot name and progress summary."
                    } else {
                        onConfirm(
                            selectedGame,
                            slotName.trim(),
                            chapterOrRank.trim(),
                            progressSlider.toInt(),
                            (progressSlider.toInt() * 2).coerceAtLeast(10),
                            platform
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_create_save_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GhostTextSecondary)
            }
        }
    )
}
