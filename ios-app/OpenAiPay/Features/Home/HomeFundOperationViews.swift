import SwiftUI
import UIKit

struct DepositView: View {
    let presentation: FundOperationPresentation
    let userId: Int64?
    let preferredToolCode: String?
    let withdrawAvailableBalanceOverride: Decimal?
    let onBack: () -> Void
    let onDepositSubmitting: (Decimal, String, Decimal?, Decimal?) -> Void
    let onDepositTradeNoConfirmed: (String) -> Void
    let onDepositSucceeded: (Decimal?, Decimal?) -> Void
    let onDepositFailed: (String) -> Void

    init(
        presentation: FundOperationPresentation,
        userId: Int64?,
        preferredToolCode: String? = nil,
        withdrawAvailableBalanceOverride: Decimal? = nil,
        onBack: @escaping () -> Void,
        onDepositSubmitting: @escaping (Decimal, String, Decimal?, Decimal?) -> Void,
        onDepositTradeNoConfirmed: @escaping (String) -> Void = { _ in },
        onDepositSucceeded: @escaping (Decimal?, Decimal?) -> Void,
        onDepositFailed: @escaping (String) -> Void
    ) {
        self.presentation = presentation
        self.userId = userId
        self.preferredToolCode = preferredToolCode
        self.withdrawAvailableBalanceOverride = withdrawAvailableBalanceOverride
        self.onBack = onBack
        self.onDepositSubmitting = onDepositSubmitting
        self.onDepositTradeNoConfirmed = onDepositTradeNoConfirmed
        self.onDepositSucceeded = onDepositSucceeded
        self.onDepositFailed = onDepositFailed
    }

    private struct DepositDestinationOption {
        let id: String
        let title: String
        let hint: String
        let bankTool: CashierPayToolData?
        let isBalance: Bool
    }

    @State private var debitPayTools: [CashierPayToolData] = []
    @State private var selectedDebitToolCode: String?
    @State private var isPaymentToolsExpanded = false
    @State private var isLoadingPayTools = false
    @State private var payToolsErrorMessage: String?
    @State private var depositAmountInput: String = ""
    @State private var isSubmittingDeposit = false
    @State private var depositSubmitMessage: String?
    @State private var depositSubmitIsError = false
    @State private var withdrawAvailableBalance: Decimal?
    @State private var withdrawPricingPreview: CashierPricingPreviewData?
    @State private var isLoadingWithdrawPricingPreview = false
    @State private var withdrawPricingPreviewTask: Task<Void, Never>?
    @State private var isAmountInputFocused = false
    @State private var depositWalletAvailableAmountText: String = "--"
    @State private var depositWalletAvailableAmount: Decimal?
    @State private var isLoadingDepositWalletBalance = false

    private var operationKind: FundOperationKind {
        presentation.kind
    }

    private var showsReferenceOnlyLayout: Bool {
        presentation == .aiCashTransferIn
    }

    private var usesCashierPickerSheet: Bool {
        presentation == .aiCashTransferIn
    }

    private var pickerSceneConfiguration: CashierSceneConfiguration {
        CashierSceneConfiguration(
            supportedChannels: presentation == .aiCashTransferIn ? [.wallet, .bankCard] : [.bankCard],
            bankCardPolicy: presentation.bankCardPolicy,
            emptyBankCardText: "暂无可用借记卡"
        )
    }

    private var pickerSelectedPaymentMethodType: RepayPaymentMethodType {
        isBalanceDestinationSelected ? .wallet : .bankCard
    }

    private var selectedBalanceAvailableAmountText: String {
        if isLoadingDepositWalletBalance {
            return "加载中..."
        }
        return depositWalletAvailableAmountText
    }

    private var transferMethodBankIconBackgroundColor: Color {
        presentation == .aiCashTransferIn ? .clear : .white
    }

    private let baseWidth: CGFloat = 402
    private let baseHeight: CGFloat = 874
    private let paymentMethodSheetOffsetY: CGFloat = 100
    private let sheetRowNameCenterY: [CGFloat] = [413, 490, 567, 644, 721, 798]
    private let sheetRowHintCenterY: [CGFloat] = [447, 524, 601, 678, 755, 832]
    private let depositCardBackgroundColor = Color(red: 239 / 255, green: 243 / 255, blue: 246 / 255)
    private let depositPageBackgroundColor = Color(red: 239 / 255, green: 243 / 255, blue: 247 / 255)
    private let balanceDestinationCode = "__wallet_balance__"

    private var selectedDestinationOption: DepositDestinationOption? {
        if let selectedDebitToolCode,
           let selectedOption = allDestinationOptions.first(where: { $0.id == selectedDebitToolCode }) {
            return selectedOption
        }
        return allDestinationOptions.first
    }

    private var selectedDepositPayTool: CashierPayToolData? {
        selectedDestinationOption?.bankTool
    }

    private var isBalanceDestinationSelected: Bool {
        selectedDestinationOption?.isBalance == true
    }

    private var allDestinationOptions: [DepositDestinationOption] {
        var options: [DepositDestinationOption] = []
        if presentation.supportsBalanceDestination {
            options.append(
                DepositDestinationOption(
                    id: balanceDestinationCode,
                    title: presentation.balanceDestinationTitle,
                    hint: presentation.balanceDestinationHintText,
                    bankTool: nil,
                    isBalance: true
                )
            )
        }
        options.append(
            contentsOf: debitPayTools.map { tool in
                DepositDestinationOption(
                    id: tool.toolCode,
                    title: sheetRowTitleText(for: tool),
                    hint: sheetRowHintText(for: tool),
                    bankTool: tool,
                    isBalance: false
                )
            }
        )
        return options
    }

    private var visibleDestinationOptions: [DepositDestinationOption] {
        Array(allDestinationOptions.prefix(6))
    }

    private var selectedVisibleRowIndex: Int? {
        guard let selectedCode = selectedDestinationOption?.id else {
            return nil
        }
        return visibleDestinationOptions.firstIndex(where: { $0.id == selectedCode })
    }

    private var shouldHideWithdrawHelperTexts: Bool {
        operationKind == .withdraw && !depositAmountInput.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    private func currentReferenceImageName(canSubmitDeposit: Bool) -> String {
        if presentation == .aiCashTransferIn && !isBalanceDestinationSelected {
            return canSubmitDeposit ? "AiCashDepositReferenceBankEnabled" : "AiCashDepositReferenceBank"
        }
        if presentation == .withdraw {
            return presentation.referenceImageName
        }
        return canSubmitDeposit ? presentation.enabledReferenceImageName : presentation.referenceImageName
    }

    private func referenceOnlySelectedMethodOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        Group {
            if showsReferenceOnlyLayout {
                if isBalanceDestinationSelected {
                    transferInSelectedBalanceOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                } else {
                    transferInSelectedBankOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                }
            }
        }
        .allowsHitTesting(false)
    }

    private func transferInSelectedBalanceOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        let rowBackgroundColor = Color(red: 245 / 255, green: 245 / 255, blue: 245 / 255)

