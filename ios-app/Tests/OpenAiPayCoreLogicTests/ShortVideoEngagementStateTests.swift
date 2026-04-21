import XCTest
@testable import OpenAiPayCoreLogic

final class ShortVideoEngagementStateTests: XCTestCase {

    func testToggleLikeAppliesOptimisticStateAndReturnsRequest() {
        var state = makeLoadedState()

        let request = state.toggleLike(for: "video-1")

        XCTAssertEqual(request?.videoId, "video-1")
        XCTAssertEqual(request?.kind, .like)
        XCTAssertEqual(request?.targetState, true)
        XCTAssertTrue(state.currentItem?.engagement.liked == true)
        XCTAssertEqual(state.currentItem?.engagement.likeCount, 11)
        XCTAssertTrue(state.isLikeRequestInFlight(for: "video-1"))
    }

    func testRepeatedLikeClicksQueueLatestIntentAndEmitFollowUpAfterSuccess() {
        var state = makeLoadedState()

        let firstRequest = state.toggleLike(for: "video-1")
        let secondRequest = state.toggleLike(for: "video-1")

        XCTAssertNotNil(firstRequest)
        XCTAssertNil(secondRequest)
        XCTAssertFalse(state.currentItem?.engagement.liked == true)
        XCTAssertEqual(state.currentItem?.engagement.likeCount, 10)

        let followUp = state.applyEngagementSuccess(
            ShortVideoEngagementData(
                liked: true,
                favorited: false,
                likeCount: 11,
                favoriteCount: 3,
                commentCount: 1
            ),
            for: firstRequest!
        )

        XCTAssertEqual(followUp?.kind, .like)
        XCTAssertEqual(followUp?.targetState, false)
        XCTAssertFalse(state.currentItem?.engagement.liked == true)
        XCTAssertEqual(state.currentItem?.engagement.likeCount, 10)
        XCTAssertTrue(state.isLikeRequestInFlight(for: "video-1"))
    }

    func testFavoriteFailureRollsBackAndShowsFeedback() {
        var state = makeLoadedState()

        let request = state.toggleFavorite(for: "video-1")
        let followUp = state.applyEngagementFailure(for: request!, message: "收藏失败，请稍后再试")

        XCTAssertNil(followUp)
        XCTAssertFalse(state.currentItem?.engagement.favorited == true)
        XCTAssertEqual(state.currentItem?.engagement.favoriteCount, 3)
        XCTAssertEqual(state.feedbackMessage, "收藏失败，请稍后再试")
        XCTAssertFalse(state.isFavoriteRequestInFlight(for: "video-1"))
    }

    func testLikeSuccessPreservesFavoriteOptimisticState() {
        var state = makeLoadedState()

        let likeRequest = state.toggleLike(for: "video-1")
        let favoriteRequest = state.toggleFavorite(for: "video-1")

        XCTAssertNotNil(likeRequest)
        XCTAssertNotNil(favoriteRequest)
        XCTAssertTrue(state.currentItem?.engagement.favorited == true)
        XCTAssertEqual(state.currentItem?.engagement.favoriteCount, 4)

        let followUp = state.applyEngagementSuccess(
            ShortVideoEngagementData(
                liked: true,
                favorited: false,
                likeCount: 11,
                favoriteCount: 3,
                commentCount: 1
            ),
            for: likeRequest!
        )

        XCTAssertNil(followUp)
        XCTAssertTrue(state.currentItem?.engagement.liked == true)
        XCTAssertTrue(state.currentItem?.engagement.favorited == true)
        XCTAssertEqual(state.currentItem?.engagement.favoriteCount, 4)
        XCTAssertTrue(state.isFavoriteRequestInFlight(for: "video-1"))
    }

    private func makeLoadedState() -> ShortVideoFeedState {
        var state = ShortVideoFeedState()
        state.beginInitialLoad()
        state.applyPage(
            ShortVideoFeedPageData(
                items: [
                    ShortVideoFeedItemData(
                        videoId: "video-1",
                        caption: "测试视频",
                        author: ShortVideoAuthorData(
                            userId: 880100068483692100,
                            nickname: "顾郡",
                            avatarUrl: nil
                        ),
                        coverUrl: "/api/media/video-1/cover",
                        playback: ShortVideoPlaybackInfoData(
                            playbackUrl: "/api/media/video-1/playback",
                            playbackProtocol: "MP4",
                            mimeType: "video/mp4",
                            durationMs: 15_000,
                            width: 720,
                            height: 1280
                        ),
                        engagement: ShortVideoEngagementData(
                            liked: false,
                            favorited: false,
                            likeCount: 10,
                            favoriteCount: 3,
                            commentCount: 1
                        )
                    )
                ],
                nextCursor: nil,
                hasMore: false
            ),
            isInitial: true
        )
        return state
    }
}
