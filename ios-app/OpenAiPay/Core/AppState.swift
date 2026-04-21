import Foundation
import UIKit
import CryptoKit
import Security

private enum AppKeychainStore {
    private static let fallbackService = "cn.openaipay.ios.runtime"

    private static var service: String {
        let normalized = Bundle.main.bundleIdentifier?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return normalized.isEmpty ? fallbackService : normalized
    }

    static func readString(account: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        guard status == errSecSuccess,
              let data = item as? Data,
              let value = String(data: data, encoding: .utf8) else {
            return nil
        }
        return value
    }

    static func saveString(_ value: String, account: String) {
        guard let data = value.data(using: .utf8) else {
            return
        }
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account
        ]
        let attributes: [String: Any] = [
            kSecValueData as String: data
        ]
        let status = SecItemUpdate(query as CFDictionary, attributes as CFDictionary)
        if status == errSecItemNotFound {
            var insertQuery = query
            insertQuery[kSecValueData as String] = data
            SecItemAdd(insertQuery as CFDictionary, nil)
        }
    }
}

struct AppUpdatePresentation: Identifiable, Equatable {
    let id: String
    let versionLabel: String
    let descriptionText: String
    let appStoreURL: URL?
    let isForce: Bool

    init?(from data: AppVersionCheckData) {
        guard data.updateAvailable || data.forceUpdate else {
            return nil
        }

        let versionToken = Self.normalizedText(data.latestVersionCode)
            ?? Self.normalizedText(data.latestVersionNo)
            ?? UUID().uuidString
        let versionLabel = Self.normalizedText(data.latestVersionNo)
            ?? Self.normalizedText(data.latestVersionCode)
            ?? "最新版本"
        let descriptionText = Self.normalizedText(data.versionDescription)
            ?? (data.forceUpdate ? "发现重要更新，请升级后继续使用。" : "发现新版本，建议尽快更新。")
        let appStoreURL = Self.normalizedText(data.appStoreUrl).flatMap(URL.init(string:))

        self.id = versionToken
        self.versionLabel = versionLabel
        self.descriptionText = descriptionText
        self.appStoreURL = appStoreURL
        self.isForce = data.forceUpdate
    }

    private static func normalizedText(_ raw: String?) -> String? {
        let trimmed = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? nil : trimmed
    }
}

enum AppRuntime {
    static let appCode = "OPENAIPAY_IOS"
    static let appSessionId = "ios-session-\(UUID().uuidString.lowercased())"
    static let appLaunchAt = Date()

    private static let stableDeviceIdAccount = "openaipay.app.runtime.stable-device-id"
    private static let legacyDeviceIdKey = "openaipay.app.runtime.device-id"
    private static let clientIdKey = "openaipay.app.runtime.client-id"
    private static let updatePromptVersionKey = "openaipay.app.runtime.update.prompt.version"
    private static let updatePromptDateKey = "openaipay.app.runtime.update.prompt.date"

    static var deviceId: String {
        stableDeviceIdentifier()
    }

    static var legacyDeviceIds: [String] {
        legacyDeviceIdentifiers(excluding: deviceId)
    }

    static var clientId: String {
        stableIdentifier(forKey: clientIdKey, prefix: "ios-client")
    }

    static var currentVersionNo: String? {
        normalizedText(Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String)
    }

    static var appBuildNo: String? {
        normalizedText(Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String)
    }

    static var deviceBrand: String? {
        UIDevice.current.userInterfaceIdiom == .phone ? "Apple iPhone" : normalizedText(UIDevice.current.model)
    }

    static var deviceModel: String? {
        normalizedText(UIDevice.current.model)
    }

    static var deviceName: String? {
        normalizedText(UIDevice.current.name)
    }

    static var deviceType: String? {
        switch UIDevice.current.userInterfaceIdiom {
        case .phone:
            return "PHONE"
        case .pad:
            return "PAD"
        case .tv:
            return "TV"
        case .carPlay:
            return "CARPLAY"
        case .mac:
            return "MAC"
        default:
            return "UNKNOWN"
        }
    }

    static var osName: String? {
        normalizedText(UIDevice.current.systemName)
    }

    static var osVersion: String? {
        normalizedText(UIDevice.current.systemVersion)
    }

    static var localeIdentifier: String? {
        normalizedText(Locale.current.identifier)
    }

