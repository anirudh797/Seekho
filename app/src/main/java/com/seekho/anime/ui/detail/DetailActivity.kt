package com.seekho.anime.ui.detail

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.seekho.anime.R
import com.seekho.anime.data.model.Anime
import com.seekho.anime.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewModel: DetailViewModel
    private lateinit var characterAdapter: CharacterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get anime ID from intent
        val animeId = intent.getIntExtra(EXTRA_ANIME_ID, -1)
        val animeTitle = intent.getStringExtra(EXTRA_ANIME_TITLE) ?: "Anime Details"

        if (animeId == -1) {
            finish()
            return
        }

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = animeTitle

        // Setup characters RecyclerView
        characterAdapter = CharacterAdapter()
        binding.charactersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.charactersRecyclerView.adapter = characterAdapter

        // Setup ViewModel
        viewModel = ViewModelProvider(this)[DetailViewModel::class.java]

        // Observe anime details
        viewModel.anime.observe(this) { anime ->
            if (anime != null) {
                displayAnimeDetails(anime)
            }
        }

        // Observe characters
        viewModel.characters.observe(this) { characters ->
            if (characters.isNotEmpty()) {
                characterAdapter.submitList(characters.take(10)) // Show top 10 characters
            }
        }

        // Observe loading state
        viewModel.loadingState.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error state
        viewModel.errorState.observe(this) { errorMessage ->
            if (errorMessage != null) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    errorMessage,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        // Load anime details
        viewModel.loadAnimeDetails(animeId)
    }

    private fun displayAnimeDetails(anime: Anime) {
        // Title
        binding.titleTextView.text = anime.title
        supportActionBar?.title = anime.title

        // Rating
        val score = anime.score ?: 0.0
        binding.ratingTextView.text = if (score > 0) {
            String.format("★ %.1f", score)
        } else {
            "N/A"
        }

        // Episodes
        val episodes = anime.episodes
        binding.episodesTextView.text = if (episodes != null && episodes > 0) {
            "$episodes Episodes"
        } else {
            "Unknown"
        }

        // Type
        binding.typeTextView.text = anime.type ?: "Unknown"

        // Genres
        val genresList = anime.genres
        if (genresList != null && genresList.isNotEmpty()) {
            binding.genresTextView.text = genresList.joinToString(", ") { it.name }
        } else {
            binding.genresTextView.text = "Unknown"
        }

        // Synopsis
        val synopsis = anime.synopsis
        if (!synopsis.isNullOrBlank()) {
            binding.synopsisTextView.text = synopsis
        } else {
            binding.synopsisTextView.text = "No synopsis available"
        }

        // Load poster image
        val imageUrl = anime.images?.jpg?.largeImageUrl 
            ?: anime.images?.jpg?.imageUrl
        
        if (imageUrl != null) {
            binding.posterImageView.load(imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_placeholder)
                error(R.drawable.ic_placeholder)
            }
            binding.noImageTextView.visibility = View.GONE
        } else {
            binding.posterImageView.setImageResource(R.drawable.ic_placeholder)
            binding.noImageTextView.visibility = View.VISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_ANIME_ID = "extra_anime_id"
        const val EXTRA_ANIME_TITLE = "extra_anime_title"
    }
}
