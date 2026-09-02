package com.dlsautoplayer.presentation

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class FloatingUIManager(private val context: Context, private val onToggleListener: (Boolean) -> Unit) {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var isBotActive = false
    private var iconView: TextView? = null

    fun show() {
        if (floatingView != null) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val sizePx = dpToPx(48)

        val textView = TextView(context).apply {
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        iconView = textView
        updateIcon()

        val layoutParams = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 180
        }

        iconView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (dx * dx + dy * dy > 25) {
                            isClick = false
                        }
                        layoutParams.x = initialX + dx
                        layoutParams.y = initialY + dy
                        windowManager?.updateViewLayout(floatingView, layoutParams)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            isBotActive = !isBotActive
                            updateIcon()
                            onToggleListener(isBotActive)
                        }
                        return true
                    }
                }
                return false
            }
        })

        floatingView = iconView
        windowManager?.addView(floatingView, layoutParams)
    }

    private fun updateIcon() {
        val view = iconView ?: return
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            // Nền đen đậm rõ nét, viền trắng tinh tế
            setColor(Color.parseColor("#EE1A1A1A"))
            setStroke(dpToPx(2), Color.WHITE)
        }
        view.background = bg
        view.text = if (isBotActive) "⏹" else "▶"
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    fun hide() {
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
            floatingView = null
        }
    }
}
