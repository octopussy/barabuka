package io.barabuka.audio

import PlatformBytes

interface AudioDataReceiver {
    fun onReceivePacket(data: PlatformBytes)
}
