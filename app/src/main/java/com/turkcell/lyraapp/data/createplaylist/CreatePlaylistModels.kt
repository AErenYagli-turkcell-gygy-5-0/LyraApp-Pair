package com.turkcell.lyraapp.data.createplaylist

data class AvailableSong(
    val id: String,
    val title: String,
    val artist: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

data class CreatePlaylistInput(
    val name: String,
    val description: String,
    val isPublic: Boolean,
    val selectedSongIds: List<String>,
)
