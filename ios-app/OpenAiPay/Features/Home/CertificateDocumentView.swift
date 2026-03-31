import SwiftUI
import UIKit
import Photos
import PhotosUI

private let certificateCardAspectRatio: CGFloat = 646.0 / 406.0
private let certificateUploadBackground = Color(red: 0.95, green: 0.95, blue: 0.96)
private let certificateTopTintStart = Color(red: 0.87, green: 0.78, blue: 0.96)
private let certificateTopTintEnd = Color(red: 0.94, green: 0.93, blue: 0.96)
private let certificateUploadSectionBackground = Color.white
private let certificateHeaderTopPadding: CGFloat = 50
private let certificateHeaderBottomPadding: CGFloat = 18

private let certificateElectronicCards: [CertificateRecommendationCard] = [
    CertificateRecommendationCard(
        title: "医保码",
        subtitle: "看病/购药刷医保",
        symbolName: "cross.case.fill",
        symbolColor: AppTheme.palette.brandPrimary,
        startColor: AppTheme.palette.brandGradientStart,
        endColor: Color(red: 0.15, green: 0.28, blue: 0.77)
    ),
    CertificateRecommendationCard(
        title: "居民身份证",
        subtitle: "网吧出示/酒店入住/查看证件...",
        symbolName: "person.text.rectangle.fill",
        symbolColor: Color(red: 0.93, green: 0.37, blue: 0.34),
        startColor: Color(red: 0.90, green: 0.93, blue: 0.98),
        endColor: Color(red: 0.84, green: 0.88, blue: 0.96)
    ),
    CertificateRecommendationCard(
        title: "社保卡",
        subtitle: "社保/养老金随时查",
        symbolName: "shield.lefthalf.filled.badge.checkmark",
        symbolColor: Color(red: 0.90, green: 0.30, blue: 0.28),
        startColor: AppTheme.palette.brandPrimaryLight,
        endColor: AppTheme.palette.brandGradientStart
    ),
    CertificateRecommendationCard(
        title: "公积金",
        subtitle: "查询公积金 / 提取公积金",
        symbolName: "building.columns.fill",
        symbolColor: Color(red: 0.95, green: 0.59, blue: 0.14),
        startColor: Color(red: 0.89, green: 0.67, blue: 0.49),
        endColor: Color(red: 0.86, green: 0.59, blue: 0.39)
    )
]

private let certificateSuggestedCards: [CertificateRecommendationCard] = [
    CertificateRecommendationCard(
        title: "驾驶证",
        subtitle: "驾照信息/交通违法信息快速查",
        symbolName: "steeringwheel",
        symbolColor: Color(red: 0.20, green: 0.22, blue: 0.25),
        startColor: Color(red: 0.12, green: 0.51, blue: 0.42),
        endColor: Color(red: 0.11, green: 0.42, blue: 0.35)
    ),
    CertificateRecommendationCard(
        title: "行驶证",
        subtitle: "车辆信息/年检状态随时看",
        symbolName: "car.front.waves.up.fill",
        symbolColor: Color(red: 0.20, green: 0.34, blue: 0.67),
        startColor: AppTheme.palette.brandGradientStart,
        endColor: Color(red: 0.31, green: 0.50, blue: 0.84)
    ),
    CertificateRecommendationCard(
        title: "居住证",
        subtitle: "居住登记/居住证办理进度可查",
        symbolName: "house.fill",
        symbolColor: Color(red: 0.45, green: 0.26, blue: 0.63),
        startColor: Color(red: 0.60, green: 0.29, blue: 0.54),
        endColor: Color(red: 0.46, green: 0.19, blue: 0.39)
    )
]

struct CertificateDocumentView: View {
    let userProfile: UserProfile?
    let onBack: () -> Void

    @State private var hideSensitiveInfo = true
    @State private var applyWatermark = false
    @State private var toastMessage: String?
    @State private var showPhotoViewer = false
    @State private var isPhotoPickerPresented = false
    @State private var selectedUploadPhotoItem: PhotosPickerItem?
    @State private var isUploadingCertificatePhoto = false
    @State private var locallyUploadedIdentity = false

    private var certificateProfile: CertificateProfile {
        CertificateProfile(userProfile: userProfile, forceUploadedIdentity: locallyUploadedIdentity)
    }

    private var hasUploadedIdentity: Bool {
        certificateProfile.hasUploadedIdentity
    }

