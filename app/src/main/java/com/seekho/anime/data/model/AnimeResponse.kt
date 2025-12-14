package com.seekho.anime.data.model

import com.google.gson.annotations.SerializedName

data class AnimeResponse(
    @SerializedName("data")
    val data: List<Anime>,
    
    @SerializedName("pagination")
    val pagination: Pagination? = null
)

data class SingleAnimeResponse(
    @SerializedName("data")
    val data: Anime
)

data class Pagination(
    @SerializedName("last_visible_page")
    val lastVisiblePage: Int,
    
    @SerializedName("has_next_page")
    val hasNextPage: Boolean,
    
    @SerializedName("current_page")
    val currentPage: Int,
    
    @SerializedName("items")
    val items: PaginationItems? = null
)

data class PaginationItems(
    @SerializedName("count")
    val count: Int,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("per_page")
    val perPage: Int
)
