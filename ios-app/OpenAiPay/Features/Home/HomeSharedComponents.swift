import SwiftUI
import UIKit
import CoreLocation

extension UIApplication {

    var activeKeyWindow: UIWindow? {
        connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first(where: \.isKeyWindow)
    }
    }

struct VerticallyCenteredTextField: UIViewRepresentable {
    @Binding var text: String
    @Binding var isFocused: Bool
    let font: UIFont
    let textColor: UIColor
    var placeholder: String? = nil
    var tintColor: UIColor = UIColor(red: 0.34, green: 0.35, blue: 0.37, alpha: 1)
    var keyboardType: UIKeyboardType = .default
    var returnKeyType: UIReturnKeyType = .search
    var autocapitalizationType: UITextAutocapitalizationType = .none
    var autocorrectionType: UITextAutocorrectionType = .no
    var spellCheckingType: UITextSpellCheckingType = .no
    var textAlignment: NSTextAlignment = .left
    var textInsets: UIEdgeInsets = .zero
    var showsAccessoryToolbar = true
    var shouldResignOnSubmit = true
    var onSubmit: (() -> Void)? = nil

    final class InsetTextField: UITextField {
        var textInsets: UIEdgeInsets = .zero

        override func textRect(forBounds bounds: CGRect) -> CGRect {
            adjustedRect(for: bounds)
        }

        override func editingRect(forBounds bounds: CGRect) -> CGRect {
            adjustedRect(for: bounds)
        }

        override func placeholderRect(forBounds bounds: CGRect) -> CGRect {
            adjustedRect(for: bounds)
        }

        private func adjustedRect(for bounds: CGRect) -> CGRect {
            CGRect(
                x: bounds.origin.x + textInsets.left,
                y: bounds.origin.y + textInsets.top,
                width: bounds.size.width - textInsets.left - textInsets.right,
                height: bounds.size.height - textInsets.top - textInsets.bottom
            )
        }
    }

    final class Coordinator: NSObject, UITextFieldDelegate {
        var parent: VerticallyCenteredTextField
        weak var textField: UITextField?

        init(parent: VerticallyCenteredTextField) {
            self.parent = parent
        }

        @objc
        func textDidChange(_ sender: UITextField) {
            parent.text = sender.text ?? ""
        }

        @objc
        func doneButtonTapped() {
            parent.isFocused = false
            textField?.resignFirstResponder()
        }

        func textFieldDidBeginEditing(_ textField: UITextField) {
            parent.isFocused = true
        }

        func textFieldDidEndEditing(_ textField: UITextField) {
            parent.isFocused = false
        }

        func textFieldShouldReturn(_ textField: UITextField) -> Bool {
            parent.onSubmit?()
            if parent.shouldResignOnSubmit {
                parent.isFocused = false
                textField.resignFirstResponder()
            } else {
                parent.isFocused = true
            }
            return false
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(parent: self)
    }

