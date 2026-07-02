package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.CommunityPost
import com.example.data.database.DailyProgress
import com.example.data.database.SavedStory
import com.example.data.network.GeminiApiClient
import com.example.data.repository.ContentRepository
import com.example.data.repository.DailyWordItem
import com.example.data.repository.ProverbItem
import com.example.ui.components.AudioPlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LokVaaniViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val database = AppDatabase.getDatabase(application)
    val repository = ContentRepository(database)

    // --- Offline Access & Network Simulation States ---
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode

    fun setOfflineMode(enabled: Boolean) {
        _isOfflineMode.value = enabled
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
        }
        return false
    }

    // --- Audio Player Stream Management ---
    val activeTrack = AudioPlayerManager.activeTrack
    val isPlaying = AudioPlayerManager.isPlaying
    val currentPosition = AudioPlayerManager.currentPosition
    val duration = AudioPlayerManager.duration
    val playbackSpeed = AudioPlayerManager.playbackSpeed
    val isRepeatEnabled = AudioPlayerManager.isRepeatEnabled
    val isShuffleEnabled = AudioPlayerManager.isShuffleEnabled
    val sleepTimerRemaining = AudioPlayerManager.sleepTimerRemaining

    // --- Search & Filtering States ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    // --- Daily word & Proverb indexes (rotated daily or randomized) ---
    private val _dailyWord = MutableStateFlow<DailyWordItem?>(null)
    val dailyWord: StateFlow<DailyWordItem?> = _dailyWord

    private val _proverb = MutableStateFlow<ProverbItem?>(null)
    val proverb: StateFlow<ProverbItem?> = _proverb

    // --- AI Story Generator States ---
    private val _aiGeneratedTitle = MutableStateFlow("")
    val aiGeneratedTitle: StateFlow<String> = _aiGeneratedTitle

    private val _aiGeneratedText = MutableStateFlow("")
    val aiGeneratedText: StateFlow<String> = _aiGeneratedText

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _selectedDialect = MutableStateFlow("Hindi")
    val selectedDialect: StateFlow<String> = _selectedDialect

    private val _selectedGenerationCategory = MutableStateFlow("Moral Story")
    val selectedGenerationCategory: StateFlow<String> = _selectedGenerationCategory

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating

    // --- Text-to-Speech Status ---
    private var tts: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    // --- Premium subscription flow ---
    private val _isPremiumUser = MutableStateFlow(false)
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser

    // --- Active navigation tab state ---
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
    }

    // --- Interface Language State ---
    private val _interfaceLanguage = MutableStateFlow("English")
    val interfaceLanguage: StateFlow<String> = _interfaceLanguage

    fun setInterfaceLanguage(lang: String) {
        _interfaceLanguage.value = lang
    }

    // --- Room flow observables ---
    val offlineDownloadedAudios = repository.offlineAudios.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteAudios = repository.favoriteAudios.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savedStories = repository.savedStories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val communityPosts = database.communityPostDao().getAllCommunityPosts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dailyProgress = database.dailyProgressDao().getProgressFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyProgress("singleton_progress", 0, 1, 0, "")
    )

    init {
        // Auto-detect network connection to set initial offline mode
        _isOfflineMode.value = !isNetworkAvailable()

        // Initialize static data and default state
        _dailyWord.value = repository.staticDailyWords.random()
        _proverb.value = repository.staticProverbs.random()
        AudioPlayerManager.initPlaylist(repository.staticAudios)

        // Init TextToSpeech
        tts = TextToSpeech(application, this)

        viewModelScope.launch {
            // Seed default community posts if empty
            val currentPosts = database.communityPostDao().getAllCommunityPosts().first()
            if (currentPosts.isEmpty()) {
                seedDefaultCommunityPosts()
            }
            // Seed / update progress
            val progress = database.dailyProgressDao().getProgress()
            if (progress == null) {
                database.dailyProgressDao().updateProgress(
                    DailyProgress(
                        id = "singleton_progress",
                        totalXp = 45,
                        level = 1,
                        currentStreak = 3,
                        lastActiveDate = getTodayDateString()
                    )
                )
            } else {
                // Verify streak
                checkAndIncreaseStreak(progress)
            }
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun checkAndIncreaseStreak(progress: DailyProgress) {
        val today = getTodayDateString()
        if (progress.lastActiveDate != today) {
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
            )
            val newStreak = if (progress.lastActiveDate == yesterday) {
                progress.currentStreak + 1
            } else if (progress.lastActiveDate == "") {
                1
            } else {
                1 // streak broken, reset to 1
            }
            viewModelScope.launch {
                database.dailyProgressDao().updateProgress(
                    progress.copy(
                        currentStreak = newStreak,
                        lastActiveDate = today,
                        totalXp = progress.totalXp + 15 // 15 XP for logging in today
                    )
                )
            }
        }
    }

    fun completeDailyWord() {
        viewModelScope.launch {
            val progress = database.dailyProgressDao().getProgress() ?: return@launch
            database.dailyProgressDao().updateProgress(
                progress.copy(
                    totalXp = progress.totalXp + 25 // 25 XP for learning a daily word
                )
            )
        }
    }

    fun completeDailyProverb() {
        viewModelScope.launch {
            val progress = database.dailyProgressDao().getProgress() ?: return@launch
            database.dailyProgressDao().updateProgress(
                progress.copy(
                    totalXp = progress.totalXp + 30 // 30 XP for learning a daily proverb
                )
            )
        }
    }

    fun setQuery(q: String) {
        _searchQuery.value = q
    }

    fun setCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun setDialect(dialect: String) {
        _selectedDialect.value = dialect
    }

    fun setGenerationCategory(cat: String) {
        _selectedGenerationCategory.value = cat
    }

    // --- Audio operations ---
    fun playAudio(track: com.example.data.model.AudioItem) {
        if (_isOfflineMode.value) {
            val downloadedIds = offlineDownloadedAudios.value.map { it.id }
            if (!downloadedIds.contains(track.id)) {
                android.widget.Toast.makeText(
                    getApplication(),
                    "यह ऑडियो ऑफलाइन उपलब्ध नहीं है। इसे सुनने के लिए इंटरनेट से कनेक्ट करें।",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                return
            }
        }
        AudioPlayerManager.playTrack(getApplication(), track)
    }

    fun toggleFavorite(audio: com.example.data.model.AudioItem) {
        viewModelScope.launch {
            if (repository.isAudioFavorite(audio.id)) {
                repository.removeFavoriteAudio(audio.id)
            } else {
                repository.saveFavoriteAudio(audio)
            }
        }
    }

    fun togglePlay() {
        AudioPlayerManager.togglePlayPause()
    }

    fun seekTo(positionMs: Int) {
        AudioPlayerManager.seekTo(positionMs)
    }

    fun setSpeed(speed: Float) {
        AudioPlayerManager.setPlaybackSpeed(speed)
    }

    fun setSleepTimer(minutes: Int) {
        AudioPlayerManager.startSleepTimer(minutes)
    }

    fun next() {
        AudioPlayerManager.next(getApplication())
    }

    fun previous() {
        AudioPlayerManager.previous(getApplication())
    }

    fun downloadAudio(audio: com.example.data.model.AudioItem) {
        viewModelScope.launch {
            repository.saveAudioOffline(audio, getApplication())
            // Add XP for learning/downloading
            val progress = database.dailyProgressDao().getProgress()
            if (progress != null) {
                database.dailyProgressDao().updateProgress(
                    progress.copy(totalXp = progress.totalXp + 40)
                )
            }
        }
    }

    fun deleteDownloadedAudio(id: String) {
        viewModelScope.launch {
            repository.removeOfflineAudio(id, getApplication())
        }
    }

    // --- Gemini Content Generation ---
    fun generateAIStory(prompt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            _aiGeneratedText.value = ""
            _aiGeneratedTitle.value = ""

            val dialectName = _selectedDialect.value
            val categoryName = _selectedGenerationCategory.value

            val systemPrompt = """
                You are LokVaani AI, an expert cultural narrator and creative writer from India, specializing in traditional regional dialects and folklore.
                Generate a captivating, authentic, and highly cultural $categoryName.
                The response must be written in the traditional regional dialect/language: $dialectName.
                Ensure the tone is warm, traditional, highly respectful, and suitable for families, village folk, and kids in Tier-2/3 India.
                Start the content with a bold Title centered around cultural themes, followed by the content.
                At the end, provide a single line summary of the moral/lesson in simple Hindi.
            """.trimIndent()

            val fullPrompt = """
                Create an original, rich Indian regional $categoryName in the $dialectName dialect.
                Topic/Theme: $prompt
                Structure:
                1. Elegant Native Title
                2. Engaging story paragraphs with rich regional vocabulary, proverbs, and local flavors.
                3. A sweet, warm lesson/moral at the bottom.
            """.trimIndent()

            val result = GeminiApiClient.generate(fullPrompt, systemPrompt)
            _isGenerating.value = false

            if (result.contains("Error:") || result.contains("Please set your GEMINI_API_KEY")) {
                _aiGeneratedTitle.value = "Cultural Story in $dialectName"
                _aiGeneratedText.value = result
            } else {
                // Try to parse out the title (usually the first line)
                val lines = result.split("\n").filter { it.isNotBlank() }
                if (lines.isNotEmpty()) {
                    val candidateTitle = lines.first().replace("#", "").replace("*", "").trim()
                    _aiGeneratedTitle.value = if (candidateTitle.length < 100) candidateTitle else "Story of the Soil"
                    _aiGeneratedText.value = lines.drop(1).joinToString("\n\n")
                } else {
                    _aiGeneratedTitle.value = "Story of the Soil"
                    _aiGeneratedText.value = result
                }

                // Award XP for AI learning
                viewModelScope.launch {
                    val progress = database.dailyProgressDao().getProgress()
                    if (progress != null) {
                        database.dailyProgressDao().updateProgress(
                            progress.copy(totalXp = progress.totalXp + 50)
                        )
                    }
                }
            }
        }
    }

    fun translateStory(targetDialect: String) {
        val currentText = _aiGeneratedText.value
        val currentTitle = _aiGeneratedTitle.value
        if (currentText.isEmpty() || currentText.contains("Error:")) return

        viewModelScope.launch(Dispatchers.IO) {
            _isTranslating.value = true
            val prompt = """
                Translate the following title and story completely into the traditional Indian dialect: $targetDialect.
                Preserve all the original emotion, traditional moral essence, and narrative flow. Use authentic local vocabulary.
                
                Title: $currentTitle
                Story Content:
                $currentText
            """.trimIndent()

            val result = GeminiApiClient.generate(prompt, "You are an expert Indian local linguist translator.")
            _isTranslating.value = false

            if (!result.contains("Error:") && !result.contains("Please set your GEMINI_API_KEY")) {
                val lines = result.split("\n").filter { it.isNotBlank() }
                if (lines.isNotEmpty()) {
                    val parsedTitle = lines.first().replace("#", "").replace("*", "").trim()
                    _aiGeneratedTitle.value = if (parsedTitle.length < 100) parsedTitle else currentTitle
                    _aiGeneratedText.value = lines.drop(1).joinToString("\n\n")
                    _selectedDialect.value = targetDialect
                } else {
                    _aiGeneratedText.value = result
                }
            }
        }
    }

    fun saveStoryToLibrary() {
        val title = _aiGeneratedTitle.value
        val content = _aiGeneratedText.value
        val dialect = _selectedDialect.value
        val category = _selectedGenerationCategory.value
        if (title.isEmpty() || content.isEmpty()) return

        viewModelScope.launch {
            repository.saveStory(title, content, dialect, category)
        }
    }

    fun deleteStoryFromLibrary(story: SavedStory) {
        viewModelScope.launch {
            repository.deleteSavedStory(story)
        }
    }

    fun loadStory(title: String, content: String, dialect: String = "Hindi", category: String = "Moral Story") {
        _aiGeneratedTitle.value = title
        _aiGeneratedText.value = content
        _selectedDialect.value = dialect
        _selectedGenerationCategory.value = category
    }

    fun playAudioById(id: String) {
        val track = repository.staticAudios.find { it.id == id }
        if (track != null) {
            playAudio(track)
        }
    }

    // --- Audio synthesis via TextToSpeech ---
    fun toggleTextToSpeech(text: String) {
        if (_isSpeaking.value) {
            tts?.stop()
            _isSpeaking.value = false
        } else {
            _isSpeaking.value = true
            // Read paragraph by paragraph smoothly
            val cleanText = text.replace("*", "").replace("#", "")
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "story_tts")
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "story_tts")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set regional Hindi/Indian accent locale if available, else fallback to default
            val localeIn = Locale("hi", "IN")
            val result = tts?.setLanguage(localeIn)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.getDefault()
            }
            tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    // --- Community operations ---
    fun addCommunityPost(text: String, category: String) {
        viewModelScope.launch {
            val post = CommunityPost(
                authorName = if (_isPremiumUser.value) "Sanskrit Scholar (Premium)" else "Lok Creator",
                text = text,
                category = category,
                likesCount = 0,
                commentsCount = 0
            )
            database.communityPostDao().insertPost(post)

            // Add XP for contributing to community
            val progress = database.dailyProgressDao().getProgress()
            if (progress != null) {
                database.dailyProgressDao().updateProgress(
                    progress.copy(totalXp = progress.totalXp + 45)
                )
            }
        }
    }

    fun toggleLikePost(post: CommunityPost) {
        viewModelScope.launch {
            val updated = post.copy(
                isLikedByUser = !post.isLikedByUser,
                likesCount = if (post.isLikedByUser) post.likesCount - 1 else post.likesCount + 1
            )
            database.communityPostDao().updatePost(updated)
        }
    }

    fun toggleBookmarkPost(post: CommunityPost) {
        viewModelScope.launch {
            val updated = post.copy(
                isBookmarkedByUser = !post.isBookmarkedByUser
            )
            database.communityPostDao().updatePost(updated)
        }
    }

    private suspend fun seedDefaultCommunityPosts() {
        val seed = listOf(
            CommunityPost(
                authorName = "Pandit Anand Mishra",
                text = "आज सुबह बनारस के अस्सी घाट पर सुप्रसिद्ध कबीर भजनों का संकीर्तन हुआ। गंगा आरती के पावन स्वर और संत कबीर के दोहे सुनकर मन प्रफुल्लित हो उठा।",
                category = "Katha",
                likesCount = 124,
                commentsCount = 18
            ),
            CommunityPost(
                authorName = "Bhojpuri Maati",
                text = "अरे भैया, का आप लोग जानत बानी? हमनी के माटी के लोकगीत चइता और कजरी अब धीरे-धीरे विलुप्त हो रहल बा। एकरा के बचावे खातिर हमनी के रोज सुनना और शेयर करना जरूरी बा।",
                category = "Folk Stories",
                likesCount = 98,
                commentsCount = 24
            ),
            CommunityPost(
                authorName = "Chanda Mama Kids",
                text = "My kids absolutely loved the Tamil Tenali Raman story today! The moral teaching about humility and cleverness is exactly what children need today. Highly recommended!",
                category = "Kids",
                likesCount = 156,
                commentsCount = 12
            )
        )
        seed.forEach { database.communityPostDao().insertPost(it) }
    }

    // --- Premium Flow ---
    fun purchaseSubscription() {
        _isPremiumUser.value = true
        viewModelScope.launch {
            val progress = database.dailyProgressDao().getProgress()
            if (progress != null) {
                database.dailyProgressDao().updateProgress(
                    progress.copy(totalXp = progress.totalXp + 200) // 200 XP premium loyalty bonus!
                )
            }
        }
    }

    fun cancelSubscription() {
        _isPremiumUser.value = false
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
        AudioPlayerManager.release()
    }
}
