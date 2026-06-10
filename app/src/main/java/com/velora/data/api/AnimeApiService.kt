package com.velora.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface AnimeApiService {
    @GET("shk/anime/home")
    suspend fun getHome(): ApiResponse<HomeData>

    @GET("shk/anime/ongoing")
    suspend fun getOngoing(@Query("page") page: Int): ApiResponse<AnimeListData>

    @GET("shk/anime/completed")
    suspend fun getCompleted(@Query("page") page: Int): ApiResponse<AnimeListData>

    @GET("shk/anime/movies")
    suspend fun getMovies(@Query("page") page: Int): ApiResponse<AnimeListData>

    @GET("shk/anime/popular")
    suspend fun getPopular(@Query("page") page: Int): ApiResponse<AnimeListData>

    @GET("shk/anime/search")
    suspend fun search(@Query("q") query: String): ApiResponse<AnimeListData>

    @GET("shk/anime/detail/{slug}")
    suspend fun getDetail(@Path("slug") slug: String): ApiResponse<DetailData>

    @GET("shk/anime/episode/{slug}")
    suspend fun getEpisode(@Path("slug") slug: String): ApiResponse<EpisodeStreamData>

    @GET("shk/anime/genres")
    suspend fun getGenres(): ApiResponse<GenresData>

    @GET("shk/anime/schedule")
    suspend fun getSchedule(): ApiResponse<ScheduleData>

    companion object {
        private const val BASE_URL = "https://dayynime-api.vercel.app/"

        fun create(): AnimeApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AnimeApiService::class.java)
        }
    }
}