    var body: some View {
        ZStack(alignment: .top) {
            if hasUploadedIdentity {
                uploadedDocumentContent
            } else {
                emptyDocumentContent
            }

            if let toastMessage {
                toastView(text: toastMessage)
                    .padding(.top, hasUploadedIdentity ? 86 : 100)
                    .transition(.opacity.combined(with: .scale(scale: 0.96)))
            }
        }
        .fullScreenCover(
            isPresented: Binding(
                get: { hasUploadedIdentity && showPhotoViewer },
                set: { showPhotoViewer = $0 }
            )
        ) {
            CertificatePhotoViewer(
                profile: certificateProfile,
                revealSensitiveInfo: !hideSensitiveInfo,
                showWatermark: applyWatermark,
                onClose: { showPhotoViewer = false }
            )
        }
        .photosPicker(isPresented: $isPhotoPickerPresented, selection: $selectedUploadPhotoItem, matching: .images)
        .onChange(of: selectedUploadPhotoItem) { _, newValue in
            guard let newValue else {
                return
            }
            Task {
                await uploadCertificatePhoto(item: newValue)
            }
        }
        .task(id: userProfile?.userId) {
            syncLocalUploadedIdentityFlag()
        }
    }

    private var uploadedDocumentContent: some View {
        ZStack(alignment: .top) {
            LinearGradient(
                colors: [
                    certificateTopTintStart,
                    certificateTopTintEnd
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            ScrollView(showsIndicators: false) {
                VStack(spacing: 0) {
                    uploadedHeaderView
                        .padding(.horizontal, 16)
                        .padding(.top, certificateHeaderTopPadding)
                        .padding(.bottom, certificateHeaderBottomPadding)

                    cardsSection
                        .padding(.horizontal, 18)

                    actionRow
                        .padding(.top, 18)
                        .padding(.horizontal, 18)

                    detailsPanel
                        .padding(.top, 18)
                }
                .padding(.bottom, 28)
            }
        }
    }

    private var emptyDocumentContent: some View {
        ZStack(alignment: .top) {
            LinearGradient(
                colors: [
                    certificateTopTintStart,
                    certificateTopTintEnd,
                    certificateUploadBackground
                ],
                startPoint: .top,
                endPoint: .bottom
            )
                .ignoresSafeArea()

            ScrollView(showsIndicators: false) {
                VStack(spacing: 0) {
                    emptyHeaderView
                        .padding(.horizontal, 16)
                        .padding(.top, certificateHeaderTopPadding)
                        .padding(.bottom, certificateHeaderBottomPadding)

                    uploadPhotoSection
                        .padding(.horizontal, 10)
                }
                .padding(.bottom, 26)
            }
        }
    }

    private var uploadedHeaderView: some View {
        certificateHeaderBar(showMoreButton: true)
    }

    private var emptyHeaderView: some View {
        ZStack {
            certificateHeaderBar(showMoreButton: false)

            Text("证件")
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(Color(red: 0.20, green: 0.22, blue: 0.25))
                .offset(y: 10)
        }
        .frame(height: 52)
    }

    private func certificateHeaderBar(showMoreButton: Bool) -> some View {
        HStack {
            Button(action: onBack) {
                HStack(spacing: 0) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 19, weight: .medium))
                        .foregroundStyle(Color(red: 0.24, green: 0.28, blue: 0.34))
                    Spacer(minLength: 0)
                }
                .frame(width: 44, height: 44)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .frame(width: 52, height: 52, alignment: .leading)
            .contentShape(Rectangle())
            .accessibilityIdentifier("certificate_back")

            Spacer()

            if showMoreButton {
                Button {
                    showToast("更多操作建设中")
                } label: {
                    Image(systemName: "ellipsis")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(Color(red: 0.24, green: 0.28, blue: 0.34))
                        .frame(width: 36, height: 36)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("certificate_more")
            } else {
                Color.clear
                    .frame(width: 36, height: 36)
            }
        }
    }

    private var uploadPhotoSection: some View {
        uploadSectionCard(title: "证件照片", trailingText: nil) {
            Button {
                guard !isUploadingCertificatePhoto else {
                    return
                }
                isPhotoPickerPresented = true
            } label: {
                HStack(spacing: 12) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .fill(Color.white.opacity(0.94))
                        Image(systemName: "person.text.rectangle.fill")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundStyle(Color(red: 0.46, green: 0.55, blue: 0.70))
                    }
                    .frame(width: 36, height: 36)

                    Text("尝试添加本人身份证照片")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundStyle(Color(red: 0.34, green: 0.39, blue: 0.47))
                        .lineLimit(2)

                    Spacer(minLength: 10)

                    Text(isUploadingCertificatePhoto ? "上传中..." : "立即上传")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(AppTheme.palette.brandPrimary)
                }
                .padding(.horizontal, 14)
                .frame(height: 56)
                .background(
                    RoundedRectangle(cornerRadius: 10, style: .continuous)
                        .fill(Color(red: 0.90, green: 0.94, blue: 0.99))
                )
            }
            .buttonStyle(.plain)
            .disabled(isUploadingCertificatePhoto)
            .accessibilityIdentifier("certificate_upload_photo")
        }
    }

    private var electronicDocumentsSection: some View {
        uploadSectionCard(title: "电子证件", trailingText: "添加") {
            VStack(spacing: 12) {
                ForEach(certificateElectronicCards) { card in
                    recommendationCard(card)
                }
            }
        }
    }

    private var recommendationTitleRow: some View {
        HStack(spacing: 12) {
            Rectangle()
                .fill(Color(red: 0.83, green: 0.84, blue: 0.86))
                .frame(height: 1)

            Text("推荐添加")
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(Color(red: 0.49, green: 0.51, blue: 0.55))

            Rectangle()
                .fill(Color(red: 0.83, green: 0.84, blue: 0.86))
                .frame(height: 1)
        }
    }

    private func uploadSectionCard<Content: View>(
        title: String,
        trailingText: String?,
        @ViewBuilder content: () -> Content
    ) -> some View {
        VStack(spacing: 12) {
            HStack {
                Text(title)
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundStyle(Color(red: 0.27, green: 0.29, blue: 0.32))

                Spacer()

                if let trailingText {
                    Button {
                        showToast("\(trailingText)功能建设中")
                    } label: {
                        HStack(spacing: 3) {
                            Text(trailingText)
                                .font(.system(size: 16, weight: .medium))
                            Image(systemName: "chevron.right")
                                .font(.system(size: 12, weight: .semibold))
                        }
                        .foregroundStyle(Color(red: 0.64, green: 0.66, blue: 0.70))
                    }
                    .buttonStyle(.plain)
                }
            }

            content()
        }
        .padding(.horizontal, 12)
        .padding(.top, 12)
        .padding(.bottom, 10)
        .background(certificateUploadSectionBackground)
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }

    private func recommendationCard(_ card: CertificateRecommendationCard) -> some View {
        Button {
            showToast("\(card.title)功能建设中")
        } label: {
            ZStack {
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .fill(
                        LinearGradient(
                            colors: [card.startColor, card.endColor],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )

                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .fill(Color.white.opacity(0.06))
                    .overlay(alignment: .topTrailing) {
                        ZStack {
                            Circle()
                                .fill(Color.white.opacity(0.09))
                                .frame(width: 116, height: 116)
                                .offset(x: 28, y: -52)
                            Circle()
                                .stroke(Color.white.opacity(0.08), lineWidth: 1)
                                .frame(width: 98, height: 98)
                                .offset(x: 16, y: -40)
                        }
                    }

                HStack(spacing: 12) {
                    ZStack {
                        Circle()
                            .fill(Color.white.opacity(0.96))
                        Image(systemName: card.symbolName)
                            .font(.system(size: 19, weight: .semibold))
                            .foregroundStyle(card.symbolColor)
                    }
                    .frame(width: 40, height: 40)

                    VStack(alignment: .leading, spacing: 4) {
                        Text(card.title)
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundStyle(Color(red: 0.23, green: 0.27, blue: 0.31))

                        Text(card.subtitle)
                            .font(.system(size: 14, weight: .regular))
                            .foregroundStyle(Color(red: 0.31, green: 0.35, blue: 0.41).opacity(0.85))
                            .lineLimit(1)
                    }

                    Spacer(minLength: 8)

                    Text("立即添加")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(Color(red: 0.33, green: 0.35, blue: 0.39))
                        .padding(.horizontal, 10)
                        .frame(height: 30)
                        .background(Color.white.opacity(0.94))
                        .clipShape(RoundedRectangle(cornerRadius: 6, style: .continuous))
                }
                .padding(.horizontal, 14)
            }
            .frame(height: 78)
        }
        .buttonStyle(.plain)
    }

    private var cardsSection: some View {
        HStack {
            Spacer(minLength: 0)
            cardButton(isFront: true)
            Spacer(minLength: 0)
        }
    }

    private func cardButton(isFront: Bool) -> some View {
        Button {
            showPhotoViewer = true
        } label: {
            ZStack(alignment: .bottomTrailing) {
                CertificateCardView(
                    profile: certificateProfile,
                    isFront: isFront,
                    revealSensitiveInfo: !hideSensitiveInfo,
                    showWatermark: applyWatermark,
                    textScale: 1.0
                )
                .aspectRatio(certificateCardAspectRatio, contentMode: .fit)
                .frame(height: 102)
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))

                ZStack {
                    RoundedRectangle(cornerRadius: 8, style: .continuous)
                        .fill(Color.black.opacity(0.72))
                    Image(systemName: "arrow.up.left.and.arrow.down.right")
                        .font(.system(size: 10.5, weight: .semibold))
                        .foregroundStyle(.white)
                }
                .frame(width: 24, height: 24)
                .padding(.trailing, 8)
                .padding(.bottom, 8)
            }
            .contentShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier(isFront ? "certificate_front_card" : "certificate_back_card")
    }

    private var actionRow: some View {
        HStack(spacing: 12) {
            actionButton(
                title: "查看照片",
                systemImage: "photo.on.rectangle.angled",
                tint: AppTheme.palette.brandPrimaryLight
            ) {
                showPhotoViewer = true
            }

            actionButton(
                title: "下载照片",
                systemImage: "square.and.arrow.down",
                tint: AppTheme.palette.brandPrimaryLight
            ) {
                downloadPhotos()
            }

            actionButton(
                title: "加水印",
                systemImage: "character.cursor.ibeam",
                tint: AppTheme.palette.brandPrimaryPressed
            ) {
                applyWatermark.toggle()
                showToast(applyWatermark ? "已开启爱付水印" : "已关闭爱付水印")
            }

            actionButton(
                title: "批量复制",
                systemImage: "checklist.checked",
                tint: Color(red: 0.41, green: 0.79, blue: 0.75)
            ) {
                copyAllFields()
            }
        }
    }

    private func actionButton(
        title: String,
        systemImage: String,
        tint: Color,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            VStack(spacing: 9) {
                ZStack {
                    RoundedRectangle(cornerRadius: 10, style: .continuous)
                        .fill(tint.opacity(0.14))
                        .frame(width: 34, height: 34)
                    Image(systemName: systemImage)
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(tint)
                }
                Text(title)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundStyle(Color(red: 0.23, green: 0.26, blue: 0.31))
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 76)
            .background(Color.white.opacity(0.94))
            .clipShape(RoundedRectangle(cornerRadius: 15, style: .continuous))
        }
        .buttonStyle(.plain)
    }

    private var detailsPanel: some View {
        VStack(spacing: 0) {
            HStack {
                HStack(spacing: 8) {
                    Text(certificateProfile.documentType)
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(Color(red: 0.21, green: 0.24, blue: 0.30))

                    Button {
                        hideSensitiveInfo.toggle()
                    } label: {
                        Image(systemName: hideSensitiveInfo ? "eye.slash" : "eye")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundStyle(Color(red: 0.49, green: 0.53, blue: 0.60))
                    }
                    .buttonStyle(.plain)
                    .accessibilityIdentifier("certificate_visibility_toggle")
                }

                Spacer()
            }
            .padding(.horizontal, 18)
            .padding(.top, 20)
            .padding(.bottom, 10)

            VStack(spacing: 0) {
                rowView(title: "姓名", visibleValue: visibleNameText, copyValue: certificateProfile.fullName)
                rowView(title: "身份证号", visibleValue: visibleIdCardText, copyValue: certificateProfile.rawIdCardNo)
                rowView(title: "失效日期", visibleValue: certificateProfile.validUntilText, copyValue: certificateProfile.validUntilText)
                rowView(title: "住址", visibleValue: certificateProfile.addressText, copyValue: certificateProfile.addressText, allowsWrap: true)
                rowView(title: "民族", visibleValue: certificateProfile.ethnicityText, copyValue: certificateProfile.ethnicityText)
            }
            .padding(.horizontal, 18)
            .padding(.bottom, 20)
        }
        .frame(maxWidth: .infinity, alignment: .top)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 28, style: .continuous))
    }

    private func rowView(
        title: String,
        visibleValue: String,
        copyValue: String,
        allowsWrap: Bool = false
    ) -> some View {
        HStack(alignment: allowsWrap ? .top : .center, spacing: 10) {
            Text(title)
                .font(.system(size: 15.5, weight: .regular))
                .foregroundStyle(Color(red: 0.64, green: 0.66, blue: 0.70))
                .frame(width: 78, alignment: .leading)
                .padding(.top, allowsWrap ? 2 : 0)

            Text(visibleValue)
                .font(.system(size: 16.5, weight: .medium))
                .foregroundStyle(Color(red: 0.33, green: 0.35, blue: 0.39))
                .frame(maxWidth: .infinity, alignment: .leading)
                .lineLimit(allowsWrap ? 2 : 1)
                .multilineTextAlignment(.leading)

            Button {
                let value = copyValue.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? visibleValue : copyValue
                UIPasteboard.general.string = value
                showToast("\(title)已复制")
            } label: {
                Text("复制")
                    .font(.system(size: 13.5, weight: .medium))
                    .foregroundStyle(AppTheme.palette.brandLink)
                    .padding(.horizontal, 13)
                    .frame(height: 31)
                    .background(Color(red: 0.94, green: 0.96, blue: 0.99))
                    .clipShape(Capsule())
            }
            .buttonStyle(.plain)
        }
        .padding(.vertical, allowsWrap ? 13 : 15)
    }

    private var visibleNameText: String {
        hideSensitiveInfo ? certificateProfile.maskedName : certificateProfile.fullName
    }

    private var visibleIdCardText: String {
        hideSensitiveInfo ? certificateProfile.maskedIdCardNo : certificateProfile.rawIdCardNo
    }

    private func copyAllFields() {
        let text = [
            "姓名：\(certificateProfile.fullName)",
            "身份证号：\(certificateProfile.rawIdCardNo)",
            "失效日期：\(certificateProfile.validUntilText)",
            "住址：\(certificateProfile.addressText)",
            "民族：\(certificateProfile.ethnicityText)"
        ]
        .joined(separator: "\n")
        UIPasteboard.general.string = text
        showToast("证件信息已批量复制")
    }

    @MainActor
    private func downloadPhotos() {
        let status = PHPhotoLibrary.authorizationStatus(for: .addOnly)
        switch status {
        case .authorized, .limited:
            saveRenderedPhotos()
        case .notDetermined:
            PHPhotoLibrary.requestAuthorization(for: .addOnly) { authorizationStatus in
                Task { @MainActor in
                    switch authorizationStatus {
                    case .authorized, .limited:
                        saveRenderedPhotos()
                    default:
                        showToast("未获得相册权限")
                    }
                }
            }
        default:
            showToast("未获得相册权限")
        }
    }

    @MainActor
    private func saveRenderedPhotos() {
        guard let rendered = renderDocumentCompositeImage() else {
            showToast("生成证件图片失败")
            return
        }
        guard let imageData = rendered.jpegData(compressionQuality: 0.98) else {
            showToast("生成证件图片失败")
            return
        }

        PHPhotoLibrary.shared().performChanges({
            let creationRequest = PHAssetCreationRequest.forAsset()
            creationRequest.addResource(with: .photo, data: imageData, options: nil)
        }) { success, _ in
            Task { @MainActor in
                showToast(success ? "证件照片已保存" : "保存失败，请稍后重试")
            }
        }
    }

    @MainActor
    private func renderDocumentCompositeImage() -> UIImage? {
        let renderer = ImageRenderer(
            content: certificateCompositeView(cardHeight: 208)
                .frame(width: 360)
                .background(Color.white)
        )
        renderer.scale = UIScreen.main.scale
        return renderer.uiImage
    }

    private func certificateCompositeView(cardHeight: CGFloat) -> some View {
        VStack(spacing: 0) {
            CertificateCardView(
                profile: certificateProfile,
                isFront: true,
                revealSensitiveInfo: !hideSensitiveInfo,
                showWatermark: applyWatermark,
                textScale: 1.0
            )
            .aspectRatio(certificateCardAspectRatio, contentMode: .fit)
            .frame(maxWidth: .infinity)
        }
        .padding(20)
    }

    private func showToast(_ message: String) {
        withAnimation(.easeInOut(duration: 0.18)) {
            toastMessage = message
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.6) {
            guard toastMessage == message else {
                return
            }
            withAnimation(.easeInOut(duration: 0.18)) {
                toastMessage = nil
            }
        }
    }

    private func toastView(text: String) -> some View {
        Text(text)
            .font(.system(size: 13, weight: .medium))
            .foregroundStyle(.white)
            .padding(.horizontal, 14)
            .padding(.vertical, 9)
            .background(Color.black.opacity(0.72))
            .clipShape(Capsule())
    }

    @MainActor
    private func uploadCertificatePhoto(item: PhotosPickerItem) async {
        defer { selectedUploadPhotoItem = nil }
        guard !isUploadingCertificatePhoto else {
            return
        }
        guard let userId = userProfile?.userId, userId > 0 else {
            showToast("用户信息异常，无法上传")
            return
        }

        isUploadingCertificatePhoto = true
        defer { isUploadingCertificatePhoto = false }

        do {
            guard let rawData = try await item.loadTransferable(type: Data.self), !rawData.isEmpty else {
                showToast("读取图片失败，请重试")
                return
            }

            let uploadData: Data
            let mimeType: String
            let fileName: String
            if let image = UIImage(data: rawData), let jpegData = image.jpegData(compressionQuality: 0.92) {
                uploadData = jpegData
                mimeType = "image/jpeg"
                fileName = "certificate-\(userId)-\(Int(Date().timeIntervalSince1970)).jpg"
            } else {
                uploadData = rawData
                mimeType = "application/octet-stream"
                fileName = "certificate-\(userId)-\(Int(Date().timeIntervalSince1970)).bin"
            }

            _ = try await APIClient.shared.uploadImage(
                ownerUserId: userId,
                imageData: uploadData,
                fileName: fileName,
                mimeType: mimeType
            )

            locallyUploadedIdentity = true
            persistLocalUploadedIdentityFlag(true)
            showToast("证件照片上传成功")
        } catch {
            showToast(userFacingErrorMessage(error))
        }
    }

    private func syncLocalUploadedIdentityFlag() {
        guard let userId = userProfile?.userId, userId > 0 else {
            locallyUploadedIdentity = false
            return
        }
        locallyUploadedIdentity = UserDefaults.standard.bool(forKey: localUploadedIdentityStorageKey(for: userId))
    }

    private func persistLocalUploadedIdentityFlag(_ value: Bool) {
        guard let userId = userProfile?.userId, userId > 0 else {
            return
        }
        UserDefaults.standard.set(value, forKey: localUploadedIdentityStorageKey(for: userId))
    }

    private func localUploadedIdentityStorageKey(for userId: Int64) -> String {
        "certificate.identity.uploaded.\(userId)"
    }
}

