package com.dlsautoplayer.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dlsautoplayer.service.BotForegroundService

class MainActivity : ComponentActivity() {

    private val startMediaProjection = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                // Khởi động BotForegroundService với dữ liệu xin quyền
                val serviceIntent = Intent(this, BotForegroundService::class.java).apply {
                    putExtra("RESULT_CODE", result.resultCode)
                    putExtra("DATA", data)
                }
                startForegroundService(serviceIntent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onStartBot = { requestScreenCapture() },
                        onStopBot = { stopBotService() }
                    )
                }
            }
        }
    }

    private fun requestScreenCapture() {
        android.util.Log.d("BotDebug", "requestScreenCapture called")
        try {
            val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val intent = mediaProjectionManager.createScreenCaptureIntent()
            android.util.Log.d("BotDebug", "Intent created: $intent")
            startMediaProjection.launch(intent)
            android.util.Log.d("BotDebug", "Intent launched")
        } catch (e: Exception) {
            android.util.Log.e("BotDebug", "Error launching MediaProjection", e)
        }
    }

    private fun stopBotService() {
        val serviceIntent = Intent(this, BotForegroundService::class.java)
        stopService(serviceIntent)
    }
}
