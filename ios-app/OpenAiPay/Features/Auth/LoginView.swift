import SwiftUI
import UIKit
import PhotosUI

struct LoginView: View {
    private enum InputField: Hashable {
        case loginId
        case password
        case smsCode
        case registerPhone
        case registerIdentity
        case registerAuthName
        case registerAuthIdentity
        case registerLoginPassword
    }

    private enum IdentityKeyboardTarget {
        case registerIdentity
        case registerAuthIdentity
    }

    @EnvironmentObject private var appState: AppState
    @StateObject private var viewModel = LoginViewModel()
    @FocusState private var focusedField: InputField?
    @State private var showOtherVerificationSheet = false
    @State private var isAccountDropdownPresented = false
    @State private var showRegisterAgreementDialog = false
    @State private var showRegisterIdentityInvalidAlert = false
    @State private var registerIdentityInvalidMessage = ""
    @State private var identityKeyboardTarget: IdentityKeyboardTarget?
    @State private var registerFaceRecognitionTask: Task<Void, Never>?
    @State private var registerIdFrontPhotoItem: PhotosPickerItem?
    @State private var registerIdFrontPhotoPreview: UIImage?
    @State private var registerAuthNameValidationMessage: String?
    @State private var smsResendCountdown = 60
    @State private var registerSmsResendCountdown = 60
    @State private var smsResendCountdownTask: Task<Void, Never>?
    @State private var registerSmsResendCountdownTask: Task<Void, Never>?

