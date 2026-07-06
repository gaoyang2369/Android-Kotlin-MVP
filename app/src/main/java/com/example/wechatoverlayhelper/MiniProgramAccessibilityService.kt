package com.example.wechatoverlayhelper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MiniProgramAccessibilityService : AccessibilityService() {
    private val config = TargetPageConfig.DEFAULT
    private val handler = Handler(Looper.getMainLooper())
    private var hitCount = 0
    private var missCount = 0
    private var isOverlayShown = false

    override fun onServiceConnected() {
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            packageNames = arrayOf(config.targetPackage)
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 120
            flags = flags or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.packageName?.toString() != config.targetPackage) {
            registerMiss()
            return
        }

        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        inspectCurrentWindow(event)
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ inspectCurrentWindow(null) }, 250L)
        handler.postDelayed({ inspectCurrentWindow(null) }, 700L)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        OverlayService.hide(this)
        super.onDestroy()
    }

    private fun inspectCurrentWindow(event: AccessibilityEvent?) {
        val texts = mutableListOf<String>()
        event?.text?.mapNotNullTo(texts) { it?.toString() }
        event?.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let(texts::add)

        rootInActiveWindow?.let { root ->
            collectTexts(root, texts)
            root.recycle()
        }

        val matched = PageRuleMatcher.matches(config, texts)
        AppPreferences.saveLastAccessibilitySnapshot(this, texts, matched)
        if (matched) {
            registerHit()
        } else {
            registerMiss()
        }
    }

    private fun registerHit() {
        hitCount += 1
        missCount = 0
        if (!isOverlayShown && hitCount >= REQUIRED_HIT_COUNT) {
            OverlayService.show(this)
            isOverlayShown = true
        }
    }

    private fun registerMiss() {
        missCount += 1
        hitCount = 0
        if (isOverlayShown && missCount >= REQUIRED_MISS_COUNT) {
            OverlayService.hide(this)
            isOverlayShown = false
        }
    }

    private fun collectTexts(node: AccessibilityNodeInfo, out: MutableList<String>) {
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let(out::add)
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let(out::add)

        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            collectTexts(child, out)
            child.recycle()
        }
    }

    companion object {
        private const val REQUIRED_HIT_COUNT = 2
        private const val REQUIRED_MISS_COUNT = 3
    }
}
