import SwiftUI
import UIKit
import CoreImage
import CryptoKit

private struct PaymentCodeMethodOption: Identifiable {
    let id: String
    let channelKind: CashierChannelKind
    let title: String
    let subtitle: String
    let bankTool: CashierPayToolData?

    static let fallbackWallet = PaymentCodeMethodOption(
        id: CashierChannelKind.wallet.rawValue,
        channelKind: .wallet,
        title: CashierChannelKind.wallet.defaultTitle,
        subtitle: "可用--",
        bankTool: nil
    )

    static let fixedAiCredit = PaymentCodeMethodOption(
        id: CashierChannelKind.aiCredit.rawValue,
        channelKind: .aiCredit,
        title: CashierChannelKind.aiCredit.defaultTitle,
        subtitle: "爱花优先付款",
        bankTool: nil
    )

    var codeSeed: String {
        let bankCodeSeed = bankTool?.toolCode.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if !bankCodeSeed.isEmpty {
            return bankCodeSeed
        }
        return channelKind.rawValue.uppercased()
    }

    var paymentRowTitle: String {
        let trimmed = title.trimmingCharacters(in: .whitespacesAndNewlines)
        if !trimmed.isEmpty {
            return trimmed
        }
        return channelKind.defaultTitle
    }

    var paymentRowSubtitle: String {
        "优先使用此付款方式"
    }
}

private enum PaymentCodePreferenceStore {
    private static func key(for userId: Int64?) -> String? {
        guard let userId, userId > 0 else {
            return nil
        }
        return "cn.openaipay.payment_code.preferred_method.\(userId)"
    }

    static func loadPreferredMethodId(userId: Int64?) -> String? {
        guard let key = key(for: userId) else {
            return nil
        }
        let stored = UserDefaults.standard.string(forKey: key)?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return stored.isEmpty ? nil : stored
    }

    static func savePreferredMethodId(_ methodId: String?, userId: Int64?) {
        guard let key = key(for: userId) else {
            return
        }
        let trimmed = methodId?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if trimmed.isEmpty {
            UserDefaults.standard.removeObject(forKey: key)
        } else {
            UserDefaults.standard.set(trimmed, forKey: key)
        }
    }
}

private enum PaymentCodeMethodCatalog {
    private static let paySceneFallback = CashierSceneConfiguration(
        supportedChannels: [.wallet, .fund, .bankCard],
        bankCardPolicy: .allCards,
        emptyBankCardText: "暂无可用银行卡"
    )

    static func loadOptions(userId: Int64?) async -> [PaymentCodeMethodOption] {
        guard let userId, userId > 0 else {
            return [PaymentCodeMethodOption.fallbackWallet]
        }

        let walletOverview = try? await APIClient.shared.fetchAssetOverview(userId: String(userId))
        let fundAccount = try? await APIClient.shared.fetchFundAccount(userId: userId)
        let creditAccount = try? await APIClient.shared.fetchCreditAccount(userId: userId)
        let cashierView = try? await APIClient.shared.fetchCashierView(userId: userId, sceneCode: "PAY")

        return buildOptions(
            walletOverview: walletOverview,
            fundAccount: fundAccount,
            creditAccount: creditAccount,
            cashierView: cashierView
        )
    }

    static func resolvePreferredMethodId(
        in options: [PaymentCodeMethodOption],
        currentMethodId: String?,
        userId: Int64?
    ) -> String? {
        let normalizedCurrent = currentMethodId?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if !normalizedCurrent.isEmpty,
           options.contains(where: { $0.id == normalizedCurrent }) {
            return normalizedCurrent
        }

        if let storedMethodId = PaymentCodePreferenceStore.loadPreferredMethodId(userId: userId),
           options.contains(where: { $0.id == storedMethodId }) {
            return storedMethodId
        }

        return options.first?.id
    }

