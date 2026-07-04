package com.example.wechatoverlayhelper

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast

class OverlayService : Service() {
    private val config = TargetPageConfig.DEFAULT
    private lateinit var windowManager: WindowManager
    private var overlayView: ImageView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> showOverlay()
            ACTION_HIDE -> {
                hideOverlay()
                stopSelf(startId)
            }
        }
        return if (overlayView == null) START_NOT_STICKY else START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }

    private fun showOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先开启悬浮窗权限", Toast.LENGTH_SHORT).show()
            return
        }
        if (overlayView != null) return

        val imageView = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = false
            assets.open(config.overlayImageAssetName).use { input ->
                setImageBitmap(android.graphics.BitmapFactory.decodeStream(input))
            }
        }

        val widthPx = dpToPx(config.widthDp)
        val heightPx = dpToPx(config.heightDp)
        val bounds = currentWindowBounds()
        val x = ((bounds.width() - widthPx) * config.xRatio).toInt()
        val y = ((bounds.height() - heightPx) * config.yRatio).toInt()

        val params = WindowManager.LayoutParams(
            widthPx,
            heightPx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x.coerceAtLeast(0)
            this.y = y.coerceAtLeast(0)
        }

        runCatching {
            windowManager.addView(imageView, params)
            overlayView = imageView
        }.onFailure {
            overlayView = null
        }
    }

    private fun hideOverlay() {
        val view = overlayView ?: return
        runCatching {
            windowManager.removeView(view)
        }
        overlayView = null
    }

    private fun currentWindowBounds(): android.graphics.Rect {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds
        } else {
            @Suppress("DEPRECATION")
            android.graphics.Rect().also { rect ->
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRectSize(rect)
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    companion object {
        const val ACTION_SHOW = "com.example.wechatoverlayhelper.action.SHOW_OVERLAY"
        const val ACTION_HIDE = "com.example.wechatoverlayhelper.action.HIDE_OVERLAY"

        fun show(context: Context) {
            runCatching {
                context.startService(Intent(context, OverlayService::class.java).setAction(ACTION_SHOW))
            }
        }

        fun hide(context: Context) {
            runCatching {
                context.startService(Intent(context, OverlayService::class.java).setAction(ACTION_HIDE))
            }
        }
    }
}
