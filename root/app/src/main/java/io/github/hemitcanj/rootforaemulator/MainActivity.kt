package io.github.hemitcanj.rootforaemulator

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hemitcanj.rootforaemulator.ui.AppsScreen
import io.github.hemitcanj.rootforaemulator.ui.ConfirmDialog
import io.github.hemitcanj.rootforaemulator.ui.HomeScreen
import io.github.hemitcanj.rootforaemulator.ui.LogsScreen
import io.github.hemitcanj.rootforaemulator.ui.RootBg
import io.github.hemitcanj.rootforaemulator.ui.RootGreen
import io.github.hemitcanj.rootforaemulator.ui.RootGuideDialog
import io.github.hemitcanj.rootforaemulator.ui.RootMuted
import io.github.hemitcanj.rootforaemulator.ui.RootSurface
import io.github.hemitcanj.rootforaemulator.ui.RootSurfaceHigh
import io.github.hemitcanj.rootforaemulator.ui.RootText
import io.github.hemitcanj.rootforaemulator.ui.RootTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            RootTheme {
                RootApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RootApp(vm: MainViewModel = viewModel()) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showDeployConfirm by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        vm.consumeMessage()
    }

    Scaffold(
        containerColor = RootBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "rootforAEmulator",
                            fontWeight = FontWeight.Bold,
                            color = RootText
                        )
                        Text(
                            "AEmulator Root 授权分发",
                            style = MaterialTheme.typography.labelSmall,
                            color = RootMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RootBg,
                    titleContentColor = RootText
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = RootSurface) {
                NavItem(tab == 0, { tab = 0 }, Icons.Default.Home, "状态")
                NavItem(tab == 1, { tab = 1 }, Icons.Default.List, "应用")
                NavItem(tab == 2, { tab = 2 }, Icons.Default.Info, "日志")
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (tab) {
                0 -> HomeScreen(
                    state = state,
                    onRequestRoot = vm::requestRoot,
                    onDeploy = {
                        if (!state.hasRoot) vm.showGuide() else showDeployConfirm = true
                    },
                    onRestore = { showRestoreConfirm = true },
                    onRefresh = vm::refresh,
                    onShowGuide = vm::showGuide
                )

                1 -> AppsScreen(
                    state = state,
                    onToggleApp = vm::setAppAllowed,
                    onShowSystemChanged = vm::setShowSystem
                )

                else -> LogsScreen(
                    state = state,
                    onRefresh = vm::loadLogs,
                    onClear = vm::clearLogs
                )
            }

            if (state.busy) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = RootGreen,
                    trackColor = RootSurfaceHigh
                )
            }
        }
    }

    if (state.showGuide) {
        RootGuideDialog(onDismiss = vm::dismissGuide)
    }

    if (showDeployConfirm) {
        ConfirmDialog(
            title = "部署 su 代理？",
            message = "将备份系统原 su 为 <su>.rfae-real，并用白名单代理替换它。部署后只有白名单内的应用能获得 root，其余应用会被拒绝并记录日志。",
            confirmLabel = "部署",
            onConfirm = {
                showDeployConfirm = false
                vm.deployBroker()
            },
            onDismiss = { showDeployConfirm = false }
        )
    }

    if (showRestoreConfirm) {
        ConfirmDialog(
            title = "恢复系统原 su？",
            message = "将用 <su>.rfae-real 覆盖回系统 su 并删除代理备份，之后恢复为模拟器原本的 root 行为。",
            confirmLabel = "恢复",
            destructive = true,
            onConfirm = {
                showRestoreConfirm = false
                vm.restoreOriginalSu()
            },
            onDismiss = { showRestoreConfirm = false }
        )
    }
}

@Composable
private fun RowScope.NavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = RootGreen,
            selectedTextColor = RootGreen,
            indicatorColor = RootGreen.copy(alpha = 0.14f),
            unselectedIconColor = RootMuted,
            unselectedTextColor = RootMuted
        )
    )
}
