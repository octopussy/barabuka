import UIKit
import AVFAudio
import SwiftUI
import ComposeApp
import Combine
import AVFAudio

class Sender : SharedAudioDataSender {
    func sendAudioData(data: Data) {
        
    }
}

struct ComposeView: UIViewControllerRepresentable {
    let engine: AVAudioEngine
    let player: AudioPlayer
    //let recorder: AudioStreamRecorder
    
    init() {
        engine = AVAudioEngine()
        player = AudioPlayer(engine: engine)
      //  recorder = AudioStreamRecorder(audioEngine: engine)
        
        player.start()
        //engine.prepare()
        //recorder.startRecording()
    }
    
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(receiver: player.getAudioDataReceiver(), sender: Sender())
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}


