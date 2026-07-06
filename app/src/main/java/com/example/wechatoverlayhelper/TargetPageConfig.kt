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
            requiredTexts = emptyList(),
            optionalTexts = listOf("确认入场", "用户姓名", "预约场次", "预约时间", "券码信息"),
            minOptionalHitCount = 1,
            overlayImageAssetName = "sticker.png",
            xRatio = 0.38f,
            yRatio = 0.81f,
            widthDp = 120,
            heightDp = 40,
        )
    }
}
