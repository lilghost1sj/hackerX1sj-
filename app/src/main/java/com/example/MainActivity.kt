package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.GamingPlatform
import com.example.data.GhostCloudRepository
import com.example.data.NetworkStatus
import com.example.data.StreamQuality
import com.example.data.UiState
import com.example.data.authStateFlow
import com.example.data.observeNetworkStatus
import com.example.data.signOutUser
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CloudStreamScreen
import com.example.ui.screens.CloudSyncScreen
import com.example.ui.screens.GameDetailSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MultiplayerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.GhostBackground
import com.example.ui.theme.GhostBlue
import com.example.ui.theme.GhostCloudTheme
import com.example.ui.theme.GhostCyan
import com.example.ui.theme.GhostSurface
import com.example.ui.theme.GhostTextPrimary
import com.example.ui.theme.GhostTextSecondary
import com.example.viewmodel.GhostCloudViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent {
            GhostCloudTheme {
                GhostCloudApp()
            }
        }
    }
}

private enum class GhostTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DISCOVER("discover", R.string.nav_discover, Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    MULTIPLAYER("multiplayer", R.string.nav_multiplayer, Icons.Filled.Groups, Icons.Outlined.Groups),
    CLOUD_SYNC("cloud_sync", R.string.nav_cloud_sync, Icons.Filled.CloudSync, Icons.Outlined.CloudSync),
    PROFILE("profile", R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun GhostCloudApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = { /* authStateFlow updates automatically */ })
    } else {
        val uid = user.uid
        val defaultGamertag = remember(user.displayName, uid) {
            user.displayName
                ?.trim()
                ?.takeIf { it.length >= 2 }
                ?.take(26)
                ?: "Ghost_${uid.take(6)}"
        }
        key(uid) {
            AuthenticatedGhostShell(
                currentUserId = uid,
                defaultGamertag = defaultGamertag
            )
        }
    }
}

