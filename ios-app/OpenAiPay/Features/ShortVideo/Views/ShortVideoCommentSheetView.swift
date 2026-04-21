import PhotosUI
import SwiftUI
import UIKit

struct ShortVideoCommentSheetView: View {
    private struct PreparedCommentImage {
        let uploadData: Data
        let previewData: Data
        let fileName: String
        let mimeType: String
    }

    private static let composerEmojis = [
        "😀", "😂", "🥹", "😍", "🤩", "😮",
        "🔥", "👍", "👏", "❤️", "🎉", "😭"
    ]

    let totalCommentCount: Int64
    let referenceTitle: String
    private let onClose: (() -> Void)?

    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var appState: AppState
    @StateObject private var viewModel: ShortVideoCommentSheetViewModel
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var isEmojiPanelVisible = false
    @FocusState private var isComposerFocused: Bool

    init(
        totalCommentCount: Int64,
        referenceTitle: String,
        viewModel: ShortVideoCommentSheetViewModel,
        onClose: (() -> Void)? = nil
    ) {
        self.totalCommentCount = max(totalCommentCount, 0)
        self.referenceTitle = referenceTitle.trimmingCharacters(in: .whitespacesAndNewlines)
        self.onClose = onClose
        _viewModel = StateObject(wrappedValue: viewModel)
    }

    var body: some View {
        VStack(spacing: 0) {
            header
            divider
            content
            composerBar
        }
        .background(Color.white)
        .presentationDetents([.fraction(0.72), .large])
        .presentationDragIndicator(.hidden)
        .presentationCornerRadius(28)
        .toolbar {
            ToolbarItemGroup(placement: .keyboard) {
                Spacer()
                Button {
                    dismissComposerInteractions()
                } label: {
                    Image(systemName: "keyboard.chevron.compact.down")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(Color.black.opacity(0.72))
                }
                .buttonStyle(.plain)
            }
        }
        .task {
            appState.recordPageVisit("/page/short-video/comments")
            viewModel.loadIfNeeded()
        }
        .onChange(of: selectedPhotoItem) { _, newItem in
            guard let newItem else {
                return
            }
            Task {
                await handlePickedPhoto(item: newItem)
            }
        }
        .animation(.easeOut(duration: 0.18), value: isEmojiPanelVisible)
    }

    private var header: some View {
        VStack(spacing: 7) {
            Capsule()
                .fill(Color.black.opacity(0.12))
                .frame(width: 34, height: 4)
                .padding(.top, 7)

            HStack(spacing: 8) {
                HStack(spacing: 4) {
                    Image(systemName: "music.note")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundStyle(Color(red: 0.36, green: 0.39, blue: 0.45))

                    headerTitleLabel
                        .lineLimit(1)
                }
                .padding(.horizontal, 10)
                .frame(height: 30)
                .background(Color(red: 0.95, green: 0.95, blue: 0.97))
                .clipShape(Capsule())

                Spacer(minLength: 6)

                expandHeaderButton
                closeHeaderButton
            }
            .padding(.horizontal, 14)

            Text(commentCountTitle)
                .font(.system(size: 16.5, weight: .semibold))
                .foregroundStyle(Color.black.opacity(0.86))
        }
        .padding(.bottom, 10)
        .background(Color.white)
    }

    private var divider: some View {
        Rectangle()
            .fill(Color.black.opacity(0.04))
            .frame(height: 0.35)
    }