    static var timezoneIdentifier: String? {
        normalizedText(TimeZone.current.identifier)
    }

    static var languageIdentifier: String? {
        normalizedText(Locale.preferredLanguages.first)
    }

    static var countryCode: String? {
        normalizedText(Locale.current.region?.identifier)
    }

    static var bffBaseURL: String {
        configuredURL(
            infoPlistKey: "OpenAiPayBffBaseURL",
            environmentKey: "OPENAIPAY_BFF_BASE_URL",
            simulatorDefault: "http://127.0.0.1:3000",
            deviceDefault: "http://127.0.0.1:3000"
        )
    }

    static var backendBaseURL: String {
        configuredURL(
            infoPlistKey: "OpenAiPayBackendBaseURL",
            environmentKey: "OPENAIPAY_BACKEND_BASE_URL",
            simulatorDefault: "http://127.0.0.1:8080",
            deviceDefault: "http://127.0.0.1:8080"
        )
    }

    static var allowsLocalSyntheticAuth: Bool {
        let normalized = bffBaseURL.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        return normalized.contains("127.0.0.1") || normalized.contains("localhost")
    }

    static func makeLocalAccessToken(for userId: Int64, expiresInSeconds: Int64 = 7200) -> String {
        guard userId > 0 else {
            return ""
        }
        let now = Int64(Date().timeIntervalSince1970)
        let ttl = max(expiresInSeconds, 300)
        let expireAt = now + ttl
        let payload = "\(userId):\(now):\(expireAt):\(deviceId):\(UUID().uuidString)"
        let payloadData = Data(payload.utf8)
        let secretData = Data(tokenSigningSecret.utf8)
        let key = SymmetricKey(data: secretData)
        let signature = HMAC<SHA256>.authenticationCode(for: payloadData, using: key)
        let signatureData = Data(signature)
        return "\(payloadData.base64URLEncodedString()).\(signatureData.base64URLEncodedString())"
    }

    static func shouldPromptForOptionalUpdate(_ versionCheck: AppVersionCheckData) -> Bool {
        guard versionCheck.updateAvailable, !versionCheck.forceUpdate else {
            return false
        }

        let frequency = normalizedText(versionCheck.updatePromptFrequency)?.uppercased() ?? "ONCE_PER_VERSION"
        let defaults = UserDefaults.standard
        switch frequency {
        case "SILENT":
            return false
        case "ALWAYS":
            return true
        case "DAILY":
            guard let lastPromptAt = defaults.object(forKey: updatePromptDateKey) as? Date else {
                return true
            }
            return !Calendar.current.isDateInToday(lastPromptAt)
        default:
            guard let versionToken = promptVersionToken(for: versionCheck) else {
                return true
            }
            return defaults.string(forKey: updatePromptVersionKey) != versionToken
        }
    }

    static func markOptionalUpdatePromptShown(for versionCheck: AppVersionCheckData) {
        let defaults = UserDefaults.standard
        defaults.set(Date(), forKey: updatePromptDateKey)
        if let versionToken = promptVersionToken(for: versionCheck) {
            defaults.set(versionToken, forKey: updatePromptVersionKey)
        }
    }

    private static func promptVersionToken(for versionCheck: AppVersionCheckData) -> String? {
        normalizedText(versionCheck.latestVersionCode) ?? normalizedText(versionCheck.latestVersionNo)
    }

    private static func stableIdentifier(forKey key: String, prefix: String) -> String {
        let defaults = UserDefaults.standard
        if let stored = normalizedText(defaults.string(forKey: key)) {
            return stored
        }
        let created = "\(prefix)-\(UUID().uuidString.lowercased())"
        defaults.set(created, forKey: key)
        return created
    }

    private static func stableDeviceIdentifier() -> String {
        let installationPrefix = "ios-device-installation-"
        if let stored = normalizedText(AppKeychainStore.readString(account: stableDeviceIdAccount)),
           stored.hasPrefix(installationPrefix) {
            return stored
        }
        let created = "\(installationPrefix)\(UUID().uuidString.lowercased())"
        AppKeychainStore.saveString(created, account: stableDeviceIdAccount)
        return created
    }

