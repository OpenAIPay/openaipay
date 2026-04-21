import SwiftUI
import AVKit
import UIKit

struct ShortVideoPlayerView: View {
    enum PresentationStyle: Equatable {
        case immersiveFill
        case fitWidthPreview
    }

    let playbackURL: URL?
    let posterURL: URL?
    let isActive: Bool
    let presentationStyle: PresentationStyle
    let onPlaybackAspectRatioResolved: ((CGFloat?) -> Void)?
    let onPlaybackFailed: (() -> Void)?
    let onPlaybackRecovered: (() -> Void)?

    private let mediaOverscanScale: CGFloat = 1.06
    private let mediaVerticalOffset: CGFloat = -18

    @State private var player = AVPlayer()
    @State private var hasPlaybackFailure = false
    @State private var isPausedByUser = false
    @State private var aspectRatioResolveTask: Task<Void, Never>?
    @State private var lastResolvedPlaybackURL: URL?

    init(
        playbackURL: URL?,
        posterURL: URL?,
        isActive: Bool,
        presentationStyle: PresentationStyle = .immersiveFill,
        onPlaybackAspectRatioResolved: ((CGFloat?) -> Void)? = nil,
        onPlaybackFailed: (() -> Void)? = nil,
        onPlaybackRecovered: (() -> Void)? = nil
    ) {
        self.playbackURL = playbackURL
        self.posterURL = posterURL
        self.isActive = isActive
        self.presentationStyle = presentationStyle
        self.onPlaybackAspectRatioResolved = onPlaybackAspectRatioResolved
        self.onPlaybackFailed = onPlaybackFailed
        self.onPlaybackRecovered = onPlaybackRecovered
    }

    var body: some View {
        GeometryReader { proxy in
            ZStack {
                Color.black

                if presentationStyle == .immersiveFill {
                    AsyncImage(url: posterURL) { phase in
                        switch phase {
                        case .success(let image):
                            posterImageView(image: image)
                        default:
                            Color.black
                        }
                    }
                    .frame(width: proxy.size.width, height: proxy.size.height)
                    .modifier(
                        MediaTransformModifier(
                            presentationStyle: presentationStyle,
                            overscanScale: mediaOverscanScale,
                            verticalOffset: mediaVerticalOffset
                        )
                    )
                }

                PlayerLayerContainer(
                    player: player,
                    videoGravity: videoGravity
                )
                    .frame(width: proxy.size.width, height: proxy.size.height)
                    .modifier(
                        MediaTransformModifier(
                            presentationStyle: presentationStyle,
                            overscanScale: mediaOverscanScale,
                            verticalOffset: mediaVerticalOffset
                        )
                    )
                    .allowsHitTesting(false)

                if hasPlaybackFailure {
                    Image(systemName: "play.slash.fill")
                        .font(.system(size: 44, weight: .regular))
                        .foregroundStyle(Color.white.opacity(0.72))
                        .shadow(color: .black.opacity(0.35), radius: 8, x: 0, y: 4)
                } else if isPausedByUser {
                    ZStack {
                        Circle()
                            .fill(Color.black.opacity(0.34))
                            .frame(width: 82, height: 82)
                        Image(systemName: "pause.fill")
                            .font(.system(size: 28, weight: .semibold))
                            .foregroundStyle(.white)
                    }
                    .shadow(color: .black.opacity(0.22), radius: 12, x: 0, y: 6)
                    .transition(.opacity)
                }
            }
            .frame(width: proxy.size.width, height: proxy.size.height)
            .clipped()
        }
        .ignoresSafeArea()
        .background(Color.black)
        .contentShape(Rectangle())
        .onTapGesture {
            togglePlaybackByUser()
        }
        .onAppear {
            configurePlayer()
        }
        .onDisappear {
            aspectRatioResolveTask?.cancel()
            player.pause()
            isPausedByUser = false
            clearPlaybackFailureIfNeeded()
        }
        .onChange(of: playbackURL) { _, _ in
            aspectRatioResolveTask?.cancel()
            lastResolvedPlaybackURL = nil
            isPausedByUser = false
            configurePlayer()
        }
        .onChange(of: presentationStyle) { _, _ in
            if isActive && !isPausedByUser {
                player.play()
            }
        }
        .onChange(of: isActive) { _, active in
            if active {
                if !isPausedByUser {
                    player.play()
                }
            } else {
                player.pause()
            }
        }
        .onReceive(NotificationCenter.default.publisher(for: .AVPlayerItemDidPlayToEndTime)) { notification in
            guard let item = notification.object as? AVPlayerItem,
                  item == player.currentItem else {
                return
            }
            item.seek(to: .zero) { _ in
                if isActive && !isPausedByUser {
                    player.play()
                }
            }
        }
        .onReceive(NotificationCenter.default.publisher(for: .AVPlayerItemFailedToPlayToEndTime)) { notification in
            guard let item = notification.object as? AVPlayerItem,
                  item == player.currentItem else {
                return
            }
            markPlaybackFailureIfNeeded()
        }
        .onReceive(
            Timer.publish(every: 1.2, on: .main, in: .common)
                .autoconnect()
        ) { _ in
            guard isActive, let item = player.currentItem else {
                return
            }
            switch item.status {
            case .failed:
                markPlaybackFailureIfNeeded()
            case .readyToPlay:
                clearPlaybackFailureIfNeeded()
            case .unknown:
                break
            @unknown default:
                break
            }
        }
    }

