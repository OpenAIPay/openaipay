import Foundation

@MainActor
final class LoginViewModel: ObservableObject {
    static let registeredDeviceOnlyLoginErrorMessage = "该账号仅支持在注册的手机上登录"

    private static let identityCardAreaCodes = [
        "110101", "120101", "310101", "320102", "330106", "350203", "370102", "410103",
        "420102", "430102", "440103", "440104", "440106", "440305", "450103", "500103",
        "510104", "520102", "530103", "610102", "620102"
    ]
    private static let identityCardCheckFactors = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
    private static let identityCardCheckCodes = Array("10X98765432")

    private struct DemoUserSeed {
        let userId: Int64
        let loginId: String
        let nickname: String
        let mobile: String
        let avatarUrl: String
        let gender: String?
        let region: String?
        let birthday: String?
        var kycLevel: String = "L2"
    }

    struct RecentLoginAccount: Codable, Identifiable, Equatable {
        let loginId: String
        let nickname: String

        var id: String { loginId }
    }

    private struct LocalRegisteredAccount: Codable {
        let loginId: String
        let nickname: String
    }

    private struct DemoAutoLoginCachedUser: Codable {
        let loginId: String
        let userId: Int64
        let nickname: String
        let mobile: String
        let avatarUrl: String
        let accountStatus: String
        let kycLevel: String
    }

    private static let recentLoginAccountsStorageKey = "aipay.login.recent.accounts"
    private static let localRegisteredAccountsStorageKey = "aipay.login.local.registered.accounts"
    private static let demoAutoLoginAccountStorageKey = "aipay.login.demo.auto.account"
    private static let demoAutoLoginUserCacheStorageKey = "aipay.login.demo.auto.user.cache"
    private static let maxRecentLoginAccountCount = 6
    private static let demoUserSeedsByLoginId: [String: DemoUserSeed] = [
        "13920000001": DemoUserSeed(
            userId: 880100068483692100,
            loginId: "13920000001",
            nickname: "顾郡",
            mobile: "13920000001",
            avatarUrl: "https://api.dicebear.com/9.x/adventurer-neutral/png?seed=880100068483692100",
            gender: "FEMALE",
            region: "广东佛山",
            birthday: "1996-01-19"
        ),
        "13920000002": DemoUserSeed(
            userId: 880902068943900002,
            loginId: "13920000002",
            nickname: "祁欣",
            mobile: "13920000002",
            avatarUrl: "https://api.dicebear.com/9.x/adventurer-neutral/png?seed=880902068943900002",
            gender: "MALE",
            region: "广东佛山",
            birthday: "1996-01-19",
            kycLevel: "L0"
        ),
        "13920000003": DemoUserSeed(
            userId: 880900069380000100,
            loginId: "13920000003",
            nickname: "林知夏",
            mobile: "13920000003",
            avatarUrl: "https://api.dicebear.com/9.x/adventurer-neutral/png?seed=880900069380000100",
            gender: "FEMALE",
            region: "浙江杭州",
            birthday: "1989-01-03"
        ),
        "13911110001": DemoUserSeed(
            userId: 880903068495400103,
            loginId: "13911110001",
            nickname: "林泽楷",
            mobile: "13911110001",
            avatarUrl: "https://api.dicebear.com/9.x/adventurer-neutral/png?seed=880903068495400103",
            gender: "MALE",
            region: "上海",
            birthday: "1994-03-11"
        ),
        "13911110002": DemoUserSeed(
            userId: 880911068495400111,
            loginId: "13911110002",
            nickname: "许安然",
            mobile: "13911110002",
            avatarUrl: "https://api.dicebear.com/9.x/adventurer-neutral/png?seed=880911068495400111",
            gender: "FEMALE",
            region: "北京",
            birthday: "1997-07-22"
        ),
        "13911110003": DemoUserSeed(
            userId: 880924068495400124,
            loginId: "13911110003",
            nickname: "周明宇",
            mobile: "13911110003",
            avatarUrl: "https://api.dicebear.com/9.x/adventurer-neutral/png?seed=880924068495400124",
            gender: "MALE",
            region: "深圳",
            birthday: "1993-12-06"
        ),
        "13911110004": DemoUserSeed(
            userId: 880932068495400132,
            loginId: "13911110004",
            nickname: "陈语彤",
            mobile: "13911110004",
            avatarUrl: "https://api.dicebear.com/9.x/adventurer-neutral/png?seed=880932068495400132",
            gender: "FEMALE",
            region: "杭州",
            birthday: "1998-05-09"
        ),
        "13911110005": DemoUserSeed(
            userId: 880940068495400140,
            loginId: "13911110005",
            nickname: "罗嘉宁",
            mobile: "13911110005",
            avatarUrl: "https://api.dicebear.com/9.x/adventurer-neutral/png?seed=880940068495400140",
            gender: "MALE",
            region: "广州",
            birthday: "1992-09-18"
        ),
        "13911110006": DemoUserSeed(
            userId: 880952068495400152,
            loginId: "13911110006",
            nickname: "宋知远",
            mobile: "13911110006",
            avatarUrl: "https://api.dicebear.com/9.x/adventurer-neutral/png?seed=880952068495400152",
            gender: "MALE",
            region: "成都",
            birthday: "1995-11-02"
        )
    ]

