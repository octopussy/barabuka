import UIKit
import SwiftUI
import ComposeApp
import SharedLib
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesAsync
import KMPNativeCoroutinesCombine

/*extension SharedAudioSessionTransport {
    func getRandomLetters() -> NativeSuspend<String, Error, KotlinUnit> {
        AudioSessionTransportWS.getRandomLetters(self)
    }
}*/

/*extension AudioSessionTransport {
    func testFlow() -> NativeFlow<String, Error, Void> {
        AudioSessionTransportNativeKt.testFlow(self)
    }
}*/

struct ComposeView: UIViewControllerRepresentable {
    
   // let audioTransport: AudioSessionTransport
    
  //  let publisher: AnyPublisher<String, Error>
    
    init() {
     //   audioTransport = AudioSessionTransportWSKt.createAudioSessionTransport()
     //   print("\(audioTransport)")
        
      //  audioTransport.doInit()
        
      //  publisher = createPublisher(for: AudioSessionTransportNativeKt.testFlow(audioTransport))
      //  let future = createFuture(for: AudioSessionTransportNativeKt.testFlow(audioTransport))

        // Now use this future as you would any other
        /*  let cancellable = future.sink { completion in
            print("Received completion: \(completion)")
        } receiveValue: { value in
            print("Received value: \(value)")
        } */
        
        // Now use this publisher as you would any other
       /* let cancellable = publisher.sink { completion in
            print("Received completion: \(completion)")
        } receiveValue: { value in
            print("Received value: \(value)")
        } */

        // To cancel the flow (collection) just cancel the publisher
        //cancellable.cancel()
    
    
        //Shared().getRandomLettersFlow()
        
       // SharedAudioSessionTransportWS.
        
        //let ss = Barabuka
        // Create an AnyPublisher for your flow
       // let publisher = createPublisher(for: audioTransport.fff)
        
        //SharedAudioSessionTransportWS.onSpeechFinished(SharedAudioSessionTransportWS)

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