    private static func buildOptions(
        walletOverview: AssetOverviewData?,
        fundAccount: FundAccountData?,
        creditAccount: CreditAccountData?,
        cashierView: CashierViewData?
    ) -> [PaymentCodeMethodOption] {
        var options: [PaymentCodeMethodOption] = []

        if let creditAccount,
           creditAccount.totalLimit.amount > .zero,
           creditAccount.accountStatus.trimmingCharacters(in: .whitespacesAndNewlines).uppercased() != "CLOSED" {
            options.append(
                PaymentCodeMethodOption(
                    id: CashierChannelKind.aiCredit.rawValue,
                    channelKind: .aiCredit,
                    title: CashierChannelKind.aiCredit.defaultTitle,
                    subtitle: "可用\(formattedCurrency(creditAccount.availableLimit.amount))",
                    bankTool: nil
                )
            )
        }

        let sceneConfiguration = CashierSceneConfiguration.fromServerData(
            cashierView?.sceneConfig,
            fallback: paySceneFallback
        )
        let bankTools = CashierPresentationHelper.filteredBankTools(
            cashierView?.payTools ?? [],
            policy: sceneConfiguration.bankCardPolicy
        )
        let creditTools = bankTools.filter { CashierPresentationHelper.normalizedCardType(for: $0) == "CREDIT" }
        let debitTools = bankTools.filter { CashierPresentationHelper.normalizedCardType(for: $0) == "DEBIT" }
        let otherTools = bankTools.filter {
            let type = CashierPresentationHelper.normalizedCardType(for: $0)
            return type != "CREDIT" && type != "DEBIT"
        }

        options.append(contentsOf: creditTools.map(bankOption))

        if sceneConfiguration.supports(.wallet) || walletOverview != nil {
            options.append(
                PaymentCodeMethodOption(
                    id: CashierChannelKind.wallet.rawValue,
                    channelKind: .wallet,
                    title: CashierChannelKind.wallet.defaultTitle,
                    subtitle: "可用\(formattedCurrency(amountText: walletOverview?.availableAmount))",
                    bankTool: nil
                )
            )
        }

        if sceneConfiguration.supports(.fund) || fundAccount != nil {
            options.append(
                PaymentCodeMethodOption(
                    id: CashierChannelKind.fund.rawValue,
                    channelKind: .fund,
                    title: CashierChannelKind.fund.defaultTitle,
                    subtitle: "可用\(formattedCurrency(fundAccount?.holdingAmount))",
                    bankTool: nil
                )
            )
        }

        options.append(contentsOf: debitTools.map(bankOption))
        options.append(contentsOf: otherTools.map(bankOption))

        return options.isEmpty ? [PaymentCodeMethodOption.fallbackWallet] : options
    }

    private static func bankOption(_ tool: CashierPayToolData) -> PaymentCodeMethodOption {
        PaymentCodeMethodOption(
            id: tool.toolCode,
            channelKind: .bankCard,
            title: CashierPresentationHelper.bankDisplayName(for: tool, replaceDebitWithSavings: true),
            subtitle: CashierPresentationHelper.bankSubtitle(for: tool, fallbackText: "银行卡"),
            bankTool: tool
        )
    }

    private static func formattedCurrency(_ amount: Decimal?) -> String {
        guard let amount else {
            return "--"
        }
        return formattedCurrencyValue(amount)
    }

    private static func formattedCurrency(amountText: String?) -> String {
        let normalized = amountText?
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: ",", with: "") ?? ""
        guard !normalized.isEmpty,
              let decimal = Decimal(string: normalized, locale: Locale(identifier: "en_US_POSIX")) else {
            return "--"
        }
        return formattedCurrencyValue(decimal)
    }

    private static func formattedCurrencyValue(_ amount: Decimal) -> String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "zh_CN")
        formatter.numberStyle = .currency
        formatter.currencySymbol = "¥"
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.string(from: NSDecimalNumber(decimal: amount)) ?? "¥0.00"
    }
}

private struct AiPayPaymentCodePayload {
    let barcodeDigits: String
    let qrPayload: String
}

