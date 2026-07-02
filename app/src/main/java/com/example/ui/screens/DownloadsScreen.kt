package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import coil.compose.AsyncImage
import com.example.data.database.OfflineAudio
import com.example.data.database.SavedStory
import com.example.ui.components.ShareUtils
import com.example.ui.theme.*
import com.example.ui.viewmodel.LokVaaniViewModel
import com.example.riverpod.watch
import com.example.riverpod.notifier
import com.example.riverpod.searchQueryProvider

@Composable
fun DownloadsScreen(viewModel: LokVaaniViewModel) {
    val context = LocalContext.current

    val offlineAudios by viewModel.offlineDownloadedAudios.collectAsState()
    val favoriteAudios by viewModel.favoriteAudios.collectAsState()
    val savedStories by viewModel.savedStories.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    val searchQuery = searchQueryProvider.watch()
    val searchQueryNotifier = searchQueryProvider.notifier()

    val filteredOfflineAudios = offlineAudios.filter { offline ->
        searchQuery.isEmpty() ||
        offline.title.contains(searchQuery, ignoreCase = true) ||
        offline.artist.contains(searchQuery, ignoreCase = true) ||
        offline.category.contains(searchQuery, ignoreCase = true) ||
        offline.language.contains(searchQuery, ignoreCase = true) ||
        offline.description.contains(searchQuery, ignoreCase = true)
    }

    val filteredFavoriteAudios = favoriteAudios.filter { favorite ->
        searchQuery.isEmpty() ||
        favorite.title.contains(searchQuery, ignoreCase = true) ||
        favorite.artist.contains(searchQuery, ignoreCase = true) ||
        favorite.category.contains(searchQuery, ignoreCase = true) ||
        favorite.language.contains(searchQuery, ignoreCase = true) ||
        favorite.description.contains(searchQuery, ignoreCase = true)
    }

    val filteredSavedStories = savedStories.filter { story ->
        searchQuery.isEmpty() ||
        story.title.contains(searchQuery, ignoreCase = true) ||
        story.content.contains(searchQuery, ignoreCase = true) ||
        story.dialect.contains(searchQuery, ignoreCase = true) ||
        story.category.contains(searchQuery, ignoreCase = true)
    }

    var activeTabState by remember { mutableStateOf(0) } // 0: Audios, 1: Favorites, 2: AI Stories
    var isClearingCache by remember { mutableStateOf(false) }

    // Dynamic storage simulation based on offline lists
    val audiosSize = offlineAudios.size * 4.2 // 4.2 MB average
    val storiesSize = savedStories.size * 0.08 // 80 KB average
    val totalUsedCache = String.format("%.2f", 12.4 + audiosSize + storiesSize)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 120.dp), // spacing for player/bottom nav
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = SaffronPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "मेरी संग्रह / My Collection",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaffronPrimary
                )
            )
        }

        Text(
            text = "Access your favorite hymns, regional stories, and custom-generated AI lore all in one curated collection.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryDark
        )

        // --- Cache Storage Health Meter ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Offline Space Guard", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    TextButton(
                        onClick = {
                            isClearingCache = true
                            Toast.makeText(context, "Optimizing cache buffers...", Toast.LENGTH_SHORT).show()
                            // Mocking simple cache wipe delay
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isClearingCache = false
                                Toast.makeText(context, "Cache optimized and cleaned!", Toast.LENGTH_SHORT).show()
                            }, 1000)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = SaffronPrimary),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(if (isClearingCache) "Cleaning..." else "Optimize Cache", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar (Total 500MB allowance)
                val usedRatio = (((12.4f + audiosSize.toFloat() + storiesSize.toFloat()) / 500f)).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { usedRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SaffronSecondary,
                    trackColor = CardBgElevated
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$totalUsedCache MB Used", fontSize = 11.sp, color = TextSecondaryDark)
                    Text("500 MB Offline Limit", fontSize = 11.sp, color = TextSecondaryDark)
                }
            }
        }

        // --- Search Bar (Persistent State Provider) ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQueryNotifier.setQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("downloads_search_bar"),
            placeholder = { Text("संग्रह में खोजें / Search saved library...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = SaffronPrimary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQueryNotifier.setQuery("") },
                        modifier = Modifier.testTag("clear_downloads_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Search",
                            tint = TextSecondaryDark
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = SaffronPrimary,
                unfocusedBorderColor = CardBgElevated,
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedPlaceholderColor = TextSecondaryDark,
                unfocusedPlaceholderColor = TextSecondaryDark
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // --- Filter Tabs ---
        TabRow(
            selectedTabIndex = activeTabState,
            containerColor = CardBg,
            contentColor = SaffronPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTabState]),
                    color = SaffronPrimary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeTabState == 0,
                onClick = { activeTabState = 0 },
                text = { Text("Downloads (${filteredOfflineAudios.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = activeTabState == 1,
                onClick = { activeTabState = 1 },
                text = { Text("Favorites (${filteredFavoriteAudios.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = activeTabState == 2,
                onClick = { activeTabState = 2 },
                text = { Text("AI Stories (${filteredSavedStories.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        // --- Tab Content ---
        if (activeTabState == 0) {
            // Offline Audios List
            if (offlineAudios.isEmpty()) {
                EmptyStateCard(
                    message = "No offline bhajans downloaded. Explore the Discover tab and tap the download icon next to any tracks!",
                    icon = Icons.Default.LibraryMusic
                )
            } else if (filteredOfflineAudios.isEmpty()) {
                EmptyStateCard(
                    message = "No matching downloads found for \"$searchQuery\". Try a different search term!",
                    icon = Icons.Default.Search
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filteredOfflineAudios.forEach { offline ->
                        OfflineAudioRow(
                            audio = offline,
                            onPlay = {
                                // Maps OfflineAudio back to static AudioItem structure for playback
                                val mapped = com.example.data.model.AudioItem(
                                    id = offline.id,
                                    title = offline.title,
                                    artist = offline.artist,
                                    category = offline.category,
                                    language = offline.language,
                                    durationSeconds = offline.durationSeconds,
                                    audioUrl = offline.localUrl,
                                    imageUrl = offline.imageUrl,
                                    description = offline.description
                                )
                                viewModel.playAudio(mapped)
                            },
                            onDelete = {
                                viewModel.deleteDownloadedAudio(offline.id)
                                Toast.makeText(context, "Download removed from storage.", Toast.LENGTH_SHORT).show()
                            },
                            onShare = {
                                ShareUtils.shareAudioTrack(context, offline.id, offline.title, offline.artist)
                            }
                        )
                    }
                }
            }
        } else if (activeTabState == 1) {
            // Favorite Audios List
            if (favoriteAudios.isEmpty()) {
                EmptyStateCard(
                    message = "No favorite tracks saved. Explore the Discover tab and tap the heart icon next to any tracks to add them here!",
                    icon = Icons.Default.FavoriteBorder
                )
            } else if (filteredFavoriteAudios.isEmpty()) {
                EmptyStateCard(
                    message = "No matching favorites found for \"$searchQuery\". Try a different search term!",
                    icon = Icons.Default.Search
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filteredFavoriteAudios.forEach { favorite ->
                        FavoriteAudioRow(
                            audio = favorite,
                            onPlay = {
                                val mapped = com.example.data.model.AudioItem(
                                    id = favorite.id,
                                    title = favorite.title,
                                    artist = favorite.artist,
                                    category = favorite.category,
                                    language = favorite.language,
                                    durationSeconds = favorite.durationSeconds,
                                    audioUrl = favorite.audioUrl,
                                    imageUrl = favorite.imageUrl,
                                    description = favorite.description
                                )
                                viewModel.playAudio(mapped)
                            },
                            onUnfavorite = {
                                val mapped = com.example.data.model.AudioItem(
                                    id = favorite.id,
                                    title = favorite.title,
                                    artist = favorite.artist,
                                    category = favorite.category,
                                    language = favorite.language,
                                    durationSeconds = favorite.durationSeconds,
                                    audioUrl = favorite.audioUrl,
                                    imageUrl = favorite.imageUrl,
                                    description = favorite.description
                                )
                                viewModel.toggleFavorite(mapped)
                                Toast.makeText(context, "Removed from Favorites.", Toast.LENGTH_SHORT).show()
                            },
                            onShare = {
                                ShareUtils.shareAudioTrack(context, favorite.id, favorite.title, favorite.artist)
                            }
                        )
                    }
                }
            }
        } else {
            // Saved AI Stories List
            if (savedStories.isEmpty()) {
                EmptyStateCard(
                    message = "No AI stories saved yet. Head to AI Studio, draft a prompt, and save the generated lore to your library!",
                    icon = Icons.Default.AutoAwesome
                )
            } else if (filteredSavedStories.isEmpty()) {
                EmptyStateCard(
                    message = "No matching AI stories found for \"$searchQuery\". Try a different search term!",
                    icon = Icons.Default.Search
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filteredSavedStories.forEach { story ->
                        SavedStoryExpandableRow(
                            story = story,
                            isSpeaking = isSpeaking,
                            onDelete = {
                                viewModel.deleteStoryFromLibrary(story)
                                Toast.makeText(context, "Story deleted from library.", Toast.LENGTH_SHORT).show()
                            },
                            onNarrate = {
                                viewModel.toggleTextToSpeech(story.content)
                            },
                            onShare = {
                                ShareUtils.shareAIStory(context, story.title, story.content, story.dialect)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineAudioRow(
    audio: OfflineAudio,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = audio.imageUrl,
                contentDescription = audio.title,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = audio.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TealMuted.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Stored Offline",
                                tint = TealMuted,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "OFFLINE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealMuted
                            )
                        }
                    }
                }
                Text("${audio.artist} • ${audio.language}", fontSize = 11.sp, color = TextSecondaryDark)
                Text("Offline Cache (4.2 MB)", fontSize = 10.sp, color = SaffronSecondary, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = onPlay) {
                Icon(Icons.Default.PlayCircle, contentDescription = "Play Offline", tint = SaffronPrimary, modifier = Modifier.size(28.dp))
            }

            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = TextSecondaryDark, modifier = Modifier.size(22.dp))
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun SavedStoryExpandableRow(
    story: SavedStory,
    isSpeaking: Boolean,
    onDelete: () -> Unit,
    onNarrate: () -> Unit,
    onShare: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(story.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                    Text("Dialect: ${story.dialect} • Category: ${story.category}", fontSize = 11.sp, color = TextSecondaryDark)
                }

                Row {
                    IconButton(onClick = onShare, modifier = Modifier.testTag("share_story_btn_${story.id}")) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Story",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand Story",
                            tint = SaffronPrimary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete Story", tint = Color.Red.copy(alpha = 0.8f))
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = CardBgElevated)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = story.content,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = TextPrimaryDark,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Button(
                    onClick = onNarrate,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CardBgElevated),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = SaffronPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSpeaking) "Pause Voice Narration" else "Listen to Story (Narration)", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteAudioRow(
    audio: com.example.data.database.FavoriteAudio,
    onPlay: () -> Unit,
    onUnfavorite: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("favorite_row_${audio.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = audio.imageUrl,
                contentDescription = audio.title,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(audio.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Text("${audio.artist} • ${audio.language}", fontSize = 11.sp, color = TextSecondaryDark)
                Text("Favorite Track", fontSize = 10.sp, color = SaffronPrimary, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = onPlay, modifier = Modifier.testTag("play_favorite_${audio.id}")) {
                Icon(Icons.Default.PlayCircle, contentDescription = "Play Favorite", tint = SaffronPrimary, modifier = Modifier.size(28.dp))
            }

            IconButton(onClick = onShare, modifier = Modifier.testTag("share_favorite_btn_${audio.id}")) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = TextSecondaryDark, modifier = Modifier.size(22.dp))
            }

            IconButton(onClick = onUnfavorite, modifier = Modifier.testTag("unfavorite_btn_${audio.id}")) {
                Icon(Icons.Default.Favorite, contentDescription = "Remove Favorite", tint = SaffronPrimary)
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SaffronPrimary.copy(alpha = 0.35f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = TextSecondaryDark,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
