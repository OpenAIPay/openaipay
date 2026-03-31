import SwiftUI

struct AipayLogoView: View {
    let scaleX: CGFloat
    let scaleY: CGFloat

    var body: some View {
        Image("登录字样")
            .resizable()
            .interpolation(.high)
            .scaledToFit()
            .frame(width: 176 * scaleX)
        .shadow(
            color: Color.black.opacity(0.06),
            radius: 3.2 * scaleX,
            x: 0,
            y: 2.0 * scaleY
        )
    }
}

#Preview {
    ZStack {
        Color.white
        AipayLogoView(scaleX: 1.0, scaleY: 1.0)
    }
}
