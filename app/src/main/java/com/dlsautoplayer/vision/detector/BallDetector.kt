package com.dlsautoplayer.vision.detector

import com.dlsautoplayer.vision.DetectionResult
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class BallDetector {

    /**
     * Thuật toán: Chuyển ảnh sang hệ màu HSV, lọc dải màu của quả bóng (ví dụ màu trắng/vàng sáng).
     * Sau đó tìm contour hình tròn lớn nhất / có diện tích phù hợp.
     */
    fun detect(mat: Mat): DetectionResult? {
        val hsvMat = Mat()
        Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.cvtColor(hsvMat, hsvMat, Imgproc.COLOR_RGB2HSV)

        // Cấu hình dải màu Cam (Orange) cho quả bóng
        // Trong OpenCV, Hue màu cam thường nằm ở khoảng 10 đến 25
        val lowerOrange = Scalar(5.0, 100.0, 100.0)
        val upperOrange = Scalar(25.0, 255.0, 255.0)

        val mask = Mat()
        Core.inRange(hsvMat, lowerOrange, upperOrange, mask)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var bestMatch: DetectionResult? = null
        var maxArea = 0.0

        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            // Lọc các đốm nhiễu (diện tích quá nhỏ) hoặc quá to
            if (area > 50 && area < 2000) {
                val boundingRect = Imgproc.boundingRect(contour)
                
                if (area > maxArea) {
                    maxArea = area
                    
                    val centerX = boundingRect.x + boundingRect.width / 2.0f
                    val centerY = boundingRect.y + boundingRect.height / 2.0f
                    
                    bestMatch = DetectionResult(
                        label = "Ball",
                        x = centerX.toFloat() / mat.width(),
                        y = centerY.toFloat() / mat.height(),
                        width = boundingRect.width.toFloat() / mat.width(),
                        height = boundingRect.height.toFloat() / mat.height(),
                        confidence = 0.8f // Giả định confidence
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
