package com.dlsautoplayer.vision.detector

import com.dlsautoplayer.vision.DetectionResult
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class PlayerDetector {

    /**
     * Thuật toán: Nhận diện mũi tên trên đầu cầu thủ đang được điều khiển.
     * Thường mũi tên có màu đỏ/xanh đặc trưng. Ta dùng HSV Color Filtering.
     */
    fun detect(mat: Mat): DetectionResult? {
        val hsvMat = Mat()
        Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.cvtColor(hsvMat, hsvMat, Imgproc.COLOR_RGB2HSV)

        // Cấu hình dải màu cho mũi tên/thanh thể lực (Xanh lá đến Vàng)
        // Màu Vàng (Hue ~ 25-35) đến Xanh lá (Hue ~ 35-85)
        val lowerGreenYellow = Scalar(20.0, 80.0, 80.0)
        val upperGreenYellow = Scalar(85.0, 255.0, 255.0)
        
        val mask = Mat()
        Core.inRange(hsvMat, lowerGreenYellow, upperGreenYellow, mask)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var bestMatch: DetectionResult? = null
        var maxArea = 0.0

        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            // Lọc theo diện tích của mũi tên
            if (area > 30 && area < 1500) {
                val boundingRect = Imgproc.boundingRect(contour)
                
                if (area > maxArea) {
                    maxArea = area
                    
                    val centerX = boundingRect.x + boundingRect.width / 2.0f
                    val centerY = boundingRect.y + boundingRect.height / 2.0f
                    
                    // Toạ độ thực của cầu thủ nằm ngay bên dưới mũi tên một chút
                    val playerY = centerY + 50 // Tuỳ chỉnh sau theo game thực tế
                    
                    bestMatch = DetectionResult(
                        label = "ControlledPlayer",
                        x = centerX.toFloat() / mat.width(),
                        y = playerY.toFloat() / mat.height(),
                        width = boundingRect.width.toFloat() / mat.width(),
                        height = boundingRect.height.toFloat() / mat.height(),
                        confidence = 0.8f
                    )
                }
            }
        }

        hsvMat.release()
        mask.release()
        hierarchy.release()
        return bestMatch
    }
}
