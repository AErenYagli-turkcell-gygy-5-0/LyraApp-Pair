package com.turkcell.lyraapp.data.playlist

import com.squareup.moshi.Moshi
import com.turkcell.lyraapp.data.home.artworkColorsFor
import com.turkcell.lyraapp.data.remote.PlaylistApiService
import com.turkcell.lyraapp.data.remote.dto.AddTrackBodyDto
import com.turkcell.lyraapp.data.remote.dto.ApiErrorResponseDto
import com.turkcell.lyraapp.data.remote.dto.CreatePlaylistBodyDto
import com.turkcell.lyraapp.data.remote.dto.PlaylistDto
import com.turkcell.lyraapp.data.remote.dto.SongDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealPlaylistRepository @Inject constructor(
    private val playlistApiService: PlaylistApiService,
    private val moshi: Moshi,
) : PlaylistRepository {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    override val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    override suspend fun refreshPlaylists(): Result<Unit> = runCatching {
        val response = playlistApiService.getMyPlaylists()
        _playlists.value = response.data.map { it.toDomain() }
    }

    override suspend fun createPlaylist(name: String, description: String?): Result<Playlist> =
        runCatching {
            val response = playlistApiService.createPlaylist(
                CreatePlaylistBodyDto(name = name, description = description),
            )
            if (!response.isSuccessful) throw apiException(response, ::createErrorMessage)
            val playlist = response.body()!!.data.toDomain()
            _playlists.value = _playlists.value + playlist
            playlist
        }

    override suspend fun deletePlaylist(playlistId: String): Result<Unit> = runCatching {
        val response = playlistApiService.deletePlaylist(playlistId)
        if (!response.isSuccessful) throw apiException(response, ::ownershipErrorMessage)
        _playlists.value = _playlists.value.filterNot { it.id == playlistId }
    }

    override suspend fun addTrack(playlistId: String, songId: String): Result<Unit> = runCatching {
        val response = playlistApiService.addTrack(playlistId, AddTrackBodyDto(songId = songId))
        if (!response.isSuccessful) {
            if (response.code() == 409) {
                throw TrackAlreadyInPlaylistException(
                    parsedErrorMessage(response) ?: "Bu şarkı zaten çalma listesinde.",
                )
            }
            throw apiException(response, ::ownershipErrorMessage)
        }
    }

    override suspend fun removeTrack(playlistId: String, songId: String): Result<Unit> = runCatching {
        val response = playlistApiService.removeTrack(playlistId, songId)
        if (!response.isSuccessful) throw apiException(response, ::ownershipErrorMessage)
    }

    override suspend fun getPlaylistWithTracks(playlistId: String): Result<PlaylistWithTracks> =
        runCatching {
            val dto = playlistApiService.getPlaylistWithSongs(playlistId).data
            PlaylistWithTracks(
                id = dto.id,
                name = dto.name,
                description = dto.description,
                ownerId = dto.ownerId,
                tracks = dto.songs.map { it.toTrack() },
            )
        }

    private fun PlaylistDto.toDomain() = Playlist(
        id = id,
        name = name,
        description = description,
        createdAt = createdAt,
        ownerId = ownerId,
    )

    private fun SongDto.toTrack(): PlaylistTrack {
        val (startColor, endColor) = artworkColorsFor(id)
        return PlaylistTrack(
            id = id,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            artworkStartColor = startColor,
            artworkEndColor = endColor,
        )
    }

    private fun parsedErrorMessage(response: Response<*>): String? {
        val errorBody = response.errorBody()?.string() ?: return null
        return try {
            moshi.adapter(ApiErrorResponseDto::class.java).fromJson(errorBody)?.error?.message
        } catch (_: Exception) {
            null
        }
    }

    private fun apiException(response: Response<*>, fallback: (Int) -> String): Exception =
        Exception(parsedErrorMessage(response) ?: fallback(response.code()))

    private fun createErrorMessage(code: Int): String = when (code) {
        400 -> "Geçersiz çalma listesi adı veya açıklaması."
        401 -> "Oturum süresi dolmuş. Lütfen tekrar giriş yapın."
        else -> "Çalma listesi oluşturulamadı (${code})."
    }

    private fun ownershipErrorMessage(code: Int): String = when (code) {
        401 -> "Oturum süresi dolmuş. Lütfen tekrar giriş yapın."
        403 -> "Bu çalma listesinin sahibi değilsiniz."
        404 -> "Çalma listesi veya şarkı bulunamadı."
        else -> "İşlem tamamlanamadı (${code})."
    }
}
