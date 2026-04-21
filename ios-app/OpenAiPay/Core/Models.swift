import Foundation

private enum ModelRuntime {
    static var bffBaseURL: String {
#if SWIFT_PACKAGE
        configuredURL(
            environmentKey: "OPENAIPAY_BFF_BASE_URL",
            simulatorDefault: "http://127.0.0.1:3000",
            deviceDefault: "http://127.0.0.1:3000"
        )
#else
        AppRuntime.bffBaseURL
#endif
    }

    static var backendBaseURL: String {
#if SWIFT_PACKAGE
        configuredURL(
            environmentKey: "OPENAIPAY_BACKEND_BASE_URL",
            simulatorDefault: "http://127.0.0.1:8080",
            deviceDefault: "http://127.0.0.1:8080"
        )
#else
        AppRuntime.backendBaseURL
#endif
    }

#if SWIFT_PACKAGE
    private static func configuredURL(
        environmentKey: String,
        simulatorDefault: String,
        deviceDefault: String
    ) -> String {
        _ = deviceDefault
        if let override = normalizedText(ProcessInfo.processInfo.environment[environmentKey]) {
            return normalizedBaseURL(override)
        }
        return normalizedBaseURL(simulatorDefault)
    }

    private static func normalizedText(_ raw: String?) -> String? {
        let trimmed = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? nil : trimmed
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
#endif
}

private func normalizeAvatarURLString(_ raw: String?) -> String? {
    let trimmed = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
    guard !trimmed.isEmpty else {
        return nil
    }
    if trimmed.hasPrefix("http://") || trimmed.hasPrefix("https://") {
        if trimmed.contains("api.dicebear.com") && trimmed.contains("/svg?") {
            return trimmed.replacingOccurrences(of: "/svg?", with: "/png?")
        }
        return trimmed
    }
    if trimmed.hasPrefix("/") {
        if trimmed.hasPrefix("/bff/") {
            return "\(ModelRuntime.bffBaseURL)\(trimmed)"
        }
        return "\(ModelRuntime.backendBaseURL)\(trimmed)"
    }
    return trimmed
}

private func normalizeBackendMediaURLString(_ raw: String?) -> String? {
    let trimmed = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
    guard !trimmed.isEmpty else {
        return nil
    }
    if trimmed.hasPrefix("http://") || trimmed.hasPrefix("https://") {
        return trimmed
    }
    if trimmed.hasPrefix("/") {
        return "\(ModelRuntime.backendBaseURL)\(trimmed)"
    }
    return trimmed
}


struct BffEnvelope<T: Decodable>: Decodable {
    let success: Bool
    let requestId: String?
    let data: T?
    let error: BffError?
}

struct BffError: Decodable, Error {
    let code: String
    let message: String
}

struct DemoLoginMeta: Codable {
    let loginId: String
    let initialized: Bool?
}

struct LoginPresetAccountData: Decodable {
    let loginId: String
    let nickname: String

    enum CodingKeys: String, CodingKey {
        case loginId
        case nickname
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let normalizedLoginId = (try? container.decode(String.self, forKey: .loginId))?
            .trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let normalizedNickname = (try? container.decode(String.self, forKey: .nickname))?
            .trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        loginId = normalizedLoginId
        nickname = normalizedNickname.isEmpty ? normalizedLoginId : normalizedNickname
    }
}

struct LoginResponseData: Codable {
    let accessToken: String
    let tokenType: String
    let expiresInSeconds: Int
    let user: UserProfile
    let demo: DemoLoginMeta?

    enum CodingKeys: String, CodingKey {
        case accessToken
        case tokenType
        case expiresInSeconds
        case expiresIn
        case user
        case demo
    }

    init(
        accessToken: String,
        tokenType: String,
        expiresInSeconds: Int,
        user: UserProfile,
        demo: DemoLoginMeta? = nil
    ) {
        self.accessToken = accessToken
        self.tokenType = tokenType
        self.expiresInSeconds = expiresInSeconds
        self.user = user
        self.demo = demo
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let decodedAccessToken = (try? container.decode(String.self, forKey: .accessToken))?
            .trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        accessToken = decodedAccessToken.isEmpty ? "local-restored-token" : decodedAccessToken
        tokenType = (try? container.decode(String.self, forKey: .tokenType)) ?? "Bearer"

        if let value = try? container.decode(Int.self, forKey: .expiresInSeconds) {
            expiresInSeconds = value
        } else if let text = try? container.decode(String.self, forKey: .expiresInSeconds),
                  let value = Int(text) {
            expiresInSeconds = value
        } else if let value = try? container.decode(Int.self, forKey: .expiresIn) {
            expiresInSeconds = value
        } else if let text = try? container.decode(String.self, forKey: .expiresIn),
                  let value = Int(text) {
            expiresInSeconds = value
        } else {
            expiresInSeconds = 7200
        }

        user = try container.decode(UserProfile.self, forKey: .user)
        demo = try? container.decode(DemoLoginMeta.self, forKey: .demo)
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(accessToken, forKey: .accessToken)
        try container.encode(tokenType, forKey: .tokenType)
        try container.encode(expiresInSeconds, forKey: .expiresInSeconds)
        try container.encode(user, forKey: .user)
        try container.encodeIfPresent(demo, forKey: .demo)
    }
}

struct UserProfile: Codable {
    let userId: Int64
    let aipayUid: String
    let loginId: String
    let accountStatus: String
    let kycLevel: String
    let nickname: String
    let avatarUrl: String?
    let countryCode: String?
    let mobile: String?
    let maskedRealName: String?
    let idCardNo: String?
    let gender: String?
    let region: String?
    let birthday: String?

    enum CodingKeys: String, CodingKey {
        case userId
        case aipayUid = "aipayUid"
        case loginId
        case accountStatus
        case kycLevel
        case nickname
        case avatarUrl
        case countryCode
        case mobile
        case maskedRealName
        case idCardNo
        case gender
        case region
        case birthday
    }

    init(
        userId: Int64,
        aipayUid: String,
        loginId: String,
        accountStatus: String,
        kycLevel: String,
        nickname: String,
        avatarUrl: String?,
        countryCode: String?,
        mobile: String?,
        maskedRealName: String?,
        idCardNo: String? = nil,
        gender: String?,
        region: String?,
        birthday: String?
    ) {
        self.userId = userId
        self.aipayUid = aipayUid
        self.loginId = loginId
        self.accountStatus = accountStatus
        self.kycLevel = kycLevel
        self.nickname = nickname
        self.avatarUrl = normalizeAvatarURLString(avatarUrl)
        self.countryCode = countryCode
        self.mobile = mobile
        self.maskedRealName = maskedRealName
        self.idCardNo = idCardNo
        self.gender = gender
        self.region = region
        self.birthday = birthday
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)

        let resolvedUserId: Int64?
        if let numericUserId = try? container.decode(Int64.self, forKey: .userId) {
            resolvedUserId = numericUserId
        } else if let stringUserId = try? container.decode(String.self, forKey: .userId),
                  let parsedUserId = Int64(stringUserId.trimmingCharacters(in: .whitespacesAndNewlines)) {
            resolvedUserId = parsedUserId
        } else {
            resolvedUserId = nil
        }

        guard let resolvedUserId else {
            throw DecodingError.dataCorruptedError(
                forKey: .userId,
                in: container,
                debugDescription: "userId 不是合法的 Int64"
            )
        }
        userId = resolvedUserId

        if let value = try? container.decode(String.self, forKey: .aipayUid),
           !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            aipayUid = value
        } else {
            aipayUid = String(userId)
        }

        if let value = try? container.decode(String.self, forKey: .loginId),
           !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            loginId = value
        } else {
            loginId = String(userId)
        }

        accountStatus = (try? container.decode(String.self, forKey: .accountStatus)) ?? "ACTIVE"
        kycLevel = (try? container.decode(String.self, forKey: .kycLevel)) ?? "L0"
        if let value = try? container.decode(String.self, forKey: .nickname),
           !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            nickname = value
        } else {
            nickname = "用户\(userId)"
        }
        avatarUrl = normalizeAvatarURLString(try container.decodeIfPresent(String.self, forKey: .avatarUrl))
        countryCode = try container.decodeIfPresent(String.self, forKey: .countryCode)
        mobile = try container.decodeIfPresent(String.self, forKey: .mobile)
        maskedRealName = try container.decodeIfPresent(String.self, forKey: .maskedRealName)
        idCardNo = try container.decodeIfPresent(String.self, forKey: .idCardNo)
        gender = try container.decodeIfPresent(String.self, forKey: .gender)
        region = try container.decodeIfPresent(String.self, forKey: .region)
        birthday = try container.decodeIfPresent(String.self, forKey: .birthday)
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(userId, forKey: .userId)
        try container.encode(aipayUid, forKey: .aipayUid)
        try container.encode(loginId, forKey: .loginId)
        try container.encode(accountStatus, forKey: .accountStatus)
        try container.encode(kycLevel, forKey: .kycLevel)
        try container.encode(nickname, forKey: .nickname)
        try container.encodeIfPresent(avatarUrl, forKey: .avatarUrl)
        try container.encodeIfPresent(countryCode, forKey: .countryCode)
        try container.encodeIfPresent(mobile, forKey: .mobile)
        try container.encodeIfPresent(maskedRealName, forKey: .maskedRealName)
        try container.encodeIfPresent(idCardNo, forKey: .idCardNo)
        try container.encodeIfPresent(gender, forKey: .gender)
        try container.encodeIfPresent(region, forKey: .region)
        try container.encodeIfPresent(birthday, forKey: .birthday)
    }
}

struct AssetOverviewData: Decodable {
    let userId: String
    let currencyCode: String
    let availableAmount: String
    let reservedAmount: String
    let totalAmount: String
    let accountStatus: String
    let generatedAt: String

    enum CodingKeys: String, CodingKey {
        case userId
        case currencyCode
        case availableAmount
        case reservedAmount
        case totalAmount
        case accountStatus
        case generatedAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        userId = Self.decodeFlexibleString(from: container, forKey: .userId) ?? ""
        currencyCode = Self.decodeFlexibleString(from: container, forKey: .currencyCode)?.uppercased() ?? "CNY"
        availableAmount = Self.decodeFlexibleAmountString(from: container, forKey: .availableAmount) ?? "0"
        reservedAmount = Self.decodeFlexibleAmountString(from: container, forKey: .reservedAmount) ?? "0"
        totalAmount = Self.decodeFlexibleAmountString(from: container, forKey: .totalAmount) ?? availableAmount
        accountStatus = Self.decodeFlexibleString(from: container, forKey: .accountStatus) ?? "UNKNOWN"
        generatedAt = Self.decodeFlexibleString(from: container, forKey: .generatedAt) ?? ""
    }

    private static func decodeFlexibleString(
        from container: KeyedDecodingContainer<CodingKeys>,
        forKey key: CodingKeys
    ) -> String? {
        if let text = try? container.decodeIfPresent(String.self, forKey: key) {
            let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? nil : trimmed
        }
        if let value = try? container.decodeIfPresent(Int64.self, forKey: key) {
            return String(value)
        }
        if let value = try? container.decodeIfPresent(Int.self, forKey: key) {
            return String(value)
        }
        if let value = try? container.decodeIfPresent(Double.self, forKey: key) {
            return String(value)
        }
        if let value = try? container.decodeIfPresent(Decimal.self, forKey: key) {
            return NSDecimalNumber(decimal: value).stringValue
        }
        return nil
    }

    private static func decodeFlexibleAmountString(
        from container: KeyedDecodingContainer<CodingKeys>,
        forKey key: CodingKeys
    ) -> String? {
        if let text = decodeFlexibleString(from: container, forKey: key) {
            let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                return trimmed.replacingOccurrences(of: ",", with: "")
            }
        }
        return nil
    }
}

struct FlexibleDecimalData: Decodable {
    let value: Decimal

    init(from decoder: Decoder) throws {
        if let container = try? decoder.container(keyedBy: DynamicCodingKey.self) {
            if let amountKey = DynamicCodingKey(stringValue: "amount"),
               let amountText = try? container.decodeIfPresent(String.self, forKey: amountKey),
               let parsed = Self.parseDecimal(amountText) {
                value = parsed
                return
            }
            if let amountKey = DynamicCodingKey(stringValue: "amount"),
               let amountDecimal = try? container.decodeIfPresent(Decimal.self, forKey: amountKey) {
                value = amountDecimal
                return
            }
            if let valueKey = DynamicCodingKey(stringValue: "value"),
               let text = try? container.decodeIfPresent(String.self, forKey: valueKey),
               let parsed = Self.parseDecimal(text) {
                value = parsed
                return
            }
            if let valueKey = DynamicCodingKey(stringValue: "value"),
               let decimalValue = try? container.decodeIfPresent(Decimal.self, forKey: valueKey) {
                value = decimalValue
                return
            }
        }

        let single = try decoder.singleValueContainer()
        if let decimalValue = try? single.decode(Decimal.self) {
            value = decimalValue
            return
        }
        if let text = try? single.decode(String.self),
           let parsed = Self.parseDecimal(text) {
            value = parsed
            return
        }
        throw DecodingError.dataCorruptedError(in: single, debugDescription: "decimal value decode failed")
    }

    private static func parseDecimal(_ raw: String?) -> Decimal? {
        let normalized = raw?
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: ",", with: "") ?? ""
        guard !normalized.isEmpty else {
            return nil
        }
        return Decimal(string: normalized, locale: Locale(identifier: "en_US_POSIX"))
    }
}

struct FundAccountData: Decodable {
    let userId: Int64
    let fundCode: String
    let currencyCode: String
    let availableShare: Decimal
    let frozenShare: Decimal
    let pendingSubscribeAmount: Decimal
    let pendingRedeemShare: Decimal
    let holdingAmount: Decimal
    let accumulatedIncome: Decimal
    let yesterdayIncome: Decimal
    let latestNav: Decimal
    let accountStatus: String
    let annualizedYieldRate: Decimal
    let incomePer10k: Decimal
    let hasHoldingAmount: Bool
    let hasAccumulatedIncome: Bool
    let hasYesterdayIncome: Bool
    let hasAnnualizedYieldRate: Bool
    let hasIncomePer10k: Bool

