package com.seekho.anime.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.seekho.anime.data.api.JikanApiService
import com.seekho.anime.data.api.RetrofitClient
import com.seekho.anime.data.db.AnimeDao
import com.seekho.anime.data.model.Anime
import com.seekho.anime.data.model.CharacterData
import com.seekho.anime.utils.NetworkResult
import com.seekho.anime.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnimeRepository(
    private val animeDao: AnimeDao,
    private val apiService: JikanApiService = RetrofitClient.jikanApiService,
    private val context: Context
) {
    
    private val TAG = "AnimeRepository"
    
    // LiveData for anime list
    val animeListLiveData: LiveData<List<Anime>> = animeDao.getAllAnime()
    
    // Fetch top anime from API and cache in database
    suspend fun fetchTopAnime(forceSync : Boolean = false): NetworkResult<List<Anime>> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isNetworkAvailable(context) && !forceSync) {
                // Return cached data if no network
                val cachedData = animeDao.getAllAnimeList()
                return@withContext if (cachedData.isNotEmpty()) {
                    NetworkResult.Success(cachedData)
                } else {
                    NetworkResult.Error("No internet connection and no cached data available")
                }
            }
            
            Log.d(TAG, "Fetching top anime from API")
            val response = apiService.getTopAnime()
            
            if (response.isSuccessful && response.body() != null) {
                val animeList = response.body()?.data
                
                // Update last updated timestamp
                val updatedList = animeList?.map { it.copy(lastUpdated = System.currentTimeMillis()) } ?: emptyList()
                
                // Cache in database
                animeDao.insertAllAnime(updatedList)
                Log.d(TAG, "Successfully fetched and cached ${updatedList.size} anime")
                
                NetworkResult.Success(updatedList)
            } else {
                Log.e(TAG, "API error: ${response.code()} - ${response.message()}")
                // Try to return cached data on API error
                val cachedData = animeDao.getAllAnimeList()
                if (cachedData.isNotEmpty()) {
                    NetworkResult.Success(cachedData)
                } else {
                    NetworkResult.Error("Error: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching anime: ${e.message}", e)
            // Try to return cached data on exception
            val cachedData = animeDao.getAllAnimeList()
            if (cachedData.isNotEmpty()) {
                NetworkResult.Success(cachedData)
            } else {
                NetworkResult.Error("Exception: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    // Fetch anime details by ID
    suspend fun fetchAnimeById(animeId: Int): NetworkResult<Anime> = withContext(Dispatchers.IO) {
        try {
            // First, check cache
            val cachedAnime = animeDao.getAnimeById(animeId)
            
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return@withContext if (cachedAnime != null) {
                    NetworkResult.Success(cachedAnime)
                } else {
                    NetworkResult.Error("No internet connection and anime not cached")
                }
            }
            
            Log.d(TAG, "Fetching anime details for ID: $animeId")
            val response = apiService.getAnimeById(animeId)
            
            if (response.isSuccessful && response.body() != null) {
                val anime = response.body()!!.data.copy(lastUpdated = System.currentTimeMillis())
                
                // Cache in database
                animeDao.insertAnime(anime)
                Log.d(TAG, "Successfully fetched anime: ${anime.title}")
                
                NetworkResult.Success(anime)
            } else {
                Log.e(TAG, "API error: ${response.code()} - ${response.message()}")
                // Return cached data if available
                if (cachedAnime != null) {
                    NetworkResult.Success(cachedAnime)
                } else {
                    NetworkResult.Error("Error: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching anime details: ${e.message}", e)
            // Return cached data if available
            val cachedAnime = animeDao.getAnimeById(animeId)
            if (cachedAnime != null) {
                NetworkResult.Success(cachedAnime)
            } else {
                NetworkResult.Error("Exception: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    // Fetch anime characters
    suspend fun fetchAnimeCharacters(animeId: Int): NetworkResult<List<CharacterData>> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return@withContext NetworkResult.Error("No internet connection")
            }
            
            Log.d(TAG, "Fetching characters for anime ID: $animeId")
            val response = apiService.getAnimeCharacters(animeId)
            
            if (response.isSuccessful && response.body() != null) {
                val characters = response.body()!!.data
                Log.d(TAG, "Successfully fetched ${characters.size} characters")
                NetworkResult.Success(characters)
            } else {
                Log.e(TAG, "API error: ${response.code()} - ${response.message()}")
                NetworkResult.Error("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching characters: ${e.message}", e)
            NetworkResult.Error("Exception: ${e.message ?: "Unknown error"}")
        }
    }
    
    // Get anime by ID from database (LiveData)
    fun getAnimeByIdLive(animeId: Int): LiveData<Anime?> {
        return animeDao.getAnimeByIdLive(animeId)
    }
}
