#!/usr/bin/env python3
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MIG_DIR = ROOT / 'local/flyway-migration'
OUT_DIR = ROOT / 'db/sql'

DDL_OUT = OUT_DIR / '00_schema.sql'
BASE_OUT = OUT_DIR / '10_base_dict_config.sql'
ADMIN_OUT = OUT_DIR / '20_admin_seed.sql'
INIT_OUT = OUT_DIR / 'init_local.sql'

# Normalize legacy table names to the current canonical names so the generated
# init bundle can bootstrap the latest schema directly instead of depending on
# intermediate rename chains.
CANONICAL_TABLE_RENAMES = {
    'portal_user_account': 'user_account',
    'portal_user_profile': 'user_profile',
    'portal_user_security_setting': 'user_security_setting',
    'portal_user_privacy_setting': 'user_privacy_setting',
    'fin_influx_transaction': 'inbound_order',
    'influx_transaction': 'inbound_order',
    'influx_order': 'inbound_order',
    'outflux_order': 'outbound_order',
    'trade_business_index': 'trade_bill_index',
    'influx-center': 'inbound-center',
    'influx-order': 'inbound-order',
    'outflux-center': 'outbound-center',
    'outflux-order': 'outbound-order',
    'influx.order.view': 'inbound.order.view',
    'outflux.order.view': 'outbound.order.view',
    '/admin/influx/orders': '/admin/inbound/orders',
    '/admin/outflux/orders': '/admin/outbound/orders',
    '/admin/influx': '/admin/inbound',
    '/admin/outflux': '/admin/outbound',
    '/api/admin/influx/': '/api/admin/inbound/',
    '/api/admin/outflux/': '/api/admin/outbound/',
    '/api/influx/**': '/api/admin/inbound/**',
    '/api/outflux/**': '/api/admin/outbound/**',
}

# Skip migrations clearly aimed at demo/business sample data.
BLOCK_NAME_PAT = re.compile(
    r'(demo|seed_.*(gujun|qixin|friend|contact|message|red_packet|coupon|avatar|phone)|gujun|qixin|test_user|auto_login)',
    re.IGNORECASE,
)

# Allowlist DML target tables that are considered foundational for runtime.
BASE_DML_TABLES = {
    'acct_subject',
    'app_info',
    'app_version',
    'app_version_channel',
    'app_version_delivery_record',
    'pricing_rule',
}

ADMIN_ONLY_TABLES = {
    'admin_account',
    'admin_menu',
    'admin_rbac_module',
    'admin_rbac_permission',
    'admin_rbac_role',
    'admin_rbac_admin_role',
    'admin_rbac_role_menu',
    'admin_rbac_role_permission',
}

DML_HEAD_PAT = re.compile(r'^\s*(INSERT|UPDATE|DELETE|REPLACE)\b', re.IGNORECASE)
INS_PAT = re.compile(r'^\s*INSERT\s+INTO\s+`?([a-zA-Z0-9_]+)`?', re.IGNORECASE)
UPD_PAT = re.compile(r'^\s*UPDATE\s+(?:IGNORE\s+)?`?([a-zA-Z0-9_]+)`?', re.IGNORECASE)
DEL_PAT = re.compile(r'^\s*DELETE\s+FROM\s+`?([a-zA-Z0-9_]+)`?', re.IGNORECASE)
REP_PAT = re.compile(r'^\s*REPLACE\s+INTO\s+`?([a-zA-Z0-9_]+)`?', re.IGNORECASE)


def strip_leading_sql_comments(stmt: str) -> str:
    s = stmt.lstrip()
    while True:
        if s.startswith('--'):
            nl = s.find('\n')
            if nl == -1:
                return ''
            s = s[nl + 1 :].lstrip()
            continue
        if s.startswith('/*'):
            end = s.find('*/')
            if end == -1:
                return ''
            s = s[end + 2 :].lstrip()
            continue
        break
    return s


def split_statements(sql: str) -> list[str]:
    # good-enough splitter for flyway files (no procedure bodies in this repo)
    chunks: list[str] = []
    cur: list[str] = []
    in_single = False
    in_double = False
    prev = ''
    for ch in sql:
        if ch == "'" and not in_double and prev != '\\':
            in_single = not in_single
        elif ch == '"' and not in_single and prev != '\\':
            in_double = not in_double
        if ch == ';' and not in_single and not in_double:
            cur.append(ch)
            s = ''.join(cur).strip()
            if s:
                chunks.append(s)
            cur = []
        else:
            cur.append(ch)
        prev = ch
    rest = ''.join(cur).strip()
    if rest:
        chunks.append(rest)
    return chunks


