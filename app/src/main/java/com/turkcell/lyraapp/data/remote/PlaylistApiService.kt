package com.turkcell.lyraapp.data.remote

import com.turkcell.lyraapp.data.remote.dto.AddTrackBodyDto
import com.turkcell.lyraapp.data.remote.dto.AddTrackResponseDto
import com.turkcell.lyraapp.data.remote.dto.CreatePlaylistBodyDto
import com.turkcell.lyraapp.data.remote.dto.DeletePlaylistResponseDto
import com.turkcell.lyraapp.data.remote.dto.PlaylistResponseDto
import com.turkcell.lyraapp.data.remote.dto.PlaylistWithSongsResponseDto
import com.turkcell.lyraapp.data.remote.dto.PlaylistsResponseDto
import com.turkcell.lyraapp.data.remote.dto.RemoveTrackResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PlaylistApiService {

    @GET("api/v1/me/playlists")
    suspend fun getMyPlaylists(): PlaylistsResponseDto

    @POST("api/v1/me/playlists")
    suspend fun createPlaylist(@Body body: CreatePlaylistBodyDto): Response<PlaylistResponseDto>

    @DELETE("api/v1/me/playlists/{id}")
    suspend fun deletePlaylist(@Path("id") playlistId: String): Response<DeletePlaylistResponseDto>

    @POST("api/v1/me/playlists/{id}/tracks")
    suspend fun addTrack(
        @Path("id") playlistId: String,
        @Body body: AddTrackBodyDto,
    ): Response<AddTrackResponseDto>

    @DELETE("api/v1/me/playlists/{id}/tracks/{songId}")
    suspend fun removeTrack(
        @Path("id") playlistId: String,
        @Path("songId") songId: String,
    ): Response<RemoveTrackResponseDto>

    @GET("api/v1/playlists/{id}")
    suspend fun getPlaylistWithSongs(@Path("id") playlistId: String): PlaylistWithSongsResponseDto
}
