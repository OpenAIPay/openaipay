import SwiftUI

struct ShortVideoFeedView: View {
    private struct CommentSheetContext: Identifiable {
        let videoId: String
        let commentCount: Int64
        let referenceTitle: String
        var id: String { videoId }
    }

    private struct LandscapeFullscreenContext: Identifiable {
        let item: ShortVideoFeedItem
        var id: String { item.videoId }
    }

    var onBackHome: (() -> Void)? = nil
    var showsBottomTab: Bool = true
    var bottomTabReservedHeight: CGFloat = 0

    @EnvironmentObject private var appState: AppState
    @StateObject private var viewModel = ShortVideoFeedViewModel()
    @GestureState private var dragTranslationY: CGFloat = 0
    @State private var commentSheetContext: CommentSheetContext?
    @State private var playbackFailedVideoId: String?
    @State private var landscapeFullscreenContext: LandscapeFullscreenContext?
    @State private var resolvedPlaybackAspectRatios: [String: CGFloat] = [:]
    @State private var hasAutoOpenedCommentSheet = false
    @State private var followedAuthorIDs: Set<Int64> = []

    private var shouldAutoOpenLandscapeFullscreenOnLaunch: Bool {
        ProcessInfo.processInfo.arguments.contains("-OpenAiPayAutoLandscapeFullscreen")
    }

    private var shouldAutoOpenCommentSheetOnLaunch: Bool {
        ProcessInfo.processInfo.arguments.contains("-OpenAiPayAutoOpenCommentSheet")
    }

    var body: some View {
        ZStack {
            Color.black
                .ignoresSafeArea()

            if let currentItem = viewModel.state.currentItem {
                activeVideoView(for: currentItem)
            } else if viewModel.state.isInitialLoading {
                loadingView
            } else if let errorMessage = viewModel.state.errorMessage {
                errorView(message: errorMessage)
            } else {
                emptyView
            }

            if onBackHome != nil,
               viewModel.state.currentItem == nil,
               !viewModel.state.isInitialLoading {
                nonActiveBackButtonOverlay
            }
        }
        .task {
            appState.recordPageVisit("/page/short-video")
            viewModel.loadIfNeeded()
        }
        .onChange(of: viewModel.state.currentItem?.videoId) { _, _ in
            playbackFailedVideoId = nil
            landscapeFullscreenContext = nil
            if shouldAutoOpenLandscapeFullscreenOnLaunch,
               let currentItem = viewModel.state.currentItem,
               currentItem.isLandscapePlayback {
                landscapeFullscreenContext = LandscapeFullscreenContext(item: currentItem)
            }
            if shouldAutoOpenCommentSheetOnLaunch,
               !hasAutoOpenedCommentSheet,
               let currentItem = viewModel.state.currentItem {
                commentSheetContext = CommentSheetContext(
                    videoId: currentItem.videoId,
                    commentCount: currentItem.engagement.commentCount,
                    referenceTitle: currentItem.displayCaption ?? currentItem.displayAuthorLine
                )
                hasAutoOpenedCommentSheet = true
            }
        }
        .sheet(item: $commentSheetContext) { context in
            ShortVideoCommentSheetView(
                totalCommentCount: context.commentCount,
                referenceTitle: context.referenceTitle,
                viewModel: ShortVideoCommentSheetViewModel(
                    videoId: context.videoId,
                    totalCommentCount: context.commentCount,
                    onCommentCreated: { comment in
                        viewModel.handleCommentCreated(comment, for: context.videoId)
                    }
                ),
                onClose: {
                    commentSheetContext = nil
                }
            )
        }
        .fullScreenCover(item: $landscapeFullscreenContext) { context in
            LandscapeFullscreenPlaybackView(
                item: context.item,
                onDismiss: {
                    landscapeFullscreenContext = nil
                }
            )
        }
    }

