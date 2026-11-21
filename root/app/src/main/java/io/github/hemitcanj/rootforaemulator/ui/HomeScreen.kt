package io.github.hemitcanj.rootforaemulator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.hemitcanj.rootforaemulator.UiState

@Composable
fun HomeScreen(
    state: UiState,
    onRequestRoot: () -> Unit,
    onDeploy: () -> Unit,
    onRestore: () -> Unit,
    onRefresh: () -> Unit,
    onShowGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        RootStatusCard(state, onRequestRoot, onDeploy, onRestore, onRefresh, onShowGuide)
        FlowCard()
        WarningCard(state)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RootStatusCard(
    state: UiState,
    onRequestRoot: () -> Unit,
    onDeploy: () -> Unit,
    onRestore: () -> Unit,
    onRefresh: () -> Unit,
    onShowGuide: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = RootSurface),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (state.hasRoot) RootGreen else RootRed)
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    text = if (state.hasRoot) "Root 已获取" else "Root 未获取",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RootText
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onRefresh, enabled = !state.busy) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = RootMuted)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(
                    text = if (state.hasRoot) "su 可用" else "su 不可用",
                    color = if (state.hasRoot) RootGreen else RootRed
                )
                StatusPill(
                    text = if (state.brokerDeployed) "代理已部署" else "未部署代理",
                    color = if (state.brokerDeployed) RootGreen else RootYellow
                )
            }

            Text(
                text = if (state.hasRoot) {
                    "当前 su：${state.suPath ?: "未知"}"
                } else {
                    "请先在 AEmulator 中开启内置 Root 终端"
                },
                color = RootMuted,
                style = MaterialTheme.typography.bodySmall
            )

            if (state.allowed.isNotEmpty()) {
                Text(
                    text = "白名单应用：${state.allowed.size} 个（含本应用 ${state.allowed.size + 1} 个 UID）",
                    color = RootMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (!state.hasRoot && state.rootDetail.isNotBlank()) {
                Text(
                    text = state.rootDetail,
                    color = RootRed,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onRequestRoot,
                    enabled = !state.busy,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.hasRoot) "重新检测" else "申请 Root")
                }
                OutlinedButton(
                    onClick = if (state.brokerDeployed) onRestore else onDeploy,
                    enabled = !state.busy,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.brokerDeployed) "恢复原 su" else "部署 su 代理")
                }
            }

            if (!state.hasRoot) {
                TextButton(onClick = onShowGuide, enabled = !state.busy) {
                    Text("查看 AEmulator 开启步骤", color = RootBlue)
                }
            }

            if (state.loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = RootGreen,
                    trackColor = RootSurfaceHigh
                )
            }
        }
    }
}

@Composable
private fun FlowCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = RootSurface),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "使用流程",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = RootText
            )
            FlowStep("1", "在 AEmulator 设置中开启 Root 权限 / Root 终端，并重启模拟器。")
            FlowStep("2", "点击「申请 Root」，确认状态变为 Root 已获取。")
            FlowStep("3", "点击「部署 su 代理」，自动备份原 su 并写入白名单代理。")
            FlowStep("4", "到「应用」页勾选需要 root 的应用，未授权的应用调用 su 会被拒绝。")
        }
    }
}

@Composable
private fun FlowStep(index: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            index,
            color = RootGreen,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.width(10.dp))
        Text(text, color = RootMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun WarningCard(state: UiState) {
    val backup = "${state.suPath ?: "/system/xbin/su"}.rfae-real"
    Card(
        colors = CardDefaults.cardColors(containerColor = RootYellow.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = RootYellow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "风险提示",
                    color = RootYellow,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                "部署会把原 su 备份到 $backup，并用白名单代理替换系统 su。",
                color = RootMuted,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "如需还原：点击本应用的「恢复原 su」，或在模拟器 Root 终端执行：",
                color = RootMuted,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "cp -p $backup ${state.suPath ?: "/system/xbin/su"}",
                color = RootGreen,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
