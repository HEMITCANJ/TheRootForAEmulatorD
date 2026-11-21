package io.github.hemitcanj.rootforaemulator

import android.content.Context
import android.content.pm.ApplicationInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    suspend fun load(includeSystem: Boolean): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val selfPackage = context.packageName

        pm.getInstalledApplications(0)
            .asSequence()
            .filter { it.packageName != selfPackage }
            .map { info ->
                AppInfo(
                    packageName = info.packageName,
                    label = runCatching { pm.getApplicationLabel(info).toString() }
                        .getOrDefault(info.packageName),
                    uid = info.uid,
                    isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .filter { includeSystem || !it.isSystem }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, AppInfo::label))
            .toList()
    }
}