private struct CertificateRecommendationCard: Identifiable {
    let id = UUID()
    let title: String
    let subtitle: String
    let symbolName: String
    let symbolColor: Color
    let startColor: Color
    let endColor: Color
}

private struct CertificatePhotoViewer: View {
    let profile: CertificateProfile
    let revealSensitiveInfo: Bool
    let showWatermark: Bool
    let onClose: () -> Void

    var body: some View {
        ZStack(alignment: .top) {
            Color.black.ignoresSafeArea()

            VStack(spacing: 18) {
                HStack {
                    Button(action: onClose) {
                        Image(systemName: "xmark")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundStyle(.white)
                            .frame(width: 40, height: 40)
                    }
                    .buttonStyle(.plain)

                    Spacer()
                }
                .padding(.horizontal, 18)
                .padding(.top, 10)

                Spacer(minLength: 0)

                CertificateCardView(
                    profile: profile,
                    isFront: true,
                    revealSensitiveInfo: revealSensitiveInfo,
                    showWatermark: showWatermark,
                    textScale: 2.0
                )
                .aspectRatio(certificateCardAspectRatio, contentMode: .fit)
                .padding(.horizontal, 18)

                Spacer(minLength: 0)
            }
        }
    }
}

private struct CertificateCardView: View {
    let profile: CertificateProfile
    let isFront: Bool
    let revealSensitiveInfo: Bool
    let showWatermark: Bool
    let textScale: CGFloat