    enum CodingKeys: String, CodingKey {
        case userId
        case fundCode
        case currencyCode
        case availableShare
        case frozenShare
        case pendingSubscribeAmount
        case pendingRedeemShare
        case holdingAmount
        case accumulatedIncome
        case yesterdayIncome
        case latestNav
        case accountStatus
        case annualizedYieldRate
        case incomePer10k
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        if let numericUserId = try? container.decode(Int64.self, forKey: .userId) {
            userId = numericUserId
        } else if let stringUserId = try? container.decode(String.self, forKey: .userId),
                  let parsedUserId = Int64(stringUserId.trimmingCharacters(in: .whitespacesAndNewlines)) {
            userId = parsedUserId
        } else {
            throw DecodingError.dataCorruptedError(
                forKey: .userId,
                in: container,
                debugDescription: "fundAccount.userId 不是合法的 Int64"
            )
        }

        fundCode = (try? container.decode(String.self, forKey: .fundCode)) ?? "AICASH"
        currencyCode = (try? container.decode(String.self, forKey: .currencyCode)) ?? "CNY"
        let availableShareData = try? container.decode(FlexibleDecimalData.self, forKey: .availableShare)
        let frozenShareData = try? container.decode(FlexibleDecimalData.self, forKey: .frozenShare)
        let pendingSubscribeAmountData = try? container.decode(FlexibleDecimalData.self, forKey: .pendingSubscribeAmount)
        let pendingRedeemShareData = try? container.decode(FlexibleDecimalData.self, forKey: .pendingRedeemShare)
        let holdingAmountData = try? container.decode(FlexibleDecimalData.self, forKey: .holdingAmount)
        let accumulatedIncomeData = try? container.decode(FlexibleDecimalData.self, forKey: .accumulatedIncome)
        let yesterdayIncomeData = try? container.decode(FlexibleDecimalData.self, forKey: .yesterdayIncome)
        let latestNavData = try? container.decode(FlexibleDecimalData.self, forKey: .latestNav)
        let annualizedYieldRateData = try? container.decode(FlexibleDecimalData.self, forKey: .annualizedYieldRate)
        let incomePer10kData = try? container.decode(FlexibleDecimalData.self, forKey: .incomePer10k)

        availableShare = availableShareData?.value ?? .zero
        frozenShare = frozenShareData?.value ?? .zero
        pendingSubscribeAmount = pendingSubscribeAmountData?.value ?? .zero
        pendingRedeemShare = pendingRedeemShareData?.value ?? .zero
        holdingAmount = holdingAmountData?.value ?? .zero
        accumulatedIncome = accumulatedIncomeData?.value ?? .zero
        yesterdayIncome = yesterdayIncomeData?.value ?? .zero
        latestNav = latestNavData?.value ?? .zero
        accountStatus = (try? container.decode(String.self, forKey: .accountStatus)) ?? "ACTIVE"
        annualizedYieldRate = annualizedYieldRateData?.value
            ?? (Decimal(string: "0.01045", locale: Locale(identifier: "en_US_POSIX")) ?? .zero)
        incomePer10k = incomePer10kData?.value
            ?? ((annualizedYieldRate / Decimal(365)) * Decimal(10_000))
        hasHoldingAmount = holdingAmountData != nil
        hasAccumulatedIncome = accumulatedIncomeData != nil
        hasYesterdayIncome = yesterdayIncomeData != nil
        hasAnnualizedYieldRate = annualizedYieldRateData != nil
        hasIncomePer10k = incomePer10kData != nil
    }

    var availableAmount: Decimal {
        max(.zero, holdingAmount)
    }
}

struct FundOpenAgreementTemplateData: Decodable {
    let templateCode: String
    let templateVersion: String
    let bizType: String
    let title: String
    let contentUrl: String
    let contentHash: String?
    let required: Bool

    enum CodingKeys: String, CodingKey {
        case templateCode
        case templateCodeSnake = "template_code"
        case templateVersion
        case templateVersionSnake = "template_version"
        case bizType
        case bizTypeSnake = "biz_type"
        case title
        case contentUrl
        case contentUrlSnake = "content_url"
        case contentHash
        case contentHashSnake = "content_hash"
        case required
        case requiredSnake = "required_flag"
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        templateCode = (try? container.decode(String.self, forKey: .templateCode))
            ?? (try? container.decode(String.self, forKey: .templateCodeSnake))
            ?? ""
        templateVersion = (try? container.decode(String.self, forKey: .templateVersion))
            ?? (try? container.decode(String.self, forKey: .templateVersionSnake))
            ?? ""
        bizType = (try? container.decode(String.self, forKey: .bizType))
            ?? (try? container.decode(String.self, forKey: .bizTypeSnake))
            ?? "FUND_ACCOUNT_OPEN"
        title = (try? container.decode(String.self, forKey: .title)) ?? "账户关联及基金交易委托协议"
        contentUrl = (try? container.decode(String.self, forKey: .contentUrl))
            ?? (try? container.decode(String.self, forKey: .contentUrlSnake))
            ?? ""
        contentHash = (try? container.decodeIfPresent(String.self, forKey: .contentHash))
            ?? (try? container.decodeIfPresent(String.self, forKey: .contentHashSnake))
        if let boolValue = try? container.decode(Bool.self, forKey: .required) {
            required = boolValue
        } else if let boolValue = try? container.decode(Bool.self, forKey: .requiredSnake) {
            required = boolValue
        } else if let intValue = try? container.decode(Int.self, forKey: .required) {
            required = intValue != 0
        } else if let intValue = try? container.decode(Int.self, forKey: .requiredSnake) {
            required = intValue != 0
        } else if let textValue = try? container.decode(String.self, forKey: .required) {
            let normalized = textValue.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
            required = normalized == "1" || normalized == "true" || normalized == "yes"
        } else if let textValue = try? container.decode(String.self, forKey: .requiredSnake) {
            let normalized = textValue.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
            required = normalized == "1" || normalized == "true" || normalized == "yes"
        } else {
            required = true
        }
    }
}

struct FundOpenAgreementPackData: Decodable {
    let userId: Int64
    let bizType: String
    let fundCode: String
    let currencyCode: String
    let agreements: [FundOpenAgreementTemplateData]

    enum CodingKeys: String, CodingKey {
        case userId
        case userIdSnake = "user_id"
        case bizType
        case bizTypeSnake = "biz_type"
        case fundCode
        case fundCodeSnake = "fund_code"
        case currencyCode
        case currencyCodeSnake = "currency_code"
        case agreements
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        if let numericUserId = try? container.decode(Int64.self, forKey: .userId) {
            userId = numericUserId
        } else if let numericUserId = try? container.decode(Int64.self, forKey: .userIdSnake) {
            userId = numericUserId
        } else if let stringUserId = try? container.decode(String.self, forKey: .userId),
                  let parsedUserId = Int64(stringUserId.trimmingCharacters(in: .whitespacesAndNewlines)) {
            userId = parsedUserId
        } else if let stringUserId = try? container.decode(String.self, forKey: .userIdSnake),
                  let parsedUserId = Int64(stringUserId.trimmingCharacters(in: .whitespacesAndNewlines)) {
            userId = parsedUserId
        } else {
            throw DecodingError.dataCorruptedError(
                forKey: .userId,
                in: container,
                debugDescription: "fundOpenAgreementPack.userId 不是合法的 Int64"
            )
        }
        bizType = (try? container.decode(String.self, forKey: .bizType))
            ?? (try? container.decode(String.self, forKey: .bizTypeSnake))
            ?? "FUND_ACCOUNT_OPEN"
        fundCode = (try? container.decode(String.self, forKey: .fundCode))
            ?? (try? container.decode(String.self, forKey: .fundCodeSnake))
            ?? "AICASH"
        currencyCode = (try? container.decode(String.self, forKey: .currencyCode))
            ?? (try? container.decode(String.self, forKey: .currencyCodeSnake))
            ?? "CNY"
        agreements = (try? container.decode([FundOpenAgreementTemplateData].self, forKey: .agreements)) ?? []
    }
}

struct FundOpenAccountWithAgreementResultData: Decodable {
    let userId: Int64
    let fundCode: String
    let currencyCode: String
    let signNo: String
    let signStatus: String
    let signedAt: String?
    let openedAt: String?
    let featureEnabled: Bool

    enum CodingKeys: String, CodingKey {
        case userId
        case userIdSnake = "user_id"
        case fundCode
        case fundCodeSnake = "fund_code"
        case currencyCode
        case currencyCodeSnake = "currency_code"
        case signNo
        case signNoSnake = "sign_no"
        case signStatus
        case signStatusSnake = "sign_status"
        case signedAt
        case signedAtSnake = "signed_at"
        case openedAt
        case openedAtSnake = "opened_at"
        case featureEnabled
        case featureEnabledSnake = "feature_enabled"
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        if let numericUserId = try? container.decode(Int64.self, forKey: .userId) {
            userId = numericUserId
        } else if let numericUserId = try? container.decode(Int64.self, forKey: .userIdSnake) {
            userId = numericUserId
        } else if let stringUserId = try? container.decode(String.self, forKey: .userId),
                  let parsedUserId = Int64(stringUserId.trimmingCharacters(in: .whitespacesAndNewlines)) {
            userId = parsedUserId
        } else if let stringUserId = try? container.decode(String.self, forKey: .userIdSnake),
                  let parsedUserId = Int64(stringUserId.trimmingCharacters(in: .whitespacesAndNewlines)) {
            userId = parsedUserId
        } else {
            throw DecodingError.dataCorruptedError(
                forKey: .userId,
                in: container,
                debugDescription: "fundOpenAccountResult.userId 不是合法的 Int64"
            )
        }
        fundCode = (try? container.decode(String.self, forKey: .fundCode))
            ?? (try? container.decode(String.self, forKey: .fundCodeSnake))
            ?? "AICASH"
        currencyCode = (try? container.decode(String.self, forKey: .currencyCode))
            ?? (try? container.decode(String.self, forKey: .currencyCodeSnake))
            ?? "CNY"
        signNo = (try? container.decode(String.self, forKey: .signNo))
            ?? (try? container.decode(String.self, forKey: .signNoSnake))
            ?? ""
        signStatus = (try? container.decode(String.self, forKey: .signStatus))
            ?? (try? container.decode(String.self, forKey: .signStatusSnake))
            ?? "SUCCEEDED"
        signedAt = (try? container.decodeIfPresent(String.self, forKey: .signedAt))
            ?? (try? container.decodeIfPresent(String.self, forKey: .signedAtSnake))
        openedAt = (try? container.decodeIfPresent(String.self, forKey: .openedAt))
            ?? (try? container.decodeIfPresent(String.self, forKey: .openedAtSnake))
        if let boolValue = try? container.decode(Bool.self, forKey: .featureEnabled) {
            featureEnabled = boolValue
        } else if let boolValue = try? container.decode(Bool.self, forKey: .featureEnabledSnake) {
            featureEnabled = boolValue
        } else if let intValue = try? container.decode(Int.self, forKey: .featureEnabled) {
            featureEnabled = intValue != 0
        } else if let intValue = try? container.decode(Int.self, forKey: .featureEnabledSnake) {
            featureEnabled = intValue != 0
        } else {
            featureEnabled = true
        }
    }
}

typealias CreditOpenAgreementTemplateData = FundOpenAgreementTemplateData

struct CreditProductOpenAgreementPackData: Decodable {
    let userId: Int64
    let bizType: String
    let productCode: String
    let agreements: [CreditOpenAgreementTemplateData]

    enum CodingKeys: String, CodingKey {
        case userId
        case userIdSnake = "user_id"
        case bizType
        case bizTypeSnake = "biz_type"
        case productCode
        case productCodeSnake = "product_code"
        case agreements
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        if let numericUserId = try? container.decode(Int64.self, forKey: .userId) {
            userId = numericUserId
        } else if let numericUserId = try? container.decode(Int64.self, forKey: .userIdSnake) {
            userId = numericUserId
        } else if let stringUserId = try? container.decode(String.self, forKey: .userId),
                  let parsedUserId = Int64(stringUserId.trimmingCharacters(in: .whitespacesAndNewlines)) {
            userId = parsedUserId
        } else if let stringUserId = try? container.decode(String.self, forKey: .userIdSnake),
                  let parsedUserId = Int64(stringUserId.trimmingCharacters(in: .whitespacesAndNewlines)) {
            userId = parsedUserId
        } else {
            throw DecodingError.dataCorruptedError(
                forKey: .userId,
                in: container,
                debugDescription: "creditProductOpenAgreementPack.userId 不是合法的 Int64"
            )
        }
        bizType = (try? container.decode(String.self, forKey: .bizType))
            ?? (try? container.decode(String.self, forKey: .bizTypeSnake))
            ?? "AICREDIT_OPEN"
        productCode = (try? container.decode(String.self, forKey: .productCode))
            ?? (try? container.decode(String.self, forKey: .productCodeSnake))
            ?? "AICREDIT"
        agreements = (try? container.decode([CreditOpenAgreementTemplateData].self, forKey: .agreements)) ?? []
    }
}

struct OpenCreditProductWithAgreementResultData: Decodable {
    let userId: Int64
    let productCode: String
    let accountNo: String
    let signNo: String
    let signStatus: String
    let signedAt: String?
    let openedAt: String?
    let featureEnabled: Bool

