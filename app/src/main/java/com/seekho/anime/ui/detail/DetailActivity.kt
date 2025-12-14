package com.seekho.anime.ui.detail

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.seekho.anime.R
import com.seekho.anime.data.model.Anime

class DetailActivity : AppCompatActivity() {

    private lateinit var viewModel: DetailViewModel
    private lateinit var characterAdapter: CharacterAdapter
    
    private lateinit var toolbar: Toolbar
    private lateinit var posterImageView: ImageView
    private lateinit var noImageTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var episodesTextView: TextView
    private lateinit var typeTextView: TextView
    private lateinit var genresTextView: TextView
    private lateinit var synopsisTextView: TextView
    private lateinit var charactersRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Get anime ID from intent
        val animeId = intent.getIntExtra(EXTRA_ANIME_ID, -1)
        val animeTitle = intent.getStringExtra(EXTRA_ANIME_TITLE) ?: "Anime Details"

        if (animeId == -1) {
            finish()
            return
        }

        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        posterImageView = findViewById(R.id.posterImageView)
        noImageTextView = findViewById(R.id.noImageTextView)
        titleTextView = findViewById(R.id.titleTextView)
        ratingTextView = findViewById(R.id.ratingTextView)
        episodesTextView = findViewById(R.id.episodesTextView)
        typeTextView = findViewById(R.id.typeTextView)
        genresTextView = findViewById(R.id.genresTextView)
        synopsisTextView = findViewById(R.id.synopsisTextView)
        charactersRecyclerView = findViewById(R.id.charactersRecyclerView)
        progressBar = findViewById(R.id.progressBar)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = animeTitle

        // Setup characters RecyclerView
        characterAdapter = CharacterAdapter()
        charactersRecyclerView.layoutManager = LinearLayoutManager(this)
        charactersRecyclerView.adapter = characterAdapter

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
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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
        titleTextView.text = anime.title
        supportActionBar?.title = anime.title

        // Rating
        val score = anime.score ?: 0.0
        ratingTextView.text = if (score > 0) {
            String.format("★ %.1f", score)
        } else {
            "N/A"
        }

        // Episodes
        val episodes = anime.episodes
        episodesTextView.text = if (episodes != null && episodes > 0) {
            "$episodes Episodes"
        } else {
            "Unknown"
        }

        // Type
        typeTextView.text = anime.type ?: "Unknown"

        // Genres
        val genresList = anime.genres
        if (genresList != null && genresList.isNotEmpty()) {
            genresTextView.text = genresList.joinToString(", ") { it.name }
        } else {
            genresTextView.text = "Unknown"
        }

        // Synopsis
        val synopsis = anime.synopsis
        if (!synopsis.isNullOrBlank()) {
            synopsisTextView.text = synopsis
        } else {
            synopsisTextView.text = "No synopsis available"
        }

        // Load poster image
        val imageUrl = anime.images?.jpg?.largeImageUrl 
            ?: anime.images?.jpg?.imageUrl
        
        if (imageUrl != null) {
            posterImageView.load(imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_placeholder)
                error(R.drawable.ic_placeholder)
            }
            noImageTextView.visibility = View.GONE
        } else {
            posterImageView.setImageResource(R.drawable.ic_placeholder)
            noImageTextView.visibility = View.VISIBLE
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
