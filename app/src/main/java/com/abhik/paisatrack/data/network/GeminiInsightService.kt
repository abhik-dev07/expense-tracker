package com.abhik.paisatrack.data.network

import android.util.Log

object GeminiInsightService {
    private const val TAG = "GeminiInsight"

    suspend fun getFinancialInsights(
        totalIncome: Double,
        totalExpense: Double,
        balance: Double,
        breakdownText: String
    ): String {
        try {
            Log.d(TAG, "Requesting financial insights from backend...")
            val response = ApiClient.api.getInsights(
                InsightsRequest(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    balance = balance,
                    breakdownText = breakdownText
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val insights = response.body()!!.insights
                if (insights.isNotEmpty()) {
                    return insights
                }
            } else {
                Log.e(TAG, "Backend insights API failed with code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during backend insights call: ${e.message}", e)
        }

        Log.d(TAG, "Falling back to default rule-based insights.")
        return getFallbackInsights(totalIncome, totalExpense)
    }

    fun getFallbackInsights(income: Double, expense: Double): String {
        val savingsRate = if (income > 0) (income - expense) / income else 0.0
        return when {
            expense == 0.0 -> {
                "Welcome to your Expense Tracker! Start by registering your daily transactions and custom collections to unlock rich insights.\n\nSetting budget limits on collections (like Food & Dining) helps you track and prevent overspending before it happens.\n\nYour net balance will calculate automatically to reflect your actual real-time financial health."
            }
            savingsRate < 0.0 -> {
                "Your expenses exceed your current income. Consider inspecting your collections to identify flexible lifestyle items that can be paused.\n\nAdding budget limits on high-expense collections will provide real-time warning indicators as you approach your limits.\n\nSmall recurring daily costs (like high daily subscriptions) often add up to significant figures over a full calendar month."
            }
            savingsRate < 0.2 -> {
                "You are saving ${String.format("%.0f%%", savingsRate * 100)} of your total income. Aiming for a 20% savings margin is a standard roadmap to long-term financial freedom.\n\nKeep tracking your transactions! Grouping similar expenses inside custom collections highlights sneaky leaks.\n\nWhenever you register a new income stream, consider setting aside 10% immediately into storage before allocation."
            }
            else -> {
                "Congratulations on keeping an exceptional savings margin of ${String.format("%.0f%%", savingsRate * 100)}! You are positioned securely in the green zone.\n\nReview your lower-activity collection budgets and re-allocate inactive caps into savings goals or debt reduction.\n\nConsistency is key. Registering your expenses daily takes under 2 minutes and prevents end-of-month surprises."
            }
        }
    }
}
