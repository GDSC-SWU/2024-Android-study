package com.androidlecture.clonecoding.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AirQualityService {
    @GET("nearest_city")
    fun getAirQualityData(
        @Query("lat") lat: String,
        @Query("lon") long: String,
        @Query("key") key: String
    ) : Call<AirQualityResponse>

}