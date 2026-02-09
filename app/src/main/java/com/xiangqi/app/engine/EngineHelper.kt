package com.xiangqi.app.engine

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object EngineHelper {
    private const val ASSET_ENGINE = "engine"
    private const val EXE_NAME = "pikafish"

    /**
     * 从 assets 解压引擎到 app 目录并返回引擎所在目录。
     * 目录结构：assets/engine/arm64-v8a/pikafish 或 assets/engine/armeabi-v7a/pikafish
     */
    fun prepareEngine(context: Context): File {
        val abi = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        val engineDir = File(context.filesDir, "engine_$abi").also { it.mkdirs() }
        val exe = File(engineDir, EXE_NAME)
        if (exe.exists()) return engineDir
        val assetPath = "$ASSET_ENGINE/$abi/$EXE_NAME"
        return try {
            context.assets.open(assetPath).use { input ->
                FileOutputStream(exe).use { output ->
                    input.copyTo(output)
                }
            }
            exe.setExecutable(true, false)
            engineDir
        } catch (e: Exception) {
            engineDir
        }
    }

    fun hasEngineInAssets(context: Context): Boolean {
        val abi = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        Log.d("EngineHelper", "Checking for engine with ABI: $abi") // 添加日志
        return try {
            context.assets.open("$ASSET_ENGINE/$abi/$EXE_NAME").close()
            true
        } catch (_: Exception) {
            false
        }
    }
}
