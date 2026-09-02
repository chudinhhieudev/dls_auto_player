package com.dlsautoplayer.vision.detector

import com.dlsautoplayer.vision.DetectionResult
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc

class ScoreboardDetector {

    /**
     * Nhận diện bảng tỉ số (Scoreboard) ở góc trên bên trái màn hình.
     * Dùng để xác nhận chắc chắn Bot đang ở trong trận đấu.
     */
    fun detect(mat: Mat): DetectionResult? {
        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGBA2GRAY)

        Imgproc.GaussianBlur(grayMat, grayMat, org.opencv.core.Size(5.0, 5.0), 0.0)

        val edges = Mat()
        Imgproc.Canny(grayMat, edges, 50.0, 150.0)

        // Chỉ tìm trong khu vực góc trên bên trái (x: 0 -> 0.5, y: 0 -> 0.25)
        val roiRect = Rect(0, 0, (mat.width() * 0.5).toInt(), (mat.height() * 0.25).toInt())
        val edgesROI = edges.submat(roiRect)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edgesROI, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var bestMatch: DetectionResult? = null
        var maxArea = 0.0

        for (contour in contours) {
            val boundingRect = Imgproc.boundingRect(contour)
            val area = boundingRect.width * boundingRect.height
            
            // Scoreboard là một hình chữ nhật dài nằm ngang
            val aspectRatio = boundingRect.width.toFloat() / boundingRect.height.toFloat()
            
            if (area > 2000 && aspectRatio > 2.5f && aspectRatio < 15.0f) {
                if (area > maxArea) {
                    maxArea = area.toDouble()
                    
                    val centerX = boundingRect.x + roiRect.x + boundingRect.width / 2.0f
                    val centerY = boundingRect.y + roiRect.y + boundingRect.height / 2.0f
                    
                    bestMatch = DetectionResult(
                        label = "Scoreboard",
                        x = centerX / mat.width(),
                        y = centerY / mat.height(),
                        width = boundingRect.width.toFloat() / mat.width(),
                        height = boundingRect.height.toFloat() / mat.height(),
                        confidence = 0.9f
                    )
                }
            }
        }

        grayMat.release()
        edges.release()
        hierarchy.release()
        return bestMatch
    }
}
