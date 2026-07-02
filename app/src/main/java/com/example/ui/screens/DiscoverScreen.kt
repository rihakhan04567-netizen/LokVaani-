package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AudioItem
import com.example.ui.components.AudioPlayerManager
import com.example.ui.components.ShareUtils
import com.example.ui.theme.*
import com.example.ui.viewmodel.LokVaaniViewModel
import com.example.riverpod.watch
import com.example.riverpod.notifier
import com.example.riverpod.dialectFilterProvider
import com.example.riverpod.searchQueryProvider
import kotlinx.coroutines.launch

@Composable
fun DiscoverScreen(viewModel: LokVaaniViewModel) {
    val activeTrack by viewModel.activeTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val sleepTimerSecs by viewModel.sleepTimerRemaining.collectAsState()
    val isRepeat by viewModel.isRepeatEnabled.collectAsState()
    val isShuffle by viewModel.isShuffleEnabled.collectAsState()
    val isPremium by viewModel.isPremiumUser.collectAsState()

    val offlineAudios by viewModel.offlineDownloadedAudios.collectAsState()
    val favoriteAudios by viewModel.favoriteAudios.collectAsState()
    val searchQuery = searchQueryProvider.watch()
    val searchQueryNotifier = searchQueryProvider.notifier()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()

    // Riverpod State for dialect filtering
    val selectedDialectFilter = dialectFilterProvider.watch()
    val dialectNotifier = dialectFilterProvider.notifier()

    var showFullPlayer by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var selectedTypeTab by remember { mutableStateOf(0) } // 0: Devotional, 1: Folk Stories, 2: Proverbs
    
    val devotionalSubCategories = listOf("All", "Bhajan", "Aarti", "Chalisa", "Katha")
    var selectedDevotionalSub by remember { mutableStateOf("All") }
    
    val storiesSubCategories = listOf("All", "Folk Stories", "Kids")
    var selectedStoriesSub by remember { mutableStateOf("All") }

    val dialects = listOf("All", "Sanskrit", "Bhojpuri", "Hindi", "Marathi", "Maithili", "Tamil", "Punjabi")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = if (activeTrack != null) 140.dp else 80.dp) // extra padding for mini player
        ) {
            Text(
                text = "संस्कृति संग्रह / Library of Lore",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaffronPrimary
                )
            )
            Text(
                text = "Listen to regional devotion, sacred chants, folktales, and ancient wisdom",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- Search Bar (Persistent State Provider) ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQueryNotifier.setQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("discover_search_bar"),
                placeholder = { Text("खोजें (शीर्षक, कलाकार, श्रेणी) / Search title, keywords...", fontSize = 14.sp) },
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
                            modifier = Modifier.testTag("clear_search_btn")
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

            // --- Offline Mode Banner & Toggle ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("discover_offline_banner"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOfflineMode) Color.Red.copy(alpha = 0.15f) else SaffronSecondary.copy(alpha = 0.12f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isOfflineMode) Color.Red.copy(alpha = 0.4f) else SaffronSecondary.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                            contentDescription = "Connection Status",
                            tint = if (isOfflineMode) Color.Red else SaffronPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isOfflineMode) "Offline Mode Enabled" else "Online Streaming Active",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isOfflineMode) "Only downloaded audio tracks are playable." else "Stream any track or save offline.",
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.setOfflineMode(!isOfflineMode) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOfflineMode) SaffronPrimary else CardBgElevated,
                            contentColor = if (isOfflineMode) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp).testTag("offline_toggle_btn")
                    ) {
                        Text(
                            text = if (isOfflineMode) "Go Online" else "Go Offline",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- Regional Dialect Toggle Component (Riverpod Powered) ---
            Text(
                text = "बोली / Select Dialect",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaffronSecondary
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("dialect_filter_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dialects) { dialect ->
                    val isSelected = dialect == selectedDialectFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { dialectNotifier.setDialect(dialect) },
                        label = { Text(dialect, fontSize = 13.sp) },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Language",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronSecondary,
                            selectedLabelColor = Color.Black,
                            selectedLeadingIconColor = Color.Black,
                            containerColor = CardBg,
                            labelColor = TextSecondaryDark,
                            iconColor = TextSecondaryDark.copy(alpha = 0.7f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = SaffronSecondary,
                            borderColor = CardBgElevated
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("dialect_chip_$dialect")
                    )
                }
            }

            // --- Categorized Library Tabs ---
            TabRow(
                selectedTabIndex = selectedTypeTab,
                containerColor = Color.Transparent,
                contentColor = SaffronPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTypeTab]),
                        color = SaffronPrimary
                    )
                },
                modifier = Modifier.padding(bottom = 16.dp).testTag("library_tab_row")
            ) {
                val tabTitles = listOf("Devotional", "Folk Stories", "Proverbs")
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTypeTab == index,
                        onClick = { selectedTypeTab = index },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (index) {
                                        0 -> "देवभक्ति"
                                        1 -> "लोक कथाएँ"
                                        else -> "कहावतें"
                                    },
                                    fontSize = 11.sp,
                                    color = if (selectedTypeTab == index) SaffronPrimary else TextSecondaryDark.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTypeTab == index) Color.White else TextSecondaryDark
                                )
                            }
                        },
                        modifier = Modifier.testTag("library_tab_$index")
                    )
                }
            }

            // --- Categorized Content Lists with Sub-filters and Search ---
            when (selectedTypeTab) {
                0 -> { // Devotional Tab
                    // Sub-categories Selection Bar for Devotional
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(devotionalSubCategories) { category ->
                            val isSelected = category == selectedDevotionalSub
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDevotionalSub = category },
                                label = { Text(category, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = CardBg,
                                    labelColor = TextSecondaryDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = SaffronPrimary,
                                    borderColor = CardBgElevated
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("devotional_sub_chip_$category")
                            )
                        }
                    }

                    val filteredDevotional = viewModel.repository.staticAudios.filter { audio ->
                        val matchesTabType = audio.category in listOf("Bhajan", "Aarti", "Chalisa", "Katha")
                        val matchesSub = selectedDevotionalSub == "All" || audio.category.equals(selectedDevotionalSub, ignoreCase = true)
                        val matchesQuery = audio.title.contains(searchQuery, ignoreCase = true) ||
                                audio.artist.contains(searchQuery, ignoreCase = true) ||
                                audio.language.contains(searchQuery, ignoreCase = true) ||
                                audio.category.contains(searchQuery, ignoreCase = true)
                        val matchesDialect = selectedDialectFilter == "All" || audio.language.equals(selectedDialectFilter, ignoreCase = true)
                        matchesTabType && matchesSub && matchesQuery && matchesDialect
                    }

                    if (filteredDevotional.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.MusicNote,
                            message = "No devotional tracks found matching filters."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredDevotional) { audio ->
                                val isCurrent = activeTrack?.id == audio.id
                                val isDownloaded = offlineAudios.any { it.id == audio.id }
                                val isFavorite = favoriteAudios.any { it.id == audio.id }

                                AudioTrackRow(
                                    audio = audio,
                                    isCurrent = isCurrent,
                                    isPlaying = isCurrent && isPlaying,
                                    isDownloaded = isDownloaded,
                                    isFavorite = isFavorite,
                                    isPremiumUser = isPremium,
                                    onPlay = {
                                        if (audio.isPremiumOnly && !isPremium) {
                                            // Handle Premium
                                        } else {
                                            viewModel.playAudio(audio)
                                        }
                                    },
                                    onDownload = {
                                        if (audio.isPremiumOnly && !isPremium) {
                                            // Premium required
                                        } else {
                                            if (isDownloaded) {
                                                viewModel.deleteDownloadedAudio(audio.id)
                                            } else {
                                                viewModel.downloadAudio(audio)
                                            }
                                        }
                                    },
                                    onFavoriteToggle = {
                                        viewModel.toggleFavorite(audio)
                                    },
                                    onShare = {
                                        ShareUtils.shareAudioTrack(context, audio.id, audio.title, audio.artist)
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> { // Folk Stories Tab
                    // Sub-categories Selection Bar for Folk Stories
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(storiesSubCategories) { category ->
                            val isSelected = category == selectedStoriesSub
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedStoriesSub = category },
                                label = { Text(category, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = CardBg,
                                    labelColor = TextSecondaryDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = SaffronPrimary,
                                    borderColor = CardBgElevated
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("stories_sub_chip_$category")
                            )
                        }
                    }

                    val filteredStories = viewModel.repository.staticAudios.filter { audio ->
                        val matchesTabType = audio.category in listOf("Folk Stories", "Kids")
                        val matchesSub = selectedStoriesSub == "All" || audio.category.equals(selectedStoriesSub, ignoreCase = true)
                        val matchesQuery = audio.title.contains(searchQuery, ignoreCase = true) ||
                                audio.artist.contains(searchQuery, ignoreCase = true) ||
                                audio.language.contains(searchQuery, ignoreCase = true) ||
                                audio.category.contains(searchQuery, ignoreCase = true)
                        val matchesDialect = selectedDialectFilter == "All" || audio.language.equals(selectedDialectFilter, ignoreCase = true)
                        matchesTabType && matchesSub && matchesQuery && matchesDialect
                    }

                    if (filteredStories.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.MenuBook,
                            message = "No regional stories found matching filters."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredStories) { audio ->
                                val isCurrent = activeTrack?.id == audio.id
                                val isDownloaded = offlineAudios.any { it.id == audio.id }
                                val isFavorite = favoriteAudios.any { it.id == audio.id }

                                AudioTrackRow(
                                    audio = audio,
                                    isCurrent = isCurrent,
                                    isPlaying = isCurrent && isPlaying,
                                    isDownloaded = isDownloaded,
                                    isFavorite = isFavorite,
                                    isPremiumUser = isPremium,
                                    onPlay = {
                                        if (audio.isPremiumOnly && !isPremium) {
                                            // Handle Premium
                                        } else {
                                            viewModel.playAudio(audio)
                                        }
                                    },
                                    onDownload = {
                                        if (audio.isPremiumOnly && !isPremium) {
                                            // Premium required
                                        } else {
                                            if (isDownloaded) {
                                                viewModel.deleteDownloadedAudio(audio.id)
                                            } else {
                                                viewModel.downloadAudio(audio)
                                            }
                                        }
                                    },
                                    onFavoriteToggle = {
                                        viewModel.toggleFavorite(audio)
                                    },
                                    onShare = {
                                        ShareUtils.shareAudioTrack(context, audio.id, audio.title, audio.artist)
                                    }
                                )
                            }
                        }
                    }
                }
                2 -> { // Proverbs Tab
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    val filteredProverbs = viewModel.repository.staticProverbs.filter { proverb ->
                        val matchesQuery = proverb.text.contains(searchQuery, ignoreCase = true) ||
                                proverb.translation.contains(searchQuery, ignoreCase = true) ||
                                proverb.meaning.contains(searchQuery, ignoreCase = true) ||
                                proverb.dialect.contains(searchQuery, ignoreCase = true) ||
                                proverb.language.contains(searchQuery, ignoreCase = true)
                        val matchesDialect = selectedDialectFilter == "All" ||
                                proverb.language.equals(selectedDialectFilter, ignoreCase = true) ||
                                proverb.dialect.contains(selectedDialectFilter, ignoreCase = true)
                        matchesQuery && matchesDialect
                    }

                    if (filteredProverbs.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.Translate,
                            message = "No regional proverbs found matching filters."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredProverbs) { proverb ->
                                ProverbCard(
                                    proverb = proverb,
                                    onShare = {
                                        ShareUtils.shareProverb(
                                            context = context,
                                            text = proverb.text,
                                            translation = proverb.translation,
                                            meaning = proverb.meaning,
                                            dialect = proverb.dialect
                                        )
                                    },
                                    onCopy = {
                                        clipboardManager.setText(
                                            androidx.compose.ui.text.AnnotatedString(
                                                "${proverb.text}\n\nTranslation: ${proverb.translation}\nMeaning: ${proverb.meaning}\nDialect: ${proverb.dialect}"
                                            )
                                        )
                                        android.widget.Toast.makeText(context, "Proverb copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Mini Player (Persistent Bottom Float) ---
        if (activeTrack != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 76.dp) // above standard navigation bar
                    .padding(horizontal = 12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showFullPlayer = true },
                    colors = CardDefaults.cardColors(containerColor = CardBgElevated),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = activeTrack!!.imageUrl,
                            contentDescription = "Cover Image",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeTrack!!.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${activeTrack!!.artist} • ${activeTrack!!.language}",
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(onClick = { viewModel.togglePlay() }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = SaffronPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(onClick = { showFullPlayer = true }) {
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = "Expand Player",
                                tint = TextSecondaryDark
                            )
                        }
                    }
                }
            }
        }

        // --- 4. Animated Full Screen Player Overlay ---
        AnimatedVisibility(
            visible = showFullPlayer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            activeTrack?.let { track ->
                FullAudioPlayerScreen(
                    track = track,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration,
                    playbackSpeed = playbackSpeed,
                    sleepTimerSecs = sleepTimerSecs,
                    isRepeat = isRepeat,
                    isShuffle = isShuffle,
                    isPremium = isPremium,
                    onClose = { showFullPlayer = false },
                    onTogglePlay = { viewModel.togglePlay() },
                    onSeek = { viewModel.seekTo(it) },
                    onNext = { viewModel.next() },
                    onPrev = { viewModel.previous() },
                    onSetSpeed = { viewModel.setSpeed(it) },
                    onSetSleepTimer = { viewModel.setSleepTimer(it) }
                )
            }
        }
    }
}

