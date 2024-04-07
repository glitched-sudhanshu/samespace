package com.example.samespace.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.samespace.databinding.FragmentSongsListBinding
import com.example.samespace.models.Resource
import com.example.samespace.models.Song
import com.example.samespace.models.SongsList
import com.example.samespace.shimmerBrush

class SongsListFragment(private val isTopTrack: Boolean) : Fragment() {
    private var _binding: FragmentSongsListBinding? = null
    private val binding get() = _binding!!
    private val songsListAdapter = SongsListAdapter()
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSongsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSongsList.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.rvSongsList.adapter = songsListAdapter
        songsListAdapter.setOnClickListener(
            object : SongsListAdapter.OnClickListener {
                override fun onClick(
                    position: Int,
                    song: Song,
                ) {
                    viewModel.setSongPosition(position = position, fromTopTrack = isTopTrack)
                    val fragment =
                        SongPlayerFragment(isTopTracks = isTopTrack, false)
                    fragment.show(requireActivity().supportFragmentManager, "SongPlayerFragment")
                }
            },
        )

        if (isTopTrack) {
            viewModel.topTrackSongsList.observe(viewLifecycleOwner) {
                handleSongResponse(it)
            }
        } else {
            viewModel.songsList.observe(viewLifecycleOwner) {
                handleSongResponse(it)
            }
        }
    }

    private fun handleSongResponse(response: Resource<SongsList>) {
        when (response) {
            is Resource.Loading -> {
                binding.isLoading = true
                binding.cvLoading.apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        SongsListShimmer()
                    }
                }
            }

            is Resource.Success -> {
                binding.isLoading = false
                binding.cvLoading.disposeComposition()
                if (response.data?.data != null) {
                    songsListAdapter.saveData(response.data.data)
                } else {
                    // show empty list
                }
            }

            is Resource.Error -> {
                binding.isLoading = false
                binding.cvLoading.disposeComposition()
                // show error
                Log.d("api call", "onCreate: ${response.message}")
            }
        }
    }

    @Composable
    fun SongsListShimmer() {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            repeat(5) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .padding(end = 10.dp)
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(shimmerBrush()),
                    )
                    Column {
                        Box(
                            modifier =
                                Modifier
                                    .padding(top = 10.dp, bottom = 20.dp)
                                    .width(100.dp)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(15))
                                    .background(shimmerBrush()),
                        )
                        Box(
                            modifier =
                                Modifier
                                    .width(80.dp)
                                    .height(15.dp)
                                    .clip(RoundedCornerShape(15))
                                    .background(shimmerBrush()),
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
