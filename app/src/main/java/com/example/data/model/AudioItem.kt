package com.example.data.model

import com.squareup.moshi.JsonClass

/**
 * Immutable data model representing an Audio track, fully serializable
 * with Moshi for local and remote data synchronization.
 */
@JsonClass(generateAdapter = true)
data class AudioItem(
    val id: String,
    val title: String,
    val artist: String,
    val category: String, // "Bhajan", "Aarti", "Chalisa", "Katha", "Folk Stories", "Kids"
    val language: String, // "Hindi", "Bhojpuri", "Maithili", "Punjabi", "Tamil", "Marathi", "Sanskrit", "Bengali"
    val durationSeconds: Int,
    val audioUrl: String, // Real or mock URL for playback
    val imageUrl: String, // Image placeholder or generated url
    val description: String,
    val isPremiumOnly: Boolean = false
)