        return ZStack {
            Rectangle()
                .fill(rowBackgroundColor)
                .frame(width: 150 * scaleX, height: 30 * scaleY)
                .position(x: 274 * scaleX, y: 130 * scaleY)

            Text(selectedBalanceAvailableAmountText)
                .font(yaHeiRegularFont(size: 15.4 * fontScale))
                .foregroundStyle(Color(red: 0.50, green: 0.52, blue: 0.54))
                .lineLimit(1)
                .minimumScaleFactor(0.72)
                .allowsTightening(true)
                .frame(width: 146 * scaleX, alignment: .leading)
                .position(x: 274 * scaleX, y: 132 * scaleY)
        }
    }

    @ViewBuilder
    private func referenceOnlyHeaderTitleOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        if presentation == .aiCashTransferIn {
            Text("转入爱存")
                .font(heiTiFont(size: 18 * fontScale))
                .foregroundStyle(Color(red: 0.14, green: 0.15, blue: 0.16))
                .lineLimit(1)
                .minimumScaleFactor(0.8)
                .frame(width: 124 * scaleX, height: 28 * scaleY, alignment: .center)
                .position(x: 201 * scaleX, y: 74 * scaleY)
                .allowsHitTesting(false)
        }
    }

    private func transferInSelectedBankOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        let minScale = min(scaleX, scaleY)
        let rowBackgroundColor = Color(red: 245 / 255, green: 245 / 255, blue: 245 / 255)

        return ZStack {
            Rectangle()
                .fill(rowBackgroundColor)
                .frame(width: 232 * scaleX, height: 56 * scaleY)
                .position(x: 198 * scaleX, y: 134 * scaleY)

            destinationOptionIcon(selectedDestinationOption, size: 28 * minScale, fontScale: fontScale)
                .frame(width: 32 * scaleX, height: 32 * scaleY)
                .position(x: 106 * scaleX, y: 137 * scaleY)

            textReplacementSlot(
                text: selectedCardTitleWithTailText,
                centerX: 224,
                centerY: 130,
                width: 194,
                height: 28,
                scaleX: scaleX,
                scaleY: scaleY,
                font: heiTiFont(size: 16 * fontScale),
                textColor: Color(red: 0.14, green: 0.15, blue: 0.16)
            )

            textReplacementSlot(
                text: selectedCardLimitTipText,
                centerX: 222,
                centerY: 152,
                width: 188,
                height: 20,
                scaleX: scaleX,
                scaleY: scaleY,
                font: yaHeiRegularFont(size: 12.4 * fontScale),
                textColor: Color(red: 0.50, green: 0.52, blue: 0.54)
            )
        }
    }

    private func referenceOnlyCashierPickerOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        Group {
            if usesCashierPickerSheet && isPaymentToolsExpanded {
                RepayPaymentToolPickerSheetView(
                    scaleX: scaleX,
                    scaleY: scaleY,
                    fontScale: fontScale,
                    sheetTitle: presentation.selectorTitle,
                    accessibilityPrefix: "deposit_cashier",
                    backdropOpacity: 0.18,
                    prefersLeadingCloseButton: true,
                    sceneConfiguration: pickerSceneConfiguration,
                    bankTools: debitPayTools,
                    selectedBankToolCode: selectedDepositPayTool?.toolCode,
                    selectedPaymentMethodType: pickerSelectedPaymentMethodType,
                    walletAvailableAmountText: depositWalletAvailableAmountText,
                    fundAvailableAmountText: "--",
                    isLoadingWalletBalance: isLoadingDepositWalletBalance,
                    isLoadingFundBalance: false,
                    isLoadingBankTools: isLoadingPayTools,
                    bankToolsErrorMessage: payToolsErrorMessage,
                    onClose: {
                        isPaymentToolsExpanded = false
                    },
                    onSelectWallet: {
                        selectedDebitToolCode = balanceDestinationCode
                        isPaymentToolsExpanded = false
                    },
                    onSelectFund: {},
                    onSelectAiCredit: {},
                    onSelectBankTool: { toolCode in
                        selectedDebitToolCode = toolCode
                        isPaymentToolsExpanded = false
                    }
                )
            }
        }
    }


    var body: some View {
        GeometryReader { proxy in
            let scaleX = proxy.size.width / baseWidth
            let scaleY = proxy.size.height / baseHeight
            let fontScale = min(scaleX, scaleY)
            let referenceImageName = currentReferenceImageName(canSubmitDeposit: canSubmitDeposit)

            ZStack {
                Image(referenceImageName)
                    .resizable()
                    .interpolation(.high)
                    .aspectRatio(contentMode: .fill)
                    .frame(width: proxy.size.width, height: proxy.size.height)
                    .clipped()

                if showsReferenceOnlyLayout || !isPaymentToolsExpanded {
                    depositAmountInputOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                        .zIndex(3)
                }
                if !showsReferenceOnlyLayout {
                    depositDynamicOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                        .zIndex(2)
                }
                hotspot(
                    identifier: "deposit_back_button",
                    centerX: 20 * scaleX,
                    centerY: 74 * scaleY,
                    width: 56 * scaleX,
                    height: 52 * scaleY,
                    action: onBack
                )

                hotspot(
                    identifier: "deposit_payment_method_open",
                    centerX: 201 * scaleX,
                    centerY: (showsReferenceOnlyLayout ? 181 : 182) * scaleY,
                    width: (showsReferenceOnlyLayout ? 366 : 356) * scaleX,
                    height: (showsReferenceOnlyLayout ? 84 : 64) * scaleY
                ) {
                    if !visibleDestinationOptions.isEmpty {
                        isAmountInputFocused = false
                        isPaymentToolsExpanded = true
                    }
                }

                if !showsReferenceOnlyLayout && operationKind == .withdraw && !shouldHideWithdrawHelperTexts {
                    hotspot(
                        identifier: "withdraw_fill_all",
                        centerX: (presentation == .aiCashTransferOut ? 336 : 334) * scaleX,
                        centerY: 318 * scaleY,
                        width: (presentation == .aiCashTransferOut ? 150 : 164) * scaleX,
                        height: 44 * scaleY
                    ) {
                        fillWithdrawAllAmount()
                    }
                    .zIndex(5)
                }

                if !isPaymentToolsExpanded {
                    hotspot(
                        identifier: "deposit_amount_focus",
                        centerX: (showsReferenceOnlyLayout ? 192 : 204) * scaleX,
                        centerY: (showsReferenceOnlyLayout ? 271 : 329) * scaleY,
                        width: (showsReferenceOnlyLayout ? 324 : 300) * scaleX,
                        height: (showsReferenceOnlyLayout ? 94 : 56) * scaleY
                    ) {
                        isAmountInputFocused = true
                    }
                    .zIndex(4)
                }
                if !showsReferenceOnlyLayout && isPaymentToolsExpanded {
                    hotspot(
                        identifier: "deposit_payment_method_dismiss_mask",
                        centerX: 201 * scaleX,
                        centerY: 437 * scaleY,
                        width: 402 * scaleX,
                        height: 874 * scaleY
                    ) {
                        isAmountInputFocused = false
                        isPaymentToolsExpanded = false
                    }

                    hotspot(
                        identifier: "deposit_payment_method_close",
                        centerX: 368 * scaleX,
                        centerY: (352 + paymentMethodSheetOffsetY) * scaleY,
                        width: 46 * scaleX,
                        height: 46 * scaleY
                    ) {
                        isAmountInputFocused = false
                        isPaymentToolsExpanded = false
                    }

                    ForEach(Array(visibleDestinationOptions.enumerated()), id: \.element.id) { index, option in
                        let rowY = sheetRowNameCenterY.indices.contains(index) ? sheetRowNameCenterY[index] : 0
                        hotspot(
                            identifier: "deposit_payment_method_row_\(index)",
                            centerX: 203.5 * scaleX,
                            centerY: (rowY + paymentMethodSheetOffsetY) * scaleY,
                            width: 347 * scaleX,
                            height: 76 * scaleY
                        ) {
                            selectedDebitToolCode = option.id
                            isAmountInputFocused = false
                            isPaymentToolsExpanded = false
                        }
                    }
                }

                referenceOnlySelectedMethodOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                    .zIndex(7)

                referenceOnlyHeaderTitleOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                    .zIndex(8)

                referenceOnlyAmountDecorationsOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                    .zIndex(9)

                referenceOnlyCashierPickerOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                    .zIndex(40)

                if !isPaymentToolsExpanded {
                    depositKeyboardHotspots(scaleX: scaleX, scaleY: scaleY)
                        .zIndex(4)
                    depositSubmitOverlay(scaleX: scaleX, scaleY: scaleY)
                        .zIndex(5)
                }
            }
            .frame(width: proxy.size.width, height: proxy.size.height)
        }
        .task(id: userId) {
            await loadDepositPayTools()
            await loadDepositWalletBalance()
            await loadWithdrawAvailableBalance()
        }
        .onChange(of: userId) { _, _ in
            withdrawPricingPreviewTask?.cancel()
            withdrawPricingPreviewTask = nil
            isLoadingWithdrawPricingPreview = false
            withdrawPricingPreview = nil
            isPaymentToolsExpanded = false
            isAmountInputFocused = false
            depositAmountInput = ""
            isSubmittingDeposit = false
            depositSubmitMessage = nil
            withdrawAvailableBalance = nil
            depositWalletAvailableAmountText = "--"
            depositWalletAvailableAmount = nil
            isLoadingDepositWalletBalance = false
        }
        .onChange(of: isPaymentToolsExpanded) { _, expanded in
            if expanded {
                isAmountInputFocused = false
            }
        }
        .onChange(of: selectedDebitToolCode) { _, _ in
            if presentation == .withdraw {
                scheduleWithdrawPricingPreviewReload()
            }
        }
        .onChange(of: depositAmountInput) { _, newValue in
            let sanitized = sanitizeAmountInput(newValue)
            if sanitized != newValue {
                depositAmountInput = sanitized
                return
            }
            if let limitErrorText = immediateAmountLimitErrorText(for: sanitized) {
                depositSubmitMessage = limitErrorText
                depositSubmitIsError = true
            } else if depositSubmitMessage != nil {
                depositSubmitMessage = nil
            }
            if presentation == .withdraw {
                scheduleWithdrawPricingPreviewReload()
            }
        }
    }

    @ViewBuilder
    private func referenceOnlyAmountDecorationsOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        if presentation == .aiCashTransferIn {
            ZStack {
                Rectangle()
                    .fill(Color.white)
                    .frame(width: 24 * scaleX, height: 46 * scaleY)
                    .position(x: 31 * scaleX, y: 277 * scaleY)

                Rectangle()
                    .fill(Color.white)
                    .frame(width: 10 * scaleX, height: 54 * scaleY)
                    .position(x: 52 * scaleX, y: 271 * scaleY)

                Text("¥")
                    .font(heiTiFont(size: 42 * fontScale))
                    .foregroundStyle(Color(red: 0.16, green: 0.16, blue: 0.17))
                    .position(x: 31 * scaleX, y: 269 * scaleY)

                let submitButtonColor = canSubmitDeposit
                    ? Color(red: 88 / 255, green: 151 / 255, blue: 235 / 255)
                    : Color(red: 169 / 255, green: 201 / 255, blue: 250 / 255)
                let submitTextColor = canSubmitDeposit
                    ? Color.white
                    : Color.white.opacity(0.92)
                let submitButtonCenterX: CGFloat = 352
                let submitButtonCenterY: CGFloat = 747
                let submitButtonWidth: CGFloat = 104
                let submitButtonHeight: CGFloat = 188

                // 始终覆盖底图里的“同意协议并转入”，统一改为“转入”。
                Rectangle()
                    .fill(submitButtonColor)
                    .frame(width: submitButtonWidth * scaleX, height: submitButtonHeight * scaleY)
                    .position(x: submitButtonCenterX * scaleX, y: submitButtonCenterY * scaleY)

                Text("转入")
                    .font(yaHeiRegularFont(size: 22 * fontScale))
                    .foregroundStyle(submitTextColor)
                    .frame(width: submitButtonWidth * scaleX, height: submitButtonHeight * scaleY)
                    .position(x: submitButtonCenterX * scaleX, y: submitButtonCenterY * scaleY)
            }
            .allowsHitTesting(false)
        }
    }

    private func depositDynamicOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        ZStack {
            if presentation == .aiCashTransferOut {
                aiCashTransferTextOverrideOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
            }
            depositPrimaryCardOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
            if presentation == .deposit, !isPaymentToolsExpanded {
                depositQuickChargeEraseOverlay(scaleX: scaleX, scaleY: scaleY)
                depositKeyboardArrowEraseOverlay(scaleX: scaleX, scaleY: scaleY)
            }
            if isPaymentToolsExpanded {
                depositMethodSheetOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
            }
            if operationKind == .withdraw, !isPaymentToolsExpanded {
                if presentation == .withdraw {
                    withdrawKeyboardArrowEraseOverlay(scaleX: scaleX, scaleY: scaleY)
                    withdrawAmountBackgroundEraseOverlay(scaleX: scaleX, scaleY: scaleY)
                    withdrawAmountCurrencyOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                }
                withdrawAvailableBalanceOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                if shouldHideWithdrawHelperTexts {
                    withdrawPricingPreviewOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                }
                withdrawFreeQuotaEraseOverlay(scaleX: scaleX, scaleY: scaleY)
                if presentation == .withdraw && canSubmitDeposit {
                    withdrawSubmitTitleOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                }
                if presentation == .aiCashTransferOut {
                    aiCashWithdrawSubmitStateOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                }
            }

            let shouldShowPayToolsLoadingBanner = isLoadingPayTools && (
                presentation != .aiCashTransferOut || isPaymentToolsExpanded
            )

            if shouldShowPayToolsLoadingBanner {
                statusBanner(
                    text: "借记卡加载中",
                    isError: false,
                    scaleX: scaleX,
                    scaleY: scaleY,
                    fontScale: fontScale
                )
            } else if let payToolsErrorMessage {
                statusBanner(
                    text: payToolsErrorMessage,
                    isError: true,
                    scaleX: scaleX,
                    scaleY: scaleY,
                    fontScale: fontScale
                )
            } else if let depositSubmitMessage {
                statusBanner(
                    text: depositSubmitMessage,
                    isError: depositSubmitIsError,
                    scaleX: scaleX,
                    scaleY: scaleY,
                    fontScale: fontScale
                )
            }
        }
        .allowsHitTesting(false)
    }

    private func aiCashTransferTextOverrideOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        EmptyView()
    }

    private func depositQuickChargeEraseOverlay(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        Rectangle()
            .fill(depositPageBackgroundColor)
            .frame(width: 386 * scaleX, height: 100 * scaleY)
            .position(x: 201 * scaleX, y: 416 * scaleY)
    }

    private func depositKeyboardArrowEraseOverlay(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        Rectangle()
            .fill(Color.white)
            .frame(width: 24 * scaleX, height: 12 * scaleY)
            .position(x: 202 * scaleX, y: 568 * scaleY)
    }

    private func withdrawKeyboardArrowEraseOverlay(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        RoundedRectangle(cornerRadius: 4 * min(scaleX, scaleY), style: .continuous)
            .fill(Color.white)
            .frame(width: 18 * scaleX, height: 8 * scaleY)
            .position(x: 202 * scaleX, y: 568 * scaleY)
    }

    private func withdrawAmountCurrencyOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        ZStack {
            Rectangle()
                .fill(Color.white)
                .frame(width: 28 * scaleX, height: 44 * scaleY)
                .position(x: 34 * scaleX, y: 333 * scaleY)

            Text("¥")
                .font(heiTiFont(size: 42 * fontScale))
                .foregroundStyle(Color(red: 0.16, green: 0.16, blue: 0.17))
                .position(x: 34 * scaleX, y: 326 * scaleY)
        }
    }

    private func withdrawAmountBackgroundEraseOverlay(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        Rectangle()
            .fill(Color.white)
            .frame(width: 300 * scaleX, height: 74 * scaleY)
            .position(x: 166 * scaleX, y: 329 * scaleY)
    }

    private func depositPrimaryCardOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        let titleWithTail = selectedCardTitleWithTailText
        let tipText = selectedCardLimitTipText
        let contentYOffset: CGFloat = presentation == .aiCashTransferOut && isBalanceDestinationSelected ? -5 : 0
        let tipCenterX: CGFloat = presentation == .aiCashTransferOut ? 172 : 170

        return ZStack {
            // 擦除参考图中旧的银行卡图标残影（例如“招商银行”旧图）
            Rectangle()
                .fill(depositCardBackgroundColor)
                .frame(width: 90 * scaleX, height: 70 * scaleY)
                .position(x: 47 * scaleX, y: (180 + contentYOffset) * scaleY)

            selectedDestinationIconOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)

            // 先整块擦除模板里的固定银行名称与尾号，再写入动态文案
            Rectangle()
                .fill(depositCardBackgroundColor)
                .frame(width: 238 * scaleX, height: 34 * scaleY)
                .position(x: 179 * scaleX, y: (169 + contentYOffset) * scaleY)

                textReplacementSlot(
                    text: titleWithTail,
                    centerX: 179,
                    centerY: 169 + contentYOffset,
                    width: 238,
                    height: 28,
                    scaleX: scaleX,
                    scaleY: scaleY,
                    font: heiTiFont(size: 16.2 * fontScale),
                    textColor: Color(red: 0.14, green: 0.15, blue: 0.16)
                )

            Rectangle()
                .fill(depositCardBackgroundColor)
                .frame(width: 224 * scaleX, height: 24 * scaleY)
                .position(x: 170 * scaleX, y: (193 + contentYOffset) * scaleY)

            textReplacementSlot(
                text: tipText,
                centerX: tipCenterX,
                centerY: 193 + contentYOffset,
                width: 224,
                height: 24,
                scaleX: scaleX,
                scaleY: scaleY,
                font: yaHeiRegularFont(size: 14 * fontScale),
                textColor: Color(red: 0.50, green: 0.52, blue: 0.54)
            )

        }
    }

    @ViewBuilder
    private func withdrawAvailableBalanceOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        if !shouldHideWithdrawHelperTexts {
            let text = withdrawAvailableAmountText
            let eraseWidth: CGFloat = presentation == .aiCashTransferOut ? 198 : 164
            let eraseCenterX: CGFloat = presentation == .aiCashTransferOut ? 274 : 296
            let textWidth: CGFloat = presentation == .aiCashTransferOut ? 194 : 156
            let textCenterX: CGFloat = presentation == .aiCashTransferOut ? 276 : 294
            let fontSize: CGFloat = presentation == .aiCashTransferOut ? 13.0 : 13.8

            ZStack {
                Rectangle()
                    .fill(Color.white)
                    .frame(width: eraseWidth * scaleX, height: 24 * scaleY)
                    .position(x: eraseCenterX * scaleX, y: 340 * scaleY)

                Text(text)
                    .font(yaHeiRegularFont(size: fontSize * fontScale))
                    .foregroundStyle(Color(red: 0.52, green: 0.53, blue: 0.55))
                    .lineLimit(1)
                    .minimumScaleFactor(presentation == .aiCashTransferOut ? 0.56 : 0.34)
                    .allowsTightening(true)
                    .frame(width: textWidth * scaleX, alignment: .trailing)
                    .clipped()
                    .position(x: textCenterX * scaleX, y: 340 * scaleY)
            }
        }
    }

    private func withdrawFreeQuotaEraseOverlay(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        Rectangle()
            .fill(Color.white)
            .frame(width: 248 * scaleX, height: 30 * scaleY)
            .position(x: 144 * scaleX, y: 387 * scaleY)
    }


    private func withdrawHelperTextsEraseOverlay(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        let topWidth: CGFloat = presentation == .withdraw ? 106 : 128
        let topCenterX: CGFloat = presentation == .withdraw ? 333 : 336
        let bottomWidth: CGFloat = presentation == .withdraw ? 176 : 188
        let bottomCenterX: CGFloat = presentation == .withdraw ? 298 : 306

        return ZStack {
            Rectangle()
                .fill(Color.white)
                .frame(width: topWidth * scaleX, height: 28 * scaleY)
                .position(x: topCenterX * scaleX, y: 315 * scaleY)

            Rectangle()
                .fill(Color.white)
                .frame(width: bottomWidth * scaleX, height: 26 * scaleY)
                .position(x: bottomCenterX * scaleX, y: 340 * scaleY)
        }
        .mask(
            RoundedRectangle(cornerRadius: 16 * min(scaleX, scaleY), style: .continuous)
                .frame(width: 376 * scaleX, height: 188 * scaleY)
                .position(x: 201 * scaleX, y: 317 * scaleY)
        )
    }

    private func withdrawPricingPreviewOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        let settleLineY: CGFloat = presentation.supportsWithdrawFee ? 340 : 315

        return ZStack {
            withdrawHelperTextsEraseOverlay(scaleX: scaleX, scaleY: scaleY)

            if presentation.supportsWithdrawFee {
                Text(withdrawPricingFeeLineText)
                    .font(yaHeiRegularFont(size: 13.2 * fontScale))
                    .foregroundStyle(Color(red: 0.91, green: 0.47, blue: 0.47))
                    .lineLimit(1)
                    .minimumScaleFactor(0.7)
                    .allowsTightening(true)
                    .frame(width: 186 * scaleX, alignment: .trailing)
                    .position(x: 284 * scaleX, y: 315 * scaleY)
            }

            Text(withdrawPricingSettleLineText)
                .font(yaHeiRegularFont(size: 13.8 * fontScale))
                .foregroundStyle(Color(red: 0.34, green: 0.36, blue: 0.39))
                .lineLimit(1)
                .minimumScaleFactor(0.72)
                .allowsTightening(true)
                .frame(width: 178 * scaleX, alignment: .trailing)
                .position(x: 295 * scaleX, y: settleLineY * scaleY)
        }
    }

    private func withdrawSubmitTitleOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        let buttonWidth: CGFloat = 100
        let buttonHeight: CGFloat = 184
        let buttonCenterX: CGFloat = 352
        let buttonCenterY: CGFloat = 746

        return ZStack {
            Rectangle()
                .fill(Color(red: 49 / 255, green: 90 / 255, blue: 178 / 255))
                .frame(width: buttonWidth * scaleX, height: buttonHeight * scaleY)
                .position(x: buttonCenterX * scaleX, y: buttonCenterY * scaleY)

            Text("提现")
                .font(yaHeiRegularFont(size: 18 * fontScale))
                .foregroundStyle(Color.white)
                .lineLimit(1)
                .frame(width: buttonWidth * scaleX, height: buttonHeight * scaleY, alignment: .center)
                .position(x: buttonCenterX * scaleX, y: buttonCenterY * scaleY)
        }
    }

    @ViewBuilder
    private func aiCashWithdrawSubmitStateOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        if canSubmitDeposit {
            Rectangle()
                .fill(Color(red: 62 / 255, green: 107 / 255, blue: 188 / 255).opacity(0.22))
                .blendMode(.multiply)
                .frame(width: 100 * scaleX, height: 184 * scaleY)
                .position(x: 352 * scaleX, y: 747 * scaleY)
        }
    }

    private func depositMethodSheetOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        return ZStack {
            Color.black.opacity(0.16)
                .frame(width: 402 * scaleX, height: 874 * scaleY)
                .position(x: 201 * scaleX, y: 437 * scaleY)

            RoundedRectangle(cornerRadius: 24 * fontScale, style: .continuous)
                .fill(Color(red: 245 / 255, green: 245 / 255, blue: 246 / 255))
                .frame(width: 372 * scaleX, height: 560 * scaleY)
                .position(x: 201 * scaleX, y: (614 + paymentMethodSheetOffsetY) * scaleY)

            Text(presentation.selectorTitle)
                .font(heiTiFont(size: 21 * fontScale))
                .foregroundStyle(Color(red: 0.16, green: 0.16, blue: 0.17))
                .position(x: 201 * scaleX, y: (359 + paymentMethodSheetOffsetY) * scaleY)

            Image(systemName: "xmark")
                .font(.system(size: 19 * fontScale, weight: .medium))
                .foregroundStyle(Color(red: 0.42, green: 0.43, blue: 0.45))
                .position(x: (presentation == .aiCashTransferOut ? 361 : 367) * scaleX, y: (352 + paymentMethodSheetOffsetY) * scaleY)

            ForEach(Array(sheetRowNameCenterY.enumerated()), id: \.offset) { index, rowCenterY in
                let option: DepositDestinationOption? = visibleDestinationOptions.indices.contains(index) ? visibleDestinationOptions[index] : nil
                let rowTitle = option?.title ?? ""
                let rowHint = option?.hint ?? ""
                let hintCenterY = (sheetRowHintCenterY.indices.contains(index) ? sheetRowHintCenterY[index] : rowCenterY + 34) - 6
                let rowIconCenterY = rowCenterY + 11 + paymentMethodSheetOffsetY

                Rectangle()
                    .fill(Color(red: 245 / 255, green: 245 / 255, blue: 246 / 255))
                    .frame(width: 36 * scaleX, height: 36 * scaleY)
                    .position(x: 43 * scaleX, y: rowIconCenterY * scaleY)

                if let option {
                    destinationOptionIcon(option, size: 30 * min(scaleX, scaleY), fontScale: fontScale)
                        .position(x: 43 * scaleX, y: rowIconCenterY * scaleY)
                }

                textReplacementSlot(
                    text: rowTitle,
                    centerX: 180,
                    centerY: rowCenterY + paymentMethodSheetOffsetY,
                    width: 236,
                    height: 34,
                    scaleX: scaleX,
                    scaleY: scaleY,
                    font: yaHeiRegularFont(size: 17 * fontScale),
                    textColor: Color(red: 0.15, green: 0.16, blue: 0.17)
                )

                textReplacementSlot(
                    text: rowHint,
                    centerX: 172,
                    centerY: hintCenterY + paymentMethodSheetOffsetY,
                    width: 220,
                    height: 24,
                    scaleX: scaleX,
                    scaleY: scaleY,
                    font: yaHeiRegularFont(size: 13.5 * fontScale),
                    textColor: Color(red: 0.52, green: 0.53, blue: 0.55)
                )
            }

            if let selectedVisibleRowIndex,
               sheetRowNameCenterY.indices.contains(selectedVisibleRowIndex) {
                Image(systemName: "checkmark")
                    .font(.system(size: 20 * fontScale, weight: .semibold))
                    .foregroundStyle(AppTheme.palette.brandPrimary)
                    .position(
                        x: 356 * scaleX,
                        y: (sheetRowNameCenterY[selectedVisibleRowIndex] + 4 + paymentMethodSheetOffsetY) * scaleY
                    )
            }
        }
    }

    private var selectedCardTitleWithTailText: String {
        guard let selectedDestinationOption else {
            return presentation.destinationPlaceholderText
        }
        return selectedDestinationOption.title
    }

    private var selectedCardLimitTipText: String {
        if isBalanceDestinationSelected {
            return presentation.balanceDestinationHintText
        }
        guard let selectedDepositPayTool else {
            return presentation.destinationHintPlaceholderText
        }
        if operationKind == .withdraw {
            return "2小时内到账"
        }
        let limit = singleLimitAmountText(from: selectedDepositPayTool)
        return "该卡本次最多可转入¥\(limit)"
    }

    private func sheetRowTitleText(for tool: CashierPayToolData) -> String {
        let tail = cardTailNo(from: tool)
        let cardName = simplifiedBankCardName(from: tool.toolName)
        if tail.isEmpty {
            return cardName
        }
        return cardName + "(\(tail))"
    }

    private func simplifiedBankCardName(from toolName: String) -> String {
        let original = cardNamePart(from: toolName)
        return BankLogoCatalog.fullDisplayName(bankCode: nil, bankName: original)
    }

    private func selectedDestinationIconOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        let contentYOffset: CGFloat = presentation == .aiCashTransferOut && isBalanceDestinationSelected ? -5 : 0

        return destinationOptionIcon(selectedDestinationOption, size: 28 * min(scaleX, scaleY), fontScale: fontScale)
            .frame(width: 32 * scaleX, height: 32 * scaleY)
            .position(x: 39 * scaleX, y: (181 + contentYOffset) * scaleY)
    }

    @ViewBuilder
    private func destinationOptionIcon(_ option: DepositDestinationOption?, size: CGFloat, fontScale: CGFloat) -> some View {
        if let option, option.isBalance {
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [
                                AppTheme.palette.brandPrimaryLight,
                                AppTheme.palette.brandPrimary
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: size, height: size)

                Text("余")
                    .font(.system(size: 14 * fontScale, weight: .bold))
                    .foregroundStyle(Color.white)
            }
        } else if let tool = option?.bankTool {
            BankLogoIconView(
                bankCode: tool.bankCode,
                bankName: tool.toolName,
                size: size,
                cornerRadius: 6 * fontScale,
                backgroundColor: transferMethodBankIconBackgroundColor
            )
        } else {
            Color.clear
                .frame(width: size, height: size)
        }
    }

    private func sheetRowHintText(for tool: CashierPayToolData) -> String {
        "银行单笔限额\(singleLimitAmountText(from: tool))元"
    }

    private func cardNamePart(from toolName: String) -> String {
        let normalized = toolName
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: "（", with: "(")
            .replacingOccurrences(of: "）", with: ")")
        guard !normalized.isEmpty else {
            return "借记卡"
        }
        if let range = normalized.range(of: "(尾号") {
            return String(normalized[..<range.lowerBound])
        }
        return normalized
    }

    private func cardTailNo(from tool: CashierPayToolData) -> String {
        let normalized = tool.toolName
            .replacingOccurrences(of: "（", with: "(")
            .replacingOccurrences(of: "）", with: ")")
        if let range = normalized.range(of: "(尾号"),
           let closeIndex = normalized[range.upperBound...].firstIndex(of: ")") {
            let inner = normalized[range.upperBound..<closeIndex]
            let digits = inner.replacingOccurrences(of: "\\D", with: "", options: .regularExpression)
            if digits.count >= 4 {
                return String(digits.suffix(4))
            }
        }
        let codeDigits = tool.toolCode.replacingOccurrences(of: "\\D", with: "", options: .regularExpression)
        if codeDigits.count >= 4 {
            return String(codeDigits.suffix(4))
        }
        return ""
    }

    private func singleLimitAmountText(from tool: CashierPayToolData) -> String {
        let description = tool.toolDescription
        if let range = description.range(of: "单笔限额") {
            let suffix = description[range.upperBound...]
            let fragment = suffix.split(separator: "，", maxSplits: 1).first.map(String.init) ?? ""
            let number = fragment.replacingOccurrences(of: "^[^0-9]*([0-9]+(?:\\.[0-9]+)?)?.*$", with: "$1", options: .regularExpression)
            let trimmed = number.trimmingCharacters(in: .whitespacesAndNewlines)
            if !trimmed.isEmpty, let decimal = Decimal(string: trimmed) {
                return formatAmountWithoutRedundantZeros(decimal)
            }
        }
        return "0"
    }

    private func formatAmountWithoutRedundantZeros(_ amount: Decimal) -> String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.numberStyle = .decimal
        formatter.usesGroupingSeparator = false
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        let number = NSDecimalNumber(decimal: amount)
        return formatter.string(from: number) ?? number.stringValue
    }

    private func yaHeiRegularFont(size: CGFloat) -> Font {
        let candidates = [
            "MicrosoftYaHei",
            "Microsoft YaHei",
            "MicrosoftYaHei-Light",
            "MicrosoftYaHeiUI",
            "Microsoft YaHei UI",
            "PingFangSC-Regular",
            "微软雅黑"
        ]
        for name in candidates where UIFont(name: name, size: size) != nil {
            return .custom(name, size: size)
        }
        return .system(size: size, weight: .regular)
    }

    private func heiTiFont(size: CGFloat) -> Font {
        let candidates = [
            "STHeitiSC-Medium",
            "STHeitiSC-Light",
            "Heiti SC",
            "PingFangSC-Semibold"
        ]
        for name in candidates where UIFont(name: name, size: size) != nil {
            return .custom(name, size: size)
        }
        return .system(size: size, weight: .black)
    }

    private func depositAmountInputOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        let minScale = min(scaleX, scaleY)
        let isAiCashTransferIn = showsReferenceOnlyLayout
        let inputWidth = (isAiCashTransferIn ? 315 : 300) * scaleX
        let inputHeight = (isAiCashTransferIn ? 64 : 56) * scaleY
        let inputCenterX = (isAiCashTransferIn ? 197 : 204) * scaleX
        let inputCenterY = (isAiCashTransferIn ? 271 : 329) * scaleY
        let displayText = depositAmountInput
        let fontSize = 38 * fontScale
        let textWidth = depositAmountTextWidth(displayText, fontSize: fontSize)
        let caretWidth = max(1.6 * minScale, 1.4)
        let caretHeight = 37 * scaleY
        let staticCaretMaskWidth = (isAiCashTransferIn ? 26 : 12) * minScale
        let staticCaretMaskOffsetX = (isAiCashTransferIn ? 8 : 0) * minScale
        let textStart = (isAiCashTransferIn ? 18 : 5) * minScale
        let contentYOffset = (isAiCashTransferIn ? -5 : 0) * scaleY
        let maxCaretOffset = max(0, inputWidth - textStart - caretWidth - (5 * minScale))
        let caretOffsetX = min(textWidth + textStart + (isAiCashTransferIn ? 0 : 1) * minScale, maxCaretOffset)

        return ZStack(alignment: .leading) {
            Rectangle()
                .fill(Color.clear)

            Rectangle()
                .fill(Color.white)
                .frame(width: staticCaretMaskWidth, height: inputHeight)
                .offset(x: staticCaretMaskOffsetX, y: isAiCashTransferIn ? 0 : -2 * scaleY)

            Text(displayText)
                .font(heiTiFont(size: fontSize))
                .foregroundStyle(Color(red: 0.16, green: 0.16, blue: 0.17))
                .lineLimit(1)
                .padding(.leading, textStart)
                .offset(y: contentYOffset)

            if isAmountInputFocused {
                TimelineView(.periodic(from: .now, by: 0.5)) { context in
                    let tick = Int(context.date.timeIntervalSinceReferenceDate * 2)
                    Rectangle()
                        .fill(AppTheme.palette.brandPrimaryPressed)
                        .frame(width: caretWidth, height: caretHeight)
                        .opacity(tick % 2 == 0 ? 1 : 0)
                }
                .padding(.leading, caretOffsetX)
                .offset(y: contentYOffset)
            }
        }
        .frame(width: inputWidth, height: inputHeight, alignment: .leading)
        .position(x: inputCenterX, y: inputCenterY)
        .allowsHitTesting(false)
        .accessibilityIdentifier("deposit_amount_input")
    }

    @ViewBuilder
    private func depositKeyboardHotspots(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        let digitKeyWidth = 92 * scaleX
        let digitKeyHeight = 60 * scaleY
        let deleteKeyWidth = 74 * scaleX
        let deleteKeyHeight = 46 * scaleY

        depositTapRegion(identifier: "deposit_key_1", centerX: 50 * scaleX, centerY: 625 * scaleY, width: digitKeyWidth, height: digitKeyHeight) {
            appendDepositAmountDigit("1")
        }
        depositTapRegion(identifier: "deposit_key_2", centerX: 150 * scaleX, centerY: 625 * scaleY, width: digitKeyWidth, height: digitKeyHeight) {
            appendDepositAmountDigit("2")
        }
        depositTapRegion(identifier: "deposit_key_3", centerX: 251 * scaleX, centerY: 625 * scaleY, width: digitKeyWidth, height: digitKeyHeight) {
            appendDepositAmountDigit("3")
        }
        depositTapRegion(identifier: "deposit_key_4", centerX: 50 * scaleX, centerY: 687 * scaleY, width: digitKeyWidth, height: digitKeyHeight) {
            appendDepositAmountDigit("4")
        }
        depositTapRegion(identifier: "deposit_key_5", centerX: 150 * scaleX, centerY: 687 * scaleY, width: digitKeyWidth, height: digitKeyHeight) {
            appendDepositAmountDigit("5")
        }
        depositTapRegion(identifier: "deposit_key_6", centerX: 251 * scaleX, centerY: 687 * scaleY, width: digitKeyWidth, height: digitKeyHeight) {
            appendDepositAmountDigit("6")
        }
        depositTapRegion(identifier: "deposit_key_7", centerX: 50 * scaleX, centerY: 748 * scaleY, width: digitKeyWidth, height: digitKeyHeight) {
            appendDepositAmountDigit("7")
        }
        depositTapRegion(identifier: "deposit_key_8", centerX: 150 * scaleX, centerY: 748 * scaleY, width: digitKeyWidth, height: digitKeyHeight) {
            appendDepositAmountDigit("8")
        }
        depositTapRegion(identifier: "deposit_key_9", centerX: 251 * scaleX, centerY: 748 * scaleY, width: digitKeyWidth, height: digitKeyHeight) {
            appendDepositAmountDigit("9")
        }
        depositTapRegion(identifier: "deposit_key_0", centerX: 100 * scaleX, centerY: 810 * scaleY, width: 188 * scaleX, height: digitKeyHeight) {
            appendDepositAmountDigit("0")
        }
        depositTapRegion(identifier: "deposit_key_dot", centerX: 251 * scaleX, centerY: 810 * scaleY, width: 92 * scaleX, height: digitKeyHeight) {
            appendDepositAmountDot()
        }
        depositTapRegion(identifier: "deposit_key_delete", centerX: 354 * scaleX, centerY: 624 * scaleY, width: deleteKeyWidth, height: deleteKeyHeight) {
            removeLastDepositAmountCharacter()
        }
    }

    private func depositSubmitOverlay(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        hotspot(
            identifier: "deposit_key_submit",
            centerX: 352 * scaleX,
            centerY: 747 * scaleY,
            width: 100 * scaleX,
            height: 184 * scaleY
        ) {
            Task {
                await submitDeposit()
            }
        }
    }

    private func appendDepositAmountDigit(_ digit: String) {
        guard digit.count == 1 else {
            return
        }
        isAmountInputFocused = true
        var next = depositAmountInput
        if next == "0" {
            next = digit == "0" ? next : digit
        } else {
            next.append(digit)
        }
        updateDepositAmountInput(next)
    }

    private func appendDepositAmountDot() {
        isAmountInputFocused = true
        if depositAmountInput.contains(".") {
            return
        }
        if depositAmountInput.isEmpty {
            updateDepositAmountInput("0.")
            return
        }
        updateDepositAmountInput(depositAmountInput + ".")
    }

    private func removeLastDepositAmountCharacter() {
        isAmountInputFocused = true
        guard !depositAmountInput.isEmpty else {
            return
        }
        depositAmountInput.removeLast()
    }

    private func sanitizeAmountInput(_ text: String) -> String {
        let filtered = text.filter { $0.isNumber || $0 == "." }
        if filtered.isEmpty {
            return ""
        }

        var result = ""
        var hasDot = false
        var fractionalCount = 0
        for ch in filtered {
            if ch == "." {
                if hasDot {
                    continue
                }
                hasDot = true
                if result.isEmpty {
                    result = "0"
                }
                result.append(ch)
                continue
            }
            if hasDot {
                if fractionalCount >= 2 {
                    continue
                }
                fractionalCount += 1
            }
            result.append(ch)
        }

        if result.hasPrefix("00"), !result.hasPrefix("0.") {
            while result.count > 1, result.first == "0", result.dropFirst().first != "." {
                result.removeFirst()
            }
        }
        return result
    }

    private var normalizedDepositAmountInput: String {
        let trimmed = depositAmountInput.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            return ""
        }
        if trimmed.hasSuffix(".") {
            return String(trimmed.dropLast())
        }
        return trimmed
    }

    private var depositAmountDecimal: Decimal? {
        let normalized = normalizedDepositAmountInput
        guard !normalized.isEmpty else {
            return nil
        }
        return Decimal(string: normalized, locale: Locale(identifier: "en_US_POSIX"))
    }

    private func immediateAmountLimitErrorText(for input: String) -> String? {
        guard let limit = presentation.singleTransactionLimitAmount,
              let limitErrorText = presentation.singleTransactionLimitErrorText else {
            return nil
        }

        let trimmed = input.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            return nil
        }

        let effectiveInput = trimmed.hasSuffix(".") ? String(trimmed.dropLast()) : trimmed
        guard !effectiveInput.isEmpty,
              let amount = Decimal(string: effectiveInput, locale: Locale(identifier: "en_US_POSIX")),
              amount > limit else {
            return nil
        }
        return limitErrorText
    }

    private func updateDepositAmountInput(_ proposed: String) {
        let sanitized = sanitizeAmountInput(proposed)
        if let limitErrorText = immediateAmountLimitErrorText(for: sanitized) {
            depositSubmitMessage = limitErrorText
            depositSubmitIsError = true
            return
        }
        depositAmountInput = sanitized
    }

    private var canSubmitDeposit: Bool {
        guard let amount = depositAmountDecimal else {
            return false
        }
        guard amount > 0 else {
            return false
        }
        if let limit = presentation.singleTransactionLimitAmount, amount > limit {
            return false
        }
        if operationKind == .withdraw {
            guard let available = withdrawAvailableBalance else {
                return false
            }
            return amount <= max(available, .zero)
        }
        return true
    }

    private var withdrawAvailableAmountText: String {
        guard let available = withdrawAvailableBalance else {
            return "\(presentation.availableAmountPrefix) --"
        }
        return "\(presentation.availableAmountPrefix) ¥\(formatAmountFixed2(max(available, .zero)))"
    }

    private var withdrawPricingFeeLineText: String {
        guard presentation.supportsWithdrawFee else {
            return ""
        }
        if isLoadingWithdrawPricingPreview {
            return "手续费试算中..."
        }
        if let preview = withdrawPricingPreview {
            return "手续费 \(formatWithdrawFeeRatePercent(preview.feeRate)) ¥\(formatAmountFixed2(max(preview.feeAmount.amount, .zero)))"
        }
        guard let fallbackFeeAmount = fallbackWithdrawFeeAmount else {
            return "手续费 --"
        }
        return "手续费 \(formatWithdrawFeeRatePercent(withdrawFallbackFeeRate)) ¥\(formatAmountFixed2(fallbackFeeAmount))"
    }

    private var withdrawPricingSettleLineText: String {
        if presentation == .aiCashTransferOut {
            guard let amount = depositAmountDecimal, amount > 0 else {
                return "预计到账 --"
            }
            return "预计到账 ¥\(formatAmountFixed2(max(amount, .zero)))"
        }
        if isLoadingWithdrawPricingPreview {
            return "预计到账 --"
        }
        if let preview = withdrawPricingPreview {
            return "预计到账 ¥\(formatAmountFixed2(max(preview.settleAmount.amount, .zero)))"
        }
        guard let fallbackSettleAmount = fallbackWithdrawSettleAmount else {
            return "预计到账 --"
        }
        return "预计到账 ¥\(formatAmountFixed2(fallbackSettleAmount))"
    }

    private func fillWithdrawAllAmount() {
        guard operationKind == .withdraw else {
            return
        }
        guard let available = withdrawAvailableBalance, available > 0 else {
            depositAmountInput = ""
            return
        }
        let normalizedAvailable = max(available, .zero)
        if let limit = presentation.singleTransactionLimitAmount,
           normalizedAvailable > limit {
            depositAmountInput = formatAmountForInput(limit)
            depositSubmitMessage = presentation.singleTransactionLimitErrorText
            depositSubmitIsError = true
            return
        }
        depositAmountInput = formatAmountForInput(normalizedAvailable)
    }

    @MainActor
    private func submitDeposit() async {
        guard !isSubmittingDeposit else {
            return
        }

        guard let amount = depositAmountDecimal, amount > 0 else {
            depositSubmitMessage = "请输入正确金额"
            depositSubmitIsError = true
            return
        }

        if let limit = presentation.singleTransactionLimitAmount,
           amount > limit {
            depositSubmitMessage = presentation.singleTransactionLimitErrorText
            depositSubmitIsError = true
            return
        }

        if operationKind == .withdraw {
            guard let available = withdrawAvailableBalance else {
                depositSubmitMessage = "可用余额加载中，请稍后重试"
                depositSubmitIsError = true
                return
            }
            if amount > max(available, .zero) {
                depositSubmitMessage = presentation.overLimitErrorText
                depositSubmitIsError = true
                return
            }
        }

        if presentation == .aiCashTransferIn {
            guard let walletAvailableAmount = depositWalletAvailableAmount else {
                depositSubmitMessage = "可用余额加载中，请稍后重试"
                depositSubmitIsError = true
                return
            }
            if amount > max(walletAvailableAmount, .zero) {
                depositSubmitMessage = "账户余额不足"
                depositSubmitIsError = true
                return
            }
        }

        guard let userId, userId > 0 else {
            depositSubmitMessage = "未找到当前登录用户，请重新登录"
            depositSubmitIsError = true
            return
        }

        if !isBalanceDestinationSelected, selectedDepositPayTool == nil {
            depositSubmitMessage = presentation.destinationPlaceholderText
            depositSubmitIsError = true
            return
        }

        let selectedTool = selectedDepositPayTool
        let methodText = selectedCardTitleWithTailText.trimmingCharacters(in: .whitespacesAndNewlines)
        let paymentMethod: String = {
            if isBalanceDestinationSelected {
                return "WALLET"
            }
            let normalized = selectedTool?.toolType.trimmingCharacters(in: .whitespacesAndNewlines).uppercased() ?? ""
            return normalized.isEmpty ? "BANK_CARD" : normalized
        }()
        let paymentToolCode = selectedTool?.toolCode.trimmingCharacters(in: .whitespacesAndNewlines)

        onDepositSubmitting(
            amount,
            methodText,
            operationKind == .withdraw ? resolvedPreviewWithdrawFeeAmount() : nil,
            operationKind == .withdraw ? resolvedPreviewWithdrawSettleAmount(for: amount) : nil
        )
        isSubmittingDeposit = true
        depositSubmitMessage = nil
        depositSubmitIsError = false

        do {
            if presentation == .aiCashTransferOut {
                let visibleBalance = aiCashVisibleBalanceTarget(for: amount)
                let fundAccount = try? await APIClient.shared.fetchFundAccount(userId: userId)
                let normalizedFundCode = normalizedAiCashFundCode(fundAccount?.fundCode ?? "AICASH")
                let latestNav = normalizedAiCashNav(fundAccount?.latestNav)
                let backendHoldingAmount = max(fundAccount?.holdingAmount ?? .zero, .zero)
                let bootstrapGap = max(visibleBalance - backendHoldingAmount, .zero)
                let minimumBootstrapAmount = Decimal(string: "0.0001", locale: Locale(identifier: "en_US_POSIX")) ?? .zero

                if bootstrapGap >= minimumBootstrapAmount {
                    try await APIClient.shared.seedFundHolding(
                        userId: userId,
                        fundCode: normalizedFundCode,
                        amount: bootstrapGap,
                        nav: latestNav
                    )
                }

                let redeemShare = aiCashRedeemShare(for: amount, latestNav: latestNav)
                let redeemDestination = isBalanceDestinationSelected ? "BALANCE" : "BANK_CARD"
                let redeemBankName: String? = {
                    if isBalanceDestinationSelected {
                        return nil
                    }
                    let toolName = selectedTool?.toolName.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
                    if !toolName.isEmpty {
                        return toolName
                    }
                    let text = methodText.trimmingCharacters(in: .whitespacesAndNewlines)
                    return text.isEmpty ? nil : text
                }()
                let redeemTransaction = try await APIClient.shared.createFundFastRedeem(
                    userId: userId,
                    fundCode: normalizedFundCode,
                    share: redeemShare,
                    redeemDestination: redeemDestination,
                    bankName: redeemBankName
                )
                _ = try await APIClient.shared.confirmFundRedeem(orderNo: redeemTransaction.orderNo)
            } else if presentation == .aiCashTransferIn {
                let fundAccount = try? await APIClient.shared.fetchFundAccount(userId: userId)
                let normalizedFundCode = normalizedAiCashFundCode(fundAccount?.fundCode ?? "AICASH")
                let latestNav = normalizedAiCashNav(fundAccount?.latestNav)
                let subscribeTrade = try await APIClient.shared.transferIntoFundAccount(
                    userId: userId,
                    fundCode: normalizedFundCode,
                    amount: amount,
                    nav: latestNav
                )
                onDepositTradeNoConfirmed(subscribeTrade.orderNo)
            } else if operationKind == .withdraw {
                let withdrawTrade = try await APIClient.shared.createWithdrawTrade(
                    payerUserId: userId,
                    amount: amount,
                    paymentMethod: paymentMethod,
                    paymentToolCode: paymentToolCode?.isEmpty == true ? nil : paymentToolCode
                )
                onDepositSucceeded(
                    resolvedTradeWithdrawFeeAmount(from: withdrawTrade),
                    resolvedTradeWithdrawSettleAmount(from: withdrawTrade, submittedAmount: amount)
                )
            } else {
                _ = try await APIClient.shared.createDepositTrade(
                    payerUserId: userId,
                    amount: amount,
                    paymentMethod: paymentMethod,
                    paymentToolCode: paymentToolCode?.isEmpty == true ? nil : paymentToolCode
                )
            }
            isSubmittingDeposit = false
            depositSubmitMessage = nil
            depositSubmitIsError = false
            if presentation == .aiCashTransferOut || operationKind != .withdraw {
                onDepositSucceeded(nil, nil)
            }
        } catch {
            let message = userFacingErrorMessage(error)
            isSubmittingDeposit = false
            if presentation == .aiCashTransferOut {
                depositSubmitMessage = message
                depositSubmitIsError = true
            }
            onDepositFailed(message)
        }
    }

    private func aiCashRedeemShare(for amount: Decimal, latestNav: Decimal) -> Decimal {
        let normalizedNav = normalizedAiCashNav(latestNav)
        let rounding = NSDecimalNumberHandler(
            roundingMode: .plain,
            scale: 4,
            raiseOnExactness: false,
            raiseOnOverflow: false,
            raiseOnUnderflow: false,
            raiseOnDivideByZero: false
        )
        let share = NSDecimalNumber(decimal: amount)
            .dividing(by: NSDecimalNumber(decimal: normalizedNav), withBehavior: rounding)
            .decimalValue
        let minimumShare = Decimal(string: "0.0001", locale: Locale(identifier: "en_US_POSIX")) ?? .zero
        return max(share, minimumShare)
    }

    private func normalizedAiCashNav(_ nav: Decimal?) -> Decimal {
        if let nav, nav > .zero {
            return nav
        }
        return Decimal(string: "1.0000", locale: Locale(identifier: "en_US_POSIX")) ?? Decimal(1)
    }

    private func aiCashVisibleBalanceTarget(for amount: Decimal) -> Decimal {
        let visibleBalance = max(withdrawAvailableBalanceOverride ?? withdrawAvailableBalance ?? .zero, .zero)
        return max(visibleBalance, amount)
    }

    private func normalizedAiCashFundCode(_ fundCode: String) -> String {
        let normalized = fundCode
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .uppercased()
        if normalized.isEmpty {
            return "AICASH"
        }
        if normalized.replacingOccurrences(of: "0", with: "O") == "AICASH" {
            return "AICASH"
        }
        return normalized
    }

    private func depositTapRegion(
        identifier: String,
        centerX: CGFloat,
        centerY: CGFloat,
        width: CGFloat,
        height: CGFloat,
        action: @escaping () -> Void
    ) -> some View {
        Color.black.opacity(0.012)
            .frame(width: width, height: height)
            .contentShape(Rectangle())
            .position(x: centerX, y: centerY)
            .highPriorityGesture(
                TapGesture().onEnded {
                    action()
                }
            )
            .accessibilityIdentifier(identifier)
    }

    private func depositAmountTextWidth(_ text: String, fontSize: CGFloat) -> CGFloat {
        guard !text.isEmpty else {
            return 0
        }
        let attributes: [NSAttributedString.Key: Any] = [
            .font: heiTiUIFont(size: fontSize)
        ]
        return (text as NSString).size(withAttributes: attributes).width
    }

    private func heiTiUIFont(size: CGFloat) -> UIFont {
        let candidates = [
            "STHeitiSC-Medium",
            "STHeitiSC-Light",
            "Heiti SC",
            "PingFangSC-Semibold"
        ]
        for name in candidates {
            if let font = UIFont(name: name, size: size) {
                return font
            }
        }
        return .systemFont(ofSize: size, weight: .black)
    }

    private func textReplacementSlot(
        text: String,
        centerX: CGFloat,
        centerY: CGFloat,
        width: CGFloat,
        height: CGFloat,
        scaleX: CGFloat,
        scaleY: CGFloat,
        font: Font,
        textColor: Color
    ) -> some View {
        ZStack(alignment: .leading) {
            Text(text)
                .font(font)
                .foregroundStyle(textColor)
                .lineLimit(1)
                .minimumScaleFactor(0.75)
                .padding(.leading, 2 * min(scaleX, scaleY))
        }
        .frame(width: width * scaleX, height: height * scaleY, alignment: .leading)
        .position(x: centerX * scaleX, y: centerY * scaleY)
    }

    private func statusBanner(
        text: String,
        isError: Bool,
        scaleX: CGFloat,
        scaleY: CGFloat,
        fontScale: CGFloat
    ) -> some View {
        Text(text)
            .font(.system(size: 13 * fontScale, weight: .medium))
            .foregroundStyle(isError ? Color(red: 0.72, green: 0.17, blue: 0.17) : Color(red: 0.16, green: 0.47, blue: 0.82))
            .frame(width: 280 * scaleX, alignment: .center)
            .position(x: 201 * scaleX, y: 262 * scaleY)
    }

    private func formatAmountForInput(_ amount: Decimal) -> String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.numberStyle = .decimal
        formatter.usesGroupingSeparator = false
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return formatter.string(from: NSDecimalNumber(decimal: amount)) ?? "0"
    }

    private func formatAmountFixed2(_ amount: Decimal) -> String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.numberStyle = .decimal
        formatter.usesGroupingSeparator = false
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.string(from: NSDecimalNumber(decimal: amount)) ?? "0.00"
    }

    private func parseAmountText(_ text: String) -> Decimal? {
        let normalized = text
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: ",", with: "")
        guard !normalized.isEmpty else {
            return nil
        }
        return Decimal(string: normalized, locale: Locale(identifier: "en_US_POSIX"))
    }

    @MainActor
    private func loadDepositPayTools() async {
        guard let userId, userId > 0 else {
            debitPayTools = []
            selectedDebitToolCode = nil
            payToolsErrorMessage = "未找到当前登录用户"
            return
        }

        isLoadingPayTools = true
        payToolsErrorMessage = nil
        do {
            let cashierView = try await APIClient.shared.fetchCashierView(
                userId: userId,
                sceneCode: operationKind.cashierSceneCode
            )
            let resolvedSceneConfiguration = CashierSceneConfiguration.fromServerData(
                cashierView.sceneConfig,
                fallback: pickerSceneConfiguration
            )
            let debitTools = CashierPresentationHelper.filteredBankTools(
                cashierView.payTools,
                policy: presentation.bankCardPolicy
            )
            debitPayTools = debitTools
            let normalizedPreferredToolCode = preferredToolCode?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            if let selectedDebitToolCode,
               debitTools.contains(where: { $0.toolCode == selectedDebitToolCode }) || (presentation.supportsBalanceDestination && selectedDebitToolCode == balanceDestinationCode) {
                // 保留用户当前选中。
            } else if !normalizedPreferredToolCode.isEmpty,
                      debitTools.contains(where: { $0.toolCode == normalizedPreferredToolCode }) {
                selectedDebitToolCode = normalizedPreferredToolCode
            } else if presentation.supportsBalanceDestination {
                selectedDebitToolCode = balanceDestinationCode
            } else {
                selectedDebitToolCode = debitTools.first(where: \.defaultSelected)?.toolCode ?? debitTools.first?.toolCode
            }
            if debitTools.isEmpty && !presentation.supportsBalanceDestination {
                payToolsErrorMessage = resolvedSceneConfiguration.emptyBankCardText
            }
        } catch {
            debitPayTools = []
            selectedDebitToolCode = presentation.supportsBalanceDestination ? balanceDestinationCode : nil
            payToolsErrorMessage = userFacingErrorMessage(error)
        }
        isLoadingPayTools = false
    }

    @MainActor
    private func loadDepositWalletBalance() async {
        guard presentation == .aiCashTransferIn else {
            depositWalletAvailableAmountText = "--"
            depositWalletAvailableAmount = nil
            isLoadingDepositWalletBalance = false
            return
        }
        guard let userId, userId > 0 else {
            depositWalletAvailableAmountText = "--"
            depositWalletAvailableAmount = nil
            isLoadingDepositWalletBalance = false
            return
        }

        isLoadingDepositWalletBalance = true
        do {
            let overview = try await APIClient.shared.fetchAssetOverview(userId: String(userId))
            if let walletAvailableAmount = parseAmountText(overview.availableAmount) {
                let normalizedAmount = max(walletAvailableAmount, .zero)
                depositWalletAvailableAmount = normalizedAmount
                depositWalletAvailableAmountText = formatWalletAvailableAmount(
                    NSDecimalNumber(decimal: normalizedAmount).stringValue
                )
            } else {
                depositWalletAvailableAmount = nil
                depositWalletAvailableAmountText = "--"
            }
        } catch {
            depositWalletAvailableAmount = nil
            depositWalletAvailableAmountText = "--"
        }
        isLoadingDepositWalletBalance = false
    }

    private func formatWalletAvailableAmount(_ raw: String?) -> String {
        let trimmed = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard !trimmed.isEmpty,
              let decimal = Decimal(string: trimmed.replacingOccurrences(of: ",", with: ""), locale: Locale(identifier: "en_US_POSIX")) else {
            return "--"
        }
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "zh_CN")
        formatter.numberStyle = .decimal
        formatter.usesGroupingSeparator = true
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        let number = NSDecimalNumber(decimal: decimal)
        return "¥" + (formatter.string(from: number) ?? number.stringValue)
    }

    private func formatWithdrawFeeRatePercent(_ feeRate: Decimal) -> String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "zh_CN")
        formatter.numberStyle = .decimal
        formatter.usesGroupingSeparator = false
        formatter.minimumFractionDigits = 1
        formatter.maximumFractionDigits = 2
        let percentValue = feeRate * 100
        let number = NSDecimalNumber(decimal: percentValue)
        return (formatter.string(from: number) ?? number.stringValue) + "%"
    }

    private var withdrawFallbackFeeRate: Decimal {
        Decimal(string: "0.001", locale: Locale(identifier: "en_US_POSIX")) ?? .zero
    }

    private var fallbackWithdrawFeeAmount: Decimal? {
        guard operationKind == .withdraw,
              let amount = depositAmountDecimal,
              amount > .zero else {
            return nil
        }
        let rawFee = max(amount * withdrawFallbackFeeRate, .zero)
        return roundedCurrencyAmount(rawFee)
    }

    private var fallbackWithdrawSettleAmount: Decimal? {
        guard operationKind == .withdraw,
              let amount = depositAmountDecimal,
              amount > .zero else {
            return nil
        }
        let feeAmount = fallbackWithdrawFeeAmount ?? .zero
        return max(amount - feeAmount, .zero)
    }

    private func roundedCurrencyAmount(_ amount: Decimal) -> Decimal {
        let rounding = NSDecimalNumberHandler(
            roundingMode: .plain,
            scale: 2,
            raiseOnExactness: false,
            raiseOnOverflow: false,
            raiseOnUnderflow: false,
            raiseOnDivideByZero: false
        )
        return NSDecimalNumber(decimal: amount)
            .rounding(accordingToBehavior: rounding)
            .decimalValue
    }

    private func resolvedPreviewWithdrawFeeAmount() -> Decimal? {
        guard operationKind == .withdraw else {
            return nil
        }
        if let preview = withdrawPricingPreview {
            return max(preview.feeAmount.amount, .zero)
        }
        return fallbackWithdrawFeeAmount
    }

    private func resolvedPreviewWithdrawSettleAmount(for submittedAmount: Decimal) -> Decimal? {
        guard operationKind == .withdraw else {
            return nil
        }
        if let preview = withdrawPricingPreview {
            return max(preview.settleAmount.amount, .zero)
        }
        if let fallbackSettle = fallbackWithdrawSettleAmount {
            return fallbackSettle
        }
        return max(submittedAmount - (resolvedPreviewWithdrawFeeAmount() ?? .zero), .zero)
    }

    private func resolvedTradeWithdrawFeeAmount(from trade: TransferTradeData) -> Decimal {
        let tradeFeeAmount = max(trade.feeAmount.amount, .zero)
        if tradeFeeAmount > .zero {
            return tradeFeeAmount
        }
        return max(withdrawPricingPreview?.feeAmount.amount ?? .zero, .zero)
    }

    private func resolvedTradeWithdrawSettleAmount(from trade: TransferTradeData, submittedAmount: Decimal) -> Decimal {
        let tradeSettleAmount = max(trade.settleAmount.amount, .zero)
        if tradeSettleAmount > .zero {
            return tradeSettleAmount
        }
        if let preview = withdrawPricingPreview {
            return max(preview.settleAmount.amount, .zero)
        }
        return max(submittedAmount - resolvedTradeWithdrawFeeAmount(from: trade), .zero)
    }

    @MainActor
    private func scheduleWithdrawPricingPreviewReload() {
        withdrawPricingPreviewTask?.cancel()
        withdrawPricingPreviewTask = nil

        guard presentation == .withdraw else {
            isLoadingWithdrawPricingPreview = false
            withdrawPricingPreview = nil
            return
        }
        guard let currentUserId = userId, currentUserId > 0 else {
            isLoadingWithdrawPricingPreview = false
            withdrawPricingPreview = nil
            return
        }
        guard let amount = depositAmountDecimal, amount > 0 else {
            isLoadingWithdrawPricingPreview = false
            withdrawPricingPreview = nil
            return
        }
        if let limit = presentation.singleTransactionLimitAmount, amount > limit {
            isLoadingWithdrawPricingPreview = false
            withdrawPricingPreview = nil
            return
        }
        guard selectedDepositPayTool != nil else {
            isLoadingWithdrawPricingPreview = false
            withdrawPricingPreview = nil
            return
        }

        let normalizedToolType = selectedDepositPayTool?.toolType.trimmingCharacters(in: .whitespacesAndNewlines).uppercased() ?? ""
        let paymentMethod = normalizedToolType.isEmpty ? "BANK_CARD" : normalizedToolType

        isLoadingWithdrawPricingPreview = true
        withdrawPricingPreview = nil
        withdrawPricingPreviewTask = Task {
            do {
                try await Task.sleep(nanoseconds: 250_000_000)
                let preview = try await APIClient.shared.fetchCashierPricingPreview(
                    userId: currentUserId,
                    sceneCode: operationKind.cashierSceneCode,
                    paymentMethod: paymentMethod,
                    amount: amount
                )
                guard !Task.isCancelled else {
                    return
                }
                await MainActor.run {
                    withdrawPricingPreview = preview
                    isLoadingWithdrawPricingPreview = false
                }
            } catch {
                guard !isCancellationLikeError(error) else {
                    return
                }
                await MainActor.run {
                    withdrawPricingPreview = nil
                    isLoadingWithdrawPricingPreview = false
                }
            }
        }
    }

    @MainActor
    private func loadWithdrawAvailableBalance() async {
        guard operationKind == .withdraw else {
            withdrawAvailableBalance = nil
            return
        }
        guard let userId, userId > 0 else {
            withdrawAvailableBalance = nil
            return
        }
        if presentation == .aiCashTransferOut {
            withdrawAvailableBalance = max(withdrawAvailableBalanceOverride ?? .zero, .zero)
            return
        }
        do {
            let overview = try await APIClient.shared.fetchAssetOverview(userId: String(userId))
            withdrawAvailableBalance = parseAmountText(overview.availableAmount) ?? .zero
        } catch {
            withdrawAvailableBalance = nil
        }
    }

    private func hotspot(
        identifier: String,
        centerX: CGFloat,
        centerY: CGFloat,
        width: CGFloat,
        height: CGFloat,
        action: @escaping () -> Void
    ) -> some View {
        Color.black.opacity(0.012)
            .frame(width: width, height: height)
            .contentShape(Rectangle())
            .position(x: centerX, y: centerY)
            .highPriorityGesture(
                TapGesture().onEnded {
                    action()
                }
            )
            .accessibilityIdentifier(identifier)
    }
    }

