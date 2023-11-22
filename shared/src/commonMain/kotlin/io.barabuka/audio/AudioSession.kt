package io.barabuka.audio

class AudioSession(
    receiver: AudioDataReceiver? = null,
    sender: AudioDataSender? = null
) {

    private val delegate = AudioSessionPlatform(receiver, sender)

    val isConnected = delegate.isConnected

    fun init() {
        delegate.init()
    }

    fun startSpeech() {
        delegate.startSpeech()
    }

    fun stopSpeech() {
        delegate.stopSpeech()
    }


}