def target_table(stmt: str) -> str | None:
    s = strip_leading_sql_comments(stmt)
    for pat in (INS_PAT, UPD_PAT, DEL_PAT, REP_PAT):
        m = pat.match(s)
        if m:
            return m.group(1).lower()
    return None


def is_dml(stmt: str) -> bool:
    s = strip_leading_sql_comments(stmt)
    return bool(DML_HEAD_PAT.match(s))


def should_skip_ddl(stmt: str) -> bool:
    normalized = strip_leading_sql_comments(stmt).strip().upper()
    if not normalized:
        return True
    if normalized.startswith('CREATE TEMPORARY TABLE') or normalized.startswith('DROP TEMPORARY TABLE'):
        return True
    if 'FLYWAY_SCHEMA_HISTORY' in normalized:
        return True
    if 'WEBSITE_VISIT_LOG' in normalized:
        return True
    return False


def normalize_stmt(stmt: str) -> str:
    normalized = strip_leading_sql_comments(stmt).strip().upper()
    return re.sub(r'\s+', ' ', normalized)


def rewrite_canonical_table_names(stmt: str) -> str:
    rewritten = stmt
    for legacy_name, canonical_name in sorted(
        CANONICAL_TABLE_RENAMES.items(), key=lambda item: len(item[0]), reverse=True
    ):
        rewritten = rewritten.replace(f'`{legacy_name}`', f'`{canonical_name}`')
        rewritten = rewritten.replace(f"'{legacy_name}'", f"'{canonical_name}'")
        rewritten = rewritten.replace(f'"{legacy_name}"', f'"{canonical_name}"')
        rewritten = re.sub(
            rf'(?<![@A-Za-z0-9_]){re.escape(legacy_name)}(?![A-Za-z0-9_])',
            canonical_name,
            rewritten,
        )
    return rewritten


def rewrite_generated_comment(label: str) -> str:
    rewritten = rewrite_canonical_table_names(label)
    rewritten = rewritten.replace('outflux', 'outbound')
    rewritten = rewritten.replace('influx', 'inbound')
    return rewritten


def should_skip_canonical_admin_rename_dml(migration_name: str, table: str, stmt: str) -> bool:
    if migration_name != 'V115__rename_influx_outflux_to_inbound_outbound.sql':
        return False
    if table not in ADMIN_ONLY_TABLES:
        return False
    upper_stmt = stmt.upper()
    return 'INFLUX' not in upper_stmt and 'OUTFLUX' not in upper_stmt


def is_prepare_stmt_wrapper(normalized_stmt: str) -> bool:
    return bool(re.fullmatch(r'PREPARE STMT FROM @[A-Z0-9_]+;', normalized_stmt))


def is_execute_stmt_wrapper(normalized_stmt: str) -> bool:
    return normalized_stmt == 'EXECUTE STMT;'


def is_deallocate_stmt_wrapper(normalized_stmt: str) -> bool:
    return normalized_stmt == 'DEALLOCATE PREPARE STMT;'


def contains_noop_table_rename(stmt: str) -> bool:
    return bool(
        re.search(
            r'RENAME\s+TABLE\s+`?([a-zA-Z0-9_]+)`?\s+TO\s+`?\1`?',
            stmt,
            re.IGNORECASE,
        )
    )


def migration_files() -> list[Path]:
    def version_key(path: Path) -> tuple[int, str]:
        m = re.match(r'^V(\d+)__', path.name)
        if not m:
            return (10**9, path.name)
        return (int(m.group(1)), path.name)

    return sorted(MIG_DIR.glob('V*.sql'), key=version_key)


