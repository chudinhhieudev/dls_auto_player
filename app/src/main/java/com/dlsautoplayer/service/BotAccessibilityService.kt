package com.dlsautoplayer.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.view.WindowManager
import com.dlsautoplayer.input.AccessibilityInputController
import com.dlsautoplayer.input.GameInputController

class BotAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "BotAccessibility"
        var inputController: GameInputController? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility Service Connected")

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        inputController = AccessibilityInputController(
            accessibilityService = this,
            screenWidth = metrics.widthPixels,
            screenHeight = metrics.heightPixels
        )
        Log.d(TAG, "Input Controller initialized: ${metrics.widthPixels}x${metrics.heightPixels}")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Không xử lý event từ UI vì bot hoạt động dựa trên screen capture
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Accessibility Service Destroyed")
        inputController = null
    }
}
