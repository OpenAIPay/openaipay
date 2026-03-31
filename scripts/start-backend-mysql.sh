#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

DB_HOST="${OPENAIPAY_DB_HOST:-127.0.0.1}"
DB_PORT="${OPENAIPAY_DB_PORT:-3306}"
DB_NAME="${OPENAIPAY_DB_NAME:-portal}"
DB_USERNAME="${OPENAIPAY_DB_USERNAME:-openaipay}"

if [[ -z "${OPENAIPAY_DB_PASSWORD:-}" && -z "${SPRING_DATASOURCE_PASSWORD:-}" ]]; then
  if [[ -t 0 ]]; then
    read -r -s -p "OpenAiPay local MySQL password: " OPENAIPAY_DB_PASSWORD
    echo
    export OPENAIPAY_DB_PASSWORD
  else
    echo "OPENAIPAY_DB_PASSWORD is required in non-interactive mode." >&2
    exit 1
  fi
fi

DB_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-${OPENAIPAY_DB_PASSWORD:-}}"

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"
export SERVER_PORT="${SERVER_PORT:-8080}"
export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false&connectTimeout=3000&socketTimeout=15000&tcpKeepAlive=true&cachePrepStmts=true&prepStmtCacheSize=256&prepStmtCacheSqlLimit=2048&useServerPrepStmts=true&useLocalSessionState=true&useLocalTransactionState=true&elideSetAutoCommits=true&maintainTimeStats=false&rewriteBatchedStatements=true}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-${DB_USERNAME}}"
export SPRING_DATASOURCE_PASSWORD="${DB_PASSWORD}"
export OPENAIPAY_FLYWAY_LOCATIONS="${OPENAIPAY_FLYWAY_LOCATIONS:-filesystem:${ROOT_DIR}/local/flyway-migration}"
export OPENAIPAY_TOKEN_SIGNING_SECRET="${OPENAIPAY_TOKEN_SIGNING_SECRET:-openaipay-local-signing-secret-please-change}"
export OPENAIPAY_ALLOW_INSECURE_DEFAULTS="${OPENAIPAY_ALLOW_INSECURE_DEFAULTS:-true}"

cd "${ROOT_DIR}"
mvn -f backend/pom.xml -pl adapter-web -am -Dmaven.test.skip=true install
exec mvn -f backend/adapter-web/pom.xml spring-boot:run
