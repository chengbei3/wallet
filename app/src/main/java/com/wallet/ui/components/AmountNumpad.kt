package com.wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val KeyIdleBg = Color(0xFF1C1C1C)
private val KeyIdleBorder = Color(0xFF5C5C5C)
private val KeyIdleText = Color(0xFFF2F2F2)
private val KeyPressedBg = Color(0xFFF5F5F5)
private val KeyPressedText = Color(0xFF000000)
private val ConfirmEnabledBg = Color(0xFFF5F5F5)
private val ConfirmEnabledText = Color(0xFF000000)
private val ConfirmDisabledBg = Color(0xFF2A2A2A)
private val ConfirmDisabledText = Color(0xFF6E6E6E)

private val NumpadSpacing = 6.dp
private val NumpadKeyHeight = 48.dp
private val NumpadCorner = 10.dp

fun appendAmountInput(current: String, key: String): String {
    return when (key) {
        "DEL" -> current.dropLast(1)
        "00" -> appendAmountInput(appendAmountInput(current, "0"), "0")
        "." -> when {
            current.contains('.') -> current
            current.isEmpty() -> "0."
            else -> "$current."
        }
        else -> {
            if (current == "0") {
                return key
            }
            val next = current + key
            val dotIndex = next.indexOf('.')
            when {
                dotIndex >= 0 && next.length - dotIndex > 3 -> current
                next.replace(".", "").length > 10 -> current
                else -> next
            }
        }
    }
}

@Composable
fun AmountNumpad(
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = "确认",
    confirmEnabled: Boolean = true,
    onConfirm: (() -> Unit)? = null
) {
    val digitRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "00")
    )

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val keyWidth = (maxWidth - NumpadSpacing * 3) / 4
        val confirmHeight = NumpadKeyHeight * 3 + NumpadSpacing * 2

        Row(horizontalArrangement = Arrangement.spacedBy(NumpadSpacing)) {
            Column(verticalArrangement = Arrangement.spacedBy(NumpadSpacing)) {
                digitRows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(NumpadSpacing)) {
                        row.forEach { key ->
                            NumpadKey(
                                key = key,
                                onClick = { onKey(key) },
                                modifier = Modifier.size(keyWidth, NumpadKeyHeight)
                            )
                        }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(NumpadSpacing)) {
                NumpadKey(
                    key = "DEL",
                    onClick = { onKey("DEL") },
                    modifier = Modifier.size(keyWidth, NumpadKeyHeight)
                )
                if (onConfirm != null) {
                    ConfirmKey(
                        label = confirmLabel,
                        enabled = confirmEnabled,
                        onClick = onConfirm,
                        modifier = Modifier.size(keyWidth, confirmHeight)
                    )
                }
            }
        }
    }
}

@Composable
private fun NumpadKey(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val background = if (pressed) KeyPressedBg else KeyIdleBg
    val content = if (pressed) KeyPressedText else KeyIdleText
    val border = if (pressed) KeyPressedBg else KeyIdleBorder

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(NumpadCorner))
            .background(background)
            .border(width = 1.dp, color = border, shape = RoundedCornerShape(NumpadCorner))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        when (key) {
            "DEL" -> Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "删除",
                tint = content,
                modifier = Modifier.size(20.dp)
            )
            "." -> Text(
                text = ".",
                color = content,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            else -> Text(
                text = key,
                color = content,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ConfirmKey(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val background = when {
        !enabled -> ConfirmDisabledBg
        pressed -> Color.White
        else -> ConfirmEnabledBg
    }
    val content = if (enabled) ConfirmEnabledText else ConfirmDisabledText

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(NumpadCorner))
            .background(background)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = content,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
