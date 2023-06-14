package com.example.materialnewsapp.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.materialnewsapp.NewsActivity
import com.example.materialnewsapp.R
import com.example.materialnewsapp.adapters.NewsAdapter
import com.example.materialnewsapp.databinding.FragmentSearchBinding
import com.example.materialnewsapp.ui.viewmodel.NewsViewModel
import com.example.materialnewsapp.utils.Constant
import com.example.materialnewsapp.utils.Constant.Companion.SEARCH_NEWS_TIME_DELAY
import com.example.materialnewsapp.utils.Resources
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var newsAdapter: NewsAdapter
    private val viewModel: NewsViewModel by lazy {
        (requireActivity() as NewsActivity).newsViewModel
    }

    private var isLoading = false
    private var isLastPage = false
    private var isScrolling = false
    private var searchJob: Job? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_searchFragment_to_articleFragment, bundle)
        }
        binding.etSearch.addTextChangedListener { editable ->
            searchJob?.cancel()
            searchJob = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                val query = editable.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.getSearchNews(query)
                }
            }
        }
        viewModel.searchNews.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resources.Sucess -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList()) // the progress bar is hidden, and the response data (newsResponse) is retrieved. The news adapter's data is updated with the new list of articles.
                        val totalPages =
                            newsResponse.totalResults / Constant.QUERY_PAGE_SIZE + 2 //  The total number of pages is calculated based on the total results from the response.
                        isLastPage =
                            viewModel.searchNewsPage == totalPages //  If the current search page is the last page, the padding of the RecyclerView is adjusted to remove any extra space
                        if (isLastPage) {
                            binding.rvSearchNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }

                is Resources.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "An Error Occurred: $message", Toast.LENGTH_LONG)
                            .show()
                    }
                }

                is Resources.Loading -> {
                    showProgressBar()
                }
            }
        }
        scrollListener
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition =
                layoutManager.findFirstVisibleItemPosition() // // This retrieves the position of the first visible item in the RecyclerView.
            val visibleItemCount =
                layoutManager.childCount // / This retrieves the number of currently visible items in the RecyclerView.
            val totalItemCount =
                layoutManager.itemCount // This retrieves the total number of items in the RecyclerView
            val isNotLoadingAndNotLastPage =
                !isLoading && !isLastPage // This checks if the isLoading flag is false and the isLastPage flag is false.
            val isAtLastItem =
                firstVisibleItemPosition + visibleItemCount >= totalItemCount // // This checks if the sum of the first visible item position and the visible item count is greater than or equal to the total item count. This indicates that the user has scrolled to the last item.
            val isNotBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible =
                totalItemCount >= Constant.QUERY_PAGE_SIZE // // This checks if the total item count is greater than or equal to a constant value QUERY_PAGE_SIZE. This ensures that there are enough items to trigger pagination.
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotBeginning && isTotalMoreThanVisible // This checks if all the conditions for pagination are met.
            if (shouldPaginate) {  // if the shouldPaginate condition is true, it means that the RecyclerView has reached the end and pagination should occur.
                viewModel.getSearchNews(binding.etSearch.toString()) // // This triggers a method call on the viewModel to fetch more breaking news data. It passes a parameter, in this case, the country code "us".
                isScrolling =
                    false  //  After triggering the pagination, it sets the isScrolling flag to false to prevent immediate consecutive pagination requests.
            }
        }
    }

    private fun hideProgressBar() {
        binding.searchProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.searchProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}