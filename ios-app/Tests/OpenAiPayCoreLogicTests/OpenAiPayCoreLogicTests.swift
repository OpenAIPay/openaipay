import XCTest
@testable import OpenAiPayCoreLogic

final class OpenAiPayCoreLogicTests: XCTestCase {

    private let sessionKey = "openaipay.session"

    override func setUp() {
        super.setUp()
        clearSessions()
    }

    override func tearDown() {
        clearSessions()
        super.tearDown()
    }

    func testUserProfileDecodesStringUserIdAndNormalizesRelativeAvatarURL() throws {
        let data = """
        {
          "userId": "880100068483692100",
          "aipayUid": "",
          "loginId": "   ",
          "nickname": " ",
          "avatarUrl": "/api/media/gujun.png"
        }
        """.data(using: .utf8)!

        let profile = try JSONDecoder().decode(UserProfile.self, from: data)

        XCTAssertEqual(profile.userId, 880100068483692100)
        XCTAssertEqual(profile.aipayUid, "880100068483692100")
        XCTAssertEqual(profile.loginId, "880100068483692100")
        XCTAssertEqual(profile.nickname, "用户880100068483692100")
        XCTAssertEqual(profile.avatarUrl, "http://127.0.0.1:8080/api/media/gujun.png")
    }

    func testUserProfileInitializerConvertsDicebearSvgAvatarToPng() {
        let profile = UserProfile(
            userId: 880100068483692100,
            aipayUid: "2088001",
            loginId: "13920000001",
            accountStatus: "ACTIVE",
            kycLevel: "L2",
            nickname: "顾郡",
            avatarUrl: "https://api.dicebear.com/9.x/adventurer-neutral/svg?seed=880100068483692100",
            countryCode: "86",
            mobile: "13920000001",
            maskedRealName: "顾*",
            gender: "FEMALE",
            region: "广东佛山",
            birthday: "1996-01-19"
        )

        XCTAssertEqual(
            profile.avatarUrl,
            "https://api.dicebear.com/9.x/adventurer-neutral/png?seed=880100068483692100"
        )
    }

    func testLoginResponseDataDecodesFallbackExpiresInAndDefaultTokenValues() throws {
        let data = """
        {
          "expiresIn": "3600",
          "user": {
            "userId": 880100068483692100,
            "nickname": "顾郡"
          }
        }
        """.data(using: .utf8)!

        let response = try JSONDecoder().decode(LoginResponseData.self, from: data)

        XCTAssertEqual(response.accessToken, "local-restored-token")
        XCTAssertEqual(response.tokenType, "Bearer")
        XCTAssertEqual(response.expiresInSeconds, 3600)
        XCTAssertEqual(response.user.nickname, "顾郡")
    }

    func testAuthStoreLoadsSessionPayload() {
        let session = makeSession(userId: 880100068483692100, nickname: "顾郡")
        let encoded = try! JSONEncoder().encode(session)
        UserDefaults.standard.set(encoded, forKey: sessionKey)
        let store = AuthStore()

        let loaded = store.loadSession()

        XCTAssertEqual(loaded?.accessToken, session.accessToken)
        XCTAssertEqual(loaded?.user.userId, session.user.userId)
    }

    func testAuthStoreClearsCorruptedSessionPayload() {
        UserDefaults.standard.set(Data("not-json".utf8), forKey: sessionKey)
        let store = AuthStore()

        let loaded = store.loadSession()

        XCTAssertNil(loaded)
        XCTAssertNil(UserDefaults.standard.data(forKey: sessionKey))
    }

    private func makeSession(userId: Int64, nickname: String) -> LoginResponseData {
        LoginResponseData(
            accessToken: "token-\(userId)",
            tokenType: "Bearer",
            expiresInSeconds: 7200,
            user: UserProfile(
                userId: userId,
                aipayUid: "2088\(userId)",
                loginId: "13920000001",
                accountStatus: "ACTIVE",
                kycLevel: "L2",
                nickname: nickname,
                avatarUrl: "/api/media/avatar.png",
                countryCode: "86",
                mobile: "13920000001",
                maskedRealName: "顾*",
                gender: "FEMALE",
                region: "广东佛山",
                birthday: "1996-01-19"
            )
        )
    }

    private func clearSessions() {
        UserDefaults.standard.removeObject(forKey: sessionKey)
    }
}
