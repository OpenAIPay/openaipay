import SwiftUI
import UIKit

enum AppProductCode: String, CaseIterable {
    case aiPay
    case aiCash
    case aiCredit
    case aiLoan
}

enum AppBranding {
    static let aiPayZh = "爱付"
    static let aiCashZh = "爱存"
    static let aiCreditZh = "爱花"
    static let aiLoanZh = "爱借"

    static let aiPayEn = "AiPay"
    static let aiCashEn = "AiCash"
    static let aiCreditEn = "AiCredit"
    static let aiLoanEn = "AiLoan"

    static func displayName(for productCode: AppProductCode) -> String {
        switch productCode {
        case .aiPay:
            return aiPayZh
        case .aiCash:
            return aiCashZh
        case .aiCredit:
            return aiCreditZh
        case .aiLoan:
            return aiLoanZh
        }
    }

    static func codeName(for productCode: AppProductCode) -> String {
        switch productCode {
        case .aiPay:
            return aiPayEn
        case .aiCash:
            return aiCashEn
        case .aiCredit:
            return aiCreditEn
        case .aiLoan:
            return aiLoanEn
        }
    }

    static func displayText(_ raw: String) -> String {
        let replacements: [(String, String)] = [
            ("爱付", aiPayZh),
            ("AiPay", aiPayZh),
            ("aipay", aiPayZh),
            ("AiPay", aiPayZh),
            ("AIPAY", aiPayZh),
            ("爱存", aiCashZh),
            ("AiCash", aiCashZh),
            ("AiCash", aiCashZh),
            ("aicash", aiCashZh),
            ("AiCash", aiCashZh),
            ("AICASH", aiCashZh),
            ("aicash", aiCashZh),
            ("爱花", aiCreditZh),
            ("AiCredit", aiCreditZh),
            ("aicredit", aiCreditZh),
            ("AiCredit", aiCreditZh),
            ("AICREDIT", aiCreditZh),
            ("aicredit", aiCreditZh),
            ("爱借", aiLoanZh),
            ("AiLoan", aiLoanZh),
            ("ailoan", aiLoanZh),
            ("AiLoan", aiLoanZh),
            ("AILOAN", aiLoanZh),
            ("ailoan", aiLoanZh)
        ]
        return replacements.reduce(raw) { partial, item in
            partial.replacingOccurrences(of: item.0, with: item.1)
        }
    }
}

enum AppThemeStyle {
    case coolPurple
}

struct AppThemePalette {
    var brandPrimary: Color { Color(red: 101.0 / 255.0, green: 87.0 / 255.0, blue: 200.0 / 255.0) } // #6557C8
    var brandPrimaryPressed: Color { Color(red: 86.0 / 255.0, green: 72.0 / 255.0, blue: 175.0 / 255.0) } // #5648AF
    var brandPrimaryLight: Color { Color(red: 123.0 / 255.0, green: 106.0 / 255.0, blue: 227.0 / 255.0) } // #7B6AE3
    var brandLink: Color { Color(red: 95.0 / 255.0, green: 79.0 / 255.0, blue: 189.0 / 255.0) } // #5F4FBD
    var brandFocus: Color { Color(red: 106.0 / 255.0, green: 90.0 / 255.0, blue: 205.0 / 255.0) } // #6A5ACD
    var brandGradientStart: Color { Color(red: 123.0 / 255.0, green: 106.0 / 255.0, blue: 227.0 / 255.0) } // #7B6AE3
    var brandGradientEnd: Color { Color(red: 90.0 / 255.0, green: 73.0 / 255.0, blue: 175.0 / 255.0) } // #5A49AF
    var brandSubtleBackground: Color { Color(red: 245.0 / 255.0, green: 242.0 / 255.0, blue: 252.0 / 255.0) } // #F5F2FC
    var brandBorder: Color { Color(red: 222.0 / 255.0, green: 216.0 / 255.0, blue: 240.0 / 255.0) } // #DED8F0

    var brandPrimaryUIColor: UIColor { UIColor(red: 101.0 / 255.0, green: 87.0 / 255.0, blue: 200.0 / 255.0, alpha: 1) }
    var brandPrimaryPressedUIColor: UIColor { UIColor(red: 86.0 / 255.0, green: 72.0 / 255.0, blue: 175.0 / 255.0, alpha: 1) }
    var brandGradientStartUIColor: UIColor { UIColor(red: 123.0 / 255.0, green: 106.0 / 255.0, blue: 227.0 / 255.0, alpha: 1) }
    var brandGradientEndUIColor: UIColor { UIColor(red: 90.0 / 255.0, green: 73.0 / 255.0, blue: 175.0 / 255.0, alpha: 1) }
}

enum AppTheme {
    static let style: AppThemeStyle = .coolPurple
    static let palette = AppThemePalette()
}
