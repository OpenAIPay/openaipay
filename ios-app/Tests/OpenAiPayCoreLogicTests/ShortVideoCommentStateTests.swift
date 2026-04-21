import XCTest
@testable import OpenAiPayCoreLogic

final class ShortVideoCommentStateTests: XCTestCase {

    func testCommentStateRejectsBlankDraft() {
        var state = ShortVideoCommentState()
        state.updateDraft("   ")

        let payload = state.beginSubmit()

        XCTAssertNil(payload)
        XCTAssertEqual(state.submissionMessage, "评论内容不能为空")
        XCTAssertFalse(state.isSubmitting)
    }

    func testCommentStateMergesPagesWithoutDuplicates() {
        var state = ShortVideoCommentState()
        state.beginInitialLoad()
        state.applyPage(
            ShortVideoCommentPageData(
                items: [makeComment("comment-1"), makeComment("comment-2")],
                nextCursor: "cursor-2",
                hasMore: true
            ),
            isInitial: true
        )

        XCTAssertEqual(state.beginLoadMoreIfNeeded(), "cursor-2")

        state.applyPage(
            ShortVideoCommentPageData(
                items: [makeComment("comment-2"), makeComment("comment-3")],
                nextCursor: nil,
                hasMore: false
            ),
            isInitial: false
        )

        XCTAssertEqual(state.items.map(\.commentId), ["comment-1", "comment-2", "comment-3"])
        XCTAssertFalse(state.hasMore)
    }

    func testCommentStatePrependsCommentOnSubmitSuccess() {
        var state = ShortVideoCommentState()
        state.updateDraft(" 新评论 ")

        let payload = state.beginSubmit()
        state.applySubmitSuccess(makeComment("comment-new", content: "新评论"))

        XCTAssertEqual(payload?.content, "新评论")
        XCTAssertNil(payload?.parentCommentId)
        XCTAssertNil(payload?.imageMediaId)
        XCTAssertEqual(state.items.first?.commentId, "comment-new")
        XCTAssertEqual(state.draft, "")
        XCTAssertNil(state.submissionMessage)
        XCTAssertFalse(state.isSubmitting)
    }

    func testCommentStateShowsFailureFeedbackOnSubmitError() {
        var state = ShortVideoCommentState()
        state.updateDraft("评论失败")

        _ = state.beginSubmit()
        state.applySubmitFailure("发布失败，请稍后重试")

        XCTAssertEqual(state.submissionMessage, "发布失败，请稍后重试")
        XCTAssertFalse(state.isSubmitting)
        XCTAssertEqual(state.draft, "评论失败")
    }

    func testCommentStateAllowsImageOnlySubmission() {
        var state = ShortVideoCommentState()
        state.beginImageUpload()
        state.applyImageUploadSuccess(
            mediaId: "MED-COMMENT-001",
            previewData: Data([0x01, 0x02]),
            fileName: "comment.jpg",
            mimeType: "image/jpeg",
            imageURLString: "http://127.0.0.1:8080/media/comment.jpg"
        )

        let payload = state.beginSubmit()

        XCTAssertEqual(payload?.imageMediaId, "MED-COMMENT-001")
        XCTAssertNil(payload?.content)
        XCTAssertTrue(state.isSubmitting)
    }

    func testCommentStateExpandsRepliesAndAppliesLoadedThread() {
        var state = ShortVideoCommentState()
        let previewReplies = [
            makeReply("reply-preview-1", content: "预览回复 1"),
            makeReply("reply-preview-2", content: "预览回复 2")
        ]
        let rootComment = makeComment(
            "comment-root",
            content: "根评论",
            replyCount: 3,
            previewReplies: previewReplies
        )

        state.beginInitialLoad()
        state.applyPage(
            ShortVideoCommentPageData(items: [rootComment], nextCursor: nil, hasMore: false),
            isInitial: true
        )

        let request = state.toggleReplies(for: rootComment)

        XCTAssertEqual(
            request,
            ShortVideoCommentState.ReplyLoadRequest(
                rootCommentId: "comment-root",
                cursor: nil,
                isInitial: true
            )
        )
        XCTAssertTrue(state.isRepliesExpanded(for: "comment-root"))
        XCTAssertTrue(state.isRepliesLoading(for: "comment-root"))
        XCTAssertEqual(state.visibleReplies(for: rootComment).map(\.commentId), ["reply-preview-1", "reply-preview-2"])

        state.applyReplyPage(
            rootCommentId: "comment-root",
            page: ShortVideoCommentPageData(
                items: previewReplies + [makeReply("reply-preview-3", content: "预览回复 3")],
                nextCursor: nil,
                hasMore: false
            ),
            isInitial: true
        )

        XCTAssertFalse(state.isRepliesLoading(for: "comment-root"))
        XCTAssertFalse(state.canLoadMoreReplies(for: "comment-root"))
        XCTAssertEqual(
            state.visibleReplies(for: rootComment).map(\.commentId),
            ["reply-preview-1", "reply-preview-2", "reply-preview-3"]
        )
    }

    func testCommentStateAppliesLikeSuccessToExpandedReply() {
        var state = ShortVideoCommentState()
        let reply = makeReply("reply-liked", content: "回复内容")
        let rootComment = makeComment(
            "comment-root",
            content: "根评论",
            replyCount: 1,
            previewReplies: [reply]
        )

        state.beginInitialLoad()
        state.applyPage(
            ShortVideoCommentPageData(items: [rootComment], nextCursor: nil, hasMore: false),
            isInitial: true
        )
        _ = state.toggleReplies(for: rootComment)

        let request = state.beginToggleLike(for: "reply-liked")

        XCTAssertEqual(
            request,
            ShortVideoCommentState.LikeRequest(commentId: "reply-liked", targetState: true)
        )
        XCTAssertTrue(state.isLikePending(for: "reply-liked"))

        state.applyLikeSuccess(
            ShortVideoCommentLikeData(commentId: "reply-liked", liked: true, likeCount: 5)
        )

        XCTAssertFalse(state.isLikePending(for: "reply-liked"))
        XCTAssertEqual(state.visibleReplies(for: rootComment).first?.commentId, "reply-liked")
        XCTAssertEqual(state.visibleReplies(for: rootComment).first?.liked, true)
        XCTAssertEqual(state.visibleReplies(for: rootComment).first?.likeCount, 5)
    }

    private func makeComment(
        _ commentId: String,
        content: String = "测试评论",
        replyCount: Int64 = 0,
        previewReplies: [ShortVideoCommentData] = []
    ) -> ShortVideoCommentData {
        ShortVideoCommentData(
            commentId: commentId,
            videoId: "video-1",
            user: ShortVideoAuthorData(
                userId: 880100068483692100,
                nickname: "顾郡",
                avatarUrl: "/api/media/avatar/content"
            ),
            content: content,
            replyCount: replyCount,
            previewReplies: previewReplies,
            createdAt: "2026-03-31T16:30:00"
        )
    }

    private func makeReply(_ commentId: String, content: String = "测试回复") -> ShortVideoCommentData {
        ShortVideoCommentData(
            commentId: commentId,
            videoId: "video-1",
            parentCommentId: "comment-root",
            rootCommentId: "comment-root",
            user: ShortVideoAuthorData(
                userId: 880902068943900002,
                nickname: "祁欣",
                avatarUrl: "/api/media/avatar/content"
            ),
            content: content,
            createdAt: "2026-04-03T10:01:00"
        )
    }
}
