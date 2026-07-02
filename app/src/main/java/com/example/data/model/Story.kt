package com.example.data.model

import com.squareup.moshi.JsonClass

/**
 * Immutable data model representing a custom Generated Story or folk narrative.
 * Designed for secure local storage and Cloud synchronization.
 */
@JsonClass(generateAdapter = true)
data class Story(
    val id: String,
    val title: String,
    val content: String,
    val dialect: String,
    val category: String,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val userId: String? = null
)
