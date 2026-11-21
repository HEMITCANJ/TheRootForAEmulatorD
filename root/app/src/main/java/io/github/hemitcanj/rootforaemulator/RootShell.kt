package io.github.hemitcanj.rootforaemulator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 通过模拟器自带的 su 执行 root 命令。
 * 注意：部署 su 代理后，本应用自身的 UID 必须始终位于白名单中，否则这里会失败。
 */
object RootShell {

    data class Result(val exitCode: Int, val stdout: String, val stderr: String) {
        val ok: Boolean get() = exitCode == 0
        val combined: String get() = (stdout + stderr).trim()
    }

    suspend fun su(script: String, timeoutMs: Long = 25_000L): Result = withContext(Dispatchers.IO) {
        run(arrayOf("su", "-c", script), timeoutMs)
    }

    suspend fun suArgs(vararg args: String, timeoutMs: Long = 25_000L): Result = withContext(Dispatchers.IO) {
        run(arrayOf("su") + args, timeoutMs)
    }

    suspend fun checkRoot(): Result = su("id", 15_000L)

    private fun run(command: Array<String>, timeoutMs: Long): Result {
        val process = try {
            ProcessBuilder(*command).start()
        } catch (t: Throwable) {
            return Result(-1, "", "无法执行 ${command.firstOrNull()}: ${t.message ?: t.javaClass.simpleName}")
        }

        // su 可能会等待标准输入，直接关闭避免卡死
        try {
            process.outputStream.close()
        } catch (_: Throwable) {
        }

        val stdout = StringBuilder()
        val stderr = StringBuilder()
        val outReader = Thread {
            try {
                process.inputStream.bufferedReader().use { stdout.append(it.readText()) }
            } catch (_: Throwable) {
            }
        }.apply { isDaemon = true; start() }
        val errReader = Thread {
            try {
                process.errorStream.bufferedReader().use { stderr.append(it.readText()) }
            } catch (_: Throwable) {
            }
        }.apply { isDaemon = true; start() }

        val deadline = System.currentTimeMillis() + timeoutMs
        var exitCode: Int? = null
        while (System.currentTimeMillis() < deadline) {
            try {
                exitCode = process.exitValue()
                break
            } catch (_: IllegalThreadStateException) {
                Thread.sleep(40L)
            }
        }

        if (exitCode == null) {
            try {
                process.destroy()
            } catch (_: Throwable) {
            }
            outReader.join(600)
            errReader.join(600)
            return Result(-2, stdout.toString(), "命令执行超时（${timeoutMs / 1000} 秒）")
        }

        outReader.join(2000)
        errReader.join(2000)
        return Result(exitCode, stdout.toString(), stderr.toString())
    }
}
