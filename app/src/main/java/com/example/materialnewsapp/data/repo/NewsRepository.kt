package com.example.materialnewsapp.data.repo

import com.example.materialnewsapp.data.api.model.Article
import com.example.materialnewsapp.data.api.model.NewsResponse
import com.example.materialnewsapp.data.api.service.RetrofitService
import com.example.materialnewsapp.data.db.ArticleDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class NewsRepository(private val db: ArticleDatabase) {
    suspend fun getBreakingNews(countryCode: String, pageNumber: Int): Response<NewsResponse> =
        withContext(Dispatchers.IO) {
            RetrofitService.api.getBreakingNews(countryCode, pageNumber)
        }

    suspend fun searchForNews(searchQuery: String, pageNumber: Int): Response<NewsResponse> =
        withContext(Dispatchers.IO) {
            RetrofitService.api.searchForNews(searchQuery, pageNumber)
        }

    fun getSaveNews() = db.getArticleDao().getAllArticles()

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

}