struct DepositSuccessView: View {
    let presentation: FundOperationPresentation
    let depositAmount: Decimal
    let paymentMethodText: String
    let feeAmount: Decimal?
    let actualArrivedAmount: Decimal?
    let aiCashTransferInSubmittedAt: Date?
    let resultState: DepositResultState
    let onBackHome: () -> Void
    let onDone: () -> Void

    init(
        presentation: FundOperationPresentation,
        depositAmount: Decimal,
        paymentMethodText: String,
        feeAmount: Decimal?,
        actualArrivedAmount: Decimal?,
        aiCashTransferInSubmittedAt: Date? = nil,
        resultState: DepositResultState,
        onBackHome: @escaping () -> Void,
        onDone: @escaping () -> Void
    ) {
        self.presentation = presentation
        self.depositAmount = depositAmount
        self.paymentMethodText = paymentMethodText
        self.feeAmount = feeAmount
        self.actualArrivedAmount = actualArrivedAmount
        self.aiCashTransferInSubmittedAt = aiCashTransferInSubmittedAt
        self.resultState = resultState
        self.onBackHome = onBackHome
        self.onDone = onDone
    }

    private var operationKind: FundOperationKind {
        presentation.kind
    }

    private let baseWidth: CGFloat = 402
    private let baseHeight: CGFloat = 874

