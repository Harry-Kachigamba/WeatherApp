package com.example.weatherapp.data

class WeatherRepository {
    private val api = RetrofitInstance.api


    suspend fun getWeather(city: String): WeatherData {
        return api.getWeather(city)
    }
}