    private static func legacyDeviceIdentifiers(excluding primaryDeviceId: String) -> [String] {
        var identifiers: [String] = []
        let defaults = UserDefaults.standard
        if let stored = normalizedText(defaults.string(forKey: legacyDeviceIdKey)),
           stored != primaryDeviceId {
            identifiers.append(stored)
        }
        if let vendorId = normalizedText(UIDevice.current.identifierForVendor?.uuidString.lowercased()) {
            let vendorDeviceId = "ios-device-vendor-\(vendorId)"
            if vendorDeviceId != primaryDeviceId && !identifiers.contains(vendorDeviceId) {
                identifiers.append(vendorDeviceId)
            }
        }
        return identifiers
    }

    private static func normalizedText(_ raw: String?) -> String? {
        let trimmed = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? nil : trimmed
    }

    private static func configuredURL(
        infoPlistKey: String,
        environmentKey: String,
        simulatorDefault: String,
        deviceDefault: String
    ) -> String {
        if let override = normalizedText(ProcessInfo.processInfo.environment[environmentKey]) {
            return normalizedBaseURL(override)
        }
        if let infoPlistValue = normalizedText(Bundle.main.object(forInfoDictionaryKey: infoPlistKey) as? String) {
            return normalizedBaseURL(infoPlistValue)
        }
#if targetEnvironment(simulator)
        return normalizedBaseURL(simulatorDefault)
#else
        return normalizedBaseURL(deviceDefault)
#endif
    }

    private static func normalizedBaseURL(_ raw: String) -> String {
        var normalized = raw.trimmingCharacters(in: .whitespacesAndNewlines)
        while normalized.count > 1 && normalized.hasSuffix("/") {
            normalized.removeLast()
        }
        let removableSuffixes = ["/bff", "/api", "/backend", "/actuator"]
        var shouldContinue = true
        while shouldContinue {
            shouldContinue = false
            let lowered = normalized.lowercased()
            for suffix in removableSuffixes where lowered.hasSuffix(suffix) {
                normalized = String(normalized.dropLast(suffix.count))
                while normalized.count > 1 && normalized.hasSuffix("/") {
                    normalized.removeLast()
                }
                shouldContinue = true
                break
            }
        }
        return normalized
    }

    private static var tokenSigningSecret: String {
        let fromEnv = normalizedText(ProcessInfo.processInfo.environment["OPENAIPAY_TOKEN_SIGNING_SECRET"])
        return fromEnv ?? "openaipay-local-signing-secret-please-change"
    }
}

@MainActor
final class AppState: ObservableObject {
    private static let debugLaunchUserId: Int64 = 880100068483692100
    private static let demoTemplateAvatarURL = "https://static.aipay.local/demo-avatar/gujun.png"

    @Published private(set) var session: LoginResponseData?
    @Published private(set) var latestVersionCheck: AppVersionCheckData?
    @Published var optionalUpdatePresentation: AppUpdatePresentation?
    @Published private(set) var lastVersionCheckErrorMessage: String?

    private let authStore = AuthStore()
    private var isCheckingVersion = false
    private var lastVersionCheckAt: Date?
    private var isReportingDevice = false
    private var lastDeviceReportAt: Date?
    private var lastSyntheticVisitAtByName: [String: Date] = [:]
    private var lastBehaviorEventAtByName: [String: Date] = [:]
    private var hasHandledInitialLaunch = false

    private var shouldUseDebugLaunchSession: Bool {
        ProcessInfo.processInfo.arguments.contains("-OpenAiPayStartPage")
    }

    var isAuthenticated: Bool {
        session != nil
    }

    var appCode: String {
        AppRuntime.appCode
    }

    var deviceId: String {
        AppRuntime.deviceId
    }

    var forceUpdatePresentation: AppUpdatePresentation? {
        guard let latestVersionCheck,
              latestVersionCheck.forceUpdate else {
            return nil
        }
        return AppUpdatePresentation(from: latestVersionCheck)
    }

    var demoAutoLoginEnabled: Bool {
        latestVersionCheck?.demoAutoLoginEnabled ?? true
    }

    init() {
        _ = AppRuntime.deviceId
        _ = AppRuntime.clientId
        restoreSessionIfNeeded(force: true)
    }

    func ensureInitialLaunchHandled() {
        guard !hasHandledInitialLaunch else {
            return
        }
        hasHandledInitialLaunch = true
        restoreSessionIfNeeded(force: true)
        Task {
            await performActivationTasks(started: true, forceVersionCheck: true)
        }
    }

    func handleAppLaunch() {
        ensureInitialLaunchHandled()
    }

