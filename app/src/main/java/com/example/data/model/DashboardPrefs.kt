package com.example.data.model

import org.json.JSONObject

data class DashboardPrefs(
    val showQuickActions: Boolean = true,
    val showAiInsight: Boolean = true,
    val showChart: Boolean = true,
    val showWallets: Boolean = true,
    val showParties: Boolean = true,
    val showRecentTransactions: Boolean = true,
    val showEarnCoins: Boolean = true,
    val showNomineeBanner: Boolean = true
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("showQuickActions", showQuickActions)
            put("showAiInsight", showAiInsight)
            put("showChart", showChart)
            put("showWallets", showWallets)
            put("showParties", showParties)
            put("showRecentTransactions", showRecentTransactions)
            put("showEarnCoins", showEarnCoins)
            put("showNomineeBanner", showNomineeBanner)
        }.toString()
    }

    companion object {
        fun fromJson(json: String): DashboardPrefs {
            return try {
                val obj = JSONObject(json)
                DashboardPrefs(
                    showQuickActions = obj.optBoolean("showQuickActions", true),
                    showAiInsight = obj.optBoolean("showAiInsight", true),
                    showChart = obj.optBoolean("showChart", true),
                    showWallets = obj.optBoolean("showWallets", true),
                    showParties = obj.optBoolean("showParties", true),
                    showRecentTransactions = obj.optBoolean("showRecentTransactions", true),
                    showEarnCoins = obj.optBoolean("showEarnCoins", true),
                    showNomineeBanner = obj.optBoolean("showNomineeBanner", true)
                )
            } catch (e: Exception) {
                DashboardPrefs()
            }
        }
    }
}
