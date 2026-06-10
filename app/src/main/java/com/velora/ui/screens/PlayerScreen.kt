package com.velora.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.velora.data.api.EpisodeStreamData
import com.velora.data.api.Server
import com.velora.ui.theme.AccentPink
import com.velora.ui.theme.BgDark
import com.velora.ui.theme.SurfaceDark
import com.velora.ui.theme.TextPrimary
import com.velora.ui.theme.TextSecondary
import com.velora.ui.viewmodel.AnimeViewModel
import com.velora.ui.viewmodel.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun PlayerScreen(
    animeId: String,
    episodeId: String,
    episodeNum: String,
    viewModel: AnimeViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsState()
    val detailState by viewModel.detailState.collectAsState()

    // 1. Force Landscape orientation during playback, restore original on dispose
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }

    // Load episode details on launch or switch
    LaunchedEffect(episodeId) {
        viewModel.loadEpisodeStream(episodeId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            when (val state = playerState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentPink)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Failed to load stream link", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(state.message, color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = onBackClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                                ) {
                                    Text("Go Back")
                                }
                                Button(
                                    onClick = { viewModel.loadEpisodeStream(episodeId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                                ) {
                                    Text("Retry", color = Color.White)
                                }
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val streamData = state.data
                    val animeDetail = (detailState as? UiState.Success)?.data

                    PlayerControllerContainer(
                        animeId = animeId,
                        streamData = streamData,
                        animeTitle = animeDetail?.title ?: "Velora Anime",
                        animePoster = animeDetail?.poster ?: "",
                        viewModel = viewModel,
                        onBackClick = onBackClick,
                        onNavigateEpisode = { nextId, nextNum ->
                            viewModel.loadEpisodeStream(nextId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerControllerContainer(
    animeId: String,
    streamData: EpisodeStreamData,
    animeTitle: String,
    animePoster: String,
    viewModel: AnimeViewModel,
    onBackClick: () -> Unit,
    onNavigateEpisode: (String, String) -> Unit
) {
    // Fallbacks and servers picking
    val servers = streamData.servers ?: emptyList()
    var selectedServer by remember { mutableStateOf<Server?>(null) }

    // Auto select first mp4 server, fallback to any first server
    LaunchedEffect(servers) {
        if (servers.isNotEmpty()) {
            val mp4Server = servers.find { it.type.lowercase() == "mp4" }
            selectedServer = mp4Server ?: servers.first()
        }
    }

    if (selectedServer != null) {
        val server = selectedServer!!
        val isMp4 = server.type.lowercase() == "mp4"

        Box(modifier = Modifier.fillMaxSize()) {
            // Streaming Panel Render
            if (isMp4) {
                Mp4ExoPlayerView(
                    videoUrl = server.embedUrl,
                    animeId = animeId,
                    title = animeTitle,
                    poster = animePoster,
                    episodeId = streamData.episodeId,
                    episodeNum = streamData.episodeNum,
                    viewModel = viewModel
                )
            } else {
                WebViewPlayerView(embedUrl = server.embedUrl)
            }

            // OSD Overlays (Controls/Metadata top row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { onBackClick() }
                        .testTag("player_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = animeTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Playing: Episode ${streamData.episodeNum} - ${streamData.title}",
                        color = AccentPink,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bottom Servers select and control row
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Servers selectors (chips row)
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOURCES:",
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(servers) { s ->
                                val active = s.embedUrl == server.embedUrl
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) AccentPink else SurfaceDark)
                                        .clickable { selectedServer = s }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("server_chip_${s.name.replace(" ", "_")}")
                                ) {
                                    Text(
                                        text = "${s.name} (${s.type})",
                                        color = if (active) Color.White else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Episode forward / backwards navigation buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!streamData.prevEpisode.isNullOrBlank()) {
                            IconButton(
                                onClick = {
                                    val num = (streamData.episodeNum.toIntOrNull() ?: 2) - 1
                                    onNavigateEpisode(streamData.prevEpisode, num.toString())
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SurfaceDark)
                                    .size(34.dp)
                                    .testTag("prev_episode_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipPrevious,
                                    contentDescription = "Prev Episode",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (!streamData.nextEpisode.isNullOrBlank()) {
                            IconButton(
                                onClick = {
                                    val num = (streamData.episodeNum.toIntOrNull() ?: 1) + 1
                                    onNavigateEpisode(streamData.nextEpisode, num.toString())
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(AccentPink)
                                    .size(34.dp)
                                    .testTag("next_episode_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipNext,
                                    contentDescription = "Next Episode",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No stream server available", color = TextSecondary)
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun Mp4ExoPlayerView(
    videoUrl: String,
    animeId: String,
    title: String,
    poster: String,
    episodeId: String,
    episodeNum: String,
    viewModel: AnimeViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize ExoPlayer
    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    // Pull previously saved progress and apply position seek on launch
    LaunchedEffect(exoPlayer) {
        val saved = viewModel.getEpisodeSavedProgress(animeId)
        if (saved != null && saved.episodeId == episodeId && saved.progressMs > 0) {
            exoPlayer.seekTo(saved.progressMs)
        }
    }

    // Position progress saver tick loop (updates position in Room DB periodically)
    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(5000) // save positions every 5s
            val currentPos = exoPlayer.currentPosition
            val totalDuration = exoPlayer.duration
            if (currentPos > 0 && totalDuration > 0) {
                viewModel.saveProgress(
                    animeId = animeId,
                    title = title,
                    poster = poster,
                    episodeId = episodeId,
                    episodeNum = episodeNum,
                    progressMs = currentPos,
                    durationMs = totalDuration
                )
            }
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            // Final progress save when exiting player screen
            val finalPos = exoPlayer.currentPosition
            val finalDur = exoPlayer.duration
            if (finalPos > 0 && finalDur > 0) {
                viewModel.saveProgress(
                    animeId = animeId,
                    title = title,
                    poster = poster,
                    episodeId = episodeId,
                    episodeNum = episodeNum,
                    progressMs = finalPos,
                    durationMs = finalDur
                )
            }
            exoPlayer.release()
        }
    }

    // Android view connection
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                useController = true
                setBackgroundColor(0xFF000000.toInt())
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .testTag("match_panel_exoplayer")
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewPlayerView(embedUrl: String) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    mediaPlaybackRequiresUserGesture = false
                }
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                loadUrl(embedUrl)
            }
        },
        update = { webView ->
            if (webView.url != embedUrl) {
                webView.loadUrl(embedUrl)
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .testTag("match_panel_webview")
    )
}
