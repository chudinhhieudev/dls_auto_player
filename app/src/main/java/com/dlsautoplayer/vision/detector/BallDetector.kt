package com.dlsautoplayer.vision.detector

import com.dlsautoplayer.vision.DetectionResult
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class BallDetector {

    /**
     * Thuật toán: Nhận diện quả bóng với đa dạng dải màu trong DLS:
     * - Bóng Cam / Vàng
     * - Bóng Hồng / Đỏ (Pink / Salmon)
     * - Bóng Trắng sáng
     */
    fun detect(mat: Mat): DetectionResult? {
        val hsvMat = Mat()
        Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.cvtColor(hsvMat, hsvMat, Imgproc.COLOR_RGB2HSV)

        // 1. Dải màu Cam / Vàng (Orange / Yellow)
        val orangeMask = Mat()
        Core.inRange(hsvMat, Scalar(10.0, 50.0, 100.0), Scalar(35.0, 255.0, 255.0), orangeMask)

        // 2. Dải màu Hồng / Đỏ (Pink / Red ball)
        val pinkMask1 = Mat()
        val pinkMask2 = Mat()
        val pinkMask = Mat()
        Core.inRange(hsvMat, Scalar(0.0, 35.0, 120.0), Scalar(10.0, 255.0, 255.0), pinkMask1)
        Core.inRange(hsvMat, Scalar(160.0, 35.0, 120.0), Scalar(180.0, 255.0, 255.0), pinkMask2)
        Core.bitwise_or(pinkMask1, pinkMask2, pinkMask)

        // 3. Dải màu Trắng sáng (White ball)
        val whiteMask = Mat()
        Core.inRange(hsvMat, Scalar(0.0, 0.0, 215.0), Scalar(180.0, 40.0, 255.0), whiteMask)

        // Gộp tất cả các mask
        val combinedMask = Mat()
        Core.bitwise_or(orangeMask, pinkMask, combinedMask)
        Core.bitwise_or(combinedMask, whiteMask, combinedMask)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(combinedMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var bestMatch: DetectionResult? = null
        var maxArea = 0.0

        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            // Quả bóng thực tế rất nhỏ trên sân (10 đến 300 pixel)
            if (area > 10 && area < 350) {
                val boundingRect = Imgproc.boundingRect(contour)
                val centerY = boundingRect.y + boundingRect.height / 2.0f
                val centerX = boundingRect.x + boundingRect.width / 2.0f
                
                val normY = centerY / mat.height()
                val normX = centerX / mat.width()

                // Bỏ qua vùng UI trên cùng (Scoreboard, Pause) và dưới cùng (Radar)
                if (normY < 0.25f || normY > 0.88f) continue
                if (normX < 0.05f || normX > 0.95f) continue

                // Quả bóng phải có tỷ lệ xấp xỉ hình tròn (width / height gần 1.0)
                val aspectRatio = boundingRect.width.toFloat() / boundingRect.height.toFloat()
                if (aspectRatio < 0.6f || aspectRatio > 1.6f) continue
                
                if (area > maxArea) {
                    maxArea = area
                    
                    bestMatch = DetectionResult(
                        label = "Ball",
                        x = normX,
                        y = normY,
                        width = boundingRect.width.toFloat() / mat.width(),
                        height = boundingRect.height.toFloat() / mat.height(),
                        confidence = 0.85f
                    )
                }
            }
        }

        hsvMat.release()
        orangeMask.release()
        pinkMask1.release()
        pinkMask2.release()
        pinkMask.release()
        whiteMask.release()
        combinedMask.release()
        hierarchy.release()
        return bestMatch
    }
}
