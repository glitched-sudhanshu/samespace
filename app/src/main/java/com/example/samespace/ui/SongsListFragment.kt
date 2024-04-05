package com.example.samespace.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.samespace.databinding.FragmentSongsListBinding
import com.example.samespace.models.Resource
import com.example.samespace.models.SongsList

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
                // show loading
                Log.d("api call", "onCreate: ")
            }

            is Resource.Success -> {
                if (response.data?.data != null) {
                    songsListAdapter.saveData(response.data.data)
                } else {
                    // show empty list
                }
            }

            is Resource.Error -> {
                // show error
                Log.d("api call", "onCreate: ${response.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
