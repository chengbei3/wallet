package com.wallet.data.local

import com.wallet.data.model.Account
import com.wallet.data.model.CurrencyType
import com.wallet.data.model.Transaction
import com.wallet.data.model.TransactionType
import com.wallet.data.model.WalletBackup
import org.json.JSONArray
import org.json.JSONObject

object WalletDataSerializer {

    fun toJson(backup: WalletBackup): String {
        return JSONObject()
            .put("version", backup.version)
            .put("exportedAt", backup.exportedAt)
            .put("accounts", accountsToJson(backup.accounts))
            .put("transactions", transactionsToJson(backup.transactions))
            .toString(2)
    }

    fun fromJson(json: String): WalletBackup {
        val root = JSONObject(json)
        return WalletBackup(
            version = root.optInt("version", 1),
            exportedAt = root.optLong("exportedAt", System.currentTimeMillis()),
            accounts = parseAccounts(root.optJSONArray("accounts")),
            transactions = parseTransactions(root.optJSONArray("transactions"))
        )
    }

    fun transactionsToJson(transactions: List<Transaction>): JSONArray {
        val array = JSONArray()
        transactions.forEach { tx ->
            array.put(
                JSONObject()
                    .put("id", tx.id)
                    .put("amount", tx.amount)
                    .put("type", tx.type.name)
                    .put("category", tx.category)
                    .put("note", tx.note)
                    .put("accountId", tx.accountId)
                    .put("timestamp", tx.timestamp)
            )
        }
        return array
    }

    fun parseTransactions(array: JSONArray?): List<Transaction> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(
                    Transaction(
                        id = item.getString("id"),
                        amount = item.getDouble("amount"),
                        type = TransactionType.valueOf(item.getString("type")),
                        category = item.getString("category"),
                        note = item.optString("note", ""),
                        accountId = item.getString("accountId"),
                        timestamp = item.getLong("timestamp")
                    )
                )
            }
        }
    }

    private fun accountsToJson(accounts: List<Account>): JSONArray {
        val array = JSONArray()
        accounts.forEach { account ->
            array.put(
                JSONObject()
                    .put("id", account.id)
                    .put("name", account.name)
                    .put("balance", account.balance)
                    .put("currency", account.currency.name)
                    .put("icon", account.icon)
            )
        }
        return array
    }

    private fun parseAccounts(array: JSONArray?): List<Account> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(
                    Account(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        balance = item.getDouble("balance"),
                        currency = CurrencyType.valueOf(item.getString("currency")),
                        icon = item.optString("icon", "account_balance_wallet")
                    )
                )
            }
        }
    }
}
