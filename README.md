# 象棋 · 人机对战（Android）

基于 **Pikafish** 的安卓端中国象棋人机对战应用：本地 UCI 引擎、默认最强难度、不联网。

## 架构

- **UI**：Kotlin + ViewBinding，自定义 `BoardView` 绘制棋盘与棋子
- **规则与局面**：FEN 解析/生成、ICCS 着法、吃将判负
- **引擎**：通过 **UCI 协议** 与 Pikafish 进程通信（stdin/stdout），无需修改 Pikafish 源码
- **难度**：默认每步思考约 4 秒，可在代码中调整

## 快速开始

1. **用 Android Studio 打开**  
   打开 `android` 目录，等待 Gradle 同步完成。

2. **放入 Pikafish 可执行文件**  
   将针对 Android 编译好的 `pikafish` 放入：
   - `android/app/src/main/assets/engine/arm64-v8a/pikafish`  
   详细构建方法见 **[BUILD_ENGINE.md](BUILD_ENGINE.md)**。

3. **运行**  
   连接真机或模拟器（建议真机，arm64），运行 `app`。

**编译 APK**：不一定要用 Android Studio，命令行也可。详见 **[BUILD_APK.md](BUILD_APK.md)**（如：在 `android` 目录执行 `gradlew.bat assembleDebug`）。

## 项目结构

```
xiangqi/
├── android/                 # Android 工程
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── assets/engine/   # 放置 pikafish 可执行文件
│   │   │   ├── java/.../game/   # 局面、着法、FEN
│   │   │   ├── java/.../engine/  # UCI 引擎封装
│   │   │   └── java/.../ui/     # 棋盘视图
│   │   └── build.gradle.kts
│   └── build.gradle.kts
├── BUILD_ENGINE.md          # Pikafish 安卓编译与集成说明
└── README.md
```

## 规则与操作

- 红方（你）在下，先手；黑方为 Pikafish。
- 点击己方棋子选中，再点击目标格落子。
- 将/帅被吃即判负，结束对局后可点「新对局」重开。

## 难度与优化

- 当前为 **最强难度**（约 4 秒/步）。修改 `MainActivity.kt` 中 `UciEngine(engineDir, movetimeMs = 4000)` 的 `movetimeMs` 可调节（越大越强、越慢）。
- 若未放入引擎，应用会提示「引擎未就绪」，不影响安装与界面。

## 许可证

应用代码可自用/修改。Pikafish 引擎遵循其原有许可证（如 GPL-3.0）。
