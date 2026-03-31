import SwiftUI
import UIKit
import CoreImage

/// Top status-bar mask used by screenshot-driven pages.
/// It samples a narrow horizontal strip right below the mask boundary
/// from the reference asset, then reuses those colors to render the mask.
struct ScreenshotStatusMaskView: View {
    let referenceAssetName: String
    let maskHeight: CGFloat

    init(referenceAssetName: String, maskHeight: CGFloat = 48) {
        self.referenceAssetName = referenceAssetName
        self.maskHeight = maskHeight
    }

    var body: some View {
        GeometryReader { _ in
            let palette = ScreenshotMaskPaletteCache.edgeStripPalette(for: referenceAssetName)
            let gradientStops = palette.colors.enumerated().map { index, color in
                let location = Double(index) / Double(max(palette.colors.count - 1, 1))
                return Gradient.Stop(color: Color(uiColor: color), location: location)
            }
            LinearGradient(
                gradient: Gradient(stops: gradientStops),
                startPoint: .leading,
                endPoint: .trailing
            )
            .frame(height: maskHeight)
            .frame(maxWidth: .infinity, alignment: .top)
            .allowsHitTesting(false)
        }
        .ignoresSafeArea(edges: .top)
    }
}

private struct ScreenshotMaskEdgeStripPalette {
    let colors: [UIColor]
}

private enum ScreenshotMaskPaletteCache {
    private static let lock = NSLock()
    private static var paletteCache: [String: ScreenshotMaskEdgeStripPalette] = [:]
    private static let ciContext = CIContext()

    static func edgeStripPalette(for assetName: String) -> ScreenshotMaskEdgeStripPalette {
        lock.lock()
        if let cached = paletteCache[assetName] {
            lock.unlock()
            return cached
        }
        lock.unlock()

        let resolved = sampledEdgeStripPalette(for: assetName) ?? fallbackEdgeStripPalette(for: assetName)

        lock.lock()
        paletteCache[assetName] = resolved
        lock.unlock()
        return resolved
    }

    private static func sampledEdgeStripPalette(for assetName: String) -> ScreenshotMaskEdgeStripPalette? {
        guard let image = UIImage(named: assetName),
              let cgImage = image.cgImage else {
            return nil
        }

        let ciImage = CIImage(cgImage: cgImage)
        let extent = ciImage.extent
        guard extent.width > 1, extent.height > 1 else {
            return nil
        }

        // Sample right below the overlay boundary to match the seam color.
        let segmentCount = 36
        let horizontalMargin: CGFloat = 0.02
        let stripStart: CGFloat = 0.052
        let stripEnd: CGFloat = 0.056
        let availableWidth = extent.width * (1 - horizontalMargin * 2)
        let segmentWidth = max(availableWidth / CGFloat(segmentCount), 1)
        var sampledColors: [UIColor] = []

        for index in 0..<segmentCount {
            let x = extent.minX + extent.width * horizontalMargin + CGFloat(index) * segmentWidth
            let rect = sampleRect(
                extent: extent,
                x: x,
                width: segmentWidth,
                fromTopStart: stripStart,
                fromTopEnd: stripEnd
            )
            if let color = averageColor(in: ciImage, rect: rect) {
                sampledColors.append(color)
            }
        }

        guard sampledColors.count >= 2 else {
            return nil
        }

        return ScreenshotMaskEdgeStripPalette(colors: sampledColors)
    }

    private static func sampleRect(
        extent: CGRect,
        x: CGFloat,
        width: CGFloat,
        fromTopStart: CGFloat,
        fromTopEnd: CGFloat
    ) -> CGRect {
        let start = min(max(fromTopStart, 0), 1)
        let end = min(max(fromTopEnd, start), 1)

        let yBottom = extent.minY + extent.height * (1 - end)
        let yTop = extent.minY + extent.height * (1 - start)
        return CGRect(
            x: x,
            y: yBottom,
            width: max(width, 1),
            height: max(yTop - yBottom, 1)
        )
    }

    private static func averageColor(in image: CIImage, rect: CGRect) -> UIColor? {
        guard let filter = CIFilter(name: "CIAreaAverage") else {
            return nil
        }
        filter.setValue(image, forKey: kCIInputImageKey)
        filter.setValue(CIVector(cgRect: rect), forKey: kCIInputExtentKey)
        guard let output = filter.outputImage else {
            return nil
        }

        var bitmap = [UInt8](repeating: 0, count: 4)
        ciContext.render(
            output,
            toBitmap: &bitmap,
            rowBytes: 4,
            bounds: CGRect(x: 0, y: 0, width: 1, height: 1),
            format: .RGBA8,
            colorSpace: CGColorSpaceCreateDeviceRGB()
        )

        return UIColor(
            red: CGFloat(bitmap[0]) / 255,
            green: CGFloat(bitmap[1]) / 255,
            blue: CGFloat(bitmap[2]) / 255,
            alpha: 1
        )
    }

    private static func fallbackEdgeStripPalette(for assetName: String) -> ScreenshotMaskEdgeStripPalette {
        switch assetName {
        case "LoginMaskFallback":
            return ScreenshotMaskEdgeStripPalette(colors: [
                UIColor(red: 0.78, green: 0.82, blue: 0.86, alpha: 1),
                UIColor(red: 0.77, green: 0.81, blue: 0.85, alpha: 1)
            ])
        case "HomeReference", "BalanceReference", "TransferSuccessReference":
            return ScreenshotMaskEdgeStripPalette(colors: [
                AppTheme.palette.brandGradientStartUIColor,
                AppTheme.palette.brandGradientEndUIColor
            ])
        case "MessageReference", "ProfileReference", "TransferListReference":
            return ScreenshotMaskEdgeStripPalette(colors: [
                UIColor(red: 0.83, green: 0.89, blue: 0.95, alpha: 1),
                UIColor(red: 0.80, green: 0.87, blue: 0.95, alpha: 1)
            ])
        default:
            return ScreenshotMaskEdgeStripPalette(colors: [
                UIColor(red: 0.93, green: 0.93, blue: 0.93, alpha: 1),
                UIColor(red: 0.93, green: 0.93, blue: 0.93, alpha: 1)
            ])
        }
    }
}
