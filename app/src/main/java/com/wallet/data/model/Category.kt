package com.wallet.data.model

import com.wallet.data.model.TransactionType.EXPENSE
import com.wallet.data.model.TransactionType.INCOME

data class Category(
    val name: String,
    val icon: String,
    val type: TransactionType
)

val expenseCategories = listOf(
    Category("餐饮", "restaurant", EXPENSE),
    Category("交通", "directions_car", EXPENSE),
    Category("购物", "shopping_bag", EXPENSE),
    Category("娱乐", "movie", EXPENSE),
    Category("居住", "home", EXPENSE),
    Category("医疗", "local_hospital", EXPENSE),
    Category("教育", "school", EXPENSE),
    Category("其他", "more_horiz", EXPENSE)
)

val incomeCategories = listOf(
    Category("工资", "work", INCOME),
    Category("奖金", "card_giftcard", INCOME),
    Category("投资", "trending_up", INCOME),
    Category("兼职", "handyman", INCOME),
    Category("其他", "more_horiz", INCOME)
)
