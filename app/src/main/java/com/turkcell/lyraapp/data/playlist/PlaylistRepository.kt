package com.turkcell.lyraapp.data.playlist

import kotlinx.coroutines.flow.StateFlow

/**
 * Kütüphane, Yeni Çalma Listesi ve Playlist Detay ekranlarının paylaştığı tek veri kaynağı.
 *
 * [playlists] reaktiftir: oluşturma/silme sonrası tüm tüketiciler otomatik güncellenir,
 * manuel refetch gerekmez (bkz. docs/decisions.md "Çalma Listeleri — Gerçek API Entegrasyonu").
 */
interface PlaylistRepository {

    val playlists: StateFlow<List<Playlist>>

    suspend fun refreshPlaylists(): Result<Unit>

    suspend fun createPlaylist(name: String, description: String?): Result<Playlist>

    suspend fun deletePlaylist(playlistId: String): Result<Unit>

    suspend fun addTrack(playlistId: String, songId: String): Result<Unit>

    suspend fun removeTrack(playlistId: String, songId: String): Result<Unit>

    suspend fun getPlaylistWithTracks(playlistId: String): Result<PlaylistWithTracks>
}
