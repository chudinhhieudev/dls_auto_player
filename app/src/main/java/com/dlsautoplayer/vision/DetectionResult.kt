package com.dlsautoplayer.vision

/**
 * Lưu trữ kết quả nhận diện của một đối tượng trên màn hình.
 * Toạ độ trả về là toạ độ chuẩn hoá (0.0 đến 1.0)
 */
data class DetectionResult(
    val label: String,
    val x: Float, // Toạ độ tâm x (chuẩn hoá)
    val y: Float, // Toạ độ tâm y (chuẩn hoá)
    val width: Float, // Chiều rộng (chuẩn hoá)
    val height: Float, // Chiều cao (chuẩn hoá)
    val confidence: Float // Độ tin cậy (0.0 đến 1.0)
)
