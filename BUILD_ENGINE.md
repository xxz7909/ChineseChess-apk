# Pikafish 安卓引擎构建与集成

本应用通过 **UCI 协议** 与 Pikafish 进程通信，无需修改 Pikafish 源码。只需为 Android 编译出可执行文件并放入 assets 即可。

## 1. 获取 Pikafish 源码

```bash
git clone https://github.com/official-pikafish/Pikafish.git
cd Pikafish
```

## 2. 使用 Android NDK 交叉编译

### 前置条件

- 安装 [Android NDK](https://developer.android.com/ndk/downloads)（建议 r25+）
- 环境变量 `ANDROID_NDK` 指向 NDK 根目录

### 使用 NDK 独立工具链编译

在 Linux 或 WSL 下（Windows 下可用 MSYS2 或 WSL）：

```bash
# 创建 arm64 工具链（一次即可）
$ANDROID_NDK/build/tools/make_standalone_toolchain.py \
  --arch arm64 \
  --api 24 \
  --install-dir /tmp/ndk-arm64

export PATH=/tmp/ndk-arm64/bin:$PATH
export CC=clang
export CXX=clang++
export CFLAGS="-O3 -DNDEBUG"
export CXXFLAGS="-O3 -DNDEBUG"

cd Pikafish/src
make -j4 build ARCH=android
# 若 Pikafish 的 Makefile 没有 android 架构，见下方「手动编译」。
```

若官方 Makefile 暂无 `ARCH=android`，可手动指定编译器：

```bash
cd Pikafish/src
make -j4 CXX=aarch64-linux-android24-clang++ ARCH=armv8
# 生成的可执行文件可能叫 pikafish 或类似名称，复制为 pikafish
cp pikafish ../../android/app/src/main/assets/engine/arm64-v8a/pikafish
```

### 使用 CMake + NDK 构建（若 Pikafish 支持）

部分引擎提供 CMake 支持，可新建 `CMakeLists.txt` 调用 NDK：

```cmake
cmake_minimum_required(VERSION 3.10)
set(CMAKE_SYSTEM_NAME Android)
set(CMAKE_ANDROID_NDK $ENV{ANDROID_NDK})
set(CMAKE_ANDROID_ARCH_ABI arm64-v8a)
set(CMAKE_ANDROID_STL_TYPE c++_shared)
project(pikafish CXX)
# 添加 Pikafish 源文件并生成可执行文件
add_executable(pikafish ...)
```

然后在项目根目录执行 NDK 的 cmake 并 `make`，将生成的 `pikafish` 放入 `android/app/src/main/assets/engine/arm64-v8a/`。

## 3. 放入本工程

将编译得到的 **可执行文件** 命名为 `pikafish`（无后缀），并放入：

```
android/app/src/main/assets/engine/arm64-v8a/pikafish
```

如需兼容 32 位设备，再编译 armeabi-v7a 并放入：

```
android/app/src/main/assets/engine/armeabi-v7a/pikafish
```

## 4. NNUE 评估文件（可选）

若 Pikafish 需要 NNUE 文件（如 `pikafish.nnue`），可从 [Pikafish 发布页](https://github.com/official-pikafish/Pikafish/releases) 或官网下载，与 `pikafish` 可执行文件放在同一目录（即 `engine/arm64-v8a/`），并在引擎工作目录中指向该文件（若 UCI 支持 EvalFile 选项，可在启动后通过 `setoption` 设置路径；本应用当前未设置，若引擎能自动从当前目录加载则无需修改）。

## 5. 验证

- 构建 APK 并安装到手机。
- 若 assets 中已正确放置 `pikafish`，应用启动后会自动解压到应用目录并启动引擎，对局时显示「思考中…」后引擎会落子。
- 若未放置引擎，界面会提示「引擎未就绪，请将 Pikafish 放入 assets」。

## 难度说明

应用内默认 **最强难度**：每步思考时间约 4 秒（`movetimeMs = 4000`）。可在 `MainActivity.kt` 中调整 `UciEngine(engineDir, movetimeMs = 4000)` 的第二个参数来改变强度（数值越大越强、越慢）。
