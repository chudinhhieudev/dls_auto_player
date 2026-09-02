package com.dlsautoplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BotForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)
        
        // TODO: Bắt đầu quá trình Screen Capture (MediaProjection) ở đây
        
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // TODO: Dừng Screen Capture
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "bot_service_channel",
            "Bot Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "bot_service_channel")
            .setContentTitle("DLS Auto Player")
            .setContentText("Bot is running...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }
}
