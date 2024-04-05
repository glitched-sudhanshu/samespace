package com.example.samespace

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.samespace.databinding.ActivityMainBinding
import com.example.samespace.exoplayer.MusicService
import com.example.samespace.models.Resource
import com.example.samespace.network.Client
import com.example.samespace.repo.SongsListRepo
import com.example.samespace.ui.MainViewPagerAdapter
import com.example.samespace.ui.forYou.ForYouFragment
import com.example.samespace.ui.topTracks.TopTracksFragment
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var musicService: MusicService? = null
    private var isBound = false
    private val connection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?,
            ) {
                val binder = service as MusicService.MusicBinder
                musicService = binder.getService()
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isBound = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = SongsListRepo(Client.api)
        viewModel =
            ViewModelProvider(this, MainViewModelFactory(repository))[MainViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewPager()

        viewModel.songsList.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    Log.d("api call", "onCreate: ")
                }

                is Resource.Success -> {
//                    it.data?.data?.get(0)?.let { it1 -> musicService?.setSong(it1.url) }
                }

                is Resource.Error -> {
                    Log.d("api call", "onCreate: ${it.message}")
                }
            }
        }
    }

    private fun setupViewPager() {
        val fragments = listOf(ForYouFragment(), TopTracksFragment())
        val adapter = MainViewPagerAdapter(fragments, supportFragmentManager, lifecycle)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "For You"
                1 -> tab.text = "Top Tracks"
            }
        }.attach()
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}
