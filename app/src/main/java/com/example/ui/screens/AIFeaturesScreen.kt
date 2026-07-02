package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ShareUtils
import com.example.ui.theme.*
import com.example.ui.viewmodel.LokVaaniViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIFeaturesScreen(viewModel: LokVaaniViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isGenerating by viewModel.isGenerating.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()
    val generatedTitle by viewModel.aiGeneratedTitle.collectAsState()
    val generatedText by viewModel.aiGeneratedText.collectAsState()
    val selectedDialect by viewModel.selectedDialect.collectAsState()
    val selectedCategory by viewModel.selectedGenerationCategory.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    val isOfflineMode by viewModel.isOfflineMode.collectAsState()

    var userPrompt by remember { mutableStateOf("") }
    var translateTargetDialect by remember { mutableStateOf("Bhojpuri") }

    val categories = listOf("Moral Story", "Devotional Bhajan", "Kids Fable", "Traditional Katha", "Local Folk Proverb")
    val dialects = listOf("Hindi", "Bhojpuri", "Maithili", "Braj Bhasha", "Awadhi", "Marwari", "Punjabi", "Marathi", "Tamil", "Bengali")

    var showDialectDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showTranslateDropdown by remember { mutableStateOf(false) }

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
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = SaffronPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "लोकवाणी AI Studio",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaffronPrimary
                )
            )
        }

        Text(
            text = "Generate traditional folk stories, bhajans, and moral tales completely in local Indian regional dialects.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryDark
        )

        // Offline Banner
        if (isOfflineMode) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ai_studio_offline_banner"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline Mode",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Offline Mode Active / इंटरनेट डिस्कनेक्टेड",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "You can read and narrate previously saved stories from your library. New generation requires internet.",
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // --- Configuration Selectors Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Dropdown
            Box(modifier = Modifier.weight(1f)) {
                OutlinedCard(
                    onClick = { showCategoryDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = CardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Category", fontSize = 10.sp, color = SaffronSecondary)
                            Text(selectedCategory, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SaffronPrimary)
                    }
                }
                DropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false },
                    modifier = Modifier.background(CardBg)
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, color = Color.White) },
                            onClick = {
                                viewModel.setGenerationCategory(cat)
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            // Dialect Dropdown
            Box(modifier = Modifier.weight(1f)) {
                OutlinedCard(
                    onClick = { showDialectDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = CardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dialect / भाषा", fontSize = 10.sp, color = SaffronSecondary)
                            Text(selectedDialect, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SaffronPrimary)
                    }
                }
                DropdownMenu(
                    expanded = showDialectDropdown,
                    onDismissRequest = { showDialectDropdown = false },
                    modifier = Modifier.background(CardBg)
                ) {
                    dialects.forEach { dialect ->
                        DropdownMenuItem(
                            text = { Text(dialect, color = Color.White) },
                            onClick = {
                                viewModel.setDialect(dialect)
                                showDialectDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // --- Prompt Inputs ---
        OutlinedTextField(
            value = userPrompt,
            onValueChange = { userPrompt = it },
            label = { Text("Enter story theme, characters, or topics...", color = TextSecondaryDark) },
            placeholder = { Text("e.g. A wise old farmer teaching his sons about hardwork near the fields of Bihar.", fontSize = 13.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("ai_prompt_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SaffronPrimary,
                unfocusedBorderColor = CardBgElevated,
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg
            ),
            maxLines = 4
        )

        // Generate Button
        Button(
            onClick = {
                if (userPrompt.isNotBlank()) {
                    viewModel.generateAIStory(userPrompt)
                } else {
                    Toast.makeText(context, "Please enter a theme/prompt first!", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !isGenerating && !isOfflineMode,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("ai_generate_button"),
            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary, disabledContainerColor = CardBgElevated),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Creating your regional lore...", color = Color.Black, fontWeight = FontWeight.Bold)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isOfflineMode) TextSecondaryDark else Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOfflineMode) "Offline Mode Enabled" else "Generate Regional Lore",
                        color = if (isOfflineMode) TextSecondaryDark else Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Generated Content Display Card ---
        if (generatedText.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Generated Header with copy & share shortcuts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generated Lore ($selectedDialect)",
                            fontWeight = FontWeight.Bold,
                            color = SaffronSecondary,
                            fontSize = 12.sp
                        )

                        Row {
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString("$generatedTitle\n\n$generatedText"))
                                Toast.makeText(context, "Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy text", tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = {
                                Toast.makeText(context, "Simulating share to family...", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Outlined.Share, contentDescription = "Share", tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title
                    Text(
                        text = generatedTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = GoldAccent,
                        lineHeight = 28.sp
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = CardBgElevated
                    )

                    // Text Content
                    Text(
                        text = generatedText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimaryDark,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- Interactive Actions Bar ---
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Narrate / Speak Voice Synthesis Button
                        Button(
                            onClick = { viewModel.toggleTextToSpeech(generatedText) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSpeaking) TealMuted else CardBgElevated
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = if (isSpeaking) Color.White else SaffronPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSpeaking) "Pause AI Narration (TTS)" else "Listen: AI-Narrated Voice (TTS)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // 2. Translate into different Dialect Selector Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1.3f)) {
                                OutlinedCard(
                                    onClick = { showTranslateDropdown = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.outlinedCardColors(containerColor = CardBgElevated),
                                    border = BorderStroke(1.dp, CardBgElevated)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "To: $translateTargetDialect",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryDark
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                                DropdownMenu(
                                    expanded = showTranslateDropdown,
                                    onDismissRequest = { showTranslateDropdown = false },
                                    modifier = Modifier.background(CardBg)
                                ) {
                                    dialects.filter { it != selectedDialect }.forEach { d ->
                                        DropdownMenuItem(
                                            text = { Text(d, color = Color.White) },
                                            onClick = {
                                                translateTargetDialect = d
                                                showTranslateDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { viewModel.translateStory(translateTargetDialect) },
                                enabled = !isTranslating && !isOfflineMode,
                                modifier = Modifier.weight(1.7f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SaffronSecondary,
                                    disabledContainerColor = CardBgElevated
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                if (isTranslating) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.Translate,
                                            contentDescription = null,
                                            tint = if (isOfflineMode) TextSecondaryDark else Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isOfflineMode) "Offline" else "AI Translate",
                                            color = if (isOfflineMode) TextSecondaryDark else Color.Black,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Save & Share Story Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.saveStoryToLibrary()
                                    Toast.makeText(context, "Story saved successfully in Offline Library! 📂", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Story", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    ShareUtils.shareAIStory(context, generatedTitle, generatedText, selectedDialect)
                                },
                                modifier = Modifier.weight(1f).testTag("share_generated_story_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronSecondary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share Story", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
