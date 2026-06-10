package com.velora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.velora.data.api.ScheduleAnime
import com.velora.data.api.ScheduleDay
import com.velora.ui.theme.AccentPink
import com.velora.ui.theme.BgDark
import com.velora.ui.theme.SurfaceDark
import com.velora.ui.theme.TextPrimary
import com.velora.ui.theme.TextSecondary
import com.velora.ui.theme.YellowStar
import com.velora.ui.viewmodel.AnimeViewModel
import com.velora.ui.viewmodel.UiState

@Composable
fun ScheduleScreen(
    viewModel: AnimeViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheduleState by viewModel.scheduleState.collectAsState()
    var selectedTabIdx by remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(bottom = 72.dp) // Offsets for bottom nav floating pill
        ) {
            // Heading titles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Weekly Schedule",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    modifier = Modifier.testTag("schedule_title")
                )
                Text(
                    text = "Weekly broadcasts calendar of ongoing anime",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            when (val state = scheduleState) {
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
                            Text("Failed to load schedule", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.message, color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadScheduleData() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val days = state.data
                    if (days.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Schedule calendar available", color = TextSecondary)
                        }
                    } else {
                        // Dynamic Tab Row across all days
                        ScrollableTabRow(
                            selectedTabIndex = selectedTabIdx.coerceAtMost(days.size - 1),
                            containerColor = BgDark,
                            contentColor = AccentPink,
                            edgePadding = 16.dp,
                            indicator = { tabPositions ->
                                if (selectedTabIdx < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIdx]),
                                        color = AccentPink
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("schedule_tabs")
                        ) {
                            days.forEachIndexed { index, dayData ->
                                Tab(
                                    selected = selectedTabIdx == index,
                                    onClick = { selectedTabIdx = index },
                                    text = {
                                        Text(
                                            text = dayData.day,
                                            fontWeight = if (selectedTabIdx == index) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp
                                        )
                                    },
                                    selectedContentColor = AccentPink,
                                    unselectedContentColor = TextSecondary
                                )
                            }
                        }

                        // Schedule contents scroll container
                        val activeDay = days[selectedTabIdx.coerceAtMost(days.size - 1)]
                        ScheduleDayList(
                            dayData = activeDay,
                            onCardClick = onNavigateToDetail
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleDayList(
    dayData: ScheduleDay,
    onCardClick: (String) -> Unit
) {
    if (dayData.animeList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No anime scheduled for ${dayData.day}",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("schedule_list_${dayData.day}")
        ) {
            items(dayData.animeList) { anime ->
                ScheduleAnimeItem(
                    anime = anime,
                    onCardClick = onCardClick
                )
            }
        }
    }
}

@Composable
fun ScheduleAnimeItem(
    anime: ScheduleAnime,
    onCardClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick(anime.animeId) }
            .testTag("schedule_card_${anime.animeId}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster Left
            AsyncImage(
                model = anime.poster,
                contentDescription = anime.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp, 100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Info middle
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                // Title
                Text(
                    text = anime.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Time (Hour of broadcast) + Action
                if (!anime.time.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = "Release Time",
                            tint = AccentPink,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = anime.time,
                            color = AccentPink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Badges (Type and Genre)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!anime.type.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = anime.type,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }

                    if (!anime.genre.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentPink.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = anime.genre,
                                color = AccentPink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Score rating on far-right
            if (!anime.score.isNullOrBlank() && anime.score != "0" && anime.score != "0.0") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Score",
                        tint = YellowStar,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = anime.score,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
