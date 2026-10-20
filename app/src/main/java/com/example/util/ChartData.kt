package com.example.util

import com.example.data.model.TransactionEntity
import java.util.Calendar

fun expenseByCategory(transactions: List<TransactionEntity>): List<Pair<String, Double>> =
    transactions
        .filter { it.type == "EXPENSE" && it.amount > 0 }
        .groupBy { it.category }
        .map { (cat, txs) -> cat to txs.sumOf { it.amount } }
        .sortedByDescending { it.second }

data class MonthBucket(
    val year: Int,
    val month: Int,          // 1-12
    val monthStartMillis: Long,
    val income: Double,
    val expense: Double
) {
    val key: String get() = "%04d-%02d".format(year, month)
}

fun monthlyIncomeExpense(transactions: List<TransactionEntity>): List<MonthBucket> {
    data class Key(val year: Int, val month0: Int) // month0 = Calendar.MONTH (0-based)
    val map = mutableMapOf<Key, Pair<Double, Double>>() // income to expense
    val cal = Calendar.getInstance()
    for (tx in transactions) {
        cal.timeInMillis = tx.dateMillis
        val k = Key(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        val (inc, exp) = map.getOrDefault(k, 0.0 to 0.0)
        map[k] = if (tx.type == "INCOME") (inc + tx.amount) to exp
                 else inc to (exp + tx.amount)
    }
    return map.entries
        .map { (k, v) ->
            val startMillis = Calendar.getInstance().apply {
                set(k.year, k.month0, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            MonthBucket(k.year, k.month0 + 1, startMillis, v.first, v.second)
        }
        .sortedBy { it.monthStartMillis }
}
