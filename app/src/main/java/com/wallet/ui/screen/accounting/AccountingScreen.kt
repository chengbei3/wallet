package com.wallet.ui.screen.accounting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
            monthlyIncome = viewModel.monthlyIncome,
            monthlyExpense = viewModel.monthlyExpense,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionSheet(
    accounts: List<Account>,
    existing: Transaction? = null,
    monthlyIncome: Double = 0.0,
    monthlyExpense: Double = 0.0,
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
    val maxSheetHeight = LocalConfiguration.current.screenHeightDp.dp * 0.92f

    val categories = if (selectedType == TransactionType.EXPENSE) {
        expenseCategories
    } else {
        incomeCategories
    }
    val canSubmit = amountText.toDoubleOrNull()?.let { it > 0 } == true &&
        selectedAccountId.isNotEmpty()
    val monthlyLabel = if (selectedType == TransactionType.EXPENSE) "本月支出" else "本月收入"
    val monthlyAmount = if (selectedType == TransactionType.EXPENSE) monthlyExpense else monthlyIncome
    val monthlyColor = if (selectedType == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
    val confirmLabel = if (existing != null) "保存\n修改" else "确认\n添加"

    fun submit() {
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
    }

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
                .heightIn(max = maxSheetHeight)
                .imePadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existing != null) "修改记账" else "添加记账",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = monthlyLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(monthlyAmount),
                            color = monthlyColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        .clickable {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = amountSymbol,
                        color = Color(0xFFBDBDBD),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 6.dp, bottom = 2.dp)
                    )
                    Text(
                        text = amountText.ifEmpty { "0" },
                        color = if (amountText.isEmpty()) Color(0xFF6E6E6E) else Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                ChipPickerRow(
                    label = "分类",
                    items = categories.map { it.name to it },
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it }
                )

                if (accounts.isNotEmpty()) {
                    ChipPickerRow(
                        label = "账户",
                        items = accounts.map { it.name to it },
                        selected = accounts.find { it.id == selectedAccountId },
                        onSelect = { selectedAccountId = it.id }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BasicTextField(
                        value = note,
                        onValueChange = { note = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(Color.White),
                        interactionSource = noteInteractionSource,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 1.dp,
                                color = Color(0xFF5C5C5C),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (note.isEmpty()) {
                                    Text(
                                        text = "备注（可选）",
                                        color = Color(0xFF6E6E6E),
                                        fontSize = 14.sp
                                    )
                                }
                                inner()
                            }
                        }
                    )
                    Row(
                        modifier = Modifier.clickable { excludeFromStats = !excludeFromStats },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = excludeFromStats,
                            onCheckedChange = { excludeFromStats = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color.White,
                                uncheckedColor = Color(0xFF5C5C5C),
                                checkmarkColor = Color.Black
                            )
                        )
                        Text(
                            text = "不计收支",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (noteFocused) {
                Button(
                    onClick = ::submit,
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
            } else {
                AmountNumpad(
                    onKey = { typeAmount(it) },
                    confirmLabel = confirmLabel,
                    confirmEnabled = canSubmit,
                    onConfirm = ::submit
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
private fun <T> ChipPickerRow(
    label: String,
    items: List<Pair<String, T>>,
    selected: T?,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (name, item) ->
                HighContrastChip(
                    selected = selected == item,
                    onClick = { onSelect(item) },
                    label = name
                )
            }
        }
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
