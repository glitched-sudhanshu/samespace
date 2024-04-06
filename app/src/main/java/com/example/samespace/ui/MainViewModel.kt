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

    init {
        getSongsList()
    }

    private fun getSongsList() {
        _songsList.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val songsList = addCovers(songsListRepo.getSongsList())
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

    private fun addCovers(resource: Resource<SongsList>): Resource<SongsList> {
        return when (resource) {
            is Resource.Loading -> resource
            is Resource.Error -> resource
            is Resource.Success<SongsList> -> {
                val listOfCovers =
                    listOf(
                        "https://i1.sndcdn.com/artworks-TxSa6wrxRAgL32fP-BvGtDw-t500x500.jpg",
                        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQzhBDi9GhIjVUkc2lALeF4d__hrt1On_UUaOuxV0_ARg&s",
                        "https://a10.gaanacdn.com/gn_img/albums/01A3mar3NQ/A3moNqGzbN/size_m.jpg",
                        "https://i.scdn.co/image/ab67616d0000b2736119682d5b9f8b1ba6919ed9",
                        "https://thisis-images.spotifycdn.com/37i9dQZF1DZ06evO2YqUuI-default.jpg",
                        "https://m.media-amazon.com/images/I/61fnfjsJq9L._UF1000,1000_QL80_.jpg",
                        "https://i.scdn.co/image/ab67616d0000b273ba5db46f4b838ef6027e6f96",
                        "https://i.scdn.co/image/ab67616d0000b273cc761ba55b0e7abad4539abe",
                        "https://i.scdn.co/image/ab67616d0000b27307ef76001ec0e627d79a6dd1",
                        "https://m.media-amazon.com/images/I/91oHxdnKWhL._AC_UF1000,1000_QL80_.jpg",
                    )
                resource.data?.data?.mapIndexed { index, song ->
                    song.cover = listOfCovers[index]
                }
                resource
            }
        }
    }

    fun setCurrentSong(song: Song)  {
        _currentlyPlaying.value = Resource.Success(song)
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
