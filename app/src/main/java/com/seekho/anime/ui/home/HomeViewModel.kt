package com.seekho.anime.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.seekho.anime.data.db.AnimeDatabase
import com.seekho.anime.data.model.Anime
import com.seekho.anime.data.repository.AnimeRepository
import com.seekho.anime.utils.NetworkConnectivityObserver
import com.seekho.anime.utils.NetworkResult
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AnimeRepository
    private val networkObserver: NetworkConnectivityObserver

    val animeListLiveData: LiveData<List<Anime>>
    
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> = _loadingState
    
    private val _errorState = MutableLiveData<String?>()
    val errorState: LiveData<String?> = _errorState
    
    // Network connectivity state
    private val _networkState = MutableLiveData<Boolean>()
    val networkState: LiveData<Boolean> = _networkState

    private var isInitialLoad = true
    private var wasOffline = false

    companion object {
        private const val TAG = "HomeViewModel"
    }

    init {
        val animeDao = AnimeDatabase.getDatabase(application).animeDao()
        repository = AnimeRepository(animeDao, context = application)
        animeListLiveData = repository.animeListLiveData
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
                if (isConnected && wasOffline && !isInitialLoad) {
                    Log.d(TAG, "Device came online - triggering auto-sync")
                    autoSyncData()
                }

                wasOffline = !isConnected
                isInitialLoad = false
            }
            .launchIn(viewModelScope)
    }

    /**
     * Auto-sync data when coming back online
     */
    private fun autoSyncData() {
        viewModelScope.launch {
            Log.d(TAG, "Auto-syncing data...")
            when (val result = repository.fetchTopAnime(forceSync = true)) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Auto-sync successful: ${result.data?.size ?: 0} items")
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
