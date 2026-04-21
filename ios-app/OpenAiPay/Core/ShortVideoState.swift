import Foundation

struct ShortVideoFeedState: Equatable {
    struct EngagementRequest: Equatable {
        enum Kind: Hashable {
            case like
            case favorite
        }

        let videoId: String
        let kind: Kind
        let targetState: Bool
    }

    private struct PendingToggleState: Equatable {
        let baselineState: Bool
        var requestedState: Bool
        var desiredState: Bool
    }

    private(set) var items: [ShortVideoFeedItemData] = []
    private(set) var currentIndex: Int = 0
    private(set) var nextCursor: String?
    private(set) var hasMore: Bool = false
    private(set) var isInitialLoading: Bool = false
    private(set) var isLoadingMore: Bool = false
    private(set) var errorMessage: String?
    private(set) var feedbackMessage: String?
    private var pendingLikeToggles: [String: PendingToggleState] = [:]
    private var pendingFavoriteToggles: [String: PendingToggleState] = [:]

    var currentItem: ShortVideoFeedItemData? {
        guard items.indices.contains(currentIndex) else {
            return nil
        }
        return items[currentIndex]
    }

    var shouldPrefetch: Bool {
        guard hasMore, !isInitialLoading, !isLoadingMore, !items.isEmpty else {
            return false
        }
        return items.count - currentIndex <= 2
    }

    var shouldShowEmptyState: Bool {
        !isInitialLoading && items.isEmpty && errorMessage == nil
    }

    mutating func beginInitialLoad() {
        isInitialLoading = true
        isLoadingMore = false
        errorMessage = nil
        feedbackMessage = nil
    }

    mutating func beginLoadMoreIfNeeded() -> String? {
        guard hasMore, !isInitialLoading, !isLoadingMore, !items.isEmpty else {
            return nil
        }
        isLoadingMore = true
        errorMessage = nil
        return nextCursor
    }

    mutating func applyPage(_ page: ShortVideoFeedPageData, isInitial: Bool) {
        if isInitial {
            items = page.items
            currentIndex = 0
            pendingLikeToggles.removeAll()
            pendingFavoriteToggles.removeAll()
            feedbackMessage = nil
        } else {
            mergePageItems(page.items)
            if !items.isEmpty {
                currentIndex = min(currentIndex, items.count - 1)
            }
        }
        nextCursor = page.nextCursor
        hasMore = page.hasMore
        isInitialLoading = false
        isLoadingMore = false
        errorMessage = nil
    }

    mutating func applyFailure(_ message: String?, isInitial: Bool) {
        isInitialLoading = false
        isLoadingMore = false
        errorMessage = normalizedErrorMessage(message)
        if isInitial, items.isEmpty {
            currentIndex = 0
        }
    }

    @discardableResult
    mutating func advanceToNextItem() -> Bool {
        guard currentIndex + 1 < items.count else {
            return false
        }
        currentIndex += 1
        errorMessage = nil
        feedbackMessage = nil
        return true
    }

    @discardableResult
    mutating func retreatToPreviousItem() -> Bool {
        guard currentIndex > 0 else {
            return false
        }
        currentIndex -= 1
        errorMessage = nil
        feedbackMessage = nil
        return true
    }

    mutating func clearError() {
        errorMessage = nil
    }

    mutating func clearFeedbackMessage() {
        feedbackMessage = nil
    }

    mutating func incrementCommentCount(for videoId: String) {
        guard let index = items.firstIndex(where: { $0.videoId == videoId }) else {
            return
        }
        let engagement = items[index].engagement
        items[index] = items[index].replacingEngagement(
            ShortVideoEngagementData(
                liked: engagement.liked,
                favorited: engagement.favorited,
                likeCount: engagement.likeCount,
                favoriteCount: engagement.favoriteCount,
                commentCount: engagement.commentCount + 1
            )
        )
    }

    func isLikeRequestInFlight(for videoId: String) -> Bool {
        pendingLikeToggles[videoId] != nil
    }

    func isFavoriteRequestInFlight(for videoId: String) -> Bool {
        pendingFavoriteToggles[videoId] != nil
    }

    mutating func toggleLike(for videoId: String) -> EngagementRequest? {
        beginToggle(for: videoId, kind: .like)
    }

    mutating func toggleFavorite(for videoId: String) -> EngagementRequest? {
        beginToggle(for: videoId, kind: .favorite)
    }

