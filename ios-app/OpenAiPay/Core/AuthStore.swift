import Foundation

final class AuthStore {
    private let sessionKey = "openaipay.session"
    private let sessionBffBaseURLKey = "openaipay.session.bff-base-url"
    private let defaults = UserDefaults.standard

    func loadSession(expectedBffBaseURL: String? = nil) -> LoginResponseData? {
        if let expectedBffBaseURL {
            let normalizedExpected = expectedBffBaseURL.trimmingCharacters(in: .whitespacesAndNewlines)
            let normalizedStored = (defaults.string(forKey: sessionBffBaseURLKey) ?? "")
                .trimmingCharacters(in: .whitespacesAndNewlines)
            if normalizedExpected.isEmpty || normalizedStored != normalizedExpected {
                clearSession()
                return nil
            }
        }
        let raw = defaults.data(forKey: sessionKey)
        guard let raw else {
            return nil
        }
        do {
            return try JSONDecoder().decode(LoginResponseData.self, from: raw)
        } catch {
            clearSession()
            return nil
        }
    }

    func saveSession(_ session: LoginResponseData, bffBaseURL: String? = nil) {
        let data = try? JSONEncoder().encode(session)
        defaults.set(data, forKey: sessionKey)
        let normalizedBffBaseURL = (bffBaseURL ?? "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
        if normalizedBffBaseURL.isEmpty {
            defaults.removeObject(forKey: sessionBffBaseURLKey)
        } else {
            defaults.set(normalizedBffBaseURL, forKey: sessionBffBaseURLKey)
        }
    }

    func clearSession() {
        defaults.removeObject(forKey: sessionKey)
        defaults.removeObject(forKey: sessionBffBaseURLKey)
    }
}
