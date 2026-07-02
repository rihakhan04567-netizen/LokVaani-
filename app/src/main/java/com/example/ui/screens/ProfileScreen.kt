package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.LokVaaniViewModel
import com.example.ui.components.Localization
import com.example.ui.components.LanguageSwitcher
import com.example.riverpod.watch
import com.example.riverpod.notifier
import com.example.riverpod.authNotifierProvider
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(viewModel: LokVaaniViewModel) {
    val context = LocalContext.current

    val progressRaw by viewModel.dailyProgress.collectAsState()
    val progress = progressRaw ?: com.example.data.database.DailyProgress("user_progress", 0, 1, 0, "")
    val isPremium by viewModel.isPremiumUser.collectAsState()
    val interfaceLang by viewModel.interfaceLanguage.collectAsState()

    var showBillingDialog by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf("Monthly") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 120.dp), // spacing for player/bottom nav
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. User Header with Premium Badge ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Avatar with Saffron glow border if premium
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (isPremium) listOf(SaffronPrimary, GoldAccent) else listOf(CardBgElevated, CardBgElevated)
                            )
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(DarkBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPremium) Icons.Default.WorkspacePremium else Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = if (isPremium) GoldAccent else TextSecondaryDark,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Text(
                    text = if (isPremium) "Karan Sharma 🌟" else "Karan Sharma (Guest)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                if (isPremium) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(colors = listOf(SaffronPrimary, GoldAccent))
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(Localization.getString("scholar_rank", interfaceLang), fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardBgElevated)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(Localization.getString("standard_tier", interfaceLang), fontSize = 11.sp, color = TextSecondaryDark, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(title = Localization.getString("total_xp", interfaceLang), value = "${progress.totalXp}")
                    VerticalDivider(color = CardBgElevated, modifier = Modifier.height(30.dp))
                    StatBox(title = Localization.getString("level", interfaceLang), value = "${progress.level}")
                    VerticalDivider(color = CardBgElevated, modifier = Modifier.height(30.dp))
                    StatBox(title = Localization.getString("streak", interfaceLang), value = "${progress.currentStreak} Days")
                }
            }
        }

        // --- Interface Language Switcher ---
        LanguageSwitcher(viewModel = viewModel)

        // --- 2. 5-Day Attendance Streak Calender ---
        Text(
            text = Localization.getString("weekly_activity", interfaceLang),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = SaffronPrimary
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
                val activeDays = listOf(true, true, true, false, false) // 3 day streak active

                days.forEachIndexed { index, day ->
                    val isActive = activeDays[index]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(day, fontSize = 12.sp, color = TextSecondaryDark)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isActive) SaffronPrimary else CardBgElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isActive) Icons.Default.LocalFireDepartment else Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = if (isActive) Color.Black else TextSecondaryDark.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. Achievements & Badges ---
        Text(
            text = Localization.getString("badges_achievements", interfaceLang),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = SaffronPrimary
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BadgeRow(
                    title = Localization.getString("bhasha_mitra", interfaceLang),
                    description = Localization.getString("bhasha_mitra_desc", interfaceLang),
                    icon = Icons.Default.Translate,
                    isUnlocked = true
                )
                HorizontalDivider(color = CardBgElevated)
                BadgeRow(
                    title = Localization.getString("sufi_shishya", interfaceLang),
                    description = Localization.getString("sufi_shishya_desc", interfaceLang),
                    icon = Icons.Default.Audiotrack,
                    isUnlocked = true
                )
                HorizontalDivider(color = CardBgElevated)
                BadgeRow(
                    title = "Sanskriti Samrakshak (Culture Guardian)",
                    description = "Unlock the Patron Premium subscription tier to protect India's folklore.",
                    icon = Icons.Default.WorkspacePremium,
                    isUnlocked = isPremium
                )
            }
        }

        // --- 4. Billing Checkout / Plan Selection Hub ---
        Text(
            text = "प्रीमियम सदस्यता / Premium Membership",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = SaffronPrimary
        )

        if (isPremium) {
            // Cancel subscription Option
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "You are a LokVaani Premium patron! 🎉 Thank you for keeping traditional regional lore alive for generations.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                    OutlinedButton(
                        onClick = {
                            viewModel.cancelSubscription()
                            Toast.makeText(context, "Premium Subscription Cancelled.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                    ) {
                        Text("Cancel Premium Patronage")
                    }
                }
            }
        } else {
            // Pricing layout
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PricingCard(
                    title = "LokVaani Premium Monthly",
                    price = "₹149 / Month",
                    features = listOf("Unlimited AI Story generations", "Full-fidelity offline downloads", "Completely Ad-free", "Premium Patron Golden badge"),
                    tag = "Popular",
                    onClick = {
                        selectedPlan = "Monthly"
                        showBillingDialog = true
                    }
                )

                PricingCard(
                    title = "LokVaani Premium Yearly",
                    price = "₹999 / Year",
                    features = listOf("All Monthly tier features", "45% discount compared to monthly billing", "Family sharing setup", "Exclusive support channels"),
                    tag = "Best Value",
                    onClick = {
                        selectedPlan = "Yearly"
                        showBillingDialog = true
                    }
                )
            }
        }

        // --- Firebase Auth Status Section ---
        val authState = authNotifierProvider.watch()
        val authNotifier = authNotifierProvider.notifier()
        val coroutineScope = rememberCoroutineScope()

        Text(
            "Account Cloud Sync (Firebase) ☁️",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(top = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().testTag("firebase_sync_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (authState.user != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Status: Cloud Connected", color = SaffronPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("User ID: ${authState.user.uid.take(15)}...", fontSize = 11.sp, color = TextSecondaryDark)
                            if (!authState.user.email.isNullOrEmpty()) {
                                Text("Email: ${authState.user.email}", fontSize = 12.sp, color = Color.White)
                            } else {
                                Text("Signed in Anonymously", fontSize = 12.sp, color = Color.White)
                            }
                        }
                        
                        Button(
                            onClick = { authNotifier.signOut() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                        ) {
                            Text("Logout", color = Color.White, fontSize = 12.sp)
                        }
                    }
                } else {
                    Text(
                        "Synchronize your generated stories, offline audio progress, and custom dialect models with production Firebase servers securely.",
                        fontSize = 12.sp,
                        color = TextPrimaryDark
                    )

                    if (authState.error != null) {
                        Text(authState.error, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    authNotifier.signInAnonymously()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                            } else {
                                Text("Anonymous Login", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    authNotifier.signUpWithEmail("demo_user@lokvaani.com", "secure123")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CardBgElevated)
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Text("Create Demo Account", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- 5. Billing Checkout Dialog Simulation ---
        if (showBillingDialog) {
            AlertDialog(
                onDismissRequest = { showBillingDialog = false },
                title = { Text("Google Play Billing Checkout 💳", color = GoldAccent, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Confirm subscription purchase for:")
                        Text(
                            text = if (selectedPlan == "Monthly") "LokVaani Premium Monthly — ₹149/month" else "LokVaani Premium Yearly — ₹999/year",
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        )
                        Text(
                            "This triggers standard Google Play Billing API hooks completely mock verified inside the Sandbox. Money will not be deducted.",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.purchaseSubscription()
                            showBillingDialog = false
                            Toast.makeText(context, "Billing verified. Premium Active! (+200 loyalty XP)", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                    ) {
                        Text("Pay Now", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBillingDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = CardBg
            )
        }
    }
}

@Composable
fun StatBox(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
        Text(title, fontSize = 11.sp, color = TextSecondaryDark)
    }
}

@Composable
fun BadgeRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isUnlocked: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) SaffronPrimary.copy(alpha = 0.2f) else CardBgElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isUnlocked) SaffronPrimary else TextSecondaryDark.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isUnlocked) Color.White else TextSecondaryDark)
                if (!isUnlocked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = TextSecondaryDark, modifier = Modifier.size(12.dp))
                }
            }
            Text(description, fontSize = 11.sp, color = TextSecondaryDark)
        }
    }
}

@Composable
fun PricingCard(
    title: String,
    price: String,
    features: List<String>,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SaffronPrimary)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Text(price, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GoldAccent, modifier = Modifier.padding(vertical = 8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(feature, fontSize = 12.sp, color = TextPrimaryDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Select Plan", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
