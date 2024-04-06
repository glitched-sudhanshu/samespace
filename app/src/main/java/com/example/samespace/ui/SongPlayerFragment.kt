package com.example.samespace.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.samespace.R
import com.example.samespace.models.Resource
import com.example.samespace.models.SongsList

class SongPlayerFragment(val fromTopTracks: Boolean) : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Scaffold { internalPadding ->
                        val songPointer by viewModel.songPointer.observeAsState(0)
                        val songsList by if (fromTopTracks) {
                            viewModel.topTrackSongsList.observeAsState()
                        } else {
                            viewModel.songsList.observeAsState()
                        }

                        Column(modifier = Modifier.padding(internalPadding)) {
                            when (songsList) {
                                is Resource.Loading -> {
                                    Text(text = "Loading", color = Color.Black)
                                }

                                is Resource.Success<SongsList> -> {
                                    Text(text = songsList?.data?.data?.get(songPointer)?.name.toString(), color = Color.Black)
                                }

                                is Resource.Error -> {
                                    Text(text = "Error", color = Color.Black)
                                }

                                else -> {}
                            }
                            Text(
                                text = "next",
                                color = colorResource(id = R.color.black),
                                modifier = Modifier.clickable { viewModel.nextSong(fromTopTracks) },
                            )
                            Text(
                                text = "prev",
                                color = colorResource(id = R.color.black),
                                modifier = Modifier.clickable { viewModel.previousSong(fromTopTracks) },
                            )
                        }
                    }
                }
            }
        }
    }
}
