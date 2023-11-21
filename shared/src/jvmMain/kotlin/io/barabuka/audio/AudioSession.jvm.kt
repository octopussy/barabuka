package io.barabuka.audio

import kotlinx.coroutines.flow.StateFlow

actual class AudioSession {
    actual fun init() {
    }

    actual fun release() {
    }

    actual fun startSpeech() {
    }

    actual fun stopSpeech() {
    }

    actual val isConnected: StateFlow<Boolean>
        get() = TODO("Not yet implemented")

    actual fun setAudioDataReceiver(receiver: AudioDataReceiver) {
    }

}