    mutating func applyEngagementSuccess(_ engagement: ShortVideoEngagementData,
                                         for request: EngagementRequest) -> EngagementRequest? {
        guard let pending = pendingState(for: request.videoId, kind: request.kind) else {
            return nil
        }

        let resolvedServerState = engagement.state(for: request.kind)
        let nextRequest: EngagementRequest?
        if pending.desiredState == resolvedServerState {
            removePendingState(for: request.videoId, kind: request.kind)
            nextRequest = nil
        } else {
            setPendingState(
                PendingToggleState(
                    baselineState: resolvedServerState,
                    requestedState: pending.desiredState,
                    desiredState: pending.desiredState
                ),
                for: request.videoId,
                kind: request.kind
            )
            nextRequest = EngagementRequest(
                videoId: request.videoId,
                kind: request.kind,
                targetState: pending.desiredState
            )
        }

        applyResolvedEngagement(
            reapplyingPendingStates(on: engagement, for: request.videoId),
            to: request.videoId
        )
        feedbackMessage = nil
        return nextRequest
    }

    mutating func applyEngagementFailure(for request: EngagementRequest,
                                         message: String?) -> EngagementRequest? {
        guard let pending = pendingState(for: request.videoId, kind: request.kind),
              let currentEngagement = engagement(for: request.videoId) else {
            return nil
        }

        let reverted = currentEngagement.applying(kind: request.kind, targetState: pending.baselineState)
        removePendingState(for: request.videoId, kind: request.kind)

        applyResolvedEngagement(
            reapplyingPendingStates(on: reverted, for: request.videoId),
            to: request.videoId
        )
        feedbackMessage = normalizedFeedbackMessage(message)
        return nil
    }

    private mutating func mergePageItems(_ pageItems: [ShortVideoFeedItemData]) {
        guard !pageItems.isEmpty else {
            return
        }
        var seenVideoIds = Set(items.map(\.videoId))
        for item in pageItems where !seenVideoIds.contains(item.videoId) {
            items.append(item)
            seenVideoIds.insert(item.videoId)
        }
    }

    private func normalizedErrorMessage(_ raw: String?) -> String {
        let trimmed = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? "加载视频失败，请稍后重试" : trimmed
    }

    private func normalizedFeedbackMessage(_ raw: String?) -> String {
        let trimmed = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? "操作失败，请稍后重试" : trimmed
    }

    private mutating func beginToggle(for videoId: String, kind: EngagementRequest.Kind) -> EngagementRequest? {
        guard let index = items.firstIndex(where: { $0.videoId == videoId }) else {
            return nil
        }
        feedbackMessage = nil

        let currentEngagement = items[index].engagement
        let desiredState = !currentEngagement.state(for: kind)
        items[index] = items[index].replacingEngagement(currentEngagement.applying(kind: kind, targetState: desiredState))

        if var pending = pendingState(for: videoId, kind: kind) {
            pending.desiredState = desiredState
            setPendingState(pending, for: videoId, kind: kind)
            return nil
        }

        setPendingState(
            PendingToggleState(
                baselineState: currentEngagement.state(for: kind),
                requestedState: desiredState,
                desiredState: desiredState
            ),
            for: videoId,
            kind: kind
        )
        return EngagementRequest(videoId: videoId, kind: kind, targetState: desiredState)
    }

    private func pendingState(for videoId: String, kind: EngagementRequest.Kind) -> PendingToggleState? {
        switch kind {
        case .like:
            return pendingLikeToggles[videoId]
        case .favorite:
            return pendingFavoriteToggles[videoId]
        }
    }

    private mutating func setPendingState(_ pending: PendingToggleState,
                                          for videoId: String,
                                          kind: EngagementRequest.Kind) {
        switch kind {
        case .like:
            pendingLikeToggles[videoId] = pending
        case .favorite:
            pendingFavoriteToggles[videoId] = pending
        }
    }

    private mutating func removePendingState(for videoId: String, kind: EngagementRequest.Kind) {
        switch kind {
        case .like:
            pendingLikeToggles.removeValue(forKey: videoId)
        case .favorite:
            pendingFavoriteToggles.removeValue(forKey: videoId)
        }
    }

    private func engagement(for videoId: String) -> ShortVideoEngagementData? {
        items.first(where: { $0.videoId == videoId })?.engagement
    }

