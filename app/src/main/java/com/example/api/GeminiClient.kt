package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiRequest(val contents: List<GeminiContent>)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

object GeminiClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val jsonAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    suspend fun getInvestmentInsight(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "দুঃখিত, এআই ফিচারটি চালু করতে গুগল এআই স্টুডিওর Secrets প্যানেলে 'GEMINI_API_KEY' কি-টি যুক্ত করুন।\n\nতবে এআই সিমুলেশনে উত্তর:\nআপনার বিনিয়োগ পরামর্শ খুবই গুরুত্বপূর্ণ। শেয়ার বাজার বা মিউচুয়াল ফান্ডে বিনিয়োগ করার পূর্বে ফান্ডামেন্টাল এবং টেকনিক্যাল অ্যানালাইসিস করা জরুরি। দীর্ঘমেয়াদী ভালো আয়ের জন্য GP বা SQURPHARMA এর মতো ব্লু চিপ শেয়ারগুলোতে নিয়মতান্ত্রিকভাবে (SIP) বিনিয়োগ শুরু করতে পারেন।"
        }

        val requestObj = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(
                            text = """
                                You are 'Biniyog Guru' (বিনিয়োগ গুরু), an elite financial advisor specializing in the Bangladesh stock market (DSE and CSE).
                                Help the user with their investment questions, portfolio decisions, stock comparison, or financial concepts.
                                Respond entirely in friendly, easy-to-understand native Bangla.
                                If they ask for technical terms (like P/E Ratio, RSI, MACD, etc.), simplify and explain them in clear, beginner-friendly Bangla.
                                Keep your advice professional, objective, and include a disclaimer that stock market investment is subject to market risks.
                                
                                User Query: $prompt
                            """.trimIndent()
                        )
                    )
                )
            )
        )

        val requestBodyJson = jsonAdapter.toJson(requestObj)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestBodyJson.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "এপিআই কল ব্যর্থ হয়েছে (কোড: ${response.code})। অনুগ্রহ করে কিছু সময় পর পুনরায় চেষ্টা করুন।"
                }
                val responseBody = response.body?.string() ?: return@withContext "সার্ভার থেকে খালি রেসপন্স পাওয়া গেছে।"
                val geminiResponse = responseAdapter.fromJson(responseBody)
                val text = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                text ?: "কোনো এআই পরামর্শ পাওয়া যায়নি।"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "নেটওয়ার্ক সংযোগ ত্রুটি: ${e.localizedMessage}. অনুগ্রহ করে ইন্টারনেট সংযোগ চেক করুন।"
        }
    }
}