    var body: some View {
        GeometryReader { proxy in
            let scaleX = proxy.size.width / baseWidth
            let scaleY = proxy.size.height / baseHeight
            let fontScale = min(scaleX, scaleY)

            ZStack {
                Image("DepositSuccessReference")
                    .resizable()
                    .interpolation(.high)
                    .aspectRatio(contentMode: .fill)
                    .frame(width: proxy.size.width, height: proxy.size.height)
                    .clipped()

                successDynamicOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)

                hotspot(
                    identifier: "deposit_success_back_home",
                    centerX: 359 * scaleX,
                    centerY: 93 * scaleY,
                    width: 78 * scaleX,
                    height: 35 * scaleY,
                    isDisabled: resultState == .processing,
                    action: onBackHome
                )

                hotspot(
                    identifier: "deposit_success_done",
                    centerX: 201 * scaleX,
                    centerY: 822 * scaleY,
                    width: 248 * scaleX,
                    height: 58 * scaleY,
                    isDisabled: resultState == .processing,
                    action: onDone
                )
            }
            .frame(width: proxy.size.width, height: proxy.size.height)
        }
        .ignoresSafeArea()
    }

    private func successDynamicOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        let amountText = "¥\(self.formatAmountFixed2(primaryDisplayAmount))"
        let paymentMethodDisplayText = paymentMethodText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            ? presentation.defaultMethodDisplayText
            : paymentMethodText.trimmingCharacters(in: .whitespacesAndNewlines)
        let methodRowCenterY: CGFloat = {
            if shouldShowAiCashIncomeSchedule {
                return 320
            }
            return shouldShowSettlementAmountSummary && !shouldShowWithdrawFeeSummary ? 260 : 290
        }()
        let titleCenterX: CGFloat = {
            if operationKind == .withdraw && resultState == .succeeded {
                return 209
            }
            if operationKind == .deposit && resultState == .succeeded {
                return 209
            }
            return 199
        }()
        let successTitleRowOffsetX: CGFloat = operationKind == .deposit && resultState == .succeeded ? 5 : 0
        let titleText: String = {
            switch resultState {
            case .processing:
                return presentation.processingTitle
            case .succeeded:
                return presentation.successTitle
            case .failed:
                return presentation.failedTitle
            }
        }()
        let stateText: String? = {
            switch resultState {
            case .processing:
                return presentation.processingStateText
            case .succeeded:
                return nil
            case .failed(let message):
                let normalized = message.trimmingCharacters(in: .whitespacesAndNewlines)
                return normalized.isEmpty ? "\(presentation.failedTitle)，请重试" : normalized
            }
        }()
        let isFailureState: Bool = {
            if case .failed = resultState {
                return true
            }
            return false
        }()

        return ZStack {
            Text(titleText)
                .font(.system(size: 23 * fontScale, weight: .semibold))
                .foregroundStyle(Color.white.opacity(0.95))
                .lineLimit(1)
                .minimumScaleFactor(0.7)
                .frame(width: 130 * scaleX, alignment: .leading)
                .position(x: titleCenterX * scaleX, y: 74 * scaleY)

            if resultState == .succeeded {
                Image(systemName: "checkmark.circle.fill")
                    .font(.system(size: 17 * fontScale, weight: .semibold))
                    .foregroundStyle(Color.white.opacity(0.95))
                    .frame(width: 24 * scaleX, height: 24 * scaleY)
                    .position(x: (126 + successTitleRowOffsetX) * scaleX, y: 74 * scaleY)
            }

            Text("回首页")
                .font(.system(size: 18 * fontScale, weight: .medium))
                .foregroundStyle(Color.white.opacity(0.92))
                .lineLimit(1)
                .minimumScaleFactor(0.7)
                .frame(width: 76 * scaleX, alignment: .leading)
                .position(x: 357 * scaleX, y: 73 * scaleY)

            if shouldShowPrimaryAmount {
                Text(amountText)
                    .font(.system(size: 58 * fontScale, weight: .medium))
                    .foregroundStyle(Color.white.opacity(0.95))
                    .lineLimit(1)
                    .minimumScaleFactor(0.5)
                    .frame(width: 240 * scaleX)
                    .position(x: 201 * scaleX, y: 176 * scaleY)
            } else {
                Text("爱存")
                    .font(.system(size: 52 * fontScale, weight: .semibold))
                    .foregroundStyle(Color.white.opacity(0.95))
                    .lineLimit(1)
                    .minimumScaleFactor(0.7)
                    .frame(width: 240 * scaleX)
                    .position(x: 201 * scaleX, y: 176 * scaleY)
            }

            if shouldShowAiCashIncomeSchedule {
                aiCashIncomeScheduleRow(
                    text: aiCashIncomeStartText,
                    centerY: 238,
                    scaleX: scaleX,
                    scaleY: scaleY,
                    fontScale: fontScale
                )

                aiCashIncomeScheduleRow(
                    text: aiCashIncomeArriveText,
                    centerY: 268,
                    scaleX: scaleX,
                    scaleY: scaleY,
                    fontScale: fontScale
                )
            }

            if shouldShowSettlementAmountSummary {
                successInfoRow(
                    label: presentation.settlementSummaryAmountLabelText,
                    valueText: "¥\(formatAmountFixed2(max(depositAmount, .zero)))",
                    centerY: 230,
                    labelCenterX: 69,
                    valueCenterX: 310,
                    labelWidth: 112,
                    valueWidth: 166,
                    scaleX: scaleX,
                    scaleY: scaleY,
                    fontScale: fontScale
                )
            }

            if shouldShowWithdrawFeeSummary {
                successInfoRow(
                    label: "手续费",
                    valueText: "¥\(formatAmountFixed2(withdrawFeeAmount))",
                    centerY: 260,
                    labelCenterX: 69,
                    valueCenterX: 310,
                    labelWidth: 112,
                    valueWidth: 166,
                    scaleX: scaleX,
                    scaleY: scaleY,
                    fontScale: fontScale
                )
            }

            successInfoRow(
                label: presentation.methodLabelText,
                valueText: paymentMethodDisplayText,
                centerY: methodRowCenterY,
                labelCenterX: operationKind == .deposit ? 68 : 69,
                valueCenterX: operationKind == .deposit ? 315 : 310,
                labelWidth: 112,
                valueWidth: 166,
                scaleX: scaleX,
                scaleY: scaleY,
                fontScale: fontScale
            )

            if let stateText {
                Text(stateText)
                    .font(.system(size: 16 * fontScale, weight: .medium))
                    .foregroundStyle(isFailureState ? Color(red: 1, green: 0.82, blue: 0.82) : Color.white.opacity(0.92))
                    .lineLimit(2)
                    .minimumScaleFactor(0.75)
                    .multilineTextAlignment(.center)
                    .frame(width: 320 * scaleX, alignment: .center)
                    .position(x: 201 * scaleX, y: 356 * scaleY)
            }
        }
        .allowsHitTesting(false)
    }

    private var shouldShowSettlementAmountSummary: Bool {
        operationKind == .withdraw && resultState == .succeeded
    }

    private var shouldShowPrimaryAmount: Bool {
        presentation != .aiCashOpenAccount
    }

    private var shouldShowAiCashIncomeSchedule: Bool {
        presentation == .aiCashTransferIn && resultState == .succeeded
    }

    private var shouldShowWithdrawFeeSummary: Bool {
        presentation.supportsWithdrawFee && resultState == .succeeded
    }

    private var primaryDisplayAmount: Decimal {
        if operationKind == .withdraw && shouldPreferWithdrawSettleAmount {
            return max(actualArrivedAmount ?? depositAmount, .zero)
        }
        return max(depositAmount, .zero)
    }

    private var shouldPreferWithdrawSettleAmount: Bool {
        switch resultState {
        case .processing, .succeeded:
            return true
        case .failed:
            return false
        }
    }

    private var withdrawFeeAmount: Decimal {
        max(feeAmount ?? .zero, .zero)
    }

    private func pixelAligned(_ value: CGFloat) -> CGFloat {
        let scale = UIScreen.main.scale
        return (value * scale).rounded() / scale
    }

    private func yaHeiBoldFont(size: CGFloat) -> Font {
        let candidates = [
            "MicrosoftYaHei-Bold",
            "Microsoft YaHei Bold",
            "MicrosoftYaHeiUI-Bold",
            "Microsoft YaHei UI Bold",
            "微软雅黑 Bold"
        ]
        for name in candidates where UIFont(name: name, size: size) != nil {
            return .custom(name, size: size)
        }
        return .system(size: size, weight: .bold)
    }

    private func yaHeiRegularFont(size: CGFloat) -> Font {
        let candidates = [
            "MicrosoftYaHei",
            "Microsoft YaHei",
            "MicrosoftYaHei-Light",
            "MicrosoftYaHeiUI",
            "Microsoft YaHei UI",
            "微软雅黑"
        ]
        for name in candidates where UIFont(name: name, size: size) != nil {
            return .custom(name, size: size)
        }
        return .system(size: size, weight: .regular)
    }

    private func formatAmountFixed2(_ amount: Decimal) -> String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.numberStyle = .decimal
        formatter.usesGroupingSeparator = false
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.string(from: NSDecimalNumber(decimal: amount)) ?? "0.00"
    }

    private func formatAmount(_ amount: Decimal) -> String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.numberStyle = .decimal
        formatter.usesGroupingSeparator = false
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.string(from: NSDecimalNumber(decimal: amount)) ?? "0.00"
    }

    private struct AiCashIncomeSchedule {
        let ownershipDate: Date
        let incomeStartDate: Date
        let incomeArrivalDate: Date
    }

    private static let aiCashHolidayDateKeysByYear: [Int: Set<String>] = [
        2024: [
            "2024-01-01",
            "2024-02-10", "2024-02-11", "2024-02-12", "2024-02-13", "2024-02-14", "2024-02-15", "2024-02-16", "2024-02-17",
            "2024-04-04", "2024-04-05", "2024-04-06",
            "2024-05-01", "2024-05-02", "2024-05-03", "2024-05-04", "2024-05-05",
            "2024-06-10",
            "2024-09-15", "2024-09-16", "2024-09-17",
            "2024-10-01", "2024-10-02", "2024-10-03", "2024-10-04", "2024-10-05", "2024-10-06", "2024-10-07"
        ],
        2025: [
            "2025-01-01",
            "2025-01-28", "2025-01-29", "2025-01-30", "2025-01-31", "2025-02-01", "2025-02-02", "2025-02-03", "2025-02-04",
            "2025-04-04", "2025-04-05", "2025-04-06",
            "2025-05-01", "2025-05-02", "2025-05-03", "2025-05-04", "2025-05-05",
            "2025-05-31", "2025-06-01", "2025-06-02",
            "2025-10-01", "2025-10-02", "2025-10-03", "2025-10-04", "2025-10-05", "2025-10-06", "2025-10-07", "2025-10-08"
        ],
        2026: [
            "2026-01-01", "2026-01-02", "2026-01-03",
            "2026-02-15", "2026-02-16", "2026-02-17", "2026-02-18", "2026-02-19", "2026-02-20", "2026-02-21", "2026-02-22", "2026-02-23",
            "2026-04-04", "2026-04-05", "2026-04-06",
            "2026-05-01", "2026-05-02", "2026-05-03", "2026-05-04", "2026-05-05",
            "2026-06-19", "2026-06-20", "2026-06-21",
            "2026-09-25", "2026-09-26", "2026-09-27",
            "2026-10-01", "2026-10-02", "2026-10-03", "2026-10-04", "2026-10-05", "2026-10-06", "2026-10-07"
        ]
    ]

    private var aiCashIncomeStartText: String {
        aiCashIncomeScheduleText(for: aiCashIncomeSchedule.incomeStartDate, suffix: "开始计算收益")
    }

    private var aiCashIncomeArriveText: String {
        aiCashIncomeScheduleText(for: aiCashIncomeSchedule.incomeArrivalDate, suffix: "收益到账")
    }

    private var aiCashIncomeSchedule: AiCashIncomeSchedule {
        let referenceTime = aiCashTransferInSubmittedAt ?? Date()
        return resolveAiCashIncomeSchedule(referenceTime: referenceTime)
    }

    private func resolveAiCashIncomeSchedule(referenceTime: Date) -> AiCashIncomeSchedule {
        let calendar = aiCashScheduleCalendar
        let transferDate = calendar.startOfDay(for: referenceTime)
        let ownershipDate: Date

        // 规则：15:00 前归属 T 日，15:00 后归属下一交易日；确认份额为 T+1 交易日，收益到账为 T+2 交易日。
        if !isAiCashTradingDay(transferDate, calendar: calendar) {
            ownershipDate = nextAiCashTradingDay(onOrAfter: transferDate, calendar: calendar)
        } else {
            let cutoff = calendar.date(
                bySettingHour: 15,
                minute: 0,
                second: 0,
                of: transferDate
            ) ?? transferDate
            ownershipDate = referenceTime < cutoff
                ? transferDate
                : nextAiCashTradingDay(after: transferDate, calendar: calendar)
        }

        let incomeStartDate = nextAiCashTradingDay(after: ownershipDate, calendar: calendar)
        let incomeArrivalDate = nextAiCashTradingDay(after: incomeStartDate, calendar: calendar)
        return AiCashIncomeSchedule(
            ownershipDate: ownershipDate,
            incomeStartDate: incomeStartDate,
            incomeArrivalDate: incomeArrivalDate
        )
    }

    private var aiCashScheduleCalendar: Calendar {
        var calendar = Calendar(identifier: .gregorian)
        calendar.locale = Locale(identifier: "zh_CN")
        calendar.timeZone = TimeZone(identifier: "Asia/Shanghai") ?? .current
        return calendar
    }

    private func isAiCashTradingDay(_ date: Date, calendar: Calendar) -> Bool {
        if calendar.isDateInWeekend(date) {
            return false
        }
        return !isAiCashHoliday(date, calendar: calendar)
    }

    private func isAiCashHoliday(_ date: Date, calendar: Calendar) -> Bool {
        let components = calendar.dateComponents([.year, .month, .day], from: date)
        guard let year = components.year,
              let month = components.month,
              let day = components.day,
              let holidayDateKeys = Self.aiCashHolidayDateKeysByYear[year] else {
            return false
        }
        let dateKey = String(format: "%04d-%02d-%02d", year, month, day)
        return holidayDateKeys.contains(dateKey)
    }

    private func nextAiCashTradingDay(onOrAfter date: Date, calendar: Calendar) -> Date {
        var cursor = calendar.startOfDay(for: date)
        var guardCounter = 0
        while !isAiCashTradingDay(cursor, calendar: calendar) && guardCounter < 400 {
            cursor = calendar.date(byAdding: .day, value: 1, to: cursor) ?? cursor.addingTimeInterval(86_400)
            guardCounter += 1
        }
        return cursor
    }

    private func nextAiCashTradingDay(after date: Date, calendar: Calendar) -> Date {
        let start = calendar.startOfDay(for: date)
        let nextDay = calendar.date(byAdding: .day, value: 1, to: start) ?? start.addingTimeInterval(86_400)
        return nextAiCashTradingDay(onOrAfter: nextDay, calendar: calendar)
    }

    private func aiCashIncomeScheduleText(for date: Date, suffix: String) -> String {
        let monthDayFormatter = DateFormatter()
        monthDayFormatter.locale = Locale(identifier: "zh_CN")
        monthDayFormatter.timeZone = TimeZone(identifier: "Asia/Shanghai") ?? .current
        monthDayFormatter.dateFormat = "MM-dd"
        let weekdayFormatter = DateFormatter()
        weekdayFormatter.locale = Locale(identifier: "zh_CN")
        weekdayFormatter.timeZone = TimeZone(identifier: "Asia/Shanghai") ?? .current
        weekdayFormatter.dateFormat = "EEEE"
        return "\(monthDayFormatter.string(from: date)) \(weekdayFormatter.string(from: date)) \(suffix)"
    }

    private func aiCashIncomeScheduleRow(
        text: String,
        centerY: CGFloat,
        scaleX: CGFloat,
        scaleY: CGFloat,
        fontScale: CGFloat
    ) -> some View {
        HStack(spacing: 8 * scaleX) {
            Circle()
                .stroke(Color.white.opacity(0.95), lineWidth: max(1, 1.1 * min(scaleX, scaleY)))
                .frame(width: 7 * scaleX, height: 7 * scaleY)

            Text(text)
                .font(yaHeiRegularFont(size: 14 * fontScale))
                .foregroundStyle(Color.white.opacity(0.96))
                .lineLimit(1)
                .minimumScaleFactor(0.82)
        }
        .frame(width: 308 * scaleX, alignment: .leading)
        .position(x: 166 * scaleX, y: centerY * scaleY)
    }

    private func successInfoRow(
        label: String,
        valueText: String,
        centerY: CGFloat,
        labelCenterX: CGFloat,
        valueCenterX: CGFloat,
        labelWidth: CGFloat,
        valueWidth: CGFloat,
        scaleX: CGFloat,
        scaleY: CGFloat,
        fontScale: CGFloat
    ) -> some View {
        let labelFontSize = (operationKind == .deposit ? 14.2 : 16) * fontScale
        let valueFontSize = 16 * fontScale

        return ZStack {
            Text(label)
                .font(yaHeiBoldFont(size: labelFontSize))
                .foregroundStyle(Color.white)
                .shadow(color: Color.black.opacity(0.08), radius: 0.6 * fontScale, x: 0, y: 0.4 * fontScale)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
                .frame(width: labelWidth * scaleX, alignment: .leading)
                .position(
                    x: pixelAligned(labelCenterX * scaleX),
                    y: pixelAligned(centerY * scaleY)
                )

            Text(valueText)
                .font(.system(size: valueFontSize, weight: .semibold))
                .foregroundStyle(Color.white.opacity(0.98))
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .frame(width: valueWidth * scaleX, alignment: .trailing)
                .position(x: valueCenterX * scaleX, y: pixelAligned(centerY * scaleY))
        }
    }

    private func hotspot(
        identifier: String,
        centerX: CGFloat,
        centerY: CGFloat,
        width: CGFloat,
        height: CGFloat,
        isDisabled: Bool = false,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Color.clear
                .frame(width: width, height: height)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .disabled(isDisabled)
        .accessibilityIdentifier(identifier)
        .position(x: centerX, y: centerY)
    }
    }

