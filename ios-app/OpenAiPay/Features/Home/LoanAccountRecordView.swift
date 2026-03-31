import SwiftUI
import Foundation

struct LoanAccountRecordBorrowItem: Identifiable, Equatable {
    let id: String
    let amount: Decimal
    let occurredAt: Date
    let statusText: String
}

struct LoanAccountRecordRepaymentItem: Identifiable, Equatable {
    let id: String
    let amount: Decimal
    let occurredAt: Date
    let statusText: String
}

enum LoanAccountRecordHistoryStore {
    private struct PersistedBorrowItem: Codable {
        let borrowId: String
        let amount: String
        let occurredAt: TimeInterval
        var statusText: String

        private enum CodingKeys: String, CodingKey {
            case borrowId
            case amount
            case occurredAt
            case statusText
            case status
        }

        init(
            borrowId: String,
            amount: String,
            occurredAt: TimeInterval,
            statusText: String
        ) {
            self.borrowId = borrowId
            self.amount = amount
            self.occurredAt = occurredAt
            self.statusText = statusText
        }

        init(from decoder: Decoder) throws {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            borrowId = LoanAccountRecordHistoryStore.decodeFlexibleString(from: container, key: .borrowId) ?? ""
            amount = LoanAccountRecordHistoryStore.decodeFlexibleString(from: container, key: .amount) ?? "0"
            occurredAt = LoanAccountRecordHistoryStore.decodeFlexibleTimeInterval(from: container, key: .occurredAt) ?? 0
            statusText = LoanAccountRecordHistoryStore.decodeFlexibleString(from: container, key: .statusText)
                ?? LoanAccountRecordHistoryStore.decodeFlexibleString(from: container, key: .status)
                ?? "待还款"
        }

        func encode(to encoder: Encoder) throws {
            var container = encoder.container(keyedBy: CodingKeys.self)
            try container.encode(borrowId, forKey: .borrowId)
            try container.encode(amount, forKey: .amount)
            try container.encode(occurredAt, forKey: .occurredAt)
            try container.encode(statusText, forKey: .statusText)
        }
    }

    private struct PersistedRepaymentItem: Codable {
        let repaymentId: String
        let borrowId: String?
        let amount: String
        let occurredAt: TimeInterval
        var statusText: String

        private enum CodingKeys: String, CodingKey {
            case repaymentId
            case borrowId
            case amount
            case occurredAt
            case statusText
            case status
        }

        init(
            repaymentId: String,
            borrowId: String?,
            amount: String,
            occurredAt: TimeInterval,
            statusText: String
        ) {
            self.repaymentId = repaymentId
            self.borrowId = borrowId
            self.amount = amount
            self.occurredAt = occurredAt
            self.statusText = statusText
        }

        init(from decoder: Decoder) throws {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            repaymentId = LoanAccountRecordHistoryStore.decodeFlexibleString(from: container, key: .repaymentId) ?? ""
            borrowId = LoanAccountRecordHistoryStore.decodeFlexibleString(from: container, key: .borrowId)
            amount = LoanAccountRecordHistoryStore.decodeFlexibleString(from: container, key: .amount) ?? "0"
            occurredAt = LoanAccountRecordHistoryStore.decodeFlexibleTimeInterval(from: container, key: .occurredAt) ?? 0
            statusText = LoanAccountRecordHistoryStore.decodeFlexibleString(from: container, key: .statusText)
                ?? LoanAccountRecordHistoryStore.decodeFlexibleString(from: container, key: .status)
                ?? "已还款"
        }

        func encode(to encoder: Encoder) throws {
            var container = encoder.container(keyedBy: CodingKeys.self)
            try container.encode(repaymentId, forKey: .repaymentId)
            try container.encodeIfPresent(borrowId, forKey: .borrowId)
            try container.encode(amount, forKey: .amount)
            try container.encode(occurredAt, forKey: .occurredAt)
            try container.encode(statusText, forKey: .statusText)
        }
    }

    private struct PersistedState: Codable {
        var borrowItems: [PersistedBorrowItem]
        var repaymentItems: [PersistedRepaymentItem]

        private enum CodingKeys: String, CodingKey {
            case borrowItems
            case repaymentItems
        }

        init(
            borrowItems: [PersistedBorrowItem],
            repaymentItems: [PersistedRepaymentItem]
        ) {
            self.borrowItems = borrowItems
            self.repaymentItems = repaymentItems
        }

