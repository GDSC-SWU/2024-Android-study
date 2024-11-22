package com.swu.clonecoding.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitConnection {
    //retrofit 객체를 만드는데 많은 자원이 소모되기때문에 매번 객체를 만드는 것은 낭비
    //따라서 싱글톤패턴으로 구현하여서 프로그램내에서 공유하도록 함
    companion object{
        private const val BASE_URL = "https://api.airvisual.com/v2/"
        private var INSTANCE : Retrofit? = null //레트로핏 인스턴스 정의 nullable

        fun getInstance() : Retrofit{

            //인스턴스가 null인 경우 레트로핏 객체 생성
            if(INSTANCE == null){
                INSTANCE = Retrofit.Builder()
                    .baseUrl(BASE_URL)//https://api.airvisual.com/v2
                    .addConverterFactory(GsonConverterFactory.create()) //받은 응답을 데이터클래스로 자동 변환
                    .build()
            }
            return INSTANCE!! //null값이면 에러를 반환
        }
    }
}