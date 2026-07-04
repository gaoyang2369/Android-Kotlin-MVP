package com.example.wechatoverlayhelper

data class TargetPageConfig(
    val targetPackage: String,
    val requiredTexts: List<String>,
    val optionalTexts: List<String>,
    val minOptionalHitCount: Int,
    val overlayImageAssetName: String,
    val xRatio: Float,
    val yRatio: Float,
    val widthDp: Int,
    val heightDp: Int,
) {
    companion object {
        val DEFAULT = TargetPageConfig(
            targetPackage = "com.tencent.mm",
            requiredTexts = listOf("确认入场"),
            optionalTexts = emptyList(),
            minOptionalHitCount = 0,
            overlayImageAssetName = "sticker.png",
            xRatio = 0.32f,
            yRatio = 0.80f,
            widthDp = 78,
            heightDp = 31,
        )
    }
}