private enum AiPayPaymentCodeFactory {
    static func makePayload(
        userId: Int64?,
        selectedMethod: PaymentCodeMethodOption?,
        now: Date = Date()
    ) -> AiPayPaymentCodePayload {
        let resolvedUserId = max(userId ?? 0, 0)
        let trimmedSeed = selectedMethod?.codeSeed.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let methodSeed = trimmedSeed.isEmpty ? CashierChannelKind.wallet.rawValue.uppercased() : trimmedSeed
        let timeWindow = Int64(now.timeIntervalSince1970) / 30
        let signatureSeed = "AIPAY|PAY_CODE|\(resolvedUserId)|\(methodSeed)|\(timeWindow)"
        let digestHex = SHA256.hash(data: Data(signatureSeed.utf8)).map { String(format: "%02x", $0) }.joined()
        let barcodeDigits = numericBarcodeDigits(userId: resolvedUserId, digestHex: digestHex)
        let compactUserId = base36String(for: resolvedUserId)
        let compactWindow = base36String(for: timeWindow)
        let compactMethodCode = compactMethodCode(from: selectedMethod)
        let signature = String(digestHex.prefix(10)).uppercased()
        let qrPayload = ["AP1", compactUserId, compactMethodCode, compactWindow, signature].joined(separator: "|")
        return AiPayPaymentCodePayload(barcodeDigits: barcodeDigits, qrPayload: qrPayload)
    }

    private static func numericBarcodeDigits(userId: Int64, digestHex: String) -> String {
        let normalizedUserId = String(format: "%010lld", llabs(userId) % 10_000_000_000)
        let digestDigits = digestHex.unicodeScalars.map { String(Int($0.value) % 10) }.joined()
        let combined = "28" + normalizedUserId + digestDigits + "0"
        return String(combined.prefix(18))
    }

    private static func compactMethodCode(from selectedMethod: PaymentCodeMethodOption?) -> String {
        guard let selectedMethod else {
            return "W"
        }
        if selectedMethod.channelKind == .bankCard, let bankTool = selectedMethod.bankTool {
            let normalizedBankCode = normalizedAlphanumeric(bankTool.bankCode).uppercased()
            let bankPrefix = String((normalizedBankCode.isEmpty ? "BANK" : normalizedBankCode).prefix(4))
            let tailDigits = digitsOnly(bankTool.phoneTailNo ?? bankTool.toolCode)
            let tailCode = String((tailDigits.isEmpty ? "0000" : tailDigits).suffix(4))
            return "B\(bankPrefix)\(tailCode)"
        }
        switch selectedMethod.channelKind {
        case .wallet:
            return "W"
        case .fund:
            return "F"
        case .aiCredit:
            return "H"
        case .bankCard:
            return "B"
        }
    }

    private static func base36String(for value: Int64) -> String {
        let charset = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        if value <= 0 {
            return "0"
        }
        var workingValue = value
        var characters: [Character] = []
        while workingValue > 0 {
            characters.append(charset[Int(workingValue % 36)])
            workingValue /= 36
        }
        return String(characters.reversed())
    }

    private static func normalizedAlphanumeric(_ raw: String?) -> String {
        let value = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return value.replacingOccurrences(of: "[^A-Za-z0-9]", with: "", options: .regularExpression)
    }

    private static func digitsOnly(_ raw: String?) -> String {
        let value = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return value.replacingOccurrences(of: "\\D", with: "", options: .regularExpression)
    }
}

enum AiPayCodeImageRenderer {
    private static let context = CIContext(options: [.useSoftwareRenderer: false])

    static func makeBarcodeImage(text: String, targetSize: CGSize) -> UIImage? {
        guard let data = text.data(using: .ascii),
              let filter = CIFilter(name: "CICode128BarcodeGenerator") else {
            return nil
        }
        filter.setValue(data, forKey: "inputMessage")
        filter.setValue(0.0, forKey: "inputQuietSpace")
        guard let outputImage = filter.outputImage else {
            return nil
        }
        let scaleX = max(1, floor(targetSize.width / outputImage.extent.width))
        let scaleY = max(1, floor(targetSize.height / outputImage.extent.height))
        let transformed = outputImage.transformed(by: CGAffineTransform(scaleX: scaleX, y: scaleY))
        guard let cgImage = context.createCGImage(transformed, from: transformed.extent) else {
            return nil
        }
        return UIImage(cgImage: cgImage)
    }

