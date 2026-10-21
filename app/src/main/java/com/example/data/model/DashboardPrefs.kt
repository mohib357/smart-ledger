package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

data class DashboardPrefs(
    val showQuickActions: Boolean = true,
    val showAiInsight: Boolean = true,
    val showChart: Boolean = true,
    val showWallets: Boolean = true,
    val showParties: Boolean = true,
    val showRecentTransactions: Boolean = true,
    val showEarnCoins: Boolean = true,
    val showNomineeBanner: Boolean = true,
    val widgetOrder: List<String> = listOf(
        "quick_actions", "earn_coins", "nominee_banner", "ai_insight",
        "chart", "wallets", "parties", "recent_transactions"
    )
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
            put("widgetOrder", JSONArray(widgetOrder))
        }.toString()
    }

    companion object {
        private val DEFAULT_ORDER = listOf(
            "quick_actions", "earn_coins", "nominee_banner", "ai_insight",
            "chart", "wallets", "parties", "recent_transactions"
        )

        fun fromJson(json: String): DashboardPrefs {
            return try {
                val obj = JSONObject(json)
                val orderArr = obj.optJSONArray("widgetOrder")
                val order: List<String> = if (orderArr != null) {
                    (0 until orderArr.length()).map { orderArr.getString(it) }
                } else {
                    DEFAULT_ORDER
                }
                DashboardPrefs(
                    showQuickActions = obj.optBoolean("showQuickActions", true),
                    showAiInsight = obj.optBoolean("showAiInsight", true),
                    showChart = obj.optBoolean("showChart", true),
                    showWallets = obj.optBoolean("showWallets", true),
                    showParties = obj.optBoolean("showParties", true),
                    showRecentTransactions = obj.optBoolean("showRecentTransactions", true),
                    showEarnCoins = obj.optBoolean("showEarnCoins", true),
                    showNomineeBanner = obj.optBoolean("showNomineeBanner", true),
                    widgetOrder = order
                )
            } catch (e: Exception) {
                DashboardPrefs()
            }
        }
    }
}