    func makeUIView(context: Context) -> UITextField {
        let textField = InsetTextField(frame: .zero)
        textField.borderStyle = .none
        textField.backgroundColor = .clear
        textField.contentVerticalAlignment = .center
        textField.textAlignment = .left
        textField.adjustsFontSizeToFitWidth = false
        textField.minimumFontSize = 1
        textField.clearButtonMode = .never
        textField.delegate = context.coordinator
        textField.addTarget(context.coordinator, action: #selector(Coordinator.textDidChange(_:)), for: .editingChanged)
        context.coordinator.textField = textField

        if showsAccessoryToolbar {
            let toolbar = UIToolbar()
            toolbar.sizeToFit()
            toolbar.items = [
                UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: nil, action: nil),
                UIBarButtonItem(title: "确认", style: .done, target: context.coordinator, action: #selector(Coordinator.doneButtonTapped))
            ]
            textField.inputAccessoryView = toolbar
        } else {
            textField.inputAccessoryView = nil
        }
        return textField
    }

    func updateUIView(_ uiView: UITextField, context: Context) {
        context.coordinator.parent = self
        context.coordinator.textField = uiView
        if uiView.text != text {
            uiView.text = text
        }
        uiView.font = font
        uiView.textColor = textColor
        uiView.placeholder = placeholder
        uiView.tintColor = tintColor
        uiView.keyboardType = keyboardType
        uiView.returnKeyType = returnKeyType
        uiView.autocapitalizationType = autocapitalizationType
        uiView.autocorrectionType = autocorrectionType
        uiView.spellCheckingType = spellCheckingType
        uiView.textAlignment = textAlignment
        if let insetTextField = uiView as? InsetTextField {
            insetTextField.textInsets = textInsets
        }
        DispatchQueue.main.async {
            if isFocused {
                if !uiView.isFirstResponder {
                    uiView.becomeFirstResponder()
                }
            } else if uiView.isFirstResponder {
                uiView.resignFirstResponder()
            }
        }
    }
}

struct ClippedTextView: UIViewRepresentable {
    @Binding var text: String
    @Binding var isFocused: Bool
    let font: UIFont
    let textColor: UIColor
    var tintColor: UIColor = UIColor(red: 0.34, green: 0.35, blue: 0.37, alpha: 1)
    var textInsets: UIEdgeInsets = .zero

    final class InsetTextView: UITextView {
        var textInsets: UIEdgeInsets = .zero {
            didSet {
                textContainerInset = textInsets
            }
        }
    }

    final class ContainerView: UIView {
        let textView = InsetTextView(frame: .zero, textContainer: nil)

        override init(frame: CGRect) {
            super.init(frame: frame)
            backgroundColor = .clear
            clipsToBounds = true
            isAccessibilityElement = false

            textView.translatesAutoresizingMaskIntoConstraints = false
            textView.backgroundColor = .clear
            textView.textAlignment = .left
            textView.isScrollEnabled = true
            textView.textContainer.lineFragmentPadding = 0

            addSubview(textView)
            NSLayoutConstraint.activate([
                textView.leadingAnchor.constraint(equalTo: leadingAnchor),
                textView.trailingAnchor.constraint(equalTo: trailingAnchor),
                textView.topAnchor.constraint(equalTo: topAnchor),
                textView.bottomAnchor.constraint(equalTo: bottomAnchor)
            ])
        }

        override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
            textView.frame.insetBy(dx: -4, dy: -4).contains(point)
        }

        override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
            guard self.point(inside: point, with: event) else {
                return nil
            }
            let convertedPoint = convert(point, to: textView)
            return textView.hitTest(convertedPoint, with: event) ?? textView
        }

        @available(*, unavailable)
        required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
    }

    final class Coordinator: NSObject, UITextViewDelegate {
        var parent: ClippedTextView

        init(parent: ClippedTextView) {
            self.parent = parent
        }

        func textViewDidBeginEditing(_ textView: UITextView) {
            parent.isFocused = true
        }

        func textViewDidEndEditing(_ textView: UITextView) {
            parent.isFocused = false
        }

        func textViewDidChange(_ textView: UITextView) {
            parent.text = textView.text ?? ""
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(parent: self)
    }

    func makeUIView(context: Context) -> ContainerView {
        let container = ContainerView(frame: .zero)
        let textView = container.textView
        textView.delegate = context.coordinator
        return container
    }

    func updateUIView(_ uiView: ContainerView, context: Context) {
        context.coordinator.parent = self
        let textView = uiView.textView
        if textView.text != text {
            textView.text = text
        }
        textView.font = font
        textView.textColor = textColor
        textView.tintColor = tintColor
        textView.textInsets = textInsets
        if isFocused {
            if !textView.isFirstResponder {
                textView.becomeFirstResponder()
            }
        } else if textView.isFirstResponder {
            textView.resignFirstResponder()
        }
    }
}

struct MoneyBagTabIcon: View {
    let size: CGFloat
    let color: Color
    let isSelected: Bool

