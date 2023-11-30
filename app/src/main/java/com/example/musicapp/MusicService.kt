package com.example.musicapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class MusicService: Service() {
    private lateinit var mediaPlayer: MediaPlayer
    private val binder = MusicBinder()
    private var trackList = listOf(R.raw.music1, R.raw.music2)
    private var currentTrackIndex = 0
    private lateinit var seekBar: SeekBar

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, trackList[currentTrackIndex])
        mediaPlayer.setOnCompletionListener {
            playNextTrack()
            playPreviousTrack()
        }
    }

    private val NOTIFICATION_ID = 1

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "PREVIOUS" -> playPreviousTrack()
            "PLAY_PAUSE" -> play()
            "NEXT" -> playNextTrack()
        }
        updateNotification()
        return START_STICKY
    }

    private fun createNotification(): Notification? {

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("music_channel", "Music Channel")
            } else {
                ""
            }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.music_logo)
            .setContentTitle("Сейчас играет")
            .setContentText("Calvin Harris feat. Florence Welch - Sweet Nothing")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.baseline_arrow_back_ios_24, "Пред.", null)
            .addAction(R.drawable.baseline_pause_24, "Пауза", null)
            .addAction(R.drawable.baseline_arrow_forward_ios_24, "След.", null)

        return notificationBuilder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        return channelId
    }

    fun playNextTrack() {
        currentTrackIndex = (currentTrackIndex + 1) % trackList.size
        mediaPlayer.release()
        mediaPlayer = MediaPlayer.create(this, trackList[currentTrackIndex])
        //seekBar.max = mediaPlayer.duration
        play()
    }

    fun playPreviousTrack() {
        if (currentTrackIndex > 0) {
            currentTrackIndex--
        } else {
            currentTrackIndex = trackList.size - 1
        }
        mediaPlayer.reset()
        mediaPlayer.setDataSource(applicationContext, Uri.parse("android.resource://${packageName}/${trackList[currentTrackIndex]}"))
        mediaPlayer.prepare()
        mediaPlayer.start()
        //seekBar.max = mediaPlayer.duration
        updateNotification()
    }

    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
    }

    private fun updateNotification() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    fun play() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            updateNotification()
        }
    }

    fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            updateNotification()
        }
    }

    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }
}