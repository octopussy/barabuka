import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.barabuka.AudioSessionTransportWS
import io.barabuka.audio.AudioSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

const val HOST = "192.168.1.13"
//const val HOST = "192.168.0.105"
const val PORT = 8080
const val PATH = "/channel"

@Composable
fun BarabukaAppContent() {
    // val networker = remember { Networker() }

    val ioScope = remember { CoroutineScope(Dispatchers.IO) }
    val transport = remember {
        AudioSessionTransportWS(
            scope = ioScope,
            host = HOST,
            port = PORT,
            path = PATH
        )
    }
    val audioSession = remember { AudioSession(transport) }

    MaterialTheme {
        var inputMessage by remember { mutableStateOf("") }
        var chatMessages by remember { mutableStateOf("") }
        val isConnected by transport.isConnected.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        /*
                LaunchedEffect(Unit) {
                    while (isActive) {
                        networker.connectToWS(host = "192.168.1.13") { receivedMsg ->
                            chatMessages += "$receivedMsg\n"
                        }
                        delay(3000)
                    }
                }
        */

        LaunchedEffect(Unit) {
            audioSession.init()
        }

        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(16.dp)
                        .background(if (isConnected) Color.Green else Color.DarkGray)
                )

                OutlinedTextField(
                    enabled = isConnected,
                    modifier = Modifier.weight(1f),
                    value = inputMessage, onValueChange = { inputMessage = it },
                )

                Button(
                    enabled = isConnected && inputMessage.isNotBlank(),
                    onClick = {
                        /*coroutineScope.launch {
                            runCatching { networker.sendMessage(inputMessage) }
                                .onSuccess {
                                    inputMessage = ""
                                }
                        }*/
                    }
                ) {
                    Text("SEND")
                }
            }

            var btnColor by remember { mutableStateOf(Color.Gray) }

            fun setPressed(value: Boolean) {
                btnColor = if (value) Color.Blue else Color.Gray
                if (value) {
                    audioSession.startSpeech()
                } else {
                    audioSession.stopSpeech()
                }
            }

            Box(modifier = Modifier
                .size(120.dp)
                .background(btnColor)
                .align(Alignment.CenterHorizontally)
                .pointerInput(Unit){
                    awaitEachGesture {
                        awaitFirstDown().also { it.consume() }
                        setPressed(true)
                        waitForUpOrCancellation()?.consume()
                        setPressed(false)
                    }
                }
            )

            Text(text = chatMessages)
        }
    }
}
