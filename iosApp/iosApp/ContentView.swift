import UIKit
import SwiftUI
import ComposeApp
import Combine
import KMPNativeCoroutinesCombine

struct ComposeView: UIViewControllerRepresentable {
    
    
    let audioTransport: SharedAudioSessionTransport
    
    init() {
        audioTransport = MainViewControllerKt.createAudioTransport()
        print("\(audioTransport)")
        
        // Create an AnyPublisher for your flow
        //let publisher = createPublisher(for: audioTransport.receiveChannel)

        // Now use this publisher as you would any other
       /* let cancellable = publisher.sink { completion in
            print("Received completion: \(completion)")
        } receiveValue: { value in
            print("Received value: \(value)")
        }

        // To cancel the flow (collection) just cancel the publisher
        cancellable.cancel()
        
        */
    }
    
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}



