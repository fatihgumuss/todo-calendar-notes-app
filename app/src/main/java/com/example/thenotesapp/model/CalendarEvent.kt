package com.example.thenotesapp.model

import com.google.gson.annotations.SerializedName

data class CalendarEvent(
    @SerializedName("day") val day: Int,
    @SerializedName("month") val month: Int,
    @SerializedName("year") val year: Int,
    @SerializedName("events") val events: List<String>
)