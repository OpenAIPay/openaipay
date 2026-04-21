import Foundation
import SwiftUI

protocol ShortVideoFeedLoadingClient {
    func fetchShortVideoFeed(cursor: String?, limit: Int) async throws -> ShortVideoFeedPageData
}

protocol ShortVideoFeedEngagementClient {
    func updateShortVideoLike(videoId: String, isLiked: Bool) async throws -> ShortVideoEngagementData
    func updateShortVideoFavorite(videoId: String, isFavorited: Bool) async throws -> ShortVideoEngagementData
}

protocol ShortVideoFeedClient: ShortVideoFeedLoadingClient, ShortVideoFeedEngagementClient {}

extension APIClient: ShortVideoFeedClient {}

@MainActor
final class ShortVideoFeedViewModel: ObservableObject {
    private struct EngagementTaskKey: Hashable {
        let videoId: String
        let kind: ShortVideoFeedState.EngagementRequest.Kind
    }

    @Published private(set) var state = ShortVideoFeedState()

    private let client: any ShortVideoFeedClient
    private let pageSize: Int
    private var initialLoadTask: Task<Void, Never>?
    private var loadMoreTask: Task<Void, Never>?
    private var engagementTasks: [EngagementTaskKey: Task<Void, Never>] = [:]

    init(client: any ShortVideoFeedClient = APIClient.shared, pageSize: Int = 3) {
        self.client = client
        self.pageSize = max(1, min(pageSize, 10))
    }

    deinit {
        initialLoadTask?.cancel()
        loadMoreTask?.cancel()
        engagementTasks.values.forEach { $0.cancel() }
    }

    func loadIfNeeded() {
        guard state.items.isEmpty else {
            loadMoreIfNeeded()
            return
        }
        guard !state.isInitialLoading else {
            return
        }
        loadInitialFeed()
    }

    func retry() {
        if state.items.isEmpty {
            loadInitialFeed()
            return
        }
        loadMoreIfNeeded()
    }

    func refresh() {
        initialLoadTask?.cancel()
        loadMoreTask?.cancel()
        engagementTasks.values.forEach { $0.cancel() }
        engagementTasks.removeAll()
        state = ShortVideoFeedState()
        loadInitialFeed()
    }

    func handleVerticalSwipe(_ translationHeight: CGFloat) {
        if translationHeight <= -72 {
            showNextVideo()
        } else if translationHeight >= 72 {
            showPreviousVideo()
        }
    }

    func showNextVideo() {
        if state.advanceToNextItem() {
            loadMoreIfNeeded()
            return
        }
        loadMoreIfNeeded()
    }

    func showPreviousVideo() {
        _ = state.retreatToPreviousItem()
    }

    func toggleLike(videoId: String) {
        guard let request = state.toggleLike(for: videoId) else {
            return
        }
        startEngagementTask(for: request)
    }

    func toggleFavorite(videoId: String) {
        guard let request = state.toggleFavorite(for: videoId) else {
            return
        }
        startEngagementTask(for: request)
    }

    func handleCommentCreated(_ comment: ShortVideoComment, for videoId: String) {
        _ = comment
        state.incrementCommentCount(for: videoId)
    }

    private func loadInitialFeed() {
        initialLoadTask?.cancel()
        state.beginInitialLoad()
        initialLoadTask = Task { @MainActor [weak self] in
            guard let self else {
                return
            }
            defer {
                initialLoadTask = nil
            }
            do {
                let page = try await client.fetchShortVideoFeed(cursor: nil, limit: pageSize)
                guard !Task.isCancelled else {
                    return
                }
                state.applyPage(page, isInitial: true)
                loadMoreIfNeeded()
            } catch {
                guard !Task.isCancelled else {
                    return
                }
                state.applyFailure(error.localizedDescription, isInitial: true)
            }
        }
    }

    private func loadMoreIfNeeded() {
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
            defer {
                loadMoreTask = nil
            }
            do {
                let page = try await client.fetchShortVideoFeed(cursor: cursor, limit: pageSize)
                guard !Task.isCancelled else {
                    return
                }
                state.applyPage(page, isInitial: false)
            } catch {
                guard !Task.isCancelled else {
                    return
                }
                state.applyFailure(error.localizedDescription, isInitial: false)
            }
        }
    }

    private func startEngagementTask(for request: ShortVideoFeedState.EngagementRequest) {
        let key = EngagementTaskKey(videoId: request.videoId, kind: request.kind)
        guard engagementTasks[key] == nil else {
            return
        }
        engagementTasks[key] = Task { @MainActor [weak self] in
            guard let self else {
                return
            }
            do {
                let engagement = try await performEngagementRequest(request)
                guard !Task.isCancelled else {
                    engagementTasks.removeValue(forKey: key)
                    return
                }
                engagementTasks.removeValue(forKey: key)
                if let nextRequest = state.applyEngagementSuccess(engagement, for: request) {
                    startEngagementTask(for: nextRequest)
                }
            } catch {
                guard !Task.isCancelled else {
                    engagementTasks.removeValue(forKey: key)
                    return
                }
                engagementTasks.removeValue(forKey: key)
                if let nextRequest = state.applyEngagementFailure(for: request, message: userFacingErrorMessage(error)) {
                    startEngagementTask(for: nextRequest)
                }
            }
        }
    }

    private func performEngagementRequest(_ request: ShortVideoFeedState.EngagementRequest) async throws -> ShortVideoEngagementData {
        switch request.kind {
        case .like:
            return try await client.updateShortVideoLike(videoId: request.videoId, isLiked: request.targetState)
        case .favorite:
            return try await client.updateShortVideoFavorite(videoId: request.videoId, isFavorited: request.targetState)
        }
    }
}