struct CashierTransferSubmitRequest {
    let payerUserId: Int64
    let payeeUserId: Int64
    let amount: Decimal
    let paymentMethod: String
    let paymentToolCode: String?
    let remark: String
}

struct CashierView: View {
    let contact: TransferRecentContact
    let payerUserId: Int64?
    let payerDisplayName: String
    let transferAmount: Decimal
    let transferRemark: String
    let submitTransferAction: ((CashierTransferSubmitRequest) async throws -> MessageData?)?
    let onBack: () -> Void
    let onPaySucceeded: (String, MessageData?) -> Void

    @State private var isPaymentToolsExpanded = false
    @State private var isSubmitting = false
    @State private var errorMessage: String?
    @State private var payTools: [CashierPayToolData] = []
    @State private var isBalanceSelected = true
    @State private var isFundSelected = false
    @State private var selectedBankToolCode: String?
    @State private var walletAvailableAmountText: String = "--"
    @State private var fundAvailableAmountText: String = "--"
    @State private var isLoadingWalletBalance = false
    @State private var isLoadingFundBalance = false
    @State private var isLoadingPayTools = false
    @State private var payToolsErrorMessage: String?

    private let baseWidth: CGFloat = 402
    private let baseHeight: CGFloat = 874
    @State private var sceneConfiguration: CashierSceneConfiguration = .transfer

