package com.example.nlcpapp.data.api

import com.example.nlcpapp.data.api.models.ApiResponse
import retrofit2.http.GET

interface NlcApiService {
    @GET("api/data")
    suspend fun getObservations(): ApiResponse
}