    var body: some View {
        ZStack {
            if isFront, let frontImageName = profile.frontImageName {
                Image(frontImageName)
                    .resizable()
                    .interpolation(.high)
                    .scaledToFill()
            } else {
                RoundedRectangle(cornerRadius: 18, style: .continuous)
                    .fill(cardGradient)

                if isFront {
                    frontContent
                } else {
                    backContent
                }
            }

            if showWatermark {
                Text("爱付 DEMO")
                    .font(.system(size: 28 * textScale, weight: .bold))
                    .foregroundStyle(Color.black.opacity(0.10))
                    .rotationEffect(.degrees(-18))
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(Color.white.opacity(0.32), lineWidth: 1)
        )
    }

    private var cardGradient: LinearGradient {
        LinearGradient(
            colors: [
                Color(red: 0.91, green: 0.86, blue: 0.77),
                Color(red: 0.86, green: 0.80, blue: 0.71)
            ],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }

    private var frontContent: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .center) {
                ZStack {
                    Circle()
                        .fill(Color(red: 0.77, green: 0.17, blue: 0.16))
                    Text("国")
                        .font(.system(size: 10 * textScale, weight: .bold))
                        .foregroundStyle(.white)
                }
                .frame(width: 18, height: 18)

                Text("居民身份证")
                    .font(.system(size: 12 * textScale, weight: .semibold))
                    .foregroundStyle(Color(red: 0.22, green: 0.19, blue: 0.16))

                Spacer()
            }

            Spacer(minLength: 10)

            HStack(alignment: .bottom, spacing: 12) {
                VStack(alignment: .leading, spacing: 5) {
                    cardLine(title: "姓名", value: revealSensitiveInfo ? profile.fullName : profile.maskedName)
                    cardLine(title: "性别", value: profile.genderText)
                    cardLine(title: "出生", value: profile.birthDateText)
                    cardLine(title: "住址", value: profile.addressShortText)
                    cardLine(title: "号码", value: revealSensitiveInfo ? profile.rawIdCardNo : profile.maskedIdCardNo)
                }

                Spacer(minLength: 0)

                RoundedRectangle(cornerRadius: 10, style: .continuous)
                    .fill(Color.white.opacity(0.26))
                    .frame(width: 54, height: 68)
                    .overlay(
                        Image(systemName: "person.crop.rectangle")
                            .font(.system(size: 28 * textScale, weight: .medium))
                            .foregroundStyle(Color.black.opacity(0.24))
                    )
            }
        }
        .padding(14)
    }

    private func cardLine(title: String, value: String) -> some View {
        HStack(alignment: .firstTextBaseline, spacing: 4) {
            Text(title)
                .font(.system(size: 8.5 * textScale, weight: .medium))
                .foregroundStyle(Color(red: 0.34, green: 0.29, blue: 0.24))
            Text(value)
                .font(.system(size: 9.2 * textScale, weight: .medium))
                .foregroundStyle(Color(red: 0.16, green: 0.14, blue: 0.12))
                .lineLimit(1)
                .minimumScaleFactor(0.65)
        }
    }

    private var backContent: some View {
        VStack(spacing: 0) {
            Spacer(minLength: 12)

            Text("中华人民共和国")
                .font(.system(size: 14 * textScale, weight: .bold))
                .foregroundStyle(Color(red: 0.57, green: 0.16, blue: 0.12))

            Text("居民身份证")
                .font(.system(size: 22 * textScale, weight: .bold))
                .foregroundStyle(Color(red: 0.36, green: 0.16, blue: 0.14))
                .padding(.top, 3)

            Spacer(minLength: 0)

            VStack(alignment: .leading, spacing: 6) {
                Text("签发机关  爱付实名测试中心")
                    .font(.system(size: 10.5 * textScale, weight: .medium))
                    .foregroundStyle(Color(red: 0.22, green: 0.18, blue: 0.15))

                Text("有效期限  \(profile.validUntilText)")
                    .font(.system(size: 10.5 * textScale, weight: .medium))
                    .foregroundStyle(Color(red: 0.22, green: 0.18, blue: 0.15))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 16)
            .padding(.bottom, 16)
        }
    }
}

