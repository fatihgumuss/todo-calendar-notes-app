package com.example.thenotesapp

data class HolidayResponse(
    val response: Holidays
)

data class Holidays(
    val holidays: List<Holiday>
)

data class Holiday(
    val name: String,
    val date: DateInfo
)

data class DateInfo(
    val iso: String
)