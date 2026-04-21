import Foundation

protocol ShortVideoCommentClient {
    func fetchShortVideoComments(videoId: String, cursor: String?, limit: Int) async throws -> ShortVideoCommentPageData
    func fetchShortVideoCommentReplies(commentId: String, cursor: String?, limit: Int) async throws -> ShortVideoCommentPageData
    func createShortVideoComment(
        videoId: String,
        content: String?,
        parentCommentId: String?,
        imageMediaId: String?
    ) async throws -> ShortVideoCommentData
    func updateShortVideoCommentLike(commentId: String, isLiked: Bool) async throws -> ShortVideoCommentLikeData
    func uploadShortVideoCommentImage(imageData: Data, fileName: String, mimeType: String) async throws -> MediaAssetData
}

extension APIClient: ShortVideoCommentClient {}

@MainActor
final class ShortVideoCommentSheetViewModel: ObservableObject {
    @Published private(set) var state: ShortVideoCommentState

    private let videoId: String
    private let client: any ShortVideoCommentClient
    private let pageSize: Int
    private let onCommentCreated: ((ShortVideoComment) -> Void)?
    private var initialLoadTask: Task<Void, Never>?
    private var loadMoreTask: Task<Void, Never>?
    private var submitTask: Task<Void, Never>?
    private var imageUploadTask: Task<Void, Never>?
    private var replyTasks: [String: Task<Void, Never>] = [:]
    private var likeTasks: [String: Task<Void, Never>] = [:]

    init(videoId: String,
         totalCommentCount: Int64 = 0,
         client: any ShortVideoCommentClient = APIClient.shared,
         pageSize: Int = 20,
         onCommentCreated: ((ShortVideoComment) -> Void)? = nil) {
        self.videoId = videoId
        self.client = client
        self.pageSize = max(1, min(pageSize, 50))
        self.onCommentCreated = onCommentCreated
        self.state = ShortVideoCommentState(totalCount: totalCommentCount)
    }

    deinit {
        initialLoadTask?.cancel()
        loadMoreTask?.cancel()
        submitTask?.cancel()
        imageUploadTask?.cancel()
        replyTasks.values.forEach { $0.cancel() }
        likeTasks.values.forEach { $0.cancel() }
    }

    func loadIfNeeded() {
        guard state.items.isEmpty else {
            loadMoreIfNeeded()
            return
        }
        guard !state.isInitialLoading else {
            return
        }
        loadInitialComments()
    }

    func retry() {
        if state.items.isEmpty {
            loadInitialComments()
        } else {
            loadMoreIfNeeded()
        }
    }

    func loadMoreIfNeeded() {
        guard loadMoreTask == nil else {
            return
        }
        guard let cursor = state.beginLoadMoreIfNeeded() else {
            return
        }
        loadMoreTask = Task { @MainActor [weak self] in
            guard let self else {
                return
            }
            do {
                let page = try await client.fetchShortVideoComments(videoId: videoId, cursor: cursor, limit: pageSize)
                guard !Task.isCancelled else {
                    loadMoreTask = nil
                    return
                }
                state.applyPage(page, isInitial: false)
                loadMoreTask = nil
            } catch {
                guard !Task.isCancelled else {
                    loadMoreTask = nil
                    return
                }
                state.applyLoadFailure(userFacingErrorMessage(error), isInitial: false)
                loadMoreTask = nil
            }
        }
    }

    func updateDraft(_ draft: String) {
        state.updateDraft(draft)
    }

    func startReply(to comment: ShortVideoComment) {
        state.setReplyTarget(comment)
    }

    func cancelReplying() {
        state.clearReplyTarget()
    }

    func toggleReplies(for rootComment: ShortVideoComment) {
        guard let request = state.toggleReplies(for: rootComment) else {
            return
        }
        loadReplies(request)
    }

    func loadMoreRepliesIfNeeded(for rootCommentId: String) {
        guard let request = state.beginLoadMoreRepliesIfNeeded(for: rootCommentId) else {
            return
        }
        loadReplies(request)
    }

