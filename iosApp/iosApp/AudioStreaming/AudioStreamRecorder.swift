import AVFoundation

class AudioStreamRecorder {
    private let audioEngine: AVAudioEngine
    
    private var audioConverter: AVAudioConverter?
    private var outputFormat: AVAudioFormat
    private var inputFormat: AVAudioFormat
    private var conversionBuffer: AVAudioPCMBuffer?
    
    init(audioEngine: AVAudioEngine) {
        self.audioEngine = audioEngine
        // Configure the audio session
        let audioSession = AVAudioSession.sharedInstance()
        try? audioSession.setCategory(.record)
        try? audioSession.setActive(true)

        // Define the input and output formats
        inputFormat = audioEngine.inputNode.outputFormat(forBus: 0)
        
        var outDesc = AudioStreamBasicDescription(mSampleRate: 48000,
                                                   mFormatID: kAudioFormatMPEG4AAC,
                                                   mFormatFlags: 0,
                                                   mBytesPerPacket: 0,
                                                   mFramesPerPacket: 0,
                                                   mBytesPerFrame: 0,
                                                   mChannelsPerFrame: 1,
                                                   mBitsPerChannel: 0,
                                                   mReserved: 0)

        outputFormat = AVAudioFormat(streamDescription: &outDesc)!
        
        //outputFormat = AVAudioFormat(commonFormat: ., sampleRate: 44100, channels: 1, interleaved: false)!

        // Initialize the audio converter for AAC conversion
        audioConverter = AVAudioConverter(from: inputFormat, to: outputFormat)

        // Prepare the audio engine
        audioEngine.prepare()
    }

    func startRecording() {
        let inputNode = audioEngine.inputNode

        // Install a tap on the input node to capture audio data
        inputNode.installTap(onBus: 0, bufferSize: 1024, format: inputFormat) { [weak self] (buffer, when) in
            self?.processAudio(buffer: buffer)
        }

        // Start the audio engine
        do {
            try audioEngine.start()
        } catch {
            print("Error starting audio engine: \(error)")
        }
    }

    private func processAudio(buffer: AVAudioPCMBuffer) {
        let inputBlock: AVAudioConverterInputBlock = { inNumPackets, outStatus in
            outStatus.pointee = .haveData
            return buffer
        }

//        let outputBuffer = AVAudioPCMBuffer(pcmFormat: outputFormat, frameCapacity: AVAudioFrameCount(outputFormat.sampleRate / 10))
        
        let outputBuffer = AVAudioCompressedBuffer(format: outputFormat,
                                                    packetCapacity: 8,
                                                    maximumPacketSize: audioConverter!.maximumOutputPacketSize)


        var error : NSError?
        let status = audioConverter?.convert(to: outputBuffer, error: &error, withInputFrom: inputBlock)
        //print("STATUS \(status?.rawValue) \(error)")
    
        
        let data = Data(bytes: outputBuffer.data, count: Int(outputBuffer.byteLength))
        
        print("OUT \(data.count)")
        
        
        /*if let data = outputBuffer.data {
            let dataBuffer = Data(bytes: data.pointee, count: Int(outputBuffer!.frameCapacity * outputFormat.streamDescription.pointee.mBytesPerFrame))
            // Handle the converted Data chunk (dataBuffer)
            print("RECORDER CHUNK \(dataBuffer.count)")
        }*/
        
        /*do {
            try audioConverter?.convert(to: outputBuffer!, error: nil, withInputFrom: inputBlock)
            if let data = outputBuffer.int16ChannelData {
                let dataBuffer = Data(bytes: data.pointee, count: Int(outputBuffer!.frameCapacity * outputFormat.streamDescription.pointee.mBytesPerFrame))
                // Handle the converted Data chunk (dataBuffer)
                print("RECORDER CHUNK \(dataBuffer.count)")
            }
        } catch {
            print("Error converting audio: \(error)")
        }*/
    }

    func stopRecording() {
        audioEngine.inputNode.removeTap(onBus: 0)
        audioEngine.stop()
    }
}
