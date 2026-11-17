package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class GhostCloudRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    // Convenience constructor resolving the provisioned named database ID
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeProfile(userId: String): Flow<GamerProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch for profile stream." }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            GamerProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeCloudSaves(userId: String): Flow<List<CloudSave>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch for cloud_saves stream." }
        val path = "users/$uid/cloud_saves"
        emitAll(
            db.collection("users").document(uid).collection("cloud_saves")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudSave::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeLibrary(userId: String): Flow<List<LibraryEntry>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch for library stream." }
        val path = "users/$uid/library"
        emitAll(
            db.collection("users").document(uid).collection("library")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        LibraryEntry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observePublicLobbies(): Flow<List<MultiplayerLobby>> = flow {
        val uid = requireUserId()
        val path = "lobbies"
        emitAll(
            db.collection("lobbies")
                .whereEqualTo("visibility", "PUBLIC")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        MultiplayerLobby::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).filter { it.status != "CLOSED" }
                        .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeMyLobbies(userId: String): Flow<List<MultiplayerLobby>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch for my lobbies stream." }
        val path = "lobbies"
        emitAll(
            db.collection("lobbies")
                .whereArrayContains("memberUids", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        MultiplayerLobby::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureGamerProfile(userId: String, defaultGamertag: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Cross-user profile bootstrap refused." }
            val docRef = db.collection("users").document(uid)
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val safeTag = defaultGamertag.trim().ifBlank { "GhostPlayer" }.take(30)
                    .let { if (it.length < 2) "GhostPlayer" else it }
                val payload = mapOf(
                    "userId" to uid,
                    "gamertag" to safeTag,
                    "preferredPlatform" to GamingPlatform.XBOX_CLOUD.code,
                    "preferredQuality" to StreamQuality.FULL_HD.code,
                    "preferredRegion" to CloudRegion.NA_EAST.code,
                    "totalPlayMinutes" to 0L,
                    "cloudPassTier" to "FREE_UNLIMITED",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateGamerProfile(
        userId: String,
        gamertag: String,
        platform: GamingPlatform,
        quality: StreamQuality,
        region: CloudRegion
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Cross-user profile update refused." }
            val cleanTag = gamertag.trim()
            require(cleanTag.length in 2..30) { "Gamertag must be between 2 and 30 characters." }
            val safeRegion = if (region == CloudRegion.GLOBAL) CloudRegion.NA_EAST else region
            val docRef = db.collection("users").document(uid)
            docRef.update(
                mapOf(
                    "gamertag" to cleanTag,
                    "preferredPlatform" to platform.code,
                    "preferredQuality" to quality.code,
                    "preferredRegion" to safeRegion.code,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun createCloudSave(
        userId: String,
        game: XboxGame,
        slotName: String,
        chapterOrRank: String,
        progressPercent: Int,
        playTimeMinutes: Int,
        platformOrigin: GamingPlatform
    ): Result<String> {
        var path = "users/$userId/cloud_saves"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Cross-user save creation refused." }
            val cleanSlot = slotName.trim().take(60)
            val cleanChapter = chapterOrRank.trim().take(100)
            require(cleanSlot.isNotEmpty()) { "Save slot name cannot be empty." }
            require(cleanChapter.isNotEmpty()) { "Chapter or rank summary cannot be empty." }
            val clampedProgress = progressPercent.coerceIn(0, 100).toLong()
            val clampedMinutes = playTimeMinutes.coerceIn(0, 1_000_000).toLong()

            val docRef = db.collection("users").document(uid).collection("cloud_saves").document()
            path = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "gameId" to game.id,
                "gameTitle" to game.title.take(80),
                "slotName" to cleanSlot,
                "chapterOrRank" to cleanChapter,
                "progressPercent" to clampedProgress,
                "playTimeMinutes" to clampedMinutes,
                "platformOrigin" to platformOrigin.code,
                "syncStatus" to "SYNCED",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun syncCloudSaveProgress(
        userId: String,
        saveId: String,
        slotName: String,
        chapterOrRank: String,
        progressPercent: Int,
        playTimeMinutes: Long,
        platformOrigin: GamingPlatform
    ): Result<Unit> {
        val path = "users/$userId/cloud_saves/$saveId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Cross-user save update refused." }
            require(saveId.isNotBlank()) { "Save ID must not be blank." }
            val cleanSlot = slotName.trim().take(60)
            val cleanChapter = chapterOrRank.trim().take(100)
            require(cleanSlot.isNotEmpty() && cleanChapter.isNotEmpty()) {
                "Slot name and progress summary are required."
            }
            val docRef = db.collection("users").document(uid)
                .collection("cloud_saves").document(saveId)
            docRef.update(
                mapOf(
                    "slotName" to cleanSlot,
                    "chapterOrRank" to cleanChapter,
                    "progressPercent" to progressPercent.coerceIn(0, 100).toLong(),
                    "playTimeMinutes" to playTimeMinutes.coerceIn(0L, 1_000_000L),
                    "platformOrigin" to platformOrigin.code,
                    "syncStatus" to "SYNCED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCloudSave(userId: String, saveId: String): Result<Unit> {
        val path = "users/$userId/cloud_saves/$saveId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Cross-user save deletion refused." }
            require(saveId.isNotBlank()) { "Save ID must not be blank." }
            db.collection("users").document(uid)
                .collection("cloud_saves").document(saveId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun togglePinGame(
        userId: String,
        game: XboxGame,
        currentlyPinned: Boolean,
        quality: StreamQuality
    ): Result<Unit> {
        val path = "users/$userId/library/${game.id}"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Cross-user library write refused." }
            val docRef = db.collection("users").document(uid)
                .collection("library").document(game.id)
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "pinned" to !currentlyPinned,
                        "gameTitle" to game.title.take(80),
                        "lastSessionQuality" to quality.code,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "gameId" to game.id,
                        "gameTitle" to game.title.take(80),
                        "pinned" to true,
                        "playTimeMinutes" to 0L,
                        "lastSessionQuality" to quality.code,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun logSessionPlaytime(
        userId: String,
        game: XboxGame,
        addedMinutes: Long,
        quality: StreamQuality
    ): Result<Unit> {
        val safeAdded = addedMinutes.coerceIn(1L, 600L)
        val libraryPath = "users/$userId/library/${game.id}"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Cross-user session log refused." }
            val libRef = db.collection("users").document(uid)
                .collection("library").document(game.id)
            val libSnap = libRef.get().await()
            if (libSnap.exists()) {
                val prevMinutes = libSnap.getLong("playTimeMinutes") ?: 0L
                libRef.update(
                    mapOf(
                        "playTimeMinutes" to (prevMinutes + safeAdded).coerceAtMost(1_000_000L),
                        "lastSessionQuality" to quality.code,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                libRef.set(
                    mapOf(
                        "userId" to uid,
                        "gameId" to game.id,
                        "gameTitle" to game.title.take(80),
                        "pinned" to true,
                        "playTimeMinutes" to safeAdded,
                        "lastSessionQuality" to quality.code,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            val profileRef = db.collection("users").document(uid)
            val profileSnap = profileRef.get().await()
            if (profileSnap.exists()) {
                val prevTotal = profileSnap.getLong("totalPlayMinutes") ?: 0L
                profileRef.update(
                    mapOf(
                        "totalPlayMinutes" to (prevTotal + safeAdded).coerceAtMost(1_000_000L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, libraryPath)
            Result.failure(e)
        }
    }

    suspend fun createLobby(
        hostGamertag: String,
        game: XboxGame,
        title: String,
        region: CloudRegion,
        platforms: List<String>,
        maxPlayers: Int,
        visibility: String
    ): Result<String> {
        var path = "lobbies"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim()
            require(cleanTitle.length in 3..60) { "Lobby title must be 3 to 60 characters." }
            val cleanHostTag = hostGamertag.trim().ifBlank { "GhostHost" }.take(30)
                .let { if (it.length < 2) "GhostHost" else it }
            val cleanPlatforms = platforms.distinct().take(4).ifEmpty { listOf("XBOX", "CLOUD") }
            val clampedMax = maxPlayers.coerceIn(2, 16).toLong()
            val safeVisibility = if (visibility == "PRIVATE") "PRIVATE" else "PUBLIC"

            val docRef = db.collection("lobbies").document()
            path = docRef.path
            val payload = mapOf(
                "hostUid" to uid,
                "hostGamertag" to cleanHostTag,
                "gameId" to game.id,
                "gameTitle" to game.title.take(80),
                "title" to cleanTitle,
                "region" to region.code,
                "crossPlayPlatforms" to cleanPlatforms,
                "visibility" to safeVisibility,
                "status" to "OPEN",
                "maxPlayers" to clampedMax,
                "memberUids" to listOf(uid),
                "readyUids" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun joinLobby(lobby: MultiplayerLobby): Result<Unit> {
        val path = "lobbies/${lobby.id}"
        return try {
            val uid = requireUserId()
            require(lobby.id.isNotBlank()) { "Lobby ID is blank." }
            if (uid in lobby.memberUids) return Result.success(Unit)
            require(lobby.memberUids.size < lobby.maxPlayers) { "Squad is full." }
            val updatedMembers = lobby.memberUids + uid
            db.collection("lobbies").document(lobby.id).update(
                mapOf(
                    "memberUids" to updatedMembers,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun leaveLobby(lobby: MultiplayerLobby): Result<Unit> {
        val path = "lobbies/${lobby.id}"
        return try {
            val uid = requireUserId()
            require(lobby.id.isNotBlank()) { "Lobby ID is blank." }
            require(uid != lobby.hostUid) { "Host must close or delete the squad." }
            val updatedMembers = lobby.memberUids.filterNot { it == uid }
            val updatedReady = lobby.readyUids.filterNot { it == uid }
            db.collection("lobbies").document(lobby.id).update(
                mapOf(
                    "memberUids" to updatedMembers,
                    "readyUids" to updatedReady,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleReady(lobby: MultiplayerLobby): Result<Unit> {
        val path = "lobbies/${lobby.id}"
        return try {
            val uid = requireUserId()
            require(lobby.id.isNotBlank()) { "Lobby ID is blank." }
            require(uid in lobby.memberUids) { "Join the squad before toggling ready." }
            val updatedReady = if (uid in lobby.readyUids) {
                lobby.readyUids.filterNot { it == uid }
            } else {
                lobby.readyUids + uid
            }
            db.collection("lobbies").document(lobby.id).update(
                mapOf(
                    "readyUids" to updatedReady,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateLobbyStatus(lobbyId: String, newStatus: String): Result<Unit> {
        val path = "lobbies/$lobbyId"
        return try {
            requireUserId()
            require(lobbyId.isNotBlank()) { "Lobby ID is blank." }
            require(newStatus in listOf("OPEN", "IN_SESSION", "CLOSED")) { "Invalid lobby status." }
            db.collection("lobbies").document(lobbyId).update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteLobby(lobbyId: String): Result<Unit> {
        val path = "lobbies/$lobbyId"
        return try {
            requireUserId()
            require(lobbyId.isNotBlank()) { "Lobby ID is blank." }
            db.collection("lobbies").document(lobbyId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
