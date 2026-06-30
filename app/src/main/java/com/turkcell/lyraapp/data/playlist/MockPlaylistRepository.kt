package com.turkcell.lyraapp.data.playlist

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * [PlaylistRepository]'nin MOCK implementasyonu — @Preview ve test amaçlıdır.
 * Gerçek API [RealPlaylistRepository] tarafından sağlanır (bkz. di/PlaylistModule.kt).
 */
class MockPlaylistRepository @Inject constructor() : PlaylistRepository {

    private val _playlists = MutableStateFlow(SEED_PLAYLISTS)
    override val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val tracksByPlaylist = mutableMapOf(
        "pl-gece" to DEFAULT_TRACKS.toMutableList(),
    )

    override suspend fun refreshPlaylists(): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        return Result.success(Unit)
    }

    override suspend fun createPlaylist(name: String, description: String?): Result<Playlist> {
        delay(NETWORK_DELAY_MS)
        val playlist = Playlist(
            id = "pl-${System.currentTimeMillis()}",
            name = name,
            description = description,
            createdAt = "2026-06-30T00:00:00Z",
            ownerId = "mock-user",
        )
        _playlists.value = _playlists.value + playlist
        tracksByPlaylist[playlist.id] = mutableListOf()
        return Result.success(playlist)
    }

    override suspend fun deletePlaylist(playlistId: String): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        _playlists.value = _playlists.value.filterNot { it.id == playlistId }
        tracksByPlaylist.remove(playlistId)
        return Result.success(Unit)
    }

    override suspend fun addTrack(playlistId: String, songId: String): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        val tracks = tracksByPlaylist.getOrPut(playlistId) { mutableListOf() }
        if (tracks.any { it.id == songId }) {
            return Result.failure(TrackAlreadyInPlaylistException("Bu şarkı zaten çalma listesinde."))
        }
        val track = DEFAULT_TRACKS.firstOrNull { it.id == songId }
            ?: PlaylistTrack(
                id = songId,
                title = "Şarkı",
                artist = "Sanatçı",
                album = null,
                durationMs = 200_000,
                artworkStartColor = 0xFF4AC2A8,
                artworkEndColor = 0xFF1F6E5C,
            )
        tracks.add(track)
        return Result.success(Unit)
    }

    override suspend fun removeTrack(playlistId: String, songId: String): Result<Unit> {
        delay(NETWORK_DELAY_MS)
        tracksByPlaylist[playlistId]?.removeAll { it.id == songId }
        return Result.success(Unit)
    }

    override suspend fun getPlaylistWithTracks(playlistId: String): Result<PlaylistWithTracks> {
        delay(NETWORK_DELAY_MS)
        val playlist = _playlists.value.firstOrNull { it.id == playlistId } ?: _playlists.value.first()
        return Result.success(
            PlaylistWithTracks(
                id = playlist.id,
                name = playlist.name,
                description = playlist.description,
                ownerId = playlist.ownerId,
                tracks = tracksByPlaylist[playlist.id].orEmpty(),
            ),
        )
    }

    private companion object {
        const val NETWORK_DELAY_MS = 400L

        val SEED_PLAYLISTS = listOf(
            Playlist(
                id = "pl-gece",
                name = "Gece Sürüşü",
                description = "Karanlık yollar için synth-pop",
                createdAt = "2026-06-01T00:00:00Z",
                ownerId = "mock-user",
            ),
        )

        val DEFAULT_TRACKS = listOf(
            PlaylistTrack("ps-1", "Neon Sokaklar", "Şehir Işıkları", null, 223_000, 0xFFD98E4A, 0xFF8A5526),
            PlaylistTrack("ps-2", "Gece Yarısı", "Mavi Deniz", null, 214_000, 0xFF4AC2A8, 0xFF1F6E5C),
            PlaylistTrack("ps-3", "Mor Bulutlar", "Derin Kaya", null, 232_000, 0xFF9B7FC4, 0xFF5A4480),
        )
    }
}
