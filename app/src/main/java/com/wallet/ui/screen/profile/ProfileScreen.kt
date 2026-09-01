package com.wallet.ui.screen.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallet.data.model.UserProfile
import com.wallet.data.model.WallpaperPage
import com.wallet.ui.components.WallpaperBackground
import com.wallet.ui.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: WalletViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current
    var showEditSheet by remember { mutableStateOf(false) }
    var showRateSheet by remember { mutableStateOf(false) }
    var pendingWallpaperPage by remember { mutableStateOf<WallpaperPage?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setNotificationsEnabled(true)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // 部分 URI 不支持持久化权限，仍可临时使用
            }
            pendingWallpaperPage?.let { page ->
                viewModel.updateWallpaper(page, uri.toString())
            }
        }
        pendingWallpaperPage = null
    }

    WallpaperBackground(wallpaperUri = profile.profileWallpaperUri) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("我的") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                ProfileHeader(
                    profile = profile,
                    onEdit = { showEditSheet = true }
                )

                Spacer(modifier = Modifier.height(24.dp))

                SettingsSection(
                    title = "通用设置",
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Default.CurrencyExchange,
                            title = "美元汇率",
                            subtitle = "1 USD = ${profile.usdToCnyRate} CNY",
                            onClick = { showRateSheet = true }
                        ),
                        SettingsItem(
                            icon = Icons.Default.Notifications,
                            title = "消息通知",
                            subtitle = if (profile.notificationsEnabled) {
                                "每天 10:00 推送当前总资产"
                            } else {
                                "已关闭"
                            },
                            showSwitch = true,
                            switchChecked = profile.notificationsEnabled,
                            onSwitchChange = { enabled ->
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    } else {
                                        viewModel.setNotificationsEnabled(true)
                                    }
                                } else {
                                    viewModel.setNotificationsEnabled(false)
                                }
                            }
                        ),
                        SettingsItem(
                            icon = Icons.Default.DarkMode,
                            title = "深色模式",
                            subtitle = "跟随系统",
                            showSwitch = true,
                            switchChecked = profile.darkMode,
                            onSwitchChange = {
                                viewModel.updateProfile(profile.copy(darkMode = it))
                            }
                        )
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsSection(
                    title = "背景壁纸",
                    items = WallpaperPage.entries.map { page ->
                        val uri = when (page) {
                            WallpaperPage.ACCOUNTING -> profile.accountingWallpaperUri
                            WallpaperPage.ASSETS -> profile.assetsWallpaperUri
                            WallpaperPage.PROFILE -> profile.profileWallpaperUri
                        }
                        SettingsItem(
                            icon = Icons.Default.Image,
                            title = "${page.title}页壁纸",
                            subtitle = if (uri != null) "已设置，点击更换" else "未设置，点击选择图片",
                            onClick = {
                                pendingWallpaperPage = page
                                imagePickerLauncher.launch(arrayOf("image/*"))
                            },
                            trailingAction = if (uri != null) {
                                {
                                    viewModel.updateWallpaper(page, null)
                                }
                            } else null,
                            trailingActionLabel = "清除"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsSection(
                    title = "其他",
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Default.Security,
                            title = "隐私与安全",
                            subtitle = "数据保护与权限"
                        ),
                        SettingsItem(
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
                            title = "帮助与反馈",
                            subtitle = "常见问题与意见反馈"
                        ),
                        SettingsItem(
                            icon = Icons.Default.Info,
                            title = "关于",
                            subtitle = "版本 1.0.0"
                        )
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showEditSheet) {
        EditProfileSheet(
            profile = profile,
            onDismiss = { showEditSheet = false },
            onConfirm = { updated ->
                viewModel.updateProfile(updated)
                showEditSheet = false
            }
        )
    }

    if (showRateSheet) {
        ExchangeRateSheet(
            currentRate = profile.usdToCnyRate,
            onDismiss = { showRateSheet = false },
            onConfirm = { rate ->
                viewModel.updateExchangeRate(rate)
                showRateSheet = false
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    profile: UserProfile,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .padding(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.nickname,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (profile.email.isNotBlank()) {
                    Text(
                        text = profile.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "汇率：1 USD = ${profile.usdToCnyRate} CNY",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
            TextButton(onClick = onEdit) {
                Text("编辑")
            }
        }
    }
}

private data class SettingsItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
    val showSwitch: Boolean = false,
    val switchChecked: Boolean = false,
    val onSwitchChange: (Boolean) -> Unit = {},
    val onClick: (() -> Unit)? = null,
    val trailingAction: (() -> Unit)? = null,
    val trailingActionLabel: String? = null
)

@Composable
private fun SettingsSection(
    title: String,
    items: List<SettingsItem>
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        items.forEachIndexed { index, item ->
            SettingsRow(item = item)
            if (index < items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(item: SettingsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (item.onClick != null && !item.showSwitch) {
                    Modifier.clickable(onClick = item.onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (item.showSwitch) {
            Switch(
                checked = item.switchChecked,
                onCheckedChange = item.onSwitchChange
            )
        } else {
            if (item.trailingAction != null && item.trailingActionLabel != null) {
                TextButton(onClick = item.trailingAction) {
                    Text(item.trailingActionLabel)
                }
            }
            if (item.onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onConfirm: (UserProfile) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nickname by remember { mutableStateOf(profile.nickname) }
    var email by remember { mutableStateOf(profile.email) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "编辑个人信息",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("昵称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("邮箱") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            TextButton(
                onClick = {
                    if (nickname.isNotBlank()) {
                        onConfirm(profile.copy(nickname = nickname.trim(), email = email.trim()))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExchangeRateSheet(
    currentRate: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rateText by remember { mutableStateOf(currentRate.toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "设置美元汇率",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "总资产计算方式：美元账户余额 × 汇率 + 人民币账户余额",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = rateText,
                onValueChange = { rateText = it },
                label = { Text("1 美元 = ? 人民币") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            TextButton(
                onClick = {
                    val rate = rateText.toDoubleOrNull()
                    if (rate != null && rate > 0) {
                        onConfirm(rate)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
