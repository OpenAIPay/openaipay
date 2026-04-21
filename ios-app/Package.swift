// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "OpenAiPayCoreLogic",
    platforms: [
        .macOS(.v14)
    ],
    products: [
        .library(
            name: "OpenAiPayCoreLogic",
            targets: ["OpenAiPayCoreLogic"]
        )
    ],
    targets: [
        .target(
            name: "OpenAiPayCoreLogic",
            path: "OpenAiPay",
            exclude: [
                "Assets.xcassets",
                "Config",
                "ContentView.swift",
                "Core/APIClient.swift",
                "Core/AppState.swift",
                "Core/AppTheme.swift",
                "Core/ScreenshotStatusMaskView.swift",
                "Features/Auth",
                "Features/Home",
                "Features/ShortVideo/Components",
                "Features/ShortVideo/ViewModels",
                "Features/ShortVideo/Views",
                "Info.plist",
                "LaunchScreen.storyboard",
                "OpenAiPayApp.swift"
            ],
            sources: [
                "Core/AuthStore.swift",
                "Core/Models.swift",
                "Core/ShortVideoState.swift",
                "Features/ShortVideo/Models/ShortVideoModels.swift",
                "Features/ShortVideo/Models/ShortVideoCommentModels.swift"
            ]
        ),
        .testTarget(
            name: "OpenAiPayCoreLogicTests",
            dependencies: ["OpenAiPayCoreLogic"],
            path: "Tests/OpenAiPayCoreLogicTests"
        )
    ]
)
