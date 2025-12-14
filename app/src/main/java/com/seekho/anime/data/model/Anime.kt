package com.seekho.anime.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "anime")
data class Anime(
    @PrimaryKey
    @SerializedName("mal_id")
    val malId: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("title_english")
    val titleEnglish: String? = null,
    
    @SerializedName("episodes")
    val episodes: Int? = null,
    
    @SerializedName("score")
    val score: Double? = null,
    
    @SerializedName("synopsis")
    val synopsis: String? = null,
    
    @Embedded
    @SerializedName("images")
    val images: AnimeImages? = null,
    
    @SerializedName("trailer")
    val trailer: TrailerInfo? = null,
    
    @SerializedName("genres")
    val genres: List<Genre>? = null,
    
    @SerializedName("type")
    val type: String? = null,
    
    @SerializedName("status")
    val status: String? = null,
    
    @SerializedName("rating")
    val rating: String? = null,
    
    @SerializedName("year")
    val year: Int? = null,
    
    @SerializedName("members")
    val members: Int? = null,
    
    @SerializedName("favorites")
    val favorites: Int? = null,
    
    // Additional metadata for offline functionality
    var lastUpdated: Long = System.currentTimeMillis(),
    var isFavorite: Boolean = false
)

data class AnimeImages(
    @SerializedName("jpg")
    val jpg: ImageUrls? = null,
    
    @SerializedName("webp")
    val webp: ImageUrls? = null
)

data class ImageUrls(
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("small_image_url")
    val smallImageUrl: String? = null,
    
    @SerializedName("large_image_url")
    val largeImageUrl: String? = null
)

data class TrailerInfo(
    @SerializedName("youtube_id")
    val youtubeId: String? = null,
    
    @SerializedName("url")
    val url: String? = null,
    
    @SerializedName("embed_url")
    val embedUrl: String? = null
)

data class Genre(
    @SerializedName("mal_id")
    val malId: Int,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("url")
    val url: String
)
