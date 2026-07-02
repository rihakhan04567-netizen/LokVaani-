package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.LokVaaniViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LokVaani", appName)
  }

  @Test
  fun `test viewModel offline mode toggle`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = LokVaaniViewModel(app)

    // Initially, it should be set dynamically based on connection (usually true in unit tests)
    val initialOffline = viewModel.isOfflineMode.value

    // Let's set it to true manually
    viewModel.setOfflineMode(true)
    assertTrue(viewModel.isOfflineMode.value)

    // Toggle it to false
    viewModel.setOfflineMode(false)
    assertFalse(viewModel.isOfflineMode.value)
  }

  @Test
  fun `test repository favorites operations`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val db = com.example.data.database.AppDatabase.getDatabase(app)
    val repository = com.example.data.repository.ContentRepository(db)

    val sampleAudio = com.example.data.model.AudioItem(
        id = "test_track_1",
        title = "Test Bhajan",
        artist = "Test Artist",
        category = "Bhajan",
        language = "Hindi",
        durationSeconds = 120,
        audioUrl = "http://test.com/audio.mp3",
        imageUrl = "http://test.com/img.jpg",
        description = "A wonderful bhajan for testing"
    )

    // Check if initially not favorited
    assertFalse(repository.isAudioFavorite("test_track_1"))

    // Save as favorite
    repository.saveFavoriteAudio(sampleAudio)
    assertTrue(repository.isAudioFavorite("test_track_1"))

    // Remove favorite
    repository.removeFavoriteAudio("test_track_1")
    assertFalse(repository.isAudioFavorite("test_track_1"))
  }

  @Test
  fun `test searchQueryProvider state and filtering`() {
    val notifier = com.example.riverpod.ProviderContainer.get(com.example.riverpod.searchQueryProvider)
    
    // Initial query should be empty
    assertEquals("", notifier.state)
    
    // Setting query
    notifier.setQuery("bhajan")
    assertEquals("bhajan", notifier.state)
    
    // Testing dynamic filtering logic matches
    val audios = listOf(
        com.example.data.model.AudioItem("1", "Morning Bhajan", "Artist A", "Bhajan", "Hindi", 60, "", "", ""),
        com.example.data.model.AudioItem("2", "Evening Aarti", "Artist B", "Aarti", "Hindi", 60, "", "", "")
    )
    
    val query = notifier.state
    val filtered = audios.filter { audio ->
        audio.title.contains(query, ignoreCase = true) ||
        audio.artist.contains(query, ignoreCase = true)
    }
    
    assertEquals(1, filtered.size)
    assertEquals("Morning Bhajan", filtered[0].title)
    
    // Reset query
    notifier.setQuery("")
    assertEquals("", notifier.state)
  }

  @Test
  fun `test activeTab state flow and setter`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = LokVaaniViewModel(app)

    // Initial tab should be 0 (Home)
    assertEquals(0, viewModel.activeTab.value)

    // Set tab to 2 (AI Studio)
    viewModel.setActiveTab(2)
    assertEquals(2, viewModel.activeTab.value)

    // Set tab to 1 (Discover)
    viewModel.setActiveTab(1)
    assertEquals(1, viewModel.activeTab.value)
  }

  @Test
  fun `test loadStory populates AI generation states`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = LokVaaniViewModel(app)

    // Check initially empty
    assertEquals("", viewModel.aiGeneratedTitle.value)
    assertEquals("", viewModel.aiGeneratedText.value)

    // Load custom story
    viewModel.loadStory(
      title = "Test Story Title",
      content = "Test Story Content",
      dialect = "Bhojpuri",
      category = "Folk Stories"
    )

    assertEquals("Test Story Title", viewModel.aiGeneratedTitle.value)
    assertEquals("Test Story Content", viewModel.aiGeneratedText.value)
    assertEquals("Bhojpuri", viewModel.selectedDialect.value)
    assertEquals("Folk Stories", viewModel.selectedGenerationCategory.value)
  }

  @Test
  fun `test playAudioById finds and plays static tracks`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = LokVaaniViewModel(app)

    // Call playAudioById with audio_1
    viewModel.playAudioById("audio_1")
    
    // Check if the current active track is set correctly
    assertEquals("audio_1", viewModel.activeTrack.value?.id)
    assertEquals("Shiv Tandav Stotram", viewModel.activeTrack.value?.title)
  }
}
