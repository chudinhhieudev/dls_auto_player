package com.dlsautoplayer.vision

import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import com.dlsautoplayer.vision.detector.BallDetector
import com.dlsautoplayer.vision.detector.PlayerDetector
import com.dlsautoplayer.vision.detector.ScoreboardDetector
import com.dlsautoplayer.vision.detector.TeammateDetector
import com.dlsautoplayer.vision.detector.GoalkeeperDetector
import com.dlsautoplayer.vision.detector.OpponentDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    private val scoreboardDetector = ScoreboardDetector()
    private val teammateDetector = TeammateDetector()
    private val goalkeeperDetector = GoalkeeperDetector()
    private val opponentDetector = OpponentDetector()

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
    suspend fun processFrame(image: Image): List<DetectionResult> = coroutineScope {
        if (!isInitialized) return@coroutineScope emptyList()

        val mat = imageToMat(image)
        if (mat == null) return@coroutineScope emptyList()

        val results = mutableListOf<DetectionResult>()

        val deferredBall = async(Dispatchers.Default) { ballDetector.detect(mat) }
        val deferredPlayer = async(Dispatchers.Default) { playerDetector.detect(mat) }
        val deferredScoreboard = async(Dispatchers.Default) { scoreboardDetector.detect(mat) }
        val deferredTeammates = async(Dispatchers.Default) { teammateDetector.detect(mat) }
        val deferredGoalkeeper = async(Dispatchers.Default) { goalkeeperDetector.detect(mat) }
        val deferredOpponents = async(Dispatchers.Default) { opponentDetector.detect(mat) }

        val ball = deferredBall.await()
        if (ball != null) results.add(ball)

        val player = deferredPlayer.await()
        if (player != null) results.add(player)

        val scoreboard = deferredScoreboard.await()
        if (scoreboard != null) results.add(scoreboard)

        val teammates = deferredTeammates.await()
        results.addAll(teammates)

        val goalkeeper = deferredGoalkeeper.await()
        if (goalkeeper != null) results.add(goalkeeper)

        val opponents = deferredOpponents.await()
        results.addAll(opponents)

        mat.release() // Giải phóng bộ nhớ
        return@coroutineScope results
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
