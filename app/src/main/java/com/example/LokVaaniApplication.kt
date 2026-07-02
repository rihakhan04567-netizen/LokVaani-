package com.example

import android.app.Application
import com.example.data.network.FirebaseWrapper

/**
 * Main Application class for LokVaani.
 * Initializing global configurations, logging, and services like Firebase securely on start.
 */
class LokVaaniApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize the safe FirebaseWrapper
        // By default emulators are false, but can be set to true for sandbox/local emulator testing.
        FirebaseWrapper.initialize(this, useEmulators = false)

        // Initialize Riverpod ProviderContainer
        com.example.riverpod.ProviderContainer.initialize(this)
    }
}
