package io.barabuka.audio

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.barabuka.createAudioSessionTransport
import io.barabuka.util.LoggerObj
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


actual class AudioSession {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val transport = createAudioSessionTransport()

    @NativeCoroutines
    actual val isConnected: StateFlow<Boolean>
        get() = MutableStateFlow(false)

    private val logger = LoggerObj("SESSION IOS", true)

    actual fun init() {
        //val ss = AVAudioEngine()
        transport.init()
        logger.d { "INIT" }

        scope.launch {
            for (data in transport.receiveChannel) {
                logger.d { "RECEIVED ${data.first.size}" }
            }
        }
    }

    actual fun release() {
        logger.d { "RELEASE" }
    }

    actual fun startSpeech() {
        logger.d { "START SPEECH" }
    }

    actual fun stopSpeech() {
        logger.d { "STOP SPEECH" }
    }
}
