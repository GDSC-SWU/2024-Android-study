package com.swu.clonecoding.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AirQualityService {
    //어노테이션으로 GET 메서드 정의
    @GET("nearest_city")
    fun getAirQualityData(@Query("lat")lat : String,@Query("lon")lon : String,@Query("key")key : String): Call<AirQualityResponse>
    //이때 @Query 값들은 이름과 타입 API 문서에 명시된대로 동일하게 작성
    //Call 객체는 레트로핏에서 요청을 처리하는 객체. excute(), enqueue() 두가지 방식으로 요청 보낼 수 있음
}