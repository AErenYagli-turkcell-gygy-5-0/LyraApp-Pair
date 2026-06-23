package com.turkcell.lyraapp.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class SongListResponseDto(
    val data: List<SongDto>,
)

@JsonClass(generateAdapter = false)
data class RecordPlayBodyDto(
    val songId: String,
)

@JsonClass(generateAdapter = false)
data class RecordPlayDataDto(
    val recorded: Boolean,
)

@JsonClass(generateAdapter = false)
data class RecordPlayResponseDto(
    val data: RecordPlayDataDto,
)
