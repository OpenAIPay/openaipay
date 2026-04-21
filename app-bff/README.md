# app-bff

`app-bff` 是 OpenAiPay iOS 客户端的 BFF 层，职责不是简单反向代理，而是：

- 提供面向页面的聚合接口与稳定契约
- 做必要的字段裁剪、格式归一和错误映射
- 透传 `x-request-id` 等链路上下文，便于排障
- 默认关闭 `/api/*` 代理，仅允许通过环境变量显式开启白名单

## 当前接口

- `POST /bff/auth/mobile-verify-login`
  - 登录聚合接口
  - 成功后会补用户资料；资料查询失败时会保留登录主流程并标记回退状态
- `GET /bff/page-init?page=<page>&uid=<userId>`
  - 页面初始化接口
  - 当前从 `/api/users/{userId}/init` 收敛为一次上游调用，再返回页面需要的裁剪字段
- `GET /bff/assets/:userId/overview`
  - 资产总览聚合接口
- `GET /bff/assets/:userId/changes?limit=3`
  - 近期资金变化聚合接口
  - 会并发获取资产总览和流水，并批量补齐交易对手资料
- `GET /health`
  - 健康检查

## 代理策略

`/api/*` 不是全量透传，而是受控白名单代理。

默认仅开放 `/api/short-video/**` 代理路径，用于短视频信息流联调与上线。

如需额外开启，可通过 `API_PROXY_ALLOWED_PREFIXES` 显式追加前缀，例如 `/api/trade,/api/media`。

无论是否配置 `API_PROXY_ALLOWED_PREFIXES`，`/api/short-video/**` 都会保持放行。

移动端应统一走显式的 `/bff/auth/*`、`/bff/apps/*`、`/bff/assets/*`、`/bff/cashier/*`、`/bff/users/*`、`/bff/contacts/*`、`/bff/messages/*` 接口。

默认禁止：

- `/api/admin/**`
- `/api/mock/**`

如果请求命中未放行路径，BFF 会返回 `API_PROXY_ROUTE_NOT_ALLOWED`，提示调用方改走 `/bff/*` 或直连后端。

## 环境变量

- `PORT`
  - BFF 监听端口，默认 `3000`
- `BACKEND_BASE_URL`
  - 后端地址，默认 `http://127.0.0.1:8080`
- `BACKEND_HTTP_TIMEOUT_MS`
  - BFF 聚合接口访问后端的默认超时，默认 `6000`
- `API_PROXY_TIMEOUT_MS`
  - `/api/*` 代理超时，默认 `60000`
- `API_PROXY_ALLOWED_PREFIXES`
  - 逗号分隔的代理白名单前缀；用于追加放行路径，`/api/short-video` 会始终保留
- `API_PROXY_BLOCKED_PREFIXES`
  - 逗号分隔的代理黑名单前缀，可覆盖默认值

## 设计说明

### 链路透传

每个进入 BFF 的请求都会生成或接收 `x-request-id`，并继续透传给上游后端服务。这样在 BFF 和 backend 间排查问题时，可以直接用同一个 requestId 串联日志。

### 资产聚合优化

- 资产总览与近期流水改为并发获取
- 交易对手资料改为一次批量查询，避免 BFF 层 N+1 上游调用
- 默认银行卡只在充值/提现场景才查询
- 交易对手资料做了短 TTL 内存缓存，减少重复页面刷新时的重复补数

## 本地开发

```bash
cd app-bff
npm install
PORT=3000 BACKEND_BASE_URL=http://127.0.0.1:8080 npm run start:dev
```

## 测试

```bash
cd app-bff
npm test
npm run test:e2e
```
