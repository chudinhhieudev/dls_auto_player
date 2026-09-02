package com.dlsautoplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dlsautoplayer.capture.ScreenCaptureManager
import com.dlsautoplayer.vision.VisionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BotForegroundService : Service() {

    companion object {
        private const val TAG = "BotForegroundService"
    }

    private var screenCaptureManager: ScreenCaptureManager? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private val visionEngine = VisionEngine()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)
        
        val resultCode = intent?.getIntExtra("RESULT_CODE", 0) ?: 0
        @Suppress("DEPRECATION")
        val data = intent?.getParcelableExtra<Intent>("DATA")

        if (resultCode != 0 && data != null) {
            startScreenCapture(resultCode, data)
        }
        
        return START_NOT_STICKY
    }

    private fun startScreenCapture(resultCode: Int, data: Intent) {
        screenCaptureManager = ScreenCaptureManager(this, resultCode, data)
        screenCaptureManager?.startCapture()

        serviceScope.launch {
            val channel = screenCaptureManager?.frameChannel
            if (channel != null) {
                var frameCount = 0
                var lastTime = System.currentTimeMillis()
                
                for (image in channel) {
                    frameCount++
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTime >= 1000) {
                        Log.d(TAG, "FPS: $frameCount")
                        frameCount = 0
                        lastTime = currentTime
                    }
                    
                    try {
                        val detections = visionEngine.processFrame(image)
                        for (det in detections) {
                            Log.d(TAG, "Vision: Detected ${det.label} at (${det.x}, ${det.y}) conf=${det.confidence}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing frame", e)
                    } finally {
                        try {
                            image.close()
                        } catch (e: Exception) {
                            // Bỏ qua lỗi đóng
                        }
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        screenCaptureManager?.stopCapture()
        serviceScope.cancel()
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
