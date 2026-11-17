package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CloudSave
import com.example.data.GameCatalog
import com.example.data.GameCategory
import com.example.data.LibraryEntry
import com.example.data.NetworkStatus
import com.example.data.XboxGame
import com.example.ui.components.FeatureCapabilityRow
import com.example.ui.components.FreePassBadge
import com.example.ui.components.GameCoverArtwork
import com.example.ui.components.GhostBrandMark
import com.example.ui.components.GhostGradientButton
import com.example.ui.components.NetworkTelemetryChip
import com.example.ui.components.PlatformPill
import com.example.ui.components.PremiumFreeBadge
import com.example.ui.theme.GhostBackground
import com.example.ui.theme.GhostBlue
import com.example.ui.theme.GhostBorder
import com.example.ui.theme.GhostCyan
import com.example.ui.theme.GhostDiagonalBrush
import com.example.ui.theme.GhostRed
import com.example.ui.theme.GhostSurface
import com.example.ui.theme.GhostSurfaceElevated
import com.example.ui.theme.GhostTextPrimary
import com.example.ui.theme.GhostTextSecondary
import com.example.ui.theme.GhostVoid
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxGreenDark

@Composable
fun HomeScreen(
    selectedCategory: GameCategory,
    searchQuery: String,
    libraryEntries: List<LibraryEntry>,
    cloudSaves: List<CloudSave>,
    networkStatus: NetworkStatus,
    onSelectCategory: (GameCategory) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onGameClick: (XboxGame) -> Unit,
    onLaunchGame: (XboxGame) -> Unit,
    onTogglePin: (XboxGame, Boolean) -> Unit,
    onNavigateToCloudSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchVisible by remember { mutableStateOf(false) }

    val pinnedIds = remember(libraryEntries) {
        libraryEntries.filter { it.pinned }.map { it.gameId }.toSet()
    }
    val saveCountsByGame = remember(cloudSaves) {
        cloudSaves.groupingBy { it.gameId }.eachCount()
    }

    val filteredGames = remember(selectedCategory, searchQuery, pinnedIds) {
        GameCatalog.allGames.filter { game ->
            val matchesCategory = when (selectedCategory) {
                GameCategory.ALL -> true
                GameCategory.PREMIUM -> game.premiumShowcase
                GameCategory.PINNED -> game.id in pinnedIds
                else -> game.category == selectedCategory
            }
            val matchesQuery = searchQuery.isBlank() ||
                game.title.contains(searchQuery, ignoreCase = true) ||
                game.genre.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(GhostBackground)
    ) {
        val columns = when {
            maxWidth >= 900.dp -> 3
            maxWidth >= 560.dp -> 2
            else -> 1
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top Command Bar
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GhostBrandMark()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NetworkTelemetryChip(networkStatus = networkStatus)
                        IconButton(
                            onClick = {
                                searchVisible = !searchVisible
                                if (!searchVisible) onSearchQueryChange("")
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("search_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (searchVisible) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = stringResource(R.string.cd_search),
                                tint = GhostTextPrimary
                            )
                        }
                    }
                }
            }

            // Optional Search Bar
            if (searchVisible) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search Watch Dogs, Forza Horizon 6, GTA V, Blur, Warzone…") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GhostBlue,
                            unfocusedBorderColor = GhostBorder,
                            focusedContainerColor = GhostSurface,
                            unfocusedContainerColor = GhostSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("game_search_input")
                    )
                }
            }

            // 2. Free Cloud Access Banner
            item(span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = XboxGreenDark.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, XboxGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = XboxGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.free_access_banner),
                                style = MaterialTheme.typography.bodySmall,
                                color = GhostTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        FreePassBadge()
                    }
                }
            }

            // 3. Featured Hero Showcase: Forza Horizon 6 (Premium Free)
            if (searchQuery.isBlank() && (selectedCategory == GameCategory.ALL || selectedCategory == GameCategory.PREMIUM || selectedCategory == GameCategory.RACING)) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FeaturedHeroCard(
                        game = GameCatalog.featuredHero,
                        onPlayNow = { onLaunchGame(GameCatalog.featuredHero) },
                        onOpenDetails = { onGameClick(GameCatalog.featuredHero) },
                        onSyncClick = onNavigateToCloudSync
                    )
                }
            }

            // 4. Category Filter Chips
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(GameCategory.entries) { category ->
                        val selected = category == selectedCategory
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectCategory(category) },
                            label = {
                                Text(
                                    text = category.label,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GhostBlue.copy(alpha = 0.24f),
                                selectedLabelColor = GhostTextPrimary,
                                containerColor = GhostSurface,
                                labelColor = GhostTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = GhostBorder,
                                selectedBorderColor = GhostBlue
                            ),
                            modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                        )
                    }
                }
            }

            // 5. Section Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory == GameCategory.ALL) "XBOX CLOUD CATALOG" else selectedCategory.label.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = GhostTextPrimary
                    )
                    Text(
                        text = "${filteredGames.size} TITLES",
                        style = MaterialTheme.typography.labelMedium,
                        color = GhostCyan
                    )
                }
            }

            // 6. Empty State for Pinned / Search
            if (filteredGames.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GhostSurface,
                        border = BorderStroke(1.dp, GhostBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BookmarkBorder,
                                contentDescription = null,
                                tint = GhostCyan,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = if (selectedCategory == GameCategory.PINNED) {
                                    "No pinned Xbox games yet"
                                } else {
                                    "No games matched \"$searchQuery\""
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = GhostTextPrimary
                            )
                            Text(
                                text = "Tap the bookmark icon on any Xbox title to pin it to your synced library.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GhostTextSecondary
                            )
                        }
                    }
                }
            } else {
                // 7. Game Cards Grid
                items(filteredGames, key = { it.id }) { game ->
                    XboxGameCard(
                        game = game,
                        pinned = game.id in pinnedIds,
                        syncedSavesCount = saveCountsByGame[game.id] ?: 0,
                        onCardClick = { onGameClick(game) },
                        onPlayClick = { onLaunchGame(game) },
                        onTogglePin = { onTogglePin(game, game.id in pinnedIds) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedHeroCard(
    game: XboxGame,
    onPlayNow: () -> Unit,
    onOpenDetails: () -> Unit,
    onSyncClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = GhostSurface,
        border = BorderStroke(1.5.dp, GhostDiagonalBrush),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clickable(onClick = onOpenDetails)
            .testTag("hero_forza_card")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GameCoverArtwork(game = game, modifier = Modifier.fillMaxSize())

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                GhostVoid.copy(alpha = 0.86f),
                                GhostVoid.copy(alpha = 0.48f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumFreeBadge()
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GhostVoid.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, GhostCyan.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = game.resolutionSpec,
                            style = MaterialTheme.typography.labelSmall,
                            color = GhostCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = game.title.uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = GhostTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${game.genre} • Cross-Platform Convoys • Cloud Sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GhostGradientButton(
                            text = stringResource(R.string.action_play_free),
                            onClick = onPlayNow,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("hero_play_button"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = GhostTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GhostSurfaceElevated.copy(alpha = 0.88f),
                            border = BorderStroke(1.dp, GhostBorder),
                            modifier = Modifier
                                .height(48.dp)
                                .clickable(onClick = onSyncClick)
                                .testTag("hero_sync_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
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
                                    text = stringResource(R.string.action_quick_sync),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GhostTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun XboxGameCard(
    game: XboxGame,
    pinned: Boolean,
    syncedSavesCount: Int,
    onCardClick: () -> Unit,
    onPlayClick: () -> Unit,
    onTogglePin: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = GhostSurface,
        border = BorderStroke(
            width = 1.dp,
            color = if (game.premiumShowcase) GhostRed.copy(alpha = 0.55f) else GhostBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("game_card_${game.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(162.dp)
            ) {
                GameCoverArtwork(game = game, modifier = Modifier.fillMaxSize())

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (game.premiumShowcase) {
                        PremiumFreeBadge()
                    } else {
                        FreePassBadge(label = "FREE CLOUD")
                    }

                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GhostVoid.copy(alpha = 0.62f))
                            .testTag("pin_button_${game.id}")
                    ) {
                        Icon(
                            imageVector = if (pinned) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = stringResource(R.string.cd_pin_game),
                            tint = if (pinned) GhostCyan else GhostTextPrimary
                        )
                    }
                }

                // Bottom-left resolution tag
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    game.supportedPlatforms.take(3).forEach { platform ->
                        PlatformPill(label = platform)
                    }
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = game.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = GhostTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${game.genre} • ${game.resolutionSpec}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GhostTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GhostSurfaceElevated,
                        border = BorderStroke(1.dp, GhostBorder)
                    ) {
                        Text(
                            text = "★ ${game.rating}",
                            style = MaterialTheme.typography.labelMedium,
                            color = GhostCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                FeatureCapabilityRow(
                    crossPlatform = game.crossPlatform,
                    cloudSync = game.cloudSync
                )

                if (syncedSavesCount > 0) {
                    Text(
                        text = "$syncedSavesCount Cloud Save${if (syncedSavesCount > 1) "s" else ""} synced",
                        style = MaterialTheme.typography.labelSmall,
                        color = XboxGreen
                    )
                }

                GhostGradientButton(
                    text = stringResource(R.string.action_play_free),
                    onClick = onPlayClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("play_button_${game.id}"),
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