private struct CertificateProfile {
    private static let demoTemplateIdCardNo = "440200199601190027"
    private static let demoTemplateAvatarSuffix = "/demo-avatar/gujun.png"

    let fullName: String
    let maskedName: String
    let rawIdCardNo: String
    let maskedIdCardNo: String
    let genderText: String
    let frontImageName: String?
    let birthDateText: String
    let validUntilText: String
    let addressText: String
    let addressShortText: String
    let ethnicityText: String
    let documentType: String
    let hasUploadedIdentity: Bool

    init(userProfile: UserProfile?, forceUploadedIdentity: Bool = false) {
        let nickname = Self.normalized(userProfile?.nickname)
        let maskedRealName = Self.normalized(userProfile?.maskedRealName)
        let normalizedIdCardNo = Self.normalized(userProfile?.idCardNo)
        let shouldUseDemoTemplateFallback = normalizedIdCardNo == nil && Self.isDemoTemplateAvatar(userProfile?.avatarUrl)
        let useDemoTemplateIdentity = normalizedIdCardNo == Self.demoTemplateIdCardNo || shouldUseDemoTemplateFallback
        let resolvedHasUploadedIdentity = forceUploadedIdentity
            || Self.isValidIdentityCard(normalizedIdCardNo)
            || shouldUseDemoTemplateFallback

        let resolvedFullName = nickname ?? maskedRealName?.replacingOccurrences(of: "*", with: "") ?? "爱付用户"
        let resolvedMaskedName = maskedRealName ?? Self.maskName(resolvedFullName)
        let resolvedRawIdCardNo = normalizedIdCardNo
            ?? (shouldUseDemoTemplateFallback ? Self.demoTemplateIdCardNo : "待补充")

        fullName = resolvedFullName
        maskedName = resolvedMaskedName
        rawIdCardNo = resolvedRawIdCardNo
        maskedIdCardNo = Self.maskIdCard(resolvedRawIdCardNo)
        genderText = Self.genderText(from: userProfile?.gender)
        frontImageName = useDemoTemplateIdentity ? "CertificateGuJunFront" : nil
        birthDateText = Self.birthDateText(from: userProfile?.birthday)
        validUntilText = resolvedHasUploadedIdentity
            ? "2046年4月30日"
            : "待补充"
        addressText = resolvedHasUploadedIdentity
            ? "中国"
            : "待补充"
        addressShortText = Self.shortAddressText(from: addressText)
        ethnicityText = resolvedHasUploadedIdentity ? "汉" : "待补充"
        documentType = "身份证"
        hasUploadedIdentity = resolvedHasUploadedIdentity
    }

