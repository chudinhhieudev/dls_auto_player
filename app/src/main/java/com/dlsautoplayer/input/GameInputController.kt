package com.dlsautoplayer.input

/**
 * Tầng trừu tượng xử lý input để điều khiển game.
 * Tất cả toạ độ (x, y) đều là toạ độ chuẩn hoá từ 0.0 đến 1.0, 
 * sẽ được dịch sang pixel dựa vào kích thước màn hình.
 */
interface GameInputController {
    /**
     * Chạm vào màn hình tại một điểm (chuẩn hoá 0.0 - 1.0)
     */
    fun tap(x: Float, y: Float)

    /**
     * Vuốt trên màn hình từ điểm bắt đầu đến điểm kết thúc trong một khoảng thời gian
     */
    fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long)
    /**
     * Tự động bấm nút SÚT (Nút A)
     */
    fun shoot()
}
