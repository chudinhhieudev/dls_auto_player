package com.dlsautoplayer.vision

import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import com.dlsautoplayer.vision.detector.BallDetector
import com.dlsautoplayer.vision.detector.PlayerDetector
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat

class VisionEngine {

    companion object {
        private const val TAG = "VisionEngine"
        var isInitialized = false
            private set
    }

    private val ballDetector = BallDetector()
    private val playerDetector = PlayerDetector()

    init {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV initialization failed!")
        } else {
            Log.d(TAG, "OpenCV initialized successfully.")
            isInitialized = true
        }
    }

    /**
     * Xử lý frame ảnh, trả về danh sách các đối tượng nhận diện được.
     */
    fun processFrame(image: Image): List<DetectionResult> {
        if (!isInitialized) return emptyList()

        val results = mutableListOf<DetectionResult>()
        val mat = imageToMat(image)
        if (mat == null) return results

        // Chạy các detector
        val ball = ballDetector.detect(mat)
        if (ball != null) results.add(ball)

        val player = playerDetector.detect(mat)
        if (player != null) results.add(player)

        mat.release() // Giải phóng bộ nhớ
        return results
    }

    private fun imageToMat(image: Image): Mat? {
        try {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * image.width

            // Tạo bitmap với padding
            val bitmap = Bitmap.createBitmap(
                image.width + rowPadding / pixelStride,
                image.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)

            // Crop bỏ padding
            val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)

            val mat = Mat()
            Utils.bitmapToMat(croppedBitmap, mat)

            bitmap.recycle()
            croppedBitmap.recycle()
            
            return mat
        } catch (e: Exception) {
            Log.e(TAG, "Error converting Image to Mat", e)
            return null
        }
    }
}