        init(from decoder: Decoder) throws {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            borrowItems = (try? container.decode([PersistedBorrowItem].self, forKey: .borrowItems)) ?? []
            repaymentItems = (try? container.decode([PersistedRepaymentItem].self, forKey: .repaymentItems)) ?? []
        }
    }

    private static let posixLocale = Locale(identifier: "en_US_POSIX")
    private static let historyKeyPrefix = "cn.openaipay.loan-account.record.history."

    private static func key(for userId: Int64?) -> String? {
        guard let userId, userId > 0 else {
            return nil
        }
        return historyKeyPrefix + String(userId)
    }

    static func recordBorrow(
        userId: Int64?,
        borrowId: String,
        amount: Decimal,
        borrowedAt: Date,
        statusText: String
    ) {
        guard !borrowId.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
              let key = key(for: userId) else {
            return
        }

        var state = loadState(forKey: key) ?? PersistedState(borrowItems: [], repaymentItems: [])
        let normalizedAmount = max(.zero, amount)
        let nextItem = PersistedBorrowItem(
            borrowId: borrowId,
            amount: decimalString(normalizedAmount),
            occurredAt: borrowedAt.timeIntervalSince1970,
            statusText: normalizedStatusText(statusText, fallback: "待还款")
        )

        if let existingIndex = state.borrowItems.firstIndex(where: { $0.borrowId == borrowId }) {
            state.borrowItems[existingIndex] = nextItem
        } else {
            state.borrowItems.append(nextItem)
        }
        state.borrowItems.sort { $0.occurredAt > $1.occurredAt }
        saveState(state, forKey: key)
    }

    static func markBorrowSettled(userId: Int64?, borrowId: String) {
        guard let key = key(for: userId),
              var state = loadState(forKey: key),
              let existingIndex = state.borrowItems.firstIndex(where: { $0.borrowId == borrowId }) else {
            return
        }

        state.borrowItems[existingIndex].statusText = "已结清"
        saveState(state, forKey: key)
    }

    static func loadBorrowItems(userId: Int64?) -> [LoanAccountRecordBorrowItem] {
        guard let key = key(for: userId),
              let state = loadState(forKey: key) else {
            return []
        }

        return state.borrowItems.compactMap { item in
            let amount = Decimal(string: item.amount, locale: posixLocale) ?? .zero
            guard amount >= .zero else {
                return nil
            }
            return LoanAccountRecordBorrowItem(
                id: item.borrowId,
                amount: amount,
                occurredAt: Date(timeIntervalSince1970: item.occurredAt),
                statusText: normalizedStatusText(item.statusText, fallback: "待还款")
            )
        }
        .sorted { $0.occurredAt > $1.occurredAt }
    }

    static func recordRepayment(
        userId: Int64?,
        repaymentId: String,
        borrowId: String?,
        amount: Decimal,
        occurredAt: Date,
        statusText: String = "已还款"
    ) {
        guard !repaymentId.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
              let key = key(for: userId) else {
            return
        }

        var state = loadState(forKey: key) ?? PersistedState(borrowItems: [], repaymentItems: [])
        let nextItem = PersistedRepaymentItem(
            repaymentId: repaymentId,
            borrowId: normalizedOptionalText(borrowId),
            amount: decimalString(max(.zero, amount)),
            occurredAt: occurredAt.timeIntervalSince1970,
            statusText: normalizedStatusText(statusText, fallback: "已还款")
        )

        if let existingIndex = state.repaymentItems.firstIndex(where: { $0.repaymentId == repaymentId }) {
            state.repaymentItems[existingIndex] = nextItem
        } else {
            state.repaymentItems.append(nextItem)
        }
        state.repaymentItems.sort { $0.occurredAt > $1.occurredAt }
        saveState(state, forKey: key)
    }

    static func loadRepaymentItems(userId: Int64?) -> [LoanAccountRecordRepaymentItem] {
        guard let key = key(for: userId),
              let state = loadState(forKey: key) else {
            return []
        }

        return state.repaymentItems.compactMap { item in
            let amount = Decimal(string: item.amount, locale: posixLocale) ?? .zero
            guard amount >= .zero else {
                return nil
            }
            return LoanAccountRecordRepaymentItem(
                id: item.repaymentId,
                amount: amount,
                occurredAt: Date(timeIntervalSince1970: item.occurredAt),
                statusText: normalizedStatusText(item.statusText, fallback: "已还款")
            )
        }
        .sorted { $0.occurredAt > $1.occurredAt }
    }

    private static func loadState(forKey key: String) -> PersistedState? {
        return loadRawState(forKey: key)
    }

