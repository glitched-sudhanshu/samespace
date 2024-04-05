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
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.samespace.databinding.ActivityMainBinding
import com.example.samespace.exoplayer.MusicService
import com.example.samespace.models.Resource
import com.example.samespace.network.Client
import com.example.samespace.repo.SongsListRepo
import com.google.android.material.bottomnavigation.BottomNavigationView

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
            ViewModelProvider(this, MainViewModelFactory(repository))
                .get(MainViewModel::class.java)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration =
            AppBarConfiguration(
                setOf(
                    R.id.navigation_for_you,
                    R.id.navigation_top_tracks,
                ),
            )

        viewModel.songsList.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    Log.d("api call", "onCreate: ")
                }

                is Resource.Success -> {
                    it.data?.data?.get(0)?.let { it1 -> musicService?.setSong(it1.url) }
                }

                is Resource.Error -> {
                    Log.d("api call", "onCreate: ${it.message}")
                }
            }
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
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