    private static let defaultRecentLoginAccounts: [RecentLoginAccount] = []

    enum Step {
        case account
        case verifyMethodSelect
        case mobileVerify
        case smsVerify
        case passwordVerify
        case phoneRegister
        case registerSmsVerify
        case registerIdentityInput
        case registerIdentityAuth
        case registerFaceRecognition
        case registerFaceSuccess
    }

    enum VerifyMethod {
        case mobile
        case sms
        case password
    }

    @Published var step: Step = .account
    @Published var loginId = ""
    @Published var password = ""
    @Published var smsCode = ""
    @Published var isLoading = false
    @Published var isDemoAutoLoginLoading = false
    @Published var errorMessage: String?
    @Published var isRegisterPreCheckLoading = false
    @Published var registerPhoneNumber = ""
    @Published var registerIdentityNumber = ""
    @Published var registerAuthFullName = ""
    @Published var registerAuthIdentityNumber = ""
    @Published var registerLoginPassword = ""
    @Published private(set) var recentLoginAccounts: [RecentLoginAccount]
    @Published private(set) var presetLoginAccounts: [RecentLoginAccount] = []
    private var localRegisteredAccountsByLoginId: [String: LocalRegisteredAccount]
    private var previousStepBeforePhoneRegister: Step = .account

    init() {
        let loadedAccounts = Self.loadRecentLoginAccounts()
        recentLoginAccounts = loadedAccounts
        Self.saveRecentLoginAccounts(loadedAccounts)
        localRegisteredAccountsByLoginId = Self.loadLocalRegisteredAccounts()
    }

    var canContinue: Bool {
        Self.isValidMainlandMobile(normalizedLoginId)
    }

