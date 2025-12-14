package com.seekho.anime.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.seekho.anime.R
import com.seekho.anime.data.model.Anime
import com.seekho.anime.utils.FeatureFlags

class AnimeAdapter(
    private val onAnimeClick: (Anime) -> Unit
) : ListAdapter<Anime, AnimeAdapter.AnimeViewHolder>(AnimeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anime, parent, false)
        return AnimeViewHolder(view, onAnimeClick)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AnimeViewHolder(
        itemView: View,
        private val onAnimeClick: (Anime) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val posterImageView: ImageView = itemView.findViewById(R.id.posterImageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val episodesTextView: TextView = itemView.findViewById(R.id.episodesTextView)
        private val ratingTextView: TextView = itemView.findViewById(R.id.ratingTextView)
        private val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)

        fun bind(anime: Anime) {
            titleTextView.text = anime.title
            
            // Episodes
            episodesTextView.text = anime.episodes?.toString() ?: "?"
            
            // Rating
            val rating = anime.score ?: 0.0
            ratingTextView.text = if (rating > 0) {
                String.format("%.1f / 10", rating)
            } else {
                "N/A"
            }
            
            // Type
            typeTextView.text = anime.type ?: "Unknown"
            
            // Load poster image with Glide (check if profile images are enabled)
            if (FeatureFlags.ENABLE_PROFILE_IMAGES) {
                posterImageView.visibility = View.VISIBLE
                val imageUrl = anime.images?.jpg?.imageUrl
                if (imageUrl != null) {
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .centerCrop()
                        .into(posterImageView)
                } else {
                    posterImageView.setImageResource(R.drawable.ic_placeholder)
                }
            } else {
                // Hide poster image due to legal requirements
                posterImageView.visibility = View.GONE
                // Adjust layout to make title take full width
                val layoutParams = posterImageView.layoutParams
                layoutParams.width = 0
                posterImageView.layoutParams = layoutParams
            }
            
            itemView.setOnClickListener {
                onAnimeClick(anime)
            }
        }
    }

    class AnimeDiffCallback : DiffUtil.ItemCallback<Anime>() {
        override fun areItemsTheSame(oldItem: Anime, newItem: Anime): Boolean {
            return oldItem.malId == newItem.malId
        }

        override fun areContentsTheSame(oldItem: Anime, newItem: Anime): Boolean {
            return oldItem == newItem
        }
    }
}
