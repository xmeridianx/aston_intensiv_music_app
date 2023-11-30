package com.example.musicapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import com.example.musicapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var musicService: MusicService
    private var musicBound = false
    private lateinit var seekBar: SeekBar

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder

            musicService = binder.getService()
            musicBound = true

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        musicService.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            musicBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        seekBar = binding.seekBar
        val intent = Intent(this, MusicService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)


        binding.seekBar.progress = 0

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

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, pos: Int, changed: Boolean) {
                if (changed) {
                    if (musicBound) {
                        musicService.seekTo(pos)
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        if (musicBound) {
            unbindService(connection)
            musicBound = false
        }
    }
}