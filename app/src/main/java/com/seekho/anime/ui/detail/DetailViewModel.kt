package com.seekho.anime.ui.detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.seekho.anime.data.db.AnimeDatabase
import com.seekho.anime.data.model.Anime
import com.seekho.anime.data.model.CharacterData
import com.seekho.anime.data.repository.AnimeRepository
import com.seekho.anime.utils.NetworkConnectivityObserver
import com.seekho.anime.utils.NetworkResult
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AnimeRepository
    private val networkObserver: NetworkConnectivityObserver

    private val _anime = MutableLiveData<Anime?>()
    val anime: LiveData<Anime?> = _anime
    
    private val _characters = MutableLiveData<List<CharacterData>>()
    val characters: LiveData<List<CharacterData>> = _characters
    
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> = _loadingState
    
    private val _errorState = MutableLiveData<String?>()
    val errorState: LiveData<String?> = _errorState
    
    // Network connectivity state
    private val _networkState = MutableLiveData<Boolean>()
    val networkState: LiveData<Boolean> = _networkState

    private var currentAnimeId: Int? = null
    private var wasOffline = false

    companion object {
        private const val TAG = "DetailViewModel"
    }

    init {
        val animeDao = AnimeDatabase.getDatabase(application).animeDao()
        repository = AnimeRepository(animeDao, context = application)
        networkObserver = NetworkConnectivityObserver(application)

        // Observe network connectivity changes
        observeNetworkConnectivity()
    }

    /**
     * Observe network connectivity changes and auto-sync when coming online
     */
    private fun observeNetworkConnectivity() {
        networkObserver.observe()
            .onEach { isConnected ->
                _networkState.value = isConnected
                Log.d(TAG, "Network state changed: $isConnected")

                // Auto-sync when coming back online
                if (isConnected && wasOffline && currentAnimeId != null) {
                    Log.d(TAG, "Device came online - refreshing anime details")
                    currentAnimeId?.let {
                        autoSyncAnimeDetails(it)
                    }
                }

                wasOffline = !isConnected
            }
            .launchIn(viewModelScope)
    }

    /**
     * Auto-sync anime details when coming back online
     */
    private fun autoSyncAnimeDetails(animeId: Int) {
        viewModelScope.launch {
            Log.d(TAG, "Auto-syncing anime details for ID: $animeId")
            when (val result = repository.fetchAnimeById(animeId)) {
                is NetworkResult.Success -> {
                    _anime.value = result.data
                    Log.d(TAG, "Auto-sync successful for anime: ${result.data?.title}")
                    // Also refresh characters
                    loadCharacters(animeId)
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Auto-sync failed: ${result.message}")
                }
                is NetworkResult.Loading -> {
                    // Do nothing
                }
            }
        }
    }
    
    fun loadAnimeDetails(animeId: Int) {
        currentAnimeId = animeId // Store for auto-sync
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
