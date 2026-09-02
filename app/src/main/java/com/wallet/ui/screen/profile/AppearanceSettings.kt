package com.wallet.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wallet.data.model.FontScale
import com.wallet.data.model.LauncherIconStyle
import com.wallet.data.model.UserProfile
import com.wallet.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontScaleSheet(
    current: FontScale,
    onDismiss: () -> Unit,
    onConfirm: (FontScale) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selected by remember { mutableStateOf(current) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "字体大小",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            FontScale.entries.forEach { scale ->
                FilterChip(
                    selected = selected == scale,
                    onClick = { selected = scale },
                    label = { Text(scale.label) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            TextButton(
                onClick = { onConfirm(selected) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperOverlaySheet(
    currentAlpha: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var alpha by remember { mutableFloatStateOf(currentAlpha) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "壁纸遮罩浓度",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "调低可让背景图片更清晰，调高可让文字更易阅读",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(alpha * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = alpha,
                onValueChange = { alpha = it },
                valueRange = 0f..0.85f
            )
            TextButton(onClick = { onConfirm(alpha) }, modifier = Modifier.fillMaxWidth()) {
                Text("保存")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabBarOverlaySheet(
    currentAlpha: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var alpha by remember { mutableFloatStateOf(currentAlpha) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "底部标签栏透明度",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "0% 为完全透明，调高可让标签更易辨认",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(alpha * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = alpha,
                onValueChange = { alpha = it },
                valueRange = 0f..0.9f
            )
            TextButton(onClick = { onConfirm(alpha) }, modifier = Modifier.fillMaxWidth()) {
                Text("保存")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LauncherIconSheet(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSelectPreset: (LauncherIconStyle) -> Unit,
    onPickCustomIcon: () -> Unit,
    onClearCustomIcon: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val iconDrawables = mapOf(
        LauncherIconStyle.DEFAULT to R.drawable.ic_launcher_style_default,
        LauncherIconStyle.FOREST to R.drawable.ic_launcher_style_forest,
        LauncherIconStyle.OCEAN to R.drawable.ic_launcher_style_ocean,
        LauncherIconStyle.SUNSET to R.drawable.ic_launcher_style_sunset
    )

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "应用图标",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "桌面图标可选用预设样式；自定义图片用于开屏和应用内展示",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LauncherIconStyle.entries.forEach { style ->
                    val selected = profile.launcherIconStyle == style
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectPreset(style) }
                            .padding(12.dp)
                    ) {
                        AsyncImage(
                            model = iconDrawables[style],
                            contentDescription = style.label,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = style.label,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            Text(
                text = "自定义图标（开屏/应用内）",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            RowWithCustomIcon(profile = profile, onPickCustomIcon = onPickCustomIcon)

            if (profile.customAppIconUri != null) {
                TextButton(onClick = onClearCustomIcon) {
                    Text("清除自定义图标")
                }
            }

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("完成")
            }
        }
    }
}

@Composable
private fun RowWithCustomIcon(
    profile: UserProfile,
    onPickCustomIcon: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onPickCustomIcon)
            .padding(16.dp)
    ) {
        Column {
            if (profile.customAppIconUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profile.customAppIconUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = R.mipmap.ic_launcher,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = if (profile.customAppIconUri != null) "点击更换自定义图标" else "点击选择自定义图标",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
