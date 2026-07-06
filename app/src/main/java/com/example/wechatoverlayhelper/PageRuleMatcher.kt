package com.example.wechatoverlayhelper

object PageRuleMatcher {
    fun matches(config: TargetPageConfig, visibleTexts: Collection<String>): Boolean {
        val normalizedText = visibleTexts
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(separator = "\n")
            .replace(Regex("\\s+"), "")

        if (normalizedText.isBlank()) return false

        val hasAllRequired = config.requiredTexts.all { required ->
            normalizedText.contains(required.replace(Regex("\\s+"), ""), ignoreCase = true)
        }
        if (!hasAllRequired) return false

        val optionalHitCount = config.optionalTexts.count { optional ->
            normalizedText.contains(optional.replace(Regex("\\s+"), ""), ignoreCase = true)
        }
        return optionalHitCount >= config.minOptionalHitCount
    }
}
