package com.example.materialnewsapp.ui.viewmodel

import java.io.IOException

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_ETHERNET
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.materialnewsapp.data.api.model.Article
import com.example.materialnewsapp.data.api.model.NewsResponse
import com.example.materialnewsapp.data.repo.NewsRepository
import com.example.materialnewsapp.utils.NewsApplication
import com.example.materialnewsapp.utils.Resources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(
    app: Application, private val repository: NewsRepository
) : AndroidViewModel(app) {
    val breakingNews: MutableLiveData<Resources<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsresponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resources<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null

    init {
        getBreakingNews("in")
    }

    //This function is responsible for fetching breaking news for a specific countryCode. It is called asynchronously using viewModelScope.launch and executes in the Dispatchers.IO coroutine context. It invokes the safeBreakingCall function, passing the countryCode parameter.
    fun getBreakingNews(countryCodes: String) = viewModelScope.launch(Dispatchers.IO) {
        safeBreakingCall(countryCodes)
    }

    //This function is used for searching news based on a searchQuery. Like the previous function, it is executed asynchronously in the Dispatchers.IO coroutine context using viewModelScope.launch. It calls the safeSearchCall function, passing the searchQuery parameter.
    fun getSearchNews(searchQuery: String) = viewModelScope.launch(Dispatchers.IO) {
        safeSearchCall(searchQuery)
    }

    //This function is responsible for saving an article to the repository. It is also executed asynchronously in the Dispatchers.IO coroutine context. It calls the repository.upsert function, passing the article parameter to insert or update the article in the repository.
    fun saveArticle(article: Article) = viewModelScope.launch(Dispatchers.IO) {
        repository.upsert(article)
    }

    //This function is used to delete an article from the repository. It is executed asynchronously in the Dispatchers.IO coroutine context. It calls the repository.deleteArticle function, passing the article parameter to delete the article from the repository.
    fun deleteArticle(article: Article) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteArticle(article)
    }

    //This function is responsible for retrieving saved news articles from the repository. It simply calls the repository.getSaveNews() function, which returns the saved news articles.
    fun getSaveNews() = repository.getSaveNews()

    private suspend fun safeBreakingCall(countryCode: String) {
        breakingNews.postValue(Resources.Loading())
        try {
            //This line checks if there is an internet connection by calling a function hasInternetConnection()
            if (hasInternetConnection()) {
                //If there is an internet connection, this line calls a function getBreakingNews on a repository object, passing the countryCode and breakingNewsPage as parameters. It retrieves a response object containing the breaking news data from the repository.
                val response = repository.getBreakingNews(countryCode, breakingNewsPage)
                //If the response is obtained successfully, this line calls a function handleBreakingNewsResponse and passes the response as a parameter. The result of this function is then posted to the breakingNews MutableLiveData object using postValue()
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else {
                //If there is no internet connection, this block of code is executed. It posts a Resources.Error instance with the message "No internet connection" to the breakingNews MutableLiveData object, indicating an error condition.
                breakingNews.postValue(Resources.Error("No internet connection"))
            }
            //If an exception is thrown during execution, the code jumps to this catch block, and the exception is caught in the variable t of type Throwable.
        } catch (t: Throwable) {
            //Inside the catch block, a when statement is used to handle different types of exceptions.
            //If the exception is an IOException, indicating a network failure, it posts a Resources.Error instance with the message "Network failure" to the breakingNews MutableLiveData object.
            //If the exception is of any other type, it posts a Resources.Error instance with the message "Conversion Error" to the breakingNews MutableLiveData object.
            when (t) {
                is IOException -> breakingNews.postValue(Resources.Error("Network failure"))
                else -> breakingNews.postValue(Resources.Error("Conversion Error"))
            }
        }
    }

    private suspend fun safeSearchCall(searchQuery: String) {
        searchNews.postValue(Resources.Loading())
        try {
            if (hasInternetConnection()) {
                val response = repository.searchForNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resources.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNews.postValue(Resources.Error("Network failure"))
                else -> searchNews.postValue(Resources.Error("Conversion Error"))
            }
        }
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resources<NewsResponse> {
        return if (response.isSuccessful) {
            response.body()?.let { responseResult ->
                breakingNewsPage++ // increment by 1
                if (breakingNewsresponse == null) {
                    breakingNewsresponse = responseResult
                } else {
                    val oldArticle = breakingNewsresponse?.articles
                    val newArticle = responseResult.articles
                    oldArticle?.addAll(newArticle)
                }
                Resources.Sucess(breakingNewsresponse ?: responseResult)
            } ?: Resources.Error("Empty Response Body")
        } else {
            Resources.Error(response.message())
        }
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resources<NewsResponse> {
        return if (response.isSuccessful) {
            response.body()?.let { responseSearch ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = responseSearch
                } else {
                    val oldArticle = searchNewsResponse?.articles
                    val newArticle = responseSearch.articles
                    oldArticle?.addAll(newArticle)
                }
                Resources.Sucess(searchNewsResponse ?: responseSearch)
            } ?: Resources.Error("Empty Response Body")
        } else {
            Resources.Error(response.message())
        }
    }


    @SuppressLint("ObsoleteSdkInt")
    private fun hasInternetConnection(): Boolean {
        val connectivityManager =
            getApplication<NewsApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(TRANSPORT_WIFI) || capabilities.hasTransport(
                TRANSPORT_CELLULAR
            ) || capabilities.hasTransport(TRANSPORT_ETHERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.run {
                type == TYPE_WIFI || type == ConnectivityManager.TYPE_MOBILE || type == TYPE_ETHERNET
            } ?: false
        }
    }
}


