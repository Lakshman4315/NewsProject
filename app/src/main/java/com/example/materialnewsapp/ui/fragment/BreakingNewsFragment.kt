package com.example.materialnewsapp.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.materialnewsapp.NewsActivity
import com.example.materialnewsapp.R
import com.example.materialnewsapp.adapters.NewsAdapter
import com.example.materialnewsapp.databinding.FragmentBreakingNewsBinding
import com.example.materialnewsapp.ui.viewmodel.NewsViewModel
import com.example.materialnewsapp.utils.Constant.Companion.QUERY_PAGE_SIZE
import com.example.materialnewsapp.utils.Resources
import kotlin.math.abs


class BreakingNewsFragment : Fragment() {
    private lateinit var binding: FragmentBreakingNewsBinding
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var viewModel: NewsViewModel


    private var isLoading = false
    private var isLastPage = false
    private var isScrolling = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBreakingNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).newsViewModel
        setUpRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it) // : This creates a new Bundle object and adds a serializable object with the key "article" to it. The serializable object is provided as an argument (it) to the putSerializable method.
            }
            findNavController().navigate(R.id.action_breakingNewsFragment_to_articleFragment,bundle)
        }

        viewModel.breakingNews.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resources.Sucess -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList()) // -> This submits a new list of articles to the differ (presumably a DiffUtil.ItemCallback) of the newsAdapter. This will update the RecyclerView's items with the new list of articles.

                        val totalPages =
                            newsResponse.totalResults / QUERY_PAGE_SIZE + 2 // -> This calculates the total number of pages by dividing the totalResults by QUERY_PAGE_SIZE (a constant) and adding 2. The addition of 2 seems to handle some kind of offset or pagination logic.

                        isLastPage =
                            viewModel.breakingNewsPage == totalPages // -> This checks if the current page of the breaking news is the last page by comparing it to the calculated totalPages. The result is stored in the isLastPage variable.

                        val defaultPadding =
                            resources.getDimensionPixelSize(R.dimen.dimen_padding) // -> This retrieves a dimension value from resources and assigns it to the defaultPadding variable. It seems to be used for setting padding in the UI.
                        binding.rvBreakingNews.setPadding(
                            0,
                            0,
                            0,
                            if (viewModel.breakingNewsPage == totalPages) 0 else defaultPadding
                        ) // -> This sets the padding of the rvBreakingNews (presumably a RecyclerView) based on the current page. If it's the last page, the bottom padding will be set to 0; otherwise, it will be set to defaultPadding.
                    }
                }

                is Resources.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "An Error Occurred $message", Toast.LENGTH_LONG)
                            .show()
                    }
                }

                is Resources.Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = true
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
            val firstVisibleItemCount =
                layoutManager.findFirstVisibleItemPosition() // This retrieves the position of the first visible item in the RecyclerView.
            val visibleItemCount =
                layoutManager.childCount // This retrieves the number of currently visible items in the RecyclerView.
            val totalItemCount =
                layoutManager.itemCount //  This retrieves the total number of items in the RecyclerView.
            val isNotLoadingAndNotLastPage =
                !isLoading && !isLastPage // This checks if the isLoading flag is false and the isLastPage flag is false.
            val isAtLastItem =
                firstVisibleItemCount + visibleItemCount >= totalItemCount // This checks if the sum of the first visible item position and the visible item count is greater than or equal to the total item count. This indicates that the user has scrolled to the last item.
            val isNotBeginning =
                firstVisibleItemCount >= 0 // This checks if the first visible item position is greater than or equal to 0. This ensures that the RecyclerView is not scrolled to the very beginning.

            val isTotalMoreThanVisible =
                totalItemCount >= QUERY_PAGE_SIZE // This checks if the total item count is greater than or equal to a constant value QUERY_PAGE_SIZE. This ensures that there are enough items to trigger pagination.

            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotBeginning && isTotalMoreThanVisible // This checks if all the conditions for pagination are met.

            if (shouldPaginate) { // if the shouldPaginate condition is true, it means that the RecyclerView has reached the end and pagination should occur.
                viewModel.getBreakingNews("us") // This triggers a method call on the viewModel to fetch more breaking news data. It passes a parameter, in this case, the country code "us".
                isScrolling =
                    false //  After triggering the pagination, it sets the isScrolling flag to false to prevent immediate consecutive pagination requests.
            }
        }
    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }
}