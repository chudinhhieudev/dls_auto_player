package com.dlsautoplayer.capture

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

class ScreenCaptureManager(
    private val context: Context,
    private val resultCode: Int,
    private val data: Intent
) {
    companion object {
        private const val TAG = "ScreenCaptureManager"
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    // Channel để chứa các frame mới nhất, bỏ qua frame cũ nếu không kịp xử lý
    val frameChannel = Channel<Image>(
        capacity = 2,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun startCapture() {
        val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)

        if (mediaProjection == null) {
            Log.e(TAG, "MediaProjection is null")
            return
        }

        setupVirtualDisplay()
    }

    private fun setupVirtualDisplay() {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        // Sử dụng metrics mặc định của màn hình chính
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val screenDensity = metrics.densityDpi

        Log.d(TAG, "Starting capture with resolution: ${screenWidth}x${screenHeight}, dpi: $screenDensity")

        // Tạo ImageReader với định dạng RGBA_8888
        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2 // maxImages
        ).apply {
            setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                if (image != null) {
                    val result = frameChannel.trySend(image)
                    if (result.isFailure) {
                        // Nếu channel đầy và đẩy bị lỗi (mặc dù đã dùng DROP_OLDEST), ta đóng image lại để tránh leak
                        image.close()
                    }
                }
            }, null)
        }

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )
    }

    fun stopCapture() {
        Log.d(TAG, "Stopping capture")
        virtualDisplay?.release()
        virtualDisplay = null

        imageReader?.close()
        imageReader = null

        mediaProjection?.stop()
        mediaProjection = null

        frameChannel.close()
    }
}
