package com.seekho.anime.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.seekho.anime.data.db.AnimeDatabase
import com.seekho.anime.data.model.Anime
import com.seekho.anime.data.repository.AnimeRepository
import com.seekho.anime.utils.NetworkResult
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AnimeRepository
    
    val animeListLiveData: LiveData<List<Anime>>
    
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> = _loadingState
    
    private val _errorState = MutableLiveData<String?>()
    val errorState: LiveData<String?> = _errorState
    
    init {
        val animeDao = AnimeDatabase.getDatabase(application).animeDao()
        repository = AnimeRepository(animeDao, context = application)
        animeListLiveData = repository.animeListLiveData
    }
    
    fun loadTopAnime() {
        _loadingState.value = true
        _errorState.value = null
        
        viewModelScope.launch {
            when (val result = repository.fetchTopAnime()) {
                is NetworkResult.Success -> {
                    _loadingState.value = false
                    _errorState.value = null
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
    
    fun refreshAnime() {
        loadTopAnime()
    }
}
