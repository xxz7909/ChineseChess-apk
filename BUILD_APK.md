# 如何编译成 APK

**不一定要用 Android Studio**，有两种常用方式。

---

## 方法一：命令行编译（不用打开 Android Studio）

在 **`android`** 目录下执行：

### Windows（PowerShell 或 CMD）

```bat
cd android
gradlew.bat assembleDebug
```

### Linux / macOS

```bash
cd android
chmod +x gradlew
./gradlew assembleDebug
```

编译完成后，APK 在：

- **调试版**：`android\app\build\outputs\apk\debug\app-debug.apk`
- 若要 **发布版**（需先配置签名）：`gradlew.bat assembleRelease`，输出在 `app\build\outputs\apk\release\`

---

## 首次使用命令行时：没有 `gradlew` 或报错找不到 `gradle-wrapper.jar`

本仓库可能没有包含 `gradle-wrapper.jar`，需要先生成 Gradle Wrapper，任选其一即可：

### 选项 A：用 Android Studio 生成（推荐，一次即可）

1. 用 Android Studio 打开 **`android`** 文件夹。
2. 等待 Gradle 同步完成（会自动生成/更新 wrapper 和 jar）。
3. 之后在 **`android`** 目录下就可以一直用 `gradlew.bat assembleDebug` 编译，无需再开 Android Studio。

### 选项 B：本机已安装 Gradle 时

1. 安装 [Gradle](https://gradle.org/install/)（或 `scoop install gradle` / `choco install gradle`）。
2. 在 **`android`** 目录执行：
   ```bat
   gradle wrapper
   ```
3. 再执行：
   ```bat
   gradlew.bat assembleDebug
   ```

---

## 方法二：只用 Android Studio 编译

1. 用 Android Studio 打开 **`android`** 目录。
2. 菜单 **Build → Build Bundle(s) / APK(s) → Build APK(s)**。
3. 完成后点通知里的 **locate**，或到 `app/build/outputs/apk/debug/` 下找到 `app-debug.apk`。

---

## 总结

| 方式 | 是否需要 Android Studio |
|------|---------------------------|
| 命令行 `gradlew.bat assembleDebug` | 首次可用来生成 wrapper，之后不必打开 |
| Android Studio 里 Build → Build APK | 需要打开 Android Studio |

只要生成好 Gradle Wrapper，以后可以**一直用命令行** `gradlew.bat assembleDebug` 打 APK，不必依赖 Android Studio。
