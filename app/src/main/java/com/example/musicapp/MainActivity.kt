package com.example.musicapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import com.example.musicapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var musicService: MusicService
    private var musicBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder

            musicService = binder.getService()
            musicBound = true

        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            musicBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = Intent(this, MusicService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)


        binding.buttonPlay.setOnClickListener {
            if (musicBound) {
                musicService.play()
            }
        }
        binding.buttonPause.setOnClickListener {
            if (musicBound) {
                musicService.pause()
            }
        }

        binding.buttonNext.setOnClickListener {
            musicService.playNextTrack()
        }

        binding.buttonPrevious.setOnClickListener {
            musicService.playPreviousTrack()
        }


    }
    override fun onDestroy() {
        super.onDestroy()
        if (musicBound) {
            unbindService(connection)
            musicBound = false
        }
    }
}