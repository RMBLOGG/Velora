package com.velora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.velora.ui.screens.BrowseScreen
import com.velora.ui.screens.DetailScreen
import com.velora.ui.screens.HomeScreen
import com.velora.ui.screens.PlayerScreen
import com.velora.ui.screens.ScheduleScreen
import com.velora.ui.screens.WatchlistScreen
import com.velora.ui.theme.AccentPink
import com.velora.ui.theme.MyApplicationTheme
import com.velora.ui.theme.SurfaceDark
import com.velora.ui.viewmodel.AnimeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AnimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppLayout(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppLayout(viewModel: AnimeViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show floating pill bottom bar only on major hub destinations
    val showBottomBar = currentRoute in listOf("home", "browse", "watchlist", "schedule")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                FloatingPillBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Home screen destination
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { animeId ->
                        navController.navigate("detail/$animeId")
                    },
                    onNavigateToSearch = {
                        navController.navigate("browse")
                    },
                    onNavigateToPlayer = { animeId, episodeId, _, episodeNum ->
                        navController.navigate("player/$animeId/$episodeId/$episodeNum")
                    }
                )
            }

            // 2. Discover / Browse destination
            composable("browse") {
                BrowseScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { animeId ->
                        navController.navigate("detail/$animeId")
                    }
                )
            }

            // 3. Saved Watchlist destination
            composable("watchlist") {
                WatchlistScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { animeId ->
                        navController.navigate("detail/$animeId")
                    }
                )
            }

            // 4. Calendar Schedule destination
            composable("schedule") {
                ScheduleScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { animeId ->
                        navController.navigate("detail/$animeId")
                    }
                )
            }

            // 5. Anime Details Screen with variable slide parameters
            composable(
                route = "detail/{animeId}",
                arguments = listOf(navArgument("animeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getString("animeId") ?: ""
                DetailScreen(
                    animeId = animeId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToPlayer = { aId, epId, _, epNum ->
                        navController.navigate("player/$aId/$epId/$epNum")
                    }
                )
            }

            // 6. Custom Multi-Format Video Player Screen
            composable(
                route = "player/{animeId}/{episodeId}/{episodeNum}",
                arguments = listOf(
                    navArgument("animeId") { type = NavType.StringType },
                    navArgument("episodeId") { type = NavType.StringType },
                    navArgument("episodeNum") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getString("animeId") ?: ""
                val episodeId = backStackEntry.arguments?.getString("episodeId") ?: ""
                val episodeNum = backStackEntry.arguments?.getString("episodeNum") ?: ""

                PlayerScreen(
                    animeId = animeId,
                    episodeId = episodeId,
                    episodeNum = episodeNum,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun FloatingPillBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Safely locks with gestational gestures pill
            .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(28.dp))
                .background(SurfaceDark.copy(alpha = 0.94f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .fillMaxWidth(0.9f)
                .height(56.dp)
                .testTag("floating_pill_nav_bar"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                NavigationItem("home", Icons.Filled.Home, Icons.Outlined.Home),
                NavigationItem("browse", Icons.Filled.Search, Icons.Outlined.Search),
                NavigationItem("watchlist", Icons.Filled.Favorite, Icons.Filled.FavoriteBorder),
                NavigationItem("schedule", Icons.Filled.DateRange, Icons.Filled.DateRange)
            )

            tabs.forEach { tab ->
                val active = currentRoute == tab.route
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .clickable { onNavigate(tab.route) }
                        .padding(vertical = 8.dp)
                        .testTag("nav_tab_${tab.route}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (active) tab.activeIcon else tab.inactiveIcon,
                        contentDescription = tab.route,
                        tint = if (active) AccentPink else Color.Gray.copy(alpha = 0.75f),
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector
)
