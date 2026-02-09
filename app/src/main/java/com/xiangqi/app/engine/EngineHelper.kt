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
    fun prepareEngine(context: Context): File? {
        val abi = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        val assetPath = findFirstAvailableAsset(context)
        if (assetPath == null) {
            Log.e("EngineHelper", "No engine asset found for supported ABIs: ${android.os.Build.SUPPORTED_ABIS.joinToString()}")
            return null
        }
        val engineDir = File(context.filesDir, "engine_$abi").also { it.mkdirs() }
        val exe = File(engineDir, EXE_NAME)
        return try {
            if (!exe.exists() || exe.length() == 0L) {
                context.assets.open(assetPath).use { input ->
                    FileOutputStream(exe).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            exe.setExecutable(true, false)
            exe.setReadable(true, false)
            exe.setWritable(true, true)
            engineDir
        } catch (e: Exception) {
            Log.e("EngineHelper", "Failed to prepare engine from asset: $assetPath", e)
            null
        }
    }

    fun hasEngineInAssets(context: Context): Boolean {
        val abis = android.os.Build.SUPPORTED_ABIS
        Log.d("EngineHelper", "Checking for engine with ABIs: ${abis.joinToString()}")
        return findFirstAvailableAsset(context) != null
    }

    private fun findFirstAvailableAsset(context: Context): String? {
        val abis = android.os.Build.SUPPORTED_ABIS.ifEmpty { arrayOf("arm64-v8a") }
        for (abi in abis) {
            for (path in candidateAssetPaths(abi)) {
                if (assetExists(context, path)) {
                    Log.d("EngineHelper", "Using engine asset: $path")
                    return path
                }
            }
        }
        return null
    }

    private fun candidateAssetPaths(abi: String): List<String> {
        val mapped = when (abi) {
            "arm64-v8a" -> listOf("armv8", "armv8-dotprod")
            "armeabi-v7a" -> listOf("armv7")
            else -> emptyList()
        }
        val paths = mutableListOf<String>()
        paths += "$ASSET_ENGINE/$abi/$EXE_NAME"
        for (suffix in mapped) {
            paths += "$ASSET_ENGINE/pikafish-$suffix"
        }
        paths += EXE_NAME
        return paths
    }

    private fun assetExists(context: Context, path: String): Boolean {
        return try {
            context.assets.open(path).close()
            true
        } catch (_: Exception) {
            false
        }
    }
}
