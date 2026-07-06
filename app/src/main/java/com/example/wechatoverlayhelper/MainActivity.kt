package com.example.wechatoverlayhelper

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var overlayStatus: TextView
    private lateinit var accessibilityStatus: TextView
    private lateinit var lastTextStatus: TextView
    private lateinit var nameInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContentView())
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionStatus()
    }

    private fun buildContentView(): ScrollView {
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(28))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }

        container.addView(TextView(this).apply {
            text = "微信小程序目标页面自动悬浮贴图助手"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
        })

        container.addView(TextView(this).apply {
            text = "只监听微信窗口；命中目标页面后显示贴图，离开后隐藏。贴图不可触摸、不可聚焦，不拦截操作。"
            textSize = 15f
            setPadding(0, dp(12), 0, dp(20))
        })

        overlayStatus = statusTextView()
        accessibilityStatus = statusTextView()
        lastTextStatus = statusTextView()
        container.addView(overlayStatus)
        container.addView(accessibilityStatus)
        container.addView(lastTextStatus)

        container.addView(TextView(this).apply {
            text = "贴图姓名"
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(18), 0, dp(6))
        })

        nameInput = EditText(this).apply {
            setSingleLine(true)
            textSize = 18f
            setText(AppPreferences.getOverlayName(this@MainActivity))
            hint = "输入要显示的姓名"
        }
        container.addView(nameInput)

        container.addView(actionButton("保存姓名") {
            AppPreferences.setOverlayName(this, nameInput.text?.toString().orEmpty())
            OverlayService.show(this)
        })

        container.addView(actionButton("打开悬浮窗权限设置") {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"),
            )
            startActivity(intent)
        })

        container.addView(actionButton("打开无障碍设置") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })

        container.addView(actionButton("测试显示贴图") {
            OverlayService.show(this)
        })

        container.addView(actionButton("测试隐藏贴图") {
            OverlayService.hide(this)
        })

        container.addView(TextView(this).apply {
            text = "当前触发词：${TargetPageConfig.DEFAULT.optionalTexts}；命中其中 ${TargetPageConfig.DEFAULT.minOptionalHitCount} 个就显示贴图。"
            textSize = 13f
            setPadding(0, dp(18), 0, 0)
        })

        return ScrollView(this).apply {
            addView(container)
        }
    }

    private fun statusTextView(): TextView {
        return TextView(this).apply {
            textSize = 16f
            setPadding(0, 8, 0, 8)
        }
    }

    private fun actionButton(label: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            gravity = Gravity.CENTER
            setOnClickListener { onClick() }
        }
    }

    private fun refreshPermissionStatus() {
        overlayStatus.text = if (Settings.canDrawOverlays(this)) {
            "悬浮窗权限：已开启"
        } else {
            "悬浮窗权限：未开启"
        }

        accessibilityStatus.text = if (isAccessibilityServiceEnabled()) {
            "无障碍服务：已开启"
        } else {
            "无障碍服务：未开启"
        }

        val lastText = AppPreferences.getLastAccessibilityText(this)
        lastTextStatus.text = if (lastText.isBlank()) {
            "最近识别：暂无"
        } else {
            val matched = if (AppPreferences.wasLastMatched(this)) "已命中" else "未命中"
            "最近识别：$matched；$lastText"
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = manager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK,
        )
        val expectedServiceName = "$packageName/${MiniProgramAccessibilityService::class.java.name}"
        return enabledServices.any { service ->
            service.resolveInfo.serviceInfo.let {
                "${it.packageName}/${it.name}" == expectedServiceName
            }
        }
    }
}
