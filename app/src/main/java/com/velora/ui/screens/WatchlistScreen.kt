package com.velora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velora.data.api.ListingAnime
import com.velora.ui.theme.AccentPink
import com.velora.ui.theme.BgDark
import com.velora.ui.theme.SurfaceDark
import com.velora.ui.theme.TextPrimary
import com.velora.ui.theme.TextSecondary
import com.velora.ui.viewmodel.AnimeViewModel

@Composable
fun WatchlistScreen(
    viewModel: AnimeViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val watchlist by viewModel.watchlist.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(bottom = 72.dp) // Leave screen room for navigation bar
        ) {
            // Heading titles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "My Watchlist",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    modifier = Modifier.testTag("watchlist_title")
                )
                Text(
                    text = "Track your favorite series and movie collections",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            if (watchlist.isEmpty()) {
                // Empty state layout with Illustration mock
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(24.dp)
                            .testTag("watchlist_empty_layout")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(SurfaceDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Empty list marker",
                                tint = AccentPink,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your Watchlist is Empty",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Navigate to any anime detail page and tap the Watchlist button to save it here offline.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                // Watchlist grid of ListingAnime converted elements
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("watchlist_grid")
                ) {
                    items(watchlist, key = { it.animeId }) { item ->
                        val animeData = ListingAnime(
                            animeId = item.animeId,
                            title = item.title,
                            poster = item.poster,
                            type = item.type,
                            score = item.score
                        )
                        GridAnimeCard(
                            anime = animeData,
                            onCardClick = onNavigateToDetail
                        )
                    }
                }
            }
        }
    }
}
