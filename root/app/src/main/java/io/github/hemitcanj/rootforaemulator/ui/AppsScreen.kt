package io.github.hemitcanj.rootforaemulator.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import io.github.hemitcanj.rootforaemulator.AppInfo
import io.github.hemitcanj.rootforaemulator.UiState

@Composable
fun AppsScreen(
    state: UiState,
    onToggleApp: (String, Boolean) -> Unit,
    onShowSystemChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }

    val filtered = remember(state.apps, query) {
        val keyword = query.trim()
        if (keyword.isEmpty()) state.apps
        else state.apps.filter {
            it.label.contains(keyword, ignoreCase = true) ||
                it.packageName.contains(keyword, ignoreCase = true)
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("搜索应用名 / 包名", color = RootMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RootMuted) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            FilterChip(
                selected = state.showSystem,
                onClick = { onShowSystemChanged(!state.showSystem) },
                label = { Text("显示系统应用") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RootGreen.copy(alpha = 0.16f),
                    selectedLabelColor = RootGreen,
                    labelColor = RootMuted
                )
            )
            Spacer(Modifier.weight(1f))
            Text(
                "已授权 ${state.allowed.size} 个",
                color = RootMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (state.appsLoading && state.apps.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RootGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp, top = 4.dp)
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Text(
                            "没有匹配的应用",
                            color = RootMuted,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
                items(filtered, key = { it.packageName }) { app ->
                    AppRow(
                        app = app,
                        checked = app.packageName in state.allowed,
                        enabled = !state.busy,
                        onCheckedChange = { onToggleApp(app.packageName, it) }
                    )
                }
                item {
                    Text(
                        "被勾选的应用调用 su 时会被放行；其他应用会被拒绝并写入授权日志。",
                        color = RootMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppRow(
    app: AppInfo,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val icon = remember(app.packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(app.packageName).toBitmap(96, 96)
        }.getOrNull()
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (checked) RootGreen.copy(alpha = 0.07f) else RootSurface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RootSurfaceHigh),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Image(
                        bitmap = icon.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(42.dp)
                    )
                } else {
                    Text(
                        app.label.take(1),
                        color = RootGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    color = RootText,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${app.packageName} · uid ${app.uid}${if (app.isSystem) " · 系统" else ""}",
                    color = RootMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF00210F),
                    checkedTrackColor = RootGreen,
                    uncheckedThumbColor = RootMuted,
                    uncheckedTrackColor = RootSurfaceHigh,
                    uncheckedBorderColor = RootMuted.copy(alpha = 0.5f)
                )
            )
        }
    }
}