    func handleSceneDidBecomeActive() {
        if !hasHandledInitialLaunch {
            ensureInitialLaunchHandled()
            return
        }
        restoreSessionIfNeeded()
        Task {
            await performActivationTasks(started: false, forceVersionCheck: false)
        }
    }

    func handleSceneWillResignActive() {
        Task {
            await emitBehaviorEvent(
                eventName: "/app/inactive",
                eventType: "APP_LIFECYCLE",
                eventCode: "APP_INACTIVE",
                pageName: nil,
                actionName: nil,
                resultSummary: "APP_INACTIVE",
                durationMs: nil,
                payload: ["appUptimeMs": max(Int64(Date().timeIntervalSince(AppRuntime.appLaunchAt) * 1000), 0)]
            )
        }
    }

    func handleSceneDidEnterBackground() {
        Task {
            await emitBehaviorEvent(
                eventName: "/app/background",
                eventType: "APP_LIFECYCLE",
                eventCode: "APP_BACKGROUND",
                pageName: nil,
                actionName: nil,
                resultSummary: "APP_BACKGROUND",
                durationMs: nil,
                payload: ["appUptimeMs": max(Int64(Date().timeIntervalSince(AppRuntime.appLaunchAt) * 1000), 0)]
            )
        }
    }

    func onLoginSuccess(_ session: LoginResponseData) {
        let normalized = normalizeSession(session)
        self.session = normalized
        authStore.saveSession(normalized, bffBaseURL: AppRuntime.bffBaseURL)
        Task {
            await emitBehaviorEvent(
                eventName: "/auth/mobile-verify-login",
                eventType: "USER_ACTION",
                eventCode: "USER_LOGIN_SUCCESS",
                pageName: "/page/login",
                actionName: "LOGIN_SUBMIT",
                resultSummary: "SUCCESS",
                durationMs: nil,
                payload: ["userId": normalized.user.userId]
            )
        }
        Task {
            await performPostLoginTasks()
            await refreshCurrentUserProfileIfNeeded(userId: normalized.user.userId)
        }
    }

    func updateCurrentUserProfile(_ profile: UserProfile) {
        guard let session else {
            return
        }
        let updatedSession = LoginResponseData(
            accessToken: session.accessToken,
            tokenType: session.tokenType,
            expiresInSeconds: session.expiresInSeconds,
            user: profile,
            demo: session.demo
        )
        let normalized = normalizeSession(updatedSession)
        self.session = normalized
        authStore.saveSession(normalized, bffBaseURL: AppRuntime.bffBaseURL)
    }

    func restoreSessionIfNeeded(force: Bool = false) {
        if !force, session != nil {
            return
        }

        if AppRuntime.allowsLocalSyntheticAuth,
           !shouldUseDebugLaunchSession,
           let storedSession = authStore.loadSession() {
            let storedNormalized = normalizeSession(storedSession)
            if storedNormalized.user.userId == Self.debugLaunchUserId {
                let preferredLoginId = normalizedMainlandMobileLoginId(storedNormalized.user.loginId)
                session = nil
                authStore.clearSession()
                Task {
                    await restoreDebugLaunchSessionFromServer(preferredLoginId: preferredLoginId)
                }
                return
            }
        }

        if shouldUseDebugLaunchSession {
            if AppRuntime.allowsLocalSyntheticAuth {
                let preferredLoginId = normalizedMainlandMobileLoginId(authStore.loadSession()?.user.loginId)
                session = nil
                authStore.clearSession()
                Task {
                    await restoreDebugLaunchSessionFromServer(preferredLoginId: preferredLoginId)
                }
                return
            }
            // 调试启动页始终重建本地会话，避免沿用旧签名密钥生成的缓存 token 导致 401。
            let normalized = normalizeSession(Self.makeDebugLaunchSession())
            session = normalized
            authStore.saveSession(normalized, bffBaseURL: AppRuntime.bffBaseURL)
            Task {
                await refreshCurrentUserProfileIfNeeded(userId: Self.debugLaunchUserId)
            }
            return
        }
        guard let loaded = authStore.loadSession(expectedBffBaseURL: AppRuntime.bffBaseURL) else {
            if force {
                session = nil
            }
            return
        }
        let normalized = normalizeSession(loaded)
        let normalizedAccessToken = normalized.accessToken.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedAccessToken.isEmpty else {
            authStore.clearSession()
            session = nil
            return
        }
        if isSyntheticUserId(normalized.user.userId) {
            authStore.clearSession()
            session = nil
            return
        }
        session = normalized
        authStore.saveSession(normalized, bffBaseURL: AppRuntime.bffBaseURL)
        Task {
            await refreshCurrentUserProfileIfNeeded(userId: normalized.user.userId)
        }
    }

