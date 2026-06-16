package com.turkcell.lyraapp.data.playlistdetail

interface PlaylistDetailRepository {
    suspend fun getPlaylistDetail(playlistId: String): Result<PlaylistDetail>
}
