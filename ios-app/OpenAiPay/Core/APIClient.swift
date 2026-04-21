import Foundation
import UIKit
import Network

private func localizedServerErrorMessage(_ raw: String) -> String {
    let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines)
    guard !trimmed.isEmpty else {
        return "操作失败，请稍后重试"
    }

    let lowercased = trimmed.lowercased()
    if lowercased.contains("user fast redeem quota exceeded") {
        return "今日快速转出额度不足，请明天再试"
    }
    if lowercased.contains("fund fast redeem quota exceeded") {
        return "爱存快速转出总额度不足，请稍后再试"
    }
    if lowercased.contains("未找到user") || lowercased.contains("未找到 user") {
        return "未找到用户"
    }
    if lowercased.contains("fund account not found") {
        return "未找到爱存账户，请稍后重试"
    }
    if lowercased.contains("credit account not found") {
        return "未找到信用账户，请稍后重试"
    }
    if lowercased.contains("fund account already exists") {
        return "爱存账户已存在"
    }
    if lowercased.contains("insufficient available share") {
        return "可转出份额不足"
    }
    if lowercased.contains("pending redeem share is not enough") {
        return "待确认转出份额不足，请稍后重试"
    }
    if lowercased.contains("pending subscribe amount is not enough") {
        return "待确认转入金额不足，请稍后重试"
    }
    if lowercased.contains("product switch is disabled") {
        return "当前爱存暂不支持切换"
    }
    if lowercased.contains("fund product status is not active") {
        return "当前爱存状态不可用"
    }
    if lowercased.contains("fund account status is not active") {
        return "当前爱存账户状态不可用"
    }
    if lowercased.contains("product not found") {
        return "未找到爱存产品"
    }
    if lowercased.contains("orderno must match aipay 32-digit id rule") {
        return "请求单号格式不正确，请稍后重试"
    }
    if lowercased.contains("orderno already used by another transaction type") {
        return "请求已提交，请勿重复操作"
    }
    if lowercased.contains("orderno is not a subscribe transaction") {
        return "转入请求状态异常，请稍后重试"
    }
    if lowercased.contains("orderno is not a redeem transaction") {
        return "转出请求状态异常，请稍后重试"
    }
    if lowercased.contains("subscribe transaction not found") {
        return "未找到转入记录，请稍后重试"
    }
    if lowercased.contains("redeem transaction not found") {
        return "未找到转出记录，请稍后重试"
    }
    if lowercased.contains("userid must be greater than 0") {
        return "用户标识无效"
    }
    if lowercased.contains("share must be greater than 0") {
        return "转出份额必须大于0"
    }
    if lowercased.contains("amount must be greater than 0") {
        return "金额必须大于0"
    }
    if lowercased.contains("subscribe amount is lower than minimum") {
        return "转入金额低于最低限制"
    }
    if lowercased.contains("subscribe amount is greater than maximum") {
        return "转入金额超出单笔上限"
    }
    if lowercased.contains("redeem share is lower than minimum") {
        return "转出金额低于最低限制"
    }
    if lowercased.contains("redeem share is greater than maximum") {
        return "转出金额超出单笔上限"
    }
    if lowercased.contains("nav must be greater than 0") {
        return "净值数据异常，请稍后重试"
    }
    if lowercased.contains("sender and receiver must be friends") {
        return "当前收款人不是好友，暂时不能发红包"
    }
    if lowercased.contains("red packet not found") {
        return "红包不存在或已失效"
    }
    if lowercased.contains("current user is not the red packet receiver") {
        return "当前用户不能领取这个红包"
    }
    if lowercased.contains("red packet status does not allow claim") {
        return "这个红包当前不可领取"
    }
    if lowercased.contains("required request parameter") && lowercased.contains("owneruser") {
        return "图片上传参数缺失，请稍后重试"
    }
    if lowercased.contains("required part") && lowercased.contains("file") {
        return "图片文件未被服务端识别，请稍后重试"
    }
    if lowercased.contains("cannot connect")
        || lowercased.contains("failed to connect")
        || lowercased.contains("could not connect to the server") {
        return "无法连接服务，请确认本地服务已启动"
    }
    if let genericLocalized = localizedServerErrorMessageByPattern(trimmed, lowercased: lowercased) {
        return AppBranding.displayText(genericLocalized)
    }
    return AppBranding.displayText(trimmed)
}

private func localizedServerErrorMessageByPattern(_ raw: String, lowercased: String) -> String? {
    if lowercased.contains("participant split amount must equal payableamount")
        || lowercased.contains("participant split amount must equalpayableamount")
        || lowercased.contains("walletdebitamount + funddebitamount + creditdebitamount + inbounddebitamount must equal payableamount") {
        return "支付拆分金额必须等于应付金额"
    }
    if lowercased.contains("insufficient available balance") {
        return "可用余额不足"
    }
    if lowercased.contains("insufficient available share") {
        return "可用份额不足"
    }
    if lowercased.contains("currency mismatch") {
        return "币种不一致"
    }
    if lowercased.contains("operationtype must be debit or credit") {
        return "操作类型必须为借记或贷记"
    }
    if lowercased.contains("payeeuserid must not equal payeruserid for transfer") {
        return "转账收款方不能是自己"
    }
    if lowercased.contains("invalid request") {
        return "请求参数不合法"
    }

    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) must not be blank$"#) {
        return "\(localizedErrorFieldName(groups[1]))不能为空"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) must not be null$"#) {
        return "\(localizedErrorFieldName(groups[1]))不能为空"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) must be greater than 0$"#) {
        return "\(localizedErrorFieldName(groups[1]))必须大于0"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) must be greater than or equal to 0$"#)
        ?? matchedGroups(in: raw, pattern: #"^(.+?) must not be less than 0$"#) {
        return "\(localizedErrorFieldName(groups[1]))不能小于0"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) length must be <= (\d+)$"#)
        ?? matchedGroups(in: raw, pattern: #"^(.+?) length must be less than or equal to (\d+)$"#) {
        return "\(localizedErrorFieldName(groups[1]))长度不能超过\(groups[2])"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) size must be less than or equal to (\d+)$"#) {
        return "\(localizedErrorFieldName(groups[1]))数量不能超过\(groups[2])"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) must use format (.+)$"#) {
        return "\(localizedErrorFieldName(groups[1]))格式必须为\(groups[2])"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) must be between (\d+) and (\d+)$"#) {
        return "\(localizedErrorFieldName(groups[1]))必须在\(groups[2])到\(groups[3])之间"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^unsupported (.+?):\s*(.+)$"#) {
        return "不支持的\(localizedErrorFieldName(groups[1]))：\(groups[2])"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) not found(?:[:].*| for .*)?$"#) {
        return "未找到\(localizedErrorEntityName(groups[1]))"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) already exists(?:[:].*)?$"#) {
        return "\(localizedErrorEntityName(groups[1]))已存在"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) and (.+?) must be different$"#) {
        return "\(localizedErrorFieldName(groups[1]))和\(localizedErrorFieldName(groups[2]))不能相同"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) currency must equal (.+)$"#) {
        let left = localizedErrorFieldName(groups[1])
        let right = localizedErrorFieldName(groups[2].replacingOccurrences(of: " currency", with: "", options: [.caseInsensitive]))
        return "\(left)币种必须与\(right)一致"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) must not be (.+)$"#) {
        return "\(localizedErrorFieldName(groups[1]))不能为\(localizedErrorValueName(groups[2]))"
    }
    if let groups = matchedGroups(in: raw, pattern: #"^(.+?) must be at least (\d+) characters$"#) {
        return "\(localizedErrorFieldName(groups[1]))至少需要\(groups[2])个字符"
    }
    return nil
}

private func matchedGroups(in value: String, pattern: String) -> [String]? {
    guard let regex = try? NSRegularExpression(pattern: pattern, options: [.caseInsensitive]) else {
        return nil
    }
    let range = NSRange(value.startIndex..<value.endIndex, in: value)
    guard let match = regex.firstMatch(in: value, options: [], range: range) else {
        return nil
    }
    return (0..<match.numberOfRanges).compactMap { index in
        let nsRange = match.range(at: index)
        guard nsRange.location != NSNotFound, let range = Range(nsRange, in: value) else {
            return nil
        }
        return String(value[range])
    }
}

private func localizedErrorFieldName(_ raw: String) -> String {
    let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines)
    guard !trimmed.isEmpty else {
        return raw
    }
    let fieldLabels: [String: String] = [
        "amount": "金额",
        "appCode": "应用编码",
        "attachmentUrls": "附件",
        "avatarUrl": "头像地址",
        "branchId": "分支事务号",
        "businessDomainCode": "业务域",
        "bizOrderNo": "业务单号",
        "businessSceneCode": "业务场景",
        "commentId": "评论标识",
        "confirmedShare": "确认份额",
        "content": "内容",
        "conversationId": "会话标识",
        "creditDebitAmount": "信用扣款金额",
        "currencyCode": "币种",
        "currencyUnit": "币种",
        "deviceId": "设备标识",
        "feeAmount": "手续费",
        "feeBearer": "费用承担方",
        "feeMode": "计费模式",
        "feeRate": "费率",
        "file": "文件",
        "fixedFee": "固定手续费",
        "fundCode": "基金代码",
        "fundDebitAmount": "爱存扣款金额",
        "imageMediaId": "图片媒体标识",
        "limit": "条数",
        "loginId": "登录账号",
        "maxFee": "最高手续费",
        "messageId": "消息标识",
        "minFee": "最低手续费",
        "nav": "净值",
        "nickname": "昵称",
        "operationType": "操作类型",
        "originalAmount": "原始金额",
        "originalTradeNo": "原交易单号",
        "payableAmount": "应付金额",
        "payeeUserId": "收款方用户标识",
        "parentCommentId": "父评论标识",
        "payerUserId": "付款方用户标识",
        "paymentId": "支付单号",
        "paymentMethod": "支付方式",
        "pricingQuoteNo": "报价单号",
        "quoteNo": "报价单号",
        "requestNo": "请求号",
        "role": "角色",
        "settleAmount": "结算金额",
        "share": "份额",
        "sourceBizNo": "来源业务单号",
        "sourceBizType": "来源业务类型",
        "status": "状态",
        "submission": "提交参数",
        "targetStatus": "目标状态",
        "templateCode": "模板编码",
        "tradeNo": "交易单号",
        "userId": "用户标识",
        "videoId": "视频标识",
        "walletDebitAmount": "余额扣款金额",
        "xid": "全局事务号"
    ]
    if let label = fieldLabels[trimmed] {
        return label
    }
    let entityLabels: [String: String] = [
        "wallet account": "余额账户",
        "fund account": "爱存账户",
        "red packet": "红包",
        "trade": "交易",
        "original trade": "原交易",
        "product": "产品",
        "coupon": "券",
        "coupon template": "券模板",
        "feedback ticket": "反馈单",
        "video": "视频"
    ]
    return entityLabels[trimmed.lowercased()] ?? trimmed
}

private func localizedErrorEntityName(_ raw: String) -> String {
    let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
    let labels: [String: String] = [
        "wallet account": "余额账户",
        "fund account": "爱存账户",
        "red packet": "红包",
        "trade": "交易",
        "original trade": "原交易",
        "product": "产品",
        "coupon": "券",
        "coupon template": "券模板",
        "feedback ticket": "反馈单",
        "message": "消息"
    ]
    return labels[trimmed] ?? localizedErrorFieldName(raw)
}

private func localizedErrorValueName(_ raw: String) -> String {
    switch raw.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() {
    case "submitted":
        return "已提交"
    case "debit":
        return "借记"
    case "credit":
        return "贷记"
    case "payee":
        return "收款方"
    case "payer":
        return "付款方"
    default:
        return raw.trimmingCharacters(in: .whitespacesAndNewlines)
    }
}

func isCancellationLikeError(_ error: Error) -> Bool {
    if error is CancellationError {
        return true
    }
    let nsError = error as NSError
    if nsError.domain == NSURLErrorDomain && nsError.code == NSURLErrorCancelled {
        return true
    }
    let localized = error.localizedDescription.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
    return localized == "cancelled" || localized == "canceled"
}

func userFacingErrorMessage(_ error: Error) -> String {
    if let apiError = error as? APIClientError {
        switch apiError {
        case .businessError(let message):
            return localizedServerErrorMessage(message)
        case .decodeFailed:
            return "数据解析失败"
        case .invalidURL, .invalidResponse:
            return apiError.errorDescription ?? "操作失败，请稍后重试"
        }
    }
    if error is DecodingError {
        return "数据解析失败"
    }
    let localized = error.localizedDescription.trimmingCharacters(in: .whitespacesAndNewlines)
    if localized.isEmpty {
        return "操作失败，请稍后重试"
    }
    return localizedServerErrorMessage(localized)
}

enum APIClientError: LocalizedError {
    case invalidURL
    case invalidResponse
    case decodeFailed
    case businessError(message: String)

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "服务地址无效"
        case .invalidResponse:
            return "服务响应异常"
        case .decodeFailed:
            return "数据解析失败"
        case .businessError(let message):
            return localizedServerErrorMessage(message)
        }
    }
}

private final class NetworkContextProvider {
    static let shared = NetworkContextProvider()

    private let monitor: NWPathMonitor
    private let queue = DispatchQueue(label: "openaipay.network.context.monitor")
    private let lock = NSLock()
    private var latestNetworkType: String?

    private init() {
        monitor = NWPathMonitor()
        monitor.pathUpdateHandler = { [weak self] path in
            self?.update(path)
        }
        monitor.start(queue: queue)
    }

    deinit {
        monitor.cancel()
    }

    var networkType: String? {
        lock.lock()
        defer { lock.unlock() }
        return latestNetworkType
    }

    private func update(_ path: NWPath) {
        let resolvedType: String
        if path.status != .satisfied {
            resolvedType = "OFFLINE"
        } else if path.usesInterfaceType(.wifi) {
            resolvedType = "WIFI"
        } else if path.usesInterfaceType(.cellular) {
            resolvedType = "CELLULAR"
        } else if path.usesInterfaceType(.wiredEthernet) {
            resolvedType = "ETHERNET"
        } else {
            resolvedType = "UNKNOWN"
        }
        lock.lock()
        latestNetworkType = resolvedType
        lock.unlock()
    }
}


final class APIClient {
    private struct BffRequestResult {
        let data: Data
        let httpResponse: HTTPURLResponse
    }

    static let shared = APIClient()

    private static let defaultRequestTimeout: TimeInterval = 10
    private let baseURL = AppRuntime.bffBaseURL
    private let requestTimeout: TimeInterval = APIClient.defaultRequestTimeout
    private let session: URLSession
    private let authStore = AuthStore()

    private init() {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = APIClient.defaultRequestTimeout
        configuration.timeoutIntervalForResource = APIClient.defaultRequestTimeout
        configuration.waitsForConnectivity = false
        session = URLSession(configuration: configuration)
    }

    private func scheduleVisitTracking(apiName: String,
                                       method: String,
                                       body: Data?,
                                       httpResponse: HTTPURLResponse?,
                                       error: Error?,
                                       startedAt: Date) {
        let normalizedApiName = normalizedTrackedApiName(apiName)
        guard shouldTrackVisit(for: normalizedApiName) else {
            return
        }

        let durationMs = max(Int64(Date().timeIntervalSince(startedAt) * 1000), 0)
        let requestParamsText = summarizedRequestParams(apiName: normalizedApiName, method: method, body: body)
        let resultSummary = summarizedResult(method: method, httpResponse: httpResponse, error: error)
        let statusCode = httpResponse?.statusCode

        Task.detached(priority: .background) {
            do {
                let payloadJson = APIClient.makeBehaviorPayloadJson([
                    "requestParamsText": requestParamsText,
                    "httpStatus": statusCode,
                    "method": method.uppercased(),
                    "api": normalizedApiName
                ])
                _ = try await APIClient.shared.recordAppBehaviorEvent(
                    eventName: normalizedApiName,
                    eventType: "API_CALL",
                    eventCode: normalizedApiName,
                    pageName: nil,
                    actionName: method.uppercased(),
                    resultSummary: resultSummary,
                    durationMs: durationMs,
                    payloadJson: payloadJson
                )
            } catch {
            }
        }
    }

