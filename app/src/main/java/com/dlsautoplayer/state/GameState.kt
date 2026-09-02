package com.dlsautoplayer.state

import com.dlsautoplayer.vision.DetectionResult

/**
 * GameState lưu trữ trạng thái hiện tại của ván game.
 * Bot sẽ sử dụng dữ liệu này để đưa ra quyết định.
 */
data class GameState(
    var ballPosition: DetectionResult? = null,
    var playerPosition: DetectionResult? = null,
    var scoreboardPosition: DetectionResult? = null,
    var teammates: List<DetectionResult> = emptyList(),
    var goalkeeper: DetectionResult? = null,
    var lastUpdateTime: Long = 0
) {
    /**
     * Cập nhật trạng thái mới nhất từ Vision Engine
     */
    fun update(
        ball: DetectionResult?, 
        player: DetectionResult?, 
        scoreboard: DetectionResult?,
        teammates: List<DetectionResult>,
        goalkeeper: DetectionResult?
    ) {
        if (ball != null) this.ballPosition = ball
        if (player != null) this.playerPosition = player
        this.scoreboardPosition = scoreboard // Cho phép null để reset
        this.teammates = teammates
        this.goalkeeper = goalkeeper
        this.lastUpdateTime = System.currentTimeMillis()
    }

    /**
     * Kiểm tra xem Bot có đang kiểm soát bóng không.
     * Dựa trên khoảng cách giữa cầu thủ đang điều khiển và bóng.
     */
    fun hasPossession(): Boolean {
        val p = playerPosition ?: return false
        val b = ballPosition ?: return false
        val dx = p.x - b.x
        val dy = p.y - b.y
        val dist = Math.sqrt((dx * dx + dy * dy).toDouble())
        return dist < 0.05 // Tuỳ chỉnh threshold
    }
}
