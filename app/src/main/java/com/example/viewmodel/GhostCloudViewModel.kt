package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CloudRegion
import com.example.data.CloudSave
import com.example.data.GameCatalog
import com.example.data.GameCategory
import com.example.data.GamerProfile
import com.example.data.GamingPlatform
import com.example.data.GhostCloudRepository
import com.example.data.LibraryEntry
import com.example.data.MultiplayerLobby
import com.example.data.StreamQuality
import com.example.data.UiState
import com.example.data.XboxGame
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActiveCloudSession(
    val game: XboxGame,
    val activeSave: CloudSave? = null,
    val activeLobby: MultiplayerLobby? = null
)

class GhostCloudViewModel(
    private val repository: GhostCloudRepository,
    val currentUserId: String,
    private val defaultGamertag: String
) : ViewModel() {

    val profileState: StateFlow<UiState<GamerProfile?>> = repository.observeProfile(currentUserId)
        .map<GamerProfile?, UiState<GamerProfile?>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing profile", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load gamer profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val cloudSavesState: StateFlow<UiState<List<CloudSave>>> = repository.observeCloudSaves(currentUserId)
        .map<List<CloudSave>, UiState<List<CloudSave>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing cloud saves", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load cloud saves"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val libraryState: StateFlow<UiState<List<LibraryEntry>>> = repository.observeLibrary(currentUserId)
        .map<List<LibraryEntry>, UiState<List<LibraryEntry>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing library", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load library"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val publicLobbiesState: StateFlow<UiState<List<MultiplayerLobby>>> = repository.observePublicLobbies()
        .map<List<MultiplayerLobby>, UiState<List<MultiplayerLobby>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing public lobbies", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load cross-play lobbies"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val myLobbiesState: StateFlow<UiState<List<MultiplayerLobby>>> = repository.observeMyLobbies(currentUserId)
        .map<List<MultiplayerLobby>, UiState<List<MultiplayerLobby>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing my lobbies", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load your squads"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _selectedCategory = MutableStateFlow(GameCategory.ALL)
    val selectedCategory: StateFlow<GameCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGameForDetail = MutableStateFlow<XboxGame?>(null)
    val selectedGameForDetail: StateFlow<XboxGame?> = _selectedGameForDetail.asStateFlow()

    private val _activeSession = MutableStateFlow<ActiveCloudSession?>(null)
    val activeSession: StateFlow<ActiveCloudSession?> = _activeSession.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureGamerProfile(currentUserId, defaultGamertag)
        }
    }

    fun selectCategory(category: GameCategory) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openGameDetail(game: XboxGame) {
        _selectedGameForDetail.value = game
    }

    fun dismissGameDetail() {
        _selectedGameForDetail.value = null
    }

    fun launchCloudSession(
        game: XboxGame,
        save: CloudSave? = null,
        lobby: MultiplayerLobby? = null
    ) {
        _selectedGameForDetail.value = null
        _activeSession.value = ActiveCloudSession(game = game, activeSave = save, activeLobby = lobby)
    }

    fun endCloudSession(elapsedMinutes: Long, syncedProgressDelta: Int = 0) {
        val session = _activeSession.value ?: return
        _activeSession.value = null
        val quality = currentQuality()
        val minutesToLog = elapsedMinutes.coerceAtLeast(1L)
        viewModelScope.launch {
            repository.logSessionPlaytime(currentUserId, session.game, minutesToLog, quality)
            val save = session.activeSave
            if (save != null && save.id.isNotBlank()) {
                val nextProgress = (save.progressPercent + syncedProgressDelta).coerceIn(0L, 100L).toInt()
                repository.syncCloudSaveProgress(
                    userId = currentUserId,
                    saveId = save.id,
                    slotName = save.slotName,
                    chapterOrRank = save.chapterOrRank,
                    progressPercent = nextProgress,
                    playTimeMinutes = save.playTimeMinutes + minutesToLog,
                    platformOrigin = currentPlatform()
                ).onSuccess {
                    emitBanner("Cloud Save synced for ${session.game.title} (+${minutesToLog}m)")
                }.onFailure { err ->
                    emitBanner("Session ended; sync failed: ${err.localizedMessage ?: "Retry in Cloud Sync"}")
                }
            } else {
                emitBanner("Synced ${minutesToLog}m session for ${session.game.title}")
            }
        }
    }

    fun togglePin(game: XboxGame, currentlyPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinGame(currentUserId, game, currentlyPinned, currentQuality())
                .onSuccess {
                    emitBanner(
                        if (!currentlyPinned) "${game.title} pinned to My Library"
                        else "${game.title} unpinned"
                    )
                }
                .onFailure { emitBanner(it.localizedMessage ?: "Unable to update library") }
        }
    }

    fun createCloudSave(
        game: XboxGame,
        slotName: String,
        chapterOrRank: String,
        progressPercent: Int,
        playTimeMinutes: Int,
        platformOrigin: GamingPlatform
    ) {
        viewModelScope.launch {
            repository.createCloudSave(
                userId = currentUserId,
                game = game,
                slotName = slotName,
                chapterOrRank = chapterOrRank,
                progressPercent = progressPercent,
                playTimeMinutes = playTimeMinutes,
                platformOrigin = platformOrigin
            ).onSuccess {
                emitBanner("Cloud Save '${slotName.trim()}' synced to Ghost Cloud")
            }.onFailure {
                emitBanner(it.localizedMessage ?: "Failed to create cloud save")
            }
        }
    }

    fun quickSyncSave(save: CloudSave) {
        val nextProgress = (save.progressPercent + 5L).coerceAtMost(100L).toInt()
        val nextMinutes = save.playTimeMinutes + 15L
        viewModelScope.launch {
            repository.syncCloudSaveProgress(
                userId = currentUserId,
                saveId = save.id,
                slotName = save.slotName,
                chapterOrRank = save.chapterOrRank,
                progressPercent = nextProgress,
                playTimeMinutes = nextMinutes,
                platformOrigin = currentPlatform()
            ).onSuccess {
                emitBanner("Synced ${save.gameTitle} • ${nextProgress}% progress")
            }.onFailure {
                emitBanner(it.localizedMessage ?: "Cloud sync failed")
            }
        }
    }

    fun deleteCloudSave(save: CloudSave) {
        viewModelScope.launch {
            repository.deleteCloudSave(currentUserId, save.id)
                .onSuccess { emitBanner("Deleted cloud save '${save.slotName}'") }
                .onFailure { emitBanner(it.localizedMessage ?: "Failed to delete save") }
        }
    }

    fun createMultiplayerLobby(
        game: XboxGame,
        title: String,
        region: CloudRegion,
        platforms: List<String>,
        maxPlayers: Int,
        visibility: String
    ) {
        val gamertag = (profileState.value as? UiState.Success)?.data?.gamertag ?: defaultGamertag
        viewModelScope.launch {
            repository.createLobby(
                hostGamertag = gamertag,
                game = game,
                title = title,
                region = region,
                platforms = platforms,
                maxPlayers = maxPlayers,
                visibility = visibility
            ).onSuccess {
                emitBanner("Cross-play squad '${title.trim()}' is live!")
            }.onFailure {
                emitBanner(it.localizedMessage ?: "Failed to host squad")
            }
        }
    }

    fun joinLobby(lobby: MultiplayerLobby) {
        viewModelScope.launch {
            repository.joinLobby(lobby)
                .onSuccess { emitBanner("Joined ${lobby.gameTitle} squad") }
                .onFailure { emitBanner(it.localizedMessage ?: "Could not join squad") }
        }
    }

    fun leaveLobby(lobby: MultiplayerLobby) {
        viewModelScope.launch {
            repository.leaveLobby(lobby)
                .onSuccess { emitBanner("Left squad '${lobby.title}'") }
                .onFailure { emitBanner(it.localizedMessage ?: "Could not leave squad") }
        }
    }

    fun toggleReady(lobby: MultiplayerLobby) {
        viewModelScope.launch {
            repository.toggleReady(lobby)
                .onFailure { emitBanner(it.localizedMessage ?: "Could not update ready state") }
        }
    }

    fun launchLobbySession(lobby: MultiplayerLobby) {
        val game = GameCatalog.findById(lobby.gameId) ?: GameCatalog.featuredHero
        viewModelScope.launch {
            if (lobby.hostUid == currentUserId && lobby.status == "OPEN") {
                repository.updateLobbyStatus(lobby.id, "IN_SESSION")
            }
            launchCloudSession(game = game, lobby = lobby)
        }
    }

    fun closeLobby(lobby: MultiplayerLobby) {
        viewModelScope.launch {
            repository.updateLobbyStatus(lobby.id, "CLOSED")
                .onSuccess { emitBanner("Squad '${lobby.title}' closed") }
                .onFailure { emitBanner(it.localizedMessage ?: "Failed to close squad") }
        }
    }

    fun deleteLobby(lobby: MultiplayerLobby) {
        viewModelScope.launch {
            repository.deleteLobby(lobby.id)
                .onSuccess { emitBanner("Squad removed") }
                .onFailure { emitBanner(it.localizedMessage ?: "Failed to remove squad") }
        }
    }

    fun updateProfile(
        gamertag: String,
        platform: GamingPlatform,
        quality: StreamQuality,
        region: CloudRegion
    ) {
        viewModelScope.launch {
            repository.updateGamerProfile(currentUserId, gamertag, platform, quality, region)
                .onSuccess { emitBanner("Gamer profile synced to Ghost Cloud") }
                .onFailure { emitBanner(it.localizedMessage ?: "Could not save profile") }
        }
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    private fun emitBanner(message: String) {
        _statusBanner.value = message
    }

    private fun currentQuality(): StreamQuality {
        val raw = (profileState.value as? UiState.Success)?.data?.preferredQuality
        return StreamQuality.fromCode(raw)
    }

    private fun currentPlatform(): GamingPlatform {
        val raw = (profileState.value as? UiState.Success)?.data?.preferredPlatform
        return GamingPlatform.fromCode(raw)
    }

    private companion object {
        const val TAG = "GhostCloudVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
