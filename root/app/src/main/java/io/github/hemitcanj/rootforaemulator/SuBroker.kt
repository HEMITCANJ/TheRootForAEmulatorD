package io.github.hemitcanj.rootforaemulator

import android.content.Context
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * 把原始 su 替换为白名单代理，并维护代理脚本中的授权 UID 列表。
 * 白名单直接写入代理脚本，避免运行时依赖其它文件的读写权限。
 */
class SuBroker(private val context: Context) {

    companion object {
        const val BASE_DIR = "/data/local/rfae"
        const val LOG_FILE = "$BASE_DIR/access.log"
    }

    data class BrokerStatus(
        val hasRoot: Boolean = false,
        val suPath: String? = null,
        val deployed: Boolean = false,
        val detail: String = ""
    )

    data class Outcome(
        val ok: Boolean,
        val message: String,
        val suPath: String? = null,
        val realPath: String? = null
    )

    private val assetCache = ConcurrentHashMap<String, String>()

    private fun asset(name: String): String = assetCache.getOrPut(name) {
        context.assets.open(name).bufferedReader().use { it.readText() }
    }

    suspend fun status(): BrokerStatus {
        val result = RootShell.su(asset("status.sh"), 20_000L)
        val text = result.stdout
        val hasRoot = result.ok && field(text, "id") == "0"
        val suPath = field(text, "SUPATH").takeIf { it.isNotBlank() }
        val deployed = field(text, "BROKER") == "1"
        val detail = if (hasRoot) "" else result.combined.ifBlank { "su 返回退出码 ${result.exitCode}" }
        return BrokerStatus(hasRoot, suPath, deployed, detail)
    }

    suspend fun deploy(packages: Set<String>): Outcome {
        val wrapper = writeWrapper(packages)
        val script = asset("install_su_broker.sh")
            .replace("@WRAPPER@", wrapper.absolutePath)
            .replace("@BASE@", BASE_DIR)

        val result = RootShell.su(script, 60_000L)
        val suPath = field(result.stdout, "SU").takeIf { it.isNotBlank() }
        val realPath = field(result.stdout, "REAL").takeIf { it.isNotBlank() }

        if (!result.ok || !result.stdout.contains("DEPLOY_OK")) {
            return Outcome(
                false,
                "部署失败：\n${result.combined.ifBlank { "退出码 ${result.exitCode}" }}",
                suPath,
                realPath
            )
        }

        // 用新 su 再验证一次（本应用 UID 始终在 ALLOW_UIDS 中）
        val verify = RootShell.su("id", 20_000L)
        val verified = verify.ok && verify.stdout.contains("uid=0")
        val message = if (verified) {
            "su 代理部署成功，root 将按白名单分发"
        } else {
            "代理已写入，但本应用未通过新的 su 验证。\n可在模拟器 Root 终端执行：${realPath ?: "/system/xbin/su.rfae-real"} -c id"
        }
        return Outcome(verified, message, suPath, realPath)
    }

    suspend fun applyWhitelist(packages: Set<String>): RootShell.Result {
        val wrapper = writeWrapper(packages)
        val script = asset("apply_wrapper.sh").replace("@WRAPPER@", wrapper.absolutePath)
        return RootShell.su(script, 30_000L)
    }

    suspend fun restore(): Outcome {
        val result = RootShell.su(asset("restore_su.sh"), 40_000L)
        val suPath = field(result.stdout, "SU").takeIf { it.isNotBlank() }
        if (result.ok && result.stdout.contains("RESTORE_DONE")) {
            val verified = result.stdout.contains("RESTORE_OK=1")
            return Outcome(
                true,
                if (verified) "已恢复系统原始 su" else "已恢复原始 su，但验证未通过，建议重启模拟器",
                suPath
            )
        }
        return Outcome(false, "恢复失败：\n${result.combined.ifBlank { "退出码 ${result.exitCode}" }}", suPath)
    }

    suspend fun readLogs(): RootShell.Result = RootShell.su("tail -n 400 $LOG_FILE", 15_000L)

    suspend fun clearLogs(): RootShell.Result =
        RootShell.su(": > $LOG_FILE; chmod 666 $LOG_FILE; echo CLEAR_OK", 15_000L)

    private fun writeWrapper(packages: Set<String>): File {
        val content = asset("su_wrapper.sh")
            .replace("@UIDS@", resolveUids(packages).joinToString(" "))
        return File(context.cacheDir, "su_wrapper.sh").apply { writeText(content) }
    }

    private fun resolveUids(packages: Set<String>): List<Int> {
        val uids = linkedSetOf(context.applicationInfo.uid)
        packages.forEach { pkg ->
            runCatching { context.packageManager.getPackageInfo(pkg, 0).applicationInfo?.uid }
                .getOrNull()
                ?.let { uids.add(it) }
        }
        return uids.toList()
    }

    private fun field(text: String, name: String): String =
        Regex("(?m)^" + Regex.escape(name) + "=(.*)$")
            .find(text)
            ?.groupValues
            ?.get(1)
            ?.trim()
            .orEmpty()
}
