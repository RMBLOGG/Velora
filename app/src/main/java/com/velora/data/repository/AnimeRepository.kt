package com.velora.data.repository

import com.velora.data.api.AnimeApiService
import com.velora.data.api.ApiResponse
import com.velora.data.api.HomeData
import com.velora.data.api.AnimeListData
import com.velora.data.api.DetailData
import com.velora.data.api.EpisodeStreamData
import com.velora.data.api.GenresData
import com.velora.data.api.ScheduleData
import com.velora.data.local.AnimeDao
import com.velora.data.local.WatchlistEntity
import com.velora.data.local.ContinueWatchingEntity
import kotlinx.coroutines.flow.Flow

class AnimeRepository(
    private val apiService: AnimeApiService,
    private val animeDao: AnimeDao
) {
    // API operations
    suspend fun getHome(): ApiResponse<HomeData> = apiService.getHome()
    suspend fun getOngoing(page: Int): ApiResponse<AnimeListData> = apiService.getOngoing(page)
    suspend fun getCompleted(page: Int): ApiResponse<AnimeListData> = apiService.getCompleted(page)
    suspend fun getMovies(page: Int): ApiResponse<AnimeListData> = apiService.getMovies(page)
    suspend fun getPopular(page: Int): ApiResponse<AnimeListData> = apiService.getPopular(page)
    suspend fun search(query: String): ApiResponse<AnimeListData> = apiService.search(query)
    suspend fun getDetail(slug: String): ApiResponse<DetailData> = apiService.getDetail(slug)
    suspend fun getEpisode(slug: String): ApiResponse<EpisodeStreamData> = apiService.getEpisode(slug)
    suspend fun getGenres(): ApiResponse<GenresData> = apiService.getGenres()
    suspend fun getSchedule(): ApiResponse<ScheduleData> = apiService.getSchedule()

    // Local DB - Watchlist
    val watchlist: Flow<List<WatchlistEntity>> = animeDao.getWatchlist()
    suspend fun addToWatchlist(item: WatchlistEntity) = animeDao.insertWatchlist(item)
    suspend fun removeFromWatchlist(animeId: String) = animeDao.deleteWatchlistById(animeId)
    fun isInWatchlist(animeId: String): Flow<Boolean> = animeDao.isInWatchlist(animeId)

    // Local DB - Continue Watching
    val continueWatchingList: Flow<List<ContinueWatchingEntity>> = animeDao.getContinueWatching()
    suspend fun saveContinueWatching(item: ContinueWatchingEntity) = animeDao.insertContinueWatching(item)
    suspend fun removeContinueWatching(animeId: String) = animeDao.deleteContinueWatching(animeId)
    suspend fun getContinueWatchingForAnime(animeId: String): ContinueWatchingEntity? {
        return animeDao.getContinueWatchingForAnime(animeId)
    }
}
