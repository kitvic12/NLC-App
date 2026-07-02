package com.example.nlcpapp.services

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class FcmTokenRequest(val token: String)

data class StartObservationRequest(
    val token: String,
    val lat: Double,
    val lon: Double
)

data class StartObservationResponse(
    val session_id: String,
    val place: String,
    val timezone: String
)

data class AddObservationRequest(
    val session_id: String,
    val brightness: Int,
    val intensity: Int,
    val weather: String
)

data class ObservationData(
    val name: String,
    val place: String,
    val date: String,
    val time: String,
    val brightness: Int,
    val weather: String,
    val intensity: Int
)

data class CameraData(
    val name: String,
    val region: String,
    val has_clouds: Boolean,
    val image_url: String?
)

interface ApiService {
    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("kitvic/api/send-fcm")
    fun sendFcmToken(@Body request: FcmTokenRequest): Call<Void>
    
    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("kitvic/api/start-observation")
    fun startObservation(@Body request: StartObservationRequest): Call<StartObservationResponse>
    
    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("kitvic/api/add-observation")
    fun addObservation(@Body request: AddObservationRequest): Call<ResponseBody>
    
    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("kitvic/api/finish-observation")
    fun finishObservation(@Body request: Map<String, String>): Call<ResponseBody>
    
    @GET("kitvic/api/weather")
    fun getCameras(): Call<List<CameraData>>
}

object ApiClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    val apiService: ApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl("https://antcloud.ddns.net/")
            .client(okHttpClient)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
