package com.turkcell.lyraapp.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class PlaybackNextBodyDto(
    val songId: String,
)

@JsonClass(generateAdapter = false)
data class StreamLinkDto(
    val url: String,
    val expiresAt: String,
    val mimeType: String,
)

@JsonClass(generateAdapter = false)
data class AdDto(
    val id: String,
    val title: String,
    val advertiser: String,
    val durationMs: Int,
    val mimeType: String,
)

@JsonClass(generateAdapter = false)
data class PlaybackNextDataDto(
    val type: String,
    val song: SongDto,
    val stream: StreamLinkDto,
    val ad: AdDto? = null,
    val adStream: StreamLinkDto? = null,
    val impressionId: String? = null,
)

@JsonClass(generateAdapter = false)
data class PlaybackNextResponseDto(
    val data: PlaybackNextDataDto,
)

@JsonClass(generateAdapter = false)
data class AdCompleteBodyDto(
    val impressionId: String,
)

@JsonClass(generateAdapter = false)
data class AdCompleteDataDto(
    val completed: Boolean,
)

@JsonClass(generateAdapter = false)
data class AdCompleteResponseDto(
    val data: AdCompleteDataDto,
)
