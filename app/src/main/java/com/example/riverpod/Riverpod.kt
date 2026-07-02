package com.example.riverpod

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A lightweight, production-ready, pure Kotlin implementation of the Riverpod Provider Architecture.
 * Provides [Provider], [StateNotifierProvider], and [StateNotifier] with Jetpack Compose integration.
 */
abstract class StateNotifier<S>(initialState: S) {
    private val _state = MutableStateFlow(initialState)
    val stateFlow: StateFlow<S> = _state.asStateFlow()

    var state: S
        get() = _state.value
        set(value) {
            _state.value = value
        }
}

class Provider<T>(val create: (ProviderContainer) -> T)

class StateNotifierProvider<Notifier : StateNotifier<S>, S>(val create: (ProviderContainer) -> Notifier)

object ProviderContainer {
    private val instances = mutableMapOf<Any, Any>()
    
    @Volatile
    var context: android.content.Context? = null
        private set

    fun initialize(context: android.content.Context) {
        if (this.context == null) {
            synchronized(this) {
                if (this.context == null) {
                    this.context = context.applicationContext
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(provider: Provider<T>): T {
        return synchronized(instances) {
            instances.getOrPut(provider) { provider.create(this) as Any } as T
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <Notifier : StateNotifier<S>, S> get(provider: StateNotifierProvider<Notifier, S>): Notifier {
        return synchronized(instances) {
            instances.getOrPut(provider) { provider.create(this) as Any } as Notifier
        }
    }
}

@Composable
fun <T> Provider<T>.read(): T {
    return ProviderContainer.get(this)
}

@Composable
fun <Notifier : StateNotifier<S>, S> StateNotifierProvider<Notifier, S>.watch(): S {
    val notifier = ProviderContainer.get(this)
    return notifier.stateFlow.collectAsState().value
}

@Composable
fun <Notifier : StateNotifier<S>, S> StateNotifierProvider<Notifier, S>.notifier(): Notifier {
    return ProviderContainer.get(this)
}
