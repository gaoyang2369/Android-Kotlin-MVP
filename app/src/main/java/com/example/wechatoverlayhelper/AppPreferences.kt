package com.example.wechatoverlayhelper

import android.content.Context

object AppPreferences {
    private const val PREFS_NAME = "overlay_helper_prefs"
    private const val KEY_OVERLAY_NAME = "overlay_name"
    private const val KEY_LAST_ACCESSIBILITY_TEXT = "last_accessibility_text"
    private const val KEY_LAST_MATCHED = "last_matched"

    fun getOverlayName(context: Context): String {
        return prefs(context).getString(KEY_OVERLAY_NAME, DEFAULT_OVERLAY_NAME)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_OVERLAY_NAME
    }

    fun setOverlayName(context: Context, name: String) {
        prefs(context).edit()
            .putString(KEY_OVERLAY_NAME, name.trim().ifBlank { DEFAULT_OVERLAY_NAME })
            .apply()
    }

    fun saveLastAccessibilitySnapshot(context: Context, texts: Collection<String>, matched: Boolean) {
        val preview = texts
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(24)
            .joinToString(separator = " / ")

        prefs(context).edit()
            .putString(KEY_LAST_ACCESSIBILITY_TEXT, preview)
            .putBoolean(KEY_LAST_MATCHED, matched)
            .apply()
    }

    fun getLastAccessibilityText(context: Context): String {
        return prefs(context).getString(KEY_LAST_ACCESSIBILITY_TEXT, "") ?: ""
    }

    fun wasLastMatched(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_LAST_MATCHED, false)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private const val DEFAULT_OVERLAY_NAME = "宋高杨"
}
