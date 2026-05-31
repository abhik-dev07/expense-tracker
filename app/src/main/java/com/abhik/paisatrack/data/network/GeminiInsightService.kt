package com.abhik.paisatrack.data.network

import android.util.Log
import com.abhik.paisatrack.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiInsightService {
    private const val TAG = "GeminiInsight"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Moshi parser for extracting text from the JSON response
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    suspend fun getFinancialInsights(
        totalIncome: Double,
        totalExpense: Double,
        balance: Double,
        breakdownText: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "Gemini API key is not configured or placeholder. Falling back to default insights.")
            return getFallbackInsights(totalIncome, totalExpense)
        }

        try {
            val prompt = """
                You are a friendly, highly professional personal finance assistant.
                Analyze this financial dataset and supply exactly 3 distinct, practical, encouraging, and actionable money-saving micro-tips (each 1-2 short sentences max).
                
                Current Financial Snapshot:
                - Income: $$totalIncome
                - Spent: $$totalExpense
                - Cash flow (Savings): $$balance
                - Spending breakdown by Collection:
                $breakdownText
                
                Format requirement:
                Write them as plain sentences, with empty lines between the tips, without asterisks, checkboxes, numbers, or headers. Just conversational finance wisdom.
            """.trimIndent()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = """
                {
                    "contents": [{
                        "parts": [{
                            "text": ${escapeJsonString(prompt)}
                        }]
                    }]
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBody.toRequestBody(mediaType))
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "API request failed with code: ${response.code}")
                    return getFallbackInsights(totalIncome, totalExpense)
                }

                val responseBody = response.body?.string() ?: return getFallbackInsights(totalIncome, totalExpense)
                
                // Extremely simple and robust parsing
                val parsedText = extractTextFromJson(responseBody)
                return parsedText.ifEmpty { getFallbackInsights(totalIncome, totalExpense) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini Call: ${e.message}", e)
            return getFallbackInsights(totalIncome, totalExpense)
        }
    }

    private fun escapeJsonString(string: String): String {
        val escaped = StringBuilder()
        escaped.append("\"")
        for (c in string) {
            when (c) {
                '\\' -> escaped.append("\\\\")
                '\"' -> escaped.append("\\\"")
                '\n' -> escaped.append("\\n")
                '\r' -> escaped.append("\\r")
                '\t' -> escaped.append("\\t")
                else -> escaped.append(c)
            }
        }
        escaped.append("\"")
        return escaped.toString()
    }

    private fun extractTextFromJson(json: String): String {
        try {
            // Locate "text": "..." inside candidates/content/parts/text
            var index = json.indexOf("\"text\":")
            if (index != -1) {
                var start = json.indexOf("\"", index + 7)
                if (start != -1) {
                    start += 1
                    var end = start
                    while (end < json.length) {
                        if (json[end] == '\"' && json[end - 1] != '\\') {
                            break
                        }
                        end++
                    }
                    if (end < json.length) {
                        val rawText = json.substring(start, end)
                        // Simple unescape of newlines and quotes
                        return rawText
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing text", e)
        }
        return ""
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
