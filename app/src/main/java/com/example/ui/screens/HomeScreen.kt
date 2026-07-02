package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.ShareUtils
import com.example.ui.components.Localization
import com.example.ui.components.LanguageSwitcher
import com.example.ui.theme.CardBg
import com.example.ui.theme.CardBgElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.SaffronSecondary
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TealMuted
import com.example.ui.viewmodel.LokVaaniViewModel

@Composable
fun HomeScreen(
    viewModel: LokVaaniViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val progressRaw by viewModel.dailyProgress.collectAsState()
    val progress = progressRaw ?: com.example.data.database.DailyProgress("user_progress", 0, 1, 0, "")
    val dailyWord by viewModel.dailyWord.collectAsState()
    val proverb by viewModel.proverb.collectAsState()
    val activeTrack by viewModel.activeTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isPremium by viewModel.isPremiumUser.collectAsState()
    val favoriteAudios by viewModel.favoriteAudios.collectAsState()
    val offlineAudios by viewModel.offlineDownloadedAudios.collectAsState()
    val context = LocalContext.current
    val interfaceLang by viewModel.interfaceLanguage.collectAsState()

    var searchInput by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("All") }
    var selectedDialect by remember { mutableStateOf("All") }
    var wordLearned by remember { mutableStateOf(false) }
    var proverbLearned by remember { mutableStateOf(false) }

    val allAudioTracks = viewModel.repository.staticAudios
    val filteredTracks = remember(searchInput, selectedGenre, selectedDialect) {
        allAudioTracks.filter { audio ->
            val matchesSearch = searchInput.isEmpty() ||
                    audio.title.contains(searchInput, ignoreCase = true) ||
                    audio.language.contains(searchInput, ignoreCase = true) ||
                    audio.category.contains(searchInput, ignoreCase = true)
            val matchesGenre = selectedGenre == "All" || audio.category.equals(selectedGenre, ignoreCase = true)
            val matchesDialect = selectedDialect == "All" || audio.language.equals(selectedDialect, ignoreCase = true)
            
            matchesSearch && matchesGenre && matchesDialect
        }
    }
    val isSearching = searchInput.isNotEmpty() || selectedGenre != "All" || selectedDialect != "All"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp) // Leave room for mini player + nav bar
    ) {
        // --- 1. Top Greeting & Streak Tracker ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Localization.getString("welcome_user", interfaceLang),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaffronPrimary
                    )
                )
                Text(
                    text = if (isPremium) Localization.getString("scholar_rank", interfaceLang) else "Welcome to LokVaani",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            // Streak indicator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SaffronPrimary.copy(alpha = 0.15f))
                    .border(1.dp, SaffronPrimary, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak Fire",
                    tint = SaffronPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${progress.currentStreak} Days",
                    fontWeight = FontWeight.Bold,
                    color = SaffronPrimary,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Instant Search Bar (Top of Main View) ---
        OutlinedTextField(
            value = searchInput,
            onValueChange = {
                searchInput = it
                viewModel.setQuery(it)
            },
            placeholder = { Text("Search title, dialect, or genre...", color = MaterialTheme.colorScheme.onBackground.copy(0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SaffronPrimary) },
            trailingIcon = {
                if (searchInput.isNotEmpty()) {
                    IconButton(onClick = {
                        searchInput = ""
                        viewModel.setQuery("")
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SaffronPrimary,
                unfocusedBorderColor = CardBgElevated,
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isSearching) {
            // Genre filter chips row
            Text(
                text = "Filter by Genre",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = SaffronPrimary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                items(listOf("All", "Bhajan", "Aarti", "Chalisa", "Katha", "Folk Stories", "Kids")) { genre ->
                    val isSelected = selectedGenre == genre
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGenre = genre },
                        label = { Text(genre, fontSize = 12.sp) },
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
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("search_genre_chip_$genre")
                    )
                }
            }

            // Dialect filter chips row
            Text(
                text = "Filter by Dialect",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = SaffronPrimary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                items(listOf("All", "Sanskrit", "Bhojpuri", "Hindi", "Marathi", "Maithili", "Tamil", "Punjabi")) { dialect ->
                    val isSelected = selectedDialect == dialect
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDialect = dialect },
                        label = { Text(dialect, fontSize = 12.sp) },
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
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("search_dialect_chip_$dialect")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search Results (${filteredTracks.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                TextButton(
                    onClick = {
                        searchInput = ""
                        selectedGenre = "All"
                        selectedDialect = "All"
                        viewModel.setQuery("")
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = SaffronPrimary)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Filters", fontSize = 12.sp)
                }
            }

            if (filteredTracks.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.SearchOff,
                    message = "No matching audio tracks found."
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    filteredTracks.forEach { audio ->
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
                                viewModel.playAudio(audio)
                            },
                            onDownload = {
                                if (audio.isPremiumOnly && !isPremium) {
                                    android.widget.Toast.makeText(context, "Premium Required for this track!", android.widget.Toast.LENGTH_SHORT).show()
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
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            // --- Language Switcher Component ---
            LanguageSwitcher(viewModel = viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. Level & XP Progress Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Localization.getString("level", interfaceLang)} ${progress.level} Bhasha Learner",
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Text(
                        text = "${progress.totalXp} ${Localization.getString("total_xp", interfaceLang)}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = SaffronSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val targetXp = progress.level * 200
                val percent = (progress.totalXp.toFloat() / targetXp).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { percent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SaffronPrimary,
                    trackColor = CardBgElevated
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Next level in ${targetXp - progress.totalXp} XP",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. Premium Loyalty / Banner Section ---
        if (!isPremium) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SaffronPrimary, Color(0xFFD84B16))
                        )
                    )
                    .clickable { onNavigateToTab(5) } // Navigate to Profile / premium checkout
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Get LokVaani Premium 🌟",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Unlimited AI Story Generations, Offline Downloads, & Ad-Free Bhajans.",
                            fontSize = 12.sp,
                            color = Color.Black.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { onNavigateToTab(5) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Get Premium",
                            tint = GoldAccent
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- 5. Featured Carousel ---
        Text(
            text = Localization.getString("featured_stories", interfaceLang),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val audio5 = viewModel.repository.staticAudios.firstOrNull { it.id == "audio_5" }
                val isFav = favoriteAudios.any { it.id == "audio_5" }
                val isDownloaded5 = offlineAudios.any { it.id == "audio_5" }
                FeaturedItemCard(
                    title = "Maithili Chhath Mahatmya",
                    subtitle = "Devotional Katha & Lore",
                    image = "https://images.unsplash.com/photo-1561489413-985b06da5bee?q=80&w=400",
                    tag = "Maithili",
                    isFavorite = isFav,
                    isDownloaded = isDownloaded5,
                    onFavoriteClick = {
                        audio5?.let { viewModel.toggleFavorite(it) }
                    },
                    onShareClick = {
                        audio5?.let { ShareUtils.shareAudioTrack(context, it.id, it.title, it.artist) }
                    },
                    onClick = {
                        audio5?.let { viewModel.playAudio(it) }
                    }
                )
            }
            item {
                FeaturedItemCard(
                    title = "AI Regional Stories Studio",
                    subtitle = "Generate any tale in 10 dialects",
                    image = "https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=400",
                    tag = "AI Powered",
                    onClick = { onNavigateToTab(2) } // AI features tab
                )
            }
            item {
                val audio7 = viewModel.repository.staticAudios.firstOrNull { it.id == "audio_7" }
                val isFav = favoriteAudios.any { it.id == "audio_7" }
                val isDownloaded7 = offlineAudios.any { it.id == "audio_7" }
                FeaturedItemCard(
                    title = "Bulleh Shah Sufi Verses",
                    subtitle = "Traditional Punjabi Mysticism",
                    image = "https://images.unsplash.com/photo-1507679799987-c73779587ccf?q=80&w=400",
                    tag = "Punjabi",
                    isFavorite = isFav,
                    isDownloaded = isDownloaded7,
                    onFavoriteClick = {
                        audio7?.let { viewModel.toggleFavorite(it) }
                    },
                    onShareClick = {
                        audio7?.let { ShareUtils.shareAudioTrack(context, it.id, it.title, it.artist) }
                    },
                    onClick = {
                        audio7?.let { viewModel.playAudio(it) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 6. Daily Word & Proverb Cards ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Daily Word Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(210.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Localization.getString("daily_word", interfaceLang), fontSize = 11.sp, color = SaffronSecondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        dailyWord?.let {
                            Text(it.word, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GoldAccent, maxLines = 1)
                            Text("Pronunciation: ${it.pronunciation}", fontSize = 11.sp, color = TextSecondaryDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(it.meaning, fontSize = 11.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Button(
                        onClick = {
                            if (!wordLearned) {
                                wordLearned = true
                                viewModel.completeDailyWord()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (wordLearned) TealMuted else SaffronPrimary
                        ),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (wordLearned) "Learned (+25 XP)" else "Mark Learned", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            // Daily Proverb Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(210.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatQuote, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Localization.getString("daily_proverb", interfaceLang), fontSize = 11.sp, color = SaffronSecondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        proverb?.let {
                            Text(it.text, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("Dialect: ${it.dialect}", fontSize = 11.sp, color = TextSecondaryDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(it.meaning, fontSize = 11.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Button(
                        onClick = {
                            if (!proverbLearned) {
                                proverbLearned = true
                                viewModel.completeDailyProverb()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (proverbLearned) TealMuted else SaffronPrimary
                        ),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (proverbLearned) "Learned (+30 XP)" else "Mark Learned", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 7. Recently Played / Continue Listening ---
        if (activeTrack != null) {
            Text(
                text = "Now Playing Shortcut",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(1) }, // Navigate to Player/Discover tab
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBgElevated)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = activeTrack!!.imageUrl,
                        contentDescription = "Active Track",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(activeTrack!!.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${activeTrack!!.artist} • ${activeTrack!!.language}", fontSize = 12.sp, color = TextSecondaryDark)
                    }
                    IconButton(
                        onClick = { viewModel.togglePlay() },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = SaffronPrimary)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun FeaturedItemCard(
    title: String,
    subtitle: String,
    image: String,
    tag: String,
    isFavorite: Boolean = false,
    isDownloaded: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = image,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Black gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SaffronPrimary)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        if (isDownloaded) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TealMuted)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = "Stored Offline",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text("OFFLINE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onShareClick != null) {
                            IconButton(
                                onClick = {
                                    onShareClick()
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .testTag("featured_share_btn_${title.hashCode()}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Story",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (onFavoriteClick != null) {
                            IconButton(
                                onClick = {
                                    onFavoriteClick()
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .testTag("featured_favorite_btn_${title.hashCode()}")
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isFavorite) "Remove from Collection" else "Add to Collection",
                                    tint = if (isFavorite) SaffronPrimary else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Column {
                    Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