def append_flyway_history_bootstrap(ddl_parts: list[str]) -> None:
    ddl_parts.append('\n-- Local bootstrap flyway history tables for schema parity')
    ddl_parts.append("""CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50) DEFAULT NULL,
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT DEFAULT NULL,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINT(1) NOT NULL,
    PRIMARY KEY (installed_rank),
    KEY flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;""")
    ddl_parts.append("""CREATE TABLE IF NOT EXISTS portal_flyway_schema_history (
    installed_rank INT NOT NULL COMMENT '安装顺序',
    version VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '迁移版本号',
    description VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '迁移说明',
    type VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '迁移类型',
    script VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '迁移脚本',
    checksum INT DEFAULT NULL COMMENT '校验和',
    installed_by VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行用户',
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    execution_time INT NOT NULL COMMENT '执行耗时（毫秒）',
    success TINYINT(1) NOT NULL COMMENT '是否执行成功',
    PRIMARY KEY (installed_rank),
    KEY portal_flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='Flyway迁移历史';""")


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)

    ddl_parts: list[str] = []
    base_parts: list[str] = []
    admin_parts: list[str] = []

    ddl_parts.append('-- Auto-generated minimal local init SQL (DDL only)')
    ddl_parts.append('-- Source: local/flyway-migration (private, gitignored)')
    ddl_parts.append('SET NAMES utf8mb4;')
    ddl_parts.append('SET FOREIGN_KEY_CHECKS = 0;')
    append_flyway_history_bootstrap(ddl_parts)

    base_parts.append('-- Auto-generated base dictionary/config DML (no business demo data)')
    base_parts.append('SET NAMES utf8mb4;')
    base_parts.append('SET FOREIGN_KEY_CHECKS = 0;')

    admin_parts.append('-- Auto-generated admin seed DML (single admin account + RBAC/menu baseline)')
    admin_parts.append('SET NAMES utf8mb4;')
    admin_parts.append('SET FOREIGN_KEY_CHECKS = 0;')

    migration_list = migration_files()
    if not migration_list:
        raise RuntimeError(f'No migration scripts found under private directory: {MIG_DIR}')

    for f in migration_list:
        name = f.name
        sql = f.read_text(encoding='utf-8')
        stmts = split_statements(sql)
        ddl_tag_written = False
        skip_prepare_stmt_chain = 0

        for st in stmts:
            s = st.strip()
            if not s:
                continue

            if not is_dml(s):
                s = rewrite_canonical_table_names(s)
                normalized_stmt = normalize_stmt(s)

                if skip_prepare_stmt_chain > 0 and (
                    is_prepare_stmt_wrapper(normalized_stmt)
                    or is_execute_stmt_wrapper(normalized_stmt)
                    or is_deallocate_stmt_wrapper(normalized_stmt)
                ):
                    skip_prepare_stmt_chain -= 1
                    continue

                if should_skip_ddl(s):
                    if normalized_stmt.startswith('SET @__SQL') and 'WEBSITE_VISIT_LOG' in normalized_stmt:
                        skip_prepare_stmt_chain = 3
                    continue
                if contains_noop_table_rename(s):
                    if normalized_stmt.startswith('SET @'):
                        skip_prepare_stmt_chain = 3
                    continue
                if not ddl_tag_written:
                    ddl_parts.append(f"\n-- [{rewrite_generated_comment(name)}]")
                    ddl_tag_written = True
                ddl_parts.append(s)
                continue

            # DML path
            s = rewrite_canonical_table_names(s)
            table = target_table(s)
            if not table:
                continue
            if should_skip_canonical_admin_rename_dml(name, table, s):
                continue

            # hard skip demo-like migrations by filename
            if BLOCK_NAME_PAT.search(name):
                continue

            if table in BASE_DML_TABLES:
                base_parts.append(f"\n-- [{rewrite_generated_comment(name)}] {table}")
                base_parts.append(s)

            if table in ADMIN_ONLY_TABLES:
                admin_parts.append(f"\n-- [{rewrite_generated_comment(name)}] {table}")
                admin_parts.append(s)

    # Keep exactly one default admin account.
    admin_parts.append("\n-- Enforce single default admin account")
    admin_parts.append("DELETE ar FROM admin_rbac_admin_role ar LEFT JOIN admin_account aa ON aa.admin_id = ar.admin_id WHERE aa.username <> 'admin' OR aa.username IS NULL;")
    admin_parts.append("DELETE FROM admin_account WHERE username <> 'admin';")

    ddl_parts.append('SET FOREIGN_KEY_CHECKS = 1;')
    base_parts.append('SET FOREIGN_KEY_CHECKS = 1;')
    admin_parts.append('SET FOREIGN_KEY_CHECKS = 1;')

    DDL_OUT.write_text('\n'.join(ddl_parts) + '\n', encoding='utf-8')
    BASE_OUT.write_text('\n'.join(base_parts) + '\n', encoding='utf-8')
    ADMIN_OUT.write_text('\n'.join(admin_parts) + '\n', encoding='utf-8')

    init_sql = """-- Local minimal initialization entry (DDL + base config + admin)
-- Usage:
--   mysql -h127.0.0.1 -P3306 -u<user> -p portal < db/sql/init_local.sql

SOURCE db/sql/00_schema.sql;
SOURCE db/sql/10_base_dict_config.sql;
SOURCE db/sql/20_admin_seed.sql;
"""
    INIT_OUT.write_text(init_sql, encoding='utf-8')

    print(f'Generated:\n- {DDL_OUT}\n- {BASE_OUT}\n- {ADMIN_OUT}\n- {INIT_OUT}')


if __name__ == '__main__':
    main()
