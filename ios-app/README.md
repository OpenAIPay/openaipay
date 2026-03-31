# OpenAiPay iOS

## 目录说明

- `project.yml`: XcodeGen 项目定义
- `OpenAiPay/`: SwiftUI 源码（目录名保留）
- `OpenAiPay.xcodeproj`: 已生成工程，可直接打开
- `OpenAiPay/Config/AppEndpoints.xcconfig`: 默认接口地址配置
- `OpenAiPay/Config/AppEndpoints.local.xcconfig.example`: 真机联调本地覆盖示例

应用展示名为 `爱付`。

## 本地运行

1. 启动 backend（8080）
2. 启动 app-bff（3000）
3. 打开工程：

```bash
open ios-app/OpenAiPay.xcodeproj
```

4. 选择 iOS Simulator（如 `iPhone 17 Pro`）后直接运行

## 登录测试账号

- 账号：`<后台配置的演示账号>`
- 密码：`888888`

## 接口地址

iOS 默认请求地址定义在 `OpenAiPay/Config/AppEndpoints.xcconfig`：

- `OPENAIPAY_BFF_SCHEME = http`
- `OPENAIPAY_BFF_HOST_PORT = 127.0.0.1:3000`
- `OPENAIPAY_BACKEND_SCHEME = http`
- `OPENAIPAY_BACKEND_HOST_PORT = 127.0.0.1:8080`

默认情况下：

- 模拟器访问 `127.0.0.1:3000` 和 `127.0.0.1:8080`
- 不需要修改 `APIClient.swift`

## 真机联调

如果需要让 USB 真机或局域网真机访问你的开发机服务，不要直接改源码，按下面方式处理：

1. 复制一份本地覆盖文件：

```bash
cp ios-app/OpenAiPay/Config/AppEndpoints.local.xcconfig.example \
   ios-app/OpenAiPay/Config/AppEndpoints.local.xcconfig
```

2. 在 `AppEndpoints.local.xcconfig` 中填入你实际可访问的地址，例如：

```xcconfig
OPENAIPAY_BFF_SCHEME = http
OPENAIPAY_BFF_HOST_PORT = 192.168.1.10:3000
OPENAIPAY_BACKEND_SCHEME = http
OPENAIPAY_BACKEND_HOST_PORT = 192.168.1.10:8080
```

3. 重新编译并安装 App

说明：

- `OPENAIPAY_BFF_HOST_PORT` 用于 BFF 地址
- `OPENAIPAY_BACKEND_HOST_PORT` 用于后端地址
- 这类配置属于本地调试配置，不建议写进根 README
- `AppEndpoints.local.xcconfig` 只用于本机，不需要提交到 Git
