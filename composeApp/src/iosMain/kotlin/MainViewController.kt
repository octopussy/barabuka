import androidx.compose.ui.window.ComposeUIViewController
import io.barabuka.AudioSessionTransportWS
import io.barabuka.audio.AudioSession
import io.barabuka.audio.AudioSessionTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

fun MainViewController() = ComposeUIViewController { BarabukaAppContent(AudioSession()) }
