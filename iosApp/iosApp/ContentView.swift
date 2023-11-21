import UIKit
import SwiftUI
import ComposeApp
import SharedLib
import Combine


struct ComposeView: UIViewControllerRepresentable {
    let player = AudioPlayer()
    
    init() {
        player.start()
    }
    
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(audioDataReceiver: player.getAudioDataReceiver())
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}