    var body: some View {
        ZStack {
            if isSelected {
                MoneyBagBodyShape()
                    .fill(color)

                MoneyBagKnotShape()
                    .fill(color)
            }

            MoneyBagKnotShape()
                .stroke(color, style: StrokeStyle(lineWidth: max(1.9, size * 0.095), lineCap: .round, lineJoin: .round))

            MoneyBagBodyShape()
                .stroke(color, style: StrokeStyle(lineWidth: max(1.9, size * 0.095), lineCap: .round, lineJoin: .round))

            let innerStrokeColor = isSelected ? Color.white : color

            RoundedRectangle(cornerRadius: size * 0.045, style: .continuous)
                .stroke(innerStrokeColor, lineWidth: max(1.5, size * 0.072))
                .frame(width: size * 0.42, height: size * 0.09)
                .offset(y: -size * 0.18)

            MoneyBagDollarShape()
                .stroke(innerStrokeColor, style: StrokeStyle(lineWidth: max(1.35, size * 0.064), lineCap: .round, lineJoin: .round))
                .frame(width: size * 0.28, height: size * 0.42)
                .offset(y: size * 0.11)
        }
        .frame(width: size, height: size)
    }
}

struct VideoPlayTabIcon: View {
    let size: CGFloat
    let color: Color
    let isSelected: Bool

    var body: some View {
        ZStack {
            if isSelected {
                RoundedRectangle(cornerRadius: size * 0.18, style: .continuous)
                    .fill(color)
                    .frame(width: size * 0.73, height: size * 0.68)
                    .offset(x: -size * 0.10, y: size * 0.01)

                VideoPlayLensShape()
                    .fill(color)
                    .frame(width: size * 0.34, height: size * 0.40)
                    .offset(x: size * 0.28, y: size * 0.02)
            }

            RoundedRectangle(cornerRadius: size * 0.18, style: .continuous)
                .stroke(
                    color,
                    style: StrokeStyle(
                        lineWidth: max(1.55, size * 0.082),
                        lineCap: .round,
                        lineJoin: .round
                    )
                )
                .frame(width: size * 0.73, height: size * 0.68)
                .offset(x: -size * 0.10, y: size * 0.01)

            VideoPlayLensShape()
                .stroke(
                    color,
                    style: StrokeStyle(
                        lineWidth: max(1.55, size * 0.082),
                        lineCap: .round,
                        lineJoin: .round
                    )
                )
                .frame(width: size * 0.34, height: size * 0.40)
                .offset(x: size * 0.28, y: size * 0.02)

            if isSelected {
                VideoPlayTriangleShape()
                    .fill(Color.white)
                    .frame(width: size * 0.25, height: size * 0.27)
                    .offset(x: -size * 0.10, y: size * 0.01)
            } else {
                VideoPlayTriangleShape()
                    .stroke(
                        color,
                        style: StrokeStyle(
                            lineWidth: max(1.45, size * 0.074),
                            lineCap: .round,
                            lineJoin: .round
                        )
                    )
                    .frame(width: size * 0.25, height: size * 0.27)
                    .offset(x: -size * 0.10, y: size * 0.01)
            }
        }
        .frame(width: size * 1.08, height: size)
    }
}

private struct VideoPlayLensShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.minX + rect.width * 0.08, y: rect.minY + rect.height * 0.24))
        path.addLine(to: CGPoint(x: rect.maxX - rect.width * 0.08, y: rect.minY + rect.height * 0.10))
        path.addLine(to: CGPoint(x: rect.maxX - rect.width * 0.08, y: rect.maxY - rect.height * 0.10))
        path.addLine(to: CGPoint(x: rect.minX + rect.width * 0.08, y: rect.maxY - rect.height * 0.24))
        path.closeSubpath()
        return path
    }
}

private struct VideoPlayTriangleShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.minX + rect.width * 0.16, y: rect.minY + rect.height * 0.14))
        path.addLine(to: CGPoint(x: rect.maxX - rect.width * 0.12, y: rect.midY))
        path.addLine(to: CGPoint(x: rect.minX + rect.width * 0.16, y: rect.maxY - rect.height * 0.14))
        path.closeSubpath()
        return path
    }
}

