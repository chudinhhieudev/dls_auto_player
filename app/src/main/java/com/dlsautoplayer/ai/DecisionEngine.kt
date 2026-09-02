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
        private const val JOYSTICK_CENTER_X = 0.19f
        private const val JOYSTICK_CENTER_Y = 0.66f
        private const val JOYSTICK_RADIUS = 0.10f // Chiều dài nét vuốt
    }

    private var lastSwipeTime: Long = 0

    /**
     * Dựa vào GameState để ra lệnh cho InputController
     */
    fun decideAndExecute(state: GameState, inputController: GameInputController?) {
        if (inputController == null) return

        val currentTime = System.currentTimeMillis()
        // Đợi 400ms để gesture hoàn thành mới gửi tiếp
        if (currentTime - lastSwipeTime < 400) {
            return
        }

        val player = state.playerPosition
        val ball = state.ballPosition
        val scoreboard = state.scoreboardPosition

        // Không có scoreboard -> Chưa vào trận
        if (scoreboard == null) {
            Log.d(TAG, "Not in game (Scoreboard missing), pausing...")
            return
        }

        // Nếu chưa tìm thấy cầu thủ điều khiển -> Tạm dừng
        if (player == null) {
            return
        }

        // 1. TRƯỜNG HỢP CÓ BÓNG HOẶC MẤT DẤU BÓNG: Mặc định TẤN CÔNG THẲNG LÊN TRÊN (Khung thành đối phương)
        if (state.hasPossession() || ball == null) {
            var attackAngle = -Math.PI / 2 // -90 độ: Vuốt thẳng đứng lên trên mép màn hình
            var action: String? = null
            
            // --- Phase 8: Smart Passing (Chỉ né khi bị đối thủ áp sát cực gần phía trước mặt) ---
            var isBlocked = false
            for (enemy in state.opponents) {
                // Đối thủ chắn ngay sát trước mũi (trong khoảng 12% chiều cao phía trên)
                if (enemy.y < player.y && (player.y - enemy.y) < 0.12f && Math.abs(enemy.x - player.x) < 0.08f) {
                    isBlocked = true
                    break
                }
            }

            if (isBlocked) {
                // Tìm đồng đội thoáng ở phía trên
                val bestTeammate = state.teammates.firstOrNull { it.y < player.y && Math.abs(it.x - player.x) > 0.1f }
                if (bestTeammate != null) {
                    val dx = bestTeammate.x - player.x
                    val dy = bestTeammate.y - player.y
                    attackAngle = atan2(dy.toDouble(), dx.toDouble())
                    Log.d(TAG, "Bị đối thủ áp sát! Chuyền cho đồng đội ở góc: $attackAngle")
                    action = "B"
                }
            }
            
            val endX = JOYSTICK_CENTER_X + JOYSTICK_RADIUS * cos(attackAngle).toFloat()
            val endY = JOYSTICK_CENTER_Y + JOYSTICK_RADIUS * sin(attackAngle).toFloat()
            
            // Nếu chạy tới sát mép trên màn hình (Vùng cấm địa) -> SÚT (Nút A)
            if (player.y < 0.28f) {
                Log.d(TAG, "Đã vào vùng cấm địa đối phương! Sút ngay!")
                action = "A"
            }
            
            Log.d(TAG, "Tấn công lên trên: start=($JOYSTICK_CENTER_X, $JOYSTICK_CENTER_Y), end=($endX, $endY), action=$action")
            inputController.moveAndAction(JOYSTICK_CENTER_X, JOYSTICK_CENTER_Y, endX, endY, action)
            lastSwipeTime = currentTime
            return
        }

        // 2. TRƯỜNG HỢP CHƯA CÓ BÓNG VÀ ĐÃ XÁC ĐỊNH ĐƯỢC VỊ TRÍ BÓNG: Đuổi theo bóng & Phòng ngự
        val footY = player.y + 0.12f
        val dx = ball.x - player.x
        val dy = ball.y - footY
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        if (distance < 0.15f) {
            return
        }

        // Góc hướng tới bóng
        val angle = atan2(dy.toDouble(), dx.toDouble()).toFloat()
        
        val endX = JOYSTICK_CENTER_X + JOYSTICK_RADIUS * cos(angle.toDouble()).toFloat()
        val endY = JOYSTICK_CENTER_Y + JOYSTICK_RADIUS * sin(angle.toDouble()).toFloat()

        Log.d(TAG, "Đuổi theo bóng & Áp sát: Góc=$angle, Khoảng cách=$distance")
        inputController.moveAndAction(JOYSTICK_CENTER_X, JOYSTICK_CENTER_Y, endX, endY, "B")
        
        lastSwipeTime = currentTime
    }
}
