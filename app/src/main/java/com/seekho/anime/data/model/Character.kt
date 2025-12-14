package com.seekho.anime.data.model

import com.google.gson.annotations.SerializedName

data class CharactersResponse(
    @SerializedName("data")
    val data: List<CharacterData>
)

data class CharacterData(
    @SerializedName("character")
    val character: Character,
    
    @SerializedName("role")
    val role: String,
    
    @SerializedName("voice_actors")
    val voiceActors: List<VoiceActor>? = null
)

data class Character(
    @SerializedName("mal_id")
    val malId: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("images")
    val images: CharacterImages? = null
)

data class CharacterImages(
    @SerializedName("jpg")
    val jpg: CharacterImageUrls? = null,
    
    @SerializedName("webp")
    val webp: CharacterImageUrls? = null
)

data class CharacterImageUrls(
    @SerializedName("image_url")
    val imageUrl: String? = null
)

data class VoiceActor(
    @SerializedName("person")
    val person: Person,
    
    @SerializedName("language")
    val language: String
)

data class Person(
    @SerializedName("mal_id")
    val malId: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("images")
    val images: CharacterImages? = null
)
