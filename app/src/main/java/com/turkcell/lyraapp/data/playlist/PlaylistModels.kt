package com.turkcell.lyraapp.data.playlist

data class Playlist(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: String,
    val ownerId: String?,
)

data class PlaylistTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val durationMs: Int,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

data class PlaylistWithTracks(
    val id: String,
    val name: String,
    val description: String?,
    val ownerId: String?,
    val tracks: List<PlaylistTrack>,
)

/** 409 — şarkı çalma listesine zaten eklenmiş. ViewModel'lerin sessizce atlama/özel mesaj göstermesi için ayrılmıştır. */
class TrackAlreadyInPlaylistException(message: String) : Exception(message)
