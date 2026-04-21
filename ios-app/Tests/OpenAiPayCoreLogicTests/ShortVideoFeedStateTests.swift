import XCTest
@testable import OpenAiPayCoreLogic

final class ShortVideoFeedStateTests: XCTestCase {

    func testShortVideoFeedPageDecodesAndNormalizesRelativeMediaURLs() throws {
        let data = """
        {
          "items": [
            {
              "videoId": "SVP202603310001",
              "caption": "测试视频",
              "author": {
                "userId": "880100068483692100",
                "nickname": "顾郡",
                "avatarUrl": "/api/media/avatar-1/content"
              },
              "coverUrl": "/api/media/cover-1/content",
              "playback": {
                "playbackUrl": "/api/media/play-1/content",
                "protocol": "mp4",
                "mimeType": "video/mp4",
                "durationMs": "15000",
                "width": 720,
                "height": 1280
              },
              "engagement": {
                "liked": true,
                "favorited": false,
                "likeCount": 10,
                "favoriteCount": 3,
                "commentCount": 1
              }
            }
          ],
          "nextCursor": "cursor-1",
          "hasMore": true
        }
        """.data(using: .utf8)!

        let page = try JSONDecoder().decode(ShortVideoFeedPageData.self, from: data)

        XCTAssertEqual(page.items.count, 1)
        XCTAssertEqual(page.items[0].coverUrl, "http://127.0.0.1:8080/api/media/cover-1/content")
        XCTAssertEqual(page.items[0].playback.playbackUrl, "http://127.0.0.1:8080/api/media/play-1/content")
        XCTAssertEqual(page.items[0].playback.playbackProtocol, "MP4")
        XCTAssertEqual(page.items[0].author.avatarUrl, "http://127.0.0.1:8080/api/media/avatar-1/content")
        XCTAssertEqual(page.nextCursor, "cursor-1")
        XCTAssertTrue(page.hasMore)
    }

    func testStateAdvancesCursorAndTriggersPrefetchNearTail() {
        var state = ShortVideoFeedState()
        state.beginInitialLoad()
        state.applyPage(
            ShortVideoFeedPageData(
                items: [
                    makeItem(videoId: "video-1"),
                    makeItem(videoId: "video-2"),
                    makeItem(videoId: "video-3")
                ],
                nextCursor: "cursor-3",
                hasMore: true
            ),
            isInitial: true
        )

        XCTAssertEqual(state.currentItem?.videoId, "video-1")
        XCTAssertFalse(state.shouldPrefetch)
        XCTAssertTrue(state.advanceToNextItem())
        XCTAssertEqual(state.currentItem?.videoId, "video-2")
        XCTAssertTrue(state.shouldPrefetch)
        XCTAssertEqual(state.beginLoadMoreIfNeeded(), "cursor-3")

        state.applyPage(
            ShortVideoFeedPageData(
                items: [
                    makeItem(videoId: "video-3"),
                    makeItem(videoId: "video-4")
                ],
                nextCursor: nil,
                hasMore: false
            ),
            isInitial: false
        )

        XCTAssertEqual(state.items.map(\.videoId), ["video-1", "video-2", "video-3", "video-4"])
        XCTAssertFalse(state.hasMore)
        XCTAssertFalse(state.isLoadingMore)
    }

    func testStateKeepsItemsOnLoadMoreFailureAndAllowsRecovery() {
        var state = ShortVideoFeedState()
        state.beginInitialLoad()
        state.applyFailure(nil, isInitial: true)

        XCTAssertEqual(state.errorMessage, "加载视频失败，请稍后重试")

        state.beginInitialLoad()
        state.applyPage(
            ShortVideoFeedPageData(
                items: [makeItem(videoId: "video-1")],
                nextCursor: "cursor-1",
                hasMore: true
            ),
            isInitial: true
        )

        XCTAssertNil(state.errorMessage)
        XCTAssertEqual(state.currentItem?.videoId, "video-1")
        XCTAssertEqual(state.beginLoadMoreIfNeeded(), "cursor-1")

        state.applyFailure("网络异常", isInitial: false)

        XCTAssertEqual(state.errorMessage, "网络异常")
        XCTAssertEqual(state.items.count, 1)
        XCTAssertEqual(state.currentItem?.videoId, "video-1")
        XCTAssertFalse(state.isLoadingMore)
    }

    private func makeItem(videoId: String) -> ShortVideoFeedItemData {
        ShortVideoFeedItemData(
            videoId: videoId,
            caption: "测试\(videoId)",
            author: ShortVideoAuthorData(
                userId: 880100068483692100,
                nickname: "顾郡",
                avatarUrl: "/api/media/avatar/content"
            ),
            coverUrl: "/api/media/\(videoId)/cover",
            playback: ShortVideoPlaybackInfoData(
                playbackUrl: "/api/media/\(videoId)/playback",
                playbackProtocol: "MP4",
                mimeType: "video/mp4",
                durationMs: 15000,
                width: 720,
                height: 1280
            ),
            engagement: ShortVideoEngagementData(
                liked: false,
                favorited: false,
                likeCount: 0,
                favoriteCount: 0,
                commentCount: 0
            )
        )
    }
}
