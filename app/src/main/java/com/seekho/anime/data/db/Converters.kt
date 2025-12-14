package com.seekho.anime.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.seekho.anime.data.model.AnimeImages
import com.seekho.anime.data.model.Genre
import com.seekho.anime.data.model.TrailerInfo

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromGenreList(genres: List<Genre>?): String? {
        if (genres == null) return null
        return gson.toJson(genres)
    }
    
    @TypeConverter
    fun toGenreList(genresString: String?): List<Genre>? {
        if (genresString == null) return null
        val listType = object : TypeToken<List<Genre>>() {}.type
        return gson.fromJson(genresString, listType)
    }
    
    @TypeConverter
    fun fromTrailerInfo(trailer: TrailerInfo?): String? {
        if (trailer == null) return null
        return gson.toJson(trailer)
    }
    
    @TypeConverter
    fun toTrailerInfo(trailerString: String?): TrailerInfo? {
        if (trailerString == null) return null
        return gson.fromJson(trailerString, TrailerInfo::class.java)
    }

    @TypeConverter
    fun fromAnimeImages(images: AnimeImages?): String? {
        if (images == null) return null
        return gson.toJson(images)
    }

    @TypeConverter
    fun toAnimeImages(imagesString: String?): AnimeImages? {
        if (imagesString == null) return null
        return gson.fromJson(imagesString, AnimeImages::class.java)
    }
}