@Composable
private fun AuthenticatedGhostShell(
    currentUserId: String,
    defaultGamertag: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: GhostCloudViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                GhostCloudViewModel(
                    repository = GhostCloudRepository(db),
                    currentUserId = currentUserId,
                    defaultGamertag = defaultGamertag
                )
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val cloudSavesState by viewModel.cloudSavesState.collectAsStateWithLifecycle()
    val libraryState by viewModel.libraryState.collectAsStateWithLifecycle()
    val publicLobbiesState by viewModel.publicLobbiesState.collectAsStateWithLifecycle()
    val myLobbiesState by viewModel.myLobbiesState.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedGameForDetail by viewModel.selectedGameForDetail.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val networkStatus by remember(context) { observeNetworkStatus(context) }
        .collectAsStateWithLifecycle(initialValue = NetworkStatus())

    var activeTab by rememberSaveable { mutableStateOf(GhostTab.DISCOVER) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusBanner) {
        val message = statusBanner
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearBanner()
        }
    }

    // Back press from secondary tabs returns to Discover home
    if (activeTab != GhostTab.DISCOVER && activeSession == null) {
        BackHandler { activeTab = GhostTab.DISCOVER }
    }

    val currentQuality = StreamQuality.fromCode(
        (profileState as? UiState.Success)?.data?.preferredQuality
    )
    val currentPlatform = GamingPlatform.fromCode(
        (profileState as? UiState.Success)?.data?.preferredPlatform
    )
    val libraryList = (libraryState as? UiState.Success)?.data.orEmpty()
    val cloudSavesList = (cloudSavesState as? UiState.Success)?.data.orEmpty()
    val publicLobbiesList = (publicLobbiesState as? UiState.Success)?.data.orEmpty()

    // Full-screen Cloud Play Session takes priority over tabbed shell
    val session = activeSession
    if (session != null) {
        CloudStreamScreen(
            session = session,
            streamQuality = currentQuality,
            networkStatus = networkStatus,
            onEndSession = { minutes, delta ->
                viewModel.endCloudSession(elapsedMinutes = minutes, syncedProgressDelta = delta)
            }
        )
        return
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostBackground)
    ) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = GhostBackground,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(containerColor = GhostSurface) {
                        GhostTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GhostCyan,
                                    selectedTextColor = GhostTextPrimary,
                                    indicatorColor = GhostBlue.copy(alpha = 0.24f),
                                    unselectedIconColor = GhostTextSecondary,
                                    unselectedTextColor = GhostTextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = GhostSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        GhostTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = GhostCyan,
                                    selectedTextColor = GhostTextPrimary,
                                    indicatorColor = GhostBlue.copy(alpha = 0.24f),
                                    unselectedIconColor = GhostTextSecondary,
                                    unselectedTextColor = GhostTextSecondary
                                ),
                                modifier = Modifier.testTag("rail_tab_${tab.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (activeTab) {
                        GhostTab.DISCOVER -> HomeScreen(
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            libraryEntries = libraryList,
                            cloudSaves = cloudSavesList,
                            networkStatus = networkStatus,
                            onSelectCategory = viewModel::selectCategory,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onGameClick = viewModel::openGameDetail,
                            onLaunchGame = { game -> viewModel.launchCloudSession(game) },
                            onTogglePin = viewModel::togglePin,
                            onNavigateToCloudSync = { activeTab = GhostTab.CLOUD_SYNC }
                        )

                        GhostTab.MULTIPLAYER -> MultiplayerScreen(
                            currentUserId = currentUserId,
                            publicLobbiesState = publicLobbiesState,
                            myLobbiesState = myLobbiesState,
                            onCreateLobby = viewModel::createMultiplayerLobby,
                            onJoinLobby = viewModel::joinLobby,
                            onLeaveLobby = viewModel::leaveLobby,
                            onToggleReady = viewModel::toggleReady,
                            onLaunchLobby = viewModel::launchLobbySession,
                            onCloseLobby = viewModel::closeLobby,
                            onDeleteLobby = viewModel::deleteLobby
                        )

                        GhostTab.CLOUD_SYNC -> CloudSyncScreen(
                            cloudSavesState = cloudSavesState,
                            onCreateSave = viewModel::createCloudSave,
                            onQuickSync = viewModel::quickSyncSave,
                            onResumeSave = { game, save ->
                                viewModel.launchCloudSession(game = game, save = save)
                            },
                            onDeleteSave = viewModel::deleteCloudSave
                        )

                        GhostTab.PROFILE -> ProfileScreen(
                            profileState = profileState,
                            pinnedCount = libraryList.count { it.pinned },
                            savesCount = cloudSavesList.size,
                            onSaveProfile = viewModel::updateProfile,
                            onSignOut = {
                                signOutUser(
                                    credentialManager = credentialManager,
                                    scope = scope
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    val detailGame = selectedGameForDetail
    if (detailGame != null) {
        val isPinned = libraryList.any { it.gameId == detailGame.id && it.pinned }
        val gameSaves = cloudSavesList.filter { it.gameId == detailGame.id }
        val gameLobbies = publicLobbiesList.filter { it.gameId == detailGame.id }
        GameDetailSheet(
            game = detailGame,
            pinned = isPinned,
            gameSaves = gameSaves,
            gameLobbies = gameLobbies,
            onDismiss = viewModel::dismissGameDetail,
            onLaunchGame = { g, save -> viewModel.launchCloudSession(game = g, save = save) },
            onTogglePin = { viewModel.togglePin(detailGame, isPinned) },
            onCreateQuickSave = {
                viewModel.createCloudSave(
                    game = detailGame,
                    slotName = detailGame.defaultSavePreset,
                    chapterOrRank = detailGame.defaultChapterPreset,
                    progressPercent = 15,
                    playTimeMinutes = 20,
                    platformOrigin = currentPlatform
                )
            },
            onOpenMultiplayer = {
                viewModel.dismissGameDetail()
                activeTab = GhostTab.MULTIPLAYER
            }
        )
    }
}