    private static func normalized(_ value: String?) -> String? {
        let trimmed = value?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? nil : trimmed
    }

    private static func isDemoTemplateAvatar(_ avatarUrl: String?) -> Bool {
        let normalizedAvatar = normalized(avatarUrl)?.lowercased() ?? ""
        guard !normalizedAvatar.isEmpty else {
            return false
        }
        return normalizedAvatar.hasSuffix(demoTemplateAvatarSuffix)
            || normalizedAvatar.contains("\(demoTemplateAvatarSuffix)?")
    }

    private static func digits(_ value: String?) -> String? {
        let normalized = normalized(value)?.filter(\.isNumber) ?? ""
        return normalized.isEmpty ? nil : normalized
    }

    private static func isValidIdentityCard(_ value: String?) -> Bool {
        guard let normalized = normalized(value) else {
            return false
        }
        let pattern = "^[0-9]{17}[0-9Xx]$"
        return normalized.range(of: pattern, options: .regularExpression) != nil
    }

    private static func maskName(_ value: String) -> String {
        guard !value.isEmpty else {
            return "未实名"
        }
        guard value.count > 1 else {
            return value
        }
        return String(value.prefix(1)) + String(repeating: "*", count: max(value.count - 1, 1))
    }

    private static func maskIdCard(_ value: String) -> String {
        let digitsOnly = value.filter(\.isNumber)
        guard digitsOnly.count >= 6 else {
            return value
        }
        return String(value.prefix(1)) + String(repeating: "*", count: max(value.count - 2, 1)) + String(value.suffix(1))
    }

    private static func genderText(from value: String?) -> String {
        switch normalized(value)?.uppercased() {
        case "FEMALE":
            return "女"
        case "MALE":
            return "男"
        default:
            return "未设置"
        }
    }

    private static func birthDateText(from value: String?) -> String {
        let normalizedValue = normalized(value) ?? ""
        if normalizedValue.count == 10 {
            return normalizedValue.replacingOccurrences(of: "-", with: ".")
        }
        return normalizedValue.isEmpty ? "待补充" : normalizedValue
    }

    private static func shortAddressText(from value: String) -> String {
        if value.count <= 12 {
            return value
        }
        return String(value.prefix(12))
    }
}
