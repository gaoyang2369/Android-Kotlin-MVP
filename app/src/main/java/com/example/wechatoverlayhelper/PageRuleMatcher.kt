package com.example.wechatoverlayhelper

object PageRuleMatcher {
    fun matches(config: TargetPageConfig, visibleTexts: Collection<String>): Boolean {
        val normalizedText = visibleTexts
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(separator = "\n")

        if (normalizedText.isBlank()) return false

        val hasAllRequired = config.requiredTexts.all { required ->
            normalizedText.contains(required, ignoreCase = true)
        }
        if (!hasAllRequired) return false

        val optionalHitCount = config.optionalTexts.count { optional ->
            normalizedText.contains(optional, ignoreCase = true)
        }
        return optionalHitCount >= config.minOptionalHitCount
    }
}
