package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    ASSISTANT
}

object GeminiService {

    private const val MODEL_NAME = "gemini-2.0-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_INSTRUCTION = """
আপনি 'সহজ হিসাব' অ্যাপের একজন বুদ্ধিমান ও দক্ষ হিসাবরক্ষক এআই সহকারী (Accounting & Finance Assistant)। 
আপনার প্রধান কাজ হলো ব্যবহারকারীকে ব্যক্তিগত ও ব্যবসার হিসাব-নিকাশ, আয়-ব্যয় ব্যবস্থাপনা, দেনা-পাওনা খাতা নিয়ন্ত্রণ, লাভ-লোকসান পর্যালোচনা এবং সঞ্চয়ের কৌশল নিয়ে প্রাঞ্জল ও সহজ বাংলায় সাহায্য করা। 
উত্তর সর্বদা মার্জিত, সুস্পষ্ট এবং পয়েন্ট আকারে উপস্থাপন করবেন। 
প্রয়োজনে টাকার হিসাব বা উদাহরণ দিয়ে সহজে বুঝিয়ে দেবেন।
"""

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String,
        contextSummary: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                Exception("এআই সেবা চালু নেই। অ্যাপ ডেভেলপারের সাথে যোগাযোগ করুন।")
            )
        }

        val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

        val contentsArray = JSONArray()

        // Include conversation history
        val fullHistory = history + ChatMessage(sender = MessageSender.USER, text = userMessage)
        
        // Take last 12 messages to respect context limits
        val recentMessages = fullHistory.takeLast(12)

        for (msg in recentMessages) {
            val contentObj = JSONObject()
            contentObj.put("role", if (msg.sender == MessageSender.USER) "user" else "model")
            
            val partsArray = JSONArray()
            val partObj = JSONObject()
            
            // If it's the very last user message and context summary is present, append context
            val textToSend = if (msg === recentMessages.last() && !contextSummary.isNullOrBlank()) {
                "${msg.text}\n\n[ব্যবহারকারীর বর্তমান অ্যাপের খতিয়ান সংক্ষেপ:\n$contextSummary]"
            } else {
                msg.text
            }
            
            partObj.put("text", textToSend)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
        }

        val rootRequest = JSONObject()
        rootRequest.put("contents", contentsArray)

        // System Instruction
        val sysInstObj = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", SYSTEM_INSTRUCTION.trimIndent()))
        sysInstObj.put("parts", sysParts)
        rootRequest.put("systemInstruction", sysInstObj)

        // Generation Config
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)
        rootRequest.put("generationConfig", genConfig)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = rootRequest.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errObj = JSONObject(responseBodyStr).optJSONObject("error")
                        errObj?.optString("message") ?: "HTTP ${response.code}"
                    } catch (_: Exception) {
                        "HTTP ${response.code}"
                    }
                    return@withContext Result.failure(Exception("এআই সেবা ত্রুটি: $errorMsg"))
                }

                val jsonResponse = JSONObject(responseBodyStr)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        return@withContext Result.success(text)
                    }
                }
                Result.failure(Exception("কোনো উত্তর পাওয়া যায়নি। আবার চেষ্টা করুন।"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "নেটওয়ার্ক ত্রুটি ঘটেছে।"))
        }
    }

    suspend fun generateInsight(financialContext: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val apiKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (_: Exception) {
                ""
            }

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext "পরামর্শ পেতে সমস্যা হয়েছে"
            }

            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

            val userMessage = "এই আর্থিক তথ্যের উপর ভিত্তি করে একটি সংক্ষিপ্ত ব্যক্তিগতকৃত পরামর্শ দাও (২ বাক্যে): $financialContext"

            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            contentObj.put("role", "user")
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", userMessage))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)

            val rootRequest = JSONObject()
            rootRequest.put("contents", contentsArray)

            val sysInstObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", SYSTEM_INSTRUCTION.trimIndent()))
            sysInstObj.put("parts", sysParts)
            rootRequest.put("systemInstruction", sysInstObj)

            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            rootRequest.put("generationConfig", genConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootRequest.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "পরামর্শ পেতে সমস্যা হয়েছে"
                }
                val responseBodyStr = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBodyStr)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "পরামর্শ পেতে সমস্যা হয়েছে")
                    }
                }
                "পরামর্শ পেতে সমস্যা হয়েছে"
            }
        } catch (e: Exception) {
            "পরামর্শ পেতে সমস্যা হয়েছে"
        }
    }
}
