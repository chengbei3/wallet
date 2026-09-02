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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wallet.data.model.LauncherIconStyle
import com.wallet.data.model.UserProfile
import com.wallet.data.model.WallpaperPage
import com.wallet.data.repository.WalletRepository
import com.wallet.ui.components.TransparentBarDefaults
import com.wallet.ui.theme.AppCard
import com.wallet.ui.theme.AppCardColors
import com.wallet.util.ExchangeRates
import com.wallet.ui.viewmodel.WalletViewModel

private sealed class PendingImagePick {
    data class Wallpaper(val page: WallpaperPage) : PendingImagePick()
    data object Splash : PendingImagePick()
    data object AppIcon : PendingImagePick()
    data object Avatar : PendingImagePick()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: WalletViewModel,
    topBarAlpha: Float = 0f
) {
    val profile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current
    var showEditSheet by remember { mutableStateOf(false) }
    var showRateSheet by remember { mutableStateOf(false) }
    var showFontScaleSheet by remember { mutableStateOf(false) }
    var showThemeColorSheet by remember { mutableStateOf(false) }
    var showOverlaySheet by remember { mutableStateOf(false) }
    var showCardBgSheet by remember { mutableStateOf(false) }
    var showTabBarSheet by remember { mutableStateOf(false) }
    var showIconSheet by remember { mutableStateOf(false) }
    var pendingImagePick by remember { mutableStateOf<PendingImagePick?>(null) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importResultMessage by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(viewModel.exportBackupJson().toByteArray(Charsets.UTF_8))
                }
                importResultMessage = "数据已导出成功"
            }.onFailure {
                importResultMessage = "导出失败：${it.message}"
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val json = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.readBytes().toString(Charsets.UTF_8)
                }
                if (json.isNullOrBlank()) {
                    importResultMessage = "文件内容为空"
                } else {
                    pendingImportJson = json
                    showImportDialog = true
                }
            }.onFailure {
                importResultMessage = "读取文件失败：${it.message}"
            }
        }
    }

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
            pendingImagePick?.let { target ->
                when (target) {
                    is PendingImagePick.Wallpaper -> {
                        viewModel.updateWallpaper(target.page, uri.toString())
                    }
                    PendingImagePick.Splash -> {
                        viewModel.updateProfile(profile.copy(splashImageUri = uri.toString()))
                    }
                    PendingImagePick.AppIcon -> {
                        val applied = viewModel.updateCustomLauncherIcon(uri.toString())
                        importResultMessage = if (applied) {
                            "自定义图标已处理并压缩保存。若系统弹出「添加到主屏幕」，请确认后使用桌面快捷方式；应用列表中会显示相框占位图标（系统限制）。"
                        } else {
                            "自定义图标处理失败，请换一张较小的图片后重试。"
                        }
                    }
                    PendingImagePick.Avatar -> {
                        viewModel.updateProfile(profile.copy(avatarUri = uri.toString()))
                    }
                }
            }
        }
        pendingImagePick = null
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TransparentBarDefaults.AppTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                containerAlpha = topBarAlpha,
                title = { Text("我的") }
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
                    onEdit = { showEditSheet = true },
                    onAvatarClick = {
                        pendingImagePick = PendingImagePick.Avatar
                        imagePickerLauncher.launch(arrayOf("image/*"))
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                SettingsSection(
                    title = "通用设置",
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Default.CurrencyExchange,
                            title = "美元汇率",
                            subtitle = "1 USD = ${ExchangeRates.format(profile.usdToCnyRate)} CNY",
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
                            subtitle = if (profile.darkMode) "已开启深色主题" else "关闭时跟随系统",
                            showSwitch = true,
                            switchChecked = profile.darkMode,
                            onSwitchChange = {
                                viewModel.updateProfile(profile.copy(darkMode = it))
                            }
                        ),
                        SettingsItem(
                            icon = Icons.Default.Add,
                            title = "启动时打开记账",
                            subtitle = if (profile.openAddTransactionOnStart) {
                                "进入应用自动弹出添加记账"
                            } else {
                                "已关闭"
                            },
                            showSwitch = true,
                            switchChecked = profile.openAddTransactionOnStart,
                            onSwitchChange = {
                                viewModel.updateProfile(profile.copy(openAddTransactionOnStart = it))
                            }
                        )
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsSection(
                    title = "个性化",
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Default.Palette,
                            title = "页面主题色",
                            subtitle = profile.themeColor.label,
                            onClick = { showThemeColorSheet = true }
                        ),
                        SettingsItem(
                            icon = Icons.Default.FormatSize,
                            title = "字体大小",
                            subtitle = profile.fontScale.label,
                            onClick = { showFontScaleSheet = true }
                        ),
                        SettingsItem(
                            icon = Icons.Default.Layers,
                            title = "壁纸遮罩浓度",
                            subtitle = "${(profile.wallpaperOverlayAlpha * 100).toInt()}%，调低可让背景更清晰",
                            onClick = { showOverlaySheet = true }
                        ),
                        SettingsItem(
                            icon = Icons.Default.Image,
                            title = "卡片背景透明度",
                            subtitle = "${(profile.cardBackgroundAlpha * 100).toInt()}%，调低可让文字区域更通透",
                            onClick = { showCardBgSheet = true }
                        ),
                        SettingsItem(
                            icon = Icons.Default.Tab,
                            title = "标签栏透明度",
                            subtitle = "${(profile.tabBarOverlayAlpha * 100).toInt()}%，同时作用于顶部与底部栏",
                            onClick = { showTabBarSheet = true }
                        ),
                        SettingsItem(
                            icon = Icons.Default.Photo,
                            title = "开屏画面",
                            subtitle = if (profile.splashImageUri != null) "已设置，点击更换" else "未设置，点击选择图片",
                            onClick = {
                                pendingImagePick = PendingImagePick.Splash
                                imagePickerLauncher.launch(arrayOf("image/*"))
                            },
                            trailingAction = if (profile.splashImageUri != null) {
                                { viewModel.updateProfile(profile.copy(splashImageUri = null)) }
                            } else null,
                            trailingActionLabel = "清除"
                        ),
                        SettingsItem(
                            icon = Icons.Default.Apps,
                            title = "应用图标",
                            subtitle = "桌面图标：${profile.launcherIconStyle.label}",
                            onClick = { showIconSheet = true }
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
                                pendingImagePick = PendingImagePick.Wallpaper(page)
                                imagePickerLauncher.launch(arrayOf("image/*"))
                            },
                            trailingAction = if (uri != null) {
                                { viewModel.updateWallpaper(page, null) }
                            } else null,
                            trailingActionLabel = "清除"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsSection(
                    title = "数据管理",
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Default.FileDownload,
                            title = "导出记账数据",
                            subtitle = "导出账户与账单为 JSON 文件",
                            onClick = {
                                exportLauncher.launch("wallet_backup_${System.currentTimeMillis()}.json")
                            }
                        ),
                        SettingsItem(
                            icon = Icons.Default.FileUpload,
                            title = "导入记账数据",
                            subtitle = "从 JSON 文件恢复数据",
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                            }
                        )
                    )
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

    if (showThemeColorSheet) {
        ThemeColorSheet(
            current = profile.themeColor,
            onDismiss = { showThemeColorSheet = false },
            onConfirm = { themeColor ->
                viewModel.updateProfile(profile.copy(themeColor = themeColor))
                showThemeColorSheet = false
            }
        )
    }

    if (showFontScaleSheet) {
        FontScaleSheet(
            current = profile.fontScale,
            onDismiss = { showFontScaleSheet = false },
            onConfirm = { scale ->
                viewModel.updateProfile(profile.copy(fontScale = scale))
                showFontScaleSheet = false
            }
        )
    }

    if (showOverlaySheet) {
        WallpaperOverlaySheet(
            currentAlpha = profile.wallpaperOverlayAlpha,
            onDismiss = { showOverlaySheet = false },
            onConfirm = { alpha ->
                viewModel.updateProfile(profile.copy(wallpaperOverlayAlpha = alpha))
                showOverlaySheet = false
            }
        )
    }

    if (showCardBgSheet) {
        CardBackgroundOverlaySheet(
            currentAlpha = profile.cardBackgroundAlpha,
            onDismiss = { showCardBgSheet = false },
            onConfirm = { alpha ->
                viewModel.updateProfile(profile.copy(cardBackgroundAlpha = alpha))
                showCardBgSheet = false
            }
        )
    }

    if (showTabBarSheet) {
        TabBarOverlaySheet(
            currentAlpha = profile.tabBarOverlayAlpha,
            onDismiss = { showTabBarSheet = false },
            onConfirm = { alpha ->
                viewModel.updateProfile(profile.copy(tabBarOverlayAlpha = alpha))
                showTabBarSheet = false
            }
        )
    }

    if (showIconSheet) {
        LauncherIconSheet(
            profile = profile,
            onDismiss = { showIconSheet = false },
            onSelectPreset = { style ->
                viewModel.updateLauncherIconStyle(style)
                importResultMessage = "桌面图标已切换为「${style.label}」。若桌面未立即更新，请返回桌面等待几秒，或重启手机。"
            },
            onPickCustomIcon = {
                showIconSheet = false
                pendingImagePick = PendingImagePick.AppIcon
                imagePickerLauncher.launch(arrayOf("image/*"))
            },
            onClearCustomIcon = {
                viewModel.updateProfile(
                    profile.copy(
                        customAppIconUri = null,
                        launcherIconStyle = LauncherIconStyle.DEFAULT
                    )
                )
                viewModel.updateLauncherIconStyle(LauncherIconStyle.DEFAULT)
            }
        )
    }

    if (showImportDialog && pendingImportJson != null) {
        AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                pendingImportJson = null
            },
            title = { Text("导入数据") },
            text = { Text("请选择导入方式：\n\n· 合并：保留现有数据，仅添加新记录\n· 覆盖：用备份文件完全替换现有数据") },
            confirmButton = {
                TextButton(onClick = {
                    val result = viewModel.importBackupJson(pendingImportJson!!, replaceExisting = false)
                    showImportDialog = false
                    pendingImportJson = null
                    importResultMessage = when (result) {
                        is WalletRepository.ImportResult.Success ->
                            "合并成功：共 ${result.transactionCount} 条账单，${result.accountCount} 个账户"
                        is WalletRepository.ImportResult.Error -> result.message
                    }
                }) {
                    Text("合并导入")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        val result = viewModel.importBackupJson(pendingImportJson!!, replaceExisting = true)
                        showImportDialog = false
                        pendingImportJson = null
                        importResultMessage = when (result) {
                            is WalletRepository.ImportResult.Success ->
                                "覆盖成功：共 ${result.transactionCount} 条账单，${result.accountCount} 个账户"
                            is WalletRepository.ImportResult.Error -> result.message
                        }
                    }) {
                        Text("覆盖导入")
                    }
                    TextButton(onClick = {
                        showImportDialog = false
                        pendingImportJson = null
                    }) {
                        Text("取消")
                    }
                }
            }
        )
    }

    importResultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { importResultMessage = null },
            title = { Text("提示") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { importResultMessage = null }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    profile: UserProfile,
    onEdit: () -> Unit,
    onAvatarClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = AppCardColors.primaryContainer()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (profile.avatarUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profile.avatarUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "头像",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAvatarClick),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "点击更换头像",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        .clickable(onClick = onAvatarClick)
                        .padding(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
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
                    text = "点击头像可更换",
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

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = AppCardColors.surface(),
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
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
    var rateText by remember { mutableStateOf(ExchangeRates.format(currentRate)) }

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
                        onConfirm(ExchangeRates.normalize(rate))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
