package com.example.nlcpapp.data.repository

import android.content.Context
import com.example.nlcpapp.data.api.NlcRetrofitClient
import com.example.nlcpapp.data.api.models.DateObservations
import com.example.nlcpapp.data.api.models.Observation
import com.example.nlcpapp.data.local.AppDatabase
import com.example.nlcpapp.data.local.entity.ObservationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObservationRepository(context: Context) {
    private val apiService = NlcRetrofitClient.apiService
    private val dao = AppDatabase.getDatabase(context).observationDao()
    
    fun getAllObservations(): Flow<List<Observation>> {
        return dao.getAllObservations().map { entities ->
            entities.map { it.toObservation() }
        }
    }
    
    fun getAllDates(): Flow<List<String>> {
        return dao.getAllDates()
    }
    
    fun getObservationsByDate(date: String): Flow<List<Observation>> {
        return dao.getObservationsByDate(date).map { entities ->
            entities.map { it.toObservation() }
        }
    }
    
    fun getObservationsByName(name: String): Flow<List<Observation>> {
        return dao.getObservationsByName(name).map { entities ->
            entities.map { it.toObservation() }
        }
    }
    
    suspend fun fetchObservationsFromApi(): Result<List<DateObservations>> {
        return try {
            val response = apiService.getObservations()
            val allObservations = mutableListOf<Observation>()
            
            response.data.forEach { dateObs ->
                dateObs.users.forEach { (_, observations) ->
                    allObservations.addAll(observations)
                }
            }
            
            val entities = allObservations.map { ObservationEntity.fromObservation(it) }
            dao.deleteAllObservations()
            dao.insertObservations(entities)
            
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refreshData() {
        fetchObservationsFromApi()
    }
}