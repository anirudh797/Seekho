package com.seekho.anime.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.seekho.anime.data.model.Anime

@Dao
interface AnimeDao {
    
    @Query("SELECT * FROM anime ORDER BY score DESC")
    fun getAllAnime(): LiveData<List<Anime>>
    
    @Query("SELECT * FROM anime ORDER BY score DESC")
    suspend fun getAllAnimeList(): List<Anime>
    
    @Query("SELECT * FROM anime WHERE malId = :animeId")
    suspend fun getAnimeById(animeId: Int): Anime?
    
    @Query("SELECT * FROM anime WHERE malId = :animeId")
    fun getAnimeByIdLive(animeId: Int): LiveData<Anime?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(anime: Anime)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAnime(animeList: List<Anime>)
    
    @Update
    suspend fun updateAnime(anime: Anime)
    
    @Delete
    suspend fun deleteAnime(anime: Anime)
    
    @Query("DELETE FROM anime")
    suspend fun deleteAllAnime()
    
    @Query("SELECT COUNT(*) FROM anime")
    suspend fun getAnimeCount(): Int
}
