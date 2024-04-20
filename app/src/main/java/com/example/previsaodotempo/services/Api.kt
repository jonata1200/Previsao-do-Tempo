package com.example.previsodotempokotlin.services

import com.example.previsodotempokotlin.model.Main
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

//https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}&lang={lang}

interface Api {
    @GET("weather")
    fun weatherMap(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("lang") linguagem: String
    ): Call<Main>
}







