-- Auto-generated admin seed DML (single admin account + RBAC/menu baseline)
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- [V7__create_coupon_and_admin_tables.sql] admin_account
INSERT INTO admin_account (admin_id, username, display_name, password_sha256, account_status)
VALUES (9000000000000001, 'admin', '系统管理员', SHA2('Admin@123456', 256), 'ACTIVE')
ON DUPLICATE KEY UPDATE admin_id = admin_id;

-- [V7__create_coupon_and_admin_tables.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES
    ('dashboard', NULL, '工作台', '/admin/dashboard', 'dashboard', 10, 1),
    ('user-center', NULL, '用户中心', '/admin/users', 'user', 20, 1),
    ('coupon-center', NULL, '红包中心', '/admin/coupons', 'gift', 30, 1),
    ('coupon-template', 'coupon-center', '红包模板', '/admin/coupons/templates', 'template', 31, 1),
    ('coupon-issue', 'coupon-center', '红包发放', '/admin/coupons/issues', 'send', 32, 1),
    ('coupon-verify', 'coupon-center', '红包核销', '/admin/coupons/redeem', 'check', 33, 1),
    ('fund-center', NULL, '资金中心', '/admin/fund', 'wallet', 40, 1),
    ('risk-center', NULL, '风控中心', '/admin/risk', 'shield', 50, 1),
    ('system-center', NULL, '系统设置', '/admin/system', 'setting', 60, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V13__create_admin_rbac_tables.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES ('rbac-center', 'system-center', '权限管理', '/admin/system/rbac', 'safety-certificate', 61, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V13__create_admin_rbac_tables.sql] admin_rbac_module
INSERT INTO admin_rbac_module (module_code, module_name, module_desc, enabled, sort_no)
VALUES
    ('dashboard', '工作台', '后台首页与初始化上下文', 1, 10),
    ('coupon-center', '红包中心', '红包模板、发放、核销权限', 1, 20),
    ('fund-center', '资金中心', '资金账务与运营权限', 1, 30),
    ('risk-center', '风控中心', '风控策略与审核权限', 1, 40),
    ('system-center', '系统设置', '系统配置、菜单管理能力', 1, 50),
    ('rbac-center', '权限中心', '角色、权限、授权管理能力', 1, 60)
ON DUPLICATE KEY UPDATE module_code = module_code;

-- [V13__create_admin_rbac_tables.sql] admin_rbac_role
INSERT INTO admin_rbac_role (role_code, role_name, role_scope, role_status, is_builtin, role_desc)
VALUES
    ('SUPER_ADMIN', '超级管理员', 'PLATFORM', 'ACTIVE', 1, '拥有后台全部权限'),
    ('OPS_ADMIN', '运营管理员', 'COUPON', 'ACTIVE', 1, '负责红包运营与审核')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V13__create_admin_rbac_tables.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission (permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('admin.page_init', '后台初始化', 'dashboard', 'API', 'GET', '/api/admin/page-init', '读取后台首页初始化数据'),
    ('admin.menu.list', '菜单列表', 'dashboard', 'API', 'GET', '/api/admin/menus', '读取后台菜单列表'),
    ('coupon.template.create', '创建红包模板', 'coupon-center', 'API', 'POST', '/api/admin/coupons/templates', '新建红包模板'),
    ('coupon.template.update', '更新红包模板', 'coupon-center', 'API', 'PUT', '/api/admin/coupons/templates/{templateId}', '编辑红包模板'),
    ('coupon.template.status', '变更模板状态', 'coupon-center', 'API', 'PUT', '/api/admin/coupons/templates/{templateId}/status', '上下线红包模板'),
    ('coupon.template.view', '查看红包模板', 'coupon-center', 'API', 'GET', '/api/admin/coupons/templates/{templateId}', '查看红包模板详情'),
    ('coupon.template.list', '查询红包模板', 'coupon-center', 'API', 'GET', '/api/admin/coupons/templates', '查询红包模板列表'),
    ('coupon.issue.create', '发放红包', 'coupon-center', 'API', 'POST', '/api/admin/coupons/issue', '给用户发放红包'),
    ('coupon.issue.redeem', '核销红包', 'coupon-center', 'API', 'POST', '/api/admin/coupons/redeem', '核销用户红包'),
    ('coupon.issue.list_user', '查询用户红包', 'coupon-center', 'API', 'GET', '/api/admin/coupons/users/{userId}', '查询用户红包记录'),
    ('rbac.view', '查看权限模型', 'rbac-center', 'API', 'GET', '/api/admin/rbac/**', '查看角色权限配置'),
    ('rbac.admin_role.assign', '分配管理员角色', 'rbac-center', 'API', 'PUT', '/api/admin/rbac/admins/{adminId}/roles', '分配管理员角色'),
    ('rbac.role_permission.assign', '分配角色权限', 'rbac-center', 'API', 'PUT', '/api/admin/rbac/roles/{roleCode}/permissions', '分配角色权限'),
    ('rbac.role_menu.assign', '分配角色菜单', 'rbac-center', 'API', 'PUT', '/api/admin/rbac/roles/{roleCode}/menus', '分配角色菜单')
ON DUPLICATE KEY UPDATE permission_code = permission_code;

-- [V13__create_admin_rbac_tables.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
SELECT 'SUPER_ADMIN', p.permission_code, 'system'
FROM admin_rbac_permission p
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V13__create_admin_rbac_tables.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('OPS_ADMIN', 'admin.page_init', 'system'),
    ('OPS_ADMIN', 'admin.menu.list', 'system'),
    ('OPS_ADMIN', 'coupon.template.view', 'system'),
    ('OPS_ADMIN', 'coupon.template.list', 'system'),
    ('OPS_ADMIN', 'coupon.issue.create', 'system'),
    ('OPS_ADMIN', 'coupon.issue.redeem', 'system'),
    ('OPS_ADMIN', 'coupon.issue.list_user', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V13__create_admin_rbac_tables.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('SUPER_ADMIN', 'dashboard', 'system'),
    ('SUPER_ADMIN', 'user-center', 'system'),
    ('SUPER_ADMIN', 'coupon-center', 'system'),
    ('SUPER_ADMIN', 'coupon-template', 'system'),
    ('SUPER_ADMIN', 'coupon-issue', 'system'),
    ('SUPER_ADMIN', 'coupon-verify', 'system'),
    ('SUPER_ADMIN', 'fund-center', 'system'),
    ('SUPER_ADMIN', 'risk-center', 'system'),
    ('SUPER_ADMIN', 'system-center', 'system'),
    ('SUPER_ADMIN', 'rbac-center', 'system'),
    ('OPS_ADMIN', 'dashboard', 'system'),
    ('OPS_ADMIN', 'coupon-center', 'system'),
    ('OPS_ADMIN', 'coupon-template', 'system'),
    ('OPS_ADMIN', 'coupon-issue', 'system'),
    ('OPS_ADMIN', 'coupon-verify', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V13__create_admin_rbac_tables.sql] admin_rbac_admin_role
INSERT INTO admin_rbac_admin_role (admin_id, role_code, created_by)
VALUES (9000000000000001, 'SUPER_ADMIN', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V14__create_pricing_tables_and_permissions.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES
    ('pricing-center', NULL, '计费中心', '/admin/pricing', 'calculator', 41, 1),
    ('pricing-rule', 'pricing-center', '计费规则', '/admin/pricing/rules', 'table', 42, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V14__create_pricing_tables_and_permissions.sql] admin_rbac_module
INSERT INTO admin_rbac_module (module_code, module_name, module_desc, enabled, sort_no)
VALUES ('pricing-center', '计费中心', '计费规则配置与费率管理权限', 1, 35)
ON DUPLICATE KEY UPDATE module_code = module_code;

-- [V14__create_pricing_tables_and_permissions.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission (permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('pricing.rule.create', '创建计费规则', 'pricing-center', 'API', 'POST', '/api/admin/pricing-rules', '创建计费规则'),
    ('pricing.rule.update', '更新计费规则', 'pricing-center', 'API', 'PUT', '/api/admin/pricing-rules/{ruleId}', '更新计费规则'),
    ('pricing.rule.status', '变更计费规则状态', 'pricing-center', 'API', 'PUT', '/api/admin/pricing-rules/{ruleId}/status', '启停计费规则'),
    ('pricing.rule.view', '查看计费规则', 'pricing-center', 'API', 'GET', '/api/admin/pricing-rules/{ruleId}', '查看计费规则详情'),
    ('pricing.rule.list', '查询计费规则', 'pricing-center', 'API', 'GET', '/api/admin/pricing-rules', '查询计费规则列表')
ON DUPLICATE KEY UPDATE permission_code = permission_code;

-- [V14__create_pricing_tables_and_permissions.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
SELECT 'SUPER_ADMIN', p.permission_code, 'system'
FROM admin_rbac_permission p
WHERE p.permission_code IN (
    'pricing.rule.create',
    'pricing.rule.update',
    'pricing.rule.status',
    'pricing.rule.view',
    'pricing.rule.list'
)
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V14__create_pricing_tables_and_permissions.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('OPS_ADMIN', 'pricing.rule.create', 'system'),
    ('OPS_ADMIN', 'pricing.rule.update', 'system'),
    ('OPS_ADMIN', 'pricing.rule.status', 'system'),
    ('OPS_ADMIN', 'pricing.rule.view', 'system'),
    ('OPS_ADMIN', 'pricing.rule.list', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V14__create_pricing_tables_and_permissions.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('SUPER_ADMIN', 'pricing-center', 'system'),
    ('SUPER_ADMIN', 'pricing-rule', 'system'),
    ('OPS_ADMIN', 'pricing-center', 'system'),
    ('OPS_ADMIN', 'pricing-rule', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V17__create_trade_tables_and_permissions.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES
    ('trade-center', NULL, '交易中心', '/admin/trade', 'swap', 43, 1),
    ('trade-order', 'trade-center', '交易查询', '/admin/trade/orders', 'unordered-list', 44, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V17__create_trade_tables_and_permissions.sql] admin_rbac_module
INSERT INTO admin_rbac_module (module_code, module_name, module_desc, enabled, sort_no)
VALUES ('trade-center', '交易中心', '交易编排、交易查询与流程追踪权限', 1, 36)
ON DUPLICATE KEY UPDATE module_code = module_code;

-- [V17__create_trade_tables_and_permissions.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission (permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('trade.order.view', '查看交易订单', 'trade-center', 'API', 'GET', '/api/admin/trades/**', '查询交易详情与流程轨迹')
ON DUPLICATE KEY UPDATE permission_code = permission_code;

-- [V17__create_trade_tables_and_permissions.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('SUPER_ADMIN', 'trade.order.view', 'system'),
    ('OPS_ADMIN', 'trade.order.view', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V17__create_trade_tables_and_permissions.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('SUPER_ADMIN', 'trade-center', 'system'),
    ('SUPER_ADMIN', 'trade-order', 'system'),
    ('OPS_ADMIN', 'trade-center', 'system'),
    ('OPS_ADMIN', 'trade-order', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V22__create_inbound_tables_and_enhance_trade_pay.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES
    ('inbound-center', NULL, '入金中心', '/admin/inbound', 'bank', 45, 1),
    ('inbound-order', 'inbound-center', '入金查询', '/admin/inbound/orders', 'unordered-list', 46, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V22__create_inbound_tables_and_enhance_trade_pay.sql] admin_rbac_module
INSERT INTO admin_rbac_module (module_code, module_name, module_desc, enabled, sort_no)
VALUES ('inbound-center', '入金中心', '入金订单与银行网关链路查询权限', 1, 37)
ON DUPLICATE KEY UPDATE module_code = module_code;

-- [V22__create_inbound_tables_and_enhance_trade_pay.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission (permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('inbound.order.view', '查看入金订单', 'inbound-center', 'API', 'GET', '/api/admin/inbound/**', '查询入金订单与状态')
ON DUPLICATE KEY UPDATE permission_code = permission_code;

-- [V22__create_inbound_tables_and_enhance_trade_pay.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('SUPER_ADMIN', 'inbound.order.view', 'system'),
    ('OPS_ADMIN', 'inbound.order.view', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V22__create_inbound_tables_and_enhance_trade_pay.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('SUPER_ADMIN', 'inbound-center', 'system'),
    ('SUPER_ADMIN', 'inbound-order', 'system'),
    ('OPS_ADMIN', 'inbound-center', 'system'),
    ('OPS_ADMIN', 'inbound-order', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V38__add_user_center_admin_permissions.sql] admin_rbac_module
INSERT INTO admin_rbac_module (module_code, module_name, module_desc, enabled, sort_no)
VALUES ('user-center', '用户中心', '用户账户查询、详情查看与状态管理权限', 1, 18)
ON DUPLICATE KEY UPDATE module_code = module_code;

-- [V38__add_user_center_admin_permissions.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission (permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('user.account.list', '查询用户列表', 'user-center', 'API', 'GET', '/api/admin/users', '查询用户中心列表与统计'),
    ('user.account.view', '查看用户详情', 'user-center', 'API', 'GET', '/api/admin/users/{userId}', '查看单个用户详情'),
    ('user.account.status', '变更用户状态', 'user-center', 'API', 'PUT', '/api/admin/users/{userId}/status', '变更用户账户状态')
ON DUPLICATE KEY UPDATE permission_code = permission_code;

-- [V38__add_user_center_admin_permissions.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
SELECT 'SUPER_ADMIN', p.permission_code, 'system'
FROM admin_rbac_permission p
WHERE p.permission_code IN (
    'user.account.list',
    'user.account.view',
    'user.account.status'
)
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V38__add_user_center_admin_permissions.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('OPS_ADMIN', 'user.account.list', 'system'),
    ('OPS_ADMIN', 'user.account.view', 'system'),
    ('OPS_ADMIN', 'user.account.status', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V38__add_user_center_admin_permissions.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('OPS_ADMIN', 'user-center', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V49__seed_admin_deliver_permissions.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES
    ('deliver-center', NULL, '投放中心', '/admin/deliver', 'notification', 45, 1),
    ('deliver-console', 'deliver-center', '投放配置', '/admin/deliver/console', 'appstore', 46, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V49__seed_admin_deliver_permissions.sql] admin_rbac_module
INSERT INTO admin_rbac_module (module_code, module_name, module_desc, enabled, sort_no)
VALUES ('deliver-center', '投放中心', '展位、创意、素材、定向与疲劳度配置能力', 1, 37)
ON DUPLICATE KEY UPDATE module_code = module_code;

-- [V49__seed_admin_deliver_permissions.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission (permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('deliver.view', '查看投放配置', 'deliver-center', 'API', 'GET', '/api/admin/deliver/console', '查看投放中心控制台与资源清单'),
    ('deliver.manage', '管理投放配置', 'deliver-center', 'API', 'POST', '/api/admin/deliver/**', '创建和更新投放资源')
ON DUPLICATE KEY UPDATE permission_code = permission_code;

-- [V49__seed_admin_deliver_permissions.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('SUPER_ADMIN', 'deliver.view', 'system'),
    ('SUPER_ADMIN', 'deliver.manage', 'system'),
    ('OPS_ADMIN', 'deliver.view', 'system'),
    ('OPS_ADMIN', 'deliver.manage', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V49__seed_admin_deliver_permissions.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('SUPER_ADMIN', 'deliver-center', 'system'),
    ('SUPER_ADMIN', 'deliver-console', 'system'),
    ('OPS_ADMIN', 'deliver-center', 'system'),
    ('OPS_ADMIN', 'deliver-console', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V57__add_feedback_admin_menu_and_permissions.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES
    ('feedback-center', NULL, '反馈中心', '/admin/feedback', 'message', 47, 1),
    ('feedback-ticket', 'feedback-center', '反馈工单', '/admin/feedback/tickets', 'unordered-list', 48, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V57__add_feedback_admin_menu_and_permissions.sql] admin_rbac_module
INSERT INTO admin_rbac_module (module_code, module_name, module_desc, enabled, sort_no)
VALUES ('feedback-center', '反馈中心', '用户产品建议、投诉与异常工单的查询处理权限', 1, 38)
ON DUPLICATE KEY UPDATE module_code = module_code;

-- [V57__add_feedback_admin_menu_and_permissions.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission (permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('feedback.ticket.list', '查询反馈工单', 'feedback-center', 'API', 'GET', '/api/admin/feedback/tickets', '查询反馈工单列表'),
    ('feedback.ticket.view', '查看反馈工单', 'feedback-center', 'API', 'GET', '/api/admin/feedback/tickets/{feedbackNo}', '查看反馈工单详情'),
    ('feedback.ticket.handle', '处理反馈工单', 'feedback-center', 'API', 'PUT', '/api/admin/feedback/tickets/{feedbackNo}/status', '处理反馈工单状态')
ON DUPLICATE KEY UPDATE permission_code = permission_code;

-- [V57__add_feedback_admin_menu_and_permissions.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('SUPER_ADMIN', 'feedback.ticket.list', 'system'),
    ('SUPER_ADMIN', 'feedback.ticket.view', 'system'),
    ('SUPER_ADMIN', 'feedback.ticket.handle', 'system'),
    ('OPS_ADMIN', 'feedback.ticket.list', 'system'),
    ('OPS_ADMIN', 'feedback.ticket.view', 'system'),
    ('OPS_ADMIN', 'feedback.ticket.handle', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V57__add_feedback_admin_menu_and_permissions.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('SUPER_ADMIN', 'feedback-center', 'system'),
    ('SUPER_ADMIN', 'feedback-ticket', 'system'),
    ('OPS_ADMIN', 'feedback-center', 'system'),
    ('OPS_ADMIN', 'feedback-ticket', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V71__add_app_version_admin_menu_and_permissions.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES
    ('app-center', NULL, 'App中心', '/admin/apps', 'mobile', 49, 1),
    ('app-version', 'app-center', '版本管理', '/admin/apps/versions', 'profile', 50, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V71__add_app_version_admin_menu_and_permissions.sql] admin_rbac_module
INSERT INTO admin_rbac_module (module_code, module_name, module_desc, enabled, sort_no)
VALUES ('app-center', 'App中心', 'iPhone 客户端版本、设备与访问记录管理权限', 1, 39)
ON DUPLICATE KEY UPDATE module_code = module_code;

-- [V71__add_app_version_admin_menu_and_permissions.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission (permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('app.info.list', '查询应用列表', 'app-center', 'API', 'GET', '/api/admin/apps', '查询应用列表'),
    ('app.info.create', '创建应用', 'app-center', 'API', 'POST', '/api/admin/apps', '创建或更新应用'),
    ('app.version.list', '查询应用版本', 'app-center', 'API', 'GET', '/api/admin/apps/{appCode}/versions', '查询应用版本列表'),
    ('app.version.create', '创建应用版本', 'app-center', 'API', 'POST', '/api/admin/apps/{appCode}/versions', '创建应用版本'),
    ('app.version.update-status', '变更应用版本状态', 'app-center', 'API', 'PUT', '/api/admin/apps/versions/{versionCode}/status', '变更应用版本状态'),
    ('app.ios-package.submit-review', '提交iOS包审核', 'app-center', 'API', 'PUT', '/api/admin/apps/versions/{versionCode}/ios-package/submit-review', '提交 iOS 包审核'),
    ('app.ios-package.publish', '发布iOS包', 'app-center', 'API', 'PUT', '/api/admin/apps/versions/{versionCode}/ios-package/publish', '发布 iOS 包'),
    ('app.device.list', '查询设备列表', 'app-center', 'API', 'GET', '/api/admin/apps/{appCode}/devices', '查询 App 设备列表'),
    ('app.visit.list', '查询访问记录', 'app-center', 'API', 'GET', '/api/admin/apps/devices/{deviceId}/visit-records', '查询设备访问记录')
ON DUPLICATE KEY UPDATE permission_code = permission_code;

-- [V71__add_app_version_admin_menu_and_permissions.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('SUPER_ADMIN', 'app.info.list', 'system'),
    ('SUPER_ADMIN', 'app.info.create', 'system'),
    ('SUPER_ADMIN', 'app.version.list', 'system'),
    ('SUPER_ADMIN', 'app.version.create', 'system'),
    ('SUPER_ADMIN', 'app.version.update-status', 'system'),
    ('SUPER_ADMIN', 'app.ios-package.submit-review', 'system'),
    ('SUPER_ADMIN', 'app.ios-package.publish', 'system'),
    ('SUPER_ADMIN', 'app.device.list', 'system'),
    ('SUPER_ADMIN', 'app.visit.list', 'system'),
    ('OPS_ADMIN', 'app.info.list', 'system'),
    ('OPS_ADMIN', 'app.info.create', 'system'),
    ('OPS_ADMIN', 'app.version.list', 'system'),
    ('OPS_ADMIN', 'app.version.create', 'system'),
    ('OPS_ADMIN', 'app.version.update-status', 'system'),
    ('OPS_ADMIN', 'app.ios-package.submit-review', 'system'),
    ('OPS_ADMIN', 'app.ios-package.publish', 'system'),
    ('OPS_ADMIN', 'app.device.list', 'system'),
    ('OPS_ADMIN', 'app.visit.list', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V71__add_app_version_admin_menu_and_permissions.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('SUPER_ADMIN', 'app-center', 'system'),
    ('SUPER_ADMIN', 'app-version', 'system'),
    ('OPS_ADMIN', 'app-center', 'system'),
    ('OPS_ADMIN', 'app-version', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V81__add_app_version_prompt_switch_admin_menu.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES
    ('app-settings', 'app-center', '提示开关', '/admin/apps/settings', 'setting', 51, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V81__add_app_version_prompt_switch_admin_menu.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission (permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('app.settings.update', '更新应用开关设置', 'app-center', 'API', 'PUT', '/api/admin/apps/{appCode}/settings', '更新应用版本提示开关')
ON DUPLICATE KEY UPDATE permission_code = permission_code;

-- [V81__add_app_version_prompt_switch_admin_menu.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('SUPER_ADMIN', 'app.settings.update', 'system'),
    ('OPS_ADMIN', 'app.settings.update', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V81__add_app_version_prompt_switch_admin_menu.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('SUPER_ADMIN', 'app-settings', 'system'),
    ('OPS_ADMIN', 'app-settings', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V91__create_accounting_tables_and_admin_menu.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES
    ('accounting-center', NULL, '会计中心', '/admin/accounting', 'table', 47, 1),
    ('accounting-event', 'accounting-center', '会计事件', '/admin/accounting/events', 'unordered-list', 48, 1),
    ('accounting-voucher', 'accounting-center', '凭证查询', '/admin/accounting/vouchers', 'unordered-list', 49, 1),
    ('accounting-entry', 'accounting-center', '分录查询', '/admin/accounting/entries', 'unordered-list', 50, 1),
    ('accounting-subject', 'accounting-center', '科目管理', '/admin/accounting/subjects', 'table', 51, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V91__create_accounting_tables_and_admin_menu.sql] admin_rbac_module
INSERT INTO admin_rbac_module (module_code, module_name, module_desc, enabled, sort_no)
VALUES ('accounting-center', '会计中心', '会计事件、凭证、分录与科目管理权限', 1, 38)
ON DUPLICATE KEY UPDATE module_code = module_code;

-- [V91__create_accounting_tables_and_admin_menu.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission (permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('admin.accounting.event.list', '查询会计事件列表', 'accounting-center', 'API', 'GET', '/api/admin/accounting/events', '查询会计事件列表'),
    ('admin.accounting.event.detail', '查看会计事件详情', 'accounting-center', 'API', 'GET', '/api/admin/accounting/events/*', '查看会计事件详情'),
    ('admin.accounting.event.retry', '重试会计事件', 'accounting-center', 'API', 'POST', '/api/admin/accounting/events/*/retry', '重试失败会计事件'),
    ('admin.accounting.voucher.list', '查询会计凭证列表', 'accounting-center', 'API', 'GET', '/api/admin/accounting/vouchers', '查询会计凭证列表'),
    ('admin.accounting.voucher.detail', '查看会计凭证详情', 'accounting-center', 'API', 'GET', '/api/admin/accounting/vouchers/*', '查看会计凭证详情'),
    ('admin.accounting.voucher.reverse', '冲正会计凭证', 'accounting-center', 'API', 'POST', '/api/admin/accounting/vouchers/*/reverse', '执行会计凭证冲正'),
    ('admin.accounting.entry.list', '查询会计分录', 'accounting-center', 'API', 'GET', '/api/admin/accounting/entries*', '查询会计分录明细'),
    ('admin.accounting.subject.list', '查询会计科目', 'accounting-center', 'API', 'GET', '/api/admin/accounting/subjects', '查询会计科目列表'),
    ('admin.accounting.subject.save', '维护会计科目', 'accounting-center', 'API', 'PUT', '/api/admin/accounting/subjects/*', '创建或更新会计科目')
ON DUPLICATE KEY UPDATE permission_code = permission_code;

-- [V91__create_accounting_tables_and_admin_menu.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('SUPER_ADMIN', 'admin.accounting.event.list', 'system'),
    ('SUPER_ADMIN', 'admin.accounting.event.detail', 'system'),
    ('SUPER_ADMIN', 'admin.accounting.event.retry', 'system'),
    ('SUPER_ADMIN', 'admin.accounting.voucher.list', 'system'),
    ('SUPER_ADMIN', 'admin.accounting.voucher.detail', 'system'),
    ('SUPER_ADMIN', 'admin.accounting.voucher.reverse', 'system'),
    ('SUPER_ADMIN', 'admin.accounting.entry.list', 'system'),
    ('SUPER_ADMIN', 'admin.accounting.subject.list', 'system'),
    ('SUPER_ADMIN', 'admin.accounting.subject.save', 'system'),
    ('OPS_ADMIN', 'admin.accounting.event.list', 'system'),
    ('OPS_ADMIN', 'admin.accounting.event.detail', 'system'),
    ('OPS_ADMIN', 'admin.accounting.event.retry', 'system'),
    ('OPS_ADMIN', 'admin.accounting.voucher.list', 'system'),
    ('OPS_ADMIN', 'admin.accounting.voucher.detail', 'system'),
    ('OPS_ADMIN', 'admin.accounting.entry.list', 'system'),
    ('OPS_ADMIN', 'admin.accounting.subject.list', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V91__create_accounting_tables_and_admin_menu.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('SUPER_ADMIN', 'accounting-center', 'system'),
    ('SUPER_ADMIN', 'accounting-event', 'system'),
    ('SUPER_ADMIN', 'accounting-voucher', 'system'),
    ('SUPER_ADMIN', 'accounting-entry', 'system'),
    ('SUPER_ADMIN', 'accounting-subject', 'system'),
    ('OPS_ADMIN', 'accounting-center', 'system'),
    ('OPS_ADMIN', 'accounting-event', 'system'),
    ('OPS_ADMIN', 'accounting-voucher', 'system'),
    ('OPS_ADMIN', 'accounting-entry', 'system'),
    ('OPS_ADMIN', 'accounting-subject', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V99__add_admin_message_fund_risk_outbound_centers.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES
    ('message-center', NULL, '消息中心', '/admin/messages', 'message', 35, 1),
    ('message-conversation', 'message-center', '会话管理', '/admin/messages/conversations', 'unordered-list', 36, 1),
    ('message-record', 'message-center', '消息记录', '/admin/messages/records', 'table', 37, 1),
    ('red-packet-record', 'message-center', '红包记录', '/admin/messages/red-packets', 'gift', 38, 1),
    ('contact-request-menu', 'message-center', '好友申请', '/admin/messages/contact-requests', 'user', 39, 1),
    ('contact-friend-menu', 'message-center', '好友关系', '/admin/messages/friends', 'user', 40, 1),
    ('contact-blacklist-menu', 'message-center', '黑名单', '/admin/messages/blacklist', 'shield', 41, 1),
    ('fund-wallet', 'fund-center', '钱包账户', '/admin/fund/wallet', 'wallet', 42, 1),
    ('fund-product-account', 'fund-center', '基金账户', '/admin/fund/funds', 'wallet', 43, 1),
    ('fund-credit-account', 'fund-center', '授信账户', '/admin/fund/credit', 'wallet', 44, 1),
    ('fund-loan-account', 'fund-center', '借贷账户', '/admin/fund/loan', 'wallet', 45, 1),
    ('fund-bank-card', 'fund-center', '银行卡', '/admin/fund/bankcards', 'bank', 46, 1),
    ('fund-cashier', 'fund-center', '收银台', '/admin/fund/cashier', 'calculator', 47, 1),
    ('risk-user-review', 'risk-center', 'KYC与风险用户', '/admin/risk/users', 'shield', 51, 1),
    ('risk-blacklist-review', 'risk-center', '黑名单', '/admin/risk/blacklist', 'shield', 52, 1),
    ('outbound-center', NULL, '出金中心', '/admin/outbound', 'bank', 47, 1),
    ('outbound-order', 'outbound-center', '出金查询', '/admin/outbound/orders', 'unordered-list', 48, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V99__add_admin_message_fund_risk_outbound_centers.sql] admin_rbac_module
INSERT INTO admin_rbac_module (module_code, module_name, module_desc, enabled, sort_no)
VALUES
    ('message-center', '消息中心', '消息、会话、红包与联系人管理权限', 1, 25),
    ('outbound-center', '出金中心', '出金订单与渠道结果查询权限', 1, 38)
ON DUPLICATE KEY UPDATE module_code = module_code;

-- [V99__add_admin_message_fund_risk_outbound_centers.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission (permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('message.center.view', '查看消息中心', 'message-center', 'API', 'GET', '/api/admin/messages/**', '查看消息、会话、红包、好友关系与黑名单'),
    ('fund.center.view', '查看资金中心', 'fund-center', 'API', 'GET', '/api/admin/fund/**', '查看钱包、基金、授信、借贷、银行卡与收银台'),
    ('risk.center.view', '查看风控中心', 'risk-center', 'API', 'GET', '/api/admin/risk/**', '查看KYC、风险等级与黑名单'),
    ('risk.user.manage', '管理风控用户', 'risk-center', 'API', 'PUT', '/api/admin/risk/users/{userId}/risk-profile', '调整用户KYC与风险等级'),
    ('outbound.order.view', '查看出金订单', 'outbound-center', 'API', 'GET', '/api/admin/outbound/**', '查询出金订单与状态')
ON DUPLICATE KEY UPDATE permission_code = permission_code;

-- [V99__add_admin_message_fund_risk_outbound_centers.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('SUPER_ADMIN', 'message.center.view', 'system'),
    ('SUPER_ADMIN', 'fund.center.view', 'system'),
    ('SUPER_ADMIN', 'risk.center.view', 'system'),
    ('SUPER_ADMIN', 'risk.user.manage', 'system'),
    ('SUPER_ADMIN', 'outbound.order.view', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V99__add_admin_message_fund_risk_outbound_centers.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('SUPER_ADMIN', 'message-center', 'system'),
    ('SUPER_ADMIN', 'message-conversation', 'system'),
    ('SUPER_ADMIN', 'message-record', 'system'),
    ('SUPER_ADMIN', 'red-packet-record', 'system'),
    ('SUPER_ADMIN', 'contact-request-menu', 'system'),
    ('SUPER_ADMIN', 'contact-friend-menu', 'system'),
    ('SUPER_ADMIN', 'contact-blacklist-menu', 'system'),
    ('SUPER_ADMIN', 'fund-wallet', 'system'),
    ('SUPER_ADMIN', 'fund-product-account', 'system'),
    ('SUPER_ADMIN', 'fund-credit-account', 'system'),
    ('SUPER_ADMIN', 'fund-loan-account', 'system'),
    ('SUPER_ADMIN', 'fund-bank-card', 'system'),
    ('SUPER_ADMIN', 'fund-cashier', 'system'),
    ('SUPER_ADMIN', 'risk-user-review', 'system'),
    ('SUPER_ADMIN', 'risk-blacklist-review', 'system'),
    ('SUPER_ADMIN', 'outbound-center', 'system'),
    ('SUPER_ADMIN', 'outbound-order', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V100__sync_admin_trade_ops_permissions.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission (permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('inbound.order.view', '查看入金订单', 'inbound-center', 'API', 'GET', '/api/admin/inbound/**', '查询入金订单与状态'),
    ('outbound.order.view', '查看出金订单', 'outbound-center', 'API', 'GET', '/api/admin/outbound/**', '查询出金订单与状态')
ON DUPLICATE KEY UPDATE
    permission_name = VALUES(permission_name),
    module_code = VALUES(module_code),
    resource_type = VALUES(resource_type),
    http_method = VALUES(http_method),
    path_pattern = VALUES(path_pattern),
    permission_desc = VALUES(permission_desc);

-- [V100__sync_admin_trade_ops_permissions.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('OPS_ADMIN', 'inbound.order.view', 'system'),
    ('OPS_ADMIN', 'outbound.order.view', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V100__sync_admin_trade_ops_permissions.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('OPS_ADMIN', 'inbound-center', 'system'),
    ('OPS_ADMIN', 'inbound-order', 'system'),
    ('OPS_ADMIN', 'outbound-center', 'system'),
    ('OPS_ADMIN', 'outbound-order', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V129__add_outbox_admin_monitor_menu_permission.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES
    ('outbox-center', NULL, 'Outbox监控', '/admin/outbox', 'notification', 60, 1),
    ('outbox-overview', 'outbox-center', '投递总览', '/admin/outbox/overview', 'dashboard', 61, 1),
    ('outbox-dead-letter', 'outbox-center', '死信消息', '/admin/outbox/dead', 'warning', 62, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V129__add_outbox_admin_monitor_menu_permission.sql] admin_rbac_module
INSERT INTO admin_rbac_module (module_code, module_name, module_desc, enabled, sort_no)
VALUES
    ('outbox-center', 'Outbox监控', '系统内可靠异步消息监控与排障权限', 1, 40)
ON DUPLICATE KEY UPDATE module_code = module_code;

-- [V129__add_outbox_admin_monitor_menu_permission.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission (permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('outbox.monitor.view', '查看Outbox监控', 'outbox-center', 'API', 'GET', '/api/admin/outbox/**', '查看Outbox消息总览、Topic分布与死信详情')
ON DUPLICATE KEY UPDATE permission_code = permission_code;

-- [V129__add_outbox_admin_monitor_menu_permission.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('SUPER_ADMIN', 'outbox.monitor.view', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V129__add_outbox_admin_monitor_menu_permission.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('SUPER_ADMIN', 'outbox-center', 'system'),
    ('SUPER_ADMIN', 'outbox-overview', 'system'),
    ('SUPER_ADMIN', 'outbox-dead-letter', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V144__rename_outbox_monitor_to_message_delivery_center.sql] admin_menu
-- 用户可见文案统一为“消息投递中心”
UPDATE admin_menu
SET menu_name = '消息投递中心'
WHERE menu_code = 'outbox-center';

-- [V144__rename_outbox_monitor_to_message_delivery_center.sql] admin_rbac_module
UPDATE admin_rbac_module
SET module_name = '消息投递中心',
    module_desc = '系统内可靠异步消息投递监控与排障权限'
WHERE module_code = 'outbox-center';

-- [V144__rename_outbox_monitor_to_message_delivery_center.sql] admin_rbac_permission
UPDATE admin_rbac_permission
SET permission_name = '查看消息投递中心',
    permission_desc = '查看消息投递总览、主题分布与死信详情'
WHERE permission_code = 'outbox.monitor.view';

-- [V149__create_risk_rule_table_and_permissions.sql] admin_rbac_permission
-- 兼容历史库：部分环境菜单表名为 admin_menu，不存在 admin_rbac_menu，直接跳过菜单插入以避免迁移失败。

INSERT INTO admin_rbac_permission(permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
SELECT 'risk.rule.view', '查看风控规则', 'risk-center', 'API', 'GET', '/api/admin/risk/rules**', '查看风控规则与决策预览'
WHERE NOT EXISTS (SELECT 1 FROM admin_rbac_permission WHERE permission_code = 'risk.rule.view');

-- [V149__create_risk_rule_table_and_permissions.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission(permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
SELECT 'risk.rule.manage', '管理风控规则', 'risk-center', 'API', 'POST', '/api/admin/risk/rules**', '新增、修改、启停风控规则'
WHERE NOT EXISTS (SELECT 1 FROM admin_rbac_permission WHERE permission_code = 'risk.rule.manage');

-- [V149__create_risk_rule_table_and_permissions.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission(permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
SELECT 'risk.rule.manage.put', '管理风控规则', 'risk-center', 'API', 'PUT', '/api/admin/risk/rules**', '新增、修改、启停风控规则'
WHERE NOT EXISTS (SELECT 1 FROM admin_rbac_permission WHERE permission_code = 'risk.rule.manage.put');

-- [V149__create_risk_rule_table_and_permissions.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission(role_code, permission_code, created_by)
SELECT role_code, 'risk.rule.view', 'system'
FROM (
    SELECT 'SUPER_ADMIN' AS role_code
    UNION ALL
    SELECT 'OPS_ADMIN' AS role_code
) roles
WHERE NOT EXISTS (
    SELECT 1 FROM admin_rbac_role_permission rp
    WHERE rp.role_code = roles.role_code
      AND rp.permission_code = 'risk.rule.view'
);

-- [V149__create_risk_rule_table_and_permissions.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission(role_code, permission_code, created_by)
SELECT role_code, 'risk.rule.manage', 'system'
FROM (
    SELECT 'SUPER_ADMIN' AS role_code
    UNION ALL
    SELECT 'OPS_ADMIN' AS role_code
) roles
WHERE NOT EXISTS (
    SELECT 1 FROM admin_rbac_role_permission rp
    WHERE rp.role_code = roles.role_code
      AND rp.permission_code = 'risk.rule.manage'
);

-- [V149__create_risk_rule_table_and_permissions.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission(role_code, permission_code, created_by)
SELECT role_code, 'risk.rule.manage.put', 'system'
FROM (
    SELECT 'SUPER_ADMIN' AS role_code
    UNION ALL
    SELECT 'OPS_ADMIN' AS role_code
) roles
WHERE NOT EXISTS (
    SELECT 1 FROM admin_rbac_role_permission rp
    WHERE rp.role_code = roles.role_code
      AND rp.permission_code = 'risk.rule.manage.put'
);

-- [V149__create_risk_rule_table_and_permissions.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu(role_code, menu_code, created_by)
SELECT role_code, 'risk-rule-config', 'system'
FROM (
    SELECT 'SUPER_ADMIN' AS role_code
    UNION ALL
    SELECT 'OPS_ADMIN' AS role_code
) roles
WHERE NOT EXISTS (
    SELECT 1 FROM admin_rbac_role_menu rm
    WHERE rm.role_code = roles.role_code
      AND rm.menu_code = 'risk-rule-config'
);

-- [V150__add_admin_audit_observability_and_outbox_requeue.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission(permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
SELECT 'outbox.monitor.requeue', '重放消息投递死信', 'outbox-center', 'API', 'POST', '/api/admin/outbox/dead-letters/**', '支持后台单条/批量重放死信消息'
WHERE NOT EXISTS (SELECT 1 FROM admin_rbac_permission WHERE permission_code = 'outbox.monitor.requeue');

-- [V150__add_admin_audit_observability_and_outbox_requeue.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission(permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
SELECT 'ops.observability.view', '查看可观测性', 'system-center', 'API', 'GET', '/api/admin/observability/**', '查看后台可观测性指标汇总'
WHERE NOT EXISTS (SELECT 1 FROM admin_rbac_permission WHERE permission_code = 'ops.observability.view');

-- [V150__add_admin_audit_observability_and_outbox_requeue.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission(permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
SELECT 'audit.log.view', '查看操作审计日志', 'system-center', 'API', 'GET', '/api/admin/audits**', '查看后台写操作审计日志'
WHERE NOT EXISTS (SELECT 1 FROM admin_rbac_permission WHERE permission_code = 'audit.log.view');

-- [V150__add_admin_audit_observability_and_outbox_requeue.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission(role_code, permission_code, created_by)
SELECT role_code, 'outbox.monitor.requeue', 'system'
FROM (
    SELECT 'SUPER_ADMIN' AS role_code
    UNION ALL
    SELECT 'OPS_ADMIN' AS role_code
) roles
WHERE NOT EXISTS (
    SELECT 1 FROM admin_rbac_role_permission rp
    WHERE rp.role_code = roles.role_code
      AND rp.permission_code = 'outbox.monitor.requeue'
);

-- [V150__add_admin_audit_observability_and_outbox_requeue.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission(role_code, permission_code, created_by)
SELECT role_code, 'ops.observability.view', 'system'
FROM (
    SELECT 'SUPER_ADMIN' AS role_code
    UNION ALL
    SELECT 'OPS_ADMIN' AS role_code
) roles
WHERE NOT EXISTS (
    SELECT 1 FROM admin_rbac_role_permission rp
    WHERE rp.role_code = roles.role_code
      AND rp.permission_code = 'ops.observability.view'
);

-- [V150__add_admin_audit_observability_and_outbox_requeue.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission(role_code, permission_code, created_by)
SELECT role_code, 'audit.log.view', 'system'
FROM (
    SELECT 'SUPER_ADMIN' AS role_code
    UNION ALL
    SELECT 'OPS_ADMIN' AS role_code
) roles
WHERE NOT EXISTS (
    SELECT 1 FROM admin_rbac_role_permission rp
    WHERE rp.role_code = roles.role_code
      AND rp.permission_code = 'audit.log.view'
);

-- [V151__create_audience_tables_and_permissions.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES
    ('audience-center', 'deliver-center', '人群管理', '/admin/deliver/audience', 'team', 47, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V151__create_audience_tables_and_permissions.sql] admin_rbac_module
INSERT INTO admin_rbac_module (module_code, module_name, module_desc, enabled, sort_no)
VALUES
    ('audience-center', '人群管理', '人群标签、分群规则与用户标签快照配置能力', 1, 38)
ON DUPLICATE KEY UPDATE module_code = module_code;

-- [V151__create_audience_tables_and_permissions.sql] admin_rbac_permission
INSERT INTO admin_rbac_permission(permission_code, permission_name, module_code, resource_type, http_method, path_pattern, permission_desc)
VALUES
    ('audience.view', '查看人群配置', 'audience-center', 'API', 'GET', '/api/admin/audience/**', '查看标签、分群、规则与用户标签快照'),
    ('audience.manage', '管理人群配置', 'audience-center', 'API', 'POST', '/api/admin/audience/**', '管理标签、分群、规则与用户标签快照')
ON DUPLICATE KEY UPDATE permission_code = permission_code;

-- [V151__create_audience_tables_and_permissions.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission(role_code, permission_code, created_by)
VALUES
    ('SUPER_ADMIN', 'audience.view', 'system'),
    ('SUPER_ADMIN', 'audience.manage', 'system'),
    ('OPS_ADMIN', 'audience.view', 'system'),
    ('OPS_ADMIN', 'audience.manage', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V151__create_audience_tables_and_permissions.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu(role_code, menu_code, created_by)
VALUES
    ('SUPER_ADMIN', 'audience-center', 'system'),
    ('OPS_ADMIN', 'audience-center', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V152__add_admin_observability_audit_menus.sql] admin_menu
INSERT INTO admin_menu (menu_code, parent_code, menu_name, path, icon, sort_no, visible)
VALUES
    ('system-observability', 'system-center', '可观测性', '/admin/system/observability', 'line-chart', 62, 1),
    ('system-audit-log', 'system-center', '操作审计', '/admin/system/audits', 'audit', 63, 1)
ON DUPLICATE KEY UPDATE menu_code = menu_code;

-- [V152__add_admin_observability_audit_menus.sql] admin_rbac_role_menu
INSERT INTO admin_rbac_role_menu (role_code, menu_code, created_by)
VALUES
    ('SUPER_ADMIN', 'system-observability', 'system'),
    ('SUPER_ADMIN', 'system-audit-log', 'system'),
    ('OPS_ADMIN', 'system-observability', 'system'),
    ('OPS_ADMIN', 'system-audit-log', 'system'),
    ('OPS_ADMIN', 'outbox-center', 'system'),
    ('OPS_ADMIN', 'outbox-overview', 'system'),
    ('OPS_ADMIN', 'outbox-dead-letter', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- [V152__add_admin_observability_audit_menus.sql] admin_rbac_role_permission
INSERT INTO admin_rbac_role_permission (role_code, permission_code, created_by)
VALUES
    ('OPS_ADMIN', 'outbox.monitor.view', 'system')
ON DUPLICATE KEY UPDATE role_code = role_code;

-- Enforce single default admin account
DELETE ar FROM admin_rbac_admin_role ar LEFT JOIN admin_account aa ON aa.admin_id = ar.admin_id WHERE aa.username <> 'admin' OR aa.username IS NULL;
DELETE FROM admin_account WHERE username <> 'admin';
SET FOREIGN_KEY_CHECKS = 1;
