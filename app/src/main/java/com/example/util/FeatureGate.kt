package com.example.util

object FeatureGate {
    const val FREE_TRANSACTION_LIMIT = 50

    fun canAddTransaction(transactionCount: Int, isPremium: Boolean): Boolean =
        isPremium || transactionCount < FREE_TRANSACTION_LIMIT

    fun canExportPdf(isPremium: Boolean): Boolean = isPremium

    fun canUseCloudSync(isPremium: Boolean): Boolean = isPremium
}