    private func activeVideoView(for item: ShortVideoFeedItem) -> some View {
        GeometryReader { proxy in
            let safeTop = max(proxy.safeAreaInsets.top, 0)
            let safeBottom = max(proxy.safeAreaInsets.bottom, 0)
            let safeTrailing = max(proxy.safeAreaInsets.trailing, 0)
            let contentBottomInset = safeBottom + (showsBottomTab ? 74 : bottomTabReservedHeight)
            let videoFrameHeight = proxy.size.height + safeTop + safeBottom
            let videoVerticalOffset = (safeBottom - safeTop) / 2
            let effectiveAspectRatio = resolvedPlaybackAspectRatios[item.videoId] ?? item.playbackAspectRatio
            let isLandscapeVideo = (effectiveAspectRatio ?? 0) > 1.05
            let isLandscapePreview = isLandscapeVideo && landscapeFullscreenContext?.id != item.videoId
            let previewAspectRatio = max(effectiveAspectRatio ?? CGFloat(16.0 / 9.0), CGFloat(1.2))
            let previewHeight = min(proxy.size.width / previewAspectRatio, proxy.size.height * 0.42)
            let previewButtonOffsetY = (previewHeight / 2) + 26

            ZStack {
                ShortVideoPlayerView(
                    playbackURL: item.playbackURL,
                    posterURL: item.coverImageURL,
                    isActive: landscapeFullscreenContext == nil,
                    presentationStyle: isLandscapePreview ? .fitWidthPreview : .immersiveFill,
                    onPlaybackAspectRatioResolved: { aspectRatio in
                        guard let aspectRatio else {
                            return
                        }
                        resolvedPlaybackAspectRatios[item.videoId] = aspectRatio
                    },
                    onPlaybackFailed: {
                        playbackFailedVideoId = item.videoId
                    },
                    onPlaybackRecovered: {
                        if playbackFailedVideoId == item.videoId {
                            playbackFailedVideoId = nil
                        }
                    }
                )
                .frame(
                    width: proxy.size.width,
                    height: isLandscapePreview ? proxy.size.height : videoFrameHeight
                )
                .ignoresSafeArea()
                .offset(
                    y: (isLandscapePreview ? 0 : videoVerticalOffset) + (dragTranslationY * 0.14)
                )

                LinearGradient(
                    stops: [
                        .init(color: .clear, location: 0.00),
                        .init(color: .clear, location: 0.56),
                        .init(color: .black.opacity(0.18), location: 0.78),
                        .init(color: .black.opacity(0.78), location: 1.00)
                    ],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()
                .allowsHitTesting(false)

                VStack(spacing: 0) {
                    topChrome(for: item, safeTop: safeTop)
                    Spacer()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .allowsHitTesting(
                    onBackHome != nil
                    && commentSheetContext == nil
                    && playbackFailedVideoId == item.videoId
                )

                bottomInfoPanel(
                    for: item,
                    bottomInset: contentBottomInset,
                    safeTrailing: safeTrailing
                )
                .allowsHitTesting(false)

                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        ShortVideoActionToolbar(
                            avatarURL: item.authorAvatarURL,
                            coverURL: item.coverImageURL,
                            engagement: item.engagement,
                            followBadgeStyle: followedAuthorIDs.contains(item.author.userId)
                                ? .check
                                : (item.author.userId > 0 ? .plus : .hidden),
                            isLikePending: viewModel.state.isLikeRequestInFlight(for: item.videoId),
                            isFavoritePending: viewModel.state.isFavoriteRequestInFlight(for: item.videoId),
                            onFollow: item.author.userId > 0 && !followedAuthorIDs.contains(item.author.userId) ? {
                                appState.recordBehaviorEvent(
                                    eventName: "/page/short-video/follow-author",
                                    eventType: "USER_ACTION",
                                    eventCode: "SHORT_VIDEO_FOLLOW_AUTHOR",
                                    pageName: "/page/short-video",
                                    actionName: "FOLLOW_AUTHOR",
                                    resultSummary: "REQUESTED",
                                    payload: [
                                        "videoId": item.videoId,
                                        "authorUserId": item.author.userId,
                                        "authorNickname": item.author.nickname
                                    ],
                                    minimumInterval: 0
                                )
                                followedAuthorIDs.insert(item.author.userId)
                            } : nil,
                            onLike: {
                                let actionName = item.engagement.liked ? "UNLIKE" : "LIKE"
                                appState.recordBehaviorEvent(
                                    eventName: "/page/short-video/like",
                                    eventType: "USER_ACTION",
                                    eventCode: "SHORT_VIDEO_LIKE",
                                    pageName: "/page/short-video",
                                    actionName: actionName,
                                    resultSummary: "REQUESTED",
                                    payload: [
                                        "videoId": item.videoId,
                                        "currentIndex": viewModel.state.currentIndex,
                                        "likedBefore": item.engagement.liked
                                    ]
                                )
                                viewModel.toggleLike(videoId: item.videoId)
                            },
                            onFavorite: {
                                let actionName = item.engagement.favorited ? "UNFAVORITE" : "FAVORITE"
                                appState.recordBehaviorEvent(
                                    eventName: "/page/short-video/favorite",
                                    eventType: "USER_ACTION",
                                    eventCode: "SHORT_VIDEO_FAVORITE",
                                    pageName: "/page/short-video",
                                    actionName: actionName,
                                    resultSummary: "REQUESTED",
                                    payload: [
                                        "videoId": item.videoId,
                                        "currentIndex": viewModel.state.currentIndex,
                                        "favoritedBefore": item.engagement.favorited
                                    ]
                                )
                                viewModel.toggleFavorite(videoId: item.videoId)
                            },
                            onComment: {
                                appState.recordBehaviorEvent(
                                    eventName: "/page/short-video/comment-panel",
                                    eventType: "USER_ACTION",
                                    eventCode: "SHORT_VIDEO_COMMENT_PANEL",
                                    pageName: "/page/short-video",
                                    actionName: "OPEN_COMMENT_PANEL",
                                    resultSummary: "REQUESTED",
                                    payload: [
                                        "videoId": item.videoId,
                                        "currentIndex": viewModel.state.currentIndex,
                                        "commentCount": item.engagement.commentCount
                                    ]
                                )
                                commentSheetContext = CommentSheetContext(
                                    videoId: item.videoId,
                                    commentCount: item.engagement.commentCount,
                                    referenceTitle: item.displayCaption ?? item.displayAuthorLine
                                )
                            },
                            onShare: {
                                appState.recordBehaviorEvent(
                                    eventName: "/page/short-video/share",
                                    eventType: "USER_ACTION",
                                    eventCode: "SHORT_VIDEO_SHARE",
                                    pageName: "/page/short-video",
                                    actionName: "OPEN_SHARE_PANEL",
                                    resultSummary: "REQUESTED",
                                    payload: [
                                        "videoId": item.videoId,
                                        "currentIndex": viewModel.state.currentIndex
                                    ]
                                )
                            }
                        )
                    }
                }
                .padding(.trailing, max(22, safeTrailing + 18))
                .padding(.bottom, max(108, contentBottomInset + 28))
                .frame(maxWidth: .infinity, maxHeight: .infinity)

                if isLandscapePreview {
                    landscapeFullscreenButton(for: item)
                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
                        .offset(y: previewButtonOffsetY)
                }

                if showsBottomTab {
                    VStack {
                        Spacer()
                        douyinBottomBar(safeBottom: safeBottom)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }

                if let feedbackMessage = viewModel.state.feedbackMessage {
                    VStack {
                        Spacer()
                        Text(feedbackMessage)
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 10)
                            .background(Color.black.opacity(0.56))
                            .clipShape(Capsule())
                            .padding(.bottom, 188)
                    }
                    .frame(maxWidth: .infinity)
                    .allowsHitTesting(false)
                }
            }
            .frame(width: proxy.size.width, height: proxy.size.height)
        }
        .ignoresSafeArea()
        .contentShape(Rectangle())
        .simultaneousGesture(
            DragGesture(minimumDistance: 24)
                .updating($dragTranslationY) { value, state, _ in
                    state = value.translation.height
                }
                .onEnded { value in
                    let previousItemId = viewModel.state.currentItem?.videoId
                    if value.translation.height <= -72 {
                        viewModel.showNextVideo()
                        if let currentVideoId = viewModel.state.currentItem?.videoId,
                           currentVideoId != previousItemId {
                            var payload: [String: Any] = [
                                "toVideoId": currentVideoId,
                                "currentIndex": viewModel.state.currentIndex
                            ]
                            if let previousItemId {
                                payload["fromVideoId"] = previousItemId
                            }
                            appState.recordBehaviorEvent(
                                eventName: "/page/short-video/swipe-next",
                                eventType: "USER_ACTION",
                                eventCode: "SHORT_VIDEO_SWIPE_NEXT",
                                pageName: "/page/short-video",
                                actionName: "SWIPE_NEXT",
                                resultSummary: "SUCCESS",
                                payload: payload,
                                minimumInterval: 0
                            )
                        }
                    } else if value.translation.height >= 72 {
                        viewModel.showPreviousVideo()
                        if let currentVideoId = viewModel.state.currentItem?.videoId,
                           currentVideoId != previousItemId {
                            var payload: [String: Any] = [
                                "toVideoId": currentVideoId,
                                "currentIndex": viewModel.state.currentIndex
                            ]
                            if let previousItemId {
                                payload["fromVideoId"] = previousItemId
                            }
                            appState.recordBehaviorEvent(
                                eventName: "/page/short-video/swipe-previous",
                                eventType: "USER_ACTION",
                                eventCode: "SHORT_VIDEO_SWIPE_PREVIOUS",
                                pageName: "/page/short-video",
                                actionName: "SWIPE_PREVIOUS",
                                resultSummary: "SUCCESS",
                                payload: payload,
                                minimumInterval: 0
                            )
                        }
                    }
                }
        )
        .accessibilityElement(children: .combine)
        .accessibilityLabel(item.accessibilitySummary)
    }