    private mutating func applyResolvedEngagement(_ engagement: ShortVideoEngagementData, to videoId: String) {
        guard let index = items.firstIndex(where: { $0.videoId == videoId }) else {
            return
        }
        items[index] = items[index].replacingEngagement(engagement)
    }

    private func reapplyingPendingStates(on engagement: ShortVideoEngagementData,
                                         for videoId: String) -> ShortVideoEngagementData {
        var resolved = engagement
        if let pendingLike = pendingLikeToggles[videoId] {
            resolved = resolved.applying(kind: .like, targetState: pendingLike.desiredState)
        }
        if let pendingFavorite = pendingFavoriteToggles[videoId] {
            resolved = resolved.applying(kind: .favorite, targetState: pendingFavorite.desiredState)
        }
        return resolved
    }
}

private extension ShortVideoFeedItemData {
    func replacingEngagement(_ engagement: ShortVideoEngagementData) -> ShortVideoFeedItemData {
        ShortVideoFeedItemData(
            videoId: videoId,
            caption: caption,
            author: author,
            coverUrl: coverUrl,
            playback: playback,
            engagement: engagement
        )
    }
}

private extension ShortVideoEngagementData {
    func state(for kind: ShortVideoFeedState.EngagementRequest.Kind) -> Bool {
        switch kind {
        case .like:
            return liked
        case .favorite:
            return favorited
        }
    }

    func applying(kind: ShortVideoFeedState.EngagementRequest.Kind,
                  targetState: Bool) -> ShortVideoEngagementData {
        switch kind {
        case .like:
            let nextLikeCount = adjustedCount(currentState: liked, targetState: targetState, count: likeCount)
            return ShortVideoEngagementData(
                liked: targetState,
                favorited: favorited,
                likeCount: nextLikeCount,
                favoriteCount: favoriteCount,
                commentCount: commentCount
            )
        case .favorite:
            let nextFavoriteCount = adjustedCount(currentState: favorited, targetState: targetState, count: favoriteCount)
            return ShortVideoEngagementData(
                liked: liked,
                favorited: targetState,
                likeCount: likeCount,
                favoriteCount: nextFavoriteCount,
                commentCount: commentCount
            )
        }
    }

    private func adjustedCount(currentState: Bool, targetState: Bool, count: Int64) -> Int64 {
        guard currentState != targetState else {
            return count
        }
        return targetState ? count + 1 : max(0, count - 1)
    }
}

struct ShortVideoCommentState: Equatable {
    struct DraftImage: Equatable {
        let mediaId: String
        let previewData: Data
        let fileName: String
        let mimeType: String
        let imageURLString: String?
    }

    struct ReplyTarget: Equatable {
        let commentId: String
        let rootCommentId: String
        let nickname: String
    }

    struct SubmitPayload: Equatable {
        let content: String?
        let parentCommentId: String?
        let imageMediaId: String?
    }

    struct ReplyLoadRequest: Equatable {
        let rootCommentId: String
        let cursor: String?
        let isInitial: Bool
    }

    struct LikeRequest: Equatable {
        let commentId: String
        let targetState: Bool
    }

    private struct ReplyThreadState: Equatable {
        var isExpanded: Bool
        var items: [ShortVideoCommentData]
        var nextCursor: String?
        var hasMore: Bool
        var isLoading: Bool
        var hasLoadedInitialPage: Bool
    }

    private(set) var items: [ShortVideoCommentData] = []
    private(set) var draft: String = ""
    private(set) var draftImage: DraftImage?
    private(set) var replyTarget: ReplyTarget?
    private(set) var nextCursor: String?
    private(set) var hasMore: Bool = false
    private(set) var isInitialLoading: Bool = false
    private(set) var isLoadingMore: Bool = false
    private(set) var isSubmitting: Bool = false
    private(set) var isUploadingImage: Bool = false
    private(set) var errorMessage: String?
    private(set) var submissionMessage: String?
    private(set) var totalCount: Int64 = 0

    private var replyThreads: [String: ReplyThreadState] = [:]
    private var pendingLikeCommentIds: Set<String> = []

    init(totalCount: Int64 = 0) {
        self.totalCount = max(totalCount, 0)
    }

    var shouldShowEmptyState: Bool {
        !isInitialLoading && items.isEmpty && errorMessage == nil
    }

    var canSubmit: Bool {
        !isSubmitting && !isUploadingImage && validationMessage(for: draft, draftImage: draftImage) == nil
    }