    var canLoginWithPassword: Bool {
        !password.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    var canLoginWithSMS: Bool {
        smsCode.filter(\.isNumber).count == 6
    }

    var isRegisterPhoneValid: Bool {
        Self.isValidMainlandMobile(registerPhoneNumber)
    }

    var isRegisterIdentityValid: Bool {
        Self.isValidMainlandIdentityCard(registerIdentityNumber)
    }

    var canSubmitRegisterIdentityAuth: Bool {
        Self.isValidRegisterAuthName(registerAuthFullName)
            && Self.isValidMainlandIdentityCard(registerAuthIdentityNumber)
    }

    var isRegisterLoginPasswordValid: Bool {
        Self.isValidSixDigitLoginPassword(registerLoginPassword)
    }

    var maskedLoginId: String {
        let digits = normalizedLoginId.filter(\.isNumber)
        if digits.count >= 11 {
            let prefix = String(digits.prefix(3))
            let suffix = String(digits.suffix(2))
            return "\(prefix) **** **\(suffix)"
        }
        return normalizedLoginId
    }

    var smsHint: String {
        if let phone = maskedPhoneForSmsHint {
            return "已向号码 \(phone) 发送验证码"
        }
        return "请输入验证码"
    }

    var registerSmsHint: String {
        if let phone = maskedRegisterPhoneForSmsHint {
            return "已向号码 \(phone) 发送验证码"
        }
        return "请输入验证码"
    }

    var quickLoginAccounts: [RecentLoginAccount] {
        let localRegisteredAccounts = localRegisteredAccountsByLoginId.values.map {
            RecentLoginAccount(loginId: $0.loginId, nickname: $0.nickname)
        }
        return Array(
            Self.deduplicatedRecentLoginAccounts(
                presetLoginAccounts + recentLoginAccounts + localRegisteredAccounts + Self.defaultRecentLoginAccounts
            )
            .prefix(Self.maxRecentLoginAccountCount)
        )
    }

    var shouldShowRegisteredDeviceOnlyErrorAboveAccountInput: Bool {
        guard step == .account else {
            return false
        }
        return Self.isRegisteredDeviceOnlyLoginErrorMessage(errorMessage)
    }

    func loadPresetLoginAccounts(deviceId: String) async {
        let normalizedDeviceId = deviceId.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedDeviceId.isEmpty else {
            presetLoginAccounts = []
            return
        }
        do {
            let remoteAccounts = try await APIClient.shared.fetchPresetLoginAccounts(deviceId: normalizedDeviceId)
            let mappedAccounts = remoteAccounts.compactMap { account -> RecentLoginAccount? in
                let normalizedLoginId = Self.normalizedMainlandMobile(account.loginId)
                guard Self.isValidMainlandMobile(normalizedLoginId) else {
                    return nil
                }
                let normalizedNickname = account.nickname.trimmingCharacters(in: .whitespacesAndNewlines)
                return RecentLoginAccount(
                    loginId: normalizedLoginId,
                    nickname: normalizedNickname.isEmpty ? normalizedLoginId : normalizedNickname
                )
            }
            presetLoginAccounts = Self.deduplicatedRecentLoginAccounts(mappedAccounts)
        } catch {
            presetLoginAccounts = []
        }
    }

    func goToDefaultVerifyStep() {
        errorMessage = nil

        guard canContinue else {
            errorMessage = "请输入手机号"
            return
        }

        password = ""
        smsCode = ""
        step = .verifyMethodSelect
    }

    func switchVerifyMethod(_ method: VerifyMethod) {
        errorMessage = nil
        switch method {
        case .mobile:
            step = .mobileVerify
        case .sms:
            smsCode = ""
            step = .smsVerify
        case .password:
            password = ""
            step = .passwordVerify
        }
    }

    func back() {
        errorMessage = nil
        switch step {
        case .account:
            break
        case .phoneRegister:
            step = previousStepBeforePhoneRegister
        case .registerSmsVerify:
            step = .phoneRegister
        case .registerIdentityInput:
            step = .registerSmsVerify
        case .registerIdentityAuth:
            step = .registerSmsVerify
        case .registerFaceRecognition, .registerFaceSuccess:
            step = .registerIdentityAuth
        case .verifyMethodSelect, .mobileVerify, .smsVerify, .passwordVerify:
            step = .account
        }
    }

    func enterPhoneRegister() {
        errorMessage = nil
        previousStepBeforePhoneRegister = step == .phoneRegister ? .account : step
        registerPhoneNumber = ""
        smsCode = ""
        registerIdentityNumber = ""
        registerAuthFullName = ""
        registerAuthIdentityNumber = ""
        registerLoginPassword = ""
        step = .phoneRegister
    }

    func enterRegisterSmsVerify() {
        errorMessage = nil
        guard isRegisterPhoneValid else {
            errorMessage = "请输入正确的手机号"
            step = .phoneRegister
            return
        }
        smsCode = ""
        step = .registerSmsVerify
    }

    func precheckRegisterPhoneBeforeAgreement() async -> Bool {
        errorMessage = nil
        guard isRegisterPhoneValid else {
            errorMessage = "请输入正确的手机号"
            step = .phoneRegister
            return false
        }

        let normalizedPhone = Self.normalizedMainlandMobile(registerPhoneNumber)
        guard Self.isValidMainlandMobile(normalizedPhone) else {
            errorMessage = "请输入正确的手机号"
            step = .phoneRegister
            return false
        }
        registerPhoneNumber = normalizedPhone

        isRegisterPreCheckLoading = true
        defer { isRegisterPreCheckLoading = false }

        do {
            _ = try await APIClient.shared.checkRegisterPhoneStatus(loginId: normalizedPhone)
            return true
        } catch {
            errorMessage = userFacingErrorMessage(error)
            return false
        }
    }

    func enterRegisterIdentityInput() {
        errorMessage = nil
        guard step == .registerSmsVerify else {
            return
        }
        guard smsCode.filter(\.isNumber).count == 6 else {
            return
        }
        registerIdentityNumber = ""
        step = .registerIdentityInput
    }

    func enterRegisterIdentityAuthFromSms() {
        errorMessage = nil
        guard step == .registerSmsVerify else {
            return
        }
        guard smsCode.filter(\.isNumber).count == 6 else {
            return
        }
        registerIdentityNumber = ""
        registerAuthFullName = ""
        registerAuthIdentityNumber = ""
        step = .registerIdentityAuth
    }

    func validateAndEnterRegisterIdentityAuth() -> String? {
        let normalized = registerIdentityNumber
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .uppercased()
        guard !normalized.isEmpty else {
            return "请输入身份证号"
        }
        guard Self.isValidMainlandIdentityCard(normalized) else {
            return "身份证号不合法，请修改后重试"
        }

        registerIdentityNumber = normalized
        registerAuthFullName = ""
        registerAuthIdentityNumber = ""
        step = .registerIdentityAuth
        return nil
    }

    func submitRegisterIdentityAuth() {
        errorMessage = nil
        if let nameErrorMessage = validateRegisterAuthFullName() {
            errorMessage = nameErrorMessage
            return
        }
        guard Self.isValidMainlandIdentityCard(registerAuthIdentityNumber) else {
            errorMessage = "身份证号格式不正确"
            return
        }
        let normalizedRegisterPassword = registerLoginPassword.filter(\.isNumber)
        guard Self.isValidSixDigitLoginPassword(normalizedRegisterPassword) else {
            errorMessage = "请设置6位数字登录密码"
            return
        }
        registerLoginPassword = normalizedRegisterPassword
        step = .registerFaceRecognition
    }

    func validateRegisterAuthFullName() -> String? {
        let normalized = registerAuthFullName.trimmingCharacters(in: .whitespacesAndNewlines)
        registerAuthFullName = normalized
        guard !normalized.isEmpty else {
            return "请输入本人姓名"
        }
        guard Self.isValidRegisterAuthName(normalized) else {
            return "姓名需为中文且不超过5个字"
        }
        return nil
    }

    func fillRandomRegisterAuthIdentityNumber() {
        registerAuthIdentityNumber = Self.generateRandomMainlandIdentityCard()
    }

    func completeRegisterFaceRecognition() {
        guard step == .registerFaceRecognition else {
            return
        }
        step = .registerFaceSuccess
    }

    func finishRegisterAndStartLife(appState: AppState) async {
        errorMessage = nil
        let normalizedPhone = Self.normalizedMainlandMobile(registerPhoneNumber)
        let normalizedName = registerAuthFullName
            .trimmingCharacters(in: .whitespacesAndNewlines)
        let normalizedIdentityNo = registerAuthIdentityNumber
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .uppercased()
        let normalizedRegisterPassword = registerLoginPassword.filter(\.isNumber)
        guard Self.isValidMainlandMobile(normalizedPhone) else {
            errorMessage = "请输入正确的手机号"
            step = .phoneRegister
            return
        }
        guard Self.isValidSixDigitLoginPassword(normalizedRegisterPassword) else {
            errorMessage = "请设置6位数字登录密码"
            return
        }
        registerLoginPassword = normalizedRegisterPassword

        loginId = normalizedPhone
        let nickname = normalizedName.isEmpty ? "新用户" : normalizedName
        let resolvedMaskedRealName = normalizedName.isEmpty ? nil : normalizedName
        let resolvedIdentityNo = normalizedIdentityNo.isEmpty ? nil : normalizedIdentityNo
        // 先记录本地注册账号，保证后续无论后端是否可用都能兜底自动登录进首页。
        registerLocalAccount(loginId: normalizedPhone, nickname: nickname)

        isLoading = true
        defer { isLoading = false }

        var resolvedSession: LoginResponseData?
        var resolvedFromBackend = false
        var lastError: Error?

        do {
            resolvedSession = try await loginWithRetry(
                loginId: normalizedPhone,
                deviceId: appState.deviceId
            )
            resolvedFromBackend = true
        } catch {
            lastError = error
        }

        if resolvedSession == nil {
            do {
                _ = try await APIClient.shared.createRegisteredUser(
                    loginId: normalizedPhone,
                    nickname: nickname,
                    mobile: normalizedPhone,
                    loginPassword: normalizedRegisterPassword,
                    realName: resolvedMaskedRealName,
                    idCardNo: resolvedIdentityNo
                )
                resolvedSession = try await loginWithRetry(
                    loginId: normalizedPhone,
                    deviceId: appState.deviceId
                )
                resolvedFromBackend = true
            } catch {
                lastError = error
                if isAccountAlreadyExistsError(error) {
                    do {
                        resolvedSession = try await loginWithRetry(
                            loginId: normalizedPhone,
                            deviceId: appState.deviceId
                        )
                        resolvedFromBackend = true
                        lastError = nil
                    } catch {
                        lastError = error
                    }
                }
            }
        }

        guard var finalSession = resolvedSession else {
            if let lastError {
                errorMessage = userFacingErrorMessage(lastError)
            } else {
                errorMessage = "注册完成，但自动登录失败，请重试"
            }
            return
        }

        let normalizedAccessToken = finalSession.accessToken.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedAccessToken.isEmpty else {
            errorMessage = "注册完成，但登录态失效，请重试"
            return
        }
        let normalizedTokenType = finalSession.tokenType.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            ? "Bearer"
            : finalSession.tokenType.trimmingCharacters(in: .whitespacesAndNewlines)
        if normalizedAccessToken != finalSession.accessToken || normalizedTokenType != finalSession.tokenType {
            finalSession = LoginResponseData(
                accessToken: normalizedAccessToken,
                tokenType: normalizedTokenType,
                expiresInSeconds: finalSession.expiresInSeconds,
                user: finalSession.user,
                demo: finalSession.demo
            )
        }

        // 注册后即刻保证证件信息可见，避免后端最终一致性延迟导致证件页先显示旧值。
        if let resolvedIdentityNo, !resolvedIdentityNo.isEmpty {
            let currentIdCardNo = finalSession.user.idCardNo?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            if currentIdCardNo.uppercased() != resolvedIdentityNo.uppercased() {
                let patchedUser = UserProfile(
                    userId: finalSession.user.userId,
                    aipayUid: finalSession.user.aipayUid,
                    loginId: finalSession.user.loginId,
                    accountStatus: finalSession.user.accountStatus,
                    kycLevel: finalSession.user.kycLevel,
                    nickname: finalSession.user.nickname,
                    avatarUrl: finalSession.user.avatarUrl,
                    countryCode: finalSession.user.countryCode,
                    mobile: finalSession.user.mobile,
                    maskedRealName: resolvedMaskedRealName ?? finalSession.user.maskedRealName,
                    idCardNo: resolvedIdentityNo,
                    gender: finalSession.user.gender,
                    region: finalSession.user.region,
                    birthday: finalSession.user.birthday
                )
                finalSession = LoginResponseData(
                    accessToken: finalSession.accessToken,
                    tokenType: finalSession.tokenType,
                    expiresInSeconds: finalSession.expiresInSeconds,
                    user: patchedUser,
                    demo: finalSession.demo
                )
            }
        }

        rememberRecentLogin(loginId: normalizedPhone, nickname: nickname)
        password = ""
        smsCode = ""
        registerIdentityNumber = ""
        registerAuthFullName = ""
        registerAuthIdentityNumber = ""
        registerLoginPassword = ""
        handleLoginSuccess(finalSession, appState: appState)

        if resolvedFromBackend {
            let loginSession = finalSession
            let targetUserId = finalSession.user.userId
            Task {
                if let reconciledProfile = try? await reconcileRegisteredProfile(
                    loginId: normalizedPhone,
                    nickname: nickname,
                    maskedRealName: resolvedMaskedRealName,
                    idCardNo: resolvedIdentityNo,
                    existingProfile: loginSession.user
                ) {
                    let effectiveIdCardNo = (resolvedIdentityNo?.trimmingCharacters(in: .whitespacesAndNewlines).uppercased().isEmpty == false)
                        ? resolvedIdentityNo?.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
                        : reconciledProfile.idCardNo
                    let patchedProfile = UserProfile(
                        userId: reconciledProfile.userId,
                        aipayUid: reconciledProfile.aipayUid,
                        loginId: reconciledProfile.loginId,
                        accountStatus: reconciledProfile.accountStatus,
                        kycLevel: reconciledProfile.kycLevel,
                        nickname: reconciledProfile.nickname,
                        avatarUrl: reconciledProfile.avatarUrl,
                        countryCode: reconciledProfile.countryCode,
                        mobile: reconciledProfile.mobile,
                        maskedRealName: reconciledProfile.maskedRealName,
                        idCardNo: effectiveIdCardNo,
                        gender: reconciledProfile.gender,
                        region: reconciledProfile.region,
                        birthday: reconciledProfile.birthday
                    )
                    appState.updateCurrentUserProfile(patchedProfile)
                }
                _ = try? await APIClient.shared.ensureCreditProductOpened(
                    userId: targetUserId,
                    productCode: "AICREDIT"
                )
                _ = try? await APIClient.shared.ensureCreditProductOpened(
                    userId: targetUserId,
                    productCode: "AILOAN"
                )
            }
        }
    }

    func switchAccount() {
        errorMessage = nil
        step = .account
        password = ""
        smsCode = ""
    }

    func selectRecentLoginAccount(_ account: RecentLoginAccount) {
        errorMessage = nil
        loginId = account.loginId
        password = ""
        smsCode = ""
    }

    func quickSelectRecentLoginAccount(_ account: RecentLoginAccount) {
        selectRecentLoginAccount(account)
        goToDefaultVerifyStep()
    }

    func loginWithMobile(appState: AppState) async {
        errorMessage = nil

        guard canContinue else {
            errorMessage = "请输入手机号"
            step = .account
            return
        }

        await finishVerifiedLogin(appState: appState)
    }

    func loginWithSMS(appState: AppState) async {
        errorMessage = nil

        guard canContinue else {
            errorMessage = "请输入手机号"
            step = .account
            return
        }

        guard canLoginWithSMS else {
            errorMessage = "请输入 6 位验证码"
            step = .smsVerify
            return
        }

        await finishVerifiedLogin(appState: appState)
    }

    func loginWithPassword(appState: AppState) async {
        errorMessage = nil

        guard canContinue else {
            errorMessage = "请输入手机号"
            step = .account
            return
        }
        await finishVerifiedLogin(appState: appState)
    }

    func loginWithDemoAuto(appState: AppState) {
        errorMessage = nil
        guard !isDemoAutoLoginLoading else {
            return
        }

        let rememberedDemoLoginId = Self.loadRememberedDemoAutoLoginId()
        let preferredDemoLoginId = rememberedDemoLoginId

        isDemoAutoLoginLoading = true

        Task {
            defer {
                isDemoAutoLoginLoading = false
            }
            do {
                let session = try await APIClient.shared.demoAutoLogin(
                    deviceId: appState.deviceId,
                    preferredLoginId: preferredDemoLoginId
                )
                let resolvedDemoLoginId = Self.resolveDemoAutoLoginId(from: session)
                if let resolvedDemoLoginId {
                    Self.saveRememberedDemoAutoLoginId(resolvedDemoLoginId)
                    Self.saveCachedDemoAutoLoginSession(from: session, loginId: resolvedDemoLoginId)
                }
                handleLoginSuccess(session, appState: appState)
            } catch {
                errorMessage = userFacingErrorMessage(error)
                step = .account
            }
        }
    }

    private var normalizedLoginId: String {
        loginId.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private var maskedPhoneForSmsHint: String? {
        let digits = normalizedLoginId.filter(\.isNumber)
        guard digits.count >= 11 else {
            return nil
        }
        return Self.formattedSmsPhone(digits)
    }

    private var maskedRegisterPhoneForSmsHint: String? {
        let normalizedPhone = Self.normalizedMainlandMobile(registerPhoneNumber)
        guard normalizedPhone.count == 11 else {
            return nil
        }
        return Self.formattedSmsPhone(normalizedPhone)
    }

    private static func formattedSmsPhone(_ digits: String) -> String {
        let first = String(digits.prefix(3))
        let middle = String(digits.dropFirst(3).prefix(4))
        let last = String(digits.dropFirst(7).prefix(4))
        return "\(first) \(middle) \(last)"
    }

    private static func isValidSixDigitLoginPassword(_ raw: String) -> Bool {
        raw.range(of: #"^[0-9]{6}$"#, options: .regularExpression) != nil
    }

    private func finishVerifiedLogin(appState: AppState) async {
        isLoading = true
        defer { isLoading = false }

        let loginId = normalizedLoginId
        do {
            // 本机号码/短信验证不做真实短信校验：仅允许本机已注册账号和本机演示账号免密登录。
            let session = try await APIClient.shared.mobileVerifyLogin(
                loginId: loginId,
                deviceId: appState.deviceId
            )
            handleLoginSuccess(session, appState: appState)
            return
        } catch {
            errorMessage = userFacingErrorMessage(error)
            step = .account
        }
    }

    private func handleLoginSuccess(_ session: LoginResponseData, appState: AppState) {
        rememberRecentLogin(loginId: session.user.loginId, nickname: session.user.nickname)
        appState.onLoginSuccess(session)
    }

    private func rememberRecentLogin(loginId: String, nickname: String) {
        let normalizedLoginId = loginId.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedLoginId.isEmpty else {
            return
        }
        let normalizedNickname = nickname.trimmingCharacters(in: .whitespacesAndNewlines)
        let recentAccount = RecentLoginAccount(
            loginId: normalizedLoginId,
            nickname: normalizedNickname.isEmpty ? normalizedLoginId : normalizedNickname
        )
        var updatedAccounts = [recentAccount]
        updatedAccounts.append(contentsOf: recentLoginAccounts.filter { $0.loginId != normalizedLoginId })
        let mergedAccounts = Self.deduplicatedRecentLoginAccounts(updatedAccounts)
        recentLoginAccounts = Array(mergedAccounts.prefix(Self.maxRecentLoginAccountCount))
        Self.saveRecentLoginAccounts(recentLoginAccounts)
    }

    private static func loadRecentLoginAccounts() -> [RecentLoginAccount] {
        let storedAccounts: [RecentLoginAccount]
        if let data = UserDefaults.standard.data(forKey: recentLoginAccountsStorageKey),
           let decodedAccounts = try? JSONDecoder().decode([RecentLoginAccount].self, from: data) {
            storedAccounts = decodedAccounts
        } else {
            storedAccounts = []
        }
        let mergedAccounts = deduplicatedRecentLoginAccounts(storedAccounts + defaultRecentLoginAccounts)
        return Array(mergedAccounts.prefix(maxRecentLoginAccountCount))
    }

    private static func saveRecentLoginAccounts(_ accounts: [RecentLoginAccount]) {
        guard let data = try? JSONEncoder().encode(accounts) else {
            return
        }
        UserDefaults.standard.set(data, forKey: recentLoginAccountsStorageKey)
    }

    private static func loadRememberedDemoAutoLoginId() -> String? {
        let rawValue = UserDefaults.standard.string(forKey: demoAutoLoginAccountStorageKey) ?? ""
        let normalized = normalizedMainlandMobile(rawValue)
        guard isValidMainlandMobile(normalized) else {
            UserDefaults.standard.removeObject(forKey: demoAutoLoginAccountStorageKey)
            return nil
        }
        return normalized
    }

    private static func saveRememberedDemoAutoLoginId(_ loginId: String) {
        let normalized = normalizedMainlandMobile(loginId)
        guard isValidMainlandMobile(normalized) else {
            return
        }
        UserDefaults.standard.set(normalized, forKey: demoAutoLoginAccountStorageKey)
    }

    private static func resolveDemoAutoLoginId(from session: LoginResponseData) -> String? {
        let candidates: [String?] = [session.demo?.loginId]
        for candidate in candidates {
            guard let candidate else {
                continue
            }
            let normalized = normalizedMainlandMobile(candidate)
            if isValidMainlandMobile(normalized) {
                return normalized
            }
        }
        return nil
    }

    private static func saveCachedDemoAutoLoginSession(from session: LoginResponseData, loginId: String) {
        let normalizedLoginId = normalizedMainlandMobile(loginId)
        guard isValidMainlandMobile(normalizedLoginId),
              session.user.userId > 0 else {
            return
        }

        var cache = loadDemoAutoLoginUserCache()
        cache[normalizedLoginId] = DemoAutoLoginCachedUser(
            loginId: normalizedLoginId,
            userId: session.user.userId,
            nickname: session.user.nickname,
            mobile: session.user.mobile ?? normalizedLoginId,
            avatarUrl: session.user.avatarUrl ?? "",
            accountStatus: session.user.accountStatus,
            kycLevel: session.user.kycLevel
        )
        saveDemoAutoLoginUserCache(cache)
    }

    private static func loadDemoAutoLoginUserCache() -> [String: DemoAutoLoginCachedUser] {
        guard let data = UserDefaults.standard.data(forKey: demoAutoLoginUserCacheStorageKey),
              let decoded = try? JSONDecoder().decode([String: DemoAutoLoginCachedUser].self, from: data) else {
            return [:]
        }
        return decoded
    }

    private static func saveDemoAutoLoginUserCache(_ cache: [String: DemoAutoLoginCachedUser]) {
        guard let data = try? JSONEncoder().encode(cache) else {
            return
        }
        UserDefaults.standard.set(data, forKey: demoAutoLoginUserCacheStorageKey)
    }

    private static func loadLocalRegisteredAccounts() -> [String: LocalRegisteredAccount] {
        guard let data = UserDefaults.standard.data(forKey: localRegisteredAccountsStorageKey),
              let decoded = try? JSONDecoder().decode([LocalRegisteredAccount].self, from: data) else {
            return [:]
        }
        var result: [String: LocalRegisteredAccount] = [:]
        for account in decoded {
            let normalizedLoginId = account.loginId.trimmingCharacters(in: .whitespacesAndNewlines)
            guard !normalizedLoginId.isEmpty else {
                continue
            }
            let normalizedNickname = account.nickname.trimmingCharacters(in: .whitespacesAndNewlines)
            result[normalizedLoginId] = LocalRegisteredAccount(
                loginId: normalizedLoginId,
                nickname: normalizedNickname.isEmpty ? "新用户" : normalizedNickname
            )
        }
        return result
    }

    private static func saveLocalRegisteredAccounts(_ accountsByLoginId: [String: LocalRegisteredAccount]) {
        let accounts = accountsByLoginId.values.sorted { $0.loginId < $1.loginId }
        guard let data = try? JSONEncoder().encode(accounts) else {
            return
        }
        UserDefaults.standard.set(data, forKey: localRegisteredAccountsStorageKey)
    }

    private func registerLocalAccount(loginId: String, nickname: String) {
        let normalizedLoginId = Self.normalizedMainlandMobile(loginId)
        guard Self.isValidMainlandMobile(normalizedLoginId) else {
            return
        }
        let normalizedNickname = nickname.trimmingCharacters(in: .whitespacesAndNewlines)
        localRegisteredAccountsByLoginId[normalizedLoginId] = LocalRegisteredAccount(
            loginId: normalizedLoginId,
            nickname: normalizedNickname.isEmpty ? "新用户" : normalizedNickname
        )
        Self.saveLocalRegisteredAccounts(localRegisteredAccountsByLoginId)
    }

    private static func deduplicatedRecentLoginAccounts(_ accounts: [RecentLoginAccount]) -> [RecentLoginAccount] {
        var seenLoginIds = Set<String>()
        var result: [RecentLoginAccount] = []
        for account in accounts {
            let normalizedLoginId = account.loginId.trimmingCharacters(in: .whitespacesAndNewlines)
            guard !normalizedLoginId.isEmpty else {
                continue
            }
            if seenLoginIds.insert(normalizedLoginId).inserted {
                let normalizedNickname = account.nickname.trimmingCharacters(in: .whitespacesAndNewlines)
                result.append(
                    RecentLoginAccount(
                        loginId: normalizedLoginId,
                        nickname: normalizedNickname.isEmpty ? normalizedLoginId : normalizedNickname
                    )
                )
            }
        }
        return result
    }

    private static func isValidMainlandMobile(_ raw: String) -> Bool {
        let normalized = normalizedMainlandMobile(raw)
        guard normalized.count == 11 else {
            return false
        }
        return normalized.range(of: "^1[3-9][0-9]{9}$", options: .regularExpression) != nil
    }

    private static func normalizedMainlandMobile(_ raw: String) -> String {
        let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            return ""
        }

        var converted = ""
        for scalar in trimmed.unicodeScalars {
            if scalar.value >= 0xFF10 && scalar.value <= 0xFF19,
               let asciiScalar = UnicodeScalar(scalar.value - 0xFF10 + 0x30) {
                converted.unicodeScalars.append(asciiScalar)
            } else {
                converted.unicodeScalars.append(scalar)
            }
        }

        var digits = converted.replacingOccurrences(of: "[^0-9]", with: "", options: .regularExpression)
        if digits.count == 13 && digits.hasPrefix("86") {
            digits = String(digits.dropFirst(2))
        } else if digits.count == 15 && digits.hasPrefix("0086") {
            digits = String(digits.dropFirst(4))
        }
        return digits
    }

    private func isAccountAlreadyExistsError(_ error: Error) -> Bool {
        let normalizedUserFacing = userFacingErrorMessage(error).lowercased()
        if normalizedUserFacing.contains("已存在") || normalizedUserFacing.contains("already exists") {
            return true
        }
        let raw = error.localizedDescription.lowercased()
        return raw.contains("already exists")
            || raw.contains("duplicate entry")
            || raw.contains("已存在")
    }

    private static func isValidMainlandIdentityCard(_ raw: String) -> Bool {
        let normalized = raw
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .uppercased()
        guard normalized.range(of: "^[1-9][0-9]{5}(19|20)[0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])[0-9]{3}[0-9X]$", options: .regularExpression) != nil else {
            return false
        }

        let factors = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
        let checks = Array("10X98765432")
        let chars = Array(normalized)

        var sum = 0
        for (index, factor) in factors.enumerated() {
            guard let value = chars[index].wholeNumberValue else {
                return false
            }
            sum += value * factor
        }
        let expected = checks[sum % 11]
        return chars[17] == expected
    }

    private static func isValidRegisterAuthName(_ raw: String) -> Bool {
        let normalized = raw.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalized.isEmpty else {
            return false
        }
        return normalized.range(of: "^[\\u4E00-\\u9FFF]{1,5}$", options: .regularExpression) != nil
    }

    private static func generateRandomMainlandIdentityCard() -> String {
        let areaCode = identityCardAreaCodes.randomElement() ?? "110101"
        let calendar = Calendar(identifier: .gregorian)
        let startDate = calendar.date(from: DateComponents(year: 1970, month: 1, day: 1)) ?? Date()
        let endDate = calendar.date(from: DateComponents(year: 2005, month: 12, day: 31)) ?? Date()
        let daySpan = max(0, calendar.dateComponents([.day], from: startDate, to: endDate).day ?? 0)
        let randomOffset = Int.random(in: 0...daySpan)
        let birthdayDate = calendar.date(byAdding: .day, value: randomOffset, to: startDate) ?? startDate

        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = TimeZone.current
        formatter.dateFormat = "yyyyMMdd"
        let birth = formatter.string(from: birthdayDate)

        let sequence = String(format: "%03d", Int.random(in: 0...999))
        let first17 = areaCode + birth + sequence
        let digits = first17.compactMap(\.wholeNumberValue)
        let sum = zip(digits, identityCardCheckFactors).reduce(0) { partialResult, pair in
            partialResult + pair.0 * pair.1
        }
        let checkCode = identityCardCheckCodes[sum % 11]
        return first17 + String(checkCode)
    }

    private func makeProfileBackedSession(
        _ profile: UserProfile,
        accessToken: String,
        tokenType: String
    ) -> LoginResponseData {
        LoginResponseData(
            accessToken: accessToken,
            tokenType: tokenType,
            expiresInSeconds: 7200,
            user: profile
        )
    }

    private static func isRegisteredDeviceOnlyLoginErrorMessage(_ message: String?) -> Bool {
        let normalized = message?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return normalized == registeredDeviceOnlyLoginErrorMessage
    }

    private func reconcileRegisteredProfile(
        loginId: String,
        nickname: String,
        maskedRealName: String?,
        idCardNo: String?,
        existingProfile: UserProfile? = nil
    ) async throws -> UserProfile {
        let normalizedLoginId = Self.normalizedMainlandMobile(loginId)
        let profile: UserProfile
        if let existingProfile {
            profile = existingProfile
        } else {
            profile = try await APIClient.shared.fetchUserProfile(loginId: normalizedLoginId)
        }

        let normalizedNickname = nickname.trimmingCharacters(in: .whitespacesAndNewlines)
        let normalizedMaskedRealName = maskedRealName?.trimmingCharacters(in: .whitespacesAndNewlines)
        let normalizedIdCardNo = idCardNo?.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        let resolvedNickname = shouldBackfillNickname(current: profile.nickname, userId: profile.userId)
            ? normalizedNickname
            : nil
        let resolvedMobile = shouldBackfillText(current: profile.mobile, desired: normalizedLoginId)
            ? normalizedLoginId
            : nil
        let resolvedMaskedRealName = shouldSyncText(current: profile.maskedRealName, desired: normalizedMaskedRealName)
            ? normalizedMaskedRealName
            : nil
        let resolvedIdCardNo = shouldSyncText(current: profile.idCardNo, desired: normalizedIdCardNo)
            ? normalizedIdCardNo
            : nil

        if resolvedNickname == nil,
           resolvedMobile == nil,
           resolvedMaskedRealName == nil,
           resolvedIdCardNo == nil {
            return profile
        }

        if resolvedNickname != nil || resolvedMobile != nil {
            try await APIClient.shared.updateUserProfile(
                userId: profile.userId,
                nickname: resolvedNickname,
                mobile: resolvedMobile
            )
        }
        if let resolvedMaskedRealName, let resolvedIdCardNo {
            try await APIClient.shared.submitUserKyc(
                userId: profile.userId,
                realName: resolvedMaskedRealName,
                idCardNo: resolvedIdCardNo
            )
        }

        if let refreshedProfile = try? await APIClient.shared.fetchUserProfile(userId: profile.userId) {
            return refreshedProfile
        }

        return UserProfile(
            userId: profile.userId,
            aipayUid: profile.aipayUid,
            loginId: profile.loginId,
            accountStatus: profile.accountStatus,
            kycLevel: (resolvedMaskedRealName != nil && resolvedIdCardNo != nil && (profile.kycLevel == "L0" || profile.kycLevel == "L1")) ? "L2" : profile.kycLevel,
            nickname: resolvedNickname ?? profile.nickname,
            avatarUrl: profile.avatarUrl,
            countryCode: profile.countryCode,
            mobile: resolvedMobile ?? profile.mobile,
            maskedRealName: resolvedMaskedRealName ?? profile.maskedRealName,
            idCardNo: resolvedIdCardNo ?? profile.idCardNo,
            gender: profile.gender,
            region: profile.region,
            birthday: profile.birthday
        )
    }

    private func shouldBackfillText(current: String?, desired: String?) -> Bool {
        let normalizedCurrent = current?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let normalizedDesired = desired?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard !normalizedDesired.isEmpty else {
            return false
        }
        return normalizedCurrent.isEmpty
    }

    private func shouldSyncText(current: String?, desired: String?) -> Bool {
        let normalizedCurrent = current?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let normalizedDesired = desired?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard !normalizedDesired.isEmpty else {
            return false
        }
        return normalizedCurrent != normalizedDesired
    }

    private func loginWithRetry(
        loginId: String,
        deviceId: String,
        maxAttempts: Int = 3
    ) async throws -> LoginResponseData {
        var lastError: Error?
        for attempt in 1...max(maxAttempts, 1) {
            do {
                return try await APIClient.shared.mobileVerifyLogin(
                    loginId: loginId,
                    deviceId: deviceId
                )
            } catch {
                lastError = error
                guard attempt < maxAttempts else {
                    break
                }
                try? await Task.sleep(nanoseconds: 350_000_000)
            }
        }
        throw lastError ?? APIClientError.decodeFailed
    }

    private func shouldBackfillNickname(current: String, userId: Int64) -> Bool {
        let normalizedCurrent = current.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedCurrent.isEmpty else {
            return true
        }
        return normalizedCurrent == "新用户" || normalizedCurrent == "用户\(userId)"
    }

}