    @ViewBuilder
    private func topChrome(for item: ShortVideoFeedItem, safeTop: CGFloat) -> some View {
        if onBackHome != nil,
           commentSheetContext == nil,
           playbackFailedVideoId == item.videoId {
            HStack {
                backHomeButton(currentItem: item)
                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.top, safeTop + 8)
        }
    }

    private func bottomInfoPanel(for item: ShortVideoFeedItem, bottomInset: CGFloat, safeTrailing: CGFloat) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(item.displayAuthorLine)
                .font(.system(size: 18.5, weight: .black))
                .foregroundStyle(.white)
                .shadow(color: .black.opacity(0.42), radius: 8, x: 0, y: 2)

            if let caption = item.displayCaption {
                (
                    Text(caption)
                        .font(.system(size: 14, weight: .medium))
                    + Text(" 展开∨")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundStyle(.white.opacity(0.86))
                )
                .foregroundStyle(.white)
                .lineLimit(2)
                .shadow(color: .black.opacity(0.42), radius: 6, x: 0, y: 2)
            }

            HStack(spacing: 6) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 11, weight: .semibold))
                Text("相关搜索 · \(item.author.nickname)热门视频")
                    .font(.system(size: 11, weight: .semibold))
                    .lineLimit(1)
            }
            .foregroundStyle(.white.opacity(0.95))
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(Color.black.opacity(0.50))
            .clipShape(Capsule())

            if viewModel.state.isLoadingMore {
                Text("正在加载下一个视频...")
                    .font(.system(size: 10, weight: .medium))
                    .foregroundStyle(.white.opacity(0.78))
            } else {
                Text("上划切换下一个视频")
                    .font(.system(size: 10, weight: .medium))
                    .foregroundStyle(.white.opacity(0.78))
            }
        }
        .padding(.leading, 24)
        .padding(.trailing, 118 + safeTrailing)
        .padding(.bottom, max(62, bottomInset + 14))
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
    }

    private func landscapeFullscreenButton(for item: ShortVideoFeedItem) -> some View {
        Button {
            landscapeFullscreenContext = LandscapeFullscreenContext(item: item)
            appState.recordBehaviorEvent(
                eventName: "/page/short-video/fullscreen",
                eventType: "USER_ACTION",
                eventCode: "SHORT_VIDEO_FULLSCREEN",
                pageName: "/page/short-video",
                actionName: "ENTER_FULLSCREEN",
                resultSummary: "REQUESTED",
                payload: [
                    "videoId": item.videoId,
                    "currentIndex": viewModel.state.currentIndex,
                    "presentation": "LANDSCAPE_FULLSCREEN"
                ],
                minimumInterval: 0
            )
        } label: {
            HStack(spacing: 8) {
                Image(systemName: "arrow.up.left.and.arrow.down.right")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(.white)

                Text("全屏观看")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundStyle(.white)
                    .lineLimit(1)
            }
            .padding(.leading, 12)
            .padding(.trailing, 16)
            .frame(height: 34)
            .background(
                LandscapeFullscreenButtonShape()
                    .fill(Color.black.opacity(0.82))
            )
            .overlay(
                LandscapeFullscreenButtonShape()
                    .stroke(Color.white.opacity(0.14), lineWidth: 0.8)
            )
            .shadow(color: .black.opacity(0.28), radius: 8, x: 0, y: 4)
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier("short_video_landscape_fullscreen_button")
    }

    private func douyinBottomBar(safeBottom: CGFloat) -> some View {
        HStack(spacing: 0) {
            bottomTabLabel("首页", isSelected: true, showBadge: false)
            bottomTabLabel("朋友", isSelected: false, showBadge: false)
            bottomTabPlusButton
            bottomTabLabel("消息", isSelected: false, showBadge: false)
            bottomTabLabel("我", isSelected: false, showBadge: true)
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 10)
        .padding(.top, 9)
        .padding(.bottom, max(8, safeBottom))
        .background(Color.black.opacity(0.94))
        .overlay(alignment: .top) {
            Rectangle()
                .fill(Color.white.opacity(0.08))
                .frame(height: 0.5)
        }
    }

    private func bottomTabLabel(_ title: String, isSelected: Bool, showBadge: Bool) -> some View {
        VStack(spacing: 5) {
            Text(title)
                .font(.system(size: 20, weight: isSelected ? .bold : .semibold))
                .foregroundStyle(isSelected ? .white : Color.white.opacity(0.84))

            if showBadge {
                Circle()
                    .fill(Color.red)
                    .frame(width: 7, height: 7)
            } else {
                Color.clear
                    .frame(width: 7, height: 7)
            }
        }
        .frame(maxWidth: .infinity)
    }

    private var bottomTabPlusButton: some View {
        Button {
            appState.recordBehaviorEvent(
                eventName: "/page/short-video/plus-entry",
                eventType: "USER_ACTION",
                eventCode: "SHORT_VIDEO_PLUS_ENTRY",
                pageName: "/page/short-video",
                actionName: "PLUS_TAP",
                resultSummary: "REQUESTED",
                payload: nil,
                minimumInterval: 0
            )
        } label: {
            ZStack {
                RoundedRectangle(cornerRadius: 8, style: .continuous)
                    .fill(Color.white)
                    .frame(width: 46, height: 32)
                RoundedRectangle(cornerRadius: 8, style: .continuous)
                    .fill(Color.black)
                    .frame(width: 42, height: 28)
                Image(systemName: "plus")
                    .font(.system(size: 17, weight: .heavy))
                    .foregroundStyle(.white)
            }
        }
        .buttonStyle(.plain)
        .frame(maxWidth: .infinity)
    }

    private var nonActiveBackButtonOverlay: some View {
        GeometryReader { proxy in
            VStack {
                HStack {
                    backHomeButton(currentItem: nil)
                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.top, max(proxy.safeAreaInsets.top, 0) + 8)
                Spacer()
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        }
        .ignoresSafeArea()
    }

    private func backHomeButton(currentItem: ShortVideoFeedItem?) -> some View {
        Button {
            handleBackHomeTap(currentItem: currentItem)
        } label: {
            HStack(spacing: 6) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 14, weight: .semibold))
                Text("返回首页")
                    .font(.system(size: 15, weight: .semibold))
            }
            .foregroundStyle(.white)
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(Color.black.opacity(0.42))
            .clipShape(Capsule())
        }
        .accessibilityIdentifier("short_video_back_home")
        .buttonStyle(.plain)
    }

    private func handleBackHomeTap(currentItem: ShortVideoFeedItem?) {
        guard let onBackHome else {
            return
        }
        var payload: [String: Any] = [
            "playbackFailed": playbackFailedVideoId != nil
        ]
        if let currentItem {
            payload["videoId"] = currentItem.videoId
            payload["currentIndex"] = viewModel.state.currentIndex
        }
        appState.recordBehaviorEvent(
            eventName: "/page/short-video/back-home",
            eventType: "USER_ACTION",
            eventCode: "SHORT_VIDEO_BACK_HOME",
            pageName: "/page/short-video",
            actionName: "BACK_HOME",
            resultSummary: "REQUESTED",
            payload: payload,
            minimumInterval: 0
        )
        onBackHome()
    }

    private var loadingView: some View {
        VStack(spacing: 18) {
            ProgressView()
                .tint(.white)
                .scaleEffect(1.2)

            Text("正在加载短视频")
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(.white)
        }
    }

    private func errorView(message: String) -> some View {
        VStack(spacing: 16) {
            Image(systemName: "wifi.slash")
                .font(.system(size: 34, weight: .medium))
                .foregroundStyle(.white.opacity(0.88))

            Text(message)
                .font(.system(size: 16, weight: .medium))
                .foregroundStyle(.white)
                .multilineTextAlignment(.center)

            Button("重新加载") {
                viewModel.retry()
            }
            .buttonStyle(.plain)
            .padding(.horizontal, 18)
            .padding(.vertical, 12)
            .background(Color.white)
            .foregroundStyle(.black)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .padding(28)
    }

    private var emptyView: some View {
        VStack(spacing: 14) {
            Image(systemName: "play.rectangle.on.rectangle")
                .font(.system(size: 34, weight: .medium))
                .foregroundStyle(.white.opacity(0.86))

            Text("暂时还没有可播放的视频")
                .font(.system(size: 17, weight: .semibold))
                .foregroundStyle(.white)

            Button("刷新看看") {
                viewModel.refresh()
            }
            .buttonStyle(.plain)
            .padding(.horizontal, 18)
            .padding(.vertical, 12)
            .background(Color.white)
            .foregroundStyle(.black)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .padding(24)
    }
}

private struct LandscapeFullscreenButtonShape: Shape {
    func path(in rect: CGRect) -> Path {
        let radius = min(rect.height / 2, 11)
        let tipWidth: CGFloat = 14

        return Path { path in
            path.move(to: CGPoint(x: radius, y: 0))
            path.addLine(to: CGPoint(x: rect.width - tipWidth, y: 0))
            path.addLine(to: CGPoint(x: rect.width, y: rect.midY))
            path.addLine(to: CGPoint(x: rect.width - tipWidth, y: rect.height))
            path.addLine(to: CGPoint(x: radius, y: rect.height))
            path.addArc(
                center: CGPoint(x: radius, y: rect.height - radius),
                radius: radius,
                startAngle: .degrees(90),
                endAngle: .degrees(180),
                clockwise: false
            )
            path.addLine(to: CGPoint(x: 0, y: radius))
            path.addArc(
                center: CGPoint(x: radius, y: radius),
                radius: radius,
                startAngle: .degrees(180),
                endAngle: .degrees(270),
                clockwise: false
            )
            path.closeSubpath()
        }
    }
}

private struct LandscapeFullscreenPlaybackView: View {
    let item: ShortVideoFeedItem
    let onDismiss: () -> Void

    @Environment(\.dismiss) private var dismiss

    var body: some View {
        GeometryReader { proxy in
            ZStack {
                Color.black
                    .ignoresSafeArea()

                if proxy.size.width > proxy.size.height {
                    landscapeContent(size: proxy.size)
                } else {
                    let rotatedSize = CGSize(width: proxy.size.height, height: proxy.size.width)
                    landscapeContent(size: rotatedSize)
                        .rotationEffect(.degrees(90))
                        .position(x: proxy.size.width / 2, y: proxy.size.height / 2)
                }
            }
        }
        .background(Color.black)
        .ignoresSafeArea()
        .onAppear {
            requestOrientation(.landscapeRight)
        }
        .onDisappear {
            requestOrientation(.portrait)
        }
    }

    private func dismissLandscape() {
        onDismiss()
        dismiss()
    }

    private func requestOrientation(_ mask: UIInterfaceOrientationMask) {
        guard let windowScene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first else {
            return
        }
        windowScene.requestGeometryUpdate(.iOS(interfaceOrientations: mask)) { _ in
            // 如果系统方向请求失败，视图会自动保留旋转兜底布局。
        }
    }

    private func landscapeContent(size: CGSize) -> some View {
        ZStack(alignment: .topLeading) {
            ShortVideoPlayerView(
                playbackURL: item.playbackURL,
                posterURL: item.coverImageURL,
                isActive: true,
                presentationStyle: .immersiveFill
            )
            .frame(width: size.width, height: size.height)
            .background(Color.black)

            Button {
                dismissLandscape()
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 15, weight: .semibold))
                    Text("退出全屏")
                        .font(.system(size: 16, weight: .semibold))
                }
                .foregroundStyle(.white)
                .padding(.horizontal, 14)
                .padding(.vertical, 9)
                .background(Color.black.opacity(0.42))
                .clipShape(Capsule())
            }
            .buttonStyle(.plain)
            .padding(.top, 18)
            .padding(.leading, 18)
        }
        .frame(width: size.width, height: size.height)
    }
}
