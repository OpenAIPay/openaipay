-- Auto-generated base dictionary/config DML (no business demo data)
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- [V14__create_pricing_tables_and_permissions.sql] pricing_rule
INSERT INTO pricing_rule (
    rule_code,
    rule_name,
    business_scene_code,
    payment_method,
    currency_code,
    fee_mode,
    fee_rate,
    fixed_fee,
    min_fee,
    max_fee,
    fee_bearer,
    priority,
    status,
    valid_from,
    valid_to,
    rule_payload,
    created_by,
    updated_by
)
VALUES (
    'DEFAULT_AIPAY_CNY',
    '爱付默认计费规则',
    'ALL',
    'ALL',
    'CNY',
    'RATE_PLUS_FIXED',
    0.006000,
    0.00,
    0.10,
    20.00,
    'PAYEE',
    100,
    'ACTIVE',
    NULL,
    NULL,
    JSON_OBJECT('remark', 'default pricing rule for aipay style scene'),
    'system',
    'system'
)
ON DUPLICATE KEY UPDATE rule_code = rule_code;

-- [V44__add_zero_fee_pricing_rules_for_deposit.sql] pricing_rule
INSERT INTO pricing_rule (
    rule_code,
    rule_name,
    business_scene_code,
    payment_method,
    currency_code,
    fee_mode,
    fee_rate,
    fixed_fee,
    min_fee,
    max_fee,
    fee_bearer,
    priority,
    status,
    valid_from,
    valid_to,
    rule_payload,
    created_by,
    updated_by,
    created_at,
    updated_at
)
SELECT
    'DEPOSIT_APP_BANK_CARD_ZERO_FEE',
    'APP充值银行卡免手续费',
    'APP_DEPOSIT',
    'BANK_CARD',
    'CNY',
    'FIXED',
    0.000000,
    0.00,
    0.00,
    0.00,
    'PAYEE',
    200,
    'ACTIVE',
    NULL,
    NULL,
    JSON_OBJECT('note', 'APP充值走银行卡时免手续费'),
    'flyway',
    'flyway',
    NOW(),
    NOW()
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM pricing_rule
    WHERE rule_code = 'DEPOSIT_APP_BANK_CARD_ZERO_FEE'
);

-- [V44__add_zero_fee_pricing_rules_for_deposit.sql] pricing_rule
UPDATE pricing_rule
SET rule_name = 'APP充值银行卡免手续费',
    business_scene_code = 'APP_DEPOSIT',
    payment_method = 'BANK_CARD',
    currency_code = 'CNY',
    fee_mode = 'FIXED',
    fee_rate = 0.000000,
    fixed_fee = 0.00,
    min_fee = 0.00,
    max_fee = 0.00,
    fee_bearer = 'PAYEE',
    priority = 200,
    status = 'ACTIVE',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE rule_code = 'DEPOSIT_APP_BANK_CARD_ZERO_FEE';

-- [V44__add_zero_fee_pricing_rules_for_deposit.sql] pricing_rule
INSERT INTO pricing_rule (
    rule_code,
    rule_name,
    business_scene_code,
    payment_method,
    currency_code,
    fee_mode,
    fee_rate,
    fixed_fee,
    min_fee,
    max_fee,
    fee_bearer,
    priority,
    status,
    valid_from,
    valid_to,
    rule_payload,
    created_by,
    updated_by,
    created_at,
    updated_at
)
SELECT
    'DEPOSIT_TRADE_BANK_CARD_ZERO_FEE',
    '交易充值银行卡免手续费',
    'TRADE_DEPOSIT',
    'BANK_CARD',
    'CNY',
    'FIXED',
    0.000000,
    0.00,
    0.00,
    0.00,
    'PAYEE',
    200,
    'ACTIVE',
    NULL,
    NULL,
    JSON_OBJECT('note', '交易域充值走银行卡时免手续费'),
    'flyway',
    'flyway',
    NOW(),
    NOW()
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM pricing_rule
    WHERE rule_code = 'DEPOSIT_TRADE_BANK_CARD_ZERO_FEE'
);

-- [V44__add_zero_fee_pricing_rules_for_deposit.sql] pricing_rule
UPDATE pricing_rule
SET rule_name = '交易充值银行卡免手续费',
    business_scene_code = 'TRADE_DEPOSIT',
    payment_method = 'BANK_CARD',
    currency_code = 'CNY',
    fee_mode = 'FIXED',
    fee_rate = 0.000000,
    fixed_fee = 0.00,
    min_fee = 0.00,
    max_fee = 0.00,
    fee_bearer = 'PAYEE',
    priority = 200,
    status = 'ACTIVE',
    updated_by = 'flyway',
    updated_at = NOW()
