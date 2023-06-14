package com.example.materialnewsapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.materialnewsapp.data.repo.NewsRepository
import com.example.materialnewsapp.utils.NewsApplication

class NewsViewModelFactory(private val app: Application, private val repository: NewsRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(app, repository) as T
    }
}