    static func makeQRCodeImage(payload: String, targetSize: CGSize) -> UIImage? {
        guard let data = payload.data(using: .utf8),
              let filter = CIFilter(name: "CIQRCodeGenerator") else {
            return nil
        }
        filter.setValue(data, forKey: "inputMessage")
        filter.setValue("L", forKey: "inputCorrectionLevel")
        guard let outputImage = filter.outputImage else {
            return nil
        }
        let scaleX = max(1, floor(targetSize.width / outputImage.extent.width))
        let scaleY = max(1, floor(targetSize.height / outputImage.extent.height))
        let transformed = outputImage.transformed(by: CGAffineTransform(scaleX: scaleX, y: scaleY))
        guard let cgImage = context.createCGImage(transformed, from: transformed.extent) else {
            return nil
        }
        return UIImage(cgImage: cgImage)
    }
}

private struct PaymentCodeMethodIconView: View {
    let option: PaymentCodeMethodOption
    let size: CGFloat

    var body: some View {
        Group {
            if option.channelKind == .bankCard, let bankTool = option.bankTool {
                BankLogoIconView(
                    bankCode: bankTool.bankCode,
                    bankName: bankTool.toolName,
                    size: size,
                    cornerRadius: size * 0.22,
                    backgroundColor: Color(red: 0.95, green: 0.96, blue: 0.99)
                )
            } else if option.channelKind == .wallet {
                ZStack {
                    RoundedRectangle(cornerRadius: size * 0.22, style: .continuous)
                        .fill(Color.white)

                    Image("ProfileBalanceCustomIcon")
                        .resizable()
                        .interpolation(.high)
                        .scaledToFit()
                        .frame(width: size * 0.86, height: size * 0.86)
                }
            } else {
                CashierChannelIconView(
                    channelKind: option.channelKind,
                    bankTool: option.bankTool,
                    size: size,
                    cornerRadius: size * 0.22
                )
            }
        }
    }
}

private struct PreferredPaymentMethodSelectionSheet: View {
    let options: [PaymentCodeMethodOption]
    let selectedMethodId: String?
    let onClose: () -> Void
    let onSelect: (PaymentCodeMethodOption) -> Void

    private let baseWidth: CGFloat = 402
    private let baseHeight: CGFloat = 874

    var body: some View {
        GeometryReader { proxy in
            let scaleX = proxy.size.width / baseWidth
            let scaleY = proxy.size.height / baseHeight
            let fontScale = min(scaleX, scaleY)

            ZStack(alignment: .bottom) {
                Color.black.opacity(0.20)
                    .ignoresSafeArea()
                    .onTapGesture(perform: onClose)

                VStack(spacing: 0) {
                    HStack(spacing: 0) {
                        Button(action: onClose) {
                            Image(systemName: "xmark")
                                .font(.system(size: 18 * fontScale, weight: .regular))
                                .foregroundStyle(Color(red: 0.60, green: 0.60, blue: 0.62))
                                .frame(width: 42 * scaleX, height: 42 * scaleY)
                                .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .accessibilityIdentifier("payment_code_method_close")

                        Spacer()

                        Text("选择优先付款方式")
                            .font(.system(size: 18 * fontScale, weight: .semibold))
                            .foregroundStyle(Color(red: 0.18, green: 0.19, blue: 0.22))

                        Spacer()

                        Color.clear
                            .frame(width: 42 * scaleX, height: 42 * scaleY)
                    }
                    .padding(.top, 10 * scaleY)
                    .padding(.horizontal, 14 * scaleX)

                    HStack(alignment: .top, spacing: 8 * scaleX) {
                        Image(systemName: "info.circle.fill")
                            .font(.system(size: 14 * fontScale, weight: .regular))
                            .foregroundStyle(Color(red: 0.75, green: 0.76, blue: 0.79))
                            .padding(.top, 1 * scaleY)

                        Text("优先使用所选付款方式付款，如付款失败将尝试使用其他付款方式完成付款")
                            .font(.system(size: 13 * fontScale, weight: .regular))
                            .foregroundStyle(Color(red: 0.62, green: 0.62, blue: 0.65))
                            .fixedSize(horizontal: false, vertical: true)
                        Spacer(minLength: 0)
                    }
                    .padding(.horizontal, 16 * scaleX)
                    .padding(.top, 8 * scaleY)
                    .padding(.bottom, 14 * scaleY)

                    RoundedRectangle(cornerRadius: 16 * fontScale, style: .continuous)
                        .fill(Color.white)
                        .overlay(methodRows(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale))
                        .padding(.horizontal, 12 * scaleX)
                        .padding(.bottom, 16 * scaleY)
                }
                .frame(width: baseWidth * scaleX, height: 542 * scaleY, alignment: .top)
                .background(
                    RoundedRectangle(cornerRadius: 22 * fontScale, style: .continuous)
                        .fill(Color(red: 0.98, green: 0.98, blue: 0.99))
                )
                .clipShape(
                    RoundedRectangle(cornerRadius: 22 * fontScale, style: .continuous)
                )
            }
        }
        .ignoresSafeArea()
    }

    private func methodRows(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                ForEach(Array(options.enumerated()), id: \.element.id) { index, option in
                    Button {
                        onSelect(option)
                    } label: {
                        HStack(spacing: 12 * scaleX) {
                            PaymentCodeMethodIconView(
                                option: option,
                                size: 24 * min(scaleX, scaleY)
                            )
                            .frame(width: 28 * scaleX, height: 28 * scaleY)

                            Text(option.paymentRowTitle)
                                .font(.system(size: 15.5 * fontScale, weight: .regular))
                                .foregroundStyle(Color(red: 0.28, green: 0.29, blue: 0.31))
                                .lineLimit(1)
                                .minimumScaleFactor(0.72)

                            Spacer(minLength: 10 * scaleX)

                            if selectedMethodId == option.id {
                                Image(systemName: "checkmark")
                                    .font(.system(size: 15 * fontScale, weight: .semibold))
                                    .foregroundStyle(AppTheme.palette.brandPrimary)
                            }
                        }
                        .padding(.horizontal, 16 * scaleX)
                        .frame(height: 54 * scaleY)
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    .accessibilityIdentifier("payment_code_method_row_\(option.id)")

                    if index != options.count - 1 {
                        Divider()
                            .padding(.leading, 54 * scaleX)
                    }
                }
            }
        }
    }
}

