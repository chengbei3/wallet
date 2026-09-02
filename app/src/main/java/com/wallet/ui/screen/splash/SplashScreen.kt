package com.wallet.ui.screen.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay

private const val SPLASH_DISPLAY_MS = 500L
private const val SPLASH_FADE_MS = 150L

@Composable
fun SplashScreen(
    splashImageUri: String?,
    onFinished: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }
    var finished by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(SPLASH_FADE_MS.toInt()),
        label = "splash_alpha"
    )

    fun finishSplash() {
        if (finished) return
        finished = true
        visible = false
    }

    LaunchedEffect(splashImageUri) {
        if (splashImageUri == null) {
            onFinished()
            return@LaunchedEffect
        }
        delay(SPLASH_DISPLAY_MS)
        finishSplash()
    }

    LaunchedEffect(visible, finished) {
        if (finished && !visible) {
            delay(SPLASH_FADE_MS)
            onFinished()
        }
    }

    if (splashImageUri == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { finishSplash() })
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(splashImageUri)
                .crossfade(false)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