    func logout() {
        Task {
            await emitBehaviorEvent(
                eventName: "/auth/logout",
                eventType: "USER_ACTION",
                eventCode: "USER_LOGOUT",
                pageName: "/page/home",
                actionName: "LOGOUT",
                resultSummary: "SUCCESS",
                durationMs: nil,
                payload: nil
            )
        }
        session = nil
        optionalUpdatePresentation = nil
        authStore.clearSession()
    }

    func dismissOptionalUpdatePresentation() {
        optionalUpdatePresentation = nil
    }

    func recheckVersionNow() {
        Task {
            await performVersionCheck(force: true)
        }
    }

    func recordPageVisit(_ pageName: String) {
        let normalizedPageName = pageName.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedPageName.isEmpty else {
            return
        }
        guard shouldTrackSyntheticVisit(named: normalizedPageName, minimumInterval: 1) else {
            return
        }
        Task {
            await emitSyntheticVisit(apiName: normalizedPageName, resultSummary: "PAGE_VIEW")
            await emitBehaviorEvent(
                eventName: normalizedPageName,
                eventType: "PAGE_VIEW",
                eventCode: normalizedPageName,
                pageName: normalizedPageName,
                actionName: nil,
                resultSummary: "PAGE_VIEW",
                durationMs: nil,
                payload: nil
            )
        }
    }

    func recordBehaviorEvent(
        eventName: String,
        eventType: String?,
        eventCode: String?,
        pageName: String?,
        actionName: String?,
        resultSummary: String?,
        payload: [String: Any]? = nil,
        minimumInterval: TimeInterval = 0
    ) {
        let normalizedEventName = eventName.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedEventName.isEmpty else {
            return
        }
        Task {
            await emitBehaviorEvent(
                eventName: normalizedEventName,
                eventType: eventType,
                eventCode: eventCode,
                pageName: pageName,
                actionName: actionName,
                resultSummary: resultSummary,
                durationMs: nil,
                payload: payload,
                minimumInterval: minimumInterval
            )
        }
    }

    private func performActivationTasks(started: Bool, forceVersionCheck: Bool) async {
        await performVersionCheck(force: forceVersionCheck)

        let lifecycleApiName = started ? "/app/launch" : "/app/active"
        let lifecycleSummary = started ? "APP_LAUNCH" : "APP_ACTIVE"
        if shouldTrackSyntheticVisit(named: lifecycleApiName, minimumInterval: started ? 5 : 10) {
            await emitSyntheticVisit(apiName: lifecycleApiName, resultSummary: lifecycleSummary)
            await emitBehaviorEvent(
                eventName: lifecycleApiName,
                eventType: "APP_LIFECYCLE",
                eventCode: lifecycleSummary,
                pageName: nil,
                actionName: nil,
                resultSummary: lifecycleSummary,
                durationMs: nil,
                payload: ["appUptimeMs": max(Int64(Date().timeIntervalSince(AppRuntime.appLaunchAt) * 1000), 0)]
            )
        }

        guard isAuthenticated else {
            return
        }
        await reportCurrentDevice(started: started)
    }

    private func performPostLoginTasks() async {
        await performVersionCheck(force: false)
        await reportCurrentDevice(started: false)
    }

    private func refreshCurrentUserProfileIfNeeded(userId: Int64) async {
        guard userId > 0 else {
            return
        }
        do {
            let profile = try await APIClient.shared.fetchUserProfile(userId: userId)
            guard session?.user.userId == userId else {
                return
            }
            updateCurrentUserProfile(profile)
        } catch {
            guard shouldReconcileCurrentUserProfile(after: error, userId: userId),
                  let fallbackLoginId = currentSessionLoginId(for: userId) else {
                return
            }
            guard let resolvedProfile = try? await APIClient.shared.fetchUserProfile(loginId: fallbackLoginId) else {
                return
            }
            guard session?.user.userId == userId || normalizedMainlandMobileLoginId(session?.user.loginId) == fallbackLoginId else {
                return
            }
            updateCurrentUserProfile(resolvedProfile)
        }
    }

