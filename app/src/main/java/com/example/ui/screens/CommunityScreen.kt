package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.CommunityPost
import com.example.ui.theme.*
import com.example.ui.viewmodel.LokVaaniViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CommunityScreen(viewModel: LokVaaniViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val posts by viewModel.communityPosts.collectAsState()

    var postInput by remember { mutableStateOf("") }
    var postCategory by remember { mutableStateOf("Folk Stories") }
    var isModerating by remember { mutableStateOf(false) }

    var showCategoryMenu by remember { mutableStateOf(false) }
    val categories = listOf("Folk Stories", "Katha", "Kids", "Bhajan", "Proverbs")

    // Leaderboard Mock Data (Week's top creators)
    val leaderboard = listOf(
        LeaderboardEntry("Kavita Devi", "Awadhi • 12 Posts", 2450, GoldAccent),
        LeaderboardEntry("Raman Swamy", "Tamil • 9 Posts", 2120, TextSecondaryDark),
        LeaderboardEntry("Acharya Anand", "Sanskrit • 7 Posts", 1850, SaffronSecondary),
        LeaderboardEntry("Vikram Marathe", "Marathi • 5 Posts", 1420, TextSecondaryDark)
    )

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
                imageVector = Icons.Default.Forum,
                contentDescription = null,
                tint = SaffronPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "चौपाल / Lok Community",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaffronPrimary
                )
            )
        }

        Text(
            text = "Share devotional verses, native wisdom, or dialect tips with fellow language conservators across India.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryDark
        )

        // --- 1. Share/Post Creator Box ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SaffronPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Draft your native post...", fontWeight = FontWeight.Bold, color = GoldAccent)
                }

                OutlinedTextField(
                    value = postInput,
                    onValueChange = { postInput = it },
                    placeholder = { Text("Write about your village lore, traditional festivals, folk song lyrics, or linguistic heritage...", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("community_post_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        unfocusedBorderColor = CardBgElevated,
                        focusedContainerColor = CardBgElevated,
                        unfocusedContainerColor = CardBgElevated
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Selector for post
                    Box {
                        TextButton(
                            onClick = { showCategoryMenu = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = SaffronSecondary)
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Category: $postCategory", fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            modifier = Modifier.background(CardBg)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = Color.White) },
                                    onClick = {
                                        postCategory = cat
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (postInput.isNotBlank()) {
                                coroutineScope.launch {
                                    isModerating = true
                                    delay(1500) // Simulate AI Cloud Content Moderation (App Check / Guardrail check)
                                    viewModel.addCommunityPost(postInput, postCategory)
                                    isModerating = false
                                    postInput = ""
                                    Toast.makeText(context, "AI Moderation Passed. Post Shared Successfully! (+45 XP)", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Write some content first!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isModerating,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary, disabledContainerColor = CardBgElevated),
                        modifier = Modifier.testTag("community_post_submit")
                    ) {
                        if (isModerating) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Moderating...", fontSize = 12.sp, color = Color.Black)
                        } else {
                            Text("Publish Post", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 2. Weekly Leaderboard (Gamification) ---
        Text(
            text = "शीर्ष लोक संरक्षक / Weekly Leaders",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = SaffronPrimary
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                leaderboard.forEachIndexed { idx, entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${idx + 1}",
                                fontWeight = FontWeight.Bold,
                                color = entry.color,
                                modifier = Modifier.width(32.dp)
                            )
                            Column {
                                Text(entry.name, fontWeight = FontWeight.Bold)
                                Text(entry.details, fontSize = 11.sp, color = TextSecondaryDark)
                            }
                        }
                        Text(
                            text = "${entry.xp} XP",
                            fontWeight = FontWeight.Bold,
                            color = SaffronSecondary
                        )
                    }
                    if (idx < leaderboard.size - 1) {
                        HorizontalDivider(color = CardBgElevated)
                    }
                }
            }
        }

        // --- 3. Community Feed ---
        Text(
            text = "लोक संवाद / Feed",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = SaffronPrimary
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            posts.forEach { post ->
                CommunityPostItem(
                    post = post,
                    onLike = { viewModel.toggleLikePost(post) },
                    onBookmark = { viewModel.toggleBookmarkPost(post) }
                )
            }
        }
    }
}

@Composable
fun CommunityPostItem(
    post: CommunityPost,
    onLike: () -> Unit,
    onBookmark: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SaffronSecondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Face, contentDescription = null, tint = SaffronSecondary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Traditional Conservator", fontSize = 10.sp, color = TextSecondaryDark)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SaffronPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(post.category.uppercase(), fontSize = 9.sp, color = SaffronPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body
            Text(
                text = post.text,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = TextPrimaryDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Footer / Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLike() }
                ) {
                    Icon(
                        imageVector = if (post.isLikedByUser) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByUser) Color.Red else TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.likesCount} Likes", fontSize = 12.sp, color = TextSecondaryDark)
                }

                // Comment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* Comment simulation */ }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.commentsCount} Comments", fontSize = 12.sp, color = TextSecondaryDark)
                }

                // Bookmark Button
                IconButton(onClick = onBookmark, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (post.isBookmarkedByUser) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (post.isBookmarkedByUser) SaffronPrimary else TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

data class LeaderboardEntry(
    val name: String,
    val details: String,
    val xp: Int,
    val color: Color
)