    enum CodingKeys: String, CodingKey {
        case userId
        case userIdSnake = "user_id"
        case productCode
        case productCodeSnake = "product_code"
        case accountNo
        case accountNoSnake = "account_no"
        case signNo
        case signNoSnake = "sign_no"
        case signStatus
        case signStatusSnake = "sign_status"
        case signedAt
        case signedAtSnake = "signed_at"
        case openedAt
        case openedAtSnake = "opened_at"
        case featureEnabled
        case featureEnabledSnake = "feature_enabled"
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        if let numericUserId = try? container.decode(Int64.self, forKey: .userId) {
            userId = numericUserId
        } else if let numericUserId = try? container.decode(Int64.self, forKey: .userIdSnake) {
            userId = numericUserId
        } else if let stringUserId = try? container.decode(String.self, forKey: .userId),
                  let parsedUserId = Int64(stringUserId.trimmingCharacters(in: .whitespacesAndNewlines)) {
            userId = parsedUserId
        } else if let stringUserId = try? container.decode(String.self, forKey: .userIdSnake),
                  let parsedUserId = Int64(stringUserId.trimmingCharacters(in: .whitespacesAndNewlines)) {
            userId = parsedUserId
        } else {
            throw DecodingError.dataCorruptedError(
                forKey: .userId,
                in: container,
                debugDescription: "openCreditProductResult.userId 不是合法的 Int64"
            )
        }
        productCode = (try? container.decode(String.self, forKey: .productCode))
            ?? (try? container.decode(String.self, forKey: .productCodeSnake))
            ?? "AICREDIT"
        accountNo = (try? container.decode(String.self, forKey: .accountNo))
            ?? (try? container.decode(String.self, forKey: .accountNoSnake))
            ?? ""
        signNo = (try? container.decode(String.self, forKey: .signNo))
            ?? (try? container.decode(String.self, forKey: .signNoSnake))
            ?? ""
        signStatus = (try? container.decode(String.self, forKey: .signStatus))
            ?? (try? container.decode(String.self, forKey: .signStatusSnake))
            ?? "SUCCEEDED"
        signedAt = (try? container.decodeIfPresent(String.self, forKey: .signedAt))
            ?? (try? container.decodeIfPresent(String.self, forKey: .signedAtSnake))
        openedAt = (try? container.decodeIfPresent(String.self, forKey: .openedAt))
            ?? (try? container.decodeIfPresent(String.self, forKey: .openedAtSnake))
        if let boolValue = try? container.decode(Bool.self, forKey: .featureEnabled) {
            featureEnabled = boolValue
        } else if let boolValue = try? container.decode(Bool.self, forKey: .featureEnabledSnake) {
            featureEnabled = boolValue
        } else if let intValue = try? container.decode(Int.self, forKey: .featureEnabled) {
            featureEnabled = intValue != 0
        } else if let intValue = try? container.decode(Int.self, forKey: .featureEnabledSnake) {
            featureEnabled = intValue != 0
        } else {
            featureEnabled = true
        }
    }
}

struct FundTransactionData: Decodable {
    let orderNo: String
    let transactionType: String
    let transactionStatus: String
    let message: String?
}

struct AssetChangesData: Codable {
    let userId: String
    let currencyCode: String
    let generatedAt: String
    let items: [AssetChangeItemData]
}

struct AssetBillEntriesData: Decodable {
    let userId: String
    let billMonth: String
    let businessDomainCode: String
    let generatedAt: String
    let items: [AssetBillEntryItemData]
    let pageNo: Int?
    let pageSize: Int?
    let hasMore: Bool?
    let nextPageNo: Int?
    let cursor: String?
    let nextCursor: String?

    enum CodingKeys: String, CodingKey {
        case userId
        case billMonth
        case businessDomainCode
        case generatedAt
        case items
        case pageNo
        case pageSize
        case hasMore
        case nextPageNo
        case cursor
        case nextCursor
    }

    init(
        userId: String,
        billMonth: String,
        businessDomainCode: String,
        generatedAt: String,
        items: [AssetBillEntryItemData],
        pageNo: Int? = nil,
        pageSize: Int? = nil,
        hasMore: Bool? = nil,
        nextPageNo: Int? = nil,
        cursor: String? = nil,
        nextCursor: String? = nil
    ) {
        self.userId = userId
        self.billMonth = billMonth
        self.businessDomainCode = businessDomainCode
        self.generatedAt = generatedAt
        self.items = items
        self.pageNo = pageNo
        self.pageSize = pageSize
        self.hasMore = hasMore
        self.nextPageNo = nextPageNo
        self.cursor = cursor
        self.nextCursor = nextCursor
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        userId = Self.decodeFlexibleString(from: container, forKey: .userId) ?? ""
        billMonth = Self.decodeFlexibleString(from: container, forKey: .billMonth) ?? ""
        businessDomainCode = Self.decodeFlexibleString(from: container, forKey: .businessDomainCode)?.uppercased() ?? ""
        generatedAt = Self.decodeFlexibleString(from: container, forKey: .generatedAt) ?? ""
        items = (try? container.decode([AssetBillEntryItemData].self, forKey: .items)) ?? []
        pageNo = Self.decodeFlexibleInt(from: container, forKey: .pageNo)
        pageSize = Self.decodeFlexibleInt(from: container, forKey: .pageSize)
        hasMore = Self.decodeFlexibleBool(from: container, forKey: .hasMore)
        nextPageNo = Self.decodeFlexibleInt(from: container, forKey: .nextPageNo)
        cursor = Self.decodeFlexibleString(from: container, forKey: .cursor)
        nextCursor = Self.decodeFlexibleString(from: container, forKey: .nextCursor)
    }

    private static func decodeFlexibleString(
        from container: KeyedDecodingContainer<CodingKeys>,
        forKey key: CodingKeys
    ) -> String? {
        if let text = try? container.decodeIfPresent(String.self, forKey: key) {
            let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? nil : trimmed
        }
        if let value = try? container.decodeIfPresent(Int64.self, forKey: key) {
            return String(value)
        }
        if let value = try? container.decodeIfPresent(Int.self, forKey: key) {
            return String(value)
        }
        if let value = try? container.decodeIfPresent(Double.self, forKey: key) {
            return String(value)
        }
        if let value = try? container.decodeIfPresent(Decimal.self, forKey: key) {
            return NSDecimalNumber(decimal: value).stringValue
        }
        return nil
    }

    private static func decodeFlexibleInt(
        from container: KeyedDecodingContainer<CodingKeys>,
        forKey key: CodingKeys
    ) -> Int? {
        if let value = try? container.decodeIfPresent(Int.self, forKey: key) {
            return value
        }
        if let value = try? container.decodeIfPresent(Int64.self, forKey: key) {
            return Int(value)
        }
        if let text = try? container.decodeIfPresent(String.self, forKey: key) {
            let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
            if let value = Int(trimmed) {
                return value
            }
        }
        return nil
    }

    private static func decodeFlexibleBool(
        from container: KeyedDecodingContainer<CodingKeys>,
        forKey key: CodingKeys
    ) -> Bool? {
        if let value = try? container.decodeIfPresent(Bool.self, forKey: key) {
            return value
        }
        if let value = try? container.decodeIfPresent(Int.self, forKey: key) {
            return value != 0
        }
        if let text = try? container.decodeIfPresent(String.self, forKey: key) {
            let normalized = text.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
            if normalized == "true" || normalized == "1" {
                return true
            }
            if normalized == "false" || normalized == "0" {
                return false
            }
        }
        return nil
    }
}

struct AssetBillEntryItemData: Decodable {
    let tradeNo: String
    let businessDomainCode: String
    let bizOrderNo: String
    let productType: String
    let businessType: String
    let direction: String?
    let tradeType: String?
    let accountNo: String
    let billNo: String
    let billMonth: String
    let displayTitle: String
    let displaySubtitle: String
    let amount: String
    let couponDiscountAmount: String?
    let currencyCode: String
    let status: String
    let tradeTime: String

    enum CodingKeys: String, CodingKey {
        case tradeNo
        case businessDomainCode
        case bizOrderNo
        case productType
        case businessType
        case direction
        case tradeType
        case accountNo
        case billNo
        case billMonth
        case displayTitle
        case displaySubtitle
        case amount
        case couponDiscountAmount
        case currencyCode
        case status
        case tradeTime
    }

    init(
        tradeNo: String,
        businessDomainCode: String,
        bizOrderNo: String,
        productType: String,
        businessType: String,
        direction: String? = nil,
        tradeType: String? = nil,
        accountNo: String,
        billNo: String,
        billMonth: String,
        displayTitle: String,
        displaySubtitle: String,
        amount: String,
        couponDiscountAmount: String? = nil,
        currencyCode: String,
        status: String,
        tradeTime: String
    ) {
        self.tradeNo = tradeNo
        self.businessDomainCode = businessDomainCode
        self.bizOrderNo = bizOrderNo
        self.productType = productType
        self.businessType = businessType
        self.direction = direction
        self.tradeType = tradeType
        self.accountNo = accountNo
        self.billNo = billNo
        self.billMonth = billMonth
        self.displayTitle = displayTitle
        self.displaySubtitle = displaySubtitle
        self.amount = amount
        self.couponDiscountAmount = couponDiscountAmount
        self.currencyCode = currencyCode
        self.status = status
        self.tradeTime = tradeTime
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        tradeNo = Self.decodeFlexibleString(from: container, forKey: .tradeNo) ?? ""
        businessDomainCode = Self.decodeFlexibleString(from: container, forKey: .businessDomainCode)?.uppercased() ?? ""
        bizOrderNo = Self.decodeFlexibleString(from: container, forKey: .bizOrderNo) ?? ""
        productType = Self.decodeFlexibleString(from: container, forKey: .productType) ?? ""
        businessType = Self.decodeFlexibleString(from: container, forKey: .businessType) ?? ""
        direction = Self.decodeFlexibleString(from: container, forKey: .direction)?.uppercased()
        tradeType = Self.decodeFlexibleString(from: container, forKey: .tradeType)?.uppercased()
        accountNo = Self.decodeFlexibleString(from: container, forKey: .accountNo) ?? ""
        billNo = Self.decodeFlexibleString(from: container, forKey: .billNo) ?? ""
        billMonth = Self.decodeFlexibleString(from: container, forKey: .billMonth) ?? ""
        displayTitle = Self.decodeFlexibleString(from: container, forKey: .displayTitle) ?? ""
        displaySubtitle = Self.decodeFlexibleString(from: container, forKey: .displaySubtitle) ?? ""
        amount = Self.decodeFlexibleAmountString(from: container, forKey: .amount) ?? "0"
        couponDiscountAmount = Self.decodeFlexibleAmountString(from: container, forKey: .couponDiscountAmount)
        currencyCode = Self.decodeFlexibleString(from: container, forKey: .currencyCode)?.uppercased() ?? "CNY"
        status = Self.decodeFlexibleString(from: container, forKey: .status)?.uppercased() ?? "UNKNOWN"
        tradeTime = Self.decodeFlexibleString(from: container, forKey: .tradeTime) ?? ""
    }

    private static func decodeFlexibleString(
        from container: KeyedDecodingContainer<CodingKeys>,
        forKey key: CodingKeys
    ) -> String? {
        if let text = try? container.decodeIfPresent(String.self, forKey: key) {
            let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? nil : trimmed
        }
        if let value = try? container.decodeIfPresent(Int64.self, forKey: key) {
            return String(value)
        }
        if let value = try? container.decodeIfPresent(Int.self, forKey: key) {
            return String(value)
        }
        if let value = try? container.decodeIfPresent(Double.self, forKey: key) {
            return String(value)
        }
        if let value = try? container.decodeIfPresent(Decimal.self, forKey: key) {
            return NSDecimalNumber(decimal: value).stringValue
        }
        return nil
    }

    private static func decodeFlexibleAmountString(
        from container: KeyedDecodingContainer<CodingKeys>,
        forKey key: CodingKeys
    ) -> String? {
        if let text = decodeFlexibleString(from: container, forKey: key) {
            let normalized = text.replacingOccurrences(of: ",", with: "")
            if normalized.isEmpty {
                return nil
            }
            return normalized
        }
        return nil
    }
}

struct AssetChangeItemData: Codable {
    let tradeNo: String
    let tradeType: String
    let businessSceneCode: String
    let direction: String
    let signedAmount: String
    let couponDiscountAmount: String?
    let currencyCode: String
    let counterpartyUserId: String?
    let counterpartyNickname: String?
    let counterpartyAvatarUrl: String?
    let displayTitle: String
    let bankCode: String?
    let bankName: String?
    let bankCardTailNo: String?
    let occurredAt: String

    enum CodingKeys: String, CodingKey {
        case tradeNo
        case tradeType
        case businessSceneCode
        case direction
        case signedAmount
        case couponDiscountAmount
        case currencyCode
        case counterpartyUserId
        case counterpartyNickname
        case counterpartyAvatarUrl
        case displayTitle
        case bankCode
        case bankName
        case bankCardTailNo
        case occurredAt
    }

    init(
        tradeNo: String,
        tradeType: String,
        businessSceneCode: String,
        direction: String,
        signedAmount: String,
        couponDiscountAmount: String? = nil,
        currencyCode: String,
        counterpartyUserId: String?,
        counterpartyNickname: String?,
        counterpartyAvatarUrl: String?,
        displayTitle: String,
        bankCode: String?,
        bankName: String?,
        bankCardTailNo: String?,
        occurredAt: String
    ) {
        self.tradeNo = tradeNo
        self.tradeType = tradeType
        self.businessSceneCode = businessSceneCode
        self.direction = direction
        self.signedAmount = signedAmount
        self.couponDiscountAmount = couponDiscountAmount
        self.currencyCode = currencyCode
        self.counterpartyUserId = counterpartyUserId
        self.counterpartyNickname = counterpartyNickname
        self.counterpartyAvatarUrl = counterpartyAvatarUrl
        self.displayTitle = displayTitle
        self.bankCode = bankCode
        self.bankName = bankName
        self.bankCardTailNo = bankCardTailNo
        self.occurredAt = occurredAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        tradeNo = try container.decode(String.self, forKey: .tradeNo)
        tradeType = try container.decode(String.self, forKey: .tradeType)
        businessSceneCode = try container.decode(String.self, forKey: .businessSceneCode)
        direction = try container.decode(String.self, forKey: .direction)
        signedAmount = try container.decode(String.self, forKey: .signedAmount)
        if let decimal = try? container.decode(Decimal.self, forKey: .couponDiscountAmount) {
            let normalized = NSDecimalNumber(decimal: decimal).stringValue
            let trimmed = normalized.trimmingCharacters(in: .whitespacesAndNewlines)
            couponDiscountAmount = trimmed.isEmpty ? nil : trimmed
        } else {
            couponDiscountAmount = Self.decodeOptionalTrimmedString(from: container, key: .couponDiscountAmount)
        }
        currencyCode = try container.decode(String.self, forKey: .currencyCode)
        if let stringValue = try? container.decode(String.self, forKey: .counterpartyUserId) {
            let trimmed = stringValue.trimmingCharacters(in: .whitespacesAndNewlines)
            counterpartyUserId = trimmed.isEmpty ? nil : trimmed
        } else if let numberValue = try? container.decode(Int64.self, forKey: .counterpartyUserId) {
            counterpartyUserId = String(numberValue)
        } else {
            counterpartyUserId = nil
        }
        counterpartyNickname = Self.decodeOptionalTrimmedString(from: container, key: .counterpartyNickname)
        counterpartyAvatarUrl = Self.decodeOptionalTrimmedString(from: container, key: .counterpartyAvatarUrl)
        displayTitle = try container.decode(String.self, forKey: .displayTitle)
        bankCode = Self.decodeOptionalTrimmedString(from: container, key: .bankCode)
        bankName = Self.decodeOptionalTrimmedString(from: container, key: .bankName)
        bankCardTailNo = Self.decodeOptionalTrimmedString(from: container, key: .bankCardTailNo)
        occurredAt = try container.decode(String.self, forKey: .occurredAt)
    }

