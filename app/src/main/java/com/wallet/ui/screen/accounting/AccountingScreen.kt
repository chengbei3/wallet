package com.wallet.ui.screen.accounting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallet.data.model.Category
import com.wallet.data.model.Transaction
import com.wallet.data.model.TransactionType
import com.wallet.data.model.expenseCategories
import com.wallet.data.model.incomeCategories
import com.wallet.ui.components.StatRow
import com.wallet.ui.components.WallpaperBackground
import com.wallet.ui.components.formatCurrency
import com.wallet.ui.components.formatDate
import com.wallet.ui.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingScreen(viewModel: WalletViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    WallpaperBackground(wallpaperUri = profile.accountingWallpaperUri) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("记账") },
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
                    Icon(Icons.Default.Add, contentDescription = "添加记账")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                StatRow(
                    income = viewModel.monthlyIncome,
                    expense = viewModel.monthlyExpense,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Text(
                    text = "账单记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (transactions.isEmpty()) {
                    EmptyTransactions()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(transactions, key = { it.id }) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                accountName = accounts.find { it.id == transaction.accountId }?.name ?: "",
                                onDelete = { viewModel.deleteTransaction(transaction) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddTransactionSheet(
            accounts = accounts,
            onDismiss = { showAddSheet = false },
            onConfirm = { amount, type, category, note, accountId ->
                viewModel.addTransaction(amount, type, category, note, accountId)
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun EmptyTransactions() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "暂无账单记录",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "点击右下角按钮开始记账",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    accountName: String,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$accountName · ${formatDate(transaction.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (transaction.type == TransactionType.INCOME) {
                        "+${formatCurrency(transaction.amount)}"
                    } else {
                        "-${formatCurrency(transaction.amount)}"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (transaction.type == TransactionType.INCOME) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
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
            title = { Text("删除记录") },
            text = { Text("确定要删除这条记账记录吗？") },
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
private fun AddTransactionSheet(
    accounts: List<com.wallet.data.model.Account>,
    onDismiss: () -> Unit,
    onConfirm: (Double, TransactionType, String, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(expenseCategories.first()) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }

    val categories = if (selectedType == TransactionType.EXPENSE) {
        expenseCategories
    } else {
        incomeCategories
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
                text = "添加记账",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = {
                        selectedType = TransactionType.EXPENSE
                        selectedCategory = expenseCategories.first()
                    },
                    label = { Text("支出") }
                )
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = {
                        selectedType = TransactionType.INCOME
                        selectedCategory = incomeCategories.first()
                    },
                    label = { Text("收入") }
                )
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("金额") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "分类",
                style = MaterialTheme.typography.labelLarge
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    CategoryChip(
                        category = category,
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }

            if (accounts.isNotEmpty()) {
                Text(
                    text = "账户",
                    style = MaterialTheme.typography.labelLarge
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts) { account ->
                        FilterChip(
                            selected = selectedAccountId == account.id,
                            onClick = { selectedAccountId = account.id },
                            label = { Text(account.name) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0 && selectedAccountId.isNotEmpty()) {
                        onConfirm(amount, selectedType, selectedCategory.name, note, selectedAccountId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("确认添加", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(category.name) }
    )
}