    private func performVersionCheck(force: Bool) async {
        if !force,
           let lastVersionCheckAt,
           Date().timeIntervalSince(lastVersionCheckAt) < 300 {
            return
        }
        guard !isCheckingVersion else {
            return
        }

        isCheckingVersion = true
        defer { isCheckingVersion = false }

        do {
            let versionCheck = try await APIClient.shared.checkAppVersion(
                appCode: AppRuntime.appCode,
                currentVersionNo: AppRuntime.currentVersionNo,
                deviceId: AppRuntime.deviceId
            )
            lastVersionCheckAt = Date()
            latestVersionCheck = versionCheck
            lastVersionCheckErrorMessage = nil

            if versionCheck.forceUpdate {
                optionalUpdatePresentation = nil
                return
            }

            guard versionCheck.versionPromptEnabled else {
                optionalUpdatePresentation = nil
                return
            }

            guard let presentation = AppUpdatePresentation(from: versionCheck) else {
                optionalUpdatePresentation = nil
                return
            }

            guard AppRuntime.shouldPromptForOptionalUpdate(versionCheck) else {
                return
            }

            AppRuntime.markOptionalUpdatePromptShown(for: versionCheck)
            optionalUpdatePresentation = presentation
        } catch {
            lastVersionCheckErrorMessage = userFacingErrorMessage(error)
        }
    }

    private func reportCurrentDevice(started: Bool) async {
        if !started,
           let lastDeviceReportAt,
           Date().timeIntervalSince(lastDeviceReportAt) < 15 {
            return
        }
        guard !isReportingDevice else {
            return
        }

        isReportingDevice = true
        defer { isReportingDevice = false }

        do {
            _ = try await APIClient.shared.reportAppDevice(
                deviceId: AppRuntime.deviceId,
                appCode: AppRuntime.appCode,
                clientIds: [AppRuntime.clientId],
                deviceBrand: AppRuntime.deviceBrand,
                osVersion: AppRuntime.osVersion,
                currentVersionCode: nil,
                currentVersionNo: AppRuntime.currentVersionNo,
                started: started
            )
            lastDeviceReportAt = Date()
        } catch {
        }
    }

    private func emitSyntheticVisit(apiName: String, resultSummary: String) async {
        do {
            _ = try await APIClient.shared.recordAppVisit(
                deviceId: AppRuntime.deviceId,
                appCode: AppRuntime.appCode,
                clientId: AppRuntime.clientId,
                ipAddress: nil,
                locationInfo: nil,
                tenantCode: nil,
                networkType: nil,
                currentVersionCode: nil,
                currentVersionNo: AppRuntime.currentVersionNo,
                deviceBrand: AppRuntime.deviceBrand,
                osVersion: AppRuntime.osVersion,
                apiName: apiName,
                requestParamsText: nil,
                resultSummary: resultSummary,
                durationMs: nil
            )
        } catch {
        }
    }

    private func emitBehaviorEvent(
        eventName: String,
        eventType: String?,
        eventCode: String?,
        pageName: String?,
        actionName: String?,
        resultSummary: String?,
        durationMs: Int64?,
        payload: [String: Any]?,
        minimumInterval: TimeInterval = 1
    ) async {
        guard shouldTrackBehaviorEvent(named: eventName, minimumInterval: minimumInterval) else {
            return
        }
        do {
            var rawPayload: [String: Any?] = [:]
            (payload ?? [:]).forEach { key, value in
                rawPayload[key] = value
            }
            let payloadJson = APIClient.makeBehaviorPayloadJson(rawPayload)
            _ = try await APIClient.shared.recordAppBehaviorEvent(
                eventName: eventName,
                eventType: eventType,
                eventCode: eventCode,
                pageName: pageName,
                actionName: actionName,
                resultSummary: resultSummary,
                durationMs: durationMs,
                payloadJson: payloadJson,
                eventAtEpochMs: Int64(Date().timeIntervalSince1970 * 1000),
                loginDurationMs: nil
            )
        } catch {
        }
    }

    private func shouldTrackSyntheticVisit(named name: String, minimumInterval: TimeInterval) -> Bool {
        let now = Date()
        if let lastTrackedAt = lastSyntheticVisitAtByName[name],
           now.timeIntervalSince(lastTrackedAt) < minimumInterval {
            return false
        }
        lastSyntheticVisitAtByName[name] = now
        return true
    }

