package com.xiangqi.app.engine

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.*

/**
 * 通过 UCI 协议与 Pikafish 通信（进程 stdin/stdout）。
 * 引擎可执行文件需放在 assets/engine/<abi>/pikafish，运行前会解压到 app 目录。
 */
class UciEngine(
    private val engineDir: File,
    private val movetimeMs: Int = 3000
) {
    private var process: Process? = null
    private var writer: OutputStreamWriter? = null
    private var reader: BufferedReader? = null
    private val executor = Executors.newSingleThreadExecutor()

    fun isAvailable(): Boolean {
        val exe = File(engineDir, "pikafish")
        return exe.canExecute() || runCatching { exe.setExecutable(true) }.getOrElse { false }
    }

    fun start(): Boolean {
        if (process?.isAlive == true) return true
        val exe = File(engineDir, "pikafish")
        if (!exe.exists()) return false
        return try {
            process = ProcessBuilder(exe.absolutePath)
                .directory(engineDir)
                .redirectErrorStream(true)
                .start()
            writer = OutputStreamWriter(process!!.outputStream, Charsets.UTF_8)
            reader = BufferedReader(InputStreamReader(process!!.inputStream, Charsets.UTF_8))
            send("uci")
            waitForUciReady()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun send(line: String) {
        writer?.write(line)
        writer?.write("\n")
        writer?.flush()
    }

    private fun waitForUciReady(): Boolean {
        val deadline = System.currentTimeMillis() + 5000
        while (System.currentTimeMillis() < deadline && reader != null) {
            val line = reader!!.readLine() ?: break
            if (line.trim().equals("uciok", ignoreCase = true)) return true
        }
        return false
    }

    /**
     * 给定当前 FEN，在后台计算最佳着法，通过 callback 回传（主线程调用 callback）。
     * 返回的着法为 ICCS 四字符，如 "e3e4"；失败时为 null。
     */
    fun getBestMoveAsync(fen: String, callback: (String?) -> Unit) {
        executor.submit {
            val result = getBestMoveInternal(fen)
            callback(result)
        }
    }

    /**
     * 同步获取最佳着法（会阻塞调用线程，建议在后台线程调用）。
     */
    fun getBestMove(fen: String): String? = getBestMoveInternal(fen)

    private fun getBestMoveInternal(fen: String): String? {
        if (process?.isAlive != true || writer == null || reader == null) return null
        send("position fen $fen")
        send("go movetime $movetimeMs")
        val deadline = System.currentTimeMillis() + movetimeMs + 5000
        while (System.currentTimeMillis() < deadline) {
            val line = reader!!.readLine() ?: break
            val t = line.trim()
            if (t.startsWith("bestmove ")) {
                val rest = t.removePrefix("bestmove ").trim()
                val move = rest.split(Regex("\\s+")).firstOrNull()
                if (!move.isNullOrBlank() && move != "none" && move.length == 4) return move
                return null
            }
        }
        return null
    }

    fun stop() {
        try {
            send("quit")
        } catch (_: Exception) { }
        process?.destroyForcibly()
        process = null
        writer = null
        reader = null
    }
}
