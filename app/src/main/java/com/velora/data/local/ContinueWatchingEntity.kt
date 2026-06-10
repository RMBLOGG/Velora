package com.velora.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "continue_watching")
data class ContinueWatchingEntity(
    @PrimaryKey val animeId: String,
    val title: String,
    val poster: String,
    val episodeId: String,
    val episodeNum: String,
    val progressMs: Long,
    val durationMs: Long,
    val lastWatched: Long = System.currentTimeMillis()
)