    private func shouldTrackBehaviorEvent(named name: String, minimumInterval: TimeInterval) -> Bool {
        let now = Date()
        if let lastTrackedAt = lastBehaviorEventAtByName[name],
           now.timeIntervalSince(lastTrackedAt) < minimumInterval {
            return false
        }
        lastBehaviorEventAtByName[name] = now
        return true
    }

    private static func makeDebugLaunchSession() -> LoginResponseData {
        let userId = Self.debugLaunchUserId
        let accessToken = AppRuntime.makeLocalAccessToken(for: userId)
        return LoginResponseData(
            accessToken: accessToken,
            tokenType: "Bearer",
            expiresInSeconds: 7200,
            user: UserProfile(
                userId: userId,
                aipayUid: String(userId),
                loginId: "13920000001",
                accountStatus: "ACTIVE",
                kycLevel: "L3",
                nickname: "顾郡",
                avatarUrl: "https://api.dicebear.com/9.x/adventurer-neutral/png?seed=\(userId)",
                countryCode: "86",
                mobile: "13920000001",
                maskedRealName: nil,
                gender: "FEMALE",
                region: "广东佛山",
                birthday: "1996-01-19"
            )
        )
    }

    private func normalizeSession(_ source: LoginResponseData) -> LoginResponseData {
        let normalizedUserId = repairedUserId(from: source) ?? source.user.userId
        let normalizedAvatarURL = sanitizedCurrentUserAvatarURL(source, userId: normalizedUserId)
        let normalizedAccessToken = normalizeAccessToken(source.accessToken, userId: normalizedUserId)
        let normalizedIdCardNo = normalizedCurrentUserIdCardNo(source)
        if normalizedUserId == source.user.userId
            && normalizedAvatarURL == source.user.avatarUrl
            && normalizedAccessToken == source.accessToken
            && normalizedIdCardNo == source.user.idCardNo {
            return source
        }

        let normalizedUser = UserProfile(
            userId: normalizedUserId,
            aipayUid: source.user.aipayUid,
            loginId: source.user.loginId,
            accountStatus: source.user.accountStatus,
            kycLevel: source.user.kycLevel,
            nickname: source.user.nickname,
            avatarUrl: normalizedAvatarURL,
            countryCode: source.user.countryCode,
            mobile: source.user.mobile,
            maskedRealName: source.user.maskedRealName,
            idCardNo: normalizedIdCardNo,
            gender: source.user.gender,
            region: source.user.region,
            birthday: source.user.birthday
        )
        return LoginResponseData(
            accessToken: normalizedAccessToken,
            tokenType: source.tokenType,
            expiresInSeconds: source.expiresInSeconds,
            user: normalizedUser,
            demo: source.demo
        )
    }

