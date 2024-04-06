package com.example.samespace.models

data class SongsList(
    val data: List<Song>,
)

data class Song(
    val accent: String,
    val artist: String,
    var cover: String,
    val id: Int,
    val name: String,
    val status: String,
    val top_track: Boolean,
    val url: String,
)
