package com.example.samespace.repo

import com.example.samespace.models.Resource
import com.example.samespace.models.SongsList
import com.example.samespace.network.ApiService

class SongsListRepo(private val apiService: ApiService) : BaseRepo() {
    suspend fun getSongsList(): Resource<SongsList> {
        return safeApiCall { apiService.getSongsList() }
    }
}
