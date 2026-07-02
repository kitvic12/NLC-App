package com.example.nlcpapp.data.api.models

import com.google.gson.annotations.SerializedName

data class Observation(
    @SerializedName("name") val name: String,
    @SerializedName("place") val place: String,
    @SerializedName("date") val date: String,
    @SerializedName("time") val time: String,
    @SerializedName("brightness") val brightness: Int,
    @SerializedName("weather") val weather: String,
    @SerializedName("intensity") val intensity: Int
)

data class DateObservations(
    @SerializedName("date") val date: String,
    @SerializedName("users") val users: Map<String, List<Observation>>
)

data class ApiResponse(
    @SerializedName("data") val data: List<DateObservations>,
    @SerializedName("total_dates") val totalDates: Int
)

// Упрощённая модель для списка (как на database.html)
data class ObservationSummary(
    val date: String,
    val user: String,
    val place: String,
    val startTime: String
)
