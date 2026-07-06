package com.example.wechatoverlayhelper

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast

class OverlayService : Service() {
    private val config = TargetPageConfig.DEFAULT
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> {
                startForeground(NOTIFICATION_ID, buildNotification())
                showOverlay()
                if (overlayView == null) {
                    stopForegroundCompat()
                    stopSelf(startId)
                }
            }
            ACTION_HIDE -> {
                startForeground(NOTIFICATION_ID, buildNotification())
                hideOverlay()
                stopForegroundCompat()
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
        val existingView = overlayView
        if (existingView is TextView) {
            existingView.text = AppPreferences.getOverlayName(this)
            return
        }

        val textView = buildNameStickerView()
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
            PixelFormat.OPAQUE,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x.coerceAtLeast(0)
            this.y = y.coerceAtLeast(0)
        }

        runCatching {
            windowManager.addView(textView, params)
            overlayView = textView
        }.onFailure {
            overlayView = null
        }
    }

    private fun buildNameStickerView(): TextView {
        return TextView(this).apply {
            text = AppPreferences.getOverlayName(this@OverlayService)
            textSize = 18f
            setTextColor(Color.rgb(37, 39, 48))
            typeface = Typeface.DEFAULT
            includeFontPadding = false
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(6), 0, dpToPx(6), 0)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.rgb(248, 246, 255))
                cornerRadius = 0f
            }
        }
    }

    private fun hideOverlay() {
        val view = overlayView ?: return
        runCatching {
            windowManager.removeView(view)
        }
        overlayView = null
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "贴图显示服务",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setSmallIcon(R.drawable.ic_overlay_notification)
            .setContentTitle("贴图助手运行中")
            .setContentText("正在显示不可触摸的姓名贴片")
            .setOngoing(true)
            .build()
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
        private const val NOTIFICATION_CHANNEL_ID = "overlay_helper_service"
        private const val NOTIFICATION_ID = 1001

        fun show(context: Context) {
            runCatching {
                val intent = Intent(context, OverlayService::class.java).setAction(ACTION_SHOW)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun hide(context: Context) {
            runCatching {
                val intent = Intent(context, OverlayService::class.java).setAction(ACTION_HIDE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
    }
}
