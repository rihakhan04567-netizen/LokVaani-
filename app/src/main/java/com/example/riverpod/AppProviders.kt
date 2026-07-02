package com.example.riverpod

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.database.OfflineAudio
import com.example.data.database.SavedStory
import com.example.data.model.AudioItem
import com.example.data.network.FirebaseWrapper
import com.example.data.repository.ContentRepository
import com.example.data.repository.DailyWordItem
import com.example.data.repository.ProverbItem
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// ==========================================
// --- 1. Authentication State & Notifier ---
// ==========================================

data class AuthState(
    val user: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthNotifier : StateNotifier<AuthState>(AuthState()) {
    init {
        // Safe check for current logged in user at initialization
        state = AuthState(user = FirebaseWrapper.currentUser)
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> {
        state = state.copy(isLoading = true, error = null)
        val result = FirebaseWrapper.signInAnonymously()
        return if (result.isSuccess) {
            val user = result.getOrThrow()
            state = AuthState(user = user, isLoading = false)
            Result.success(user)
        } else {
            val err = result.exceptionOrNull()
            state = AuthState(isLoading = false, error = err?.message ?: "Anonymous Login Failed")
            Result.failure(err ?: Exception("Anonymous Login Failed"))
        }
    }

    suspend fun signInWithEmail(email: String, authCode: String): Result<FirebaseUser> {
        state = state.copy(isLoading = true, error = null)
        val result = FirebaseWrapper.signInWithEmail(email, authCode)
        return if (result.isSuccess) {
            val user = result.getOrThrow()
            state = AuthState(user = user, isLoading = false)
            Result.success(user)
        } else {
            val err = result.exceptionOrNull()
            state = AuthState(isLoading = false, error = err?.message ?: "Sign-in Failed")
            Result.failure(err ?: Exception("Sign-in Failed"))
        }
    }

    suspend fun signUpWithEmail(email: String, authCode: String): Result<FirebaseUser> {
        state = state.copy(isLoading = true, error = null)
        val result = FirebaseWrapper.signUpWithEmail(email, authCode)
        return if (result.isSuccess) {
            val user = result.getOrThrow()
            state = AuthState(user = user, isLoading = false)
            Result.success(user)
        } else {
            val err = result.exceptionOrNull()
            state = AuthState(isLoading = false, error = err?.message ?: "Registration Failed")
            Result.failure(err ?: Exception("Registration Failed"))
        }
    }

    fun signOut() {
        FirebaseWrapper.signOut()
        state = AuthState(user = null, isLoading = false)
    }
}

// ==========================================
// --- 2. Repository State & Notifier ---
// ==========================================

data class RepositoryState(
    val staticAudios: List<AudioItem> = emptyList(),
    val proverbs: List<ProverbItem> = emptyList(),
    val dailyWords: List<DailyWordItem> = emptyList(),
    val offlineAudios: List<OfflineAudio> = emptyList(),
    val savedStories: List<SavedStory> = emptyList(),
    val isLoading: Boolean = false
)

class RepositoryNotifier(private val repository: ContentRepository) : StateNotifier<RepositoryState>(RepositoryState()) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        state = RepositoryState(
            staticAudios = repository.staticAudios,
            proverbs = repository.staticProverbs,
            dailyWords = repository.staticDailyWords,
            isLoading = true
        )

        // Observe Room DB Flow for offline audios
        repository.offlineAudios
            .onEach { audios ->
                state = state.copy(offlineAudios = audios, isLoading = false)
            }
            .launchIn(scope)

        // Observe Room DB Flow for saved AI stories
        repository.savedStories
            .onEach { stories ->
                state = state.copy(savedStories = stories, isLoading = false)
            }
            .launchIn(scope)
    }

    fun saveAudioOffline(audio: AudioItem) {
        scope.launch {
            ProviderContainer.context?.let { ctx ->
                repository.saveAudioOffline(audio, ctx)
            }
        }
    }

    fun removeOfflineAudio(id: String) {
        scope.launch {
            ProviderContainer.context?.let { ctx ->
                repository.removeOfflineAudio(id, ctx)
            }
        }
    }

    fun saveStory(title: String, content: String, dialect: String, category: String) {
        scope.launch {
            repository.saveStory(title, content, dialect, category)
        }
    }

    fun deleteSavedStory(story: SavedStory) {
        scope.launch {
            repository.deleteSavedStory(story)
        }
    }
}

// ==========================================
// --- 3. Riverpod Provider Declarations ---
// ==========================================

val databaseProvider = Provider<AppDatabase> { container ->
    val context = container.context ?: throw IllegalStateException("ProviderContainer must be initialized with a Context.")
    AppDatabase.getDatabase(context)
}

val repositoryProvider = Provider<ContentRepository> { container ->
    val database = container.get(databaseProvider)
    ContentRepository(database)
}

val authNotifierProvider = StateNotifierProvider<AuthNotifier, AuthState> { _ ->
    AuthNotifier()
}

val repositoryNotifierProvider = StateNotifierProvider<RepositoryNotifier, RepositoryState> { container ->
    val repository = container.get(repositoryProvider)
    RepositoryNotifier(repository)
}

// ==========================================
// --- 4. Dialect Filter State & Notifier ---
// ==========================================

class DialectFilterNotifier : StateNotifier<String>("All") {
    fun setDialect(dialect: String) {
        state = dialect
    }
}

val dialectFilterProvider = StateNotifierProvider<DialectFilterNotifier, String> { _ ->
    DialectFilterNotifier()
}

// ==========================================
// --- 5. Search Query State & Notifier ---
// ==========================================

class SearchQueryNotifier : StateNotifier<String>("") {
    fun setQuery(query: String) {
        state = query
    }
}

val searchQueryProvider = StateNotifierProvider<SearchQueryNotifier, String> { _ ->
    SearchQueryNotifier()
}
