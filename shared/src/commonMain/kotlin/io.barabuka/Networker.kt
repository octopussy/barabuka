package io.barabuka

import co.touchlab.kermit.Logger
import io.barabuka.util.unsafeLazy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TestJsonItem(
    @SerialName("name") val name: String,
    @SerialName("language") val language: String,
    @SerialName("id") val id: String,
    @SerialName("bio") val bio: String,
    @SerialName("version") val version: Double,
)

class Networker {

    private val client by unsafeLazy {
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

    private var wsSession = MutableStateFlow<DefaultClientWebSocketSession?>(null)

    val isConnected = wsSession.map { it != null }

    suspend fun connectToWS(host: String, onMessageReceived: (msg: String) -> Unit) {
        try {
            client.webSocket(method = HttpMethod.Get, host = host, port = 8080, path = "/chat") {
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

    suspend fun testRequest(): List<TestJsonItem> {
        return client.get("https://microsoftedge.github.io/Demos/json-dummy-data/64KB.json")
            .body<List<TestJsonItem>>()
    }
}
