package com.example.samespace.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.samespace.databinding.ActivityMainBinding
import com.example.samespace.exoplayer.MusicService
import com.example.samespace.network.Client
import com.example.samespace.repo.SongsListRepo

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = SongsListRepo(Client.api)
        viewModel =
            ViewModelProvider(this, MainViewModelFactory(repository))[MainViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, MusicService::class.java)
        startService(serviceIntent)
    }

    override fun onStop() {
        super.onStop()
        val serviceIntent = Intent(this, MusicService::class.java)
        stopService(serviceIntent)
    }
}
