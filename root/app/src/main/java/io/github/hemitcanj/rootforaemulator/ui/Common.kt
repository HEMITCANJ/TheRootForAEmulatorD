package io.github.hemitcanj.rootforaemulator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatusPill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(text, color = color, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    destructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RootSurface,
        titleContentColor = RootText,
        textContentColor = RootMuted,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(
                    confirmLabel,
                    color = if (destructive) RootRed else RootGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = RootMuted) }
        }
    )
}

@Composable
fun RootGuideDialog(onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RootSurface,
        titleContentColor = RootText,
        textContentColor = RootMuted,
        title = { Text("开启 AEmulator 内置 Root", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GuideStep("1", "打开 AEmulator 模拟器，进入 设置 / 其他设置，打开「Root 权限」或「Root 终端」。")
                GuideStep("2", "按提示重启模拟器，让 root 生效。")
                GuideStep("3", "在内置 Root 终端执行下列命令，输出 uid=0 即成功：")
                Surface(color = RootBg, shape = RoundedCornerShape(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 4.dp)
                    ) {
                        Text(
                            "su -c id",
                            color = RootGreen,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            clipboard.setText(AnnotatedString("su -c id"))
                        }) {
                            Text("复制", color = RootGreen)
                        }
                    }
                }
                GuideStep("4", "回到本应用点击「申请 Root」，再部署 su 代理。")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("知道了", color = RootGreen) }
        }
    )
}

@Composable
private fun GuideStep(index: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            color = RootGreen.copy(alpha = 0.15f),
            shape = CircleShape
        ) {
            Text(
                index,
                color = RootGreen,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(text, color = RootText, style = MaterialTheme.typography.bodySmall)
    }
}
