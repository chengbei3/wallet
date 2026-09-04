package com.wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val KeyIdleBg = Color(0xFF1C1C1C)
private val KeyIdleBorder = Color(0xFF5C5C5C)
private val KeyIdleText = Color(0xFFF2F2F2)
private val KeyPressedBg = Color(0xFFF5F5F5)
private val KeyPressedText = Color(0xFF000000)

fun appendAmountInput(current: String, key: String): String {
    return when (key) {
        "DEL" -> current.dropLast(1)
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
    compact: Boolean = false
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "DEL")
    )
    val spacing = if (compact) 4.dp else 8.dp
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                row.forEach { key ->
                    NumpadKey(
                        key = key,
                        onClick = { onKey(key) },
                        compact = compact,
                        modifier = Modifier.weight(1f)
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
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val background = if (pressed) KeyPressedBg else KeyIdleBg
    val content = if (pressed) KeyPressedText else KeyIdleText
    val border = if (pressed) KeyPressedBg else KeyIdleBorder

    Box(
        modifier = modifier
            .height(if (compact) 40.dp else 52.dp)
            .clip(RoundedCornerShape(if (compact) 10.dp else 12.dp))
            .background(background)
            .border(width = 1.5.dp, color = border, shape = RoundedCornerShape(if (compact) 10.dp else 12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (key == "DEL") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "删除",
                tint = content,
                modifier = Modifier.size(if (compact) 18.dp else 22.dp)
            )
        } else {
            Text(
                text = key,
                color = content,
                fontSize = if (compact) 18.sp else 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
