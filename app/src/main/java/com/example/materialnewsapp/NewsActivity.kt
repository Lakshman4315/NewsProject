package com.example.materialnewsapp

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.materialnewsapp.data.db.ArticleDatabase
import com.example.materialnewsapp.data.repo.NewsRepository
import com.example.materialnewsapp.databinding.ActivityMainBinding
import com.example.materialnewsapp.ui.viewmodel.NewsViewModel
import com.example.materialnewsapp.ui.viewmodel.NewsViewModelFactory
import com.example.materialnewsapp.utils.NewsApplication

class NewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
     lateinit var newsViewModel: NewsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        navController = navHostFragment!!.findNavController()
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.nav_menu)
        binding.smoothBottomBar.setupWithNavController(popupMenu.menu, navController)

        val db = ArticleDatabase.getDatabase(this)
        val repository = NewsRepository(db)
        newsViewModel = ViewModelProvider(
            this, NewsViewModelFactory(application, repository)
        )[NewsViewModel::class.java]
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
