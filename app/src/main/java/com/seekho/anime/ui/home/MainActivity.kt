package com.seekho.anime.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.seekho.anime.data.model.Anime
import com.seekho.anime.databinding.ActivityMainBinding
import com.seekho.anime.ui.detail.DetailActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: AnimeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        adapter = AnimeAdapter { anime ->
            onAnimeClick(anime)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Setup SwipeRefresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshAnime()
        }

        // Setup ViewModel
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Observe anime list
        viewModel.animeListLiveData.observe(this) { animeList ->
            if (animeList != null && animeList.isNotEmpty()) {
                adapter.submitList(animeList)
                binding.recyclerView.visibility = View.VISIBLE
                binding.errorTextView.visibility = View.GONE
            }
        }

        // Observe loading state
        viewModel.loadingState.observe(this) { isLoading ->
            if (isLoading) {
                if (!binding.swipeRefreshLayout.isRefreshing) {
                    binding.progressBar.visibility = View.VISIBLE
                }
                binding.errorTextView.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        // Observe error state
        viewModel.errorState.observe(this) { errorMessage ->
            if (errorMessage != null) {
                // Check if we have cached data
                val currentList = adapter.currentList
                if (currentList.isEmpty()) {
                    // No cached data, show error message
                    binding.errorTextView.text = errorMessage
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    // Have cached data, show Snackbar
                    Snackbar.make(
                        binding.recyclerView,
                        errorMessage,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Observe network state for auto-sync notifications
        var previousNetworkState: Boolean? = null
        viewModel.networkState.observe(this) { isConnected ->
            // Only show message when transitioning from offline to online
            if (previousNetworkState == false && isConnected == true) {
                Snackbar.make(
                    binding.recyclerView,
                    "Connected! Syncing latest data...",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else if (previousNetworkState == true && isConnected == false) {
                Snackbar.make(
                    binding.recyclerView,
                    "Offline - Showing cached data",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            previousNetworkState = isConnected
        }

        // Load anime on first launch only
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
