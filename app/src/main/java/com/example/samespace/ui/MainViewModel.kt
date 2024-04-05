package com.example.samespace.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.samespace.models.Resource
import com.example.samespace.models.SongsList
import com.example.samespace.repo.SongsListRepo
import kotlinx.coroutines.launch

class MainViewModel(private val songsListRepo: SongsListRepo) : ViewModel() {
    private val _songsList = MutableLiveData<Resource<SongsList>>(Resource.Loading())
    val songsList: LiveData<Resource<SongsList>> = _songsList
    private val _topTrackSongsList = MutableLiveData<Resource<SongsList>>(Resource.Loading())
    val topTrackSongsList: LiveData<Resource<SongsList>> = _topTrackSongsList

    init {
        getSongsList()
    }

    private fun getSongsList() {
        _songsList.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val songsList = songsListRepo.getSongsList()
                _songsList.value = songsList
                _topTrackSongsList.value =
                    Resource.Success(
                        SongsList(
                            songsList.data?.data?.filter {
                                it.top_track
                            } ?: emptyList(),
                        ),
                    )
            } catch (e: Exception) {
                _songsList.value = Resource.Error("Something went wrong!")
            }
        }
    }
}

class MainViewModelFactory(private val songsListRepo: SongsListRepo) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(songsListRepo) as T
    }
}