    private static func decodeOptionalTrimmedString(
        from container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> String? {
        if let raw = try? container.decodeIfPresent(String.self, forKey: key) {
            let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? nil : trimmed
        }
        return nil
    }
}

struct MoneyValueData: Decodable {
    let amount: Decimal
    let currencyCode: String

    static let zero = MoneyValueData(amount: .zero, currencyCode: "CNY")

    init(amount: Decimal, currencyCode: String) {
        self.amount = amount
        self.currencyCode = currencyCode
    }

    init(from decoder: Decoder) throws {
        if let container = try? decoder.container(keyedBy: DynamicCodingKey.self) {
            let decodedAmount = Self.decodeAmount(from: container) ?? .zero
            let decodedCurrency = Self.decodeCurrencyCode(from: container) ?? "CNY"
            amount = decodedAmount
            currencyCode = decodedCurrency
            return
        }

        let single = try decoder.singleValueContainer()
        if let decimalAmount = try? single.decode(Decimal.self) {
            amount = decimalAmount
            currencyCode = "CNY"
            return
        }
        if let text = try? single.decode(String.self),
           let parsed = Self.parseDecimal(text) {
            amount = parsed
            currencyCode = "CNY"
            return
        }
        throw DecodingError.dataCorruptedError(in: single, debugDescription: "money amount decode failed")
    }

    private static func decodeAmount(from container: KeyedDecodingContainer<DynamicCodingKey>) -> Decimal? {
        for key in ["amount", "value"] {
            guard let codingKey = DynamicCodingKey(stringValue: key) else {
                continue
            }
            if let decimal = try? container.decodeIfPresent(Decimal.self, forKey: codingKey) {
                return decimal
            }
            if let text = try? container.decode(String.self, forKey: codingKey),
               let parsed = parseDecimal(text) {
                return parsed
            }
            if let number = try? container.decodeIfPresent(Double.self, forKey: codingKey) {
                return Decimal(string: String(number))
            }
            if let number = try? container.decodeIfPresent(Int.self, forKey: codingKey) {
                return Decimal(number)
            }
        }
        return nil
    }

    private static func decodeCurrencyCode(from container: KeyedDecodingContainer<DynamicCodingKey>) -> String? {
        for key in ["currencyCode", "currency"] {
            guard let codingKey = DynamicCodingKey(stringValue: key) else {
                continue
            }
            if let code = try? container.decodeIfPresent(String.self, forKey: codingKey) {
                let trimmed = code.trimmingCharacters(in: .whitespacesAndNewlines)
                if !trimmed.isEmpty {
                    return trimmed.uppercased()
                }
            }
        }

        guard let currencyUnitKey = DynamicCodingKey(stringValue: "currencyUnit") else {
            return nil
        }
        if let currencyUnitText = try? container.decodeIfPresent(String.self, forKey: currencyUnitKey) {
            let trimmed = currencyUnitText.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                return trimmed.uppercased()
            }
        }
        if let nested = try? container.nestedContainer(keyedBy: DynamicCodingKey.self, forKey: currencyUnitKey),
           let codeKey = DynamicCodingKey(stringValue: "code"),
           let code = try? nested.decodeIfPresent(String.self, forKey: codeKey) {
            let trimmed = code.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                return trimmed.uppercased()
            }
        }
        return nil
    }

    private static func parseDecimal(_ raw: String) -> Decimal? {
        let normalized = raw
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: ",", with: "")
        guard !normalized.isEmpty else {
            return nil
        }
        return Decimal(string: normalized, locale: Locale(identifier: "en_US_POSIX"))
    }
}

struct CreditCurrentBillDetailItemData: Decodable {
    let dateText: String
    let displayTitle: String
    let displaySubtitle: String
    let amount: MoneyValueData
    let businessNo: String
}

struct CreditCurrentBillDetailData: Decodable {
    let title: String
    let periodText: String
    let dueAmount: MoneyValueData
    let statementTotalAmount: MoneyValueData
    let refundedAmount: MoneyValueData
    let repaidAmount: MoneyValueData
    let items: [CreditCurrentBillDetailItemData]
}

struct CreditAccountData: Decodable {
    let accountNo: String
    let userId: Int64
    let totalLimit: MoneyValueData
    let availableLimit: MoneyValueData
    let principalBalance: MoneyValueData
    let overduePrincipalBalance: MoneyValueData
    let interestBalance: MoneyValueData
    let fineBalance: MoneyValueData
    // 爱花总计账单页展示的下月账单累计金额，来自本月新增消费汇总。
    let nextMonthBillAccumulatedAmount: MoneyValueData
    let repayDayOfMonth: Int
    let accountStatus: String
    let payStatus: String

    enum CodingKeys: String, CodingKey {
        case accountNo
        case userId
        case totalLimit
        case availableLimit
        case principalBalance
        case overduePrincipalBalance
        case interestBalance
        case fineBalance
        case nextMonthBillAccumulatedAmount
        case repayDayOfMonth
        case accountStatus
        case payStatus
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)

        accountNo = try container.decode(String.self, forKey: .accountNo)
        if let numericUserId = try? container.decode(Int64.self, forKey: .userId) {
            userId = numericUserId
        } else if let textUserId = try? container.decode(String.self, forKey: .userId),
                  let parsedUserId = Int64(textUserId.trimmingCharacters(in: .whitespacesAndNewlines)) {
            userId = parsedUserId
        } else {
            throw DecodingError.dataCorruptedError(
                forKey: .userId,
                in: container,
                debugDescription: "creditAccount.userId 不是合法的 Int64"
            )
        }

        totalLimit = (try? container.decode(MoneyValueData.self, forKey: .totalLimit)) ?? .zero
        availableLimit = (try? container.decode(MoneyValueData.self, forKey: .availableLimit)) ?? .zero
        principalBalance = (try? container.decode(MoneyValueData.self, forKey: .principalBalance)) ?? .zero
        overduePrincipalBalance = (try? container.decode(MoneyValueData.self, forKey: .overduePrincipalBalance)) ?? .zero
        interestBalance = (try? container.decode(MoneyValueData.self, forKey: .interestBalance)) ?? .zero
        fineBalance = (try? container.decode(MoneyValueData.self, forKey: .fineBalance)) ?? .zero
        nextMonthBillAccumulatedAmount = (try? container.decode(MoneyValueData.self, forKey: .nextMonthBillAccumulatedAmount)) ?? .zero
        repayDayOfMonth = (try? container.decode(Int.self, forKey: .repayDayOfMonth)) ?? 10
        accountStatus = (try? container.decode(String.self, forKey: .accountStatus)) ?? "NORMAL"
        payStatus = (try? container.decode(String.self, forKey: .payStatus)) ?? "NORMAL"
    }

    var totalOutstandingAmount: Decimal {
        max(Decimal.zero, principalBalance.amount)
            + max(Decimal.zero, overduePrincipalBalance.amount)
            + max(Decimal.zero, interestBalance.amount)
            + max(Decimal.zero, fineBalance.amount)
    }
}

struct LoanAccountData: Decodable {
    let accountNo: String
    let userId: Int64
    let totalLimit: MoneyValueData
    let availableLimit: MoneyValueData
    let annualRate: Decimal
    let originalAnnualRate: Decimal
    let repayDayOfMonth: Int
    let accountStatus: String
    let payStatus: String

    enum CodingKeys: String, CodingKey {
        case accountNo
        case userId
        case totalLimit
        case availableLimit
        case annualRate
        case originalAnnualRate
        case repayDayOfMonth
        case accountStatus
        case payStatus
    }

    private static let defaultLimitAmount = Decimal(string: "880000", locale: Locale(identifier: "en_US_POSIX")) ?? .zero
    private static let defaultAnnualRate = Decimal(string: "3.24", locale: Locale(identifier: "en_US_POSIX")) ?? .zero
    private static let defaultOriginalAnnualRate = Decimal(string: "5.04", locale: Locale(identifier: "en_US_POSIX")) ?? .zero

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)

        accountNo = (try? container.decode(String.self, forKey: .accountNo)) ?? ""
        if let numericUserId = try? container.decode(Int64.self, forKey: .userId) {
            userId = numericUserId
        } else if let textUserId = try? container.decode(String.self, forKey: .userId),
                  let parsedUserId = Int64(textUserId.trimmingCharacters(in: .whitespacesAndNewlines)) {
            userId = parsedUserId
        } else {
            throw DecodingError.dataCorruptedError(
                forKey: .userId,
                in: container,
                debugDescription: "loanAccount.userId 不是合法的 Int64"
            )
        }

        let defaultLimit = MoneyValueData(amount: Self.defaultLimitAmount, currencyCode: "CNY")
        totalLimit = (try? container.decode(MoneyValueData.self, forKey: .totalLimit)) ?? defaultLimit
        availableLimit = (try? container.decode(MoneyValueData.self, forKey: .availableLimit)) ?? defaultLimit
        annualRate = (try? container.decode(FlexibleDecimalData.self, forKey: .annualRate).value) ?? Self.defaultAnnualRate
        originalAnnualRate = (try? container.decode(FlexibleDecimalData.self, forKey: .originalAnnualRate).value) ?? Self.defaultOriginalAnnualRate
        repayDayOfMonth = (try? container.decode(Int.self, forKey: .repayDayOfMonth)) ?? 10
        accountStatus = (try? container.decode(String.self, forKey: .accountStatus)) ?? "NORMAL"
        payStatus = (try? container.decode(String.self, forKey: .payStatus)) ?? "NORMAL"
    }

    init(
        accountNo: String,
        userId: Int64,
        totalLimit: MoneyValueData,
        availableLimit: MoneyValueData,
        annualRate: Decimal,
        originalAnnualRate: Decimal,
        repayDayOfMonth: Int,
        accountStatus: String,
        payStatus: String
    ) {
        self.accountNo = accountNo
        self.userId = userId
        self.totalLimit = totalLimit
        self.availableLimit = availableLimit
        self.annualRate = annualRate
        self.originalAnnualRate = originalAnnualRate
        self.repayDayOfMonth = repayDayOfMonth
        self.accountStatus = accountStatus
        self.payStatus = payStatus
    }

    static func demo(userId: Int64) -> LoanAccountData {
        let defaultLimit = MoneyValueData(amount: defaultLimitAmount, currencyCode: "CNY")
        return LoanAccountData(
            accountNo: "LA\(userId)",
            userId: userId,
            totalLimit: defaultLimit,
            availableLimit: defaultLimit,
            annualRate: defaultAnnualRate,
            originalAnnualRate: defaultOriginalAnnualRate,
            repayDayOfMonth: 10,
            accountStatus: "NORMAL",
            payStatus: "NORMAL"
        )
    }
}

private struct DynamicCodingKey: CodingKey {
    let stringValue: String
    let intValue: Int?

    init?(stringValue: String) {
        self.stringValue = stringValue
        intValue = nil
    }

    init?(intValue: Int) {
        stringValue = "\(intValue)"
        self.intValue = intValue
    }
}

struct TransferTradeData: Decodable {
    let tradeNo: String
    let requestNo: String
    let status: String
    let payerUserId: Int64
    let payeeUserId: Int64?
    let feeAmount: MoneyValueData
    let payableAmount: MoneyValueData
    let settleAmount: MoneyValueData

    enum CodingKeys: String, CodingKey {
        case tradeNo
        case requestNo
        case status
        case payerUserId
        case payeeUserId
        case feeAmount
        case payableAmount
        case settleAmount
    }

    init(
        tradeNo: String,
        requestNo: String,
        status: String,
        payerUserId: Int64,
        payeeUserId: Int64?,
        feeAmount: MoneyValueData = .zero,
        payableAmount: MoneyValueData = .zero,
        settleAmount: MoneyValueData = .zero
    ) {
        self.tradeNo = tradeNo
        self.requestNo = requestNo
        self.status = status
        self.payerUserId = payerUserId
        self.payeeUserId = payeeUserId
        self.feeAmount = feeAmount
        self.payableAmount = payableAmount
        self.settleAmount = settleAmount
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        tradeNo = (try? container.decode(String.self, forKey: .tradeNo)) ?? ""
        requestNo = (try? container.decode(String.self, forKey: .requestNo)) ?? ""
        status = (try? container.decode(String.self, forKey: .status)) ?? "UNKNOWN"
        payerUserId = Self.decodeInt64(from: container, key: .payerUserId) ?? 0
        payeeUserId = Self.decodeInt64(from: container, key: .payeeUserId)
        feeAmount = (try? container.decodeIfPresent(MoneyValueData.self, forKey: .feeAmount)) ?? .zero
        payableAmount = (try? container.decodeIfPresent(MoneyValueData.self, forKey: .payableAmount)) ?? .zero
        settleAmount = (try? container.decodeIfPresent(MoneyValueData.self, forKey: .settleAmount)) ?? .zero
    }

