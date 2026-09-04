package com.wallet.ui.screen.assets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallet.data.model.Account
import com.wallet.data.model.CurrencyType
import com.wallet.data.model.Transaction
import com.wallet.data.model.TransactionType
import com.wallet.ui.components.SummaryCard
import com.wallet.ui.components.TransparentBarDefaults
import com.wallet.ui.components.formatCurrency
import com.wallet.ui.components.formatDate
import com.wallet.ui.theme.AppCard
import com.wallet.ui.theme.AppCardColors
import com.wallet.util.ExchangeRates
import com.wallet.ui.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    viewModel: WalletViewModel,
    topBarAlpha: Float = 0f
) {
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var ledgerAccount by remember { mutableStateOf<Account?>(null) }
    var editingBalanceAccount by remember { mutableStateOf<Account?>(null) }
    var pendingBalanceAdjust by remember { mutableStateOf<Pair<Account, Double>?>(null) }

    val totalAssets = viewModel.getTotalAssets(profile.usdToCnyRate)
    val usdTotal = accounts.filter { it.currency == CurrencyType.USD }.sumOf { it.balance }
    val cnyTotal = accounts.filter { it.currency == CurrencyType.CNY }.sumOf { it.balance }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TransparentBarDefaults.AppTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                containerAlpha = topBarAlpha,
                title = { Text("资产管理") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加账户")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
                SummaryCard(
                    title = "总资产（折合人民币）",
                    amount = totalAssets,
                    subtitle = buildString {
                        append("人民币 ${formatCurrency(cnyTotal)}")
                        if (usdTotal > 0) {
                            append(" + 美元 ${formatCurrency(usdTotal, CurrencyType.USD)}")
                            append(" × ${ExchangeRates.format(profile.usdToCnyRate)}")
                        }
                        append(" · 共 ${accounts.size} 个账户")
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Text(
                    text = "账户列表",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(accounts, key = { it.id }) { account ->
                        AccountItem(
                            account = account,
                            usdToCnyRate = profile.usdToCnyRate,
                            onNameClick = { ledgerAccount = account },
                            onBalanceClick = { editingBalanceAccount = account },
                            onDelete = { viewModel.deleteAccount(account.id) }
                        )
                    }
                }
            }
        }

    if (showAddSheet) {
        AddAccountSheet(
            onDismiss = { showAddSheet = false },
            onConfirm = { name, balance, currency ->
                viewModel.addAccount(name, balance, currency)
                showAddSheet = false
            }
        )
    }

    ledgerAccount?.let { account ->
        AccountLedgerSheet(
            account = account,
            transactions = transactions.filter { it.accountId == account.id },
            onDismiss = { ledgerAccount = null }
        )
    }

    editingBalanceAccount?.let { account ->
        EditAccountBalanceSheet(
            account = account,
            onDismiss = { editingBalanceAccount = null },
            onConfirm = { newBalance ->
                editingBalanceAccount = null
                if (newBalance != account.balance) {
                    pendingBalanceAdjust = account to newBalance
                } else {
                    viewModel.updateAccount(account.copy(balance = newBalance))
                }
            }
        )
    }

    pendingBalanceAdjust?.let { (account, newBalance) ->
        val delta = newBalance - account.balance
        val deltaText = if (delta > 0) {
            "+${formatCurrency(delta, account.currency)}"
        } else {
            formatCurrency(delta, account.currency)
        }
        AlertDialog(
            onDismissRequest = { pendingBalanceAdjust = null },
            title = { Text("生成差额账单？") },
            text = {
                Text(
                    "余额将从 ${formatCurrency(account.balance, account.currency)} 改为 ${formatCurrency(newBalance, account.currency)}，差额 $deltaText。\n\n生成后可在该账户流水中查看，且不计入本月收支统计。"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.adjustAccountBalance(account, newBalance, createDifferenceBill = true)
                        pendingBalanceAdjust = null
                    }
                ) {
                    Text("生成账单")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.adjustAccountBalance(account, newBalance, createDifferenceBill = false)
                            pendingBalanceAdjust = null
                        }
                    ) {
                        Text("仅改余额")
                    }
                    TextButton(onClick = { pendingBalanceAdjust = null }) {
                        Text("取消")
                    }
                }
            }
        )
    }
}

@Composable
private fun AccountItem(
    account: Account,
    usdToCnyRate: Double,
    onNameClick: () -> Unit,
    onBalanceClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = AppCardColors.surface(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onNameClick)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = account.currency.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.clickable(onClick = onBalanceClick)
                ) {
                    Text(
                        text = formatCurrency(account.balance, account.currency),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (account.currency == CurrencyType.USD) {
                        Text(
                            text = "≈ ${formatCurrency(account.balance * usdToCnyRate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除账户") },
            text = { Text("确定要删除「${account.name}」账户吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountLedgerSheet(
    account: Account,
    transactions: List<Transaction>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "${account.name}流水",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "当前余额 ${formatCurrency(account.balance, account.currency)} · 共 ${transactions.size} 笔",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            if (transactions.isEmpty()) {
                Text(
                    text = "该账户暂无流水",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(transactions, key = { it.id }) { transaction ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = transaction.category,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                val detail = buildString {
                                    if (transaction.note.isNotBlank()) {
                                        append(transaction.note)
                                        append(" · ")
                                    }
                                    append(formatDate(transaction.timestamp))
                                }
                                Text(
                                    text = detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (transaction.type == TransactionType.INCOME) {
                                    "+${formatCurrency(transaction.amount, account.currency)}"
                                } else {
                                    "-${formatCurrency(transaction.amount, account.currency)}"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (transaction.type == TransactionType.INCOME) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, CurrencyType) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(CurrencyType.CNY) }

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
                text = "添加账户",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("账户名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("如：现金、银行卡") }
            )

            Text(
                text = "账户币种",
                style = MaterialTheme.typography.labelLarge
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CurrencyType.entries.forEach { currency ->
                    FilterChip(
                        selected = selectedCurrency == currency,
                        onClick = { selectedCurrency = currency },
                        label = { Text(currency.label) }
                    )
                }
            }

            OutlinedTextField(
                value = balanceText,
                onValueChange = { balanceText = it },
                label = { Text("初始余额（${selectedCurrency.label}）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            TextButton(
                onClick = {
                    val balance = balanceText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), balance, selectedCurrency)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("确认添加", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAccountBalanceSheet(
    account: Account,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var balanceText by remember {
        mutableStateOf(
            if (account.balance == account.balance.toLong().toDouble()) {
                account.balance.toLong().toString()
            } else {
                String.format(java.util.Locale.US, "%.2f", account.balance).trimEnd('0').trimEnd('.')
            }
        )
    }

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
                text = "修改「${account.name}」余额",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "当前余额 ${formatCurrency(account.balance, account.currency)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = balanceText,
                onValueChange = { balanceText = it },
                label = { Text("新余额（${account.currency.label}）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            TextButton(
                onClick = {
                    val newBalance = balanceText.toDoubleOrNull()
                    if (newBalance != null) {
                        onConfirm(newBalance)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("确定", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