    var composerPlaceholder: String {
        if let replyTarget {
            return "回复 @\(replyTarget.nickname)"
        }
        return "发条评论，说说你的感受"
    }

    var displayedCommentCount: Int64 {
        max(totalCount, Int64(items.count))
    }

    mutating func updateDraft(_ rawDraft: String) {
        draft = rawDraft
        submissionMessage = nil
    }

    mutating func setReplyTarget(_ comment: ShortVideoCommentData) {
        let nickname = comment.user.nickname.trimmingCharacters(in: .whitespacesAndNewlines)
        replyTarget = ReplyTarget(
            commentId: comment.commentId,
            rootCommentId: comment.rootThreadCommentId,
            nickname: nickname.isEmpty ? "用户" : nickname
        )
        submissionMessage = nil
    }

    mutating func clearReplyTarget() {
        replyTarget = nil
    }

    mutating func beginImageUpload() {
        isUploadingImage = true
        submissionMessage = nil
    }

    mutating func applyImageUploadSuccess(mediaId: String,
                                          previewData: Data,
                                          fileName: String,
                                          mimeType: String,
                                          imageURLString: String?) {
        draftImage = DraftImage(
            mediaId: mediaId.trimmingCharacters(in: .whitespacesAndNewlines),
            previewData: previewData,
            fileName: fileName.trimmingCharacters(in: .whitespacesAndNewlines),
            mimeType: mimeType.trimmingCharacters(in: .whitespacesAndNewlines),
            imageURLString: imageURLString?.trimmingCharacters(in: .whitespacesAndNewlines)
        )
        isUploadingImage = false
        submissionMessage = nil
    }

    mutating func applyImageUploadFailure(_ message: String?) {
        isUploadingImage = false
        submissionMessage = normalizedSubmitMessage(message, fallback: "图片上传失败，请稍后重试")
    }

    mutating func removeDraftImage() {
        draftImage = nil
    }

    mutating func beginInitialLoad() {
        isInitialLoading = true
        isLoadingMore = false
        errorMessage = nil
    }

    mutating func beginLoadMoreIfNeeded() -> String? {
        guard hasMore, !isInitialLoading, !isLoadingMore else {
            return nil
        }
        isLoadingMore = true
        errorMessage = nil
        return nextCursor
    }

    mutating func applyPage(_ page: ShortVideoCommentPageData, isInitial: Bool) {
        if isInitial {
            items = page.items
        } else {
            mergeComments(page.items)
        }
        nextCursor = page.nextCursor
        hasMore = page.hasMore
        isInitialLoading = false
        isLoadingMore = false
        errorMessage = nil
    }

    mutating func applyLoadFailure(_ message: String?, isInitial: Bool) {
        isInitialLoading = false
        isLoadingMore = false
        errorMessage = normalizedLoadMessage(message)
        if isInitial {
            items = []
            nextCursor = nil
            hasMore = false
        }
    }

    mutating func toggleReplies(for rootComment: ShortVideoCommentData) -> ReplyLoadRequest? {
        guard rootComment.replyCount > 0 else {
            return nil
        }
        let rootCommentId = rootComment.commentId
        var thread = replyThreads[rootCommentId] ?? seedReplyThread(from: rootComment)
        if thread.isExpanded {
            thread.isExpanded = false
            replyThreads[rootCommentId] = thread
            return nil
        }

        thread.isExpanded = true
        if !thread.hasLoadedInitialPage {
            thread.isLoading = true
            replyThreads[rootCommentId] = thread
            return ReplyLoadRequest(rootCommentId: rootCommentId, cursor: nil, isInitial: true)
        }

        replyThreads[rootCommentId] = thread
        return nil
    }

    mutating func beginLoadMoreRepliesIfNeeded(for rootCommentId: String) -> ReplyLoadRequest? {
        guard var thread = replyThreads[rootCommentId],
              thread.isExpanded,
              thread.hasMore,
              !thread.isLoading else {
            return nil
        }
        thread.isLoading = true
        replyThreads[rootCommentId] = thread
        return ReplyLoadRequest(rootCommentId: rootCommentId, cursor: thread.nextCursor, isInitial: false)
    }