WHERE rule_code = 'DEPOSIT_TRADE_BANK_CARD_ZERO_FEE';

-- [V73__seed_openaipay_ios_app_version_data.sql] app_info
-- 初始化 iPhone 客户端应用与首个版本数据。
-- 目标：保证 OPENAIPAY_IOS 在新环境中开箱可用于版本检查、设备绑定与访问记录落库。
-- 策略：仅在不存在同编码/同版本号记录时插入，不覆盖已有人工维护数据。

INSERT INTO app_info (
    app_code,
    app_name,
    status
)
SELECT
    'OPENAIPAY_IOS',
    'OpenAiPay iPhone',
    'ENABLED'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM app_info
    WHERE app_code = 'OPENAIPAY_IOS'
);

-- [V73__seed_openaipay_ios_app_version_data.sql] app_version
INSERT INTO app_version (
    version_code,
    app_code,
    client_type,
    app_version_no,
    update_type,
    update_prompt_frequency,
    version_description,
    release_region_list_text,
    target_region_list_text,
    min_supported_version_no,
    latest_published_version,
    status
)
SELECT
    'OPENAIPAY_IOS_VER_1_0',
    'OPENAIPAY_IOS',
    'IOS_IPHONE',
    '1.0',
    'OPTIONAL',
    'ONCE_PER_VERSION',
    'OpenAiPay iPhone 初始化版本，用于承接 iOS 客户端启动版本校验、设备上报与访问记录埋点。',
    'CN',
    'CN',
    '1.0',
    1,
    'ENABLED'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM app_version
    WHERE version_code = 'OPENAIPAY_IOS_VER_1_0'
       OR (app_code = 'OPENAIPAY_IOS' AND client_type = 'IOS_IPHONE' AND app_version_no = '1.0')
);

-- [V73__seed_openaipay_ios_app_version_data.sql] app_version
INSERT INTO app_version (
    version_code,
    app_code,
    client_type,
    app_version_no,
    update_type,
    update_prompt_frequency,
    version_description,
    release_region_list_text,
    target_region_list_text,
    min_supported_version_no,
    latest_published_version,
    status
)
SELECT
    'OPENAIPAY_IOS_VER_1_1',
    'OPENAIPAY_IOS',
    'IOS_IPHONE',
    '1.1',
    'RECOMMENDED',
    'ALWAYS',
    'OpenAiPay iPhone 测试更新版本：用于验证启动版本检查、推荐更新弹窗、设备绑定与访问记录按版本号落库。',
    'CN',
    'CN',
    '1.0',
    0,
    'ENABLED'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM app_version
    WHERE version_code = 'OPENAIPAY_IOS_VER_1_1'
       OR (app_code = 'OPENAIPAY_IOS' AND client_type = 'IOS_IPHONE' AND app_version_no = '1.1')
);

-- [V75__activate_openaipay_ios_test_update.sql] app_version
-- 激活 OPENAIPAY_IOS 的测试更新版本。
-- 目标：让当前 iOS 客户端版本 1.0 在启动检查时能直接命中 1.1 推荐更新。

UPDATE app_version
SET latest_published_version = CASE
    WHEN version_code = 'OPENAIPAY_IOS_VER_1_1' THEN 1
    WHEN version_code = 'OPENAIPAY_IOS_VER_1_0' THEN 0
    ELSE latest_published_version
END,
updated_at = CURRENT_TIMESTAMP
WHERE app_code = 'OPENAIPAY_IOS'
  AND version_code IN ('OPENAIPAY_IOS_VER_1_0', 'OPENAIPAY_IOS_VER_1_1');

