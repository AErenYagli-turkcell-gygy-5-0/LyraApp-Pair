package com.turkcell.lyraapp.data.createplaylist

interface CreatePlaylistRepository {
    suspend fun getAvailableSongs(): Result<List<AvailableSong>>
    suspend fun createPlaylist(input: CreatePlaylistInput): Result<Unit>
}
