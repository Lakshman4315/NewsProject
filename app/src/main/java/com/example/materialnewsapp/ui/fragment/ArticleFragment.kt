package com.example.materialnewsapp.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.example.materialnewsapp.NewsActivity
import com.example.materialnewsapp.R
import com.example.materialnewsapp.databinding.FragmentArticleBinding
import com.example.materialnewsapp.ui.viewmodel.NewsViewModel
import com.google.android.material.snackbar.Snackbar


class ArticleFragment : Fragment(R.layout.fragment_article) {

    private lateinit var viewModel: NewsViewModel
    private val args: ArticleFragmentArgs by navArgs()
    private lateinit var binding: FragmentArticleBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).newsViewModel
        val article = args.article
        binding.rvWebView.apply {
            webViewClient = WebViewClient()
            if (article != null) {
                loadUrl(article.url)
            }
        }
        binding.floatingActionButton.setOnClickListener {
            if (article != null) {
                viewModel.saveArticle(article)
            }
            Snackbar.make(view, "Article Save Successfully", Snackbar.LENGTH_SHORT).show()
        }
    }
}