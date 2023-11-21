package io.barabuka

import co.touchlab.kermit.Logger
import io.barabuka.util.unsafeLazy
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json


val httpClient by unsafeLazy {
    val json = Json {
        prettyPrint = true
    }

    HttpClient {
        install(WebSockets)
        install(ContentNegotiation) {
            json(json)
        }
    }
}

class Networker {
    private var wsSession = MutableStateFlow<DefaultClientWebSocketSession?>(null)

    val isConnected = wsSession.map { it != null }

    suspend fun connectToWS(host: String, onMessageReceived: (msg: String) -> Unit) {
        try {
            httpClient.webSocket(method = HttpMethod.Get, host = host, port = 8080, path = "/chat") {
                wsSession.value = this
                while (true) {
                    val frame = incoming.receive() as? Frame.Text
                    val message = frame?.readText()
                    if (message != null) {
                        println(message)
                        onMessageReceived(message)
                    }
                }
            }
        } catch (th: Throwable) {
            Logger.e(th) { "WS connection error." }
        }
        wsSession.value = null
    }

    suspend fun sendMessage(msg: String) {
        wsSession.value?.send(Frame.Text(msg))
    }
}