    var body: some View {
        GeometryReader { proxy in
            let scaleX = proxy.size.width / baseWidth
            let scaleY = proxy.size.height / baseHeight

            ZStack {
                cashierSheetReference(scaleX: scaleX, scaleY: scaleY)

                cashierDynamicOverlay(scaleX: scaleX, scaleY: scaleY)

                hotspot(
                    identifier: "cashier_back",
                    centerX: 23 * scaleX,
                    centerY: (isPaymentToolsExpanded ? 82 : 282) * scaleY,
                    width: 84 * scaleX,
                    height: 64 * scaleY,
                    isDisabled: isSubmitting,
                    action: onBack
                )

                hotspot(
                    identifier: "cashier_global_back",
                    centerX: 17 * scaleX,
                    centerY: 100 * scaleY,
                    width: 56 * scaleX,
                    height: 56 * scaleY,
                    isDisabled: isSubmitting,
                    action: onBack
                )

                if !isPaymentToolsExpanded {
                    hotspot(
                        identifier: "cashier_expand_payment_tools",
                        centerX: 200.5 * scaleX,
                        centerY: 663.5 * scaleY,
                        width: 377 * scaleX,
                        height: 35 * scaleY,
                        isDisabled: isSubmitting,
                        action: {
                            isPaymentToolsExpanded = true
                        }
                    )
                }

                if !isPaymentToolsExpanded {
                    hotspot(
                        identifier: "cashier_select_balance_collapsed",
                        centerX: 201 * scaleX,
                        centerY: collapsedBalanceRowTapCenterY * scaleY,
                        width: 368 * scaleX,
                        height: 38 * scaleY,
                        isDisabled: isSubmitting,
                        action: {
                            isBalanceSelected = true
                            isFundSelected = false
                        }
                    )

                    ForEach(Array(collapsedBankRows.enumerated()), id: \.offset) { index, tool in
                        if let rowY = rowCenterY(at: index, from: collapsedBankRowTapCenterY) {
                            hotspot(
                                identifier: "cashier_select_collapsed_\(index)",
                                centerX: 201 * scaleX,
                                centerY: rowY * scaleY,
                                width: 368 * scaleX,
                                height: 38 * scaleY,
                                isDisabled: isSubmitting,
                                action: {
                                    selectedBankToolCode = tool.toolCode
                                    isBalanceSelected = false
                                    isFundSelected = false
                                }
                            )
                        }
                    }
                }

                hotspot(
                    identifier: "cashier_confirm_pay",
                    centerX: 201 * scaleX,
                    centerY: 768.5 * scaleY,
                    width: 368 * scaleX,
                    height: 53 * scaleY,
                    isDisabled: isSubmitting,
                    action: {
                        Task {
                            await submitTransfer()
                        }
                    }
                )

                if isSubmitting {
                    AILoadingIndicator(
                        text: "转账处理中...",
                        accentColor: AppTheme.palette.brandPrimaryPressed,
                        textColor: Color(red: 0.23, green: 0.25, blue: 0.29)
                    )
                        .padding(.horizontal, 20)
                        .padding(.vertical, 16)
                        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                }

                if isPaymentToolsExpanded {
                    RepayPaymentToolPickerSheetView(
                        scaleX: scaleX,
                        scaleY: scaleY,
                        fontScale: min(scaleX, scaleY),
                        sheetTitle: "选择付款方式",
                        accessibilityPrefix: "transfer_cashier",
                        backdropOpacity: 0.01,
                        prefersLeadingCloseButton: false,
                        sceneConfiguration: sceneConfiguration,
                        bankTools: filteredBankTools,
                        selectedBankToolCode: selectedBankToolCode,
                        selectedPaymentMethodType: selectedPaymentMethodType,
                        walletAvailableAmountText: walletAvailableAmountText,
                        fundAvailableAmountText: fundAvailableAmountText,
                        isLoadingWalletBalance: isLoadingWalletBalance,
                        isLoadingFundBalance: isLoadingFundBalance,
                        isLoadingBankTools: isLoadingPayTools,
                        bankToolsErrorMessage: payToolsErrorMessage,
                        onClose: {
                            isPaymentToolsExpanded = false
                        },
                        onSelectWallet: {
                            isBalanceSelected = true
                            isFundSelected = false
                            isPaymentToolsExpanded = false
                        },
                        onSelectFund: {
                            isBalanceSelected = false
                            isFundSelected = true
                            selectedBankToolCode = nil
                            isPaymentToolsExpanded = false
                        },
                        onSelectAiCredit: {},
                        onSelectBankTool: { toolCode in
                            selectedBankToolCode = toolCode
                            isBalanceSelected = false
                            isFundSelected = false
                            isPaymentToolsExpanded = false
                        }
                    )
                    .zIndex(20)
                }
            }
            .frame(width: proxy.size.width, height: proxy.size.height)
        }
        .ignoresSafeArea()
        .alert("付款失败", isPresented: Binding(
            get: { errorMessage != nil },
            set: { isPresented in
                if !isPresented {
                    errorMessage = nil
                }
            }
        )) {
            Button("确定", role: .cancel) {}
        } message: {
            Text(errorMessage ?? "")
        }
        .task(id: payerUserId) {
            await loadCashierPayTools()
            await loadCashierWalletBalance()
            await loadCashierFundBalance()
        }
    }