-- [V77__add_withdraw_bank_card_pricing_rules.sql] pricing_rule
INSERT INTO pricing_rule (
    rule_code,
    rule_name,
    business_scene_code,
    payment_method,
    currency_code,
    fee_mode,
    fee_rate,
    fixed_fee,
    min_fee,
    max_fee,
    fee_bearer,
    priority,
    status,
    valid_from,
    valid_to,
    rule_payload,
    created_by,
    updated_by,
    created_at,
    updated_at
)
SELECT
    'WITHDRAW_APP_BANK_CARD_RATE_FEE',
    'APP提现银行卡手续费规则',
    'APP_WITHDRAW',
    'BANK_CARD',
    'CNY',
    'RATE',
    0.001000,
    0.00,
    0.00,
    0.00,
    'PAYEE',
    210,
    'ACTIVE',
    NULL,
    NULL,
    JSON_OBJECT('note', 'APP提现走银行卡时按千分之一收取手续费，手续费从到账金额中扣减'),
    'flyway',
    'flyway',
    NOW(),
    NOW()
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM pricing_rule
    WHERE rule_code = 'WITHDRAW_APP_BANK_CARD_RATE_FEE'
);

-- [V77__add_withdraw_bank_card_pricing_rules.sql] pricing_rule
UPDATE pricing_rule
SET rule_name = 'APP提现银行卡手续费规则',
    business_scene_code = 'APP_WITHDRAW',
    payment_method = 'BANK_CARD',
    currency_code = 'CNY',
    fee_mode = 'RATE',
    fee_rate = 0.001000,
    fixed_fee = 0.00,
    min_fee = 0.00,
    max_fee = 0.00,
    fee_bearer = 'PAYEE',
    priority = 210,
    status = 'ACTIVE',
    rule_payload = JSON_OBJECT('note', 'APP提现走银行卡时按千分之一收取手续费，手续费从到账金额中扣减'),
    updated_by = 'flyway',
    updated_at = NOW()
WHERE rule_code = 'WITHDRAW_APP_BANK_CARD_RATE_FEE';

-- [V77__add_withdraw_bank_card_pricing_rules.sql] pricing_rule
INSERT INTO pricing_rule (
    rule_code,
    rule_name,
    business_scene_code,
    payment_method,
    currency_code,
    fee_mode,
    fee_rate,
    fixed_fee,
    min_fee,
    max_fee,
    fee_bearer,
    priority,
    status,
    valid_from,
    valid_to,
    rule_payload,
    created_by,
    updated_by,
    created_at,
    updated_at
)
SELECT
    'WITHDRAW_TRADE_BANK_CARD_RATE_FEE',
    '交易提现银行卡手续费规则',
    'TRADE_WITHDRAW',
    'BANK_CARD',
    'CNY',
    'RATE',
    0.001000,
    0.00,
    0.00,
    0.00,
    'PAYEE',
    210,
    'ACTIVE',
    NULL,
    NULL,
    JSON_OBJECT('note', '交易提现走银行卡时按千分之一收取手续费，手续费从到账金额中扣减'),
    'flyway',
    'flyway',
    NOW(),
    NOW()
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM pricing_rule
    WHERE rule_code = 'WITHDRAW_TRADE_BANK_CARD_RATE_FEE'
);

-- [V77__add_withdraw_bank_card_pricing_rules.sql] pricing_rule
UPDATE pricing_rule
SET rule_name = '交易提现银行卡手续费规则',
    business_scene_code = 'TRADE_WITHDRAW',
    payment_method = 'BANK_CARD',
    currency_code = 'CNY',
    fee_mode = 'RATE',
    fee_rate = 0.001000,
    fixed_fee = 0.00,
    min_fee = 0.00,
    max_fee = 0.00,
    fee_bearer = 'PAYEE',
    priority = 210,
    status = 'ACTIVE',
    rule_payload = JSON_OBJECT('note', '交易提现走银行卡时按千分之一收取手续费，手续费从到账金额中扣减'),
    updated_by = 'flyway',
    updated_at = NOW()
WHERE rule_code = 'WITHDRAW_TRADE_BANK_CARD_RATE_FEE';