    private static func decodeInt64(
        from container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> Int64? {
        if let numeric = try? container.decode(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key) {
            let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
            if trimmed.isEmpty {
                return nil
            }
            return Int64(trimmed)
        }
        return nil
    }
}

struct MobileTopUpRewardCouponData: Codable {
    let couponNo: String
    let templateCode: String
    let couponAmount: Decimal
    let currencyCode: String
    let claimedAt: String
    let dailyClaimLimit: Int
    let todayClaimedCount: Int
    let remainingClaimCount: Int

    enum CodingKeys: String, CodingKey {
        case couponNo
        case templateCode
        case couponAmount
        case currencyCode
        case claimedAt
        case dailyClaimLimit
        case todayClaimedCount
        case remainingClaimCount
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        couponNo = (try? container.decode(String.self, forKey: .couponNo)) ?? ""
        templateCode = (try? container.decode(String.self, forKey: .templateCode)) ?? ""
        currencyCode = (try? container.decode(String.self, forKey: .currencyCode)) ?? "CNY"
        claimedAt = (try? container.decode(String.self, forKey: .claimedAt)) ?? ""
        dailyClaimLimit = Self.decodeInt(container: container, key: .dailyClaimLimit) ?? 0
        todayClaimedCount = Self.decodeInt(container: container, key: .todayClaimedCount) ?? 0
        remainingClaimCount = Self.decodeInt(container: container, key: .remainingClaimCount) ?? 0

        if let decimal = try? container.decode(Decimal.self, forKey: .couponAmount) {
            couponAmount = decimal
        } else if let text = try? container.decode(String.self, forKey: .couponAmount),
                  let decimal = Decimal(string: text.trimmingCharacters(in: .whitespacesAndNewlines), locale: Locale(identifier: "en_US_POSIX")) {
            couponAmount = decimal
        } else {
            couponAmount = .zero
        }
    }

    private static func decodeInt(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> Int? {
        if let value = try? container.decode(Int.self, forKey: key) {
            return value
        }
        if let text = try? container.decode(String.self, forKey: key) {
            let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
            return Int(trimmed)
        }
        return nil
    }
}

struct MobileTopUpRewardCouponIssueData: Codable, Equatable, Identifiable {
    let couponNo: String
    let couponAmount: Decimal
    let currencyCode: String
    let expireAt: String?
    let expireDate: String?
    let status: String

    var id: String { couponNo }

    enum CodingKeys: String, CodingKey {
        case couponNo
        case couponAmount
        case currencyCode
        case expireAt
        case expireDate
        case status
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        couponNo = (try? container.decode(String.self, forKey: .couponNo)) ?? ""
        currencyCode = (try? container.decode(String.self, forKey: .currencyCode)) ?? "CNY"
        expireAt = Self.decodeOptionalString(container: container, key: .expireAt)
        expireDate = Self.decodeOptionalString(container: container, key: .expireDate)
        status = (try? container.decode(String.self, forKey: .status)) ?? "UNUSED"
        couponAmount = Self.decodeDecimal(container: container, key: .couponAmount) ?? .zero
    }

