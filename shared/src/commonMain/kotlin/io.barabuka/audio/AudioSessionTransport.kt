package io.barabuka.audio

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.barabuka.AudioSessionTransportWS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


interface AudioSessionTransport {
    @NativeCoroutines
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
    val testFlow: Flow<String>
}
