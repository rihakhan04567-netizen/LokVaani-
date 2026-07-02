package com.example.data.model

import com.squareup.moshi.JsonClass

/**
 * Immutable data model representing a User profile.
 * Designed for secure serialization with Firestore and JSON endpoints.
 */
@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    val email: String?,
    val displayName: String?,
    val isPremium: Boolean = false,
    val joinedAtTimestamp: Long = System.currentTimeMillis(),
    val preferredDialect: String? = null
)