private struct MoneyBagBodyShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.minX + rect.width * 0.25, y: rect.minY + rect.height * 0.29))
        path.addCurve(
            to: CGPoint(x: rect.minX + rect.width * 0.12, y: rect.minY + rect.height * 0.82),
            control1: CGPoint(x: rect.minX + rect.width * 0.14, y: rect.minY + rect.height * 0.40),
            control2: CGPoint(x: rect.minX + rect.width * 0.06, y: rect.minY + rect.height * 0.58)
        )
        path.addCurve(
            to: CGPoint(x: rect.minX + rect.width * 0.88, y: rect.minY + rect.height * 0.82),
            control1: CGPoint(x: rect.minX + rect.width * 0.28, y: rect.minY + rect.height * 0.95),
            control2: CGPoint(x: rect.minX + rect.width * 0.72, y: rect.minY + rect.height * 0.95)
        )
        path.addCurve(
            to: CGPoint(x: rect.minX + rect.width * 0.75, y: rect.minY + rect.height * 0.29),
            control1: CGPoint(x: rect.minX + rect.width * 0.94, y: rect.minY + rect.height * 0.58),
            control2: CGPoint(x: rect.minX + rect.width * 0.86, y: rect.minY + rect.height * 0.40)
        )
        path.closeSubpath()
        return path
    }
}

private struct MoneyBagKnotShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.minX + rect.width * 0.28, y: rect.minY + rect.height * 0.24))
        path.addCurve(
            to: CGPoint(x: rect.minX + rect.width * 0.20, y: rect.minY + rect.height * 0.10),
            control1: CGPoint(x: rect.minX + rect.width * 0.22, y: rect.minY + rect.height * 0.20),
            control2: CGPoint(x: rect.minX + rect.width * 0.16, y: rect.minY + rect.height * 0.15)
        )
        path.addCurve(
            to: CGPoint(x: rect.minX + rect.width * 0.40, y: rect.minY + rect.height * 0.08),
            control1: CGPoint(x: rect.minX + rect.width * 0.24, y: rect.minY + rect.height * 0.04),
            control2: CGPoint(x: rect.minX + rect.width * 0.32, y: rect.minY + rect.height * 0.02)
        )
        path.addCurve(
            to: CGPoint(x: rect.minX + rect.width * 0.50, y: rect.minY + rect.height * 0.01),
            control1: CGPoint(x: rect.minX + rect.width * 0.43, y: rect.minY + rect.height * 0.01),
            control2: CGPoint(x: rect.minX + rect.width * 0.47, y: rect.minY + rect.height * 0.01)
        )
        path.addCurve(
            to: CGPoint(x: rect.minX + rect.width * 0.60, y: rect.minY + rect.height * 0.08),
            control1: CGPoint(x: rect.minX + rect.width * 0.53, y: rect.minY + rect.height * 0.01),
            control2: CGPoint(x: rect.minX + rect.width * 0.57, y: rect.minY + rect.height * 0.01)
        )
        path.addCurve(
            to: CGPoint(x: rect.minX + rect.width * 0.80, y: rect.minY + rect.height * 0.10),
            control1: CGPoint(x: rect.minX + rect.width * 0.68, y: rect.minY + rect.height * 0.02),
            control2: CGPoint(x: rect.minX + rect.width * 0.76, y: rect.minY + rect.height * 0.04)
        )
        path.addCurve(
            to: CGPoint(x: rect.minX + rect.width * 0.72, y: rect.minY + rect.height * 0.24),
            control1: CGPoint(x: rect.minX + rect.width * 0.84, y: rect.minY + rect.height * 0.15),
            control2: CGPoint(x: rect.minX + rect.width * 0.78, y: rect.minY + rect.height * 0.20)
        )
        path.closeSubpath()
        return path
    }
}

