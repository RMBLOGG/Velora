package com.velora.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val animeId: String,
    val title: String,
    val poster: String,
    val type: String?,
    val score: String?,
    val addedAt: Long = System.currentTimeMillis()
)
