package com.example.samespace

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

    init {
        getSongsList()
    }

    private fun getSongsList() {
        _songsList.value = Resource.Loading()
        viewModelScope.launch {
            try {
                _songsList.value = songsListRepo.getSongsList()
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
