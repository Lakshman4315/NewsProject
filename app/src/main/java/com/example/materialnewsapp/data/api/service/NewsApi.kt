package com.example.materialnewsapp.data.api.service

import com.example.materialnewsapp.data.api.model.NewsResponse
import com.example.materialnewsapp.utils.Constant.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {


    @GET("v2/top-headlines")
    suspend fun getBreakingNews(
        @Query("country") countryCode: String,
        @Query("page") page: Int = 2,
        @Query("apiKey") apiKey: String = API_KEY
    ): Response<NewsResponse>

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q") searchQuery: String,
        @Query("page") pageNumber: Int = 2,
        @Query("apiKey") apiKey: String = API_KEY
    ): Response<NewsResponse>
}