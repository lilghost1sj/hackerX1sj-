package com.example.data

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class GameCategory(val label: String) {
    ALL("All Games"),
    PREMIUM("Premium Free"),
    RACING("Racing"),
    ACTION("Action"),
    SHOOTER("Shooter"),
    PINNED("My Library")
}

enum class GamingPlatform(val code: String, val label: String) {
    XBOX_CLOUD("XBOX_CLOUD", "Xbox Cloud"),
    XBOX_SERIES_X("XBOX_SERIES_X", "Xbox Series X|S"),
    PC_CROSSPLAY("PC_CROSSPLAY", "PC Cross-Play"),
    ANDROID_TOUCH("ANDROID_TOUCH", "Android Touch");

    companion object {
        fun fromCode(raw: String?): GamingPlatform =
            entries.firstOrNull { it.code == raw } ?: XBOX_CLOUD
    }
}

enum class StreamQuality(val code: String, val label: String, val bitrateLabel: String) {
    LOW_LATENCY("720P_LOW_LATENCY", "720p • Low Latency", "12 Mbps"),
    FULL_HD("1080P_60FPS", "1080p • 60 FPS", "25 Mbps"),
    ULTRA_HDR("4K_HDR_60FPS", "4K HDR • 60 FPS", "45 Mbps");

    companion object {
        fun fromCode(raw: String?): StreamQuality =
            entries.firstOrNull { it.code == raw } ?: FULL_HD
    }
}

enum class CloudRegion(val code: String, val label: String) {
    NA_EAST("NA_EAST", "NA-East"),
    EU_WEST("EU_WEST", "EU-West"),
    ASIA_PACIFIC("ASIA_PACIFIC", "Asia-Pacific"),
    SA_EAST("SA_EAST", "SA-East"),
    GLOBAL("GLOBAL", "Global");

    companion object {
        fun fromCode(raw: String?): CloudRegion =
            entries.firstOrNull { it.code == raw } ?: NA_EAST
    }
}

enum class VisualStyle {
    FORZA_HIGHWAY,
    WATCH_DOGS_CYBER,
    BLUR_NEON_POWERUP,
    WARZONE_TACTICAL,
    GTAV_SUNSET,
    NFS_UNDERGROUND,
    HALO_PLASMA,
    CYBERPUNK_NIGHT
}

data class XboxGame(
    val id: String,
    val title: String,
    val subtitle: String,
    val genre: String,
    val category: GameCategory,
    val premiumShowcase: Boolean,
    val crossPlatform: Boolean,
    val cloudSync: Boolean,
    val maxSquadSize: Int,
    val rating: String,
    val resolutionSpec: String,
    val description: String,
    val supportedPlatforms: List<String>,
    val defaultSavePreset: String,
    val defaultChapterPreset: String,
    @DrawableRes val coverArtRes: Int?,
    val visualStyle: VisualStyle
)

@Keep
@IgnoreExtraProperties
data class GamerProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val gamertag: String = "GhostPlayer",
    val preferredPlatform: String = GamingPlatform.XBOX_CLOUD.code,
    val preferredQuality: String = StreamQuality.FULL_HD.code,
    val preferredRegion: String = CloudRegion.NA_EAST.code,
    val totalPlayMinutes: Long = 0L,
    val cloudPassTier: String = "FREE_UNLIMITED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudSave(
    @DocumentId val id: String = "",
    val userId: String = "",
    val gameId: String = "",
    val gameTitle: String = "",
    val slotName: String = "",
    val chapterOrRank: String = "",
    val progressPercent: Long = 0L,
    val playTimeMinutes: Long = 0L,
    val platformOrigin: String = GamingPlatform.XBOX_CLOUD.code,
    val syncStatus: String = "SYNCED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class LibraryEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val gameId: String = "",
    val gameTitle: String = "",
    val pinned: Boolean = true,
    val playTimeMinutes: Long = 0L,
    val lastSessionQuality: String = StreamQuality.FULL_HD.code,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class MultiplayerLobby(
    @DocumentId val id: String = "",
    val hostUid: String = "",
    val hostGamertag: String = "",
    val gameId: String = "",
    val gameTitle: String = "",
    val title: String = "",
    val region: String = CloudRegion.NA_EAST.code,
    val crossPlayPlatforms: List<String> = emptyList(),
    val visibility: String = "PUBLIC",
    val status: String = "OPEN",
    val maxPlayers: Long = 4L,
    val memberUids: List<String> = emptyList(),
    val readyUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class NetworkStatus(
    val connected: Boolean = true,
    val transportLabel: String = "Wi-Fi",
    val speedTier: String = "Cloud Ready"
)
