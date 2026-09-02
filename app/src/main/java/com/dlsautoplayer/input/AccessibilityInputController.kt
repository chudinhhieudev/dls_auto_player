package com.dlsautoplayer.input

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log

class AccessibilityInputController(
    private val accessibilityService: AccessibilityService
) : GameInputController {

    companion object {
        private const val TAG = "AccessibilityInput"
    }

    override fun tap(x: Float, y: Float) {
        val displayMetrics = accessibilityService.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        
        val realX = x * screenWidth
        val realY = y * screenHeight
        
        Log.d(TAG, "Tap at ($realX, $realY) [Norm: $x, $y]")
        
        val path = Path().apply {
            moveTo(realX, realY)
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
            
        accessibilityService.dispatchGesture(gesture, null, null)
    }

    override fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long) {
        val displayMetrics = accessibilityService.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        
        val realStartX = startX * screenWidth
        val realStartY = startY * screenHeight
        val realEndX = endX * screenWidth
        val realEndY = endY * screenHeight
        
        Log.d(TAG, "Swipe from ($realStartX, $realStartY) to ($realEndX, $realEndY)")

        val path = Path().apply {
            moveTo(realStartX, realStartY)
            lineTo(realEndX, realEndY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
            .build()

        accessibilityService.dispatchGesture(gesture, null, null)
    }

    override fun shoot() {
        // Nút A (Sút) thường nằm ở góc dưới bên phải. Bạn có thể cần tinh chỉnh toạ độ này.
        val shootX = 0.85f
        val shootY = 0.85f
        tap(shootX, shootY)
    }
}
