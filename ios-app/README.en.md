# OpenAiPay iOS

## Directory Overview

- `project.yml`: XcodeGen project definition
- `OpenAiPay/`: SwiftUI source code
- `OpenAiPay.xcodeproj`: generated Xcode project that can be opened directly
- `OpenAiPay/Config/AppEndpoints.xcconfig`: default endpoint configuration
- `OpenAiPay/Config/AppEndpoints.local.xcconfig.example`: local override example for real-device debugging

The app display name is `爱付`.

## Local Run

1. Start backend on port `8080`
2. Start app-bff on port `3000`
3. Open the Xcode project:

```bash
open ios-app/OpenAiPay.xcodeproj
```

4. Choose an iOS Simulator such as `iPhone 17 Pro` and run the app directly

## Demo Login Account

- Account: `<demo account configured in admin backend>`
- Password: `888888`

## Endpoint Configuration

iOS default endpoints are defined in `OpenAiPay/Config/AppEndpoints.xcconfig`:

- `OPENAIPAY_BFF_SCHEME = http`
- `OPENAIPAY_BFF_HOST_PORT = 127.0.0.1:3000`
- `OPENAIPAY_BACKEND_SCHEME = http`
- `OPENAIPAY_BACKEND_HOST_PORT = 127.0.0.1:8080`

By default:

- the simulator talks to `127.0.0.1:3000` and `127.0.0.1:8080`
- you do not need to modify `APIClient.swift`

## Real Device Debugging

If a USB-connected device or LAN-connected real device needs to access services running on your development machine, do not change source code directly. Use a local override file instead.

1. Copy the local override template:

```bash
cp ios-app/OpenAiPay/Config/AppEndpoints.local.xcconfig.example \
   ios-app/OpenAiPay/Config/AppEndpoints.local.xcconfig
```

2. Fill in the actual reachable host and port in `AppEndpoints.local.xcconfig`, for example:

```xcconfig
OPENAIPAY_BFF_SCHEME = http
OPENAIPAY_BFF_HOST_PORT = 192.168.1.10:3000
OPENAIPAY_BACKEND_SCHEME = http
OPENAIPAY_BACKEND_HOST_PORT = 192.168.1.10:8080
```

3. Rebuild and install the app again

Notes:

- `OPENAIPAY_BFF_HOST_PORT` is used for the BFF endpoint
- `OPENAIPAY_BACKEND_HOST_PORT` is used for the backend endpoint
- these values are local debugging settings and should not be expanded in the root README
- `AppEndpoints.local.xcconfig` is machine-local and should not be committed
