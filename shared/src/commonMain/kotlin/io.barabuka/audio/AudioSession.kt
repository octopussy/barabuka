package io.barabuka.audio

import kotlinx.coroutines.flow.StateFlow

expect class AudioSession {
    val isConnected: StateFlow<Boolean>

    fun setAudioDataReceiver(receiver: AudioDataReceiver)

    fun init()
    fun release()
    fun startSpeech()
    fun stopSpeech()
}
