package com.example.data.api

import android.util.Log
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
import java.util.regex.Pattern

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun extractVideoId(url: String): String? {
        val clean = url.trim().removePrefix("-").trim().removePrefix("•").trim()
        val pattern = Pattern.compile("(?:v=|/v/|youtu\\.be/|/embed/|/shorts/|/live/|\\?v=)([^#&?\\n]+)")
        val matcher = pattern.matcher(clean)
        return if (matcher.find()) {
            matcher.group(1)?.trim()
        } else if (clean.length == 11 && !clean.contains("/") && !clean.contains("?")) {
            clean
        } else {
            null
        }
    }

    suspend fun fetchOEmbedTitle(url: String): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val videoId = extractVideoId(url)
            val targetUrl = if (!videoId.isNullOrBlank()) "https://www.youtube.com/watch?v=$videoId" else url.trim().removePrefix("-").trim()
            val oEmbedUrl = "https://www.youtube.com/oembed?url=${java.net.URLEncoder.encode(targetUrl, "UTF-8")}&format=json"
            val request = Request.Builder()
                .url(oEmbedUrl)
                .header("User-Agent", "Mozilla/5.0")
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@use null
                    val json = JSONObject(body)
                    val title = json.optString("title", "YouTube Video")
                    val author = json.optString("author_name", "Creator")
                    return@use Pair(title, author)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch oEmbed info: ${e.message}")
        }
        null
    }

    suspend fun generateVideoNotes(
        videoUrl: String,
        targetLanguage: String
    ): JSONObject? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "No valid Gemini API key present in BuildConfig")
            return@withContext null
        }

        val prompt = """
            You are "Saar" (सार = essence), the world's finest study companion for students.
            Turn this YouTube video/concept into comprehensive, highly structured visual notes:
            Video URL: $videoUrl
            Target Language: $targetLanguage (natively translate explanations and flowchart labels into $targetLanguage while keeping technical keywords recognizable).
            
            Return STRICT RAW JSON ONLY (no markdown code fences, no extra text) with this exact schema:
            {
              "title": "Clear concise topic title",
              "channel": "Channel or Author name",
              "category": "STEM, History, Biology, Tech, or General",
              "durationText": "e.g. 14:20",
              "tldr": "2-3 crisp sentences capturing the core essence (Saar) of the video.",
              "key_takeaways": [
                "Takeaway 1", "Takeaway 2", "Takeaway 3", "Takeaway 4", "Takeaway 5"
              ],
              "notes": [
                {
                  "heading": "Section Heading",
                  "bullets": ["Point A", "Point B", "Point C"],
                  "timestamp_sec": 45
                }
              ],
              "flowchart": {
                "diagram_type": "FLOWCHART",
                "nodes": [
                  {"id": "n1", "label": "Start Concept", "detail": "Core input", "type": "START", "tint": "mint"},
                  {"id": "n2", "label": "Processing Step", "detail": "Transformation", "type": "STEP", "tint": "peach"},
                  {"id": "n3", "label": "Decision / Check", "detail": "Condition", "type": "DECISION", "tint": "butter"},
                  {"id": "n4", "label": "Final Result", "detail": "Outcome", "type": "END", "tint": "lavender"}
                ],
                "edges": [
                  {"from": "n1", "to": "n2", "label": "flows to"},
                  {"from": "n2", "to": "n3", "label": "evaluates"},
                  {"from": "n3", "to": "n4", "label": "yields"}
                ]
              },
              "tables": [
                {
                  "title": "Core Comparison / Stages",
                  "columns": ["Component", "Function", "Key Impact"],
                  "rows": [
                    ["Item 1", "Does X", "High efficiency"],
                    ["Item 2", "Does Y", "Safety margin"]
                  ]
                }
              ],
              "quiz": [
                {
                  "question": "Clear multiple-choice question?",
                  "options": ["Option A", "Option B", "Option C", "Option D"],
                  "answer_index": 0,
                  "why": "Clear explanation of why this answer is correct."
                }
              ],
              "flashcards": [
                {
                  "front": "Key term or concept question",
                  "back": "Concise memorable explanation",
                  "topic": "Topic Name"
                }
              ],
              "audio_script": "Conversational, engaging audio summary written to be HEARD. Short friendly sentences explaining the essence of this video like an inspiring study buddy."
            }
        """.trimIndent()

        try {
            val requestBodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)

                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.4)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Gemini API error code: ${response.code} body: ${response.body?.string()}")
                    return@use null
                }

                val responseStr = response.body?.string() ?: return@use null
                val rootJson = JSONObject(responseStr)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        val cleanText = text.trim()
                            .removePrefix("```json")
                            .removePrefix("```")
                            .removeSuffix("```")
                            .trim()
                        return@use JSONObject(cleanText)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating notes via Gemini: ${e.message}", e)
        }
        null
    }
}
