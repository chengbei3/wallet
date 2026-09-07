package com.wallet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wallet.data.model.CurrencyType
import com.wallet.ui.theme.AppCard
import com.wallet.ui.theme.AppCardColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    modifier: Modifier = Modifier,
    amountLabel: String = "折合人民币",
    amountCurrency: CurrencyType = CurrencyType.CNY,
    secondaryTitle: String? = null,
    secondaryAmount: Double? = null,
    secondaryCurrency: CurrencyType = CurrencyType.USD,
    subtitle: String? = null
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = AppCardColors.primaryContainer()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            if (secondaryTitle != null && secondaryAmount != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DualAmountColumn(
                        label = amountLabel,
                        amount = amount,
                        currency = amountCurrency,
                        modifier = Modifier.weight(1f)
                    )
                    DualAmountColumn(
                        label = secondaryTitle,
                        amount = secondaryAmount,
                        currency = secondaryCurrency,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    text = formatCurrency(amount, amountCurrency),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun DualAmountColumn(
    label: String,
    amount: Double,
    currency: CurrencyType,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
        )
        Text(
            text = formatCurrency(amount, currency),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StatRow(
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "本月收入",
            amount = income,
            isIncome = true,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "本月支出",
            amount = expense,
            isIncome = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    amount: Double,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = AppCardColors.surfaceVariant()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isIncome) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

fun formatCurrency(amount: Double, currency: CurrencyType = CurrencyType.CNY): String {
    val locale = when (currency) {
        CurrencyType.CNY -> Locale.CHINA
        CurrencyType.USD -> Locale.US
    }
    val format = NumberFormat.getCurrencyInstance(locale)
    return format.format(amount)
}

fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
    return sdf.format(java.util.Date(timestamp))
}
