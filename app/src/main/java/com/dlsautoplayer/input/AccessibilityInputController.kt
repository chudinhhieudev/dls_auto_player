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
        // Nút A (Sút/Xoạc)
        val shootX = 0.85f
        val shootY = 0.85f
        tap(shootX, shootY)
    }

    override fun pressB() {
        // Nút B (Tăng toạ độ X lên 0.80f theo feedback)
        val bX = 0.85f
        val bY = 0.855f
        tap(bX, bY)
    }

    override fun pressC() {
        // Nút C (Chuyền bổng/Đổi người)
        val cX = 0.85f
        val cY = 0.60f
        tap(cX, cY)
    }

    override fun moveAndAction(
        startX: Float, startY: Float, endX: Float, endY: Float,
        actionType: String?
    ) {
        val displayMetrics = accessibilityService.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val builder = GestureDescription.Builder()

        // 1. Vuốt Joystick (Giữ 400ms để cầu thủ chạy liên tục)
        val swipePath = Path().apply {
            moveTo(startX * screenWidth, startY * screenHeight)
            lineTo(endX * screenWidth, endY * screenHeight)
        }
        builder.addStroke(GestureDescription.StrokeDescription(swipePath, 0, 400))

        // 2. Bấm nút đồng thời
        if (actionType != null) {
            val tapPath = Path()
            when (actionType) {
                "A" -> tapPath.moveTo(0.85f * screenWidth, 0.85f * screenHeight)
                "B" -> tapPath.moveTo(0.85f * screenWidth, 0.855f * screenHeight) // Theo toạ độ bạn sửa
                "C" -> tapPath.moveTo(0.85f * screenWidth, 0.60f * screenHeight)
            }
            // Delay 50ms để game nhận Joystick trước, giữ 50ms
            builder.addStroke(GestureDescription.StrokeDescription(tapPath, 50, 50))
        }

        accessibilityService.dispatchGesture(builder.build(), null, null)
    }
}