    private func cashierSheetReference(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        let sheetName = "CashierCollapsedSheetReference"
        let sheetHeight = 615 * scaleY
        let centerY = 566.5 * scaleY

        return Image(sheetName)
            .resizable()
            .interpolation(.high)
            .aspectRatio(contentMode: .fill)
            .frame(width: 402 * scaleX, height: sheetHeight)
            .position(x: 201 * scaleX, y: centerY)
    }

    private func cashierDynamicOverlay(scaleX: CGFloat, scaleY: CGFloat) -> some View {
        let fontScale = min(scaleX, scaleY)
        let transferTitle = "向\(contact.listDisplayName)转账"
        let transferAmountText = "¥\(formatAmount(transferAmount))"
        let remarkText = transferRemark.trimmingCharacters(in: .whitespacesAndNewlines)

        let titleY = 364 * scaleY
        let amountY = 420 * scaleY

        return ZStack {
            Text(transferTitle)
                .font(.system(size: 17 * fontScale, weight: .semibold))
                .foregroundStyle(Color(red: 0.24, green: 0.24, blue: 0.24))
                .lineLimit(1)
                .minimumScaleFactor(0.7)
                .frame(width: 220 * scaleX)
                .position(x: 201 * scaleX, y: titleY)

            Text(transferAmountText)
                .font(.system(size: 58 * fontScale, weight: .medium))
                .foregroundStyle(Color(red: 0.20, green: 0.20, blue: 0.22))
                .lineLimit(1)
                .minimumScaleFactor(0.45)
                .frame(width: 220 * scaleX)
                .position(x: 201 * scaleX, y: amountY)

            cashierBalanceOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)

            cashierRemarkOverlay(
                remarkText: remarkText,
                scaleX: scaleX,
                scaleY: scaleY,
                fontScale: fontScale
            )

            cashierPrimaryPayToolOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
        }
        .allowsHitTesting(false)
    }

    private func cashierRemarkOverlay(
        remarkText: String,
        scaleX: CGFloat,
        scaleY: CGFloat,
        fontScale: CGFloat
    ) -> some View {
        let rowCenterY: CGFloat = 481
        let rowBackgroundColor = Color(red: 254 / 255, green: 254 / 255, blue: 254 / 255)
        let normalizedText = remarkText.trimmingCharacters(in: .whitespacesAndNewlines)
        let remarkCenterX: CGFloat = 324
        let remarkWidth: CGFloat = 156

        return cashierTrailingTextReplacementSlot(
            text: normalizedText,
            centerX: remarkCenterX,
            centerY: rowCenterY,
            width: remarkWidth,
            height: 34,
            scaleX: scaleX,
            scaleY: scaleY,
            font: yaHeiRegularFont(size: 16.5 * fontScale),
            textColor: Color(red: 0.24, green: 0.24, blue: 0.24),
            backgroundColor: rowBackgroundColor
        )
    }

    private func cashierPrimaryPayToolOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        ZStack {
            CashierReferenceBankRowsOverlayView(
                bankTools: collapsedBankRows,
                selectedToolCode: isBalanceSelected ? nil : selectedBankTool?.toolCode,
                rowLayouts: collapsedBankRowLayouts,
                textCenterX: 210,
                textWidth: 288,
                textHeight: 30,
                iconCenterX: 45,
                iconSlotWidth: 38,
                iconSlotHeight: 38,
                iconSize: 30,
                checkmarkCenterX: 357,
                scaleX: scaleX,
                scaleY: scaleY,
                fontScale: fontScale,
                fontProvider: yaHeiRegularFont,
                textFormatter: { tool in
                    CashierPresentationHelper.bankDisplayName(for: tool, stripCardTypeWords: true)
                },
                textColor: Color(red: 0.22, green: 0.22, blue: 0.24),
                checkmarkColor: AppTheme.palette.brandPrimary
            )

            if isBalanceSelected {
                payToolCheckmarkSlot(
                    centerY: collapsedBalanceRowTapCenterY - 4,
                    scaleX: scaleX,
                    scaleY: scaleY,
                    fontScale: fontScale
                )
            }
        }
    }

    private func cashierExpandedPayToolsOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        ZStack {
            CashierReferenceBankRowsOverlayView(
                bankTools: expandedBankRows,
                selectedToolCode: isBalanceSelected ? nil : selectedBankTool?.toolCode,
                rowLayouts: expandedBankRowLayouts,
                textCenterX: 210,
                textWidth: 288,
                textHeight: 30,
                iconCenterX: 45,
                iconSlotWidth: 38,
                iconSlotHeight: 38,
                iconSize: 30,
                checkmarkCenterX: 357,
                scaleX: scaleX,
                scaleY: scaleY,
                fontScale: fontScale,
                fontProvider: yaHeiRegularFont,
                textFormatter: { tool in
                    CashierPresentationHelper.bankDisplayName(for: tool, stripCardTypeWords: true)
                },
                textColor: Color(red: 0.22, green: 0.22, blue: 0.24),
                checkmarkColor: AppTheme.palette.brandPrimary
            )

            if isBalanceSelected {
                payToolCheckmarkSlot(
                    centerY: expandedBalanceCheckmarkCenterY,
                    scaleX: scaleX,
                    scaleY: scaleY,
                    fontScale: fontScale
                )
            }
        }
    }

    private var collapsedBalanceRowTapCenterY: CGFloat {
        539
    }

    private var expandedBalanceRowTapCenterY: CGFloat {
        411
    }

    private var expandedBalanceCheckmarkCenterY: CGFloat {
        411
    }


    private var collapsedBankRowTapCenterY: [CGFloat] {
        [581, 623]
    }

    private var collapsedBankRowLayouts: [CashierReferenceBankRowLayout] {
        [
            CashierReferenceBankRowLayout(
                id: "collapsed_bank_0",
                iconCenterY: 585,
                textCenterY: 581,
                checkmarkCenterY: 585,
                textFontSize: 15.5,
                textBackgroundColor: bankTextRowBackgroundColor(isExpanded: false, rowIndex: 0),
                iconBackgroundColor: nil
            ),
            CashierReferenceBankRowLayout(
                id: "collapsed_bank_1",
                iconCenterY: 627,
                textCenterY: 627,
                checkmarkCenterY: 627,
                textFontSize: 15.5,
                textBackgroundColor: bankTextRowBackgroundColor(isExpanded: false, rowIndex: 1),
                iconBackgroundColor: nil
            )
        ]
    }


    private var expandedBankRowTapCenterY: [CGFloat] {
        [531, 577, 623, 709]
    }

    private var expandedBankRowLayouts: [CashierReferenceBankRowLayout] {
        [
            CashierReferenceBankRowLayout(
                id: "expanded_bank_0",
                iconCenterY: 535,
                textCenterY: 531,
                checkmarkCenterY: 532,
                textFontSize: 14.8,
                textBackgroundColor: bankTextRowBackgroundColor(isExpanded: true, rowIndex: 0),
                iconBackgroundColor: balanceRowBackgroundColor(isExpanded: true)
            ),
            CashierReferenceBankRowLayout(
                id: "expanded_bank_1",
                iconCenterY: 581,
                textCenterY: 577,
                checkmarkCenterY: 578,
                textFontSize: 14.8,
                textBackgroundColor: bankTextRowBackgroundColor(isExpanded: true, rowIndex: 1),
                iconBackgroundColor: balanceRowBackgroundColor(isExpanded: true)
            ),
            CashierReferenceBankRowLayout(
                id: "expanded_bank_2",
                iconCenterY: 627,
                textCenterY: 623,
                checkmarkCenterY: 624,
                textFontSize: 14.8,
                textBackgroundColor: bankTextRowBackgroundColor(isExpanded: true, rowIndex: 2),
                iconBackgroundColor: balanceRowBackgroundColor(isExpanded: true)
            ),
            CashierReferenceBankRowLayout(
                id: "expanded_bank_3",
                iconCenterY: 713,
                textCenterY: 709,
                checkmarkCenterY: 710,
                textFontSize: 14.8,
                textBackgroundColor: bankTextRowBackgroundColor(isExpanded: true, rowIndex: 3),
                iconBackgroundColor: balanceRowBackgroundColor(isExpanded: true)
            )
        ]
    }

    private var collapsedBankRows: [CashierPayToolData] {
        Array(filteredBankTools.prefix(2))
    }

    private var expandedBankRows: [CashierPayToolData] {
        Array(filteredBankTools.prefix(4))
    }

    private var filteredBankTools: [CashierPayToolData] {
        CashierPresentationHelper.filteredBankTools(payTools, policy: sceneConfiguration.bankCardPolicy)
    }

    private var selectedPaymentMethodType: RepayPaymentMethodType {
        if isBalanceSelected {
            return .wallet
        }
        if isFundSelected {
            return .fund
        }
        return .bankCard
    }

    private var selectedBankTool: CashierPayToolData? {
        guard !isBalanceSelected, !isFundSelected else {
            return nil
        }
        if let selectedBankToolCode {
            return filteredBankTools.first(where: { $0.toolCode == selectedBankToolCode })
        }
        return filteredBankTools.first(where: \.defaultSelected) ?? filteredBankTools.first
    }

    private func rowCenterY(at index: Int, from values: [CGFloat]) -> CGFloat? {
        guard values.indices.contains(index) else {
            return nil
        }
        return values[index]
    }

    private func payToolCheckmarkSlot(centerY: CGFloat, scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        Image(systemName: "checkmark")
            .font(.system(size: 20 * fontScale, weight: .semibold))
            .foregroundStyle(AppTheme.palette.brandPrimary)
            .frame(width: 24 * scaleX, height: 24 * scaleY)
            .position(x: 357 * scaleX, y: centerY * scaleY)
    }

    private func cashierBalanceOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        cashierCollapsedBalanceAmountOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
    }

    private func cashierCollapsedBalanceAmountOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        let balanceText = isLoadingWalletBalance ? "加载中..." : walletAvailableAmountText
        return cashierTextReplacementSlot(
            text: balanceText,
            centerX: 198,
            centerY: collapsedBalanceRowTapCenterY - 8,
            width: 140,
            height: 24,
            scaleX: scaleX,
            scaleY: scaleY,
            font: yaHeiRegularFont(size: 13.5 * fontScale),
            textColor: Color(red: 0.54, green: 0.54, blue: 0.56)
        )
    }

    private func cashierExpandedBalanceRowOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        let backgroundColor = balanceRowBackgroundColor(isExpanded: true)
        let detailText: String = {
            if isLoadingWalletBalance {
                return "可用: 加载中..."
            }
            if walletAvailableAmountText == "--" {
                return "可用: --"
            }
            return "可用: \(walletAvailableAmountText)"
        }()

        return ZStack {
            cashierTextReplacementSlot(
                text: "账户余额",
                centerX: 184,
                centerY: 409,
                width: 156,
                height: 28,
                scaleX: scaleX,
                scaleY: scaleY,
                font: yaHeiRegularFont(size: 17 * fontScale),
                textColor: Color(red: 0.22, green: 0.22, blue: 0.24),
                backgroundColor: backgroundColor
            )

            cashierTextReplacementSlot(
                text: detailText,
                centerX: 222,
                centerY: 437,
                width: 250,
                height: 28,
                scaleX: scaleX,
                scaleY: scaleY,
                font: yaHeiRegularFont(size: 15.5 * fontScale),
                textColor: Color(red: 0.57, green: 0.57, blue: 0.58),
                backgroundColor: backgroundColor
            )

            if isBalanceSelected {
                Rectangle()
                    .fill(backgroundColor)
                    .frame(width: 118 * scaleX, height: 42 * scaleY)
                    .position(x: 343 * scaleX, y: expandedBalanceCheckmarkCenterY * scaleY)
            }
        }
    }

    private func balanceRowBackgroundColor(isExpanded: Bool) -> Color {
        if isExpanded {
            return Color(red: 245 / 255, green: 248 / 255, blue: 249 / 255)
        }
        return Color(red: 245 / 255, green: 248 / 255, blue: 249 / 255)
    }

    private func bankTextRowBackgroundColor(isExpanded: Bool, rowIndex: Int) -> Color {
        let lastExpandedRowIndex = max(expandedBankRowTapCenterY.count - 1, 0)
        if isExpanded && rowIndex == lastExpandedRowIndex {
            return Color(red: 250 / 255, green: 251 / 255, blue: 252 / 255)
        }
        return balanceRowBackgroundColor(isExpanded: isExpanded)
    }


    private func yaHeiRegularFont(size: CGFloat) -> Font {
        let candidates = [
            "MicrosoftYaHei",
            "Microsoft YaHei",
            "MicrosoftYaHei-Light",
            "MicrosoftYaHeiUI",
            "Microsoft YaHei UI",
            "微软雅黑"
        ]
        for name in candidates where UIFont(name: name, size: size) != nil {
            return .custom(name, size: size)
        }
        return .system(size: size, weight: .regular)
    }

    private func cashierTextReplacementSlot(
        text: String,
        centerX: CGFloat,
        centerY: CGFloat,
        width: CGFloat,
        height: CGFloat,
        scaleX: CGFloat,
        scaleY: CGFloat,
        font: Font,
        textColor: Color,
        backgroundColor: Color = .clear
    ) -> some View {
        ZStack(alignment: .leading) {
            Rectangle()
                .fill(backgroundColor)

            if !text.isEmpty {
                Text(text)
                    .font(font)
                    .foregroundStyle(textColor)
                    .lineLimit(1)
                    .minimumScaleFactor(0.75)
                    .padding(.leading, 8 * min(scaleX, scaleY))
            }
        }
        .frame(width: width * scaleX, height: height * scaleY, alignment: .leading)
        .position(x: centerX * scaleX, y: centerY * scaleY)
    }

    private func cashierTrailingTextReplacementSlot(
        text: String,
        centerX: CGFloat,
        centerY: CGFloat,
        width: CGFloat,
        height: CGFloat,
        scaleX: CGFloat,
        scaleY: CGFloat,
        font: Font,
        textColor: Color,
        backgroundColor: Color = .clear
    ) -> some View {
        ZStack(alignment: .trailing) {
            Rectangle()
                .fill(backgroundColor)

            if !text.isEmpty {
                Text(text)
                    .font(font)
                    .foregroundStyle(textColor)
                    .lineLimit(1)
                    .minimumScaleFactor(0.75)
                    .padding(.trailing, 8 * min(scaleX, scaleY))
            }
        }
        .frame(width: width * scaleX, height: height * scaleY, alignment: .trailing)
        .position(x: centerX * scaleX, y: centerY * scaleY)
    }

    private func formatAmount(_ amount: Decimal) -> String {
        String(format: "%.2f", NSDecimalNumber(decimal: amount).doubleValue)
    }

    private func formatAmountWithGrouping(_ amount: Decimal) -> String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "zh_CN")
        formatter.numberStyle = .decimal
        formatter.usesGroupingSeparator = true
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        let number = NSDecimalNumber(decimal: amount)
        return formatter.string(from: number) ?? number.stringValue
    }

    private func formatWalletAvailableAmount(_ raw: String?) -> String {
        let trimmed = raw?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard !trimmed.isEmpty,
              let decimal = Decimal(string: trimmed.replacingOccurrences(of: ",", with: "")) else {
            return "--"
        }
        return "¥" + formatAmountWithGrouping(decimal)
    }

    private func hotspot(
        identifier: String,
        centerX: CGFloat,
        centerY: CGFloat,
        width: CGFloat,
        height: CGFloat,
        isDisabled: Bool = false,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Color.clear
                .frame(width: width, height: height)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .disabled(isDisabled)
        .accessibilityIdentifier(identifier)
        .position(x: centerX, y: centerY)
    }

    @MainActor
    private func submitTransfer() async {
        guard !isSubmitting else {
            return
        }
        guard let payerUserId else {
            errorMessage = "未找到当前登录用户，请重新登录"
            return
        }

        isSubmitting = true
        errorMessage = nil

        let paymentMethod: String
        let paymentToolCode: String?
        let paymentMethodDisplayText: String
        if isBalanceSelected {
            paymentMethod = "WALLET"
            paymentToolCode = nil
            paymentMethodDisplayText = "账户余额"
        } else if isFundSelected {
            paymentMethod = "FUND_ACCOUNT"
            paymentToolCode = nil
            paymentMethodDisplayText = "爱存"
        } else {
            guard let selectedBankTool else {
                isSubmitting = false
                errorMessage = payToolsErrorMessage ?? "请选择借记卡"
                return
            }
            paymentMethod = "BANK_CARD"
            paymentToolCode = selectedBankTool.toolCode
            paymentMethodDisplayText = successPaymentMethodDisplayText(for: selectedBankTool)
        }

        do {
            let submitRequest = CashierTransferSubmitRequest(
                payerUserId: payerUserId,
                payeeUserId: contact.payeeUserId,
                amount: transferAmount,
                paymentMethod: paymentMethod,
                paymentToolCode: paymentToolCode,
                remark: transferRemark.trimmingCharacters(in: .whitespacesAndNewlines)
            )
            let sentMessage: MessageData?
            if let submitTransferAction {
                sentMessage = try await submitTransferAction(submitRequest)
            } else {
                _ = try await APIClient.shared.createTransferTrade(
                    payerUserId: submitRequest.payerUserId,
                    payeeUserId: submitRequest.payeeUserId,
                    amount: submitRequest.amount,
                    currencyCode: "CNY",
                    paymentMethod: submitRequest.paymentMethod,
                    paymentToolCode: submitRequest.paymentToolCode,
                    metadata: submitRequest.remark
                )
                sentMessage = nil
            }
            isSubmitting = false
            onPaySucceeded(paymentMethodDisplayText, sentMessage)
        } catch {
            isSubmitting = false
            errorMessage = userFacingErrorMessage(error)
        }
    }

    private func successPaymentMethodDisplayText(for tool: CashierPayToolData) -> String {
        CashierPresentationHelper.bankDisplayName(for: tool)
    }

    @MainActor
    private func loadCashierPayTools() async {
        guard let payerUserId, payerUserId > 0 else {
            payTools = []
            selectedBankToolCode = nil
            payToolsErrorMessage = "未找到当前登录用户"
            isBalanceSelected = true
            isFundSelected = false
            return
        }

        isLoadingPayTools = true
        payToolsErrorMessage = nil
        do {
            let cashierView = try await APIClient.shared.fetchCashierView(userId: payerUserId, sceneCode: "TRANSFER")
            let resolvedServerSceneConfiguration = CashierSceneConfiguration.fromServerData(
                cashierView.sceneConfig,
                fallback: .transfer
            )
            let normalizedTransferSceneConfiguration = CashierSceneConfiguration(
                supportedChannels: CashierSceneConfiguration.transfer.supportedChannels,
                bankCardPolicy: CashierSceneConfiguration.transfer.bankCardPolicy,
                emptyBankCardText: resolvedServerSceneConfiguration.emptyBankCardText
            )
            sceneConfiguration = normalizedTransferSceneConfiguration
            let bankTools = CashierPresentationHelper.filteredBankTools(
                cashierView.payTools,
                policy: normalizedTransferSceneConfiguration.bankCardPolicy
            )
            payTools = bankTools
            if let selectedBankToolCode,
               bankTools.contains(where: { $0.toolCode == selectedBankToolCode }) {
                // keep user selection
            } else {
                selectedBankToolCode = bankTools.first(where: \.defaultSelected)?.toolCode ?? bankTools.first?.toolCode
            }
            if bankTools.isEmpty {
                selectedBankToolCode = nil
                payToolsErrorMessage = normalizedTransferSceneConfiguration.emptyBankCardText
                if normalizedTransferSceneConfiguration.supports(.wallet) {
                    isBalanceSelected = true
                    isFundSelected = false
                } else if normalizedTransferSceneConfiguration.supports(.fund) {
                    isBalanceSelected = false
                    isFundSelected = true
                } else {
                    isBalanceSelected = false
                    isFundSelected = false
                }
            } else if !normalizedTransferSceneConfiguration.supports(.wallet) {
                isBalanceSelected = false
                isFundSelected = normalizedTransferSceneConfiguration.supports(.fund)
            } else if isBalanceSelected {
                isFundSelected = false
            }
        } catch {
            payTools = []
            selectedBankToolCode = nil
            payToolsErrorMessage = userFacingErrorMessage(error)
            isBalanceSelected = true
            isFundSelected = false
        }
        isLoadingPayTools = false
    }

    @MainActor
    private func loadCashierWalletBalance() async {
        guard let payerUserId, payerUserId > 0 else {
            walletAvailableAmountText = "--"
            return
        }

        isLoadingWalletBalance = true
        do {
            let overview = try await APIClient.shared.fetchAssetOverview(userId: String(payerUserId))
            walletAvailableAmountText = formatWalletAvailableAmount(overview.availableAmount)
        } catch {
            walletAvailableAmountText = "--"
        }
        isLoadingWalletBalance = false
    }

    @MainActor
    private func loadCashierFundBalance() async {
        guard let payerUserId, payerUserId > 0 else {
            fundAvailableAmountText = "--"
            return
        }

        isLoadingFundBalance = true
        defer { isLoadingFundBalance = false }
        do {
            let account = try await APIClient.shared.fetchFundAccount(userId: payerUserId)
            fundAvailableAmountText = "¥" + formatAmountWithGrouping(account.availableAmount)
        } catch {
            fundAvailableAmountText = "--"
        }
    }
    }