    mutating func applyReplyPage(rootCommentId: String, page: ShortVideoCommentPageData, isInitial: Bool) {
        var thread = replyThreads[rootCommentId] ?? ReplyThreadState(
            isExpanded: true,
            items: [],
            nextCursor: nil,
            hasMore: false,
            isLoading: false,
            hasLoadedInitialPage: false
        )
        thread.isExpanded = true
        thread.isLoading = false
        thread.hasLoadedInitialPage = true
        if isInitial {
            thread.items = deduplicatedComments(page.items)
        } else {
            thread.items = deduplicatedComments(thread.items + page.items)
        }
        thread.nextCursor = page.nextCursor
        thread.hasMore = page.hasMore
        replyThreads[rootCommentId] = thread
    }

    mutating func applyReplyLoadFailure(rootCommentId: String, message: String?) {
        if var thread = replyThreads[rootCommentId] {
            thread.isLoading = false
            replyThreads[rootCommentId] = thread
        }
        submissionMessage = normalizedSubmitMessage(message, fallback: "加载回复失败，请稍后重试")
    }

    mutating func beginToggleLike(for commentId: String) -> LikeRequest? {
        guard !pendingLikeCommentIds.contains(commentId),
              let current = comment(withId: commentId) else {
            return nil
        }
        pendingLikeCommentIds.insert(commentId)
        submissionMessage = nil
        return LikeRequest(commentId: commentId, targetState: !current.liked)
    }

    mutating func applyLikeSuccess(_ payload: ShortVideoCommentLikeData) {
        pendingLikeCommentIds.remove(payload.commentId)
        updateComment(withId: payload.commentId) { comment in
            ShortVideoCommentData(
                commentId: comment.commentId,
                videoId: comment.videoId,
                parentCommentId: comment.parentCommentId,
                rootCommentId: comment.rootCommentId,
                user: comment.user,
                content: comment.content,
                imageUrl: comment.imageUrl,
                liked: payload.liked,
                likeCount: payload.likeCount,
                replyCount: comment.replyCount,
                previewReplies: comment.previewReplies,
                createdAt: comment.createdAt
            )
        }
    }

    mutating func applyLikeFailure(commentId: String, message: String?) {
        pendingLikeCommentIds.remove(commentId)
        submissionMessage = normalizedSubmitMessage(message, fallback: "点赞失败，请稍后重试")
    }

    func isLikePending(for commentId: String) -> Bool {
        pendingLikeCommentIds.contains(commentId)
    }

    func isRepliesExpanded(for rootCommentId: String) -> Bool {
        replyThreads[rootCommentId]?.isExpanded ?? false
    }

    func isRepliesLoading(for rootCommentId: String) -> Bool {
        replyThreads[rootCommentId]?.isLoading ?? false
    }

    func canLoadMoreReplies(for rootCommentId: String) -> Bool {
        guard let thread = replyThreads[rootCommentId] else {
            return false
        }
        return thread.isExpanded && thread.hasMore && !thread.isLoading
    }

    func visibleReplies(for rootComment: ShortVideoCommentData) -> [ShortVideoCommentData] {
        guard let thread = replyThreads[rootComment.commentId], thread.isExpanded else {
            return []
        }
        if !thread.items.isEmpty {
            return thread.items
        }
        return rootComment.previewReplies
    }

    mutating func beginSubmit() -> SubmitPayload? {
        guard !isSubmitting else {
            return nil
        }
        if let message = validationMessage(for: draft, draftImage: draftImage) {
            submissionMessage = message
            return nil
        }
        isSubmitting = true
        submissionMessage = nil
        let trimmedContent = draft.trimmingCharacters(in: .whitespacesAndNewlines)
        return SubmitPayload(
            content: trimmedContent.isEmpty ? nil : trimmedContent,
            parentCommentId: replyTarget?.commentId,
            imageMediaId: draftImage?.mediaId
        )
    }

    mutating func applySubmitSuccess(_ comment: ShortVideoCommentData) {
        isSubmitting = false
        draft = ""
        draftImage = nil
        replyTarget = nil
        submissionMessage = nil
        errorMessage = nil
        totalCount += 1

        if comment.isReply {
            let rootCommentId = comment.rootThreadCommentId
            var thread = replyThreads[rootCommentId] ?? ReplyThreadState(
                isExpanded: true,
                items: [],
                nextCursor: nil,
                hasMore: false,
                isLoading: false,
                hasLoadedInitialPage: false
            )
            thread.isExpanded = true
            thread.isLoading = false
            thread.items = deduplicatedComments(thread.items + [comment])
            thread.hasLoadedInitialPage = true
            replyThreads[rootCommentId] = thread

            incrementReplyCount(for: rootCommentId)
            return
        }

        if let index = items.firstIndex(where: { $0.commentId == comment.commentId }) {
            items[index] = comment
        } else {
            items.insert(comment, at: 0)
        }
    }

