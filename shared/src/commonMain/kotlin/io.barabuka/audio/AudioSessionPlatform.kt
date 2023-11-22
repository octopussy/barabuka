package io.barabuka.audio

import kotlinx.coroutines.flow.StateFlow

internal expect class AudioSessionPlatform(
    receiver: AudioDataReceiver?,
    sender: AudioDataSender?
) {
    val isConnected: StateFlow<Boolean>

    fun init()
    fun release()
    fun startSpeech()
    fun stopSpeech()
}
