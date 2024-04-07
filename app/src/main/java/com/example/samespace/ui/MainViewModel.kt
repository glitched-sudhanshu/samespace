package com.example.samespace.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.samespace.models.Resource
import com.example.samespace.models.Song
import com.example.samespace.models.SongsList
import com.example.samespace.repo.SongsListRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(private val songsListRepo: SongsListRepo) : ViewModel() {
    private val _songsList = MutableLiveData<Resource<SongsList>>(Resource.Loading())
    val songsList: LiveData<Resource<SongsList>> = _songsList
    private val _topTrackSongsList = MutableLiveData<Resource<SongsList>>(Resource.Loading())
    val topTrackSongsList: LiveData<Resource<SongsList>> = _topTrackSongsList
    private val _songPointer = MutableLiveData(0)
    val songPointer: LiveData<Int> = _songPointer
    private val _currentlyPlaying = MutableLiveData<Resource<Song>>(null)
    val currentlyPlaying: LiveData<Resource<Song>> = _currentlyPlaying
    private val _isPlaying = MutableLiveData(true)
    val isPlaying: LiveData<Boolean> = _isPlaying

    init {
        getSongsList()
    }

    private fun getSongsList() {
        _songsList.value = Resource.Loading()
        viewModelScope.launch {
            // added delay to to show loading views
            delay(2000)
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

    fun setCurrentSong(song: Song) {
        _currentlyPlaying.value = Resource.Success(song)
    }

    fun setIsPlaying(boolean: Boolean) {
        _isPlaying.value = boolean
    }

    fun setSongPosition(
        position: Int,
        fromTopTrack: Boolean,
    ) {
        if (fromTopTrack) {
            _songPointer.value =
                position.rem(_topTrackSongsList.value?.data?.data?.size ?: 1)
        } else {
            _songPointer.value =
                position.rem(_songsList.value?.data?.data?.size ?: 1)
        }
    }

    fun nextSong(fromTopTrack: Boolean) {
        if (fromTopTrack) {
            _songPointer.value =
                (_songPointer.value?.plus(1))?.rem(_topTrackSongsList.value?.data?.data?.size ?: 1)
        } else {
            _songPointer.value =
                (_songPointer.value?.plus(1))?.rem(_songsList.value?.data?.data?.size ?: 1)
        }
    }

    fun previousSong(fromTopTrack: Boolean) {
        if (fromTopTrack) {
            if (_songPointer.value == 0) {
                _songPointer.value = (_topTrackSongsList.value?.data?.data?.size ?: 1).minus(1)
            } else {
                _songPointer.value =
                    _songPointer.value?.minus(1)
                        ?.rem(_topTrackSongsList.value?.data?.data?.size ?: 1)
            }
        } else {
            if (_songPointer.value == 0) {
                _songPointer.value = (_songsList.value?.data?.data?.size ?: 1).minus(1)
            } else {
                _songPointer.value =
                    _songPointer.value?.minus(1)?.rem(_songsList.value?.data?.data?.size ?: 1)
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
