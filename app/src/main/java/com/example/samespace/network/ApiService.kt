package com.example.samespace.network

import com.example.samespace.models.SongsList
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("items/songs")
    suspend fun getSongsList(): Response<SongsList>
}
