package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.OfflineAudio
import com.example.data.database.SavedStory
import com.example.data.model.AudioItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ProverbItem(
    val id: String,
    val text: String,
    val translation: String,
    val meaning: String,
    val dialect: String,
    val language: String
)

data class DailyWordItem(
    val id: String,
    val word: String,
    val meaning: String,
    val language: String,
    val pronunciation: String,
    val example: String
)

class ContentRepository(private val database: AppDatabase) {

    // --- Static Audios Library (India's Rich regional devotion & lore) ---
    val staticAudios = listOf(
        AudioItem(
            id = "audio_1",
            title = "Shiv Tandav Stotram",
            artist = "Pandit Ravindra",
            category = "Bhajan",
            language = "Sanskrit",
            durationSeconds = 248,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            imageUrl = "https://images.unsplash.com/photo-1609137144814-1e07357497d5?q=80&w=400",
            description = "A powerful, energetic Sanskrit hymn describing the majestic dance of Lord Shiva.",
            isPremiumOnly = false
        ),
        AudioItem(
            id = "audio_2",
            title = "Kabir Ke Amrit Dohe",
            artist = "Sant Kabir Das",
            category = "Katha",
            language = "Bhojpuri",
            durationSeconds = 312,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            imageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=400",
            description = "Timeless philosophical couplets (dohe) by Saint Kabir, in local regional Awadhi/Bhojpuri blend.",
            isPremiumOnly = false
        ),
        AudioItem(
            id = "audio_3",
            title = "Shree Hanuman Chalisa",
            artist = "Shri Hariharan ji",
            category = "Chalisa",
            language = "Hindi",
            durationSeconds = 515,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            imageUrl = "https://images.unsplash.com/photo-1590076155609-b75a1d7c34ef?q=80&w=400",
            description = "Devotional tribute composed by Goswami Tulsidas in Awadhi, dedicating pure energy to Lord Hanuman.",
            isPremiumOnly = false
        ),
        AudioItem(
            id = "audio_4",
            title = "Ganesh Utsav Aarti",
            artist = "Lata Mangeshkar",
            category = "Aarti",
            language = "Marathi",
            durationSeconds = 180,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            imageUrl = "https://images.unsplash.com/photo-1608925585090-bb0f3316ba69?q=80&w=400",
            description = "Traditional Marathi Sukhkarta Dukhharta Aarti sung during Ganesh Chaturthi.",
            isPremiumOnly = true
        ),
        AudioItem(
            id = "audio_5",
            title = "Chhath Mahaparv Geet",
            artist = "Sharda Sinha",
            category = "Folk Stories",
            language = "Maithili",
            durationSeconds = 295,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            imageUrl = "https://images.unsplash.com/photo-1561489413-985b06da5bee?q=80&w=400",
            description = "Soul-stirring Bihari folk songs dedicated to the Sun God, celebrating simplicity and nature.",
            isPremiumOnly = false
        ),
        AudioItem(
            id = "audio_6",
            title = "Tenali Rama & The Greedy King",
            artist = "Chanda Mama Kids",
            category = "Kids",
            language = "Tamil",
            durationSeconds = 210,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
            imageUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=400",
            description = "Witty tales of the great Tenali Raman in Tamil dialect, teaching clever reasoning and moral virtues.",
            isPremiumOnly = false
        ),
        AudioItem(
            id = "audio_7",
            title = "Bulleh Shah Sufi Kalam",
            artist = "Gurmeet Singh",
            category = "Folk Stories",
            language = "Punjabi",
            durationSeconds = 425,
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
            imageUrl = "https://images.unsplash.com/photo-1507679799987-c73779587ccf?q=80&w=400",
            description = "Mystical Sufi verses of Bulleh Shah, preaching inner love, tolerance, and unity.",
            isPremiumOnly = true
        )
    )

    // --- Static Proverbs Library ---
    val staticProverbs = listOf(
        ProverbItem(
            id = "p_1",
            text = "एक तो करेला, दूजे नीम चढ़ा",
            translation = "A bitter gourd, further climbed onto a neem tree.",
            meaning = "When a naturally bad-natured person gets bad company, or a bad situation gets worse.",
            dialect = "Braj Bhasha / Khadi Boli",
            language = "Hindi"
        ),
        ProverbItem(
            id = "p_2",
            text = "आपन हाथ जगन्नाथ",
            translation = "Your own hand is your Lord Jagannath.",
            meaning = "Self-reliance is the greatest virtue. Doing your own work leads to ultimate satisfaction.",
            dialect = "Odia / Bengali Dialect",
            language = "Bengali"
        ),
        ProverbItem(
            id = "p_3",
            text = "घर की मुर्गी दाल बराबर",
            translation = "The home chicken is valued equivalent to simple lentils.",
            meaning = "Familiarity breeds contempt. People undervalue native talents or home-grown resources.",
            dialect = "Awadhi",
            language = "Hindi"
        ),
        ProverbItem(
            id = "p_4",
            text = "चोर-चोर मौसेरे भाई",
            translation = "Thieves are cousins by extension.",
            meaning = "Birds of a feather flock together; people of similar bad character form quick alliances.",
            dialect = "Bhojpuri",
            language = "Bhojpuri"
        ),
        ProverbItem(
            id = "p_5",
            text = "நாயை கண்டால் கல்லை காணோம்",
            translation = "If you see a dog, you cannot find the stone.",
            meaning = "When a solution is needed, the material resources are missing, and vice versa.",
            dialect = "Sangam Dialect",
            language = "Tamil"
        )
    )