@Composable
fun AudioTrackRow(
    audio: AudioItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isDownloaded: Boolean,
    isFavorite: Boolean,
    isPremiumUser: Boolean,
    onPlay: () -> Unit,
    onDownload: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onShare: () -> Unit
) {
    val isLocked = audio.isPremiumOnly && !isPremiumUser

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audio_track_${audio.id}")
            .clickable { onPlay() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) SaffronPrimary.copy(alpha = 0.12f) else CardBg
        ),
        border = if (isCurrent) BorderStroke(1.dp, SaffronPrimary) else null
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(50.dp)) {
                AsyncImage(
                    model = audio.imageUrl,
                    contentDescription = audio.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                if (isCurrent && isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Playing Animation",
                            tint = SaffronPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = audio.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isCurrent) SaffronPrimary else MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (audio.isPremiumOnly) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium Track",
                            tint = GoldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (isDownloaded) {
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
                }
                Text(
                    text = "${audio.artist} • ${audio.language}",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
                Text(
                    text = "${audio.category} • ${formatDuration(audio.durationSeconds * 1000)}",
                    fontSize = 11.sp,
                    color = TextSecondaryDark.copy(alpha = 0.7f)
                )
            }

            // Lock or Download Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("share_button_${audio.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share track",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.testTag("favorite_button_${audio.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                        tint = if (isFavorite) SaffronPrimary else TextSecondaryDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Premium Required",
                        tint = GoldAccent,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                } else {
                    IconButton(onClick = onDownload) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.DownloadForOffline,
                            contentDescription = if (isDownloaded) "Downloaded" else "Download Offline",
                            tint = if (isDownloaded) TealMuted else TextSecondaryDark,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FullAudioPlayerScreen(
    track: AudioItem,
    isPlaying: Boolean,
    currentPosition: Int,
    duration: Int,
    playbackSpeed: Float,
    sleepTimerSecs: Int,
    isRepeat: Boolean,
    isShuffle: Boolean,
    isPremium: Boolean,
    onClose: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetSleepTimer: (Int) -> Unit
) {
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showTimerMenu by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Text(
                text = "अब बज रहा है / Now Playing",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SaffronPrimary
            )
            IconButton(onClick = { /* More options mocked */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
            }
        }

        // Center album art with rounded glowing border
        Card(
            modifier = Modifier
                .size(240.dp)
                .border(2.dp, SaffronPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            AsyncImage(
                model = track.imageUrl,
                contentDescription = track.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Title and artist
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${track.artist} • ${track.language}",
                style = MaterialTheme.typography.titleMedium,
                color = SaffronSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SaffronPrimary.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(track.category.uppercase(), fontSize = 11.sp, color = SaffronPrimary, fontWeight = FontWeight.Bold)
            }
        }

        // Custom Oscillating Sound Waveform Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = 6.dp.toPx()
                val spacing = 4.dp.toPx()
                val totalBars = (size.width / (barWidth + spacing)).toInt()
                val centerY = size.height / 2f

                for (i in 0 until totalBars) {
                    // Calculate individual bar height based on sine waves + noise if playing
                    val oscillation = if (isPlaying) {
                        val factor1 = kotlin.math.sin((i.toFloat() * 0.3f) + (waveOffset * 0.05f))
                        val factor2 = kotlin.math.cos((i.toFloat() * 0.15f) - (waveOffset * 0.08f))
                        (factor1 * 0.6f + factor2 * 0.4f)
                    } else {
                        // Static beautiful flat wave shape
                        val centerDistance = kotlin.math.abs(i - (totalBars / 2f)) / (totalBars / 2f)
                        (1f - centerDistance) * 0.3f
                    }

                    val rawHeight = (size.height * 0.8f) * kotlin.math.abs(oscillation)
                    val barHeight = rawHeight.coerceAtLeast(4.dp.toPx())

                    drawRoundRect(
                        color = if (i % 2 == 0) SaffronPrimary else SaffronSecondary,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            x = i * (barWidth + spacing),
                            y = centerY - (barHeight / 2f)
                        ),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
        }

        // Slider progress
        Column {
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { onSeek(it.toInt()) },
                valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = SaffronPrimary,
                    activeTrackColor = SaffronPrimary,
                    inactiveTrackColor = CardBgElevated
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatDuration(currentPosition), color = TextSecondaryDark, fontSize = 12.sp)
                Text(formatDuration(duration), color = TextSecondaryDark, fontSize = 12.sp)
            }
        }

        // Advanced controls: speed and sleep timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed Controller Button with Dropdown
            Box {
                TextButton(
                    onClick = { showSpeedMenu = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = SaffronSecondary)
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${playbackSpeed}x")
                }
                DropdownMenu(
                    expanded = showSpeedMenu,
                    onDismissRequest = { showSpeedMenu = false },
                    modifier = Modifier.background(CardBg)
                ) {
                    listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        DropdownMenuItem(
                            text = { Text("${speed}x", color = Color.White) },
                            onClick = {
                                onSetSpeed(speed)
                                showSpeedMenu = false
                            }
                        )
                    }
                }
            }

            // Sleep Timer Button with Dropdown
            Box {
                TextButton(
                    onClick = { showTimerMenu = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = SaffronSecondary)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (sleepTimerSecs > 0) {
                            "${sleepTimerSecs / 60}:${String.format("%02d", sleepTimerSecs % 60)}"
                        } else {
                            "Sleep Timer"
                        }
                    )
                }
                DropdownMenu(
                    expanded = showTimerMenu,
                    onDismissRequest = { showTimerMenu = false },
                    modifier = Modifier.background(CardBg)
                ) {
                    listOf(0, 10, 20, 30, 60).forEach { mins ->
                        DropdownMenuItem(
                            text = { Text(if (mins == 0) "Turn Off" else "$mins Mins", color = Color.White) },
                            onClick = {
                                onSetSleepTimer(mins)
                                showTimerMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Primary play row (48dp+ targets)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* Toggle Shuffle Mocked */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffle) SaffronPrimary else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onPrev,
                modifier = Modifier.size(54.dp)
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
            }

            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SaffronPrimary)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.Black,
                    modifier = Modifier.size(40.dp)
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(54.dp)
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
            }

            IconButton(
                onClick = { /* Toggle Repeat Mocked */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (isRepeat) SaffronPrimary else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

fun formatDuration(ms: Int): String {
    val totalSecs = ms / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format("%02d:%02d", mins, secs)
}

@Composable
fun EmptyStateView(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = "Empty State Icon",
                tint = SaffronPrimary.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = TextSecondaryDark,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun ProverbCard(
    proverb: com.example.data.repository.ProverbItem,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("proverb_card_${proverb.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBgElevated
        ),
        border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Native Proverb Text
            Text(
                text = proverb.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            // Translation
            Text(
                text = "“${proverb.translation}”",
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Meaning
            Text(
                text = "Meaning:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronSecondary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = proverb.meaning,
                fontSize = 13.sp,
                color = TextSecondaryDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Bottom actions & labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dialect Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SaffronSecondary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, SaffronSecondary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = SaffronSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = proverb.dialect,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SaffronSecondary
                        )
                    }
                }

                // Copy & Share buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(36.dp).testTag("copy_proverb_btn_${proverb.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy proverb",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp).testTag("share_proverb_btn_${proverb.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share proverb",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
