package com.dlsautoplayer.ai

import android.util.Log
import com.dlsautoplayer.input.GameInputController
import com.dlsautoplayer.state.GameState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class DecisionEngine {
    
    companion object {
        private const val TAG = "DecisionEngine"
        // Toạ độ khu vực Joystick (Dựa trên ảnh: X~305.5, Y~478.0 trên màn hình ~1600x720)
        // Tâm chính xác của vòng tròn
        private const val JOYSTICK_CENTER_X = 0.19f
        private const val JOYSTICK_CENTER_Y = 0.66f
        private const val JOYSTICK_RADIUS = 0.1f // Chiều dài nét vuốt
    }

    private var lastSwipeTime: Long = 0

    /**
     * Dựa vào GameState để ra lệnh cho InputController
     */
    fun decideAndExecute(state: GameState, inputController: GameInputController?) {
        if (inputController == null) {
            // Chưa cấp quyền Accessibility hoặc service chưa chạy
            return
        }

        val player = state.playerPosition
        val ball = state.ballPosition
        val scoreboard = state.scoreboardPosition

        if (scoreboard == null) {
            Log.d(TAG, "Not in game (Scoreboard missing), pausing...")
            return
        }

        if (player == null || ball == null) {
            // Thiếu dữ liệu, tạm dừng
            return
        }

        val currentTime = System.currentTimeMillis()
        // Tránh ra lệnh liên tục quá nhanh gây nghẽn AccessibilityService
        if (currentTime - lastSwipeTime < 200) {
            return
        }

        if (state.hasPossession()) {
            // Đã có bóng -> Tấn công hướng lên trên (góc -90 độ hoặc -pi/2)
            Log.d(TAG, "Đã có bóng! Tấn công thẳng lên trên.")
            
            // Góc tấn công là -pi/2 (radian) hướng lên trên màn hình (vì toạ độ y từ trên xuống dưới)
            val attackAngle = -Math.PI / 2
            
            val endX = JOYSTICK_CENTER_X + JOYSTICK_RADIUS * cos(attackAngle).toFloat()
            val endY = JOYSTICK_CENTER_Y + JOYSTICK_RADIUS * sin(attackAngle).toFloat()
            
            inputController.swipe(JOYSTICK_CENTER_X, JOYSTICK_CENTER_Y, endX, endY, 150)
            lastSwipeTime = currentTime
            
            // Nếu chạy tới sát mép trên màn hình (Vùng cấm địa) -> SÚT
            if (player.y < 0.25f) {
                Log.d(TAG, "Vào vùng sút! Sút ngay!")
                inputController.shoot()
            }
            return
        }

        // Chưa có bóng -> Đuổi theo bóng
        // Tính Vector hướng từ cầu thủ -> Bóng
        val dx = ball.x - player.x
        val dy = ball.y - player.y
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        if (distance < 0.05f) {
            // Không chạy code này nữa vì hasPossession đã bắt
            return
        }

        // Góc radian
        val angle = atan2(dy.toDouble(), dx.toDouble()).toFloat()
        
        // Tính toạ độ kéo thả của Joystick trên màn hình
        val endX = JOYSTICK_CENTER_X + JOYSTICK_RADIUS * cos(angle.toDouble()).toFloat()
        val endY = JOYSTICK_CENTER_Y + JOYSTICK_RADIUS * sin(angle.toDouble()).toFloat()

        Log.d(TAG, "Chạy tới bóng: Góc=$angle, Khoảng cách=$distance")
        inputController.swipe(JOYSTICK_CENTER_X, JOYSTICK_CENTER_Y, endX, endY, 150)
        
        lastSwipeTime = currentTime
    }
}
