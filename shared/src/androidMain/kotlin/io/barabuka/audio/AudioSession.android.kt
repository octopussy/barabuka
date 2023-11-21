package io.barabuka.audio

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.*
import android.media.MediaCodec.BufferInfo
import android.os.Handler
import android.os.Looper
import co.touchlab.kermit.Logger
import io.barabuka.createAudioSessionTransport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Clock
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.ByteBuffer
import kotlin.coroutines.resume


private const val BASE_SAMPLE_RATE = 48000

const val PLAYER_SAMPLERATE = BASE_SAMPLE_RATE
const val PLAYER_CHANNELS: Int = AudioFormat.CHANNEL_OUT_MONO
const val PLAYER_AUDIO_ENCODING: Int = AudioFormat.ENCODING_PCM_16BIT

private const val RECORDER_SAMPLE_RATE = BASE_SAMPLE_RATE
private const val RECORDER_CHANNELS: Int = AudioFormat.CHANNEL_IN_MONO
private const val RECORDER_AUDIO_ENCODING: Int = AudioFormat.ENCODING_PCM_16BIT

private const val RECORD_BUFFER_SIZE = 8192

const val ENCODER_MAX_BUFFER_SIZE = RECORD_BUFFER_SIZE
const val ENCODER_SAMPLE_RATE = RECORDER_SAMPLE_RATE
const val ENCODER_PROFILE = AACProfiles.OMX_AUDIO_AACObjectLC

const val MIME_TYPE = "audio/mp4a-latm"
const val ENCODER_CHANNELS = 1
const val ENCODER_BITRATE = 128000

const val SAMPLES_PER_FRAME = 1024
const val FRAMES_PER_BUFFER = 24

val audioFormat = MediaFormat().apply {
    setString(MediaFormat.KEY_MIME, MIME_TYPE)
    setInteger(MediaFormat.KEY_AAC_PROFILE, ENCODER_PROFILE)
    setInteger(MediaFormat.KEY_SAMPLE_RATE, ENCODER_SAMPLE_RATE)
    setInteger(MediaFormat.KEY_CHANNEL_COUNT, ENCODER_CHANNELS)
    setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, ENCODER_MAX_BUFFER_SIZE)
    setInteger(MediaFormat.KEY_BIT_RATE, ENCODER_BITRATE)
    val bytes = byteArrayOf(0x11.toByte(), 0x90.toByte())
    val bb: ByteBuffer = ByteBuffer.wrap(bytes)
    setByteBuffer("csd-0", bb)
}

object AACProfiles {
    const val OMX_AUDIO_AACObjectLC = 2
    const val OMX_AUDIO_AACObjectHE = 5
    const val OMX_AUDIO_AACObjectELD = 39
}


//val kSampleRates = intArrayOf(8000, 11025, 22050, 44100, 48000)
//val kBitRates = intArrayOf(64000, 128000)

private const val MAX_ATTEMPTS_TO_CONNECT = 3

