package com.turkcell.lyraapp.data.playlistdetail

data class PlaylistDetail(
    val id: String,
    val title: String,
    val description: String,
    val ownerName: String,
    val songCount: Int,
    val totalDuration: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
    val songs: List<PlaylistSong>,
)

data class PlaylistSong(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
    val isLiked: Boolean = false,
    val isPlaying: Boolean = false,
)
