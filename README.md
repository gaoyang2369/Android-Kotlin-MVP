# 微信小程序目标页面自动悬浮贴图助手

Android Kotlin MVP：只监听 `com.tencent.mm` 的无障碍窗口内容，命中目标页面后用 `WindowManager.TYPE_APPLICATION_OVERLAY` 显示一个不透明姓名贴片，离开目标页面后隐藏。

## 边界

- 不模拟点击。
- 不注入微信。
- 不修改小程序内容。
- 需要用户手动开启悬浮窗权限和无障碍服务后才工作。
- 悬浮窗使用 `FLAG_NOT_FOCUSABLE` 和 `FLAG_NOT_TOUCHABLE`，不接收焦点、不拦截触摸。
- 未实现 MediaProjection。

## 配置入口

页面识别和贴片位置配置在：

`app/src/main/java/com/example/wechatoverlayhelper/TargetPageConfig.kt`

默认规则：

- `targetPackage = "com.tencent.mm"`
- `requiredTexts = []`
- `optionalTexts = ["确认入场", "用户姓名", "预约场次", "预约时间", "券码信息"]`
- `minOptionalHitCount = 1`
- `xRatio = 0.38`
- `yRatio = 0.81`
- `widthDp = 120`
- `heightDp = 40`

贴片姓名可在 App 首页输入并保存，默认是“宋高杨”。贴片使用不透明浅紫底色，用于遮住原页面姓名。

## 安装和测试

1. 用 Android Studio 打开本目录。
2. 等 Gradle 同步完成后，连接 Android 8.0 及以上手机。
3. 运行 `app` 到手机。
4. 打开 App，点击“打开悬浮窗权限设置”，允许本 App 显示在其他应用上层。
5. 返回 App，点击“打开无障碍设置”，找到“微信小程序目标页面贴图助手”并开启。
6. 在“贴图姓名”里输入要显示的姓名，点击“保存姓名”。
7. 点击“测试显示贴图”，确认贴图能显示且不能被触摸拖动；点击“测试隐藏贴图”确认能隐藏。
8. 当前识别规则是页面可访问文本中包含“确认入场 / 用户姓名 / 预约场次 / 预约时间 / 券码信息”任意一个。首页“最近识别”会显示无障碍服务最近读到的文字，方便判断微信是否暴露了这些文本。
8. 打开微信并进入目标小程序页面：连续 2 次命中后显示贴图，连续 3 次未命中后隐藏贴图。
