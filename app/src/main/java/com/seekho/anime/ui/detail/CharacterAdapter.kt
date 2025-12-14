package com.seekho.anime.ui.detail

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
import com.seekho.anime.data.model.CharacterData
import com.seekho.anime.utils.FeatureFlags

class CharacterAdapter : ListAdapter<CharacterData, CharacterAdapter.CharacterViewHolder>(CharacterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_character, parent, false)
        return CharacterViewHolder(view)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CharacterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val characterImageView: ImageView = itemView.findViewById(R.id.characterImageView)
        private val characterNameTextView: TextView = itemView.findViewById(R.id.characterNameTextView)
        private val characterRoleTextView: TextView = itemView.findViewById(R.id.characterRoleTextView)

        fun bind(characterData: CharacterData) {
            characterNameTextView.text = characterData.character.name
            characterRoleTextView.text = characterData.role
            
            // Check if profile images are enabled (legal compliance)
            if (FeatureFlags.ENABLE_PROFILE_IMAGES) {
                // Load character image with Glide
                characterImageView.visibility = View.VISIBLE
                val imageUrl = characterData.character.images?.jpg?.imageUrl
                if (imageUrl != null) {
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .circleCrop()
                        .into(characterImageView)
                } else {
                    characterImageView.setImageResource(R.drawable.ic_placeholder)
                }
            } else {
                // Hide image view due to legal requirements
                characterImageView.visibility = View.GONE
                // Remove any margins that depend on the image
                val layoutParams = characterNameTextView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.marginStart = 0
                characterNameTextView.layoutParams = layoutParams
            }
        }
    }

    class CharacterDiffCallback : DiffUtil.ItemCallback<CharacterData>() {
        override fun areItemsTheSame(oldItem: CharacterData, newItem: CharacterData): Boolean {
            return oldItem.character.malId == newItem.character.malId
        }

        override fun areContentsTheSame(oldItem: CharacterData, newItem: CharacterData): Boolean {
            return oldItem == newItem
        }
    }
}
