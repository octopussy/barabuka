import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import io.barabuka.Networker
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun BarabukaAppContent() {
    val networker = remember { Networker() }

    MaterialTheme {
        var inputMessage by remember { mutableStateOf("") }
        var chatMessages by remember { mutableStateOf("") }
        val isConnected by networker.isConnected.collectAsState(false)
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            while (isActive) {
                networker.connectToWS(host = "192.168.1.13") { receivedMsg ->
                    chatMessages += "$receivedMsg\n"
                }
                delay(3000)
            }
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
                        coroutineScope.launch {
                            runCatching { networker.sendMessage(inputMessage) }
                                .onSuccess {
                                    inputMessage = ""
                                }
                        }
                    }
                ) {
                    Text("SEND")
                }
            }

            Text(text = chatMessages)
        }
    }
}
