package com.velora.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.velora.data.api.AnimeApiService
import com.velora.data.api.HomeAnime
import com.velora.data.api.ListingAnime
import com.velora.data.api.DetailData
import com.velora.data.api.EpisodeStreamData
import com.velora.data.api.ScheduleDay
import com.velora.data.local.AnimeDatabase
import com.velora.data.local.ContinueWatchingEntity
import com.velora.data.local.WatchlistEntity
import com.velora.data.repository.AnimeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class AnimeViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = AnimeApiService.create()
    private val database = AnimeDatabase.getDatabase(application)
    private val repository = AnimeRepository(apiService, database.animeDao)

    // Watchlist Flow
    val watchlist: StateFlow<List<WatchlistEntity>> = repository.watchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Continue Watching Flow
    val continueWatchingList: StateFlow<List<ContinueWatchingEntity>> = repository.continueWatchingList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Home Screen states
    private val _carouselTrending = MutableStateFlow<List<HomeAnime>>(emptyList())
    val carouselTrending: StateFlow<List<HomeAnime>> = _carouselTrending.asStateFlow()

    private val _ongoingAnime = MutableStateFlow<List<ListingAnime>>(emptyList())
    val ongoingAnime: StateFlow<List<ListingAnime>> = _ongoingAnime.asStateFlow()

    private val _trendingAnime = MutableStateFlow<List<ListingAnime>>(emptyList())
    val trendingAnime: StateFlow<List<ListingAnime>> = _trendingAnime.asStateFlow()

    private val _popularAnime = MutableStateFlow<List<ListingAnime>>(emptyList())
    val popularAnime: StateFlow<List<ListingAnime>> = _popularAnime.asStateFlow()

    private val _moviesAnime = MutableStateFlow<List<ListingAnime>>(emptyList())
    val moviesAnime: StateFlow<List<ListingAnime>> = _moviesAnime.asStateFlow()

    private val _homeState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val homeState: StateFlow<UiState<Unit>> = _homeState.asStateFlow()

    // Browse Screen states
    private val _trendingNow = MutableStateFlow<List<ListingAnime>>(emptyList())
    val trendingNow: StateFlow<List<ListingAnime>> = _trendingNow.asStateFlow()

    private val _allTimePopular = MutableStateFlow<List<ListingAnime>>(emptyList())
    val allTimePopular: StateFlow<List<ListingAnime>> = _allTimePopular.asStateFlow()

    private val _searchResult = MutableStateFlow<List<ListingAnime>?>(null)
    val searchResult: StateFlow<List<ListingAnime>?> = _searchResult.asStateFlow()

    private val _browseState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val browseState: StateFlow<UiState<Unit>> = _browseState.asStateFlow()

    // Detail Screen states
    private val _detailState = MutableStateFlow<UiState<DetailData>>(UiState.Loading)
    val detailState: StateFlow<UiState<DetailData>> = _detailState.asStateFlow()

    // Player Screen states
    private val _playerState = MutableStateFlow<UiState<EpisodeStreamData>>(UiState.Loading)
    val playerState: StateFlow<UiState<EpisodeStreamData>> = _playerState.asStateFlow()

    // Schedule Screen states
    private val _scheduleState = MutableStateFlow<UiState<List<ScheduleDay>>>(UiState.Loading)
    val scheduleState: StateFlow<UiState<List<ScheduleDay>>> = _scheduleState.asStateFlow()

    init {
        loadHomeScreenData()
        loadBrowseScreenData()
        loadScheduleData()
    }

    fun loadHomeScreenData() {
        viewModelScope.launch {
            _homeState.value = UiState.Loading
            try {
                // Fetch concurrent api elements
                val homeResponse = repository.getHome()
                val trendingResponse = repository.getPopular(1)
                val ongoingResponse = repository.getOngoing(1)
                val popularResponse = repository.getCompleted(1) // map Completed as alternate Popular
                val moviesResponse = repository.getMovies(1)

                _carouselTrending.value = homeResponse.data.animeList
                _trendingAnime.value = trendingResponse.data.animeList
                _ongoingAnime.value = ongoingResponse.data.animeList
                _popularAnime.value = popularResponse.data.animeList
                _moviesAnime.value = moviesResponse.data.animeList

                _homeState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _homeState.value = UiState.Error(e.localizedMessage ?: "Failed to load home data")
            }
        }
    }

    fun loadBrowseScreenData() {
        viewModelScope.launch {
            _browseState.value = UiState.Loading
            try {
                val trendingNowResponse = repository.getPopular(1)
                val allTimePopularResponse = repository.getPopular(2)

                _trendingNow.value = trendingNowResponse.data.animeList
                _allTimePopular.value = allTimePopularResponse.data.animeList
                _browseState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _browseState.value = UiState.Error(e.localizedMessage ?: "Failed to load browse default data")
            }
        }
    }

    fun searchAnime(query: String) {
        if (query.trim().isEmpty()) {
            _searchResult.value = null
            return
        }
        viewModelScope.launch {
            try {
                val response = repository.search(query)
                _searchResult.value = response.data.animeList
            } catch (e: Exception) {
                _searchResult.value = emptyList() // Fallback to empty result list
            }
        }
    }

    fun loadAnimeDetail(slug: String) {
        viewModelScope.launch {
            _detailState.value = UiState.Loading
            try {
                val response = repository.getDetail(slug)
                _detailState.value = UiState.Success(response.data)
            } catch (e: Exception) {
                _detailState.value = UiState.Error(e.localizedMessage ?: "Failed to load anime details")
            }
        }
    }

    fun loadEpisodeStream(slug: String) {
        viewModelScope.launch {
            _playerState.value = UiState.Loading
            try {
                val response = repository.getEpisode(slug)
                _playerState.value = UiState.Success(response.data)
            } catch (e: Exception) {
                _playerState.value = UiState.Error(e.localizedMessage ?: "Failed to load episode stream")
            }
        }
    }

    fun loadScheduleData() {
        viewModelScope.launch {
            _scheduleState.value = UiState.Loading
            try {
                val response = repository.getSchedule()
                _scheduleState.value = UiState.Success(response.data.days)
            } catch (e: Exception) {
                _scheduleState.value = UiState.Error(e.localizedMessage ?: "Failed to load weekly schedule")
            }
        }
    }

    // Is in Watchlist Flow builder
    fun isInWatchlist(animeId: String): Flow<Boolean> {
        return repository.isInWatchlist(animeId)
    }

    fun toggleWatchlist(detail: DetailData) {
        viewModelScope.launch {
            val contains = repository.isInWatchlist(detail.animeId).first()
            if (contains) {
                repository.removeFromWatchlist(detail.animeId)
            } else {
                repository.addToWatchlist(
                    WatchlistEntity(
                        animeId = detail.animeId,
                        title = detail.title,
                        poster = detail.poster,
                        type = detail.type,
                        score = detail.score
                    )
                )
            }
        }
    }

    // Continue Watching helper functions
    fun saveProgress(
        animeId: String,
        title: String,
        poster: String,
        episodeId: String,
        episodeNum: String,
        progressMs: Long,
        durationMs: Long
    ) {
        viewModelScope.launch {
            val entity = ContinueWatchingEntity(
                animeId = animeId,
                title = title,
                poster = poster,
                episodeId = episodeId,
                episodeNum = episodeNum,
                progressMs = progressMs,
                durationMs = durationMs,
                lastWatched = System.currentTimeMillis()
            )
            repository.saveContinueWatching(entity)
        }
    }

    suspend fun getEpisodeSavedProgress(animeId: String): ContinueWatchingEntity? {
        return repository.getContinueWatchingForAnime(animeId)
    }

    fun deleteProgress(animeId: String) {
        viewModelScope.launch {
            repository.removeContinueWatching(animeId)
        }
    }
}
