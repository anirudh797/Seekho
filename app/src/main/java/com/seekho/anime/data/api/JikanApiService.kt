package com.seekho.anime.data.api

import com.seekho.anime.data.model.AnimeResponse
import com.seekho.anime.data.model.CharactersResponse
import com.seekho.anime.data.model.SingleAnimeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanApiService {
    
    @GET("top/anime")
    suspend fun getTopAnime(
        @Query("page") page: Int = DEFAULT_PAGE,
        @Query("limit") limit: Int = DEFAULT_LIMIT
    ): Response<AnimeResponse>
    
    @GET("anime/{id}/full")
    suspend fun getAnimeById(
        @Path("id") animeId: Int
    ): Response<SingleAnimeResponse>
    
    @GET("anime/{id}/characters")
    suspend fun getAnimeCharacters(
        @Path("id") animeId: Int
    ): Response<CharactersResponse>
    
    companion object {
        const val BASE_URL = "https://api.jikan.moe/v4/"
        const val DEFAULT_PAGE = 1
        const val DEFAULT_LIMIT = 25
    }
}
