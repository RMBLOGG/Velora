package com.velora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velora.ui.theme.AccentPink
import com.velora.ui.theme.BgDark
import com.velora.ui.theme.SurfaceDark
import com.velora.ui.theme.TextPrimary
import com.velora.ui.theme.TextSecondary
import com.velora.ui.viewmodel.AnimeViewModel
import com.velora.ui.viewmodel.UiState

@Composable
fun BrowseScreen(
    viewModel: AnimeViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val browseState by viewModel.browseState.collectAsState()
    val trendingNow by viewModel.trendingNow.collectAsState()
    val allTimePopular by viewModel.allTimePopular.collectAsState()
    val searchResult by viewModel.searchResult.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(bottom = 72.dp) // Offset for bottom nav pill bar
        ) {
            // Heading titles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Discover Anime",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    modifier = Modifier.testTag("discover_title")
                )
                Text(
                    text = "Find movies, series, and ongoing details",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Central Rounded Search bar with leading and trailing widgets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchAnime(it)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("search_text_input"),
                    placeholder = { Text("Search your interest...", color = TextSecondary, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search icon",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear text",
                                tint = AccentPink,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        searchQuery = ""
                                        viewModel.searchAnime("")
                                    }
                                    .testTag("search_clear_button")
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentPink,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Custom trailing filter action button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .clickable { /* Filter clicks, expandable sheets can be integrated in later enhancements */ }
                        .testTag("filter_action_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Filters",
                        tint = AccentPink,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Display context - query active vs inactive
            if (searchQuery.isNotBlank()) {
                val resultsList = searchResult
                if (resultsList == null) {
                    // Fetching or wait
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentPink)
                    }
                } else if (resultsList.isEmpty()) {
                    // Empty list state UI
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
                                "No Anime Found",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.testTag("empty_search_title")
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Try modifying your keywords or check spellings",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.testTag("empty_search_body")
                            )
                        }
                    }
                } else {
                    // Double column scroll grid for search items
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("search_results_grid")
                    ) {
                        items(resultsList, key = { it.animeId }) { anime ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToDetail(anime.animeId) }
                            ) {
                                GridAnimeCard(
                                    anime = anime,
                                    onCardClick = onNavigateToDetail
                                )
                            }
                        }
                    }
                }
            } else {
                // Inactive state - render default modules
                val state = browseState
                when (state) {
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
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Failed to load listings", color = TextPrimary, modifier = Modifier.testTag("browse_error"))
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.loadBrowseScreenData() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
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
                        ) {
                            // Section 1: Trending Now
                            if (trendingNow.isNotEmpty()) {
                                HorizontalAnimeSection(
                                    title = "Trending Now",
                                    list = trendingNow,
                                    onCardClick = onNavigateToDetail
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Section 2: All Time Popular
                            if (allTimePopular.isNotEmpty()) {
                                HorizontalAnimeSection(
                                    title = "All Time Popular",
                                    list = allTimePopular,
                                    onCardClick = onNavigateToDetail
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
