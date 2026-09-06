package com.example.consultapaises.network

import com.example.consultapaises.model.ApiResponse
import com.example.consultapaises.model.Country
import com.example.consultapaises.model.HistoryItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CountryApi {
    @GET("search_country.php") fun searchCountry(@Query("name") name: String): Call<ApiResponse<Country>>
    @GET("history.php") fun getHistory(): Call<ApiResponse<List<HistoryItem>>>
    @POST("clear_history.php") fun clearHistory(): Call<ApiResponse<Map<String, Int>>>
}
