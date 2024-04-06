package com.example.samespace

import android.app.Application
import androidx.media3.exoplayer.ExoPlayer

class MyApp : Application() {
    val exoPlayer by lazy {
        ExoPlayer.Builder(baseContext).build()
    }
}