    private static func decodeOptionalString(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> String? {
        if let value = try? container.decodeIfPresent(String.self, forKey: key) {
            let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? nil : trimmed
        }
        return nil
    }

    private static func decodeDecimal(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> Decimal? {
        if let value = try? container.decode(Decimal.self, forKey: key) {
            return value
        }
        if let value = try? container.decode(Double.self, forKey: key) {
            return Decimal(string: String(value), locale: Locale(identifier: "en_US_POSIX"))
        }
        if let value = try? container.decode(String.self, forKey: key) {
            let normalized = value.trimmingCharacters(in: .whitespacesAndNewlines)
            guard !normalized.isEmpty else {
                return nil
            }
            return Decimal(string: normalized, locale: Locale(identifier: "en_US_POSIX"))
        }
        return nil
    }
}

struct CashierSceneConfigurationData: Codable {
    let supportedChannels: [String]
    let bankCardPolicy: String?
    let emptyBankCardText: String?
}

struct CashierViewData: Codable {
    let userId: Int64
    let sceneCode: String
    let sceneConfig: CashierSceneConfigurationData?
    let payTools: [CashierPayToolData]
    let generatedAt: String?

    enum CodingKeys: String, CodingKey {
        case userId
        case sceneCode
        case sceneConfig
        case payTools
        case generatedAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        if let numericUserId = try? container.decode(Int64.self, forKey: .userId) {
            userId = numericUserId
        } else if let stringUserId = try? container.decode(String.self, forKey: .userId),
                  let parsedUserId = Int64(stringUserId) {
            userId = parsedUserId
        } else {
            throw DecodingError.dataCorruptedError(
                forKey: .userId,
                in: container,
                debugDescription: "cashier.userId 不是合法的 Int64"
            )
        }
        sceneCode = try container.decode(String.self, forKey: .sceneCode)
        sceneConfig = try container.decodeIfPresent(CashierSceneConfigurationData.self, forKey: .sceneConfig)
        payTools = try container.decodeIfPresent([CashierPayToolData].self, forKey: .payTools) ?? []
        generatedAt = try container.decodeIfPresent(String.self, forKey: .generatedAt)
    }
}

struct CashierPricingPreviewData: Decodable {
    let userId: Int64
    let sceneCode: String
    let pricingSceneCode: String
    let paymentMethod: String
    let quoteNo: String
    let ruleCode: String
    let ruleName: String
    let originalAmount: MoneyValueData
    let feeAmount: MoneyValueData
    let payableAmount: MoneyValueData
    let settleAmount: MoneyValueData
    let feeRate: Decimal
    let feeBearer: String

    enum CodingKeys: String, CodingKey {
        case userId
        case sceneCode
        case pricingSceneCode
        case paymentMethod
        case quoteNo
        case ruleCode
        case ruleName
        case originalAmount
        case feeAmount
        case payableAmount
        case settleAmount
        case feeRate
        case feeBearer
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        if let numericUserId = try? container.decode(Int64.self, forKey: .userId) {
            userId = numericUserId
        } else if let stringUserId = try? container.decode(String.self, forKey: .userId),
                  let parsedUserId = Int64(stringUserId) {
            userId = parsedUserId
        } else {
            throw DecodingError.dataCorruptedError(
                forKey: .userId,
                in: container,
                debugDescription: "cashierPricing.userId 不是合法的 Int64"
            )
        }
        sceneCode = try container.decodeIfPresent(String.self, forKey: .sceneCode) ?? "WITHDRAW"
        pricingSceneCode = try container.decodeIfPresent(String.self, forKey: .pricingSceneCode) ?? sceneCode
        paymentMethod = try container.decodeIfPresent(String.self, forKey: .paymentMethod) ?? "BANK_CARD"
        quoteNo = try container.decodeIfPresent(String.self, forKey: .quoteNo) ?? ""
        ruleCode = try container.decodeIfPresent(String.self, forKey: .ruleCode) ?? ""
        ruleName = try container.decodeIfPresent(String.self, forKey: .ruleName) ?? ""
        originalAmount = try container.decodeIfPresent(MoneyValueData.self, forKey: .originalAmount) ?? .zero
        feeAmount = try container.decodeIfPresent(MoneyValueData.self, forKey: .feeAmount) ?? .zero
        payableAmount = try container.decodeIfPresent(MoneyValueData.self, forKey: .payableAmount) ?? .zero
        settleAmount = try container.decodeIfPresent(MoneyValueData.self, forKey: .settleAmount) ?? .zero
        if let decimalRate = try? container.decode(Decimal.self, forKey: .feeRate) {
            feeRate = decimalRate
        } else if let textRate = try? container.decode(String.self, forKey: .feeRate),
                  let parsedRate = Decimal(string: textRate, locale: Locale(identifier: "en_US_POSIX")) {
            feeRate = parsedRate
        } else {
            feeRate = .zero
        }
        feeBearer = try container.decodeIfPresent(String.self, forKey: .feeBearer) ?? "PAYEE"
    }
}

struct CashierPayToolData: Codable {
    let toolType: String
    let toolCode: String
    let toolName: String
    let toolDescription: String
    let defaultSelected: Bool
    let bankCode: String?
    let cardType: String?
    let phoneTailNo: String?
}

struct RecentContactData: Codable {
    let ownerUserId: Int64
    let contactUserId: Int64
    let contactAipayUid: String
    let contactNickname: String?
    let contactDisplayName: String
    let contactMaskedRealName: String?
    let contactAvatarUrl: String?
    let contactMobileMasked: String?
    let interactionSceneCode: String
    let interactionRemark: String?
    let interactionCount: Int64
    let lastInteractionAt: String?

    enum CodingKeys: String, CodingKey {
        case ownerUserId
        case contactUserId
        case contactAipayUid
        case contactNickname
        case contactDisplayName
        case contactMaskedRealName
        case contactAvatarUrl
        case contactMobileMasked
        case interactionSceneCode
        case interactionRemark
        case interactionCount
        case lastInteractionAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        ownerUserId = try Self.decodeInt64(container: container, key: .ownerUserId)
        contactUserId = try Self.decodeInt64(container: container, key: .contactUserId)
        contactAipayUid = try container.decodeIfPresent(String.self, forKey: .contactAipayUid) ?? String(contactUserId)
        contactNickname = try container.decodeIfPresent(String.self, forKey: .contactNickname)
        contactDisplayName = try container.decodeIfPresent(String.self, forKey: .contactDisplayName)
            ?? contactNickname
            ?? contactAipayUid
        contactMaskedRealName = try container.decodeIfPresent(String.self, forKey: .contactMaskedRealName)
        contactAvatarUrl = normalizeAvatarURLString(try container.decodeIfPresent(String.self, forKey: .contactAvatarUrl))
        contactMobileMasked = try container.decodeIfPresent(String.self, forKey: .contactMobileMasked)
        interactionSceneCode = try container.decodeIfPresent(String.self, forKey: .interactionSceneCode) ?? "UNKNOWN"
        interactionRemark = try container.decodeIfPresent(String.self, forKey: .interactionRemark)
        interactionCount = try Self.decodeInt64(container: container, key: .interactionCount, fallback: 0)
        lastInteractionAt = try container.decodeIfPresent(String.self, forKey: .lastInteractionAt)
    }

    private static func decodeInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys,
        fallback: Int64? = nil
    ) throws -> Int64 {
        if let numeric = try? container.decode(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        if let fallback {
            return fallback
        }
        throw DecodingError.dataCorruptedError(
            forKey: key,
            in: container,
            debugDescription: "\(key.stringValue) 不是合法的 Int64"
        )
    }
}

struct ConversationData: Codable {
    let conversationNo: String
    let conversationType: String
    let userId: Int64
    let peerUserId: Int64
    let peerAipayUid: String?
    let peerNickname: String?
    let peerAvatarUrl: String?
    let unreadCount: Int64
    let lastMessageId: String?
    let lastMessagePreview: String?
    let lastMessageAt: String?
    let updatedAt: String?

    enum CodingKeys: String, CodingKey {
        case conversationNo
        case conversationType
        case userId
        case peerUserId
        case peerAipayUid
        case peerNickname
        case peerAvatarUrl
        case unreadCount
        case lastMessageId
        case lastMessagePreview
        case lastMessageAt
        case updatedAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        conversationNo = try container.decode(String.self, forKey: .conversationNo)
        conversationType = try container.decodeIfPresent(String.self, forKey: .conversationType) ?? "PRIVATE"
        userId = try Self.decodeInt64(container: container, key: .userId)
        peerUserId = try Self.decodeInt64(container: container, key: .peerUserId)
        peerAipayUid = try container.decodeIfPresent(String.self, forKey: .peerAipayUid)
        peerNickname = try container.decodeIfPresent(String.self, forKey: .peerNickname)
        peerAvatarUrl = normalizeAvatarURLString(try container.decodeIfPresent(String.self, forKey: .peerAvatarUrl))
        unreadCount = try Self.decodeInt64(container: container, key: .unreadCount, fallback: 0)
        lastMessageId = try container.decodeIfPresent(String.self, forKey: .lastMessageId)
        lastMessagePreview = try container.decodeIfPresent(String.self, forKey: .lastMessagePreview)
        lastMessageAt = try container.decodeIfPresent(String.self, forKey: .lastMessageAt)
        updatedAt = try container.decodeIfPresent(String.self, forKey: .updatedAt)
    }

    private static func decodeInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys,
        fallback: Int64? = nil
    ) throws -> Int64 {
        if let numeric = try? container.decode(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        if let fallback {
            return fallback
        }
        throw DecodingError.dataCorruptedError(
            forKey: key,
            in: container,
            debugDescription: "\(key.stringValue) 不是合法的 Int64"
        )
    }
}

struct MessageHomeData: Codable {
    let userId: Int64
    let unreadTotal: Int64
    let conversations: [ConversationData]
    let generatedAt: String?

    enum CodingKeys: String, CodingKey {
        case userId
        case unreadTotal
        case conversations
        case generatedAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        userId = try Self.decodeInt64(container: container, key: .userId)
        unreadTotal = try Self.decodeInt64(container: container, key: .unreadTotal, fallback: 0)
        conversations = try container.decodeIfPresent([ConversationData].self, forKey: .conversations) ?? []
        generatedAt = try container.decodeIfPresent(String.self, forKey: .generatedAt)
    }

    private static func decodeInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys,
        fallback: Int64? = nil
    ) throws -> Int64 {
        if let numeric = try? container.decode(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        if let fallback {
            return fallback
        }
        throw DecodingError.dataCorruptedError(
            forKey: key,
            in: container,
            debugDescription: "\(key.stringValue) 不是合法的 Int64"
        )
    }
}

struct ContactFriendData: Codable {
    let friendUserId: Int64
    let aipayUid: String?
    let nickname: String?
    let maskedRealName: String?
    let mobileMasked: String?
    let avatarUrl: String?
    let remark: String?
    let createdAt: String?

    enum CodingKeys: String, CodingKey {
        case friendUserId
        case aipayUid
        case nickname
        case maskedRealName
        case mobileMasked
        case avatarUrl
        case remark
        case createdAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        friendUserId = try Self.decodeInt64(container: container, key: .friendUserId)
        aipayUid = try container.decodeIfPresent(String.self, forKey: .aipayUid)
        nickname = try container.decodeIfPresent(String.self, forKey: .nickname)
        maskedRealName = try container.decodeIfPresent(String.self, forKey: .maskedRealName)
        mobileMasked = try container.decodeIfPresent(String.self, forKey: .mobileMasked)
        avatarUrl = normalizeAvatarURLString(try container.decodeIfPresent(String.self, forKey: .avatarUrl))
        remark = try container.decodeIfPresent(String.self, forKey: .remark)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
    }

    private static func decodeInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) throws -> Int64 {
        if let numeric = try? container.decode(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        throw DecodingError.dataCorruptedError(
            forKey: key,
            in: container,
            debugDescription: "\(key.stringValue) 不是合法的 Int64"
        )
    }
}

struct ContactSearchData: Codable {
    let userId: Int64
    let aipayUid: String?
    let nickname: String?
    let avatarUrl: String?
    let mobile: String?
    let maskedRealName: String?
    let friend: Bool
    let blocked: Bool
    let remark: String?

    enum CodingKeys: String, CodingKey {
        case userId
        case aipayUid
        case nickname
        case avatarUrl
        case mobile
        case maskedRealName
        case friend
        case blocked
        case remark
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        userId = try Self.decodeInt64(container: container, key: .userId)
        aipayUid = try container.decodeIfPresent(String.self, forKey: .aipayUid)
        nickname = try container.decodeIfPresent(String.self, forKey: .nickname)
        avatarUrl = normalizeAvatarURLString(try container.decodeIfPresent(String.self, forKey: .avatarUrl))
        mobile = try container.decodeIfPresent(String.self, forKey: .mobile)
        maskedRealName = try container.decodeIfPresent(String.self, forKey: .maskedRealName)
        friend = Self.decodeBool(container: container, key: .friend, fallback: false)
        blocked = Self.decodeBool(container: container, key: .blocked, fallback: false)
        remark = try container.decodeIfPresent(String.self, forKey: .remark)
    }

    private static func decodeInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) throws -> Int64 {
        if let numeric = try? container.decode(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        throw DecodingError.dataCorruptedError(
            forKey: key,
            in: container,
            debugDescription: "\(key.stringValue) 不是合法的 Int64"
        )
    }

    private static func decodeBool(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys,
        fallback: Bool
    ) -> Bool {
        if let value = try? container.decode(Bool.self, forKey: key) {
            return value
        }
        if let value = try? container.decode(Int.self, forKey: key) {
            return value != 0
        }
        if let value = try? container.decode(String.self, forKey: key) {
            let normalized = value.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
            if normalized == "true" || normalized == "1" || normalized == "yes" {
                return true
            }
            if normalized == "false" || normalized == "0" || normalized == "no" {
                return false
            }
        }
        return fallback
    }
}

struct ContactRequestData: Codable {
    let requestNo: String
    let requesterUserId: Int64
    let targetUserId: Int64
    let requesterNickname: String?
    let requesterMaskedRealName: String?
    let requesterMobileMasked: String?
    let requesterAvatarUrl: String?
    let applyMessage: String?
    let status: String
    let handledByUserId: Int64?
    let handledAt: String?
    let createdAt: String?

    enum CodingKeys: String, CodingKey {
        case requestNo
        case requesterUserId
        case targetUserId
        case requesterNickname
        case requesterMaskedRealName
        case requesterMobileMasked
        case requesterAvatarUrl
        case applyMessage
        case status
        case handledByUserId
        case handledAt
        case createdAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        requestNo = try container.decodeIfPresent(String.self, forKey: .requestNo) ?? ""
        requesterUserId = try Self.decodeInt64(container: container, key: .requesterUserId)
        targetUserId = try Self.decodeInt64(container: container, key: .targetUserId)
        requesterNickname = try container.decodeIfPresent(String.self, forKey: .requesterNickname)
        requesterMaskedRealName = try container.decodeIfPresent(String.self, forKey: .requesterMaskedRealName)
        requesterMobileMasked = try container.decodeIfPresent(String.self, forKey: .requesterMobileMasked)
        requesterAvatarUrl = try container.decodeIfPresent(String.self, forKey: .requesterAvatarUrl)
        applyMessage = try container.decodeIfPresent(String.self, forKey: .applyMessage)
        status = try container.decodeIfPresent(String.self, forKey: .status) ?? "UNKNOWN"
        handledByUserId = Self.decodeOptionalInt64(container: container, key: .handledByUserId)
        handledAt = try container.decodeIfPresent(String.self, forKey: .handledAt)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
    }

    private static func decodeInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) throws -> Int64 {
        if let numeric = try? container.decode(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        throw DecodingError.dataCorruptedError(
            forKey: key,
            in: container,
            debugDescription: "\(key.stringValue) 不是合法的 Int64"
        )
    }

    private static func decodeOptionalInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> Int64? {
        if let numeric = try? container.decodeIfPresent(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        return nil
    }
}

struct SearchResponseData: Decodable {
    let keyword: String
    let sections: [SearchSectionData]
    let empty: Bool
    let meta: SearchMetaData?

    enum CodingKeys: String, CodingKey {
        case keyword
        case sections
        case empty
        case meta
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        keyword = try container.decodeIfPresent(String.self, forKey: .keyword) ?? ""
        sections = try container.decodeIfPresent([SearchSectionData].self, forKey: .sections) ?? []
        empty = (try? container.decode(Bool.self, forKey: .empty)) ?? sections.isEmpty
        meta = try container.decodeIfPresent(SearchMetaData.self, forKey: .meta)
    }
}

struct SearchMetaData: Decodable {
    let userId: Int64?
    let limit: Int?

    enum CodingKeys: String, CodingKey {
        case userId
        case limit
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        userId = Self.decodeOptionalInt64(container: container, key: .userId)
        limit = Self.decodeOptionalInt(container: container, key: .limit)
    }

    private static func decodeOptionalInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> Int64? {
        if let numeric = try? container.decodeIfPresent(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        return nil
    }

    private static func decodeOptionalInt(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> Int? {
        if let numeric = try? container.decodeIfPresent(Int.self, forKey: key) {
            return numeric
        }
        if let numeric = try? container.decodeIfPresent(Int64.self, forKey: key) {
            return Int(numeric)
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        return nil
    }
}

struct SearchSectionData: Decodable, Identifiable {
    let type: String
    let title: String
    let count: Int
    let items: [SearchResultItemData]

    var id: String {
        "\(type)_\(title)"
    }

    enum CodingKeys: String, CodingKey {
        case type
        case title
        case count
        case items
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        type = try container.decodeIfPresent(String.self, forKey: .type) ?? "unknown"
        title = try container.decodeIfPresent(String.self, forKey: .title) ?? type
        items = try container.decodeIfPresent([SearchResultItemData].self, forKey: .items) ?? []
        count = Self.decodeOptionalInt(container: container, key: .count) ?? items.count
    }

    private static func decodeOptionalInt(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> Int? {
        if let numeric = try? container.decodeIfPresent(Int.self, forKey: key) {
            return numeric
        }
        if let numeric = try? container.decodeIfPresent(Int64.self, forKey: key) {
            return Int(numeric)
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        return nil
    }
}

struct SearchResultItemData: Decodable, Identifiable {
    let id: String
    let userId: Int64?
    let aipayUid: String?
    let featureKey: String?
    let title: String
    let subtitle: String?
    let avatarUrl: String?
    let matchedField: String?
    let icon: String?
    let route: SearchRouteData?

    var resolvedUserId: Int64? {
        userId ?? route?.userId
    }

    var isContactResult: Bool {
        (route?.type ?? "").trimmingCharacters(in: .whitespacesAndNewlines).lowercased() == "contact"
    }

    var isFeatureResult: Bool {
        (route?.type ?? "").trimmingCharacters(in: .whitespacesAndNewlines).lowercased() == "page"
    }

    enum CodingKeys: String, CodingKey {
        case id
        case userId
        case aipayUid
        case featureKey
        case title
        case subtitle
        case avatarUrl
        case matchedField
        case icon
        case route
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decodeIfPresent(String.self, forKey: .id) ?? UUID().uuidString
        userId = Self.decodeOptionalInt64(container: container, key: .userId)
        aipayUid = try container.decodeIfPresent(String.self, forKey: .aipayUid)
        featureKey = try container.decodeIfPresent(String.self, forKey: .featureKey)
        title = try container.decodeIfPresent(String.self, forKey: .title) ?? "搜索结果"
        subtitle = Self.decodeOptionalTrimmedString(container: container, key: .subtitle)
        avatarUrl = normalizeAvatarURLString(try container.decodeIfPresent(String.self, forKey: .avatarUrl))
        matchedField = Self.decodeOptionalTrimmedString(container: container, key: .matchedField)
        icon = Self.decodeOptionalTrimmedString(container: container, key: .icon)
        route = try container.decodeIfPresent(SearchRouteData.self, forKey: .route)
    }

    private static func decodeOptionalTrimmedString(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> String? {
        let raw = try? container.decodeIfPresent(String.self, forKey: key)
        let trimmed = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? nil : trimmed
    }

    private static func decodeOptionalInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> Int64? {
        if let numeric = try? container.decodeIfPresent(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        return nil
    }
}

struct SearchRouteData: Decodable {
    let type: String
    let page: String?
    let userId: Int64?

    enum CodingKeys: String, CodingKey {
        case type
        case page
        case userId
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        type = try container.decodeIfPresent(String.self, forKey: .type) ?? ""
        page = Self.decodeOptionalTrimmedString(container: container, key: .page)
        userId = Self.decodeOptionalInt64(container: container, key: .userId)
    }

    private static func decodeOptionalTrimmedString(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> String? {
        let raw = try? container.decodeIfPresent(String.self, forKey: key)
        let trimmed = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? nil : trimmed
    }

    private static func decodeOptionalInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> Int64? {
        if let numeric = try? container.decodeIfPresent(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        return nil
    }
}

struct MessageData: Codable {
    let messageId: String
    let conversationNo: String
    let senderUserId: Int64
    let receiverUserId: Int64
    let messageType: String
    let contentText: String?
    let mediaId: String?
    let amount: MessageAmountData?
    let tradeNo: String?
    let extPayload: String?
    let messageStatus: String
    let createdAt: String?

    enum CodingKeys: String, CodingKey {
        case messageId
        case conversationNo
        case senderUserId
        case receiverUserId
        case messageType
        case contentText
        case mediaId
        case amount
        case tradeNo
        case extPayload
        case messageStatus
        case createdAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        messageId = try container.decode(String.self, forKey: .messageId)
        conversationNo = try container.decode(String.self, forKey: .conversationNo)
        senderUserId = try Self.decodeInt64(container: container, key: .senderUserId)
        receiverUserId = try Self.decodeInt64(container: container, key: .receiverUserId)
        messageType = try container.decodeIfPresent(String.self, forKey: .messageType) ?? "TEXT"
        contentText = try container.decodeIfPresent(String.self, forKey: .contentText)
        mediaId = try container.decodeIfPresent(String.self, forKey: .mediaId)
        amount = try container.decodeIfPresent(MessageAmountData.self, forKey: .amount)
        tradeNo = try container.decodeIfPresent(String.self, forKey: .tradeNo)
        extPayload = try container.decodeIfPresent(String.self, forKey: .extPayload)
        messageStatus = try container.decodeIfPresent(String.self, forKey: .messageStatus) ?? "SENT"
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
    }

    private static func decodeInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) throws -> Int64 {
        if let numeric = try? container.decode(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        throw DecodingError.dataCorruptedError(
            forKey: key,
            in: container,
            debugDescription: "\(key.stringValue) 不是合法的 Int64"
        )
    }
}

struct ConversationMessagesData: Codable {
    let userId: Int64
    let conversationNo: String
    let items: [MessageData]
    let generatedAt: String?

    enum CodingKeys: String, CodingKey {
        case userId
        case conversationNo
        case items
        case generatedAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        userId = try Self.decodeInt64(container: container, key: .userId)
        conversationNo = try container.decode(String.self, forKey: .conversationNo)
        items = try container.decodeIfPresent([MessageData].self, forKey: .items) ?? []
        generatedAt = try container.decodeIfPresent(String.self, forKey: .generatedAt)
    }

    private static func decodeInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) throws -> Int64 {
        if let numeric = try? container.decode(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        throw DecodingError.dataCorruptedError(
            forKey: key,
            in: container,
            debugDescription: "\(key.stringValue) 不是合法的 Int64"
        )
    }
}

struct RedPacketHistoryResponseData: Codable {
    let userId: Int64
    let direction: String
    let year: Int?
    let totalCount: Int64
    let totalAmount: MessageAmountData?
    let items: [RedPacketHistoryItemData]

    enum CodingKeys: String, CodingKey {
        case userId
        case direction
        case year
        case totalCount
        case totalAmount
        case items
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        userId = try Self.decodeInt64(container: container, key: .userId)
        direction = try container.decodeIfPresent(String.self, forKey: .direction) ?? "SENT"
        year = try container.decodeIfPresent(Int.self, forKey: .year)
        totalCount = try Self.decodeInt64(container: container, key: .totalCount, fallback: 0)
        totalAmount = try container.decodeIfPresent(MessageAmountData.self, forKey: .totalAmount)
        items = try container.decodeIfPresent([RedPacketHistoryItemData].self, forKey: .items) ?? []
    }

    private static func decodeInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys,
        fallback: Int64? = nil
    ) throws -> Int64 {
        if let numeric = try? container.decode(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        if let fallback {
            return fallback
        }
        throw DecodingError.dataCorruptedError(
            forKey: key,
            in: container,
            debugDescription: "\(key.stringValue) 不是合法的 Int64"
        )
    }
}

struct RedPacketHistoryItemData: Codable, Identifiable {
    let messageId: String
    let conversationNo: String
    let direction: String
    let counterpartyUserId: Int64
    let counterpartyNickname: String
    let counterpartyAvatarUrl: String?
    let amount: MessageAmountData?
    let tradeNo: String?
    let messageStatus: String
    let redPacketNo: String?
    let redPacketStatus: String?
    let createdAt: String?

    var id: String { messageId }

    enum CodingKeys: String, CodingKey {
        case messageId
        case conversationNo
        case direction
        case counterpartyUserId
        case counterpartyNickname
        case counterpartyAvatarUrl
        case amount
        case tradeNo
        case messageStatus
        case redPacketNo
        case redPacketStatus
        case createdAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        messageId = try container.decode(String.self, forKey: .messageId)
        conversationNo = try container.decode(String.self, forKey: .conversationNo)
        direction = try container.decodeIfPresent(String.self, forKey: .direction) ?? "SENT"
        counterpartyUserId = try Self.decodeInt64(container: container, key: .counterpartyUserId)
        counterpartyNickname = try container.decodeIfPresent(String.self, forKey: .counterpartyNickname) ?? ""
        counterpartyAvatarUrl = normalizeAvatarURLString(try container.decodeIfPresent(String.self, forKey: .counterpartyAvatarUrl))
        amount = try container.decodeIfPresent(MessageAmountData.self, forKey: .amount)
        tradeNo = try container.decodeIfPresent(String.self, forKey: .tradeNo)
        messageStatus = try container.decodeIfPresent(String.self, forKey: .messageStatus) ?? "SENT"
        redPacketNo = try container.decodeIfPresent(String.self, forKey: .redPacketNo)
        redPacketStatus = try container.decodeIfPresent(String.self, forKey: .redPacketStatus)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
    }

    private static func decodeInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) throws -> Int64 {
        if let numeric = try? container.decode(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        throw DecodingError.dataCorruptedError(
            forKey: key,
            in: container,
            debugDescription: "\(key.stringValue) 不是合法的 Int64"
        )
    }
}

struct RedPacketDetailData: Codable {
    let redPacketNo: String
    let messageId: String
    let conversationNo: String
    let senderUserId: Int64
    let senderNickname: String
    let senderAvatarUrl: String?
    let receiverUserId: Int64
    let receiverNickname: String
    let receiverAvatarUrl: String?
    let holdingUserId: Int64
    let amount: MessageAmountData?
    let paymentMethod: String
    let coverId: String?
    let coverTitle: String?
    let blessingText: String?
    let status: String
    let fundingTradeNo: String?
    let claimTradeNo: String?
    let claimableByViewer: Bool
    let claimedByViewer: Bool
    let claimedAt: String?
    let createdAt: String?

    enum CodingKeys: String, CodingKey {
        case redPacketNo
        case messageId
        case conversationNo
        case senderUserId
        case senderNickname
        case senderAvatarUrl
        case receiverUserId
        case receiverNickname
        case receiverAvatarUrl
        case holdingUserId
        case amount
        case paymentMethod
        case coverId
        case coverTitle
        case blessingText
        case status
        case fundingTradeNo
        case claimTradeNo
        case claimableByViewer
        case claimedByViewer
        case claimedAt
        case createdAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        redPacketNo = try container.decode(String.self, forKey: .redPacketNo)
        messageId = try container.decode(String.self, forKey: .messageId)
        conversationNo = try container.decode(String.self, forKey: .conversationNo)
        senderUserId = try Self.decodeInt64(container: container, key: .senderUserId)
        senderNickname = try container.decodeIfPresent(String.self, forKey: .senderNickname) ?? ""
        senderAvatarUrl = normalizeAvatarURLString(try container.decodeIfPresent(String.self, forKey: .senderAvatarUrl))
        receiverUserId = try Self.decodeInt64(container: container, key: .receiverUserId)
        receiverNickname = try container.decodeIfPresent(String.self, forKey: .receiverNickname) ?? ""
        receiverAvatarUrl = normalizeAvatarURLString(try container.decodeIfPresent(String.self, forKey: .receiverAvatarUrl))
        holdingUserId = try Self.decodeInt64(container: container, key: .holdingUserId)
        amount = try container.decodeIfPresent(MessageAmountData.self, forKey: .amount)
        paymentMethod = try container.decodeIfPresent(String.self, forKey: .paymentMethod) ?? "WALLET"
        coverId = try container.decodeIfPresent(String.self, forKey: .coverId)
        coverTitle = try container.decodeIfPresent(String.self, forKey: .coverTitle)
        blessingText = try container.decodeIfPresent(String.self, forKey: .blessingText)
        status = try container.decodeIfPresent(String.self, forKey: .status) ?? "PENDING_CLAIM"
        fundingTradeNo = try container.decodeIfPresent(String.self, forKey: .fundingTradeNo)
        claimTradeNo = try container.decodeIfPresent(String.self, forKey: .claimTradeNo)
        claimableByViewer = (try? container.decode(Bool.self, forKey: .claimableByViewer)) ?? false
        claimedByViewer = (try? container.decode(Bool.self, forKey: .claimedByViewer)) ?? false
        claimedAt = try container.decodeIfPresent(String.self, forKey: .claimedAt)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
    }

    private static func decodeInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) throws -> Int64 {
        if let numeric = try? container.decode(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        throw DecodingError.dataCorruptedError(
            forKey: key,
            in: container,
            debugDescription: "\(key.stringValue) 不是合法的 Int64"
        )
    }
}

struct MessageAmountData: Codable {
    let amount: Decimal?
    let currencyCode: String?
    let currencyUnit: MessageCurrencyUnitData?

    init(amount: Decimal?, currencyCode: String?, currencyUnit: MessageCurrencyUnitData?) {
        self.amount = amount
        self.currencyCode = currencyCode
        self.currencyUnit = currencyUnit
    }

    init(from decoder: Decoder) throws {
        if let container = try? decoder.container(keyedBy: DynamicCodingKey.self) {
            let resolvedAmount = Self.decodeAmount(from: container)
            let resolvedCurrencyCode = Self.decodeCurrencyCode(from: container)
            let resolvedCurrencyUnit = Self.decodeCurrencyUnit(from: container)
            self.init(
                amount: resolvedAmount,
                currencyCode: resolvedCurrencyCode,
                currencyUnit: resolvedCurrencyUnit
            )
            return
        }

        let single = try decoder.singleValueContainer()
        if let decimalAmount = try? single.decode(Decimal.self) {
            self.init(amount: decimalAmount, currencyCode: "CNY", currencyUnit: MessageCurrencyUnitData(code: "CNY"))
            return
        }
        if let text = try? single.decode(String.self),
           let parsed = Self.parseDecimal(text) {
            self.init(amount: parsed, currencyCode: "CNY", currencyUnit: MessageCurrencyUnitData(code: "CNY"))
            return
        }

        self.init(amount: nil, currencyCode: nil, currencyUnit: nil)
    }

    private static func decodeAmount(from container: KeyedDecodingContainer<DynamicCodingKey>) -> Decimal? {
        for key in ["amount", "value"] {
            guard let codingKey = DynamicCodingKey(stringValue: key) else {
                continue
            }
            if let decimal = try? container.decodeIfPresent(Decimal.self, forKey: codingKey) {
                return decimal
            }
            if let text = try? container.decode(String.self, forKey: codingKey),
               let parsed = parseDecimal(text) {
                return parsed
            }
            if let number = try? container.decodeIfPresent(Double.self, forKey: codingKey) {
                return Decimal(string: String(number))
            }
            if let number = try? container.decodeIfPresent(Int.self, forKey: codingKey) {
                return Decimal(number)
            }
        }
        return nil
    }

    private static func decodeCurrencyCode(from container: KeyedDecodingContainer<DynamicCodingKey>) -> String? {
        for key in ["currencyCode", "currency"] {
            guard let codingKey = DynamicCodingKey(stringValue: key) else {
                continue
            }
            if let code = try? container.decodeIfPresent(String.self, forKey: codingKey) {
                let trimmed = code.trimmingCharacters(in: .whitespacesAndNewlines)
                if !trimmed.isEmpty {
                    return trimmed.uppercased()
                }
            }
        }

        guard let currencyUnitKey = DynamicCodingKey(stringValue: "currencyUnit"),
              let nested = try? container.nestedContainer(keyedBy: DynamicCodingKey.self, forKey: currencyUnitKey),
              let codeKey = DynamicCodingKey(stringValue: "code"),
              let code = try? nested.decodeIfPresent(String.self, forKey: codeKey) else {
            return nil
        }
        let trimmed = code.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.isEmpty ? nil : trimmed.uppercased()
    }

    private static func decodeCurrencyUnit(from container: KeyedDecodingContainer<DynamicCodingKey>) -> MessageCurrencyUnitData? {
        if let currencyUnitKey = DynamicCodingKey(stringValue: "currencyUnit"),
           let nested = try? container.decodeIfPresent(MessageCurrencyUnitData.self, forKey: currencyUnitKey) {
            return nested
        }
        if let code = decodeCurrencyCode(from: container) {
            return MessageCurrencyUnitData(code: code)
        }
        return nil
    }

    private static func parseDecimal(_ raw: String) -> Decimal? {
        let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            return nil
        }
        return Decimal(string: trimmed, locale: Locale(identifier: "en_US_POSIX"))
    }
}

struct MessageCurrencyUnitData: Codable {
    let code: String?
}

struct MediaAssetData: Codable {
    let mediaId: String
    let ownerUserId: Int64?
    let mediaType: String?
    let originalName: String?
    let mimeType: String?
    let sizeBytes: Int64?
    let compressedSizeBytes: Int64?
    let width: Int?
    let height: Int?
    let contentUrl: String?
    let createdAt: String?

    enum CodingKeys: String, CodingKey {
        case mediaId
        case ownerUserId
        case mediaType
        case originalName
        case mimeType
        case sizeBytes
        case compressedSizeBytes
        case width
        case height
        case contentUrl
        case createdAt
    }

    init(
        mediaId: String,
        ownerUserId: Int64?,
        mediaType: String?,
        originalName: String?,
        mimeType: String?,
        sizeBytes: Int64?,
        compressedSizeBytes: Int64?,
        width: Int?,
        height: Int?,
        contentUrl: String?,
        createdAt: String?
    ) {
        self.mediaId = mediaId
        self.ownerUserId = ownerUserId
        self.mediaType = mediaType
        self.originalName = originalName
        self.mimeType = mimeType
        self.sizeBytes = sizeBytes
        self.compressedSizeBytes = compressedSizeBytes
        self.width = width
        self.height = height
        self.contentUrl = contentUrl
        self.createdAt = createdAt
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        mediaId = try container.decode(String.self, forKey: .mediaId)
        ownerUserId = try Self.decodeOptionalInt64(container: container, key: .ownerUserId)
        mediaType = try container.decodeIfPresent(String.self, forKey: .mediaType)
        originalName = try container.decodeIfPresent(String.self, forKey: .originalName)
        mimeType = try container.decodeIfPresent(String.self, forKey: .mimeType)
        sizeBytes = try Self.decodeOptionalInt64(container: container, key: .sizeBytes)
        compressedSizeBytes = try Self.decodeOptionalInt64(container: container, key: .compressedSizeBytes)
        width = try Self.decodeOptionalInt(container: container, key: .width)
        height = try Self.decodeOptionalInt(container: container, key: .height)
        contentUrl = try container.decodeIfPresent(String.self, forKey: .contentUrl)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
    }

    private static func decodeOptionalInt64(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) throws -> Int64? {
        if let numeric = try? container.decode(Int64.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        return nil
    }

    private static func decodeOptionalInt(
        container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) throws -> Int? {
        if let numeric = try? container.decode(Int.self, forKey: key) {
            return numeric
        }
        if let text = try? container.decode(String.self, forKey: key),
           let parsed = Int(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return parsed
        }
        return nil
    }
}



struct AppVersionCheckData: Codable {
    let appCode: String
    let currentVersionNo: String?
    let versionPromptEnabled: Bool
    let demoAutoLoginEnabled: Bool?
    let latestVersionCode: String?
    let latestVersionNo: String?
    let updateAvailable: Bool
    let forceUpdate: Bool
    let updateType: String?
    let updatePromptFrequency: String?
    let versionDescription: String?
    let minSupportedVersionNo: String?
    let appStoreUrl: String?
    let packageSizeBytes: Int64?
    let md5: String?
    let releaseStatus: String?
}

struct AppDeviceReportData: Codable {
    let deviceId: String
    let appCode: String
    let clientIds: [String]
    let status: String
    let installedAt: String?
    let startedAt: String?
    let lastOpenedAt: String?
    let currentAppVersionId: Int64?
    let currentVersionCode: String?
    let currentVersionNo: String?
    let currentIosPackageId: Int64?
    let currentIosCode: String?
    let appUpdatedAt: String?
    let deviceBrand: String?
    let osVersion: String?
}

struct AppVisitRecordData: Codable {
    let id: Int64?
    let deviceId: String
    let appCode: String
    let clientId: String?
    let ipAddress: String?
    let locationInfo: String?
    let tenantCode: String?
    let clientType: String
    let networkType: String?
    let appVersionId: Int64?
    let deviceBrand: String?
    let osVersion: String?
    let apiName: String
    let requestParamsText: String?
    let calledAt: String?
    let resultSummary: String?
    let durationMs: Int64?
}

struct AppBehaviorEventData: Codable {
    let id: Int64?
    let eventId: String?
    let sessionId: String?
    let appCode: String
    let eventName: String
    let eventType: String?
    let eventCode: String?
    let pageName: String?
    let actionName: String?
    let resultStatus: String?
    let traceId: String?
    let deviceId: String
    let clientId: String?
    let userId: Int64?
    let aipayUid: String?
    let loginId: String?
    let accountStatus: String?
    let kycLevel: String?
    let nickname: String?
    let mobile: String?
    let ipAddress: String?
    let locationInfo: String?
    let tenantCode: String?
    let networkType: String?
    let appVersionNo: String?
    let appBuildNo: String?
    let deviceBrand: String?
    let deviceModel: String?
    let deviceName: String?
    let deviceType: String?
    let osName: String?
    let osVersion: String?
    let locale: String?
    let timezone: String?
    let language: String?
    let countryCode: String?
    let carrierName: String?
    let screenWidth: Int?
    let screenHeight: Int?
    let viewportWidth: Int?
    let viewportHeight: Int?
    let durationMs: Int64?
    let loginDurationMs: Int64?
    let eventOccurredAt: String?
    let payloadJson: String?
    let createdAt: String?
}

struct FeedbackTicketData: Codable {
    let feedbackNo: String
    let userId: Int64
    let feedbackType: String
    let sourceChannel: String?
    let sourcePageCode: String?
    let title: String?
    let content: String
    let contactMobile: String?
    let attachmentUrls: [String]
    let status: String
    let handledBy: String?
    let handleNote: String?
    let handledAt: String?
    let closedAt: String?
    let createdAt: String?
    let updatedAt: String?
}


struct DeliverPositionData: Codable {
    let positionCode: String
    let positionName: String?
    let positionType: String?
    let previewImage: String?
    let slideInterval: Int?
    let maxDisplayCount: Int?
    let fallbackReturned: Bool
    let creativeList: [DeliverCreativeData]
}

struct DeliverCreativeData: Codable {
    let creativeCode: String
    let creativeName: String?
    let unitCode: String?
    let materialCode: String?
    let materialType: String?
    let materialTitle: String?
    let imageUrl: String?
    let landingUrl: String?
    let schemaJson: String?
    let priority: Int?
    let weight: Int?
    let displayOrder: Int?
    let fallback: Bool
    let previewImage: String?
}

struct ShortVideoFeedPageData: Decodable, Equatable {
    let items: [ShortVideoFeedItemData]
    let nextCursor: String?
    let hasMore: Bool

    enum CodingKeys: String, CodingKey {
        case items
        case nextCursor
        case hasMore
    }

    init(items: [ShortVideoFeedItemData], nextCursor: String?, hasMore: Bool) {
        self.items = items
        self.nextCursor = nextCursor?.trimmingCharacters(in: .whitespacesAndNewlines).nilIfEmpty
        self.hasMore = hasMore
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        items = try container.decodeIfPresent([ShortVideoFeedItemData].self, forKey: .items) ?? []
        nextCursor = (try? container.decode(String.self, forKey: .nextCursor))?
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .nilIfEmpty
        hasMore = try container.decodeIfPresent(Bool.self, forKey: .hasMore) ?? false
    }
}

struct ShortVideoFeedItemData: Decodable, Equatable, Identifiable {
    let videoId: String
    let caption: String
    let author: ShortVideoAuthorData
    let coverUrl: String
    let playback: ShortVideoPlaybackInfoData
    let engagement: ShortVideoEngagementData

    var id: String { videoId }

    enum CodingKeys: String, CodingKey {
        case videoId
        case caption
        case author
        case coverUrl
        case playback
        case engagement
    }

    init(
        videoId: String,
        caption: String,
        author: ShortVideoAuthorData,
        coverUrl: String,
        playback: ShortVideoPlaybackInfoData,
        engagement: ShortVideoEngagementData
    ) {
        self.videoId = videoId.trimmingCharacters(in: .whitespacesAndNewlines)
        self.caption = caption.trimmingCharacters(in: .whitespacesAndNewlines)
        self.author = author
        self.coverUrl = normalizeBackendMediaURLString(coverUrl) ?? coverUrl
        self.playback = playback
        self.engagement = engagement
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let rawVideoId = (try? container.decode(String.self, forKey: .videoId))?
            .trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        videoId = rawVideoId
        caption = (try? container.decode(String.self, forKey: .caption))?
            .trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        author = try container.decode(ShortVideoAuthorData.self, forKey: .author)
        let rawCoverUrl = (try? container.decode(String.self, forKey: .coverUrl)) ?? ""
        coverUrl = normalizeBackendMediaURLString(rawCoverUrl) ?? rawCoverUrl
        playback = try container.decode(ShortVideoPlaybackInfoData.self, forKey: .playback)
        engagement = try container.decodeIfPresent(ShortVideoEngagementData.self, forKey: .engagement)
            ?? ShortVideoEngagementData(liked: false, favorited: false, likeCount: 0, favoriteCount: 0, commentCount: 0)
    }
}

struct ShortVideoAuthorData: Decodable, Equatable {
    let userId: Int64
    let nickname: String
    let avatarUrl: String?

    enum CodingKeys: String, CodingKey {
        case userId
        case nickname
        case avatarUrl
    }

    init(userId: Int64, nickname: String, avatarUrl: String?) {
        self.userId = userId
        let trimmedNickname = nickname.trimmingCharacters(in: .whitespacesAndNewlines)
        self.nickname = trimmedNickname.isEmpty ? "用户\(userId)" : trimmedNickname
        self.avatarUrl = normalizeAvatarURLString(avatarUrl)
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        if let value = try? container.decode(Int64.self, forKey: .userId) {
            userId = value
        } else if let text = try? container.decode(String.self, forKey: .userId),
                  let value = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            userId = value
        } else {
            userId = 0
        }
        let rawNickname = (try? container.decode(String.self, forKey: .nickname)) ?? ""
        let trimmedNickname = rawNickname.trimmingCharacters(in: .whitespacesAndNewlines)
        nickname = trimmedNickname.isEmpty ? "用户\(userId)" : trimmedNickname
        avatarUrl = normalizeAvatarURLString(try? container.decode(String.self, forKey: .avatarUrl))
    }
}

struct ShortVideoPlaybackInfoData: Decodable, Equatable {
    let playbackUrl: String
    let playbackProtocol: String
    let mimeType: String
    let durationMs: Int64
    let width: Int?
    let height: Int?

    enum CodingKeys: String, CodingKey {
        case playbackUrl
        case playbackProtocol = "protocol"
        case mimeType
        case durationMs
        case width
        case height
    }

    init(
        playbackUrl: String,
        playbackProtocol: String,
        mimeType: String,
        durationMs: Int64,
        width: Int?,
        height: Int?
    ) {
        self.playbackUrl = normalizeBackendMediaURLString(playbackUrl) ?? playbackUrl
        self.playbackProtocol = playbackProtocol.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        self.mimeType = mimeType.trimmingCharacters(in: .whitespacesAndNewlines)
        self.durationMs = max(durationMs, 0)
        self.width = width
        self.height = height
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let rawPlaybackURL = (try? container.decode(String.self, forKey: .playbackUrl)) ?? ""
        playbackUrl = normalizeBackendMediaURLString(rawPlaybackURL) ?? rawPlaybackURL
        playbackProtocol = ((try? container.decode(String.self, forKey: .playbackProtocol)) ?? "MP4")
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .uppercased()
        mimeType = ((try? container.decode(String.self, forKey: .mimeType)) ?? "video/mp4")
            .trimmingCharacters(in: .whitespacesAndNewlines)
        if let value = try? container.decode(Int64.self, forKey: .durationMs) {
            durationMs = max(value, 0)
        } else if let text = try? container.decode(String.self, forKey: .durationMs),
                  let value = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            durationMs = max(value, 0)
        } else {
            durationMs = 0
        }
        width = try? container.decode(Int.self, forKey: .width)
        height = try? container.decode(Int.self, forKey: .height)
    }
}

struct ShortVideoEngagementData: Decodable, Equatable {
    let liked: Bool
    let favorited: Bool
    let likeCount: Int64
    let favoriteCount: Int64
    let commentCount: Int64

    enum CodingKeys: String, CodingKey {
        case liked
        case favorited
        case likeCount
        case favoriteCount
        case commentCount
    }

    init(liked: Bool, favorited: Bool, likeCount: Int64, favoriteCount: Int64, commentCount: Int64) {
        self.liked = liked
        self.favorited = favorited
        self.likeCount = max(likeCount, 0)
        self.favoriteCount = max(favoriteCount, 0)
        self.commentCount = max(commentCount, 0)
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        liked = try container.decodeIfPresent(Bool.self, forKey: .liked) ?? false
        favorited = try container.decodeIfPresent(Bool.self, forKey: .favorited) ?? false
        likeCount = ShortVideoEngagementData.decodeCount(container, key: .likeCount)
        favoriteCount = ShortVideoEngagementData.decodeCount(container, key: .favoriteCount)
        commentCount = ShortVideoEngagementData.decodeCount(container, key: .commentCount)
    }

    private static func decodeCount(
        _ container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> Int64 {
        if let value = try? container.decode(Int64.self, forKey: key) {
            return max(value, 0)
        }
        if let text = try? container.decode(String.self, forKey: key),
           let value = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return max(value, 0)
        }
        return 0
    }
}

struct ShortVideoCommentPageData: Decodable, Equatable {
    let items: [ShortVideoCommentData]
    let nextCursor: String?
    let hasMore: Bool

    enum CodingKeys: String, CodingKey {
        case items
        case nextCursor
        case hasMore
    }

    init(items: [ShortVideoCommentData], nextCursor: String?, hasMore: Bool) {
        self.items = items
        self.nextCursor = nextCursor?.trimmingCharacters(in: .whitespacesAndNewlines).nilIfEmpty
        self.hasMore = hasMore
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        items = try container.decodeIfPresent([ShortVideoCommentData].self, forKey: .items) ?? []
        nextCursor = (try? container.decode(String.self, forKey: .nextCursor))?
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .nilIfEmpty
        hasMore = try container.decodeIfPresent(Bool.self, forKey: .hasMore) ?? false
    }
}

struct ShortVideoCommentData: Decodable, Equatable, Identifiable {
    let commentId: String
    let videoId: String
    let parentCommentId: String?
    let rootCommentId: String?
    let user: ShortVideoAuthorData
    let content: String
    let imageUrl: String?
    let liked: Bool
    let likeCount: Int64
    let replyCount: Int64
    let previewReplies: [ShortVideoCommentData]
    let createdAt: String

    var id: String { commentId }

    enum CodingKeys: String, CodingKey {
        case commentId
        case videoId
        case parentCommentId
        case rootCommentId
        case user
        case content
        case imageUrl
        case liked
        case likeCount
        case replyCount
        case previewReplies
        case createdAt
    }

    init(commentId: String,
         videoId: String,
         parentCommentId: String? = nil,
         rootCommentId: String? = nil,
         user: ShortVideoAuthorData,
         content: String,
         imageUrl: String? = nil,
         liked: Bool = false,
         likeCount: Int64 = 0,
         replyCount: Int64 = 0,
         previewReplies: [ShortVideoCommentData] = [],
         createdAt: String) {
        self.commentId = commentId.trimmingCharacters(in: .whitespacesAndNewlines)
        self.videoId = videoId.trimmingCharacters(in: .whitespacesAndNewlines)
        self.parentCommentId = parentCommentId?.trimmingCharacters(in: .whitespacesAndNewlines).nilIfEmpty
        self.rootCommentId = rootCommentId?.trimmingCharacters(in: .whitespacesAndNewlines).nilIfEmpty
        self.user = user
        self.content = content.trimmingCharacters(in: .whitespacesAndNewlines)
        self.imageUrl = normalizeBackendMediaURLString(imageUrl)
        self.liked = liked
        self.likeCount = max(likeCount, 0)
        self.replyCount = max(replyCount, 0)
        self.previewReplies = previewReplies
        self.createdAt = createdAt.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        commentId = ((try? container.decode(String.self, forKey: .commentId)) ?? "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
        videoId = ((try? container.decode(String.self, forKey: .videoId)) ?? "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
        parentCommentId = ((try? container.decode(String.self, forKey: .parentCommentId)) ?? "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .nilIfEmpty
        rootCommentId = ((try? container.decode(String.self, forKey: .rootCommentId)) ?? "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .nilIfEmpty
        user = try container.decode(ShortVideoAuthorData.self, forKey: .user)
        content = ((try? container.decode(String.self, forKey: .content)) ?? "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
        imageUrl = normalizeBackendMediaURLString(try? container.decode(String.self, forKey: .imageUrl))
        liked = try container.decodeIfPresent(Bool.self, forKey: .liked) ?? false
        likeCount = ShortVideoCommentData.decodeCount(container, key: .likeCount)
        replyCount = ShortVideoCommentData.decodeCount(container, key: .replyCount)
        previewReplies = try container.decodeIfPresent([ShortVideoCommentData].self, forKey: .previewReplies) ?? []
        createdAt = ((try? container.decode(String.self, forKey: .createdAt)) ?? "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private static func decodeCount(
        _ container: KeyedDecodingContainer<CodingKeys>,
        key: CodingKeys
    ) -> Int64 {
        if let value = try? container.decode(Int64.self, forKey: key) {
            return max(value, 0)
        }
        if let text = try? container.decode(String.self, forKey: key),
           let value = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return max(value, 0)
        }
        return 0
    }
}

struct ShortVideoCommentLikeData: Decodable, Equatable {
    let commentId: String
    let liked: Bool
    let likeCount: Int64

    enum CodingKeys: String, CodingKey {
        case commentId
        case liked
        case likeCount
    }

    init(commentId: String, liked: Bool, likeCount: Int64) {
        self.commentId = commentId.trimmingCharacters(in: .whitespacesAndNewlines)
        self.liked = liked
        self.likeCount = max(likeCount, 0)
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        commentId = ((try? container.decode(String.self, forKey: .commentId)) ?? "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
        liked = try container.decodeIfPresent(Bool.self, forKey: .liked) ?? false
        if let value = try? container.decode(Int64.self, forKey: .likeCount) {
            likeCount = max(value, 0)
        } else if let text = try? container.decode(String.self, forKey: .likeCount),
                  let value = Int64(text.trimmingCharacters(in: .whitespacesAndNewlines)) {
            likeCount = max(value, 0)
        } else {
            likeCount = 0
        }
    }
}

private extension String {
    var nilIfEmpty: String? {
        isEmpty ? nil : self
    }
}