-- [V91__create_accounting_tables_and_admin_menu.sql] acct_subject
INSERT INTO acct_subject
(subject_code, subject_name, subject_type, balance_direction, parent_subject_code, level_no, enabled, remark, created_at, updated_at)
VALUES
    ('1001', '备付金银行存款', 'ASSET', 'DEBIT', NULL, 1, 1, '平台备付金银行账户', NOW(3), NOW(3)),
    ('1002', '渠道在途资金', 'ASSET', 'DEBIT', NULL, 1, 1, '外部渠道结算在途', NOW(3), NOW(3)),
    ('1003', '消费信贷应收', 'ASSET', 'DEBIT', NULL, 1, 1, '花呗借呗等消费信贷应收', NOW(3), NOW(3)),
    ('2001', '用户钱包负债', 'LIABILITY', 'CREDIT', NULL, 1, 1, '用户余额账户负债', NOW(3), NOW(3)),
    ('2002', '商户待结算负债', 'LIABILITY', 'CREDIT', NULL, 1, 1, '商户待结算资金', NOW(3), NOW(3)),
    ('2003', '用户提现中负债', 'LIABILITY', 'CREDIT', NULL, 1, 1, '提现处理中冻结或在途资金', NOW(3), NOW(3)),
    ('2004', '基金份额负债', 'LIABILITY', 'CREDIT', NULL, 1, 1, '余额宝基金份额或申赎在途', NOW(3), NOW(3)),
    ('5001', '支付手续费收入', 'INCOME', 'CREDIT', NULL, 1, 1, '平台手续费收入', NOW(3), NOW(3)),
    ('6001', '通道手续费成本', 'EXPENSE', 'DEBIT', NULL, 1, 1, '支付通道成本', NOW(3), NOW(3)),
    ('9999', '待映射会计科目', 'MEMO', 'DEBIT', NULL, 1, 1, '规则未精细化前的兜底科目', NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE subject_code = subject_code;

-- [V97__seed_standard_accounting_subjects.sql] acct_subject
-- 清理旧版示例科目，统一由新版层级科目表承接。
DELETE FROM acct_subject
WHERE subject_code IN ('1001', '1002', '1003', '2001', '2002', '2003', '2004', '5001', '6001', '9999');

-- [V97__seed_standard_accounting_subjects.sql] acct_subject
INSERT INTO acct_subject
    (subject_code, subject_name, subject_type, balance_direction, parent_subject_code, level_no, enabled, remark, created_at, updated_at)
VALUES
    ('100000', '资产类', 'ASSET', 'DEBIT', NULL, 1, 1, '资产类根科目', NOW(3), NOW(3)),
    ('100100', '备付金与银行资金', 'ASSET', 'DEBIT', '100000', 2, 1, '平台备付金、银行户、冻结户等资金资产', NOW(3), NOW(3)),
    ('100101', '备付金银行存款', 'ASSET', 'DEBIT', '100100', 3, 1, '平台自有备付金/出金银行存款', NOW(3), NOW(3)),
    ('100102', '备付金冻结存款', 'ASSET', 'DEBIT', '100100', 3, 1, '银行冻结或监管冻结的备付金', NOW(3), NOW(3)),
    ('100200', '渠道清算资产', 'ASSET', 'DEBIT', '100000', 2, 1, '通道已扣款但尚未完成最终清算的资产', NOW(3), NOW(3)),
    ('100201', '入金渠道待清算资产', 'ASSET', 'DEBIT', '100200', 3, 1, '银行卡充值、收单后待与渠道清算的资金', NOW(3), NOW(3)),
    ('100202', '出金渠道待清算资产', 'ASSET', 'DEBIT', '100200', 3, 1, '提现出金发起后待渠道回执或对账完成的资金', NOW(3), NOW(3)),
    ('100300', '消费信贷应收', 'ASSET', 'DEBIT', '100000', 2, 1, '花呗/借呗等内部信用支付形成的应收资产', NOW(3), NOW(3)),
    ('100301', '花呗应收', 'ASSET', 'DEBIT', '100300', 3, 1, '花呗类信用支付应收', NOW(3), NOW(3)),
    ('100302', '借呗应收', 'ASSET', 'DEBIT', '100300', 3, 1, '借呗/现金贷类信用支付应收', NOW(3), NOW(3)),
    ('100400', '基金申赎在途资产', 'ASSET', 'DEBIT', '100000', 2, 1, '基金申购赎回尚未确认或到账的在途资产', NOW(3), NOW(3)),
    ('100401', '基金赎回待到账', 'ASSET', 'DEBIT', '100400', 3, 1, '余额宝等货币基金赎回在途资产', NOW(3), NOW(3)),

    ('200000', '负债类', 'LIABILITY', 'CREDIT', NULL, 1, 1, '负债类根科目', NOW(3), NOW(3)),
    ('200100', '用户资金负债', 'LIABILITY', 'CREDIT', '200000', 2, 1, '对用户持有资金承担的负债', NOW(3), NOW(3)),
    ('200101', '用户钱包可用余额', 'LIABILITY', 'CREDIT', '200100', 3, 1, '用户钱包可用余额负债', NOW(3), NOW(3)),
    ('200102', '用户钱包冻结余额', 'LIABILITY', 'CREDIT', '200100', 3, 1, '用户钱包冻结资金负债', NOW(3), NOW(3)),
    ('200200', '待结算负债', 'LIABILITY', 'CREDIT', '200000', 2, 1, '支付成功后待释放给商户/用户的钱款', NOW(3), NOW(3)),
    ('200201', '待结算资金', 'LIABILITY', 'CREDIT', '200200', 3, 1, '待结算资金主科目', NOW(3), NOW(3)),
    ('200202', '待结算冻结资金', 'LIABILITY', 'CREDIT', '200200', 3, 1, '待结算但被风控/人工冻结的资金', NOW(3), NOW(3)),
    ('200300', '用户提现中负债', 'LIABILITY', 'CREDIT', '200000', 2, 1, '用户提现流程中尚未兑付完成的负债', NOW(3), NOW(3)),
    ('200301', '用户提现处理中', 'LIABILITY', 'CREDIT', '200300', 3, 1, '已提交提现、待到账或待渠道处理', NOW(3), NOW(3)),
    ('200400', '理财资金负债', 'LIABILITY', 'CREDIT', '200000', 2, 1, '余额宝等基金份额及申赎在途负债', NOW(3), NOW(3)),
    ('200401', '余额宝已确认份额', 'LIABILITY', 'CREDIT', '200400', 3, 1, '已确认到用户名下的余额宝份额', NOW(3), NOW(3)),
    ('200402', '余额宝申购在途', 'LIABILITY', 'CREDIT', '200400', 3, 1, '余额宝申购待确认份额', NOW(3), NOW(3)),
    ('200403', '余额宝赎回在途', 'LIABILITY', 'CREDIT', '200400', 3, 1, '余额宝赎回待到账负债', NOW(3), NOW(3)),
    ('200500', '营销权益负债', 'LIABILITY', 'CREDIT', '200000', 2, 1, '红包、优惠券等平台营销权益负债', NOW(3), NOW(3)),
    ('200501', '红包余额负债', 'LIABILITY', 'CREDIT', '200500', 3, 1, '红包待领取或待核销金额', NOW(3), NOW(3)),
    ('200502', '优惠券资金负债', 'LIABILITY', 'CREDIT', '200500', 3, 1, '优惠券补贴待核销金额', NOW(3), NOW(3)),

    ('500000', '收入类', 'INCOME', 'CREDIT', NULL, 1, 1, '收入类根科目', NOW(3), NOW(3)),
    ('500100', '支付服务收入', 'INCOME', 'CREDIT', '500000', 2, 1, '支付、提现等平台收费收入', NOW(3), NOW(3)),
    ('500101', '支付手续费收入', 'INCOME', 'CREDIT', '500100', 3, 1, '支付成功产生的平台手续费收入', NOW(3), NOW(3)),
    ('500102', '提现手续费收入', 'INCOME', 'CREDIT', '500100', 3, 1, '提现成功产生的平台手续费收入', NOW(3), NOW(3)),
    ('500200', '理财服务收入', 'INCOME', 'CREDIT', '500000', 2, 1, '理财销售与运营类收入', NOW(3), NOW(3)),
    ('500201', '基金销售服务费收入', 'INCOME', 'CREDIT', '500200', 3, 1, '余额宝/基金销售服务费', NOW(3), NOW(3)),
    ('500300', '信贷服务收入', 'INCOME', 'CREDIT', '500000', 2, 1, '信贷服务类收入', NOW(3), NOW(3)),
    ('500301', '信贷息费收入', 'INCOME', 'CREDIT', '500300', 3, 1, '花呗/借呗等信用业务息费', NOW(3), NOW(3)),

    ('600000', '费用类', 'EXPENSE', 'DEBIT', NULL, 1, 1, '费用类根科目', NOW(3), NOW(3)),
    ('600100', '通道与清算成本', 'EXPENSE', 'DEBIT', '600000', 2, 1, '支付通道、银行清算及网络费用', NOW(3), NOW(3)),
    ('600101', '入金通道手续费成本', 'EXPENSE', 'DEBIT', '600100', 3, 1, '银行卡充值/收单通道成本', NOW(3), NOW(3)),
    ('600102', '出金通道手续费成本', 'EXPENSE', 'DEBIT', '600100', 3, 1, '提现出金通道成本', NOW(3), NOW(3)),
    ('600200', '营销补贴成本', 'EXPENSE', 'DEBIT', '600000', 2, 1, '红包、券补贴及营销活动成本', NOW(3), NOW(3)),
    ('600201', '红包营销成本', 'EXPENSE', 'DEBIT', '600200', 3, 1, '红包类营销补贴成本', NOW(3), NOW(3)),
    ('600202', '优惠券营销成本', 'EXPENSE', 'DEBIT', '600200', 3, 1, '优惠券类营销补贴成本', NOW(3), NOW(3)),
    ('600300', '资金成本', 'EXPENSE', 'DEBIT', '600000', 2, 1, '信贷、理财等业务的资金占用成本', NOW(3), NOW(3)),
    ('600301', '信贷资金成本', 'EXPENSE', 'DEBIT', '600300', 3, 1, '花呗/借呗等内部授信资金成本', NOW(3), NOW(3)),

    ('900000', '备查类', 'MEMO', 'DEBIT', NULL, 1, 1, '备查类根科目', NOW(3), NOW(3)),
    ('900101', '待映射过账科目', 'MEMO', 'DEBIT', '900000', 2, 1, '新业务场景未建科目前的兜底科目', NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE
    subject_name = VALUES(subject_name),
    subject_type = VALUES(subject_type),
    balance_direction = VALUES(balance_direction),
    parent_subject_code = VALUES(parent_subject_code),
    level_no = VALUES(level_no),
    enabled = VALUES(enabled),
    remark = VALUES(remark),
    updated_at = VALUES(updated_at);

-- [V102__normalize_openaipay_ios_version_number_format.sql] app_version
UPDATE app_version target
LEFT JOIN app_version existing
  ON existing.version_code = 'OPENAIPAY_IOS_VER_26_314_1'
  OR (existing.app_code = 'OPENAIPAY_IOS' AND existing.client_type = 'IOS_IPHONE' AND existing.app_version_no = '26.314.1')
SET target.version_code = 'OPENAIPAY_IOS_VER_26_314_1',
    target.app_version_no = '26.314.1',
    target.min_supported_version_no = '26.314.1',
    target.version_description = 'OpenAiPay iPhone 历史稳定版本，版本号已统一为 YY.MMDD.SEQUENCE 格式。',
    target.updated_at = CURRENT_TIMESTAMP
WHERE target.app_code = 'OPENAIPAY_IOS'
  AND target.client_type = 'IOS_IPHONE'
  AND (target.version_code = 'OPENAIPAY_IOS_VER_1_0' OR target.app_version_no = '1.0')
  AND existing.id IS NULL;

-- [V102__normalize_openaipay_ios_version_number_format.sql] app_version
UPDATE app_version target
LEFT JOIN app_version existing
  ON existing.version_code = 'OPENAIPAY_IOS_VER_26_315_1'
  OR (existing.app_code = 'OPENAIPAY_IOS' AND existing.client_type = 'IOS_IPHONE' AND existing.app_version_no = '26.315.1')
SET target.version_code = 'OPENAIPAY_IOS_VER_26_315_1',
    target.app_version_no = '26.315.1',
    target.min_supported_version_no = '26.315.1',
    target.version_description = 'OpenAiPay iPhone 当前版本，版本号已统一为 YY.MMDD.SEQUENCE 格式。',
    target.latest_published_version = 1,
    target.status = 'ENABLED',
    target.updated_at = CURRENT_TIMESTAMP
WHERE target.app_code = 'OPENAIPAY_IOS'
  AND target.client_type = 'IOS_IPHONE'
  AND (target.version_code = 'OPENAIPAY_IOS_VER_1_1' OR target.app_version_no = '1.1')
  AND existing.id IS NULL;

-- [V102__normalize_openaipay_ios_version_number_format.sql] app_version
UPDATE app_version
SET latest_published_version = CASE
    WHEN version_code = 'OPENAIPAY_IOS_VER_26_315_1' OR app_version_no = '26.315.1' THEN 1
    WHEN version_code = 'OPENAIPAY_IOS_VER_26_314_1' OR app_version_no = '26.314.1' THEN 0
    ELSE latest_published_version
END,
updated_at = CURRENT_TIMESTAMP
WHERE app_code = 'OPENAIPAY_IOS'
  AND client_type = 'IOS_IPHONE'
  AND (
      version_code IN ('OPENAIPAY_IOS_VER_26_314_1', 'OPENAIPAY_IOS_VER_26_315_1')
      OR app_version_no IN ('26.314.1', '26.315.1')
  );

-- [V103__add_zero_fee_pricing_rules_for_chat_red_packet.sql] pricing_rule
INSERT INTO pricing_rule (
    rule_code,
    rule_name,
    business_scene_code,
    payment_method,
    currency_code,
    fee_mode,
    fee_rate,
    fixed_fee,
    min_fee,
    max_fee,
    fee_bearer,
    priority,
    status,
    valid_from,
    valid_to,
    rule_payload,
    created_by,
    updated_by,
    created_at,
    updated_at
)
SELECT
    'RED_PACKET_SEND_ZERO_FEE',
    '聊天发红包免手续费',
    'CHAT_RED_PACKET_SEND',
    'ALL',
    'CNY',
    'FIXED',
    0.000000,
    0.00,
    0.00,
    0.00,
    'PAYEE',
    220,
    'ACTIVE',
    NULL,
    NULL,
    JSON_OBJECT('note', '聊天发红包资金冻结与划转均不收手续费'),
    'flyway',
    'flyway',
    NOW(),
    NOW()
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM pricing_rule
    WHERE rule_code = 'RED_PACKET_SEND_ZERO_FEE'
);

-- [V103__add_zero_fee_pricing_rules_for_chat_red_packet.sql] pricing_rule
UPDATE pricing_rule
SET rule_name = '聊天发红包免手续费',
    business_scene_code = 'CHAT_RED_PACKET_SEND',
    payment_method = 'ALL',
    currency_code = 'CNY',
    fee_mode = 'FIXED',
    fee_rate = 0.000000,
    fixed_fee = 0.00,
    min_fee = 0.00,
    max_fee = 0.00,
    fee_bearer = 'PAYEE',
    priority = 220,
    status = 'ACTIVE',
    rule_payload = JSON_OBJECT('note', '聊天发红包资金冻结与划转均不收手续费'),
    updated_by = 'flyway',
    updated_at = NOW()
WHERE rule_code = 'RED_PACKET_SEND_ZERO_FEE';

-- [V103__add_zero_fee_pricing_rules_for_chat_red_packet.sql] pricing_rule
INSERT INTO pricing_rule (
    rule_code,
    rule_name,
    business_scene_code,
    payment_method,
    currency_code,
    fee_mode,
    fee_rate,
    fixed_fee,
    min_fee,
    max_fee,
    fee_bearer,
    priority,
    status,
    valid_from,
    valid_to,
    rule_payload,
    created_by,
    updated_by,
    created_at,
    updated_at
)
SELECT
    'RED_PACKET_CLAIM_ZERO_FEE',
    '聊天收红包免手续费',
    'CHAT_RED_PACKET_CLAIM',
    'ALL',
    'CNY',
    'FIXED',
    0.000000,
    0.00,
    0.00,
    0.00,
    'PAYEE',
    220,
    'ACTIVE',
    NULL,
    NULL,
    JSON_OBJECT('note', '聊天收红包入余额时不收手续费'),
    'flyway',
    'flyway',
    NOW(),
    NOW()
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM pricing_rule
    WHERE rule_code = 'RED_PACKET_CLAIM_ZERO_FEE'
);

