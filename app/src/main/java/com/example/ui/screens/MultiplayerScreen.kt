package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CloudRegion
import com.example.data.GameCatalog
import com.example.data.MultiplayerLobby
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

@Composable
fun MultiplayerScreen(
    currentUserId: String,
    publicLobbiesState: UiState<List<MultiplayerLobby>>,
    myLobbiesState: UiState<List<MultiplayerLobby>>,
    onCreateLobby: (XboxGame, String, CloudRegion, List<String>, Int, String) -> Unit,
    onJoinLobby: (MultiplayerLobby) -> Unit,
    onLeaveLobby: (MultiplayerLobby) -> Unit,
    onToggleReady: (MultiplayerLobby) -> Unit,
    onLaunchLobby: (MultiplayerLobby) -> Unit,
    onCloseLobby: (MultiplayerLobby) -> Unit,
    onDeleteLobby: (MultiplayerLobby) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMySquadsOnly by remember { mutableStateOf(false) }
    var selectedGameFilter by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val crossPlayGames = remember { GameCatalog.allGames.filter { it.crossPlatform } }
    val activeState = if (showMySquadsOnly) myLobbiesState else publicLobbiesState

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GhostBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CROSS-PLATFORM SQUADS",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GhostTextPrimary
                )
                Text(
                    text = "Live Xbox, PC, PlayStation & Cloud lobbies",
                    style = MaterialTheme.typography.bodySmall,
                    color = GhostTextSecondary
                )
            }
            GhostGradientButton(
                text = stringResource(R.string.action_create_lobby),
                onClick = { showCreateDialog = true },
                modifier = Modifier.testTag("host_squad_button"),
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

        // Public vs My Squads toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = !showMySquadsOnly,
                onClick = { showMySquadsOnly = false },
                label = { Text("Public Lobbies", style = MaterialTheme.typography.labelLarge) },
                leadingIcon = {
                    Icon(Icons.Filled.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GhostBlue.copy(alpha = 0.25f),
                    selectedLabelColor = GhostTextPrimary,
                    containerColor = GhostSurface,
                    labelColor = GhostTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = !showMySquadsOnly,
                    borderColor = GhostBorder,
                    selectedBorderColor = GhostBlue
                ),
                modifier = Modifier.testTag("tab_public_lobbies")
            )
            FilterChip(
                selected = showMySquadsOnly,
                onClick = { showMySquadsOnly = true },
                label = { Text("My Squads", style = MaterialTheme.typography.labelLarge) },
                leadingIcon = {
                    Icon(Icons.Filled.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GhostRed.copy(alpha = 0.25f),
                    selectedLabelColor = GhostTextPrimary,
                    containerColor = GhostSurface,
                    labelColor = GhostTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = showMySquadsOnly,
                    borderColor = GhostBorder,
                    selectedBorderColor = GhostRed
                ),
                modifier = Modifier.testTag("tab_my_squads")
            )
        }

        Spacer(Modifier.height(8.dp))

        // Game filter strip
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedGameFilter == null,
                    onClick = { selectedGameFilter = null },
                    label = { Text("All Titles") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GhostSurfaceElevated,
                        selectedLabelColor = GhostCyan,
                        containerColor = GhostSurface,
                        labelColor = GhostTextSecondary
                    )
                )
            }
            items(crossPlayGames, key = { it.id }) { game ->
                FilterChip(
                    selected = selectedGameFilter == game.id,
                    onClick = {
                        selectedGameFilter = if (selectedGameFilter == game.id) null else game.id
                    },
                    label = { Text(game.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GhostSurfaceElevated,
                        selectedLabelColor = GhostCyan,
                        containerColor = GhostSurface,
                        labelColor = GhostTextSecondary
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        when (activeState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
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
                        text = activeState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GhostTextPrimary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            is UiState.Success -> {
                val lobbies = activeState.data.filter { lobby ->
                    selectedGameFilter == null || lobby.gameId == selectedGameFilter
                }
                if (lobbies.isEmpty()) {
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
                                imageVector = Icons.Filled.Groups,
                                contentDescription = null,
                                tint = GhostCyan,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = if (showMySquadsOnly) "You haven't joined any squads yet" else "No open cross-play squads yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = GhostTextPrimary
                            )
                            Text(
                                text = "Host a free cross-platform lobby for Forza Horizon 6, Call of Duty: Warzone, GTA V, Blur, Watch Dogs, or Need for Speed.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GhostTextSecondary
                            )
                            GhostGradientButton(
                                text = stringResource(R.string.action_create_lobby),
                                onClick = { showCreateDialog = true },
                                modifier = Modifier.testTag("empty_host_squad_button")
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(lobbies, key = { it.id }) { lobby ->
                            LobbyCard(
                                lobby = lobby,
                                currentUserId = currentUserId,
                                onJoin = { onJoinLobby(lobby) },
                                onLeave = { onLeaveLobby(lobby) },
                                onToggleReady = { onToggleReady(lobby) },
                                onLaunch = { onLaunchLobby(lobby) },
                                onClose = { onCloseLobby(lobby) },
                                onDelete = { onDeleteLobby(lobby) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateLobbyDialog(
            games = crossPlayGames,
            onDismiss = { showCreateDialog = false },
            onConfirm = { game, title, region, platforms, maxPlayers, visibility ->
                showCreateDialog = false
                onCreateLobby(game, title, region, platforms, maxPlayers, visibility)
            }
        )
    }
}

@Composable
private fun LobbyCard(
    lobby: MultiplayerLobby,
    currentUserId: String,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onToggleReady: () -> Unit,
    onLaunch: () -> Unit,
    onClose: () -> Unit,
    onDelete: () -> Unit
) {
    val isHost = lobby.hostUid == currentUserId
    val isMember = currentUserId in lobby.memberUids
    val isReady = currentUserId in lobby.readyUids
    val isClosed = lobby.status == "CLOSED"
    val fillRatio = (lobby.memberUids.size.toFloat() / lobby.maxPlayers.coerceAtLeast(1L).toFloat())
        .coerceIn(0f, 1f)

    val statusColor = when (lobby.status) {
        "OPEN" -> XboxGreen
        "IN_SESSION" -> GhostCyan
        else -> GhostRed
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GhostSurface,
        border = BorderStroke(
            1.dp,
            if (isMember) GhostBlue.copy(alpha = 0.7f) else GhostBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lobby_card_${lobby.id}")
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
                        color = statusColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = lobby.status.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = lobby.gameTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = GhostCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isHost) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("delete_lobby_${lobby.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete squad",
                            tint = GhostRed
                        )
                    }
                }
            }

            Text(
                text = lobby.title,
                style = MaterialTheme.typography.titleLarge,
                color = GhostTextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Host: ${lobby.hostGamertag} • ${CloudRegion.fromCode(lobby.region).label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GhostTextSecondary
                )
                Text(
                    text = "${lobby.memberUids.size}/${lobby.maxPlayers} PLAYERS",
                    style = MaterialTheme.typography.labelMedium,
                    color = GhostTextPrimary
                )
            }

            LinearProgressIndicator(
                progress = { fillRatio },
                color = if (isMember) GhostCyan else GhostBlue,
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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    lobby.crossPlayPlatforms.forEach { PlatformPill(label = it) }
                }
                Text(
                    text = "${lobby.readyUids.size} Ready",
                    style = MaterialTheme.typography.labelSmall,
                    color = XboxGreen
                )
            }

            if (!isClosed) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isMember) {
                        GhostGradientButton(
                            text = stringResource(R.string.action_join_lobby),
                            enabled = lobby.status == "OPEN" && lobby.memberUids.size < lobby.maxPlayers,
                            onClick = onJoin,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("join_lobby_${lobby.id}")
                        )
                    } else {
                        Button(
                            onClick = onToggleReady,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isReady) XboxGreenDark else GhostSurfaceElevated,
                                contentColor = if (isReady) XboxGreen else GhostTextPrimary
                            ),
                            border = BorderStroke(1.dp, if (isReady) XboxGreen else GhostBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("ready_lobby_${lobby.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isReady) {
                                    stringResource(R.string.action_ready_done)
                                } else {
                                    stringResource(R.string.action_ready_up)
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        GhostGradientButton(
                            text = stringResource(R.string.action_launch_squad),
                            onClick = onLaunch,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("launch_lobby_${lobby.id}"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = GhostTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        OutlinedButton(
                            onClick = if (isHost) onClose else onLeave,
                            border = BorderStroke(1.dp, GhostRed.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("leave_or_close_lobby_${lobby.id}")
                        ) {
                            Text(
                                text = if (isHost) {
                                    stringResource(R.string.action_close_lobby)
                                } else {
                                    stringResource(R.string.action_leave_lobby)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = GhostRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateLobbyDialog(
    games: List<XboxGame>,
    onDismiss: () -> Unit,
    onConfirm: (XboxGame, String, CloudRegion, List<String>, Int, String) -> Unit
) {
    var selectedGame by remember { mutableStateOf(games.first()) }
    var title by remember { mutableStateOf("${selectedGame.title} Cross-Play Squad") }
    var region by remember { mutableStateOf(CloudRegion.NA_EAST) }
    var maxPlayers by remember { mutableIntStateOf(selectedGame.maxSquadSize.coerceIn(2, 8)) }
    var selectedPlatforms by remember { mutableStateOf(setOf("XBOX", "PC", "CLOUD")) }
    var visibility by remember { mutableStateOf("PUBLIC") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GhostSurface,
        title = {
            Text(
                text = "HOST CROSS-PLATFORM SQUAD",
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
                Text("Select Xbox Game", style = MaterialTheme.typography.labelLarge, color = GhostCyan)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(games, key = { it.id }) { game ->
                        FilterChip(
                            selected = selectedGame.id == game.id,
                            onClick = {
                                selectedGame = game
                                title = "${game.title} Cross-Play Squad".take(60)
                                maxPlayers = game.maxSquadSize.coerceIn(2, 16)
                            },
                            label = { Text(game.title) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it.take(60)
                        validationError = null
                    },
                    label = { Text("Squad Title (3–60 chars)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GhostBlue,
                        unfocusedBorderColor = GhostBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lobby_title_input")
                )

                Text("Cross-Play Platforms", style = MaterialTheme.typography.labelLarge, color = GhostCyan)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("XBOX", "PC", "PS", "CLOUD").forEach { plat ->
                        val active = plat in selectedPlatforms
                        FilterChip(
                            selected = active,
                            onClick = {
                                val next = if (active && selectedPlatforms.size > 1) {
                                    selectedPlatforms - plat
                                } else {
                                    selectedPlatforms + plat
                                }
                                selectedPlatforms = next
                            },
                            label = { Text(plat) }
                        )
                    }
                }

                Text("Server Region", style = MaterialTheme.typography.labelLarge, color = GhostCyan)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CloudRegion.entries) { reg ->
                        FilterChip(
                            selected = region == reg,
                            onClick = { region = reg },
                            label = { Text(reg.label) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Squad Size: $maxPlayers", style = MaterialTheme.typography.titleSmall, color = GhostTextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 4, 8, 12).forEach { count ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (maxPlayers == count) GhostBlue else GhostSurfaceElevated,
                                modifier = Modifier
                                    .clickable { maxPlayers = count }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GhostTextPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PUBLIC", "PRIVATE").forEach { vis ->
                        FilterChip(
                            selected = visibility == vis,
                            onClick = { visibility = vis },
                            label = { Text(vis) }
                        )
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostRed
                    )
                }
            }
        },
        confirmButton = {
            GhostGradientButton(
                text = "CREATE LIVE",
                onClick = {
                    val trimmed = title.trim()
                    if (trimmed.length < 3) {
                        validationError = "Squad title must be at least 3 characters."
                    } else {
                        onConfirm(
                            selectedGame,
                            trimmed,
                            region,
                            selectedPlatforms.toList(),
                            maxPlayers,
                            visibility
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_create_lobby_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GhostTextSecondary)
            }
        }
    )
}