private struct MoneyBagDollarShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.midX + rect.width * 0.20, y: rect.minY + rect.height * 0.16))
        path.addCurve(
            to: CGPoint(x: rect.midX - rect.width * 0.05, y: rect.minY + rect.height * 0.42),
            control1: CGPoint(x: rect.midX + rect.width * 0.02, y: rect.minY + rect.height * 0.08),
            control2: CGPoint(x: rect.midX - rect.width * 0.12, y: rect.minY + rect.height * 0.22)
        )
        path.addCurve(
            to: CGPoint(x: rect.midX + rect.width * 0.06, y: rect.minY + rect.height * 0.56),
            control1: CGPoint(x: rect.midX - rect.width * 0.02, y: rect.minY + rect.height * 0.47),
            control2: CGPoint(x: rect.midX + rect.width * 0.08, y: rect.minY + rect.height * 0.49)
        )
        path.addCurve(
            to: CGPoint(x: rect.midX - rect.width * 0.22, y: rect.minY + rect.height * 0.84),
            control1: CGPoint(x: rect.midX + rect.width * 0.22, y: rect.minY + rect.height * 0.64),
            control2: CGPoint(x: rect.midX + rect.width * 0.03, y: rect.minY + rect.height * 0.90)
        )
        path.move(to: CGPoint(x: rect.midX, y: rect.minY + rect.height * 0.04))
        path.addLine(to: CGPoint(x: rect.midX, y: rect.minY + rect.height * 0.96))
        return path
    }
}

struct AILoadingIndicator: View {

    var text: String? = nil
    var accentColor: Color = AppTheme.palette.brandPrimary
    var secondaryAccentColor: Color = AppTheme.palette.brandPrimaryLight
    var textColor: Color = Color(red: 0.49, green: 0.53, blue: 0.60)
    var aiFontSize: CGFloat = 18
    var dotSize: CGFloat = 6
    var textFontSize: CGFloat = 14



    var body: some View {
        TimelineView(.periodic(from: .now, by: 0.17)) { context in
            let tick = Int(context.date.timeIntervalSinceReferenceDate / 0.17)
            let visibleDots = visibleDotCount(for: tick)
            VStack(spacing: text == nil ? 0 : 8) {
                let accentGradient = LinearGradient(
                    colors: [
                        accentColor.opacity(0.96),
                        secondaryAccentColor.opacity(0.96),
                        AppTheme.palette.brandGradientStart.opacity(0.96)
                    ],
                    startPoint: .leading,
                    endPoint: .trailing
                )

                HStack(spacing: 7) {
                    Text("AI")
                        .font(.system(size: aiFontSize, weight: .semibold, design: .monospaced))
                        .tracking(max(0.6, aiFontSize * 0.06))
                        .foregroundStyle(accentGradient)
                        .overlay {
                            Text("AI")
                                .font(.system(size: aiFontSize, weight: .semibold, design: .monospaced))
                                .tracking(max(0.6, aiFontSize * 0.06))
                                .foregroundStyle(Color.white.opacity(0.26))
                                .blur(radius: 0.6)
                        }

                    HStack(spacing: 4) {
                        ForEach(0..<6, id: \.self) { index in
                            Circle()
                                .fill(accentGradient)
                                .frame(width: dotSize, height: dotSize)
                                .opacity(index < visibleDots ? 0.96 : 0.20)
                                .scaleEffect(index < visibleDots ? 1.0 : 0.72)
                        }
                    }
                }

                if let text, !text.isEmpty {
                    Text(text)
                        .font(.system(size: textFontSize, weight: .regular))
                        .foregroundStyle(textColor)
                }
            }
        }
    }

    private func visibleDotCount(for tick: Int) -> Int {
        let phase = tick % 10
        if phase <= 5 {
            return phase + 1
        }
        return 11 - phase
    }
    }

enum HomeCitySelectionMode: String {
    case auto
    case manual
}

struct HomeCitySection: Identifiable {
    let id: String
    let title: String
    let cities: [String]
}

