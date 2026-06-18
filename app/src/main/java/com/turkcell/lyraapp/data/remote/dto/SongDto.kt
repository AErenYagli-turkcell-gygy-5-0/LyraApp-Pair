package com.turkcell.lyraapp.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class SongDto(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val durationMs: Int,
    val mimeType: String,
    val sizeBytes: Long,
    val createdAt: String,
)

@JsonClass(generateAdapter = false)
data class SongsResponseDto(
    val data: List<SongDto>,
    @Json(name = "nextCursor") val nextCursor: String?,
)
