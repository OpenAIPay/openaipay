# app-bff

`app-bff` is the BFF layer for the OpenAiPay iOS client. It is not a simple reverse proxy. Its responsibilities are:

- providing page-oriented aggregation APIs and stable client contracts
- trimming fields, normalizing formats, and mapping upstream errors when needed
- forwarding tracing context such as `x-request-id` for easier troubleshooting
- keeping `/api/*` proxying disabled by default and only allowing explicit whitelist-based enabling through environment variables

## Current Endpoints

- `POST /bff/auth/mobile-verify-login`
  - login aggregation endpoint
  - enriches user profile after login; if profile lookup fails, the main login flow still succeeds and returns with fallback markers
- `GET /bff/page-init?page=<page>&uid=<userId>`
  - page initialization endpoint
  - currently converges `/api/users/{userId}/init` into one upstream call and returns only the fields needed by the page
- `GET /bff/assets/:userId/overview`
  - asset overview aggregation endpoint
- `GET /bff/assets/:userId/changes?limit=3`
  - recent asset change aggregation endpoint
  - fetches overview and bill changes concurrently, then batch-fills counterparty profile data
- `GET /health`
  - health check endpoint

## Proxy Policy

`/api/*` is a controlled whitelist proxy, not a full passthrough.

By default, no `/api/*` routes are exposed.

If you need to enable a route temporarily, declare it explicitly through `API_PROXY_ALLOWED_PREFIXES`, for example `/api/trade,/api/media`.

Mobile clients should use explicit endpoints such as `/bff/auth/*`, `/bff/apps/*`, `/bff/assets/*`, `/bff/cashier/*`, `/bff/users/*`, `/bff/contacts/*`, and `/bff/messages/*`.

Blocked by default:

- `/api/admin/**`
- `/api/mock/**`

If a request hits a non-allowed path, BFF returns `API_PROXY_ROUTE_NOT_ALLOWED` and instructs the caller to use `/bff/*` or call backend directly.

## Environment Variables

- `PORT`
  - BFF listening port, default `3000`
- `BACKEND_BASE_URL`
  - backend base URL, default `http://127.0.0.1:8080`
- `BACKEND_HTTP_TIMEOUT_MS`
  - default upstream timeout for BFF aggregation APIs, default `6000`
- `API_PROXY_TIMEOUT_MS`
  - timeout for `/api/*` proxy requests, default `60000`
- `API_PROXY_ALLOWED_PREFIXES`
  - comma-separated allowlist prefixes for proxying; by default no `/api/*` route is enabled
- `API_PROXY_BLOCKED_PREFIXES`
  - comma-separated blocklist prefixes that can override defaults

## Design Notes

### Trace Propagation

Every request entering BFF generates or accepts an `x-request-id`, then forwards it to upstream backend services. This makes it possible to correlate logs between BFF and backend with the same request ID.

### Asset Aggregation Optimizations

- asset overview and recent changes are fetched concurrently
- counterparty profile lookup is batched to avoid N+1 upstream calls in the BFF layer
- default bank card lookup runs only for top-up and withdrawal scenarios
- counterparty profile data uses a short in-memory TTL cache to reduce repeated backfills during frequent refreshes

## Local Development

```bash
cd app-bff
npm install
PORT=3000 BACKEND_BASE_URL=http://127.0.0.1:8080 npm run start:dev
```

## Tests

```bash
cd app-bff
npm test
npm run test:e2e
```
