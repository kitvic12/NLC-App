package com.example.nlcpapp.data.local.dao

import androidx.room.*
import com.example.nlcpapp.data.local.entity.ObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {
    @Query("SELECT * FROM observations ORDER BY date DESC, time ASC")
    fun getAllObservations(): Flow<List<ObservationEntity>>
    
    @Query("SELECT DISTINCT date FROM observations ORDER BY date DESC")
    fun getAllDates(): Flow<List<String>>
    
    @Query("SELECT * FROM observations WHERE date = :date ORDER BY name, time")
    fun getObservationsByDate(date: String): Flow<List<ObservationEntity>>
    
    @Query("SELECT * FROM observations WHERE name = :name ORDER BY date DESC, time ASC")
    fun getObservationsByName(name: String): Flow<List<ObservationEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservations(observations: List<ObservationEntity>)
    
    @Query("DELETE FROM observations")
    suspend fun deleteAllObservations()
}