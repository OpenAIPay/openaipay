import SwiftUI
import UIKit
import PhotosUI
import AVFoundation
import Vision
import CoreImage

struct ScanPageView: View {
    let currentUserId: Int64?
    let onBack: () -> Void
    let onOpenPayCode: () -> Void
    let onOpenAiPayPersonalTransfer: (Int64) async -> String?
    let onApplyAiPayFriendRequest: (Int64) async -> String

    @StateObject private var scanner = NativeScannerController()
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var scanResultText: String?
    @State private var alertToken = UUID()
    @State private var helperMessage: String?
    @State private var helperMessageToken = UUID()
    @State private var pendingAiPayPersonalCode: AiPayResolvedReceiveCode?
    @State private var shouldResumeAfterPersonalCodeDialogDismiss = true

    private let baseWidth: CGFloat = 402
    private let baseHeight: CGFloat = 874

    var body: some View {
        GeometryReader { proxy in
            let scaleX = proxy.size.width / baseWidth
            let scaleY = proxy.size.height / baseHeight
            let fontScale = min(scaleX, scaleY)

            ZStack {
                ScanCameraPreview(session: scanner.session)
                    .frame(width: proxy.size.width, height: proxy.size.height)
                    .overlay {
                        LinearGradient(
                            colors: [
                                Color.black.opacity(0.20),
                                Color.black.opacity(0.06),
                                Color.black.opacity(0.22)
                            ],
                            startPoint: .top,
                            endPoint: .bottom
                        )
                    }
                    .ignoresSafeArea()

                topButtons(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                flashlightSection(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                bottomActions(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)

                if !scanner.hasCameraAccess {
                    permissionPrompt(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                }

                if let helperMessage {
                    helperToast(text: helperMessage, scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                }
            }
            .frame(width: proxy.size.width, height: proxy.size.height)
            .background(Color.black)
        }
        .ignoresSafeArea()
        .task {
            scanner.onCodeDetected = { value in
                Task { @MainActor in
                    handleScannedValue(value)
                }
            }
            scanner.start()
        }
        .onDisappear {
            scanner.stop()
        }
        .onChange(of: selectedPhotoItem) { _, newValue in
            guard let newValue else {
                return
            }
            Task {
                await handlePickedPhoto(item: newValue)
            }
        }
        .confirmationDialog(
            "识别到爱付个人动态二维码",
            isPresented: Binding(
                get: { pendingAiPayPersonalCode != nil },
                set: { presented in
                    if !presented {
                        pendingAiPayPersonalCode = nil
                        let shouldResume = shouldResumeAfterPersonalCodeDialogDismiss
                        shouldResumeAfterPersonalCodeDialogDismiss = true
                        if shouldResume {
                            scanner.resumeScanning()
                        }
                    }
                }
            ),
            titleVisibility: .visible
        ) {
            Button("转账") {
                handleAiPayPersonalTransfer()
            }
            Button("加好友") {
                handleAiPayPersonalAddFriend()
            }
            Button("取消", role: .cancel) {
                pendingAiPayPersonalCode = nil
            }
        } message: {
            Text("可直接向对方转账，或发起好友申请")
        }
        .alert(
            "扫码结果",
            isPresented: Binding(
                get: { scanResultText != nil },
                set: { presented in
                    if !presented {
                        scanResultText = nil
                        scanner.resumeScanning()
                    }
                }
            ),
            presenting: scanResultText
        ) { result in
            Button("复制") {
                UIPasteboard.general.string = result
                scanResultText = nil
                showHelperMessage("已复制扫码内容")
                scanner.resumeScanning()
            }
            Button("完成", role: .cancel) {
                scanResultText = nil
                scanner.resumeScanning()
            }
        } message: { result in
            Text(result)
        }
        .id(alertToken)
    }

    private func topButtons(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        Button(action: onBack) {
            Image(systemName: "chevron.left")
                .font(.system(size: 22 * fontScale, weight: .medium))
                .foregroundStyle(.white)
                .frame(width: 52 * scaleX, height: 52 * scaleY)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .position(x: 22 * scaleX, y: 67 * scaleY)
        .accessibilityIdentifier("scan_back")
    }

    private func flashlightSection(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        VStack(spacing: 8 * scaleY) {
            Button {
                scanner.toggleTorch()
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.16) {
                    showHelperMessage(scanner.isTorchOn ? "闪光灯已打开" : "闪光灯已关闭")
                }
            } label: {
                Image(systemName: scanner.isTorchOn ? "flashlight.on.fill" : "flashlight.off.fill")
                    .font(.system(size: 29 * fontScale, weight: .medium))
                    .foregroundStyle(.white)
                    .frame(width: 62 * scaleX, height: 56 * scaleY)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityIdentifier("scan_flashlight")

            Text(scanner.isTorchOn ? "轻触关闭" : "轻触照亮")
                .font(.system(size: 13 * fontScale, weight: .regular))
                .foregroundStyle(Color.white.opacity(0.92))

            Text("扫二维码/条码/小程序码")
                .font(.system(size: 16 * fontScale, weight: .medium))
                .foregroundStyle(Color.white.opacity(0.92))
                .padding(.top, 18 * scaleY)
        }
        .position(x: 201 * scaleX, y: 535 * scaleY)
    }

    private func bottomActions(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        HStack(spacing: 208 * scaleX) {
            Button {
                onOpenPayCode()
            } label: {
                scanRoundAction(iconName: "qrcode.viewfinder", title: "收付款", scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
            }
            .buttonStyle(.plain)
            .accessibilityIdentifier("scan_paycode")

            PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                scanRoundAction(iconName: "photo.on.rectangle.angled", title: "相册", scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
            }
            .buttonStyle(.plain)
            .accessibilityIdentifier("scan_album")
        }
        .position(x: 201 * scaleX, y: 650 * scaleY)
    }

    private func scanRoundAction(iconName: String, title: String, scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        VStack(spacing: 4 * scaleY) {
            ZStack {
                Circle()
                    .fill(Color.white.opacity(0.16))
                    .frame(width: 50 * scaleX, height: 50 * scaleY)
                Image(systemName: iconName)
                    .font(.system(size: 22 * fontScale, weight: .medium))
                    .foregroundStyle(.white)
            }
            Text(title)
                .font(.system(size: 13 * fontScale, weight: .regular))
                .foregroundStyle(Color.white.opacity(0.9))
        }
        .frame(width: 78 * scaleX, height: 84 * scaleY)
        .contentShape(Rectangle())
    }

    private func helperToast(text: String, scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        Text(text)
            .font(.system(size: 14 * fontScale, weight: .medium))
            .foregroundStyle(.white)
            .padding(.horizontal, 14 * scaleX)
            .frame(height: 36 * scaleY)
            .background(
                Capsule()
                    .fill(Color.black.opacity(0.68))
            )
            .position(x: 201 * scaleX, y: 716 * scaleY)
    }

    private func permissionPrompt(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        VStack(spacing: 10 * scaleY) {
            Text("请允许访问相机")
                .font(.system(size: 18 * fontScale, weight: .semibold))
                .foregroundStyle(.white)
            Button("打开系统设置") {
                guard let url = URL(string: UIApplication.openSettingsURLString) else {
                    return
                }
                UIApplication.shared.open(url)
            }
            .font(.system(size: 15 * fontScale, weight: .medium))
            .foregroundStyle(AppTheme.palette.brandPrimary)
        }
        .padding(.horizontal, 20 * scaleX)
        .padding(.vertical, 18 * scaleY)
        .background(
            RoundedRectangle(cornerRadius: 18 * fontScale, style: .continuous)
                .fill(Color.black.opacity(0.66))
        )
        .position(x: 201 * scaleX, y: 300 * scaleY)
    }

    @MainActor
    private func handlePickedPhoto(item: PhotosPickerItem) async {
        defer {
            selectedPhotoItem = nil
        }
        guard let data = try? await item.loadTransferable(type: Data.self),
              let image = UIImage(data: data) else {
            showHelperMessage("读取相册图片失败")
            return
        }
        do {
            if let result = try await NativeScannerController.detectCode(in: image) {
                handleScannedValue(result)
            } else {
                showHelperMessage("未识别到二维码或条码")
                scanner.resumeScanning()
            }
        } catch {
            showHelperMessage("相册识别失败")
            scanner.resumeScanning()
        }
    }

    @MainActor
    private func showHelperMessage(_ message: String) {
        helperMessage = message
        helperMessageToken = UUID()
        let currentToken = helperMessageToken
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.8) {
            guard helperMessageToken == currentToken else {
                return
            }
            helperMessage = nil
        }
    }

    @MainActor
    private func handleScannedValue(_ rawValue: String) {
        let trimmedValue = rawValue.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedValue.isEmpty else {
            scanner.resumeScanning()
            return
        }

        scanResultText = nil
        if AiPayReceiveCodeFactory.isAiPayPersonalPayload(trimmedValue) {
            guard let resolvedCode = AiPayReceiveCodeFactory.resolvePayload(trimmedValue) else {
                showHelperMessage("爱付个人码已失效，请对方刷新后重试")
                scanner.resumeScanning()
                return
            }
            if let currentUserId, currentUserId > 0, currentUserId == resolvedCode.userId {
                showHelperMessage("这是你自己的个人码")
                scanner.resumeScanning()
                return
            }
            shouldResumeAfterPersonalCodeDialogDismiss = true
            pendingAiPayPersonalCode = resolvedCode
            return
        }

        pendingAiPayPersonalCode = nil
        scanResultText = trimmedValue
        alertToken = UUID()
    }

    private func handleAiPayPersonalTransfer() {
        guard let resolvedCode = pendingAiPayPersonalCode else {
            return
        }
        shouldResumeAfterPersonalCodeDialogDismiss = false
        pendingAiPayPersonalCode = nil

        Task { @MainActor in
            if let errorMessage = await onOpenAiPayPersonalTransfer(resolvedCode.userId) {
                showHelperMessage(errorMessage)
                scanner.resumeScanning()
            }
        }
    }

    private func handleAiPayPersonalAddFriend() {
        guard let resolvedCode = pendingAiPayPersonalCode else {
            return
        }
        shouldResumeAfterPersonalCodeDialogDismiss = false
        pendingAiPayPersonalCode = nil

        Task { @MainActor in
            let resultMessage = await onApplyAiPayFriendRequest(resolvedCode.userId)
            showHelperMessage(resultMessage)
            scanner.resumeScanning()
        }
    }
}

private struct ScanCameraPreview: UIViewRepresentable {
    let session: AVCaptureSession

    func makeUIView(context: Context) -> PreviewView {
        let view = PreviewView()
        view.previewLayer.videoGravity = .resizeAspectFill
        view.previewLayer.session = session
        return view
    }

    func updateUIView(_ uiView: PreviewView, context: Context) {
        uiView.previewLayer.session = session
    }

    final class PreviewView: UIView {
        override class var layerClass: AnyClass {
            AVCaptureVideoPreviewLayer.self
        }

        var previewLayer: AVCaptureVideoPreviewLayer {
            guard let layer = layer as? AVCaptureVideoPreviewLayer else {
                fatalError("unexpected layer type")
            }
            return layer
        }
    }
}

private final class NativeScannerController: NSObject, ObservableObject, AVCaptureMetadataOutputObjectsDelegate {
    let session = AVCaptureSession()
    var onCodeDetected: ((String) -> Void)?

    @Published private(set) var hasCameraAccess = true
    @Published private(set) var isTorchOn = false

    private let sessionQueue = DispatchQueue(label: "cn.openaipay.scan.session")
    private var isConfigured = false
    private var isHandlingCode = false
    private weak var currentDevice: AVCaptureDevice?

    func start() {
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        switch status {
        case .authorized:
            hasCameraAccess = true
            configureIfNeededAndStart()
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                guard let self else {
                    return
                }
                DispatchQueue.main.async {
                    self.hasCameraAccess = granted
                }
                if granted {
                    self.configureIfNeededAndStart()
                }
            }
        default:
            hasCameraAccess = false
        }
    }

    func stop() {
        sessionQueue.async { [weak self] in
            guard let self else {
                return
            }
            if let device = self.currentDevice, device.hasTorch, device.torchMode == .on {
                do {
                    try device.lockForConfiguration()
                    device.torchMode = .off
                    device.unlockForConfiguration()
                } catch {
                    // Ignore torch cleanup failures when leaving the scan page.
                }
            }
            if self.session.isRunning {
                self.session.stopRunning()
            }
            DispatchQueue.main.async {
                self.isTorchOn = false
            }
        }
    }

    func resumeScanning() {
        isHandlingCode = false
        start()
    }

    func toggleTorch() {
        sessionQueue.async { [weak self] in
            guard let self, let device = self.currentDevice, device.hasTorch else {
                return
            }
            do {
                try device.lockForConfiguration()
                if device.torchMode == .on {
                    device.torchMode = .off
                    DispatchQueue.main.async {
                        self.isTorchOn = false
                    }
                } else {
                    try device.setTorchModeOn(level: AVCaptureDevice.maxAvailableTorchLevel)
                    DispatchQueue.main.async {
                        self.isTorchOn = true
                    }
                }
                device.unlockForConfiguration()
            } catch {
                DispatchQueue.main.async {
                    self.isTorchOn = false
                }
            }
        }
    }

    private func configureIfNeededAndStart() {
        sessionQueue.async { [weak self] in
            guard let self else {
                return
            }
            if !self.isConfigured {
                self.configureSession()
            }
            guard !self.session.isRunning else {
                return
            }
            self.session.startRunning()
        }
    }

    private func configureSession() {
        session.beginConfiguration()
        session.sessionPreset = .high

        guard let device = AVCaptureDevice.default(for: .video),
              let input = try? AVCaptureDeviceInput(device: device),
              session.canAddInput(input) else {
            session.commitConfiguration()
            DispatchQueue.main.async {
                self.hasCameraAccess = false
            }
            return
        }

        currentDevice = device
        session.addInput(input)

        let output = AVCaptureMetadataOutput()
        guard session.canAddOutput(output) else {
            session.commitConfiguration()
            return
        }
        session.addOutput(output)
        output.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
        let preferredTypes: [AVMetadataObject.ObjectType] = [
            .qr,
            .ean8,
            .ean13,
            .code39,
            .code93,
            .code128,
            .pdf417,
            .aztec,
            .dataMatrix,
            .upce,
            .interleaved2of5,
            .itf14
        ]
        let supportedTypes = preferredTypes.filter { output.availableMetadataObjectTypes.contains($0) }
        output.metadataObjectTypes = supportedTypes.isEmpty ? output.availableMetadataObjectTypes : supportedTypes

        session.commitConfiguration()
        isConfigured = true
    }

    func metadataOutput(
        _ output: AVCaptureMetadataOutput,
        didOutput metadataObjects: [AVMetadataObject],
        from connection: AVCaptureConnection
    ) {
        guard !isHandlingCode else {
            return
        }
        guard let readableObject = metadataObjects.compactMap({ $0 as? AVMetadataMachineReadableCodeObject }).first,
              let value = readableObject.stringValue,
              !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return
        }
        isHandlingCode = true
        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
        stop()
        onCodeDetected?(value)
    }

    static func detectCode(in image: UIImage) async throws -> String? {
        try await withCheckedThrowingContinuation { continuation in
            DispatchQueue.global(qos: .userInitiated).async {
                do {
                    guard let cgImage = Self.cgImage(from: image) else {
                        continuation.resume(returning: nil)
                        return
                    }
                    let request = VNDetectBarcodesRequest()
                    let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
                    try handler.perform([request])
                    let result = request.results?.compactMap { $0.payloadStringValue }.first
                    continuation.resume(returning: result)
                } catch {
                    continuation.resume(throwing: error)
                }
            }
        }
    }

    private static func cgImage(from image: UIImage) -> CGImage? {
        if let cgImage = image.cgImage {
            return cgImage
        }
        guard let ciImage = image.ciImage ?? image.cgImage.map(CIImage.init) ?? CIImage(image: image) else {
            return nil
        }
        return CIContext().createCGImage(ciImage, from: ciImage.extent)
    }
}
