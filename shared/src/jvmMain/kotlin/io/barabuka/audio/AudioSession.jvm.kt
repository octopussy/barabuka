package io.barabuka.audio

import kotlinx.coroutines.flow.StateFlow

internal actual class AudioSessionPlatform actual constructor(receiver: AudioDataReceiver?, sender: AudioDataSender?) {
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
}
