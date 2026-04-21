import Foundation
import CoreGraphics

typealias ShortVideoFeedItem = ShortVideoFeedItemData
typealias ShortVideoFeedPage = ShortVideoFeedPageData
typealias ShortVideoAuthor = ShortVideoAuthorData
typealias ShortVideoPlaybackInfo = ShortVideoPlaybackInfoData
typealias ShortVideoEngagement = ShortVideoEngagementData

extension ShortVideoFeedItemData {
    var playbackURL: URL? {
        URL(string: playback.playbackUrl)
    }

    var coverImageURL: URL? {
        URL(string: coverUrl)
    }

    var authorAvatarURL: URL? {
        guard let avatarUrl = author.avatarUrl else {
            return nil
        }
        return URL(string: avatarUrl)
    }

    var displayCaption: String? {
        let trimmed = caption.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.isEmpty ? nil : trimmed
    }

    var displayAuthorLine: String {
        "@\(author.nickname)"
    }

    var accessibilitySummary: String {
        [author.nickname, displayCaption].compactMap { $0 }.joined(separator: "，")
    }

    var playbackAspectRatio: CGFloat? {
        guard let width = playback.width,
              let height = playback.height,
              width > 0,
              height > 0 else {
            return nil
        }
        return CGFloat(width) / CGFloat(height)
    }

    var isLandscapePlayback: Bool {
        guard let playbackAspectRatio else {
            return false
        }
        return playbackAspectRatio > 1.05
    }
}
