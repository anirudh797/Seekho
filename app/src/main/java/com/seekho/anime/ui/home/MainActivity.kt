package com.seekho.anime.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.seekho.anime.R
import com.seekho.anime.data.model.Anime
import com.seekho.anime.ui.detail.DetailActivity

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: AnimeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        // Setup RecyclerView
        adapter = AnimeAdapter { anime ->
            onAnimeClick(anime)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Setup SwipeRefresh
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshAnime()
        }

        // Setup ViewModel
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Observe anime list
        viewModel.animeListLiveData.observe(this) { animeList ->
            if (animeList != null && animeList.isNotEmpty()) {
                adapter.submitList(animeList)
                recyclerView.visibility = View.VISIBLE
                errorTextView.visibility = View.GONE
            }
        }

        // Observe loading state
        viewModel.loadingState.observe(this) { isLoading ->
            if (isLoading) {
                if (!swipeRefreshLayout.isRefreshing) {
                    progressBar.visibility = View.VISIBLE
                }
                errorTextView.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // Observe error state
        viewModel.errorState.observe(this) { errorMessage ->
            if (errorMessage != null) {
                // Check if we have cached data
                val currentList = adapter.currentList
                if (currentList.isEmpty()) {
                    // No cached data, show error message
                    errorTextView.text = errorMessage
                    errorTextView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    // Have cached data, show Snackbar
                    Snackbar.make(
                        recyclerView,
                        errorMessage,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Load anime on first launch
        if (savedInstanceState == null) {
            viewModel.loadTopAnime()
        }
    }

    private fun onAnimeClick(anime: Anime) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_ANIME_ID, anime.malId)
            putExtra(DetailActivity.EXTRA_ANIME_TITLE, anime.title)
        }
        startActivity(intent)
    }
}
