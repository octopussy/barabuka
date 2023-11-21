//
//  SrtAudioStreamSource.swift
//  PTT
//
//  Created by Developer on 19.11.2023.
//

import Foundation
import AudioToolbox
import ComposeApp

class SrtAudioStreamSource: CoreAudioStreamSource, SharedAudioDataReceiver {
    var forcedSourceAudioFormat: AudioStreamBasicDescription? = AudioStreamBasicDescription(
        mSampleRate: Float64(48000),
        mFormatID: kAudioFormatMPEG4AAC,
        mFormatFlags: 0,
        mBytesPerPacket: 0,
        mFramesPerPacket: 0,
        mBytesPerFrame: 0,
        mChannelsPerFrame: 1,
        mBitsPerChannel: 0,
        mReserved: 0
    )
    
    weak var delegate: AudioStreamSourceDelegate? = nil
    
    var audioFileHint: AudioFileTypeID = kAudioFileAAC_ADTSType
    
    var position: Int = 0
    var length: Int = 0
    
    var underlyingQueue: DispatchQueue
    var url: URL
    
    init(url: URL, underlyingQueue: DispatchQueue) {
        self.url = url
        self.underlyingQueue = underlyingQueue
    }
    
    func close() {
        UberLog.debug("[SRT] close", category: UberLog.Category.generic)
    }
    
    func suspend() {
        UberLog.debug("[SRT] suspend", category: UberLog.Category.generic)
    }
    
    func resume() {
        UberLog.debug("[SRT] resume", category: UberLog.Category.generic)
    }
    
    func seek(at offset: Int) {
        UberLog.debug("[SRT] seek", category: UberLog.Category.generic)
    }

    func storePacket(data: Data) {
        print("[SRT] store packet \(data.count)")
        delegate?.dataAvailable(source: self, data: data)
    }
    
    func onReceivePacket(data: Data) {
        print("[!!!!!] store packet \(data.count)")
        delegate?.dataAvailable(source: self, data: data)
    }
}
