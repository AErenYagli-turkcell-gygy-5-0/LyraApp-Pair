package com.turkcell.lyraapp.data.remote.dto

import com.squareup.moshi.JsonClass

// region — Playlist
@JsonClass(generateAdapter = false)
data class PlaylistDto(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: String,
    val ownerId: String?,
)

@JsonClass(generateAdapter = false)
data class PlaylistsResponseDto(
    val data: List<PlaylistDto>,
)

@JsonClass(generateAdapter = false)
data class PlaylistResponseDto(
    val data: PlaylistDto,
)
// endregion

// region — Playlist with songs
@JsonClass(generateAdapter = false)
data class PlaylistWithSongsDto(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: String,
    val ownerId: String?,
    val songs: List<SongDto>,
)

@JsonClass(generateAdapter = false)
data class PlaylistWithSongsResponseDto(
    val data: PlaylistWithSongsDto,
)
// endregion

// region — Create playlist
@JsonClass(generateAdapter = false)
data class CreatePlaylistBodyDto(
    val name: String,
    val description: String? = null,
)
// endregion

// region — Delete playlist
@JsonClass(generateAdapter = false)
data class DeletePlaylistDataDto(
    val deleted: Boolean,
)

@JsonClass(generateAdapter = false)
data class DeletePlaylistResponseDto(
    val data: DeletePlaylistDataDto,
)
// endregion

// region — Add track
@JsonClass(generateAdapter = false)
data class AddTrackBodyDto(
    val songId: String,
)

@JsonClass(generateAdapter = false)
data class AddTrackDataDto(
    val added: Boolean,
)

@JsonClass(generateAdapter = false)
data class AddTrackResponseDto(
    val data: AddTrackDataDto,
)
// endregion

// region — Remove track
@JsonClass(generateAdapter = false)
data class RemoveTrackDataDto(
    val removed: Boolean,
)

@JsonClass(generateAdapter = false)
data class RemoveTrackResponseDto(
    val data: RemoveTrackDataDto,
)
// endregion