actual class AudioSession {
    private val scope = CoroutineScope(Dispatchers.Default)

    private var audioRecord: AudioRecord? = null
    private var encoder: MediaCodec? = null
    private var decoder: MediaCodec? = null

    @Volatile
    private var isEncoderReleased = false

    private var audioPlayer: AudioTrack? = null
    private var playerBufSize: Int = 0

    private var isRecording = false

    private val recordBuffer = ByteArray(RECORD_BUFFER_SIZE)

    private val delegate = createAudioSessionTransport()

    actual val isConnected: StateFlow<Boolean> = delegate.isConnected

    private val rawRecordedChunks =
        Channel<ByteArray>(UNLIMITED, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    actual fun init() {
        Logger.d { "AAAAA AudioSession init $this" }
        createAudioPlayer()
        createAudioRecordIfNeeded()
        createCodecs()

        delegate.init()
    }

    actual fun release() {
        Logger.d { "AAAAA AudioSession release $this" }
        delegate.release()
        scope.cancel()
        releaseAudioPlayer()
        releaseAudioRecord()
        releaseEncoder()
    }

    @Volatile
    private var isSpeechStarted = false

    actual fun startSpeech() {
        if (isSpeechStarted) return

        isSpeechStarted = true
        delegate.onSpeechStarted()
        createAudioRecordIfNeeded()
        audioRecord?.startRecording()
        Logger.d("[ speech start => recordingState = ${audioRecord?.recordingState} ]")
        audioRecordReadStart()
    }

    actual fun stopSpeech() {
        if (!isSpeechStarted) return
        isSpeechStarted = false

        audioRecordJob?.cancel()
        audioRecordJob = null
        delegate.onSpeechFinished()
        audioRecord?.stop()
        Logger.d("[ speech stop => recordingState = ${audioRecord?.recordingState} ]")
        isRecording = false
    }

    private fun audioRecordReadStart() {
        if (audioRecordJob?.isActive == true) return
        isRecording = true

        audioRecordJob = scope.launch {
            if (audioRecord == null) return@launch

            var readCount = 0
            var totalCount = 0
            while (isRecording && isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                readCount = audioRecord!!.read(recordBuffer, 0, RECORD_BUFFER_SIZE)
                totalCount += readCount
                val copiedBuffer = recordBuffer.copyOfRange(0, readCount)
                rawRecordedChunks.send(copiedBuffer)
            }
        }
    }


    private fun playPcmChunkData(array: ByteArray) {
        var bytesread = 0
        val input = ByteArrayInputStream(array)

        val buf = ByteArray(playerBufSize)
        while (bytesread < array.size) {
            val ret = input.read(buf)
            if (ret != -1) {
                audioPlayer?.write(buf, 0, ret)
                bytesread += ret
            }
        }
    }

    private fun createAudioPlayer() {
        playerBufSize = AudioTrack.getMinBufferSize(
            PLAYER_SAMPLERATE,
            PLAYER_CHANNELS,
            PLAYER_AUDIO_ENCODING
        )

        audioPlayer = AudioTrack(
            AudioManager.STREAM_MUSIC,
            PLAYER_SAMPLERATE,
            PLAYER_CHANNELS,
            PLAYER_AUDIO_ENCODING,
            playerBufSize,
            AudioTrack.MODE_STREAM
        )

        audioPlayer?.play()
    }

    private fun releaseAudioPlayer() {
        audioPlayer?.stop()
        audioPlayer?.release()
        audioPlayer = null
    }

    private fun createCodecs() {
        val suitedEncoders = getEncoderNamesForType(MIME_TYPE)
        val suitedDecoders = getDecoderNamesForType(MIME_TYPE)
        suitedEncoders.forEach {
            Logger.d("CODEC-> $it")
        }
        startEncoder(suitedEncoders.first(), audioFormat)
        startDecoder(suitedDecoders.first(), audioFormat)
    }

    private fun releaseEncoder() {
        isEncoderReleased = true
        encoder?.stop()
        encoder?.release()
        encoder = null
    }

    private fun startEncoder(componentName: String, format: MediaFormat) {
        Logger.d("startEncoder CODEC: $componentName")
        encoder = MediaCodec.createByCodecName(componentName)
        val encoder = encoder
        if (encoder == null) {
            Logger.e("[ startEncoder ] codec is null")
            return
        }

        encoder.setCallback(object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                if (isEncoderReleased) return
                val buffer = codec.getInputBuffer(index)
                    ?: throw IllegalStateException("input buffer is null")
                scope.launch {
                    try {
                        val data = rawRecordedChunks.receive()
                        buffer.clear()
                        buffer.put(data)
                        //Logger.d { "AAAAAAAAA encode data size=${data.size}" }
                        codec.queueInputBuffer(index, 0, data.size, 0L, 0)
                    } catch (th: Throwable) {
                        if (th !is CancellationException) {
                            Logger.e(th) { "ACHTUNG!!!" }
                        }
                    }

                }
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: BufferInfo
            ) {
                if (isEncoderReleased) return
                val buffer = codec.getOutputBuffer(index)
                    ?: throw IllegalStateException("output buffer is null")

                val ba = ByteArray(info.size)
                buffer.get(ba)

                delegate.writeToSink(ba, 0, info.size) // -> SRT
                writeDataToFile(ba, info) // -> file
                codec.releaseOutputBuffer(index, false)
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                Logger.e("[ startEncoder ] onError", e)
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                Logger.d("[ startEncoder ] onOutputFormatChanged $format")
            }
        })

        try {
            encoder.configure(
                format,
                null /* surface */,
                null /* crypto */,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )
            encoder.start()
        } catch (e: IllegalStateException) {
            Logger.e("codec '$componentName' failed configuration.", e)
        }
    }

    private val decoderInputBuffersLock = Any()
    private val decoderInputBuffers = Channel<Pair<Int, ByteBuffer>>(capacity = UNLIMITED)

    private fun startDecoder(componentName: String, format: MediaFormat) {
        //decoder = MediaCodec.createDecoderByType(MIME_TYPE)
        decoder = MediaCodec.createByCodecName(componentName)
        val decoder = decoder

        Logger.d("[ startDecoder ] Decoder started: $decoder")

        if (decoder == null) {
            Logger.e("[ startDecoder ] codec is null")
            return
        }
        decoder.setCallback(object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                scope.launch {
                    val buffer = codec.getInputBuffer(index) ?: return@launch
                    val (data, pts) = delegate.receiveChannel.receive()
                    //Logger.d { "AAAAAAAA BUFFER --> $index $codec data.size=${data.size} pts=$pts" }
                    buffer.clear()
                    buffer.put(data, 0, data.size)
                    codec.queueInputBuffer(index, 0, data.size, pts, 0)
                }
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: BufferInfo
            ) {
                val buffer = codec.getOutputBuffer(index)
                    ?: throw IllegalStateException("output buffer is null")

                // Logger.d { "AAAAAAAA BUFFER <--" }
                // val outputFormat = codec.getOutputFormat(index)
                //Logger.d("onOutputBufferAvailable info=${info} format=${outputFormat}")

                val ba = ByteArray(info.size)
                buffer.get(ba)
                playPcmChunkData(ba)
                codec.releaseOutputBuffer(index, false)
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                Logger.e("[ startDecoder ] onError", e)
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                Logger.d("[ startDecoder ] onOutputFormatChanged")
            }
        })

        try {
            decoder.configure(
                format,
                null /* surface */,
                null /* crypto */,
                0//MediaCodec.CONFIGURE_FLAG_ENCODE
            )
        } catch (e: IllegalStateException) {
            Logger.e("codec '$componentName' failed configuration.", e)
        }
        decoder.start()
    }

    private var audioRecordJob: Job? = null

    private fun writeDataToFile(ba: ByteArray, info: BufferInfo) {
        //fileWriter?.write(ba, info)
    }

    private fun createAudioRecordIfNeeded() {
        if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) return
        createAudioRecord()
    }

    @SuppressLint("MissingPermission")
    private fun createAudioRecord() {
        val minInternalBufferSize = AudioRecord.getMinBufferSize(
            RECORDER_SAMPLE_RATE,
            RECORDER_CHANNELS,
            RECORDER_AUDIO_ENCODING
        )

        val internalBufferSize = minInternalBufferSize * 4

        Logger.d(
            "minInternalBufferSize = " + minInternalBufferSize
                    + ", internalBufferSize = " + internalBufferSize
                    + ", myBufferSize = " + RECORD_BUFFER_SIZE
        )


        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT,
            RECORDER_SAMPLE_RATE,        // сэмплрейт
            RECORDER_CHANNELS,          // режим каналов моно/стерео
            RECORDER_AUDIO_ENCODING,    // формат аудио
            internalBufferSize          // размер буфера
        )
        Logger.d("[ initAudioRecord => ${audioRecord!!.state}] ")
    }

    private fun getCodecsList(kind: Int): List<String> {
        return MediaCodecList(kind).codecInfos.map { it.name }
    }

    private fun releaseAudioRecord() {
        audioRecord?.release()
        audioRecord = null
    }

    companion object {
        fun getEncoderNamesForType(mime: String): List<String> {
            return MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.asSequence()
                .filter { it.isEncoder }
                .filter { it.name.startsWith("OMX.") }
                .filter { it.supportedTypes.any { type -> type.contains(mime, true) } }
                .map { it.name }
                .toList()
        }

        fun getDecoderNamesForType(mime: String): List<String> {
            return MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.asSequence()
                .filter { !it.isEncoder }
                .filter { it.name.startsWith("OMX.") }
                .filter { it.supportedTypes.any { type -> type.contains(mime, true) } }
                .map { it.name }
                .toList()
        }
    }

    actual fun setAudioDataReceiver(receiver: AudioDataReceiver) {
    }
}
