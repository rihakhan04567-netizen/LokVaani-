package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.*
import com.example.ui.theme.LokVaaniTheme
import com.example.ui.theme.SaffronPrimary
import com.example.ui.viewmodel.LokVaaniViewModel
import com.example.ui.components.Localization
import java.net.URLDecoder

class MainActivity : ComponentActivity() {
    private val viewModel: LokVaaniViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        handleIntent(intent)

        setContent {
            LokVaaniTheme {
                val selectedTab by viewModel.activeTab.collectAsState()
                val interfaceLang by viewModel.interfaceLanguage.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .testTag("main_bottom_nav_bar"),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            val items = listOf(
                                NavigationItem(Localization.getString("tab_home", interfaceLang), Icons.Default.Home, 0),
                                NavigationItem(Localization.getString("tab_discover", interfaceLang), Icons.Default.LibraryMusic, 1),
                                NavigationItem(Localization.getString("tab_ai_stories", interfaceLang), Icons.Default.AutoAwesome, 2),
                                NavigationItem(Localization.getString("tab_community", interfaceLang), Icons.Default.Forum, 3),
                                NavigationItem(Localization.getString("tab_offline", interfaceLang), Icons.Default.Favorite, 4),
                                NavigationItem(Localization.getString("tab_profile", interfaceLang), Icons.Default.AccountCircle, 5)
                            )

                            items.forEach { item ->
                                NavigationBarItem(
                                    selected = selectedTab == item.index,
                                    onClick = { viewModel.setActiveTab(item.index) },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = if (selectedTab == item.index) Color.Black else SaffronPrimary
                                        )
                                    },
                                    label = { Text(item.label) },
                                    alwaysShowLabel = false, // Hides inactive text labels to keep visual rhythm and avoid overlap
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = SaffronPrimary,
                                        indicatorColor = SaffronPrimary,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToTab = { viewModel.setActiveTab(it) }
                            )
                            1 -> DiscoverScreen(viewModel = viewModel)
                            2 -> AIFeaturesScreen(viewModel = viewModel)
                            3 -> CommunityScreen(viewModel = viewModel)
                            4 -> DownloadsScreen(viewModel = viewModel)
                            5 -> ProfileScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val intentData = intent?.data
        if (intentData != null) {
            val scheme = intentData.scheme
            val host = intentData.host
            if (scheme == "lokvaani" || scheme == "http" || scheme == "https") {
                if (host == "track" || intentData.path?.contains("track") == true) {
                    val trackId = intentData.getQueryParameter("id")
                    if (trackId != null) {
                        viewModel.playAudioById(trackId)
                        viewModel.setActiveTab(1) // Navigate to Discover screen
                    }
                } else if (host == "story" || intentData.path?.contains("story") == true) {
                    val titleEncoded = intentData.getQueryParameter("title")
                    val contentEncoded = intentData.getQueryParameter("content")
                    val dialectEncoded = intentData.getQueryParameter("dialect") ?: "Hindi"
                    
                    if (titleEncoded != null && contentEncoded != null) {
                        try {
                            val title = URLDecoder.decode(titleEncoded, "UTF-8")
                            val content = URLDecoder.decode(contentEncoded, "UTF-8")
                            val dialect = URLDecoder.decode(dialectEncoded, "UTF-8")
                            viewModel.loadStory(title, content, dialect)
                            viewModel.setActiveTab(2) // Navigate to AI Studio (index 2)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val index: Int
)