    @ViewBuilder
    private var content: some View {
        if viewModel.state.isInitialLoading {
            VStack(spacing: 12) {
                Spacer()
                ProgressView()
                    .tint(.black.opacity(0.78))
                Text("正在加载评论")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundStyle(Color.black.opacity(0.46))
                Spacer()
            }
            .contentShape(Rectangle())
            .onTapGesture {
                dismissComposerInteractions()
            }
        } else if let errorMessage = viewModel.state.errorMessage, viewModel.state.items.isEmpty {
            VStack(spacing: 14) {
                Spacer()
                Image(systemName: "text.bubble")
                    .font(.system(size: 30, weight: .semibold))
                    .foregroundStyle(Color.black.opacity(0.24))
                Text(errorMessage)
                    .font(.system(size: 15, weight: .medium))
                    .foregroundStyle(Color.black.opacity(0.74))
                    .multilineTextAlignment(.center)
                Button("重新加载") {
                    viewModel.retry()
                }
                .buttonStyle(.plain)
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(.white)
                .padding(.horizontal, 18)
                .padding(.vertical, 11)
                .background(Color.black.opacity(0.82))
                .clipShape(Capsule())
                Spacer()
            }
            .padding(.horizontal, 24)
            .contentShape(Rectangle())
            .onTapGesture {
                dismissComposerInteractions()
            }
        } else if viewModel.state.shouldShowEmptyState {
            VStack(spacing: 12) {
                Spacer()
                Image(systemName: "bubble.left.and.bubble.right")
                    .font(.system(size: 30, weight: .semibold))
                    .foregroundStyle(Color.black.opacity(0.22))
                Text("还没有评论，来抢个沙发")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundStyle(Color.black.opacity(0.42))
                Spacer()
            }
            .contentShape(Rectangle())
            .onTapGesture {
                dismissComposerInteractions()
            }
        } else {
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 0) {
                    ForEach(Array(viewModel.state.items.enumerated()), id: \.element.id) { index, comment in
                        commentRow(comment)
                            .onAppear {
                                if index == viewModel.state.items.count - 1 {
                                    viewModel.loadMoreIfNeeded()
                                }
                            }
                    }

                    if viewModel.state.isLoadingMore {
                        HStack {
                            Spacer()
                            ProgressView()
                                .tint(.black.opacity(0.72))
                            Spacer()
                        }
                        .padding(.vertical, 14)
                    }
                }
            }
            .scrollIndicators(.hidden)
            .scrollDismissesKeyboard(.immediately)
        }
    }

    private func commentRow(_ comment: ShortVideoComment) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .top, spacing: 10) {
                avatarView(comment, size: 34)
                    .padding(.top, 2)

                VStack(alignment: .leading, spacing: 6) {
                    Text(comment.user.nickname)
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundStyle(Color(red: 0.63, green: 0.66, blue: 0.71))
                        .lineLimit(1)

                    if comment.hasRenderableText {
                        Text(comment.content)
                            .font(.system(size: 16, weight: .regular))
                            .foregroundStyle(Color.black.opacity(0.88))
                            .lineSpacing(2)
                            .fixedSize(horizontal: false, vertical: true)
                    }

                    if let imageURL = comment.imageDisplayURL {
                        commentImage(url: imageURL)
                    }

                    commentMetaLine(comment)

                    if comment.replyCount > 0 {
                        replyToggleButton(for: comment)
                            .padding(.top, 2)
                    }

                    if viewModel.state.isRepliesExpanded(for: comment.commentId) {
                        replyThreadView(for: comment)
                    }
                }

                Spacer(minLength: 8)

                likeToolbar(for: comment)
                    .padding(.top, 3)
            }
            .padding(.horizontal, 15)
            .padding(.vertical, 11)
            .contentShape(Rectangle())
            .onTapGesture {
                dismissComposerInteractions()
            }
        }
    }

    private func replyThreadView(for rootComment: ShortVideoComment) -> some View {
        let replies = viewModel.state.visibleReplies(for: rootComment)

        return VStack(alignment: .leading, spacing: 0) {
            if !replies.isEmpty {
                ForEach(Array(replies.enumerated()), id: \.element.id) { index, reply in
                    replyRow(reply)
                        .padding(.top, index == 0 ? 8 : 4)
                        .onAppear {
                            if index == replies.count - 1 {
                                viewModel.loadMoreRepliesIfNeeded(for: rootComment.commentId)
                            }
                        }
                }
            }

            if viewModel.state.isRepliesLoading(for: rootComment.commentId) {
                HStack(spacing: 8) {
                    ProgressView()
                        .scaleEffect(0.8)
                    Text("正在加载回复")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(Color(red: 0.60, green: 0.63, blue: 0.68))
                }
                .padding(.top, 8)
            } else if viewModel.state.canLoadMoreReplies(for: rootComment.commentId) {
                Button("查看更多回复") {
                    viewModel.loadMoreRepliesIfNeeded(for: rootComment.commentId)
                }
                .buttonStyle(.plain)
                .font(.system(size: 12, weight: .semibold))
                .foregroundStyle(Color(red: 0.47, green: 0.50, blue: 0.56))
                .padding(.top, 8)
            }
        }
        .padding(.leading, 2)
    }

    private func replyRow(_ comment: ShortVideoComment) -> some View {
        HStack(alignment: .top, spacing: 9) {
            avatarView(comment, size: 28)

            VStack(alignment: .leading, spacing: 5) {
                Text(comment.user.nickname)
                    .font(.system(size: 11.5, weight: .semibold))
                    .foregroundStyle(Color(red: 0.60, green: 0.63, blue: 0.68))
                    .lineLimit(1)

                if comment.hasRenderableText {
                    Text(comment.content)
                        .font(.system(size: 14.5, weight: .regular))
                        .foregroundStyle(Color.black.opacity(0.86))
                        .lineSpacing(2)
                        .fixedSize(horizontal: false, vertical: true)
                }

                if let imageURL = comment.imageDisplayURL {
                    commentImage(url: imageURL)
                        .frame(maxWidth: 180)
                }

                commentMetaLine(comment)
            }

            Spacer(minLength: 8)

            likeToolbar(for: comment, iconSize: 15, width: 30)
        }
        .padding(.leading, 10)
        .contentShape(Rectangle())
        .onTapGesture {
            dismissComposerInteractions()
        }
    }

    private func commentMetaLine(_ comment: ShortVideoComment) -> some View {
        HStack(spacing: 8) {
            Text(comment.displayCreatedAt)

            Button("回复") {
                viewModel.startReply(to: comment)
                isEmojiPanelVisible = false
                isComposerFocused = true
            }
            .buttonStyle(.plain)

            Spacer(minLength: 0)
        }
        .font(.system(size: 12, weight: .medium))
        .foregroundStyle(Color(red: 0.69, green: 0.71, blue: 0.75))
    }

    private func replyToggleButton(for comment: ShortVideoComment) -> some View {
        let isExpanded = viewModel.state.isRepliesExpanded(for: comment.commentId)
        let title = isExpanded ? "收起回复" : "展开\(comment.replyCount)条回复"

        return Button {
            viewModel.toggleReplies(for: comment)
        } label: {
            HStack(spacing: 6) {
                Text(title)
                Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                    .font(.system(size: 9, weight: .semibold))
            }
            .font(.system(size: 12, weight: .semibold))
            .foregroundStyle(Color(red: 0.47, green: 0.50, blue: 0.56))
        }
        .buttonStyle(.plain)
    }

    private func likeToolbar(for comment: ShortVideoComment,
                             iconSize: CGFloat = 17,
                             width: CGFloat = 32) -> some View {
        VStack(spacing: 3) {
            Button {
                viewModel.toggleLike(commentId: comment.commentId)
            } label: {
                if viewModel.state.isLikePending(for: comment.commentId) {
                    ProgressView()
                        .tint(Color(red: 0.96, green: 0.22, blue: 0.39))
                        .scaleEffect(0.82)
                        .frame(width: width, height: 22)
                } else {
                    Image(systemName: comment.liked ? "heart.fill" : "heart")
                        .font(.system(size: iconSize, weight: .regular))
                        .foregroundStyle(
                            comment.liked
                            ? Color(red: 0.96, green: 0.22, blue: 0.39)
                            : Color(red: 0.61, green: 0.63, blue: 0.69)
                        )
                        .frame(width: width, height: 22)
                }
            }
            .buttonStyle(.plain)
            .disabled(viewModel.state.isLikePending(for: comment.commentId))

            if comment.likeCount > 0 {
                Text(formattedSideCount(comment.likeCount))
                    .font(.system(size: 11, weight: .medium))
                    .foregroundStyle(Color(red: 0.61, green: 0.63, blue: 0.69))
            } else {
                Color.clear
                    .frame(height: 13)
            }
        }
        .frame(width: width)
    }

    @ViewBuilder
    private func avatarView(_ comment: ShortVideoComment, size: CGFloat) -> some View {
        if let avatarURL = comment.avatarURL {
            AsyncImage(url: avatarURL) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .scaledToFill()
                default:
                    Circle()
                        .fill(Color.black.opacity(0.08))
                }
            }
            .frame(width: size, height: size)
            .clipShape(Circle())
        } else {
            Circle()
                .fill(Color.black.opacity(0.08))
                .frame(width: size, height: size)
                .overlay(
                    Text(String(comment.user.nickname.prefix(1)))
                        .font(.system(size: max(size * 0.38, 11), weight: .bold))
                        .foregroundStyle(Color.black.opacity(0.42))
                )
        }
    }

    private func commentImage(url: URL) -> some View {
        AsyncImage(url: url) { phase in
            switch phase {
            case .success(let image):
                image
                    .resizable()
                    .scaledToFill()
            case .failure:
                RoundedRectangle(cornerRadius: 14)
                    .fill(Color.black.opacity(0.06))
                    .overlay(
                        Image(systemName: "photo")
                            .font(.system(size: 20, weight: .medium))
                            .foregroundStyle(Color.black.opacity(0.18))
                    )
            default:
                RoundedRectangle(cornerRadius: 14)
                    .fill(Color.black.opacity(0.06))
                    .overlay(
                        ProgressView()
                            .tint(.black.opacity(0.55))
                    )
            }
        }
        .frame(width: 168, height: 210)
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }

    private var composerBar: some View {
        VStack(spacing: 8) {
            if isEmojiPanelVisible {
                emojiPickerBar
            }

            if let replyTarget = viewModel.state.replyTarget {
                HStack(spacing: 8) {
                    Text("正在回复 @\(replyTarget.nickname)")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(Color(red: 0.53, green: 0.56, blue: 0.62))
                        .lineLimit(1)

                    Spacer(minLength: 0)

                    Button {
                        viewModel.cancelReplying()
                    } label: {
                        Image(systemName: "xmark")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundStyle(Color.black.opacity(0.48))
                            .frame(width: 18, height: 18)
                            .background(Color.black.opacity(0.06))
                            .clipShape(Circle())
                    }
                    .buttonStyle(.plain)
                }
            }

            if let draftImage = viewModel.state.draftImage {
                HStack(spacing: 10) {
                    if let previewImage = UIImage(data: draftImage.previewData) {
                        Image(uiImage: previewImage)
                            .resizable()
                            .scaledToFill()
                            .frame(width: 60, height: 76)
                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text("图片评论已准备好")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(Color.black.opacity(0.82))
                        Text(draftImage.fileName)
                            .font(.system(size: 11.5, weight: .medium))
                            .foregroundStyle(Color(red: 0.61, green: 0.63, blue: 0.69))
                            .lineLimit(1)
                    }

                    Spacer(minLength: 0)

                    Button("移除") {
                        viewModel.removeDraftImage()
                    }
                    .buttonStyle(.plain)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(Color(red: 0.98, green: 0.24, blue: 0.39))
                }
            }

            if viewModel.state.isUploadingImage {
                HStack(spacing: 8) {
                    ProgressView()
                        .scaleEffect(0.82)
                    Text("图片上传中...")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(Color(red: 0.60, green: 0.63, blue: 0.68))
                    Spacer(minLength: 0)
                }
            }

            if let submissionMessage = viewModel.state.submissionMessage {
                Text(submissionMessage)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundStyle(Color(red: 0.82, green: 0.22, blue: 0.24))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }

            HStack(spacing: 11) {
                TextField(
                    viewModel.state.composerPlaceholder,
                    text: Binding(
                        get: { viewModel.state.draft },
                        set: { viewModel.updateDraft($0) }
                    ),
                    axis: .vertical
                )
                .textFieldStyle(.plain)
                .submitLabel(.send)
                .onSubmit {
                    submitCommentIfNeeded()
                }
                .focused($isComposerFocused)
                .lineLimit(1...4)
                .font(.system(size: 14, weight: .medium))
                .padding(.horizontal, 15)
                .padding(.vertical, 10)
                .background(Color(red: 0.95, green: 0.95, blue: 0.97))
                .clipShape(Capsule())
                .onTapGesture {
                    isEmojiPanelVisible = false
                }

                PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                    composerIconLabel(systemName: "photo.on.rectangle")
                }
                .buttonStyle(.plain)
                .disabled(viewModel.state.isUploadingImage || viewModel.state.isSubmitting)

                Button {
                    viewModel.cancelReplying()
                } label: {
                    composerIconLabel(systemName: "at")
                }
                .buttonStyle(.plain)

                if viewModel.state.canSubmit {
                    Button("发送") {
                        submitCommentIfNeeded()
                    }
                    .buttonStyle(.plain)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(Color(red: 0.98, green: 0.24, blue: 0.39))
                }

                Button {
                    toggleEmojiPanel()
                } label: {
                    composerIconLabel(
                        systemName: "face.smiling",
                        showsBackground: true,
                        isActive: isEmojiPanelVisible
                    )
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 13)
        .padding(.top, 8)
        .padding(.bottom, 8)
        .background(Color.white)
        .overlay(alignment: .top) {
            Rectangle()
                .fill(Color.black.opacity(0.04))
                .frame(height: 0.35)
        }
    }

    private var emojiPickerBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 10) {
                ForEach(Self.composerEmojis, id: \.self) { emoji in
                    Button {
                        appendEmoji(emoji)
                    } label: {
                        Text(emoji)
                            .font(.system(size: 27))
                            .frame(width: 38, height: 38)
                            .background(Color(red: 0.95, green: 0.95, blue: 0.97))
                            .clipShape(Circle())
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 4)
        }
        .padding(.vertical, 2)
    }

    private func composerIconLabel(systemName: String,
                                   showsBackground: Bool = false,
                                   isActive: Bool = false) -> some View {
        Image(systemName: systemName)
            .font(.system(size: systemName == "at" ? 20 : 19, weight: .regular))
            .foregroundStyle(
                isActive
                ? Color(red: 0.98, green: 0.24, blue: 0.39)
                : Color.black.opacity(0.78)
            )
            .frame(width: 28, height: 28)
            .background(
                Circle()
                    .fill(
                        showsBackground
                        ? (isActive
                           ? Color(red: 0.99, green: 0.92, blue: 0.94)
                           : Color(red: 0.94, green: 0.94, blue: 0.96))
                        : .clear
                    )
            )
    }

    private var expandHeaderButton: some View {
        Button {
            // 预留来源卡片展开入口。
        } label: {
            Image(systemName: "arrow.up.left.and.arrow.down.right")
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(Color.black.opacity(0.58))
                .frame(width: 20, height: 20)
        }
        .buttonStyle(.plain)
    }

    private var closeHeaderButton: some View {
        Button {
            closeCommentSheet()
        } label: {
            Image(systemName: "xmark")
                .font(.system(size: 12, weight: .bold))
                .foregroundStyle(Color.black.opacity(0.64))
                .frame(width: 26, height: 26)
                .background(Color(red: 0.96, green: 0.96, blue: 0.97))
                .clipShape(Circle())
        }
        .buttonStyle(.plain)
    }

    private var commentCountTitle: String {
        "\(formattedCommentCount(max(totalCommentCount, viewModel.state.displayedCommentCount)))条评论"
    }

    private var headerTitleLabel: some View {
        let normalized = referenceTitle.trimmingCharacters(in: .whitespacesAndNewlines)
        if normalized.isEmpty {
            return AnyView(
                Text("去汽水听·评论区")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(Color(red: 0.29, green: 0.33, blue: 0.39))
            )
        }
        return AnyView(
            (
                Text("去汽水听·")
                    .foregroundStyle(Color(red: 0.36, green: 0.39, blue: 0.45))
                + Text(normalized)
                    .foregroundStyle(Color(red: 0.24, green: 0.43, blue: 0.77))
            )
            .font(.system(size: 13, weight: .semibold))
        )
    }

    private func formattedCommentCount(_ value: Int64) -> String {
        if value >= 10_000 {
            return String(format: "%.1f万", Double(value) / 10_000)
        }
        return "\(value)"
    }

    private func formattedSideCount(_ value: Int64) -> String {
        if value >= 10_000 {
            return String(format: "%.1f万", Double(value) / 10_000)
        }
        return "\(value)"
    }

    private func dismissComposerInteractions() {
        isComposerFocused = false
        isEmojiPanelVisible = false
    }

    private func toggleEmojiPanel() {
        if isEmojiPanelVisible {
            isEmojiPanelVisible = false
            isComposerFocused = true
        } else {
            isComposerFocused = false
            isEmojiPanelVisible = true
        }
    }

    private func appendEmoji(_ emoji: String) {
        viewModel.updateDraft(viewModel.state.draft + emoji)
        isEmojiPanelVisible = false
        isComposerFocused = true
    }

    private func closeCommentSheet() {
        dismissComposerInteractions()
        onClose?()
        dismiss()
    }

    private func submitCommentIfNeeded() {
        appState.recordBehaviorEvent(
            eventName: "/page/short-video/comments/submit",
            eventType: "USER_ACTION",
            eventCode: "SHORT_VIDEO_COMMENT_SUBMIT",
            pageName: "/page/short-video/comments",
            actionName: "SUBMIT_COMMENT",
            resultSummary: "REQUESTED",
            payload: [
                "draftLength": viewModel.state.draft.trimmingCharacters(in: .whitespacesAndNewlines).count,
                "hasImage": viewModel.state.draftImage != nil,
                "isReply": viewModel.state.replyTarget != nil
            ],
            minimumInterval: 0
        )
        isEmojiPanelVisible = false
        viewModel.submitComment()
    }

    @MainActor
    private func handlePickedPhoto(item: PhotosPickerItem) async {
        defer {
            selectedPhotoItem = nil
        }

        do {
            guard let rawData = try await item.loadTransferable(type: Data.self) else {
                throw APIClientError.businessError(message: "读取图片失败")
            }
            let prepared = try await Task.detached(priority: .userInitiated) {
                try Self.prepareCommentImage(from: rawData)
            }.value
            viewModel.uploadDraftImage(
                uploadData: prepared.uploadData,
                previewData: prepared.previewData,
                fileName: prepared.fileName,
                mimeType: prepared.mimeType
            )
        } catch {
            viewModel.removeDraftImage()
        }
    }

    private static func prepareCommentImage(from rawData: Data) throws -> PreparedCommentImage {
        guard let image = UIImage(data: rawData) else {
            throw APIClientError.businessError(message: "读取图片失败")
        }

        let uploadImage = resizedImage(image, maxLongSide: 1600)
        let previewImage = resizedImage(image, maxLongSide: 320)
        guard let uploadData = uploadImage.jpegData(compressionQuality: 0.84), !uploadData.isEmpty else {
            throw APIClientError.businessError(message: "处理图片失败")
        }
        let previewData = previewImage.jpegData(compressionQuality: 0.72) ?? uploadData
        let timestamp = Int(Date().timeIntervalSince1970)
        return PreparedCommentImage(
            uploadData: uploadData,
            previewData: previewData,
            fileName: "short-video-comment-\(timestamp).jpg",
            mimeType: "image/jpeg"
        )
    }

    private static func resizedImage(_ image: UIImage, maxLongSide: CGFloat) -> UIImage {
        let originalSize = image.size
        let longSide = max(originalSize.width, originalSize.height)
        guard longSide > 0, longSide > maxLongSide else {
            return image
        }

        let scaleRatio = maxLongSide / longSide
        let targetSize = CGSize(
            width: max(1, floor(originalSize.width * scaleRatio)),
            height: max(1, floor(originalSize.height * scaleRatio))
        )
        let format = UIGraphicsImageRendererFormat.default()
        format.opaque = false
        format.scale = 1
        return UIGraphicsImageRenderer(size: targetSize, format: format).image { _ in
            image.draw(in: CGRect(origin: .zero, size: targetSize))
        }
    }
}
