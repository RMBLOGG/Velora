package com.velora.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.velora.data.api.DetailData
import com.velora.data.api.Episode
import com.velora.ui.theme.AccentPink
import com.velora.ui.theme.BgDark
import com.velora.ui.theme.SurfaceDark
import com.velora.ui.theme.TextPrimary
import com.velora.ui.theme.TextSecondary
import com.velora.ui.theme.YellowStar
import com.velora.ui.viewmodel.AnimeViewModel
import com.velora.ui.viewmodel.UiState
import kotlinx.coroutines.flow.first

@Composable
fun DetailScreen(
    animeId: String,
    viewModel: AnimeViewModel,
    onBackClick: () -> Unit,
    onNavigateToPlayer: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val detailState by viewModel.detailState.collectAsState()
    val isFavorite by viewModel.isInWatchlist(animeId).collectAsState(initial = false)
    val continueWatchingList by viewModel.continueWatchingList.collectAsState()

    // Query Detail on active launch
    LaunchedEffect(animeId) {
        viewModel.loadAnimeDetail(animeId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = detailState) {
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
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text("Failed to load details", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(state.message, color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadAnimeDetail(animeId) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val detail = state.data
                    val savedWatchingInfo = continueWatchingList.find { it.animeId == detail.animeId }

                    DetailContent(
                        detail = detail,
                        isFavorite = isFavorite,
                        savedProgress = savedWatchingInfo,
                        onBackClick = onBackClick,
                        onWatchlistToggle = { viewModel.toggleWatchlist(detail) },
                        onEpisodeClick = { episode ->
                            onNavigateToPlayer(
                                detail.animeId,
                                episode.episodeId,
                                "Episode ${episode.num}",
                                episode.num
                            )
                        },
                        onPlayFloatingClick = {
                            if (savedWatchingInfo != null) {
                                onNavigateToPlayer(
                                    detail.animeId,
                                    savedWatchingInfo.episodeId,
                                    "Episode ${savedWatchingInfo.episodeNum}",
                                    savedWatchingInfo.episodeNum
                                )
                            } else if (!detail.episodeList.isNullOrEmpty()) {
                                val firstEp = detail.episodeList.first()
                                onNavigateToPlayer(
                                    detail.animeId,
                                    firstEp.episodeId,
                                    "Episode ${firstEp.num}",
                                    firstEp.num
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailContent(
    detail: DetailData,
    isFavorite: Boolean,
    savedProgress: com.velora.data.local.ContinueWatchingEntity?,
    onBackClick: () -> Unit,
    onWatchlistToggle: () -> Unit,
    onEpisodeClick: (Episode) -> Unit,
    onPlayFloatingClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = About, 1 = Episodes
    var isSynopsisExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable content body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp) // Offset room for Floating Bottom button
        ) {
            // Header Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // Blurred poster backdrop
                AsyncImage(
                    model = detail.poster,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(16.dp)
                )

                // Dark-to-light gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    BgDark.copy(alpha = 0.5f),
                                    BgDark
                                )
                            )
                        )
                )

                // Back arrow & watchlist top actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { onBackClick() }
                            .testTag("detail_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { onWatchlistToggle() }
                                .testTag("detail_watchlist_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Watchlist toggle",
                                tint = if (isFavorite) AccentPink else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { onWatchlistToggle() }
                                .testTag("detail_add_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Check else Icons.Filled.Add,
                                contentDescription = "Quick add watch toggle",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Foreground details: thumbnail, title, subtitle chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Small thumbnail
                    AsyncImage(
                        model = detail.poster,
                        contentDescription = detail.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp, 140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .background(SurfaceDark)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Title
                        Text(
                            text = detail.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Subtitle / Studio info
                        if (!detail.studio.isNullOrBlank()) {
                            Text(
                                text = "Studio: ${detail.studio}",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Chips row: Score + Year + Type + Status
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            // Score Pill
                            if (!detail.score.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AccentPink)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Rating",
                                            tint = YellowStar,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = detail.score,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            // Type
                            if (!detail.type.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = detail.type.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            // Released Year
                            val releasedYear = detail.info?.released?.split(" ")?.lastOrNull() 
                                ?: detail.info?.released
                            if (!releasedYear.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = releasedYear,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            // Status
                            if (!detail.status.isNullOrBlank()) {
                                val isReleasing = detail.status.uppercase().contains("ONGOING") 
                                        || detail.status.uppercase().contains("RELEASING")
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isReleasing) AccentPink.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = detail.status.uppercase(),
                                        color = if (isReleasing) AccentPink else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Genre Chips row (pill outline style)
            if (!detail.genres.isNullOrEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    detail.genres.forEach { genre ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, AccentPink.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .background(Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = genre.name,
                                color = AccentPink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Expandable Synopsis section
            if (!detail.synopsis.isNullOrBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Synopsis",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = detail.synopsis,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = if (isSynopsisExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isSynopsisExpanded) "Read Less" else "Read More",
                        color = AccentPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                            .padding(vertical = 4.dp)
                            .testTag("synopsis_expand_toggle")
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Custom tab bar: About | Episodes
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BgDark,
                contentColor = AccentPink,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AccentPink
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("About", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    selectedContentColor = AccentPink,
                    unselectedContentColor = TextSecondary
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Episodes", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    selectedContentColor = AccentPink,
                    unselectedContentColor = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Panels
            if (selectedTab == 0) {
                // About Panel parameters
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AboutInfoRow(label = "Studio", value = detail.studio ?: "-")
                    AboutInfoRow(label = "Status", value = detail.status ?: "-")
                    AboutInfoRow(label = "Type", value = detail.type ?: "-")
                    AboutInfoRow(label = "Release Date", value = detail.info?.released ?: "-")
                }
            } else {
                // Episodes Panel list
                if (detail.episodeList.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No episodes available currently", color = TextSecondary)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        detail.episodeList.forEach { episode ->
                            // Map continue watched history specifically for this episode
                            val continueWatched = if (savedProgress?.episodeId == episode.episodeId) savedProgress else null

                            EpisodeCardItem(
                                episode = episode,
                                posterUrl = detail.poster,
                                continueProgress = continueWatched,
                                onClick = { onEpisodeClick(episode) }
                            )
                        }
                    }
                }
            }
        }

        // Bottom floating playback button "▶ EP {lastEpisode}" or "▶ RESUME EP {num}"
        val hasEpisodes = !detail.episodeList.isNullOrEmpty()
        if (hasEpisodes) {
            val label = if (savedProgress != null) {
                "▶ RESUME EP ${savedProgress.episodeNum}"
            } else {
                "▶ WATCH EP ${detail.episodeList!!.first().num}"
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp, start = 24.dp, end = 24.dp)
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(AccentPink)
                    .clickable { onPlayFloatingClick() }
                    .testTag("detail_floating_play_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        Text(text = value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun EpisodeCardItem(
    episode: Episode,
    posterUrl: String,
    continueProgress: com.velora.data.local.ContinueWatchingEntity?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("episode_item_card_${episode.num}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Episode picture with play indicator and fallback poster
            Box(
                modifier = Modifier
                    .size(90.dp, 60.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
            ) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Half dark filter cover
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )

                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )

                // Dynamic Progress overlay bar if watched
                if (continueProgress != null) {
                    val ratio = if (continueProgress.durationMs > 0) {
                        continueProgress.progressMs.toFloat() / continueProgress.durationMs.toFloat()
                    } else 0f

                    LinearProgressIndicator(
                        progress = { ratio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(3.dp),
                        color = AccentPink,
                        trackColor = Color.Transparent
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Episode ${episode.num}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (episode.title.isNotBlank()) episode.title else "Continuous broadcast",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!episode.date.isNullOrBlank()) {
                    Text(
                        text = episode.date,
                        color = AccentPink,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