-- [V103__add_zero_fee_pricing_rules_for_chat_red_packet.sql] pricing_rule
UPDATE pricing_rule
SET rule_name = '聊天收红包免手续费',
    business_scene_code = 'CHAT_RED_PACKET_CLAIM',
    payment_method = 'ALL',
    currency_code = 'CNY',
    fee_mode = 'FIXED',
    fee_rate = 0.000000,
    fixed_fee = 0.00,
    min_fee = 0.00,
    max_fee = 0.00,
    fee_bearer = 'PAYEE',
    priority = 220,
    status = 'ACTIVE',
    rule_payload = JSON_OBJECT('note', '聊天收红包入余额时不收手续费'),
    updated_by = 'flyway',
    updated_at = NOW()
WHERE rule_code = 'RED_PACKET_CLAIM_ZERO_FEE';

-- [V123__rename_legacy_product_labels_for_display.sql] acct_subject
UPDATE acct_subject
SET subject_name = '爱花应收',
    remark = '爱花类信用支付应收'
WHERE subject_code = '100301';

-- [V123__rename_legacy_product_labels_for_display.sql] acct_subject
UPDATE acct_subject
SET subject_name = '爱借应收',
    remark = '爱借/现金贷类信用支付应收'
WHERE subject_code = '100302';

