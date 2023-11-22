package io.barabuka.audio

import PlatformBytes

interface AudioDataSender {
    fun sendAudioData(data: PlatformBytes)
}