    private func normalizeAccessToken(_ sourceToken: String, userId _: Int64) -> String {
        sourceToken.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private func sanitizedCurrentUserAvatarURL(_ source: LoginResponseData, userId: Int64) -> String? {
        let avatarUrl = source.user.avatarUrl
        let trimmed = avatarUrl?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let normalized = trimmed.lowercased()
        guard !normalized.isEmpty else {
            return "https://api.dicebear.com/9.x/adventurer-neutral/png?seed=\(userId)"
        }
        // 保留后端返回的真实头像，避免重新登录后被本地规则覆盖回旧头像。
        return trimmed
    }

    private func normalizedCurrentUserIdCardNo(_ source: LoginResponseData) -> String? {
        // 身份证号始终以用户真实资料为准，不注入演示模板证件号，
        // 避免与用户后续提交的实名信息串写。
        return source.user.idCardNo
    }

    private func isDemoAutoLoginSession(_ source: LoginResponseData) -> Bool {
        let demoLoginId = source.demo?.loginId.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return !demoLoginId.isEmpty
    }

    private func repairedUserId(from source: LoginResponseData) -> Int64? {
        let normalizedUid = source.user.aipayUid.trimmingCharacters(in: .whitespacesAndNewlines)
        let parsedAipayUid: Int64? = {
            guard let parsed = Int64(normalizedUid), parsed > 0 else {
                return nil
            }
            return parsed
        }()

        if let accessTokenUserId = userIdFromAccessToken(source.accessToken) {
            if accessTokenUserId == source.user.userId {
                return nil
            }

            if let parsedAipayUid, parsedAipayUid == accessTokenUserId {
                return accessTokenUserId
            }

            if source.user.userId <= 0 {
                return accessTokenUserId
            }

            if let parsedAipayUid, parsedAipayUid == source.user.userId {
                return accessTokenUserId
            }
        }

        // 历史 mock 会话里 accessToken 可能不可逆，但 aipayUid 仍然保存了真实用户号。
        if source.accessToken.trimmingCharacters(in: .whitespacesAndNewlines).lowercased().hasPrefix("mock-"),
           let parsedAipayUid,
           parsedAipayUid != source.user.userId {
            return parsedAipayUid
        }

        return nil
    }

    private func shouldReconcileCurrentUserProfile(after error: Error, userId: Int64) -> Bool {
        if isSyntheticUserId(userId) {
            return true
        }
        return isUserNotFoundError(error)
    }

    private func currentSessionLoginId(for userId: Int64) -> String? {
        guard let session else {
            return nil
        }
        guard session.user.userId == userId else {
            return nil
        }
        return normalizedMainlandMobileLoginId(session.user.loginId)
            ?? normalizedMainlandMobileLoginId(session.user.mobile)
    }

    private func restoreDebugLaunchSessionFromServer(preferredLoginId: String?) async {
        do {
            let remoteSession = try await APIClient.shared.demoAutoLogin(
                deviceId: AppRuntime.deviceId,
                preferredLoginId: preferredLoginId
            )
            let normalized = normalizeSession(remoteSession)
            session = normalized
            authStore.saveSession(normalized, bffBaseURL: AppRuntime.bffBaseURL)
            await performPostLoginTasks()
            await refreshCurrentUserProfileIfNeeded(userId: normalized.user.userId)
        } catch {
            // 本地调试兜底：若网络不可用，仍保留原有 synthetic 会话行为。
            let fallback = normalizeSession(Self.makeDebugLaunchSession())
            session = fallback
            authStore.saveSession(fallback, bffBaseURL: AppRuntime.bffBaseURL)
            await refreshCurrentUserProfileIfNeeded(userId: Self.debugLaunchUserId)
        }
    }

    private func isUserNotFoundError(_ error: Error) -> Bool {
        let normalized = userFacingErrorMessage(error)
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .lowercased()
        if normalized.contains("未找到用户")
            || normalized.contains("未找到user")
            || normalized.contains("未找到 user")
            || normalized.contains("user not found")
            || normalized.contains("用户不存在") {
            return true
        }
        return error.localizedDescription.lowercased().contains("user not found")
    }

    private func isSyntheticUserId(_ userId: Int64) -> Bool {
        userId >= 889_500_000_000_000_000
    }

    private func normalizedMainlandMobileLoginId(_ raw: String?) -> String? {
        guard let raw else {
            return nil
        }
        let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            return nil
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
        guard digits.range(of: "^1[3-9][0-9]{9}$", options: .regularExpression) != nil else {
            return nil
        }
        return digits
    }

    private func userIdFromAccessToken(_ accessToken: String) -> Int64? {
        let trimmedToken = accessToken.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedToken.isEmpty else {
            return nil
        }

        let lowercasedToken = trimmedToken.lowercased()
        if lowercasedToken.hasPrefix("mock-") {
            let suffix = trimmedToken.dropFirst(5)
            if let userId = Int64(suffix), userId > 0 {
                return userId
            }
        }

        let payloadToken: String
        if let separatorIndex = trimmedToken.firstIndex(of: ".") {
            payloadToken = String(trimmedToken[..<separatorIndex])
        } else {
            payloadToken = trimmedToken
        }

        let base64Token = payloadToken
            .replacingOccurrences(of: "-", with: "+")
            .replacingOccurrences(of: "_", with: "/")
        let paddingLength = (4 - (base64Token.count % 4)) % 4
        let paddedToken = base64Token + String(repeating: "=", count: paddingLength)

        guard let tokenData = Data(base64Encoded: paddedToken),
              let tokenPayload = String(data: tokenData, encoding: .utf8) else {
            return nil
        }

        guard let userIdSegment = tokenPayload.split(separator: ":", maxSplits: 1).first,
              let userId = Int64(userIdSegment),
              userId > 0 else {
            return nil
        }
        return userId
    }
}

private extension Data {
    func base64URLEncodedString() -> String {
        base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
    }
}
