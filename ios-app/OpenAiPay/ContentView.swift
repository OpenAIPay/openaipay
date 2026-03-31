import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.openURL) private var openURL

    private var suppressUpdatePromptForDebugLaunch: Bool {
        ProcessInfo.processInfo.arguments.contains("-OpenAiPayStartPage")
    }

    private var forceUpdateCoverPresented: Binding<Bool> {
        Binding(
            get: {
                appState.forceUpdatePresentation != nil
            },
            set: { _ in
                // 强制更新弹窗不可主动关闭，展示状态由服务端版本检查结果驱动。
            }
        )
    }

    var body: some View {
        ZStack {
            if appState.isAuthenticated {
                HomeView()
            } else {
                LoginView()
            }

            if !suppressUpdatePromptForDebugLaunch,
               appState.forceUpdatePresentation == nil,
               let optionalUpdatePresentation = appState.optionalUpdatePresentation {
                UpdatePromptOverlay(
                    presentation: optionalUpdatePresentation,
                    title: "发现新版本",
                    onOpenUpdate: {
                        openAppStoreIfPossible(using: optionalUpdatePresentation)
                        appState.dismissOptionalUpdatePresentation()
                    },
                    onRetry: {
                        appState.recheckVersionNow()
                    },
                    onDismiss: {
                        appState.dismissOptionalUpdatePresentation()
                    }
                )
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .task {
            appState.ensureInitialLaunchHandled()
        }
        .task(id: appState.isAuthenticated) {
            let pageName = appState.isAuthenticated ? "/page/home" : "/page/login"
            appState.recordPageVisit(pageName)
        }
        .fullScreenCover(isPresented: forceUpdateCoverPresented) {
            if let forceUpdatePresentation = appState.forceUpdatePresentation {
                UpdatePromptOverlay(
                    presentation: forceUpdatePresentation,
                    title: "需要更新后继续使用",
                    onOpenUpdate: {
                        openAppStoreIfPossible(using: forceUpdatePresentation)
                    },
                    onRetry: nil,
                    onDismiss: nil
                )
                .interactiveDismissDisabled(true)
            } else {
                Color.clear
                    .ignoresSafeArea()
            }
        }
    }

    private func openAppStoreIfPossible(using presentation: AppUpdatePresentation) {
        guard let appStoreURL = presentation.appStoreURL else {
            appState.recheckVersionNow()
            return
        }
        openURL(appStoreURL)
    }
}

private struct UpdatePromptOverlay: View {
    let presentation: AppUpdatePresentation
    let title: String
    let onOpenUpdate: () -> Void
    let onRetry: (() -> Void)?
    let onDismiss: (() -> Void)?

    var body: some View {
        ZStack {
            Color.black.opacity(0.36)
                .ignoresSafeArea()

            VStack(alignment: .leading, spacing: 16) {
                Text(title)
                    .font(.headline)
                    .foregroundStyle(.primary)

                Text("版本 \(presentation.versionLabel)")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                Text(presentation.descriptionText)
                    .font(.body)
                    .foregroundStyle(.primary)
                    .fixedSize(horizontal: false, vertical: true)

                VStack(spacing: 12) {
                    Button(action: onOpenUpdate) {
                        Text("去更新")
                            .font(.body.weight(.semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(AppTheme.palette.brandPrimary)
                            .foregroundStyle(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                    }
                    .buttonStyle(.plain)

                    if let onRetry {
                        Button(action: onRetry) {
                            Text("重新检查")
                                .font(.body.weight(.medium))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .background(Color(.secondarySystemBackground))
                                .foregroundStyle(.primary)
                                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                        }
                        .buttonStyle(.plain)
                    }

                    if let onDismiss {
                        Button(action: onDismiss) {
                            Text("稍后")
                                .font(.body.weight(.medium))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .background(Color.clear)
                                .foregroundStyle(.secondary)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .padding(24)
            .frame(maxWidth: 320)
            .background(Color(.systemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
            .shadow(color: .black.opacity(0.14), radius: 16, y: 8)
            .padding(24)
        }
    }
}

#Preview {
    ContentView()
        .environmentObject(AppState())
}
