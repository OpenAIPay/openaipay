import SwiftUI

struct ShortVideoActionToolbar: View {
    enum FollowBadgeStyle {
        case plus
        case check
        case hidden
    }

    let avatarURL: URL?
    let coverURL: URL?
    let engagement: ShortVideoEngagement
    let followBadgeStyle: FollowBadgeStyle
    let isLikePending: Bool
    let isFavoritePending: Bool
    let onFollow: (() -> Void)?
    let onLike: () -> Void
    let onFavorite: () -> Void
    let onComment: () -> Void
    let onShare: () -> Void

    @State private var rotatesAlbum = false

    var body: some View {
        VStack(spacing: 18) {
            avatarFollowButton

            actionButton(
                icon: engagement.liked ? "heart.fill" : "heart",
                tint: engagement.liked ? Color(red: 1.0, green: 0.34, blue: 0.43) : .white,
                value: formattedCount(engagement.likeCount),
                isPending: isLikePending,
                action: onLike
            )

            actionButton(
                icon: "ellipsis.bubble.fill",
                tint: .white,
                value: formattedCount(engagement.commentCount),
                isPending: false,
                action: onComment
            )

            actionButton(
                icon: engagement.favorited ? "star.fill" : "star",
                tint: engagement.favorited ? Color(red: 1.0, green: 0.84, blue: 0.28) : .white,
                value: formattedCount(engagement.favoriteCount),
                isPending: isFavoritePending,
                action: onFavorite
            )

            actionButton(
                icon: "arrowshape.turn.up.right.fill",
                tint: .white,
                value: "分享",
                isPending: false,
                action: onShare
            )

            albumButton
        }
    }

    private func actionButton(icon: String,
                              tint: Color,
                              value: String,
                              isPending: Bool,
                              action: @escaping () -> Void) -> some View {
        Button(action: action) {
            VStack(spacing: 5) {
                if isPending {
                    ProgressView()
                        .tint(.white)
                        .frame(width: 44, height: 44)
                } else {
                    Image(systemName: icon)
                        .font(.system(size: 33, weight: .semibold))
                        .foregroundStyle(tint)
                        .shadow(color: .black.opacity(0.45), radius: 8, x: 0, y: 2)
                }

                Text(value)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundStyle(.white)
                    .shadow(color: .black.opacity(0.38), radius: 4, x: 0, y: 1)
            }
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder
    private var avatarFollowButton: some View {
        if followBadgeStyle == .plus, let onFollow {
            Button(action: onFollow) {
                avatarFollowButtonContent
            }
            .buttonStyle(.plain)
        } else {
            avatarFollowButtonContent
        }
    }

    private var avatarFollowButtonContent: some View {
        ZStack(alignment: .bottom) {
            AsyncImage(url: avatarURL) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .scaledToFill()
                default:
                    LinearGradient(
                        colors: [
                            Color.white.opacity(0.82),
                            Color.white.opacity(0.44)
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                }
            }
            .frame(width: 52, height: 52)
            .clipShape(Circle())
            .overlay(
                Circle()
                    .stroke(Color.white.opacity(0.88), lineWidth: 2)
            )
            .shadow(color: .black.opacity(0.35), radius: 8, x: 0, y: 2)

            if followBadgeStyle != .hidden {
                Circle()
                    .fill(Color(red: 1.0, green: 0.25, blue: 0.33))
                    .frame(width: 20, height: 20)
                    .overlay(
                        Image(systemName: followBadgeStyle == .check ? "checkmark" : "plus")
                            .font(.system(size: 11.5, weight: .heavy))
                            .foregroundStyle(.white)
                    )
                    .offset(y: 9)
                    .shadow(color: .black.opacity(0.32), radius: 6, x: 0, y: 2)
            }
        }
        .padding(.bottom, 4)
    }

    private var albumButton: some View {
        ZStack {
            Circle()
                .stroke(Color.white.opacity(0.34), lineWidth: 1.5)
                .frame(width: 45, height: 45)

            AsyncImage(url: coverURL) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .scaledToFill()
                default:
                    Circle()
                        .fill(Color.white.opacity(0.14))
                }
            }
            .frame(width: 38, height: 38)
            .clipShape(Circle())
            .rotationEffect(.degrees(rotatesAlbum ? 360 : 0))
            .animation(
                .linear(duration: 6).repeatForever(autoreverses: false),
                value: rotatesAlbum
            )
        }
        .onAppear {
            rotatesAlbum = true
        }
    }

    private func formattedCount(_ value: Int64) -> String {
        if value >= 100_000_000 {
            return String(format: "%.1f亿", Double(value) / 100_000_000)
        }
        if value >= 10_000 {
            return String(format: "%.1f万", Double(value) / 10_000)
        }
        return "\(value)"
    }
}
