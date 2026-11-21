package io.github.hemitcanj.rootforaemulator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val hasRoot: Boolean = false,
    val suPath: String? = null,
    val brokerDeployed: Boolean = false,
    val rootDetail: String = "",
    val apps: List<AppInfo> = emptyList(),
    val appsLoading: Boolean = false,
    val allowed: Set<String> = emptySet(),
    val showSystem: Boolean = false,
    val logs: String = "",
    val logsLoading: Boolean = false,
    val message: String? = null,
    val showGuide: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val broker = SuBroker(application)
    private val whitelist = WhitelistStore(application)
    private val repository = AppRepository(application)

    private val _ui = MutableStateFlow(UiState(allowed = whitelist.allowed()))
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    init {
        refresh()
        loadApps()
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true) }
            val status = broker.status()
            _ui.update {
                it.copy(
                    loading = false,
                    hasRoot = status.hasRoot,
                    suPath = status.suPath,
                    brokerDeployed = status.deployed,
                    rootDetail = status.detail
                )
            }
        }
    }

    fun requestRoot() {
        viewModelScope.launch {
            _ui.update { it.copy(busy = true) }
            val result = RootShell.checkRoot()
            val granted = result.ok && result.stdout.contains("uid=0")
            _ui.update { it.copy(busy = false) }
            if (granted) {
                post("已获取 Root 权限")
                refresh()
            } else {
                _ui.update { it.copy(showGuide = true) }
                post("未获取 Root：${result.combined.ifBlank { "su 不可用" }}")
            }
        }
    }

    fun showGuide() = _ui.update { it.copy(showGuide = true) }

    fun dismissGuide() = _ui.update { it.copy(showGuide = false) }

    fun deployBroker() {
        viewModelScope.launch {
            _ui.update { it.copy(busy = true) }
            val outcome = broker.deploy(whitelist.allowed())
            _ui.update { it.copy(busy = false) }
            post(outcome.message)
            refresh()
        }
    }

    fun restoreOriginalSu() {
        viewModelScope.launch {
            _ui.update { it.copy(busy = true) }
            val outcome = broker.restore()
            _ui.update { it.copy(busy = false) }
            post(outcome.message)
            refresh()
        }
    }

    fun setAppAllowed(packageName: String, allow: Boolean) {
        viewModelScope.launch {
            whitelist.setAllowed(packageName, allow)
            _ui.update { it.copy(allowed = whitelist.allowed()) }
            if (_ui.value.hasRoot && _ui.value.brokerDeployed) {
                val result = broker.applyWhitelist(whitelist.allowed())
                if (result.ok && result.stdout.contains("APPLY_OK")) {
                    post(if (allow) "$packageName 已加入 root 白名单" else "$packageName 的 root 已撤销")
                } else {
                    post("已保存，但同步到系统失败：${result.combined.ifBlank { "su 不可用" }}")
                }
                refresh()
            } else {
                post("已保存；部署 su 代理后生效")
            }
        }
    }

    fun setShowSystem(show: Boolean) {
        if (_ui.value.showSystem == show) return
        _ui.update { it.copy(showSystem = show) }
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _ui.update { it.copy(appsLoading = true) }
            val apps = repository.load(_ui.value.showSystem)
            _ui.update { it.copy(apps = apps, appsLoading = false) }
        }
    }

    fun loadLogs() {
        viewModelScope.launch {
            _ui.update { it.copy(logsLoading = true) }
            val result = broker.readLogs()
            val text = if (result.ok) {
                result.stdout.ifBlank { "（暂无授权记录）" }
            } else {
                "读取日志失败：${result.combined.ifBlank { "su 不可用" }}"
            }
            _ui.update { it.copy(logs = text, logsLoading = false) }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            val result = broker.clearLogs()
            _ui.update {
                it.copy(
                    logs = if (result.ok) "（暂无授权记录）"
                    else "清空日志失败：${result.combined.ifBlank { "su 不可用" }}"
                )
            }
        }
    }

    fun consumeMessage() = _ui.update { it.copy(message = null) }

    private fun post(message: String) = _ui.update { it.copy(message = message) }
}
