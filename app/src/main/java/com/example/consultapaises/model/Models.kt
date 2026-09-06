package com.example.consultapaises.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(val success: Boolean, val message: String, val data: T?)
data class Country(
    val name: String, val code: String, val capital: String, val continent: String,
    val population: Long, @SerializedName("income_level") val incomeLevel: String,
    val coordinates: String, @SerializedName("flag_url") val flagUrl: String
)
data class HistoryItem(
    val id: Int, @SerializedName("search_term") val searchTerm: String,
    @SerializedName("country_name") val countryName: String,
    @SerializedName("country_code") val countryCode: String,
    @SerializedName("searched_at") val searchedAt: String
)