    private func shouldTrackVisit(for apiName: String) -> Bool {
        let path = apiName.split(separator: "?", maxSplits: 1, omittingEmptySubsequences: false).first.map(String.init) ?? apiName
        if path == "/bff/apps/visit-records" || path == "/bff/apps/devices" || path == "/bff/apps/behavior-events" {
            return false
        }
        if path.hasPrefix("/bff/apps/") && path.hasSuffix("/versions/check") {
            return false
        }
        return true
    }

    private func normalizedTrackedApiName(_ apiName: String) -> String {
        let trimmed = apiName.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.isEmpty ? "/unknown" : trimmed
    }

    private func normalizedTrackedApiName(url: URL) -> String {
        let path = url.path.isEmpty ? "/unknown" : url.path
        guard let query = url.query, !query.isEmpty else {
            return path
        }
        return "\(path)?\(query)"
    }

    private func makeBffURL(path: String) -> URL? {
        let normalizedBaseURL = normalizedGatewayBaseURL(baseURL)
        guard !normalizedBaseURL.isEmpty else {
            return nil
        }
        var normalizedPath = path.hasPrefix("/") ? path : "/\(path)"
        normalizedPath = normalizedGatewayPath(normalizedPath)
        return URL(string: "\(normalizedBaseURL)\(normalizedPath)")
    }

    private func normalizedGatewayBaseURL(_ rawBaseURL: String) -> String {
        var normalized = rawBaseURL.trimmingCharacters(in: .whitespacesAndNewlines)
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

    private func normalizedGatewayPath(_ rawPath: String) -> String {
        let normalizedSlashes = rawPath.replacingOccurrences(of: "/{2,}", with: "/", options: .regularExpression)
        let collapsedBffPrefix = normalizedSlashes.replacingOccurrences(
            of: "^/bff(?:/bff)+(?:/|$)",
            with: "/bff/",
            options: .regularExpression
        )
        let collapsedApiPrefix = collapsedBffPrefix.replacingOccurrences(
            of: "^/api(?:/api)+(?:/|$)",
            with: "/api/",
            options: .regularExpression
        )
        if collapsedApiPrefix.count > 1, collapsedApiPrefix.hasSuffix("/") {
            return String(collapsedApiPrefix.dropLast())
        }
        return collapsedApiPrefix
    }

    private func summarizedRequestParams(apiName: String, method: String, body: Data?) -> String? {
        let uppercasedMethod = method.uppercased()
        if uppercasedMethod == "GET", let queryStartIndex = apiName.firstIndex(of: "?") {
            let query = String(apiName[apiName.index(after: queryStartIndex)...])
            return truncatedText(query, maxLength: 500)
        }
        if let body, !body.isEmpty {
            return "bodyBytes=\(body.count)"
        }
        return nil
    }

    private func summarizedResult(method: String, httpResponse: HTTPURLResponse?, error: Error?) -> String {
        let uppercasedMethod = method.uppercased()
        if let httpResponse {
            return "\(uppercasedMethod) \(httpResponse.statusCode)"
        }
        if let error {
            if isCancellationLikeError(error) {
                return "\(uppercasedMethod) CANCELLED"
            }
            if let urlError = error as? URLError {
                return "\(uppercasedMethod) NETWORK_\(urlError.code.rawValue)"
            }
            return "\(uppercasedMethod) FAILED"
        }
        return "\(uppercasedMethod) UNKNOWN"
    }

    private func truncatedText(_ text: String, maxLength: Int) -> String {
        if text.count <= maxLength {
            return text
        }
        return String(text.prefix(maxLength))
    }

    static func makeBehaviorPayloadJson(_ rawPayload: [String: Any?]) -> String? {
        var payload: [String: Any] = [:]
        rawPayload.forEach { key, value in
            guard let value else {
                return
            }
            if let text = value as? String {
                let normalized = text.trimmingCharacters(in: .whitespacesAndNewlines)
                if !normalized.isEmpty {
                    payload[key] = normalized
                }
                return
            }
            payload[key] = value
        }
        guard !payload.isEmpty,
              JSONSerialization.isValidJSONObject(payload),
              let data = try? JSONSerialization.data(withJSONObject: payload),
              let encoded = String(data: data, encoding: .utf8) else {
            return nil
        }
        return encoded
    }

    private func assignOptionalPayloadText(_ payload: inout [String: Any], key: String, value: String?) {
        let normalized = value?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard !normalized.isEmpty else {
            return
        }
        payload[key] = normalized
    }

    private func currentViewportSize() async -> (width: Int, height: Int) {
        await MainActor.run {
            let boundsFromWindowScene = UIApplication.shared.connectedScenes
                .compactMap { $0 as? UIWindowScene }
                .flatMap { $0.windows }
                .first(where: { $0.isKeyWindow })?
                .bounds
            let bounds = boundsFromWindowScene ?? UIScreen.main.bounds
            return (
                width: max(Int(bounds.width.rounded()), 0),
                height: max(Int(bounds.height.rounded()), 0)
            )
        }
    }

    private func generateAiPayNumericId(domainCode: String, bizType: String, userId: Int64?) -> String {
        let now = Date()
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = TimeZone.current
        formatter.dateFormat = "yyyyMMddHHmmss"
        let timestamp = formatter.string(from: now)
        let millis = Int((now.timeIntervalSince1970 * 1000).truncatingRemainder(dividingBy: 1000))
        let userGene: String = {
            guard let userId, userId > 0 else {
                return "0000"
            }
            let digits = String(userId)
            if digits.count >= 4 {
                return String(digits.suffix(4))
            }
            return String(repeating: "0", count: 4 - digits.count) + digits
        }()
        let sequence = Int.random(in: 0...99_999)
        return String(format: "%@%@%@00%@%03d%05d", domainCode, bizType, timestamp, userGene, millis, sequence)
    }

    private func normalizedFundNav(_ nav: Decimal) -> Decimal {
        if nav > .zero {
            return nav
        }
        return Decimal(string: "1.0000", locale: Locale(identifier: "en_US_POSIX")) ?? Decimal(1)
    }

    private func normalizedFundShare(amount: Decimal, nav: Decimal) -> Decimal {
        let rounding = NSDecimalNumberHandler(
            roundingMode: .plain,
            scale: 4,
            raiseOnExactness: false,
            raiseOnOverflow: false,
            raiseOnUnderflow: false,
            raiseOnDivideByZero: false
        )
        let share = NSDecimalNumber(decimal: amount)
            .dividing(by: NSDecimalNumber(decimal: normalizedFundNav(nav)), withBehavior: rounding)
            .decimalValue
        let minimumShare = Decimal(string: "0.0001", locale: Locale(identifier: "en_US_POSIX")) ?? .zero
        return max(share, minimumShare)
    }

    private func isFundAccountAlreadyExistsError(_ error: Error) -> Bool {
        let containsAlreadyExists: (String) -> Bool = { message in
            message.localizedCaseInsensitiveContains("fund account already exists")
                || message.localizedCaseInsensitiveContains("爱存账户已存在")
        }
        if let apiError = error as? APIClientError,
           case let .businessError(message) = apiError {
            return containsAlreadyExists(message)
        }
        return containsAlreadyExists(error.localizedDescription)
    }

    private func isFundAccountNotFoundError(_ error: Error) -> Bool {
        let containsNotFound: (String) -> Bool = { message in
            message.localizedCaseInsensitiveContains("fund account not found")
                || message.localizedCaseInsensitiveContains("未找到爱存账户")
        }
        if let apiError = error as? APIClientError,
           case let .businessError(message) = apiError {
            return containsNotFound(message)
        }
        return containsNotFound(error.localizedDescription)
    }

    private func normalizedMainlandPhoneLoginId(_ raw: String) -> String {
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

    private struct RegisterPhoneCheckPayload: Decodable {
        let userExists: Bool?
        let realNameVerified: Bool?
        let kycLevel: String?
    }

    private struct RegistrationPayload: Decodable {
        let userId: Int64?
        let aipayUid: String?
        let loginId: String?
        let kycSubmitted: Bool?
    }

    func checkRegisterPhoneStatus(loginId: String) async throws -> (userExists: Bool, realNameVerified: Bool, kycLevel: String?) {
        let normalizedLoginId = normalizedMainlandPhoneLoginId(loginId)
        guard !normalizedLoginId.isEmpty else {
            throw APIClientError.businessError(message: "loginId不能为空")
        }
        guard normalizedLoginId.range(of: "^1[3-9][0-9]{9}$", options: .regularExpression) != nil else {
            throw APIClientError.businessError(message: "loginId参数格式不正确")
        }

        let path = "/register/check?loginId=\(normalizedLoginId)"
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/user-flows\(path)",
            method: "GET",
            body: nil,
            bffTimeoutOverride: 6
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<RegisterPhoneCheckPayload>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return (
                    userExists: payload.userExists ?? false,
                    realNameVerified: payload.realNameVerified ?? false,
                    kycLevel: payload.kycLevel?.trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)
                )
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "注册手机号校验失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<RegisterPhoneCheckPayload>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "注册手机号校验失败")
        }
        throw APIClientError.decodeFailed
    }