    var body: some View {
        GeometryReader { proxy in
            let width = proxy.size.width
            let height = proxy.size.height
            let scaleX = width / 402.0
            let scaleY = height / 874.0

            ZStack(alignment: .topLeading) {
                backgroundLayer(scaleX: scaleX, scaleY: scaleY, width: width, height: height)

                switch viewModel.step {
                case .account:
                    accountSheetLayer(scaleX: scaleX, scaleY: scaleY, width: width, height: height)
                case .verifyMethodSelect:
                    Color.clear
                case .mobileVerify:
                    mobileVerifyLayer(scaleX: scaleX, scaleY: scaleY, width: width, height: height)
                case .smsVerify:
                    smsVerifyLayer(scaleX: scaleX, scaleY: scaleY, width: width, height: height)
                case .passwordVerify:
                    passwordVerifyLayer(scaleX: scaleX, scaleY: scaleY, width: width, height: height)
                case .phoneRegister:
                    phoneRegisterLayer(scaleX: scaleX, scaleY: scaleY, width: width, height: height)
                case .registerSmsVerify:
                    registerSmsVerifyLayer(scaleX: scaleX, scaleY: scaleY, width: width, height: height)
                case .registerIdentityInput:
                    registerIdentityInputLayer(scaleX: scaleX, scaleY: scaleY, width: width, height: height)
                case .registerIdentityAuth:
                    registerIdentityAuthLayer(scaleX: scaleX, scaleY: scaleY, width: width, height: height)
                case .registerFaceRecognition:
                    registerFaceRecognitionLayer(scaleX: scaleX, scaleY: scaleY, width: width, height: height)
                case .registerFaceSuccess:
                    registerFaceSuccessLayer(scaleX: scaleX, scaleY: scaleY, width: width, height: height)
                }

                if viewModel.step != .account, showOtherVerificationSheet {
                    otherVerificationSheet(scaleX: scaleX, scaleY: scaleY, width: width, height: height)
                }

                if showRegisterAgreementDialog {
                    registerAgreementDialog(scaleX: scaleX, scaleY: scaleY, width: width, height: height)
                        .transition(.opacity)
                        .zIndex(20)
                }

                if identityKeyboardTarget != nil {
                    identityCardKeyboardOverlay(scaleX: scaleX, scaleY: scaleY)
                        .zIndex(40)
                }
            }
            .frame(width: width, height: height)
            .contentShape(Rectangle())
            .onTapGesture {
                if identityKeyboardTarget != nil {
                    return
                }
                focusedField = nil
                isAccountDropdownPresented = false
                identityKeyboardTarget = nil
            }
            .onChange(of: viewModel.step) { _, newStep in
                registerFaceRecognitionTask?.cancel()
                registerFaceRecognitionTask = nil
                smsResendCountdownTask?.cancel()
                smsResendCountdownTask = nil
                registerSmsResendCountdownTask?.cancel()
                registerSmsResendCountdownTask = nil

                if newStep == .verifyMethodSelect {
                    showOtherVerificationSheet = true
                } else if newStep == .account {
                    showOtherVerificationSheet = false
                }
                if newStep == .smsVerify || newStep == .registerSmsVerify {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                        focusedField = .smsCode
                    }
                } else if newStep == .phoneRegister {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                        focusedField = .registerPhone
                    }
                } else if newStep == .registerIdentityInput {
                    focusedField = nil
                    identityKeyboardTarget = .registerIdentity
                } else if newStep == .registerIdentityAuth {
                    focusedField = nil
                    identityKeyboardTarget = nil
                } else if newStep == .registerFaceRecognition {
                    focusedField = nil
                    identityKeyboardTarget = nil
                    registerFaceRecognitionTask = Task {
                        try? await Task.sleep(nanoseconds: 5_000_000_000)
                        guard !Task.isCancelled else {
                            return
                        }
                        await MainActor.run {
                            viewModel.completeRegisterFaceRecognition()
                        }
                    }
                } else {
                    focusedField = nil
                }
                if newStep == .smsVerify {
                    startSmsResendCountdown()
                } else if newStep == .registerSmsVerify {
                    startRegisterSmsResendCountdown()
                }
                if newStep != .phoneRegister {
                    showRegisterAgreementDialog = false
                }
                if newStep != .account {
                    isAccountDropdownPresented = false
                }
                if newStep != .registerIdentityInput && newStep != .registerIdentityAuth {
                    identityKeyboardTarget = nil
                }
                if newStep != .registerIdentityAuth {
                    registerAuthNameValidationMessage = nil
                }
            }
            .onChange(of: focusedField) { oldField, newField in
                if newField != .registerIdentity && newField != .registerAuthIdentity {
                    identityKeyboardTarget = nil
                }

                if viewModel.step == .registerIdentityAuth {
                    if oldField == .registerAuthName && newField != .registerAuthName {
                        registerAuthNameValidationMessage = viewModel.validateRegisterAuthFullName()
                    } else if newField == .registerAuthName {
                        registerAuthNameValidationMessage = nil
                    }
                }

                guard viewModel.step == .account else {
                    isAccountDropdownPresented = false
                    return
                }
                if newField != nil {
                    isAccountDropdownPresented = false
                }
            }
            .onChange(of: registerIdFrontPhotoItem) { _, newItem in
                guard let newItem else {
                    return
                }
                Task {
                    guard let data = try? await newItem.loadTransferable(type: Data.self),
                          let image = UIImage(data: data) else {
                        return
                    }
                    await MainActor.run {
                        registerIdFrontPhotoPreview = image
                    }
                }
            }
        }
        .ignoresSafeArea()
        .overlay(alignment: .top) {
            ScreenshotStatusMaskView(referenceAssetName: "LoginMaskFallback")
        }
        .task(id: appState.deviceId) {
            await viewModel.loadPresetLoginAccounts(deviceId: appState.deviceId)
        }
        .onDisappear {
            registerFaceRecognitionTask?.cancel()
            registerFaceRecognitionTask = nil
            smsResendCountdownTask?.cancel()
            smsResendCountdownTask = nil
            registerSmsResendCountdownTask?.cancel()
            registerSmsResendCountdownTask = nil
        }
        .alert("身份证号校验失败", isPresented: $showRegisterIdentityInvalidAlert) {
            Button("我知道了", role: .cancel) { }
        } message: {
            Text(registerIdentityInvalidMessage)
        }
    }

    private func backgroundLayer(scaleX: CGFloat, scaleY: CGFloat, width: CGFloat, height: CGFloat) -> some View {
        return ZStack {
            LinearGradient(
                colors: [Color(red: 0.88, green: 0.93, blue: 0.96), Color(red: 0.93, green: 0.89, blue: 0.94)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            RadialGradient(
                colors: [AppTheme.palette.brandSubtleBackground.opacity(0.28), .clear],
                center: .topTrailing,
                startRadius: 0,
                endRadius: 250 * scaleX
            )
            .offset(x: 66 * scaleX, y: 40 * scaleY)

            if viewModel.step != .account {
                topBackButton(scaleX: scaleX, scaleY: scaleY)
                    .position(x: 21 * scaleX, y: 78 * scaleY)
            }

            AipayLogoView(scaleX: scaleX, scaleY: scaleY)
                .position(x: width / 2, y: 185 * scaleY)
        }
        .frame(width: width, height: height)
    }

    private func topBackButton(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        Button {
            viewModel.back()
        } label: {
            Image(systemName: "chevron.left")
                .font(.system(size: 17 * scaleX, weight: .medium))
                .foregroundStyle(Color.black.opacity(viewModel.step == .account ? 0.32 : 0.68))
                .frame(width: 30 * scaleX, height: 30 * scaleY)
        }
        .buttonStyle(.plain)
        .disabled(viewModel.step == .account)
    }

    private func mobileVerifyLayer(scaleX: CGFloat, scaleY: CGFloat, width: CGFloat, height: CGFloat) -> some View {
        VStack(spacing: 0) {
            Spacer(minLength: 470 * scaleY)

            HStack(spacing: 10 * scaleX) {
                RoundedRectangle(cornerRadius: 5 * scaleX)
                    .fill(AppTheme.palette.brandPrimary)
                    .frame(width: 28 * scaleX, height: 28 * scaleY)
                    .overlay(
                        Text("爱")
                            .font(.system(size: 19 * scaleX, weight: .bold, design: .rounded))
                            .foregroundStyle(.white)
                    )

                Text(viewModel.maskedLoginId)
                    .font(.system(size: 38 * scaleX / 1.5, weight: .semibold))
                    .foregroundStyle(Color.black.opacity(0.76))
                    .lineLimit(1)

                Button("换号") {
                    viewModel.switchAccount()
                }
                .font(.system(size: 16 * scaleX))
                .foregroundStyle(Color(red: 0.29, green: 0.44, blue: 0.68))
                .buttonStyle(.plain)
            }
            .frame(maxWidth: .infinity)
            .padding(.horizontal, 50 * scaleX)

            Text("同意爱付向运营商获取完整本机号码")
                .font(.system(size: 14 * scaleX))
                .foregroundStyle(Color.black.opacity(0.35))
                .padding(.top, 6 * scaleY)

            if let errorMessage = viewModel.errorMessage, !errorMessage.isEmpty {
                Text(errorMessage)
                    .font(.system(size: 14 * scaleX, weight: .medium))
                    .foregroundStyle(Color(red: 0.91, green: 0.31, blue: 0.27))
                    .frame(maxWidth: .infinity, alignment: .center)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 28 * scaleX)
                    .padding(.top, 12 * scaleY)
            }

            Button {
                Task {
                    await viewModel.loginWithMobile(appState: appState)
                }
            } label: {
                Text(viewModel.isLoading ? "登录中..." : "本机号码一键登录")
                    .font(.system(size: 20 * scaleX, weight: .semibold))
                    .foregroundStyle(Color.white.opacity(0.95))
                    .frame(maxWidth: .infinity)
                    .frame(height: 49 * scaleY)
                    .background(
                        RoundedRectangle(cornerRadius: 24.5 * scaleX)
                            .fill(AppTheme.palette.brandPrimary)
                    )
            }
            .buttonStyle(.plain)
            .disabled(viewModel.isLoading)
            .padding(.horizontal, 20 * scaleX)
            .padding(.top, 18 * scaleY)

            Button("其他验证方式 >") {
                showOtherVerificationSheet = true
            }
            .font(.system(size: 16 * scaleX, weight: .medium))
            .foregroundStyle(Color(red: 0.35, green: 0.43, blue: 0.58))
            .buttonStyle(.plain)
            .padding(.top, 16 * scaleY)

            Spacer(minLength: 0)

            Button("注册账号") {
                viewModel.enterPhoneRegister()
            }
            .font(.system(size: 26 * scaleX / 1.5, weight: .semibold))
            .foregroundStyle(Color(red: 0.38, green: 0.50, blue: 0.71))
            .buttonStyle(.plain)
            .frame(maxWidth: .infinity, alignment: .center)
            .padding(.bottom, 78 * scaleY)
        }
        .frame(width: width, height: height, alignment: .top)
    }

    private func otherVerificationSheet(scaleX: CGFloat, scaleY: CGFloat, width: CGFloat, height: CGFloat) -> some View {
        ZStack {
            Color.black.opacity(0.34)
                .ignoresSafeArea()
                .onTapGesture {
                    dismissVerificationSheet()
                }

            VStack(spacing: 12 * scaleY) {
                VStack(spacing: 0) {
                    ForEach(Array(availableVerificationOptions.enumerated()), id: \.offset) { index, option in
                        verificationRow(
                            title: option.title,
                            icon: option.icon,
                            scaleX: scaleX,
                            scaleY: scaleY
                        ) {
                            selectVerificationMethod(option.method)
                        }

                        if index < availableVerificationOptions.count - 1 {
                            Divider()
                        }
                    }
                }
                .background(Color(red: 0.97, green: 0.97, blue: 0.97))
                .clipShape(RoundedRectangle(cornerRadius: 12 * scaleX))

                Button("取消") {
                    dismissVerificationSheet()
                }
                .font(.system(size: 18 * scaleX, weight: .medium))
                .foregroundStyle(Color.black.opacity(0.6))
                .frame(maxWidth: .infinity)
                .frame(height: 56 * scaleY)
                .background(Color(red: 0.97, green: 0.97, blue: 0.97))
                .clipShape(RoundedRectangle(cornerRadius: 12 * scaleX))
            }
            .padding(.horizontal, 16 * scaleX)
            .frame(maxWidth: 350 * scaleX)
        }
        .frame(width: width, height: height)
    }

    private func verificationRow(
        title: String,
        icon: String,
        scaleX: CGFloat,
        scaleY: CGFloat,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            HStack(spacing: 12 * scaleX) {
                Image(systemName: icon)
                    .font(.system(size: 18 * scaleX, weight: .medium))
                    .foregroundStyle(Color.black.opacity(0.58))
                    .frame(width: 20 * scaleX, height: 20 * scaleY)
                Text(title)
                    .font(.system(size: 26 * scaleX / 1.5))
                    .foregroundStyle(Color.black.opacity(0.7))
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.system(size: 16 * scaleX, weight: .medium))
                    .foregroundStyle(Color.gray.opacity(0.6))
            }
            .padding(.horizontal, 14 * scaleX)
            .frame(maxWidth: .infinity, alignment: .leading)
            .frame(height: 56 * scaleY)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .frame(maxWidth: .infinity)
    }

    private func accountSheetLayer(scaleX: CGFloat, scaleY: CGFloat, width: CGFloat, height: CGFloat) -> some View {
        let sheetTop = 254 * scaleY
        let contentHeight = max(height - sheetTop, 0)
        let dropdownHeight = shouldShowAccountDropdown ? accountDropdownHeight(scaleY: scaleY) : 0
        let dropdownSpacing = shouldShowAccountDropdown ? 10 * scaleY : 0

        return ZStack(alignment: .topLeading) {
            sheetHeader(scaleX: scaleX, scaleY: scaleY)
                .frame(width: width)
                .offset(y: 24 * scaleY)

            inputCell(scaleX: scaleX, scaleY: scaleY)
                .frame(width: 346 * scaleX, height: 49 * scaleY)
                .offset(x: 20 * scaleX, y: 84 * scaleY)

            if shouldShowAccountDropdown {
                accountDropdown(scaleX: scaleX, scaleY: scaleY)
                    .frame(width: 346 * scaleX, height: dropdownHeight)
                    .offset(x: 20 * scaleX, y: 140 * scaleY)
                    .zIndex(3)
            }

            actionButton(scaleX: scaleX, scaleY: scaleY)
                .frame(width: 362 * scaleX, height: 49 * scaleY)
                .offset(x: 20 * scaleX, y: 152 * scaleY + dropdownHeight + dropdownSpacing)

            Button("注册账号") {
                viewModel.enterPhoneRegister()
            }
            .font(.system(size: 22 * scaleX / 1.5, weight: .semibold))
            .foregroundStyle(Color(red: 0.38, green: 0.50, blue: 0.71))
            .buttonStyle(.plain)
            .frame(width: 362 * scaleX, alignment: .center)
            .offset(x: 20 * scaleX, y: 234 * scaleY + dropdownHeight + dropdownSpacing)

            if appState.demoAutoLoginEnabled {
                Button {
                    viewModel.loginWithDemoAuto(appState: appState)
                } label: {
                    Text(viewModel.isDemoAutoLoginLoading ? "登录中..." : "演示账号自动登录")
                        .font(.custom("Microsoft YaHei", size: 19 * scaleX / 1.5))
                        .fontWeight(.semibold)
                        .lineLimit(1)
                        .minimumScaleFactor(0.8)
                        .foregroundStyle(Color.white)
                        .frame(width: 152 * scaleX, height: 31 * scaleY, alignment: .center)
                        .background(
                            ZStack {
                                Capsule(style: .continuous)
                                    .fill(
                                        LinearGradient(
                                            colors: [
                                                Color(red: 1.00, green: 0.66, blue: 0.29),
                                                Color(red: 0.90, green: 0.45, blue: 0.12)
                                            ],
                                            startPoint: .top,
                                            endPoint: .bottom
                                        )
                                    )

                                Capsule(style: .continuous)
                                    .stroke(Color.white.opacity(0.55), lineWidth: 1)
                                    .padding(0.5)

                                Capsule(style: .continuous)
                                    .fill(
                                        LinearGradient(
                                            colors: [Color.white.opacity(0.34), Color.white.opacity(0.02)],
                                            startPoint: .top,
                                            endPoint: .center
                                        )
                                    )
                                    .padding(.horizontal, 2 * scaleX)
                                    .padding(.vertical, 1 * scaleY)
                                    .offset(y: -4 * scaleY)
                            }
                        )
                        .shadow(color: Color.black.opacity(0.22), radius: 6 * scaleY, x: 0, y: 3 * scaleY)
                        .shadow(color: Color(red: 0.98, green: 0.78, blue: 0.50).opacity(0.45), radius: 2 * scaleY, x: 0, y: -1 * scaleY)
                        .frame(width: 362 * scaleX, alignment: .center)
                }
                .buttonStyle(.plain)
                .allowsHitTesting(!viewModel.isDemoAutoLoginLoading)
                .offset(x: 20 * scaleX, y: 276 * scaleY + dropdownHeight + dropdownSpacing)
            }

            if let errorMessage = viewModel.errorMessage {
                if viewModel.shouldShowRegisteredDeviceOnlyErrorAboveAccountInput {
                    Text(errorMessage)
                        .font(.system(size: 14 * scaleX, weight: .medium))
                        .foregroundStyle(Color(red: 0.91, green: 0.31, blue: 0.27))
                        .frame(width: width, alignment: .center)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 24 * scaleX)
                        .offset(y: 58 * scaleY)
                } else {
                    Text(errorMessage)
                        .font(.system(size: 14 * scaleX, weight: .medium))
                        .foregroundStyle(Color(red: 0.91, green: 0.31, blue: 0.27))
                        .frame(width: 362 * scaleX, alignment: .center)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 12 * scaleX)
                        .offset(x: 20 * scaleX, y: 122 * scaleY + dropdownHeight + dropdownSpacing)
                }
            }
        }
        .frame(width: width, height: contentHeight, alignment: .topLeading)
        .background(Color(red: 0.96, green: 0.96, blue: 0.97))
        .clipShape(RoundedCorner(radius: 22 * scaleX, corners: [.topLeft, .topRight]))
        .offset(y: sheetTop)
    }

    private func passwordVerifyLayer(scaleX: CGFloat, scaleY: CGFloat, width: CGFloat, height: CGFloat) -> some View {
        let sheetTop = 254 * scaleY
        let contentHeight = max(height - sheetTop, 0)

        return ZStack(alignment: .topLeading) {
            passwordInputCell(scaleX: scaleX, scaleY: scaleY)
                .frame(width: 362 * scaleX, height: 49 * scaleY)
                .offset(x: 20 * scaleX, y: 44 * scaleY)

            passwordActionButton(scaleX: scaleX, scaleY: scaleY)
                .frame(width: 362 * scaleX, height: 49 * scaleY)
                .offset(x: 20 * scaleX, y: 113 * scaleY)

            Button("其他验证方式 >") {
                showOtherVerificationSheet = true
            }
            .font(.system(size: 16 * scaleX, weight: .medium))
            .foregroundStyle(Color(red: 0.35, green: 0.43, blue: 0.58))
            .buttonStyle(.plain)
            .offset(x: 142 * scaleX, y: 178 * scaleY)

            if let errorMessage = viewModel.errorMessage {
                Text(errorMessage)
                    .font(.system(size: 14 * scaleX))
                    .foregroundStyle(Color(red: 0.91, green: 0.31, blue: 0.27))
                    .offset(x: 20 * scaleX, y: 230 * scaleY)
            }

            bottomLinks(scaleX: scaleX, scaleY: scaleY)
                .offset(x: 131 * scaleX, y: contentHeight - 92 * scaleY)
        }
        .frame(width: width, height: contentHeight, alignment: .topLeading)
        .background(Color(red: 0.96, green: 0.96, blue: 0.97))
        .clipShape(RoundedCorner(radius: 22 * scaleX, corners: [.topLeft, .topRight]))
        .offset(y: sheetTop)
    }

    private func phoneRegisterLayer(scaleX: CGFloat, scaleY: CGFloat, width: CGFloat, height: CGFloat) -> some View {
        let sheetTop = 252 * scaleY
        let contentHeight = max(height - sheetTop, 0)
        let canSubmitRegister = viewModel.isRegisterPhoneValid

        let phoneTextBinding = Binding<String>(
            get: { viewModel.registerPhoneNumber },
            set: { value in
                let digits = String(value.filter(\.isNumber).prefix(15))
                if digits != viewModel.registerPhoneNumber {
                    viewModel.registerPhoneNumber = digits
                }
            }
        )

        return ZStack(alignment: .topLeading) {
            HStack(spacing: 0) {
                Button {
                    viewModel.back()
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 17 * scaleX, weight: .medium))
                        .foregroundStyle(Color.black.opacity(0.64))
                        .frame(width: 36 * scaleX, height: 36 * scaleY)
                }
                .buttonStyle(.plain)

                Spacer(minLength: 0)

                Text("输入手机号")
                    .font(.system(size: 30 * scaleX / 1.5, weight: .semibold))
                    .foregroundStyle(Color.black.opacity(0.76))

                Spacer(minLength: 0)

                Color.clear
                    .frame(width: 36 * scaleX, height: 36 * scaleY)
            }
            .frame(width: 362 * scaleX)
            .offset(x: 20 * scaleX, y: 18 * scaleY)

            HStack(spacing: 0) {
                HStack(spacing: 4 * scaleX) {
                    Text("+86")
                        .font(.system(size: 16 * scaleX, weight: .medium))
                        .foregroundStyle(Color.black.opacity(0.72))
                    Image(systemName: "chevron.down")
                        .font(.system(size: 12 * scaleX, weight: .medium))
                        .foregroundStyle(Color.black.opacity(0.36))
                }
                .frame(width: 80 * scaleX, height: 48 * scaleY)

                Rectangle()
                    .fill(Color(red: 0.84, green: 0.87, blue: 0.92))
                    .frame(width: 1, height: 22 * scaleY)

                HStack(spacing: 8 * scaleX) {
                    ZStack(alignment: .leading) {
                        if viewModel.registerPhoneNumber.isEmpty {
                            Text("输入手机号，使用爱付")
                                .font(.system(size: 16 * scaleX))
                                .foregroundStyle(Color.black.opacity(0.28))
                        }

                        TextField("", text: phoneTextBinding)
                            .keyboardType(.numberPad)
                            .textContentType(.telephoneNumber)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                            .focused($focusedField, equals: .registerPhone)
                            .font(.system(size: 16 * scaleX))
                            .foregroundStyle(Color.black.opacity(0.78))
                            .accessibilityIdentifier("auth_phone_register_input")
                    }

                    if focusedField == .registerPhone && !viewModel.registerPhoneNumber.isEmpty {
                        Button {
                            viewModel.registerPhoneNumber = ""
                            focusedField = .registerPhone
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .font(.system(size: 17 * scaleX, weight: .semibold))
                                .foregroundStyle(Color.gray.opacity(0.58))
                                .frame(width: 22 * scaleX, height: 22 * scaleY)
                                .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.leading, 12 * scaleX)
                .padding(.trailing, 14 * scaleX)
            }
            .frame(width: 362 * scaleX, height: 48 * scaleY)
            .background(Color.white)
            .overlay(
                RoundedRectangle(cornerRadius: 24 * scaleX, style: .continuous)
                    .stroke(AppTheme.palette.brandBorder, lineWidth: 1.6 * scaleX)
            )
            .clipShape(RoundedRectangle(cornerRadius: 24 * scaleX, style: .continuous))
            .contentShape(Rectangle())
            .onTapGesture {
                focusedField = .registerPhone
            }
            .offset(x: 20 * scaleX, y: 74 * scaleY)

            Button {
                focusedField = nil
                Task {
                    let canContinue = await viewModel.precheckRegisterPhoneBeforeAgreement()
                    guard canContinue else {
                        return
                    }
                    showRegisterAgreementDialog = true
                }
            } label: {
                Text(viewModel.isRegisterPreCheckLoading ? "检查中..." : "立即注册")
                    .font(.system(size: 34 * scaleX / 1.5, weight: .semibold))
                    .foregroundStyle(
                        Color.white.opacity(
                            (canSubmitRegister && !viewModel.isRegisterPreCheckLoading) ? 0.96 : 0.62
                        )
                    )
                    .frame(width: 362 * scaleX, height: 49 * scaleY)
                    .background(
                        RoundedRectangle(cornerRadius: 24.5 * scaleX, style: .continuous)
                            .fill(
                                (canSubmitRegister && !viewModel.isRegisterPreCheckLoading)
                                    ? AppTheme.palette.brandPrimaryPressed
                                    : AppTheme.palette.brandPrimaryLight
                            )
                    )
            }
                .buttonStyle(.plain)
                .disabled(!canSubmitRegister || viewModel.isRegisterPreCheckLoading)
                .offset(x: 20 * scaleX, y: 138 * scaleY)

            if let errorMessage = viewModel.errorMessage {
                Text(errorMessage)
                    .font(.system(size: 14 * scaleX, weight: .medium))
                    .foregroundStyle(Color(red: 0.91, green: 0.31, blue: 0.27))
                    .offset(x: 20 * scaleX, y: 198 * scaleY)
            }
        }
        .frame(width: width, height: contentHeight, alignment: .topLeading)
        .background(Color(red: 0.96, green: 0.96, blue: 0.97))
        .clipShape(RoundedCorner(radius: 22 * scaleX, corners: [.topLeft, .topRight]))
        .offset(y: sheetTop)
    }

    private func registerAgreementDialog(scaleX: CGFloat, scaleY: CGFloat, width: CGFloat, height: CGFloat) -> some View {
        ZStack {
            Color.black.opacity(0.38)
                .ignoresSafeArea()

            VStack(spacing: 0) {
                Text("服务协议及隐私保护")
                    .font(.system(size: 26 * scaleX / 1.5, weight: .semibold))
                    .foregroundStyle(Color.black.opacity(0.78))
                    .padding(.top, 30 * scaleY)

                VStack(alignment: .leading, spacing: 16 * scaleY) {
                    Text("尊敬的用户，为了更好地保障你的合法权益，让你正常使用爱付服务，爱付需要依照法律法规进行实名制管理、履行反洗钱职责以及采取风险防范措施，为此需要依法收集并使用你的身份信息、联系方式、交易信息等。")
                        .font(.system(size: 19 * scaleX / 1.5, weight: .medium))
                        .foregroundStyle(Color.black.opacity(0.62))
                        .lineSpacing(3 * scaleY)

                    Text("爱付将严格保护你的个人信息，确保你的信息安全。")
                        .font(.system(size: 19 * scaleX / 1.5, weight: .medium))
                        .foregroundStyle(Color.black.opacity(0.62))
                        .lineSpacing(3 * scaleY)
                }
                .padding(.top, 24 * scaleY)
                .padding(.horizontal, 24 * scaleX)

                Button("同意协议并注册新账号") {
                    showRegisterAgreementDialog = false
                    viewModel.enterRegisterSmsVerify()
                }
                .font(.system(size: 24 * scaleX / 1.5, weight: .semibold))
                .foregroundStyle(Color.white)
                .frame(maxWidth: .infinity)
                .frame(height: 50 * scaleY)
                .background(
                    RoundedRectangle(cornerRadius: 25 * scaleX, style: .continuous)
                        .fill(AppTheme.palette.brandPrimary)
                )
                .buttonStyle(.plain)
                .padding(.horizontal, 24 * scaleX)
                .padding(.top, 30 * scaleY)

                Button("不同意") {
                    showRegisterAgreementDialog = false
                }
                .font(.system(size: 24 * scaleX / 1.5, weight: .semibold))
                .foregroundStyle(AppTheme.palette.brandPrimary)
                .frame(maxWidth: .infinity)
                .frame(height: 50 * scaleY)
                .background(
                    RoundedRectangle(cornerRadius: 25 * scaleX, style: .continuous)
                        .stroke(AppTheme.palette.brandPrimaryLight, lineWidth: 1.5 * scaleX)
                )
                .buttonStyle(.plain)
                .padding(.horizontal, 24 * scaleX)
                .padding(.top, 14 * scaleY)
                .padding(.bottom, 24 * scaleY)
            }
            .frame(width: min(344 * scaleX, width - 38 * scaleX))
            .background(
                RoundedRectangle(cornerRadius: 18 * scaleX, style: .continuous)
                    .fill(Color.white)
            )
        }
        .frame(width: width, height: height, alignment: .center)
    }

    private func smsVerifyLayer(scaleX: CGFloat, scaleY: CGFloat, width: CGFloat, height: CGFloat) -> some View {
        VStack(spacing: 0) {
            Spacer(minLength: 244 * scaleY)

            VStack(spacing: 0) {
                Text("请输入验证码")
                    .font(.system(size: 31 * scaleX / 1.5, weight: .semibold))
                    .foregroundStyle(Color.black.opacity(0.8))
                    .padding(.top, 24 * scaleY)

                Text(viewModel.smsHint)
                    .font(.system(size: 18 * scaleX / 1.5, weight: .medium))
                    .foregroundStyle(Color.black.opacity(0.42))
                    .padding(.top, 8 * scaleY)

                Text("演示系统，可随意输入6位数字")
                    .font(.system(size: 16 * scaleX / 1.5, weight: .medium))
                    .foregroundStyle(Color(red: 0.94, green: 0.55, blue: 0.55))
                    .padding(.top, 8 * scaleY)

                smsCodeInputRow(scaleX: scaleX, scaleY: scaleY)
                    .padding(.top, 16 * scaleY)
                    .padding(.horizontal, 24 * scaleX)

                Button {
                    startSmsResendCountdown()
                } label: {
                    Text(smsResendCountdown > 0 ? "重发验证码 (\(smsResendCountdown)秒)" : "重发验证码")
                }
                    .font(.system(size: 18 * scaleX / 1.5, weight: .medium))
                    .foregroundStyle(
                        smsResendCountdown > 0
                            ? Color.gray.opacity(0.6)
                            : AppTheme.palette.brandLink
                    )
                    .buttonStyle(.plain)
                    .disabled(smsResendCountdown > 0)
                    .padding(.top, 18 * scaleY)

                Button {
                    showOtherVerificationSheet = true
                } label: {
                    (
                        Text("收不到验证码？")
                            .foregroundStyle(Color.gray.opacity(0.6))
                        +
                        Text("试试其他登录方式")
                            .foregroundStyle(AppTheme.palette.brandLink)
                    )
                    .font(.system(size: 16 * scaleX / 1.5, weight: .medium))
                }
                .buttonStyle(.plain)
                .padding(.top, 14 * scaleY)

                Button {
                    Task {
                        await viewModel.loginWithSMS(appState: appState)
                    }
                } label: {
                    Text(viewModel.isLoading ? "登录中..." : "验证并登录")
                        .font(.system(size: 18 * scaleX, weight: .semibold))
                        .foregroundStyle(Color.white.opacity(viewModel.canLoginWithSMS ? 0.96 : 0.42))
                        .frame(maxWidth: .infinity)
                        .frame(height: 46 * scaleY)
                        .background(
                            RoundedRectangle(cornerRadius: 23 * scaleX)
                                .fill(
                                    viewModel.canLoginWithSMS
                                    ? AppTheme.palette.brandPrimary
                                    : AppTheme.palette.brandSubtleBackground
                                )
                        )
                }
                .buttonStyle(.plain)
                .disabled(!viewModel.canLoginWithSMS || viewModel.isLoading)
                .padding(.horizontal, 24 * scaleX)
                .padding(.top, 22 * scaleY)
                .padding(.bottom, 24 * scaleY)
            }
            .frame(width: 362 * scaleX)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 18 * scaleX))

            Spacer(minLength: 0)

            bottomLinks(scaleX: scaleX, scaleY: scaleY)
                .padding(.bottom, 78 * scaleY)
        }
        .frame(width: width, height: height, alignment: .top)
    }

    private func registerSmsVerifyLayer(scaleX: CGFloat, scaleY: CGFloat, width: CGFloat, height: CGFloat) -> some View {
        let sheetTop = 360 * scaleY
        let contentHeight = max(height - sheetTop, 0)

        return ZStack(alignment: .topLeading) {
            HStack(spacing: 0) {
                Button {
                    viewModel.back()
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 17 * scaleX, weight: .medium))
                        .foregroundStyle(Color.black.opacity(0.56))
                        .frame(width: 36 * scaleX, height: 36 * scaleY)
                }
                .buttonStyle(.plain)

                Spacer(minLength: 0)

                Text("请输入验证码")
                    .font(.system(size: 31 * scaleX / 1.5, weight: .semibold))
                    .foregroundStyle(Color.black.opacity(0.8))

                Spacer(minLength: 0)

                Color.clear
                    .frame(width: 36 * scaleX, height: 36 * scaleY)
            }
            .frame(width: 362 * scaleX)
            .offset(x: 20 * scaleX, y: 22 * scaleY)

            Text(viewModel.registerSmsHint)
                .font(.system(size: 18 * scaleX / 1.5, weight: .medium))
                .foregroundStyle(Color.black.opacity(0.38))
                .frame(width: width, alignment: .center)
                .offset(y: 74 * scaleY)

            Text("演示系统，可随意输入6位数字")
                .font(.system(size: 16 * scaleX / 1.5, weight: .medium))
                .foregroundStyle(Color(red: 0.94, green: 0.55, blue: 0.55))
                .frame(width: width, alignment: .center)
                .offset(y: 102 * scaleY)

            registerSmsCodeInputRow(scaleX: scaleX, scaleY: scaleY)
                .frame(width: width)
                .offset(y: 142 * scaleY)

            Button {
                startRegisterSmsResendCountdown()
            } label: {
                Text(registerSmsResendCountdown > 0 ? "重发验证码 (\(registerSmsResendCountdown)秒)" : "重发验证码")
            }
                .font(.system(size: 18 * scaleX / 1.5, weight: .medium))
                .foregroundStyle(
                    registerSmsResendCountdown > 0
                        ? Color.black.opacity(0.30)
                        : AppTheme.palette.brandLink
                )
                .buttonStyle(.plain)
                .disabled(registerSmsResendCountdown > 0)
                .frame(width: width, alignment: .center)
                .offset(y: 208 * scaleY)
        }
        .frame(width: width, height: contentHeight, alignment: .topLeading)
        .background(Color(red: 0.96, green: 0.96, blue: 0.97))
        .clipShape(RoundedCorner(radius: 22 * scaleX, corners: [.topLeft, .topRight]))
        .offset(y: sheetTop)
    }

    private func registerSmsCodeInputRow(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        let code = Array(viewModel.smsCode.prefix(6))
        let activeIndex = code.count < 6 ? code.count : -1

        return ZStack {
            HStack(spacing: 10 * scaleX) {
                ForEach(0..<6, id: \.self) { index in
                    RoundedRectangle(cornerRadius: 12 * scaleX, style: .continuous)
                        .fill(Color(red: 0.94, green: 0.94, blue: 0.95))
                        .frame(width: 48 * scaleX, height: 48 * scaleY)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12 * scaleX, style: .continuous)
                                .stroke(
                                    index == activeIndex
                                    ? AppTheme.palette.brandPrimaryLight
                                    : Color.clear,
                                    lineWidth: 1.6 * scaleX
                                )
                        )
                        .overlay(
                            Text(index < code.count ? String(code[index]) : "")
                                .font(.system(size: 24 * scaleX / 1.5, weight: .semibold))
                                .foregroundStyle(Color.black.opacity(0.78))
                        )
                }
            }

            TextField("", text: $viewModel.smsCode)
                .keyboardType(.numberPad)
                .textContentType(.oneTimeCode)
                .focused($focusedField, equals: .smsCode)
                .frame(width: 1, height: 1)
                .opacity(0.02)
                .onChange(of: viewModel.smsCode) { _, newValue in
                    let numbers = newValue.filter(\.isNumber)
                    if numbers != newValue {
                        viewModel.smsCode = numbers
                        return
                    }
                    if numbers.count > 6 {
                        viewModel.smsCode = String(numbers.prefix(6))
                        return
                    }
                    if numbers.count == 6, viewModel.step == .registerSmsVerify {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.10) {
                            guard viewModel.step == .registerSmsVerify else {
                                return
                            }
                            guard viewModel.smsCode.filter(\.isNumber).count == 6 else {
                                return
                            }
                            viewModel.enterRegisterIdentityAuthFromSms()
                        }
                    }
                }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            focusedField = .smsCode
        }
    }

    private func registerIdentityInputLayer(scaleX: CGFloat, scaleY: CGFloat, width: CGFloat, height: CGFloat) -> some View {
        let canSubmitIdentity = viewModel.isRegisterIdentityValid && viewModel.isRegisterLoginPasswordValid
        let showIdentityFormatHint = viewModel.registerIdentityNumber.count == 18 && !viewModel.isRegisterIdentityValid
        let registerPasswordBinding = Binding<String>(
            get: { viewModel.registerLoginPassword },
            set: { value in
                let digitsOnly = String(value.filter(\.isNumber).prefix(6))
                if digitsOnly != viewModel.registerLoginPassword {
                    viewModel.registerLoginPassword = digitsOnly
                }
            }
        )

        return ZStack(alignment: .topLeading) {
            Color.white
                .ignoresSafeArea()

            Button {
                viewModel.back()
            } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 17 * scaleX, weight: .medium))
                    .foregroundStyle(Color.black.opacity(0.52))
                    .frame(width: 30 * scaleX, height: 30 * scaleY)
            }
            .buttonStyle(.plain)
            .position(x: 21 * scaleX, y: 78 * scaleY)

            Text("输入你的身份证号")
                .font(.system(size: 31 * scaleX / 1.5, weight: .semibold))
                .foregroundStyle(Color.black.opacity(0.82))
                .position(x: width / 2, y: 141 * scaleY)

            if showIdentityFormatHint {
                Text("身份证号格式不正确")
                    .font(.system(size: 14 * scaleX, weight: .medium))
                    .foregroundStyle(Color(red: 0.92, green: 0.28, blue: 0.24))
                    .offset(x: 28 * scaleX, y: 202 * scaleY)
            }

            ZStack(alignment: .leading) {
                if viewModel.registerIdentityNumber.isEmpty && identityKeyboardTarget != .registerIdentity {
                    Text("请输入你的身份证号")
                        .font(.system(size: 16 * scaleX))
                        .foregroundStyle(Color.black.opacity(0.20))
                        .padding(.leading, 16 * scaleX)
                }

                HStack(spacing: 2 * scaleX) {
                    Text(viewModel.registerIdentityNumber)
                        .font(.system(size: 18 * scaleX))
                        .foregroundStyle(Color.black.opacity(0.78))
                        .lineLimit(1)

                    if identityKeyboardTarget == .registerIdentity {
                        identityInputBlinkingCaret(scaleY: scaleY)
                    }

                    Spacer(minLength: 0)
                }
                .padding(.horizontal, 16 * scaleX)
            }
            .frame(width: 377 * scaleX, height: 49 * scaleY)
            .background(Color.white)
            .overlay(
                RoundedRectangle(cornerRadius: 24.5 * scaleX, style: .continuous)
                    .stroke(Color(red: 0.88, green: 0.88, blue: 0.89), lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 24.5 * scaleX, style: .continuous))
            .contentShape(Rectangle())
            .onTapGesture {
                focusedField = nil
                identityKeyboardTarget = .registerIdentity
            }
            .offset(x: 12 * scaleX, y: 229 * scaleY)

            Button("下一步") {
                focusedField = nil
                identityKeyboardTarget = nil
                guard viewModel.isRegisterLoginPasswordValid else {
                    viewModel.errorMessage = "请设置6位数字登录密码"
                    return
                }
                if let validationMessage = viewModel.validateAndEnterRegisterIdentityAuth() {
                    registerIdentityInvalidMessage = validationMessage
                    showRegisterIdentityInvalidAlert = true
                }
            }
                .font(.system(size: 36 * scaleX / 1.5, weight: .semibold))
                .foregroundStyle(Color.white.opacity(canSubmitIdentity ? 0.96 : 0.55))
                .frame(width: 352 * scaleX, height: 49 * scaleY)
                .background(
                    RoundedRectangle(cornerRadius: 24.5 * scaleX, style: .continuous)
                        .fill(
                            canSubmitIdentity
                                ? AppTheme.palette.brandPrimary
                                : AppTheme.palette.brandPrimaryLight
                        )
                )
                .buttonStyle(.plain)
                .offset(x: 25 * scaleX, y: 309 * scaleY)

            Button("非中国大陆身份") { }
                .font(.system(size: 32 * scaleX / 1.5, weight: .semibold))
                .foregroundStyle(Color(red: 0.29, green: 0.45, blue: 0.67))
                .buttonStyle(.plain)
                .frame(width: width, alignment: .center)
                .offset(y: 384 * scaleY)

            VStack(alignment: .leading, spacing: 8 * scaleY) {
                Text("设置登录密码")
                    .font(.system(size: 14 * scaleX, weight: .medium))
                    .foregroundStyle(Color.black.opacity(0.72))

                ZStack(alignment: .leading) {
                    if viewModel.registerLoginPassword.isEmpty {
                        Text("请输入6位数字")
                            .font(.system(size: 16 * scaleX))
                            .foregroundStyle(Color.black.opacity(0.35))
                            .padding(.leading, 16 * scaleX)
                    }

                    SecureField("", text: registerPasswordBinding)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .keyboardType(.numberPad)
                        .textContentType(.oneTimeCode)
                        .focused($focusedField, equals: .registerLoginPassword)
                        .font(.system(size: 18 * scaleX, weight: .semibold, design: .monospaced))
                        .foregroundStyle(Color.black.opacity(0.82))
                        .padding(.horizontal, 16 * scaleX)
                        .frame(height: 49 * scaleY)
                        .onTapGesture {
                            identityKeyboardTarget = nil
                        }
                }
                .frame(width: 377 * scaleX, height: 49 * scaleY)
                .background(Color.white)
                .overlay(
                    RoundedRectangle(cornerRadius: 24.5 * scaleX, style: .continuous)
                        .stroke(Color(red: 0.88, green: 0.88, blue: 0.89), lineWidth: 1)
                )
                .clipShape(RoundedRectangle(cornerRadius: 24.5 * scaleX, style: .continuous))

                if let errorMessage = viewModel.errorMessage, !errorMessage.isEmpty {
                    Text(errorMessage)
                        .font(.system(size: 13 * scaleX))
                        .foregroundStyle(Color(red: 0.86, green: 0.24, blue: 0.24))
                }
            }
            .frame(width: 377 * scaleX, alignment: .leading)
            .offset(x: 12 * scaleX, y: 463 * scaleY)
        }
        .frame(width: width, height: height, alignment: .topLeading)
    }

    private func registerIdentityAuthLayer(scaleX: CGFloat, scaleY: CGFloat, width: CGFloat, height: CGFloat) -> some View {
        let submitEnabled = viewModel.canSubmitRegisterIdentityAuth && viewModel.isRegisterLoginPasswordValid
        let authIdentityKeyboardLift = identityKeyboardTarget == .registerAuthIdentity ? 120 * scaleY : 0
        let authPasswordKeyboardLift = focusedField == .registerLoginPassword ? 180 * scaleY : 0
        let authKeyboardLift = max(authIdentityKeyboardLift, authPasswordKeyboardLift)
        let authExtraBlankSpace = 560 * scaleY
        let registerPasswordBinding = Binding<String>(
            get: { viewModel.registerLoginPassword },
            set: { value in
                let digitsOnly = String(value.filter(\.isNumber).prefix(6))
                if digitsOnly != viewModel.registerLoginPassword {
                    viewModel.registerLoginPassword = digitsOnly
                }
            }
        )

        let nameBinding = Binding<String>(
            get: { viewModel.registerAuthFullName },
            set: { value in
                let normalized = String(value.prefix(20))
                if normalized != viewModel.registerAuthFullName {
                    viewModel.registerAuthFullName = normalized
                    registerAuthNameValidationMessage = nil
                }
            }
        )

        return ZStack(alignment: .bottom) {
            ScrollView(.vertical, showsIndicators: false) {
                ZStack(alignment: .topLeading) {
                    Color(red: 0.95, green: 0.95, blue: 0.95)
                        .frame(width: width, height: height + authExtraBlankSpace)

                HStack(spacing: 0) {
                    Button {
                        viewModel.back()
                    } label: {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 17 * scaleX, weight: .medium))
                            .foregroundStyle(Color.black.opacity(0.60))
                            .frame(width: 36 * scaleX, height: 36 * scaleY)
                    }
                    .buttonStyle(.plain)

                    Spacer(minLength: 0)

                    Text("身份认证")
                        .font(.system(size: 24 * scaleX / 1.5, weight: .semibold))
                        .foregroundStyle(Color.black.opacity(0.82))

                    Spacer(minLength: 0)

                    Color.clear.frame(width: 36 * scaleX, height: 36 * scaleY)
                }
                .frame(width: width - 20 * scaleX)
                .offset(x: 10 * scaleX, y: 78 * scaleY)

                Rectangle()
                    .fill(Color.black.opacity(0.08))
                    .frame(width: width, height: 1)
                    .offset(y: 120 * scaleY)

                Text("填写证件信息")
                    .font(.system(size: 50 * scaleX / 1.5, weight: .semibold))
                    .foregroundStyle(Color.black.opacity(0.74))
                    .frame(width: width, alignment: .center)
                    .offset(y: 170 * scaleY)

            Text("国家/地区")
                .font(.system(size: 21 * scaleX / 1.5, weight: .medium))
                .foregroundStyle(Color.black.opacity(0.40))
                .offset(x: 12 * scaleX, y: 261 * scaleY)

            registerAuthPickerLikeRow(
                title: "中国大陆",
                scaleX: scaleX,
                scaleY: scaleY
            )
            .offset(x: 12 * scaleX, y: 291 * scaleY)

            Text("证件类型")
                .font(.system(size: 21 * scaleX / 1.5, weight: .medium))
                .foregroundStyle(Color.black.opacity(0.40))
                .offset(x: 12 * scaleX, y: 356 * scaleY)

            registerAuthPickerLikeRow(
                title: "身份证",
                scaleX: scaleX,
                scaleY: scaleY
            )
            .offset(x: 12 * scaleX, y: 386 * scaleY)

            Text("证件信息")
                .font(.system(size: 21 * scaleX / 1.5, weight: .medium))
                .foregroundStyle(Color.black.opacity(0.40))
                .offset(x: 12 * scaleX, y: 451 * scaleY)

            VStack(spacing: 0) {
                HStack(spacing: 0) {
                    Text("姓名")
                        .font(.system(size: 23 * scaleX / 1.5, weight: .medium))
                        .foregroundStyle(Color.black.opacity(0.76))
                    Spacer(minLength: 0)
                }

                TextField("", text: nameBinding)
                    .focused($focusedField, equals: .registerAuthName)
                    .font(.system(size: 22 * scaleX / 1.5))
                    .foregroundStyle(Color.black.opacity(0.82))
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .onTapGesture {
                        identityKeyboardTarget = nil
                    }
                    .padding(.top, 10 * scaleY)
                    .overlay(alignment: .leading) {
                        if viewModel.registerAuthFullName.isEmpty {
                            Text("请输入本人姓名")
                                .font(.system(size: 22 * scaleX / 1.5))
                                .foregroundStyle(Color.black.opacity(0.18))
                        }
                    }

                Divider()
                    .padding(.top, 12 * scaleY)

                if let nameValidationMessage = registerAuthNameValidationMessage {
                    Text(nameValidationMessage)
                        .font(.system(size: 14 * scaleX / 1.5, weight: .medium))
                        .foregroundStyle(Color(red: 0.91, green: 0.31, blue: 0.27))
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.top, 8 * scaleY)
                }

                HStack(spacing: 0) {
                    Text("身份证号")
                        .font(.system(size: 23 * scaleX / 1.5, weight: .medium))
                        .foregroundStyle(Color.black.opacity(0.76))

                    Spacer(minLength: 0)

                    Button("生成随机身份证号") {
                        focusedField = nil
                        identityKeyboardTarget = nil
                        viewModel.fillRandomRegisterAuthIdentityNumber()
                    }
                    .font(.system(size: 19 * scaleX / 1.5, weight: .medium))
                    .foregroundStyle(AppTheme.palette.brandLink)
                    .buttonStyle(.plain)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.top, 18 * scaleY)

                ZStack(alignment: .leading) {
                    if viewModel.registerAuthIdentityNumber.isEmpty && identityKeyboardTarget != .registerAuthIdentity {
                        Text("请输入身份证号码")
                            .font(.system(size: 22 * scaleX / 1.5))
                            .foregroundStyle(Color.black.opacity(0.18))
                    }

                    HStack(spacing: 2 * scaleX) {
                        Text(viewModel.registerAuthIdentityNumber)
                            .font(.system(size: 22 * scaleX / 1.5))
                            .foregroundStyle(Color.black.opacity(0.82))
                            .lineLimit(1)

                        if identityKeyboardTarget == .registerAuthIdentity {
                            identityInputBlinkingCaret(scaleY: scaleY)
                        }

                        Spacer(minLength: 0)
                    }
                }
                .padding(.top, 10 * scaleY)
                .contentShape(Rectangle())
                .onTapGesture {
                    focusedField = nil
                    identityKeyboardTarget = .registerAuthIdentity
                }
                Spacer(minLength: 0)
            }
            .padding(.horizontal, 16 * scaleX)
            .padding(.top, 18 * scaleY)
            .frame(width: 377 * scaleX, height: 236 * scaleY)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 14 * scaleX, style: .continuous))
            .offset(x: 12 * scaleX, y: 478 * scaleY - authKeyboardLift)

                    registerLoginPasswordBox(scaleX: scaleX, scaleY: scaleY)
                        .offset(x: 25 * scaleX, y: 730 * scaleY - authKeyboardLift * 0.70)

                    if let errorMessage = viewModel.errorMessage, !errorMessage.isEmpty {
                        Text(errorMessage)
                            .font(.system(size: 13 * scaleX))
                            .foregroundStyle(Color(red: 0.86, green: 0.24, blue: 0.24))
                            .frame(width: 352 * scaleX, alignment: .leading)
                            .offset(x: 25 * scaleX, y: 810 * scaleY - authKeyboardLift * 0.70)
                    }

                    Button("提交") {
                        identityKeyboardTarget = nil
                        focusedField = nil
                        viewModel.submitRegisterIdentityAuth()
                    }
                    .font(.system(size: 30 * scaleX / 1.5, weight: .semibold))
                    .foregroundStyle(Color.white.opacity(submitEnabled ? 0.96 : 0.62))
                    .frame(width: 352 * scaleX, height: 49 * scaleY)
                    .background(
                        RoundedRectangle(cornerRadius: 24.5 * scaleX, style: .continuous)
                            .fill(
                                submitEnabled
                                    ? AppTheme.palette.brandPrimary
                                    : AppTheme.palette.brandPrimaryLight
                            )
                    )
                    .buttonStyle(.plain)
                    .disabled(!submitEnabled)
                    .offset(x: 25 * scaleX, y: 854 * scaleY - authKeyboardLift * 0.70)
                }
                .frame(width: width, height: height + authExtraBlankSpace + 120 * scaleY, alignment: .topLeading)
                .background(Color(red: 0.95, green: 0.95, blue: 0.95))
            }
            .scrollIndicators(.hidden)
            .frame(width: width, height: height, alignment: .topLeading)
        }
        .frame(width: width, height: height, alignment: .topLeading)
    }

    private func registerLoginPasswordBox(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        let registerPasswordBinding = Binding<String>(
            get: { viewModel.registerLoginPassword },
            set: { value in
                let digitsOnly = String(value.filter(\.isNumber).prefix(6))
                if digitsOnly != viewModel.registerLoginPassword {
                    viewModel.registerLoginPassword = digitsOnly
                }
            }
        )

        return VStack(alignment: .leading, spacing: 8 * scaleY) {
            Text("设置登录密码")
                .font(.system(size: 14 * scaleX, weight: .medium))
                .foregroundStyle(Color.black.opacity(0.72))

            ZStack(alignment: .leading) {
                if viewModel.registerLoginPassword.isEmpty {
                    Text("请输入6位数字")
                        .font(.system(size: 16 * scaleX))
                        .foregroundStyle(Color.black.opacity(0.35))
                        .padding(.leading, 16 * scaleX)
                }

                SecureField("", text: registerPasswordBinding)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .keyboardType(.numberPad)
                    .textContentType(.oneTimeCode)
                    .focused($focusedField, equals: .registerLoginPassword)
                    .font(.system(size: 18 * scaleX, weight: .semibold, design: .monospaced))
                    .foregroundStyle(Color.black.opacity(0.82))
                    .padding(.horizontal, 16 * scaleX)
                    .frame(height: 49 * scaleY)
                    .onTapGesture {
                        identityKeyboardTarget = nil
                    }
                    .onChange(of: viewModel.registerLoginPassword) { _, newValue in
                        let digitsOnly = String(newValue.filter(\.isNumber).prefix(6))
                        if digitsOnly != newValue {
                            viewModel.registerLoginPassword = digitsOnly
                            return
                        }
                        if digitsOnly.count == 6 {
                            focusedField = nil
                            identityKeyboardTarget = nil
                        }
                    }
            }
            .frame(width: 352 * scaleX, height: 49 * scaleY)
            .background(Color.white)
            .overlay(
                RoundedRectangle(cornerRadius: 24.5 * scaleX, style: .continuous)
                    .stroke(Color(red: 0.88, green: 0.88, blue: 0.89), lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 24.5 * scaleX, style: .continuous))
        }
        .frame(width: 352 * scaleX, alignment: .leading)
    }

    private func registerAuthPickerLikeRow(title: String, scaleX: CGFloat, scaleY: CGFloat) -> some View {
        HStack(spacing: 0) {
            Text(title)
                .font(.system(size: 24 * scaleX / 1.5, weight: .medium))
                .foregroundStyle(Color.black.opacity(0.75))
            Spacer(minLength: 0)
        }
        .padding(.horizontal, 16 * scaleX)
        .frame(width: 377 * scaleX, height: 54 * scaleY)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 14 * scaleX, style: .continuous))
    }

    private func registerFaceRecognitionLayer(scaleX: CGFloat, scaleY: CGFloat, width: CGFloat, height: CGFloat) -> some View {
        return ZStack(alignment: .topLeading) {
            Color.black.opacity(0.96)
                .ignoresSafeArea()

            HStack(spacing: 0) {
                Button {
                    viewModel.back()
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 17 * scaleX, weight: .medium))
                        .foregroundStyle(Color.white.opacity(0.92))
                        .frame(width: 36 * scaleX, height: 36 * scaleY)
                }
                .buttonStyle(.plain)

                Spacer(minLength: 0)

                Text("人脸识别")
                    .font(.system(size: 24 * scaleX / 1.5, weight: .semibold))
                    .foregroundStyle(Color.white.opacity(0.92))

                Spacer(minLength: 0)

                Color.clear.frame(width: 36 * scaleX, height: 36 * scaleY)
            }
            .frame(width: width - 20 * scaleX)
            .offset(x: 10 * scaleX, y: 78 * scaleY)

            VStack(spacing: 0) {
                Spacer(minLength: 186 * scaleY)

                Text("请将脸部保持在取景框内")
                    .font(.system(size: 18 * scaleX / 1.5, weight: .medium))
                    .foregroundStyle(Color.white.opacity(0.88))

                ZStack {
                    Circle()
                        .stroke(Color.white.opacity(0.20), lineWidth: 2)
                        .frame(width: 228 * scaleX, height: 228 * scaleY)

                    Circle()
                        .trim(from: 0.05, to: 0.95)
                        .stroke(
                            LinearGradient(
                                colors: [AppTheme.palette.brandPrimaryPressed, AppTheme.palette.brandPrimaryPressed],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            ),
                            style: StrokeStyle(lineWidth: 4.5 * scaleX, lineCap: .round)
                        )
                        .frame(width: 212 * scaleX, height: 212 * scaleY)

                    Image(systemName: "person.crop.circle.fill")
                        .font(.system(size: 122 * scaleX, weight: .regular))
                        .foregroundStyle(Color.white.opacity(0.18))

                    faceScanningLine(scaleX: scaleX, scaleY: scaleY)
                        .frame(width: 170 * scaleX, height: 170 * scaleY)
                }
                .padding(.top, 34 * scaleY)

                Text("认证中...")
                    .font(.system(size: 22 * scaleX / 1.5, weight: .semibold))
                    .foregroundStyle(Color.white.opacity(0.96))
                    .padding(.top, 42 * scaleY)

                Text("请保持正脸，约 5 秒完成")
                    .font(.system(size: 16 * scaleX / 1.5, weight: .regular))
                    .foregroundStyle(Color.white.opacity(0.62))
                    .padding(.top, 12 * scaleY)

                Spacer(minLength: 0)
            }
            .frame(width: width, height: height)
        }
        .frame(width: width, height: height, alignment: .topLeading)
    }

    private func registerFaceSuccessLayer(scaleX: CGFloat, scaleY: CGFloat, width: CGFloat, height: CGFloat) -> some View {
        return ZStack(alignment: .topLeading) {
            Color.white
                .ignoresSafeArea()

            VStack(spacing: 0) {
                Spacer(minLength: 246 * scaleY)

                Image(systemName: "checkmark.seal.fill")
                    .font(.system(size: 98 * scaleX, weight: .regular))
                    .foregroundStyle(Color(red: 0.14, green: 0.71, blue: 0.39))

                Text("认证成功")
                    .font(.system(size: 42 * scaleX / 1.5, weight: .semibold))
                    .foregroundStyle(Color.black.opacity(0.82))
                    .padding(.top, 34 * scaleY)

                Text("你已完成实名认证")
                    .font(.system(size: 19 * scaleX / 1.5, weight: .regular))
                    .foregroundStyle(Color.black.opacity(0.44))
                    .padding(.top, 10 * scaleY)

                Spacer(minLength: 0)

                if let errorMessage = viewModel.errorMessage, !errorMessage.isEmpty {
                    Text(errorMessage)
                        .font(.system(size: 13 * scaleX))
                        .foregroundStyle(Color(red: 0.86, green: 0.24, blue: 0.24))
                        .padding(.bottom, 10 * scaleY)
                }

                Button {
                    Task {
                        await viewModel.finishRegisterAndStartLife(appState: appState)
                    }
                } label: {
                    Text(viewModel.isLoading ? "登录中..." : "开启爱付生活")
                        .font(.system(size: 34 * scaleX / 1.5, weight: .semibold))
                        .foregroundStyle(Color.white.opacity(0.96))
                        .frame(width: 352 * scaleX, height: 49 * scaleY)
                }
                .background(
                    RoundedRectangle(cornerRadius: 24.5 * scaleX, style: .continuous)
                        .fill(AppTheme.palette.brandPrimary)
                )
                .buttonStyle(.plain)
                .disabled(viewModel.isLoading)
                .padding(.bottom, 88 * scaleY)
            }
            .frame(width: width, height: height)
        }
        .frame(width: width, height: height, alignment: .topLeading)
    }

    private func faceScanningLine(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        TimelineView(.animation) { context in
            let cycle = context.date.timeIntervalSinceReferenceDate.truncatingRemainder(dividingBy: 2.4) / 2.4
            let yOffset = (cycle - 0.5) * 146 * scaleY
            RoundedRectangle(cornerRadius: 2 * scaleX, style: .continuous)
                .fill(AppTheme.palette.brandSubtleBackground.opacity(0.95))
                .frame(width: 172 * scaleX, height: 3 * scaleY)
                .shadow(color: AppTheme.palette.brandPrimaryLight.opacity(0.70), radius: 7 * scaleX)
                .offset(y: yOffset)
        }
        .clipShape(RoundedRectangle(cornerRadius: 85 * scaleX, style: .continuous))
    }

    private func sheetHeader(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        HStack(spacing: 0) {
            Color.clear
                .frame(width: 52 * scaleX, height: 52 * scaleY)

            Spacer()
            Text("输入账号")
                .font(.system(size: 19 * scaleX, weight: .semibold))
                .foregroundStyle(Color.black.opacity(0.72))
                .lineLimit(1)
                .frame(height: 20 * scaleY)
            Spacer()
            Color.clear.frame(width: 52 * scaleX, height: 52 * scaleY)
        }
    }

    private func inputCell(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        let loginIdBinding = Binding<String>(
            get: { viewModel.loginId },
            set: { value in
                let digits = String(value.filter(\.isNumber).prefix(11))
                if digits != viewModel.loginId {
                    viewModel.loginId = digits
                }
            }
        )

        return HStack(spacing: 8 * scaleX) {
            ZStack(alignment: .leading) {
                if viewModel.loginId.isEmpty {
                    Text("请输入手机号")
                        .font(.system(size: 14 * scaleX))
                        .foregroundStyle(Color.black.opacity(0.4))
                }

                TextField("", text: loginIdBinding)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .keyboardType(.numberPad)
                    .textContentType(.telephoneNumber)
                    .focused($focusedField, equals: .loginId)
                    .font(.system(size: 18 * scaleX))
                    .foregroundStyle(Color.black.opacity(0.8))
            }
            .contentShape(Rectangle())
            .onTapGesture {
                focusedField = .loginId
                isAccountDropdownPresented = false
            }

            if focusedField == .loginId && !viewModel.loginId.isEmpty {
                Button {
                    viewModel.loginId = ""
                    isAccountDropdownPresented = false
                    focusedField = .loginId
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .font(.system(size: 17 * scaleX, weight: .semibold))
                        .foregroundStyle(Color.gray.opacity(0.58))
                        .frame(width: 22 * scaleX, height: 22 * scaleY)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
            }

            Button {
                if shouldShowAccountDropdown {
                    focusedField = nil
                    isAccountDropdownPresented = false
                } else {
                    focusedField = nil
                    isAccountDropdownPresented = !viewModel.quickLoginAccounts.isEmpty
                }
            } label: {
                Image(systemName: "chevron.down")
                    .font(.system(size: 14 * scaleX, weight: .medium))
                    .foregroundStyle(Color.gray.opacity(0.7))
                    .rotationEffect(.degrees(shouldShowAccountDropdown ? 180 : 0))
                    .frame(width: 22 * scaleX, height: 22 * scaleY)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 14 * scaleX)
        .frame(height: 49 * scaleY)
        .background(Color.white)
        .overlay(
            RoundedRectangle(cornerRadius: 24.5 * scaleX)
                .stroke(AppTheme.palette.brandPrimaryLight, lineWidth: 1.8 * scaleX)
        )
        .clipShape(RoundedRectangle(cornerRadius: 24.5 * scaleX))
    }

    private var shouldShowAccountDropdown: Bool {
        viewModel.step == .account && isAccountDropdownPresented && !viewModel.quickLoginAccounts.isEmpty
    }

    private func canAppendIdentityX(to raw: String) -> Bool {
        let normalized = String(
            raw
                .uppercased()
                .filter { $0.isNumber || $0 == "X" }
                .prefix(18)
        )
        return normalized.count == 17 && !normalized.contains("X")
    }

    private var currentIdentityInputValue: String? {
        switch identityKeyboardTarget {
        case .registerIdentity:
            return viewModel.registerIdentityNumber
        case .registerAuthIdentity:
            return viewModel.registerAuthIdentityNumber
        case nil:
            return nil
        }
    }

    private var canAppendIdentityXForCurrentTarget: Bool {
        guard let currentIdentityInputValue else {
            return false
        }
        return canAppendIdentityX(to: currentIdentityInputValue)
    }

    private func setCurrentIdentityInputValue(_ value: String) {
        let normalized = String(
            value
                .uppercased()
                .filter { $0.isNumber || $0 == "X" }
                .prefix(18)
        )
        switch identityKeyboardTarget {
        case .registerIdentity:
            viewModel.registerIdentityNumber = normalized
        case .registerAuthIdentity:
            viewModel.registerAuthIdentityNumber = normalized
        case nil:
            break
        }
    }

    private func appendIdentityKeyboardKey(_ key: String) {
        guard var value = currentIdentityInputValue else {
            return
        }

        if key == "X" {
            guard canAppendIdentityX(to: value) else {
                return
            }
            value.append("X")
        } else {
            guard key.count == 1, let scalar = key.unicodeScalars.first, CharacterSet.decimalDigits.contains(scalar) else {
                return
            }
            guard value.count < 18 else {
                return
            }
            value.append(key)
        }

        setCurrentIdentityInputValue(value)
    }

    private func removeLastIdentityKeyboardCharacter() {
        guard var value = currentIdentityInputValue, !value.isEmpty else {
            return
        }
        value.removeLast()
        setCurrentIdentityInputValue(value)
    }

    private func confirmIdentityKeyboardInput() {
        identityKeyboardTarget = nil
    }

    private func identityCardKeyboardOverlay(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        let keyHeight = 64 * scaleY
        let leftWidth = 302 * scaleX
        let rightWidth = 100 * scaleX
        let totalWidth = leftWidth + rightWidth
        let totalHeight = keyHeight * 4
        let lineColor = Color.black.opacity(0.10)
        let keyboardBackground = Color(red: 0.94, green: 0.94, blue: 0.95)

        return VStack(spacing: 0) {
            Spacer(minLength: 0)

            HStack(spacing: 0) {
                VStack(spacing: 0) {
                    identityKeyboardRow(keys: ["1", "2", "3"], keyHeight: keyHeight, lineColor: lineColor)
                    identityKeyboardRow(keys: ["4", "5", "6"], keyHeight: keyHeight, lineColor: lineColor)
                    identityKeyboardRow(keys: ["7", "8", "9"], keyHeight: keyHeight, lineColor: lineColor)
                    identityKeyboardRow(keys: ["", "0", "X"], keyHeight: keyHeight, lineColor: lineColor)
                }
                .frame(width: leftWidth, height: totalHeight)

                VStack(spacing: 0) {
                    Button {
                        removeLastIdentityKeyboardCharacter()
                    } label: {
                        Image(systemName: "delete.left")
                            .font(.system(size: 20, weight: .regular))
                            .foregroundStyle(Color.black.opacity(0.78))
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    }
                    .buttonStyle(.plain)
                    .frame(width: rightWidth, height: keyHeight)
                    .background(keyboardBackground)
                    .overlay(alignment: .bottom) {
                        Rectangle()
                            .fill(lineColor)
                            .frame(height: 1)
                    }

                    Button("确定") {
                        confirmIdentityKeyboardInput()
                    }
                    .font(.system(size: 34 * scaleX / 1.5, weight: .medium))
                    .foregroundStyle(Color.white)
                    .buttonStyle(.plain)
                    .frame(width: rightWidth, height: keyHeight * 3)
                    .background(AppTheme.palette.brandPrimary)
                }
                .frame(width: rightWidth, height: totalHeight)
                .overlay(alignment: .leading) {
                    Rectangle()
                        .fill(lineColor)
                        .frame(width: 1)
                }
            }
            .frame(width: totalWidth, height: totalHeight)
            .background(keyboardBackground)
            .contentShape(Rectangle())
            .onTapGesture { }
            .overlay(alignment: .top) {
                Rectangle()
                    .fill(lineColor)
                    .frame(height: 1)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .ignoresSafeArea(edges: .bottom)
    }

    private func identityKeyboardRow(keys: [String], keyHeight: CGFloat, lineColor: Color) -> some View {
        HStack(spacing: 0) {
            ForEach(Array(keys.enumerated()), id: \.offset) { index, key in
                let isXKey = key == "X"
                let isEnabled = !key.isEmpty && (!isXKey || canAppendIdentityXForCurrentTarget)
                let textOpacity = isXKey && !canAppendIdentityXForCurrentTarget ? 0.24 : 0.84

                ZStack {
                    Color.clear
                    if !key.isEmpty {
                        Text(key)
                            .font(.system(size: 24, weight: .regular))
                            .foregroundStyle(Color.black.opacity(textOpacity))
                            .allowsHitTesting(false)
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .contentShape(Rectangle())
                .onTapGesture {
                    guard isEnabled else {
                        return
                    }
                    appendIdentityKeyboardKey(key)
                }

                if index < keys.count - 1 {
                    Rectangle()
                        .fill(lineColor)
                        .frame(width: 1)
                }
            }
        }
        .frame(maxWidth: .infinity)
        .frame(height: keyHeight)
        .overlay(alignment: .bottom) {
            Rectangle()
                .fill(lineColor)
                .frame(height: 1)
        }
    }

    private func registerIdentityFrontPhotoUploadBox(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        PhotosPicker(selection: $registerIdFrontPhotoItem, matching: .images) {
            HStack(spacing: 12 * scaleX) {
                if let preview = registerIdFrontPhotoPreview {
                    Image(uiImage: preview)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 44 * scaleX, height: 44 * scaleY)
                        .clipShape(RoundedRectangle(cornerRadius: 10 * scaleX, style: .continuous))
                } else {
                    RoundedRectangle(cornerRadius: 10 * scaleX, style: .continuous)
                        .fill(Color(red: 0.95, green: 0.97, blue: 1.00))
                        .frame(width: 44 * scaleX, height: 44 * scaleY)
                        .overlay(
                            Image(systemName: "photo")
                                .font(.system(size: 18 * scaleX, weight: .medium))
                                .foregroundStyle(AppTheme.palette.brandPrimaryLight)
                        )
                }

                VStack(alignment: .leading, spacing: 4 * scaleY) {
                    Text("上传身份证正面")
                        .font(.system(size: 22 * scaleX / 1.5, weight: .medium))
                        .foregroundStyle(Color.black.opacity(0.78))
                    Text(registerIdFrontPhotoPreview == nil ? "可选项，提交时不做校验" : "已选择身份证正面图片")
                        .font(.system(size: 17 * scaleX / 1.5, weight: .regular))
                        .foregroundStyle(Color.black.opacity(0.45))
                }

                Spacer(minLength: 0)

                Image(systemName: "chevron.right")
                    .font(.system(size: 14 * scaleX, weight: .semibold))
                    .foregroundStyle(Color.black.opacity(0.28))
            }
            .padding(.horizontal, 14 * scaleX)
            .frame(width: 377 * scaleX, height: 68 * scaleY)
            .background(Color.white)
            .overlay(
                RoundedRectangle(cornerRadius: 14 * scaleX, style: .continuous)
                    .stroke(Color.black.opacity(0.08), lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 14 * scaleX, style: .continuous))
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }

    private func identityInputBlinkingCaret(scaleY: CGFloat) -> some View {
        TimelineView(.periodic(from: .now, by: 0.55)) { context in
            let phase = Int(context.date.timeIntervalSinceReferenceDate / 0.55)
            Rectangle()
                .fill(Color.black.opacity(0.72))
                .frame(width: 2 * scaleY, height: 22 * scaleY)
                .opacity(phase.isMultiple(of: 2) ? 1 : 0.12)
        }
        .frame(width: 2 * scaleY, height: 22 * scaleY)
    }

    private func accountDropdownHeight(scaleY: CGFloat) -> CGFloat {
        CGFloat(min(viewModel.quickLoginAccounts.count, 4)) * (56 * scaleY) + 8 * scaleY
    }

    private func accountDropdown(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        let visibleAccounts = Array(viewModel.quickLoginAccounts.prefix(4))

        return VStack(spacing: 0) {
            ForEach(Array(visibleAccounts.enumerated()), id: \.element.id) { index, account in
                Button {
                    focusedField = nil
                    isAccountDropdownPresented = false
                    viewModel.quickSelectRecentLoginAccount(account)
                } label: {
                    HStack(spacing: 12 * scaleX) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 11 * scaleX, style: .continuous)
                                .fill(Color(red: 0.90, green: 0.95, blue: 1.00))
                            Text(String(account.nickname.prefix(1)))
                                .font(.system(size: 16 * scaleX, weight: .semibold))
                                .foregroundStyle(AppTheme.palette.brandPrimary)
                        }
                        .frame(width: 34 * scaleX, height: 34 * scaleY)

                        VStack(alignment: .leading, spacing: 3 * scaleY) {
                            Text(account.loginId)
                                .font(.system(size: 16 * scaleX, weight: .medium))
                                .foregroundStyle(Color.black.opacity(0.78))
                                .lineLimit(1)
                            Text(account.nickname)
                                .font(.system(size: 12 * scaleX, weight: .regular))
                                .foregroundStyle(Color.black.opacity(0.42))
                                .lineLimit(1)
                        }

                        Spacer(minLength: 8 * scaleX)

                        Text("快速登录")
                            .font(.system(size: 12 * scaleX, weight: .medium))
                            .foregroundStyle(AppTheme.palette.brandLink)
                    }
                    .padding(.horizontal, 14 * scaleX)
                    .frame(height: 56 * scaleY)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)

                if index != visibleAccounts.count - 1 {
                    Divider()
                        .padding(.leading, 60 * scaleX)
                }
            }
        }
        .padding(.vertical, 4 * scaleY)
        .background(Color.white)
        .overlay(
            RoundedRectangle(cornerRadius: 18 * scaleX, style: .continuous)
                .stroke(Color(red: 0.87, green: 0.91, blue: 0.96), lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 18 * scaleX, style: .continuous))
        .shadow(color: Color.black.opacity(0.08), radius: 14 * scaleX, x: 0, y: 10 * scaleY)
    }

    private func actionButton(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        Button {
            focusedField = nil
            viewModel.goToDefaultVerifyStep()
        } label: {
            Text("下一步")
                .font(.system(size: 28 * scaleX, weight: .semibold))
                .foregroundStyle(Color.white.opacity(viewModel.canContinue ? 0.95 : 0.44))
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(
                    Group {
                        if viewModel.canContinue {
                            LinearGradient(
                                colors: [AppTheme.palette.brandPrimaryLight, AppTheme.palette.brandPrimaryLight],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        } else {
                            LinearGradient(
                                colors: [AppTheme.palette.brandPrimaryLight, AppTheme.palette.brandPrimaryLight],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        }
                    }
                )
                .clipShape(RoundedRectangle(cornerRadius: 24.5 * scaleX))
        }
        .buttonStyle(.plain)
        .disabled(!viewModel.canContinue)
    }

    private func passwordInputCell(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        HStack(spacing: 10 * scaleX) {
            ZStack(alignment: .leading) {
                if viewModel.password.isEmpty {
                    Text("请输入登录密码")
                        .font(.system(size: 14 * scaleX))
                        .foregroundStyle(Color.black.opacity(0.4))
                }

                SecureField("", text: $viewModel.password)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .focused($focusedField, equals: .password)
                    .font(.system(size: 18 * scaleX))
                    .foregroundStyle(Color.black.opacity(0.8))
            }

            Image(systemName: "eye.slash")
                .font(.system(size: 14 * scaleX, weight: .regular))
                .foregroundStyle(Color.black.opacity(0.22))

            Button("忘记密码") { }
                .font(.system(size: 15 * scaleX, weight: .medium))
                .foregroundStyle(Color(red: 0.34, green: 0.47, blue: 0.66))
                .buttonStyle(.plain)
        }
        .padding(.horizontal, 14 * scaleX)
        .frame(height: 49 * scaleY)
        .background(Color(red: 0.94, green: 0.94, blue: 0.95))
        .clipShape(RoundedRectangle(cornerRadius: 24.5 * scaleX))
    }

    private func passwordActionButton(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        Button {
            focusedField = nil
            Task {
                await viewModel.loginWithPassword(appState: appState)
            }
        } label: {
            Text(viewModel.isLoading ? "登录中..." : "登录")
                .font(.system(size: 28 * scaleX, weight: .semibold))
                .foregroundStyle(Color.white.opacity(viewModel.canLoginWithPassword ? 0.95 : 0.44))
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(
                    Group {
                        if viewModel.canLoginWithPassword {
                            LinearGradient(
                                colors: [AppTheme.palette.brandPrimaryLight, AppTheme.palette.brandPrimaryLight],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        } else {
                            LinearGradient(
                                colors: [AppTheme.palette.brandPrimaryLight, AppTheme.palette.brandPrimaryLight],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        }
                    }
                )
                .clipShape(RoundedRectangle(cornerRadius: 24.5 * scaleX))
        }
        .buttonStyle(.plain)
        .disabled(!viewModel.canLoginWithPassword || viewModel.isLoading)
    }

    private func smsCodeInputRow(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        let code = Array(viewModel.smsCode.prefix(6))
        let activeIndex = code.count < 6 ? code.count : -1

        return ZStack {
            HStack(spacing: 8 * scaleX) {
                ForEach(0..<6, id: \.self) { index in
                    RoundedRectangle(cornerRadius: 10 * scaleX)
                        .fill(Color(red: 0.95, green: 0.95, blue: 0.96))
                        .frame(width: 46 * scaleX, height: 48 * scaleY)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10 * scaleX)
                                .stroke(
                                    index == activeIndex
                                    ? AppTheme.palette.brandPrimary
                                    : Color.clear,
                                    lineWidth: 1.6 * scaleX
                                )
                        )
                        .overlay(
                            Text(index < code.count ? String(code[index]) : "")
                                .font(.system(size: 22 * scaleX, weight: .semibold))
                                .foregroundStyle(Color.black.opacity(0.75))
                        )
                }
            }

            TextField("", text: $viewModel.smsCode)
                .keyboardType(.numberPad)
                .textContentType(.oneTimeCode)
                .focused($focusedField, equals: .smsCode)
                .frame(width: 1, height: 1)
                .opacity(0.02)
                .onChange(of: viewModel.smsCode) { _, newValue in
                    let numbers = newValue.filter(\.isNumber)
                    if numbers != newValue {
                        viewModel.smsCode = numbers
                        return
                    }
                    if numbers.count > 6 {
                        viewModel.smsCode = String(numbers.prefix(6))
                    }
                }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            focusedField = .smsCode
        }
    }

    private func bottomLinks(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        HStack(spacing: 36 * scaleX) {
            Button("注册账号") {
                viewModel.enterPhoneRegister()
            }
            .font(.system(size: 21 * scaleX / 1.5, weight: .semibold))

            Button("遇到问题") { }
                .font(.system(size: 18 * scaleX / 1.5, weight: .medium))
        }
        .foregroundStyle(Color(red: 0.38, green: 0.50, blue: 0.71))
        .buttonStyle(.plain)
    }

    private func startSmsResendCountdown() {
        smsResendCountdownTask?.cancel()
        smsResendCountdownTask = nil
        smsResendCountdown = 60
        smsResendCountdownTask = Task {
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                guard !Task.isCancelled else {
                    return
                }
                let shouldContinue = await MainActor.run { () -> Bool in
                    if smsResendCountdown > 0 {
                        smsResendCountdown -= 1
                    }
                    return smsResendCountdown > 0
                }
                if !shouldContinue {
                    break
                }
            }
        }
    }

    private func startRegisterSmsResendCountdown() {
        registerSmsResendCountdownTask?.cancel()
        registerSmsResendCountdownTask = nil
        registerSmsResendCountdown = 60
        registerSmsResendCountdownTask = Task {
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                guard !Task.isCancelled else {
                    return
                }
                let shouldContinue = await MainActor.run { () -> Bool in
                    if registerSmsResendCountdown > 0 {
                        registerSmsResendCountdown -= 1
                    }
                    return registerSmsResendCountdown > 0
                }
                if !shouldContinue {
                    break
                }
            }
        }
    }

    private func selectVerificationMethod(_ method: LoginViewModel.VerifyMethod) {
        showOtherVerificationSheet = false
        viewModel.switchVerifyMethod(method)
    }

    private func dismissVerificationSheet() {
        if viewModel.step == .verifyMethodSelect {
            selectVerificationMethod(.mobile)
            return
        }
        showOtherVerificationSheet = false
    }

    private var currentVerificationMethod: LoginViewModel.VerifyMethod? {
        switch viewModel.step {
        case .mobileVerify:
            return .mobile
        case .smsVerify:
            return .sms
        case .passwordVerify:
            return .password
        case .account, .verifyMethodSelect, .phoneRegister, .registerSmsVerify, .registerIdentityInput, .registerIdentityAuth, .registerFaceRecognition, .registerFaceSuccess:
            return nil
        }
    }

    private var availableVerificationOptions: [(method: LoginViewModel.VerifyMethod, title: String, icon: String)] {
        let all: [(LoginViewModel.VerifyMethod, String, String)] = [
            (.mobile, "本机号码验证", "iphone"),
            (.sms, "短信验证", "message"),
            (.password, "密码验证", "lock")
        ]

        if let currentVerificationMethod {
            return all.filter { $0.0 != currentVerificationMethod }
        }
        return all
    }
}

private struct RoundedCorner: Shape {
    var radius: CGFloat
    var corners: UIRectCorner

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(
            roundedRect: rect,
            byRoundingCorners: corners,
            cornerRadii: CGSize(width: radius, height: radius)
        )
        return Path(path.cgPath)
    }
}

#Preview {
    LoginView()
        .environmentObject(AppState())
}
