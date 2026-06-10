package com.velora.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.velora.data.api.HomeAnime
import com.velora.data.api.ListingAnime
import com.velora.data.local.ContinueWatchingEntity
import com.velora.ui.theme.AccentPink
import com.velora.ui.theme.BgDark
import com.velora.ui.theme.SurfaceDark
import com.velora.ui.theme.TextPrimary
import com.velora.ui.theme.TextSecondary
import com.velora.ui.theme.YellowStar
import com.velora.ui.viewmodel.AnimeViewModel
import com.velora.ui.viewmodel.UiState

@Composable
fun HomeScreen(
    viewModel: AnimeViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToPlayer: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val homeState by viewModel.homeState.collectAsState()
    val carouselTrending by viewModel.carouselTrending.collectAsState()
    val trendingAnime by viewModel.trendingAnime.collectAsState()
    val ongoingAnime by viewModel.ongoingAnime.collectAsState()
    val popularAnime by viewModel.popularAnime.collectAsState()
    val moviesAnime by viewModel.moviesAnime.collectAsState()
    val continueWatchingList by viewModel.continueWatchingList.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgDark,
        topBar = {
            HomeHeader(
                onSearchClick = onNavigateToSearch,
                onSettingsClick = { showAboutDialog = true }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = homeState) {
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
                            Text(
                                text = "Something went wrong",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("error_title")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                color = TextSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.testTag("error_detail")
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadHomeScreenData() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("retry_button")
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 80.dp) // Leave screen room for the floating tab bar
                    ) {
                        // 1. Trending Carousel Spotlight
                        if (carouselTrending.isNotEmpty()) {
                            SpotlightCarousel(
                                animeList = carouselTrending,
                                onCardClick = onNavigateToDetail
                            )
                        }

                        // 2. Continue Watching Section
                        if (continueWatchingList.isNotEmpty()) {
                            ContinueWatchingSection(
                                list = continueWatchingList,
                                onResumeClick = onNavigateToPlayer
                            )
                        }

                        // 3. Trending Anime Grid
                        if (trendingAnime.isNotEmpty()) {
                            HorizontalAnimeSection(
                                title = "Trending Anime",
                                list = trendingAnime,
                                onCardClick = onNavigateToDetail
                            )
                        }

                        // 4. Ongoing Section
                        if (ongoingAnime.isNotEmpty()) {
                            HorizontalAnimeSection(
                                title = "Ongoing Series",
                                list = ongoingAnime,
                                onCardClick = onNavigateToDetail
                            )
                        }

                        // 5. Popular Section
                        if (popularAnime.isNotEmpty()) {
                            HorizontalAnimeSection(
                                title = "Popular Completed",
                                list = popularAnime,
                                onCardClick = onNavigateToDetail
                            )
                        }

                        // 6. Movies Section
                        if (moviesAnime.isNotEmpty()) {
                            HorizontalAnimeSection(
                                title = "Anime Movies",
                                list = moviesAnime,
                                onCardClick = onNavigateToDetail
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = SurfaceDark,
            title = { Text("About Velora", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Velora is an ultra-premium offline-capable native anime streaming client powered by the Dayynime endpoint API.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Designed with aesthetic precision and high-fidelity layouts.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showAboutDialog = false }
                ) {
                    Text("OK", color = AccentPink)
                }
            }
        )
    }
}

@Composable
fun HomeHeader(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo / Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentPink),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "V",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "VELORA",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = 1.5.sp
            )
        }

        // Header controls (rounded buttons)
        Row {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceDark)
                    .clickable { onSearchClick() }
                    .testTag("search_icon_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceDark)
                    .clickable { onSettingsClick() }
                    .testTag("settings_icon_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SpotlightCarousel(
    animeList: List<HomeAnime>,
    onCardClick: (String) -> Unit
) {
    val count = animeList.size
    val pagerState = rememberPagerState(pageCount = { count })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trending",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(AccentPink.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count",
                    color = AccentPink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .testTag("trending_carousel")
        ) { page ->
            val anime = animeList[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .clickable { onCardClick(anime.animeId) }
                    .testTag("carousel_card_${anime.animeId}")
            ) {
                // Background image
                AsyncImage(
                    model = anime.poster,
                    contentDescription = anime.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top overlay gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // "RELEASING" top left badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentPink)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "RELEASING",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                // Star Score top right badge (Fallbacks to high rating)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = YellowStar,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "8.5", // Static average star for spot checks
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                // Type bottom-left badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "TV",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }

                // Bottom title text
                Text(
                    text = anime.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }
        }

        // Pager indicator dots
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(count.coerceAtMost(8)) { index -> // Max 8 indicators
                val isSelected = pagerState.currentPage % count.coerceAtMost(8) == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) AccentPink else Color.Gray.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
fun ContinueWatchingSection(
    list: List<ContinueWatchingEntity>,
    onResumeClick: (String, String, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Continue Watching",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "See All",
                tint = AccentPink,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("continue_watching_row")
        ) {
            items(list, key = { it.animeId }) { item ->
                Column(
                    modifier = Modifier
                        .width(220.dp)
                        .clickable {
                            onResumeClick(
                                item.animeId,
                                item.episodeId,
                                "Episode ${item.episodeNum}",
                                item.episodeNum
                            )
                        }
                        .testTag("continue_card_${item.animeId}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(124.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark)
                    ) {
                        // Poster landscape ratio
                        AsyncImage(
                            model = item.poster,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Dark overlay cover
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.40f))
                        )

                        // Floating play button center
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AccentPink),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Episode badge bottom right
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "EP ${item.episodeNum}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Title
                    Text(
                        text = item.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Episode text
                    Text(
                        text = "Episode ${item.episodeNum}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Video progress calculation bar
                    val progressRatio = if (item.durationMs > 0) {
                        item.progressMs.toFloat() / item.durationMs.toFloat()
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progressRatio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = AccentPink,
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalAnimeSection(
    title: String,
    list: List<ListingAnime>,
    onCardClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = ">",
                color = AccentPink,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("section_row_${title.replace(" ", "_")}")
        ) {
            items(list, key = { it.animeId }) { anime ->
                GridAnimeCard(
                    anime = anime,
                    onCardClick = onCardClick
                )
            }
        }
    }
}

@Composable
fun GridAnimeCard(
    anime: ListingAnime,
    onCardClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable { onCardClick(anime.animeId) }
            .testTag("anime_card_${anime.animeId}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
        ) {
            // Poster
            AsyncImage(
                model = anime.poster,
                contentDescription = anime.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top-right Score pill
            if (!anime.score.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentPink)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
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
                            text = anime.score,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        )
                    }
                }
            }

            // Bottom-left Type pill
            if (!anime.type.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = anime.type.trim(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title below poster
        Text(
            text = anime.title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