    func createRegisteredUser(
        loginId: String,
        nickname: String,
        mobile: String,
        loginPassword: String,
        realName: String? = nil,
        idCardNo: String? = nil
    ) async throws -> Int64 {
        let normalizedLoginId = normalizedMainlandPhoneLoginId(loginId)
        guard normalizedLoginId.range(of: "^1[3-9][0-9]{9}$", options: .regularExpression) != nil else {
            throw APIClientError.businessError(message: "loginId参数格式不正确")
        }

        let normalizedMobile = normalizedMainlandPhoneLoginId(mobile)
        let resolvedMobile = normalizedMobile.isEmpty ? normalizedLoginId : normalizedMobile
        let normalizedNickname = nickname.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            ? "新用户"
            : nickname.trimmingCharacters(in: .whitespacesAndNewlines)
        let normalizedRealName = realName?
            .trimmingCharacters(in: .whitespacesAndNewlines)
        let normalizedIdCardNo = idCardNo?
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .uppercased()
        let normalizedLoginPassword = loginPassword.filter(\.isNumber)
        guard normalizedLoginPassword.range(of: "^[0-9]{6}$", options: .regularExpression) != nil else {
            throw APIClientError.businessError(message: "登录密码需为6位数字")
        }

        var payload: [String: Any] = [
            "deviceId": AppRuntime.deviceId,
            "legacyDeviceIds": AppRuntime.legacyDeviceIds,
            "loginId": normalizedLoginId,
            "userTypeCode": "01",
            "nickname": normalizedNickname,
            "countryCode": "86",
            "mobile": resolvedMobile
        ]
        if let normalizedRealName, !normalizedRealName.isEmpty {
            payload["realName"] = normalizedRealName
        }
        if let normalizedIdCardNo, !normalizedIdCardNo.isEmpty {
            payload["idCardNo"] = normalizedIdCardNo
        }
        payload["loginPassword"] = normalizedLoginPassword

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/user-flows/registrations",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            if let envelope = try? decoder.decode(BffEnvelope<RegistrationPayload>.self, from: data),
               envelope.success,
               let payload = envelope.data,
               let userId = payload.userId {
                return userId
            }
            if let envelope = try? decoder.decode(BffEnvelope<Int64>.self, from: data),
               envelope.success,
               let payload = envelope.data {
                return payload
            }
            if let envelope = try? decoder.decode(BffEnvelope<String>.self, from: data),
               envelope.success,
               let payload = envelope.data,
               let userId = Int64(payload.trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)) {
                return userId
            }
            if let envelope = try? decoder.decode(BffEnvelope<Double>.self, from: data),
               envelope.success,
               let payload = envelope.data {
                return Int64(payload)
            }
            if let envelopeObject = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
               (envelopeObject["success"] as? Bool) == true,
               let dataObject = envelopeObject["data"] {
                if let dataDict = dataObject as? [String: Any] {
                    if let userIdInt64 = dataDict["userId"] as? Int64 {
                        return userIdInt64
                    }
                    if let userIdInt = dataDict["userId"] as? Int {
                        return Int64(userIdInt)
                    }
                    if let userIdDouble = dataDict["userId"] as? Double {
                        return Int64(userIdDouble)
                    }
                    if let userIdString = dataDict["userId"] as? String,
                       let userId = Int64(userIdString.trimmingCharacters(in: .whitespacesAndNewlines)) {
                        return userId
                    }
                } else if let dataString = dataObject as? String,
                          let userId = Int64(dataString.trimmingCharacters(in: .whitespacesAndNewlines)) {
                    return userId
                } else if let dataInt64 = dataObject as? Int64 {
                    return dataInt64
                } else if let dataInt = dataObject as? Int {
                    return Int64(dataInt)
                } else if let dataDouble = dataObject as? Double {
                    return Int64(dataDouble)
                }
            }
            if let envelope = try? decoder.decode(BffEnvelope<String>.self, from: data) {
                throw APIClientError.businessError(message: envelope.error?.message ?? "注册账号失败")
            }
            if let envelope = try? decoder.decode(BffEnvelope<Int64>.self, from: data) {
                throw APIClientError.businessError(message: envelope.error?.message ?? "注册账号失败")
            }
            throw APIClientError.decodeFailed
        }

        if let envelope = try? decoder.decode(BffEnvelope<String>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "注册账号失败")
        }
        if let envelope = try? decoder.decode(BffEnvelope<Int64>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "注册账号失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchUserProfile(loginId: String) async throws -> UserProfile {
        let normalizedLoginId = normalizedMainlandPhoneLoginId(loginId)
        guard normalizedLoginId.range(of: "^1[3-9][0-9]{9}$", options: .regularExpression) != nil else {
            throw APIClientError.businessError(message: "loginId参数格式不正确")
        }

        let encodedLoginId = normalizedLoginId.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)
            ?? normalizedLoginId
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/users/profile-by-login?loginId=\(encodedLoginId)",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<UserProfile>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询用户资料失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<UserProfile>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询用户资料失败")
        }
        throw APIClientError.decodeFailed
    }

    func mobileVerifyLogin(loginId: String, deviceId: String) async throws -> LoginResponseData {
        guard let url = makeBffURL(path: "/bff/auth/mobile-verify-login") else {
            throw APIClientError.invalidURL
        }

        let requestBody = try JSONSerialization.data(withJSONObject: [
            "loginId": loginId,
            "deviceId": deviceId
        ])
        let (data, httpResponse) = try await executeRequest(url: url, method: "POST", body: requestBody)

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<LoginResponseData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return try validatedAuthSession(payload, failureMessage: "本机号码验证登录失败，请重试")
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "本机号码验证登录失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<LoginResponseData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "本机号码验证登录失败")
        }
        throw APIClientError.decodeFailed
    }

    func demoAutoLogin(deviceId: String, preferredLoginId: String? = nil) async throws -> LoginResponseData {
        guard let url = makeBffURL(path: "/bff/auth/demo-auto-login") else {
            throw APIClientError.invalidURL
        }

        var payload: [String: Any] = [
            "deviceId": deviceId
        ]
        if let preferredLoginId {
            let normalizedPreferredLoginId = preferredLoginId.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalizedPreferredLoginId.isEmpty {
                payload["preferredLoginId"] = normalizedPreferredLoginId
            }
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await executeRequest(
            url: url,
            method: "POST",
            body: requestBody,
            timeoutInterval: 60
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<LoginResponseData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return try validatedAuthSession(payload, failureMessage: "演示账号自动登录失败，请重试")
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "演示账号自动登录失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<LoginResponseData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "演示账号自动登录失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchPresetLoginAccounts(deviceId: String) async throws -> [LoginPresetAccountData] {
        let normalizedDeviceId = deviceId.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedDeviceId.isEmpty else {
            throw APIClientError.businessError(message: "deviceId 不能为空")
        }
        guard let encodedDeviceId = normalizedDeviceId.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            throw APIClientError.invalidURL
        }
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/auth/preset-login-accounts?deviceId=\(encodedDeviceId)",
            method: "GET",
            body: nil,
            skipVisitTracking: true
        )
        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<[LoginPresetAccountData]>.self, from: data)
            if envelope.success {
                return (envelope.data ?? []).filter { !$0.loginId.isEmpty }
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询登录下拉账号失败")
        }
        if let envelope = try? decoder.decode(BffEnvelope<[LoginPresetAccountData]>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询登录下拉账号失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchUserProfile(userId: Int64) async throws -> UserProfile {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }

        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/users/\(userId)/profile",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<UserProfile>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询用户资料失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<UserProfile>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询用户资料失败")
        }
        throw APIClientError.decodeFailed
    }

    func updateUserAvatar(userId: Int64, avatarUrl: String) async throws {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }

        let normalizedAvatarUrl = avatarUrl.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedAvatarUrl.isEmpty else {
            throw APIClientError.businessError(message: "头像地址不能为空")
        }

        let requestBody = try JSONSerialization.data(withJSONObject: [
            "avatarUrl": normalizedAvatarUrl
        ])
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/users/\(userId)/profile",
            method: "PUT",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<EmptyPayload>.self, from: data)
            if envelope.success {
                return
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "更新头像失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<EmptyPayload>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "更新头像失败")
        }
        throw APIClientError.decodeFailed
    }

    func updateUserNickname(userId: Int64, nickname: String) async throws {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }

        let normalizedNickname = nickname.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedNickname.isEmpty else {
            throw APIClientError.businessError(message: "昵称不能为空")
        }

        let requestBody = try JSONSerialization.data(withJSONObject: [
            "nickname": normalizedNickname
        ])
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/users/\(userId)/profile",
            method: "PUT",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<EmptyPayload>.self, from: data)
            if envelope.success {
                return
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "更新昵称失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<EmptyPayload>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "更新昵称失败")
        }
        throw APIClientError.decodeFailed
    }

    func updateUserProfile(
        userId: Int64,
        nickname: String? = nil,
        avatarUrl: String? = nil,
        mobile: String? = nil,
        gender: String? = nil,
        region: String? = nil,
        birthday: String? = nil
    ) async throws {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }

        var payload: [String: Any] = [:]
        let stringFields: [(String, String?)] = [
            ("nickname", nickname),
            ("avatarUrl", avatarUrl),
            ("mobile", mobile),
            ("gender", gender),
            ("region", region),
            ("birthday", birthday),
        ]
        for (key, rawValue) in stringFields {
            let trimmed = rawValue?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            if !trimmed.isEmpty {
                payload[key] = trimmed
            }
        }

        guard !payload.isEmpty else {
            return
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/users/\(userId)/profile",
            method: "PUT",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<EmptyPayload>.self, from: data)
            if envelope.success {
                return
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "更新用户资料失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<EmptyPayload>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "更新用户资料失败")
        }
        throw APIClientError.decodeFailed
    }

    func submitUserKyc(userId: Int64, realName: String, idCardNo: String) async throws {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedRealName = realName.trimmingCharacters(in: .whitespacesAndNewlines)
        let normalizedIdCardNo = idCardNo.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        guard !normalizedRealName.isEmpty, !normalizedIdCardNo.isEmpty else {
            throw APIClientError.businessError(message: "实名信息不能为空")
        }

        let requestBody = try JSONSerialization.data(withJSONObject: [
            "realName": normalizedRealName,
            "idCardNo": normalizedIdCardNo,
        ])
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/kyc/users/\(userId)/submissions",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<EmptyPayload>.self, from: data)
            if envelope.success {
                return
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "提交实名认证失败")
        }
        if let envelope = try? decoder.decode(BffEnvelope<EmptyPayload>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "提交实名认证失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchAssetOverview(userId: String, timeoutInterval: TimeInterval? = nil) async throws -> AssetOverviewData {
        let trimmedUserId = userId.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedUserId.isEmpty else {
            throw APIClientError.businessError(message: "userId 不能为空")
        }
        guard let encodedUserId = trimmedUserId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) else {
            throw APIClientError.invalidURL
        }
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/assets/\(encodedUserId)/overview",
            method: "GET",
            body: nil,
            bffTimeoutOverride: timeoutInterval
        )
        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<AssetOverviewData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询余额失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<AssetOverviewData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询余额失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchAssetChanges(
        userId: String,
        limit: Int = 3,
        timeoutInterval: TimeInterval? = nil
    ) async throws -> [AssetChangeItemData] {
        let trimmedUserId = userId.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedUserId.isEmpty else {
            throw APIClientError.businessError(message: "userId 不能为空")
        }
        let normalizedLimit = min(max(limit, 1), 100)
        guard let encodedUserId = trimmedUserId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) else {
            throw APIClientError.invalidURL
        }
        let result = try await requestBffDetailed(
            bffPath: "/bff/assets/\(encodedUserId)/changes?limit=\(normalizedLimit)",
            method: "GET",
            body: nil,
            bffTimeoutOverride: timeoutInterval
        )
        let data = result.data
        let httpResponse = result.httpResponse

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<AssetChangesData>.self, from: data)
            if envelope.success {
                return envelope.data?.items ?? []
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询余额明细失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<AssetChangesData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询余额明细失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchAssetBillEntries(
        userId: String,
        limit: Int = 20,
        pageNo: Int = 1,
        pageSize: Int? = nil,
        billMonth: String? = nil,
        businessDomainCode: String? = nil,
        cursor: String? = nil
    ) async throws -> AssetBillEntriesData {
        let trimmedUserId = userId.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedUserId.isEmpty else {
            throw APIClientError.businessError(message: "userId 不能为空")
        }
        let normalizedPageNo = max(pageNo, 1)
        let normalizedPageSize = min(max(pageSize ?? limit, 1), 100)
        guard let encodedUserId = trimmedUserId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) else {
            throw APIClientError.invalidURL
        }

        var queryItems = ["pageNo=\(normalizedPageNo)", "pageSize=\(normalizedPageSize)"]
        let normalizedBillMonth = billMonth?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if !normalizedBillMonth.isEmpty,
           let encoded = normalizedBillMonth.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            queryItems.append("billMonth=\(encoded)")
        }
        let normalizedBusinessDomainCode = businessDomainCode?.trimmingCharacters(in: .whitespacesAndNewlines).uppercased() ?? ""
        if !normalizedBusinessDomainCode.isEmpty,
           let encoded = normalizedBusinessDomainCode.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            queryItems.append("businessDomainCode=\(encoded)")
        }
        let normalizedCursor = cursor?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if !normalizedCursor.isEmpty,
           let encoded = normalizedCursor.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
            queryItems.append("cursor=\(encoded)")
        }
        let query = queryItems.joined(separator: "&")

        let result = try await requestBffDetailed(
            bffPath: "/bff/bills/users/\(encodedUserId)/entries?\(query)",
            method: "GET",
            body: nil
        )
        let data = result.data
        let httpResponse = result.httpResponse

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<AssetBillEntriesData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询账单失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<AssetBillEntriesData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询账单失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchCreditCurrentBillDetail(userId: Int64) async throws -> CreditCurrentBillDetailData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }

        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/accounts/credit/users/\(userId)/current-bill-detail",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<CreditCurrentBillDetailData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询爱花账单明细失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<CreditCurrentBillDetailData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询爱花账单明细失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchCreditNextBillDetail(userId: Int64) async throws -> CreditCurrentBillDetailData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }

        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/accounts/credit/users/\(userId)/next-bill-detail",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<CreditCurrentBillDetailData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询下期爱花账单明细失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<CreditCurrentBillDetailData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询下期爱花账单明细失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchCreditAccount(userId: Int64) async throws -> CreditAccountData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }

        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/accounts/credit/users/\(userId)",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<CreditAccountData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询爱花账户失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<CreditAccountData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询爱花账户失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchLoanAccount(userId: Int64) async throws -> LoanAccountData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }

        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/accounts/loan/users/\(userId)",
            method: "GET",
            body: nil
        )

        if httpResponse.statusCode == 404 {
            return LoanAccountData.demo(userId: userId)
        }

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<LoanAccountData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询爱借账户失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<LoanAccountData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询爱借账户失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchFundAccount(userId: Int64) async throws -> FundAccountData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }

        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/accounts/fund/users/\(userId)",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<FundAccountData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询爱存失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<FundAccountData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询爱存失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchFundOpenAgreementPack(
        userId: Int64,
        fundCode: String = "AICASH",
        currencyCode: String = "CNY"
    ) async throws -> FundOpenAgreementPackData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedFundCode = fundCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        let normalizedCurrencyCode = currencyCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        guard let encodedFundCode = normalizedFundCode.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let encodedCurrencyCode = normalizedCurrencyCode.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            throw APIClientError.invalidURL
        }

        let bffPath = "/bff/accounts/fund/open/agreement-pack/\(userId)?fundCode=\(encodedFundCode)&currencyCode=\(encodedCurrencyCode)"
        let (data, httpResponse) = try await requestBff(
            bffPath: bffPath,
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<FundOpenAgreementPackData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询开通协议失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<FundOpenAgreementPackData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询开通协议失败")
        }
        throw APIClientError.decodeFailed
    }

    func openFundAccountWithAgreement(
        userId: Int64,
        fundCode: String = "AICASH",
        currencyCode: String = "CNY",
        agreementAccepts: [FundOpenAgreementTemplateData],
        idempotencyKey: String? = nil
    ) async throws -> FundOpenAccountWithAgreementResultData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedFundCode = fundCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        let normalizedCurrencyCode = currencyCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        let accepts = agreementAccepts.filter { !$0.templateCode.isEmpty && !$0.templateVersion.isEmpty }
        guard !accepts.isEmpty else {
            throw APIClientError.businessError(message: "缺少签约协议")
        }
        let normalizedIdempotencyKey: String = {
            let trimmed = idempotencyKey?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            if !trimmed.isEmpty {
                return trimmed
            }
            return "FUND_OPEN_\(userId)_\(UUID().uuidString.replacingOccurrences(of: "-", with: ""))"
        }()

        var payload: [String: Any] = [
            "userId": String(userId),
            "fundCode": normalizedFundCode,
            "currencyCode": normalizedCurrencyCode,
            "idempotencyKey": normalizedIdempotencyKey,
            "agreementAccepts": accepts.map { template in
                [
                    "templateCode": template.templateCode,
                    "templateVersion": template.templateVersion
                ]
            }
        ]

        if normalizedFundCode.isEmpty {
            payload["fundCode"] = "AICASH"
        }
        if normalizedCurrencyCode.isEmpty {
            payload["currencyCode"] = "CNY"
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/accounts/fund/open",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<FundOpenAccountWithAgreementResultData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "签约开通爱存失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<FundOpenAccountWithAgreementResultData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "签约开通爱存失败")
        }
        throw APIClientError.decodeFailed
    }

    func createFundAccount(
        userId: Int64,
        fundCode: String = "AICASH",
        currencyCode: String = "CNY"
    ) async throws {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedFundCode = fundCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        guard !normalizedFundCode.isEmpty else {
            throw APIClientError.businessError(message: "基金代码无效")
        }
        let normalizedCurrencyCode = currencyCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        let agreementPack = try await fetchFundOpenAgreementPack(
            userId: userId,
            fundCode: normalizedFundCode,
            currencyCode: normalizedCurrencyCode
        )
        let requiredTemplates = agreementPack.agreements.filter(\.required)
        let finalTemplates = requiredTemplates.isEmpty ? agreementPack.agreements : requiredTemplates
        guard !finalTemplates.isEmpty else {
            throw APIClientError.businessError(message: "未找到可签约协议")
        }
        _ = try await openFundAccountWithAgreement(
            userId: userId,
            fundCode: normalizedFundCode,
            currencyCode: normalizedCurrencyCode,
            agreementAccepts: finalTemplates,
            idempotencyKey: "FUND_OPEN_\(userId)_\(normalizedFundCode)"
        )
    }

    func fetchCreditOpenAgreementPack(
        userId: Int64,
        productCode: String = "AICREDIT"
    ) async throws -> CreditProductOpenAgreementPackData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedProductCode: String = {
            let trimmed = productCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
            switch trimmed {
            case "", "AICREDIT", "AICREDIT":
                return "AICREDIT"
            case "AILOAN", "AILOAN":
                return "AILOAN"
            default:
                return trimmed
            }
        }()
        switch normalizedProductCode {
        case "AICREDIT", "AILOAN":
            break
        default:
            throw APIClientError.businessError(message: "不支持的信用产品类型")
        }

        let bffPath = "/bff/accounts/credit/open/agreement-pack/\(userId)?productCode=\(normalizedProductCode)"
        let (data, httpResponse) = try await requestBff(
            bffPath: bffPath,
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<CreditProductOpenAgreementPackData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询信用产品开通协议失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<CreditProductOpenAgreementPackData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询信用产品开通协议失败")
        }
        throw APIClientError.decodeFailed
    }

    func openCreditProductWithAgreement(
        userId: Int64,
        productCode: String = "AICREDIT",
        agreementAccepts: [CreditOpenAgreementTemplateData],
        idempotencyKey: String? = nil
    ) async throws -> OpenCreditProductWithAgreementResultData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedProductCode: String = {
            let trimmed = productCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
            switch trimmed {
            case "", "AICREDIT", "AICREDIT":
                return "AICREDIT"
            case "AILOAN", "AILOAN":
                return "AILOAN"
            default:
                return trimmed
            }
        }()
        switch normalizedProductCode {
        case "AICREDIT", "AILOAN":
            break
        default:
            throw APIClientError.businessError(message: "不支持的信用产品类型")
        }

        let accepts = agreementAccepts.filter { !$0.templateCode.isEmpty && !$0.templateVersion.isEmpty }
        guard !accepts.isEmpty else {
            throw APIClientError.businessError(message: "缺少签约协议")
        }
        let normalizedIdempotencyKey: String = {
            let trimmed = idempotencyKey?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            if !trimmed.isEmpty {
                return trimmed
            }
            return "AUTO_REG_\(normalizedProductCode)_\(userId)_IOS"
        }()

        let payload: [String: Any] = [
            "userId": String(userId),
            "productCode": normalizedProductCode,
            "idempotencyKey": normalizedIdempotencyKey,
            "agreementAccepts": accepts.map { template in
                [
                    "templateCode": template.templateCode,
                    "templateVersion": template.templateVersion
                ]
            }
        ]

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/accounts/credit/open",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<OpenCreditProductWithAgreementResultData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "签约开通信用产品失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<OpenCreditProductWithAgreementResultData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "签约开通信用产品失败")
        }
        throw APIClientError.decodeFailed
    }

    func ensureCreditProductOpened(
        userId: Int64,
        productCode: String
    ) async throws {
        let pack = try await fetchCreditOpenAgreementPack(userId: userId, productCode: productCode)
        let requiredTemplates = pack.agreements.filter(\.required)
        let finalTemplates = requiredTemplates.isEmpty ? pack.agreements : requiredTemplates
        guard !finalTemplates.isEmpty else {
            throw APIClientError.businessError(message: "未找到可签约协议")
        }
        _ = try await openCreditProductWithAgreement(
            userId: userId,
            productCode: productCode,
            agreementAccepts: finalTemplates,
            idempotencyKey: "AUTO_REG_\(productCode.uppercased())_\(userId)"
        )
    }

    func createFundSubscribe(
        orderNo: String,
        userId: Int64,
        fundCode: String,
        amount: Decimal,
        businessNo: String? = nil
    ) async throws -> FundTransactionData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        guard amount > 0 else {
            throw APIClientError.businessError(message: "申购金额必须大于0")
        }

        var payload: [String: Any] = [
            "orderNo": orderNo,
            "userId": String(userId),
            "fundCode": fundCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased(),
            "amount": String(format: "%.4f", NSDecimalNumber(decimal: amount).doubleValue)
        ]
        if let businessNo {
            let trimmed = businessNo.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                payload["businessNo"] = trimmed
            }
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/accounts/fund/subscribe",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<FundTransactionData>.self, from: data)
            if envelope.success {
                return envelope.data ?? FundTransactionData(orderNo: orderNo, transactionType: "SUBSCRIBE", transactionStatus: "PENDING", message: "转入申请已受理")
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "补齐爱存持仓失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<FundTransactionData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "补齐爱存持仓失败")
        }
        throw APIClientError.decodeFailed
    }

    func confirmFundSubscribe(
        orderNo: String,
        confirmedShare: Decimal,
        nav: Decimal
    ) async throws -> FundTransactionData {
        let payload: [String: Any] = [
            "orderNo": orderNo,
            "confirmedShare": String(format: "%.4f", NSDecimalNumber(decimal: confirmedShare).doubleValue),
            "nav": String(format: "%.4f", NSDecimalNumber(decimal: normalizedFundNav(nav)).doubleValue)
        ]

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/accounts/fund/subscribe/confirm",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<FundTransactionData>.self, from: data)
            if envelope.success {
                return envelope.data ?? FundTransactionData(orderNo: orderNo, transactionType: "SUBSCRIBE", transactionStatus: "CONFIRMED", message: "转入已确认")
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "确认爱存持仓失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<FundTransactionData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "确认爱存持仓失败")
        }
        throw APIClientError.decodeFailed
    }

    func transferIntoFundAccount(
        userId: Int64,
        fundCode: String = "AICASH",
        amount: Decimal,
        nav: Decimal,
        businessNo: String? = nil
    ) async throws -> FundTransactionData {
        guard amount > 0 else {
            throw APIClientError.businessError(message: "转入金额必须大于0")
        }

        let normalizedFundCode = fundCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        do {
            try await createFundAccount(userId: userId, fundCode: normalizedFundCode)
        } catch {
            if !isFundAccountAlreadyExistsError(error) {
                throw error
            }
        }

        let executeTransfer: () async throws -> FundTransactionData = {
            let orderNo = self.generateAiPayNumericId(domainCode: "30", bizType: "11", userId: userId)
            _ = try await self.createFundSubscribe(
                orderNo: orderNo,
                userId: userId,
                fundCode: normalizedFundCode,
                amount: amount,
                businessNo: businessNo
            )
            return try await self.confirmFundSubscribe(
                orderNo: orderNo,
                confirmedShare: self.normalizedFundShare(amount: amount, nav: nav),
                nav: nav
            )
        }

        do {
            return try await executeTransfer()
        } catch {
            guard isFundAccountNotFoundError(error) else {
                throw error
            }
            do {
                try await createFundAccount(userId: userId, fundCode: normalizedFundCode)
            } catch {
                if !isFundAccountAlreadyExistsError(error) {
                    throw error
                }
            }
            return try await executeTransfer()
        }
    }

    func seedFundHolding(
        userId: Int64,
        fundCode: String = "AICASH",
        amount: Decimal,
        nav: Decimal
    ) async throws {
        guard amount > 0 else {
            return
        }

        _ = try await transferIntoFundAccount(
            userId: userId,
            fundCode: fundCode,
            amount: amount,
            nav: nav,
            businessNo: "SYSTEM_SEED_FUND_HOLDING"
        )
    }

    func createFundFastRedeem(
        userId: Int64,
        fundCode: String,
        share: Decimal,
        businessNo: String? = nil,
        redeemDestination: String? = nil,
        bankName: String? = nil
    ) async throws -> FundTransactionData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedFundCode = fundCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        guard !normalizedFundCode.isEmpty else {
            throw APIClientError.businessError(message: "基金代码无效")
        }
        guard share > 0 else {
            throw APIClientError.businessError(message: "转出份额必须大于0")
        }

        let orderNo = generateAiPayNumericId(domainCode: "30", bizType: "21", userId: userId)
        let shareText = String(format: "%.4f", NSDecimalNumber(decimal: share).doubleValue)

        var payload: [String: Any] = [
            "orderNo": orderNo,
            "userId": String(userId),
            "fundCode": normalizedFundCode,
            "share": shareText
        ]
        if let businessNo {
            let trimmed = businessNo.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                payload["businessNo"] = trimmed
            }
        }
        if let redeemDestination {
            let trimmed = redeemDestination.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                payload["redeemDestination"] = trimmed.uppercased()
            }
        }
        if let bankName {
            let trimmed = bankName.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                payload["bankName"] = trimmed
            }
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/accounts/fund/fast-redeem",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<FundTransactionData>.self, from: data)
            if envelope.success {
                if let payload = envelope.data {
                    return payload
                }
                return FundTransactionData(
                    orderNo: orderNo,
                    transactionType: "FAST_REDEEM",
                    transactionStatus: "PENDING",
                    message: "快速转出已受理"
                )
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "爱存转出失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<FundTransactionData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "爱存转出失败")
        }
        throw APIClientError.decodeFailed
    }

    func confirmFundRedeem(orderNo: String) async throws -> FundTransactionData {
        let payload: [String: Any] = [
            "orderNo": orderNo
        ]

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/accounts/fund/redeem/confirm",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<FundTransactionData>.self, from: data)
            if envelope.success {
                return envelope.data ?? FundTransactionData(
                    orderNo: orderNo,
                    transactionType: "FAST_REDEEM",
                    transactionStatus: "CONFIRMED",
                    message: "快速转出已确认"
                )
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "爱存转出确认失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<FundTransactionData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "爱存转出确认失败")
        }
        throw APIClientError.decodeFailed
    }

    func createTransferTrade(
        payerUserId: Int64,
        payeeUserId: Int64,
        amount: Decimal = 10.00,
        currencyCode: String = "CNY",
        paymentMethod: String = "WALLET",
        paymentToolCode: String? = nil,
        metadata: String? = nil
    ) async throws -> TransferTradeData {
        guard payerUserId > 0 else {
            throw APIClientError.businessError(message: "付款账户无效")
        }
        guard payeeUserId > 0 else {
            throw APIClientError.businessError(message: "收款账户无效")
        }
        guard payerUserId != payeeUserId else {
            throw APIClientError.businessError(message: "不能给自己转账")
        }

        let normalizedCurrency = currencyCode.uppercased()
        let normalizedPaymentMethod: String = {
            let trimmed = paymentMethod.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? "WALLET" : trimmed.uppercased()
        }()
        let requestNo = "IOS-TRF-\(Int(Date().timeIntervalSince1970 * 1000))-\(UUID().uuidString.replacingOccurrences(of: "-", with: "").prefix(8))"
        let amountText = String(format: "%.2f", NSDecimalNumber(decimal: amount).doubleValue)

        var payload: [String: Any] = [
            "requestNo": requestNo,
            "businessSceneCode": "APP_INTERNAL_TRANSFER",
            // JS Number 在 BFF 中会丢失 64 位精度，用户ID按字符串透传到后端。
            "payerUserId": String(payerUserId),
            "payeeUserId": String(payeeUserId),
            "paymentMethod": normalizedPaymentMethod,
            "currencyCode": normalizedCurrency,
            "amount": amountText
        ]
        if normalizedPaymentMethod == "BANK_CARD" {
            payload["walletDebitAmount"] = "0.00"
            payload["fundDebitAmount"] = "0.00"
            payload["creditDebitAmount"] = "0.00"
            payload["inboundDebitAmount"] = amountText
        }
        if let paymentToolCode, !paymentToolCode.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            payload["paymentToolCode"] = paymentToolCode.trimmingCharacters(in: .whitespacesAndNewlines)
        }
        if let metadata, !metadata.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            payload["metadata"] = metadata.trimmingCharacters(in: .whitespacesAndNewlines)
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/trade/transfer",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<TransferTradeData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "转账失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<TransferTradeData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "转账失败")
        }
        throw APIClientError.decodeFailed
    }

    func createDepositTrade(
        payerUserId: Int64,
        amount: Decimal,
        paymentMethod: String = "BANK_CARD",
        paymentToolCode: String? = nil,
        metadata: String? = nil
    ) async throws -> TransferTradeData {
        guard payerUserId > 0 else {
            throw APIClientError.businessError(message: "付款账户无效")
        }
        guard amount > 0 else {
            throw APIClientError.businessError(message: "充值金额必须大于0")
        }

        let normalizedPaymentMethod: String = {
            let trimmed = paymentMethod.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? "BANK_CARD" : trimmed.uppercased()
        }()
        let requestNo = "IOS-DPS-\(Int(Date().timeIntervalSince1970 * 1000))-\(UUID().uuidString.replacingOccurrences(of: "-", with: "").prefix(8))"
        let amountText = String(format: "%.2f", NSDecimalNumber(decimal: amount).doubleValue)

        var payload: [String: Any] = [
            "requestNo": requestNo,
            "businessSceneCode": "APP_DEPOSIT",
            // JS Number 在 BFF 中会丢失 64 位精度，用户ID按字符串透传到后端。
            "payerUserId": String(payerUserId),
            "paymentMethod": normalizedPaymentMethod,
            // 后端 Money 反序列化支持直接传字符串金额，默认币种为 CNY。
            "amount": amountText
        ]
        if normalizedPaymentMethod == "BANK_CARD" {
            payload["walletDebitAmount"] = "0.00"
            payload["fundDebitAmount"] = "0.00"
            payload["creditDebitAmount"] = "0.00"
            payload["inboundDebitAmount"] = amountText
        }
        if let paymentToolCode, !paymentToolCode.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            payload["paymentToolCode"] = paymentToolCode.trimmingCharacters(in: .whitespacesAndNewlines)
        }
        if let metadata, !metadata.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            payload["metadata"] = metadata.trimmingCharacters(in: .whitespacesAndNewlines)
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/trade/deposit",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<TransferTradeData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "充值失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<TransferTradeData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "充值失败")
        }
        throw APIClientError.decodeFailed
    }

    func createWithdrawTrade(
        payerUserId: Int64,
        amount: Decimal,
        paymentMethod: String = "BANK_CARD",
        paymentToolCode: String? = nil,
        metadata: String? = nil
    ) async throws -> TransferTradeData {
        guard payerUserId > 0 else {
            throw APIClientError.businessError(message: "付款账户无效")
        }
        guard amount > 0 else {
            throw APIClientError.businessError(message: "提现金额必须大于0")
        }

        let normalizedPaymentMethod: String = {
            let trimmed = paymentMethod.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? "BANK_CARD" : trimmed.uppercased()
        }()
        let requestNo = "IOS-WDR-\(Int(Date().timeIntervalSince1970 * 1000))-\(UUID().uuidString.replacingOccurrences(of: "-", with: "").prefix(8))"
        let amountText = String(format: "%.2f", NSDecimalNumber(decimal: amount).doubleValue)

        var payload: [String: Any] = [
            "requestNo": requestNo,
            "businessSceneCode": "APP_WITHDRAW",
            // JS Number 在 BFF 中会丢失 64 位精度，用户ID按字符串透传到后端。
            "payerUserId": String(payerUserId),
            "paymentMethod": normalizedPaymentMethod,
            // 后端 Money 反序列化支持直接传字符串金额，默认币种为 CNY。
            "amount": amountText
        ]
        if let paymentToolCode, !paymentToolCode.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            payload["paymentToolCode"] = paymentToolCode.trimmingCharacters(in: .whitespacesAndNewlines)
        }
        if let metadata, !metadata.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            payload["metadata"] = metadata.trimmingCharacters(in: .whitespacesAndNewlines)
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/trade/withdraw",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<TransferTradeData>.self, from: data)
            if envelope.success {
                if let payload = envelope.data {
                    return payload
                }
                return TransferTradeData(
                    tradeNo: "",
                    requestNo: requestNo,
                    status: "ACCEPTED",
                    payerUserId: payerUserId,
                    payeeUserId: nil
                )
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "提现失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<TransferTradeData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "提现失败")
        }
        throw APIClientError.decodeFailed
    }

    func createPayTrade(
        payerUserId: Int64,
        payeeUserId: Int64,
        amount: Decimal,
        paymentMethod: String = "BANK_CARD",
        paymentToolCode: String? = nil,
        couponNo: String? = nil,
        businessSceneCode: String = "APP_CREDIT_REPAY",
        metadata: String? = nil
    ) async throws -> TransferTradeData {
        guard payerUserId > 0 else {
            throw APIClientError.businessError(message: "付款账户无效")
        }
        guard payeeUserId > 0 else {
            throw APIClientError.businessError(message: "收款账户无效")
        }
        guard amount > 0 else {
            throw APIClientError.businessError(message: "还款金额必须大于0")
        }

        let normalizedPaymentMethod: String = {
            let trimmed = paymentMethod.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
            if trimmed == "BANK_CARD" || trimmed.hasPrefix("BANK_CARD:") {
                return "BANK_CARD"
            }
            if trimmed == "FUND_ACCOUNT" || trimmed == "FUND" || trimmed == "AICASH" || trimmed == "AICASH" {
                return "FUND_ACCOUNT"
            }
            if trimmed == "LOAN_ACCOUNT" || trimmed == "LOAN" || trimmed == "AILOAN" || trimmed == "AILOAN" {
                return "LOAN_ACCOUNT"
            }
            if trimmed == "CREDIT_ACCOUNT" || trimmed == "AICREDIT" || trimmed == "AICREDIT" || trimmed == "CREDIT" {
                return "CREDIT_ACCOUNT"
            }
            return "WALLET"
        }()
        let requestNo = "IOS-PAY-\(Int(Date().timeIntervalSince1970 * 1000))-\(UUID().uuidString.replacingOccurrences(of: "-", with: "").prefix(8))"
        let amountText = String(format: "%.2f", NSDecimalNumber(decimal: amount).doubleValue)

        var payload: [String: Any] = [
            "requestNo": requestNo,
            "businessSceneCode": businessSceneCode,
            "payerUserId": String(payerUserId),
            "payeeUserId": String(payeeUserId),
            "paymentMethod": normalizedPaymentMethod,
            "amount": amountText
        ]
        // 交易服务端会基于 paymentMethod + 计费 payableAmount 自动生成 split plan。
        // 客户端不再显式透传拆分金额，避免与服务端计费口径不一致导致的提交失败。
        if let paymentToolCode, !paymentToolCode.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            payload["paymentToolCode"] = paymentToolCode.trimmingCharacters(in: .whitespacesAndNewlines)
        }
        if let couponNo, !couponNo.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            payload["couponNo"] = couponNo.trimmingCharacters(in: .whitespacesAndNewlines)
        }
        if let metadata, !metadata.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            payload["metadata"] = metadata.trimmingCharacters(in: .whitespacesAndNewlines)
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/trade/pay",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<TransferTradeData>.self, from: data)
            if envelope.success {
                if let payload = envelope.data {
                    return payload
                }
                return TransferTradeData(
                    tradeNo: "",
                    requestNo: requestNo,
                    status: "ACCEPTED",
                    payerUserId: payerUserId,
                    payeeUserId: payeeUserId
                )
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "支付失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<TransferTradeData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "支付失败")
        }
        throw APIClientError.decodeFailed
    }

    func createLoanAccountPayTrade(
        payerUserId: Int64,
        payeeUserId: Int64,
        amount: Decimal,
        metadata: String? = nil
    ) async throws -> TransferTradeData {
        let mergedMetadata = mergeMetadata(
            "entry=trade-pay-loanAccount",
            metadata
        )
        return try await createPayTrade(
            payerUserId: payerUserId,
            payeeUserId: payeeUserId,
            amount: amount,
            paymentMethod: "LOAN_ACCOUNT",
            paymentToolCode: nil,
            couponNo: nil,
            businessSceneCode: "TRADE_PAY_LOAN_ACCOUNT",
            metadata: mergedMetadata
        )
    }

    func fetchMobileTopUpRewardCoupons(userId: Int64) async throws -> [MobileTopUpRewardCouponIssueData] {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "未找到当前登录用户")
        }
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/coupons/mobile-topup-reward/available?userId=\(userId)",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<[MobileTopUpRewardCouponIssueData]>.self, from: data)
            if envelope.success {
                return envelope.data ?? []
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询话费红包失败")
        }
        if let envelope = try? decoder.decode(BffEnvelope<[MobileTopUpRewardCouponIssueData]>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询话费红包失败")
        }
        throw APIClientError.decodeFailed
    }

    func claimMobileTopUpRewardCoupon(
        userId: Int64,
        businessNo: String
    ) async throws -> MobileTopUpRewardCouponData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "未找到当前登录用户")
        }
        let normalizedBusinessNo = businessNo.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedBusinessNo.isEmpty else {
            throw APIClientError.businessError(message: "领取业务号不能为空")
        }

        let payload: [String: Any] = [
            "userId": String(userId),
            "businessNo": normalizedBusinessNo
        ]
        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/coupons/mobile-topup-reward/claim",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<MobileTopUpRewardCouponData>.self, from: data)
            if envelope.success, let result = envelope.data {
                return result
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "领取话费红包失败")
        }
        if let envelope = try? decoder.decode(BffEnvelope<MobileTopUpRewardCouponData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "领取话费红包失败")
        }
        throw APIClientError.decodeFailed
    }

    private func mergeMetadata(_ base: String, _ extra: String?) -> String {
        let normalizedBase = base.trimmingCharacters(in: .whitespacesAndNewlines)
        let normalizedExtra = extra?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if normalizedBase.isEmpty {
            return normalizedExtra
        }
        if normalizedExtra.isEmpty {
            return normalizedBase
        }
        return normalizedBase + ";" + normalizedExtra
    }

    func fetchCashierView(
        userId: Int64,
        sceneCode: String = "TRANSFER"
    ) async throws -> CashierViewData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedSceneCode = sceneCode.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            ? "TRANSFER"
            : sceneCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        guard let encodedSceneCode = normalizedSceneCode.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) else {
            throw APIClientError.invalidURL
        }

        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/cashier/users/\(userId)/view?sceneCode=\(encodedSceneCode)",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<CashierViewData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询支付工具失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<CashierViewData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询支付工具失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchCashierPayTools(
        userId: Int64,
        sceneCode: String = "TRANSFER"
    ) async throws -> [CashierPayToolData] {
        try await fetchCashierView(
            userId: userId,
            sceneCode: sceneCode
        ).payTools
    }

    func fetchCashierPricingPreview(
        userId: Int64,
        sceneCode: String = "WITHDRAW",
        paymentMethod: String = "BANK_CARD",
        amount: Decimal,
        currencyCode: String = "CNY"
    ) async throws -> CashierPricingPreviewData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        guard amount > 0 else {
            throw APIClientError.businessError(message: "试算金额必须大于0")
        }

        let normalizedSceneCode = sceneCode.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            ? "WITHDRAW"
            : sceneCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        let normalizedPaymentMethod = paymentMethod.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            ? "BANK_CARD"
            : paymentMethod.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        let normalizedCurrencyCode = currencyCode.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            ? "CNY"
            : currencyCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()

        var components = URLComponents()
        components.queryItems = [
            URLQueryItem(name: "sceneCode", value: normalizedSceneCode),
            URLQueryItem(name: "paymentMethod", value: normalizedPaymentMethod),
            URLQueryItem(name: "amount", value: String(format: "%.2f", NSDecimalNumber(decimal: amount).doubleValue)),
            URLQueryItem(name: "currencyCode", value: normalizedCurrencyCode)
        ]
        let query = components.percentEncodedQuery ?? ""
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/cashier/users/\(userId)/pricing-preview?\(query)",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<CashierPricingPreviewData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "提现手续费试算失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<CashierPricingPreviewData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "提现手续费试算失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchHomeDeliverBanner(userId: Int64?) async throws -> DeliverPositionData? {
        var components = URLComponents()
        components.queryItems = [
            URLQueryItem(name: "positionCodeList", value: "HOME_COMMON_BANNER"),
            URLQueryItem(name: "clientId", value: "IOS_APP"),
            URLQueryItem(name: "sceneCode", value: "HOME"),
            URLQueryItem(name: "channel", value: "IOS"),
            URLQueryItem(
                name: "_ts",
                value: String(Int64((Date().timeIntervalSince1970 * 1000).rounded()))
            )
        ]
        if let userId, userId > 0 {
            components.queryItems?.append(URLQueryItem(name: "userId", value: String(userId)))
        }
        let query = components.percentEncodedQuery ?? ""
        let bffPath = query.isEmpty ? "/bff/deliver" : "/bff/deliver?\(query)"
        let (data, httpResponse) = try await requestBff(
            bffPath: bffPath,
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<[String: DeliverPositionData]>.self, from: data)
            if envelope.success {
                return envelope.data?["HOME_COMMON_BANNER"]
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询首页投放横幅失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<[String: DeliverPositionData]>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询首页投放横幅失败")
        }
        throw APIClientError.decodeFailed
    }

    func reportDeliverClick(userId: Int64?,
                            positionCode: String,
                            unitCode: String?,
                            creativeCode: String?) async throws {
        var payload: [String: Any] = [
            "clientId": "IOS_APP",
            "sceneCode": "HOME",
            "channel": "IOS",
            "positionCode": positionCode,
            "eventType": "CLICK"
        ]
        if let userId, userId > 0 {
            payload["userId"] = String(userId)
        }
        if let unitCode, !unitCode.isEmpty {
            payload["unitCode"] = unitCode
        }
        if let creativeCode, !creativeCode.isEmpty {
            payload["creativeCode"] = creativeCode
        }
        let body = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/deliver/events",
            method: "POST",
            body: body
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<Bool>.self, from: data)
            if envelope.success {
                return
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "投放点击回传失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<Bool>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "投放点击回传失败")
        }
        throw APIClientError.decodeFailed
    }

    private func requestBff(
        bffPath: String,
        method: String,
        body: Data?,
        bffTimeoutOverride: TimeInterval? = nil,
        skipVisitTracking: Bool = false
    ) async throws -> (Data, HTTPURLResponse) {
        let result = try await requestBffDetailed(
            bffPath: bffPath,
            method: method,
            body: body,
            bffTimeoutOverride: bffTimeoutOverride,
            skipVisitTracking: skipVisitTracking
        )
        return (result.data, result.httpResponse)
    }

    private func requestBffOnly(
        path: String,
        method: String,
        body: Data?,
        skipVisitTracking: Bool = false
    ) async throws -> (Data, HTTPURLResponse) {
        guard let url = makeBffURL(path: path) else {
            throw APIClientError.invalidURL
        }
        return try await executeRequest(
            url: url,
            method: method,
            body: body,
            skipVisitTracking: skipVisitTracking
        )
    }

    private func requestBffDetailed(
        bffPath: String,
        method: String,
        body: Data?,
        bffTimeoutOverride: TimeInterval? = nil,
        skipVisitTracking: Bool = false
    ) async throws -> BffRequestResult {
        guard let bffURL = makeBffURL(path: bffPath) else {
            throw APIClientError.invalidURL
        }

        let startedAt = Date()
        do {
            let result = try await executeRequest(
                url: bffURL,
                method: method,
                body: body,
                timeoutInterval: bffTimeoutOverride,
                skipVisitTracking: true
            )
            if !skipVisitTracking {
                scheduleVisitTracking(apiName: bffPath, method: method, body: body, httpResponse: result.1, error: nil, startedAt: startedAt)
            }
            return BffRequestResult(data: result.0, httpResponse: result.1)
        } catch {
            if shouldFallback(error) {
                let finalError = APIClientError.businessError(message: "无法连接服务器，请稍后重试")
                if !skipVisitTracking {
                    scheduleVisitTracking(apiName: bffPath, method: method, body: body, httpResponse: nil, error: finalError, startedAt: startedAt)
                }
                throw finalError
            }
            if !skipVisitTracking {
                scheduleVisitTracking(apiName: bffPath, method: method, body: body, httpResponse: nil, error: error, startedAt: startedAt)
            }
            throw error
        }
    }

    private func executeRequest(
        url: URL,
        method: String,
        body: Data?,
        timeoutInterval: TimeInterval? = nil,
        skipVisitTracking: Bool = false
    ) async throws -> (Data, HTTPURLResponse) {
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.timeoutInterval = max(0.5, timeoutInterval ?? requestTimeout)
        if body != nil {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }
        attachRuntimeHeaders(to: &request)
        if shouldAttachAuthorization(for: url), let authorizationHeader = currentAuthorizationHeader() {
            request.setValue(authorizationHeader, forHTTPHeaderField: "Authorization")
        }
        request.httpBody = body

        let startedAt = Date()
        do {
            let (data, response) = try await session.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse else {
                throw APIClientError.invalidResponse
            }
            if shouldAttemptAuthorizationRecovery(httpResponse: httpResponse, url: url),
               let refreshedAuthorizationHeader = refreshedAuthorizationHeaderIfNeeded(
                   previousAuthorizationHeader: request.value(forHTTPHeaderField: "Authorization")
               ) {
                var retryRequest = request
                retryRequest.setValue(refreshedAuthorizationHeader, forHTTPHeaderField: "Authorization")
                let (retryData, retryResponse) = try await session.data(for: retryRequest)
                guard let retryHttpResponse = retryResponse as? HTTPURLResponse else {
                    throw APIClientError.invalidResponse
                }
                if !skipVisitTracking {
                    scheduleVisitTracking(
                        apiName: normalizedTrackedApiName(url: url),
                        method: method,
                        body: body,
                        httpResponse: retryHttpResponse,
                        error: nil,
                        startedAt: startedAt
                    )
                }
                return (retryData, retryHttpResponse)
            }
            if !skipVisitTracking {
                scheduleVisitTracking(apiName: normalizedTrackedApiName(url: url), method: method, body: body, httpResponse: httpResponse, error: nil, startedAt: startedAt)
            }
            return (data, httpResponse)
        } catch {
            if !skipVisitTracking {
                scheduleVisitTracking(apiName: normalizedTrackedApiName(url: url), method: method, body: body, httpResponse: nil, error: error, startedAt: startedAt)
            }
            throw error
        }
    }

    private func shouldAttemptAuthorizationRecovery(
        httpResponse: HTTPURLResponse,
        url: URL
    ) -> Bool {
        guard shouldAttachAuthorization(for: url) else {
            return false
        }
        return httpResponse.statusCode == 401
    }

    private func refreshedAuthorizationHeaderIfNeeded(
        previousAuthorizationHeader: String?
    ) -> String? {
        guard let session = authStore.loadSession(expectedBffBaseURL: AppRuntime.bffBaseURL) else {
            return nil
        }
        let accessToken = session.accessToken.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !accessToken.isEmpty else {
            return nil
        }
        let normalizedTokenType = session.tokenType
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .isEmpty ? "Bearer" : session.tokenType.trimmingCharacters(in: .whitespacesAndNewlines)
        let refreshedHeader = "\(normalizedTokenType) \(accessToken)"
        let normalizedPrevious = previousAuthorizationHeader?
            .trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard normalizedPrevious != refreshedHeader else {
            return nil
        }
        return refreshedHeader
    }

    private func currentAuthorizationHeader() -> String? {
        guard let session = authStore.loadSession(expectedBffBaseURL: AppRuntime.bffBaseURL) else {
            return nil
        }
        let tokenType = session.tokenType.trimmingCharacters(in: .whitespacesAndNewlines)
        let accessToken = session.accessToken.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !accessToken.isEmpty else {
            return nil
        }
        if tokenType.isEmpty {
            return "Bearer \(accessToken)"
        }
        return "\(tokenType) \(accessToken)"
    }

    private func validatedAuthSession(_ session: LoginResponseData, failureMessage: String) throws -> LoginResponseData {
        let normalizedAccessToken = session.accessToken.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedAccessToken.isEmpty else {
            throw APIClientError.businessError(message: failureMessage)
        }
        let normalizedTokenType = session.tokenType.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            ? "Bearer"
            : session.tokenType.trimmingCharacters(in: .whitespacesAndNewlines)
        if normalizedAccessToken == session.accessToken && normalizedTokenType == session.tokenType {
            return session
        }
        return LoginResponseData(
            accessToken: normalizedAccessToken,
            tokenType: normalizedTokenType,
            expiresInSeconds: session.expiresInSeconds,
            user: session.user,
            demo: session.demo
        )
    }

    private func shouldAttachAuthorization(for url: URL) -> Bool {
        let path = normalizedGatewayPath(url.path.lowercased())
        if path == "/bff/auth/mobile-verify-login"
            || path == "/api/auth/mobile-verify-login"
            || path == "/bff/auth/demo-auto-login"
            || path == "/api/auth/demo-auto-login"
            || path == "/bff/auth/preset-login-accounts"
            || path == "/api/auth/preset-login-accounts"
            || path == "/bff/page-init"
            || path == "/api/page-init"
            || path == "/bff/users/profile-by-login"
            || path == "/api/users/profile-by-login" {
            return false
        }
        if path.hasPrefix("/bff/user-flows/") || path.hasPrefix("/api/user-flows/") {
            return false
        }
        if path == "/bff/apps/devices" || path == "/bff/apps/visit-records" || path == "/bff/apps/behavior-events" {
            return false
        }
        if path.range(of: "^/bff/apps/[^/]+/versions/check$", options: .regularExpression) != nil {
            return false
        }
        return path.hasPrefix("/bff/") || path.hasPrefix("/api/")
    }

    private func shouldFallback(_ error: Error) -> Bool {
        if let apiError = error as? APIClientError {
            switch apiError {
            case .invalidResponse:
                return true
            default:
                break
            }
        }
        if let decodingError = error as? DecodingError {
            switch decodingError {
            case .dataCorrupted, .keyNotFound, .typeMismatch, .valueNotFound:
                return true
            @unknown default:
                break
            }
        }
        guard let urlError = error as? URLError else {
            return false
        }
        switch urlError.code {
        case .badServerResponse,
             .cannotFindHost,
             .cannotConnectToHost,
             .cannotParseResponse,
             .networkConnectionLost,
             .timedOut,
             .notConnectedToInternet:
            return true
        default:
            return false
        }
    }

    func fetchRecentContacts(userId: Int64, limit: Int = 20) async throws -> [RecentContactData] {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedLimit = max(1, min(limit, 50))
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/users/\(userId)/recent-contacts?limit=\(normalizedLimit)",
            method: "GET",
            body: nil,
            bffTimeoutOverride: 2.5
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<[RecentContactData]>.self, from: data)
            if envelope.success {
                return envelope.data ?? []
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询最近联系人失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<[RecentContactData]>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询最近联系人失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchShortVideoFeed(cursor: String?, limit: Int = 3) async throws -> ShortVideoFeedPageData {
        let normalizedLimit = max(1, min(limit, 10))
        var components = URLComponents()
        var queryItems = [URLQueryItem(name: "limit", value: String(normalizedLimit))]
        if let cursor {
            let trimmedCursor = cursor.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmedCursor.isEmpty {
                queryItems.append(URLQueryItem(name: "cursor", value: trimmedCursor))
            }
        }
        components.queryItems = queryItems
        let query = components.percentEncodedQuery ?? ""
        let path = query.isEmpty ? "/api/short-video/feed" : "/api/short-video/feed?\(query)"
        let (data, httpResponse) = try await requestBff(
            bffPath: path,
            method: "GET",
            body: nil,
            bffTimeoutOverride: 8
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<ShortVideoFeedPageData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "加载短视频失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<ShortVideoFeedPageData>.self, from: data) {
            if httpResponse.statusCode == 401 {
                throw APIClientError.businessError(message: envelope.error?.message ?? "请先登录后再查看视频")
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "加载短视频失败")
        }
        if httpResponse.statusCode == 401 {
            throw APIClientError.businessError(message: "请先登录后再查看视频")
        }
        throw APIClientError.decodeFailed
    }

    func updateShortVideoLike(videoId: String, isLiked: Bool) async throws -> ShortVideoEngagementData {
        let encodedVideoId = try shortVideoEncodedVideoId(videoId)
        let failureMessage = isLiked ? "点赞失败" : "取消点赞失败"
        return try await requestShortVideoEngagement(
            path: "/api/short-video/videos/\(encodedVideoId)/like",
            method: isLiked ? "POST" : "DELETE",
            failureMessage: failureMessage
        )
    }

    func updateShortVideoFavorite(videoId: String, isFavorited: Bool) async throws -> ShortVideoEngagementData {
        let encodedVideoId = try shortVideoEncodedVideoId(videoId)
        let failureMessage = isFavorited ? "收藏失败" : "取消收藏失败"
        return try await requestShortVideoEngagement(
            path: "/api/short-video/videos/\(encodedVideoId)/favorite",
            method: isFavorited ? "POST" : "DELETE",
            failureMessage: failureMessage
        )
    }

    private func requestShortVideoEngagement(path: String,
                                             method: String,
                                             failureMessage: String) async throws -> ShortVideoEngagementData {
        let (data, httpResponse) = try await requestBff(
            bffPath: path,
            method: method,
            body: nil,
            bffTimeoutOverride: 6
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<ShortVideoEngagementData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? failureMessage)
        }

        if let envelope = try? decoder.decode(BffEnvelope<ShortVideoEngagementData>.self, from: data) {
            if httpResponse.statusCode == 401 {
                throw APIClientError.businessError(message: envelope.error?.message ?? "请先登录后再进行互动")
            }
            if httpResponse.statusCode == 404 {
                throw APIClientError.businessError(message: envelope.error?.message ?? "视频不存在或已下线")
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? failureMessage)
        }
        if httpResponse.statusCode == 401 {
            throw APIClientError.businessError(message: "请先登录后再进行互动")
        }
        if httpResponse.statusCode == 404 {
            throw APIClientError.businessError(message: "视频不存在或已下线")
        }
        throw APIClientError.decodeFailed
    }

    func fetchShortVideoComments(videoId: String,
                                 cursor: String?,
                                 limit: Int = 20) async throws -> ShortVideoCommentPageData {
        let encodedVideoId = try shortVideoEncodedVideoId(videoId)
        let normalizedLimit = max(1, min(limit, 50))
        var components = URLComponents()
        var queryItems = [URLQueryItem(name: "limit", value: String(normalizedLimit))]
        if let cursor {
            let trimmedCursor = cursor.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmedCursor.isEmpty {
                queryItems.append(URLQueryItem(name: "cursor", value: trimmedCursor))
            }
        }
        components.queryItems = queryItems
        let query = components.percentEncodedQuery ?? ""
        let path = query.isEmpty
            ? "/api/short-video/videos/\(encodedVideoId)/comments"
            : "/api/short-video/videos/\(encodedVideoId)/comments?\(query)"
        let (data, httpResponse) = try await requestBff(
            bffPath: path,
            method: "GET",
            body: nil,
            bffTimeoutOverride: 6
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<ShortVideoCommentPageData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "加载评论失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<ShortVideoCommentPageData>.self, from: data) {
            if httpResponse.statusCode == 401 {
                throw APIClientError.businessError(message: envelope.error?.message ?? "请先登录后再查看评论")
            }
            if httpResponse.statusCode == 404 {
                throw APIClientError.businessError(message: envelope.error?.message ?? "视频不存在或已下线")
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "加载评论失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchShortVideoCommentReplies(commentId: String,
                                       cursor: String?,
                                       limit: Int = 20) async throws -> ShortVideoCommentPageData {
        let encodedCommentId = try shortVideoEncodedCommentId(commentId)
        let normalizedLimit = max(1, min(limit, 50))
        var components = URLComponents()
        var queryItems = [URLQueryItem(name: "limit", value: String(normalizedLimit))]
        if let cursor {
            let trimmedCursor = cursor.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmedCursor.isEmpty {
                queryItems.append(URLQueryItem(name: "cursor", value: trimmedCursor))
            }
        }
        components.queryItems = queryItems
        let query = components.percentEncodedQuery ?? ""
        let path = query.isEmpty
            ? "/api/short-video/comments/\(encodedCommentId)/replies"
            : "/api/short-video/comments/\(encodedCommentId)/replies?\(query)"
        let (data, httpResponse) = try await requestBff(
            bffPath: path,
            method: "GET",
            body: nil,
            bffTimeoutOverride: 6
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<ShortVideoCommentPageData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "加载回复失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<ShortVideoCommentPageData>.self, from: data) {
            if httpResponse.statusCode == 401 {
                throw APIClientError.businessError(message: envelope.error?.message ?? "请先登录后再查看回复")
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "加载回复失败")
        }
        throw APIClientError.decodeFailed
    }

    func createShortVideoComment(videoId: String,
                                 content: String?,
                                 parentCommentId: String?,
                                 imageMediaId: String?) async throws -> ShortVideoCommentData {
        let encodedVideoId = try shortVideoEncodedVideoId(videoId)
        let normalizedContent = try normalizedShortVideoCommentContent(content)
        let normalizedParentCommentId = normalizedShortVideoCommentOptionalId(parentCommentId)
        let normalizedImageMediaId = normalizedShortVideoCommentOptionalId(imageMediaId)
        guard normalizedContent != nil || normalizedImageMediaId != nil else {
            throw APIClientError.businessError(message: "评论内容不能为空")
        }
        var requestObject: [String: Any] = [:]
        if let normalizedContent {
            requestObject["content"] = normalizedContent
        }
        if let normalizedParentCommentId {
            requestObject["parentCommentId"] = normalizedParentCommentId
        }
        if let normalizedImageMediaId {
            requestObject["imageMediaId"] = normalizedImageMediaId
        }
        let body = try JSONSerialization.data(withJSONObject: requestObject, options: [])
        let (data, httpResponse) = try await requestBff(
            bffPath: "/api/short-video/videos/\(encodedVideoId)/comments",
            method: "POST",
            body: body,
            bffTimeoutOverride: 6
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<ShortVideoCommentData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "评论发布失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<ShortVideoCommentData>.self, from: data) {
            if httpResponse.statusCode == 401 {
                throw APIClientError.businessError(message: envelope.error?.message ?? "请先登录后再发表评论")
            }
            if httpResponse.statusCode == 404 {
                throw APIClientError.businessError(message: envelope.error?.message ?? "视频不存在或已下线")
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "评论发布失败")
        }
        throw APIClientError.decodeFailed
    }

    func updateShortVideoCommentLike(commentId: String, isLiked: Bool) async throws -> ShortVideoCommentLikeData {
        let encodedCommentId = try shortVideoEncodedCommentId(commentId)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/api/short-video/comments/\(encodedCommentId)/like",
            method: isLiked ? "POST" : "DELETE",
            body: nil,
            bffTimeoutOverride: 6
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<ShortVideoCommentLikeData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "评论点赞失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<ShortVideoCommentLikeData>.self, from: data) {
            if httpResponse.statusCode == 401 {
                throw APIClientError.businessError(message: envelope.error?.message ?? "请先登录后再点赞评论")
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "评论点赞失败")
        }
        throw APIClientError.decodeFailed
    }

    func uploadShortVideoCommentImage(imageData: Data,
                                      fileName: String = "short-video-comment.jpg",
                                      mimeType: String = "image/jpeg") async throws -> MediaAssetData {
        let ownerUserId = try currentAuthenticatedUserId()
        return try await uploadImage(
            ownerUserId: ownerUserId,
            imageData: imageData,
            fileName: fileName,
            mimeType: mimeType
        )
    }

    private func shortVideoEncodedVideoId(_ rawVideoId: String) throws -> String {
        let trimmedVideoId = rawVideoId.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedVideoId.isEmpty else {
            throw APIClientError.businessError(message: "videoId must not be blank")
        }
        if let encoded = trimmedVideoId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) {
            return encoded
        }
        throw APIClientError.invalidURL
    }

    private func shortVideoEncodedCommentId(_ rawCommentId: String) throws -> String {
        let trimmedCommentId = rawCommentId.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedCommentId.isEmpty else {
            throw APIClientError.businessError(message: "commentId must not be blank")
        }
        if let encoded = trimmedCommentId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) {
            return encoded
        }
        throw APIClientError.invalidURL
    }

    private func normalizedShortVideoCommentContent(_ rawContent: String?) throws -> String? {
        let trimmed = rawContent?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard !trimmed.isEmpty else {
            return nil
        }
        guard trimmed.count <= 500 else {
            throw APIClientError.businessError(message: "content length must be <= 500")
        }
        return trimmed
    }

    private func normalizedShortVideoCommentOptionalId(_ rawValue: String?) -> String? {
        let trimmed = rawValue?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? nil : trimmed
    }

    private func currentAuthenticatedUserId() throws -> Int64 {
        guard let session = authStore.loadSession(expectedBffBaseURL: AppRuntime.bffBaseURL),
              session.user.userId > 0 else {
            throw APIClientError.businessError(message: "请先登录后再上传图片")
        }
        return session.user.userId
    }

    func fetchFriends(userId: Int64, limit: Int = 100) async throws -> [ContactFriendData] {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedLimit = max(1, min(limit, 200))
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/users/\(userId)/friends?limit=\(normalizedLimit)",
            method: "GET",
            body: nil,
            bffTimeoutOverride: 2.5
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<[ContactFriendData]>.self, from: data)
            if envelope.success {
                return envelope.data ?? []
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询好友失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<[ContactFriendData]>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询好友失败")
        }
        throw APIClientError.decodeFailed
    }

    func searchContacts(ownerUserId: Int64, keyword: String, limit: Int = 20) async throws -> [ContactSearchData] {
        guard ownerUserId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedKeyword = keyword.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedKeyword.isEmpty else {
            throw APIClientError.businessError(message: "搜索关键字不能为空")
        }
        let normalizedLimit = max(1, min(limit, 100))

        var components = URLComponents()
        components.queryItems = [
            URLQueryItem(name: "ownerUserId", value: String(ownerUserId)),
            URLQueryItem(name: "keyword", value: normalizedKeyword),
            URLQueryItem(name: "limit", value: String(normalizedLimit))
        ]
        let query = components.percentEncodedQuery ?? ""

        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/contacts/search?\(query)",
            method: "GET",
            body: nil,
            bffTimeoutOverride: 2.5
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<[ContactSearchData]>.self, from: data)
            if envelope.success {
                return envelope.data ?? []
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "联系人搜索失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<[ContactSearchData]>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "联系人搜索失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchReceivedFriendRequests(targetUserId: Int64, limit: Int = 20) async throws -> [ContactRequestData] {
        guard targetUserId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedLimit = max(1, min(limit, 100))
        let pathQuery = "targetUserId=\(targetUserId)&limit=\(normalizedLimit)"
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/contacts/requests/received?\(pathQuery)",
            method: "GET",
            body: nil,
            bffTimeoutOverride: 2.5
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<[ContactRequestData]>.self, from: data)
            if envelope.success {
                return envelope.data ?? []
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询好友申请失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<[ContactRequestData]>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询好友申请失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchSentFriendRequests(requesterUserId: Int64, limit: Int = 20) async throws -> [ContactRequestData] {
        guard requesterUserId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedLimit = max(1, min(limit, 100))
        let pathQuery = "requesterUserId=\(requesterUserId)&limit=\(normalizedLimit)"
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/contacts/requests/sent?\(pathQuery)",
            method: "GET",
            body: nil,
            bffTimeoutOverride: 2.5
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<[ContactRequestData]>.self, from: data)
            if envelope.success {
                return envelope.data ?? []
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询好友申请失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<[ContactRequestData]>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询好友申请失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchSearchResults(userId: Int64, keyword: String, limit: Int = 12) async throws -> SearchResponseData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedKeyword = keyword.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedKeyword.isEmpty else {
            throw APIClientError.businessError(message: "搜索关键字不能为空")
        }
        let normalizedLimit = max(1, min(limit, 20))

        var components = URLComponents()
        components.queryItems = [
            URLQueryItem(name: "uid", value: String(userId)),
            URLQueryItem(name: "keyword", value: normalizedKeyword),
            URLQueryItem(name: "limit", value: String(normalizedLimit))
        ]
        let query = components.percentEncodedQuery ?? ""

        let (data, httpResponse) = try await requestBffOnly(
            path: "/bff/search?\(query)",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<SearchResponseData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "搜索失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<SearchResponseData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "搜索失败")
        }
        if httpResponse.statusCode == 404 {
            throw APIClientError.businessError(message: "搜索服务未就绪，请确认 app-bff 已更新并重启")
        }
        throw APIClientError.decodeFailed
    }

    func applyFriendRequest(
        requesterUserId: Int64,
        targetUserId: Int64,
        applyMessage: String? = nil
    ) async throws {
        guard requesterUserId > 0 else {
            throw APIClientError.businessError(message: "申请人标识无效")
        }
        guard targetUserId > 0 else {
            throw APIClientError.businessError(message: "目标用户标识无效")
        }

        let normalizedApplyMessage = applyMessage?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        var payload: [String: Any] = [
            "requesterUserId": String(requesterUserId),
            "targetUserId": String(targetUserId)
        ]
        if !normalizedApplyMessage.isEmpty {
            payload["applyMessage"] = normalizedApplyMessage
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/contacts/requests",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<EmptyPayload>.self, from: data)
            if envelope.success {
                return
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "发送好友申请失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<EmptyPayload>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "发送好友申请失败")
        }
        throw APIClientError.decodeFailed
    }

    func handleFriendRequest(
        operatorUserId: Int64,
        requestNo: String,
        action: String
    ) async throws {
        guard operatorUserId > 0 else {
            throw APIClientError.businessError(message: "处理人标识无效")
        }
        let normalizedRequestNo = requestNo.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedRequestNo.isEmpty else {
            throw APIClientError.businessError(message: "申请编号无效")
        }
        let normalizedAction = action.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        guard normalizedAction == "ACCEPT" || normalizedAction == "REJECT" else {
            throw APIClientError.businessError(message: "处理动作无效")
        }

        let requestBody = try JSONSerialization.data(withJSONObject: [
            "operatorUserId": String(operatorUserId),
            "action": normalizedAction
        ])
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/contacts/requests/\(normalizedRequestNo)/handle",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<EmptyPayload>.self, from: data)
            if envelope.success {
                return
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "处理好友申请失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<EmptyPayload>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "处理好友申请失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchConversations(userId: Int64, limit: Int = 30) async throws -> [ConversationData] {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let normalizedLimit = max(1, min(limit, 100))
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/messages/users/\(userId)/home?limit=\(normalizedLimit)",
            method: "GET",
            body: nil,
            bffTimeoutOverride: 2.5
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            if let envelope = try? decoder.decode(BffEnvelope<MessageHomeData>.self, from: data) {
                if envelope.success {
                    return envelope.data?.conversations ?? []
                }
                throw APIClientError.businessError(message: envelope.error?.message ?? "查询会话失败")
            }

            let envelope = try decoder.decode(BffEnvelope<[ConversationData]>.self, from: data)
            if envelope.success {
                return envelope.data ?? []
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询会话失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<MessageHomeData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询会话失败")
        }
        if let envelope = try? decoder.decode(BffEnvelope<[ConversationData]>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询会话失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchConversationMessages(
        userId: Int64,
        conversationNo: String,
        beforeMessageId: String? = nil,
        limit: Int = 100
    ) async throws -> [MessageData] {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let trimmedConversationNo = conversationNo.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedConversationNo.isEmpty else {
            throw APIClientError.businessError(message: "会话号无效")
        }
        let normalizedLimit = max(1, min(limit, 200))
        guard let encodedConversationNo = trimmedConversationNo.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) else {
            throw APIClientError.invalidURL
        }

        var queryItems = [
            URLQueryItem(name: "userId", value: String(userId)),
            URLQueryItem(name: "limit", value: String(normalizedLimit))
        ]
        if let beforeMessageId {
            let trimmed = beforeMessageId.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                queryItems.append(URLQueryItem(name: "beforeMessageId", value: trimmed))
            }
        }
        var queryBuilder = URLComponents()
        queryBuilder.queryItems = queryItems
        let query = queryBuilder.percentEncodedQuery ?? ""

        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/messages/conversations/\(encodedConversationNo)/messages?\(query)",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            if let envelope = try? decoder.decode(BffEnvelope<ConversationMessagesData>.self, from: data) {
                if envelope.success {
                    return envelope.data?.items ?? []
                }
                throw APIClientError.businessError(message: envelope.error?.message ?? "查询会话消息失败")
            }

            let envelope = try decoder.decode(BffEnvelope<[MessageData]>.self, from: data)
            if envelope.success {
                return envelope.data ?? []
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询会话消息失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<ConversationMessagesData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询会话消息失败")
        }
        if let envelope = try? decoder.decode(BffEnvelope<[MessageData]>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询会话消息失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchRedPacketHistory(
        userId: Int64,
        direction: String,
        year: Int,
        limit: Int = 100
    ) async throws -> RedPacketHistoryResponseData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let trimmedDirection = direction.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        let normalizedDirection = trimmedDirection.isEmpty ? "SENT" : trimmedDirection
        let normalizedLimit = max(1, min(limit, 200))

        var components = URLComponents()
        components.queryItems = [
            URLQueryItem(name: "userId", value: String(userId)),
            URLQueryItem(name: "direction", value: normalizedDirection),
            URLQueryItem(name: "year", value: String(year)),
            URLQueryItem(name: "limit", value: String(normalizedLimit))
        ]
        let query = components.percentEncodedQuery ?? ""

        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/messages/red-packets/history?\(query)",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<RedPacketHistoryResponseData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询红包记录失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<RedPacketHistoryResponseData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询红包记录失败")
        }
        throw APIClientError.decodeFailed
    }

    func sendTextMessage(
        senderUserId: Int64,
        receiverUserId: Int64,
        contentText: String,
        extPayload: String? = nil
    ) async throws -> MessageData {
        guard senderUserId > 0 else {
            throw APIClientError.businessError(message: "发送方无效")
        }
        guard receiverUserId > 0 else {
            throw APIClientError.businessError(message: "接收方无效")
        }
        let trimmedText = contentText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedText.isEmpty else {
            throw APIClientError.businessError(message: "消息内容不能为空")
        }

        var payload: [String: Any] = [
            "senderUserId": String(senderUserId),
            "receiverUserId": String(receiverUserId),
            "contentText": trimmedText
        ]
        if let extPayload {
            let trimmed = extPayload.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                payload["extPayload"] = trimmed
            }
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/messages/text",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<MessageData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "发送消息失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<MessageData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "发送消息失败")
        }
        throw APIClientError.decodeFailed
    }

    func sendImageMessage(
        senderUserId: Int64,
        receiverUserId: Int64,
        mediaId: String,
        extPayload: String? = nil
    ) async throws -> MessageData {
        guard senderUserId > 0 else {
            throw APIClientError.businessError(message: "发送方无效")
        }
        guard receiverUserId > 0 else {
            throw APIClientError.businessError(message: "接收方无效")
        }
        let trimmedMediaId = mediaId.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedMediaId.isEmpty else {
            throw APIClientError.businessError(message: "图片资源无效")
        }

        var payload: [String: Any] = [
            "senderUserId": String(senderUserId),
            "receiverUserId": String(receiverUserId),
            "mediaId": trimmedMediaId
        ]
        if let extPayload {
            let trimmed = extPayload.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                payload["extPayload"] = trimmed
            }
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/messages/image",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<MessageData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "发送图片失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<MessageData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "发送图片失败")
        }
        throw APIClientError.decodeFailed
    }

    func sendTransferMessage(
        senderUserId: Int64,
        receiverUserId: Int64,
        amount: Decimal,
        paymentMethod: String = "WALLET",
        paymentToolCode: String? = nil,
        remark: String? = nil,
        extPayload: String? = nil
    ) async throws -> MessageData {
        guard senderUserId > 0 else {
            throw APIClientError.businessError(message: "发送方无效")
        }
        guard receiverUserId > 0 else {
            throw APIClientError.businessError(message: "接收方无效")
        }
        guard amount > 0 else {
            throw APIClientError.businessError(message: "转账金额必须大于0")
        }

        let normalizedPaymentMethod: String = {
            let trimmed = paymentMethod.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
            if trimmed == "BANK_CARD" || trimmed.hasPrefix("BANK_CARD:") {
                return "BANK_CARD"
            }
            if trimmed == "FUND_ACCOUNT" || trimmed == "FUND" || trimmed == "AICASH" || trimmed == "AICASH" {
                return "FUND_ACCOUNT"
            }
            return "WALLET"
        }()
        let amountText = String(format: "%.2f", NSDecimalNumber(decimal: amount).doubleValue)

        var payload: [String: Any] = [
            "senderUserId": String(senderUserId),
            "receiverUserId": String(receiverUserId),
            "amount": amountText,
            "paymentMethod": normalizedPaymentMethod
        ]
        if let paymentToolCode {
            let trimmed = paymentToolCode.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                payload["paymentToolCode"] = trimmed
            }
        }
        if let remark {
            let trimmed = remark.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                payload["remark"] = trimmed
            }
        }
        if let extPayload {
            let trimmed = extPayload.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                payload["extPayload"] = trimmed
            }
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/messages/transfer",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<MessageData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "发送转账失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<MessageData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "发送转账失败")
        }
        throw APIClientError.decodeFailed
    }

    func sendRedPacketMessage(
        senderUserId: Int64,
        receiverUserId: Int64,
        amount: Decimal,
        paymentMethod: String = "WALLET",
        extPayload: String? = nil
    ) async throws -> MessageData {
        guard senderUserId > 0 else {
            throw APIClientError.businessError(message: "发送方无效")
        }
        guard receiverUserId > 0 else {
            throw APIClientError.businessError(message: "接收方无效")
        }
        guard amount > 0 else {
            throw APIClientError.businessError(message: "红包金额必须大于0")
        }

        let normalizedPaymentMethod: String = {
            let trimmed = paymentMethod.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
            if trimmed == "BANK_CARD" || trimmed.hasPrefix("BANK_CARD:") {
                return "BANK_CARD"
            }
            if trimmed == "FUND_ACCOUNT" || trimmed == "FUND" || trimmed == "AICASH" || trimmed == "AICASH" {
                return "FUND_ACCOUNT"
            }
            return "WALLET"
        }()
        let amountText = String(format: "%.2f", NSDecimalNumber(decimal: amount).doubleValue)
        let bffPath = "/bff/messages/red-packet"

        var payload: [String: Any] = [
            "senderUserId": String(senderUserId),
            "receiverUserId": String(receiverUserId),
            // 后端 Money 反序列化支持直接传字符串金额，默认币种为 CNY。
            "amount": amountText,
            "paymentMethod": normalizedPaymentMethod
        ]
        if let extPayload {
            let trimmed = extPayload.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                payload["extPayload"] = trimmed
            }
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let decoder = JSONDecoder()
        let effectiveResult = try await requestBffDetailed(
            bffPath: bffPath,
            method: "POST",
            body: requestBody
        )

        if effectiveResult.httpResponse.statusCode >= 200 && effectiveResult.httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<MessageData>.self, from: effectiveResult.data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "发送红包失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<MessageData>.self, from: effectiveResult.data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "发送红包失败")
        }
        throw APIClientError.decodeFailed
    }

    func fetchRedPacketDetail(
        redPacketNo: String,
        userId: Int64
    ) async throws -> RedPacketDetailData {
        let trimmedRedPacketNo = redPacketNo.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedRedPacketNo.isEmpty else {
            throw APIClientError.businessError(message: "红包单号无效")
        }
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        guard let encoded = trimmedRedPacketNo.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) else {
            throw APIClientError.invalidURL
        }
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/messages/red-packets/\(encoded)?userId=\(userId)",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<RedPacketDetailData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询红包详情失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<RedPacketDetailData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "查询红包详情失败")
        }
        throw APIClientError.decodeFailed
    }

    func claimRedPacket(
        redPacketNo: String,
        userId: Int64
    ) async throws -> RedPacketDetailData {
        let trimmedRedPacketNo = redPacketNo.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedRedPacketNo.isEmpty else {
            throw APIClientError.businessError(message: "红包单号无效")
        }
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        guard let encoded = trimmedRedPacketNo.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) else {
            throw APIClientError.invalidURL
        }
        let body = try JSONSerialization.data(withJSONObject: [
            "userId": String(userId)
        ])
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/messages/red-packets/\(encoded)/claim",
            method: "POST",
            body: body
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<RedPacketDetailData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "领取红包失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<RedPacketDetailData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "领取红包失败")
        }
        throw APIClientError.decodeFailed
    }


    func checkAppVersion(appCode: String, currentVersionNo: String?, deviceId: String?) async throws -> AppVersionCheckData {
        let normalizedAppCode = appCode.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedAppCode.isEmpty else {
            throw APIClientError.businessError(message: "appCode 不能为空")
        }

        var queryItems: [String] = []
        if let currentVersionNo {
            let normalizedVersion = currentVersionNo.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalizedVersion.isEmpty,
               let encoded = normalizedVersion.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
                queryItems.append("currentVersionNo=\(encoded)")
            }
        }
        if let deviceId {
            let normalizedDeviceId = deviceId.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalizedDeviceId.isEmpty,
               let encoded = normalizedDeviceId.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) {
                queryItems.append("deviceId=\(encoded)")
            }
        }
        let suffix = queryItems.isEmpty ? "" : "?" + queryItems.joined(separator: "&")
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/apps/\(normalizedAppCode)/versions/check\(suffix)",
            method: "GET",
            body: nil
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<AppVersionCheckData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "版本检查失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<AppVersionCheckData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "版本检查失败")
        }
        throw APIClientError.decodeFailed
    }

    func reportAppDevice(
        deviceId: String,
        appCode: String,
        clientIds: [String],
        deviceBrand: String?,
        osVersion: String?,
        currentVersionCode: String?,
        currentVersionNo: String?,
        started: Bool
    ) async throws -> AppDeviceReportData {
        var payload: [String: Any] = [
            "deviceId": deviceId,
            "appCode": appCode,
            "clientIds": clientIds,
            "started": started,
        ]
        if let deviceBrand {
            let normalized = deviceBrand.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["deviceBrand"] = normalized
            }
        }
        if let osVersion {
            let normalized = osVersion.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["osVersion"] = normalized
            }
        }
        if let currentVersionCode {
            let normalized = currentVersionCode.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["currentVersionCode"] = normalized
            }
        }
        if let currentVersionNo {
            let normalized = currentVersionNo.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["currentVersionNo"] = normalized
            }
        }
        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/apps/devices",
            method: "POST",
            body: requestBody,
            skipVisitTracking: true
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<AppDeviceReportData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "设备上报失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<AppDeviceReportData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "设备上报失败")
        }
        throw APIClientError.decodeFailed
    }

    func recordAppBehaviorEvent(
        eventName: String,
        eventType: String?,
        eventCode: String?,
        pageName: String?,
        actionName: String?,
        resultSummary: String?,
        durationMs: Int64?,
        payloadJson: String? = nil,
        eventAtEpochMs: Int64? = nil,
        loginDurationMs: Int64? = nil
    ) async throws -> AppBehaviorEventData {
        let normalizedEventName = eventName.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedEventName.isEmpty else {
            throw APIClientError.businessError(message: "eventName 不能为空")
        }

        let screenBounds = await MainActor.run { UIScreen.main.bounds }
        let screenWidth = max(Int(screenBounds.width.rounded()), 0)
        let screenHeight = max(Int(screenBounds.height.rounded()), 0)
        let viewportSize = await currentViewportSize()

        var payload: [String: Any] = [
            "eventId": "ev-\(UUID().uuidString.lowercased().replacingOccurrences(of: "-", with: ""))",
            "sessionId": AppRuntime.appSessionId,
            "appCode": AppRuntime.appCode,
            "eventName": normalizedEventName,
            "deviceId": AppRuntime.deviceId,
            "clientId": AppRuntime.clientId,
            "screenWidth": screenWidth,
            "screenHeight": screenHeight,
            "viewportWidth": viewportSize.width,
            "viewportHeight": viewportSize.height,
            "eventAtEpochMs": eventAtEpochMs ?? Int64(Date().timeIntervalSince1970 * 1000)
        ]
        assignOptionalPayloadText(&payload, key: "eventType", value: eventType)
        assignOptionalPayloadText(&payload, key: "eventCode", value: eventCode)
        assignOptionalPayloadText(&payload, key: "pageName", value: pageName)
        assignOptionalPayloadText(&payload, key: "actionName", value: actionName)
        assignOptionalPayloadText(&payload, key: "resultStatus", value: resultSummary)
        assignOptionalPayloadText(&payload, key: "networkType", value: NetworkContextProvider.shared.networkType)
        assignOptionalPayloadText(&payload, key: "appVersionNo", value: AppRuntime.currentVersionNo)
        assignOptionalPayloadText(&payload, key: "appBuildNo", value: AppRuntime.appBuildNo)
        assignOptionalPayloadText(&payload, key: "deviceBrand", value: AppRuntime.deviceBrand)
        assignOptionalPayloadText(&payload, key: "deviceModel", value: AppRuntime.deviceModel)
        assignOptionalPayloadText(&payload, key: "deviceName", value: AppRuntime.deviceName)
        assignOptionalPayloadText(&payload, key: "deviceType", value: AppRuntime.deviceType)
        assignOptionalPayloadText(&payload, key: "osName", value: AppRuntime.osName)
        assignOptionalPayloadText(&payload, key: "osVersion", value: AppRuntime.osVersion)
        assignOptionalPayloadText(&payload, key: "locale", value: AppRuntime.localeIdentifier)
        assignOptionalPayloadText(&payload, key: "timezone", value: AppRuntime.timezoneIdentifier)
        assignOptionalPayloadText(&payload, key: "language", value: AppRuntime.languageIdentifier)
        assignOptionalPayloadText(&payload, key: "countryCode", value: AppRuntime.countryCode)
        if let durationMs, durationMs >= 0 {
            payload["durationMs"] = durationMs
        }
        if let loginDurationMs, loginDurationMs >= 0 {
            payload["loginDurationMs"] = loginDurationMs
        }
        assignOptionalPayloadText(&payload, key: "payloadJson", value: payloadJson)

        if let session = authStore.loadSession(), session.user.userId > 0 {
            payload["userId"] = session.user.userId
            assignOptionalPayloadText(&payload, key: "aipayUid", value: session.user.aipayUid)
            assignOptionalPayloadText(&payload, key: "loginId", value: session.user.loginId)
            assignOptionalPayloadText(&payload, key: "accountStatus", value: session.user.accountStatus)
            assignOptionalPayloadText(&payload, key: "kycLevel", value: session.user.kycLevel)
            assignOptionalPayloadText(&payload, key: "nickname", value: session.user.nickname)
            assignOptionalPayloadText(&payload, key: "mobile", value: session.user.mobile)
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/apps/behavior-events",
            method: "POST",
            body: requestBody,
            skipVisitTracking: true
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<AppBehaviorEventData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "行为埋点上报失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<AppBehaviorEventData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "行为埋点上报失败")
        }
        throw APIClientError.decodeFailed
    }

    func recordAppVisit(
        deviceId: String,
        appCode: String,
        clientId: String?,
        ipAddress: String?,
        locationInfo: String?,
        tenantCode: String?,
        networkType: String?,
        currentVersionCode: String?,
        currentVersionNo: String?,
        deviceBrand: String?,
        osVersion: String?,
        apiName: String,
        requestParamsText: String?,
        resultSummary: String?,
        durationMs: Int64?
    ) async throws -> AppVisitRecordData {
        var payload: [String: Any] = [
            "deviceId": deviceId,
            "appCode": appCode,
            "apiName": apiName,
        ]
        if let clientId {
            let normalized = clientId.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["clientId"] = normalized
            }
        }
        if let ipAddress {
            let normalized = ipAddress.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["ipAddress"] = normalized
            }
        }
        if let locationInfo {
            let normalized = locationInfo.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["locationInfo"] = normalized
            }
        }
        if let tenantCode {
            let normalized = tenantCode.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["tenantCode"] = normalized
            }
        }
        if let networkType {
            let normalized = networkType.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["networkType"] = normalized
            }
        }
        if let currentVersionCode {
            let normalized = currentVersionCode.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["currentVersionCode"] = normalized
            }
        }
        if let currentVersionNo {
            let normalized = currentVersionNo.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["currentVersionNo"] = normalized
            }
        }
        if let deviceBrand {
            let normalized = deviceBrand.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["deviceBrand"] = normalized
            }
        }
        if let osVersion {
            let normalized = osVersion.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["osVersion"] = normalized
            }
        }
        if let requestParamsText {
            let normalized = requestParamsText.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["requestParamsText"] = normalized
            }
        }
        if let resultSummary {
            let normalized = resultSummary.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalized.isEmpty {
                payload["resultSummary"] = normalized
            }
        }
        if let durationMs {
            payload["durationMs"] = durationMs
        }
        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/apps/visit-records",
            method: "POST",
            body: requestBody,
            skipVisitTracking: true
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<AppVisitRecordData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return payload
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "访问记录上报失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<AppVisitRecordData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "访问记录上报失败")
        }
        throw APIClientError.decodeFailed
    }

    func submitFeedbackTicket(
        userId: Int64,
        content: String,
        contactMobile: String?,
        attachmentUrls: [String],
        feedbackType: String = "PRODUCT_SUGGESTION",
        sourceChannel: String = "IOS_APP",
        sourcePageCode: String = "SETTINGS_PRODUCT_SUGGESTION",
        title: String? = "反馈与投诉"
    ) async throws -> FeedbackTicketData {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }

        let normalizedContent = content.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalizedContent.isEmpty else {
            throw APIClientError.businessError(message: "问题描述不能为空")
        }

        var payload: [String: Any] = [
            "userId": String(userId),
            "feedbackType": feedbackType,
            "sourceChannel": sourceChannel,
            "sourcePageCode": sourcePageCode,
            "content": normalizedContent
        ]
        if let title {
            let normalizedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalizedTitle.isEmpty {
                payload["title"] = normalizedTitle
            }
        }
        if let contactMobile {
            let normalizedMobile = contactMobile.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalizedMobile.isEmpty {
                payload["contactMobile"] = normalizedMobile
            }
        }
        if !attachmentUrls.isEmpty {
            payload["attachmentUrls"] = attachmentUrls
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/feedback/tickets",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        let extractString: (Any?) -> String? = { raw in
            if let value = raw as? String {
                let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
                return trimmed.isEmpty ? nil : trimmed
            }
            if let value = raw as? NSNumber {
                let text = value.stringValue.trimmingCharacters(in: .whitespacesAndNewlines)
                return text.isEmpty ? nil : text
            }
            return nil
        }
        let extractInt64: (Any?) -> Int64? = { raw in
            if let value = raw as? Int64 {
                return value
            }
            if let value = raw as? Int {
                return Int64(value)
            }
            if let value = raw as? Double {
                return Int64(value)
            }
            if let value = raw as? NSNumber {
                return value.int64Value
            }
            if let value = raw as? String {
                return Int64(value.trimmingCharacters(in: .whitespacesAndNewlines))
            }
            return nil
        }
        let extractStringArray: (Any?) -> [String] = { raw in
            guard let array = raw as? [Any] else {
                return []
            }
            return array.compactMap { extractString($0) }
        }
        let extractErrorMessage: ([String: Any]) -> String? = { envelope in
            if let errorObj = envelope["error"] as? [String: Any] {
                return extractString(errorObj["message"])
                    ?? extractString(errorObj["msg"])
                    ?? extractString(errorObj["error"])
            }
            return extractString(envelope["message"])
                ?? extractString(envelope["msg"])
                ?? extractString(envelope["error"])
        }
        let fallbackFeedbackData: ([String: Any]?) -> FeedbackTicketData = { payload in
            let now = ISO8601DateFormatter().string(from: Date())
            return FeedbackTicketData(
                feedbackNo: extractString(payload?["feedbackNo"]) ?? "FB-\(Int(Date().timeIntervalSince1970))",
                userId: extractInt64(payload?["userId"]) ?? userId,
                feedbackType: extractString(payload?["feedbackType"]) ?? feedbackType,
                sourceChannel: extractString(payload?["sourceChannel"]) ?? sourceChannel,
                sourcePageCode: extractString(payload?["sourcePageCode"]) ?? sourcePageCode,
                title: extractString(payload?["title"]) ?? title,
                content: extractString(payload?["content"]) ?? normalizedContent,
                contactMobile: extractString(payload?["contactMobile"]) ?? contactMobile,
                attachmentUrls: {
                    let values = extractStringArray(payload?["attachmentUrls"])
                    return values.isEmpty ? attachmentUrls : values
                }(),
                status: extractString(payload?["status"]) ?? "PENDING",
                handledBy: extractString(payload?["handledBy"]),
                handleNote: extractString(payload?["handleNote"]),
                handledAt: extractString(payload?["handledAt"]),
                closedAt: extractString(payload?["closedAt"]),
                createdAt: extractString(payload?["createdAt"]) ?? now,
                updatedAt: extractString(payload?["updatedAt"]) ?? now
            )
        }

        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            if let envelope = try? decoder.decode(BffEnvelope<FeedbackTicketData>.self, from: data),
               envelope.success,
               let payload = envelope.data {
                return payload
            }
            if let object = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                let success = object["success"] as? Bool
                if success == false {
                    throw APIClientError.businessError(message: extractErrorMessage(object) ?? "提交反馈失败")
                }
                if let payload = object["data"] as? [String: Any] {
                    return fallbackFeedbackData(payload)
                }
                if success == true, object["data"] is NSNull || object["data"] == nil {
                    return fallbackFeedbackData(nil)
                }
                if success == nil {
                    // Some BFF paths may return plain ticket objects directly.
                    return fallbackFeedbackData(object)
                }
            }
            throw APIClientError.decodeFailed
        }

        if let envelope = try? decoder.decode(BffEnvelope<FeedbackTicketData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "提交反馈失败")
        }
        if let object = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
            throw APIClientError.businessError(message: extractErrorMessage(object) ?? "提交反馈失败")
        }
        throw APIClientError.decodeFailed
    }

    func uploadImage(
        ownerUserId: Int64,
        imageData: Data,
        fileName: String = "ios-chat.jpg",
        mimeType: String = "image/jpeg"
    ) async throws -> MediaAssetData {
        guard ownerUserId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        guard !imageData.isEmpty else {
            throw APIClientError.businessError(message: "图片内容为空")
        }

        let boundary = "Boundary-\(UUID().uuidString)"
        let requestBody = makeImageUploadBody(
            boundary: boundary,
            ownerUserId: ownerUserId,
            imageData: imageData,
            fileName: fileName,
            mimeType: mimeType
        )
        let bffPath = "/bff/media/images/upload?ownerUserId=\(ownerUserId)"
        let (data, httpResponse) = try await requestMultipartBff(
            bffPath: bffPath,
            body: requestBody,
            boundary: boundary
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<MediaAssetData>.self, from: data)
            if envelope.success, let payload = envelope.data {
                return normalizedUploadedMediaAsset(payload)
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "上传图片失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<MediaAssetData>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "上传图片失败")
        }
        if let text = String(data: data, encoding: .utf8)?
            .trimmingCharacters(in: .whitespacesAndNewlines),
           !text.isEmpty {
            throw APIClientError.businessError(message: String(text.prefix(180)))
        }
        throw APIClientError.decodeFailed
    }

    func mediaContentURL(mediaId: String) -> URL? {
        let trimmed = mediaId.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty,
              let encodedMediaId = trimmed.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed)
        else {
            return nil
        }
        return makeBffURL(path: "/bff/media/\(encodedMediaId)/content")
    }

    private func makeImageUploadBody(
        boundary: String,
        ownerUserId: Int64,
        imageData: Data,
        fileName: String,
        mimeType: String
    ) -> Data {
        var data = Data()
        data.append("--\(boundary)\r\n")
        data.append("Content-Disposition: form-data; name=\"ownerUserId\"\r\n\r\n")
        data.append("\(ownerUserId)\r\n")
        data.append("--\(boundary)\r\n")
        data.append("Content-Disposition: form-data; name=\"file\"; filename=\"\(fileName)\"\r\n")
        data.append("Content-Type: \(mimeType)\r\n\r\n")
        data.append(imageData)
        data.append("\r\n")
        data.append("--\(boundary)--\r\n")
        return data
    }

    private func shouldFallbackUpload(_ error: Error) -> Bool {
        if shouldFallback(error) {
            return true
        }
        guard case let APIClientError.businessError(message) = error else {
            return false
        }
        let normalized = message.lowercased()
        let isOwnerParamMissing = normalized.contains("required request parameter")
            && normalized.contains("owneruser")
        let isFilePartMissing = normalized.contains("required part")
            && normalized.contains("file")
        return isOwnerParamMissing || isFilePartMissing
    }

    private func requestMultipartBff(
        bffPath: String,
        body: Data,
        boundary: String,
        skipVisitTracking: Bool = false
    ) async throws -> (Data, HTTPURLResponse) {
        guard let bffURL = makeBffURL(path: bffPath) else {
            throw APIClientError.invalidURL
        }

        let startedAt = Date()
        do {
            let result = try await executeMultipartRequest(url: bffURL, body: body, boundary: boundary, skipVisitTracking: true)
            if !skipVisitTracking {
                scheduleVisitTracking(apiName: bffPath, method: "POST", body: nil, httpResponse: result.1, error: nil, startedAt: startedAt)
            }
            return result
        } catch {
            if shouldFallbackUpload(error) {
                let finalError = APIClientError.businessError(message: "无法连接服务器，请稍后重试")
                if !skipVisitTracking {
                    scheduleVisitTracking(apiName: bffPath, method: "POST", body: nil, httpResponse: nil, error: finalError, startedAt: startedAt)
                }
                throw finalError
            }
            if !skipVisitTracking {
                scheduleVisitTracking(apiName: bffPath, method: "POST", body: nil, httpResponse: nil, error: error, startedAt: startedAt)
            }
            throw error
        }
    }

    private func executeMultipartRequest(
        url: URL,
        body: Data,
        boundary: String,
        skipVisitTracking: Bool = false
    ) async throws -> (Data, HTTPURLResponse) {
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.timeoutInterval = requestTimeout
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        attachRuntimeHeaders(to: &request)
        if shouldAttachAuthorization(for: url), let authorizationHeader = currentAuthorizationHeader() {
            request.setValue(authorizationHeader, forHTTPHeaderField: "Authorization")
        }
        request.httpBody = body

        let startedAt = Date()
        do {
            let (data, response) = try await session.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse else {
                throw APIClientError.invalidResponse
            }
            if shouldAttemptAuthorizationRecovery(httpResponse: httpResponse, url: url),
               let refreshedAuthorizationHeader = refreshedAuthorizationHeaderIfNeeded(
                   previousAuthorizationHeader: request.value(forHTTPHeaderField: "Authorization")
               ) {
                var retryRequest = request
                retryRequest.setValue(refreshedAuthorizationHeader, forHTTPHeaderField: "Authorization")
                let (retryData, retryResponse) = try await session.data(for: retryRequest)
                guard let retryHttpResponse = retryResponse as? HTTPURLResponse else {
                    throw APIClientError.invalidResponse
                }
                if !skipVisitTracking {
                    scheduleVisitTracking(
                        apiName: normalizedTrackedApiName(url: url),
                        method: "POST",
                        body: nil,
                        httpResponse: retryHttpResponse,
                        error: nil,
                        startedAt: startedAt
                    )
                }
                return (retryData, retryHttpResponse)
            }
            if !skipVisitTracking {
                scheduleVisitTracking(apiName: normalizedTrackedApiName(url: url), method: "POST", body: nil, httpResponse: httpResponse, error: nil, startedAt: startedAt)
            }
            return (data, httpResponse)
        } catch {
            if !skipVisitTracking {
                scheduleVisitTracking(apiName: normalizedTrackedApiName(url: url), method: "POST", body: nil, httpResponse: nil, error: error, startedAt: startedAt)
            }
            throw error
        }
    }

    private func attachRuntimeHeaders(to request: inout URLRequest) {
        request.setValue(AppRuntime.deviceId, forHTTPHeaderField: "X-Device-Id")
        let legacyDeviceIds = AppRuntime.legacyDeviceIds
        if legacyDeviceIds.isEmpty {
            request.setValue(nil, forHTTPHeaderField: "X-Legacy-Device-Ids")
            return
        }
        request.setValue(legacyDeviceIds.joined(separator: ","), forHTTPHeaderField: "X-Legacy-Device-Ids")
    }

    private func normalizedUploadedMediaAsset(_ asset: MediaAssetData) -> MediaAssetData {
        let normalizedContentUrl = mediaContentURL(mediaId: asset.mediaId)?.absoluteString
            ?? asset.contentUrl?.trimmingCharacters(in: .whitespacesAndNewlines)
        return MediaAssetData(
            mediaId: asset.mediaId,
            ownerUserId: asset.ownerUserId,
            mediaType: asset.mediaType,
            originalName: asset.originalName,
            mimeType: asset.mimeType,
            sizeBytes: asset.sizeBytes,
            compressedSizeBytes: asset.compressedSizeBytes,
            width: asset.width,
            height: asset.height,
            contentUrl: normalizedContentUrl,
            createdAt: asset.createdAt
        )
    }

    func markConversationRead(
        userId: Int64,
        conversationNo: String,
        lastReadMessageId: String?
    ) async throws {
        guard userId > 0 else {
            throw APIClientError.businessError(message: "用户标识无效")
        }
        let trimmedConversationNo = conversationNo.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedConversationNo.isEmpty else {
            throw APIClientError.businessError(message: "会话号无效")
        }

        var payload: [String: Any] = [
            "userId": String(userId),
            "conversationNo": trimmedConversationNo
        ]
        if let lastReadMessageId {
            let trimmed = lastReadMessageId.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty {
                payload["lastReadMessageId"] = trimmed
            }
        }

        let requestBody = try JSONSerialization.data(withJSONObject: payload)
        let (data, httpResponse) = try await requestBff(
            bffPath: "/bff/messages/conversations/read",
            method: "POST",
            body: requestBody
        )

        let decoder = JSONDecoder()
        if httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 {
            let envelope = try decoder.decode(BffEnvelope<EmptyPayload>.self, from: data)
            if envelope.success {
                return
            }
            throw APIClientError.businessError(message: envelope.error?.message ?? "标记已读失败")
        }

        if let envelope = try? decoder.decode(BffEnvelope<EmptyPayload>.self, from: data) {
            throw APIClientError.businessError(message: envelope.error?.message ?? "标记已读失败")
        }
        throw APIClientError.decodeFailed
    }
}

private struct EmptyPayload: Codable {}

private extension Data {
    mutating func append(_ string: String) {
        guard let data = string.data(using: .utf8) else {
            return
        }
        append(data)
    }
}
