package com.dlsautoplayer.vision.detector

import com.dlsautoplayer.vision.DetectionResult
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class GoalkeeperDetector {
    fun detect(mat: Mat): DetectionResult? {
        val hsvMat = Mat()
        Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.cvtColor(hsvMat, hsvMat, Imgproc.COLOR_RGB2HSV)

        // Màu vàng (Yellow)
        val lowerYellow = Scalar(20.0, 100.0, 100.0)
        val upperYellow = Scalar(40.0, 255.0, 255.0)

        val mask = Mat()
        Core.inRange(hsvMat, lowerYellow, upperYellow, mask)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var bestMatch: DetectionResult? = null
        var maxArea = 0.0

        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > 50 && area < 2000) {
                if (area > maxArea) {
                    maxArea = area
                    val boundingRect = Imgproc.boundingRect(contour)
                    val centerX = boundingRect.x + boundingRect.width / 2.0f
                    val centerY = boundingRect.y + boundingRect.height / 2.0f
                    
                    bestMatch = DetectionResult(
                        label = "Goalkeeper",
                        x = centerX / mat.width(),
                        y = centerY / mat.height(),
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
