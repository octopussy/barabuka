package io.barabuka

import io.barabuka.audio.AudioSessionTransport
import io.barabuka.util.LoggerObj
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.util.date.getTimeMillis
import io.ktor.websocket.Frame
import io.ktor.websocket.send
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class AudioSessionTransportWS(
    private val scope: CoroutineScope,
    private val host: String,
    private val port: Int,
    private val path: String
) : AudioSessionTransport {

    private val logger = LoggerObj("SESSION", true)

    private var wsSession = MutableStateFlow<DefaultClientWebSocketSession?>(null)
    private var connectionJob: Job? = null

    override val isConnected = wsSession
        .map { it != null }
        .stateIn(scope, SharingStarted.Eagerly, false)

    override val receiveChannel = Channel<Pair<ByteArray, Long>>(
        capacity = Channel.UNLIMITED,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val sendChannel = Channel<ByteArray>(
        capacity = Channel.UNLIMITED,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun receiveChannelFlow(): Flow<Pair<ByteArray, Long>> {
        return receiveChannel.receiveAsFlow()
    }

    override val fff: StateFlow<String> = MutableStateFlow("123")

    override val fff2: StateFlow<String> = MutableStateFlow("123")

    override fun init() {
        openSocket()
    }

    override fun release() {
        connectionJob?.cancel()
        connectionJob = null
    }

    override fun onSpeechStarted() {
    }

    override fun onSpeechFinished() {
    }

    override fun writeToSink(data: ByteArray, offset: Int, size: Int) {
        val session = wsSession.value
        if (session == null) {
            logger.w { "Cannot write to channel: session is not open!" }
            return
        }

        logger.w { "Write data to channel: ${data.size}" }
        sendChannel.trySend(data)
    }

    private fun openSocket() {
        if (connectionJob?.isActive == true) {
            logger.w { "Incoming channel already open" }
            return
        }

        connectionJob = scope.launch {
            while (isActive) {
                try {
                    httpClient.webSocket(
                        method = HttpMethod.Get,
                        host = host,
                        port = port,
                        path = path
                    ) {
                        wsSession.value = this
                        handleSendChannel(this)
                        handleReceiveChannel()
                    }
                } catch (th: Throwable) {
                    logger.e(th) { "WS connection error. Reconnecting..." }
                } catch (th: CancellationException) {
                    return@launch
                }

                delay(5000)
            }
        }

        wsSession.value = null
    }

    private fun CoroutineScope.handleSendChannel(session: DefaultClientWebSocketSession) {
        launch {
            for (data in sendChannel) {
                session.send(data)
                logger.d { "[SESSION] --> ${data.size} bytes." }
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.handleReceiveChannel() {
        while (true) {
            val frame = incoming.receive() as? Frame.Binary
            val data = frame?.data
            if (data != null) {
                val pts = getTimeMillis() * 1000 + Clock.System.now().nanosecondsOfSecond
                logger.d { "[SESSION] <-- ${data.size} bytes " }
                receiveChannel.trySend(data to pts)
            }
        }
    }
}