-- [V123__rename_legacy_product_labels_for_display.sql] acct_subject
UPDATE acct_subject
SET remark = '爱存等货币基金赎回在途资产'
WHERE subject_code = '100401';

-- [V123__rename_legacy_product_labels_for_display.sql] acct_subject
UPDATE acct_subject
SET remark = '爱花/爱借等内部信用支付形成的应收资产'
WHERE subject_code = '100300';

-- [V123__rename_legacy_product_labels_for_display.sql] acct_subject
UPDATE acct_subject
SET remark = '爱存等基金份额及申赎在途负债'
WHERE subject_code = '200400';

-- [V123__rename_legacy_product_labels_for_display.sql] acct_subject
UPDATE acct_subject
SET subject_name = '爱存已确认份额',
    remark = '已确认到用户名下的爱存份额'
WHERE subject_code = '200401';

-- [V123__rename_legacy_product_labels_for_display.sql] acct_subject
UPDATE acct_subject
SET subject_name = '爱存申购在途',
    remark = '爱存申购待确认份额'
WHERE subject_code = '200402';

-- [V123__rename_legacy_product_labels_for_display.sql] acct_subject
UPDATE acct_subject
SET subject_name = '爱存赎回在途',
    remark = '爱存赎回待到账负债'
WHERE subject_code = '200403';

