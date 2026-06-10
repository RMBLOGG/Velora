package com.velora.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {
    // Watchlist
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getWatchlist(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE animeId = :animeId")
    suspend fun deleteWatchlistById(animeId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE animeId = :animeId)")
    fun isInWatchlist(animeId: String): Flow<Boolean>

    // Continue Watching
    @Query("SELECT * FROM continue_watching ORDER BY lastWatched DESC")
    fun getContinueWatching(): Flow<List<ContinueWatchingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContinueWatching(item: ContinueWatchingEntity)

    @Query("DELETE FROM continue_watching WHERE animeId = :animeId")
    suspend fun deleteContinueWatching(animeId: String)

    @Query("SELECT * FROM continue_watching WHERE animeId = :animeId LIMIT 1")
    suspend fun getContinueWatchingForAnime(animeId: String): ContinueWatchingEntity?
}
