package com.dlsautoplayer.vision.detector

import com.dlsautoplayer.vision.DetectionResult
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class JoystickDetector {

    /**
     * Nhận diện cụm Joystick ở góc dưới bên trái màn hình.
     * Dùng để xác nhận chắc chắn Bot đang ở trong trận đấu.
     */
    fun detect(mat: Mat): DetectionResult? {
        val hsvMat = Mat()
        Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.cvtColor(hsvMat, hsvMat, Imgproc.COLOR_RGB2HSV)

        // Joystick thường là hình tròn mờ màu trắng/sáng
        val lowerWhite = Scalar(0.0, 0.0, 150.0)
        val upperWhite = Scalar(180.0, 60.0, 255.0)

        val mask = Mat()
        Core.inRange(hsvMat, lowerWhite, upperWhite, mask)

        // Chỉ tìm trong khu vực góc dưới bên trái (x: 0 -> 0.4, y: 0.5 -> 1.0)
        val roiRect = Rect(0, (mat.height() * 0.5).toInt(), (mat.width() * 0.4).toInt(), (mat.height() * 0.5).toInt())
        val maskROI = mask.submat(roiRect)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(maskROI, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var bestMatch: DetectionResult? = null
        var maxArea = 0.0

        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            // Joystick có diện tích khá lớn
            if (area > 300 && area < 15000) {
                val boundingRect = Imgproc.boundingRect(contour)
                if (area > maxArea) {
                    maxArea = area
                    
                    val centerX = boundingRect.x + roiRect.x + boundingRect.width / 2.0f
                    val centerY = boundingRect.y + roiRect.y + boundingRect.height / 2.0f
                    
                    bestMatch = DetectionResult(
                        label = "Joystick",
                        x = centerX.toFloat() / mat.width(),
                        y = centerY.toFloat() / mat.height(),
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