-- [V123__rename_legacy_product_labels_for_display.sql] acct_subject
UPDATE acct_subject
SET remark = '爱存/基金销售服务费'
WHERE subject_code = '500201';

-- [V123__rename_legacy_product_labels_for_display.sql] acct_subject
UPDATE acct_subject
SET remark = '爱花/爱借等信用业务息费'
WHERE subject_code = '500301';

-- [V123__rename_legacy_product_labels_for_display.sql] acct_subject
UPDATE acct_subject
SET remark = '爱花/爱借等内部授信资金成本'
WHERE subject_code = '600301';

-- [V123__rename_legacy_product_labels_for_display.sql] acct_subject
UPDATE acct_subject
SET remark = '爱花爱借等消费信贷应收'
WHERE subject_code = '1003';

-- [V123__rename_legacy_product_labels_for_display.sql] acct_subject
UPDATE acct_subject
SET remark = '爱存基金份额或申赎在途'
WHERE subject_code = '2004';

-- [V127__backfill_aicredit_sign_items_and_legacy_subject_aliases.sql] acct_subject
-- 补齐历史兼容科目编码，避免旧口径查询命中空值。
INSERT INTO acct_subject (
    subject_code,
    subject_name,
    subject_type,
    balance_direction,
    parent_subject_code,
    level_no,
    enabled,
    remark
)
VALUES (
    '1003',
    '消费信贷应收',
    'ASSET',
    'DEBIT',
    '100000',
    2,
    1,
    '爱花爱借等消费信贷应收'
)
ON DUPLICATE KEY UPDATE
    subject_name = VALUES(subject_name),
    subject_type = VALUES(subject_type),
    balance_direction = VALUES(balance_direction),
    parent_subject_code = VALUES(parent_subject_code),
    level_no = VALUES(level_no),
    enabled = VALUES(enabled),
    remark = VALUES(remark);

-- [V127__backfill_aicredit_sign_items_and_legacy_subject_aliases.sql] acct_subject
INSERT INTO acct_subject (
    subject_code,
    subject_name,
    subject_type,
    balance_direction,
    parent_subject_code,
    level_no,
    enabled,
    remark
)
VALUES (
    '2004',
    '理财资金负债',
    'LIABILITY',
    'CREDIT',
    '200000',
    2,
    1,
    '爱存基金份额或申赎在途'
)
ON DUPLICATE KEY UPDATE
    subject_name = VALUES(subject_name),
    subject_type = VALUES(subject_type),
    balance_direction = VALUES(balance_direction),
    parent_subject_code = VALUES(parent_subject_code),
    level_no = VALUES(level_no),
    enabled = VALUES(enabled),
    remark = VALUES(remark);

-- [V161__add_login_device_binding_check_switch_to_app_info.sql] app_info
UPDATE app_info
SET login_device_binding_check_enabled = 1
WHERE login_device_binding_check_enabled IS NULL;
SET FOREIGN_KEY_CHECKS = 1;
