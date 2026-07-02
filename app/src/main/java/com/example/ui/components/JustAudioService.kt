package com.example.ui.components

import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

@UnstableApi
class JustAudioService : MediaSessionService() {

    override fun onCreate() {
        super.onCreate()
        Log.i("JustAudioService", "Initializing JustAudioService...")
        // Ensure the MediaSession is loaded from AudioPlayerManager
        AudioPlayerManager.getMediaSession(this)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return AudioPlayerManager.getMediaSession(this)
    }

    override fun onDestroy() {
        Log.i("JustAudioService", "Destroying JustAudioService...")
        super.onDestroy()
    }
}
