#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

printf '\n==> backend unit tests\n'
(cd "$ROOT_DIR" && mvn -pl backend/adapter-web -am test)

printf '\n==> app-bff unit tests\n'
(cd "$ROOT_DIR/app-bff" && npm test -- --runInBand)

printf '\n==> app-bff e2e tests\n'
(cd "$ROOT_DIR/app-bff" && npm run test:e2e)

printf '\n==> ios core logic tests\n'
(cd "$ROOT_DIR" && swift test --package-path ios-app)