    func toggleLike(commentId: String) {
        guard likeTasks[commentId] == nil,
              let request = state.beginToggleLike(for: commentId) else {
            return
        }
        likeTasks[commentId] = Task { @MainActor [weak self] in
            guard let self else {
                return
            }
            do {
                let payload = try await client.updateShortVideoCommentLike(
                    commentId: request.commentId,
                    isLiked: request.targetState
                )
                guard !Task.isCancelled else {
                    likeTasks.removeValue(forKey: commentId)
                    return
                }
                state.applyLikeSuccess(payload)
                likeTasks.removeValue(forKey: commentId)
            } catch {
                guard !Task.isCancelled else {
                    likeTasks.removeValue(forKey: commentId)
                    return
                }
                state.applyLikeFailure(commentId: commentId, message: userFacingErrorMessage(error))
                likeTasks.removeValue(forKey: commentId)
            }
        }
    }

    func uploadDraftImage(uploadData: Data, previewData: Data, fileName: String, mimeType: String) {
        guard imageUploadTask == nil else {
            return
        }
        state.beginImageUpload()
        imageUploadTask = Task { @MainActor [weak self] in
            guard let self else {
                return
            }
            do {
                let uploaded = try await client.uploadShortVideoCommentImage(
                    imageData: uploadData,
                    fileName: fileName,
                    mimeType: mimeType
                )
                guard !Task.isCancelled else {
                    imageUploadTask = nil
                    return
                }
                state.applyImageUploadSuccess(
                    mediaId: uploaded.mediaId,
                    previewData: previewData,
                    fileName: fileName,
                    mimeType: mimeType,
                    imageURLString: uploaded.contentUrl
                )
                imageUploadTask = nil
            } catch {
                guard !Task.isCancelled else {
                    imageUploadTask = nil
                    return
                }
                state.applyImageUploadFailure(userFacingErrorMessage(error))
                imageUploadTask = nil
            }
        }
    }

    func removeDraftImage() {
        state.removeDraftImage()
    }

    func submitComment() {
        guard submitTask == nil else {
            return
        }
        guard let payload = state.beginSubmit() else {
            return
        }
        submitTask = Task { @MainActor [weak self] in
            guard let self else {
                return
            }
            do {
                let comment = try await client.createShortVideoComment(
                    videoId: videoId,
                    content: payload.content,
                    parentCommentId: payload.parentCommentId,
                    imageMediaId: payload.imageMediaId
                )
                guard !Task.isCancelled else {
                    submitTask = nil
                    return
                }
                state.applySubmitSuccess(comment)
                onCommentCreated?(comment)
                submitTask = nil
            } catch {
                guard !Task.isCancelled else {
                    submitTask = nil
                    return
                }
                state.applySubmitFailure(userFacingErrorMessage(error))
                submitTask = nil
            }
        }
    }

    private func loadInitialComments() {
        initialLoadTask?.cancel()
        state.beginInitialLoad()
        initialLoadTask = Task { @MainActor [weak self] in
            guard let self else {
                return
            }
            do {
                let page = try await client.fetchShortVideoComments(videoId: videoId, cursor: nil, limit: pageSize)
                guard !Task.isCancelled else {
                    initialLoadTask = nil
                    return
                }
                state.applyPage(page, isInitial: true)
                initialLoadTask = nil
            } catch {
                guard !Task.isCancelled else {
                    initialLoadTask = nil
                    return
                }
                state.applyLoadFailure(userFacingErrorMessage(error), isInitial: true)
                initialLoadTask = nil
            }
        }
    }

    private func loadReplies(_ request: ShortVideoCommentState.ReplyLoadRequest) {
        guard replyTasks[request.rootCommentId] == nil else {
            return
        }
        replyTasks[request.rootCommentId] = Task { @MainActor [weak self] in
            guard let self else {
                return
            }
            do {
                let page = try await client.fetchShortVideoCommentReplies(
                    commentId: request.rootCommentId,
                    cursor: request.cursor,
                    limit: pageSize
                )
                guard !Task.isCancelled else {
                    replyTasks.removeValue(forKey: request.rootCommentId)
                    return
                }
                state.applyReplyPage(rootCommentId: request.rootCommentId, page: page, isInitial: request.isInitial)
                replyTasks.removeValue(forKey: request.rootCommentId)
            } catch {
                guard !Task.isCancelled else {
                    replyTasks.removeValue(forKey: request.rootCommentId)
                    return
                }
                state.applyReplyLoadFailure(
                    rootCommentId: request.rootCommentId,
                    message: userFacingErrorMessage(error)
                )
                replyTasks.removeValue(forKey: request.rootCommentId)
            }
        }
    }
}
