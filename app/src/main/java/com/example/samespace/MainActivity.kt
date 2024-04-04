package com.example.samespace

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.samespace.databinding.ActivityMainBinding
import com.example.samespace.models.Resource
import com.example.samespace.network.Client
import com.example.samespace.repo.SongsListRepo
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

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
                    Log.d("api call", "onCreate: ${it.data}")
                }
                is Resource.Error -> {
                    Log.d("api call", "onCreate: ${it.message}")
                }
            }
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}