struct TransferSuccessView: View {
    let contact: TransferRecentContact
    let transferAmount: Decimal
    let transferRemark: String
    let paymentMethodText: String
    let onBackHome: () -> Void
    let onDone: () -> Void

    private let baseWidth: CGFloat = 402
    private let baseHeight: CGFloat = 874

    var body: some View {
        GeometryReader { proxy in
            let scaleX = proxy.size.width / baseWidth
            let scaleY = proxy.size.height / baseHeight
            let fontScale = min(scaleX, scaleY)

            ZStack {
                Image("TransferSuccessReference")
                    .resizable()
                    .interpolation(.high)
                    .aspectRatio(contentMode: .fill)
                    .frame(width: proxy.size.width, height: proxy.size.height)
                    .clipped()

                successDynamicOverlay(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)

                hotspot(
                    identifier: "transfer_success_back_home",
                    centerX: 359 * scaleX,
                    centerY: 93 * scaleY,
                    width: 78 * scaleX,
                    height: 35 * scaleY,
                    action: onBackHome
                )

                hotspot(
                    identifier: "transfer_success_done",
                    centerX: 201 * scaleX,
                    centerY: 822 * scaleY,
                    width: 248 * scaleX,
                    height: 58 * scaleY,
                    action: onDone
                )
            }
            .frame(width: proxy.size.width, height: proxy.size.height)
        }
        .ignoresSafeArea()
    }

    private func successDynamicOverlay(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        let amountText = "¥\(formatAmount(transferAmount))"
        let payeeText = contact.listDisplayName
        let paymentMethodDisplayText = paymentMethodText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            ? "账户余额"
            : paymentMethodText.trimmingCharacters(in: .whitespacesAndNewlines)
        let textBlockOffsetY: CGFloat = 100
        let leftLabelCenterX: CGFloat = 68
        let leftLabelTextWidth: CGFloat = 96
        let rowPayeeCenterY: CGFloat = 199 + textBlockOffsetY
        let rowPaymentCenterY: CGFloat = 225 + textBlockOffsetY
        let topPayeeCenterX: CGFloat = 306
        let topPayeeCenterY: CGFloat = rowPayeeCenterY
        let paymentMethodCenterX: CGFloat = 306
        let paymentMethodCenterY: CGFloat = rowPaymentCenterY
        let topInfoTextWidth: CGFloat = 144

        return ZStack {
            Text(amountText)
                .font(.system(size: 85.5 * fontScale, weight: .medium))
                .foregroundStyle(Color.white.opacity(0.95))
                .lineLimit(1)
                .minimumScaleFactor(0.38)
                .frame(width: 320 * scaleX)
                .position(x: 201 * scaleX, y: (126 + textBlockOffsetY) * scaleY)

            Text("收款方")
                .font(.system(size: 14 * fontScale, weight: .medium))
                .foregroundStyle(Color.white.opacity(0.90))
                .lineLimit(1)
                .frame(width: leftLabelTextWidth * scaleX, alignment: .leading)
                .position(x: leftLabelCenterX * scaleX, y: rowPayeeCenterY * scaleY)

            Text("付款方式")
                .font(.system(size: 14 * fontScale, weight: .medium))
                .foregroundStyle(Color.white.opacity(0.90))
                .lineLimit(1)
                .frame(width: leftLabelTextWidth * scaleX, alignment: .leading)
                .position(x: leftLabelCenterX * scaleX, y: rowPaymentCenterY * scaleY)

            Text(payeeText)
                .font(.system(size: 14 * fontScale, weight: .medium))
                .foregroundStyle(Color.white.opacity(0.98))
                .lineLimit(1)
                .minimumScaleFactor(0.55)
                .frame(width: topInfoTextWidth * scaleX, alignment: .trailing)
                .position(x: topPayeeCenterX * scaleX, y: topPayeeCenterY * scaleY)

            Text(paymentMethodDisplayText)
                .font(.system(size: 14 * fontScale, weight: .medium))
                .foregroundStyle(Color.white.opacity(0.98))
                .lineLimit(1)
                .minimumScaleFactor(0.52)
                .frame(width: topInfoTextWidth * scaleX, alignment: .trailing)
                .position(x: paymentMethodCenterX * scaleX, y: paymentMethodCenterY * scaleY)
        }
        .allowsHitTesting(false)
    }

    private func hotspot(
        identifier: String,
        centerX: CGFloat,
        centerY: CGFloat,
        width: CGFloat,
        height: CGFloat,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Color.clear
                .frame(width: width, height: height)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier(identifier)
        .position(x: centerX, y: centerY)
    }

    private func formatAmount(_ amount: Decimal) -> String {
        String(format: "%.2f", NSDecimalNumber(decimal: amount).doubleValue)
    }

    }
