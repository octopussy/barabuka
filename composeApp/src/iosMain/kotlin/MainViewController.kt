import androidx.compose.ui.window.ComposeUIViewController
import io.barabuka.AudioSessionTransportWS
import io.barabuka.audio.AudioSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

fun MainViewController() = ComposeUIViewController { BarabukaAppContent() }

fun createAudioTransport() = AudioSessionTransportWS(
    scope = CoroutineScope(Dispatchers.IO),
    host = HOST,
    port = PORT,
    path = PATH
)
