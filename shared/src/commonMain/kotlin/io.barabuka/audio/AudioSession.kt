package io.barabuka.audio

import kotlinx.coroutines.flow.StateFlow

expect class AudioSession {
    val isConnected: StateFlow<Boolean>

    fun init()
    fun release()
    fun startSpeech()
    fun stopSpeech()
}