    mutating func applySubmitFailure(_ message: String?) {
        isSubmitting = false
        submissionMessage = normalizedSubmitMessage(message)
    }

    private mutating func mergeComments(_ pageItems: [ShortVideoCommentData]) {
        items = deduplicatedComments(items + pageItems)
    }

    private func validationMessage(for rawDraft: String, draftImage: DraftImage?) -> String? {
        let trimmed = rawDraft.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.count > 500 {
            return "评论内容长度不能超过500"
        }
        if trimmed.isEmpty && draftImage == nil {
            return "评论内容不能为空"
        }
        return nil
    }

    private func normalizedLoadMessage(_ raw: String?) -> String {
        let trimmed = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? "加载评论失败，请稍后重试" : trimmed
    }

    private func normalizedSubmitMessage(_ raw: String?, fallback: String = "评论发布失败，请稍后重试") -> String {
        let trimmed = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? fallback : trimmed
    }

    private func seedReplyThread(from rootComment: ShortVideoCommentData) -> ReplyThreadState {
        let previewReplies = deduplicatedComments(rootComment.previewReplies)
        return ReplyThreadState(
            isExpanded: false,
            items: previewReplies,
            nextCursor: nil,
            hasMore: rootComment.replyCount > Int64(previewReplies.count),
            isLoading: false,
            hasLoadedInitialPage: false
        )
    }

    private func deduplicatedComments(_ comments: [ShortVideoCommentData]) -> [ShortVideoCommentData] {
        var result: [ShortVideoCommentData] = []
        var seen = Set<String>()
        for comment in comments where !seen.contains(comment.commentId) {
            result.append(comment)
            seen.insert(comment.commentId)
        }
        return result
    }

    private mutating func incrementReplyCount(for rootCommentId: String) {
        updateComment(withId: rootCommentId) { comment in
            ShortVideoCommentData(
                commentId: comment.commentId,
                videoId: comment.videoId,
                parentCommentId: comment.parentCommentId,
                rootCommentId: comment.rootCommentId,
                user: comment.user,
                content: comment.content,
                imageUrl: comment.imageUrl,
                liked: comment.liked,
                likeCount: comment.likeCount,
                replyCount: comment.replyCount + 1,
                previewReplies: comment.previewReplies,
                createdAt: comment.createdAt
            )
        }
    }

    private func comment(withId commentId: String) -> ShortVideoCommentData? {
        for comment in items {
            if let found = search(comment: comment, targetId: commentId) {
                return found
            }
        }
        for thread in replyThreads.values {
            if let found = thread.items.first(where: { $0.commentId == commentId }) {
                return found
            }
        }
        return nil
    }

    private func search(comment: ShortVideoCommentData, targetId: String) -> ShortVideoCommentData? {
        if comment.commentId == targetId {
            return comment
        }
        for reply in comment.previewReplies {
            if let found = search(comment: reply, targetId: targetId) {
                return found
            }
        }
        return nil
    }

    private mutating func updateComment(withId commentId: String,
                                        transform: (ShortVideoCommentData) -> ShortVideoCommentData) {
        items = items.map { update(comment: $0, targetId: commentId, transform: transform) }
        replyThreads = replyThreads.mapValues { thread in
            var updatedThread = thread
            updatedThread.items = thread.items.map { comment in
                comment.commentId == commentId ? transform(comment) : comment
            }
            return updatedThread
        }
    }

    private func update(comment: ShortVideoCommentData,
                        targetId: String,
                        transform: (ShortVideoCommentData) -> ShortVideoCommentData) -> ShortVideoCommentData {
        let updatedPreviewReplies = comment.previewReplies.map { reply in
            update(comment: reply, targetId: targetId, transform: transform)
        }
        let base = comment.commentId == targetId ? transform(comment) : comment
        return ShortVideoCommentData(
            commentId: base.commentId,
            videoId: base.videoId,
            parentCommentId: base.parentCommentId,
            rootCommentId: base.rootCommentId,
            user: base.user,
            content: base.content,
            imageUrl: base.imageUrl,
            liked: base.liked,
            likeCount: base.likeCount,
            replyCount: base.replyCount,
            previewReplies: updatedPreviewReplies,
            createdAt: base.createdAt
        )
    }
}
