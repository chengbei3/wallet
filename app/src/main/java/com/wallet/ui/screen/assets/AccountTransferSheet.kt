package com.wallet.ui.screen.assets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wallet.data.model.Account
import com.wallet.data.model.CurrencyType
import com.wallet.ui.components.formatCurrency
import com.wallet.util.ExchangeRates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTransferSheet(
    accounts: List<Account>,
    defaultRate: Double,
    onDismiss: () -> Unit,
    onConfirm: (fromAccountId: String, toAccountId: String, amount: Double, fee: Double, rate: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var fromAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id.orEmpty()) }
    var toAccountId by remember {
        mutableStateOf(accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id.orEmpty())
    }
    var amountText by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf(ExchangeRates.format(defaultRate)) }

    val fromAccount = accounts.find { it.id == fromAccountId }
    val toAccount = accounts.find { it.id == toAccountId }
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val fee = feeText.toDoubleOrNull() ?: 0.0
    val rate = rateText.toDoubleOrNull() ?: defaultRate
    val crossCurrency = fromAccount != null && toAccount != null && fromAccount.currency != toAccount.currency
    val received = when {
        fromAccount == null || toAccount == null || amount <= 0.0 -> 0.0
        fromAccount.currency == toAccount.currency -> ExchangeRates.roundMoney(amount)
        fromAccount.currency == CurrencyType.USD && toAccount.currency == CurrencyType.CNY ->
            ExchangeRates.roundMoney(amount * rate)
        fromAccount.currency == CurrencyType.CNY && toAccount.currency == CurrencyType.USD ->
            ExchangeRates.roundMoney(amount / rate)
        else -> ExchangeRates.roundMoney(amount)
    }
    val canSubmit = fromAccountId.isNotEmpty() &&
        toAccountId.isNotEmpty() &&
        fromAccountId != toAccountId &&
        amount > 0.0 &&
        (!crossCurrency || rate > 0.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "账户互转",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "互转只调整账户余额，不会出现在记账账单，也不计入收支统计。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(text = "转出账户", style = MaterialTheme.typography.labelLarge)
            AccountChipRow(
                accounts = accounts,
                selectedId = fromAccountId,
                onSelect = { fromAccountId = it }
            )

            Text(text = "转入账户", style = MaterialTheme.typography.labelLarge)
            AccountChipRow(
                accounts = accounts,
                selectedId = toAccountId,
                onSelect = { toAccountId = it }
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("转出金额（${fromAccount?.currency?.label ?: "账户币种"}）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            if (crossCurrency) {
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("美元兑人民币汇率") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            OutlinedTextField(
                value = feeText,
                onValueChange = { feeText = it },
                label = { Text("手续费（从转出账户扣除，可留空）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            if (fromAccount != null && toAccount != null && amount > 0.0) {
                Text(
                    text = buildString {
                        append("从 ${fromAccount.name} 转出 ${formatCurrency(amount, fromAccount.currency)}")
                        if (fee > 0.0) {
                            append("，手续费 ${formatCurrency(fee, fromAccount.currency)}")
                        }
                        append("\n${toAccount.name} 入账 ${formatCurrency(received, toAccount.currency)}")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            TextButton(
                onClick = {
                    if (canSubmit) {
                        onConfirm(fromAccountId, toAccountId, amount, fee, rate)
                    }
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("确认互转", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun AccountChipRow(
    accounts: List<Account>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        accounts.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { account ->
                    FilterChip(
                        selected = selectedId == account.id,
                        onClick = { onSelect(account.id) },
                        label = { Text(account.name) }
                    )
                }
            }
        }
    }
}
