package com.wallet.ui.screen.accounting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wallet.data.model.Account
import com.wallet.data.model.CurrencyType
import com.wallet.data.model.Transaction
import com.wallet.data.model.TransactionType
import com.wallet.data.model.expenseCategories
import com.wallet.data.model.incomeCategories
import com.wallet.ui.components.AmountNumpad
import com.wallet.ui.components.StatRow
import com.wallet.ui.components.appendAmountInput
import com.wallet.ui.theme.AppCard
import com.wallet.ui.theme.AppCardColors
import com.wallet.ui.components.TransparentBarDefaults
import com.wallet.ui.components.formatCurrency
import com.wallet.ui.components.formatDate
import com.wallet.ui.theme.ExpenseRed
import com.wallet.ui.theme.IncomeGreen
import com.wallet.ui.viewmodel.WalletViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingScreen(
    viewModel: WalletViewModel,
    topBarAlpha: Float = 0f
) {
    val transactions by viewModel.transactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showStatsSheet by remember { mutableStateOf(false) }
    var wallpaperOnlyMode by remember { mutableStateOf(false) }
    var hasAutoOpenedAdd by remember { mutableStateOf(false) }

    LaunchedEffect(profile.openAddTransactionOnStart) {
        if (profile.openAddTransactionOnStart && !hasAutoOpenedAdd) {
            delay(200)
            showAddSheet = true
            hasAutoOpenedAdd = true
        }
    }

    val toggleWallpaperMode = {
        wallpaperOnlyMode = !wallpaperOnlyMode
        viewModel.setAccountingWallpaperOnly(wallpaperOnlyMode)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { toggleWallpaperMode() })
            }
    ) {
        if (wallpaperOnlyMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = { toggleWallpaperMode() })
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "双击恢复界面",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        } else {
            AccountingContent(
                viewModel = viewModel,
                transactions = transactions.filter { !it.isTransfer },
                accounts = accounts,
                topBarAlpha = topBarAlpha,
                onShowStats = { showStatsSheet = true },
                onShowAdd = { showAddSheet = true },
                onEditTransaction = { editingTransaction = it }
            )
        }
    }

    if (showAddSheet || editingTransaction != null) {
        val editing = editingTransaction
        AddTransactionSheet(
            accounts = accounts,
            existing = editing,
            bindings = profile.categoryAccountBindings,
            sheetAlpha = profile.cardBackgroundAlpha.coerceIn(0.55f, 0.96f),
            onDismiss = {
                showAddSheet = false
                editingTransaction = null
            },
            onConfirm = { amount, type, category, note, accountId, excludeFromStats ->
                viewModel.bindCategoryAccount(category, accountId)
                if (editing != null) {
                    viewModel.updateTransaction(
                        editing.copy(
                            amount = amount,
                            type = type,
                            category = category,
                            note = note,
                            accountId = accountId,
                            excludeFromStats = excludeFromStats
                        )
                    )
                } else {
                    viewModel.addTransaction(amount, type, category, note, accountId, excludeFromStats)
                }
                showAddSheet = false
                editingTransaction = null
            }
        )
    }

    if (showStatsSheet) {
        StatisticsSheet(
            viewModel = viewModel,
            onDismiss = { showStatsSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountingContent(
    viewModel: WalletViewModel,
    transactions: List<Transaction>,
    accounts: List<Account>,
    topBarAlpha: Float,
    onShowStats: () -> Unit,
    onShowAdd: () -> Unit,
    onEditTransaction: (Transaction) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TransparentBarDefaults.AppTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                containerAlpha = topBarAlpha,
                title = { Text("记账") },
                actions = {
                    IconButton(onClick = onShowStats) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "统计"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onShowAdd,
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
                color = MaterialTheme.colorScheme.onBackground,
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
                        val account = accounts.find { it.id == transaction.accountId }
                        TransactionItem(
                            transaction = transaction,
                            accountName = account?.name.orEmpty(),
                            currency = account?.currency ?: CurrencyType.CNY,
                            onClick = { onEditTransaction(transaction) },
                            onDelete = { viewModel.deleteTransaction(transaction) }
                        )
                    }
                }
            }
        }
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
            text = "点击账单可修改，点击右下角开始记账",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    accountName: String,
    currency: CurrencyType,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (transaction.excludeFromStats) {
                        Text(
                            text = "不计收支",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
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
                        "+${formatCurrency(transaction.amount, currency)}"
                    } else {
                        "-${formatCurrency(transaction.amount, currency)}"
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddTransactionSheet(
    accounts: List<Account>,
    existing: Transaction? = null,
    bindings: Map<String, String> = emptyMap(),
    sheetAlpha: Float = 0.88f,
    onDismiss: () -> Unit,
    onConfirm: (Double, TransactionType, String, String, String, Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val initialCategories = if (existing?.type == TransactionType.INCOME) {
        incomeCategories
    } else {
        expenseCategories
    }
    fun boundAccountId(categoryName: String): String? {
        val bound = bindings[categoryName]
        return if (bound != null && accounts.any { it.id == bound }) bound else null
    }
    var amountText by remember {
        mutableStateOf(existing?.amount?.let { formatAmountInput(it) } ?: "")
    }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var selectedType by remember { mutableStateOf(existing?.type ?: TransactionType.EXPENSE) }
    var selectedCategory by remember {
        mutableStateOf(
            initialCategories.find { it.name == existing?.category } ?: initialCategories.first()
        )
    }
    var selectedAccountId by remember {
        mutableStateOf(
            existing?.accountId
                ?: boundAccountId(initialCategories.first().name)
                ?: accounts.firstOrNull()?.id.orEmpty()
        )
    }
    var excludeFromStats by remember { mutableStateOf(existing?.excludeFromStats ?: false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val noteInteractionSource = remember { MutableInteractionSource() }
    val noteFocused by noteInteractionSource.collectIsFocusedAsState()
    val selectedAccount = accounts.find { it.id == selectedAccountId }
    val amountSymbol = selectedAccount?.currency?.symbol ?: "¥"

    val categories = if (selectedType == TransactionType.EXPENSE) {
        expenseCategories
    } else {
        incomeCategories
    }
    val canSubmit = amountText.toDoubleOrNull()?.let { it > 0 } == true &&
        selectedAccountId.isNotEmpty()

    LaunchedEffect(selectedCategory.name, selectedType) {
        if (existing != null &&
            selectedCategory.name == existing.category &&
            selectedType == existing.type
        ) {
            return@LaunchedEffect
        }
        boundAccountId(selectedCategory.name)?.let { selectedAccountId = it }
    }

    fun typeAmount(key: String) {
        focusManager.clearFocus()
        keyboardController?.hide()
        amountText = appendAmountInput(amountText, key)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = sheetAlpha),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (existing != null) "修改记账" else "添加记账",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HighContrastChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = {
                        selectedType = TransactionType.EXPENSE
                        selectedCategory = expenseCategories.first()
                    },
                    label = "支出",
                    selectedContainer = ExpenseRed,
                    selectedContent = Color.White
                )
                HighContrastChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = {
                        selectedType = TransactionType.INCOME
                        selectedCategory = incomeCategories.first()
                    },
                    label = "收入",
                    selectedContainer = IncomeGreen,
                    selectedContent = Color.White
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF161616).copy(alpha = sheetAlpha))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = amountSymbol,
                    color = Color(0xFFBDBDBD),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 6.dp, bottom = 1.dp)
                )
                Text(
                    text = amountText.ifEmpty { "0" },
                    color = if (amountText.isEmpty()) Color(0xFF6E6E6E) else Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "分类",
                style = MaterialTheme.typography.labelMedium
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { category ->
                    HighContrastChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = category.name
                    )
                }
            }

            if (accounts.isNotEmpty()) {
                Text(
                    text = "账户",
                    style = MaterialTheme.typography.labelMedium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.forEach { account ->
                        HighContrastChip(
                            selected = selectedAccountId == account.id,
                            onClick = { selectedAccountId = account.id },
                            label = account.name
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                interactionSource = noteInteractionSource
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = excludeFromStats,
                    onCheckedChange = { excludeFromStats = it }
                )
                Text(
                    text = "不计入收支",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (!noteFocused) {
                AmountNumpad(
                    onKey = { typeAmount(it) },
                    compact = true
                )
            }

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0 && selectedAccountId.isNotEmpty()) {
                        onConfirm(
                            amount,
                            selectedType,
                            selectedCategory.name,
                            note,
                            selectedAccountId,
                            excludeFromStats
                        )
                    }
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF2A2A2A),
                    disabledContentColor = Color(0xFF6E6E6E)
                )
            ) {
                Text(
                    text = if (existing != null) "保存修改" else "确认添加",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatAmountInput(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        String.format(java.util.Locale.US, "%.2f", amount).trimEnd('0').trimEnd('.')
    }
}

@Composable
private fun HighContrastChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    selectedContainer: Color = Color.White,
    selectedContent: Color = Color.Black
) {
    val container = if (selected) selectedContainer else Color(0xFF1C1C1C)
    val content = if (selected) selectedContent else Color(0xFF9E9E9E)
    val border = if (selected) selectedContainer else Color(0xFF5C5C5C)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(container)
            .border(width = 1.5.dp, color = border, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = content,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
