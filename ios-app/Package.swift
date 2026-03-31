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
            path: "OpenAiPay/Core",
            exclude: [
                "APIClient.swift",
                "AppState.swift",
                "AppTheme.swift",
                "ScreenshotStatusMaskView.swift"
            ],
            sources: [
                "AuthStore.swift",
                "Models.swift"
            ]
        ),
        .testTarget(
            name: "OpenAiPayCoreLogicTests",
            dependencies: ["OpenAiPayCoreLogic"],
            path: "Tests/OpenAiPayCoreLogicTests"
        )
    ]
)
