package com.wallet.ui.screen.assets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallet.data.model.Account
import com.wallet.data.model.CurrencyType
import com.wallet.ui.components.SummaryCard
import com.wallet.ui.components.WallpaperBackground
import com.wallet.ui.components.formatCurrency
import com.wallet.ui.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(viewModel: WalletViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    val totalAssets = viewModel.getTotalAssets(profile.usdToCnyRate)
    val usdTotal = accounts.filter { it.currency == CurrencyType.USD }.sumOf { it.balance }
    val cnyTotal = accounts.filter { it.currency == CurrencyType.CNY }.sumOf { it.balance }

    WallpaperBackground(wallpaperUri = profile.assetsWallpaperUri) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("资产管理") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    )
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
                            append(" × ${profile.usdToCnyRate}")
                        }
                        append(" · 共 ${accounts.size} 个账户")
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Text(
                    text = "账户列表",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
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
                            onDelete = { viewModel.deleteAccount(account.id) }
                        )
                    }
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
}

@Composable
private fun AccountItem(
    account: Account,
    usdToCnyRate: Double,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val icon = getAccountIcon(account.name)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = account.currency.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(account.balance, account.currency),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
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

private fun getAccountIcon(name: String): ImageVector {
    return when {
        name.contains("现金") -> Icons.Default.Payments
        name.contains("银行") || name.contains("储蓄") -> Icons.Default.Savings
        name.contains("信用") -> Icons.Default.CreditCard
        else -> Icons.Default.AccountBalanceWallet
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
