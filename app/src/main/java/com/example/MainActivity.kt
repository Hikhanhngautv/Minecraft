package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

enum class MainTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    SANDBOX("Sandbox", Icons.Default.SportsEsports, "nav_tab_sandbox"),
    CRAFTING("Crafting", Icons.Default.Build, "nav_tab_crafting"),
    BREWING("Brewing", Icons.Default.Science, "nav_tab_brewing"),
    PORTAL("Portal", Icons.Default.SwapHoriz, "nav_tab_portal"),
    WAYPOINTS("Waypoints", Icons.Default.Place, "nav_tab_waypoints"),
    BESTIARY("Bestiary", Icons.Default.Pets, "nav_tab_bestiary")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold()
            }
        }
    }
}

@Composable
fun MainAppScaffold() {
    var selectedTab by remember { mutableStateOf(MainTab.SANDBOX) }

    // Back handling: If not on Sandbox, back navigates to Sandbox
    BackHandler(enabled = selectedTab != MainTab.SANDBOX) {
        selectedTab = MainTab.SANDBOX
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF141517),
                contentColor = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF81C784),
                            unselectedIconColor = Color(0xFF757575),
                            unselectedTextColor = Color(0xFF757575),
                            indicatorColor = when (tab) {
                                MainTab.SANDBOX -> Color(0xFF2E7D32)
                                MainTab.CRAFTING -> Color(0xFF388E3C)
                                MainTab.BREWING -> Color(0xFF7B1FA2)
                                MainTab.PORTAL -> Color(0xFF512DA8)
                                MainTab.WAYPOINTS -> Color(0xFF00796B)
                                MainTab.BESTIARY -> Color(0xFFD32F2F)
                            }
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF1E1F22))
        ) {
            when (selectedTab) {
                MainTab.SANDBOX -> GameScreen(
                    onNavigateToCrafting = { selectedTab = MainTab.CRAFTING }
                )
                MainTab.CRAFTING -> CraftingScreen()
                MainTab.BREWING -> BrewingScreen()
                MainTab.PORTAL -> PortalScreen()
                MainTab.WAYPOINTS -> WaypointsScreen()
                MainTab.BESTIARY -> BestiaryScreen()
            }
        }
    }
}
