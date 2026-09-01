package com.wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun WallpaperBackground(
    wallpaperUri: String?,
    modifier: Modifier = Modifier,
    overlayAlpha: Float = 0.55f,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (wallpaperUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(wallpaperUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 1f
            )
            val overlayColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
                Color.Black
            } else {
                Color.Black
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayColor.copy(alpha = overlayAlpha))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }
        content()
    }
}

private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