    private func configurePlayer() {
        guard let playbackURL else {
            aspectRatioResolveTask?.cancel()
            lastResolvedPlaybackURL = nil
            onPlaybackAspectRatioResolved?(nil)
            player.replaceCurrentItem(with: nil)
            clearPlaybackFailureIfNeeded()
            return
        }
        if let currentAsset = player.currentItem?.asset as? AVURLAsset,
           currentAsset.url == playbackURL {
            resolvePlaybackAspectRatioIfNeeded(for: playbackURL)
            if isActive && !isPausedByUser {
                player.play()
            }
            return
        }
        clearPlaybackFailureIfNeeded()
        let item = AVPlayerItem(url: playbackURL)
        player.replaceCurrentItem(with: item)
        resolvePlaybackAspectRatioIfNeeded(for: playbackURL)
        if isActive && !isPausedByUser {
            player.play()
        }
    }

    private func togglePlaybackByUser() {
        guard isActive,
              !hasPlaybackFailure,
              player.currentItem != nil else {
            return
        }
        if isPausedByUser {
            isPausedByUser = false
            player.play()
        } else {
            isPausedByUser = true
            player.pause()
        }
    }

    private func markPlaybackFailureIfNeeded() {
        guard !hasPlaybackFailure else {
            return
        }
        hasPlaybackFailure = true
        onPlaybackFailed?()
    }

    private func clearPlaybackFailureIfNeeded() {
        guard hasPlaybackFailure else {
            return
        }
        hasPlaybackFailure = false
        onPlaybackRecovered?()
    }

    private var videoGravity: AVLayerVideoGravity {
        switch presentationStyle {
        case .immersiveFill:
            return .resizeAspectFill
        case .fitWidthPreview:
            return .resizeAspect
        }
    }

    @ViewBuilder
    private func posterImageView(image: Image) -> some View {
        switch presentationStyle {
        case .immersiveFill:
            image
                .resizable()
                .scaledToFill()
        case .fitWidthPreview:
            image
                .resizable()
                .scaledToFit()
        }
    }

    private func resolvePlaybackAspectRatioIfNeeded(for playbackURL: URL) {
        guard lastResolvedPlaybackURL != playbackURL else {
            return
        }
        aspectRatioResolveTask?.cancel()
        aspectRatioResolveTask = Task {
            let ratio = await loadPlaybackAspectRatio(from: playbackURL)
            guard !Task.isCancelled else {
                return
            }
            await MainActor.run {
                guard self.playbackURL == playbackURL else {
                    return
                }
                self.lastResolvedPlaybackURL = playbackURL
                self.onPlaybackAspectRatioResolved?(ratio)
            }
        }
    }

    private func loadPlaybackAspectRatio(from playbackURL: URL) async -> CGFloat? {
        let asset = AVURLAsset(url: playbackURL)
        do {
            let tracks = try await asset.loadTracks(withMediaType: .video)
            guard let track = tracks.first else {
                return nil
            }
            let naturalSize = try await track.load(.naturalSize)
            let preferredTransform = try await track.load(.preferredTransform)
            let transformed = naturalSize.applying(preferredTransform)
            let resolvedWidth = abs(transformed.width)
            let resolvedHeight = abs(transformed.height)
            guard resolvedWidth > 0, resolvedHeight > 0 else {
                return nil
            }
            return resolvedWidth / resolvedHeight
        } catch {
            return nil
        }
    }
}

private struct PlayerLayerContainer: UIViewRepresentable {
    let player: AVPlayer
    let videoGravity: AVLayerVideoGravity

    func makeUIView(context: Context) -> PlayerLayerView {
        let view = PlayerLayerView()
        view.playerLayer.videoGravity = videoGravity
        view.playerLayer.player = player
        return view
    }

    func updateUIView(_ uiView: PlayerLayerView, context: Context) {
        uiView.playerLayer.videoGravity = videoGravity
        if uiView.playerLayer.player !== player {
            uiView.playerLayer.player = player
        }
    }
}

private struct MediaTransformModifier: ViewModifier {
    let presentationStyle: ShortVideoPlayerView.PresentationStyle
    let overscanScale: CGFloat
    let verticalOffset: CGFloat

    func body(content: Content) -> some View {
        switch presentationStyle {
        case .immersiveFill:
            content
                .scaleEffect(overscanScale)
                .offset(y: verticalOffset)
        case .fitWidthPreview:
            content
        }
    }
}

private final class PlayerLayerView: UIView {
    override static var layerClass: AnyClass {
        AVPlayerLayer.self
    }

    var playerLayer: AVPlayerLayer {
        layer as! AVPlayerLayer
    }
}
