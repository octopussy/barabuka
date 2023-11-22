import androidx.compose.ui.window.ComposeUIViewController
import io.barabuka.audio.AudioDataReceiver
import io.barabuka.audio.AudioDataSender
import io.barabuka.audio.AudioSession

fun MainViewController(receiver: AudioDataReceiver, sender: AudioDataSender) = ComposeUIViewController {
    BarabukaAppContent(AudioSession(receiver, sender))
}
