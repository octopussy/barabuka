package io.barabuka.audio

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class AudioSessionTransportParams(
    val sessionId: String,
    val channelId: String
)
interface AudioSessionTransport {
    val isConnected: StateFlow<Boolean>
    fun init()
    fun release()
    fun onSpeechStarted()
    fun onSpeechFinished()
    fun writeToSink(data: ByteArray, offset: Int, size: Int)

    val receiveChannel: ReceiveChannel<Pair<ByteArray, Long>>

    @NativeCoroutines
    fun receiveChannelFlow(): Flow<Pair<ByteArray, Long>>

    @NativeCoroutines
    val fff: StateFlow<String>

    val fff2: StateFlow<String>
}

expect class AudioSession(delegate: AudioSessionTransport) {
    fun init()
    fun release()
    fun startSpeech()
    fun stopSpeech()
}
