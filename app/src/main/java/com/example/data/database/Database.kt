package com.example.data.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- 1. Entities ---

@Entity(tableName = "offline_audios")
data class OfflineAudio(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val artist: String,
    val durationSeconds: Int,
    val localUrl: String,
    val imageUrl: String,
    val description: String,
    val language: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_stories")
data class SavedStory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val dialect: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val translatedText: String? = null,
    val audioUrl: String? = null
)

@Entity(tableName = "daily_progress")
data class DailyProgress(
    @PrimaryKey val id: String, // e.g. "progress_key" (singleton row or date)
    val totalXp: Int,
    val level: Int,
    val currentStreak: Int,
    val lastActiveDate: String // "YYYY-MM-DD"
)

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val text: String,
    val category: String,
    val likesCount: Int,
    val commentsCount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isLikedByUser: Boolean = false,
    val isBookmarkedByUser: Boolean = false
)

@Entity(tableName = "favorite_audios")
data class FavoriteAudio(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val artist: String,
    val language: String,
    val durationSeconds: Int,
    val audioUrl: String,
    val imageUrl: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- 2. DAOs ---

@Dao
interface OfflineAudioDao {
    @Query("SELECT * FROM offline_audios ORDER BY timestamp DESC")
    fun getAllOfflineAudios(): Flow<List<OfflineAudio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudio(audio: OfflineAudio)

    @Delete
    suspend fun deleteAudio(audio: OfflineAudio)

    @Query("DELETE FROM offline_audios WHERE id = :audioId")
    suspend fun deleteAudioById(audioId: String)

    @Query("SELECT EXISTS(SELECT * FROM offline_audios WHERE id = :audioId)")
    suspend fun isAudioDownloaded(audioId: String): Boolean
}

@Dao
interface SavedStoryDao {
    @Query("SELECT * FROM saved_stories ORDER BY timestamp DESC")
    fun getAllSavedStories(): Flow<List<SavedStory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: SavedStory): Long

    @Delete
    suspend fun deleteStory(story: SavedStory)
}

@Dao
interface DailyProgressDao {
    @Query("SELECT * FROM daily_progress WHERE id = :id LIMIT 1")
    fun getProgressFlow(id: String = "singleton_progress"): Flow<DailyProgress?>

    @Query("SELECT * FROM daily_progress WHERE id = :id LIMIT 1")
    suspend fun getProgress(id: String = "singleton_progress"): DailyProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProgress(progress: DailyProgress)
}

@Dao
interface CommunityPostDao {
    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getAllCommunityPosts(): Flow<List<CommunityPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPost)

    @Update
    suspend fun updatePost(post: CommunityPost)
}

@Dao
interface FavoriteAudioDao {
    @Query("SELECT * FROM favorite_audios ORDER BY timestamp DESC")
    fun getAllFavoriteAudios(): Flow<List<FavoriteAudio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(audio: FavoriteAudio)

    @Query("DELETE FROM favorite_audios WHERE id = :audioId")
    suspend fun deleteFavoriteById(audioId: String)

    @Query("SELECT EXISTS(SELECT * FROM favorite_audios WHERE id = :audioId)")
    suspend fun isAudioFavorite(audioId: String): Boolean
}

// --- 3. App Database ---

@Database(
    entities = [OfflineAudio::class, SavedStory::class, DailyProgress::class, CommunityPost::class, FavoriteAudio::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun offlineAudioDao(): OfflineAudioDao
    abstract fun savedStoryDao(): SavedStoryDao
    abstract fun dailyProgressDao(): DailyProgressDao
    abstract fun communityPostDao(): CommunityPostDao
    abstract fun favoriteAudioDao(): FavoriteAudioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lokvaani_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