    private static func loadRawState(forKey key: String) -> PersistedState? {
        guard let data = UserDefaults.standard.data(forKey: key),
              let state = try? JSONDecoder().decode(PersistedState.self, from: data) else {
            return nil
        }
        return state
    }

    private static func saveState(_ state: PersistedState, forKey key: String) {
        guard let data = try? JSONEncoder().encode(state) else {
            return
        }
        UserDefaults.standard.set(data, forKey: key)
    }

    private static func decimalString(_ value: Decimal) -> String {
        NSDecimalNumber(decimal: value).stringValue
    }

    private static func normalizedOptionalText(_ value: String?) -> String? {
        let trimmed = value?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return trimmed.isEmpty ? nil : trimmed
    }

    private static func normalizedStatusText(_ value: String?, fallback: String) -> String {
        normalizedOptionalText(value) ?? fallback
    }

    private static func decodeFlexibleString<K: CodingKey>(
        from container: KeyedDecodingContainer<K>,
        key: K
    ) -> String? {
        guard container.contains(key) else {
            return nil
        }
        if (try? container.decodeNil(forKey: key)) == true {
            return nil
        }

        if let text = try? container.decode(String.self, forKey: key) {
            let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? nil : trimmed
        }
        if let intValue = try? container.decode(Int.self, forKey: key) {
            return String(intValue)
        }
        if let int64Value = try? container.decode(Int64.self, forKey: key) {
            return String(int64Value)
        }
        if let doubleValue = try? container.decode(Double.self, forKey: key) {
            return NSDecimalNumber(value: doubleValue).stringValue
        }
        return nil
    }

    private static func decodeFlexibleTimeInterval<K: CodingKey>(
        from container: KeyedDecodingContainer<K>,
        key: K
    ) -> TimeInterval? {
        guard container.contains(key) else {
            return nil
        }
        if (try? container.decodeNil(forKey: key)) == true {
            return nil
        }

        if let doubleValue = try? container.decode(Double.self, forKey: key) {
            return doubleValue
        }
        if let intValue = try? container.decode(Int.self, forKey: key) {
            return TimeInterval(intValue)
        }
        if let int64Value = try? container.decode(Int64.self, forKey: key) {
            return TimeInterval(int64Value)
        }
        if let textValue = try? container.decode(String.self, forKey: key) {
            let normalizedText = textValue.trimmingCharacters(in: .whitespacesAndNewlines)
            if !normalizedText.isEmpty, let parsed = TimeInterval(normalizedText) {
                return parsed
            }
        }
        return nil
    }
}

private enum LoanAccountRecordTab: String {
    case borrow
    case repay

    var title: String {
        switch self {
        case .borrow:
            return "借款记录"
        case .repay:
            return "还款记录"
        }
    }
}

struct LoanAccountRecordView: View {
    let borrowRecords: [LoanAccountRecordBorrowItem]
    let repaymentRecords: [LoanAccountRecordRepaymentItem]
    let onBack: () -> Void
    let onOpenPendingBorrowRepay: (LoanAccountRecordBorrowItem) -> Void

    @State private var selectedTab: LoanAccountRecordTab = .borrow
    @State private var visibleBorrowCount = 8
    @State private var visibleRepaymentCount = 8

    private let baseWidth: CGFloat = 402
    private let baseHeight: CGFloat = 874
    private let selectedTabColor = Color(red: 41 / 255, green: 37 / 255, blue: 135 / 255)
    private let tabBorderColor = Color(red: 81 / 255, green: 75 / 255, blue: 163 / 255)
    private let primaryTextColor = Color(red: 0.17, green: 0.17, blue: 0.18)
    private let secondaryTextColor = Color(red: 0.63, green: 0.63, blue: 0.65)
    private let dividerColor = Color(red: 0.93, green: 0.93, blue: 0.94)
    private let footerBackgroundColor = Color(red: 0.94, green: 0.95, blue: 0.96)

    private var displayedBorrowRecords: [LoanAccountRecordBorrowItem] {
        Array(borrowRecords.prefix(max(visibleBorrowCount, 0)))
    }

    private var displayedRepaymentRecords: [LoanAccountRecordRepaymentItem] {
        Array(repaymentRecords.prefix(max(visibleRepaymentCount, 0)))
    }

    private var hasMoreBorrowRecords: Bool {
        borrowRecords.count > displayedBorrowRecords.count
    }

    private var hasMoreRepaymentRecords: Bool {
        repaymentRecords.count > displayedRepaymentRecords.count
    }

