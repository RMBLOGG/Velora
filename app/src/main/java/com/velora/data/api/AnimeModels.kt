package com.velora.data.api

import com.google.gson.annotations.SerializedName

// Generic API response wrappers
data class ApiResponse<T>(
    @SerializedName("data") val data: T
)

// Home items
data class HomeData(
    @SerializedName("animeList") val animeList: List<HomeAnime>
)

data class HomeAnime(
    @SerializedName("animeId") val animeId: String,
    @SerializedName("title") val title: String,
    @SerializedName("poster") val poster: String,
    @SerializedName("url") val url: String?
)

// List with pagination (ongoing, completed, movies, popular)
data class AnimeListData(
    @SerializedName("animeList") val animeList: List<ListingAnime>,
    @SerializedName("pagination") val pagination: PaginationInfo?
)

data class ListingAnime(
    @SerializedName("animeId") val animeId: String,
    @SerializedName("title") val title: String,
    @SerializedName("poster") val poster: String,
    @SerializedName("type") val type: String?,
    @SerializedName("score") val score: String?
)

data class PaginationInfo(
    @SerializedName("hasNextPage") val hasNextPage: Boolean,
    @SerializedName("hasPrevPage") val hasPrevPage: Boolean,
    @SerializedName("currentPage") val currentPage: Int
)

// Detail
data class DetailData(
    @SerializedName("animeId") val animeId: String,
    @SerializedName("title") val title: String,
    @SerializedName("poster") val poster: String,
    @SerializedName("synopsis") val synopsis: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("score") val score: String?,
    @SerializedName("studio") val studio: String?,
    @SerializedName("info") val info: InfoReleased?,
    @SerializedName("genres") val genres: List<Genre>?,
    @SerializedName("episodeList") val episodeList: List<Episode>?
)

data class InfoReleased(
    @SerializedName("released") val released: String?
)

data class Genre(
    @SerializedName("name") val name: String,
    @SerializedName("genreId") val genreId: String
)

data class Episode(
    @SerializedName("episodeId") val episodeId: String,
    @SerializedName("num") val num: String,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String?
)

// Episode stream / detail
data class EpisodeStreamData(
    @SerializedName("episodeId") val episodeId: String,
    @SerializedName("title") val title: String,
    @SerializedName("animeId") val animeId: String,
    @SerializedName("episodeNum") val episodeNum: String,
    @SerializedName("prevEpisode") val prevEpisode: String?,
    @SerializedName("nextEpisode") val nextEpisode: String?,
    @SerializedName("defaultEmbed") val defaultEmbed: String?,
    @SerializedName("servers") val servers: List<Server>?
)

data class Server(
    @SerializedName("name") val name: String,
    @SerializedName("embedUrl") val embedUrl: String,
    @SerializedName("type") val type: String // "mp4" | "embed" | "blogger" | "mega"
)

// Genres
data class GenresData(
    @SerializedName("genreList") val genreList: List<Genre>
)

// Schedule
data class ScheduleData(
    @SerializedName("days") val days: List<ScheduleDay>
)

data class ScheduleDay(
    @SerializedName("day") val day: String,
    @SerializedName("animeList") val animeList: List<ScheduleAnime>
)

data class ScheduleAnime(
    @SerializedName("title") val title: String,
    @SerializedName("animeId") val animeId: String,
    @SerializedName("poster") val poster: String,
    @SerializedName("score") val score: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("time") val time: String?,
    @SerializedName("genre") val genre: String?
)
