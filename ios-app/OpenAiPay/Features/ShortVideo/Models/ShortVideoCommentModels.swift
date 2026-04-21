import Foundation

typealias ShortVideoComment = ShortVideoCommentData
typealias ShortVideoCommentPage = ShortVideoCommentPageData

extension ShortVideoCommentData {
    var displayAuthorLine: String {
        "@\(user.nickname)"
    }

    var rootThreadCommentId: String {
        rootCommentId ?? parentCommentId ?? commentId
    }

    var isReply: Bool {
        parentCommentId != nil
    }

    var hasRenderableText: Bool {
        !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    var displayCreatedAt: String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        let fallbackFormatter = ISO8601DateFormatter()
        fallbackFormatter.formatOptions = [.withInternetDateTime]
        let localFormatter = DateFormatter()
        localFormatter.locale = Locale(identifier: "en_US_POSIX")
        localFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"

        let resolvedDate = formatter.date(from: createdAt)
            ?? fallbackFormatter.date(from: createdAt)
            ?? localFormatter.date(from: createdAt)
        guard let resolvedDate else {
            return createdAt
        }

        let displayFormatter = DateFormatter()
        displayFormatter.locale = Locale(identifier: "zh_CN")
        displayFormatter.dateFormat = "MM-dd HH:mm"
        return displayFormatter.string(from: resolvedDate)
    }

    var avatarURL: URL? {
        guard let avatarUrl = user.avatarUrl else {
            return nil
        }
        return URL(string: avatarUrl)
    }

    var imageDisplayURL: URL? {
        guard let imageUrl else {
            return nil
        }
        return URL(string: imageUrl)
    }
}
