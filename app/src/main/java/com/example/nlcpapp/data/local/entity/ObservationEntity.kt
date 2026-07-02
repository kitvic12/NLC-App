package com.example.nlcpapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nlcpapp.data.api.models.Observation

@Entity(tableName = "observations")
data class ObservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val place: String,
    val date: String,
    val time: String,
    val brightness: Int,
    val weather: String,
    val intensity: Int,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toObservation() = Observation(
        name = name,
        place = place,
        date = date,
        time = time,
        brightness = brightness,
        weather = weather,
        intensity = intensity
    )
    
    companion object {
        fun fromObservation(observation: Observation) = ObservationEntity(
            name = observation.name,
            place = observation.place,
            date = observation.date,
            time = observation.time,
            brightness = observation.brightness,
            weather = observation.weather,
            intensity = observation.intensity
        )
    }
}