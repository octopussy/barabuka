//
//  Created by Dimitrios Chatzieleftheriou on 11/11/2020.
//  Copyright © 2020 Decimal. All rights reserved.
//

import AVFoundation
import ComposeApp

protocol AudioEntryProviding {
    func provideAudioEntry(url: URL, headers: [String: String]) -> AudioEntry
    func provideAudioEntry(url: URL) -> AudioEntry
}

final class AudioEntryProvider: AudioEntryProviding {
    private let networkingClient: NetworkingClient
    private let underlyingQueue: DispatchQueue
    private let outputAudioFormat: AVAudioFormat
    
    private let aaa: SrtAudioStreamSource

    init(networkingClient: NetworkingClient,
         underlyingQueue: DispatchQueue,
         outputAudioFormat: AVAudioFormat)
    {
        self.networkingClient = networkingClient
        self.underlyingQueue = underlyingQueue
        self.outputAudioFormat = outputAudioFormat
        self.aaa = SrtAudioStreamSource(url: URL.init(string: "http://google.com")!, underlyingQueue: underlyingQueue)
    }
    
    func getAudioReceiver() -> SharedAudioDataReceiver {
        return aaa
    }

    func provideAudioEntry(url: URL, headers: [String: String]) -> AudioEntry {
        let source = self.source(for: url, headers: headers)
        return AudioEntry(source: source,
                          entryId: AudioEntryId(id: url.absoluteString),
                          outputAudioFormat: outputAudioFormat)
    }

    func provideAudioEntry(url: URL) -> AudioEntry {
        provideAudioEntry(url: url, headers: [:])
    }

    func provideAudioSource(url: URL, headers: [String: String]) -> AudioStreamSource {
        RemoteAudioSource(networking: networkingClient,
                          url: url,
                          underlyingQueue: underlyingQueue,
                          httpHeaders: headers)
    }

    func provideFileAudioSource(url: URL) -> CoreAudioStreamSource {
        FileAudioSource(url: url, underlyingQueue: underlyingQueue)
    }
    
    func provideSrtAudioSource(url: URL) -> CoreAudioStreamSource {
        aaa
    }

    func source(for url: URL, headers: [String: String]) -> CoreAudioStreamSource {
        return provideSrtAudioSource(url: url)
    }
}
