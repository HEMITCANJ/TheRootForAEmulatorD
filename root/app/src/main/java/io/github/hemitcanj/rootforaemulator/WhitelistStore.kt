package io.github.hemitcanj.rootforaemulator

import android.content.Context

class WhitelistStore(context: Context) {

    private val prefs = context.getSharedPreferences("rfae_whitelist", Context.MODE_PRIVATE)

    fun allowed(): Set<String> = prefs.getStringSet(KEY, emptySet())?.toSet().orEmpty()

    fun setAllowed(packageName: String, allow: Boolean) {
        val next = allowed().toMutableSet()
        if (allow) next.add(packageName) else next.remove(packageName)
        prefs.edit().putStringSet(KEY, next).apply()
    }

    private companion object {
        const val KEY = "allowed_packages"
    }
}
