package com.dlsautoplayer.presentation

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout

class FloatingUIManager(private val context: Context, private val onToggleListener: (Boolean) -> Unit) {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var isBotActive = false
    private var button: Button? = null

    fun show() {
        if (floatingView != null) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Khởi tạo Layout
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.TRANSPARENT)
        }

        // Tạo nút Play/Pause
        button = Button(context).apply {
            text = "▶ PLAY"
            setBackgroundColor(Color.parseColor("#4CAF50")) // Màu xanh
            setTextColor(Color.WHITE)
            setOnClickListener {
                isBotActive = !isBotActive
                updateButtonState()
                onToggleListener(isBotActive)
            }
        }

        layout.addView(button)

        // Cấu hình Layout Params cho WindowManager
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 0
        layoutParams.y = 100

        // Lắng nghe sự kiện vuốt để kéo thả nút trên màn hình
        layout.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(floatingView, layoutParams)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        // Tính khoảng cách di chuyển để phân biệt Drag và Click
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY
                        if (dx * dx + dy * dy < 100) { // Nếu vuốt rất nhỏ -> Coi là Click
                            button?.performClick()
                        }
                        return true
                    }
                }
                return false
            }
        })

        floatingView = layout
        windowManager?.addView(floatingView, layoutParams)
    }

    private fun updateButtonState() {
        if (isBotActive) {
            button?.text = "⏸ PAUSE"
            button?.setBackgroundColor(Color.parseColor("#F44336")) // Màu đỏ
        } else {
            button?.text = "▶ PLAY"
            button?.setBackgroundColor(Color.parseColor("#4CAF50")) // Màu xanh
        }
    }

    fun hide() {
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
            floatingView = null
        }
    }
}
