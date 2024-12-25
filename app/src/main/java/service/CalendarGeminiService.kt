package com.example.thenotesapp.service

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.thenotesapp.model.CalendarEvent
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class CalendarGeminiService(context: Context) {
    private val apiKey = "AIzaSyCwNWX1WJk44RLdKtqmqtffXbbEJG3kxLs" // Replace with your actual API key
    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }
    private val gson = Gson()
    private val TAG = "CalendarGeminiService"

    suspend fun extractEventsFromImages(images: List<Bitmap>): List<CalendarEvent> = withContext(Dispatchers.IO) {
        try {
            val events = mutableListOf<CalendarEvent>()

            // Process first image to get month and year
            val firstImagePrompt = """
                You are a calendar event parser. Analyze this calendar image and extract ONLY the month and year.
                Return the result in this EXACT format (no other text):
                {"month": NUMBER, "year": NUMBER}
            """.trimIndent()

            val firstImageContent = content {
                image(images.first())
                text(firstImagePrompt)
            }

            val monthYearResponse = model.generateContent(firstImageContent).text?.trim() ?:
            throw IllegalStateException("No response from Gemini API")

            Log.d(TAG, "Month/Year Response: $monthYearResponse")

            // Clean and validate the JSON response
            val monthYearJson = try {
                cleanJsonResponse(monthYearResponse)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clean month/year JSON: $monthYearResponse", e)
                monthYearResponse // Use original response if cleaning fails
            }

            val monthYearData = try {
                JSONObject(monthYearJson)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse month/year JSON: $monthYearJson", e)
                throw IllegalStateException("Invalid month/year format from API")
            }

            val month = monthYearData.getInt("month")
            val year = monthYearData.getInt("year")

            // Process each image for events
            for (image in images) {
                val prompt = """
                    You are a calendar event parser. Analyze this calendar image and extract the events.
                    Return ONLY a JSON array in this EXACT format (no other text):
                    [{"day": NUMBER, "month": $month, "year": $year, "events": ["Event 1", "Event 2"]}]
                """.trimIndent()

                val content = content {
                    image(image)
                    text(prompt)
                }

                val response = model.generateContent(content).text?.trim() ?:
                throw IllegalStateException("No response from Gemini API")

                Log.d(TAG, "Events Response: $response")

                try {
                    val cleanedJson = cleanJsonResponse(response)
                    Log.d(TAG, "Cleaned JSON: $cleanedJson")

                    val extractedEvents = parseEventsJson(cleanedJson)
                    events.addAll(extractedEvents)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse events JSON: $response", e)
                    // Continue with next image instead of failing completely
                    continue
                }
            }

            if (events.isEmpty()) {
                Log.w(TAG, "No events were extracted from any of the images")
            }

            return@withContext events
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting events", e)
            throw e
        }
    }

    private fun cleanJsonResponse(response: String): String {
        Log.d(TAG, "Original response: $response")

        // Remove any text before and after the JSON array, including "Ask Gemini" text
        val withoutExtra = response.replace("(Ask Gemini)", "")
            .replace("```json", "")
            .replace("```", "")
            .trim()

        // Find the first '[' and the last ']' for array responses
        val startIndex = withoutExtra.indexOf('[')
        val endIndex = withoutExtra.lastIndexOf(']')

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return withoutExtra.substring(startIndex, endIndex + 1)
                .replace("\n", "")
                .replace("\\s+".toRegex(), " ")
                .trim()
        }

        // If no array found, try object format
        val objStartIndex = withoutExtra.indexOf('{')
        val objEndIndex = withoutExtra.lastIndexOf('}')

        if (objStartIndex != -1 && objEndIndex != -1 && objStartIndex < objEndIndex) {
            return withoutExtra.substring(objStartIndex, objEndIndex + 1)
                .replace("\n", "")
                .replace("\\s+".toRegex(), " ")
                .trim()
        }

        throw IllegalArgumentException("No valid JSON found in response")
    }

    private fun parseEventsJson(jsonStr: String): List<CalendarEvent> {
        return try {
            // Try parsing as JSONArray first
            val jsonArray = JSONArray(jsonStr)
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                CalendarEvent(
                    day = obj.getInt("day"),
                    month = obj.getInt("month"),
                    year = obj.getInt("year"),
                    events = obj.getJSONArray("events").let { array ->
                        List(array.length()) { array.getString(it) }
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse with JSONArray, trying Gson: $e")
            try {
                // If JSONArray fails, try Gson
                gson.fromJson<List<CalendarEvent>>(jsonStr, object : TypeToken<List<CalendarEvent>>() {}.type)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse with Gson: $e")
                throw e
            }
        }
    }
}