    var body: some View {
        GeometryReader { proxy in
            let scaleX = proxy.size.width / baseWidth
            let scaleY = proxy.size.height / baseHeight
            let fontScale = min(scaleX, scaleY)

            ZStack {
                Color.white
                    .ignoresSafeArea()

                VStack(spacing: 0) {
                    topBar(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                        .padding(.top, 46 * scaleY)
                        .padding(.horizontal, 8 * scaleX)

                    segmentControl(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                        .padding(.top, 18 * scaleY)
                        .padding(.horizontal, 8 * scaleX)

                    Divider()
                        .overlay(dividerColor)
                        .padding(.top, 18 * scaleY)

                    contentList(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                }
            }
            .frame(width: proxy.size.width, height: proxy.size.height)
        }
        .ignoresSafeArea()
    }

    private func topBar(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        HStack {
            Button(action: onBack) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 19 * fontScale, weight: .medium))
                    .foregroundStyle(primaryTextColor)
                    .frame(width: 44 * scaleX, height: 40 * scaleY)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityIdentifier("loan_account_record_back")

            Spacer(minLength: 0)

            Text(selectedTab.title)
                .font(.system(size: 22 * fontScale, weight: .semibold))
                .foregroundStyle(primaryTextColor)

            Spacer(minLength: 0)

            Color.clear
                .frame(width: 88 * scaleX, height: 40 * scaleY)
        }
    }

    private func segmentControl(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        ZStack(alignment: selectedTab == .borrow ? .leading : .trailing) {
            RoundedRectangle(cornerRadius: 7 * fontScale, style: .continuous)
                .stroke(tabBorderColor, lineWidth: max(1, 1 * fontScale))
                .background(
                    RoundedRectangle(cornerRadius: 7 * fontScale, style: .continuous)
                        .fill(Color.white)
                )

            if selectedTab == .borrow {
                UnevenRoundedRectangle(
                    cornerRadii: .init(
                        topLeading: 7 * fontScale,
                        bottomLeading: 7 * fontScale,
                        bottomTrailing: 0,
                        topTrailing: 0
                    ),
                    style: .continuous
                )
                .fill(selectedTabColor)
                .frame(width: 189 * scaleX, height: 32 * scaleY)
            } else {
                UnevenRoundedRectangle(
                    cornerRadii: .init(
                        topLeading: 0,
                        bottomLeading: 0,
                        bottomTrailing: 7 * fontScale,
                        topTrailing: 7 * fontScale
                    ),
                    style: .continuous
                )
                .fill(selectedTabColor)
                .frame(width: 189 * scaleX, height: 32 * scaleY)
            }

            HStack(spacing: 0) {
                tabButton(.borrow, scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                tabButton(.repay, scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
            }
        }
        .frame(height: 32 * scaleY)
        .accessibilityIdentifier("loan_account_record_tabs")
    }

    private func tabButton(
        _ tab: LoanAccountRecordTab,
        scaleX: CGFloat,
        scaleY: CGFloat,
        fontScale: CGFloat
    ) -> some View {
        let isSelected = selectedTab == tab
        return Button(action: {
            selectedTab = tab
        }) {
            Text(tab.title)
                .font(.system(size: 15.5 * fontScale, weight: .medium))
                .foregroundStyle(isSelected ? Color.white : tabBorderColor)
                .frame(width: 189 * scaleX, height: 32 * scaleY)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier("loan_account_record_tab_\(tab.rawValue)")
    }

    @ViewBuilder
    private func contentList(scaleX: CGFloat, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        let borrowEmpty = borrowRecords.isEmpty
        let repaymentEmpty = repaymentRecords.isEmpty

        if selectedTab == .borrow && borrowEmpty {
            emptyState(text: "暂无借款记录", scaleY: scaleY, fontScale: fontScale)
        } else if selectedTab == .repay && repaymentEmpty {
            emptyState(text: "暂无还款记录", scaleY: scaleY, fontScale: fontScale)
        } else {
            ScrollView(showsIndicators: false) {
                LazyVStack(spacing: 0) {
                    if selectedTab == .borrow {
                        ForEach(displayedBorrowRecords) { item in
                            borrowRow(item: item, scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                        }
                        if hasMoreBorrowRecords {
                            loadMoreButton(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale) {
                                visibleBorrowCount += 8
                            }
                        }
                    } else {
                        ForEach(displayedRepaymentRecords) { item in
                            repaymentRow(item: item, scaleX: scaleX, scaleY: scaleY, fontScale: fontScale)
                        }
                        if hasMoreRepaymentRecords {
                            loadMoreButton(scaleX: scaleX, scaleY: scaleY, fontScale: fontScale) {
                                visibleRepaymentCount += 8
                            }
                        }
                    }
                }
            }
        }
    }

    private func borrowRow(
        item: LoanAccountRecordBorrowItem,
        scaleX: CGFloat,
        scaleY: CGFloat,
        fontScale: CGFloat
    ) -> some View {
        HStack(spacing: 12 * scaleX) {
            VStack(alignment: .leading, spacing: 6 * scaleY) {
                Text(formattedAmount(item.amount))
                    .font(.system(size: 18 * fontScale, weight: .semibold))
                    .foregroundStyle(primaryTextColor)

                Text(formattedDate(item.occurredAt))
                    .font(.system(size: 13 * fontScale, weight: .regular))
                    .foregroundStyle(secondaryTextColor)
            }

            Spacer(minLength: 12 * scaleX)

            if item.statusText == "待还款" {
                Button(action: {
                    onOpenPendingBorrowRepay(item)
                }) {
                    HStack(spacing: 4 * scaleX) {
                        Text(item.statusText)
                            .font(.system(size: 16 * fontScale, weight: .regular))
                            .foregroundStyle(secondaryTextColor)

                        Image(systemName: "chevron.right")
                            .font(.system(size: 13 * fontScale, weight: .medium))
                            .foregroundStyle(Color(red: 0.82, green: 0.82, blue: 0.84))
                    }
                    .frame(minWidth: 72 * scaleX, alignment: .trailing)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("loan_account_record_pending_repay_\(item.id)")
            } else {
                Text(item.statusText)
                    .font(.system(size: 16 * fontScale, weight: .regular))
                    .foregroundStyle(secondaryTextColor)

                if item.statusText != "已结清" {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 13 * fontScale, weight: .medium))
                        .foregroundStyle(Color(red: 0.82, green: 0.82, blue: 0.84))
                }
            }
        }
        .padding(.horizontal, 16 * scaleX)
        .frame(maxWidth: .infinity)
        .frame(height: 66 * scaleY, alignment: .center)
        .background(Color.white)
        .overlay(alignment: .bottom) {
            Divider().overlay(dividerColor)
        }
    }

    private func repaymentRow(
        item: LoanAccountRecordRepaymentItem,
        scaleX: CGFloat,
        scaleY: CGFloat,
        fontScale: CGFloat
    ) -> some View {
        HStack(spacing: 12 * scaleX) {
            VStack(alignment: .leading, spacing: 6 * scaleY) {
                Text(formattedAmount(item.amount))
                    .font(.system(size: 18 * fontScale, weight: .semibold))
                    .foregroundStyle(primaryTextColor)

                Text(formattedDate(item.occurredAt))
                    .font(.system(size: 13 * fontScale, weight: .regular))
                    .foregroundStyle(secondaryTextColor)
            }

            Spacer(minLength: 12 * scaleX)

            Text(item.statusText)
                .font(.system(size: 16 * fontScale, weight: .regular))
                .foregroundStyle(secondaryTextColor)
        }
        .padding(.horizontal, 16 * scaleX)
        .frame(maxWidth: .infinity)
        .frame(height: 66 * scaleY, alignment: .center)
        .background(Color.white)
        .overlay(alignment: .bottom) {
            Divider().overlay(dividerColor)
        }
    }

    private func loadMoreButton(
        scaleX: CGFloat,
        scaleY: CGFloat,
        fontScale: CGFloat,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Text("点击加载更多")
                .font(.system(size: 15 * fontScale, weight: .medium))
                .foregroundStyle(Color(red: 0.46, green: 0.46, blue: 0.48))
                .frame(maxWidth: .infinity)
                .frame(height: 46 * scaleY)
                .background(footerBackgroundColor)
        }
        .buttonStyle(.plain)
        .padding(.top, 4 * scaleY)
    }

    private func emptyState(text: String, scaleY: CGFloat, fontScale: CGFloat) -> some View {
        VStack {
            Spacer(minLength: 0)
            Text(text)
                .font(.system(size: 15 * fontScale, weight: .regular))
                .foregroundStyle(secondaryTextColor)
            Spacer(minLength: 0)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(.bottom, 120 * scaleY)
    }

    private func formattedAmount(_ amount: Decimal) -> String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.numberStyle = .decimal
        formatter.usesGroupingSeparator = true
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.string(from: NSDecimalNumber(decimal: amount)) ?? "0.00"
    }

    private func formattedDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "zh_CN")
        formatter.timeZone = TimeZone.current
        formatter.dateFormat = "yyyy年M月d日"
        return formatter.string(from: date)
    }
}
