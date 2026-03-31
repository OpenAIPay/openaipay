import SwiftUI
import UIKit

@main
struct AiPayApp: App {
    @Environment(\.scenePhase) private var scenePhase
    @StateObject private var appState = AppState()

    init() {
        let brandColor = AppTheme.palette.brandPrimaryUIColor
        UINavigationBar.appearance().tintColor = brandColor
        UITabBar.appearance().tintColor = brandColor
        UITextField.appearance().tintColor = brandColor
        UITextView.appearance().tintColor = brandColor
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appState)
                .tint(AppTheme.palette.brandPrimary)
                .onAppear {
                    appState.handleAppLaunch()
                }
                .onChange(of: scenePhase) { _, newPhase in
                    if newPhase == .active {
                        appState.handleSceneDidBecomeActive()
                    } else if newPhase == .inactive {
                        appState.handleSceneWillResignActive()
                    } else if newPhase == .background {
                        appState.handleSceneDidEnterBackground()
                    }
                }
        }
    }
}
