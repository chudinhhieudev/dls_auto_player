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
import com.dlsautoplayer.state.GameState
import com.dlsautoplayer.ai.DecisionEngine
import com.dlsautoplayer.service.BotAccessibilityService
import com.dlsautoplayer.vision.DetectionResult
import com.dlsautoplayer.presentation.FloatingUIManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import android.os.Handler
import android.os.Looper

class BotForegroundService : Service() {

    companion object {
        private const val TAG = "BotForegroundService"
    }

    private var screenCaptureManager: ScreenCaptureManager? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private val visionEngine = VisionEngine()
    private val gameState = GameState()
    private val decisionEngine = DecisionEngine()
    
    private var floatingUIManager: FloatingUIManager? = null
    private var isBotActive = false // Bắt đầu ở trạng thái Pause

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)
        
        // Khởi tạo Floating UI chạy trên Main Thread
        Handler(Looper.getMainLooper()).post {
            floatingUIManager = FloatingUIManager(this) { isActive ->
                isBotActive = isActive
                if (isActive) {
                    Log.d(TAG, "Bot Resumed")
                } else {
                    Log.d(TAG, "Bot Paused")
                    // Reset trạng thái
                    gameState.update(null, null, null, emptyList(), null)
                }
            }
            floatingUIManager?.show()
        }
        
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
                    
                    // Nếu Bot đang Pause thì bỏ qua không nhận diện gì cả
                    if (!isBotActive) {
                        try {
                            image.close()
                        } catch (e: Exception) {}
                        continue
                    }
                    
                    try {
                        val detections = visionEngine.processFrame(image)
                        
                        var ball: DetectionResult? = null
                        var player: DetectionResult? = null
                        var scoreboard: DetectionResult? = null
                        val teammates = mutableListOf<DetectionResult>()
                        var goalkeeper: DetectionResult? = null
                        
                        for (det in detections) {
                            if (det.label == "Ball") ball = det
                            if (det.label == "ControlledPlayer") player = det
                            if (det.label == "Scoreboard") scoreboard = det
                            if (det.label == "Teammate") teammates.add(det)
                            if (det.label == "Goalkeeper") goalkeeper = det
                        }
                        
                        // Cập nhật Game State
                        gameState.update(ball, player, scoreboard, teammates, goalkeeper)
                        
                        // Kích hoạt Decision Engine
                        decisionEngine.decideAndExecute(gameState, BotAccessibilityService.inputController)
                        
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
        serviceScope.cancel()
        screenCaptureManager?.stopCapture()
        
        Handler(Looper.getMainLooper()).post {
            floatingUIManager?.hide()
            floatingUIManager = null
        }
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
            .setContentText("Bot is capturing screen...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }
}
