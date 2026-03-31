import SwiftUI

struct ProfileBillRowIconView: View {
    let size: CGFloat

    private let outlineBlue = Color(red: 55.0 / 255.0, green: 132.0 / 255.0, blue: 1.0)
    private let secondaryBlue = Color(red: 124.0 / 255.0, green: 182.0 / 255.0, blue: 1.0)
    private let accentYellow = Color(red: 250.0 / 255.0, green: 197.0 / 255.0, blue: 57.0 / 255.0)

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: size * 0.18, style: .continuous)
                .stroke(secondaryBlue.opacity(0.7), lineWidth: max(CGFloat(1), size * 0.07))
                .frame(width: size * 0.54, height: size * 0.70)
                .offset(x: -size * 0.13, y: size * 0.06)

            RoundedRectangle(cornerRadius: size * 0.18, style: .continuous)
                .fill(Color.white)
                .frame(width: size * 0.54, height: size * 0.70)
                .overlay(
                    RoundedRectangle(cornerRadius: size * 0.18, style: .continuous)
                        .stroke(outlineBlue, lineWidth: max(CGFloat(1.2), size * 0.08))
                )
                .offset(x: size * 0.06, y: -size * 0.03)

            VStack(spacing: size * 0.09) {
                RoundedRectangle(cornerRadius: size * 0.03, style: .continuous)
                    .fill(outlineBlue.opacity(0.26))
                    .frame(width: size * 0.18, height: max(CGFloat(1.4), size * 0.05))

                RoundedRectangle(cornerRadius: size * 0.03, style: .continuous)
                    .fill(outlineBlue.opacity(0.18))
                    .frame(width: size * 0.24, height: max(CGFloat(1.2), size * 0.045))
            }
            .offset(x: size * 0.07, y: -size * 0.08)

            Circle()
                .fill(accentYellow)
                .frame(width: size * 0.34, height: size * 0.34)
                .overlay(
                    Text("¥")
                        .font(.system(size: size * 0.20, weight: .black, design: .rounded))
                        .foregroundStyle(Color.white)
                )
                .offset(x: size * 0.10, y: size * 0.23)
        }
        .frame(width: size, height: size)
    }
}
