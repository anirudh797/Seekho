package com.seekho.anime.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.seekho.anime.data.db.AnimeDatabase
import com.seekho.anime.data.model.Anime
import com.seekho.anime.data.model.CharacterData
import com.seekho.anime.data.repository.AnimeRepository
import com.seekho.anime.utils.NetworkResult
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AnimeRepository
    
    private val _anime = MutableLiveData<Anime?>()
    val anime: LiveData<Anime?> = _anime
    
    private val _characters = MutableLiveData<List<CharacterData>>()
    val characters: LiveData<List<CharacterData>> = _characters
    
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> = _loadingState
    
    private val _errorState = MutableLiveData<String?>()
    val errorState: LiveData<String?> = _errorState
    
    init {
        val animeDao = AnimeDatabase.getDatabase(application).animeDao()
        repository = AnimeRepository(animeDao, context = application)
    }
    
    fun loadAnimeDetails(animeId: Int) {
        _loadingState.value = true
        _errorState.value = null
        
        viewModelScope.launch {
            // Load anime details
            when (val result = repository.fetchAnimeById(animeId)) {
                is NetworkResult.Success -> {
                    _anime.value = result.data
                    _errorState.value = null
                    // Load characters after anime details
                    loadCharacters(animeId)
                }
                is NetworkResult.Error -> {
                    _loadingState.value = false
                    _errorState.value = result.message
                }
                is NetworkResult.Loading -> {
                    _loadingState.value = true
                }
            }
        }
    }
    
    private fun loadCharacters(animeId: Int) {
        viewModelScope.launch {
            when (val result = repository.fetchAnimeCharacters(animeId)) {
                is NetworkResult.Success -> {
                    _characters.value = result.data ?: emptyList()
                    _loadingState.value = false
                }
                is NetworkResult.Error -> {
                    // Characters are optional, so just set empty list
                    _characters.value = emptyList()
                    _loadingState.value = false
                }
                is NetworkResult.Loading -> {
                    // Keep loading
                }
            }
        }
    }
}