struct PaymentCodeView: View {
    let userId: Int64?
    let onBack: () -> Void
    let onOpenReceive: () -> Void
    let onOpenTransfer: () -> Void

    @State private var methodOptions: [PaymentCodeMethodOption] = [.fixedAiCredit]
    @State private var selectedMethodId: String? = CashierChannelKind.aiCredit.rawValue
    @State private var codeTimestamp = Date()
    @State private var showsBarcodeDigits = false

    private let baseWidth: CGFloat = 402
    private let baseHeight: CGFloat = 874

    private var selectedMethod: PaymentCodeMethodOption {
        methodOptions.first(where: { $0.id == selectedMethodId })
            ?? methodOptions.first
            ?? PaymentCodeMethodOption.fixedAiCredit
    }

    private var paymentPayload: AiPayPaymentCodePayload {
        AiPayPaymentCodeFactory.makePayload(
            userId: userId,
            selectedMethod: selectedMethod,
            now: codeTimestamp
        )
    }

    var body: some View {
        GeometryReader { proxy in
            let scaleX = proxy.size.width / baseWidth
            let scaleY = proxy.size.height / baseHeight
            let fontScale = min(scaleX, scaleY)

            ZStack {
                payCodeMainView(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
            }
            .frame(width: proxy.size.width, height: proxy.size.height)
        }
        .ignoresSafeArea()
    }

    private func payCodeMainView(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        ZStack {
            Image("PaymentCodeReference")
                .resizable()
                .interpolation(.high)
                .aspectRatio(contentMode: .fill)
                .frame(width: baseWidth * scaleX, height: baseHeight * scaleY)
                .clipped()

            paymentReferenceOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)

            backButton(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                .position(x: 18 * scaleX, y: 69 * scaleY)

            Button(action: onOpenReceive) {
                Color.clear
                    .frame(width: 350 * scaleX, height: 58 * scaleY)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityIdentifier("payment_code_receive_entry")
            .position(x: 201 * scaleX, y: 582 * scaleY)

            Button(action: onOpenTransfer) {
                Color.clear
                    .frame(width: 350 * scaleX, height: 44 * scaleY)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityIdentifier("payment_code_transfer_entry")
            .position(x: 201 * scaleX, y: 648 * scaleY)
        }
    }

    private func paymentReferenceOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        let selectedMethodSubtitle = selectedMethod.paymentRowSubtitle
        let barcodeImage = AiPayCodeImageRenderer.makeBarcodeImage(
            text: paymentPayload.barcodeDigits,
            targetSize: CGSize(width: 334, height: 78)
        )
        let qrCodeSize: CGFloat = 170 * 1.2
        let qrImage = AiPayCodeImageRenderer.makeQRCodeImage(
            payload: paymentPayload.qrPayload,
            targetSize: CGSize(width: qrCodeSize, height: qrCodeSize)
        )

        return ZStack {
            if let barcodeImage {
                Image(uiImage: barcodeImage)
                    .resizable()
                    .interpolation(.none)
                    .scaledToFill()
                    .frame(width: 334 * scaleX, height: 78 * scaleY)
                    .clipped()
                    .position(x: 201 * scaleX, y: 205 * scaleY)
            }

            Button {
                withAnimation(.easeInOut(duration: 0.18)) {
                    showsBarcodeDigits.toggle()
                }
            } label: {
                Color.clear
                    .frame(width: 220 * scaleX, height: 24 * scaleY)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityIdentifier("payment_code_toggle_digits")
            .position(x: 201 * scaleX, y: 264 * scaleY)

            Text(showsBarcodeDigits ? paymentPayload.barcodeDigits : "点击可查看付款码数字")
                .font(showsBarcodeDigits
                    ? .system(size: 15 * fontScale, weight: .medium, design: .monospaced)
                    : .system(size: 13 * fontScale, weight: .regular)
                )
                .foregroundStyle(showsBarcodeDigits
                    ? Color(red: 0.23, green: 0.24, blue: 0.27)
                    : Color(red: 0.60, green: 0.60, blue: 0.62)
                )
                .lineLimit(1)
                .minimumScaleFactor(0.72)
                .frame(width: 220 * scaleX)
                .position(x: 201 * scaleX, y: 264 * scaleY)

            if let qrImage {
                Image(uiImage: qrImage)
                    .resizable()
                    .interpolation(.none)
                    .scaledToFit()
                    .frame(width: qrCodeSize * scaleX, height: qrCodeSize * scaleY)
                    .position(x: 201 * scaleX, y: 386 * scaleY)
            }

            Image("PaymentCodeAppIcon")
                .resizable()
                .interpolation(.high)
                .scaledToFill()
                .frame(width: 30 * scaleX, height: 30 * scaleY)
                .clipShape(RoundedRectangle(cornerRadius: 7 * fontScale, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 7 * fontScale, style: .continuous)
                        .stroke(Color.white, lineWidth: 2 * min(scaleX, scaleY))
                )
                .position(x: 201 * scaleX, y: 386 * scaleY)

            PaymentCodeMethodIconView(
                option: selectedMethod,
                size: 24 * min(scaleX, scaleY)
            )
            .frame(width: 30 * scaleX, height: 30 * scaleY)
            .position(x: 31 * scaleX, y: 526 * scaleY)

            Text(selectedMethod.paymentRowTitle)
                .font(.system(size: 16 * fontScale, weight: .medium))
                .foregroundStyle(Color(red: 0.24, green: 0.25, blue: 0.27))
                .lineLimit(1)
                .minimumScaleFactor(0.68)
                .frame(width: 228 * scaleX, alignment: .leading)
                .position(x: 164 * scaleX, y: 519 * scaleY)

            Text(selectedMethodSubtitle)
                .font(.system(size: 13.3 * fontScale, weight: .regular))
                .foregroundStyle(Color(red: 0.62, green: 0.62, blue: 0.64))
                .lineLimit(1)
                .minimumScaleFactor(0.82)
                .frame(width: 228 * scaleX, alignment: .leading)
                .position(x: 164 * scaleX, y: 540 * scaleY)

            Button(action: {}) {
                Color.clear
                    .frame(width: 350 * scaleX, height: 58 * scaleY)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .disabled(true)
            .accessibilityIdentifier("payment_code_method_entry")
            .position(x: 193 * scaleX, y: 526 * scaleY)
        }
    }

    private func backButton(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        Button(action: onBack) {
            Image(systemName: "chevron.left")
                .font(.system(size: 19 * fontScale, weight: .medium))
                .foregroundStyle(.white)
                .frame(width: 40 * scaleX, height: 40 * scaleY)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier("payment_code_back")
    }

}
