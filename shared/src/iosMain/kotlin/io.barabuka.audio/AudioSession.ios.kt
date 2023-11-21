package io.barabuka.audio

import co.touchlab.kermit.Logger
import io.barabuka.util.LoggerObj
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import platform.AVFAudio.AVAudioEngine


actual class AudioSession actual constructor(
    private val delegate: AudioSessionTransport
) {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val logger = LoggerObj("SESSION IOS", true)

    actual fun init() {
        val ss = AVAudioEngine()
        delegate.init()

        logger.d { "INIT $ss" }
    }

    actual fun release() {
    }

    actual fun startSpeech() {
    }

    actual fun stopSpeech() {
    }

}