    // --- Static Daily Words ---
    val staticDailyWords = listOf(
        DailyWordItem(
            id = "w_1",
            word = "जुगाड़ (Jugaad)",
            meaning = "A flexible, innovative hack or quick workaround to fix or create something with limited resources.",
            language = "Punjabi / Hindi",
            pronunciation = "Ju-gaadh",
            example = "Using old cycle parts to make a low-cost organic farm sprayer."
        ),
        DailyWordItem(
            id = "w_2",
            word = "खम्मा घणी (Khamma Ghani)",
            meaning = "A traditional royal greeting in Rajasthan expressing deep respect, peace, and abundance.",
            language = "Marwari",
            pronunciation = "Kham-ma Gha-ni",
            example = "Ghani Khamma Sa! Welcome to our humble home in Jaisalmer."
        ),
        DailyWordItem(
            id = "w_3",
            word = "पंत भात (Panta Bhaat)",
            meaning = "A traditional dish of cooked rice soaked in water overnight, cooling the body in summers.",
            language = "Bengali",
            pronunciation = "Pan-tah Bhaat",
            example = "Eating Panta Bhaat with mustard oil and fried green chillies on Pohela Boishakh."
        ),
        DailyWordItem(
            id = "w_4",
            word = "आशीर्वाद (Aashirvaad)",
            meaning = "A warm, sacred blessing from elders wishing for health, longevity, and success.",
            language = "Sanskrit / Hindi",
            pronunciation = "Ah-shir-vaadh",
            example = "Always seek the Aashirvaad of your parents before launching a new venture."
        )
    )

    // --- Offline Audio database Methods ---
    val offlineAudios: Flow<List<OfflineAudio>> = database.offlineAudioDao().getAllOfflineAudios()

    suspend fun saveAudioOffline(audio: AudioItem, context: android.content.Context) {
        var finalLocalPath = audio.audioUrl
        try {
            withContext(Dispatchers.IO) {
                val destinationFile = File(context.filesDir, "audio_${audio.id}.mp3")
                if (destinationFile.exists()) {
                    destinationFile.delete()
                }
                // Download bytes from audioUrl
                val url = URL(audio.audioUrl)
                url.openStream().use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                finalLocalPath = destinationFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val offline = OfflineAudio(
            id = audio.id,
            title = audio.title,
            artist = audio.artist,
            category = audio.category,
            durationSeconds = audio.durationSeconds,
            localUrl = finalLocalPath,
            imageUrl = audio.imageUrl,
            description = audio.description,
            language = audio.language
        )
        database.offlineAudioDao().insertAudio(offline)
    }

    suspend fun removeOfflineAudio(id: String, context: android.content.Context) {
        try {
            withContext(Dispatchers.IO) {
                val file = File(context.filesDir, "audio_${id}.mp3")
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        database.offlineAudioDao().deleteAudioById(id)
    }

    suspend fun isAudioDownloaded(id: String): Boolean {
        return database.offlineAudioDao().isAudioDownloaded(id)
    }

    // --- AI Story Persistence Methods ---
    val savedStories: Flow<List<SavedStory>> = database.savedStoryDao().getAllSavedStories()

    suspend fun saveStory(title: String, content: String, dialect: String, category: String): Long {
        val story = SavedStory(
            title = title,
            content = content,
            dialect = dialect,
            category = category
        )
        return database.savedStoryDao().insertStory(story)
    }

    suspend fun deleteSavedStory(story: SavedStory) {
        database.savedStoryDao().deleteStory(story)
    }

    // --- Favorite Audio Database Methods ---
    val favoriteAudios: Flow<List<com.example.data.database.FavoriteAudio>> = database.favoriteAudioDao().getAllFavoriteAudios()

    suspend fun saveFavoriteAudio(audio: AudioItem) {
        val favorite = com.example.data.database.FavoriteAudio(
            id = audio.id,
            title = audio.title,
            category = audio.category,
            artist = audio.artist,
            durationSeconds = audio.durationSeconds,
            audioUrl = audio.audioUrl,
            imageUrl = audio.imageUrl,
            description = audio.description,
            language = audio.language
        )
        database.favoriteAudioDao().insertFavorite(favorite)
    }

    suspend fun removeFavoriteAudio(id: String) {
        database.favoriteAudioDao().deleteFavoriteById(id)
    }

    suspend fun isAudioFavorite(id: String): Boolean {
        return database.favoriteAudioDao().isAudioFavorite(id)
    }
}