func normalizedHomeDisplayCityName(_ raw: String?) -> String {
    let trimmed = (raw ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
    guard !trimmed.isEmpty else {
        return ""
    }
    let suffixes = [
        "特别行政区",
        "自治州",
        "地区",
        "自治区",
        "省",
        "市",
        "盟"
    ]
    for suffix in suffixes where trimmed.hasSuffix(suffix) {
        return String(trimmed.dropLast(suffix.count))
    }
    return trimmed
}

final class HomeCityLocationService: NSObject, ObservableObject, CLLocationManagerDelegate {
    @Published private(set) var resolvedCityName: String = ""
    @Published private(set) var isLocating = false
    @Published private(set) var errorMessage: String?
    @Published private(set) var authorizationStatus: CLAuthorizationStatus

    private let locationManager = CLLocationManager()
    private let geocoder = CLGeocoder()
    private var hasRequestedCurrentLocation = false

    override init() {
        authorizationStatus = CLLocationManager.authorizationStatus()
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyKilometer
    }

    @MainActor
    func requestCurrentCity(force: Bool = false) {
        if isLocating && !force {
            return
        }
        if hasRequestedCurrentLocation && !force {
            return
        }
        hasRequestedCurrentLocation = true
        errorMessage = nil

        let status = locationManager.authorizationStatus
        authorizationStatus = status
        switch status {
        case .notDetermined:
            isLocating = true
            locationManager.requestWhenInUseAuthorization()
        case .authorizedWhenInUse, .authorizedAlways:
            isLocating = true
            locationManager.requestLocation()
        case .restricted, .denied:
            isLocating = false
            errorMessage = "定位权限未开启，可手动选择城市"
        @unknown default:
            isLocating = false
            errorMessage = "暂时无法获取当前位置"
        }
    }

    @MainActor
    @discardableResult
    func refreshAuthorizationStatus() -> CLAuthorizationStatus {
        let status = locationManager.authorizationStatus
        authorizationStatus = status
        return status
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let status = manager.authorizationStatus
        DispatchQueue.main.async {
            self.authorizationStatus = status
            switch status {
            case .authorizedWhenInUse, .authorizedAlways:
                self.isLocating = true
                self.errorMessage = nil
                manager.requestLocation()
            case .restricted, .denied:
                self.isLocating = false
                self.errorMessage = "定位权限未开启，可手动选择城市"
            case .notDetermined:
                break
            @unknown default:
                self.isLocating = false
                self.errorMessage = "暂时无法获取当前位置"
            }
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        DispatchQueue.main.async {
            self.isLocating = false
            let nsError = error as NSError
            if nsError.domain == kCLErrorDomain, nsError.code == CLError.denied.rawValue {
                self.errorMessage = "定位权限未开启，可手动选择城市"
            } else {
                self.errorMessage = "定位失败，可手动选择城市"
            }
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else {
            DispatchQueue.main.async {
                self.isLocating = false
                self.errorMessage = "未获取到定位结果"
            }
            return
        }

        geocoder.cancelGeocode()
        geocoder.reverseGeocodeLocation(location, preferredLocale: Locale(identifier: "zh_Hans_CN")) { [weak self] placemarks, error in
            guard let self else {
                return
            }
            DispatchQueue.main.async {
                self.isLocating = false
                if error != nil {
                    self.errorMessage = "定位失败，可手动选择城市"
                    return
                }
                guard let cityName = self.resolveCityName(from: placemarks), !cityName.isEmpty else {
                    self.errorMessage = "未识别到所在城市"
                    return
                }
                self.resolvedCityName = cityName
                self.errorMessage = nil
            }
        }
    }

    private func resolveCityName(from placemarks: [CLPlacemark]?) -> String? {
        guard let placemark = placemarks?.first else {
            return nil
        }
        let candidates = [
            placemark.locality,
            placemark.administrativeArea,
            placemark.subAdministrativeArea,
            placemark.name
        ]
        for candidate in candidates {
            let normalized = normalizedHomeDisplayCityName(candidate)
            if !normalized.isEmpty {
                return normalized
            }
        }
        return nil
    }
}
