package com.dlsautoplayer.vision.detector

import com.dlsautoplayer.vision.DetectionResult
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class OpponentDetector {
    /**
     * Tìm đối thủ bằng phương pháp loại trừ:
     * Trừ đi Xanh lá (Sân), Đen (Đồng đội), Vàng (Thủ môn), Trắng (UI/Vạch vôi).
     * Những khối màu còn lại trên sân có khả năng cao là Đối thủ.
     */
    fun detect(mat: Mat): List<DetectionResult> {
        val hsvMat = Mat()
        Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.cvtColor(hsvMat, hsvMat, Imgproc.COLOR_RGB2HSV)

        val allMask = Mat(hsvMat.size(), org.opencv.core.CvType.CV_8UC1, Scalar(255.0))

        val greenMask = Mat()
        Core.inRange(hsvMat, Scalar(30.0, 40.0, 40.0), Scalar(85.0, 255.0, 255.0), greenMask)
        
        val blackMask = Mat()
        Core.inRange(hsvMat, Scalar(0.0, 0.0, 0.0), Scalar(180.0, 255.0, 50.0), blackMask)
        
        val yellowMask = Mat()
        Core.inRange(hsvMat, Scalar(20.0, 100.0, 100.0), Scalar(40.0, 255.0, 255.0), yellowMask)

        val whiteMask = Mat()
        Core.inRange(hsvMat, Scalar(0.0, 0.0, 200.0), Scalar(180.0, 40.0, 255.0), whiteMask)

        val enemyMask = Mat()
        Core.subtract(allMask, greenMask, enemyMask)
        Core.subtract(enemyMask, blackMask, enemyMask)
        Core.subtract(enemyMask, yellowMask, enemyMask)
        Core.subtract(enemyMask, whiteMask, enemyMask)

        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, org.opencv.core.Size(3.0, 3.0))
        Imgproc.morphologyEx(enemyMask, enemyMask, Imgproc.MORPH_OPEN, kernel)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(enemyMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        val results = mutableListOf<DetectionResult>()
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > 50 && area < 2000) { 
                val boundingRect = Imgproc.boundingRect(contour)
                val centerX = boundingRect.x + boundingRect.width / 2.0f
                val centerY = boundingRect.y + boundingRect.height / 2.0f
                
                // Chỉ lấy đối thủ trên sân (y > 0.25 để bỏ qua UI phía trên)
                if (centerY / mat.height() > 0.25f) {
                    results.add(DetectionResult(
                        label = "Opponent",
                        x = centerX / mat.width(),
                        y = centerY / mat.height(),
                        width = boundingRect.width.toFloat() / mat.width(),
                        height = boundingRect.height.toFloat() / mat.height(),
                        confidence = 0.8f
                    ))
                }
            }
        }

        hsvMat.release()
        allMask.release()
        greenMask.release()
        blackMask.release()
        yellowMask.release()
        whiteMask.release()
        enemyMask.release()
        kernel.release()
        hierarchy.release()
        
        return results
    }
}
