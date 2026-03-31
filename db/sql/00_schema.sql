-- Auto-generated minimal local init SQL (DDL only)
-- Source: local/flyway-migration (private, gitignored)
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Local bootstrap flyway history tables for schema parity
CREATE TABLE IF NOT EXISTS flyway_schema_history (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
CREATE TABLE IF NOT EXISTS portal_flyway_schema_history (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='Flyway迁移历史';

-- [V1__create_user_tables.sql]
CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    aipay_uid VARCHAR(64) NOT NULL,
    login_id VARCHAR(128) NOT NULL,
    account_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    kyc_level VARCHAR(16) NOT NULL DEFAULT 'L1',
    login_password_set TINYINT(1) NOT NULL DEFAULT 1,
    pay_password_set TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_portal_user_account_user_id (user_id),
    UNIQUE KEY uk_portal_user_account_aipay_uid (aipay_uid),
    KEY idx_portal_user_account_login_id (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS user_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    avatar_url VARCHAR(512) NULL,
    country_code VARCHAR(8) NULL,
    mobile VARCHAR(32) NULL,
    masked_real_name VARCHAR(64) NULL,
    gender VARCHAR(16) NULL,
    region VARCHAR(64) NULL,
    birthday DATE NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_portal_user_profile_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS user_security_setting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    biometric_enabled TINYINT(1) NOT NULL DEFAULT 0,
    two_factor_mode VARCHAR(32) NOT NULL DEFAULT 'NONE',
    risk_level VARCHAR(32) NOT NULL DEFAULT 'LOW',
    device_lock_enabled TINYINT(1) NOT NULL DEFAULT 0,
    privacy_mode_enabled TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_portal_user_security_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS user_privacy_setting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    allow_search_by_mobile TINYINT(1) NOT NULL DEFAULT 1,
    allow_search_by_aipay_uid TINYINT(1) NOT NULL DEFAULT 1,
    hide_real_name TINYINT(1) NOT NULL DEFAULT 0,
    personalized_recommendation_enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_portal_user_privacy_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [V2__add_user_login_password.sql]
SET @column_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_account'
      AND COLUMN_NAME = 'login_password_sha256'
);
SET @ddl = IF(
    @column_exists = 0,
    'ALTER TABLE user_account ADD COLUMN login_password_sha256 CHAR(64) NULL AFTER pay_password_set',
    'SET @noop = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V3__rename_portal_user_tables_to_user_tables.sql]
SET @portal_user_account_exists = (
    SELECT COUNT(1)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_account'
);
SET @user_account_exists = (
    SELECT COUNT(1)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_account'
);
SET @portal_user_profile_exists = (
    SELECT COUNT(1)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_profile'
);
SET @user_profile_exists = (
    SELECT COUNT(1)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_profile'
);
SET @portal_user_security_exists = (
    SELECT COUNT(1)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_security_setting'
);
SET @user_security_exists = (
    SELECT COUNT(1)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_security_setting'
);
SET @portal_user_privacy_exists = (
    SELECT COUNT(1)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_privacy_setting'
);
SET @user_privacy_exists = (
    SELECT COUNT(1)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_privacy_setting'
);

-- [V4__prefix_legacy_tables_with_portal.sql]
SET SESSION group_concat_max_len = 1024000;
SET @rename_sql = IF(
    @rename_pairs IS NULL,
    'SET @noop_portal_prefix = 1',
    CONCAT('RENAME TABLE ', @rename_pairs)
);
PREPARE stmt FROM @rename_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V5__rename_legacy_uid_to_aipay_uid.sql]
SET @user_account_old_col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_account'
      AND COLUMN_NAME = 'aipay_uid'
);
SET @user_account_new_col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_account'
      AND COLUMN_NAME = 'aipay_uid'
);
SET @ddl = IF(
    @user_account_old_col_exists = 1 AND @user_account_new_col_exists = 0,
    'ALTER TABLE user_account CHANGE COLUMN aipay_uid aipay_uid VARCHAR(64) NOT NULL',
    'SET @noop_user_account_col = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @old_uid_index_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_account'
      AND INDEX_NAME = 'uk_portal_user_account_aipay_uid'
);
SET @new_uid_index_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_account'
      AND INDEX_NAME = 'uk_user_account_aipay_uid'
);
SET @ddl = IF(
    @old_uid_index_exists = 1 AND @new_uid_index_exists = 0,
    'ALTER TABLE user_account RENAME INDEX uk_portal_user_account_aipay_uid TO uk_user_account_aipay_uid',
    'SET @noop_uid_index = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @privacy_old_col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_privacy_setting'
      AND COLUMN_NAME = 'allow_search_by_aipay_uid'
);
SET @privacy_new_col_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_privacy_setting'
      AND COLUMN_NAME = 'allow_search_by_aipay_uid'
);
SET @ddl = IF(
    @privacy_old_col_exists = 1 AND @privacy_new_col_exists = 0,
    'ALTER TABLE user_privacy_setting CHANGE COLUMN allow_search_by_aipay_uid allow_search_by_aipay_uid TINYINT(1) NOT NULL DEFAULT 1',
    'SET @noop_privacy_col = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V6__create_wallet_account_tables.sql]
CREATE TABLE IF NOT EXISTS wallet_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    currency_code VARCHAR(8) NOT NULL,
    available_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    reserved_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    account_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wallet_account_user_id (user_id),
    KEY idx_wallet_account_status (account_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS wallet_tcc_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    xid VARCHAR(128) NOT NULL,
    branch_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    operation_type VARCHAR(16) NOT NULL,
    branch_status VARCHAR(16) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    business_no VARCHAR(64) NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wallet_tcc_xid_branch (xid, branch_id),
    KEY idx_wallet_tcc_user_id (user_id),
    KEY idx_wallet_tcc_status (branch_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [V7__create_coupon_and_admin_tables.sql]
CREATE TABLE IF NOT EXISTS admin_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    password_sha256 CHAR(64) NOT NULL,
    account_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_account_admin_id (admin_id),
    UNIQUE KEY uk_admin_account_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS admin_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_code VARCHAR(64) NOT NULL,
    parent_code VARCHAR(64) NULL,
    menu_name VARCHAR(64) NOT NULL,
    path VARCHAR(255) NOT NULL,
    icon VARCHAR(64) NULL,
    sort_no INT NOT NULL DEFAULT 0,
    visible TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_menu_code (menu_code),
    KEY idx_admin_menu_parent (parent_code),
    KEY idx_admin_menu_visible_sort (visible, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS coupon_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_code VARCHAR(64) NOT NULL,
    template_name VARCHAR(128) NOT NULL,
    scene_type VARCHAR(64) NOT NULL,
    value_type VARCHAR(16) NOT NULL,
    amount DECIMAL(18,2) NULL,
    min_amount DECIMAL(18,2) NULL,
    max_amount DECIMAL(18,2) NULL,
    threshold_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_budget DECIMAL(18,2) NOT NULL,
    total_stock INT NOT NULL,
    claimed_count INT NOT NULL DEFAULT 0,
    per_user_limit INT NOT NULL DEFAULT 1,
    claim_start_time DATETIME NOT NULL,
    claim_end_time DATETIME NOT NULL,
    use_start_time DATETIME NOT NULL,
    use_end_time DATETIME NOT NULL,
    funding_source VARCHAR(64) NOT NULL,
    rule_payload JSON NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_coupon_template_code (template_code),
    KEY idx_coupon_template_scene_status (scene_type, status),
    KEY idx_coupon_template_claim_window (claim_start_time, claim_end_time),
    KEY idx_coupon_template_use_window (use_start_time, use_end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS coupon_issue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coupon_no VARCHAR(64) NOT NULL,
    template_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    coupon_amount DECIMAL(18,2) NOT NULL,
    status VARCHAR(16) NOT NULL,
    claim_channel VARCHAR(64) NOT NULL,
    business_no VARCHAR(64) NULL,
    order_no VARCHAR(64) NULL,
    claimed_at DATETIME NOT NULL,
    expire_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_coupon_issue_coupon_no (coupon_no),
    KEY idx_coupon_issue_template_user (template_id, user_id),
    KEY idx_coupon_issue_user_status (user_id, status),
    KEY idx_coupon_issue_expire_at (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [V8__create_fund_account_tables.sql]
CREATE TABLE IF NOT EXISTS fund_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    fund_code VARCHAR(32) NOT NULL,
    currency_code VARCHAR(8) NOT NULL,
    available_share DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    frozen_share DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    pending_subscribe_amount DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    pending_redeem_share DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    accumulated_income DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    yesterday_income DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    latest_nav DECIMAL(18,4) NOT NULL DEFAULT 1.0000,
    account_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_fund_account_user_id (user_id),
    KEY idx_fund_account_fund_code (fund_code),
    KEY idx_fund_account_status (account_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS fund_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    fund_code VARCHAR(32) NOT NULL,
    transaction_type VARCHAR(32) NOT NULL,
    transaction_status VARCHAR(16) NOT NULL,
    request_amount DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    request_share DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    confirmed_amount DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    confirmed_share DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    business_no VARCHAR(64) NULL,
    ext_info VARCHAR(256) NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_fund_transaction_order_no (order_no),
    KEY idx_fund_transaction_user_id (user_id),
    KEY idx_fund_transaction_type_status (transaction_type, transaction_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [V9__enhance_fund_account_for_aicash.sql]
SET @old_fund_account_index_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fund_account'
      AND INDEX_NAME = 'uk_fund_account_user_id'
);
SET @new_fund_account_index_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fund_account'
      AND INDEX_NAME = 'uk_fund_account_user_fund'
);
SET @ddl = IF(
    @old_fund_account_index_exists = 1,
    'ALTER TABLE fund_account DROP INDEX uk_fund_account_user_id',
    'SET @noop_drop_old_fund_account_index = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = IF(
    @new_fund_account_index_exists = 0,
    'ALTER TABLE fund_account ADD UNIQUE KEY uk_fund_account_user_fund (user_id, fund_code)',
    'SET @noop_add_new_fund_account_index = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE TABLE IF NOT EXISTS fund_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fund_code VARCHAR(32) NOT NULL,
    product_name VARCHAR(64) NOT NULL,
    currency_code VARCHAR(8) NOT NULL,
    product_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    single_subscribe_min_amount DECIMAL(18,4) NOT NULL DEFAULT 0.0100,
    single_subscribe_max_amount DECIMAL(18,4) NOT NULL DEFAULT 1000000.0000,
    daily_subscribe_max_amount DECIMAL(18,4) NOT NULL DEFAULT 5000000.0000,
    single_redeem_min_share DECIMAL(18,4) NOT NULL DEFAULT 0.0100,
    single_redeem_max_share DECIMAL(18,4) NOT NULL DEFAULT 1000000.0000,
    daily_redeem_max_share DECIMAL(18,4) NOT NULL DEFAULT 5000000.0000,
    fast_redeem_daily_quota DECIMAL(18,4) NOT NULL DEFAULT 10000000.0000,
    fast_redeem_per_user_daily_quota DECIMAL(18,4) NOT NULL DEFAULT 100000.0000,
    switch_enabled TINYINT(1) NOT NULL DEFAULT 1,
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_fund_product_fund_code (fund_code),
    KEY idx_fund_product_status (product_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS fund_income_calendar (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fund_code VARCHAR(32) NOT NULL,
    biz_date DATE NOT NULL,
    nav DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    income_per_10k DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    calendar_status VARCHAR(16) NOT NULL DEFAULT 'PLANNED',
    published_at DATETIME NULL,
    settled_at DATETIME NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_fund_income_calendar (fund_code, biz_date),
    KEY idx_fund_income_calendar_status (calendar_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS fund_fast_redeem_quota (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fund_code VARCHAR(32) NOT NULL,
    quota_date DATE NOT NULL,
    quota_limit DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    quota_used DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_fund_fast_redeem_quota (fund_code, quota_date),
    KEY idx_fund_fast_redeem_quota_date (quota_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS fund_user_fast_redeem_quota (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fund_code VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    quota_date DATE NOT NULL,
    quota_limit DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    quota_used DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_fund_user_fast_redeem_quota (fund_code, user_id, quota_date),
    KEY idx_fund_user_fast_redeem_quota_date (quota_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [V10__create_credit_account_tables.sql]
CREATE TABLE IF NOT EXISTS credit_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    total_limit DECIMAL(18,2) NOT NULL,
    principal_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    principal_unreach_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    overdue_principal_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    overdue_principal_unreach_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fine_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    account_status VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    pay_status VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_credit_account_account_no (account_no),
    UNIQUE KEY uk_credit_account_user_id (user_id),
    KEY idx_credit_account_status (account_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [V11__create_pay_order_tables.sql]
CREATE TABLE IF NOT EXISTS pay_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id VARCHAR(64) NOT NULL,
    merchant_order_no VARCHAR(64) NOT NULL,
    business_scene_code VARCHAR(64) NOT NULL,
    payer_user_id BIGINT NOT NULL,
    payee_user_id BIGINT NULL,
    currency_code VARCHAR(8) NOT NULL,
    original_amount DECIMAL(18,2) NOT NULL,
    discount_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    payable_amount DECIMAL(18,2) NOT NULL,
    actual_paid_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    wallet_debit_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fund_debit_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    credit_debit_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    coupon_no VARCHAR(64) NULL,
    seata_xid VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    failure_reason VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pay_order_payment_id (payment_id),
    UNIQUE KEY uk_pay_order_merchant_order_no (merchant_order_no),
    KEY idx_pay_order_payer_status (payer_user_id, status),
    KEY idx_pay_order_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS pay_participant_branch (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id VARCHAR(64) NOT NULL,
    participant_type VARCHAR(32) NOT NULL,
    branch_id VARCHAR(64) NOT NULL,
    participant_resource_id VARCHAR(128) NOT NULL,
    request_payload TEXT NULL,
    status VARCHAR(32) NOT NULL,
    response_message VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pay_branch_payment_participant (payment_id, participant_type),
    UNIQUE KEY uk_pay_branch_payment_branch_id (payment_id, branch_id),
    KEY idx_pay_branch_payment_status (payment_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [V12__create_credit_tcc_tables.sql]
CREATE TABLE IF NOT EXISTS credit_tcc_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    xid VARCHAR(128) NOT NULL,
    branch_id VARCHAR(64) NOT NULL,
    account_no VARCHAR(32) NOT NULL,
    operation_type VARCHAR(16) NOT NULL,
    asset_category VARCHAR(32) NOT NULL,
    branch_status VARCHAR(16) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    business_no VARCHAR(64) NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_credit_tcc_xid_branch (xid, branch_id),
    KEY idx_credit_tcc_account_no (account_no),
    KEY idx_credit_tcc_status (branch_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [V13__create_admin_rbac_tables.sql]
CREATE TABLE IF NOT EXISTS admin_rbac_module (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    module_code VARCHAR(64) NOT NULL,
    module_name VARCHAR(64) NOT NULL,
    module_desc VARCHAR(255) NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    sort_no INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_rbac_module_code (module_code),
    KEY idx_admin_rbac_module_enabled_sort (enabled, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS admin_rbac_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_code VARCHAR(128) NOT NULL,
    permission_name VARCHAR(128) NOT NULL,
    module_code VARCHAR(64) NOT NULL,
    resource_type VARCHAR(16) NOT NULL DEFAULT 'API',
    http_method VARCHAR(16) NULL,
    path_pattern VARCHAR(255) NULL,
    permission_desc VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_rbac_permission_code (permission_code),
    KEY idx_admin_rbac_permission_module (module_code),
    KEY idx_admin_rbac_permission_resource (resource_type, http_method)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS admin_rbac_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    role_scope VARCHAR(32) NOT NULL DEFAULT 'PLATFORM',
    role_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    is_builtin TINYINT(1) NOT NULL DEFAULT 0,
    role_desc VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_rbac_role_code (role_code),
    KEY idx_admin_rbac_role_status (role_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS admin_rbac_admin_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    role_code VARCHAR(64) NOT NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_rbac_admin_role (admin_id, role_code),
    KEY idx_admin_rbac_admin_role_role (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS admin_rbac_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL,
    permission_code VARCHAR(128) NOT NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_rbac_role_permission (role_code, permission_code),
    KEY idx_admin_rbac_role_permission_permission (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS admin_rbac_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL,
    menu_code VARCHAR(64) NOT NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_rbac_role_menu (role_code, menu_code),
    KEY idx_admin_rbac_role_menu_menu (menu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [V14__create_pricing_tables_and_permissions.sql]
CREATE TABLE IF NOT EXISTS pricing_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_code VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    business_scene_code VARCHAR(64) NOT NULL,
    payment_method VARCHAR(64) NOT NULL,
    currency_code VARCHAR(64) NOT NULL,
    fee_mode VARCHAR(32) NOT NULL,
    fee_rate DECIMAL(10,6) NOT NULL DEFAULT 0.000000,
    fixed_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    min_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    max_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fee_bearer VARCHAR(32) NOT NULL,
    priority INT NOT NULL DEFAULT 100,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    valid_from DATETIME NULL,
    valid_to DATETIME NULL,
    rule_payload JSON NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pricing_rule_code (rule_code),
    KEY idx_pricing_rule_match_status (business_scene_code, payment_method, currency_code, status),
    KEY idx_pricing_rule_priority (priority, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS pricing_quote (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    quote_no VARCHAR(64) NOT NULL,
    request_no VARCHAR(64) NOT NULL,
    rule_id BIGINT NOT NULL,
    rule_code VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    business_scene_code VARCHAR(64) NOT NULL,
    payment_method VARCHAR(64) NOT NULL,
    currency_code VARCHAR(64) NOT NULL,
    original_amount DECIMAL(18,2) NOT NULL,
    fee_amount DECIMAL(18,2) NOT NULL,
    payable_amount DECIMAL(18,2) NOT NULL,
    settle_amount DECIMAL(18,2) NOT NULL,
    fee_mode VARCHAR(32) NOT NULL,
    fee_bearer VARCHAR(32) NOT NULL,
    fee_rate DECIMAL(10,6) NOT NULL DEFAULT 0.000000,
    fixed_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    rule_payload JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pricing_quote_quote_no (quote_no),
    UNIQUE KEY uk_pricing_quote_request_no (request_no),
    KEY idx_pricing_quote_rule_code (rule_code),
    KEY idx_pricing_quote_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [V15__add_comments_for_coupon_pay_pricing_tables.sql]
ALTER TABLE coupon_template
COMMENT = '红包模板表：定义营销红包的发放规则、库存约束、领取与使用时间窗口';
ALTER TABLE coupon_template
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '红包模板主键ID',
    MODIFY COLUMN template_code VARCHAR(64) NOT NULL COMMENT '红包模板编码，运营侧唯一标识',
    MODIFY COLUMN template_name VARCHAR(128) NOT NULL COMMENT '红包模板名称，用于后台展示',
    MODIFY COLUMN scene_type VARCHAR(64) NOT NULL COMMENT '营销场景类型，如拉新、促活、补贴',
    MODIFY COLUMN value_type VARCHAR(16) NOT NULL COMMENT '红包面额类型，固定或随机',
    MODIFY COLUMN amount DECIMAL(18,2) NULL COMMENT '固定面额红包金额，value_type为FIXED时生效',
    MODIFY COLUMN min_amount DECIMAL(18,2) NULL COMMENT '随机红包最小金额，value_type为RANDOM时生效',
    MODIFY COLUMN max_amount DECIMAL(18,2) NULL COMMENT '随机红包最大金额，value_type为RANDOM时生效',
    MODIFY COLUMN threshold_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛金额，订单达到该金额才可抵扣',
    MODIFY COLUMN total_budget DECIMAL(18,2) NOT NULL COMMENT '红包活动总预算',
    MODIFY COLUMN total_stock INT NOT NULL COMMENT '红包总库存数量',
    MODIFY COLUMN claimed_count INT NOT NULL DEFAULT 0 COMMENT '已发放数量',
    MODIFY COLUMN per_user_limit INT NOT NULL DEFAULT 1 COMMENT '单用户可领取上限',
    MODIFY COLUMN claim_start_time DATETIME NOT NULL COMMENT '领取开始时间',
    MODIFY COLUMN claim_end_time DATETIME NOT NULL COMMENT '领取结束时间',
    MODIFY COLUMN use_start_time DATETIME NOT NULL COMMENT '可使用开始时间',
    MODIFY COLUMN use_end_time DATETIME NOT NULL COMMENT '可使用结束时间',
    MODIFY COLUMN funding_source VARCHAR(64) NOT NULL COMMENT '资金承担方，如平台或商户',
    MODIFY COLUMN rule_payload JSON NULL COMMENT '扩展规则JSON，例如黑白名单或渠道限制',
    MODIFY COLUMN status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '模板状态，如DRAFT/ACTIVE/PAUSED/EXPIRED',
    MODIFY COLUMN created_by VARCHAR(64) NOT NULL COMMENT '创建操作人',
    MODIFY COLUMN updated_by VARCHAR(64) NOT NULL COMMENT '最后更新操作人',
    MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE coupon_issue
COMMENT = '红包发放实例表：记录红包发给哪个用户及其在支付中的预占与核销状态';
ALTER TABLE coupon_issue
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '红包实例主键ID',
    MODIFY COLUMN coupon_no VARCHAR(64) NOT NULL COMMENT '红包券号，业务侧唯一',
    MODIFY COLUMN template_id BIGINT NOT NULL COMMENT '关联红包模板ID',
    MODIFY COLUMN user_id BIGINT NOT NULL COMMENT '红包归属用户ID',
    MODIFY COLUMN coupon_amount DECIMAL(18,2) NOT NULL COMMENT '红包金额',
    MODIFY COLUMN status VARCHAR(16) NOT NULL COMMENT '实例状态，如ISSUED/RESERVED/USED/EXPIRED',
    MODIFY COLUMN claim_channel VARCHAR(64) NOT NULL COMMENT '领取渠道，如活动页或补偿发放',
    MODIFY COLUMN business_no VARCHAR(64) NULL COMMENT '外部业务号，用于营销链路追踪',
    MODIFY COLUMN order_no VARCHAR(64) NULL COMMENT '核销时绑定的订单号',
    MODIFY COLUMN claimed_at DATETIME NOT NULL COMMENT '领取时间',
    MODIFY COLUMN expire_at DATETIME NOT NULL COMMENT '过期时间',
    MODIFY COLUMN used_at DATETIME NULL COMMENT '核销时间',
    MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE pay_order
COMMENT = '支付主单表：记录一次支付编排的金额拆分、状态流转和失败原因';
ALTER TABLE pay_order
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付主单主键ID',
    MODIFY COLUMN payment_id VARCHAR(64) NOT NULL COMMENT '支付流水号，系统内唯一',
    MODIFY COLUMN merchant_order_no VARCHAR(64) NOT NULL COMMENT '商户订单号，幂等键',
    MODIFY COLUMN business_scene_code VARCHAR(64) NOT NULL COMMENT '业务场景编码，如转账或收银台付款',
    MODIFY COLUMN payer_user_id BIGINT NOT NULL COMMENT '付款用户ID',
    MODIFY COLUMN payee_user_id BIGINT NULL COMMENT '收款用户ID，可为空',
    MODIFY COLUMN currency_code VARCHAR(8) NOT NULL COMMENT '币种编码，如CNY',
    MODIFY COLUMN original_amount DECIMAL(18,2) NOT NULL COMMENT '订单原始金额',
    MODIFY COLUMN discount_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额（红包抵扣等）',
    MODIFY COLUMN payable_amount DECIMAL(18,2) NOT NULL COMMENT '应付金额',
    MODIFY COLUMN actual_paid_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额，提交成功后回填',
    MODIFY COLUMN wallet_debit_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '钱包账户扣款金额',
    MODIFY COLUMN fund_debit_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '余额宝/基金账户扣款金额',
    MODIFY COLUMN credit_debit_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '信用账户扣款金额',
    MODIFY COLUMN coupon_no VARCHAR(64) NULL COMMENT '使用的红包券号',
    MODIFY COLUMN seata_xid VARCHAR(128) NOT NULL COMMENT '全局事务XID',
    MODIFY COLUMN status VARCHAR(32) NOT NULL COMMENT '支付状态，如TRYING/PREPARED/COMMITTED/ROLLED_BACK',
    MODIFY COLUMN failure_reason VARCHAR(255) NULL COMMENT '失败或回滚原因',
    MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE pay_participant_branch
COMMENT = '支付参与方分支表：记录钱包/基金/信用/红包各参与方分支执行结果';
ALTER TABLE pay_participant_branch
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '参与方分支主键ID',
    MODIFY COLUMN payment_id VARCHAR(64) NOT NULL COMMENT '关联支付流水号',
    MODIFY COLUMN participant_type VARCHAR(32) NOT NULL COMMENT '参与方类型，如WALLET/FUND/CREDIT/COUPON',
    MODIFY COLUMN branch_id VARCHAR(64) NOT NULL COMMENT '分支事务ID',
    MODIFY COLUMN participant_resource_id VARCHAR(128) NOT NULL COMMENT '参与方资源标识，如accountNo或couponNo',
    MODIFY COLUMN request_payload TEXT NULL COMMENT '分支请求快照',
    MODIFY COLUMN status VARCHAR(32) NOT NULL COMMENT '分支状态，如TRY_OK/CONFIRM_OK/CANCEL_OK',
    MODIFY COLUMN response_message VARCHAR(255) NULL COMMENT '分支执行返回信息',
    MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE pricing_rule
COMMENT = '计费规则表：定义场景维度下的费率、固定费、封顶保底和承担方策略';
ALTER TABLE pricing_rule
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '计费规则主键ID',
    MODIFY COLUMN rule_code VARCHAR(64) NOT NULL COMMENT '计费规则编码，运营侧唯一',
    MODIFY COLUMN rule_name VARCHAR(128) NOT NULL COMMENT '计费规则名称',
    MODIFY COLUMN business_scene_code VARCHAR(64) NOT NULL COMMENT '业务场景编码，支持ALL通配',
    MODIFY COLUMN payment_method VARCHAR(64) NOT NULL COMMENT '支付方式编码，支持ALL通配',
    MODIFY COLUMN currency_code VARCHAR(64) NOT NULL COMMENT '币种编码，支持ALL通配',
    MODIFY COLUMN fee_mode VARCHAR(32) NOT NULL COMMENT '计费模式：RATE/FIXED/RATE_PLUS_FIXED',
    MODIFY COLUMN fee_rate DECIMAL(10,6) NOT NULL DEFAULT 0.000000 COMMENT '费率值，如0.006表示千分之六',
    MODIFY COLUMN fixed_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '固定手续费',
    MODIFY COLUMN min_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '手续费保底值',
    MODIFY COLUMN max_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '手续费封顶值，0表示不封顶',
    MODIFY COLUMN fee_bearer VARCHAR(32) NOT NULL COMMENT '手续费承担方：PAYER/PAYEE/PLATFORM',
    MODIFY COLUMN priority INT NOT NULL DEFAULT 100 COMMENT '规则优先级，数值越大优先',
    MODIFY COLUMN status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '规则状态：DRAFT/ACTIVE/INACTIVE',
    MODIFY COLUMN valid_from DATETIME NULL COMMENT '生效开始时间，空表示立即可用',
    MODIFY COLUMN valid_to DATETIME NULL COMMENT '生效结束时间，空表示长期有效',
    MODIFY COLUMN rule_payload JSON NULL COMMENT '扩展规则JSON，如特殊商户白名单',
    MODIFY COLUMN created_by VARCHAR(64) NOT NULL COMMENT '创建操作人',
    MODIFY COLUMN updated_by VARCHAR(64) NOT NULL COMMENT '最后更新操作人',
    MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE pricing_quote
COMMENT = '计费快照表：记录一次交易请求命中的规则与计算结果，作为后续支付核对依据';
ALTER TABLE pricing_quote
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '计费快照主键ID',
    MODIFY COLUMN quote_no VARCHAR(64) NOT NULL COMMENT '报价单号，系统内唯一',
    MODIFY COLUMN request_no VARCHAR(64) NOT NULL COMMENT '交易侧请求号，幂等键',
    MODIFY COLUMN rule_id BIGINT NOT NULL COMMENT '命中的计费规则ID',
    MODIFY COLUMN rule_code VARCHAR(64) NOT NULL COMMENT '命中的计费规则编码',
    MODIFY COLUMN rule_name VARCHAR(128) NOT NULL COMMENT '命中的计费规则名称',
    MODIFY COLUMN business_scene_code VARCHAR(64) NOT NULL COMMENT '报价时使用的业务场景编码',
    MODIFY COLUMN payment_method VARCHAR(64) NOT NULL COMMENT '报价时使用的支付方式编码',
    MODIFY COLUMN currency_code VARCHAR(64) NOT NULL COMMENT '报价时使用的币种编码',
    MODIFY COLUMN original_amount DECIMAL(18,2) NOT NULL COMMENT '原始交易金额',
    MODIFY COLUMN fee_amount DECIMAL(18,2) NOT NULL COMMENT '计算出的手续费金额',
    MODIFY COLUMN payable_amount DECIMAL(18,2) NOT NULL COMMENT '付款方应付金额',
    MODIFY COLUMN settle_amount DECIMAL(18,2) NOT NULL COMMENT '收款方结算金额',
    MODIFY COLUMN fee_mode VARCHAR(32) NOT NULL COMMENT '本次报价采用的计费模式快照',
    MODIFY COLUMN fee_bearer VARCHAR(32) NOT NULL COMMENT '本次报价采用的承担方快照',
    MODIFY COLUMN fee_rate DECIMAL(10,6) NOT NULL DEFAULT 0.000000 COMMENT '本次报价采用的费率快照',
    MODIFY COLUMN fixed_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '本次报价采用的固定费快照',
    MODIFY COLUMN rule_payload JSON NULL COMMENT '规则扩展参数快照',
    MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- [V16__add_comments_for_all_backend_tables.sql]
ALTER TABLE `admin_account`
COMMENT = '后台管理员账户表：记录运营人员登录凭证、账户状态与最近登录时间';
ALTER TABLE `admin_account`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '后台管理员账户主键ID',
    MODIFY COLUMN `admin_id` bigint NOT NULL COMMENT '管理员业务ID',
    MODIFY COLUMN `username` varchar(64) NOT NULL COMMENT '管理员登录账号',
    MODIFY COLUMN `display_name` varchar(64) NOT NULL COMMENT '管理员展示名称',
    MODIFY COLUMN `password_sha256` char(64) NOT NULL COMMENT '管理员登录密码摘要（SHA-256）',
    MODIFY COLUMN `account_status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '管理员账户状态，ACTIVE/LOCKED/DISABLED',
    MODIFY COLUMN `last_login_at` datetime NULL COMMENT '最近登录时间',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `admin_menu`
COMMENT = '后台菜单表：维护运营后台导航结构、路由路径与可见性配置';
ALTER TABLE `admin_menu`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '后台菜单主键ID',
    MODIFY COLUMN `menu_code` varchar(64) NOT NULL COMMENT '菜单编码，后台内唯一',
    MODIFY COLUMN `parent_code` varchar(64) NULL COMMENT '父级菜单编码，顶级菜单为空',
    MODIFY COLUMN `menu_name` varchar(64) NOT NULL COMMENT '菜单名称',
    MODIFY COLUMN `path` varchar(255) NOT NULL COMMENT '前端路由路径',
    MODIFY COLUMN `icon` varchar(64) NULL COMMENT '菜单图标标识',
    MODIFY COLUMN `sort_no` int NOT NULL DEFAULT 0 COMMENT '菜单排序号，越小越靠前',
    MODIFY COLUMN `visible` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否在导航中可见，1可见0隐藏',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `admin_rbac_admin_role`
COMMENT = '管理员角色绑定表：记录管理员与角色的授权关系';
ALTER TABLE `admin_rbac_admin_role`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员角色绑定主键ID',
    MODIFY COLUMN `admin_id` bigint NOT NULL COMMENT '管理员业务ID',
    MODIFY COLUMN `role_code` varchar(64) NOT NULL COMMENT '角色编码',
    MODIFY COLUMN `created_by` varchar(64) NOT NULL DEFAULT 'system' COMMENT '授权操作人',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `admin_rbac_module`
COMMENT = 'RBAC模块表：定义后台权限所属业务模块与启停状态';
ALTER TABLE `admin_rbac_module`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '后台权限模块主键ID',
    MODIFY COLUMN `module_code` varchar(64) NOT NULL COMMENT '模块编码，后台内唯一',
    MODIFY COLUMN `module_name` varchar(64) NOT NULL COMMENT '模块名称',
    MODIFY COLUMN `module_desc` varchar(255) NULL COMMENT '模块说明',
    MODIFY COLUMN `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '模块是否启用，1启用0停用',
    MODIFY COLUMN `sort_no` int NOT NULL DEFAULT 0 COMMENT '模块展示排序号',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `admin_rbac_permission`
COMMENT = 'RBAC权限点表：定义后台接口与资源级权限点';
ALTER TABLE `admin_rbac_permission`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '后台权限点主键ID',
    MODIFY COLUMN `permission_code` varchar(128) NOT NULL COMMENT '权限点编码，后台内唯一',
    MODIFY COLUMN `permission_name` varchar(128) NOT NULL COMMENT '权限点名称',
    MODIFY COLUMN `module_code` varchar(64) NOT NULL COMMENT '所属模块编码',
    MODIFY COLUMN `resource_type` varchar(16) NOT NULL DEFAULT 'API' COMMENT '资源类型，如API/PAGE',
    MODIFY COLUMN `http_method` varchar(16) NULL COMMENT '接口HTTP方法',
    MODIFY COLUMN `path_pattern` varchar(255) NULL COMMENT '接口路径匹配表达式',
    MODIFY COLUMN `permission_desc` varchar(255) NULL COMMENT '权限点描述',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `admin_rbac_role`
COMMENT = 'RBAC角色表：定义岗位角色、范围与生效状态';
ALTER TABLE `admin_rbac_role`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '后台角色主键ID',
    MODIFY COLUMN `role_code` varchar(64) NOT NULL COMMENT '角色编码，后台内唯一',
    MODIFY COLUMN `role_name` varchar(64) NOT NULL COMMENT '角色名称',
    MODIFY COLUMN `role_scope` varchar(32) NOT NULL DEFAULT 'PLATFORM' COMMENT '角色适用范围，如PLATFORM',
    MODIFY COLUMN `role_status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '角色状态，ACTIVE/INACTIVE',
    MODIFY COLUMN `is_builtin` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否系统内置角色，1是0否',
    MODIFY COLUMN `role_desc` varchar(255) NULL COMMENT '角色描述',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `admin_rbac_role_menu`
COMMENT = '角色菜单授权表：配置角色可见菜单集合';
ALTER TABLE `admin_rbac_role_menu`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色菜单授权关系主键ID',
    MODIFY COLUMN `role_code` varchar(64) NOT NULL COMMENT '角色编码',
    MODIFY COLUMN `menu_code` varchar(64) NOT NULL COMMENT '菜单编码',
    MODIFY COLUMN `created_by` varchar(64) NOT NULL DEFAULT 'system' COMMENT '授权操作人',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `admin_rbac_role_permission`
COMMENT = '角色权限授权表：配置角色可执行的权限点集合';
ALTER TABLE `admin_rbac_role_permission`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色权限授权关系主键ID',
    MODIFY COLUMN `role_code` varchar(64) NOT NULL COMMENT '角色编码',
    MODIFY COLUMN `permission_code` varchar(128) NOT NULL COMMENT '权限点编码',
    MODIFY COLUMN `created_by` varchar(64) NOT NULL DEFAULT 'system' COMMENT '授权操作人',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `coupon_issue`
COMMENT = '红包发放实例表：记录红包发给哪个用户及其在支付中的预占与核销状态';
ALTER TABLE `coupon_issue`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '红包发放实例主键ID',
    MODIFY COLUMN `coupon_no` varchar(64) NOT NULL COMMENT '红包券号，业务侧唯一',
    MODIFY COLUMN `template_id` bigint NOT NULL COMMENT '关联红包模板ID',
    MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '红包归属用户ID',
    MODIFY COLUMN `coupon_amount` decimal(18,2) NOT NULL COMMENT '红包金额',
    MODIFY COLUMN `status` varchar(16) NOT NULL COMMENT '实例状态，如ISSUED/RESERVED/USED/EXPIRED',
    MODIFY COLUMN `claim_channel` varchar(64) NOT NULL COMMENT '领取渠道，如活动页或补偿发放',
    MODIFY COLUMN `business_no` varchar(64) NULL COMMENT '外部业务号，用于营销链路追踪',
    MODIFY COLUMN `order_no` varchar(64) NULL COMMENT '核销时绑定的订单号',
    MODIFY COLUMN `claimed_at` datetime NOT NULL COMMENT '领取时间',
    MODIFY COLUMN `expire_at` datetime NOT NULL COMMENT '过期时间',
    MODIFY COLUMN `used_at` datetime NULL COMMENT '核销时间',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `coupon_template`
COMMENT = '红包模板表：定义营销红包的发放规则、库存约束、领取与使用时间窗口';
ALTER TABLE `coupon_template`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '红包模板主键ID',
    MODIFY COLUMN `template_code` varchar(64) NOT NULL COMMENT '红包模板编码，运营侧唯一标识',
    MODIFY COLUMN `template_name` varchar(128) NOT NULL COMMENT '红包模板名称，用于后台展示',
    MODIFY COLUMN `scene_type` varchar(64) NOT NULL COMMENT '营销场景类型，如拉新、促活、补贴',
    MODIFY COLUMN `value_type` varchar(16) NOT NULL COMMENT '红包面额类型，固定或随机',
    MODIFY COLUMN `amount` decimal(18,2) NULL COMMENT '固定面额红包金额，value_type为FIXED时生效',
    MODIFY COLUMN `min_amount` decimal(18,2) NULL COMMENT '随机红包最小金额，value_type为RANDOM时生效',
    MODIFY COLUMN `max_amount` decimal(18,2) NULL COMMENT '随机红包最大金额，value_type为RANDOM时生效',
    MODIFY COLUMN `threshold_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛金额，订单达到该金额才可抵扣',
    MODIFY COLUMN `total_budget` decimal(18,2) NOT NULL COMMENT '红包活动总预算',
    MODIFY COLUMN `total_stock` int NOT NULL COMMENT '红包总库存数量',
    MODIFY COLUMN `claimed_count` int NOT NULL DEFAULT 0 COMMENT '已发放数量',
    MODIFY COLUMN `per_user_limit` int NOT NULL DEFAULT 1 COMMENT '单用户可领取上限',
    MODIFY COLUMN `claim_start_time` datetime NOT NULL COMMENT '领取开始时间',
    MODIFY COLUMN `claim_end_time` datetime NOT NULL COMMENT '领取结束时间',
    MODIFY COLUMN `use_start_time` datetime NOT NULL COMMENT '可使用开始时间',
    MODIFY COLUMN `use_end_time` datetime NOT NULL COMMENT '可使用结束时间',
    MODIFY COLUMN `funding_source` varchar(64) NOT NULL COMMENT '资金承担方，如平台或商户',
    MODIFY COLUMN `rule_payload` json NULL COMMENT '扩展规则JSON，例如黑白名单或渠道限制',
    MODIFY COLUMN `status` varchar(16) NOT NULL DEFAULT 'DRAFT' COMMENT '模板状态，如DRAFT/ACTIVE/PAUSED/EXPIRED',
    MODIFY COLUMN `created_by` varchar(64) NOT NULL COMMENT '创建操作人',
    MODIFY COLUMN `updated_by` varchar(64) NOT NULL COMMENT '最后更新操作人',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `credit_account`
COMMENT = '信用账户表：记录花呗式授信额度、本金利息罚息与账户状态';
ALTER TABLE `credit_account`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '信用账户主键ID',
    MODIFY COLUMN `account_no` varchar(32) NOT NULL COMMENT '信用账户号，系统内唯一',
    MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '信用账户所属用户ID',
    MODIFY COLUMN `total_limit` decimal(18,2) NOT NULL COMMENT '总授信额度',
    MODIFY COLUMN `principal_balance` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '当期应还本金余额',
    MODIFY COLUMN `principal_unreach_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '未到期本金余额',
    MODIFY COLUMN `overdue_principal_balance` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '逾期本金余额',
    MODIFY COLUMN `overdue_principal_unreach_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '逾期未核销本金余额',
    MODIFY COLUMN `interest_balance` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '应还利息余额',
    MODIFY COLUMN `fine_balance` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '应还罚息余额',
    MODIFY COLUMN `account_status` varchar(16) NOT NULL DEFAULT 'NORMAL' COMMENT '信用账户状态，NORMAL/FROZEN/CLOSED',
    MODIFY COLUMN `pay_status` varchar(16) NOT NULL DEFAULT 'NORMAL' COMMENT '还款状态，NORMAL/OVERDUE/SETTLED',
    MODIFY COLUMN `lock_version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，用于并发更新控制',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `credit_tcc_transaction`
COMMENT = '信用账户TCC分支表：记录额度占用、确认、回滚分支执行状态';
ALTER TABLE `credit_tcc_transaction`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '信用账户TCC分支事务主键ID',
    MODIFY COLUMN `xid` varchar(128) NOT NULL COMMENT '信用账户分支所属全局事务XID',
    MODIFY COLUMN `branch_id` varchar(64) NOT NULL COMMENT '信用账户分支事务ID',
    MODIFY COLUMN `account_no` varchar(32) NOT NULL COMMENT '关联信用账户号',
    MODIFY COLUMN `operation_type` varchar(16) NOT NULL COMMENT '信用额度TCC操作类型',
    MODIFY COLUMN `asset_category` varchar(32) NOT NULL COMMENT '额度资产分类，如PRINCIPAL/INTEREST/FINE',
    MODIFY COLUMN `branch_status` varchar(16) NOT NULL COMMENT '信用分支执行状态',
    MODIFY COLUMN `amount` decimal(18,2) NOT NULL COMMENT '本次额度占用或释放金额',
    MODIFY COLUMN `business_no` varchar(64) NULL COMMENT '关联业务单号（如paymentId）',
    MODIFY COLUMN `lock_version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，用于并发更新控制',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `fund_account`
COMMENT = '基金账户表：记录用户持仓份额、冻结份额与收益信息';
ALTER TABLE `fund_account`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '基金账户主键ID',
    MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '基金账户所属用户ID',
    MODIFY COLUMN `fund_code` varchar(32) NOT NULL COMMENT '基金产品编码',
    MODIFY COLUMN `currency_code` varchar(8) NOT NULL COMMENT '计价币种编码',
    MODIFY COLUMN `available_share` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '可用份额',
    MODIFY COLUMN `frozen_share` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '冻结份额',
    MODIFY COLUMN `pending_subscribe_amount` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '待确认申购金额',
    MODIFY COLUMN `pending_redeem_share` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '待确认赎回份额',
    MODIFY COLUMN `accumulated_income` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '累计收益',
    MODIFY COLUMN `yesterday_income` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '昨日收益',
    MODIFY COLUMN `latest_nav` decimal(18,4) NOT NULL DEFAULT 1.0000 COMMENT '最新净值',
    MODIFY COLUMN `account_status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '基金账户状态，ACTIVE/FROZEN/CLOSED',
    MODIFY COLUMN `lock_version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，用于并发更新控制',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `fund_fast_redeem_quota`
COMMENT = '基金快赎总额度表：记录基金维度当日快速赎回额度使用情况';
ALTER TABLE `fund_fast_redeem_quota`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '基金快赎总额度主键ID',
    MODIFY COLUMN `fund_code` varchar(32) NOT NULL COMMENT '基金产品编码',
    MODIFY COLUMN `quota_date` date NOT NULL COMMENT '额度生效日期',
    MODIFY COLUMN `quota_limit` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '当日快赎总额度上限',
    MODIFY COLUMN `quota_used` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '当日已使用快赎额度',
    MODIFY COLUMN `lock_version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，用于并发更新控制',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `fund_income_calendar`
COMMENT = '基金收益日历表：维护每日净值、万份收益及发布结算状态';
ALTER TABLE `fund_income_calendar`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '基金收益日历主键ID',
    MODIFY COLUMN `fund_code` varchar(32) NOT NULL COMMENT '基金产品编码',
    MODIFY COLUMN `biz_date` date NOT NULL COMMENT '收益所属业务日期',
    MODIFY COLUMN `nav` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '当日净值',
    MODIFY COLUMN `income_per_10k` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '万份收益',
    MODIFY COLUMN `calendar_status` varchar(16) NOT NULL DEFAULT 'PLANNED' COMMENT '收益日历状态，PLANNED/PUBLISHED/SETTLED',
    MODIFY COLUMN `published_at` datetime NULL COMMENT '收益公布时间',
    MODIFY COLUMN `settled_at` datetime NULL COMMENT '收益结算完成时间',
    MODIFY COLUMN `lock_version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，用于并发更新控制',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `fund_product`
COMMENT = '基金产品参数表：维护申赎限额、快赎额度与开关';
ALTER TABLE `fund_product`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '基金产品主键ID',
    MODIFY COLUMN `fund_code` varchar(32) NOT NULL COMMENT '基金产品编码，系统内唯一',
    MODIFY COLUMN `product_name` varchar(64) NOT NULL COMMENT '基金产品名称',
    MODIFY COLUMN `currency_code` varchar(8) NOT NULL COMMENT '计价币种编码',
    MODIFY COLUMN `product_status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '产品状态，ACTIVE/PAUSED/CLOSED',
    MODIFY COLUMN `single_subscribe_min_amount` decimal(18,4) NOT NULL DEFAULT 0.0100 COMMENT '单笔申购最小金额',
    MODIFY COLUMN `single_subscribe_max_amount` decimal(18,4) NOT NULL DEFAULT 1000000.0000 COMMENT '单笔申购最大金额',
    MODIFY COLUMN `daily_subscribe_max_amount` decimal(18,4) NOT NULL DEFAULT 5000000.0000 COMMENT '单日申购上限金额',
    MODIFY COLUMN `single_redeem_min_share` decimal(18,4) NOT NULL DEFAULT 0.0100 COMMENT '单笔赎回最小份额',
    MODIFY COLUMN `single_redeem_max_share` decimal(18,4) NOT NULL DEFAULT 1000000.0000 COMMENT '单笔赎回最大份额',
    MODIFY COLUMN `daily_redeem_max_share` decimal(18,4) NOT NULL DEFAULT 5000000.0000 COMMENT '单日赎回上限份额',
    MODIFY COLUMN `fast_redeem_daily_quota` decimal(18,4) NOT NULL DEFAULT 10000000.0000 COMMENT '产品维度单日快赎总额度',
    MODIFY COLUMN `fast_redeem_per_user_daily_quota` decimal(18,4) NOT NULL DEFAULT 100000.0000 COMMENT '单用户单日快赎额度',
    MODIFY COLUMN `switch_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否开启基金转换功能，1开启0关闭',
    MODIFY COLUMN `lock_version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，用于并发更新控制',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `fund_transaction`
COMMENT = '基金交易流水表：记录申购、赎回、转换请求与确认结果';
ALTER TABLE `fund_transaction`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '基金交易流水主键ID',
    MODIFY COLUMN `order_no` varchar(64) NOT NULL COMMENT '基金交易订单号，系统内唯一',
    MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '交易所属用户ID',
    MODIFY COLUMN `fund_code` varchar(32) NOT NULL COMMENT '基金产品编码',
    MODIFY COLUMN `transaction_type` varchar(32) NOT NULL COMMENT '交易类型，如SUBSCRIBE/REDEEM/SWITCH',
    MODIFY COLUMN `transaction_status` varchar(16) NOT NULL COMMENT '交易状态，PENDING/CONFIRMED/CANCELED',
    MODIFY COLUMN `request_amount` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '请求金额（申购场景）',
    MODIFY COLUMN `request_share` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '请求份额（赎回场景）',
    MODIFY COLUMN `confirmed_amount` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '确认金额',
    MODIFY COLUMN `confirmed_share` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '确认份额',
    MODIFY COLUMN `business_no` varchar(64) NULL COMMENT '关联外部业务单号',
    MODIFY COLUMN `ext_info` varchar(256) NULL COMMENT '扩展信息JSON或备注',
    MODIFY COLUMN `lock_version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，用于并发更新控制',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `fund_user_fast_redeem_quota`
COMMENT = '用户快赎额度表：记录用户在基金维度当日快赎额度使用情况';
ALTER TABLE `fund_user_fast_redeem_quota`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户基金快赎额度主键ID',
    MODIFY COLUMN `fund_code` varchar(32) NOT NULL COMMENT '基金产品编码',
    MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '用户业务ID',
    MODIFY COLUMN `quota_date` date NOT NULL COMMENT '额度生效日期',
    MODIFY COLUMN `quota_limit` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '用户当日快赎额度上限',
    MODIFY COLUMN `quota_used` decimal(18,4) NOT NULL DEFAULT 0.0000 COMMENT '用户当日已使用快赎额度',
    MODIFY COLUMN `lock_version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，用于并发更新控制',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `pay_order`
COMMENT = '支付主单表：记录一次支付编排的金额拆分、状态流转和失败原因';
ALTER TABLE `pay_order`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付主单主键ID',
    MODIFY COLUMN `payment_id` varchar(64) NOT NULL COMMENT '支付流水号，系统内唯一',
    MODIFY COLUMN `merchant_order_no` varchar(64) NOT NULL COMMENT '商户订单号，幂等键',
    MODIFY COLUMN `business_scene_code` varchar(64) NOT NULL COMMENT '业务场景编码，如转账或收银台付款',
    MODIFY COLUMN `payer_user_id` bigint NOT NULL COMMENT '付款用户ID',
    MODIFY COLUMN `payee_user_id` bigint NULL COMMENT '收款用户ID，可为空',
    MODIFY COLUMN `currency_code` varchar(8) NOT NULL COMMENT '币种编码，如CNY',
    MODIFY COLUMN `original_amount` decimal(18,2) NOT NULL COMMENT '订单原始金额',
    MODIFY COLUMN `discount_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额（红包抵扣等）',
    MODIFY COLUMN `payable_amount` decimal(18,2) NOT NULL COMMENT '应付金额',
    MODIFY COLUMN `actual_paid_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额，提交成功后回填',
    MODIFY COLUMN `wallet_debit_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '钱包账户扣款金额',
    MODIFY COLUMN `fund_debit_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '余额宝/基金账户扣款金额',
    MODIFY COLUMN `credit_debit_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '信用账户扣款金额',
    MODIFY COLUMN `coupon_no` varchar(64) NULL COMMENT '使用的红包券号',
    MODIFY COLUMN `seata_xid` varchar(128) NOT NULL COMMENT '全局事务XID',
    MODIFY COLUMN `status` varchar(32) NOT NULL COMMENT '支付状态，如TRYING/PREPARED/COMMITTED/ROLLED_BACK',
    MODIFY COLUMN `failure_reason` varchar(255) NULL COMMENT '失败或回滚原因',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `pay_participant_branch`
COMMENT = '支付参与方分支表：记录钱包/基金/信用/红包各参与方分支执行结果';
ALTER TABLE `pay_participant_branch`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付参与方分支主键ID',
    MODIFY COLUMN `payment_id` varchar(64) NOT NULL COMMENT '关联支付流水号',
    MODIFY COLUMN `participant_type` varchar(32) NOT NULL COMMENT '参与方类型，如WALLET/FUND/CREDIT/COUPON',
    MODIFY COLUMN `branch_id` varchar(64) NOT NULL COMMENT '分支事务ID',
    MODIFY COLUMN `participant_resource_id` varchar(128) NOT NULL COMMENT '参与方资源标识，如accountNo或couponNo',
    MODIFY COLUMN `request_payload` text NULL COMMENT '分支请求快照',
    MODIFY COLUMN `status` varchar(32) NOT NULL COMMENT '分支状态，如TRY_OK/CONFIRM_OK/CANCEL_OK',
    MODIFY COLUMN `response_message` varchar(255) NULL COMMENT '分支执行返回信息',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `pricing_quote`
COMMENT = '计费快照表：记录一次交易请求命中的规则与计算结果，作为后续支付核对依据';
ALTER TABLE `pricing_quote`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '计费报价快照主键ID',
    MODIFY COLUMN `quote_no` varchar(64) NOT NULL COMMENT '报价单号，系统内唯一',
    MODIFY COLUMN `request_no` varchar(64) NOT NULL COMMENT '交易侧请求号，幂等键',
    MODIFY COLUMN `rule_id` bigint NOT NULL COMMENT '命中的计费规则ID',
    MODIFY COLUMN `rule_code` varchar(64) NOT NULL COMMENT '命中的计费规则编码',
    MODIFY COLUMN `rule_name` varchar(128) NOT NULL COMMENT '命中的计费规则名称',
    MODIFY COLUMN `business_scene_code` varchar(64) NOT NULL COMMENT '报价时使用的业务场景编码',
    MODIFY COLUMN `payment_method` varchar(64) NOT NULL COMMENT '报价时使用的支付方式编码',
    MODIFY COLUMN `currency_code` varchar(64) NOT NULL COMMENT '报价时使用的币种编码',
    MODIFY COLUMN `original_amount` decimal(18,2) NOT NULL COMMENT '原始交易金额',
    MODIFY COLUMN `fee_amount` decimal(18,2) NOT NULL COMMENT '计算出的手续费金额',
    MODIFY COLUMN `payable_amount` decimal(18,2) NOT NULL COMMENT '付款方应付金额',
    MODIFY COLUMN `settle_amount` decimal(18,2) NOT NULL COMMENT '收款方结算金额',
    MODIFY COLUMN `fee_mode` varchar(32) NOT NULL COMMENT '本次报价采用的计费模式快照',
    MODIFY COLUMN `fee_bearer` varchar(32) NOT NULL COMMENT '本次报价采用的承担方快照',
    MODIFY COLUMN `fee_rate` decimal(10,6) NOT NULL DEFAULT 0.000000 COMMENT '本次报价采用的费率快照',
    MODIFY COLUMN `fixed_fee` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '本次报价采用的固定费快照',
    MODIFY COLUMN `rule_payload` json NULL COMMENT '规则扩展参数快照',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `pricing_rule`
COMMENT = '计费规则表：定义场景维度下的费率、固定费、封顶保底和承担方策略';
ALTER TABLE `pricing_rule`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '计费规则主键ID',
    MODIFY COLUMN `rule_code` varchar(64) NOT NULL COMMENT '计费规则编码，运营侧唯一',
    MODIFY COLUMN `rule_name` varchar(128) NOT NULL COMMENT '计费规则名称',
    MODIFY COLUMN `business_scene_code` varchar(64) NOT NULL COMMENT '业务场景编码，支持ALL通配',
    MODIFY COLUMN `payment_method` varchar(64) NOT NULL COMMENT '支付方式编码，支持ALL通配',
    MODIFY COLUMN `currency_code` varchar(64) NOT NULL COMMENT '币种编码，支持ALL通配',
    MODIFY COLUMN `fee_mode` varchar(32) NOT NULL COMMENT '计费模式：RATE/FIXED/RATE_PLUS_FIXED',
    MODIFY COLUMN `fee_rate` decimal(10,6) NOT NULL DEFAULT 0.000000 COMMENT '费率值，如0.006表示千分之六',
    MODIFY COLUMN `fixed_fee` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '固定手续费',
    MODIFY COLUMN `min_fee` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '手续费保底值',
    MODIFY COLUMN `max_fee` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '手续费封顶值，0表示不封顶',
    MODIFY COLUMN `fee_bearer` varchar(32) NOT NULL COMMENT '手续费承担方：PAYER/PAYEE/PLATFORM',
    MODIFY COLUMN `priority` int NOT NULL DEFAULT 100 COMMENT '规则优先级，数值越大优先',
    MODIFY COLUMN `status` varchar(16) NOT NULL DEFAULT 'DRAFT' COMMENT '规则状态：DRAFT/ACTIVE/INACTIVE',
    MODIFY COLUMN `valid_from` datetime NULL COMMENT '生效开始时间，空表示立即可用',
    MODIFY COLUMN `valid_to` datetime NULL COMMENT '生效结束时间，空表示长期有效',
    MODIFY COLUMN `rule_payload` json NULL COMMENT '扩展规则JSON，如特殊商户白名单',
    MODIFY COLUMN `created_by` varchar(64) NOT NULL COMMENT '创建操作人',
    MODIFY COLUMN `updated_by` varchar(64) NOT NULL COMMENT '最后更新操作人',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `user_account`
COMMENT = '用户账户表：记录登录标识、KYC等级、密码状态与账户状态';
ALTER TABLE `user_account`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户账户主键ID',
    MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '用户业务ID',
    MODIFY COLUMN `aipay_uid` varchar(64) NOT NULL COMMENT '平台用户唯一UID',
    MODIFY COLUMN `login_id` varchar(128) NOT NULL COMMENT '登录账号（手机号或邮箱）',
    MODIFY COLUMN `account_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户账户状态，ACTIVE/FROZEN/CLOSED',
    MODIFY COLUMN `kyc_level` varchar(16) NOT NULL DEFAULT 'L1' COMMENT '实名认证等级，如L1/L2/L3',
    MODIFY COLUMN `login_password_set` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否已设置登录密码，1是0否',
    MODIFY COLUMN `pay_password_set` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已设置支付密码，1是0否',
    MODIFY COLUMN `login_password_sha256` char(64) NULL COMMENT '登录密码摘要（SHA-256）',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `user_privacy_setting`
COMMENT = '用户隐私设置表：记录搜索可见性与个性化推荐配置';
ALTER TABLE `user_privacy_setting`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户隐私设置主键ID',
    MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '用户业务ID',
    MODIFY COLUMN `allow_search_by_mobile` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许通过手机号搜索，1允许0禁止',
    MODIFY COLUMN `allow_search_by_aipay_uid` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许通过AIPAY_UID搜索，1允许0禁止',
    MODIFY COLUMN `hide_real_name` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否隐藏实名信息，1隐藏0展示',
    MODIFY COLUMN `personalized_recommendation_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否开启个性化推荐，1开启0关闭',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `user_profile`
COMMENT = '用户资料表：记录昵称、头像、地区与脱敏实名信息';
ALTER TABLE `user_profile`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户资料主键ID',
    MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '用户业务ID',
    MODIFY COLUMN `nickname` varchar(64) NOT NULL COMMENT '用户昵称',
    MODIFY COLUMN `avatar_url` varchar(512) NULL COMMENT '头像地址',
    MODIFY COLUMN `country_code` varchar(8) NULL COMMENT '国家或地区编码',
    MODIFY COLUMN `mobile` varchar(32) NULL COMMENT '手机号（可脱敏展示）',
    MODIFY COLUMN `masked_real_name` varchar(64) NULL COMMENT '脱敏后的真实姓名',
    MODIFY COLUMN `gender` varchar(16) NULL COMMENT '性别标识',
    MODIFY COLUMN `region` varchar(64) NULL COMMENT '常驻地区',
    MODIFY COLUMN `birthday` date NULL COMMENT '生日',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `user_security_setting`
COMMENT = '用户安全设置表：记录生物识别、双因子与风控等级配置';
ALTER TABLE `user_security_setting`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户安全设置主键ID',
    MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '用户业务ID',
    MODIFY COLUMN `biometric_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否开启生物识别登录，1开启0关闭',
    MODIFY COLUMN `two_factor_mode` varchar(32) NOT NULL DEFAULT 'NONE' COMMENT '双因子认证模式，如NONE/SMS/TOTP',
    MODIFY COLUMN `risk_level` varchar(32) NOT NULL DEFAULT 'LOW' COMMENT '当前风控等级，如LOW/MEDIUM/HIGH',
    MODIFY COLUMN `device_lock_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用设备锁，1启用0关闭',
    MODIFY COLUMN `privacy_mode_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用隐私模式，1启用0关闭',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `wallet_account`
COMMENT = '钱包账户表：记录可用余额、预留余额与账户状态';
ALTER TABLE `wallet_account`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '钱包账户主键ID',
    MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '用户业务ID',
    MODIFY COLUMN `currency_code` varchar(8) NOT NULL COMMENT '钱包币种编码',
    MODIFY COLUMN `available_balance` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '可用余额，可直接用于支付',
    MODIFY COLUMN `reserved_balance` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '预留余额，已冻结待确认',
    MODIFY COLUMN `account_status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '钱包账户状态，ACTIVE/FROZEN/CLOSED',
    MODIFY COLUMN `lock_version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，用于并发更新控制',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `wallet_tcc_transaction`
COMMENT = '钱包TCC分支表：记录余额冻结、扣减、解冻分支执行状态';
ALTER TABLE `wallet_tcc_transaction`
    MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '钱包TCC分支事务主键ID',
    MODIFY COLUMN `xid` varchar(128) NOT NULL COMMENT '全局事务XID',
    MODIFY COLUMN `branch_id` varchar(64) NOT NULL COMMENT '分支事务ID',
    MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '钱包所属用户ID',
    MODIFY COLUMN `operation_type` varchar(16) NOT NULL COMMENT '钱包TCC操作类型，TRY/CONFIRM/CANCEL',
    MODIFY COLUMN `branch_status` varchar(16) NOT NULL COMMENT '钱包分支状态，INIT/TRY_OK/CONFIRM_OK/CANCEL_OK',
    MODIFY COLUMN `amount` decimal(18,2) NOT NULL COMMENT '本次冻结或扣减金额',
    MODIFY COLUMN `business_no` varchar(64) NULL COMMENT '关联业务单号（如paymentId）',
    MODIFY COLUMN `lock_version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，用于并发更新控制',
    MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- [V17__create_trade_tables_and_permissions.sql]
CREATE TABLE IF NOT EXISTS trade_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交易主单主键ID',
    trade_no VARCHAR(64) NOT NULL COMMENT '交易流水号，系统内唯一',
    request_no VARCHAR(64) NOT NULL COMMENT '请求幂等号，BFF侧唯一',
    trade_type VARCHAR(32) NOT NULL COMMENT '交易类型：DEPOSIT/WITHDRAW/PAY/TRANSFER/REFUND',
    business_scene_code VARCHAR(64) NOT NULL COMMENT '业务场景编码',
    original_trade_no VARCHAR(64) NULL COMMENT '原交易号，退款场景使用',
    payer_user_id BIGINT NOT NULL COMMENT '付款用户ID',
    payee_user_id BIGINT NULL COMMENT '收款用户ID',
    payment_method VARCHAR(64) NOT NULL COMMENT '支付方式编码',
    currency_code VARCHAR(8) NOT NULL COMMENT '币种编码',
    original_amount DECIMAL(18,2) NOT NULL COMMENT '原始交易金额',
    fee_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '计费手续费金额',
    payable_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '应付金额',
    settle_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '结算金额',
    wallet_debit_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '钱包扣款金额',
    fund_debit_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '基金扣款金额',
    credit_debit_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '信用扣款金额',
    pricing_quote_no VARCHAR(64) NULL COMMENT '计费快照号',
    pay_payment_id VARCHAR(64) NULL COMMENT '支付模块支付单号',
    status VARCHAR(32) NOT NULL COMMENT '交易状态',
    failure_reason VARCHAR(255) NULL COMMENT '失败原因',
    metadata TEXT NULL COMMENT '扩展信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_trade_order_trade_no (trade_no),
    UNIQUE KEY uk_trade_order_request_no (request_no),
    KEY idx_trade_order_original_status (original_trade_no, status),
    KEY idx_trade_order_payer_status (payer_user_id, status),
    KEY idx_trade_order_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易主单表：BFF交易入口在计费与支付编排后形成的业务主单';
CREATE TABLE IF NOT EXISTS trade_flow_step (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流程步骤主键ID',
    trade_no VARCHAR(64) NOT NULL COMMENT '关联交易号',
    step_code VARCHAR(32) NOT NULL COMMENT '步骤编码：PRICING_QUOTE/PAY_PREPARE/PAY_COMMIT/PAY_ROLLBACK',
    step_status VARCHAR(16) NOT NULL COMMENT '步骤状态：RUNNING/SUCCESS/FAILED/SKIPPED',
    request_payload TEXT NULL COMMENT '步骤请求快照',
    response_payload TEXT NULL COMMENT '步骤响应快照',
    error_message VARCHAR(255) NULL COMMENT '错误信息',
    started_at DATETIME NOT NULL COMMENT '步骤开始时间',
    finished_at DATETIME NULL COMMENT '步骤结束时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_trade_flow_trade_no (trade_no),
    KEY idx_trade_flow_trade_step (trade_no, step_code),
    KEY idx_trade_flow_status (step_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易流程步骤表：记录计费、支付预提交、提交与补偿回滚的执行轨迹';

-- [V22__create_inbound_tables_and_enhance_trade_pay.sql]
ALTER TABLE pay_order
    ADD COLUMN influx_debit_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '入金扣款金额' AFTER credit_debit_amount;
ALTER TABLE trade_order
    ADD COLUMN influx_debit_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '入金扣款金额' AFTER credit_debit_amount;
CREATE TABLE IF NOT EXISTS inbound_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    influx_id VARCHAR(64) NOT NULL COMMENT '入金订单号',
    org_influx_id VARCHAR(64) NULL COMMENT '原入金订单号',
    inst_id VARCHAR(64) NULL COMMENT '机构标识',
    business_code VARCHAR(64) NOT NULL COMMENT '业务码',
    sub_business_code VARCHAR(64) NOT NULL COMMENT '子业务码',
    exchange_type VARCHAR(32) NOT NULL COMMENT '交换类型',
    finance_exchange_code VARCHAR(64) NULL COMMENT '金融交换码',
    settle_serial_no VARCHAR(64) NULL COMMENT '清算流水号',
    payer_account_no VARCHAR(64) NOT NULL COMMENT '付款账户号',
    exchange_amount DECIMAL(18,2) NOT NULL COMMENT '交易金额',
    exchange_currency VARCHAR(8) NOT NULL COMMENT '交易币种',
    account_amount DECIMAL(18,2) NOT NULL COMMENT '记账金额',
    account_currency VARCHAR(8) NOT NULL COMMENT '记账币种',
    settle_amount DECIMAL(18,2) NOT NULL COMMENT '清算金额',
    settle_currency VARCHAR(8) NOT NULL COMMENT '清算币种',
    settle_status VARCHAR(32) NOT NULL COMMENT '清算状态',
    exchange_status VARCHAR(32) NOT NULL COMMENT '交换状态',
    result_code VARCHAR(64) NULL COMMENT '结果码',
    result_description VARCHAR(255) NULL COMMENT '结果描述',
    recover_flag CHAR(1) NOT NULL DEFAULT 'N' COMMENT '补偿标识',
    recon_flag CHAR(1) NOT NULL DEFAULT 'N' COMMENT '对账标识',
    negative_flag CHAR(1) NOT NULL DEFAULT 'N' COMMENT '冲正标识',
    negative_exchange_type VARCHAR(32) NULL COMMENT '冲正交换类型',
    request_identify VARCHAR(64) NOT NULL COMMENT '请求标识',
    request_biz_no VARCHAR(64) NOT NULL COMMENT '业务请求号',
    pay_unique_no VARCHAR(64) NOT NULL COMMENT '支付唯一号',
    pay_channel_api VARCHAR(64) NOT NULL COMMENT '支付渠道接口',
    inst_channel_api VARCHAR(64) NOT NULL COMMENT '机构渠道接口',
    clear_channel VARCHAR(32) NOT NULL COMMENT '清算渠道',
    biz_identity VARCHAR(64) NOT NULL COMMENT '业务身份',
    gmt_submit DATETIME NULL COMMENT '提交时间',
    gmt_resp DATETIME NULL COMMENT '响应时间',
    gmt_settle DATETIME NULL COMMENT '清算时间',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_fin_influx_influx_id (influx_id),
    UNIQUE KEY uk_fin_influx_request_biz_no (request_biz_no),
    UNIQUE KEY uk_fin_influx_pay_unique_no (pay_unique_no),
    KEY idx_fin_influx_exchange_status (exchange_status),
    KEY idx_fin_influx_settle_status (settle_status),
    KEY idx_fin_influx_inst_channel_api (inst_channel_api),
    KEY idx_fin_influx_submit_time (gmt_submit)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入金订单主表';

-- [V23__rename_inbound_tables_and_add_inst_payer.sql]
CREATE TABLE IF NOT EXISTS influx_payer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    influx_id VARCHAR(64) NOT NULL COMMENT '入金订单号',
    payer_account_no VARCHAR(64) NOT NULL COMMENT '付款账户号',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_influx_payer_influx_id (influx_id),
    KEY idx_influx_payer_account_no (payer_account_no),
    CONSTRAINT fk_influx_payer_influx_id FOREIGN KEY (influx_id) REFERENCES inbound_order (influx_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入金付款方信息表';
CREATE TABLE IF NOT EXISTS influx_inst (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    influx_id VARCHAR(64) NOT NULL COMMENT '入金订单号',
    inst_id VARCHAR(64) NULL COMMENT '机构标识',
    inst_channel_api VARCHAR(64) NOT NULL COMMENT '机构渠道接口',
    finance_exchange_code VARCHAR(64) NULL COMMENT '金融交换码',
    settle_serial_no VARCHAR(64) NULL COMMENT '清算流水号',
    result_code VARCHAR(64) NULL COMMENT '结果码',
    result_description VARCHAR(255) NULL COMMENT '结果描述',
    exchange_status VARCHAR(32) NOT NULL COMMENT '交换状态',
    gmt_submit DATETIME NULL COMMENT '提交时间',
    gmt_resp DATETIME NULL COMMENT '响应时间',
    gmt_settle DATETIME NULL COMMENT '清算时间',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_influx_inst_influx_id (influx_id),
    KEY idx_influx_inst_channel_api (inst_channel_api),
    KEY idx_influx_inst_finance_exchange_code (finance_exchange_code),
    CONSTRAINT fk_influx_inst_influx_id FOREIGN KEY (influx_id) REFERENCES inbound_order (influx_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入金机构信息表';

-- [V24__create_bank_card_table_and_seed_data.sql]
CREATE TABLE IF NOT EXISTS bank_card (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '银行卡主键ID',
    card_no VARCHAR(32) NOT NULL COMMENT '银行卡号，系统内唯一',
    user_id BIGINT NOT NULL COMMENT '绑卡所属用户ID',
    bank_code VARCHAR(16) NOT NULL COMMENT '发卡行编码',
    bank_name VARCHAR(64) NOT NULL COMMENT '发卡行名称',
    card_type VARCHAR(16) NOT NULL COMMENT '银行卡类型，DEBIT/CREDIT',
    card_holder_name VARCHAR(64) NOT NULL COMMENT '持卡人姓名',
    reserved_mobile VARCHAR(32) NULL COMMENT '银行预留手机号',
    phone_tail_no VARCHAR(4) NULL COMMENT '预留手机号后四位',
    card_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '银行卡状态，ACTIVE/INACTIVE/UNBOUND',
    is_default TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认卡，1是0否',
    single_limit DECIMAL(18,2) NOT NULL DEFAULT 50000.00 COMMENT '单笔支付限额',
    daily_limit DECIMAL(18,2) NOT NULL DEFAULT 200000.00 COMMENT '单日支付限额',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_bank_card_card_no (card_no),
    KEY idx_bank_card_user_status (user_id, card_status),
    KEY idx_bank_card_user_default (user_id, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户银行卡表：维护用户在收银台可选银行卡及限额配置';

-- [V25__add_user_profile_id_card_no.sql]
SET @id_card_no_column_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_profile'
      AND COLUMN_NAME = 'id_card_no'
);
SET @ddl = IF(
    @id_card_no_column_exists = 0,
    'ALTER TABLE user_profile ADD COLUMN id_card_no VARCHAR(18) NULL COMMENT ''身份证号（仅用于实名测试）'' AFTER birthday',
    'SET @noop_user_profile_id_card_no = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @id_card_no_index_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_profile'
      AND INDEX_NAME = 'uk_user_profile_id_card_no'
);
SET @ddl = IF(
    @id_card_no_index_exists = 0,
    'ALTER TABLE user_profile ADD UNIQUE KEY uk_user_profile_id_card_no (id_card_no)',
    'SET @noop_user_profile_id_card_no_index = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V26__remove_portal_table_prefix.sql]
SET SESSION group_concat_max_len = 1024000;
SET @rename_sql = IF(
    @rename_pairs IS NULL,
    'SET @noop_remove_portal_prefix = 1',
    CONCAT('RENAME TABLE ', @rename_pairs)
);
PREPARE stmt FROM @rename_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V28__align_inbound_flag_column_types.sql]
ALTER TABLE inbound_order
    MODIFY COLUMN recover_flag VARCHAR(1) NOT NULL DEFAULT 'N' COMMENT '补偿标识',
    MODIFY COLUMN recon_flag VARCHAR(1) NOT NULL DEFAULT 'N' COMMENT '对账标识',
    MODIFY COLUMN negative_flag VARCHAR(1) NOT NULL DEFAULT 'N' COMMENT '冲正标识';

-- [V29__create_pay_fund_detail_tables.sql]
CREATE TABLE IF NOT EXISTS pay_fund_detail_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资金明细摘要主键ID',
    payment_id VARCHAR(64) NOT NULL COMMENT '支付单号',
    pay_tool VARCHAR(32) NOT NULL COMMENT '支付工具',
    detail_owner VARCHAR(16) NOT NULL COMMENT '明细归属方',
    amount DECIMAL(18,2) NOT NULL COMMENT '金额',
    currency_code VARCHAR(8) NOT NULL COMMENT '币种编码',
    cumulative_refund_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '累计退款金额',
    detail_type VARCHAR(32) NOT NULL COMMENT '明细类型',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_pay_fund_detail_payment_tool_owner (payment_id, pay_tool, detail_owner),
    KEY idx_pay_fund_detail_payment_id (payment_id),
    KEY idx_pay_fund_detail_owner (detail_owner)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付资金明细摘要表';
CREATE TABLE IF NOT EXISTS pay_bank_card_fund_detail (
    summary_id BIGINT PRIMARY KEY COMMENT '资金明细摘要ID',
    channel VARCHAR(64) NULL COMMENT '渠道',
    bank_order_no VARCHAR(64) NULL COMMENT '银行订单号',
    bank_card_no VARCHAR(64) NULL COMMENT '银行卡号',
    channel_fee_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '渠道收费金额',
    deposit_order_no VARCHAR(64) NULL COMMENT '充值订单号',
    KEY idx_pay_bank_card_bank_order_no (bank_order_no),
    KEY idx_pay_bank_card_deposit_order_no (deposit_order_no),
    CONSTRAINT fk_pay_bank_card_fund_detail_summary
        FOREIGN KEY (summary_id) REFERENCES pay_fund_detail_summary (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银行卡资金明细表';
CREATE TABLE IF NOT EXISTS pay_red_packet_fund_detail (
    summary_id BIGINT PRIMARY KEY COMMENT '资金明细摘要ID',
    red_packet_id VARCHAR(64) NOT NULL COMMENT '红包ID',
    KEY idx_pay_red_packet_id (red_packet_id),
    CONSTRAINT fk_pay_red_packet_fund_detail_summary
        FOREIGN KEY (summary_id) REFERENCES pay_fund_detail_summary (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='红包资金明细表';
CREATE TABLE IF NOT EXISTS pay_wallet_fund_detail (
    summary_id BIGINT PRIMARY KEY COMMENT '资金明细摘要ID',
    account_no VARCHAR(64) NOT NULL COMMENT '账户号',
    KEY idx_pay_wallet_account_no (account_no),
    CONSTRAINT fk_pay_wallet_fund_detail_summary
        FOREIGN KEY (summary_id) REFERENCES pay_fund_detail_summary (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包资金明细表';

-- [V30__drop_currency_code_from_pay_fund_detail_summary.sql]
SET @currency_code_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pay_fund_detail_summary'
      AND COLUMN_NAME = 'currency_code'
);
SET @ddl = IF(
    @currency_code_exists = 1,
    'ALTER TABLE pay_fund_detail_summary DROP COLUMN currency_code',
    'SET @noop_drop_pay_fund_detail_currency_code = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V32__drop_legacy_tables.sql]
SET SESSION group_concat_max_len = 1024000;
SET @legacy_table_list = (
    SELECT GROUP_CONCAT(CONCAT('`', t.TABLE_NAME, '`') ORDER BY t.TABLE_NAME SEPARATOR ', ')
    FROM information_schema.TABLES t
    WHERE t.TABLE_SCHEMA = DATABASE()
      AND t.TABLE_TYPE = 'BASE TABLE'
      AND t.TABLE_NAME LIKE 'legacy\\_%'
);
SET @drop_legacy_sql = IF(
    @legacy_table_list IS NULL,
    'SET @noop_drop_legacy_tables = 1',
    CONCAT('DROP TABLE IF EXISTS ', @legacy_table_list)
);
PREPARE stmt FROM @drop_legacy_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V33__drop_currency_code_from_pay_trade_order.sql]
SET @pay_currency_exists = (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pay_order'
      AND COLUMN_NAME = 'currency_code'
);
SET @drop_pay_currency_sql = IF(
    @pay_currency_exists = 1,
    'ALTER TABLE pay_order DROP COLUMN currency_code',
    'SET @noop_drop_pay_order_currency_code = 1'
);
PREPARE stmt_drop_pay_currency FROM @drop_pay_currency_sql;
EXECUTE stmt_drop_pay_currency;
DEALLOCATE PREPARE stmt_drop_pay_currency;
SET @trade_currency_exists = (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'trade_order'
      AND COLUMN_NAME = 'currency_code'
);
SET @drop_trade_currency_sql = IF(
    @trade_currency_exists = 1,
    'ALTER TABLE trade_order DROP COLUMN currency_code',
    'SET @noop_drop_trade_order_currency_code = 1'
);
PREPARE stmt_drop_trade_currency FROM @drop_trade_currency_sql;
EXECUTE stmt_drop_trade_currency;
DEALLOCATE PREPARE stmt_drop_trade_currency;

-- [V34__drop_currency_code_from_pricing_quote.sql]
SET @pricing_quote_currency_code_exists = (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pricing_quote'
      AND COLUMN_NAME = 'currency_code'
);
SET @drop_pricing_quote_currency_code_sql = IF(
    @pricing_quote_currency_code_exists = 1,
    'ALTER TABLE pricing_quote DROP COLUMN currency_code',
    'SET @noop_drop_pricing_quote_currency_code = 1'
);
PREPARE stmt_drop_pricing_quote_currency_code FROM @drop_pricing_quote_currency_code_sql;
EXECUTE stmt_drop_pricing_quote_currency_code;
DEALLOCATE PREPARE stmt_drop_pricing_quote_currency_code;

-- [V35__create_user_recent_contact_table_and_seed_data.sql]
CREATE TABLE IF NOT EXISTS user_recent_contact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户最近联系人主键ID',
    owner_user_id BIGINT NOT NULL COMMENT '联系人所有者用户ID',
    contact_user_id BIGINT NOT NULL COMMENT '最近互动联系人用户ID',
    interaction_scene_code VARCHAR(64) NOT NULL DEFAULT 'APP_INTERNAL_TRANSFER' COMMENT '最近互动场景编码',
    remark VARCHAR(128) NULL COMMENT '最近互动备注',
    last_interaction_at DATETIME NOT NULL COMMENT '最近互动时间',
    interaction_count BIGINT NOT NULL DEFAULT 1 COMMENT '累计互动次数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_recent_contact_owner_contact (owner_user_id, contact_user_id),
    KEY idx_user_recent_contact_owner_last_time (owner_user_id, last_interaction_at DESC),
    KEY idx_user_recent_contact_contact_user_id (contact_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户最近联系人表：记录转账等场景下最近使用的收款联系人';

-- [V37__create_contact_conversation_message_media_tables.sql]
CREATE TABLE IF NOT EXISTS contact_friendship (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_user_id BIGINT NOT NULL,
    friend_user_id BIGINT NOT NULL,
    remark VARCHAR(64) NULL,
    source_request_no VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_contact_friendship_owner_friend (owner_user_id, friend_user_id),
    KEY idx_contact_friendship_friend_user (friend_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS contact_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_user_id BIGINT NOT NULL,
    blocked_user_id BIGINT NOT NULL,
    reason VARCHAR(128) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_contact_blacklist_owner_blocked (owner_user_id, blocked_user_id),
    KEY idx_contact_blacklist_blocked_user (blocked_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS contact_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_no VARCHAR(64) NOT NULL,
    requester_user_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    apply_message VARCHAR(256) NULL,
    status VARCHAR(32) NOT NULL,
    handled_by_user_id BIGINT NULL,
    handled_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_contact_request_request_no (request_no),
    KEY idx_contact_request_target_status (target_user_id, status, created_at),
    KEY idx_contact_request_requester_status (requester_user_id, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS conversation_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_no VARCHAR(64) NOT NULL,
    conversation_type VARCHAR(32) NOT NULL,
    biz_key VARCHAR(128) NOT NULL,
    last_message_id VARCHAR(64) NULL,
    last_message_preview VARCHAR(256) NULL,
    last_message_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_conversation_session_no (conversation_no),
    UNIQUE KEY uk_conversation_session_biz_key (biz_key),
    KEY idx_conversation_session_last_message_at (last_message_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS conversation_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    peer_user_id BIGINT NOT NULL,
    unread_count BIGINT NOT NULL DEFAULT 0,
    last_read_message_id VARCHAR(64) NULL,
    last_read_at DATETIME NULL,
    mute_flag TINYINT(1) NOT NULL DEFAULT 0,
    pin_flag TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_conversation_member_conversation_user (conversation_no, user_id),
    KEY idx_conversation_member_user_updated (user_id, updated_at),
    KEY idx_conversation_member_peer_user (peer_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS message_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(64) NOT NULL,
    conversation_no VARCHAR(64) NOT NULL,
    sender_user_id BIGINT NOT NULL,
    receiver_user_id BIGINT NOT NULL,
    message_type VARCHAR(32) NOT NULL,
    content_text VARCHAR(2048) NULL,
    media_id VARCHAR(64) NULL,
    amount DECIMAL(18,2) NULL,
    trade_no VARCHAR(64) NULL,
    ext_payload JSON NULL,
    message_status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_message_record_message_id (message_id),
    KEY idx_message_record_conversation_id (conversation_no, id),
    KEY idx_message_record_receiver_id (receiver_user_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS media_asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    media_id VARCHAR(64) NOT NULL,
    owner_user_id BIGINT NOT NULL,
    media_type VARCHAR(32) NOT NULL,
    original_name VARCHAR(256) NOT NULL,
    mime_type VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL,
    compressed_size_bytes BIGINT NULL,
    width INT NULL,
    height INT NULL,
    storage_path VARCHAR(512) NOT NULL,
    thumbnail_path VARCHAR(512) NULL,
    sha256 VARCHAR(128) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_media_asset_media_id (media_id),
    KEY idx_media_asset_owner_created (owner_user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [V41__add_credit_repay_day_and_seed_gujun_aicredit.sql]
ALTER TABLE credit_account
    ADD COLUMN repay_day_of_month TINYINT NOT NULL DEFAULT 10 COMMENT '每月还款日' AFTER pay_status;

-- [V46__create_deliver_tables.sql]
CREATE TABLE IF NOT EXISTS deliver_position (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    position_code VARCHAR(64) NOT NULL,
    position_name VARCHAR(128) NOT NULL,
    position_type VARCHAR(32) NOT NULL,
    preview_image VARCHAR(512) NULL,
    slide_interval INT NULL,
    max_display_count INT NOT NULL DEFAULT 1,
    sort_type VARCHAR(32) NOT NULL DEFAULT 'PRIORITY',
    sort_rule TEXT NULL,
    need_fallback TINYINT(1) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'EDITING',
    memo VARCHAR(255) NULL,
    published_at DATETIME NULL,
    active_from DATETIME NULL,
    active_to DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_deliver_position_code (position_code),
    KEY idx_deliver_position_status_window (status, active_from, active_to)
);
CREATE TABLE IF NOT EXISTS deliver_unit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    unit_code VARCHAR(64) NOT NULL,
    unit_name VARCHAR(128) NOT NULL,
    priority INT NOT NULL DEFAULT 100,
    status VARCHAR(32) NOT NULL DEFAULT 'EDITING',
    memo VARCHAR(255) NULL,
    active_from DATETIME NULL,
    active_to DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_deliver_unit_code (unit_code),
    KEY idx_deliver_unit_status_window (status, active_from, active_to)
);
CREATE TABLE IF NOT EXISTS deliver_material (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    material_code VARCHAR(64) NOT NULL,
    material_name VARCHAR(128) NOT NULL,
    material_type VARCHAR(32) NOT NULL,
    title VARCHAR(128) NULL,
    image_url VARCHAR(512) NULL,
    landing_url VARCHAR(512) NULL,
    schema_json TEXT NULL,
    preview_image VARCHAR(512) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'EDITING',
    active_from DATETIME NULL,
    active_to DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_deliver_material_code (material_code),
    KEY idx_deliver_material_status_window (status, active_from, active_to)
);
CREATE TABLE IF NOT EXISTS deliver_creative (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creative_code VARCHAR(64) NOT NULL,
    creative_name VARCHAR(128) NOT NULL,
    unit_code VARCHAR(64) NOT NULL,
    material_code VARCHAR(64) NOT NULL,
    landing_url VARCHAR(512) NULL,
    schema_json TEXT NULL,
    priority INT NOT NULL DEFAULT 100,
    weight INT NOT NULL DEFAULT 0,
    is_fallback TINYINT(1) NOT NULL DEFAULT 0,
    preview_image VARCHAR(512) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'EDITING',
    active_from DATETIME NULL,
    active_to DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_deliver_creative_code (creative_code),
    KEY idx_deliver_creative_unit_code (unit_code),
    KEY idx_deliver_creative_material_code (material_code),
    KEY idx_deliver_creative_status_window (status, active_from, active_to)
);
CREATE TABLE IF NOT EXISTS deliver_position_unit_creative_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    position_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,
    creative_id BIGINT NOT NULL,
    display_order INT NOT NULL DEFAULT 100,
    is_fallback TINYINT(1) NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_deliver_position_unit_creative (position_id, unit_id, creative_id),
    KEY idx_deliver_position_relation_position (position_id, is_fallback, enabled),
    CONSTRAINT fk_deliver_relation_position FOREIGN KEY (position_id) REFERENCES deliver_position(id),
    CONSTRAINT fk_deliver_relation_unit FOREIGN KEY (unit_id) REFERENCES deliver_unit(id),
    CONSTRAINT fk_deliver_relation_creative FOREIGN KEY (creative_id) REFERENCES deliver_creative(id)
);
CREATE TABLE IF NOT EXISTS deliver_fatigue_control_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fatigue_code VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    entity_type VARCHAR(32) NOT NULL,
    entity_code VARCHAR(64) NOT NULL,
    event_type VARCHAR(32) NOT NULL DEFAULT 'DISPLAY',
    time_window_minutes INT NOT NULL DEFAULT 60,
    max_count INT NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_deliver_fatigue_code (fatigue_code),
    KEY idx_deliver_fatigue_entity (entity_type, entity_code, enabled)
);
CREATE TABLE IF NOT EXISTS deliver_targeting_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_code VARCHAR(64) NOT NULL,
    entity_type VARCHAR(32) NOT NULL,
    entity_code VARCHAR(64) NOT NULL,
    targeting_type VARCHAR(32) NOT NULL,
    operator VARCHAR(32) NOT NULL DEFAULT 'IN',
    targeting_value VARCHAR(1024) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_deliver_targeting_rule_code (rule_code),
    KEY idx_deliver_targeting_entity (entity_type, entity_code, enabled)
);
CREATE TABLE IF NOT EXISTS deliver_event_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    client_id VARCHAR(64) NOT NULL,
    user_id BIGINT NULL,
    entity_type VARCHAR(32) NOT NULL,
    entity_code VARCHAR(64) NOT NULL,
    position_code VARCHAR(64) NULL,
    unit_code VARCHAR(64) NULL,
    creative_code VARCHAR(64) NULL,
    event_type VARCHAR(32) NOT NULL,
    scene_code VARCHAR(64) NULL,
    channel VARCHAR(64) NULL,
    event_time DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_deliver_event_entity (client_id, entity_type, entity_code, event_type, event_time),
    KEY idx_deliver_event_user_entity (user_id, entity_type, entity_code, event_type, event_time)
);

-- [V55__allow_multiple_credit_accounts_per_user.sql]
ALTER TABLE credit_account
    DROP INDEX uk_credit_account_user_id,
    ADD KEY idx_credit_account_user_id (user_id);

-- [V56__create_feedback_ticket_table.sql]
CREATE TABLE IF NOT EXISTS feedback_ticket (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    feedback_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    feedback_type VARCHAR(32) NOT NULL DEFAULT 'PRODUCT_SUGGESTION',
    source_channel VARCHAR(32) NOT NULL DEFAULT 'IOS_APP',
    source_page_code VARCHAR(64) NULL,
    title VARCHAR(128) NULL,
    content VARCHAR(200) NOT NULL,
    contact_mobile VARCHAR(32) NULL,
    attachment_urls_text TEXT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    handled_by VARCHAR(64) NULL,
    handle_note VARCHAR(1000) NULL,
    handled_at DATETIME NULL,
    closed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_feedback_ticket_no (feedback_no),
    KEY idx_feedback_ticket_user_status (user_id, status, created_at),
    KEY idx_feedback_ticket_type_status (feedback_type, status, created_at)
);

-- [V59__rename_inbound_transaction_to_inbound_order.sql]
SET @drop_influx_payer_fk_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.REFERENTIAL_CONSTRAINTS
                WHERE CONSTRAINT_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'influx_payer'
                  AND CONSTRAINT_NAME = 'fk_influx_payer_influx_id'
        ),
        'ALTER TABLE influx_payer DROP FOREIGN KEY fk_influx_payer_influx_id',
        'SELECT 1'
                                 );
PREPARE stmt FROM @drop_influx_payer_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @drop_influx_inst_fk_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.REFERENTIAL_CONSTRAINTS
                WHERE CONSTRAINT_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'influx_inst'
                  AND CONSTRAINT_NAME = 'fk_influx_inst_influx_id'
        ),
        'ALTER TABLE influx_inst DROP FOREIGN KEY fk_influx_inst_influx_id',
        'SELECT 1'
                                );
PREPARE stmt FROM @drop_influx_inst_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @add_influx_payer_fk_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'inbound_order'
        )
        AND NOT EXISTS(
                SELECT 1
                FROM information_schema.REFERENTIAL_CONSTRAINTS
                WHERE CONSTRAINT_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'influx_payer'
                  AND CONSTRAINT_NAME = 'fk_influx_payer_influx_id'
        ),
        'ALTER TABLE influx_payer ADD CONSTRAINT fk_influx_payer_influx_id FOREIGN KEY (influx_id) REFERENCES inbound_order (influx_id)',
        'SELECT 1'
                                );
PREPARE stmt FROM @add_influx_payer_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @add_influx_inst_fk_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'inbound_order'
        )
        AND NOT EXISTS(
                SELECT 1
                FROM information_schema.REFERENTIAL_CONSTRAINTS
                WHERE CONSTRAINT_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'influx_inst'
                  AND CONSTRAINT_NAME = 'fk_influx_inst_influx_id'
        ),
        'ALTER TABLE influx_inst ADD CONSTRAINT fk_influx_inst_influx_id FOREIGN KEY (influx_id) REFERENCES inbound_order (influx_id)',
        'SELECT 1'
                               );
PREPARE stmt FROM @add_influx_inst_fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V60__enhance_trade_business_order_model.sql]
ALTER TABLE trade_order
    ADD COLUMN business_domain_code VARCHAR(32) NULL COMMENT '业务域编码：用于区分通用交易、花呗、借呗、余额宝、余额等不同业务交易查询视角' AFTER business_scene_code,
    ADD COLUMN business_order_no VARCHAR(64) NULL COMMENT '业务交易单号：在对应业务域内唯一，用于后台按业务单直接检索交易' AFTER business_domain_code;
ALTER TABLE trade_order
    MODIFY COLUMN business_domain_code VARCHAR(32) NOT NULL COMMENT '业务域编码：用于区分通用交易、花呗、借呗、余额宝、余额等不同业务交易查询视角',
    MODIFY COLUMN business_order_no VARCHAR(64) NOT NULL COMMENT '业务交易单号：在对应业务域内唯一，用于后台按业务单直接检索交易',
    ADD KEY idx_trade_order_business_order (business_domain_code, business_order_no);
CREATE TABLE IF NOT EXISTS trade_credit_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '信用业务交易扩展单主键ID',
    business_order_no VARCHAR(64) NOT NULL COMMENT '信用业务交易单号：花呗还款、借呗借款等业务域内唯一单号',
    trade_no VARCHAR(64) NOT NULL COMMENT '关联统一交易主单号',
    credit_product_type VARCHAR(32) NOT NULL COMMENT '信用产品类型：HUABEI/JIEBEI',
    credit_account_no VARCHAR(64) NOT NULL COMMENT '信用账户号：定位花呗或借呗账户',
    bill_no VARCHAR(64) NULL COMMENT '账单号：花呗月账单或借呗还款计划账单号',
    bill_month VARCHAR(16) NULL COMMENT '账单月份：格式如 2026-03，用于账单页快速查询',
    repayment_plan_no VARCHAR(64) NULL COMMENT '还款计划号：借呗分期或信用账单计划标识',
    credit_trade_type VARCHAR(32) NOT NULL COMMENT '信用业务交易类型：CONSUME/REPAY/MINIMUM_REPAY/FULL_REPAY/LOAN_DRAW 等',
    subject_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '业务主体金额：本次消费、借款或还款的展示金额',
    principal_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '本金金额：信用资产中属于本金的部分',
    interest_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '利息金额：借呗利息或信用账单利息部分',
    fee_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '费用金额：服务费、手续费等信用附加费用',
    currency_code VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '币种编码：当前爱付场景统一使用 CNY，保留后续扩展能力',
    counterparty_name VARCHAR(64) NULL COMMENT '交易对手名称：例如商户名、收款方昵称或资金渠道名称',
    occurred_at DATETIME NOT NULL COMMENT '业务发生时间：用于信用账单明细排序展示',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    UNIQUE KEY uk_trade_credit_order_business_order_no (business_order_no),
    UNIQUE KEY uk_trade_credit_order_trade_no (trade_no),
    KEY idx_trade_credit_order_account_time (credit_account_no, occurred_at),
    KEY idx_trade_credit_order_bill_month (bill_month),
    KEY idx_trade_credit_order_bill_no (bill_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用业务交易扩展单表：承接花呗、借呗等信用产品的业务交易明细';
CREATE TABLE IF NOT EXISTS trade_fund_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '基金业务交易扩展单主键ID',
    business_order_no VARCHAR(64) NOT NULL COMMENT '基金业务交易单号：余额宝申购、赎回等业务域内唯一单号',
    trade_no VARCHAR(64) NOT NULL COMMENT '关联统一交易主单号',
    fund_product_type VARCHAR(32) NOT NULL COMMENT '基金产品类型：当前支持 YUEBAO',
    fund_account_no VARCHAR(64) NOT NULL COMMENT '基金账户号：定位余额宝或其他理财账户',
    fund_trade_type VARCHAR(32) NOT NULL COMMENT '基金业务交易类型：PURCHASE/REDEEM/TRANSFER_IN/TRANSFER_OUT/YIELD_SETTLE',
    share_amount DECIMAL(18,8) NOT NULL DEFAULT 0.00000000 COMMENT '基金份额：余额宝份额或其他货币基金份额变化量',
    confirm_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '确认金额：基金确认成交后的金额',
    currency_code VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '币种编码：当前余额宝场景统一使用 CNY',
    nav_date DATE NULL COMMENT '净值日期：基金确认或收益结转对应的净值日',
    occurred_at DATETIME NOT NULL COMMENT '业务发生时间：用于余额宝明细与收益展示排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    UNIQUE KEY uk_trade_fund_order_business_order_no (business_order_no),
    UNIQUE KEY uk_trade_fund_order_trade_no (trade_no),
    KEY idx_trade_fund_order_account_time (fund_account_no, occurred_at),
    KEY idx_trade_fund_order_nav_date (nav_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基金业务交易扩展单表：承接余额宝等基金理财业务的交易明细';
CREATE TABLE IF NOT EXISTS trade_bill_index (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '业务交易查询索引主键ID',
    trade_no VARCHAR(64) NOT NULL COMMENT '统一交易主单号：关联 trade_order',
    business_domain_code VARCHAR(32) NOT NULL COMMENT '业务域编码：如 TRADE/HUABEI/JIEBEI/YUEBAO/WALLET',
    business_order_no VARCHAR(64) NOT NULL COMMENT '业务交易单号：后台与账单页按业务单查询时使用',
    product_type VARCHAR(32) NULL COMMENT '产品类型：例如 HUABEI/JIEBEI/YUEBAO，用于细分业务产品',
    business_type VARCHAR(32) NULL COMMENT '业务类型：例如 REPAY/MINIMUM_REPAY/PURCHASE/REDEEM',
    user_id BIGINT NOT NULL COMMENT '业务所属用户ID：默认写入交易发起用户',
    counterparty_user_id BIGINT NULL COMMENT '交易对手用户ID：转账、付款等双边交易场景使用',
    account_no VARCHAR(64) NULL COMMENT '业务账户号：花呗账户号、余额宝账户号、余额账户号等',
    bill_no VARCHAR(64) NULL COMMENT '账单号：账单场景按账单检索时使用',
    bill_month VARCHAR(16) NULL COMMENT '账单月份：账单中心按月筛选时使用',
    display_title VARCHAR(128) NULL COMMENT '展示标题：用于后台交易列表或账单页直接展示的主标题',
    display_subtitle VARCHAR(128) NULL COMMENT '展示副标题：用于展示商户名、验证方式、支付工具等补充信息',
    amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '展示金额：业务列表默认展示的交易金额',
    currency_code VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '币种编码：用于金额对象恢复与多币种展示',
    status VARCHAR(32) NOT NULL COMMENT '业务交易状态：与统一交易主单同步',
    trade_time DATETIME NOT NULL COMMENT '交易时间：统一作为业务列表排序时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    UNIQUE KEY uk_trade_business_index_trade_no (trade_no),
    UNIQUE KEY uk_trade_business_index_business_order (business_domain_code, business_order_no),
    KEY idx_trade_business_index_user_domain_time (user_id, business_domain_code, trade_time),
    KEY idx_trade_business_index_account_time (account_no, trade_time),
    KEY idx_trade_business_index_bill_month (bill_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务交易查询索引表：为花呗、借呗、余额宝、余额等业务域提供统一查询入口';

-- [V61__enhance_trade_fund_order_bill_model.sql]
ALTER TABLE trade_fund_order
    ADD COLUMN bill_no VARCHAR(64) NULL COMMENT '基金账单号：按月归集余额宝账单时使用' AFTER fund_account_no,
    ADD COLUMN bill_month VARCHAR(16) NULL COMMENT '基金账单月份：例如 2026-03' AFTER bill_no;

-- [V70__create_app_version_management_tables.sql]
-- App 版本管理第一版：只覆盖 iPhone / iOS，不创建 Android / APK 相关表。

CREATE TABLE IF NOT EXISTS app_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '应用主键ID',
    app_code VARCHAR(64) NOT NULL COMMENT '应用编码，业务唯一标识',
    app_name VARCHAR(128) NOT NULL COMMENT '应用名称',
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED' COMMENT '应用状态：ENABLED/DISABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_app_info_code (app_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用定义表';
CREATE TABLE IF NOT EXISTS app_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '应用版本主键ID',
    version_code VARCHAR(64) NOT NULL COMMENT '版本编码，业务唯一标识',
    app_code VARCHAR(64) NOT NULL COMMENT '所属应用编码',
    client_type VARCHAR(32) NOT NULL DEFAULT 'IOS_IPHONE' COMMENT '客户端类型：当前仅支持 IOS_IPHONE',
    app_version_no VARCHAR(32) NOT NULL COMMENT '应用版本号，如 1.0.0',
    update_type VARCHAR(32) NOT NULL DEFAULT 'OPTIONAL' COMMENT '更新类型：OPTIONAL/RECOMMENDED/FORCE',
    update_prompt_frequency VARCHAR(32) NOT NULL DEFAULT 'ONCE_PER_VERSION' COMMENT '更新提示频率',
    version_description VARCHAR(1000) NULL COMMENT '版本更新描述',
    release_region_list_text TEXT NULL COMMENT '发行区域列表，按换行方式存储',
    target_region_list_text TEXT NULL COMMENT '定向更新区域列表，按换行方式存储',
    min_supported_version_no VARCHAR(32) NULL COMMENT '允许最低版本号',
    latest_published_version TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否最新发布版',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '版本状态：DRAFT/ENABLED/DISABLED/ARCHIVED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_app_version_code (version_code),
    UNIQUE KEY uk_app_version_app_client_no (app_code, client_type, app_version_no),
    KEY idx_app_version_app_client_status (app_code, client_type, status),
    KEY idx_app_version_latest_published (app_code, client_type, latest_published_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用版本表';
CREATE TABLE IF NOT EXISTS app_ios_package (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'iOS 包主键ID',
    ios_code VARCHAR(64) NOT NULL COMMENT 'iOS 包编码，业务唯一标识',
    app_code VARCHAR(64) NOT NULL COMMENT '所属应用编码',
    version_code VARCHAR(64) NOT NULL COMMENT '所属版本编码',
    app_store_url VARCHAR(512) NULL COMMENT '商店地址或测试分发地址',
    package_size_bytes BIGINT NULL COMMENT '安装包大小，单位字节',
    md5 VARCHAR(64) NULL COMMENT '安装包摘要',
    review_submitted_at DATETIME NULL COMMENT '提交审核时间',
    published_at DATETIME NULL COMMENT '发布时间',
    release_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '发布状态：DRAFT/REVIEWING/PUBLISHED/REJECTED/OFFLINE',
    review_submitted_by VARCHAR(64) NULL COMMENT '提交审核人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_app_ios_package_code (ios_code),
    UNIQUE KEY uk_app_ios_package_version_code (version_code),
    KEY idx_app_ios_package_app_version (app_code, version_code),
    KEY idx_app_ios_package_status (release_status, published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iOS 安装包发布表';
CREATE TABLE IF NOT EXISTS app_device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备记录主键ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备标识',
    app_code VARCHAR(64) NOT NULL COMMENT '应用编码',
    client_id_list_text TEXT NULL COMMENT '客户端ID列表',
    status VARCHAR(32) NOT NULL DEFAULT 'INSTALLED' COMMENT '设备状态：INSTALLED/ACTIVE/INACTIVE/UNINSTALLED',
    installed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '安装时间',
    started_at DATETIME NULL COMMENT '启动时间',
    last_opened_at DATETIME NULL COMMENT '最近打开时间',
    current_app_version_id BIGINT NULL COMMENT '当前版本ID',
    current_ios_package_id BIGINT NULL COMMENT '当前 iOS 包ID',
    app_updated_at DATETIME NULL COMMENT '最近升级时间',
    device_brand VARCHAR(64) NULL COMMENT '设备品牌，iPhone 默认 APPLE',
    os_version VARCHAR(64) NULL COMMENT '操作系统版本',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_app_device_device_id (device_id),
    KEY idx_app_device_app_status (app_code, status),
    KEY idx_app_device_last_opened (app_code, last_opened_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='App 设备安装与活跃表';
CREATE TABLE IF NOT EXISTS app_visit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '访问记录主键ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备标识',
    app_code VARCHAR(64) NOT NULL COMMENT '应用编码',
    client_id VARCHAR(64) NULL COMMENT '客户端ID',
    ip_address VARCHAR(64) NULL COMMENT 'IP 地址',
    location_info VARCHAR(255) NULL COMMENT '位置信息',
    tenant_code VARCHAR(64) NULL COMMENT '租户标识',
    client_type VARCHAR(32) NOT NULL DEFAULT 'IOS_IPHONE' COMMENT '客户端类型',
    network_type VARCHAR(32) NULL COMMENT '联网方式',
    app_version_id BIGINT NULL COMMENT '应用版本ID',
    device_brand VARCHAR(64) NULL COMMENT '设备品牌',
    os_version VARCHAR(64) NULL COMMENT '操作系统版本',
    api_name VARCHAR(128) NOT NULL COMMENT '调用接口',
    request_params_text TEXT NULL COMMENT '请求参数',
    called_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
    result_summary VARCHAR(1000) NULL COMMENT '返回结果摘要',
    duration_ms BIGINT NULL COMMENT '调用耗时，毫秒',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    KEY idx_app_visit_record_device_time (device_id, called_at),
    KEY idx_app_visit_record_app_time (app_code, called_at),
    KEY idx_app_visit_record_version_time (app_version_id, called_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='App 访问记录表';

-- [V72__rename_pay_global_tx_and_create_outbound_order.sql]
SET @rename_pay_global_tx_column_sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'pay_order'
              AND COLUMN_NAME = 'seata_xid'
        )
        AND NOT EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'pay_order'
              AND COLUMN_NAME = 'global_tx_id'
        ),
        'ALTER TABLE pay_order CHANGE COLUMN seata_xid global_tx_id VARCHAR(128) NOT NULL COMMENT ''全局事务号''',
        'SELECT 1'
    )
);
PREPARE stmt FROM @rename_pay_global_tx_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @modify_pay_global_tx_column_sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'pay_order'
              AND COLUMN_NAME = 'global_tx_id'
        ),
        'ALTER TABLE pay_order MODIFY COLUMN global_tx_id VARCHAR(128) NOT NULL COMMENT ''全局事务号''',
        'SELECT 1'
    )
);
PREPARE stmt FROM @modify_pay_global_tx_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
CREATE TABLE IF NOT EXISTS outbound_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    outflux_id VARCHAR(64) NOT NULL COMMENT '出金订单号',
    org_outflux_id VARCHAR(64) NULL COMMENT '原出金订单号',
    inst_id VARCHAR(64) NULL COMMENT '机构标识',
    business_code VARCHAR(64) NOT NULL COMMENT '业务码',
    sub_business_code VARCHAR(64) NOT NULL COMMENT '子业务码',
    exchange_type VARCHAR(32) NOT NULL COMMENT '交换类型',
    finance_exchange_code VARCHAR(64) NULL COMMENT '金融交换码',
    settle_serial_no VARCHAR(64) NULL COMMENT '清算流水号',
    payee_account_no VARCHAR(64) NOT NULL COMMENT '收款账户号',
    exchange_amount DECIMAL(18,2) NOT NULL COMMENT '交易金额',
    exchange_currency VARCHAR(8) NOT NULL COMMENT '交易币种',
    exchange_status VARCHAR(32) NOT NULL COMMENT '交换状态',
    result_code VARCHAR(64) NULL COMMENT '结果码',
    result_description VARCHAR(255) NULL COMMENT '结果描述',
    request_identify VARCHAR(64) NOT NULL COMMENT '请求标识',
    request_biz_no VARCHAR(64) NOT NULL COMMENT '业务请求号',
    pay_unique_no VARCHAR(64) NOT NULL COMMENT '支付唯一号',
    pay_channel_api VARCHAR(64) NOT NULL COMMENT '支付渠道接口',
    inst_channel_api VARCHAR(64) NOT NULL COMMENT '机构渠道接口',
    clear_channel VARCHAR(32) NOT NULL COMMENT '清算渠道',
    biz_identity VARCHAR(64) NOT NULL COMMENT '业务身份',
    gmt_submit DATETIME NULL COMMENT '提交时间',
    gmt_resp DATETIME NULL COMMENT '响应时间',
    gmt_settle DATETIME NULL COMMENT '清算时间',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_outflux_order_outflux_id (outflux_id),
    UNIQUE KEY uk_outflux_order_request_biz_no (request_biz_no),
    UNIQUE KEY uk_outflux_order_pay_unique_no (pay_unique_no),
    KEY idx_outflux_order_exchange_status (exchange_status),
    KEY idx_outflux_order_inst_channel_api (inst_channel_api),
    KEY idx_outflux_order_submit_time (gmt_submit)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出金订单主表';

-- [V74__align_app_ios_package_single_channel_default.sql]
-- 对齐现网 app_ios_package 单渠道结构。
-- 背景：iOS 当前仅保留单一发行渠道，代码侧已不再显式传 release channel。
-- 目标：确保 channel_code 存在且默认值固定为 IOS_APP_STORE，避免管理端创建版本时因必填列报错。

SET @has_channel_code := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'app_ios_package'
      AND COLUMN_NAME = 'channel_code'
);
SET @add_channel_code_sql := IF(
    @has_channel_code = 0,
    "ALTER TABLE app_ios_package ADD COLUMN channel_code VARCHAR(64) NOT NULL DEFAULT 'IOS_APP_STORE' COMMENT '发行渠道编码' AFTER version_code",
    'SELECT 1'
);
PREPARE add_channel_code_stmt FROM @add_channel_code_sql;
EXECUTE add_channel_code_stmt;
DEALLOCATE PREPARE add_channel_code_stmt;
ALTER TABLE app_ios_package
    MODIFY COLUMN channel_code VARCHAR(64) NOT NULL DEFAULT 'IOS_APP_STORE' COMMENT '发行渠道编码';

-- [V76__add_missing_table_and_column_comments.sql]
-- Auto-generated migration: add missing table and column comments
-- Generated from current portal schema metadata

ALTER TABLE `contact_blacklist` COMMENT = '表：contact_blacklist。';
ALTER TABLE `contact_friendship` COMMENT = '表：contact_friendship。';
ALTER TABLE `contact_request` COMMENT = '表：contact_request。';
ALTER TABLE `conversation_member` COMMENT = '表：conversation_member。';
ALTER TABLE `conversation_session` COMMENT = '表：conversation_session。';
ALTER TABLE `deliver_creative` COMMENT = '表：deliver_creative。';
ALTER TABLE `deliver_event_record` COMMENT = '表：deliver_event_record。';
ALTER TABLE `deliver_fatigue_control_rule` COMMENT = '表：deliver_fatigue_control_rule。';
ALTER TABLE `deliver_material` COMMENT = '表：deliver_material。';
ALTER TABLE `deliver_position` COMMENT = '表：deliver_position。';
ALTER TABLE `deliver_position_unit_creative_relation` COMMENT = '表：deliver_position_unit_creative_relation。';
ALTER TABLE `deliver_targeting_rule` COMMENT = '表：deliver_targeting_rule。';
ALTER TABLE `deliver_unit` COMMENT = '表：deliver_unit。';
ALTER TABLE `feedback_ticket` COMMENT = '表：feedback_ticket。';
ALTER TABLE `media_asset` COMMENT = '表：media_asset。';
ALTER TABLE `message_record` COMMENT = '表：message_record。';
ALTER TABLE `contact_blacklist` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `contact_blacklist` MODIFY COLUMN `owner_user_id` bigint NOT NULL COMMENT '字段：owner_user_id。';
ALTER TABLE `contact_blacklist` MODIFY COLUMN `blocked_user_id` bigint NOT NULL COMMENT '字段：blocked_user_id。';
ALTER TABLE `contact_blacklist` MODIFY COLUMN `reason` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字段：reason。';
ALTER TABLE `contact_blacklist` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `contact_blacklist` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `contact_friendship` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `contact_friendship` MODIFY COLUMN `owner_user_id` bigint NOT NULL COMMENT '字段：owner_user_id。';
ALTER TABLE `contact_friendship` MODIFY COLUMN `friend_user_id` bigint NOT NULL COMMENT '字段：friend_user_id。';
ALTER TABLE `contact_friendship` MODIFY COLUMN `remark` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字段：remark。';
ALTER TABLE `contact_friendship` MODIFY COLUMN `source_request_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字段：source_request_no。';
ALTER TABLE `contact_friendship` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `contact_friendship` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `contact_request` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `contact_request` MODIFY COLUMN `request_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：request_no。';
ALTER TABLE `contact_request` MODIFY COLUMN `requester_user_id` bigint NOT NULL COMMENT '字段：requester_user_id。';
ALTER TABLE `contact_request` MODIFY COLUMN `target_user_id` bigint NOT NULL COMMENT '字段：target_user_id。';
ALTER TABLE `contact_request` MODIFY COLUMN `apply_message` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字段：apply_message。';
ALTER TABLE `contact_request` MODIFY COLUMN `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：status。';
ALTER TABLE `contact_request` MODIFY COLUMN `handled_by_user_id` bigint NULL COMMENT '字段：handled_by_user_id。';
ALTER TABLE `contact_request` MODIFY COLUMN `handled_at` datetime NULL COMMENT '字段：handled_at。';
ALTER TABLE `contact_request` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `contact_request` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `conversation_member` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `conversation_member` MODIFY COLUMN `conversation_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：conversation_no。';
ALTER TABLE `conversation_member` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '字段：user_id。';
ALTER TABLE `conversation_member` MODIFY COLUMN `peer_user_id` bigint NOT NULL COMMENT '字段：peer_user_id。';
ALTER TABLE `conversation_member` MODIFY COLUMN `unread_count` bigint NOT NULL DEFAULT 0 COMMENT '字段：unread_count。';
ALTER TABLE `conversation_member` MODIFY COLUMN `last_read_message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字段：last_read_message_id。';
ALTER TABLE `conversation_member` MODIFY COLUMN `last_read_at` datetime NULL COMMENT '字段：last_read_at。';
ALTER TABLE `conversation_member` MODIFY COLUMN `mute_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '字段：mute_flag。';
ALTER TABLE `conversation_member` MODIFY COLUMN `pin_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '字段：pin_flag。';
ALTER TABLE `conversation_member` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `conversation_member` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `conversation_session` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `conversation_session` MODIFY COLUMN `conversation_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：conversation_no。';
ALTER TABLE `conversation_session` MODIFY COLUMN `conversation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：conversation_type。';
ALTER TABLE `conversation_session` MODIFY COLUMN `biz_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：biz_key。';
ALTER TABLE `conversation_session` MODIFY COLUMN `last_message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字段：last_message_id。';
ALTER TABLE `conversation_session` MODIFY COLUMN `last_message_preview` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字段：last_message_preview。';
ALTER TABLE `conversation_session` MODIFY COLUMN `last_message_at` datetime NULL COMMENT '字段：last_message_at。';
ALTER TABLE `conversation_session` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `conversation_session` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `creative_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：creative_code。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `creative_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：creative_name。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `unit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：unit_code。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `material_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：material_code。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `landing_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：landing_url。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `schema_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：schema_json。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `priority` int NOT NULL DEFAULT 100 COMMENT '字段：priority。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `weight` int NOT NULL DEFAULT 0 COMMENT '字段：weight。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `is_fallback` tinyint(1) NOT NULL DEFAULT 0 COMMENT '字段：is_fallback。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `preview_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：preview_image。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EDITING' COMMENT '字段：status。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `active_from` datetime NULL COMMENT '字段：active_from。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `active_to` datetime NULL COMMENT '字段：active_to。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `deliver_creative` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `client_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：client_id。';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `user_id` bigint NULL COMMENT '字段：user_id。';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `entity_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：entity_type。';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `entity_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：entity_code。';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `position_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：position_code。';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `unit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：unit_code。';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `creative_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：creative_code。';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `event_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：event_type。';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `scene_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：scene_code。';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `channel` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：channel。';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `event_time` datetime NOT NULL COMMENT '字段：event_time。';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `fatigue_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：fatigue_code。';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：rule_name。';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `entity_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：entity_type。';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `entity_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：entity_code。';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `event_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DISPLAY' COMMENT '字段：event_type。';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `time_window_minutes` int NOT NULL DEFAULT 60 COMMENT '字段：time_window_minutes。';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `max_count` int NOT NULL COMMENT '字段：max_count。';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '字段：enabled。';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `deliver_material` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `deliver_material` MODIFY COLUMN `material_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：material_code。';
ALTER TABLE `deliver_material` MODIFY COLUMN `material_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：material_name。';
ALTER TABLE `deliver_material` MODIFY COLUMN `material_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：material_type。';
ALTER TABLE `deliver_material` MODIFY COLUMN `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：title。';
ALTER TABLE `deliver_material` MODIFY COLUMN `image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：image_url。';
ALTER TABLE `deliver_material` MODIFY COLUMN `landing_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：landing_url。';
ALTER TABLE `deliver_material` MODIFY COLUMN `schema_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：schema_json。';
ALTER TABLE `deliver_material` MODIFY COLUMN `preview_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：preview_image。';
ALTER TABLE `deliver_material` MODIFY COLUMN `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EDITING' COMMENT '字段：status。';
ALTER TABLE `deliver_material` MODIFY COLUMN `active_from` datetime NULL COMMENT '字段：active_from。';
ALTER TABLE `deliver_material` MODIFY COLUMN `active_to` datetime NULL COMMENT '字段：active_to。';
ALTER TABLE `deliver_material` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `deliver_material` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `deliver_position` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `deliver_position` MODIFY COLUMN `position_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：position_code。';
ALTER TABLE `deliver_position` MODIFY COLUMN `position_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：position_name。';
ALTER TABLE `deliver_position` MODIFY COLUMN `position_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：position_type。';
ALTER TABLE `deliver_position` MODIFY COLUMN `preview_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：preview_image。';
ALTER TABLE `deliver_position` MODIFY COLUMN `slide_interval` int NULL COMMENT '字段：slide_interval。';
ALTER TABLE `deliver_position` MODIFY COLUMN `max_display_count` int NOT NULL DEFAULT 1 COMMENT '字段：max_display_count。';
ALTER TABLE `deliver_position` MODIFY COLUMN `sort_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PRIORITY' COMMENT '字段：sort_type。';
ALTER TABLE `deliver_position` MODIFY COLUMN `sort_rule` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：sort_rule。';
ALTER TABLE `deliver_position` MODIFY COLUMN `need_fallback` tinyint(1) NOT NULL DEFAULT 0 COMMENT '字段：need_fallback。';
ALTER TABLE `deliver_position` MODIFY COLUMN `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EDITING' COMMENT '字段：status。';
ALTER TABLE `deliver_position` MODIFY COLUMN `memo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：memo。';
ALTER TABLE `deliver_position` MODIFY COLUMN `published_at` datetime NULL COMMENT '字段：published_at。';
ALTER TABLE `deliver_position` MODIFY COLUMN `active_from` datetime NULL COMMENT '字段：active_from。';
ALTER TABLE `deliver_position` MODIFY COLUMN `active_to` datetime NULL COMMENT '字段：active_to。';
ALTER TABLE `deliver_position` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `deliver_position` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `position_id` bigint NOT NULL COMMENT '字段：position_id。';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `unit_id` bigint NOT NULL COMMENT '字段：unit_id。';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `creative_id` bigint NOT NULL COMMENT '字段：creative_id。';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `display_order` int NOT NULL DEFAULT 100 COMMENT '字段：display_order。';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `is_fallback` tinyint(1) NOT NULL DEFAULT 0 COMMENT '字段：is_fallback。';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '字段：enabled。';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `rule_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：rule_code。';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `entity_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：entity_type。';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `entity_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：entity_code。';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `targeting_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：targeting_type。';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `operator` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IN' COMMENT '字段：operator。';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `targeting_value` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：targeting_value。';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '字段：enabled。';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `deliver_unit` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `deliver_unit` MODIFY COLUMN `unit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：unit_code。';
ALTER TABLE `deliver_unit` MODIFY COLUMN `unit_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：unit_name。';
ALTER TABLE `deliver_unit` MODIFY COLUMN `priority` int NOT NULL DEFAULT 100 COMMENT '字段：priority。';
ALTER TABLE `deliver_unit` MODIFY COLUMN `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EDITING' COMMENT '字段：status。';
ALTER TABLE `deliver_unit` MODIFY COLUMN `memo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：memo。';
ALTER TABLE `deliver_unit` MODIFY COLUMN `active_from` datetime NULL COMMENT '字段：active_from。';
ALTER TABLE `deliver_unit` MODIFY COLUMN `active_to` datetime NULL COMMENT '字段：active_to。';
ALTER TABLE `deliver_unit` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `deliver_unit` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `feedback_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：feedback_no。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '字段：user_id。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `feedback_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PRODUCT_SUGGESTION' COMMENT '字段：feedback_type。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `source_channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IOS_APP' COMMENT '字段：source_channel。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `source_page_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：source_page_code。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：title。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段：content。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `contact_mobile` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：contact_mobile。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `attachment_urls_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：attachment_urls_text。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SUBMITTED' COMMENT '字段：status。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `handled_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：handled_by。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `handle_note` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字段：handle_note。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `handled_at` datetime NULL COMMENT '字段：handled_at。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `closed_at` datetime NULL COMMENT '字段：closed_at。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `media_asset` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `media_asset` MODIFY COLUMN `media_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：media_id。';
ALTER TABLE `media_asset` MODIFY COLUMN `owner_user_id` bigint NOT NULL COMMENT '字段：owner_user_id。';
ALTER TABLE `media_asset` MODIFY COLUMN `media_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：media_type。';
ALTER TABLE `media_asset` MODIFY COLUMN `original_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：original_name。';
ALTER TABLE `media_asset` MODIFY COLUMN `mime_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：mime_type。';
ALTER TABLE `media_asset` MODIFY COLUMN `size_bytes` bigint NOT NULL COMMENT '字段：size_bytes。';
ALTER TABLE `media_asset` MODIFY COLUMN `compressed_size_bytes` bigint NULL COMMENT '字段：compressed_size_bytes。';
ALTER TABLE `media_asset` MODIFY COLUMN `width` int NULL COMMENT '字段：width。';
ALTER TABLE `media_asset` MODIFY COLUMN `height` int NULL COMMENT '字段：height。';
ALTER TABLE `media_asset` MODIFY COLUMN `storage_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：storage_path。';
ALTER TABLE `media_asset` MODIFY COLUMN `thumbnail_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字段：thumbnail_path。';
ALTER TABLE `media_asset` MODIFY COLUMN `sha256` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字段：sha256。';
ALTER TABLE `media_asset` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `media_asset` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';
ALTER TABLE `message_record` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字段：id。';
ALTER TABLE `message_record` MODIFY COLUMN `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：message_id。';
ALTER TABLE `message_record` MODIFY COLUMN `conversation_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：conversation_no。';
ALTER TABLE `message_record` MODIFY COLUMN `sender_user_id` bigint NOT NULL COMMENT '字段：sender_user_id。';
ALTER TABLE `message_record` MODIFY COLUMN `receiver_user_id` bigint NOT NULL COMMENT '字段：receiver_user_id。';
ALTER TABLE `message_record` MODIFY COLUMN `message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：message_type。';
ALTER TABLE `message_record` MODIFY COLUMN `content_text` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字段：content_text。';
ALTER TABLE `message_record` MODIFY COLUMN `media_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字段：media_id。';
ALTER TABLE `message_record` MODIFY COLUMN `amount` decimal(18,2) NULL COMMENT '字段：amount。';
ALTER TABLE `message_record` MODIFY COLUMN `trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字段：trade_no。';
ALTER TABLE `message_record` MODIFY COLUMN `ext_payload` json NULL COMMENT '字段：ext_payload。';
ALTER TABLE `message_record` MODIFY COLUMN `message_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段：message_status。';
ALTER TABLE `message_record` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '字段：created_at。';
ALTER TABLE `message_record` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '字段：updated_at。';

-- [V78__polish_generic_table_and_column_comments.sql]
-- 为废话型表注释/字段注释补充业务语义。
-- 将“表：xxx。”、“字段：xxx。”替换为实际业务含义注释。

ALTER TABLE `contact_blacklist` COMMENT = '联系人黑名单';
ALTER TABLE `contact_friendship` COMMENT = '用户好友关系';
ALTER TABLE `contact_request` COMMENT = '好友申请记录';
ALTER TABLE `conversation_member` COMMENT = '会话成员关系';
ALTER TABLE `conversation_session` COMMENT = '会话主表';
ALTER TABLE `deliver_creative` COMMENT = '投放创意';
ALTER TABLE `deliver_event_record` COMMENT = '投放事件记录';
ALTER TABLE `deliver_fatigue_control_rule` COMMENT = '投放疲劳度控制规则';
ALTER TABLE `deliver_material` COMMENT = '投放素材';
ALTER TABLE `deliver_position` COMMENT = '投放展位';
ALTER TABLE `deliver_position_unit_creative_relation` COMMENT = '展位单元创意绑定关系';
ALTER TABLE `deliver_targeting_rule` COMMENT = '投放定向规则';
ALTER TABLE `deliver_unit` COMMENT = '投放单元';
ALTER TABLE `feedback_ticket` COMMENT = '用户反馈工单';
ALTER TABLE `media_asset` COMMENT = '用户媒体资源';
ALTER TABLE `message_record` COMMENT = '聊天消息记录';
ALTER TABLE `contact_blacklist` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `contact_blacklist` MODIFY COLUMN `owner_user_id` bigint NOT NULL COMMENT '所属用户ID';
ALTER TABLE `contact_blacklist` MODIFY COLUMN `blocked_user_id` bigint NOT NULL COMMENT '被拉黑用户ID';
ALTER TABLE `contact_blacklist` MODIFY COLUMN `reason` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原因说明';
ALTER TABLE `contact_blacklist` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `contact_blacklist` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `contact_friendship` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `contact_friendship` MODIFY COLUMN `owner_user_id` bigint NOT NULL COMMENT '所属用户ID';
ALTER TABLE `contact_friendship` MODIFY COLUMN `friend_user_id` bigint NOT NULL COMMENT '好友用户ID';
ALTER TABLE `contact_friendship` MODIFY COLUMN `remark` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注';
ALTER TABLE `contact_friendship` MODIFY COLUMN `source_request_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源请求号';
ALTER TABLE `contact_friendship` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `contact_friendship` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `contact_request` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `contact_request` MODIFY COLUMN `request_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求号';
ALTER TABLE `contact_request` MODIFY COLUMN `requester_user_id` bigint NOT NULL COMMENT '申请发起方用户ID';
ALTER TABLE `contact_request` MODIFY COLUMN `target_user_id` bigint NOT NULL COMMENT '目标用户ID';
ALTER TABLE `contact_request` MODIFY COLUMN `apply_message` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请附言';
ALTER TABLE `contact_request` MODIFY COLUMN `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '当前状态';
ALTER TABLE `contact_request` MODIFY COLUMN `handled_by_user_id` bigint NULL DEFAULT NULL COMMENT '处理人用户ID';
ALTER TABLE `contact_request` MODIFY COLUMN `handled_at` datetime NULL DEFAULT NULL COMMENT '处理时间';
ALTER TABLE `contact_request` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `contact_request` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `conversation_member` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `conversation_member` MODIFY COLUMN `conversation_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话号';
ALTER TABLE `conversation_member` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '用户ID';
ALTER TABLE `conversation_member` MODIFY COLUMN `peer_user_id` bigint NOT NULL COMMENT '对端用户ID';
ALTER TABLE `conversation_member` MODIFY COLUMN `unread_count` bigint NOT NULL DEFAULT 0 COMMENT '未读消息数';
ALTER TABLE `conversation_member` MODIFY COLUMN `last_read_message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后已读消息ID';
ALTER TABLE `conversation_member` MODIFY COLUMN `last_read_at` datetime NULL DEFAULT NULL COMMENT '最后已读时间';
ALTER TABLE `conversation_member` MODIFY COLUMN `mute_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '免打扰标记';
ALTER TABLE `conversation_member` MODIFY COLUMN `pin_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '置顶标记';
ALTER TABLE `conversation_member` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `conversation_member` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `conversation_session` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `conversation_session` MODIFY COLUMN `conversation_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话号';
ALTER TABLE `conversation_session` MODIFY COLUMN `conversation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话类型';
ALTER TABLE `conversation_session` MODIFY COLUMN `biz_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务唯一键';
ALTER TABLE `conversation_session` MODIFY COLUMN `last_message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一条消息ID';
ALTER TABLE `conversation_session` MODIFY COLUMN `last_message_preview` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一条消息预览';
ALTER TABLE `conversation_session` MODIFY COLUMN `last_message_at` datetime NULL DEFAULT NULL COMMENT '最后消息时间';
ALTER TABLE `conversation_session` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `conversation_session` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `deliver_creative` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `deliver_creative` MODIFY COLUMN `creative_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创意编码';
ALTER TABLE `deliver_creative` MODIFY COLUMN `creative_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创意名称';
ALTER TABLE `deliver_creative` MODIFY COLUMN `unit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '投放单元编码';
ALTER TABLE `deliver_creative` MODIFY COLUMN `material_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '素材编码';
ALTER TABLE `deliver_creative` MODIFY COLUMN `landing_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '落地页地址';
ALTER TABLE `deliver_creative` MODIFY COLUMN `schema_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '端内跳转 Schema 配置';
ALTER TABLE `deliver_creative` MODIFY COLUMN `priority` int NOT NULL DEFAULT 100 COMMENT '优先级';
ALTER TABLE `deliver_creative` MODIFY COLUMN `weight` int NOT NULL DEFAULT 0 COMMENT '权重';
ALTER TABLE `deliver_creative` MODIFY COLUMN `is_fallback` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否兜底';
ALTER TABLE `deliver_creative` MODIFY COLUMN `preview_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '预览图地址';
ALTER TABLE `deliver_creative` MODIFY COLUMN `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EDITING' COMMENT '创意发布状态';
ALTER TABLE `deliver_creative` MODIFY COLUMN `active_from` datetime NULL DEFAULT NULL COMMENT '生效开始时间';
ALTER TABLE `deliver_creative` MODIFY COLUMN `active_to` datetime NULL DEFAULT NULL COMMENT '生效结束时间';
ALTER TABLE `deliver_creative` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `deliver_creative` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `client_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户端ID';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `entity_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作用实体类型';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `entity_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作用实体编码';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `position_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '展位编码';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `unit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '投放单元编码';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `creative_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创意编码';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `event_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件类型';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `scene_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '场景编码';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `channel` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '渠道编码';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `event_time` datetime NOT NULL COMMENT '事件发生时间';
ALTER TABLE `deliver_event_record` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `fatigue_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '疲劳度规则编码';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则名称';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `entity_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作用实体类型';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `entity_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作用实体编码';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `event_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DISPLAY' COMMENT '事件类型';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `time_window_minutes` int NOT NULL DEFAULT 60 COMMENT '统计时间窗口（分钟）';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `max_count` int NOT NULL COMMENT '最大触发次数';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `deliver_fatigue_control_rule` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `deliver_material` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `deliver_material` MODIFY COLUMN `material_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '素材编码';
ALTER TABLE `deliver_material` MODIFY COLUMN `material_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '素材名称';
ALTER TABLE `deliver_material` MODIFY COLUMN `material_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '素材类型';
ALTER TABLE `deliver_material` MODIFY COLUMN `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '素材标题';
ALTER TABLE `deliver_material` MODIFY COLUMN `image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片地址';
ALTER TABLE `deliver_material` MODIFY COLUMN `landing_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '落地页地址';
ALTER TABLE `deliver_material` MODIFY COLUMN `schema_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '端内跳转 Schema 配置';
ALTER TABLE `deliver_material` MODIFY COLUMN `preview_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '预览图地址';
ALTER TABLE `deliver_material` MODIFY COLUMN `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EDITING' COMMENT '素材发布状态';
ALTER TABLE `deliver_material` MODIFY COLUMN `active_from` datetime NULL DEFAULT NULL COMMENT '生效开始时间';
ALTER TABLE `deliver_material` MODIFY COLUMN `active_to` datetime NULL DEFAULT NULL COMMENT '生效结束时间';
ALTER TABLE `deliver_material` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `deliver_material` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `deliver_position` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `deliver_position` MODIFY COLUMN `position_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展位编码';
ALTER TABLE `deliver_position` MODIFY COLUMN `position_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展位名称';
ALTER TABLE `deliver_position` MODIFY COLUMN `position_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展位类型';
ALTER TABLE `deliver_position` MODIFY COLUMN `preview_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '预览图地址';
ALTER TABLE `deliver_position` MODIFY COLUMN `slide_interval` int NULL DEFAULT NULL COMMENT '轮播间隔';
ALTER TABLE `deliver_position` MODIFY COLUMN `max_display_count` int NOT NULL DEFAULT 1 COMMENT '最大展示数量';
ALTER TABLE `deliver_position` MODIFY COLUMN `sort_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PRIORITY' COMMENT '排序策略';
ALTER TABLE `deliver_position` MODIFY COLUMN `sort_rule` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '排序规则';
ALTER TABLE `deliver_position` MODIFY COLUMN `need_fallback` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否需要兜底';
ALTER TABLE `deliver_position` MODIFY COLUMN `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EDITING' COMMENT '展位发布状态';
ALTER TABLE `deliver_position` MODIFY COLUMN `memo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注';
ALTER TABLE `deliver_position` MODIFY COLUMN `published_at` datetime NULL DEFAULT NULL COMMENT '发布时间';
ALTER TABLE `deliver_position` MODIFY COLUMN `active_from` datetime NULL DEFAULT NULL COMMENT '生效开始时间';
ALTER TABLE `deliver_position` MODIFY COLUMN `active_to` datetime NULL DEFAULT NULL COMMENT '生效结束时间';
ALTER TABLE `deliver_position` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `deliver_position` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `position_id` bigint NOT NULL COMMENT '展位ID';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `unit_id` bigint NOT NULL COMMENT '投放单元ID';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `creative_id` bigint NOT NULL COMMENT '创意ID';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `display_order` int NOT NULL DEFAULT 100 COMMENT '展示顺序';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `is_fallback` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否兜底';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `deliver_position_unit_creative_relation` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `rule_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则编码';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `entity_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作用实体类型';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `entity_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作用实体编码';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `targeting_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '定向类型';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `operator` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IN' COMMENT '定向操作符';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `targeting_value` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '定向规则值';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `deliver_targeting_rule` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `deliver_unit` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `deliver_unit` MODIFY COLUMN `unit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '投放单元编码';
ALTER TABLE `deliver_unit` MODIFY COLUMN `unit_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '投放单元名称';
ALTER TABLE `deliver_unit` MODIFY COLUMN `priority` int NOT NULL DEFAULT 100 COMMENT '优先级';
ALTER TABLE `deliver_unit` MODIFY COLUMN `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EDITING' COMMENT '投放单元发布状态';
ALTER TABLE `deliver_unit` MODIFY COLUMN `memo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注';
ALTER TABLE `deliver_unit` MODIFY COLUMN `active_from` datetime NULL DEFAULT NULL COMMENT '生效开始时间';
ALTER TABLE `deliver_unit` MODIFY COLUMN `active_to` datetime NULL DEFAULT NULL COMMENT '生效结束时间';
ALTER TABLE `deliver_unit` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `deliver_unit` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `feedback_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈单号';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT '用户ID';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `feedback_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PRODUCT_SUGGESTION' COMMENT '反馈类型';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `source_channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IOS_APP' COMMENT '来源渠道';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `source_page_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源页面编码';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `contact_mobile` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系方式手机号';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `attachment_urls_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '附件地址列表 JSON';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SUBMITTED' COMMENT '当前状态';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `handled_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理人';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `handle_note` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理备注';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `handled_at` datetime NULL DEFAULT NULL COMMENT '处理时间';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `closed_at` datetime NULL DEFAULT NULL COMMENT '关闭时间';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `feedback_ticket` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `media_asset` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `media_asset` MODIFY COLUMN `media_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '媒体资源ID';
ALTER TABLE `media_asset` MODIFY COLUMN `owner_user_id` bigint NOT NULL COMMENT '所属用户ID';
ALTER TABLE `media_asset` MODIFY COLUMN `media_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '媒体类型';
ALTER TABLE `media_asset` MODIFY COLUMN `original_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名';
ALTER TABLE `media_asset` MODIFY COLUMN `mime_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件 MIME 类型';
ALTER TABLE `media_asset` MODIFY COLUMN `size_bytes` bigint NOT NULL COMMENT '文件大小（字节）';
ALTER TABLE `media_asset` MODIFY COLUMN `compressed_size_bytes` bigint NULL DEFAULT NULL COMMENT '压缩后文件大小（字节）';
ALTER TABLE `media_asset` MODIFY COLUMN `width` int NULL DEFAULT NULL COMMENT '宽度';
ALTER TABLE `media_asset` MODIFY COLUMN `height` int NULL DEFAULT NULL COMMENT '高度';
ALTER TABLE `media_asset` MODIFY COLUMN `storage_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储路径';
ALTER TABLE `media_asset` MODIFY COLUMN `thumbnail_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缩略图路径';
ALTER TABLE `media_asset` MODIFY COLUMN `sha256` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件 SHA-256 摘要';
ALTER TABLE `media_asset` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `media_asset` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `message_record` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE `message_record` MODIFY COLUMN `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息ID';
ALTER TABLE `message_record` MODIFY COLUMN `conversation_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话号';
ALTER TABLE `message_record` MODIFY COLUMN `sender_user_id` bigint NOT NULL COMMENT '发送方用户ID';
ALTER TABLE `message_record` MODIFY COLUMN `receiver_user_id` bigint NOT NULL COMMENT '接收方用户ID';
ALTER TABLE `message_record` MODIFY COLUMN `message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息类型';
ALTER TABLE `message_record` MODIFY COLUMN `content_text` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文本内容';
ALTER TABLE `message_record` MODIFY COLUMN `media_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '媒体资源ID';
ALTER TABLE `message_record` MODIFY COLUMN `amount` decimal(18,2) NULL DEFAULT NULL COMMENT '金额';
ALTER TABLE `message_record` MODIFY COLUMN `trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交易号';
ALTER TABLE `message_record` MODIFY COLUMN `ext_payload` json NULL DEFAULT NULL COMMENT '扩展载荷';
ALTER TABLE `message_record` MODIFY COLUMN `message_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息状态';
ALTER TABLE `message_record` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE `message_record` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- [V80__drop_unused_inbound_outbound_fields.sql]
SET @drop_influx_org_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'inbound_order'
                  AND COLUMN_NAME = 'org_influx_id'
        ),
        'ALTER TABLE inbound_order DROP COLUMN org_influx_id',
        'SELECT 1'
);
PREPARE stmt FROM @drop_influx_org_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @drop_influx_recover_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'inbound_order'
                  AND COLUMN_NAME = 'recover_flag'
        ),
        'ALTER TABLE inbound_order DROP COLUMN recover_flag',
        'SELECT 1'
);
PREPARE stmt FROM @drop_influx_recover_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @drop_influx_recon_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'inbound_order'
                  AND COLUMN_NAME = 'recon_flag'
        ),
        'ALTER TABLE inbound_order DROP COLUMN recon_flag',
        'SELECT 1'
);
PREPARE stmt FROM @drop_influx_recon_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @drop_influx_negative_flag_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'inbound_order'
                  AND COLUMN_NAME = 'negative_flag'
        ),
        'ALTER TABLE inbound_order DROP COLUMN negative_flag',
        'SELECT 1'
);
PREPARE stmt FROM @drop_influx_negative_flag_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @drop_influx_negative_type_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'inbound_order'
                  AND COLUMN_NAME = 'negative_exchange_type'
        ),
        'ALTER TABLE inbound_order DROP COLUMN negative_exchange_type',
        'SELECT 1'
);
PREPARE stmt FROM @drop_influx_negative_type_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @drop_outflux_org_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'outbound_order'
                  AND COLUMN_NAME = 'org_outflux_id'
        ),
        'ALTER TABLE outbound_order DROP COLUMN org_outflux_id',
        'SELECT 1'
);
PREPARE stmt FROM @drop_outflux_org_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
DROP TABLE IF EXISTS influx_payer;
DROP TABLE IF EXISTS influx_inst;

-- [V81__add_app_version_prompt_switch_admin_menu.sql]
ALTER TABLE `app_info`
    ADD COLUMN `version_prompt_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '版本提示开关：0关闭，1开启' AFTER `status`;

-- [V82__drop_constant_inbound_outbound_semantic_columns.sql]
SET @drop_influx_business_code_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'inbound_order'
                  AND COLUMN_NAME = 'business_code'
        ),
        'ALTER TABLE inbound_order DROP COLUMN business_code',
        'SELECT 1'
);
PREPARE stmt FROM @drop_influx_business_code_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @drop_influx_sub_business_code_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'inbound_order'
                  AND COLUMN_NAME = 'sub_business_code'
        ),
        'ALTER TABLE inbound_order DROP COLUMN sub_business_code',
        'SELECT 1'
);
PREPARE stmt FROM @drop_influx_sub_business_code_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @drop_influx_exchange_type_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'inbound_order'
                  AND COLUMN_NAME = 'exchange_type'
        ),
        'ALTER TABLE inbound_order DROP COLUMN exchange_type',
        'SELECT 1'
);
PREPARE stmt FROM @drop_influx_exchange_type_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @drop_influx_clear_channel_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'inbound_order'
                  AND COLUMN_NAME = 'clear_channel'
        ),
        'ALTER TABLE inbound_order DROP COLUMN clear_channel',
        'SELECT 1'
);
PREPARE stmt FROM @drop_influx_clear_channel_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @drop_outflux_business_code_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'outbound_order'
                  AND COLUMN_NAME = 'business_code'
        ),
        'ALTER TABLE outbound_order DROP COLUMN business_code',
        'SELECT 1'
);
PREPARE stmt FROM @drop_outflux_business_code_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @drop_outflux_sub_business_code_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'outbound_order'
                  AND COLUMN_NAME = 'sub_business_code'
        ),
        'ALTER TABLE outbound_order DROP COLUMN sub_business_code',
        'SELECT 1'
);
PREPARE stmt FROM @drop_outflux_sub_business_code_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @drop_outflux_exchange_type_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'outbound_order'
                  AND COLUMN_NAME = 'exchange_type'
        ),
        'ALTER TABLE outbound_order DROP COLUMN exchange_type',
        'SELECT 1'
);
PREPARE stmt FROM @drop_outflux_exchange_type_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @drop_outflux_clear_channel_sql = IF(
        EXISTS(
                SELECT 1
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'outbound_order'
                  AND COLUMN_NAME = 'clear_channel'
        ),
        'ALTER TABLE outbound_order DROP COLUMN clear_channel',
        'SELECT 1'
);
PREPARE stmt FROM @drop_outflux_clear_channel_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V83__normalize_user_account_kyc_level_to_l0.sql]
ALTER TABLE `user_account`
    MODIFY COLUMN `kyc_level` varchar(16) NOT NULL DEFAULT 'L0' COMMENT '实名认证等级：L0未实名，L1基础实名，L2增强实名，L3高等级实名';

-- [V84__optimize_trade_order_recent_user_flow_indexes.sql]
ALTER TABLE trade_order
    DROP INDEX idx_trade_order_payer_status,
    ADD KEY idx_trade_order_payer_status_updated_id (payer_user_id, status, updated_at, id),
    ADD KEY idx_trade_order_payee_status_updated_id (payee_user_id, status, updated_at, id);

-- [V85__add_async_pay_message_infra.sql]
CREATE TABLE IF NOT EXISTS async_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '系统内可靠异步消息主键ID',
    topic VARCHAR(64) NOT NULL COMMENT '消息主题',
    message_key VARCHAR(128) NOT NULL COMMENT '消息幂等键',
    payload TEXT NOT NULL COMMENT '消息载荷',
    status VARCHAR(32) NOT NULL COMMENT '消息状态：PENDING/PROCESSING/SUCCEEDED/DEAD',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '已重试次数',
    max_retry_count INT NOT NULL DEFAULT 16 COMMENT '最大重试次数',
    next_retry_at DATETIME NOT NULL COMMENT '下一次可执行时间',
    processing_started_at DATETIME NULL COMMENT '本次开始处理时间',
    last_error VARCHAR(255) NULL COMMENT '最后一次错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_async_message_topic_key (topic, message_key),
    KEY idx_async_message_claim (status, next_retry_at, id),
    KEY idx_async_message_processing (status, processing_started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统内可靠异步消息表';
ALTER TABLE pay_order
    ADD COLUMN source_biz_type VARCHAR(32) NULL COMMENT '来源业务类型：如 TRADE、MERCHANT_ORDER' AFTER merchant_order_no,
    ADD COLUMN source_biz_no VARCHAR(64) NULL COMMENT '来源业务单号' AFTER source_biz_type,
    ADD COLUMN settlement_plan_snapshot TEXT NULL COMMENT '结算计划快照载荷' AFTER coupon_no,
    ADD COLUMN status_version INT NOT NULL DEFAULT 0 COMMENT '支付状态版本号' AFTER status,
    ADD COLUMN result_code VARCHAR(64) NULL COMMENT '支付结果码' AFTER status_version,
    ADD COLUMN result_message VARCHAR(255) NULL COMMENT '支付结果描述' AFTER result_code;
ALTER TABLE pay_order
    MODIFY COLUMN source_biz_type VARCHAR(32) NOT NULL COMMENT '来源业务类型：如 TRADE、MERCHANT_ORDER',
    MODIFY COLUMN source_biz_no VARCHAR(64) NOT NULL COMMENT '来源业务单号',
    ADD UNIQUE KEY uk_pay_order_source_biz (source_biz_type, source_biz_no),
    ADD KEY idx_pay_order_status_version (status, status_version);
ALTER TABLE trade_order
    ADD COLUMN last_pay_status_version INT NOT NULL DEFAULT 0 COMMENT '最近一次已应用的支付状态版本号' AFTER pay_payment_id,
    ADD COLUMN pay_result_code VARCHAR(64) NULL COMMENT '支付结果码' AFTER last_pay_status_version,
    ADD COLUMN pay_result_message VARCHAR(255) NULL COMMENT '支付结果描述' AFTER pay_result_code;

-- [V86__add_pay_source_biz_snapshot.sql]
ALTER TABLE pay_order
    ADD COLUMN source_biz_snapshot TEXT NULL COMMENT '来源业务执行快照' AFTER source_biz_no;

-- [V87__add_pay_fund_and_credit_detail_tables.sql]
CREATE TABLE IF NOT EXISTS pay_fund_account_fund_detail (
    summary_id BIGINT PRIMARY KEY COMMENT '资金明细摘要ID',
    fund_code VARCHAR(64) NOT NULL COMMENT '基金编码',
    fund_product_code VARCHAR(64) NOT NULL COMMENT '基金产品编码',
    account_identity VARCHAR(128) NULL COMMENT '基金账户标识',
    KEY idx_pay_fund_account_fund_code (fund_code),
    KEY idx_pay_fund_account_product_code (fund_product_code),
    CONSTRAINT fk_pay_fund_account_fund_detail_summary
        FOREIGN KEY (summary_id) REFERENCES pay_fund_detail_summary (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基金账户资金明细表';
CREATE TABLE IF NOT EXISTS pay_credit_account_fund_detail (
    summary_id BIGINT PRIMARY KEY COMMENT '资金明细摘要ID',
    account_no VARCHAR(64) NOT NULL COMMENT '信用账户号',
    credit_account_type VARCHAR(32) NOT NULL COMMENT '信用账户类型',
    credit_product_code VARCHAR(32) NOT NULL COMMENT '信用产品编码',
    KEY idx_pay_credit_account_no (account_no),
    KEY idx_pay_credit_account_type (credit_account_type),
    CONSTRAINT fk_pay_credit_account_fund_detail_summary
        FOREIGN KEY (summary_id) REFERENCES pay_fund_detail_summary (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用账户资金明细表';

-- [V89__replace_pay_order_flat_amounts_with_split_plan_snapshot.sql]
ALTER TABLE pay_order
    ADD COLUMN split_plan_snapshot TEXT NULL COMMENT '扣款拆分计划快照' AFTER actual_paid_amount;
ALTER TABLE pay_order
    MODIFY COLUMN split_plan_snapshot TEXT NOT NULL COMMENT '扣款拆分计划快照',
    DROP COLUMN wallet_debit_amount,
    DROP COLUMN fund_debit_amount,
    DROP COLUMN credit_debit_amount,
    DROP COLUMN influx_debit_amount;

-- [V90__replace_trade_order_flat_amounts_with_split_plan_snapshot.sql]
ALTER TABLE trade_order
    ADD COLUMN split_plan_snapshot TEXT NULL COMMENT '交易查询侧支付拆分快照' AFTER settle_amount;
ALTER TABLE trade_order
    MODIFY COLUMN split_plan_snapshot TEXT NOT NULL COMMENT '交易查询侧支付拆分快照';
ALTER TABLE trade_order
    DROP COLUMN wallet_debit_amount,
    DROP COLUMN fund_debit_amount,
    DROP COLUMN credit_debit_amount,
    DROP COLUMN influx_debit_amount;

-- [V91__create_accounting_tables_and_admin_menu.sql]
CREATE TABLE IF NOT EXISTS acct_subject (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    subject_code VARCHAR(64) NOT NULL COMMENT '科目编码',
    subject_name VARCHAR(128) NOT NULL COMMENT '科目名称',
    subject_type VARCHAR(32) NOT NULL COMMENT '科目类型',
    balance_direction VARCHAR(16) NOT NULL COMMENT '余额方向',
    parent_subject_code VARCHAR(64) DEFAULT NULL COMMENT '父级科目编码',
    level_no INT NOT NULL DEFAULT 1 COMMENT '层级',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_acct_subject_code (subject_code),
    KEY idx_acct_subject_parent (parent_subject_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计科目表';
CREATE TABLE IF NOT EXISTS acct_event_journal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    event_id VARCHAR(64) NOT NULL COMMENT '事件ID',
    event_type VARCHAR(64) NOT NULL COMMENT '事件类型',
    event_version INT NOT NULL DEFAULT 1 COMMENT '事件版本',
    book_id VARCHAR(64) NOT NULL COMMENT '账簿号',
    source_system VARCHAR(32) NOT NULL COMMENT '来源系统',
    source_biz_type VARCHAR(64) NOT NULL COMMENT '来源业务类型',
    source_biz_no VARCHAR(64) NOT NULL COMMENT '来源业务号',
    request_no VARCHAR(64) DEFAULT NULL COMMENT '请求幂等号',
    trade_no VARCHAR(64) DEFAULT NULL COMMENT '交易号',
    payment_id VARCHAR(64) DEFAULT NULL COMMENT '支付号',
    business_scene_code VARCHAR(64) DEFAULT NULL COMMENT '业务场景码',
    business_domain_code VARCHAR(64) DEFAULT NULL COMMENT '业务域码',
    payer_user_id BIGINT DEFAULT NULL COMMENT '付款方用户ID',
    payee_user_id BIGINT DEFAULT NULL COMMENT '收款方用户ID',
    currency_code VARCHAR(8) NOT NULL COMMENT '币种',
    occurred_at DATETIME(3) NOT NULL COMMENT '业务发生时间',
    posting_date DATE NOT NULL COMMENT '会计日',
    idempotency_key VARCHAR(128) NOT NULL COMMENT '幂等键',
    trace_id VARCHAR(64) DEFAULT NULL COMMENT '链路追踪号',
    payload_json LONGTEXT DEFAULT NULL COMMENT '业务扩展载荷',
    legs_json LONGTEXT NOT NULL COMMENT '资金腿快照',
    process_status VARCHAR(32) NOT NULL COMMENT '处理状态',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    failure_reason VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    posted_voucher_no VARCHAR(64) DEFAULT NULL COMMENT '已生成凭证号',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_acct_event_id (event_id),
    UNIQUE KEY uk_acct_event_idempotency (idempotency_key),
    KEY idx_acct_event_source (source_biz_type, source_biz_no),
    KEY idx_acct_event_trade (trade_no),
    KEY idx_acct_event_payment (payment_id),
    KEY idx_acct_event_status (process_status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计事件日志表';
CREATE TABLE IF NOT EXISTS acct_voucher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    voucher_no VARCHAR(64) NOT NULL COMMENT '凭证号',
    book_id VARCHAR(64) NOT NULL COMMENT '账簿号',
    voucher_type VARCHAR(32) NOT NULL COMMENT '凭证类型',
    event_id VARCHAR(64) NOT NULL COMMENT '事件ID',
    source_biz_type VARCHAR(64) NOT NULL COMMENT '来源业务类型',
    source_biz_no VARCHAR(64) NOT NULL COMMENT '来源业务号',
    trade_no VARCHAR(64) DEFAULT NULL COMMENT '交易号',
    payment_id VARCHAR(64) DEFAULT NULL COMMENT '支付号',
    business_scene_code VARCHAR(64) DEFAULT NULL COMMENT '业务场景码',
    business_domain_code VARCHAR(64) DEFAULT NULL COMMENT '业务域码',
    status VARCHAR(32) NOT NULL COMMENT '凭证状态',
    currency_code VARCHAR(8) NOT NULL COMMENT '币种',
    total_debit_amount DECIMAL(18,2) NOT NULL COMMENT '借方合计',
    total_credit_amount DECIMAL(18,2) NOT NULL COMMENT '贷方合计',
    occurred_at DATETIME(3) NOT NULL COMMENT '业务发生时间',
    posting_date DATE NOT NULL COMMENT '会计日',
    reversed_voucher_no VARCHAR(64) DEFAULT NULL COMMENT '被冲正凭证号',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_acct_voucher_no (voucher_no),
    UNIQUE KEY uk_acct_voucher_event (event_id),
    KEY idx_acct_voucher_source (source_biz_type, source_biz_no),
    KEY idx_acct_voucher_trade (trade_no),
    KEY idx_acct_voucher_payment (payment_id),
    KEY idx_acct_voucher_posting_date (posting_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计凭证表';
CREATE TABLE IF NOT EXISTS acct_entry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    voucher_no VARCHAR(64) NOT NULL COMMENT '凭证号',
    line_no INT NOT NULL COMMENT '行号',
    subject_code VARCHAR(64) NOT NULL COMMENT '科目编码',
    dc_flag VARCHAR(16) NOT NULL COMMENT '借贷方向',
    amount DECIMAL(18,2) NOT NULL COMMENT '金额',
    currency_code VARCHAR(8) NOT NULL COMMENT '币种',
    owner_type VARCHAR(32) DEFAULT NULL COMMENT '主体类型',
    owner_id BIGINT DEFAULT NULL COMMENT '主体ID',
    account_domain VARCHAR(32) DEFAULT NULL COMMENT '账户域',
    account_type VARCHAR(64) DEFAULT NULL COMMENT '账户类型',
    account_no VARCHAR(64) DEFAULT NULL COMMENT '账户号',
    biz_role VARCHAR(32) DEFAULT NULL COMMENT '业务角色',
    trade_no VARCHAR(64) DEFAULT NULL COMMENT '交易号',
    payment_id VARCHAR(64) DEFAULT NULL COMMENT '支付号',
    source_biz_type VARCHAR(64) NOT NULL COMMENT '来源业务类型',
    source_biz_no VARCHAR(64) NOT NULL COMMENT '来源业务号',
    reference_no VARCHAR(64) DEFAULT NULL COMMENT '引用流水号',
    entry_memo VARCHAR(255) DEFAULT NULL COMMENT '分录摘要',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_acct_entry_line (voucher_no, line_no),
    KEY idx_acct_entry_subject (subject_code),
    KEY idx_acct_entry_owner (owner_type, owner_id),
    KEY idx_acct_entry_trade (trade_no),
    KEY idx_acct_entry_payment (payment_id),
    KEY idx_acct_entry_source (source_biz_type, source_biz_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计分录表';

-- [V93__add_global_tx_id_to_accounting_event_journal.sql]
SET @add_acct_event_global_tx_id_sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'acct_event_journal'
              AND COLUMN_NAME = 'idempotency_key'
        )
        AND NOT EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'acct_event_journal'
              AND COLUMN_NAME = 'global_tx_id'
        ),
        'ALTER TABLE acct_event_journal ADD COLUMN global_tx_id VARCHAR(128) DEFAULT NULL COMMENT ''支付全局事务号'' AFTER idempotency_key',
        'SELECT 1'
    )
);
PREPARE stmt FROM @add_acct_event_global_tx_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V95__create_message_red_packet_order_table.sql]
CREATE TABLE IF NOT EXISTS message_red_packet_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    red_packet_no VARCHAR(64) NOT NULL COMMENT '红包单号',
    message_id VARCHAR(64) NOT NULL COMMENT '关联消息ID',
    conversation_no VARCHAR(64) NOT NULL COMMENT '关联会话号',
    sender_user_id BIGINT NOT NULL COMMENT '发红包用户ID',
    receiver_user_id BIGINT NOT NULL COMMENT '收红包用户ID',
    holding_user_id BIGINT NOT NULL COMMENT '红包中间账户用户ID',
    amount DECIMAL(18, 2) NOT NULL COMMENT '红包金额',
    currency_code VARCHAR(16) NOT NULL COMMENT '币种编码',
    funding_trade_no VARCHAR(64) NOT NULL COMMENT '发出资金交易号',
    claim_trade_no VARCHAR(64) NULL COMMENT '领取资金交易号',
    payment_method VARCHAR(64) NOT NULL COMMENT '发红包付款方式',
    cover_id VARCHAR(64) NULL COMMENT '红包封面ID',
    cover_title VARCHAR(128) NULL COMMENT '红包封面标题',
    blessing_text VARCHAR(255) NULL COMMENT '红包祝福语',
    status VARCHAR(32) NOT NULL COMMENT '红包状态',
    claimed_at DATETIME NULL COMMENT '领取时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_message_red_packet_order_no (red_packet_no),
    UNIQUE KEY uk_message_red_packet_order_message_id (message_id),
    KEY idx_message_red_packet_sender_created (sender_user_id, created_at DESC),
    KEY idx_message_red_packet_receiver_created (receiver_user_id, created_at DESC),
    KEY idx_message_red_packet_conversation (conversation_no),
    KEY idx_message_red_packet_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天红包订单表';

-- [V100__support_dual_side_trade_bill_index.sql]
-- 支持同一笔转账按“交易+用户”双边入账单索引，确保收款方也能在账单中看到记录。
ALTER TABLE trade_bill_index
    DROP INDEX uk_trade_business_index_trade_order_no,
    DROP INDEX uk_trade_business_index_biz_order;
ALTER TABLE trade_bill_index
    ADD UNIQUE KEY uk_trade_business_index_trade_user (trade_order_no, user_id),
    ADD UNIQUE KEY uk_trade_business_index_biz_user (business_domain_code, biz_order_no, user_id);

-- [V108__create_agreement_and_user_feature_tables.sql]
CREATE TABLE IF NOT EXISTS agreement_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_code VARCHAR(64) NOT NULL,
    template_version VARCHAR(32) NOT NULL,
    biz_type VARCHAR(64) NOT NULL,
    title VARCHAR(256) NOT NULL,
    content_url VARCHAR(512) NOT NULL,
    content_hash VARCHAR(128) NULL,
    required_flag TINYINT(1) NOT NULL DEFAULT 1,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agreement_template_code_version (template_code, template_version),
    KEY idx_agreement_template_biz_status (biz_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS agreement_sign_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sign_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    biz_type VARCHAR(64) NOT NULL,
    fund_code VARCHAR(32) NULL,
    currency_code VARCHAR(8) NULL,
    idempotency_key VARCHAR(64) NOT NULL,
    sign_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    signed_at DATETIME NULL,
    opened_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agreement_sign_record_sign_no (sign_no),
    UNIQUE KEY uk_agreement_sign_record_idempotency (user_id, biz_type, idempotency_key),
    KEY idx_agreement_sign_record_user_status (user_id, sign_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS agreement_sign_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sign_no VARCHAR(64) NOT NULL,
    template_code VARCHAR(64) NOT NULL,
    template_version VARCHAR(32) NOT NULL,
    title VARCHAR(256) NOT NULL,
    content_url VARCHAR(512) NOT NULL,
    content_hash VARCHAR(128) NULL,
    accepted TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agreement_sign_item_unique (sign_no, template_code, template_version),
    KEY idx_agreement_sign_item_sign_no (sign_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS user_feature_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    feature_code VARCHAR(64) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    opened_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_feature_status_user_feature (user_id, feature_code),
    KEY idx_user_feature_status_feature (feature_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [V109__extend_app_device_login_user_snapshot.sql]
ALTER TABLE app_device
    ADD COLUMN user_id BIGINT NULL COMMENT '当前登录用户ID' AFTER os_version,
    ADD COLUMN aipay_uid VARCHAR(64) NULL COMMENT '当前登录爱付号' AFTER user_id,
    ADD COLUMN login_id VARCHAR(128) NULL COMMENT '当前登录账号' AFTER aipay_uid,
    ADD COLUMN account_status VARCHAR(32) NULL COMMENT '账户状态' AFTER login_id,
    ADD COLUMN kyc_level VARCHAR(16) NULL COMMENT '实名等级' AFTER account_status,
    ADD COLUMN nickname VARCHAR(64) NULL COMMENT '昵称' AFTER kyc_level,
    ADD COLUMN avatar_url VARCHAR(512) NULL COMMENT '头像地址' AFTER nickname,
    ADD COLUMN mobile VARCHAR(32) NULL COMMENT '手机号' AFTER avatar_url,
    ADD COLUMN masked_real_name VARCHAR(64) NULL COMMENT '脱敏姓名' AFTER mobile,
    ADD COLUMN id_card_no_masked VARCHAR(64) NULL COMMENT '脱敏证件号' AFTER masked_real_name,
    ADD COLUMN country_code VARCHAR(8) NULL COMMENT '国家编码' AFTER id_card_no_masked,
    ADD COLUMN gender VARCHAR(16) NULL COMMENT '性别' AFTER country_code,
    ADD COLUMN region VARCHAR(64) NULL COMMENT '地区' AFTER gender,
    ADD COLUMN last_login_at DATETIME NULL COMMENT '最近登录时间' AFTER region,
    ADD KEY idx_app_device_user_last_login (user_id, last_login_at);

-- [V112__create_loan_trade_order_table.sql]
CREATE TABLE IF NOT EXISTS loan_trade_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '借呗交易单主键ID',
    xid VARCHAR(128) NOT NULL COMMENT '全局事务XID，来自支付链路',
    branch_id VARCHAR(64) NOT NULL COMMENT '父分支ID，对应 loanAccount 接收到的分支号',
    business_no VARCHAR(64) NULL COMMENT '业务流水号，通常为 paymentId',
    account_no VARCHAR(32) NOT NULL COMMENT '借呗账户号',
    operation_type VARCHAR(16) NOT NULL COMMENT '操作类型：LEND/REPAY',
    status VARCHAR(16) NOT NULL COMMENT '状态机：TRIED/CONFIRMED/CANCELED',
    currency_code VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '币种编码',
    request_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '请求金额',
    interest_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '应还利息分摊金额',
    principal_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '应还本金分摊金额',
    fine_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '应还罚息分摊金额',
    interest_branch_id VARCHAR(96) NULL COMMENT '利息子分支ID',
    principal_branch_id VARCHAR(96) NULL COMMENT '本金子分支ID',
    fine_branch_id VARCHAR(96) NULL COMMENT '罚息子分支ID',
    annual_rate_percent DECIMAL(10,4) NOT NULL DEFAULT 3.2400 COMMENT '年化利率（百分数）',
    remaining_term_months INT NOT NULL DEFAULT 24 COMMENT '剩余期数（月）',
    monthly_payment DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '保持期数后重算月供',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_loan_trade_xid_branch (xid, branch_id),
    KEY idx_loan_trade_account_status_time (account_no, status, updated_at),
    KEY idx_loan_trade_business_no (business_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借呗交易单：承接借呗还款拆分与状态机';

-- [V113__rename_finance_exchange_code_to_inst_channel_code.sql]
-- Rename legacy finance_exchange_code to inst_channel_code on existing runtime tables.
-- Keep migration idempotent so repeated executions or already-updated environments remain safe.

SET @sql = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND COLUMN_NAME = 'finance_exchange_code'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND COLUMN_NAME = 'inst_channel_code'
               )
                   THEN 'ALTER TABLE inbound_order CHANGE COLUMN finance_exchange_code inst_channel_code VARCHAR(64) NULL COMMENT ''机构渠道编码'''
               ELSE 'SELECT ''skip inbound_order column rename'''
        END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND COLUMN_NAME = 'finance_exchange_code'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND COLUMN_NAME = 'inst_channel_code'
               )
                   THEN 'ALTER TABLE outbound_order CHANGE COLUMN finance_exchange_code inst_channel_code VARCHAR(64) NULL COMMENT ''机构渠道编码'''
               ELSE 'SELECT ''skip outbound_order column rename'''
        END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V114__rename_channel_and_order_no_fields.sql]
-- Align influx/outflux runtime table field names with current domain naming.
-- Keep the migration idempotent so already-updated environments can run safely.

SET @sql = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'pay_channel_api'
               ) AND NOT EXISTS (
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'pay_channel_code'
               )
                   THEN 'ALTER TABLE inbound_order CHANGE COLUMN pay_channel_api pay_channel_code VARCHAR(64) NOT NULL COMMENT ''支付渠道编码'''
               ELSE 'SELECT ''skip inbound_order pay_channel rename'''
        END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'settle_serial_no'
               ) AND NOT EXISTS (
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'influx_order_no'
               )
                   THEN 'ALTER TABLE inbound_order CHANGE COLUMN settle_serial_no influx_order_no VARCHAR(64) NULL COMMENT ''入金单号'''
               ELSE 'SELECT ''skip influx_order_no rename'''
        END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM information_schema.TABLES
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'influx_inst'
               ) AND EXISTS (
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'influx_inst' AND COLUMN_NAME = 'settle_serial_no'
               ) AND NOT EXISTS (
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'influx_inst' AND COLUMN_NAME = 'influx_order_no'
               )
                   THEN 'ALTER TABLE influx_inst CHANGE COLUMN settle_serial_no influx_order_no VARCHAR(64) NULL COMMENT ''入金单号'''
               ELSE 'SELECT ''skip influx_inst order_no rename'''
        END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM information_schema.TABLES
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'influx_inst'
               ) AND EXISTS (
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'influx_inst' AND COLUMN_NAME = 'inst_channel_api'
               )
                   THEN 'ALTER TABLE influx_inst DROP COLUMN inst_channel_api'
               ELSE 'SELECT ''skip influx_inst inst_channel_api drop'''
        END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'inst_channel_api'
               )
                   THEN 'ALTER TABLE inbound_order DROP COLUMN inst_channel_api'
               ELSE 'SELECT ''skip inbound_order inst_channel_api drop'''
        END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'pay_channel_api'
               ) AND NOT EXISTS (
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'pay_channel_code'
               )
                   THEN 'ALTER TABLE outbound_order CHANGE COLUMN pay_channel_api pay_channel_code VARCHAR(64) NOT NULL COMMENT ''支付渠道编码'''
               ELSE 'SELECT ''skip outbound_order pay_channel rename'''
        END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'settle_serial_no'
               ) AND NOT EXISTS (
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'outflux_order_no'
               )
                   THEN 'ALTER TABLE outbound_order CHANGE COLUMN settle_serial_no outflux_order_no VARCHAR(64) NULL COMMENT ''出金单号'''
               ELSE 'SELECT ''skip outflux_order_no rename'''
        END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'inst_channel_api'
               )
                   THEN 'ALTER TABLE outbound_order DROP COLUMN inst_channel_api'
               ELSE 'SELECT ''skip outbound_order inst_channel_api drop'''
        END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V115__rename_inbound_outbound_to_inbound_outbound.sql]
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND COLUMN_NAME = 'influx_id'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND COLUMN_NAME = 'inbound_id'
               )
                   THEN 'ALTER TABLE inbound_order CHANGE COLUMN influx_id inbound_id VARCHAR(64) NOT NULL COMMENT ''入金订单号'''
               ELSE 'SELECT ''skip rename inbound_id column'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND COLUMN_NAME = 'influx_order_no'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND COLUMN_NAME = 'inbound_order_no'
               )
                   THEN 'ALTER TABLE inbound_order CHANGE COLUMN influx_order_no inbound_order_no VARCHAR(64) NULL COMMENT ''入金单号'''
               ELSE 'SELECT ''skip rename inbound_order_no column'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND COLUMN_NAME = 'org_influx_id'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND COLUMN_NAME = 'org_inbound_id'
               )
                   THEN 'ALTER TABLE inbound_order CHANGE COLUMN org_influx_id org_inbound_id VARCHAR(64) NULL COMMENT ''原入金订单号'''
               ELSE 'SELECT ''skip rename org_inbound_id column'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND COLUMN_NAME = 'outflux_id'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND COLUMN_NAME = 'outbound_id'
               )
                   THEN 'ALTER TABLE outbound_order CHANGE COLUMN outflux_id outbound_id VARCHAR(64) NOT NULL COMMENT ''出金订单号'''
               ELSE 'SELECT ''skip rename outbound_id column'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND COLUMN_NAME = 'outflux_order_no'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND COLUMN_NAME = 'outbound_order_no'
               )
                   THEN 'ALTER TABLE outbound_order CHANGE COLUMN outflux_order_no outbound_order_no VARCHAR(64) NULL COMMENT ''出金单号'''
               ELSE 'SELECT ''skip rename outbound_order_no column'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND COLUMN_NAME = 'org_outflux_id'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND COLUMN_NAME = 'org_outbound_id'
               )
                   THEN 'ALTER TABLE outbound_order CHANGE COLUMN org_outflux_id org_outbound_id VARCHAR(64) NULL COMMENT ''原出金订单号'''
               ELSE 'SELECT ''skip rename org_outbound_id column'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'uk_fin_influx_influx_id'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'uk_fin_inbound_inbound_id'
               )
                   THEN 'ALTER TABLE inbound_order RENAME INDEX uk_fin_influx_influx_id TO uk_fin_inbound_inbound_id'
               ELSE 'SELECT ''skip rename uk_fin_influx_influx_id'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'uk_fin_influx_request_biz_no'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'uk_fin_inbound_request_biz_no'
               )
                   THEN 'ALTER TABLE inbound_order RENAME INDEX uk_fin_influx_request_biz_no TO uk_fin_inbound_request_biz_no'
               ELSE 'SELECT ''skip rename uk_fin_influx_request_biz_no'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'uk_fin_influx_pay_unique_no'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'uk_fin_inbound_pay_unique_no'
               )
                   THEN 'ALTER TABLE inbound_order RENAME INDEX uk_fin_influx_pay_unique_no TO uk_fin_inbound_pay_unique_no'
               ELSE 'SELECT ''skip rename uk_fin_influx_pay_unique_no'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'idx_fin_influx_exchange_status'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'idx_fin_inbound_exchange_status'
               )
                   THEN 'ALTER TABLE inbound_order RENAME INDEX idx_fin_influx_exchange_status TO idx_fin_inbound_exchange_status'
               ELSE 'SELECT ''skip rename idx_fin_influx_exchange_status'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'idx_fin_influx_settle_status'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'idx_fin_inbound_settle_status'
               )
                   THEN 'ALTER TABLE inbound_order RENAME INDEX idx_fin_influx_settle_status TO idx_fin_inbound_settle_status'
               ELSE 'SELECT ''skip rename idx_fin_influx_settle_status'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'idx_fin_influx_submit_time'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'idx_fin_inbound_submit_time'
               )
                   THEN 'ALTER TABLE inbound_order RENAME INDEX idx_fin_influx_submit_time TO idx_fin_inbound_submit_time'
               ELSE 'SELECT ''skip rename idx_fin_influx_submit_time'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND INDEX_NAME = 'uk_outflux_order_outflux_id'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND INDEX_NAME = 'uk_outbound_order_outbound_id'
               )
                   THEN 'ALTER TABLE outbound_order RENAME INDEX uk_outflux_order_outflux_id TO uk_outbound_order_outbound_id'
               ELSE 'SELECT ''skip rename uk_outflux_order_outflux_id'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND INDEX_NAME = 'uk_outflux_order_request_biz_no'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND INDEX_NAME = 'uk_outbound_order_request_biz_no'
               )
                   THEN 'ALTER TABLE outbound_order RENAME INDEX uk_outflux_order_request_biz_no TO uk_outbound_order_request_biz_no'
               ELSE 'SELECT ''skip rename uk_outflux_order_request_biz_no'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND INDEX_NAME = 'uk_outflux_order_pay_unique_no'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND INDEX_NAME = 'uk_outbound_order_pay_unique_no'
               )
                   THEN 'ALTER TABLE outbound_order RENAME INDEX uk_outflux_order_pay_unique_no TO uk_outbound_order_pay_unique_no'
               ELSE 'SELECT ''skip rename uk_outflux_order_pay_unique_no'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND INDEX_NAME = 'idx_outflux_order_exchange_status'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND INDEX_NAME = 'idx_outbound_order_exchange_status'
               )
                   THEN 'ALTER TABLE outbound_order RENAME INDEX idx_outflux_order_exchange_status TO idx_outbound_order_exchange_status'
               ELSE 'SELECT ''skip rename idx_outflux_order_exchange_status'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND INDEX_NAME = 'idx_outflux_order_submit_time'
               ) AND NOT EXISTS(
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND INDEX_NAME = 'idx_outbound_order_submit_time'
               )
                   THEN 'ALTER TABLE outbound_order RENAME INDEX idx_outflux_order_submit_time TO idx_outbound_order_submit_time'
               ELSE 'SELECT ''skip rename idx_outflux_order_submit_time'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V116__rename_order_number_and_amount_fields.sql]
-- Align inbound/outbound/pay/trade runtime field naming with latest domain terms.
-- This migration is idempotent for environments that have already partially migrated.

SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'inbound_order_no'
               )
                   THEN 'ALTER TABLE inbound_order MODIFY COLUMN inbound_order_no VARCHAR(64) NULL COMMENT ''入金订单号'''
               ELSE 'SELECT ''skip inbound_order_no comment'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'outbound_order_no'
               )
                   THEN 'ALTER TABLE outbound_order MODIFY COLUMN outbound_order_no VARCHAR(64) NULL COMMENT ''出金订单号'''
               ELSE 'SELECT ''skip outbound_order_no comment'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'exchange_amount'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'inbound_amount'
               )
                   THEN 'ALTER TABLE inbound_order CHANGE COLUMN exchange_amount inbound_amount DECIMAL(18,2) NOT NULL COMMENT ''入金金额'''
               ELSE 'SELECT ''skip inbound_amount rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'exchange_currency'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'inbound_currency'
               )
                   THEN 'ALTER TABLE inbound_order CHANGE COLUMN exchange_currency inbound_currency VARCHAR(8) NOT NULL COMMENT ''入金币种'''
               ELSE 'SELECT ''skip inbound_currency rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'exchange_status'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'inbound_status'
               )
                   THEN 'ALTER TABLE inbound_order CHANGE COLUMN exchange_status inbound_status VARCHAR(32) NOT NULL COMMENT ''入金状态'''
               ELSE 'SELECT ''skip inbound_status rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'exchange_amount'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'outbound_amount'
               )
                   THEN 'ALTER TABLE outbound_order CHANGE COLUMN exchange_amount outbound_amount DECIMAL(18,2) NOT NULL COMMENT ''出金金额'''
               ELSE 'SELECT ''skip outbound_amount rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'exchange_currency'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'outbound_currency'
               )
                   THEN 'ALTER TABLE outbound_order CHANGE COLUMN exchange_currency outbound_currency VARCHAR(8) NOT NULL COMMENT ''出金币种'''
               ELSE 'SELECT ''skip outbound_currency rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'exchange_status'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'outbound_status'
               )
                   THEN 'ALTER TABLE outbound_order CHANGE COLUMN exchange_status outbound_status VARCHAR(32) NOT NULL COMMENT ''出金状态'''
               ELSE 'SELECT ''skip outbound_status rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_order' AND COLUMN_NAME = 'payment_id'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_order' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE pay_order CHANGE COLUMN payment_id pay_order_no VARCHAR(64) NOT NULL COMMENT ''支付订单号'''
               ELSE 'SELECT ''skip pay_order pay_order_no rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_participant_branch' AND COLUMN_NAME = 'payment_id'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_participant_branch' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE pay_participant_branch CHANGE COLUMN payment_id pay_order_no VARCHAR(64) NOT NULL COMMENT ''关联支付订单号'''
               ELSE 'SELECT ''skip pay_participant_branch pay_order_no rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_fund_detail_summary' AND COLUMN_NAME = 'payment_id'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_fund_detail_summary' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE pay_fund_detail_summary CHANGE COLUMN payment_id pay_order_no VARCHAR(64) NOT NULL COMMENT ''支付订单号'''
               ELSE 'SELECT ''skip pay_fund_detail_summary pay_order_no rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'trade_no'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'ALTER TABLE trade_order CHANGE COLUMN trade_no trade_order_no VARCHAR(64) NOT NULL COMMENT ''交易订单号，系统内唯一'''
               ELSE 'SELECT ''skip trade_order trade_order_no rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'original_trade_no'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'original_trade_order_no'
               )
                   THEN 'ALTER TABLE trade_order CHANGE COLUMN original_trade_no original_trade_order_no VARCHAR(64) NULL COMMENT ''原交易订单号，退款场景使用'''
               ELSE 'SELECT ''skip trade_order original_trade_order_no rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'pay_payment_id'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE trade_order CHANGE COLUMN pay_payment_id pay_order_no VARCHAR(64) NULL COMMENT ''支付模块支付订单号'''
               ELSE 'SELECT ''skip trade_order pay_order_no rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_flow_step' AND COLUMN_NAME = 'trade_no'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_flow_step' AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'ALTER TABLE trade_flow_step CHANGE COLUMN trade_no trade_order_no VARCHAR(64) NOT NULL COMMENT ''关联交易订单号'''
               ELSE 'SELECT ''skip trade_flow_step trade_order_no rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_credit_order' AND COLUMN_NAME = 'trade_no'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_credit_order' AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'ALTER TABLE trade_credit_order CHANGE COLUMN trade_no trade_order_no VARCHAR(64) NOT NULL COMMENT ''关联统一交易订单号'''
               ELSE 'SELECT ''skip trade_credit_order trade_order_no rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_fund_order' AND COLUMN_NAME = 'trade_no'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_fund_order' AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'ALTER TABLE trade_fund_order CHANGE COLUMN trade_no trade_order_no VARCHAR(64) NOT NULL COMMENT ''关联统一交易订单号'''
               ELSE 'SELECT ''skip trade_fund_order trade_order_no rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_bill_index' AND COLUMN_NAME = 'trade_no'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_bill_index' AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'ALTER TABLE trade_bill_index CHANGE COLUMN trade_no trade_order_no VARCHAR(64) NOT NULL COMMENT ''统一交易订单号：关联 trade_order'''
               ELSE 'SELECT ''skip trade_bill_index trade_order_no rename'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND INDEX_NAME = 'idx_fin_inbound_exchange_status'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND INDEX_NAME = 'idx_fin_inbound_status'
               )
                   THEN 'ALTER TABLE inbound_order RENAME INDEX idx_fin_inbound_exchange_status TO idx_fin_inbound_status'
               ELSE 'SELECT ''skip rename idx_fin_inbound_status'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND INDEX_NAME = 'idx_outbound_order_exchange_status'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND INDEX_NAME = 'idx_outbound_order_status'
               )
                   THEN 'ALTER TABLE outbound_order RENAME INDEX idx_outbound_order_exchange_status TO idx_outbound_order_status'
               ELSE 'SELECT ''skip rename idx_outbound_order_status'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_order' AND INDEX_NAME = 'uk_pay_order_payment_id'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_order' AND INDEX_NAME = 'uk_pay_order_pay_order_no'
               )
                   THEN 'ALTER TABLE pay_order RENAME INDEX uk_pay_order_payment_id TO uk_pay_order_pay_order_no'
               ELSE 'SELECT ''skip rename uk_pay_order_pay_order_no'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_participant_branch' AND INDEX_NAME = 'uk_pay_branch_payment_participant'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_participant_branch' AND INDEX_NAME = 'uk_pay_branch_pay_order_participant'
               )
                   THEN 'ALTER TABLE pay_participant_branch RENAME INDEX uk_pay_branch_payment_participant TO uk_pay_branch_pay_order_participant'
               ELSE 'SELECT ''skip rename uk_pay_branch_pay_order_participant'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_participant_branch' AND INDEX_NAME = 'uk_pay_branch_payment_branch_id'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_participant_branch' AND INDEX_NAME = 'uk_pay_branch_pay_order_branch_id'
               )
                   THEN 'ALTER TABLE pay_participant_branch RENAME INDEX uk_pay_branch_payment_branch_id TO uk_pay_branch_pay_order_branch_id'
               ELSE 'SELECT ''skip rename uk_pay_branch_pay_order_branch_id'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_participant_branch' AND INDEX_NAME = 'idx_pay_branch_payment_status'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_participant_branch' AND INDEX_NAME = 'idx_pay_branch_pay_order_status'
               )
                   THEN 'ALTER TABLE pay_participant_branch RENAME INDEX idx_pay_branch_payment_status TO idx_pay_branch_pay_order_status'
               ELSE 'SELECT ''skip rename idx_pay_branch_pay_order_status'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_fund_detail_summary' AND INDEX_NAME = 'uk_pay_fund_detail_payment_tool_owner'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_fund_detail_summary' AND INDEX_NAME = 'uk_pay_fund_detail_pay_order_tool_owner'
               )
                   THEN 'ALTER TABLE pay_fund_detail_summary RENAME INDEX uk_pay_fund_detail_payment_tool_owner TO uk_pay_fund_detail_pay_order_tool_owner'
               ELSE 'SELECT ''skip rename uk_pay_fund_detail_pay_order_tool_owner'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_fund_detail_summary' AND INDEX_NAME = 'idx_pay_fund_detail_payment_id'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_fund_detail_summary' AND INDEX_NAME = 'idx_pay_fund_detail_pay_order_no'
               )
                   THEN 'ALTER TABLE pay_fund_detail_summary RENAME INDEX idx_pay_fund_detail_payment_id TO idx_pay_fund_detail_pay_order_no'
               ELSE 'SELECT ''skip rename idx_pay_fund_detail_pay_order_no'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND INDEX_NAME = 'uk_trade_order_trade_no'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND INDEX_NAME = 'uk_trade_order_trade_order_no'
               )
                   THEN 'ALTER TABLE trade_order RENAME INDEX uk_trade_order_trade_no TO uk_trade_order_trade_order_no'
               ELSE 'SELECT ''skip rename uk_trade_order_trade_order_no'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND INDEX_NAME = 'idx_trade_order_original_status'
               )
                   THEN 'ALTER TABLE trade_order DROP INDEX idx_trade_order_original_status, ADD KEY idx_trade_order_original_order_status (original_trade_order_no, status)'
               ELSE 'SELECT ''skip rebuild idx_trade_order_original_order_status'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_flow_step' AND INDEX_NAME = 'idx_trade_flow_trade_no'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_flow_step' AND INDEX_NAME = 'idx_trade_flow_trade_order_no'
               )
                   THEN 'ALTER TABLE trade_flow_step RENAME INDEX idx_trade_flow_trade_no TO idx_trade_flow_trade_order_no'
               ELSE 'SELECT ''skip rename idx_trade_flow_trade_order_no'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_flow_step' AND INDEX_NAME = 'idx_trade_flow_trade_step'
               )
                   THEN 'ALTER TABLE trade_flow_step DROP INDEX idx_trade_flow_trade_step, ADD KEY idx_trade_flow_trade_order_step (trade_order_no, step_code)'
               ELSE 'SELECT ''skip rebuild idx_trade_flow_trade_order_step'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_credit_order' AND INDEX_NAME = 'uk_trade_credit_order_trade_no'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_credit_order' AND INDEX_NAME = 'uk_trade_credit_order_trade_order_no'
               )
                   THEN 'ALTER TABLE trade_credit_order RENAME INDEX uk_trade_credit_order_trade_no TO uk_trade_credit_order_trade_order_no'
               ELSE 'SELECT ''skip rename uk_trade_credit_order_trade_order_no'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_fund_order' AND INDEX_NAME = 'uk_trade_fund_order_trade_no'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_fund_order' AND INDEX_NAME = 'uk_trade_fund_order_trade_order_no'
               )
                   THEN 'ALTER TABLE trade_fund_order RENAME INDEX uk_trade_fund_order_trade_no TO uk_trade_fund_order_trade_order_no'
               ELSE 'SELECT ''skip rename uk_trade_fund_order_trade_order_no'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (
    SELECT CASE
               WHEN EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_bill_index' AND INDEX_NAME = 'uk_trade_business_index_trade_no'
               ) AND NOT EXISTS(
                   SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_bill_index' AND INDEX_NAME = 'uk_trade_business_index_trade_order_no'
               )
                   THEN 'ALTER TABLE trade_bill_index RENAME INDEX uk_trade_business_index_trade_no TO uk_trade_business_index_trade_order_no'
               ELSE 'SELECT ''skip rename uk_trade_business_index_trade_order_no'''
           END
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V117__rename_accounting_and_message_trade_fields.sql]
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_entry' AND COLUMN_NAME = 'trade_no'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_entry' AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'ALTER TABLE acct_entry CHANGE COLUMN trade_no trade_order_no VARCHAR(64) NULL COMMENT ''交易单号'''
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_entry' AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'ALTER TABLE acct_entry MODIFY COLUMN trade_order_no VARCHAR(64) NULL COMMENT ''交易单号'''
               ELSE 'SELECT ''skip acct_entry trade_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_entry' AND COLUMN_NAME = 'payment_id'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_entry' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE acct_entry CHANGE COLUMN payment_id pay_order_no VARCHAR(64) NULL COMMENT ''支付单号'''
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_entry' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE acct_entry MODIFY COLUMN pay_order_no VARCHAR(64) NULL COMMENT ''支付单号'''
               ELSE 'SELECT ''skip acct_entry pay_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_event_journal' AND COLUMN_NAME = 'trade_no'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_event_journal' AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'ALTER TABLE acct_event_journal CHANGE COLUMN trade_no trade_order_no VARCHAR(64) NULL COMMENT ''交易单号'''
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_event_journal' AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'ALTER TABLE acct_event_journal MODIFY COLUMN trade_order_no VARCHAR(64) NULL COMMENT ''交易单号'''
               ELSE 'SELECT ''skip acct_event_journal trade_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_event_journal' AND COLUMN_NAME = 'payment_id'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_event_journal' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE acct_event_journal CHANGE COLUMN payment_id pay_order_no VARCHAR(64) NULL COMMENT ''支付单号'''
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_event_journal' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE acct_event_journal MODIFY COLUMN pay_order_no VARCHAR(64) NULL COMMENT ''支付单号'''
               ELSE 'SELECT ''skip acct_event_journal pay_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_voucher' AND COLUMN_NAME = 'trade_no'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_voucher' AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'ALTER TABLE acct_voucher CHANGE COLUMN trade_no trade_order_no VARCHAR(64) NULL COMMENT ''交易单号'''
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_voucher' AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'ALTER TABLE acct_voucher MODIFY COLUMN trade_order_no VARCHAR(64) NULL COMMENT ''交易单号'''
               ELSE 'SELECT ''skip acct_voucher trade_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_voucher' AND COLUMN_NAME = 'payment_id'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_voucher' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE acct_voucher CHANGE COLUMN payment_id pay_order_no VARCHAR(64) NULL COMMENT ''支付单号'''
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_voucher' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE acct_voucher MODIFY COLUMN pay_order_no VARCHAR(64) NULL COMMENT ''支付单号'''
               ELSE 'SELECT ''skip acct_voucher pay_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'message_record' AND COLUMN_NAME = 'trade_no'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'message_record' AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'ALTER TABLE message_record CHANGE COLUMN trade_no trade_order_no VARCHAR(64) NULL COMMENT ''交易单号'''
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'message_record' AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'ALTER TABLE message_record MODIFY COLUMN trade_order_no VARCHAR(64) NULL COMMENT ''交易单号'''
               ELSE 'SELECT ''skip message_record trade_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_entry' AND INDEX_NAME = 'idx_acct_entry_trade'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_entry' AND INDEX_NAME = 'idx_acct_entry_trade_order'
               )
                   THEN 'ALTER TABLE acct_entry RENAME INDEX idx_acct_entry_trade TO idx_acct_entry_trade_order'
               ELSE 'SELECT ''skip idx_acct_entry_trade_order'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_entry' AND INDEX_NAME = 'idx_acct_entry_payment'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_entry' AND INDEX_NAME = 'idx_acct_entry_pay_order'
               )
                   THEN 'ALTER TABLE acct_entry RENAME INDEX idx_acct_entry_payment TO idx_acct_entry_pay_order'
               ELSE 'SELECT ''skip idx_acct_entry_pay_order'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_event_journal' AND INDEX_NAME = 'idx_acct_event_trade'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_event_journal' AND INDEX_NAME = 'idx_acct_event_trade_order'
               )
                   THEN 'ALTER TABLE acct_event_journal RENAME INDEX idx_acct_event_trade TO idx_acct_event_trade_order'
               ELSE 'SELECT ''skip idx_acct_event_trade_order'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_event_journal' AND INDEX_NAME = 'idx_acct_event_payment'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_event_journal' AND INDEX_NAME = 'idx_acct_event_pay_order'
               )
                   THEN 'ALTER TABLE acct_event_journal RENAME INDEX idx_acct_event_payment TO idx_acct_event_pay_order'
               ELSE 'SELECT ''skip idx_acct_event_pay_order'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_voucher' AND INDEX_NAME = 'idx_acct_voucher_trade'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_voucher' AND INDEX_NAME = 'idx_acct_voucher_trade_order'
               )
                   THEN 'ALTER TABLE acct_voucher RENAME INDEX idx_acct_voucher_trade TO idx_acct_voucher_trade_order'
               ELSE 'SELECT ''skip idx_acct_voucher_trade_order'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_voucher' AND INDEX_NAME = 'idx_acct_voucher_payment'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'acct_voucher' AND INDEX_NAME = 'idx_acct_voucher_pay_order'
               )
                   THEN 'ALTER TABLE acct_voucher RENAME INDEX idx_acct_voucher_payment TO idx_acct_voucher_pay_order'
               ELSE 'SELECT ''skip idx_acct_voucher_pay_order'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V118__rename_inbound_outbound_pay_unique_no_to_pay_order_no.sql]
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'pay_unique_no'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE inbound_order CHANGE COLUMN pay_unique_no pay_order_no VARCHAR(64) NOT NULL COMMENT ''支付单号'''
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE inbound_order MODIFY COLUMN pay_order_no VARCHAR(64) NOT NULL COMMENT ''支付单号'''
               ELSE 'SELECT ''skip inbound_order pay_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'pay_unique_no'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE outbound_order CHANGE COLUMN pay_unique_no pay_order_no VARCHAR(64) NOT NULL COMMENT ''支付单号'''
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE outbound_order MODIFY COLUMN pay_order_no VARCHAR(64) NOT NULL COMMENT ''支付单号'''
               ELSE 'SELECT ''skip outbound_order pay_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND INDEX_NAME = 'uk_fin_inbound_pay_unique_no'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND INDEX_NAME = 'uk_fin_inbound_pay_order_no'
               )
                   THEN 'ALTER TABLE inbound_order RENAME INDEX uk_fin_inbound_pay_unique_no TO uk_fin_inbound_pay_order_no'
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND INDEX_NAME = 'uk_fin_influx_pay_unique_no'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inbound_order' AND INDEX_NAME = 'uk_fin_inbound_pay_order_no'
               )
                   THEN 'ALTER TABLE inbound_order RENAME INDEX uk_fin_influx_pay_unique_no TO uk_fin_inbound_pay_order_no'
               ELSE 'SELECT ''skip inbound_order pay_order_no index'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND INDEX_NAME = 'uk_outbound_order_pay_unique_no'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND INDEX_NAME = 'uk_outbound_order_pay_order_no'
               )
                   THEN 'ALTER TABLE outbound_order RENAME INDEX uk_outbound_order_pay_unique_no TO uk_outbound_order_pay_order_no'
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND INDEX_NAME = 'uk_outflux_order_pay_unique_no'
               ) AND NOT EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'outbound_order' AND INDEX_NAME = 'uk_outbound_order_pay_order_no'
               )
                   THEN 'ALTER TABLE outbound_order RENAME INDEX uk_outflux_order_pay_unique_no TO uk_outbound_order_pay_order_no'
               ELSE 'SELECT ''skip outbound_order pay_order_no index'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V119__support_trade_pay_current_attempt_model.sql]
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_order'
                     AND COLUMN_NAME = 'attempt_no'
               )
                   THEN 'SELECT ''skip pay_order attempt_no add'''
               ELSE 'ALTER TABLE pay_order ADD COLUMN attempt_no INT NULL COMMENT ''来源业务内支付尝试序号：同一 source_biz_type + source_biz_no 从 1 递增'' AFTER source_biz_no'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_order'
                     AND COLUMN_NAME = 'attempt_no'
               )
                   THEN 'ALTER TABLE pay_order MODIFY COLUMN attempt_no INT NOT NULL DEFAULT 1 COMMENT ''来源业务内支付尝试序号：同一 source_biz_type + source_biz_no 从 1 递增'''
               ELSE 'SELECT ''skip pay_order attempt_no modify'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_order'
                     AND INDEX_NAME = 'uk_pay_order_source_biz'
               )
                   THEN 'ALTER TABLE pay_order DROP INDEX uk_pay_order_source_biz'
               ELSE 'SELECT ''skip drop uk_pay_order_source_biz'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_order'
                     AND INDEX_NAME = 'uk_pay_order_source_attempt'
               )
                   THEN 'ALTER TABLE pay_order DROP INDEX uk_pay_order_source_attempt'
               ELSE 'SELECT ''skip drop uk_pay_order_source_attempt'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
ALTER TABLE pay_order
    ADD UNIQUE KEY uk_pay_order_source_attempt (source_biz_type, source_biz_no, attempt_no);
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_order'
                     AND INDEX_NAME = 'idx_pay_order_source_latest'
               )
                   THEN 'SELECT ''skip add idx_pay_order_source_latest'''
               ELSE 'ALTER TABLE pay_order ADD KEY idx_pay_order_source_latest (source_biz_type, source_biz_no, attempt_no, id)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'trade_order'
                     AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'ALTER TABLE trade_order MODIFY COLUMN pay_order_no VARCHAR(64) NULL COMMENT ''当前生效支付订单号：同一交易多次支付尝试时指向最新尝试'''
               ELSE 'SELECT ''skip trade_order pay_order_no comment'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'trade_order'
                     AND COLUMN_NAME = 'last_pay_status_version'
               )
                   THEN 'ALTER TABLE trade_order MODIFY COLUMN last_pay_status_version INT NOT NULL DEFAULT 0 COMMENT ''当前生效支付单最近一次已应用的状态版本号'''
               ELSE 'SELECT ''skip trade_order last_pay_status_version comment'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V128__drop_inbound_settle_status_column.sql]
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'idx_fin_inbound_settle_status'
               )
                   THEN 'ALTER TABLE inbound_order DROP INDEX idx_fin_inbound_settle_status'
               ELSE 'SELECT ''skip drop idx_fin_inbound_settle_status'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'idx_fin_influx_settle_status'
               )
                   THEN 'ALTER TABLE inbound_order DROP INDEX idx_fin_influx_settle_status'
               ELSE 'SELECT ''skip drop idx_fin_influx_settle_status'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND COLUMN_NAME = 'settle_status'
               )
                   THEN 'ALTER TABLE inbound_order DROP COLUMN settle_status'
               ELSE 'SELECT ''skip drop settle_status'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V130__rename_business_order_columns_to_biz_order_no.sql]
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_order' AND COLUMN_NAME = 'merchant_order_no'
               ) AND NOT EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_order' AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'ALTER TABLE pay_order CHANGE COLUMN merchant_order_no biz_order_no VARCHAR(64) NOT NULL COMMENT ''业务单号，幂等键'''
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_order' AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'ALTER TABLE pay_order MODIFY COLUMN biz_order_no VARCHAR(64) NOT NULL COMMENT ''业务单号，幂等键'''
               ELSE 'SELECT ''skip pay_order biz_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'business_order_no'
               ) AND NOT EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'ALTER TABLE trade_order CHANGE COLUMN business_order_no biz_order_no VARCHAR(64) NOT NULL COMMENT ''业务交易单号：在对应业务域内唯一，用于后台按业务单直接检索交易'''
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'ALTER TABLE trade_order MODIFY COLUMN biz_order_no VARCHAR(64) NOT NULL COMMENT ''业务交易单号：在对应业务域内唯一，用于后台按业务单直接检索交易'''
               ELSE 'SELECT ''skip trade_order biz_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_credit_order' AND COLUMN_NAME = 'business_order_no'
               ) AND NOT EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_credit_order' AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'ALTER TABLE trade_credit_order CHANGE COLUMN business_order_no biz_order_no VARCHAR(64) NOT NULL COMMENT ''信用业务交易单号：花呗还款、借呗借款等业务域内唯一单号'''
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_credit_order' AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'ALTER TABLE trade_credit_order MODIFY COLUMN biz_order_no VARCHAR(64) NOT NULL COMMENT ''信用业务交易单号：花呗还款、借呗借款等业务域内唯一单号'''
               ELSE 'SELECT ''skip trade_credit_order biz_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_fund_order' AND COLUMN_NAME = 'business_order_no'
               ) AND NOT EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_fund_order' AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'ALTER TABLE trade_fund_order CHANGE COLUMN business_order_no biz_order_no VARCHAR(64) NOT NULL COMMENT ''基金业务交易单号：余额宝申购、赎回等业务域内唯一单号'''
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_fund_order' AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'ALTER TABLE trade_fund_order MODIFY COLUMN biz_order_no VARCHAR(64) NOT NULL COMMENT ''基金业务交易单号：余额宝申购、赎回等业务域内唯一单号'''
               ELSE 'SELECT ''skip trade_fund_order biz_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_bill_index' AND COLUMN_NAME = 'business_order_no'
               ) AND NOT EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_bill_index' AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'ALTER TABLE trade_bill_index CHANGE COLUMN business_order_no biz_order_no VARCHAR(64) NOT NULL COMMENT ''业务交易单号：后台与账单页按业务单查询时使用'''
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_bill_index' AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'ALTER TABLE trade_bill_index MODIFY COLUMN biz_order_no VARCHAR(64) NOT NULL COMMENT ''业务交易单号：后台与账单页按业务单查询时使用'''
               ELSE 'SELECT ''skip trade_bill_index biz_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_order' AND INDEX_NAME = 'uk_pay_order_merchant_order_no'
               ) AND NOT EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_order' AND INDEX_NAME = 'uk_pay_order_biz_order_no'
               )
                   THEN 'ALTER TABLE pay_order RENAME INDEX uk_pay_order_merchant_order_no TO uk_pay_order_biz_order_no'
               ELSE 'SELECT ''skip uk_pay_order_biz_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND INDEX_NAME = 'idx_trade_order_business_order'
               ) AND NOT EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND INDEX_NAME = 'idx_trade_order_biz_order'
               )
                   THEN 'ALTER TABLE trade_order RENAME INDEX idx_trade_order_business_order TO idx_trade_order_biz_order'
               ELSE 'SELECT ''skip idx_trade_order_biz_order'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_credit_order' AND INDEX_NAME = 'uk_trade_credit_order_business_order_no'
               ) AND NOT EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_credit_order' AND INDEX_NAME = 'uk_trade_credit_order_biz_order_no'
               )
                   THEN 'ALTER TABLE trade_credit_order RENAME INDEX uk_trade_credit_order_business_order_no TO uk_trade_credit_order_biz_order_no'
               ELSE 'SELECT ''skip uk_trade_credit_order_biz_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_fund_order' AND INDEX_NAME = 'uk_trade_fund_order_business_order_no'
               ) AND NOT EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_fund_order' AND INDEX_NAME = 'uk_trade_fund_order_biz_order_no'
               )
                   THEN 'ALTER TABLE trade_fund_order RENAME INDEX uk_trade_fund_order_business_order_no TO uk_trade_fund_order_biz_order_no'
               ELSE 'SELECT ''skip uk_trade_fund_order_biz_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_bill_index' AND INDEX_NAME = 'uk_trade_business_index_business_order'
               ) AND NOT EXISTS (
                   SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_bill_index' AND INDEX_NAME = 'uk_trade_business_index_biz_order'
               )
                   THEN 'ALTER TABLE trade_bill_index RENAME INDEX uk_trade_business_index_business_order TO uk_trade_business_index_biz_order'
               ELSE 'SELECT ''skip uk_trade_business_index_biz_order'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V131__add_wallet_freeze_type_and_record_table.sql]
ALTER TABLE wallet_tcc_transaction
    ADD COLUMN freeze_type VARCHAR(32) NOT NULL DEFAULT 'PAY_HOLD' AFTER operation_type;
CREATE INDEX idx_wallet_tcc_freeze_type
    ON wallet_tcc_transaction (freeze_type);
CREATE TABLE IF NOT EXISTS wallet_freeze_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    xid VARCHAR(128) NOT NULL,
    branch_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    freeze_type VARCHAR(32) NOT NULL,
    operation_type VARCHAR(16) NOT NULL,
    freeze_status VARCHAR(16) NOT NULL DEFAULT 'FROZEN',
    amount DECIMAL(18,2) NOT NULL,
    business_no VARCHAR(64) NULL,
    freeze_reason VARCHAR(128) NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wallet_freeze_record_xid_branch (xid, branch_id),
    KEY idx_wallet_freeze_record_user_status (user_id, freeze_status),
    KEY idx_wallet_freeze_record_user_type_status (user_id, freeze_type, freeze_status),
    KEY idx_wallet_freeze_record_business_no (business_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [V132__wallet_multi_currency_m0.sql]
ALTER TABLE wallet_account
    MODIFY COLUMN available_balance DECIMAL(20,8) NOT NULL DEFAULT 0.00000000,
    MODIFY COLUMN reserved_balance DECIMAL(20,8) NOT NULL DEFAULT 0.00000000;
ALTER TABLE wallet_account
    DROP INDEX uk_wallet_account_user_id;
ALTER TABLE wallet_account
    ADD UNIQUE KEY uk_wallet_account_user_currency (user_id, currency_code);
ALTER TABLE wallet_account
    ADD KEY idx_wallet_account_user_currency (user_id, currency_code);
ALTER TABLE wallet_tcc_transaction
    MODIFY COLUMN amount DECIMAL(20,8) NOT NULL,
    ADD COLUMN currency_code VARCHAR(8) NOT NULL DEFAULT 'CNY' AFTER amount;
ALTER TABLE wallet_freeze_record
    MODIFY COLUMN amount DECIMAL(20,8) NOT NULL,
    ADD COLUMN currency_code VARCHAR(8) NOT NULL DEFAULT 'CNY' AFTER amount;
CREATE INDEX idx_wallet_tcc_user_currency ON wallet_tcc_transaction (user_id, currency_code);
CREATE INDEX idx_wallet_freeze_user_currency_status ON wallet_freeze_record (user_id, currency_code, freeze_status);

-- [V133__drop_outbound_currency_column.sql]
ALTER TABLE outbound_order
    DROP COLUMN outbound_currency;

-- [V134__drop_redundant_money_currency_columns.sql]
ALTER TABLE inbound_order
    DROP COLUMN inbound_currency,
    DROP COLUMN account_currency,
    DROP COLUMN settle_currency;
ALTER TABLE trade_bill_index
    DROP COLUMN currency_code;
ALTER TABLE trade_credit_order
    DROP COLUMN currency_code;
ALTER TABLE trade_fund_order
    DROP COLUMN currency_code;
ALTER TABLE loan_trade_order
    DROP COLUMN currency_code;
ALTER TABLE message_red_packet_order
    DROP COLUMN currency_code;
ALTER TABLE acct_entry
    DROP COLUMN currency_code;

-- [V135__drop_acct_voucher_currency_code.sql]
ALTER TABLE acct_voucher
    DROP COLUMN currency_code;

-- [V139__normalize_legacy_fund_code_to_aicash.sql]
-- 统一历史过渡基金编码到 AICASH
-- 说明：历史内部过渡编码统一迁移为 AICASH，避免继续保留多套爱存产品码。

SET @legacy_fund_code = CONCAT('AI', 'CUN');
SET @legacy_fund_product_name = CONCAT(@legacy_fund_code, ' PRODUCT');
SET @legacy_biz_marker = CONCAT(':', @legacy_fund_code, ':');
SET @legacy_biz_prefix = CONCAT(':', @legacy_fund_code);
SET @legacy_idempotency_suffix = CONCAT('_', @legacy_fund_code);
SET @legacy_payment_prefix = CONCAT(@legacy_fund_code, ':');
SET @legacy_payment_like = CONCAT(@legacy_fund_code, '%');
SET @legacy_account_suffix = CONCAT('-', @legacy_fund_code);
SET @legacy_bill_prefix = CONCAT(@legacy_fund_code, 'BILL-');

-- [V140__align_cross_domain_order_trace_numbers.sql]
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_order'
                     AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'SELECT ''skip add pay_order.trade_order_no'''
               ELSE 'ALTER TABLE pay_order ADD COLUMN trade_order_no VARCHAR(64) NULL COMMENT ''上游交易订单号'' AFTER pay_order_no'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_order'
                     AND INDEX_NAME = 'idx_pay_order_trade_order_no'
               )
                   THEN 'SELECT ''skip add idx_pay_order_trade_order_no'''
               ELSE 'CREATE INDEX idx_pay_order_trade_order_no ON pay_order (trade_order_no)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'SELECT ''skip add inbound_order.biz_order_no'''
               ELSE 'ALTER TABLE inbound_order ADD COLUMN biz_order_no VARCHAR(64) NULL COMMENT ''全局业务单号'' AFTER request_biz_no'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'SELECT ''skip add inbound_order.trade_order_no'''
               ELSE 'ALTER TABLE inbound_order ADD COLUMN trade_order_no VARCHAR(64) NULL COMMENT ''上游交易订单号'' AFTER biz_order_no'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'ALTER TABLE inbound_order MODIFY COLUMN biz_order_no VARCHAR(64) NOT NULL COMMENT ''全局业务单号'''
               ELSE 'SELECT ''skip modify inbound_order.biz_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'idx_inbound_order_biz_order_no'
               )
                   THEN 'SELECT ''skip add idx_inbound_order_biz_order_no'''
               ELSE 'CREATE INDEX idx_inbound_order_biz_order_no ON inbound_order (biz_order_no)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'inbound_order'
                     AND INDEX_NAME = 'idx_inbound_order_trade_order_no'
               )
                   THEN 'SELECT ''skip add idx_inbound_order_trade_order_no'''
               ELSE 'CREATE INDEX idx_inbound_order_trade_order_no ON inbound_order (trade_order_no)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'SELECT ''skip add outbound_order.biz_order_no'''
               ELSE 'ALTER TABLE outbound_order ADD COLUMN biz_order_no VARCHAR(64) NULL COMMENT ''全局业务单号'' AFTER request_biz_no'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'SELECT ''skip add outbound_order.trade_order_no'''
               ELSE 'ALTER TABLE outbound_order ADD COLUMN trade_order_no VARCHAR(64) NULL COMMENT ''上游交易订单号'' AFTER biz_order_no'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'ALTER TABLE outbound_order MODIFY COLUMN biz_order_no VARCHAR(64) NOT NULL COMMENT ''全局业务单号'''
               ELSE 'SELECT ''skip modify outbound_order.biz_order_no'''
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND INDEX_NAME = 'idx_outbound_order_biz_order_no'
               )
                   THEN 'SELECT ''skip add idx_outbound_order_biz_order_no'''
               ELSE 'CREATE INDEX idx_outbound_order_biz_order_no ON outbound_order (biz_order_no)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'outbound_order'
                     AND INDEX_NAME = 'idx_outbound_order_trade_order_no'
               )
                   THEN 'SELECT ''skip add idx_outbound_order_trade_order_no'''
               ELSE 'CREATE INDEX idx_outbound_order_trade_order_no ON outbound_order (trade_order_no)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'coupon_issue'
                     AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'SELECT ''skip add coupon_issue.biz_order_no'''
               ELSE 'ALTER TABLE coupon_issue ADD COLUMN biz_order_no VARCHAR(64) NULL COMMENT ''全局业务单号'' AFTER order_no'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'coupon_issue'
                     AND COLUMN_NAME = 'trade_order_no'
               )
                   THEN 'SELECT ''skip add coupon_issue.trade_order_no'''
               ELSE 'ALTER TABLE coupon_issue ADD COLUMN trade_order_no VARCHAR(64) NULL COMMENT ''交易订单号'' AFTER biz_order_no'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'coupon_issue'
                     AND COLUMN_NAME = 'pay_order_no'
               )
                   THEN 'SELECT ''skip add coupon_issue.pay_order_no'''
               ELSE 'ALTER TABLE coupon_issue ADD COLUMN pay_order_no VARCHAR(64) NULL COMMENT ''支付订单号'' AFTER trade_order_no'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'coupon_issue'
                     AND INDEX_NAME = 'idx_coupon_issue_biz_order_no'
               )
                   THEN 'SELECT ''skip add idx_coupon_issue_biz_order_no'''
               ELSE 'CREATE INDEX idx_coupon_issue_biz_order_no ON coupon_issue (biz_order_no)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'coupon_issue'
                     AND INDEX_NAME = 'idx_coupon_issue_trade_order_no'
               )
                   THEN 'SELECT ''skip add idx_coupon_issue_trade_order_no'''
               ELSE 'CREATE INDEX idx_coupon_issue_trade_order_no ON coupon_issue (trade_order_no)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'coupon_issue'
                     AND INDEX_NAME = 'idx_coupon_issue_pay_order_no'
               )
                   THEN 'SELECT ''skip add idx_coupon_issue_pay_order_no'''
               ELSE 'CREATE INDEX idx_coupon_issue_pay_order_no ON coupon_issue (pay_order_no)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'acct_event_journal'
                     AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'SELECT ''skip add acct_event_journal.biz_order_no'''
               ELSE 'ALTER TABLE acct_event_journal ADD COLUMN biz_order_no VARCHAR(64) NULL COMMENT ''全局业务单号'' AFTER source_biz_no'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'acct_event_journal'
                     AND INDEX_NAME = 'idx_acct_event_biz_order_no'
               )
                   THEN 'SELECT ''skip add idx_acct_event_biz_order_no'''
               ELSE 'CREATE INDEX idx_acct_event_biz_order_no ON acct_event_journal (biz_order_no)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'acct_voucher'
                     AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'SELECT ''skip add acct_voucher.biz_order_no'''
               ELSE 'ALTER TABLE acct_voucher ADD COLUMN biz_order_no VARCHAR(64) NULL COMMENT ''全局业务单号'' AFTER source_biz_no'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'acct_voucher'
                     AND INDEX_NAME = 'idx_acct_voucher_biz_order_no'
               )
                   THEN 'SELECT ''skip add idx_acct_voucher_biz_order_no'''
               ELSE 'CREATE INDEX idx_acct_voucher_biz_order_no ON acct_voucher (biz_order_no)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'acct_entry'
                     AND COLUMN_NAME = 'biz_order_no'
               )
                   THEN 'SELECT ''skip add acct_entry.biz_order_no'''
               ELSE 'ALTER TABLE acct_entry ADD COLUMN biz_order_no VARCHAR(64) NULL COMMENT ''全局业务单号'' AFTER biz_role'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'acct_entry'
                     AND INDEX_NAME = 'idx_acct_entry_biz_order_no'
               )
                   THEN 'SELECT ''skip add idx_acct_entry_biz_order_no'''
               ELSE 'CREATE INDEX idx_acct_entry_biz_order_no ON acct_entry (biz_order_no)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V142__rename_trade_business_index_to_trade_bill_index.sql]
SET @trade_business_index_exists = (
    SELECT COUNT(1)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'trade_bill_index'
);
SET @trade_bill_index_exists = (
    SELECT COUNT(1)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'trade_bill_index'
);

-- [V143__create_loan_account_profile_table.sql]
CREATE TABLE IF NOT EXISTS loan_account_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    account_no VARCHAR(64) NOT NULL COMMENT '爱借账户号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    annual_rate_percent DECIMAL(10,4) NOT NULL DEFAULT 3.2400 COMMENT '当前年化利率（百分数）',
    original_annual_rate_percent DECIMAL(10,4) NOT NULL DEFAULT 5.0400 COMMENT '原始年化利率（百分数）',
    total_term_months INT NOT NULL DEFAULT 24 COMMENT '总期数（月）',
    draw_date DATE NOT NULL COMMENT '放款日期',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_loan_account_profile_account_no (account_no),
    KEY idx_loan_account_profile_user_id (user_id)
) COMMENT='爱借账户档案表';

-- [V145__upgrade_realname_credit_limits.sql]
-- 登录密码哈希从 SHA256 升级为 PBKDF2 后，值长度超过 64，扩容字段避免登录更新失败。
ALTER TABLE user_account
    MODIFY COLUMN login_password_sha256 VARCHAR(255) NULL COMMENT '登录密码哈希';

-- [V147__expand_app_visit_record_api_name_length.sql]
ALTER TABLE app_visit_record
    MODIFY COLUMN api_name VARCHAR(1024) NOT NULL COMMENT '调用接口';

-- [V148__create_security_attempt_and_scheduler_lock_tables.sql]
CREATE TABLE IF NOT EXISTS security_attempt_guard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    guard_scope VARCHAR(64) NOT NULL COMMENT '限流作用域，如 USER_LOGIN/REGISTER_CHECK',
    principal_key VARCHAR(128) NOT NULL COMMENT '限流主体键，如 loginId',
    attempt_count INT NOT NULL DEFAULT 0 COMMENT '当前窗口累计次数',
    last_attempt_at DATETIME(3) NOT NULL COMMENT '最近一次尝试时间',
    lock_until DATETIME(3) NULL COMMENT '锁定截止时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_security_attempt_scope_principal (guard_scope, principal_key),
    KEY idx_security_attempt_lock_until (lock_until)
) COMMENT='安全限流状态表';
CREATE TABLE IF NOT EXISTS scheduler_lock (
    lock_name VARCHAR(128) NOT NULL PRIMARY KEY COMMENT '锁名称',
    lock_until DATETIME(3) NOT NULL COMMENT '锁过期时间',
    locked_at DATETIME(3) NOT NULL COMMENT '加锁时间',
    locked_by VARCHAR(128) NOT NULL COMMENT '加锁实例标识'
) COMMENT='调度任务分布式锁表';

-- [V149__create_risk_rule_table_and_permissions.sql]
CREATE TABLE IF NOT EXISTS risk_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    rule_code VARCHAR(64) NOT NULL COMMENT '规则编码，全局唯一',
    scene_code VARCHAR(64) NOT NULL COMMENT '场景编码，如 TRADE_PAY',
    rule_type VARCHAR(32) NOT NULL COMMENT '规则类型：SINGLE_LIMIT/DAILY_LIMIT/USER_BLOCK',
    scope_type VARCHAR(16) NOT NULL DEFAULT 'GLOBAL' COMMENT '生效范围：GLOBAL/USER',
    scope_value VARCHAR(64) NULL COMMENT '作用域值，scope_type=USER时为userId',
    threshold_amount DECIMAL(18,2) NULL COMMENT '阈值金额，USER_BLOCK可为空',
    currency_code VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '币种编码',
    priority INT NOT NULL DEFAULT 100 COMMENT '优先级，越小越先执行',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '规则状态：ACTIVE/INACTIVE',
    rule_desc VARCHAR(255) NULL COMMENT '规则说明',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '更新人',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_risk_rule_rule_code (rule_code),
    KEY idx_risk_rule_scene_status_priority (scene_code, status, priority)
) COMMENT='风控规则配置表';

-- [V150__add_admin_audit_observability_and_outbox_requeue.sql]
CREATE TABLE IF NOT EXISTS admin_operation_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    trace_id VARCHAR(64) NULL COMMENT '链路追踪号',
    admin_id BIGINT NULL COMMENT '管理员ID',
    admin_username VARCHAR(64) NULL COMMENT '管理员账号',
    request_method VARCHAR(16) NOT NULL COMMENT '请求方法',
    request_path VARCHAR(255) NOT NULL COMMENT '请求路径',
    request_query VARCHAR(500) NULL COMMENT '请求query',
    request_body VARCHAR(4000) NULL COMMENT '请求体摘要',
    result_status VARCHAR(16) NOT NULL COMMENT '结果状态：SUCCESS/FAILED',
    error_message VARCHAR(500) NULL COMMENT '错误摘要',
    cost_ms BIGINT NULL COMMENT '执行耗时（毫秒）',
    client_ip VARCHAR(64) NULL COMMENT '客户端IP',
    user_agent VARCHAR(255) NULL COMMENT '客户端UA',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    KEY idx_admin_operation_audit_admin_created (admin_id, created_at),
    KEY idx_admin_operation_audit_path_created (request_path, created_at),
    KEY idx_admin_operation_audit_status_created (result_status, created_at)
) COMMENT='后台操作审计日志表';

-- [V151__create_audience_tables_and_permissions.sql]
CREATE TABLE IF NOT EXISTS audience_tag_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tag_code VARCHAR(64) NOT NULL COMMENT '标签编码',
    tag_name VARCHAR(64) NOT NULL COMMENT '标签名称',
    tag_type VARCHAR(16) NOT NULL DEFAULT 'ENUM' COMMENT '标签值类型',
    value_scope VARCHAR(512) NULL COMMENT '标签取值范围',
    description VARCHAR(255) NULL COMMENT '标签说明',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_audience_tag_definition_code (tag_code),
    KEY idx_audience_tag_definition_enabled_code (enabled, tag_code)
) COMMENT='人群标签定义表';
CREATE TABLE IF NOT EXISTS audience_user_tag_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tag_code VARCHAR(64) NOT NULL COMMENT '标签编码',
    tag_value VARCHAR(255) NOT NULL COMMENT '标签值',
    source VARCHAR(64) NULL COMMENT '标签来源',
    value_updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '标签值更新时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_audience_user_tag_snapshot_user_tag (user_id, tag_code),
    KEY idx_audience_user_tag_snapshot_tag_code (tag_code),
    KEY idx_audience_user_tag_snapshot_value_updated (value_updated_at)
) COMMENT='用户标签快照表';
CREATE TABLE IF NOT EXISTS audience_segment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    segment_code VARCHAR(64) NOT NULL COMMENT '人群编码',
    segment_name VARCHAR(128) NOT NULL COMMENT '人群名称',
    description VARCHAR(255) NULL COMMENT '人群说明',
    scene_code VARCHAR(64) NULL COMMENT '适用场景',
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_audience_segment_code (segment_code),
    KEY idx_audience_segment_status_scene (status, scene_code)
) COMMENT='人群分群表';
CREATE TABLE IF NOT EXISTS audience_segment_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    rule_code VARCHAR(64) NOT NULL COMMENT '规则编码',
    segment_code VARCHAR(64) NOT NULL COMMENT '人群编码',
    tag_code VARCHAR(64) NOT NULL COMMENT '标签编码',
    operator VARCHAR(16) NOT NULL DEFAULT 'EQ' COMMENT '运算符',
    target_value VARCHAR(255) NULL COMMENT '目标值',
    relation_type VARCHAR(16) NOT NULL DEFAULT 'INCLUDE' COMMENT '规则归属',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_audience_segment_rule_code (segment_code, rule_code),
    KEY idx_audience_segment_rule_segment_enabled (segment_code, enabled),
    KEY idx_audience_segment_rule_tag_code (tag_code)
) COMMENT='人群分群规则表';

-- [V154__add_user_account_source_and_relax_demo_id_card_constraint.sql]
SET @account_source_column_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_account'
      AND COLUMN_NAME = 'account_source'
);
SET @ddl = IF(
    @account_source_column_exists = 0,
    'ALTER TABLE user_account ADD COLUMN account_source VARCHAR(32) NOT NULL DEFAULT ''REGISTER'' COMMENT ''账号来源：REGISTER注册账号，DEMO演示账号'' AFTER kyc_level',
    'SET @noop_user_account_source_column = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @account_source_index_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_account'
      AND INDEX_NAME = 'idx_user_account_source'
);
SET @ddl = IF(
    @account_source_index_exists = 0,
    'ALTER TABLE user_account ADD INDEX idx_user_account_source (account_source)',
    'SET @noop_user_account_source_index = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @id_card_unique_index_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_profile'
      AND INDEX_NAME = 'uk_user_profile_id_card_no'
);
SET @ddl = IF(
    @id_card_unique_index_exists > 0,
    'ALTER TABLE user_profile DROP INDEX uk_user_profile_id_card_no',
    'SET @noop_drop_user_profile_id_card_unique = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @id_card_plain_index_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_profile'
      AND INDEX_NAME = 'idx_user_profile_id_card_no'
);
SET @ddl = IF(
    @id_card_plain_index_exists = 0,
    'ALTER TABLE user_profile ADD INDEX idx_user_profile_id_card_no (id_card_no)',
    'SET @noop_add_user_profile_id_card_index = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V155__create_app_behavior_event_log_table.sql]
CREATE TABLE IF NOT EXISTS app_behavior_event_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '埋点日志主键ID',
    event_id VARCHAR(64) NOT NULL COMMENT '事件ID',
    session_id VARCHAR(64) NULL COMMENT '会话ID',
    app_code VARCHAR(64) NOT NULL COMMENT '应用编码',
    event_name VARCHAR(128) NOT NULL COMMENT '事件名称',
    event_type VARCHAR(32) NULL COMMENT '事件类型',
    event_code VARCHAR(128) NULL COMMENT '事件编码/页面编码',
    page_name VARCHAR(128) NULL COMMENT '页面名称',
    action_name VARCHAR(128) NULL COMMENT '动作名称',
    result_status VARCHAR(64) NULL COMMENT '结果状态',
    trace_id VARCHAR(64) NULL COMMENT '链路追踪ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    client_id VARCHAR(64) NULL COMMENT '客户端ID',
    user_id BIGINT NULL COMMENT '用户ID',
    aipay_uid VARCHAR(64) NULL COMMENT '爱付UID',
    login_id VARCHAR(64) NULL COMMENT '登录账号',
    account_status VARCHAR(32) NULL COMMENT '账户状态',
    kyc_level VARCHAR(32) NULL COMMENT '实名等级',
    nickname VARCHAR(128) NULL COMMENT '昵称',
    mobile VARCHAR(32) NULL COMMENT '手机号',
    ip_address VARCHAR(64) NULL COMMENT 'IP地址',
    location_info VARCHAR(255) NULL COMMENT '位置信息',
    tenant_code VARCHAR(64) NULL COMMENT '租户编码',
    network_type VARCHAR(32) NULL COMMENT '网络类型',
    app_version_no VARCHAR(64) NULL COMMENT '应用版本号',
    app_build_no VARCHAR(32) NULL COMMENT '应用构建号',
    device_brand VARCHAR(64) NULL COMMENT '设备品牌',
    device_model VARCHAR(64) NULL COMMENT '设备型号',
    device_name VARCHAR(128) NULL COMMENT '设备名称',
    device_type VARCHAR(32) NULL COMMENT '设备类型',
    os_name VARCHAR(64) NULL COMMENT '系统名称',
    os_version VARCHAR(64) NULL COMMENT '系统版本',
    locale VARCHAR(64) NULL COMMENT '本地化区域',
    timezone VARCHAR(64) NULL COMMENT '时区',
    language VARCHAR(64) NULL COMMENT '语言',
    country_code VARCHAR(16) NULL COMMENT '国家编码',
    carrier_name VARCHAR(64) NULL COMMENT '运营商',
    screen_width INT NULL COMMENT '屏幕宽度',
    screen_height INT NULL COMMENT '屏幕高度',
    viewport_width INT NULL COMMENT '可视区域宽度',
    viewport_height INT NULL COMMENT '可视区域高度',
    duration_ms BIGINT NULL COMMENT '耗时(毫秒)',
    login_duration_ms BIGINT NULL COMMENT '登录时长(毫秒)',
    event_occurred_at DATETIME(3) NOT NULL COMMENT '事件发生时间',
    payload_json TEXT NULL COMMENT '扩展上下文JSON',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    KEY idx_app_behavior_event_device_time (device_id, event_occurred_at),
    KEY idx_app_behavior_event_user_time (user_id, event_occurred_at),
    KEY idx_app_behavior_event_name_time (event_name, event_occurred_at),
    KEY idx_app_behavior_event_session_time (session_id, event_occurred_at),
    KEY idx_app_behavior_event_app_time (app_code, event_occurred_at),
    UNIQUE KEY uk_app_behavior_event_id (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='iOS 客户端行为埋点日志表';

-- [V156__add_missing_table_and_column_comments_part2.sql]
ALTER TABLE `agreement_template`
COMMENT = '协议模板表：定义业务场景协议模板版本';
ALTER TABLE `agreement_template`
    MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '协议模板主键ID',
    MODIFY COLUMN `template_code` VARCHAR(64) NOT NULL COMMENT '协议模板编码',
    MODIFY COLUMN `template_version` VARCHAR(32) NOT NULL COMMENT '协议模板版本号',
    MODIFY COLUMN `biz_type` VARCHAR(64) NOT NULL COMMENT '业务类型编码',
    MODIFY COLUMN `title` VARCHAR(256) NOT NULL COMMENT '协议标题',
    MODIFY COLUMN `content_url` VARCHAR(512) NOT NULL COMMENT '协议内容地址',
    MODIFY COLUMN `content_hash` VARCHAR(128) NULL COMMENT '协议内容摘要哈希',
    MODIFY COLUMN `required_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否必签，1是0否',
    MODIFY COLUMN `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '模板状态：ACTIVE/INACTIVE',
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `agreement_sign_record`
COMMENT = '协议签署记录表：记录用户协议签署结果';
ALTER TABLE `agreement_sign_record`
    MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '协议签署记录主键ID',
    MODIFY COLUMN `sign_no` VARCHAR(64) NOT NULL COMMENT '签署流水号',
    MODIFY COLUMN `user_id` BIGINT NOT NULL COMMENT '用户ID',
    MODIFY COLUMN `biz_type` VARCHAR(64) NOT NULL COMMENT '业务类型编码',
    MODIFY COLUMN `fund_code` VARCHAR(32) NULL COMMENT '基金编码',
    MODIFY COLUMN `currency_code` VARCHAR(8) NULL COMMENT '币种编码',
    MODIFY COLUMN `idempotency_key` VARCHAR(64) NOT NULL COMMENT '幂等键',
    MODIFY COLUMN `sign_status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '签署状态：PENDING/SIGNED/OPENED/FAILED',
    MODIFY COLUMN `signed_at` DATETIME NULL COMMENT '签署完成时间',
    MODIFY COLUMN `opened_at` DATETIME NULL COMMENT '功能开通时间',
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `agreement_sign_item`
COMMENT = '协议签署明细表：记录每个模板条款的签署结果';
ALTER TABLE `agreement_sign_item`
    MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '协议签署明细主键ID',
    MODIFY COLUMN `sign_no` VARCHAR(64) NOT NULL COMMENT '签署流水号',
    MODIFY COLUMN `template_code` VARCHAR(64) NOT NULL COMMENT '协议模板编码',
    MODIFY COLUMN `template_version` VARCHAR(32) NOT NULL COMMENT '协议模板版本号',
    MODIFY COLUMN `title` VARCHAR(256) NOT NULL COMMENT '条款标题',
    MODIFY COLUMN `content_url` VARCHAR(512) NOT NULL COMMENT '条款内容地址',
    MODIFY COLUMN `content_hash` VARCHAR(128) NULL COMMENT '条款内容摘要哈希',
    MODIFY COLUMN `accepted` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否同意，1同意0不同意',
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `user_feature_status`
COMMENT = '用户功能开通状态表：记录用户功能权限与开通状态';
ALTER TABLE `user_feature_status`
    MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户功能状态主键ID',
    MODIFY COLUMN `user_id` BIGINT NOT NULL COMMENT '用户ID',
    MODIFY COLUMN `feature_code` VARCHAR(64) NOT NULL COMMENT '功能编码',
    MODIFY COLUMN `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用，1启用0停用',
    MODIFY COLUMN `opened_at` DATETIME NULL COMMENT '开通时间',
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE `wallet_account`
    MODIFY COLUMN `available_balance` DECIMAL(20,8) NOT NULL DEFAULT 0.00000000 COMMENT '可用余额，可直接用于支付',
    MODIFY COLUMN `reserved_balance` DECIMAL(20,8) NOT NULL DEFAULT 0.00000000 COMMENT '预留余额，已冻结待确认';
ALTER TABLE `wallet_tcc_transaction`
    MODIFY COLUMN `freeze_type` VARCHAR(32) NOT NULL DEFAULT 'PAY_HOLD' COMMENT '冻结类型：PAY_HOLD/WITHDRAW_HOLD/RISK_HOLD/DEBT_HOLD',
    MODIFY COLUMN `amount` DECIMAL(20,8) NOT NULL COMMENT '本次冻结或扣减金额',
    MODIFY COLUMN `currency_code` VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '币种编码';
ALTER TABLE `wallet_freeze_record`
COMMENT = '钱包冻结记录表：记录冻结、解冻、扣减的状态流转';
ALTER TABLE `wallet_freeze_record`
    MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '钱包冻结记录主键ID',
    MODIFY COLUMN `xid` VARCHAR(128) NOT NULL COMMENT '全局事务XID',
    MODIFY COLUMN `branch_id` VARCHAR(64) NOT NULL COMMENT '分支事务ID',
    MODIFY COLUMN `user_id` BIGINT NOT NULL COMMENT '用户ID',
    MODIFY COLUMN `freeze_type` VARCHAR(32) NOT NULL COMMENT '冻结类型：PAY_HOLD/WITHDRAW_HOLD/RISK_HOLD/DEBT_HOLD',
    MODIFY COLUMN `operation_type` VARCHAR(16) NOT NULL COMMENT '操作类型：TRY/CONFIRM/CANCEL',
    MODIFY COLUMN `freeze_status` VARCHAR(16) NOT NULL DEFAULT 'FROZEN' COMMENT '冻结状态：FROZEN/UNFROZEN/DEBITED',
    MODIFY COLUMN `amount` DECIMAL(20,8) NOT NULL COMMENT '冻结金额',
    MODIFY COLUMN `currency_code` VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '币种编码',
    MODIFY COLUMN `business_no` VARCHAR(64) NULL COMMENT '关联业务单号',
    MODIFY COLUMN `freeze_reason` VARCHAR(128) NULL COMMENT '冻结原因',
    MODIFY COLUMN `lock_version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- [V158__add_demo_auto_login_switch_to_app_info.sql]
SET @demo_auto_login_column_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'app_info'
      AND COLUMN_NAME = 'demo_auto_login_enabled'
);
SET @ddl = IF(
    @demo_auto_login_column_exists = 0,
    'ALTER TABLE app_info ADD COLUMN demo_auto_login_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''演示账号自动登录开关，1启用0关闭'' AFTER version_prompt_enabled',
    'SET @noop_app_info_demo_auto_login_column = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V159__extend_pay_tool_snapshot_and_trade_payment_tools.sql]
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_bank_card_fund_detail'
                     AND COLUMN_NAME = 'inst_id'
               )
                   THEN 'SELECT ''skip add pay_bank_card_fund_detail.inst_id'''
               ELSE 'ALTER TABLE pay_bank_card_fund_detail ADD COLUMN inst_id VARCHAR(32) NULL COMMENT ''机构ID（银行机构码）'' AFTER channel'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_bank_card_fund_detail'
                     AND COLUMN_NAME = 'inst_channel_code'
               )
                   THEN 'SELECT ''skip add pay_bank_card_fund_detail.inst_channel_code'''
               ELSE 'ALTER TABLE pay_bank_card_fund_detail ADD COLUMN inst_channel_code VARCHAR(32) NULL COMMENT ''机构渠道编码'' AFTER inst_id'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_bank_card_fund_detail'
                     AND COLUMN_NAME = 'pay_channel_code'
               )
                   THEN 'SELECT ''skip add pay_bank_card_fund_detail.pay_channel_code'''
               ELSE 'ALTER TABLE pay_bank_card_fund_detail ADD COLUMN pay_channel_code VARCHAR(32) NULL COMMENT ''支付渠道编码'' AFTER inst_channel_code'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_bank_card_fund_detail'
                     AND COLUMN_NAME = 'bank_code'
               )
                   THEN 'SELECT ''skip add pay_bank_card_fund_detail.bank_code'''
               ELSE 'ALTER TABLE pay_bank_card_fund_detail ADD COLUMN bank_code VARCHAR(32) NULL COMMENT ''银行编码'' AFTER pay_channel_code'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_bank_card_fund_detail'
                     AND COLUMN_NAME = 'bank_name'
               )
                   THEN 'SELECT ''skip add pay_bank_card_fund_detail.bank_name'''
               ELSE 'ALTER TABLE pay_bank_card_fund_detail ADD COLUMN bank_name VARCHAR(64) NULL COMMENT ''银行名称'' AFTER bank_code'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_bank_card_fund_detail'
                     AND COLUMN_NAME = 'card_type'
               )
                   THEN 'SELECT ''skip add pay_bank_card_fund_detail.card_type'''
               ELSE 'ALTER TABLE pay_bank_card_fund_detail ADD COLUMN card_type VARCHAR(32) NULL COMMENT ''卡类型'' AFTER bank_name'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_bank_card_fund_detail'
                     AND COLUMN_NAME = 'card_holder_name'
               )
                   THEN 'SELECT ''skip add pay_bank_card_fund_detail.card_holder_name'''
               ELSE 'ALTER TABLE pay_bank_card_fund_detail ADD COLUMN card_holder_name VARCHAR(64) NULL COMMENT ''持卡人姓名'' AFTER card_type'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_bank_card_fund_detail'
                     AND COLUMN_NAME = 'card_tail_no'
               )
                   THEN 'SELECT ''skip add pay_bank_card_fund_detail.card_tail_no'''
               ELSE 'ALTER TABLE pay_bank_card_fund_detail ADD COLUMN card_tail_no VARCHAR(8) NULL COMMENT ''银行卡尾号'' AFTER card_holder_name'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_bank_card_fund_detail'
                     AND COLUMN_NAME = 'tool_snapshot'
               )
                   THEN 'SELECT ''skip add pay_bank_card_fund_detail.tool_snapshot'''
               ELSE 'ALTER TABLE pay_bank_card_fund_detail ADD COLUMN tool_snapshot TEXT NULL COMMENT ''支付工具快照'' AFTER card_tail_no'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_bank_card_fund_detail'
                     AND INDEX_NAME = 'idx_pay_bank_card_inst_channel_code'
               )
                   THEN 'SELECT ''skip add idx_pay_bank_card_inst_channel_code'''
               ELSE 'ALTER TABLE pay_bank_card_fund_detail ADD KEY idx_pay_bank_card_inst_channel_code (inst_channel_code)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'pay_bank_card_fund_detail'
                     AND INDEX_NAME = 'idx_pay_bank_card_pay_channel_code'
               )
                   THEN 'SELECT ''skip add idx_pay_bank_card_pay_channel_code'''
               ELSE 'ALTER TABLE pay_bank_card_fund_detail ADD KEY idx_pay_bank_card_pay_channel_code (pay_channel_code)'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @ddl = (
    SELECT CASE
               WHEN EXISTS (
                   SELECT 1
                   FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'trade_order'
                     AND COLUMN_NAME = 'payment_tool_snapshot'
               )
                   THEN 'SELECT ''skip add trade_order.payment_tool_snapshot'''
               ELSE 'ALTER TABLE trade_order ADD COLUMN payment_tool_snapshot TEXT NULL COMMENT ''支付工具列表快照'' AFTER metadata'
        END
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V160__create_app_login_device_account_whitelist_table.sql]
CREATE TABLE IF NOT EXISTS app_login_device_account_whitelist (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    app_code VARCHAR(64) NOT NULL COMMENT '应用编码',
    device_id VARCHAR(128) NOT NULL COMMENT '设备ID',
    login_id VARCHAR(32) NOT NULL COMMENT '登录账号',
    nickname VARCHAR(64) NOT NULL DEFAULT '' COMMENT '下拉展示昵称',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用，1启用0停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_login_device_account_whitelist (app_code, device_id, login_id),
    KEY idx_app_login_device_account_whitelist_lookup (app_code, device_id, enabled),
    KEY idx_app_login_device_account_whitelist_login (app_code, login_id, enabled)
) COMMENT='登录设备白名单账号配置';

-- [V161__add_login_device_binding_check_switch_to_app_info.sql]
SET @login_device_binding_check_column_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'app_info'
      AND COLUMN_NAME = 'login_device_binding_check_enabled'
);
SET @ddl = IF(
    @login_device_binding_check_column_exists = 0,
    'ALTER TABLE app_info ADD COLUMN login_device_binding_check_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''登录本机注册校验开关，1启用0关闭'' AFTER demo_auto_login_enabled',
    'SET @noop_app_info_login_device_binding_check_column = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- [V162__enforce_non_negative_account_balances.sql]
SET @wallet_non_negative_constraint_exists = (
    SELECT COUNT(1)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'wallet_account'
      AND CONSTRAINT_NAME = 'chk_wallet_account_non_negative'
);
SET @ddl_wallet_non_negative = IF(
    @wallet_non_negative_constraint_exists = 0,
    'ALTER TABLE wallet_account ADD CONSTRAINT chk_wallet_account_non_negative CHECK (available_balance >= 0 AND reserved_balance >= 0)',
    'SET @noop_wallet_account_non_negative = 1'
);
PREPARE stmt_wallet_non_negative FROM @ddl_wallet_non_negative;
EXECUTE stmt_wallet_non_negative;
DEALLOCATE PREPARE stmt_wallet_non_negative;
SET @fund_non_negative_constraint_exists = (
    SELECT COUNT(1)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fund_account'
      AND CONSTRAINT_NAME = 'chk_fund_account_non_negative'
);
SET @ddl_fund_non_negative = IF(
    @fund_non_negative_constraint_exists = 0,
    'ALTER TABLE fund_account ADD CONSTRAINT chk_fund_account_non_negative CHECK (available_share >= 0 AND frozen_share >= 0 AND pending_subscribe_amount >= 0 AND pending_redeem_share >= 0 AND accumulated_income >= 0 AND yesterday_income >= 0 AND latest_nav >= 0)',
    'SET @noop_fund_account_non_negative = 1'
);
PREPARE stmt_fund_non_negative FROM @ddl_fund_non_negative;
EXECUTE stmt_fund_non_negative;
DEALLOCATE PREPARE stmt_fund_non_negative;
SET @credit_non_negative_constraint_exists = (
    SELECT COUNT(1)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'credit_account'
      AND CONSTRAINT_NAME = 'chk_credit_account_non_negative'
);
SET @ddl_credit_non_negative = IF(
    @credit_non_negative_constraint_exists = 0,
    'ALTER TABLE credit_account ADD CONSTRAINT chk_credit_account_non_negative CHECK (total_limit >= 0 AND principal_balance >= 0 AND principal_unreach_amount >= 0 AND overdue_principal_balance >= 0 AND overdue_principal_unreach_amount >= 0 AND interest_balance >= 0 AND fine_balance >= 0)',
    'SET @noop_credit_account_non_negative = 1'
);
PREPARE stmt_credit_non_negative FROM @ddl_credit_non_negative;
EXECUTE stmt_credit_non_negative;
DEALLOCATE PREPARE stmt_credit_non_negative;

-- [V164__add_demo_provisioning_config_to_app_info.sql]
SET @demo_template_login_id_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'app_info'
      AND COLUMN_NAME = 'demo_template_login_id'
);
SET @ddl = IF(
    @demo_template_login_id_exists = 0,
    'ALTER TABLE app_info ADD COLUMN demo_template_login_id VARCHAR(32) NULL COMMENT ''演示模板登录号'' AFTER login_device_binding_check_enabled',
    'SET @noop_demo_template_login_id = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @demo_contact_login_id_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'app_info'
      AND COLUMN_NAME = 'demo_contact_login_id'
);
SET @ddl = IF(
    @demo_contact_login_id_exists = 0,
    'ALTER TABLE app_info ADD COLUMN demo_contact_login_id VARCHAR(32) NULL COMMENT ''演示联系人登录号'' AFTER demo_template_login_id',
    'SET @noop_demo_contact_login_id = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @demo_login_password_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'app_info'
      AND COLUMN_NAME = 'demo_login_password'
);
SET @ddl = IF(
    @demo_login_password_exists = 0,
    'ALTER TABLE app_info ADD COLUMN demo_login_password VARCHAR(64) NULL COMMENT ''演示注册默认密码'' AFTER demo_contact_login_id',
    'SET @noop_demo_login_password = 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET FOREIGN_KEY_CHECKS = 1;
