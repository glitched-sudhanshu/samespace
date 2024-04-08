package com.example.samespace.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.ViewModelProvider
import com.example.samespace.R
import com.example.samespace.databinding.ActivityMainBinding
import com.example.samespace.exoplayer.MusicService
import com.example.samespace.network.Client
import com.example.samespace.repo.SongsListRepo
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT)
                    .show()
                val serviceIntent = Intent(this, MusicService::class.java)
                startService(serviceIntent)
            } else {
                Toast.makeText(
                    this,
                    "${getString(R.string.app_name)} can't play songs without Notification permission",
                    Toast.LENGTH_LONG,
                ).show()
                Snackbar.make(
                    binding.container,
                    String.format(
                        String.format(
                            getString(R.string.txt_error_post_notification),
                            getString(R.string.app_name),
                        ),
                    ),
                    Snackbar.LENGTH_INDEFINITE,
                ).setAction(getString(R.string.goto_settings)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val settingsIntent: Intent =
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        startActivity(settingsIntent)
                    }
                }.show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = SongsListRepo(Client.api)
        viewModel =
            ViewModelProvider(this, MainViewModelFactory(repository))[MainViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        askNotificationPermission()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PERMISSION_GRANTED
            ) {
                val serviceIntent = Intent(this, MusicService::class.java)
                startService(serviceIntent)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val serviceIntent = Intent(this, MusicService::class.java)
        stopService(serviceIntent)
    }
}
