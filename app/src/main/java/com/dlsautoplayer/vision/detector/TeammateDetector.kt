package com.dlsautoplayer.vision.detector

import com.dlsautoplayer.vision.DetectionResult
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class TeammateDetector {
    fun detect(mat: Mat): List<DetectionResult> {
        val hsvMat = Mat()
        Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.cvtColor(hsvMat, hsvMat, Imgproc.COLOR_RGB2HSV)

        // Màu đen có độ sáng (Value) rất thấp
        val lowerBlack = Scalar(0.0, 0.0, 0.0)
        val upperBlack = Scalar(180.0, 255.0, 50.0)

        val mask = Mat()
        Core.inRange(hsvMat, lowerBlack, upperBlack, mask)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        val results = mutableListOf<DetectionResult>()
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > 50 && area < 2000) { 
                val boundingRect = Imgproc.boundingRect(contour)
                val centerX = boundingRect.x + boundingRect.width / 2.0f
                val centerY = boundingRect.y + boundingRect.height / 2.0f
                
                results.add(DetectionResult(
                    label = "Teammate",
                    x = centerX / mat.width(),
                    y = centerY / mat.height(),
                    width = boundingRect.width.toFloat() / mat.width(),
                    height = boundingRect.height.toFloat() / mat.height(),
                    confidence = 0.8f
                ))
            }
        }

        hsvMat.release()
        mask.release()
        hierarchy.release()
        return results
    }
}
