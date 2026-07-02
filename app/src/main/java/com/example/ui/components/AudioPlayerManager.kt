package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.data.model.AudioItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@UnstableApi
object AudioPlayerManager {
    private const val TAG = "AudioPlayerManager"

    @Volatile
    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var updateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _activeTrack = MutableStateFlow<AudioItem?>(null)
    val activeTrack: StateFlow<AudioItem?> = _activeTrack

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed

    private val _isRepeatEnabled = MutableStateFlow(false)
    val isRepeatEnabled: StateFlow<Boolean> = _isRepeatEnabled

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled

    private val _sleepTimerRemaining = MutableStateFlow(-1) // -1 means inactive, else seconds remaining
    val sleepTimerRemaining: StateFlow<Int> = _sleepTimerRemaining

    private var playlist: List<AudioItem> = emptyList()
    private var sleepTimerJob: Job? = null

    @Synchronized
    fun getMediaSession(context: Context): MediaSession? {
        if (exoPlayer == null) {
            val appContext = context.applicationContext
            Log.i(TAG, "Creating background-enabled ExoPlayer & MediaSession")
            
            val player = ExoPlayer.Builder(appContext)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                    true
                )
                .setHandleAudioBecomingNoisy(true)
                .build()

            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            _duration.value = player.duration.toInt()
                        }
                        Player.STATE_ENDED -> {
                            handleCompletion(appContext)
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                    _isPlaying.value = isPlayingValue
                    if (isPlayingValue) {
                        startProgressTracker()
                    } else {
                        updateJob?.cancel()
                    }
                }
            })

            exoPlayer = player
            mediaSession = MediaSession.Builder(appContext, player).build()
        }
        return mediaSession
    }

    fun initPlaylist(tracks: List<AudioItem>) {
        this.playlist = tracks
    }

    fun playTrack(context: Context, track: AudioItem) {
        val appContext = context.applicationContext
        
        val session = getMediaSession(appContext)
        val player = exoPlayer ?: return

        try {
            val serviceIntent = Intent(appContext, JustAudioService::class.java)
            appContext.startService(serviceIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start JustAudioService", e)
        }

        _activeTrack.value = track
        
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.category)
            .setDisplayTitle(track.title)
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(track.audioUrl)
            .setMediaMetadata(mediaMetadata)
            .build()

        player.setMediaItem(mediaItem)
        player.setPlaybackSpeed(_playbackSpeed.value)
        player.repeatMode = if (_isRepeatEnabled.value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        player.shuffleModeEnabled = _isShuffleEnabled.value
        
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Int) {
        exoPlayer?.seekTo(positionMs.toLong())
        _currentPosition.value = positionMs
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer?.setPlaybackSpeed(speed)
    }

    fun toggleRepeat() {
        _isRepeatEnabled.value = !_isRepeatEnabled.value
        exoPlayer?.repeatMode = if (_isRepeatEnabled.value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
        exoPlayer?.shuffleModeEnabled = _isShuffleEnabled.value
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimerRemaining.value = -1
            return
        }
        _sleepTimerRemaining.value = minutes * 60
        sleepTimerJob = scope.launch {
            while (_sleepTimerRemaining.value > 0) {
                delay(1000)
                _sleepTimerRemaining.value -= 1
            }
            _sleepTimerRemaining.value = -1
            exoPlayer?.pause()
        }
    }

    fun next(context: Context) {
        val current = _activeTrack.value ?: return
        if (playlist.isEmpty()) return

        val currentIndex = playlist.indexOfFirst { it.id == current.id }
        if (currentIndex == -1) return

        val nextIndex = if (_isShuffleEnabled.value) {
            playlist.indices.random()
        } else {
            (currentIndex + 1) % playlist.size
        }

        playTrack(context, playlist[nextIndex])
    }

    fun previous(context: Context) {
        val current = _activeTrack.value ?: return
        if (playlist.isEmpty()) return

        val currentIndex = playlist.indexOfFirst { it.id == current.id }
        if (currentIndex == -1) return

        val prevIndex = if (currentIndex - 1 < 0) playlist.size - 1 else currentIndex - 1
        playTrack(context, playlist[prevIndex])
    }

    private fun handleCompletion(context: Context) {
        if (_isRepeatEnabled.value) {
            val track = _activeTrack.value
            if (track != null) {
                playTrack(context, track)
            }
        } else {
            next(context)
        }
    }

    private fun startProgressTracker() {
        updateJob?.cancel()
        updateJob = scope.launch {
            while (true) {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPosition.value = player.currentPosition.toInt()
                        _duration.value = player.duration.toInt()
                    }
                }
                delay(1000)
            }
        }
    }

    fun release() {
        updateJob?.cancel()
        sleepTimerJob?.cancel()
        _activeTrack.value = null
        _isPlaying.value = false
        _currentPosition.value = 0
        _duration.value = 0
        
        try {
            mediaSession?.release()
            exoPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing ExoPlayer/MediaSession", e)
        }
        mediaSession = null
        exoPlayer = null
    }
}
