package com.turkcell.lyraapp.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class SongListResponseDto(
    val data: List<SongDto>,
)
