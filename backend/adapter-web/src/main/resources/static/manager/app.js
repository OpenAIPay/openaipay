(function () {
  var MANAGER_ASSET_VERSION = '20260329-5';
  var SESSION_KEY = 'openaipay.admin.session.v2';
  var PAGE_INIT_CACHE_KEY = 'openaipay.admin.page_init.v2';
  var LANG_KEY = 'openaipay.admin.lang.v1';
  var MANAGER_ROOT_PATH = '/manager';
  var MANAGER_LOGIN_PATH = '/manager/login';
  var DEFAULT_DEVICE = 'manager-web';
  var DEFAULT_VERSION_PROMPT_MESSAGE = '为保障使用体验，请更新到最新版本后继续使用。';
  var SYSTEM_ROOT_PATH = '/admin/system';
  var RBAC_PAGE_PATH = '/admin/system/rbac';
  var SYSTEM_OBSERVABILITY_PATH = '/admin/system/observability';
  var SYSTEM_AUDIT_PATH = '/admin/system/audits';
  var USER_ROOT_PATH = '/admin/users';
  var APP_ROOT_PATH = '/admin/apps';
  var APP_VERSION_PATH = '/admin/apps/versions';
  var APP_SETTINGS_PATH = '/admin/apps/settings';
  var APP_BEHAVIOR_PATH = '/admin/apps/behavior';
  var MESSAGE_ROOT_PATH = '/admin/messages';
  var MESSAGE_CONVERSATION_PATH = '/admin/messages/conversations';
  var MESSAGE_RECORD_PATH = '/admin/messages/records';
  var MESSAGE_RED_PACKET_PATH = '/admin/messages/red-packets';
  var MESSAGE_CONTACT_REQUEST_PATH = '/admin/messages/contact-requests';
  var MESSAGE_FRIENDSHIP_PATH = '/admin/messages/friends';
  var MESSAGE_BLACKLIST_PATH = '/admin/messages/blacklist';
  var FUND_ROOT_PATH = '/admin/fund';
  var FUND_WALLET_PATH = '/admin/fund/wallet';
  var FUND_PRODUCT_PATH = '/admin/fund/funds';
  var FUND_CREDIT_PATH = '/admin/fund/credit';
  var FUND_LOAN_PATH = '/admin/fund/loan';
  var FUND_BANKCARD_PATH = '/admin/fund/bankcards';
  var FUND_CASHIER_PATH = '/admin/fund/cashier';
  var RISK_ROOT_PATH = '/admin/risk';
  var RISK_USER_PATH = '/admin/risk/users';
  var RISK_BLACKLIST_PATH = '/admin/risk/blacklist';
  var PRICING_ROOT_PATH = '/admin/pricing';
  var PRICING_RULE_PATH = '/admin/pricing/rules';
  var TRADE_ROOT_PATH = '/admin/trade';
  var TRADE_ORDER_PATH = '/admin/trade/orders';
  var INBOUND_ROOT_PATH = '/admin/inbound';
  var INBOUND_ORDER_PATH = '/admin/inbound/orders';
  var OUTBOUND_ROOT_PATH = '/admin/outbound';
  var OUTBOUND_ORDER_PATH = '/admin/outbound/orders';
  var OUTBOX_ROOT_PATH = '/admin/outbox';
  var OUTBOX_OVERVIEW_PATH = '/admin/outbox/overview';
  var OUTBOX_DEAD_PATH = '/admin/outbox/dead';
  var ACCOUNTING_ROOT_PATH = '/admin/accounting';
  var ACCOUNTING_EVENT_PATH = '/admin/accounting/events';
  var ACCOUNTING_VOUCHER_PATH = '/admin/accounting/vouchers';
  var ACCOUNTING_ENTRY_PATH = '/admin/accounting/entries';
  var ACCOUNTING_SUBJECT_PATH = '/admin/accounting/subjects';
  var COUPON_ROOT_PATH = '/admin/coupons';
  var COUPON_TEMPLATE_PATH = '/admin/coupons/templates';
  var COUPON_ISSUE_PATH = '/admin/coupons/issues';
  var COUPON_REDEEM_PATH = '/admin/coupons/redeem';
  var FEEDBACK_ROOT_PATH = '/admin/feedback';
  var FEEDBACK_TICKET_PATH = '/admin/feedback/tickets';
  var DELIVER_ROOT_PATH = '/admin/deliver';
  var DELIVER_CONSOLE_PATH = '/admin/deliver/console';
  var DELIVER_AUDIENCE_PATH = '/admin/deliver/audience';
  var DASHBOARD_PATH = '/admin/dashboard';

  var EN_MENU_NAME_BY_CODE = {
    dashboard: 'Dashboard',
    'user-center': 'User Center',
    'user-account-list': 'User List',
    'app-overview': 'App Overview',
    'app-behavior': 'Behavior Tracking',
    'app-center': 'App Center',
    'app-version': 'App Version Management',
    'app-settings': 'Prompt Switches',
    'coupon-overview': 'Coupon Overview',
    'coupon-center': 'Coupon Center',
    'feedback-overview': 'Feedback Overview',
    'feedback-center': 'Feedback Center',
    'feedback-ticket': 'Feedback Tickets',
    'coupon-template': 'Coupon Templates',
    'coupon-issue': 'Coupon Issuance',
    'coupon-verify': 'Coupon Redemption',
    'deliver-center': 'Deliver Center',
    'deliver-console': 'Deliver Console',
    'audience-center': 'Audience',
    'message-center': 'Message Center',
    'message-conversation': 'Conversation Query',
    'message-record': 'Message Records',
    'red-packet-record': 'Red Packet Records',
    'contact-request-menu': 'Contact Requests',
    'contact-friend-menu': 'Friendships',
    'contact-blacklist-menu': 'Blacklist',
    'fund-center': 'Fund Center',
    'fund-wallet': 'Wallet Accounts',
    'fund-product-account': 'Fund Accounts',
    'fund-credit-account': 'Credit Accounts',
    'fund-loan-account': 'Loan Accounts',
    'fund-bank-card': 'Bank Cards',
    'fund-cashier': 'Cashier',
    'risk-center': 'Risk Control',
    'risk-user-review': 'Risk Users',
    'risk-blacklist-review': 'Blacklist',
    'system-center': 'System Settings',
    'rbac-center': 'Access Control',
    'system-observability': 'Observability',
    'system-audit-log': 'Operation Audits',
    'pricing-center': 'Pricing Center',
    'pricing-rule': 'Pricing Rules',
    'trade-center': 'Trade Center',
    'trade-order': 'Trade Query',
    'accounting-center': 'Accounting Center',
    'accounting-event': 'Accounting Events',
    'accounting-voucher': 'Voucher Query',
    'accounting-entry': 'Entry Query',
    'accounting-subject': 'Subject Management',
    'capital-flow-center': 'Capital Flow Center',
    'inbound-center': 'Inbound Center',
    'inbound-order': 'Inbound Query',
    'outbound-center': 'Outbound Center',
    'outbound-order': 'Outbound Query',
    'outbox-center': 'Message Delivery Center',
    'outbox-overview': 'Delivery Overview',
    'outbox-dead-letter': 'Dead Letters',
  };
  var EN_MENU_NAME_BY_PATH = {
    '/admin/dashboard': 'Dashboard',
    '/admin/users': 'User Center',
    '/admin/apps': 'App Center',
    '/admin/apps/versions': 'App Version Management',
    '/admin/apps/settings': 'Prompt Switches',
    '/admin/apps/behavior': 'Behavior Tracking',
    '/admin/messages': 'Message Center',
    '/admin/messages/conversations': 'Conversation Query',
    '/admin/messages/records': 'Message Records',
    '/admin/messages/red-packets': 'Red Packet Records',
    '/admin/messages/contact-requests': 'Contact Requests',
    '/admin/messages/friends': 'Friendships',
    '/admin/messages/blacklist': 'Blacklist',
    '/admin/feedback': 'Feedback Center',
    '/admin/feedback/tickets': 'Feedback Tickets',
    '/admin/system': 'System Settings',
    '/admin/system/rbac': 'Access Control',
    '/admin/system/observability': 'Observability',
    '/admin/system/audits': 'Operation Audits',
    '/admin/coupons': 'Coupon Center',
    '/admin/coupons/templates': 'Coupon Templates',
    '/admin/coupons/issues': 'Coupon Issuance',
    '/admin/coupons/redeem': 'Coupon Redemption',
    '/admin/deliver': 'Deliver Center',
    '/admin/deliver/console': 'Deliver Console',
    '/admin/deliver/audience': 'Audience',
    '/admin/pricing': 'Pricing Center',
    '/admin/pricing/rules': 'Pricing Rules',
    '/admin/fund': 'Fund Center',
    '/admin/fund/wallet': 'Wallet Accounts',
    '/admin/fund/funds': 'Fund Accounts',
    '/admin/fund/credit': 'Credit Accounts',
    '/admin/fund/loan': 'Loan Accounts',
    '/admin/fund/bankcards': 'Bank Cards',
    '/admin/fund/cashier': 'Cashier',
    '/admin/risk': 'Risk Control',
    '/admin/risk/users': 'Risk Users',
    '/admin/risk/blacklist': 'Blacklist',
    '/admin/trade': 'Trade Center',
    '/admin/trade/orders': 'Trade Query',
    '/admin/accounting': 'Accounting Center',
    '/admin/accounting/events': 'Accounting Events',
    '/admin/accounting/vouchers': 'Voucher Query',
    '/admin/accounting/entries': 'Entry Query',
    '/admin/accounting/subjects': 'Subject Management',
    '/admin/inbound': 'Inbound Center',
    '/admin/inbound/orders': 'Inbound Query',
    '/admin/outbound': 'Outbound Center',
    '/admin/outbound/orders': 'Outbound Query',
    '/admin/outbox': 'Message Delivery Center',
    '/admin/outbox/overview': 'Delivery Overview',
    '/admin/outbox/dead': 'Dead Letters',
  };
  var EN_TEXT_MAP = {
    '爱付管理后台': 'AiPay Admin Console',
    '请输入管理员账号与密码登录': 'Please sign in with admin account and password',
    '管理员账号': 'Admin Account',
    '登录密码': 'Password',
    '登录后台': 'Sign In',
    '默认种子账号：admin / Admin@123456': 'Default seed account: admin / Admin@123456',
    '请输入账号密码后登录': 'Enter credentials and sign in',
    '退出登录': 'Sign Out',
    '工作台': 'Dashboard',
    '检测到登录态，正在恢复后台会话...': 'Session found, restoring console...',
    '账号和密码不能为空': 'Account and password are required',
    '正在校验管理员身份...': 'Verifying admin identity...',
    '登录中...': 'Signing in...',
    '登录成功，正在加载后台...': 'Signed in, loading console...',
    '登录失败，请稍后重试': 'Sign in failed, please retry later',
    '请求失败': 'Request failed',
    '请求返回失败': 'Request failed',
    '后台鉴权失败，请重新登录': 'Authentication failed, please sign in again',
    '权限不足，无法访问该页面': 'Permission denied',
    '后台鉴权失败：管理员不存在': 'Authentication failed: admin not found',
    '后台初始化失败，请重新登录': 'Console initialization failed, please sign in again',
    '后台初始化较慢，已保留登录态': 'Console initialization is slow, session is kept',
    '已退出登录': 'Signed out',
    '切换语言': 'Switch language',
    '当前页面：': 'Current Page: ',
    '请选择左侧菜单查看业务域。': 'Select a menu from the left.',
    '正在恢复后台会话': 'Restoring Console Session',
    '正在拉取菜单与权限，请稍候...': 'Loading menus and permissions, please wait...',
    '后台运行概览': 'Console Overview',
    '当前权限与角色': 'Current Roles and Permissions',
    '角色：': 'Roles: ',
    '无': 'None',
    '无角色': 'No roles',
    '权限数量：': 'Permission Count: ',
    '菜单数量：': 'Menu Count: ',
    '管理员账号：': 'Admin Account: ',
    '优先任务': 'Priority Tasks',
    '管理员、角色、菜单与接口权限管理已经可用。': 'Admin, role, menu, and API access control is available.',
    '可在“系统设置-权限管理”进行分配。': 'Configure assignments in System Settings - Permission Management.',
    '变更后可立即重新加载权限视图。': 'Reload permission views immediately after updates.',
    '暂无可访问模块': 'No accessible modules',
    '点击打开模块功能菜单': 'Click to open the module menu',
    '用户与账号管理': 'User & account management',
    '资金账户与收银台': 'Funds, accounts, and checkout',
    '交易、入金与出金': 'Trades, inbound, and outbound',
    '风险规则与核身': 'Risk rules and verification',
    '消息会话与红包': 'Messages, sessions, and red packets',
    '投放位与素材配置': 'Ad slots and creative setup',
    '权限角色与系统配置': 'Roles, permissions, and system config',
    '账务分录与对账查询': 'Accounting entries and reconciliation',
    '用户账号、实名与安全信息': 'User accounts, identity, and security',
    '红包模板、发放与核销': 'Coupon templates, issuance, and redemption',
    '会话消息、好友与红包记录': 'Messages, contacts, and red packet records',
    '钱包、基金、授信与借贷账户': 'Wallet, fund, credit, and loan accounts',
    '费率规则与报价查询': 'Pricing rules and quote lookup',
    '交易单查询与链路追踪': 'Trade queries and flow tracing',
    '入金订单与通道状态': 'Inbound orders and channel status',
    '投放素材与投放位配置': 'Creative assets and placement configuration',
    '反馈工单流转与处理': 'Feedback ticket routing and processing',
    '会计事件、凭证与分录': 'Accounting events, vouchers, and entries',
    '出金订单与通道状态': 'Outbound orders and channel status',
    '版本发布与提示开关': 'Release management and prompt switches',
    '埋点管理': 'Behavior Tracking',
    '查看埋点': 'View Tracking',
    '查看应用基础信息与当前选中应用概览，并可快速进入版本管理与埋点管理。': 'View app basics and selected app summary, with quick entry to version and behavior pages.',
    '选中应用': 'Select App',
    '当前应用概览': 'Current App Overview',
    '请选择应用查看概览': 'Select an app to view overview',
    '版本数量': 'Version Count',
    '设备数量': 'Device Count',
    '进入版本管理': 'Go to Version Management',
    '进入埋点管理': 'Go to Behavior Tracking',
    '进入提示开关': 'Go to Prompt Switches',
    '独立管理 iPhone 客户端行为埋点明细、统计与报表导出。': 'Manage behavior details, metrics, and report export in a dedicated page.',
    'KYC审核与黑名单管理': 'KYC review and blacklist management',
    '消息投递与死信排查': 'Message delivery and dead-letter diagnostics',
    '消息投递中心（死信排查）': 'Message delivery center (dead-letter diagnostics)',
    '模块功能入口': 'Module function entry',
    '用户总量：': 'Total Users: ',
    '活跃：': 'Active: ',
    '冻结：': 'Frozen: ',
    '关闭：': 'Closed: ',
    '钱包可用：': 'Wallet Active: ',
    '爱花正常：': 'AiCredit Normal: ',
    '用户筛选': 'User Filters',
    '关键字': 'Keyword',
    '账户状态': 'Account Status',
    '页码': 'Page No.',
    '每页条数': 'Page Size',
    '返回条数': 'Limit',
    '按条件查询': 'Search',
    '用户列表': 'User List',
    '上一页': 'Previous',
    '下一页': 'Next',
    '账户状态 正常': 'Status Active',
    '账户状态 冻结': 'Status Frozen',
    '账户状态 关闭': 'Status Closed',
    '刷新用户数据': 'Refresh User Data',
    '暂无用户数据': 'No user data',
    '用户中心数据已加载': 'User center data loaded',
    '用户中心数据加载失败': 'Failed to load user center data',
    '正在查询用户...': 'Querying users...',
    '用户详情': 'User Detail',
    '正在加载用户详情，请稍候...': 'Loading user detail...',
    '查询用户失败': 'User query failed',
    '用户查询失败': 'User query failed',
    '用户查询完成，共 ': 'Query complete, total ',
    '用户详情加载失败': 'Failed to load user detail',
    '正在变更用户状态...': 'Updating user status...',
    '用户状态已更新为 ': 'User status updated to ',
    '变更用户状态失败': 'Failed to update user status',
    '用户ID': 'User ID',
    '姓名': 'Real Name',
    '昵称': 'Nickname',
    '手机号': 'Mobile',
    'AI付号': 'AiPay ID',
    '登录标识': 'Login Identifier',
    'KYC等级': 'KYC Level',
    '账户状态': 'Account Status',
    '钱包可用余额': 'Wallet Available',
    '钱包冻结余额': 'Wallet Frozen',
    '爱花额度/使用额度': 'AiCredit Limit / Used',
    '爱花总额度': 'AiCredit Total Limit',
    '爱花使用额度': 'AiCredit Used',
    '爱花利息余额': 'AiCredit Interest Balance',
    '爱花罚息余额': 'AiCredit Penalty Balance',
    '基金账户数': 'Fund Accounts',
    '基金总可用份额': 'Total Fund Available Share',
    '基金累计收益': 'Total Fund Income',
    '双因子模式': 'Two-factor Mode',
    '风险等级': 'Risk Level',
    '设备锁': 'Device Lock',
    '隐私模式': 'Privacy Mode',
    '手机号可搜索': 'Searchable by Mobile',
    'AI付号可搜索': 'Searchable by AiPay ID',
    '隐藏实名': 'Hide Real Name',
    '个性化推荐': 'Personalized Recommendation',
    '地区': 'Region',
    '生日': 'Birthday',
    '详情更新时间': 'Profile Updated At',
    '操作': 'Actions',
    '详情': 'Detail',
    '激活': 'Activate',
    '冻结': 'Freeze',
    '关闭': 'Close',
    '全部': 'All',
    '刷新': 'Refresh',
    '正常': 'Active',
    '是': 'Yes',
    '否': 'No',
    '暂无管理员': 'No admins',
    '暂无角色': 'No roles',
    '暂无菜单': 'No menus',
    '暂无权限': 'No permissions',
    '权限管理': 'Access Control',
    '管理员、角色、菜单与接口权限管理。': 'Manage admins, roles, menus, and API permissions.',
    '管理员列表': 'Admin List',
    '选择管理员后可分配角色并查看最终可见菜单。': 'Select an admin to assign roles and review effective menus.',
    '刷新权限数据': 'Refresh Access Data',
    '管理员角色分配': 'Admin Role Assignment',
    '当前管理员：': 'Current Admin: ',
    '未选择': 'Not selected',
    '已生效菜单数：': 'Effective Menus: ',
    '，已生效权限数：': ', Effective Permissions: ',
    '保存管理员角色': 'Save Admin Roles',
    '角色菜单与接口权限配置': 'Role Menu and API Permission Configuration',
    '菜单可见权限': 'Menu Visibility Permissions',
    '保存角色菜单': 'Save Role Menus',
    '接口权限': 'API Permissions',
    '保存角色权限': 'Save Role Permissions',
    '请先选择管理员': 'Select an admin first',
    '请先选择角色': 'Select a role first',
    '正在保存管理员角色...': 'Saving admin roles...',
    '管理员角色保存成功': 'Admin roles saved',
    '保存管理员角色失败': 'Failed to save admin roles',
    '正在保存角色菜单...': 'Saving role menus...',
    '角色菜单保存成功': 'Role menus saved',
    '保存角色菜单失败': 'Failed to save role menus',
    '正在保存角色权限...': 'Saving role permissions...',
    '角色权限保存成功': 'Role permissions saved',
    '保存角色权限失败': 'Failed to save role permissions',
    '权限数据已加载': 'Access data loaded',
    '权限数据加载失败': 'Failed to load access data',
    '活跃模板数': 'Active Templates',
    '红包模板在线生效数量': 'Template count currently effective',
    '暂停模板数': 'Paused Templates',
    '运营暂停使用模板数量': 'Template count paused by operations',
    '累计发放量': 'Total Issued',
    '红包总发放笔数': 'Total number of issued coupons',
    '活跃模板': 'Active Templates',
    '可发放模板数量': 'Templates available for issuing',
    '暂停模板': 'Paused Templates',
    '已暂停模板数量': 'Number of paused templates',
    '草稿模板': 'Draft Templates',
    '待发布模板数量': 'Draft templates waiting for release',
    '过期模板': 'Expired Templates',
    '已过期模板数量': 'Number of expired templates',
    '累计发放': 'Total Issued',
    '红包发放总数': 'Total coupons issued',
    '刷新红包数据': 'Refresh Coupon Data',
    '红包模板快照': 'Template Snapshot',
    '模板编码': 'Template Code',
    '模板名称': 'Template Name',
    '状态': 'Status',
    '库存': 'Stock',
    '暂无模板数据': 'No template data',
    '模板数据加载中...': 'Template data loading...',
    '最近发放记录': 'Recent Issue Records',
    '券号': 'Coupon No.',
    '金额': 'Amount',
    '暂无发放记录': 'No issue records',
    '查询模板': 'Query Templates',
    '模板筛选': 'Template Filters',
    '场景类型': 'Scene Type',
    '社交赠礼': 'Social Gift',
    '拉新激励': 'New User Acquisition',
    '用户活跃': 'User Activation',
    '支付激励': 'Payment Incentive',
    '商家营销': 'Merchant Marketing',
    '节日活动': 'Festival Campaign',
    '企业发放': 'Enterprise Grant',
    '服务补偿': 'Service Compensation',
    '模板状态': 'Template Status',
    '刷新汇总': 'Refresh Summary',
    '创建红包模板': 'Create Coupon Template',
    '面额类型': 'Value Type',
    '固定金额': 'Fixed Amount',
    '随机金额': 'Random Amount',
    '固定金额(元)': 'Fixed Amount (CNY)',
    '随机最小金额(元)': 'Random Min Amount (CNY)',
    '随机最大金额(元)': 'Random Max Amount (CNY)',
    '门槛金额(元)': 'Threshold Amount (CNY)',
    '总预算(元)': 'Total Budget (CNY)',
    '总库存': 'Total Stock',
    '单用户限领': 'Per-user Limit',
    '资金来源': 'Funding Source',
    '领取开始': 'Claim Start',
    '领取结束': 'Claim End',
    '使用开始': 'Use Start',
    '使用结束': 'Use End',
    '初始状态': 'Initial Status',
    '规则JSON': 'Rule JSON',
    '创建模板': 'Create Template',
    '模板列表': 'Template List',
    '编码': 'Code',
    '名称': 'Name',
    '场景': 'Scene',
    '面额': 'Amount Type',
    '发放记录筛选': 'Issue Record Filters',
    '模板ID': 'Template ID',
    '查询发放记录': 'Query Issue Records',
    '刷新模块数据': 'Refresh Module Data',
    '发放红包': 'Issue Coupon',
    '发放渠道': 'Issue Channel',
    '业务单号': 'Business No.',
    '发放成功，券号：': 'Issue successful, coupon no: ',
    '发放红包失败': 'Failed to issue coupon',
    '发放记录': 'Issue Records',
    '渠道': 'Channel',
    '领取时间': 'Claimed At',
    '过期时间': 'Expire At',
    '红包核销': 'Coupon Redemption',
    '订单号': 'Order No.',
    '核销红包': 'Redeem Coupon',
    '券详情查询': 'Coupon Detail Query',
    '查询当前券': 'Query Current Coupon',
    '券详情加载成功：': 'Coupon detail loaded: ',
    '券详情加载失败': 'Failed to load coupon detail',
    '使用时间': 'Used At',
    '暂无券详情，请在发放记录页点击“详情”，或在此页输入券号查询。': 'No coupon detail. Click "Detail" in issue records or query by coupon no.',
    '启用': 'Enable',
    '暂停': 'Pause',
    '过期': 'Expire',
    '正在加载红包数据...': 'Loading coupon data...',
    '红包数据已加载': 'Coupon data loaded',
    '红包数据加载失败': 'Failed to load coupon data',
    '正在查询模板...': 'Querying templates...',
    '模板查询完成，共 ': 'Template query complete, total ',
    '模板查询失败': 'Template query failed',
    '正在创建模板...': 'Creating template...',
    '模板创建成功：': 'Template created: ',
    '模板创建失败': 'Failed to create template',
    '正在变更模板状态...': 'Updating template status...',
    '模板状态已更新为 ': 'Template status updated to ',
    '模板状态变更失败': 'Failed to update template status',
    '正在查询发放记录...': 'Querying issue records...',
    '发放记录查询完成，共 ': 'Issue query complete, total ',
    '发放记录查询失败': 'Issue query failed',
    '正在发放红包...': 'Issuing coupon...',
    '正在核销红包...': 'Redeeming coupon...',
    '核销成功：': 'Redeem successful: ',
    '核销失败': 'Redeem failed',
    '计费数据': 'Pricing Data',
    '规则总量': 'Total Rules',
    '规则总量：': 'Total Rules: ',
    '计费规则总数': 'Total pricing rules',
    '启用规则': 'Active Rules',
    '状态 ACTIVE': 'Status ACTIVE',
    '草稿规则': 'Draft Rules',
    '状态 DRAFT': 'Status DRAFT',
    '停用规则': 'Inactive Rules',
    '状态 INACTIVE': 'Status INACTIVE',
    '刷新计费数据': 'Refresh Pricing Data',
    '规则筛选': 'Rule Filters',
    '业务场景': 'Business Scene',
    '支付方式': 'Payment Method',
    '规则状态': 'Rule Status',
    '创建计费规则': 'Create Pricing Rule',
    '规则编码': 'Rule Code',
    '规则名称': 'Rule Name',
    '币种': 'Currency',
    '计费模式': 'Fee Mode',
    '费率(0~1)': 'Rate (0~1)',
    '固定费用': 'Fixed Fee',
    '最低费用': 'Min Fee',
    '最高费用': 'Max Fee',
    '手续费承担方': 'Fee Bearer',
    '优先级': 'Priority',
    '生效开始': 'Valid From',
    '生效结束': 'Valid To',
    '创建规则': 'Create Rule',
    '计费规则列表': 'Pricing Rule List',
    '模式': 'Mode',
    '费率': 'Rate',
    '固定费': 'Fixed Fee',
    '最小/最大费': 'Min/Max Fee',
    '承担方': 'Bearer',
    '生效区间': 'Valid Range',
    '暂无计费规则': 'No pricing rules',
    '规则详情': 'Rule Detail',
    '规则ID': 'Rule ID',
    '查询规则详情': 'Query Rule Detail',
    '规则详情加载成功：': 'Rule detail loaded: ',
    '规则详情加载失败': 'Failed to load rule detail',
    '创建人': 'Created By',
    '更新人': 'Updated By',
    '创建时间': 'Created At',
    '更新时间': 'Updated At',
    '暂无规则详情，请在规则列表点击“详情”，或在上方输入规则ID查询。': 'No rule detail. Click "Detail" in rule list or query by rule ID.',
    '停用': 'Disable',
    '正在加载计费规则...': 'Loading pricing rules...',
    '计费规则已加载': 'Pricing rules loaded',
    '计费规则加载失败': 'Failed to load pricing rules',
    '正在查询计费规则...': 'Querying pricing rules...',
    '规则查询完成，共 ': 'Rule query complete, total ',
    '计费规则查询失败': 'Pricing rule query failed',
    '正在创建计费规则...': 'Creating pricing rule...',
    '计费规则创建成功：': 'Pricing rule created: ',
    '创建计费规则失败': 'Failed to create pricing rule',
    '正在变更计费规则状态...': 'Updating pricing rule status...',
    '计费规则状态已更新为 ': 'Pricing rule status updated to ',
    '变更计费规则状态失败': 'Failed to update pricing rule status',
    '暂无基金账户数据': 'No fund account data',
    '基金代码': 'Fund Code',
    '可用份额': 'Available Share',
    '冻结份额': 'Frozen Share',
    '累计收益': 'Accumulated Income',
    '昨日收益': 'Yesterday Income',
    '最新净值': 'Latest NAV',
    '可用模板ID参考：': 'Available template IDs: ',
    '用户中心': 'User Center',
    '红包中心': 'Coupon Center',
    '红包模板': 'Coupon Templates',
    '红包发放': 'Coupon Issuance',
    '红包核销': 'Coupon Redemption',
    '计费中心': 'Pricing Center',
    '计费规则': 'Pricing Rules',
    '反馈中心': 'Feedback Center',
    '反馈工单': 'Feedback Tickets',
    '用户产品建议、投诉与异常工单统一受理与处理。': 'Centralized handling for product suggestions, complaints, and exception tickets.',
    '反馈概览': 'Feedback Overview',
    '总单量：': 'Total Tickets: ',
    '待处理：': 'Pending: ',
    '处理中：': 'Processing: ',
    '已解决：': 'Resolved: ',
    '已驳回：': 'Rejected: ',
    '已关闭：': 'Closed: ',
    '反馈筛选': 'Feedback Filters',
    '反馈单号': 'Feedback No',
    '反馈类型': 'Feedback Type',
    '处理状态': 'Status',
    '查询反馈单': 'Search Tickets',
    '反馈工单列表': 'Ticket List',
    '来源页面': 'Source Page',
    '提交时间': 'Submitted At',
    '查看详情': 'View Detail',
    '暂无反馈工单': 'No feedback tickets',
    '反馈详情': 'Ticket Detail',
    '处理备注': 'Handle Note',
    '处理人': 'Handled By',
    '处理时间': 'Handled At',
    '关闭时间': 'Closed At',
    '附件链接': 'Attachments',
    '无附件': 'No attachments',
    '处理动作': 'Handle Actions',
    '转处理中': 'Mark Processing',
    '标记已解决': 'Mark Resolved',
    '标记驳回': 'Mark Rejected',
    '关闭工单': 'Close Ticket',
    '请选择要查看的反馈工单': 'Select a feedback ticket to view details',
    '刷新反馈数据': 'Refresh Feedback Data',
    '正在加载反馈工单...': 'Loading feedback tickets...',
    '反馈工单已加载': 'Feedback tickets loaded',
    '反馈工单加载失败': 'Failed to load feedback tickets',
    '正在查询反馈工单...': 'Querying feedback tickets...',
    '反馈工单查询完成，共 ': 'Feedback query complete, total ',
    '反馈工单查询失败': 'Feedback query failed',
    '反馈详情加载成功：': 'Feedback detail loaded: ',
    '反馈详情加载失败': 'Failed to load feedback detail',
    '正在更新反馈状态...': 'Updating feedback status...',
    '反馈单状态已更新为 ': 'Feedback status updated to ',
    '更新反馈状态失败': 'Failed to update feedback status',
    '产品建议': 'Product Suggestion',
    '服务投诉': 'Service Complaint',
    '功能异常': 'Function Exception',
    '其他': 'Other',
    '待处理': 'Pending',
    '处理中': 'Processing',
    '已解决': 'Resolved',
    '已驳回': 'Rejected',
    '已关闭': 'Closed',
    '资金中心': 'Fund Center',
    '消息中心': 'Message Center',
    '风控中心': 'Risk Control',
    '系统设置': 'System Settings',
    '权限设置': 'Permission Settings',
    '权限中心': 'Permission Center',
    '权限管理': 'Access Control',
    '交易中心': 'Trade Center',
    '交易查询': 'Trade Query',
    '出入金中心': 'Capital Flow Center',
    '入金中心': 'Inbound Center',
    '入金查询': 'Inbound Query',
    '出金中心': 'Outbound Center',
    '出金查询': 'Outbound Query',
    '投放配置台': 'Deliver Console',
    '该模块页面框架已就绪，后续可继续接入具体查询与操作功能。': 'Module framework is ready. You can connect concrete query and operations here.',
    '不能为空': ' is required',
    '必须为正整数': ' must be a positive integer',
    '必须大于0': ' must be greater than 0',
    '必须为非负整数': ' must be a non-negative integer',
    '必须大于等于0': ' must be greater than or equal to 0',
    '金额格式不正确': 'Invalid amount format',
    '格式必须为 yyyy-MM-ddTHH:mm:ss': ' must follow yyyy-MM-ddTHH:mm:ss',
    '费率必须在 0 到 1 之间': 'Rate must be between 0 and 1',
    'RATE 模式下费率必须大于0': 'Rate must be greater than 0 in RATE mode',
    'FIXED 模式下固定费用必须大于0': 'Fixed fee must be greater than 0 in FIXED mode',
    'RATE_PLUS_FIXED 模式下费率或固定费用至少一个大于0': 'In RATE_PLUS_FIXED mode, either rate or fixed fee must be greater than 0',
    '查询参数必须为正整数': 'Query parameter must be a positive integer',
    '点击遮罩可关闭弹窗': 'Click mask to close',
    '为保障使用体验，请更新到最新版本后继续使用。': 'For a better experience, please update to the latest version before continuing.',
    '操作失败，请稍后重试': 'Operation failed. Please try again later.',
    '已创建': 'Created',
    '已过期': 'Expired',
    '已过账': 'Posted',
    '已冲正': 'Reversed',
    '已跳过': 'Skipped',
    '已归档': 'Archived',
    '未使用': 'Unused',
    '已使用': 'Used',
    '审核中': 'Under Review',
    '执行中': 'In Progress',
    '已成功': 'Succeeded',
    '已回滚': 'Rolled Back',
    '已失败': 'Failed',
    '待对账': 'Pending Reconciliation',
    '已冻结': 'Frozen',
    '活跃': 'Active',
    '未活跃': 'Inactive',
    '无线网络': 'Wi-Fi',
    '蜂窝网络': 'Cellular',
    '移动网络': 'Mobile Network',
    '有线网络': 'Wired Network',
    '未知': 'Unknown',
    '转账': 'Transfer',
    '入金': 'Inbound',
    '出金': 'Outbound',
    '提现': 'Withdraw',
    '还款': 'Repayment',
    '红包': 'Red Packet',
    '红包券': 'Coupon',
    '红包消息': 'Red Packet Message',
    '营销活动': 'Marketing Campaign',
    '系统发放': 'System Issuance',
    '活动投放': 'Campaign Delivery',
    '投放系统': 'Delivery System',
    '统一交易': 'Unified Trade',
    '入金订单': 'Inbound Order',
    '出金订单': 'Outbound Order',
    '银行卡': 'Bank Card',
    '收银台': 'Cashier',
    '流入': 'Inflow',
    '流出': 'Outflow',
    '爱存': 'AiCash',
    '爱花': 'AiCredit',
    '爱借': 'AiLoan',
    '钱包账户': 'Wallet Account',
    '基金账户': 'Fund Account',
    '信用账户': 'Credit Account',
    '爱花账户': 'AiCredit Account',
    '爱借账户': 'AiLoan Account',
    '商户账户': 'Merchant Account',
    '内部账户': 'Internal Account',
    '入金网关': 'Inbound Gateway',
    '出金网关': 'Outbound Gateway',
    '普通凭证': 'Normal Voucher',
    '冲正凭证': 'Reversal Voucher',
    '调整凭证': 'Adjustment Voucher',
    '已报价': 'Quoted',
    '已提交支付': 'Payment Submitted',
    '支付处理中': 'Payment Processing',
    '支付预处理中': 'Payment Preprocessing',
    '支付预处理完成': 'Payment Preprocess Completed',
    '支付提交中': 'Payment Submitting',
    '支付回滚中': 'Payment Rolling Back',
    '预处理执行中': 'Preprocessing',
    '预处理完成': 'Preprocess Completed',
    '提交中': 'Submitting',
    '回滚中': 'Rolling Back',
    '待执行': 'Pending Execution',
    '预处理成功': 'Preprocess Succeeded',
    '预处理失败': 'Preprocess Failed',
    '确认成功': 'Confirmed',
    '取消成功': 'Canceled Successfully',
    '基金申购': 'Fund Subscription',
    '基金赎回': 'Fund Redemption',
    '按费率': 'Rate',
    '费率+固定额': 'Rate + Fixed',
    '可选更新': 'Optional Update',
    '推荐更新': 'Recommended Update',
    '强制更新': 'Force Update',
    '每次打开都提示': 'Prompt Every Launch',
    '每天提示一次': 'Prompt Once Per Day',
    '每个版本提示一次': 'Prompt Once Per Version',
    '静默不提示': 'Silent',
    'L0 未实名': 'L0 Unverified',
    'L1 基础实名': 'L1 Basic Verified',
    'L2 增强实名': 'L2 Enhanced Verified',
    'L3 高级实名': 'L3 Advanced Verified',
    '低风险': 'Low Risk',
    '中风险': 'Medium Risk',
    '高风险': 'High Risk',
    '安全': 'Safe',
    '未开启': 'Disabled',
    '短信验证': 'SMS Verification',
    '验证器': 'Authenticator',
    '生物识别': 'Biometric',
    '本机注册校验开关': 'Device Binding Check Toggle',
    '本机注册校验': 'Device Binding Check',
    '关闭后可在非注册设备登录账号': 'When disabled, accounts can sign in on non-registered devices',
    '演示模板登录号': 'Demo Template Login ID',
    '默认联系人登录号': 'Default Contact Login ID',
    '演示注册默认密码': 'Demo Default Password',
    '如 177xxxxxxx': 'e.g. 177xxxxxxx',
    '如 138xxxxxxx': 'e.g. 138xxxxxxx',
    '已配置: ': 'Configured: ',
    '未配置': 'Not Configured',
    '保存中...': 'Saving...',
    '创建中...': 'Creating...',
    '保存修改': 'Save Changes',
    '修改版本 - ': 'Edit Version - ',
    '在弹窗中修改版本策略与商店地址。': 'Edit version strategies and store URL in the modal.',
    '管理 iPhone 客户端版本、iOS 包、设备和访问记录。': 'Manage iPhone app versions, iOS packages, devices, and visit logs.',
    '管理 iPhone 客户端功能开关，可直接在列表中开启或关闭。': 'Manage iPhone feature toggles and switch them directly in the list.',
    '按统一交易号、请求号或业务域单号查询交易编排主单。': 'Query orchestrated trade orders by trade no., request no., or business-domain order no.',
    '查看会计事件、凭证、分录与会计科目，并支持失败事件重试与凭证冲正。': 'View accounting events, vouchers, entries, and subjects, and support retrying failed events and voucher reversal.',
    '按标签、分群和规则维护投放人群，并支持用户标签快照调试。': 'Maintain delivery audiences by tags, segments, and rules, with user-tag snapshot debugging.',
    '展位、素材、创意、单元、关系、疲劳度与定向规则统一管理。': 'Unified management for placements, materials, creatives, units, relations, fatigue, and targeting rules.',
    '会话、消息、红包、好友申请、好友关系与黑名单统一查看。': 'Unified view for conversations, messages, red packets, friend requests, friendships, and blacklists.',
    '钱包、爱存、爱花、爱借、银行卡与收银台统一查看。': 'Unified view for wallet, AiCash, AiCredit, AiLoan, bank cards, and cashier.',
    'KYC、风险档案、双因子与黑名单统一排查与配置。': 'Unified troubleshooting and configuration for KYC, risk profiles, two-factor, and blacklists.',
    '按入金或出金链路查看订单状态、结果码与通道字段。': 'View order status, result codes, and channel fields by inbound/outbound flows.',
    '可靠异步消息投递监控，支持死信批量重放与链路排查。': 'Reliable async message delivery monitoring with dead-letter replay and flow diagnostics.',
    '汇总 API 指标、请求场景耗时与 Outbox 健康状态。': 'Summarize API metrics, request-scenario latency, and Outbox health.',
    '记录后台写操作审计日志，支持按管理员、路径、状态与时间范围筛选。': 'Record backend write-operation audit logs and support filtering by admin, path, status, and time range.',
    '应用版本已更新': 'App version updated',
    '保存版本失败': 'Failed to save version',
    '默认管理员账号：admin': 'Default admin account: admin',
    '<option value="">不限制</option>': '<option value="">No Limit</option>',
    ' · 用户名: ': ' · Username: ',
    '/>显示载荷</label>': '/>Show Payload</label>',
    '/>仅看已重试</label>': '/>Retried Only</label>',
    '支付': 'Payment',
    '付款方': 'Payer',
    '收款方': 'Payee',
    '已发布': 'Published',
    '已下线': 'Offline',
    '已提交': 'Submitted',
    '已安装': 'Installed',
    '已卸载': 'Uninstalled',
    '余额': 'Balance',
    '平台': 'Platform',
    '借': 'Debit',
    '贷': 'Credit',
    '人群管理': 'Audience Management',
    '人群管理台': 'Audience Console',
    '投放中心': 'Delivery Center',
    '暂无内容': 'No Content',
    '编辑中': 'Editing',
    '已取消': 'Canceled',
    '初始化': 'Initialized',
    '已受理': 'Accepted',
    '清算中': 'Settling',
    '已清算': 'Settled',
    '退款': 'Refund',
    '资产': 'Asset',
    '负债': 'Liability',
    '权益': 'Equity',
    '收入': 'Income',
    '费用': 'Expense',
    '备查': 'Memo',
    '用户': 'User',
    '商户': 'Merchant',
    '系统': 'System',
    ' 页': ' page(s)',
    '未找到版本': 'Version not found',
    '">修改</button>': '">Edit</button>',
    '" placeholder="自动生成，如 OPENAIPAY_IOS_VER_26_314_1" readonly/>': '" placeholder="Auto generated, e.g. OPENAIPAY_IOS_VER_26_314_1" readonly/>',
    '" placeholder="自动生成，如 26.314.1"': '" placeholder="Auto generated, e.g. 26.314.1"',
    '<div class="manager-rbac-hint" style="margin-top:8px;">强制更新无需设置提示频率，客户端每次打开都会立即提示更新。</div>': '<div class="manager-rbac-hint" style="margin-top:8px;">Force update does not require prompt frequency. Clients will be prompted immediately on every launch.</div>',
    '<div class="manager-rbac-hint" style="margin-top:8px;">发行区域与定向区域已固定为 CN。</div>': '<div class="manager-rbac-hint" style="margin-top:8px;">Release region and target region are fixed to CN.</div>',
    '<textarea id="appVersionDescription" class="form-input" rows="4" placeholder="填写客户端版本更新提示">': '<textarea id="appVersionDescription" class="form-input" rows="4" placeholder="Enter client version update prompt">',
    '<textarea id="appVersionPublisherRemark" class="form-input" rows="3" placeholder="填写发布者备注（仅后台可见）">': '<textarea id="appVersionPublisherRemark" class="form-input" rows="3" placeholder="Enter publisher notes (visible in admin only)">',
    '版本提示关闭后，普通更新与强制更新提示都会被抑制。演示自动登录关闭后，登录页不展示演示账号自动登录按钮。本机注册校验关闭后，可在非注册设备登录。默认联系人登录号用于新注册用户自动加好友及欢迎消息发送。': 'When version prompts are disabled, both regular and force update prompts are suppressed. When demo auto-login is disabled, the login page hides the demo auto-login button. When device binding check is disabled, accounts can sign in on non-registered devices. The default contact login ID is used for auto-friending and welcome messages for newly registered users.'
  };
  var EN_TEXT_AUTO_MAP = {
    '条': 'strip',
    '次': 'Second-rate',
    '第': 'No.',
    '中文': 'Chinese',
    '主题': 'theme',
    '备注': 'Remark',
    '层级': 'Hierarchy',
    '性别': 'gender',
    '时间': 'time',
    '条数': 'Number of items',
    '业务域': 'business domain',
    '事件号': 'event number',
    '交易号': 'Transaction number',
    '会计日': 'accounting day',
    '凭证号': 'Voucher number',
    '启用中': 'Activating',
    '已停用': 'Deactivated',
    '已启用': 'Enabled',
    '已开启': 'Already turned on',
    '幂等键': 'idempotent keys',
    '总科目': 'General subjects',
    '总量：': 'Total amount:',
    '总预算': 'total budget',
    '手续费': 'handling fee',
    '支付号': 'Payment number',
    '根科目': 'root account',
    '爱付号': 'Aifuhao',
    '版本号': 'version number',
    '结果码': 'result code',
    '证件页': 'ID page',
    '请求号': 'Request number',
    '链路号': 'link number',
    '主体ID': 'Subject ID',
    '事件名称': 'event name',
    '事件总数': 'total number of events',
    '事件类型': 'event type',
    '交易状态': 'transaction status',
    '交易类型': 'transaction type',
    '交易详情': 'Transaction details',
    '优惠金额': 'Discount amount',
    '余额扣款': 'Balance deduction',
    '余额方向': 'Balance direction',
    '信用扣款': 'Credit charge',
    '借方总额': 'total debit',
    '入金扣款': 'Deposit deduction',
    '凭证类型': 'Voucher type',
    '原交易号': 'Original transaction number',
    '原始金额': 'original amount',
    '反馈总览': 'Feedback overview',
    '发生时间': 'Occurrence time',
    '发行区域': 'Release area',
    '启动时间': 'Start time',
    '商店地址': 'Store address',
    '国家编码': 'country code',
    '失败原因': 'Reason for failure',
    '头像地址': 'Avatar address',
    '安装时间': 'Installation time',
    '定向区域': 'directional area',
    '实付金额': 'Actual amount paid',
    '客户截图': 'Customer screenshots',
    '应付金额': 'Amount payable',
    '应用名称': 'Application name',
    '应用总览': 'Application overview',
    '开始时间': 'start time',
    '当前版本': 'Current version',
    '待重试：': 'To retry:',
    '执行结果': 'Execution result',
    '扩展信息': 'Extended information',
    '批量上限': 'Batch limit',
    '批量主题': 'Batch theme',
    '报表条数': 'Number of report items',
    '提示频率': 'Prompt frequency',
    '支付单号': 'Payment order number',
    '新建科目': 'Create new account',
    '明细条数': 'Number of details',
    '暂无版本': 'No version yet',
    '更新类型': 'Update type',
    '最近打开': 'Recently opened',
    '最近登录': 'Recently logged in',
    '权限不足': 'Insufficient permissions',
    '来源业务': 'Source business',
    '来源系统': 'source system',
    '死信主题': 'Dead letter topic',
    '爱存扣款': 'Ai deposit deduction',
    '父级科目': 'parent account',
    '版本提示': 'Version Prompt',
    '用户提示信息': 'User Prompt Message',
    '版本说明': 'Release Notes',
    '状态版本': 'status version',
    '用户昵称': 'User nickname',
    '登录账号': 'Login account',
    '目标状态': 'target state',
    '科目名称': 'Subject name',
    '科目类型': 'Account type',
    '科目编码': 'Subject code',
    '系统版本': 'System version',
    '红包总览': 'Red envelope overview',
    '结束时间': 'end time',
    '结果描述': 'Result description',
    '结算金额': 'Settlement amount',
    '联系电话': 'Contact number',
    '脱敏姓名': 'Desensitized name',
    '设备ID': 'Device ID',
    '设备品牌': 'Equipment brand',
    '设备状态': 'Device status',
    '请求方法': 'Request method',
    '请求路径': 'Request path',
    '贷方总额': 'total credit',
    '重放时间': 'replay time',
    '鉴权失败': 'Authentication failed',
    '页面名称': 'Page name',
    '，失败：': ',fail:',
    '，成功：': ',success:',
    '，死信：': ', dead letter:',
    '，草稿：': ', Draft: ',
    '全局事务号': 'Global transaction number',
    '反馈与投诉': 'Feedback and complaints',
    '失败事件数': 'Number of failed events',
    '客户端ID': 'Client ID',
    '应用已保存': 'App saved',
    '成功事件数': 'Number of successful events',
    '来源业务号': 'Source business number',
    '用户去重数': 'User deduplication',
    '管理员ID': 'Administrator ID',
    '脱敏证件号': 'Desensitization ID number',
    '被冲正凭证': 'Voucher corrected',
    '设备去重数': 'Device deduplication',
    '，冲正凭证': ', correct the voucher',
    '，可分发：': ', Active: ',
    '，已重试：': ', retried:',
    'API总量：': 'Total API volume:',
    '业务支付单号': 'Business payment order number',
    '会计事件详情': 'Accounting event details',
    '会计凭证详情': 'Accounting document details',
    '使用开始时间': 'Use start time',
    '使用结束时间': 'Use end time',
    '历史支付尝试': 'Historical payment attempts',
    '后台人工冲正': 'Manual correction in the background',
    '应用Code': 'App Code',
    '开关值不合法': 'The switch value is illegal',
    '当前iOS包': 'Current iOS Package',
    '当前支付单号': 'Current payment order number',
    '当前支付尝试': 'Current payment attempt',
    '批量重放失败': 'Batch replay failed',
    '支付尝试次数': 'Payment attempts',
    '新建版本 -': 'New version -',
    '新建红包模板': 'Create a new red envelope template',
    '新建计费规则': 'Create new billing rule',
    '暂无事件载荷': 'No event payload yet',
    '暂无会计事件': 'No accounting events yet',
    '暂无会计凭证': 'No accounting documents yet',
    '暂无会计分录': 'No accounting entries yet',
    '暂无会计科目': 'No accounting account yet',
    '暂无场景指标': 'No scene indicators yet',
    '暂无埋点明细': 'No behavior event details',
    '暂无审计日志': 'No audit log yet',
    '暂无应用定义': 'No application definition yet',
    '暂无死信消息': 'No dead letter news yet',
    '暂无消息数据': 'No message data yet',
    '暂无统计数据': 'No statistics yet',
    '暂无设备记录': 'No device record yet',
    '暂无访问记录': 'No access record yet',
    '最低支持版本': 'Minimum supported version',
    '最早事件时间': 'earliest event time',
    '最近事件时间': 'Last event time',
    '查询交易失败': 'Query transaction failed',
    '死信查询失败': 'Dead letter query failed',
    '死信重放失败': 'Dead letter replay failed',
    '消息查询失败': 'Message query failed',
    '版本Code': 'Version Code',
    '版本更新提示': 'Version Update Prompt',
    '版本更新时间': 'Version update time',
    '管理员不存在': 'Administrator does not exist',
    '编辑科目 -': 'Edit account -',
    '计费报价单号': 'Billing quotation number',
    '计费规则详情': 'Billing rule details',
    '记录创建时间': 'record creation time',
    '记录更新时间': 'Record update time',
    '请先选择应用': 'Please select an application first',
    '随机最大金额': 'Random maximum amount',
    '随机最小金额': 'Random minimum amount',
    '领取开始时间': 'Collection start time',
    '领取结束时间': 'Collection end time',
    '，生成时间：': ', generation time:',
    '付款方用户ID': 'Payer User ID',
    '审计日志已加载': 'Audit log loaded',
    '应用开关已更新': 'App switch updated',
    '应用版本已创建': 'Application version created',
    '开关字段不合法': 'Switch field is illegal',
    '当前支付结果码': 'Current payment result code',
    '当前生效支付单': 'Current effective payment order',
    '当前选中科目有': 'The currently selected subjects are',
    '收款方用户ID': 'Payee user ID',
    '版本状态已更新': 'Version status updated',
    '请输入冲正原因': 'Please enter the reason for the reversal',
    '，待对账支付：': ', payment pending reconciliation:',
    '，超时处理中：': ', timeout processing:',
    'iOS 包已发布': 'iOS package released',
    'iOS包Code': 'iOS Package Code',
    '一级会计科目数量': 'Number of first-level accounting accounts',
    '业务单查询成功：': 'Business order query successful:',
    '事件分布 Top': 'Event distribution Top',
    '会计凭证冲正失败': 'Accounting voucher correction failed',
    '保存会计科目失败': 'Failed to save accounting account',
    '冲正原因不能为空': 'The reason for reversal cannot be empty',
    '加载会计科目失败': 'Failed to load accounting accounts',
    '可观测指标已加载': 'Observables loaded',
    '场景指标查询失败': 'Scene indicator query failed',
    '埋点报表导出失败': 'Behavior report export failed',
    '埋点报表导出完成': 'Behavior report export completed',
    '审计日志加载失败': 'Audit log loading failed',
    '审计日志查询失败': 'Audit log query failed',
    '平均耗时(ms)': 'Average time taken (ms)',
    '当前支付状态版本': 'Current payment status version',
    '当前支付结果描述': 'Description of current payment results',
    '暂无主题分布数据': 'No topic distribution data yet',
    '暂无埋点报表数据': 'There is no buried point report data yet.',
    '未找到会计科目：': 'Accounting account not found:',
    '查询会计事件失败': 'Failed to query accounting events',
    '查询会计凭证失败': 'Failed to query accounting documents',
    '查询会计分录失败': 'Failed to query accounting entries',
    '模板查询完成，共': 'Template query completed, total',
    '模板状态已更新为': 'Template status has been updated to',
    '正在重放死信 #': 'Replaying dead letter #',
    '死信查询完成，共': 'Dead letter query completed, total',
    '消息ID不能为空': 'Message ID cannot be empty',
    '消息查询完成，共': 'Message query completed, total',
    '演示账号自动登录': 'Demo account automatically logs in',
    '版本提示开关 -': 'Version prompt switch -',
    '用户查询完成，共': 'User query completed, total',
    '用户状态已更新为': 'User status has been updated to',
    '类型分布 Top': 'Type distribution Top',
    '统计加载中...': 'Statistics loading...',
    '规则查询完成，共': 'The rule query is completed, a total of',
    '请求号查询成功：': 'Request number query successful:',
    '重试会计事件失败': 'Retry accounting event failed',
    '，今日失败交易：': ', failed transaction today:',
    'Outbox死信：': 'Outbox dead letter:',
    '交易详情加载成功：': 'Transaction details loaded successfully:',
    '会计中心数据已加载': 'Accounting center data has been loaded',
    '会计科目保存成功：': 'Accounting account saved successfully:',
    '会计科目已加载，共': 'Accounting accounts have been loaded, total',
    '创建 / 更新应用': 'Create/update app',
    '反馈单状态已更新为': 'The feedback ticket status has been updated to',
    '可观测指标加载失败': 'Observable indicator loading failed',
    '埋点查询条件已更新': 'Behavior query filters updated',
    '应用功能开关已更新': 'App function switch has been updated',
    '应用版本数据已刷新': 'App version data has been refreshed',
    '批量重放完成，成功': 'Batch replay completed, successful',
    '正在查询交易...': 'Inquiring about transaction...',
    '正在查询死信...': 'Querying for dead mail...',
    '正在查询消息...': 'Looking for news...',
    '死信重放完成，成功': 'Dead letter replay completed, successful',
    '消息投递数据已加载': 'Message delivery data has been loaded',
    '科目已载入编辑器：': 'The account has been loaded into the editor:',
    '设置页-反馈与投诉': 'Settings Page-Feedback and Complaints',
    'iOS 包已提交审核': 'iOS package submitted for review',
    '主题分布加载中...': 'Theme distribution is loading...',
    '会计中心数据加载失败': 'Accounting center data loading failed',
    '会计事件加载中...': 'Accounting events are loading...',
    '会计事件查询完成，共': 'Accounting event query completed, total',
    '会计凭证加载中...': 'Accounting vouchers are loading...',
    '会计凭证查询完成，共': 'Accounting voucher inquiry completed, total',
    '会计分录加载中...': 'Accounting entries are loading...',
    '会计分录查询完成，共': 'Accounting entry inquiry completed, total',
    '会计科目加载中...': 'Accounting accounts are loading...',
    '保留但不再使用的科目': 'Accounts retained but no longer used',
    '加载会计事件详情失败': 'Failed to load accounting event details',
    '加载会计凭证详情失败': 'Failed to load accounting document details',
    '反馈工单查询完成，共': 'Feedback work order query completed, total',
    '发放记录加载中...': 'Issuance records are loading...',
    '发放记录查询完成，共': 'The issuance record query is completed, a total of',
    '场景指标加载中...': 'Scenario indicators are loading...',
    '场景指标查询完成，共': 'The scene indicator query is completed, a total of',
    '埋点报表加载中...': 'The buried point report is loading...',
    '埋点明细加载中...': 'Buried point details are loading...',
    '审计日志加载中...': 'Audit log loading...',
    '审计日志查询完成，共': 'Audit log query completed, total',
    '已切换到新建科目模式': 'Switched to new account mode',
    '应用定义加载中...': 'Application definition loading...',
    '应用版本数据加载失败': 'Application version data loading failed',
    '当前可参与过账的科目': 'Accounts that can currently participate in posting',
    '按业务域查询交易失败': 'Failed to query transactions by business domain',
    '按请求号查询交易失败': 'Failed to query transaction by request number',
    '更新会计科目状态失败': 'Failed to update accounting account status',
    '权限数据加载中...': 'Permission data is loading...',
    '标准会计科目重置完成': 'Standard accounting account reset completed',
    '死信列表加载中...': 'The dead letter list is loading...',
    '消息列表加载中...': 'The message list is loading...',
    '消息投递数据加载失败': 'Message delivery data loading failed',
    '版本数据加载中...': 'Version data is loading...',
    '用户数据加载中...': 'User data is loading...',
    '菜单数据加载中...': 'Menu data is loading...',
    '角色数据加载中...': 'Character data is loading...',
    '计费规则加载中...': 'Billing rules are loading...',
    '计费规则状态已更新为': 'The billing rule status has been updated to',
    '设备数据加载中...': 'Device data loading...',
    '访问记录加载中...': 'Access records are loading...',
    '重置标准会计科目失败': 'Reset standard accounting failed',
    '，平均耗时(ms)：': ', average time consumption (ms):',
    '，最大耗时(ms)：': ', maximum time consuming (ms):',
    '会计事件详情加载成功：': 'Accounting event details loaded successfully:',
    '会计凭证详情加载成功：': 'Accounting voucher details are loaded successfully:',
    '会计科目状态更新成功：': 'Accounting account status updated successfully:',
    '初始化标准会计科目失败': 'Failed to initialize standard accounting accounts',
    '标准会计科目初始化完成': 'Standard accounting account initialization completed',
    '正在保存会计科目...': 'Saving accounting...',
    '正在停用会计科目...': 'Deactivating accounting account...',
    '正在加载会计科目...': 'Loading accounting...',
    '正在加载审计日志...': 'Loading audit logs...',
    '正在启用会计科目...': 'Enabling accounting accounts...',
    '正在批量重放死信...': 'Replaying dead messages in batches...',
    '正在查询会计事件...': 'Querying accounting events...',
    '正在查询会计凭证...': 'Querying accounting documents...',
    '正在查询会计分录...': 'Querying accounting entries...',
    '正在查询场景指标...': 'Querying scene indicators...',
    '正在查询审计日志...': 'Querying audit logs...',
    '正在重试会计事件...': 'Retrying accounting events...',
    '管理员数据加载中...': 'Administrator data is loading...',
    '，Outbox待重试：': ', Outbox is waiting to be retried:',
    '会计凭证冲正成功：原凭证': 'Accounting voucher corrected successfully: original voucher',
    '安装包大小(bytes)': 'Installation package size (bytes)',
    '当前可查看的会计科目总数': 'The total number of accounting accounts currently viewable',
    '正在加载可观测指标...': 'Loading observable indicators...',
    '正在加载会计事件详情...': 'Loading accounting event details...',
    '正在加载会计凭证详情...': 'Loading accounting voucher details...',
    '正在加载消息投递数据...': 'Loading message delivery data...',
    '正在加载用户中心数据...': 'Loading user center data...',
    '正在执行会计凭证冲正...': 'Accounting document correction is being executed...',
    '正在按业务域查询交易...': 'Querying transactions by business domain...',
    '正在按请求号查询交易...': 'Querying transaction by request number...',
    '正在重置标准会计科目...': 'Resetting standard accounting...',
    '会计事件重试成功，生成凭证：': 'The accounting event is retried successfully and the voucher is generated:',
    '正在初始化标准会计科目...': 'Initializing standard accounting accounts...',
    '关闭后客户端不展示版本更新提示': 'The client does not display version update prompts after closing',
    '关闭后登录页不展示演示账号自动登录按钮': 'After closing, the login page does not display the demo account automatic login button.',
    '可空，例如 {"biz":"pay"}': 'Optional, for example {"biz":"pay"}',
    '详情查看通过弹窗完成，不再占据列表页空间。': 'Detailed viewing is completed through a pop-up window and no longer takes up space on the list page.',
    '配置入口迁移到弹窗，列表页只保留应用列表。': 'The configuration entrance is moved to a pop-up window, and the list page only retains the application list.',
    '查询结果通过弹窗展示，主页面只保留查询入口。': 'The query results are displayed through pop-up windows, and only the query entry is retained on the main page.',
    '详情查看通过弹窗完成，不再挤占事件列表页面。': 'Detailed viewing is completed through a pop-up window, no longer occupying the event list page.',
    '详情查看通过弹窗完成，不再挤占凭证列表页面。': 'Detailed viewing is completed through a pop-up window, no longer occupying the voucher list page.',
    '" placeholder="如 200101"': '" placeholder="such as 200101"',
    '创建入口迁移到弹窗，列表页仅保留筛选和模板列表。': 'The creation portal is moved to a pop-up window, and the list page only retains filter and template lists.',
    '核销入口迁移到弹窗，避免主页面直接摊开操作表单。': 'The write-off entrance is moved to a pop-up window to avoid directly spreading the operation form on the main page.',
    '科目维护改为弹窗处理，主页面只保留科目树和列表。': 'Account maintenance is changed to pop-up window processing, and only the account tree and list are retained on the main page.',
    '券详情查看迁移到弹窗，主页面不再展示整块详情表单。': 'The coupon details view is moved to a pop-up window, and the entire details form is no longer displayed on the main page.',
    '发放入口迁移到弹窗，主页面仅保留筛选与发放记录列表。': 'The issuance entrance is moved to a pop-up window, and the main page only retains the filtering and issuance record list.',
    '创建入口迁移到弹窗，主页面只保留统计、筛选和规则列表。': 'The created portal is moved to a pop-up window, and the main page only retains statistics, filtering and rule lists.',
    '应用定义在弹窗中维护，列表页只展示应用列表和版本数据。': 'Application definitions are maintained in pop-up windows, and the list page only displays the application list and version data.',
    '版本创建入口迁移到弹窗，主页面只展示版本列表和设备数据。': 'The version creation entrance is moved to a pop-up window, and the main page only displays the version list and device data.',
    '初始化仅补齐缺失标准科目，不会覆盖现有科目配置。是否继续？': 'Initialization only fills in missing standard accounts and does not overwrite existing account configurations. Continue?',
    '新建子科目时，填写父级科目后会自动带出层级、科目类型和余额方向。': 'When creating a new sub-account, filling in the parent account will automatically bring out the level, account type and balance direction.',
    '个直接下级；存在下级时，不允许修改父级、层级、科目类型或余额方向。': 'There are direct subordinates; when there are subordinates, modification of the parent, level, account type or balance direction is not allowed.',
    '可空，例如 {"remark":"rate for wallet"}': 'Optional, for example {"remark":"rate for wallet"}',
    '可空': 'Optional',
    '可空，默认 0': 'Optional, default 0',
    '可空，如 TRANSFER': 'Optional, e.g. TRANSFER',
    '可空，如 WALLET（余额）': 'Optional, e.g. WALLET (Balance)',
    '草稿': 'Draft',
    '重置会恢复标准科目的名称、层级、启停状态，并停用非标准科目。是否继续？': 'Resetting will restore the name, level, start and stop status of standard accounts, and deactivate non-standard accounts. Continue?',
  };
  Object.keys(EN_TEXT_AUTO_MAP).forEach(function (key) {
    if (!EN_TEXT_MAP[key]) {
      EN_TEXT_MAP[key] = EN_TEXT_AUTO_MAP[key];
    }
  });

  var EN_TEXT_AUTO_PARTS_MAP = {
    '下级': 'subordinate',
    '主体': 'main body',
    '借方': 'debit',
    '借贷': 'loan',
    '冲正': 'Reverse',
    '动作': 'Action',
    '发布': 'Publish',
    '可选': 'Optional',
    '失败': 'Failed',
    '开启': 'Enable',
    '归档': 'Archive',
    '必填': 'Required',
    '总数': 'Total',
    '总量': 'Total',
    '成功': 'Success',
    '提审': 'Submit for Review',
    '摘要': 'Summary',
    '方向': 'direction',
    '查看': 'View',
    '死信': 'dead letter',
    '父级': 'parent',
    '科目': 'suject',
    '类型': 'type',
    '结果': 'result',
    '编辑': 'Edit',
    '网络': 'network',
    '腿号': 'Leg number',
    '行号': 'Line number',
    '说明': 'Description',
    '请求': 'ask',
    '贷方': 'lender',
    '重放': 'replay',
    '重试': 'Retry',
    '页面': 'page',
    '分支号': 'branch number',
    '包摘要': 'package summary',
    '包状态': 'package status',
    '去核销': 'Go to write-off',
    '参与方': 'Participants',
    '可为空': 'Optional',
    '失败量': 'amount of failure',
    '已重试': 'Retried',
    '成功量': 'amount of success',
    '消息键': 'message key',
    '用户数': 'Number of users',
    '科目树': 'Account tree',
    '管理员': 'administrator',
    '设备数': 'Number of devices',
    '账户号': 'Account number',
    '账户域': 'Account field',
    'iOS包': 'iOS Package',
    '下次重试': 'Try again next time',
    '业务角色': 'business role',
    '主题分布': 'Topic distribution',
    '事件时间': 'event time',
    '事件载荷': 'event payload',
    '保存开关': 'Save Switches',
    '保存科目': 'Save Subject',
    '凭证列表': 'Credential list',
    '分录列表': 'List of entries',
    '创建版本': 'Create version',
    '刷新报表': 'Refresh report',
    '刷新指标': 'refresh indicator',
    '刷新数据': 'Refresh Data',
    '刷新科目': 'Refresh account',
    '刷新统计': 'Refresh statistics',
    '响应结果': 'response result',
    '埋点报表': 'Behavior Report',
    '如 CN': 'Such as CN',
    '应用列表': 'Application list',
    '引用流水': 'Quote running water',
    '当前应用': 'Current application',
    '操作入口': 'Operation entrance',
    '新建应用': 'Create App',
    '新建模板': 'Create Template',
    '新建版本': 'Create Version',
    '新建规则': 'Create Rule',
    '暂无分录': 'No entries yet',
    '最新发布': 'Latest releases',
    '最近错误': 'recent errors',
    '查看版本': 'View version',
    '查看访问': 'view visit',
    '查询事件': 'Query events',
    '查询凭证': 'Query voucher',
    '查询分录': 'Query entries',
    '查询明细': 'Query details',
    '查询死信': 'Query dead mail',
    '查询消息': 'Query message',
    '步骤状态': 'step status',
    '步骤编码': 'step encoding',
    '消息查询': 'Message query',
    '版本列表': 'Version list',
    '科目列表': 'Account list',
    '筛选条件': 'Filter criteria',
    '结果摘要': 'summary of results',
    '统计日期': 'Statistics date',
    '设备列表': 'Device list',
    '设备详情': 'Device details',
    '访问记录': 'access record',
    '请求快照': 'Request a snapshot',
    '请求总量': 'Total requests',
    '调用接口': 'Call interface',
    '调用时间': 'Call time',
    '账户类型': 'Account type',
    '资源标识': 'Resource ID',
    '载荷预览': 'Payload preview',
    '错误信息': 'error message',
    '错误摘要': 'Error summary',
    '问题描述': 'Problem description',
    '冲正该凭证': 'Reverse the voucher',
    '刷新当前页': 'Refresh current page',
    '如 CNY': 'Such as CNY',
    '客户端IP': 'Client IP',
    '导出CSV': 'Export CSV',
    '暂无资金腿': 'No funding legs yet',
    '查看券详情': 'View coupon details',
    '查询券详情': 'Check coupon details',
    '次支付尝试': 'Payment attempts',
    '重试该事件': 'Retry the event',
    '会计事件列表': 'Accounting Event List',
    '会计事件查询': 'Accounting event inquiry',
    '会计凭证查询': 'Accounting Voucher Query',
    '会计分录查询': 'Accounting Entry Query',
    '刷新审计日志': 'Refresh audit log',
    '刷新投递数据': 'Refresh delivery data',
    '埋点统计看板': 'Behavior Metrics Dashboard',
    '如 Home': 'Such as Home',
    '审计日志列表': 'Audit log list',
    '客户截图大图': 'Customer screenshot large picture',
    '批量重放死信': 'Batch replay of dead messages',
    '按交易号查询': 'Query by transaction number',
    '按请求号查询': 'Search by request number',
    '支付尝试列表': 'Payment attempt list',
    '暂无流程步骤': 'No process steps yet',
    '查看关联事件': 'View related events',
    '查看关联凭证': 'View associated credentials',
    '查看冲正凭证': 'View the reversal voucher',
    '查询场景指标': 'Query scene indicators',
    '查询审计日志': 'Query audit logs',
    '标准科目运维': 'Standard account operation and maintenance',
    '清空查询结果': 'Clear query results',
    '点击查看大图': 'Click to view larger image',
    '统一交易查询': 'Unified Trade Query',
    '耗时(ms)': 'Time taken(ms)',
    '行为埋点明细': 'Behavior Event Details',
    '请求幂等查询': 'Request idempotent query',
    '例如 1000': 'For example 1000',
    '初始化标准科目': 'Initialize standard accounts',
    '如 CN,HK': 'Such as CN,HK',
    '必填，模板ID': 'Required, template ID',
    '死信消息与重放': 'Dead letter messages and replays',
    '留空则立即重试': 'Leave blank to try again immediately',
    '重置为标准科目': 'Reset to standard account',
    'API 场景指标': 'API scenario metrics',
    'RATE模式必填': 'RATE mode is required',
    '可选 topic': 'Optional topic',
    '填写版本更新说明': 'Fill in the version update instructions',
    '暂无支付尝试数据': 'No payment attempt data yet',
    '最大耗时(ms)': 'Maximum time taken (ms)',
    '版本更新功能开关': 'Version update function switch',
    '留空表示所有主题': 'Leave blank for all topics',
    '行为埋点筛选 -': 'Behavioral focus filtering -',
    '请先登录管理后台': 'Please sign in to the admin console',
    'FIXED模式必填': 'FIXED mode is required',
    '例如 转账费率规则': 'For example, transfer rate rules',
    '必填，32位订单号': 'Required, 32-digit order number',
    '数字越小优先级越高': 'The smaller the number, the higher the priority.',
    '暂无参与方分支明细': 'No details of participating branches are available yet.',
    '请选择设备查看详情': 'Please select a device to view details',
    '输入券号后点击查询': 'After entering the coupon number, click Query',
    '反馈工单加载中...': 'Feedback tickets are loading...',
    '反馈详情加载中...': 'Feedback detail is loading...',
    '如 26.315.1': 'Such as 26.315.1',
    '如 用户钱包可用余额': 'Such as the available balance of the user\'s wallet',
    '已冲正 / 冲正凭证': 'Reversed/reversed voucher',
    '演示自动登录功能开关': 'Demonstrate automatic login function switch',
    '输入 eventId': 'Enter eventId',
    '输入 ownerId': 'Enter ownerId',
    '默认50，最多500': 'Default 50, maximum 500',
    '例如 10000.00': 'For example 10000.00',
    '例如 顾郡支付补贴红包': 'For example, Gu Jun pays subsidy red envelopes',
    '如 123456789': 'Such as 123456789',
    '如 OPEN_PAGE': 'Such as OPEN_PAGE',
    '如 PAGE_VIEW': 'Such as PAGE_VIEW',
    '精确匹配 userId': 'Exact match userId',
    '固定红包填，如 8.88': 'Fixed red envelope filling, such as 8.88',
    '输入 voucherNo': 'Enter voucherNo',
    '随机红包填，如 2.00': 'Fill in a random red envelope, such as 2.00',
    '默认200，最多1000': 'Default 200, maximum 1000',
    '如 WALLET/CARD': 'Such as WALLET/CARD',
    '必填，例如 4010...': 'Required, for example 4010...',
    '按业务域 + 业务单号查询': 'Query by business domain + business order number',
    '精确匹配 deviceId': 'Exact match deviceId',
    '输入 payOrderNo': 'Enter payOrderNo',
    '随机红包填，如 12.00': 'Fill in a random red envelope, such as 12.00',
    '反馈工单处理中，请稍候...': 'Processing feedback ticket, please wait...',
    '输入 sourceBizNo': 'Enter sourceBizNo',
    '输入 subjectCode': 'Enter subjectCode',
    '例如 APP_ACTIVITY': 'For example APP_ACTIVITY',
    '如 OPENAIPAY_IOS': 'Such as OPENAIPAY_IOS',
    '输入 tradeOrderNo': 'Enter tradeOrderNo',
    '例如 GJ_PAY_2026Q2': 'For example GJ_PAY_2026Q2',
    '填写处理说明、回访结果或关闭原因': 'Fill in the processing instructions, return visit results or closure reasons',
    '如 OpenAiPay iPhone': 'Like OpenAiPay iPhone',
    '如 TRANSFER/PAYMENT': 'Such as TRANSFER/PAYMENT',
    '输入 requestNo 后回车或点击查询': 'Enter requestNo and press Enter or click Query',
    '例如 PRC_TRANSFER_2026Q2': 'For example PRC_TRANSFER_2026Q2',
    '如爱花还款单号、爱存申购单号、入金单号、出金单号': 'Such as Aihua repayment order number, Aicun purchase order number, deposit order number, withdrawal order number',
    '点击节点通过弹窗查看与编辑，树会按父子层级展示。': 'Click on the node to view and edit through the pop-up window, and the tree will be displayed at the parent-child level.',
    '输入 tradeOrderNo 后回车或点击查询': 'Enter tradeOrderNo and press Enter or click Query',
    '核销动作改为弹窗处理，主页面不再展示整块操作表单。': 'The write-off action is changed to a pop-up window, and the entire operation form is no longer displayed on the main page.',
    '如 OPENAIPAY_IOS_PKG_26_315_1': 'Such as OPENAIPAY_IOS_PKG_26_315_1',
    '如 OPENAIPAY_IOS_VER_26_315_1': 'Such as OPENAIPAY_IOS_VER_26_315_1',
    '详情查询改为弹窗处理，可输入券号查看领取、过期与核销状态。': 'Detailed inquiry has been changed to a pop-up window. You can enter the coupon number to check the status of collection, expiration and cancellation.',
    '暂无券详情，请在发放记录页点击“详情”，或在此处输入券号查询。': 'There are currently no coupon details, please click "Details" on the issuance record page, or enter the coupon number here to check.',
    '初始化只补齐缺失标准科目；重置会恢复标准定义，并自动停用非标准科目。': 'Initialization only fills in missing standard accounts; reset will restore standard definitions and automatically deactivate non-standard accounts.',
  };
  Object.keys(EN_TEXT_AUTO_PARTS_MAP).forEach(function (key) {
    if (!EN_TEXT_MAP[key]) {
      EN_TEXT_MAP[key] = EN_TEXT_AUTO_PARTS_MAP[key];
    }
  });

  var EN_TEXT_KEYS = Object.keys(EN_TEXT_MAP).sort(function (a, b) {
    return b.length - a.length;
  });

  var ICON_MAP = {
    dashboard: {
      tone: 'sky',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M4.75 5.75h6.5v5.5h-6.5zM12.75 5.75h6.5v9.5h-6.5zM4.75 12.75h6.5v5.5h-6.5zM12.75 16.75h6.5v1.5h-6.5z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>' +
        '</svg>',
    },
    user: {
      tone: 'cyan',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M12 12a3.75 3.75 0 1 0 0-7.5 3.75 3.75 0 0 0 0 7.5Z" stroke="currentColor" stroke-width="1.8"/>' +
        '<path d="M5.75 19.25a6.25 6.25 0 0 1 12.5 0" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>' +
        '</svg>',
    },
    team: {
      tone: 'aurora',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M12 11.25a3.25 3.25 0 1 0 0-6.5 3.25 3.25 0 0 0 0 6.5Z" stroke="currentColor" stroke-width="1.8"/>' +
        '<path d="M6.25 12.75a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5ZM17.75 12.75a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z" stroke="currentColor" stroke-width="1.6"/>' +
        '<path d="M4 19.25a4.5 4.5 0 0 1 4.5-4.5M19.5 19.25a4.5 4.5 0 0 0-4.5-4.5M7.5 19.25a4.5 4.5 0 0 1 9 0" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>' +
        '</svg>',
    },
    gift: {
      tone: 'coral',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M4.75 10.25h14.5v8a1 1 0 0 1-1 1H5.75a1 1 0 0 1-1-1zM3.75 7.75h16.5v2.5H3.75z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>' +
        '<path d="M12 7.75v11.5M12 7.75h-2.75a2.25 2.25 0 1 1 0-4.5c1.7 0 2.75 1.75 2.75 4.5ZM12 7.75h2.75a2.25 2.25 0 1 0 0-4.5c-1.7 0-2.75 1.75-2.75 4.5Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>' +
        '</svg>',
    },
    template: {
      tone: 'violet',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M7.25 5.25h7.5l3 3v9.5a1 1 0 0 1-1 1h-9.5a1 1 0 0 1-1-1v-11.5a1 1 0 0 1 1-1Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>' +
        '<path d="M14.75 5.25v3h3M9 12h6M9 15h6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>' +
        '</svg>',
    },
    send: {
      tone: 'teal',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M4.25 11.75 19.5 4.5l-4.75 15-3.5-5-7-2.75Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>' +
        '<path d="m11.25 13.75 4.25-4.25" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>' +
        '</svg>',
    },
    check: {
      tone: 'mint',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Z" stroke="currentColor" stroke-width="1.8"/>' +
        '<path d="m8.5 12.25 2.25 2.25 4.75-5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>' +
        '</svg>',
    },
    wallet: {
      tone: 'amber',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M5.25 7.25h11.5a2 2 0 0 1 2 2v7.5a2 2 0 0 1-2 2H5.25a2 2 0 0 1-2-2v-7.5a2 2 0 0 1 2-2Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>' +
        '<path d="M15.75 12.5h3v2.25h-3a1.125 1.125 0 1 1 0-2.25ZM6.25 7.25V5.75a1 1 0 0 1 1-1h8.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>' +
        '</svg>',
    },
    calculator: {
      tone: 'indigo',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<rect x="5.25" y="3.75" width="13.5" height="16.5" rx="2" stroke="currentColor" stroke-width="1.8"/>' +
        '<path d="M8.5 8h7M8.5 12h1M12 12h1M15.5 12h.01M8.5 15.5h1M12 15.5h1M15.5 15.5h.01" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>' +
        '</svg>',
    },
    table: {
      tone: 'slate',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<rect x="4.25" y="5.25" width="15.5" height="13.5" rx="1.5" stroke="currentColor" stroke-width="1.8"/>' +
        '<path d="M4.75 9.25h14.5M9.25 9.25v9M14.75 9.25v9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>' +
        '</svg>',
    },
    swap: {
      tone: 'blue',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M7 7.25h10.5M14.75 4.5l2.75 2.75L14.75 10M17 16.75H6.5M9.25 14 6.5 16.75 9.25 19.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>' +
        '</svg>',
    },
    bank: {
      tone: 'emerald',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="m4 9 8-4 8 4M5.25 9.75h13.5M6.75 9.75v7.5M11.25 9.75v7.5M15.75 9.75v7.5M4.75 18.25h14.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>' +
        '</svg>',
    },
    shield: {
      tone: 'ocean',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M12 4.25 18 6.5v5.25c0 3.2-2.15 6.1-6 8-3.85-1.9-6-4.8-6-8V6.5l6-2.25Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>' +
        '<path d="m9.25 12.25 1.75 1.75 3.75-4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>' +
        '</svg>',
    },
    setting: {
      tone: 'plum',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M5.5 7.5h5M14.5 7.5h4M9.5 7.5a1.75 1.75 0 1 0 0-.01ZM4.5 16.5h3M11.5 16.5h8M9.5 16.5a1.75 1.75 0 1 0 0-.01Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>' +
        '</svg>',
    },
    'safety-certificate': {
      tone: 'rose',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M12 4.5 17.75 7v4.5c0 3-1.85 5.5-5.75 7.75-3.9-2.25-5.75-4.75-5.75-7.75V7L12 4.5Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>' +
        '<path d="m10.25 11.75 1.25 1.25 2.5-2.75M9.5 17.5l-1.25 2M14.5 17.5l1.25 2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>' +
        '</svg>',
    },
    'unordered-list': {
      tone: 'steel',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M9.25 7h8M9.25 12h8M9.25 17h8M6.25 7h.01M6.25 12h.01M6.25 17h.01" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>' +
        '</svg>',
    },
    message: {
      tone: 'aqua',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M6 6.25h12a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2h-7l-4.75 3v-3H6a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>' +
        '<path d="M8.5 10.25h7M8.5 13.25h4.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>' +
        '</svg>',
    },
    notification: {
      tone: 'sunset',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<path d="M4.75 8.25h14.5a1.5 1.5 0 0 1 1.5 1.5v4.5a1.5 1.5 0 0 1-1.5 1.5H4.75a1.5 1.5 0 0 1-1.5-1.5v-4.5a1.5 1.5 0 0 1 1.5-1.5Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>' +
        '<path d="M7.25 11h6.5M7.25 13h4.25M16.75 10.25h1.5v3.5h-1.5z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>' +
        '<path d="M6.25 8.25v-1.5M17.75 8.25v-1.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>' +
        '</svg>',
    },
    appstore: {
      tone: 'aurora',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<rect x="5.25" y="3.75" width="13.5" height="16.5" rx="2.5" stroke="currentColor" stroke-width="1.8"/>' +
        '<path d="M10 6.75h4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>' +
        '<text x="12" y="14.4" text-anchor="middle" fill="currentColor" font-size="4.4" font-weight="700" font-family="-apple-system,BlinkMacSystemFont,Segoe UI,Arial,sans-serif">APP</text>' +
        '<circle cx="12" cy="17.5" r="0.9" fill="currentColor"/>' +
        '</svg>',
    },
    mobile: {
      tone: 'aurora',
      svg:
        '<svg viewBox="0 0 24 24" fill="none" aria-hidden="true">' +
        '<rect x="5.25" y="3.75" width="13.5" height="16.5" rx="2.5" stroke="currentColor" stroke-width="1.8"/>' +
        '<path d="M10 6.75h4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>' +
        '<text x="12" y="14.4" text-anchor="middle" fill="currentColor" font-size="4.4" font-weight="700" font-family="-apple-system,BlinkMacSystemFont,Segoe UI,Arial,sans-serif">APP</text>' +
        '<circle cx="12" cy="17.5" r="0.9" fill="currentColor"/>' +
        '</svg>',
    },
  };
  var GLYPH_ICON_TONE_MAP = {
    '#': 'slate',
    '✓': 'mint',
    '✎': 'violet',
    'Ⅱ': 'amber',
    '★': 'rose',
    '⌛': 'amber',
    '↗': 'aqua',
    '•': 'steel',
  };

  var loginView = document.getElementById('loginView');
  var consoleView = document.getElementById('consoleView');
  var loginForm = document.getElementById('loginForm');
  var usernameInput = document.getElementById('usernameInput');
  var passwordInput = document.getElementById('passwordInput');
  var loginSubmit = document.getElementById('loginSubmit');
  var loginTip = document.getElementById('loginTip');
  var managerName = document.getElementById('managerName');
  var languageButton = document.getElementById('languageButton');
  var logoutButton = document.getElementById('logoutButton');
  var sidebarMenu = document.getElementById('sidebarMenu');
  var contentPane = document.getElementById('contentPane');

  var state = {
    session: loadSession(),
    pageInit: loadCachedPageInit(),
    selectedPath: resolvePathFromUrl(),
    homePath: DASHBOARD_PATH,
    lang: loadLang(),
    expandedMap: {},
    rbac: {
      loaded: false,
      loading: false,
      admins: [],
      roles: [],
      permissions: [],
      modules: [],
      menus: [],
      authorizationMap: {},
      selectedAdminId: null,
      selectedRoleCode: null,
      notice: '',
    },
    outbox: {
      loaded: false,
      loading: false,
      overview: null,
      topics: [],
      messages: [],
      deadLetters: [],
      messageLoading: false,
      deadLetterLoading: false,
      filter: {
        topic: '',
        status: '',
        keyword: '',
        onlyRetried: false,
        includePayload: false,
        limit: '20',
      },
      deadFilter: {
        topic: '',
        keyword: '',
        includePayload: false,
        limit: '20',
      },
      requeue: {
        topic: '',
        limit: '20',
        nextRetryAt: '',
      },
      notice: '',
    },
    observability: {
      loaded: false,
      loading: false,
      sceneLoading: false,
      overview: null,
      apiScenes: [],
      filter: {
        limit: '20',
      },
      notice: '',
    },
    audit: {
      loaded: false,
      loading: false,
      rows: [],
      filter: {
        adminId: '',
        requestMethod: '',
        requestPath: '',
        resultStatus: '',
        from: '',
        to: '',
        limit: '20',
      },
      notice: '',
    },
    coupon: {
      loaded: false,
      loading: false,
      summary: null,
      templates: [],
      templatesLoading: false,
      issues: [],
      issuesLoading: false,
      selectedIssue: null,
      templateCreateVisible: false,
      issueCreateVisible: false,
      redeemVisible: false,
      issueDetailVisible: false,
      templateFilter: {
        sceneType: '',
        status: '',
      },
      issueFilter: {
        templateId: '',
        userId: '880100068483692100',
        status: '',
        limit: '20',
      },
      notice: '',
    },
    feedback: {
      loaded: false,
      loading: false,
      tickets: [],
      selectedTicket: null,
      filter: {
        feedbackNo: '',
        userId: '',
        feedbackType: '',
        status: '',
        limit: '20',
      },
      notice: '',
      previewImageUrl: '',
      statusUpdating: false,
      statusUpdatingFeedbackNo: '',
    },

appCenter: {
  loaded: false,
  loading: false,
  detailLoading: false,
  visitLoading: false,
  behaviorLoading: false,
  behaviorStatsLoading: false,
  behaviorReportLoading: false,
  apps: [],
  selectedApp: null,
  appFormVisible: false,
  versionFormVisible: false,
  versionFormMode: 'CREATE',
  versionEditingCode: '',
  versionDraft: null,
  versionFormNotice: '',
  versionSubmitting: false,
  settingsFormVisible: false,
  versions: [],
  devices: [],
  selectedDevice: null,
  visitRecords: [],
  behaviorEvents: [],
  behaviorStats: null,
  behaviorReport: null,
  behaviorFilter: {
    eventType: '',
    eventName: '',
    pageName: '',
    deviceId: '',
    userId: '',
    startAt: buildDateTimeLocal(-1),
    endAt: buildDateTimeLocal(0),
    limit: '50',
    reportLimit: '200',
  },
  notice: '',
},
userCenter: {
      loaded: false,
      loading: false,
      listLoading: false,
      hasNextPage: false,
      summary: null,
      users: [],
      selectedUser: null,
      filter: {
        keyword: '',
        accountStatus: '',
        kycLevel: '',
        pageNo: '1',
        pageSize: '20',
      },
      notice: '',
    },
    pricing: {
      loaded: false,
      loading: false,
      ruleLoading: false,
      rules: [],
      selectedRule: null,
      createVisible: false,
      detailVisible: false,
      filter: {
        businessSceneCode: '',
        paymentMethod: '',
        status: '',
      },
      notice: '',
    },
    trade: {
      query: {
        tradeOrderNo: '',
        requestNo: '',
        businessDomainCode: 'AICREDIT',
        bizOrderNo: '',
      },
      selectedOrder: null,
      detailVisible: false,
      notice: '',
    },
    accounting: {
      loaded: false,
      loading: false,
      eventLoading: false,
      voucherLoading: false,
      entryLoading: false,
      subjectLoading: false,
      loadedViews: createEmptyAccountingLoadedViews(),
      events: [],
      vouchers: [],
      entries: [],
      subjects: [],
      selectedEvent: null,
      eventDetailVisible: false,
      selectedVoucher: null,
      voucherDetailVisible: false,
      selectedSubjectCode: '',
      subjectDraft: createEmptyAccountingSubjectDraft(),
      subjectEditorVisible: false,
      eventFilter: {
        eventId: '',
        sourceBizNo: '',
        tradeOrderNo: '',
        payOrderNo: '',
        status: '',
        limit: '20',
      },
      voucherFilter: {
        voucherNo: '',
        sourceBizNo: '',
        tradeOrderNo: '',
        payOrderNo: '',
        status: '',
        limit: '20',
      },
      entryFilter: {
        voucherNo: '',
        subjectCode: '',
        ownerId: '',
        payOrderNo: '',
        limit: '50',
      },
      notice: '',
    },
  };

  bindEvents();
  start();

  function bindEvents() {
    loginForm.addEventListener('submit', onSubmitLogin);
    logoutButton.addEventListener('click', onLogout);
    if (languageButton) {
      languageButton.addEventListener('click', onToggleLanguage);
    }
    sidebarMenu.addEventListener('click', onMenuClick);
    contentPane.addEventListener('click', onContentClick);
    contentPane.addEventListener('change', onContentChange);
    contentPane.addEventListener('input', onContentInput);
    contentPane.addEventListener('keydown', onContentKeydown);
    window.addEventListener('popstate', onLocationChange);
    window.addEventListener('message', onEmbeddedFrameMessage);
  }

  async function start() {
    applyLanguageToStaticViews();
    if (state.session && state.session.adminId) {
      setLoginTip(localizeText('检测到登录态，正在恢复后台会话...'));
      await bootstrapConsole();
      return;
    }
    showLogin();
  }

  function showLogin() {
    loginView.classList.remove('hidden');
    consoleView.classList.add('hidden');
    syncLoginUrl(true);
    applyLanguageToStaticViews();
  }

  function showConsole() {
    loginView.classList.add('hidden');
    consoleView.classList.remove('hidden');
  }

  async function onSubmitLogin(event) {
    event.preventDefault();
    var username = usernameInput.value.trim();
    var password = passwordInput.value;

    if (!username || !password) {
      setLoginTip(localizeText('账号和密码不能为空'));
      return;
    }

    loginSubmit.disabled = true;
    loginSubmit.textContent = localizeText('登录中...');
    setLoginTip(localizeText('正在校验管理员身份...'));

    try {
      var loginData = await requestJson('/api/admin/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: username,
          password: password,
          deviceId: DEFAULT_DEVICE,
        }),
      });

      state.session = {
        adminId: parsePositiveLongText(loginData.adminId, '管理员ID'),
        username: loginData.username,
        displayName: loginData.displayName,
        accessToken: loginData.accessToken,
        tokenType: loginData.tokenType,
        expiresInSeconds: loginData.expiresInSeconds,
        loginAt: Date.now(),
      };
      saveSession(state.session);

      setLoginTip(localizeText('登录成功，正在加载后台...'));
      await bootstrapConsole();
    } catch (error) {
      clearSession();
      state.session = null;
      showLogin();
      setLoginTip(error && error.message ? error.message : localizeText('登录失败，请稍后重试'));
    } finally {
      loginSubmit.disabled = false;
      loginSubmit.textContent = localizeText('登录后台');
    }
  }

  async function bootstrapConsole() {
    if (!state.session || !state.session.adminId) {
      showLogin();
      return;
    }

    if (state.pageInit) {
      renderShell();
      showConsole();
    } else {
      showConsole();
      contentPane.innerHTML = renderPlaceholderPage('正在恢复后台会话', '正在拉取菜单与权限，请稍候...');
    }

    try {
      await refreshPageInit();
      await ensureSelectedPageData(false);
      renderShell();
      syncPathToUrl(true);
    } catch (error) {
      if (isAuthError(error)) {
        clearSession();
        clearCachedPageInit();
        state.session = null;
        state.pageInit = null;
        resetRbacState();
        resetOutboxState();
        resetObservabilityState();
        resetAuditState();
        resetAppCenterState();
        resetTradeState();
        resetAccountingState();
        showLogin();
        setLoginTip(error && error.message ? error.message : '后台初始化失败，请重新登录');
        return;
      }
      if (!state.pageInit) {
        state.pageInit = createFallbackPageInit();
        state.homePath = resolveHomePath(state.pageInit.menus || []);
      }
      await ensureSelectedPageData(false);
      renderShell();
      syncPathToUrl(true);
      setLoginTip(error && error.message ? error.message : '后台初始化较慢，已保留登录态');
    }
  }

  async function refreshPageInit() {
    var query = buildQueryString({
      adminId: String((state.session && state.session.adminId) || ''),
    });
    var pageInit = await requestJson('/api/admin/page-init' + query, {
      method: 'GET',
      headers: buildAdminHeaders(),
    });

    state.pageInit = pageInit;
    saveCachedPageInit(pageInit);
    state.homePath = resolveHomePath(pageInit.menus || []);

    if (!state.selectedPath || !menuPathExists(state.selectedPath, pageInit.menus || [])) {
      state.selectedPath = state.homePath;
    }
    syncPathToUrl(true);

    initExpanded(pageInit.menus || []);
  }

  function initExpanded(menus) {
    syncExpandedToSelectedPath(menus);
  }

  function onLogout() {
    clearSession();
    clearCachedPageInit();
    state.session = null;
    state.pageInit = null;
    state.selectedPath = DASHBOARD_PATH;
    resetRbacState();
    resetOutboxState();
    resetObservabilityState();
    resetAuditState();
    resetCouponState();
    resetFeedbackState();
    resetAppCenterState();
    resetUserCenterState();
    resetPricingState();
    resetTradeState();
    resetAccountingState();
    showLogin();
    setLoginTip(localizeText('已退出登录'));
  }

  async function navigateToSelectedPath(options) {
    var replaceHistory = !!(options && options.replaceHistory);
    var syncUrl = !options || options.syncUrl !== false;
    if (syncUrl) {
      syncPathToUrl(replaceHistory);
    }
    syncExpandedToSelectedPath();
    renderMenu();
    renderContent();
    scrollManagerViewportToTop();
    await ensureSelectedPageData(false);
    syncExpandedToSelectedPath();
    renderContent();
    scrollManagerViewportToTop();
  }

  function scrollManagerViewportToTop() {
    var mainContainer = contentPane && contentPane.closest ? contentPane.closest('.manager-main') : null;
    var embeddedFrame = contentPane ? contentPane.querySelector('iframe.manager-embedded-frame') : null;

    if (contentPane) {
      if (typeof contentPane.scrollTo === 'function') {
        try {
          contentPane.scrollTo({ top: 0, left: 0, behavior: 'auto' });
        } catch (error) {
          contentPane.scrollTop = 0;
          contentPane.scrollLeft = 0;
        }
      } else {
        contentPane.scrollTop = 0;
        contentPane.scrollLeft = 0;
      }
    }

    if (mainContainer) {
      if (typeof mainContainer.scrollTo === 'function') {
        try {
          mainContainer.scrollTo({ top: 0, left: 0, behavior: 'auto' });
        } catch (error) {
          mainContainer.scrollTop = 0;
          mainContainer.scrollLeft = 0;
        }
      } else {
        mainContainer.scrollTop = 0;
        mainContainer.scrollLeft = 0;
      }
    }

    if (typeof window.scrollTo === 'function') {
      try {
        window.scrollTo({ top: 0, left: 0, behavior: 'auto' });
      } catch (error) {
        window.scrollTo(0, 0);
      }
    }
    if (document.documentElement) {
      document.documentElement.scrollTop = 0;
      document.documentElement.scrollLeft = 0;
    }
    if (document.body) {
      document.body.scrollTop = 0;
      document.body.scrollLeft = 0;
    }

    if (embeddedFrame && embeddedFrame.contentWindow && typeof embeddedFrame.contentWindow.scrollTo === 'function') {
      try {
        embeddedFrame.contentWindow.scrollTo(0, 0);
      } catch (error) {
        // ignore same-origin timing issues during iframe refresh
      }
    }
  }

  async function onMenuClick(event) {
    var toggleTarget = event.target.closest('[data-toggle-code]');
    if (toggleTarget) {
      var menuCode = toggleTarget.getAttribute('data-toggle-code');
      toggleExpandedRootMenu(menuCode);
      renderMenu();
      return;
    }

    var rootMenuTarget = event.target.closest('[data-menu-code][data-has-children="true"]');
    if (rootMenuTarget) {
      toggleExpandedRootMenu(rootMenuTarget.getAttribute('data-menu-code'));
      renderMenu();
      return;
    }

    var menuTarget = event.target.closest('[data-path]');
    if (!menuTarget) {
      return;
    }

    var path = menuTarget.getAttribute('data-path');
    if (!path) {
      return;
    }

    state.selectedPath = path;
    await navigateToSelectedPath({ replaceHistory: false, syncUrl: true });
  }

  async function onLocationChange() {
    if (isLoginUrl()) {
      showLogin();
      return;
    }
    if (!state.session || !state.session.adminId) {
      showLogin();
      return;
    }
    var nextPath = resolvePathFromUrl();
    if (!nextPath || nextPath === state.selectedPath) {
      return;
    }
    state.selectedPath = nextPath;

    var shouldSyncUrl = false;
    if (state.pageInit && !menuPathExists(state.selectedPath, state.pageInit.menus || [])) {
      state.selectedPath = state.homePath;
      shouldSyncUrl = true;
    }

    await navigateToSelectedPath({ replaceHistory: shouldSyncUrl, syncUrl: shouldSyncUrl });
  }

  async function ensureSelectedPageData(forceReload) {
    if (isRbacPath(state.selectedPath)) {
      await ensureRbacData(forceReload);
      return;
    }
    if (isSystemObservabilityPath(state.selectedPath)) {
      await ensureObservabilityData(forceReload);
      return;
    }
    if (isSystemAuditPath(state.selectedPath)) {
      await ensureAuditData(forceReload);
      return;
    }
    if (isOutboxPath(state.selectedPath)) {
      await ensureOutboxData(forceReload);
      return;
    }
    if (isCouponPath(state.selectedPath)) {
      await ensureCouponData(forceReload);
      return;
    }
    if (isAppPath(state.selectedPath)) {
      await ensureAppCenterData(forceReload);
      return;
    }
    if (isFeedbackPath(state.selectedPath)) {
      await ensureFeedbackData(forceReload);
      return;
    }
    if (isDeliverPath(state.selectedPath)) {
      return;
    }
    if (isMessagePath(state.selectedPath) || isFundPath(state.selectedPath) || isRiskPath(state.selectedPath) || isInboundPath(state.selectedPath) || isOutboundPath(state.selectedPath)) {
      return;
    }
    if (isUserPath(state.selectedPath)) {
      await ensureUserCenterData(forceReload);
      return;
    }
    if (isPricingPath(state.selectedPath)) {
      await ensurePricingData(forceReload);
      return;
    }
    if (isTradePath(state.selectedPath)) {
      return;
    }
    if (isAccountingPath(state.selectedPath)) {
      await ensureAccountingData(forceReload);
      return;
    }
  }

  async function onContentClick(event) {
    var actionElement = event.target.closest('[data-action], [data-path]');
    if (!actionElement) {
      return;
    }

    try {
    var targetPath = actionElement.getAttribute('data-path');
    if (targetPath) {
      state.selectedPath = targetPath;
      await navigateToSelectedPath({ replaceHistory: false, syncUrl: true });
      return;
    }

    var action = actionElement.getAttribute('data-action');
    if (!action) {
      return;
    }

    if (action === 'open-home-module') {
      var actionTargetPath = actionElement.getAttribute('data-path');
      if (!actionTargetPath) {
        return;
      }
      state.selectedPath = actionTargetPath;
      await navigateToSelectedPath({ replaceHistory: false, syncUrl: true });
      return;
    }

    if (action === 'select-admin') {
      var adminId = normalizePositiveIdText(actionElement.getAttribute('data-admin-id'));
      if (!adminId) {
        return;
      }
      state.rbac.selectedAdminId = adminId;
      await loadAdminAuthorization(adminId, true);
      updateRbacDefaultRole();
      renderContent();
      return;
    }

    if (action === 'save-admin-roles') {
      await saveAdminRoles();
      return;
    }

    if (action === 'save-role-menus') {
      await saveRoleMenus();
      return;
    }

    if (action === 'save-role-permissions') {
      await saveRolePermissions();
      return;
    }

    if (action === 'reload-feedback') {
      await ensureFeedbackData(true);
      renderContent();
      return;
    }

    if (action === 'query-feedback-tickets') {
      await queryFeedbackTicketsFromForm();
      return;
    }

    if (action === 'load-feedback-ticket') {
      var feedbackNo = actionElement.getAttribute('data-feedback-no');
      if (!feedbackNo) {
        feedbackNo = readInputValue('feedbackFilterNo');
      }
      if (!feedbackNo) {
        return;
      }
      await loadFeedbackTicket(feedbackNo, true);
      return;
    }

    if (action === 'hide-feedback-ticket-modal') {
      state.feedback.selectedTicket = null;
      state.feedback.previewImageUrl = '';
      renderContent();
      return;
    }

    if (action === 'preview-feedback-attachment') {
      state.feedback.previewImageUrl = actionElement.getAttribute('data-image-url') || '';
      renderContent();
      return;
    }

    if (action === 'hide-feedback-image-preview') {
      state.feedback.previewImageUrl = '';
      renderContent();
      return;
    }

    if (action === 'change-feedback-ticket-status') {
      var statusFeedbackNo = actionElement.getAttribute('data-feedback-no');
      var feedbackTargetStatus = actionElement.getAttribute('data-target-status');
      if (!statusFeedbackNo || !feedbackTargetStatus) {
        return;
      }
      await changeFeedbackTicketStatus(statusFeedbackNo, feedbackTargetStatus);
      return;
    }

    if (action === 'reload-coupon') {
      state.coupon.templateCreateVisible = false;
      state.coupon.issueCreateVisible = false;
      state.coupon.redeemVisible = false;
      state.coupon.issueDetailVisible = false;
      await ensureCouponData(true);
      renderContent();
      return;
    }

    if (action === 'query-coupon-templates') {
      await queryCouponTemplatesFromForm();
      return;
    }

    if (action === 'open-coupon-template-create') {
      state.coupon.templateCreateVisible = true;
      renderContent();
      return;
    }

    if (action === 'hide-coupon-template-create') {
      state.coupon.templateCreateVisible = false;
      renderContent();
      return;
    }

    if (action === 'create-coupon-template') {
      await createCouponTemplateFromForm();
      return;
    }

    if (action === 'change-coupon-template-status') {
      var statusTemplateIdText = actionElement.getAttribute('data-template-id');
      var targetStatus = actionElement.getAttribute('data-target-status');
      var statusTemplateId = Number(statusTemplateIdText);
      if (!Number.isFinite(statusTemplateId) || statusTemplateId <= 0 || !targetStatus) {
        return;
      }
      await changeCouponTemplateStatus(statusTemplateId, targetStatus);
      return;
    }

    if (action === 'query-coupon-issues') {
      await queryCouponIssuesFromForm();
      return;
    }

    if (action === 'open-coupon-issue-create') {
      state.coupon.issueCreateVisible = true;
      renderContent();
      return;
    }

    if (action === 'hide-coupon-issue-create') {
      state.coupon.issueCreateVisible = false;
      renderContent();
      return;
    }

    if (action === 'issue-coupon') {
      await issueCouponFromForm();
      return;
    }

    if (action === 'open-coupon-redeem') {
      state.coupon.redeemVisible = true;
      renderContent();
      return;
    }

    if (action === 'hide-coupon-redeem') {
      state.coupon.redeemVisible = false;
      renderContent();
      return;
    }

    if (action === 'redeem-coupon') {
      await redeemCouponFromForm();
      return;
    }

    if (action === 'open-coupon-issue-detail') {
      state.coupon.issueDetailVisible = true;
      renderContent();
      return;
    }

    if (action === 'hide-coupon-issue-detail') {
      state.coupon.issueDetailVisible = false;
      renderContent();
      return;
    }

    if (action === 'load-coupon-issue') {
      var couponNo = actionElement.getAttribute('data-coupon-no');
      if (!couponNo) {
        couponNo = readInputValue('couponIssueDetailNo');
      }
      if (!couponNo) {
        return;
      }
      await loadCouponIssueDetail(couponNo, true);
      return;
    }

    if (action === 'reload-outbox') {
      await ensureOutboxData(true);
      renderContent();
      return;
    }

    if (action === 'query-outbox-messages') {
      await queryOutboxMessagesFromForm();
      return;
    }

    if (action === 'query-outbox-dead-letters') {
      await queryOutboxDeadLettersFromForm();
      return;
    }

    if (action === 'requeue-outbox-dead-letter') {
      var outboxMessageId = normalizePositiveIdText(actionElement.getAttribute('data-message-id'));
      if (!outboxMessageId) {
        return;
      }
      await requeueOutboxDeadLetter(outboxMessageId);
      return;
    }

    if (action === 'requeue-outbox-dead-letters') {
      await requeueOutboxDeadLettersBatch();
      return;
    }

    if (action === 'reload-observability') {
      await ensureObservabilityData(true);
      renderContent();
      return;
    }

    if (action === 'query-observability-scenes') {
      await queryObservabilityScenesFromForm();
      return;
    }

    if (action === 'reload-audit') {
      await ensureAuditData(true);
      renderContent();
      return;
    }

    if (action === 'query-audits') {
      await queryAuditsFromForm();
      return;
    }


if (action === 'reload-app-center') {
  await ensureAppCenterData(true);
  renderContent();
  return;
}

if (action === 'open-app-form') {
  state.appCenter.appFormVisible = true;
  renderContent();
  return;
}

if (action === 'hide-app-form') {
  state.appCenter.appFormVisible = false;
  renderContent();
  return;
}

if (action === 'create-app') {
  await createAppFromForm();
  return;
}

if (action === 'select-app') {
  var appCode = actionElement.getAttribute('data-app-code');
  if (!appCode) {
    return;
  }
  await selectAppCenterApp(appCode, true);
  if (isPathActive(APP_SETTINGS_PATH)) {
    state.appCenter.settingsFormVisible = true;
    renderContent();
  }
  return;
}

if (action === 'open-app-version-form') {
  openAppVersionForm();
  return;
}

if (action === 'open-app-version-edit-form') {
  var editVersionCode = actionElement.getAttribute('data-version-code');
  if (!editVersionCode) {
    return;
  }
  openAppVersionEditForm(editVersionCode);
  return;
}

if (action === 'hide-app-version-form') {
  closeAppVersionForm();
  return;
}

if (action === 'create-app-version') {
  await createAppVersionFromForm();
  return;
}

if (action === 'hide-app-settings') {
  state.appCenter.settingsFormVisible = false;
  renderContent();
  return;
}

if (action === 'save-app-settings') {
  await updateAppSettingsFromForm();
  return;
}

if (action === 'update-app-setting-inline') {
  await updateAppSettingInline(actionElement);
  return;
}

if (action === 'set-app-setting-toggle') {
  applyAppSettingToggle(actionElement);
  return;
}

if (action === 'change-app-version-status') {
  var versionCode = actionElement.getAttribute('data-version-code');
  var versionStatus = actionElement.getAttribute('data-target-status');
  if (!versionCode || !versionStatus) {
    return;
  }
  await changeAppVersionStatus(versionCode, versionStatus);
  return;
}

if (action === 'load-app-device-visits') {
  var deviceId = actionElement.getAttribute('data-device-id');
  if (!deviceId) {
    return;
  }
  await loadAppDeviceVisits(deviceId, true);
  return;
}

if (action === 'query-app-behavior-events') {
  await queryAppBehaviorEventsFromForm();
  return;
}

if (action === 'load-app-behavior-stats') {
  syncAppBehaviorFilterFromForm();
  await loadAppBehaviorStats(true);
  return;
}

if (action === 'load-app-behavior-report') {
  syncAppBehaviorFilterFromForm();
  await loadAppBehaviorReport(true);
  return;
}

if (action === 'export-app-behavior-report') {
  await exportAppBehaviorReportCsv();
  return;
}

if (action === 'select-app-device') {
  var selectedDeviceId = actionElement.getAttribute('data-device-id');
  if (!selectedDeviceId) {
    return;
  }
  selectAppDevice(selectedDeviceId, true);
  return;
}

if (action === 'reload-user-center') {
      await ensureUserCenterData(true);
      renderContent();
      return;
    }

    if (action === 'query-users') {
      await queryUsersFromForm();
      return;
    }

    if (action === 'query-users-prev-page') {
      await queryUsersByOffset(-1);
      return;
    }

    if (action === 'query-users-next-page') {
      await queryUsersByOffset(1);
      return;
    }

    if (action === 'load-user-detail') {
      var detailUserId = actionElement.getAttribute('data-user-id');
      if (!detailUserId) {
        detailUserId = readInputValue('userDetailUserId');
      }
      if (!detailUserId) {
        return;
      }
      await loadUserDetail(detailUserId, true);
      return;
    }

    if (action === 'hide-user-detail') {
      state.userCenter.selectedUser = null;
      renderContent();
      return;
    }

    if (action === 'change-user-status') {
      var statusUserId = actionElement.getAttribute('data-user-id');
      var userTargetStatus = actionElement.getAttribute('data-target-status');
      if (!statusUserId || !userTargetStatus) {
        return;
      }
      await changeUserStatus(statusUserId, userTargetStatus);
      return;
    }

    if (action === 'reload-trade') {
      resetTradeState();
      renderContent();
      return;
    }

    if (action === 'query-trade-by-no') {
      await queryTradeByTradeNoFromForm();
      return;
    }

    if (action === 'hide-trade-detail') {
      state.trade.selectedOrder = null;
      state.trade.detailVisible = false;
      renderContent();
      return;
    }

    if (action === 'query-trade-by-request') {
      await queryTradeByRequestNoFromForm();
      return;
    }

    if (action === 'query-trade-by-business') {
      await queryTradeByBusinessOrderFromForm();
      return;
    }

    if (action === 'reload-accounting-page') {
      await ensureAccountingData(true);
      renderContent();
      return;
    }

    if (action === 'query-accounting-events') {
      await queryAccountingEventsFromForm();
      return;
    }

    if (action === 'load-accounting-event') {
      var eventId = actionElement.getAttribute('data-event-id');
      if (!eventId) {
        return;
      }
      await loadAccountingEvent(eventId, true);
      return;
    }

    if (action === 'hide-accounting-event-detail') {
      state.accounting.eventDetailVisible = false;
      renderContent();
      return;
    }

    if (action === 'retry-accounting-event') {
      var retryEventId = actionElement.getAttribute('data-event-id');
      if (!retryEventId) {
        return;
      }
      await retryAccountingEvent(retryEventId);
      return;
    }

    if (action === 'query-accounting-vouchers') {
      await queryAccountingVouchersFromForm();
      return;
    }

    if (action === 'load-accounting-voucher') {
      var voucherNo = actionElement.getAttribute('data-voucher-no');
      if (!voucherNo) {
        return;
      }
      await loadAccountingVoucher(voucherNo, true);
      return;
    }

    if (action === 'hide-accounting-voucher-detail') {
      state.accounting.voucherDetailVisible = false;
      renderContent();
      return;
    }

    if (action === 'reverse-accounting-voucher') {
      var reverseVoucherNo = actionElement.getAttribute('data-voucher-no');
      if (!reverseVoucherNo) {
        return;
      }
      await reverseAccountingVoucher(reverseVoucherNo);
      return;
    }

    if (action === 'query-accounting-entries') {
      await queryAccountingEntriesFromForm();
      return;
    }

    if (action === 'save-accounting-subject') {
      await saveAccountingSubjectFromForm();
      return;
    }

    if (action === 'change-accounting-subject-status') {
      var statusSubjectCode = actionElement.getAttribute('data-subject-code');
      var targetEnabled = actionElement.getAttribute('data-target-enabled');
      if (!statusSubjectCode || (targetEnabled !== 'true' && targetEnabled !== 'false')) {
        return;
      }
      await changeAccountingSubjectStatus(statusSubjectCode, targetEnabled === 'true');
      return;
    }

    if (action === 'initialize-accounting-subjects') {
      await initializeAccountingSubjects();
      return;
    }

    if (action === 'reset-accounting-subjects') {
      await resetAccountingSubjectsToStandard();
      return;
    }

    if (action === 'clear-accounting-subject-form') {
      clearAccountingSubjectDraft();
      state.accounting.subjectEditorVisible = true;
      setAccountingNotice('已切换到新建科目模式');
      renderContent();
      return;
    }

    if (action === 'open-accounting-subject-create') {
      clearAccountingSubjectDraft();
      state.accounting.subjectEditorVisible = true;
      setAccountingNotice('已切换到新建科目模式');
      renderContent();
      return;
    }

    if (action === 'hide-accounting-subject-editor') {
      state.accounting.subjectEditorVisible = false;
      renderContent();
      return;
    }

    if (action === 'select-accounting-subject') {
      var subjectCode = actionElement.getAttribute('data-subject-code');
      if (!subjectCode) {
        return;
      }
      selectAccountingSubjectByCode(subjectCode);
      state.accounting.subjectEditorVisible = true;
      renderContent();
      return;
    }

    if (action === 'reload-accounting-subjects') {
      await loadAccountingSubjects(true);
      renderContent();
      return;
    }

    if (action === 'reload-pricing') {
      await ensurePricingData(true);
      renderContent();
      return;
    }

    if (action === 'query-pricing-rules') {
      await queryPricingRulesFromForm();
      return;
    }

    if (action === 'open-pricing-rule-create') {
      state.pricing.createVisible = true;
      renderContent();
      return;
    }

    if (action === 'hide-pricing-rule-create') {
      state.pricing.createVisible = false;
      renderContent();
      return;
    }

    if (action === 'create-pricing-rule') {
      await createPricingRuleFromForm();
      return;
    }

    if (action === 'change-pricing-rule-status') {
      var statusRuleIdText = actionElement.getAttribute('data-rule-id');
      var ruleTargetStatus = actionElement.getAttribute('data-target-status');
      var statusRuleId = Number(statusRuleIdText);
      if (!Number.isFinite(statusRuleId) || statusRuleId <= 0 || !ruleTargetStatus) {
        return;
      }
      await changePricingRuleStatus(statusRuleId, ruleTargetStatus);
      return;
    }

    if (action === 'load-pricing-rule') {
      var detailRuleIdText = actionElement.getAttribute('data-rule-id');
      if (!detailRuleIdText) {
        detailRuleIdText = readInputValue('pricingRuleDetailId');
      }
      var detailRuleId = Number(detailRuleIdText);
      if (!Number.isFinite(detailRuleId) || detailRuleId <= 0) {
        return;
      }
      await loadPricingRuleDetail(detailRuleId, true);
      return;
    }

    if (action === 'hide-pricing-rule-detail') {
      state.pricing.detailVisible = false;
      renderContent();
      return;
    }

    if (action === 'reload-rbac') {
      await ensureRbacData(true);
      renderContent();
      return;
    }
    } catch (error) {
      handleContentActionError(error);
    }
  }

  function handleContentActionError(error) {
    var message = error && error.message ? error.message : '操作失败，请稍后重试';
    if (isAppPath(state.selectedPath)) {
      if (state.appCenter && state.appCenter.versionFormVisible) {
        state.appCenter.versionFormNotice = message;
      }
      setAppCenterNotice(message);
      renderContent();
      return;
    }
    if (isFeedbackPath(state.selectedPath)) {
      state.feedback.notice = message;
      renderContent();
      return;
    }
    if (isPricingPath(state.selectedPath)) {
      state.pricing.notice = message;
      renderContent();
      return;
    }
    if (isUserPath(state.selectedPath)) {
      state.userCenter.notice = message;
      renderContent();
      return;
    }
    if (isAccountingPath(state.selectedPath)) {
      state.accounting.notice = message;
      renderContent();
      return;
    }
    setLoginTip(message);
  }

  function onContentChange(event) {
    var target = event.target;
    if (!target) {
      return;
    }

    if (target.id === 'rbacRoleSelect') {
      state.rbac.selectedRoleCode = target.value || null;
      renderContent();
      return;
    }

    if (target.id === 'accountingSubjectParentCode') {
      syncAccountingSubjectParentHints(target.value || '');
      return;
    }

    if (target.id === 'appVersionUpdateType') {
      var draft = syncAppVersionDraftFromForm();
      draft.updateType = target.value || 'OPTIONAL';
      if (normalizeEnumValue(draft.updateType) === 'FORCE') {
        draft.updatePromptFrequency = 'ALWAYS';
        draft.minSupportedVersionNo = '';
      }
      renderContent();
      return;
    }

    if (target.id === 'appVersionPromptFrequency') {
      var promptDraft = syncAppVersionDraftFromForm();
      promptDraft.updatePromptFrequency = target.value || 'ONCE_PER_VERSION';
      return;
    }

    if (target.id === 'appVersionMinSupported') {
      var minVersionDraft = syncAppVersionDraftFromForm();
      minVersionDraft.minSupportedVersionNo = String(target.value || '').trim();
      return;
    }

    if (target.id === 'appIosStoreUrl') {
      var storeUrlDraft = syncAppVersionDraftFromForm();
      storeUrlDraft.appStoreUrl = String(target.value || '').trim();
      return;
    }

    if (target.id === 'appVersionDescription') {
      var descriptionDraft = syncAppVersionDraftFromForm();
      descriptionDraft.versionDescription = String(target.value || '').trim() || DEFAULT_VERSION_PROMPT_MESSAGE;
      return;
    }

    if (target.id === 'appVersionPublisherRemark') {
      var publisherRemarkDraft = syncAppVersionDraftFromForm();
      publisherRemarkDraft.publisherRemark = String(target.value || '').trim();
      return;
    }

    if (target.id === 'appVersionNo') {
      updateAppVersionDraftByVersionNo(target.value || '');
      return;
    }
  }

  function onContentInput(event) {
    var target = event.target;
    if (!target) {
      return;
    }
    if (target.id === 'appVersionNo') {
      updateAppVersionDraftByVersionNo(target.value || '');
    }
  }

  async function onContentKeydown(event) {
    var target = event.target;
    if (!target || event.key !== 'Enter') {
      return;
    }
    if (target.id === 'userFilterKeyword') {
      event.preventDefault();
      await queryUsersFromForm();
      return;
    }
    if (target.id === 'feedbackFilterNo' || target.id === 'feedbackFilterUserId') {
      event.preventDefault();
      await queryFeedbackTicketsFromForm();
      return;
    }
    if (target.id === 'outboxMessageTopicFilter' || target.id === 'outboxMessageKeywordFilter') {
      event.preventDefault();
      await queryOutboxMessagesFromForm();
      return;
    }
    if (target.id === 'outboxDeadTopicFilter' || target.id === 'outboxDeadKeywordFilter') {
      event.preventDefault();
      await queryOutboxDeadLettersFromForm();
      return;
    }
    if (target.id === 'outboxRequeueTopic' || target.id === 'outboxRequeueLimit') {
      event.preventDefault();
      await requeueOutboxDeadLettersBatch();
      return;
    }
    if (target.id === 'observabilitySceneLimit') {
      event.preventDefault();
      await queryObservabilityScenesFromForm();
      return;
    }
    if (target.id === 'auditFilterAdminId' || target.id === 'auditFilterRequestPath') {
      event.preventDefault();
      await queryAuditsFromForm();
      return;
    }
    if (target.id === 'tradeQueryTradeNo') {
      event.preventDefault();
      await queryTradeByTradeNoFromForm();
      return;
    }
    if (target.id === 'tradeQueryRequestNo') {
      event.preventDefault();
      await queryTradeByRequestNoFromForm();
      return;
    }
    if (target.id === 'tradeQueryBizOrderNo') {
      event.preventDefault();
      await queryTradeByBusinessOrderFromForm();
    }
  }

  function onEmbeddedFrameMessage(event) {
    if (!event || event.origin !== window.location.origin) {
      return;
    }
    var data = event.data;
    if (!data) {
      return;
    }
    if (data.type === 'openaipay-deliver-frame-height') {
      updateEmbeddedFrameHeight('managerDeliverFrame', data.height);
      return;
    }
    if (data.type !== 'openaipay-admin-frame-height') {
      return;
    }
    updateEmbeddedFrameHeight(data.frameId, data.height);
  }

  function updateEmbeddedFrameHeight(frameId, rawHeight) {
    var frame = document.getElementById(String(frameId || ''));
    var height = Number(rawHeight);
    if (!frame || !frame.classList || !frame.classList.contains('manager-embedded-frame')) {
      return;
    }
    if (!Number.isFinite(height) || height <= 0) {
      return;
    }
    var targetHeight = Math.max(Math.ceil(height + 2), 240);
    var currentHeight = Number(String(frame.style.height || '').replace('px', ''));
    if (Number.isFinite(currentHeight) && Math.abs(currentHeight - targetHeight) < 2) {
      return;
    }
    frame.style.height = String(targetHeight) + 'px';
  }

  function onToggleLanguage() {
    state.lang = state.lang === 'en' ? 'zh' : 'en';
    saveLang(state.lang);
    applyLanguageToStaticViews();
    if (state.session && state.session.adminId) {
      renderShell();
      return;
    }
    loginSubmit.textContent = localizeText('登录后台');
    setLoginTip(localizeText('请输入账号密码后登录'));
  }

  function renderShell() {
    var displayName = pickDisplayName();
    managerName.textContent = displayName;
    if (languageButton) {
      languageButton.textContent = state.lang === 'en' ? '中文' : 'EN';
    }
    logoutButton.textContent = localizeText('退出登录');
    applyLanguageToStaticViews();
    renderMenu();
    renderContent();
  }

  function renderMenu() {
    var menus = state.pageInit && Array.isArray(state.pageInit.menus) ? state.pageInit.menus : [];
    var tree = buildDisplayMenuTree(menus);
    sidebarMenu.innerHTML = tree.map(renderMenuGroup).join('');
  }

  function renderMenuGroup(node) {
    var hasChildren = node.children && node.children.length > 0;
    var expanded = !!state.expandedMap[node.menuCode];
    var activeClass = isMenuNodeActive(node) ? ' active' : '';
    var html = '';
    html += '<div class="manager-menu-group">';
    var rootActionAttr = hasChildren
      ? ' data-menu-code="' + escapeHtml(node.menuCode) + '" data-has-children="true" aria-expanded="' + (expanded ? 'true' : 'false') + '"'
      : ' data-path="' + escapeHtml(node.path) + '"';
    html +=
      '<button class="manager-menu-item' +
      activeClass +
      '" type="button"' +
      rootActionAttr +
      '>';
    html += '<span class="manager-menu-icon">' + renderManagerIcon(node.icon, 'menu') + '</span>';
    html += '<span class="manager-menu-name">' + escapeHtml(localizeMenuName(node.menuName, node.path, node.menuCode)) + '</span>';
    if (hasChildren) {
      html +=
        '<span class="manager-menu-expand" data-toggle-code="' +
        escapeHtml(node.menuCode) +
        '">' +
        (expanded ? '▾' : '▸') +
        '</span>';
    }
    html += '</button>';

    if (hasChildren && expanded) {
      html += '<div class="manager-sub-menu">';
      html += node.children
        .map(function (child) {
          var childActive = isMenuNodeActive(child) ? ' active' : '';
          return (
            '<button class="manager-menu-item' +
            childActive +
            '" type="button" data-path="' +
            escapeHtml(child.path) +
            '">' +
            '<span class="manager-menu-icon">' +
            renderManagerIcon(child.icon, 'menu') +
            '</span>' +
            '<span class="manager-menu-name">' +
            escapeHtml(localizeMenuName(child.menuName, child.path, child.menuCode)) +
            '</span>' +
            '</button>'
          );
        })
        .join('');
      html += '</div>';
    }

    html += '</div>';
    return html;
  }

  function renderContent() {
    var currentMenu = findMenuByPath(state.selectedPath);
    var title = currentMenu ? localizeMenuName(currentMenu.menuName, currentMenu.path, currentMenu.menuCode) : localizeText('工作台');
    var subtitle = '';

    if (isRbacPath(state.selectedPath)) {
      renderStandardContent(renderRbacPage(title, subtitle));
      return;
    }

    if (isSystemObservabilityPath(state.selectedPath)) {
      renderStandardContent(renderObservabilityPage(title, subtitle));
      return;
    }

    if (isSystemAuditPath(state.selectedPath)) {
      renderStandardContent(renderAuditPage(title, subtitle));
      return;
    }

    if (isOutboxPath(state.selectedPath)) {
      renderStandardContent(renderOutboxPage(title, subtitle));
      return;
    }

    if (isFeedbackPath(state.selectedPath)) {
      renderStandardContent(renderFeedbackPage(title, subtitle));
      return;
    }

    if (isAppPath(state.selectedPath)) {
      renderStandardContent(renderAppPage(title, subtitle));
      return;
    }

    if (isMessagePath(state.selectedPath)) {
      renderEmbeddedContent(
        'managerMessageFrame',
        buildEmbeddedConsoleSrc('/manager/message-console.html'),
        renderMessagePage(title, subtitle)
      );
      return;
    }

    if (isFundPath(state.selectedPath)) {
      renderEmbeddedContent(
        'managerFundFrame',
        resolveFundConsoleSrc(),
        renderFundPage(title, subtitle)
      );
      return;
    }

    if (isRiskPath(state.selectedPath)) {
      renderEmbeddedContent(
        'managerRiskFrame',
        buildEmbeddedConsoleSrc('/manager/risk-console.html'),
        renderRiskPage(title, subtitle)
      );
      return;
    }

    if (isUserPath(state.selectedPath)) {
      renderStandardContent(renderUserCenterPage(title, subtitle));
      return;
    }

    if (isPricingPath(state.selectedPath)) {
      renderStandardContent(renderPricingPage(title, subtitle));
      return;
    }

    if (isTradePath(state.selectedPath)) {
      renderStandardContent(renderTradePage(title, subtitle));
      return;
    }

    if (isAccountingPath(state.selectedPath)) {
      renderStandardContent(renderAccountingPage(title, subtitle));
      return;
    }

    if (isCouponPath(state.selectedPath)) {
      renderStandardContent(renderCouponPage(title, subtitle));
      return;
    }

    if (isDeliverPath(state.selectedPath)) {
      renderEmbeddedContent(
        'managerDeliverFrame',
        '/manager/deliver-console.html',
        renderDeliverPage(title, subtitle)
      );
      return;
    }

    if (isInboundPath(state.selectedPath) || isOutboundPath(state.selectedPath)) {
      renderEmbeddedContent(
        'managerTradeOpsFrame',
        buildEmbeddedConsoleSrc('/manager/trade-ops-console.html'),
        renderTradeOpsPage(title, subtitle)
      );
      return;
    }

    if (isPathActive(state.homePath)) {
      renderStandardContent(renderHomePage(title, subtitle));
      return;
    }

    renderStandardContent(renderPlaceholderPage(title, subtitle));
  }

  function renderStandardContent(html) {
    if (contentPane) {
      contentPane.removeAttribute('data-embedded-render-key');
    }
    contentPane.innerHTML = localizeHtml(html);
  }

  function renderEmbeddedContent(frameId, frameSrc, html) {
    var renderKey = [String(frameId || ''), String(frameSrc || ''), String(state.selectedPath || ''), String(state.lang || 'zh')].join('|');
    if (
      contentPane &&
      contentPane.getAttribute('data-embedded-render-key') === renderKey &&
      contentPane.querySelector('#' + frameId)
    ) {
      return;
    }
    contentPane.setAttribute('data-embedded-render-key', renderKey);
    contentPane.innerHTML = localizeHtml(html);
  }

  function captureFeedbackDetailViewState() {
    var detailPanel = contentPane.querySelector('.manager-feedback-detail-modal');
    return {
      modalScrollTop: detailPanel ? detailPanel.scrollTop : null,
      pageScrollTop: typeof window.scrollY === 'number' ? window.scrollY : (window.pageYOffset || 0),
    };
  }

  function restoreFeedbackDetailViewState(snapshot) {
    if (!snapshot) {
      return;
    }
    if (typeof snapshot.pageScrollTop === 'number') {
      window.scrollTo(0, snapshot.pageScrollTop);
    }
    if (typeof snapshot.modalScrollTop !== 'number') {
      return;
    }
    var detailPanel = contentPane.querySelector('.manager-feedback-detail-modal');
    if (detailPanel) {
      detailPanel.scrollTop = snapshot.modalScrollTop;
    }
  }

  function renderContentWithFeedbackDetailViewState(snapshot) {
    renderContent();
    restoreFeedbackDetailViewState(snapshot);
  }


  function renderAppPage(title, subtitle) {
    if (isPathActive(APP_SETTINGS_PATH)) {
      return renderAppSettingsPage(title, subtitle);
    }
    if (isPathActive(APP_BEHAVIOR_PATH)) {
      return renderAppBehaviorPage(title, subtitle);
    }
    if (isPathActive(APP_ROOT_PATH)) {
      return renderAppOverviewPage(title, subtitle);
    }

    var appCenter = state.appCenter || {};
    var selectedApp = appCenter.selectedApp;
    var versions = Array.isArray(appCenter.versions) ? appCenter.versions : [];
    var devices = Array.isArray(appCenter.devices) ? appCenter.devices : [];
    var selectedDevice = appCenter.selectedDevice;
    var visitRecords = Array.isArray(appCenter.visitRecords) ? appCenter.visitRecords : [];
    var appListLoading = isManagerDataLoading(appCenter.loaded, appCenter.loading, appCenter.notice);
    var appDetailLoading = appListLoading || !!appCenter.detailLoading;
    var visitRecordsLoading = appDetailLoading || !!appCenter.visitLoading;
    var html = '';

    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle || langText('管理 iPhone 客户端版本、iOS 包、设备和访问记录。', 'Manage iPhone app versions, iOS packages, devices, and visit records.')) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="open-app-form">新建应用</button>';
    if (selectedApp) {
      html += '<button class="manager-btn" type="button" data-action="open-app-version-form">新建版本</button>';
    }
    html += '<button class="manager-btn" type="button" data-action="reload-app-center">' + escapeHtml(langText('刷新数据', 'Refresh')) + '</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(appCenter.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">' + escapeHtml(langText('应用列表', 'Apps')) + '</h3>';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>应用Code</th><th>应用名称</th><th>版本提示</th><th>状态</th><th>更新时间</th><th>操作</th></tr></thead><tbody>';
    if (!appCenter.apps || appCenter.apps.length === 0) {
      html += renderManagerTableStateRow(6, appListLoading, '应用定义加载中...', '暂无应用定义');
    } else {
      html += appCenter.apps.map(function (app) {
        var selected = selectedApp && selectedApp.appCode === app.appCode ? ' style="background:#f5fbff;"' : '';
        return '<tr' + selected + '>' +
          '<td>' + escapeHtml(app.appCode || '-') + '</td>' +
          '<td>' + escapeHtml(app.appName || '-') + '</td>' +
          '<td>' + renderBooleanBadge(app.versionPromptEnabled, '已开启', '已关闭') + '</td>' +
          '<td>' + renderStatusBadge(app.status || '-', formatAppStatus(app.status)) + '</td>' +
          '<td>' + escapeHtml(formatDateTime(app.updatedAt)) + '</td>' +
          '<td><button class="manager-btn" type="button" data-action="select-app" data-app-code="' + escapeHtml(app.appCode || '') + '">查看版本</button></td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';

    if (selectedApp) {
      html += '<section class="manager-panel" style="margin-top:14px;">';
      html += '<h3 class="manager-panel-title">版本列表</h3>';
      html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
      html += '<thead><tr><th>版本Code</th><th>版本号</th><th>更新类型</th><th>状态</th><th>最新发布</th><th>包状态</th><th>商店地址</th><th>操作</th></tr></thead><tbody>';
      if (!versions || versions.length === 0) {
        html += renderManagerTableStateRow(8, appDetailLoading, '版本数据加载中...', '暂无版本');
      } else {
        html += versions.map(function (version) {
          var iosPackage = version.iosPackage || null;
          var actions = [];
          var normalizedVersionStatus = normalizeEnumValue(version.status);
          actions.push('<button class="manager-btn" type="button" data-action="open-app-version-edit-form" data-version-code="' + escapeHtml(version.versionCode || '') + '">修改</button>');
          if (normalizedVersionStatus !== 'ENABLED') {
            actions.push('<button class="manager-btn" type="button" data-action="change-app-version-status" data-version-code="' + escapeHtml(version.versionCode || '') + '" data-target-status="ENABLED">启用</button>');
          }
          if (normalizedVersionStatus !== 'DISABLED') {
            actions.push('<button class="manager-btn" type="button" data-action="change-app-version-status" data-version-code="' + escapeHtml(version.versionCode || '') + '" data-target-status="DISABLED">停用</button>');
          }
          return '<tr>' +
            '<td>' + escapeHtml(version.versionCode || '-') + '</td>' +
            '<td>' + escapeHtml(version.appVersionNo || '-') + '</td>' +
            '<td>' + escapeHtml(formatAppUpdateType(version.updateType)) + '</td>' +
            '<td>' + renderStatusBadge(version.status || '-', formatAppVersionStatus(version.status)) + '</td>' +
            '<td>' + (version.latestPublishedVersion ? '是' : '否') + '</td>' +
            '<td>' + renderStatusBadge(iosPackage && iosPackage.releaseStatus || '-', formatAppReleaseStatus(iosPackage && iosPackage.releaseStatus)) + '</td>' +
            '<td>' + escapeHtml(iosPackage && iosPackage.appStoreUrl || '-') + '</td>' +
            '<td>' + actions.join(' ') + '</td>' +
            '</tr>';
        }).join('');
      }
      html += '</tbody></table></div>';
      html += '</section>';

      html += '<section class="manager-panel" style="margin-top:14px;">';
      html += '<h3 class="manager-panel-title">设备列表</h3>';
      html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
      html += '<thead><tr><th>设备ID</th><th>用户ID</th><th>昵称</th><th>登录账号</th><th>手机号</th><th>状态</th><th>当前版本</th><th>当前iOS包</th><th>最近打开</th><th>系统版本</th><th>操作</th></tr></thead><tbody>';
      if (!devices || devices.length === 0) {
        html += renderManagerTableStateRow(11, appDetailLoading, '设备数据加载中...', '暂无设备记录');
      } else {
        html += devices.map(function (device) {
          var selectedRow = selectedDevice && selectedDevice.deviceId === device.deviceId ? ' style="background:#f5fbff;"' : '';
          return '<tr' + selectedRow + '>' +
            '<td>' + escapeHtml(device.deviceId || '-') + '</td>' +
            '<td>' + escapeHtml(device.userId || '-') + '</td>' +
            '<td>' + escapeHtml(device.nickname || '-') + '</td>' +
            '<td>' + escapeHtml(device.loginId || '-') + '</td>' +
            '<td>' + escapeHtml(device.mobile || '-') + '</td>' +
            '<td>' + renderStatusBadge(device.status || '-', formatAppDeviceStatus(device.status)) + '</td>' +
            '<td>' + escapeHtml(device.currentVersionNo || device.currentVersionCode || '-') + '</td>' +
            '<td>' + escapeHtml(device.currentIosCode || '-') + '</td>' +
            '<td>' + escapeHtml(formatDateTime(device.lastOpenedAt)) + '</td>' +
            '<td>' + escapeHtml(device.osVersion || '-') + '</td>' +
            '<td><button class="manager-btn" type="button" data-action="select-app-device" data-device-id="' + escapeHtml(device.deviceId || '') + '">查看</button> <button class="manager-btn" type="button" data-action="load-app-device-visits" data-device-id="' + escapeHtml(device.deviceId || '') + '">查看访问</button></td>' +
            '</tr>';
        }).join('');
      }
      html += '</tbody></table></div>';
      html += '</section>';

      html += '<section class="manager-panel" style="margin-top:14px;">';
      html += '<h3 class="manager-panel-title">设备详情' + (selectedDevice ? ' - ' + escapeHtml(selectedDevice.deviceId || '') : '') + '</h3>';
      if (!selectedDevice) {
        html += '<div class="manager-empty-state" style="padding:24px 0 8px;">请选择设备查看详情</div>';
      } else {
        html += '<div class="manager-coupon-detail-grid">';
        html += renderDetailItem('设备ID', selectedDevice.deviceId || '-');
        html += renderDetailItem('应用Code', selectedDevice.appCode || '-');
        html += renderDetailItem('设备状态', formatAppDeviceStatus(selectedDevice.status));
        html += renderDetailItem('当前版本', selectedDevice.currentVersionNo || selectedDevice.currentVersionCode || '-');
        html += renderDetailItem('当前iOS包', selectedDevice.currentIosCode || '-');
        html += renderDetailItem('设备品牌', selectedDevice.deviceBrand || '-');
        html += renderDetailItem('系统版本', selectedDevice.osVersion || '-');
        html += renderDetailItem('客户端ID', Array.isArray(selectedDevice.clientIds) && selectedDevice.clientIds.length > 0 ? selectedDevice.clientIds.join(', ') : '-');
        html += renderDetailItem('用户ID', selectedDevice.userId || '-');
        html += renderDetailItem('爱付号', selectedDevice.aipayUid || '-');
        html += renderDetailItem('登录账号', selectedDevice.loginId || '-');
        html += renderDetailItem('账户状态', formatUserAccountStatus(selectedDevice.accountStatus));
        html += renderDetailItem('KYC等级', formatUserKycLevel(selectedDevice.kycLevel));
        html += renderDetailItem('昵称', selectedDevice.nickname || '-');
        html += renderDetailItem('手机号', selectedDevice.mobile || '-');
        html += renderDetailItem('脱敏姓名', selectedDevice.maskedRealName || '-');
        html += renderDetailItem('脱敏证件号', selectedDevice.idCardNoMasked || '-');
        html += renderDetailItem('头像地址', selectedDevice.avatarUrl || '-');
        html += renderDetailItem('国家编码', selectedDevice.countryCode || '-');
        html += renderDetailItem('性别', selectedDevice.gender || '-');
        html += renderDetailItem('地区', selectedDevice.region || '-');
        html += renderDetailItem('安装时间', formatDateTime(selectedDevice.installedAt));
        html += renderDetailItem('启动时间', formatDateTime(selectedDevice.startedAt));
        html += renderDetailItem('最近打开', formatDateTime(selectedDevice.lastOpenedAt));
        html += renderDetailItem('最近登录', formatDateTime(selectedDevice.lastLoginAt));
        html += renderDetailItem('版本更新时间', formatDateTime(selectedDevice.appUpdatedAt));
        html += renderDetailItem('记录创建时间', formatDateTime(selectedDevice.createdAt));
        html += renderDetailItem('记录更新时间', formatDateTime(selectedDevice.updatedAt));
        html += '</div>';
      }
      html += '</section>';

      html += '<section class="manager-panel" style="margin-top:14px;">';
      html += '<h3 class="manager-panel-title">访问记录' + (selectedDevice ? ' - ' + escapeHtml(selectedDevice.deviceId || '') : '') + '</h3>';
      html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
      html += '<thead><tr><th>调用接口</th><th>客户端ID</th><th>网络</th><th>结果摘要</th><th>耗时(ms)</th><th>调用时间</th></tr></thead><tbody>';
      if (!visitRecords || visitRecords.length === 0) {
        html += renderManagerTableStateRow(6, visitRecordsLoading, '访问记录加载中...', '暂无访问记录');
      } else {
        html += visitRecords.map(function (record) {
          return '<tr>' +
            '<td>' + escapeHtml(record.apiName || '-') + '</td>' +
            '<td>' + escapeHtml(record.clientId || '-') + '</td>' +
            '<td>' + escapeHtml(formatNetworkType(record.networkType)) + '</td>' +
            '<td>' + escapeHtml(record.resultSummary || '-') + '</td>' +
            '<td>' + escapeHtml(record.durationMs || '-') + '</td>' +
            '<td>' + escapeHtml(formatDateTime(record.calledAt)) + '</td>' +
            '</tr>';
        }).join('');
      }
      html += '</tbody></table></div>';
      html += '</section>';

    }

    html += renderAppDefinitionModal(appCenter.appFormVisible);
    html += renderAppVersionModal(selectedApp, appCenter.versionFormVisible);
    return html;
  }

  function renderAppOverviewPage(title, subtitle) {
    var appCenter = state.appCenter || {};
    var selectedApp = appCenter.selectedApp;
    var appListLoading = isManagerDataLoading(appCenter.loaded, appCenter.loading, appCenter.notice);
    var html = '';

    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle || langText('查看应用基础信息与当前选中应用概览，并可快速进入版本管理与埋点管理。', 'View app basics and selected-app overview, with quick access to version and behavior management.')) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="open-app-form">新建应用</button>';
    html += '<button class="manager-btn" type="button" data-action="reload-app-center">' + escapeHtml(langText('刷新数据', 'Refresh')) + '</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(appCenter.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">' + escapeHtml(langText('应用列表', 'Apps')) + '</h3>';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>应用Code</th><th>应用名称</th><th>版本提示</th><th>状态</th><th>更新时间</th><th>操作</th></tr></thead><tbody>';
    if (!appCenter.apps || appCenter.apps.length === 0) {
      html += renderManagerTableStateRow(6, appListLoading, '应用定义加载中...', '暂无应用定义');
    } else {
      html += appCenter.apps.map(function (app) {
        var selected = selectedApp && selectedApp.appCode === app.appCode ? ' style="background:#f5fbff;"' : '';
        return '<tr' + selected + '>'
          + '<td>' + escapeHtml(app.appCode || '-') + '</td>'
          + '<td>' + escapeHtml(app.appName || '-') + '</td>'
          + '<td>' + renderBooleanBadge(app.versionPromptEnabled, '已开启', '已关闭') + '</td>'
          + '<td>' + renderStatusBadge(app.status || '-', formatAppStatus(app.status)) + '</td>'
          + '<td>' + escapeHtml(formatDateTime(app.updatedAt)) + '</td>'
          + '<td><button class="manager-btn" type="button" data-action="select-app" data-app-code="' + escapeHtml(app.appCode || '') + '">选中应用</button></td>'
          + '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">当前应用概览</h3>';
    if (!selectedApp) {
      html += '<div class="manager-empty-state" style="padding:24px 0 8px;">请选择应用查看概览</div>';
    } else {
      html += '<div class="manager-coupon-detail-grid">';
      html += renderDetailItem('应用Code', selectedApp.appCode || '-');
      html += renderDetailItem('应用名称', selectedApp.appName || '-');
      html += renderDetailItem('状态', formatAppStatus(selectedApp.status));
      html += renderDetailItem('版本提示', selectedApp.versionPromptEnabled ? '已开启' : '已关闭');
      html += renderDetailItem('版本数量', Array.isArray(appCenter.versions) ? appCenter.versions.length : 0);
      html += renderDetailItem('设备数量', Array.isArray(appCenter.devices) ? appCenter.devices.length : 0);
      html += renderDetailItem('更新时间', formatDateTime(selectedApp.updatedAt));
      html += '</div>';
      html += '<div class="manager-rbac-actions" style="margin-top:10px;">';
      html += '<button class="manager-btn" type="button" data-path="' + escapeHtml(APP_VERSION_PATH) + '">进入版本管理</button>';
      html += '<button class="manager-btn" type="button" data-path="' + escapeHtml(APP_BEHAVIOR_PATH) + '">进入埋点管理</button>';
      html += '<button class="manager-btn" type="button" data-path="' + escapeHtml(APP_SETTINGS_PATH) + '">进入提示开关</button>';
      html += '</div>';
    }
    html += '</section>';

    html += renderAppDefinitionModal(appCenter.appFormVisible);
    return html;
  }

  function renderAppBehaviorPage(title, subtitle) {
    var appCenter = state.appCenter || {};
    var selectedApp = appCenter.selectedApp;
    var behaviorEvents = Array.isArray(appCenter.behaviorEvents) ? appCenter.behaviorEvents : [];
    var behaviorStats = appCenter.behaviorStats || null;
    var behaviorReport = appCenter.behaviorReport || null;
    var behaviorReportRows = behaviorReport && Array.isArray(behaviorReport.rows) ? behaviorReport.rows : [];
    var behaviorFilter = appCenter.behaviorFilter || {};
    var appCenterLoading = isManagerDataLoading(appCenter.loaded, appCenter.loading, appCenter.notice);
    var behaviorLoading = appCenterLoading || !!appCenter.behaviorLoading;
    var behaviorStatsLoading = appCenterLoading || !!appCenter.behaviorStatsLoading;
    var behaviorReportLoading = appCenterLoading || !!appCenter.behaviorReportLoading;
    var behaviorEventTypeText = behaviorStats && Array.isArray(behaviorStats.eventTypeDistribution) && behaviorStats.eventTypeDistribution.length > 0
      ? behaviorStats.eventTypeDistribution.slice(0, 8).map(function (item) {
          return (item && item.key ? item.key : '-') + ':' + String(item && item.count != null ? item.count : 0);
        }).join(' / ')
      : '-';
    var behaviorTopEventText = behaviorStats && Array.isArray(behaviorStats.topEventDistribution) && behaviorStats.topEventDistribution.length > 0
      ? behaviorStats.topEventDistribution.slice(0, 8).map(function (item) {
          return (item && item.key ? item.key : '-') + ':' + String(item && item.count != null ? item.count : 0);
        }).join(' / ')
      : '-';
    var html = '';

    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle || langText('独立管理 iPhone 客户端行为埋点明细、统计与报表导出。', 'Manage iPhone behavior tracking details, metrics, and report exports in a dedicated page.')) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="reload-app-center">' + escapeHtml(langText('刷新数据', 'Refresh')) + '</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(appCenter.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    if (!selectedApp) {
      html += '<section class="manager-panel" style="margin-top:14px;">';
      html += '<h3 class="manager-panel-title">当前应用</h3>';
      html += '<div class="manager-empty-state" style="padding:24px 0 8px;">' + (appCenterLoading ? '应用定义加载中...' : '暂无应用定义') + '</div>';
      html += '</section>';
    } else {
      html += '<section class="manager-panel" style="margin-top:14px;">';
      html += '<h3 class="manager-panel-title">当前应用</h3>';
      html += '<div class="manager-coupon-detail-grid">';
      html += renderDetailItem('应用Code', selectedApp.appCode || '-');
      html += renderDetailItem('应用名称', selectedApp.appName || '-');
      html += renderDetailItem('状态', formatAppStatus(selectedApp.status));
      html += renderDetailItem('更新时间', formatDateTime(selectedApp.updatedAt));
      html += '</div>';
      html += '</section>';

      html += '<section class="manager-panel" style="margin-top:14px;">';
      html += '<h3 class="manager-panel-title">行为埋点筛选 - ' + escapeHtml(selectedApp.appCode || '') + '</h3>';
      html += '<div class="manager-coupon-form-grid compact">';
      html += renderField('事件类型', '<input id="appBehaviorFilterEventType" class="form-input" value="' + escapeHtml(behaviorFilter.eventType || '') + '" placeholder="如 PAGE_VIEW"/>');
      html += renderField('事件名称', '<input id="appBehaviorFilterEventName" class="form-input" value="' + escapeHtml(behaviorFilter.eventName || '') + '" placeholder="如 OPEN_PAGE"/>');
      html += renderField('页面名称', '<input id="appBehaviorFilterPageName" class="form-input" value="' + escapeHtml(behaviorFilter.pageName || '') + '" placeholder="如 Home"/>');
      html += renderField('设备ID', '<input id="appBehaviorFilterDeviceId" class="form-input" value="' + escapeHtml(behaviorFilter.deviceId || '') + '" placeholder="精确匹配 deviceId"/>');
      html += renderField('用户ID', '<input id="appBehaviorFilterUserId" class="form-input" value="' + escapeHtml(behaviorFilter.userId || '') + '" placeholder="精确匹配 userId"/>');
      html += renderField('开始时间', '<input id="appBehaviorFilterStartAt" type="datetime-local" class="form-input" value="' + escapeHtml(behaviorFilter.startAt || '') + '"/>');
      html += renderField('结束时间', '<input id="appBehaviorFilterEndAt" type="datetime-local" class="form-input" value="' + escapeHtml(behaviorFilter.endAt || '') + '"/>');
      html += renderField('明细条数', '<input id="appBehaviorFilterLimit" class="form-input" value="' + escapeHtml(behaviorFilter.limit || '50') + '" placeholder="默认50，最多500"/>');
      html += renderField('报表条数', '<input id="appBehaviorFilterReportLimit" class="form-input" value="' + escapeHtml(behaviorFilter.reportLimit || '200') + '" placeholder="默认200，最多1000"/>');
      html += '</div>';
      html += '<div class="manager-rbac-actions">';
      html += '<button class="manager-btn primary" type="button" data-action="query-app-behavior-events">查询明细</button>';
      html += '<button class="manager-btn" type="button" data-action="load-app-behavior-stats">刷新统计</button>';
      html += '<button class="manager-btn" type="button" data-action="load-app-behavior-report">刷新报表</button>';
      html += '<button class="manager-btn" type="button" data-action="export-app-behavior-report">导出CSV</button>';
      html += '</div>';
      html += '</section>';

      html += '<section class="manager-panel" style="margin-top:14px;">';
      html += '<h3 class="manager-panel-title">埋点统计看板</h3>';
      if (!behaviorStats) {
        html += '<div class="manager-empty-state" style="padding:24px 0 8px;">' + (behaviorStatsLoading ? '统计加载中...' : '暂无统计数据') + '</div>';
      } else {
        html += '<div class="manager-coupon-detail-grid">';
        html += renderDetailItem('事件总数', behaviorStats.totalCount == null ? '-' : behaviorStats.totalCount);
        html += renderDetailItem('设备去重数', behaviorStats.uniqueDeviceCount == null ? '-' : behaviorStats.uniqueDeviceCount);
        html += renderDetailItem('用户去重数', behaviorStats.uniqueUserCount == null ? '-' : behaviorStats.uniqueUserCount);
        html += renderDetailItem('成功事件数', behaviorStats.successCount == null ? '-' : behaviorStats.successCount);
        html += renderDetailItem('失败事件数', behaviorStats.failureCount == null ? '-' : behaviorStats.failureCount);
        html += renderDetailItem('平均耗时(ms)', formatDecimalText(behaviorStats.avgDurationMs, 2));
        html += renderDetailItem('最早事件时间', formatDateTime(behaviorStats.firstOccurredAt));
        html += renderDetailItem('最近事件时间', formatDateTime(behaviorStats.lastOccurredAt));
        html += renderDetailItem('类型分布 Top', behaviorEventTypeText);
        html += renderDetailItem('事件分布 Top', behaviorTopEventText);
        html += '</div>';
      }
      html += '</section>';

      html += '<section class="manager-panel" style="margin-top:14px;">';
      html += '<h3 class="manager-panel-title">行为埋点明细</h3>';
      html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
      html += '<thead><tr><th>事件时间</th><th>事件名称</th><th>事件类型</th><th>页面</th><th>动作</th><th>设备ID</th><th>用户ID</th><th>结果</th><th>耗时(ms)</th></tr></thead><tbody>';
      if (!behaviorEvents || behaviorEvents.length === 0) {
        html += renderManagerTableStateRow(9, behaviorLoading, '埋点明细加载中...', '暂无埋点明细');
      } else {
        html += behaviorEvents.map(function (event) {
          return '<tr>'
            + '<td>' + escapeHtml(formatDateTime(event.eventOccurredAt || event.createdAt)) + '</td>'
            + '<td>' + escapeHtml(event.eventName || '-') + '</td>'
            + '<td>' + escapeHtml(event.eventType || '-') + '</td>'
            + '<td>' + escapeHtml(event.pageName || '-') + '</td>'
            + '<td>' + escapeHtml(event.actionName || '-') + '</td>'
            + '<td>' + escapeHtml(event.deviceId || '-') + '</td>'
            + '<td>' + escapeHtml(event.userId || '-') + '</td>'
            + '<td>' + escapeHtml(event.resultStatus || '-') + '</td>'
            + '<td>' + escapeHtml(event.durationMs || '-') + '</td>'
            + '</tr>';
        }).join('');
      }
      html += '</tbody></table></div>';
      html += '</section>';

      html += '<section class="manager-panel" style="margin-top:14px;">';
      html += '<h3 class="manager-panel-title">埋点报表</h3>';
      html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
      html += '<thead><tr><th>统计日期</th><th>事件类型</th><th>事件名称</th><th>总数</th><th>成功</th><th>失败</th><th>设备数</th><th>用户数</th><th>平均耗时(ms)</th><th>最大耗时(ms)</th></tr></thead><tbody>';
      if (!behaviorReportRows || behaviorReportRows.length === 0) {
        html += renderManagerTableStateRow(10, behaviorReportLoading, '埋点报表加载中...', '暂无埋点报表数据');
      } else {
        html += behaviorReportRows.map(function (row) {
          return '<tr>'
            + '<td>' + escapeHtml(row.statDate || '-') + '</td>'
            + '<td>' + escapeHtml(row.eventType || '-') + '</td>'
            + '<td>' + escapeHtml(row.eventName || '-') + '</td>'
            + '<td>' + escapeHtml(row.totalCount == null ? '-' : row.totalCount) + '</td>'
            + '<td>' + escapeHtml(row.successCount == null ? '-' : row.successCount) + '</td>'
            + '<td>' + escapeHtml(row.failureCount == null ? '-' : row.failureCount) + '</td>'
            + '<td>' + escapeHtml(row.deviceCount == null ? '-' : row.deviceCount) + '</td>'
            + '<td>' + escapeHtml(row.userCount == null ? '-' : row.userCount) + '</td>'
            + '<td>' + escapeHtml(formatDecimalText(row.avgDurationMs, 2)) + '</td>'
            + '<td>' + escapeHtml(row.maxDurationMs == null ? '-' : row.maxDurationMs) + '</td>'
            + '</tr>';
        }).join('');
      }
      html += '</tbody></table></div>';
      html += '</section>';
    }

    return html;
  }

  function renderAppSettingsPage(title, subtitle) {
    var appCenter = state.appCenter || {};
    var selectedApp = appCenter.selectedApp;
    var appListLoading = isManagerDataLoading(appCenter.loaded, appCenter.loading, appCenter.notice);
    var html = '';

    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle || langText('管理 iPhone 客户端功能开关，可直接在列表中开启或关闭。', 'Manage iPhone feature toggles and switch them directly in the list.')) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="reload-app-center">' + escapeHtml(langText('刷新数据', 'Refresh')) + '</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(appCenter.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">' + escapeHtml(langText('应用列表', 'Apps')) + '</h3>';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>' + escapeHtml(langText('应用Code', 'App Code')) + '</th><th>' + escapeHtml(langText('应用名称', 'App Name')) + '</th><th>' + escapeHtml(langText('版本更新功能开关', 'Version Prompt Toggle')) + '</th><th>' + escapeHtml(langText('演示自动登录功能开关', 'Demo Auto Login Toggle')) + '</th><th>' + escapeHtml(langText('本机注册校验开关', 'Device Binding Check Toggle')) + '</th><th>' + escapeHtml(langText('状态', 'Status')) + '</th><th>' + escapeHtml(langText('更新时间', 'Updated At')) + '</th></tr></thead><tbody>';
    if (!appCenter.apps || appCenter.apps.length === 0) {
      html += renderManagerTableStateRow(7, appListLoading, langText('应用定义加载中...', 'Loading app definitions...'), langText('暂无应用定义', 'No app definitions'));
    } else {
      html += appCenter.apps.map(function (app) {
        var selected = selectedApp && selectedApp.appCode === app.appCode ? ' style="background:#f5fbff;"' : '';
        return '<tr' + selected + '>' +
          '<td>' + escapeHtml(app.appCode || '-') + '</td>' +
          '<td>' + escapeHtml(app.appName || '-') + '</td>' +
          '<td>' + renderInlineAppSettingControls(app, 'versionPromptEnabled', !!app.versionPromptEnabled) + '</td>' +
          '<td>' + renderInlineAppSettingControls(app, 'demoAutoLoginEnabled', !!app.demoAutoLoginEnabled) + '</td>' +
          '<td>' + renderInlineAppSettingControls(app, 'loginDeviceBindingCheckEnabled', !(app && app.loginDeviceBindingCheckEnabled === false)) + '</td>' +
          '<td>' + renderStatusBadge(app.status || '-', formatAppStatus(app.status)) + '</td>' +
          '<td>' + escapeHtml(formatDateTime(app.updatedAt)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderInlineAppSettingControls(app, fieldName, enabled) {
    var appCode = String(app && app.appCode || '');
    var versionPromptEnabled = !!(app && app.versionPromptEnabled);
    var demoAutoLoginEnabled = !(app && app.demoAutoLoginEnabled === false);
    var loginDeviceBindingCheckEnabled = !(app && app.loginDeviceBindingCheckEnabled === false);
    var html = '';
    html += '<div style="margin-bottom:8px;">' + renderBooleanBadge(enabled, langText('已开启', 'Enabled'), langText('已关闭', 'Disabled')) + '</div>';
    html += '<div class="manager-rbac-actions" style="margin:0;gap:8px;justify-content:flex-start;">';
    html += '<button class="manager-btn' + (enabled ? ' primary' : '') + '" type="button" data-action="update-app-setting-inline" data-app-code="' + escapeHtml(appCode) + '" data-field-name="' + escapeHtml(fieldName) + '" data-target-value="true" data-version-prompt-enabled="' + (versionPromptEnabled ? 'true' : 'false') + '" data-demo-auto-login-enabled="' + (demoAutoLoginEnabled ? 'true' : 'false') + '" data-login-device-binding-check-enabled="' + (loginDeviceBindingCheckEnabled ? 'true' : 'false') + '">' + escapeHtml(langText('开启', 'Enable')) + '</button>';
    html += '<button class="manager-btn' + (!enabled ? ' primary' : '') + '" type="button" data-action="update-app-setting-inline" data-app-code="' + escapeHtml(appCode) + '" data-field-name="' + escapeHtml(fieldName) + '" data-target-value="false" data-version-prompt-enabled="' + (versionPromptEnabled ? 'true' : 'false') + '" data-demo-auto-login-enabled="' + (demoAutoLoginEnabled ? 'true' : 'false') + '" data-login-device-binding-check-enabled="' + (loginDeviceBindingCheckEnabled ? 'true' : 'false') + '">' + escapeHtml(langText('关闭', 'Disable')) + '</button>';
    html += '</div>';
    return html;
  }

  function renderAppDefinitionModal(visible) {
    if (!visible) {
      return '';
    }
    var bodyHtml = '';
    bodyHtml += '<div class="manager-coupon-form-grid compact">';
    bodyHtml += renderField('应用Code', '<input id="appInfoCode" class="form-input" placeholder="如 OPENAIPAY_IOS"/>');
    bodyHtml += renderField('应用名称', '<input id="appInfoName" class="form-input" placeholder="如 OpenAiPay iPhone"/>');
    bodyHtml += '</div>';
    bodyHtml += '<div class="manager-rbac-actions">';
    bodyHtml += '<button class="manager-btn primary" type="button" data-action="create-app">创建 / 更新应用</button>';
    bodyHtml += '</div>';
    return renderManagerModal({
      title: '创建 / 更新应用',
      description: '应用定义在弹窗中维护，列表页只展示应用列表和版本数据。',
      closeAction: 'hide-app-form',
      bodyHtml: bodyHtml,
    });
  }

  function renderAppVersionModal(selectedApp, visible) {
    if (!visible || !selectedApp) {
      return '';
    }
    var appCenter = state.appCenter || {};
    var formMode = appCenter.versionFormMode === 'EDIT' ? 'EDIT' : 'CREATE';
    var isEditMode = formMode === 'EDIT';
    var draft = state.appCenter.versionDraft || createDefaultAppVersionDraft(selectedApp, state.appCenter.versions || []);
    state.appCenter.versionDraft = draft;
    var normalizedUpdateType = normalizeEnumValue(draft.updateType || 'OPTIONAL');
    if (!normalizedUpdateType) {
      normalizedUpdateType = 'OPTIONAL';
    }
    var isForceUpdate = normalizedUpdateType === 'FORCE';
    var promptFrequencyValue = isForceUpdate ? 'ALWAYS' : normalizeEnumValue(draft.updatePromptFrequency || 'ONCE_PER_VERSION');
    var bodyHtml = '';
    bodyHtml += '<div class="manager-coupon-form-grid compact">';
    bodyHtml += renderField('版本Code', '<input id="appVersionCode" class="form-input" value="' + escapeHtml(draft.versionCode || '') + '" placeholder="自动生成，如 OPENAIPAY_IOS_VER_26_314_1" readonly/>');
    bodyHtml += renderField('版本号', '<input id="appVersionNo" class="form-input" value="' + escapeHtml(draft.appVersionNo || '') + '" placeholder="自动生成，如 26.314.1"' + (isEditMode ? ' readonly' : '') + '/>');
    bodyHtml += renderField(
      '更新类型',
      '<select id="appVersionUpdateType" class="form-input"><option value="OPTIONAL"' + (normalizedUpdateType === 'OPTIONAL' ? ' selected' : '') + '>' +
        escapeHtml(formatAppUpdateType('OPTIONAL')) +
        '</option><option value="RECOMMENDED"' + (normalizedUpdateType === 'RECOMMENDED' ? ' selected' : '') + '>' +
        escapeHtml(formatAppUpdateType('RECOMMENDED')) +
        '</option><option value="FORCE"' + (normalizedUpdateType === 'FORCE' ? ' selected' : '') + '>' +
        escapeHtml(formatAppUpdateType('FORCE')) +
        '</option></select>'
    );
    if (!isForceUpdate) {
      bodyHtml += renderField('提示频率', '<select id="appVersionPromptFrequency" class="form-input">' + renderAppUpdatePromptFrequencyOptions(promptFrequencyValue) + '</select>');
      bodyHtml += renderField('最低支持版本', '<select id="appVersionMinSupported" class="form-input">' + renderMinSupportedVersionOptions(draft.minSupportedVersionNo || '', state.appCenter.versions || []) + '</select>');
    }
    bodyHtml += renderField('商店地址', '<input id="appIosStoreUrl" class="form-input" value="' + escapeHtml(draft.appStoreUrl || '') + '" placeholder="https://apps.apple.com/..."/>');
    bodyHtml += '</div>';
    if (isForceUpdate) {
      bodyHtml += '<div class="manager-rbac-hint" style="margin-top:8px;">强制更新无需设置提示频率，客户端每次打开都会立即提示更新。</div>';
    }
    bodyHtml += '<div class="manager-rbac-hint" style="margin-top:8px;">发行区域与定向区域已固定为 CN。</div>';
    bodyHtml += '<div class="manager-coupon-form-grid">';
    bodyHtml += renderField('用户提示信息', '<textarea id="appVersionDescription" class="form-input" rows="4" placeholder="填写客户端版本更新提示">' + escapeHtml(draft.versionDescription || DEFAULT_VERSION_PROMPT_MESSAGE) + '</textarea>');
    bodyHtml += renderField('版本说明', '<textarea id="appVersionPublisherRemark" class="form-input" rows="3" placeholder="填写发布者备注（仅后台可见）">' + escapeHtml(draft.publisherRemark || '') + '</textarea>');
    bodyHtml += '</div>';
    if (appCenter.versionFormNotice) {
      bodyHtml += '<div class="manager-coupon-notice" style="margin-top:8px;">' + escapeHtml(appCenter.versionFormNotice) + '</div>';
    }
    bodyHtml += '<div class="manager-rbac-actions">';
    bodyHtml += '<button class="manager-btn primary" type="button" data-action="create-app-version"' + (appCenter.versionSubmitting ? ' disabled' : '') + '>'
      + (appCenter.versionSubmitting ? (isEditMode ? '保存中...' : '创建中...') : (isEditMode ? '保存修改' : '创建版本'))
      + '</button>';
    bodyHtml += '</div>';
    return renderManagerModal({
      title: (isEditMode ? '修改版本 - ' : '新建版本 - ') + (selectedApp.appName || selectedApp.appCode || ''),
      description: isEditMode ? '在弹窗中修改版本策略与商店地址。' : '版本创建入口迁移到弹窗，主页面只展示版本列表和设备数据。',
      closeAction: 'hide-app-version-form',
      bodyHtml: bodyHtml,
    });
  }

  function renderAppSettingsModal(selectedApp, visible) {
    if (!visible || !selectedApp) {
      return '';
    }
    var bodyHtml = '';
    bodyHtml += '<div class="manager-coupon-form-grid compact">';
    bodyHtml += renderField(langText('应用Code', 'App Code'), '<input class="form-input" value="' + escapeHtml(selectedApp.appCode || '') + '" disabled/>');
    bodyHtml += renderField(langText('应用名称', 'App Name'), '<input class="form-input" value="' + escapeHtml(selectedApp.appName || '') + '" disabled/>');
    bodyHtml += '</div>';
    bodyHtml += '<div style="margin-top:12px;border:1px solid #e5eaf0;border-radius:10px;overflow:hidden;">';
    bodyHtml += renderAppSettingToggleRow(
      'appVersionPromptEnabled',
      langText('版本更新提示', 'Version Prompt'),
      langText('关闭后客户端不展示版本更新提示', 'When disabled, clients will not show version update prompts'),
      !!selectedApp.versionPromptEnabled,
      false
    );
    bodyHtml += renderAppSettingToggleRow(
      'appDemoAutoLoginEnabled',
      langText('演示账号自动登录', 'Demo Auto Login'),
      langText('关闭后登录页不展示演示账号自动登录按钮', 'When disabled, the login page hides demo auto-login'),
      !!selectedApp.demoAutoLoginEnabled,
      true
    );
    bodyHtml += renderAppSettingToggleRow(
      'appLoginDeviceBindingCheckEnabled',
      langText('本机注册校验', 'Device Binding Check'),
      langText('关闭后可在非注册设备登录账号', 'When disabled, accounts can sign in on non-registered devices'),
      !(selectedApp.loginDeviceBindingCheckEnabled === false),
      true
    );
    bodyHtml += '</div>';
    bodyHtml += '<div class="manager-coupon-form-grid compact" style="margin-top:12px;">';
    bodyHtml += renderField(langText('演示模板登录号', 'Demo Template Login ID'), '<input id="appDemoTemplateLoginId" class="form-input" value="' + escapeHtml(selectedApp.demoTemplateLoginId || '') + '" placeholder="' + escapeHtml(langText('如 177xxxxxxx', 'e.g. 177xxxxxxx')) + '"/>');
    bodyHtml += renderField(langText('默认联系人登录号', 'Default Contact Login ID'), '<input id="appDemoContactLoginId" class="form-input" value="' + escapeHtml(selectedApp.demoContactLoginId || '') + '" placeholder="' + escapeHtml(langText('如 138xxxxxxx', 'e.g. 138xxxxxxx')) + '"/>');
    bodyHtml += renderField(langText('演示注册默认密码', 'Demo Default Password'), '<input id="appDemoLoginPassword" class="form-input" type="password" value="" placeholder="' + escapeHtml((selectedApp.demoLoginPasswordMasked || '') ? langText('已配置: ', 'Configured: ') + selectedApp.demoLoginPasswordMasked : langText('未配置', 'Not configured')) + '"/>');
    bodyHtml += '</div>';
    bodyHtml += '<p class="manager-subtext" style="margin-top:12px;">' + escapeHtml(langText('版本提示关闭后，普通更新与强制更新提示都会被抑制。演示自动登录关闭后，登录页不展示演示账号自动登录按钮。本机注册校验关闭后，可在非注册设备登录。默认联系人登录号用于新注册用户自动加好友及欢迎消息发送。', 'When version prompts are disabled, both normal and forced update prompts are suppressed. When demo auto-login is disabled, the login page hides the demo auto-login button. When device binding verification is disabled, login is allowed from non-registered devices. The default contact login ID is used for automatic friend creation and welcome messages for newly registered users.')) + '</p>';
    bodyHtml += '<div class="manager-rbac-actions">';
    bodyHtml += '<button class="manager-btn primary" type="button" data-action="save-app-settings">' + escapeHtml(langText('保存开关', 'Save Settings')) + '</button>';
    bodyHtml += '</div>';
    return renderManagerModal({
      title: langText('版本提示开关 - ', 'App Settings - ') + (selectedApp.appName || selectedApp.appCode || ''),
      description: langText('配置入口迁移到弹窗，列表页只保留应用列表。', 'Configuration is edited in modal. List page only keeps app list.'),
      closeAction: 'hide-app-settings',
      bodyHtml: bodyHtml,
    });
  }

  function renderAppSettingToggleRow(fieldId, title, desc, enabled, withTopBorder) {
    var html = '';
    html += '<div data-app-setting-group="' + escapeHtml(fieldId) + '" style="display:flex;align-items:center;justify-content:space-between;gap:16px;padding:14px;' + (withTopBorder ? 'border-top:1px solid #eef2f7;' : '') + '">';
    html += '<div style="flex:1;min-width:0;">';
    html += '<div style="font-size:14px;font-weight:600;color:#1f2937;">' + escapeHtml(title) + '</div>';
    if (desc) {
      html += '<div style="margin-top:4px;font-size:12px;color:#6b7280;">' + escapeHtml(desc) + '</div>';
    }
    html += '<input id="' + escapeHtml(fieldId) + '" type="hidden" value="' + (enabled ? 'true' : 'false') + '"/>';
    html += '</div>';
    html += '<div class="manager-rbac-actions" style="gap:8px;margin:0;flex-shrink:0;">';
    html += '<button class="manager-btn' + (enabled ? ' primary' : '') + '" type="button" data-action="set-app-setting-toggle" data-field-id="' + escapeHtml(fieldId) + '" data-value="true">' + escapeHtml(langText('开启', 'Enable')) + '</button>';
    html += '<button class="manager-btn' + (!enabled ? ' primary' : '') + '" type="button" data-action="set-app-setting-toggle" data-field-id="' + escapeHtml(fieldId) + '" data-value="false">' + escapeHtml(langText('关闭', 'Disable')) + '</button>';
    html += '</div>';
    html += '</div>';
    return html;
  }

  function renderTradePage(title, subtitle) {
    var trade = state.trade || {};
    var query = trade.query || {};
    var html = '';

    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle || langText('按统一交易号、请求号或业务域单号查询交易编排主单。', 'Query orchestrated trade orders by trade no., request no., or business-domain order no.')) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="reload-trade">清空查询结果</button>';
    html += '</div>';
    html += '</div>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">统一交易查询</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('交易号', '<input id="tradeQueryTradeNo" class="form-input" value="' + escapeHtml(query.tradeOrderNo || '') + '" placeholder="输入 tradeOrderNo 后回车或点击查询"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="query-trade-by-no">按交易号查询</button>';
    html += '</div>';
    if (trade.notice) {
      html += '<p class="manager-coupon-notice" style="margin-top:10px;">' + escapeHtml(trade.notice) + '</p>';
    }
    html += '</section>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">请求幂等查询</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('请求号', '<input id="tradeQueryRequestNo" class="form-input" value="' + escapeHtml(query.requestNo || '') + '" placeholder="输入 requestNo 后回车或点击查询"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="query-trade-by-request">按请求号查询</button>';
    html += '</div>';
    html += '</section>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">业务域交易查询</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('业务域', '<select id="tradeQueryBusinessDomainCode" class="form-input">' + renderTradeBusinessDomainOptions(query.businessDomainCode) + '</select>');
    html += renderField('业务单号', '<input id="tradeQueryBizOrderNo" class="form-input" value="' + escapeHtml(query.bizOrderNo || '') + '" placeholder="如爱花还款单号、爱存申购单号、入金单号、出金单号"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="query-trade-by-business">按业务域 + 业务单号查询</button>';
    html += '</div>';
    html += '</section>';
    html += renderTradeDetailModal(trade.selectedOrder, trade.detailVisible);
    return html;
  }

  function renderTradeBusinessDomainOptions(selected) {
    var normalizedSelected = normalizeEnumValue(selected);
    if (normalizedSelected === 'AICASH') {
      normalizedSelected = 'AICASH';
    }
    if (normalizedSelected === 'AICREDIT') {
      normalizedSelected = 'AICREDIT';
    }
    if (normalizedSelected === 'AILOAN') {
      normalizedSelected = 'AILOAN';
    }
    var options = ['TRADE', 'WALLET', 'AICREDIT', 'AILOAN', 'AICASH', 'RED_PACKET', 'INBOUND', 'OUTBOUND'];
    return options
      .map(function (code) {
        var isSelected = normalizedSelected === code ? ' selected' : '';
        return '<option value="' + code + '"' + isSelected + '>' + formatTradeBusinessDomain(code) + '</option>';
      })
      .join('');
  }

  function renderTradeDetailModal(selectedOrder, visible) {
    if (!visible || !selectedOrder) {
      return '';
    }
    var flowSteps = Array.isArray(selectedOrder.flowSteps) ? selectedOrder.flowSteps : [];
    var payAttempts = Array.isArray(selectedOrder.payAttempts) ? selectedOrder.payAttempts : [];
    var splitPlan = selectedOrder.splitPlan || {};
    var bodyHtml = '';
    bodyHtml += '<div class="manager-coupon-detail-grid">';
    bodyHtml += renderDetailItem('交易号', selectedOrder.tradeOrderNo || '-');
    bodyHtml += renderDetailItem('请求号', selectedOrder.requestNo || '-');
    bodyHtml += renderDetailItem('交易类型', formatTradeType(selectedOrder.tradeType));
    bodyHtml += renderDetailItem('交易状态', formatTradeStatus(selectedOrder.status));
    bodyHtml += renderDetailItem('业务场景', formatBusinessSceneCode(selectedOrder.businessSceneCode));
    bodyHtml += renderDetailItem('业务域', formatTradeBusinessDomain(selectedOrder.businessDomainCode));
    bodyHtml += renderDetailItem('业务单号', selectedOrder.bizOrderNo || '-');
    bodyHtml += renderDetailItem('原交易号', selectedOrder.originalTradeOrderNo || '-');
    bodyHtml += renderDetailItem('付款方用户ID', selectedOrder.payerUserId || '-');
    bodyHtml += renderDetailItem('收款方用户ID', selectedOrder.payeeUserId || '-');
    bodyHtml += renderDetailItem('支付方式', formatPaymentMethod(selectedOrder.paymentMethod));
    bodyHtml += renderDetailItem('计费报价单号', selectedOrder.pricingQuoteNo || '-');
    bodyHtml += renderDetailItem('当前支付单号', selectedOrder.payOrderNo || '-');
    bodyHtml += renderDetailItem('当前支付尝试', selectedOrder.currentPayAttemptNo == null ? '-' : ('第' + String(selectedOrder.currentPayAttemptNo) + '次'));
    bodyHtml += renderDetailItem('当前支付状态版本', selectedOrder.currentPayStatusVersion == null ? '-' : String(selectedOrder.currentPayStatusVersion));
    bodyHtml += renderDetailItem('当前支付结果码', selectedOrder.currentPayResultCode || '-');
    bodyHtml += renderDetailItem('当前支付结果描述', selectedOrder.currentPayResultMessage || '-');
    bodyHtml += renderDetailItem('支付尝试次数', String(selectedOrder.payAttemptCount == null ? payAttempts.length : selectedOrder.payAttemptCount));
    bodyHtml += renderDetailItem('失败原因', selectedOrder.failureReason || '-');
    bodyHtml += renderDetailItem('创建时间', formatDateTime(selectedOrder.createdAt));
    bodyHtml += renderDetailItem('更新时间', formatDateTime(selectedOrder.updatedAt));
    bodyHtml += renderDetailItem('原始金额', formatMoney(selectedOrder.originalAmount));
    bodyHtml += renderDetailItem('手续费', formatMoney(selectedOrder.feeAmount));
    bodyHtml += renderDetailItem('应付金额', formatMoney(selectedOrder.payableAmount));
    bodyHtml += renderDetailItem('结算金额', formatMoney(selectedOrder.settleAmount));
    bodyHtml += renderDetailItem('余额扣款', formatMoney(splitPlan.walletDebitAmount));
    bodyHtml += renderDetailItem('爱存扣款', formatMoney(splitPlan.fundDebitAmount));
    bodyHtml += renderDetailItem('信用扣款', formatMoney(splitPlan.creditDebitAmount));
    bodyHtml += renderDetailItem('入金扣款', formatMoney(splitPlan.inboundDebitAmount));
    bodyHtml += '</div>';
    bodyHtml += '<div class="manager-coupon-form-grid" style="margin-top:14px;">';
    bodyHtml += renderField('扩展信息', '<textarea class="form-input" rows="4" readonly>' + escapeHtml(selectedOrder.metadata || '-') + '</textarea>');
    bodyHtml += '</div>';
    bodyHtml += renderTradePayAttemptsSection(payAttempts, selectedOrder.payOrderNo);
    bodyHtml += '<div class="manager-coupon-table-wrap" style="margin-top:14px;"><table class="manager-coupon-table">';
    bodyHtml += '<thead><tr><th>步骤编码</th><th>步骤状态</th><th>开始时间</th><th>结束时间</th><th>错误信息</th></tr></thead><tbody>';
    if (!flowSteps.length) {
      bodyHtml += '<tr><td colspan="5" class="manager-coupon-empty-row">暂无流程步骤</td></tr>';
    } else {
      bodyHtml += flowSteps.map(function (step) {
        return '<tr>' +
          '<td>' + escapeHtml(step.stepCode || '-') + '</td>' +
          '<td>' + renderStatusBadge(step.stepStatus || '-', formatTradeStepStatus(step.stepStatus)) + '</td>' +
          '<td>' + escapeHtml(formatDateTime(step.startedAt)) + '</td>' +
          '<td>' + escapeHtml(formatDateTime(step.finishedAt)) + '</td>' +
          '<td>' + escapeHtml(step.errorMessage || '-') + '</td>' +
          '</tr>';
      }).join('');
    }
    bodyHtml += '</tbody></table></div>';
    return renderManagerModal({
      title: '交易详情',
      description: '查询结果通过弹窗展示，主页面只保留查询入口。',
      closeAction: 'hide-trade-detail',
      bodyHtml: bodyHtml,
    });
  }

  function renderTradePayAttemptsSection(payAttempts, currentPayOrderNo) {
    var attempts = Array.isArray(payAttempts) ? payAttempts : [];
    var html = '';
    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">支付尝试列表</h3>';
    if (!attempts.length) {
      html += '<div class="manager-coupon-empty-row">暂无支付尝试数据</div>';
      html += '</section>';
      return html;
    }
    html += attempts.map(function (attempt) {
      return renderTradePayAttemptCard(attempt, currentPayOrderNo);
    }).join('');
    html += '</section>';
    return html;
  }

  function renderTradePayAttemptCard(attempt, currentPayOrderNo) {
    var item = attempt || {};
    var participants = Array.isArray(item.participants) ? item.participants : [];
    var isCurrent = !!currentPayOrderNo && String(item.payOrderNo || '') === String(currentPayOrderNo || '');
    var html = '';
    html += '<section class="manager-panel" style="margin-top:12px;">';
    html += '<div class="manager-title-row" style="margin-bottom:10px;align-items:center;">';
    html += '<div>';
    html += '<h3 class="manager-panel-title" style="margin:0;">第' + escapeHtml(String(item.attemptNo == null ? '-' : item.attemptNo)) + '次支付尝试</h3>';
    html += '<p class="manager-subtext" style="margin-top:4px;">' + escapeHtml(isCurrent ? '当前生效支付单' : '历史支付尝试') + '</p>';
    html += '</div>';
    html += '<div>' + renderStatusBadge(item.status || '-', formatPayStatus(item.status)) + '</div>';
    html += '</div>';
    html += '<div class="manager-coupon-detail-grid">';
    html += renderDetailItem('支付单号', item.payOrderNo || '-');
    html += renderDetailItem('业务支付单号', item.bizOrderNo || '-');
    html += renderDetailItem('状态版本', item.statusVersion == null ? '-' : String(item.statusVersion));
    html += renderDetailItem('结果码', item.resultCode || '-');
    html += renderDetailItem('结果描述', item.resultMessage || '-');
    html += renderDetailItem('失败原因', item.failureReason || '-');
    html += renderDetailItem('原始金额', formatMoney(item.originalAmount));
    html += renderDetailItem('优惠金额', formatMoney(item.discountAmount));
    html += renderDetailItem('应付金额', formatMoney(item.payableAmount));
    html += renderDetailItem('实付金额', formatMoney(item.actualPaidAmount));
    html += renderDetailItem('创建时间', formatDateTime(item.createdAt));
    html += renderDetailItem('更新时间', formatDateTime(item.updatedAt));
    html += '</div>';
    html += '<div class="manager-coupon-table-wrap" style="margin-top:14px;"><table class="manager-coupon-table">';
    html += '<thead><tr><th>参与方</th><th>分支号</th><th>资源标识</th><th>状态</th><th>请求快照</th><th>响应结果</th><th>更新时间</th></tr></thead><tbody>';
    if (!participants.length) {
      html += '<tr><td colspan="7" class="manager-coupon-empty-row">暂无参与方分支明细</td></tr>';
    } else {
      html += participants.map(function (participant) {
        return '<tr>'
          + '<td>' + escapeHtml(formatPayParticipantType(participant.participantType)) + '</td>'
          + '<td>' + escapeHtml(participant.branchId || '-') + '</td>'
          + '<td>' + escapeHtml(participant.participantResourceId || '-') + '</td>'
          + '<td>' + renderStatusBadge(participant.status || '-', formatPayParticipantStatus(participant.status)) + '</td>'
          + '<td style="white-space:pre-wrap;word-break:break-all;max-width:220px;">' + escapeHtml(participant.requestPayload || '-') + '</td>'
          + '<td style="white-space:pre-wrap;word-break:break-all;max-width:220px;">' + escapeHtml(participant.responseMessage || '-') + '</td>'
          + '<td>' + escapeHtml(formatDateTime(participant.updatedAt)) + '</td>'
          + '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderAccountingPage(title, subtitle) {
    var accounting = state.accounting || {};
    var html = '';
    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle || langText('查看会计事件、凭证、分录与会计科目，并支持失败事件重试与凭证冲正。', 'View accounting events, vouchers, entries, and subjects, with support for failed-event retry and voucher reversal.')) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="reload-accounting-page">刷新当前页</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(accounting.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    if (isPathActive(ACCOUNTING_EVENT_PATH) || isPathActive(ACCOUNTING_ROOT_PATH)) {
      html += renderAccountingEventSection(accounting);
      html += renderAccountingEventDetailModal(accounting.selectedEvent, accounting.eventDetailVisible);
      return html;
    }
    if (isPathActive(ACCOUNTING_VOUCHER_PATH)) {
      html += renderAccountingVoucherSection(accounting);
      html += renderAccountingVoucherDetailModal(accounting.selectedVoucher, accounting.voucherDetailVisible);
      return html;
    }
    if (isPathActive(ACCOUNTING_ENTRY_PATH)) {
      html += renderAccountingEntrySection(accounting);
      return html;
    }
    if (isPathActive(ACCOUNTING_SUBJECT_PATH)) {
      html += renderAccountingSubjectSection(accounting);
      html += renderAccountingSubjectEditorModal(accounting);
      return html;
    }
    html += renderPlaceholderPage(title, subtitle);
    return html;
  }

  function renderAccountingEventSection(accounting) {
    var filter = accounting.eventFilter || {};
    var selectedEvent = accounting.selectedEvent;
    var eventListLoading = !!accounting.eventLoading || (!!accounting.loading && !accounting.loadedViews.events) || (!accounting.loadedViews.events && !accounting.notice);
    var html = '';
    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">会计事件查询</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('事件号', '<input id="accountingEventId" class="form-input" value="' + escapeHtml(filter.eventId || '') + '" placeholder="输入 eventId"/>');
    html += renderField('来源业务号', '<input id="accountingEventSourceBizNo" class="form-input" value="' + escapeHtml(filter.sourceBizNo || '') + '" placeholder="输入 sourceBizNo"/>');
    html += renderField('交易号', '<input id="accountingEventTradeNo" class="form-input" value="' + escapeHtml(filter.tradeOrderNo || '') + '" placeholder="输入 tradeOrderNo"/>');
    html += renderField('支付号', '<input id="accountingEventPaymentId" class="form-input" value="' + escapeHtml(filter.payOrderNo || '') + '" placeholder="输入 payOrderNo"/>');
    html += renderField('状态', '<select id="accountingEventStatus" class="form-input">' + renderAccountingEventStatusOptions(filter.status) + '</select>');
    html += renderField('条数', '<input id="accountingEventLimit" class="form-input" value="' + escapeHtml(filter.limit || '20') + '" placeholder="20"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="query-accounting-events">查询事件</button>';
    html += '</div>';
    html += '</section>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">会计事件列表</h3>';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>事件号</th><th>事件类型</th><th>来源业务</th><th>交易号</th><th>支付号</th><th>状态</th><th>凭证号</th><th>更新时间</th><th>操作</th></tr></thead><tbody>';
    if (!accounting.events || accounting.events.length === 0) {
      html += renderManagerTableStateRow(9, eventListLoading, '会计事件加载中...', '暂无会计事件');
    } else {
      html += accounting.events.map(function (item) {
        var selectedRow = selectedEvent && selectedEvent.eventId === item.eventId ? ' class="manager-table-row-selected"' : '';
        return '<tr' + selectedRow + '>' +
          '<td>' + escapeHtml(item.eventId || '-') + '</td>' +
          '<td>' + escapeHtml(formatAccountingBizType(item.eventType)) + '</td>' +
          '<td>' + escapeHtml(formatAccountingBizType(item.sourceBizType) + ' / ' + (item.sourceBizNo || '-')) + '</td>' +
          '<td>' + escapeHtml(item.tradeOrderNo || '-') + '</td>' +
          '<td>' + escapeHtml(item.payOrderNo || '-') + '</td>' +
          '<td>' + renderStatusBadge(item.status || '-', formatAccountingEventStatus(item.status)) + '</td>' +
          '<td>' + escapeHtml(item.postedVoucherNo || '-') + '</td>' +
          '<td>' + escapeHtml(formatDateTime(item.updatedAt)) + '</td>' +
          '<td>' +
          '<button class="manager-btn" type="button" data-action="load-accounting-event" data-event-id="' + escapeHtml(item.eventId || '') + '">详情</button> ' +
          '<button class="manager-btn" type="button" data-action="retry-accounting-event" data-event-id="' + escapeHtml(item.eventId || '') + '">重试</button>' +
          '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderAccountingVoucherSection(accounting) {
    var filter = accounting.voucherFilter || {};
    var selectedVoucher = accounting.selectedVoucher;
    var voucherListLoading = !!accounting.voucherLoading || (!!accounting.loading && !accounting.loadedViews.vouchers) || (!accounting.loadedViews.vouchers && !accounting.notice);
    var html = '';
    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">会计凭证查询</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('凭证号', '<input id="accountingVoucherNo" class="form-input" value="' + escapeHtml(filter.voucherNo || '') + '" placeholder="输入 voucherNo"/>');
    html += renderField('来源业务号', '<input id="accountingVoucherSourceBizNo" class="form-input" value="' + escapeHtml(filter.sourceBizNo || '') + '" placeholder="输入 sourceBizNo"/>');
    html += renderField('交易号', '<input id="accountingVoucherTradeNo" class="form-input" value="' + escapeHtml(filter.tradeOrderNo || '') + '" placeholder="输入 tradeOrderNo"/>');
    html += renderField('支付号', '<input id="accountingVoucherPaymentId" class="form-input" value="' + escapeHtml(filter.payOrderNo || '') + '" placeholder="输入 payOrderNo"/>');
    html += renderField('状态', '<select id="accountingVoucherStatus" class="form-input">' + renderAccountingVoucherStatusOptions(filter.status) + '</select>');
    html += renderField('条数', '<input id="accountingVoucherLimit" class="form-input" value="' + escapeHtml(filter.limit || '20') + '" placeholder="20"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions"><button class="manager-btn primary" type="button" data-action="query-accounting-vouchers">查询凭证</button></div>';
    html += '</section>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">凭证列表</h3>';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>凭证号</th><th>凭证类型</th><th>来源业务</th><th>借方</th><th>贷方</th><th>状态</th><th>会计日</th><th>操作</th></tr></thead><tbody>';
    if (!accounting.vouchers || accounting.vouchers.length === 0) {
      html += renderManagerTableStateRow(8, voucherListLoading, '会计凭证加载中...', '暂无会计凭证');
    } else {
      html += accounting.vouchers.map(function (item) {
        var selectedRow = selectedVoucher && selectedVoucher.voucherNo === item.voucherNo ? ' class="manager-table-row-selected"' : '';
        var allowReverse = item.voucherType !== 'REVERSE' && item.status !== 'REVERSED';
        return '<tr' + selectedRow + '>' +
          '<td>' + escapeHtml(item.voucherNo || '-') + '</td>' +
          '<td>' + escapeHtml(formatAccountingVoucherType(item.voucherType)) + '</td>' +
          '<td>' + escapeHtml(formatAccountingBizType(item.sourceBizType) + ' / ' + (item.sourceBizNo || '-')) + '</td>' +
          '<td>' + escapeHtml(formatMoney(item.totalDebitAmount)) + '</td>' +
          '<td>' + escapeHtml(formatMoney(item.totalCreditAmount)) + '</td>' +
          '<td>' + renderStatusBadge(item.status || '-', formatAccountingVoucherStatus(item.status)) + '</td>' +
          '<td>' + escapeHtml(item.postingDate || '-') + '</td>' +
          '<td>' +
          '<button class="manager-btn" type="button" data-action="load-accounting-voucher" data-voucher-no="' + escapeHtml(item.voucherNo || '') + '">详情</button> ' +
          (allowReverse
            ? '<button class="manager-btn" type="button" data-action="reverse-accounting-voucher" data-voucher-no="' + escapeHtml(item.voucherNo || '') + '">冲正</button>'
            : '<span class="manager-coupon-notice">已冲正 / 冲正凭证</span>') +
          '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderAccountingEntrySection(accounting) {
    var filter = accounting.entryFilter || {};
    var entryListLoading = !!accounting.entryLoading || (!!accounting.loading && !accounting.loadedViews.entries) || (!accounting.loadedViews.entries && !accounting.notice);
    var html = '';
    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">会计分录查询</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('凭证号', '<input id="accountingEntryVoucherNo" class="form-input" value="' + escapeHtml(filter.voucherNo || '') + '" placeholder="输入 voucherNo"/>');
    html += renderField('科目编码', '<input id="accountingEntrySubjectCode" class="form-input" value="' + escapeHtml(filter.subjectCode || '') + '" placeholder="输入 subjectCode"/>');
    html += renderField('主体ID', '<input id="accountingEntryOwnerId" class="form-input" value="' + escapeHtml(filter.ownerId || '') + '" placeholder="输入 ownerId"/>');
    html += renderField('支付号', '<input id="accountingEntryPaymentId" class="form-input" value="' + escapeHtml(filter.payOrderNo || '') + '" placeholder="输入 payOrderNo"/>');
    html += renderField('条数', '<input id="accountingEntryLimit" class="form-input" value="' + escapeHtml(filter.limit || '50') + '" placeholder="50"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions"><button class="manager-btn primary" type="button" data-action="query-accounting-entries">查询分录</button></div>';
    html += '</section>';
    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">分录列表</h3>';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>凭证号</th><th>行号</th><th>科目</th><th>借贷</th><th>金额</th><th>账户域</th><th>账户类型</th><th>账户号</th><th>主体</th><th>来源业务</th><th>摘要</th></tr></thead><tbody>';
    if (!accounting.entries || accounting.entries.length === 0) {
      html += renderManagerTableStateRow(11, entryListLoading, '会计分录加载中...', '暂无会计分录');
    } else {
      html += accounting.entries.map(function (item) {
        return '<tr>' +
          '<td>' + escapeHtml(item.voucherNo || '-') + '</td>' +
          '<td>' + escapeHtml(item.lineNo || '-') + '</td>' +
          '<td>' + escapeHtml(buildSubjectDisplayText(item.subjectCode, item.subjectName)) + '</td>' +
          '<td>' + escapeHtml(formatAccountingDebitCreditFlag(item.dcFlag)) + '</td>' +
          '<td>' + escapeHtml(formatMoney(item.amount)) + '</td>' +
          '<td>' + escapeHtml(item.accountDomain || '-') + '</td>' +
          '<td>' + escapeHtml(formatAccountingAccountType(item.accountType)) + '</td>' +
          '<td>' + escapeHtml(item.accountNo || '-') + '</td>' +
          '<td>' + escapeHtml(formatAccountingOwnerType(item.ownerType) + ' / ' + (item.ownerId || '-')) + '</td>' +
          '<td>' + escapeHtml(formatAccountingBizType(item.sourceBizType) + ' / ' + (item.sourceBizNo || '-')) + '</td>' +
          '<td>' + escapeHtml(item.entryMemo || '-') + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div></section>';
    return html;
  }

  function renderAccountingSubjectSection(accounting) {
    var subjects = accounting.subjects || [];
    var stats = buildAccountingSubjectStats(subjects);
    var subjectTree = buildAccountingSubjectTree(subjects);
    var subjectListLoading = !!accounting.subjectLoading || (!!accounting.loading && !accounting.loadedViews.subjects) || (!accounting.loadedViews.subjects && !accounting.notice);
    var html = '';
    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<div class="manager-accounting-subject-header">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">标准科目运维</h3>';
    html += '<div class="manager-accounting-subject-hint">初始化只补齐缺失标准科目；重置会恢复标准定义，并自动停用非标准科目。</div>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="open-accounting-subject-create">新建科目</button> ';
    html += '<button class="manager-btn" type="button" data-action="initialize-accounting-subjects">初始化标准科目</button> ';
    html += '<button class="manager-btn" type="button" data-action="reset-accounting-subjects">重置为标准科目</button>';
    html += '</div>';
    html += '</div>';
    html += '<div class="manager-accounting-subject-metrics">';
    html += renderAccountingSubjectMetric('总科目', stats.totalCount, '当前可查看的会计科目总数');
    html += renderAccountingSubjectMetric('启用中', stats.enabledCount, '当前可参与过账的科目');
    html += renderAccountingSubjectMetric('已停用', stats.disabledCount, '保留但不再使用的科目');
    html += renderAccountingSubjectMetric('根科目', stats.rootCount, '一级会计科目数量');
    html += '</div>';
    html += '</section>';

    html += '<div class="manager-accounting-subject-layout" style="margin-top:14px;">';
    html += '<section class="manager-panel">';
    html += '<h3 class="manager-panel-title">科目树</h3>';
    html += '<div class="manager-accounting-subject-hint">点击节点通过弹窗查看与编辑，树会按父子层级展示。</div>';
    html += renderAccountingSubjectTree(subjectTree, accounting.selectedSubjectCode);
    html += '</section>';
    html += '</div>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">科目列表</h3>';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>科目编码</th><th>科目名称</th><th>类型</th><th>余额方向</th><th>父级</th><th>层级</th><th>启用</th><th>备注</th><th>更新时间</th><th>操作</th></tr></thead><tbody>';
    if (!subjects || subjects.length === 0) {
      html += renderManagerTableStateRow(10, subjectListLoading, '会计科目加载中...', '暂无会计科目');
    } else {
      html += subjects.map(function (item) {
        var selectedRow = accounting.selectedSubjectCode && accounting.selectedSubjectCode === item.subjectCode ? ' class="manager-table-row-selected"' : '';
        return '<tr' + selectedRow + '>' +
          '<td>' + escapeHtml(item.subjectCode || '-') + '</td>' +
          '<td>' + escapeHtml(item.subjectName || '-') + '</td>' +
          '<td>' + escapeHtml(formatAccountingSubjectType(item.subjectType)) + '</td>' +
          '<td>' + escapeHtml(formatAccountingBalanceDirection(item.balanceDirection)) + '</td>' +
          '<td>' + escapeHtml(item.parentSubjectCode || '-') + '</td>' +
          '<td>' + escapeHtml(item.levelNo || '-') + '</td>' +
          '<td>' + renderBooleanBadge(item.enabled, '启用', '停用') + '</td>' +
          '<td>' + escapeHtml(item.remark || '-') + '</td>' +
          '<td>' + escapeHtml(formatDateTime(item.updatedAt)) + '</td>' +
          '<td>' +
          '<button class="manager-btn" type="button" data-action="select-accounting-subject" data-subject-code="' + escapeHtml(item.subjectCode || '') + '">编辑</button> ' +
          '<button class="manager-btn" type="button" data-action="change-accounting-subject-status" data-subject-code="' + escapeHtml(item.subjectCode || '') + '" data-target-enabled="' + escapeHtml(item.enabled ? 'false' : 'true') + '">' + escapeHtml(item.enabled ? '停用' : '启用') + '</button>' +
          '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div></section>';
    return html;
  }

  function renderAccountingEventDetailModal(selectedEvent, visible) {
    if (!visible || !selectedEvent) {
      return '';
    }
    var bodyHtml = '';
    bodyHtml += '<div class="manager-coupon-detail-grid">';
    bodyHtml += renderDetailItem('事件号', selectedEvent.eventId || '-');
    bodyHtml += renderDetailItem('事件类型', formatAccountingBizType(selectedEvent.eventType));
    bodyHtml += renderDetailItem('来源系统', selectedEvent.sourceSystem || '-');
    bodyHtml += renderDetailItem('来源业务', formatAccountingBizType(selectedEvent.sourceBizType) + ' / ' + (selectedEvent.sourceBizNo || '-'));
    bodyHtml += renderDetailItem('请求号', selectedEvent.requestNo || '-');
    bodyHtml += renderDetailItem('交易号', selectedEvent.tradeOrderNo || '-');
    bodyHtml += renderDetailItem('支付号', selectedEvent.payOrderNo || '-');
    bodyHtml += renderDetailItem('付款方', selectedEvent.payerUserId || '-');
    bodyHtml += renderDetailItem('收款方', selectedEvent.payeeUserId || '-');
    bodyHtml += renderDetailItem('业务场景', formatBusinessSceneCode(selectedEvent.businessSceneCode));
    bodyHtml += renderDetailItem('业务域', formatTradeBusinessDomain(selectedEvent.businessDomainCode));
    bodyHtml += renderDetailItem('币种', selectedEvent.currencyCode || '-');
    bodyHtml += renderDetailItem('会计日', selectedEvent.postingDate || '-');
    bodyHtml += renderDetailItem('发生时间', formatDateTime(selectedEvent.occurredAt));
    bodyHtml += renderDetailItem('状态', formatAccountingEventStatus(selectedEvent.status));
    bodyHtml += renderDetailItem('凭证号', selectedEvent.postedVoucherNo || '-');
    bodyHtml += renderDetailItem('幂等键', selectedEvent.idempotencyKey || '-');
    bodyHtml += renderDetailItem('全局事务号', selectedEvent.globalTxId || '-');
    bodyHtml += renderDetailItem('链路号', selectedEvent.traceId || '-');
    bodyHtml += renderDetailItem('创建时间', formatDateTime(selectedEvent.createdAt));
    bodyHtml += renderDetailItem('更新时间', formatDateTime(selectedEvent.updatedAt));
    bodyHtml += '</div>';
    bodyHtml += '<div class="manager-inline-actions" style="margin-top:14px;">';
    if (selectedEvent.postedVoucherNo) {
      bodyHtml += '<button class="manager-btn" type="button" data-action="load-accounting-voucher" data-voucher-no="' + escapeHtml(selectedEvent.postedVoucherNo) + '">查看关联凭证</button>';
    }
    bodyHtml += '<button class="manager-btn" type="button" data-action="retry-accounting-event" data-event-id="' + escapeHtml(selectedEvent.eventId || '') + '">重试该事件</button>';
    bodyHtml += '</div>';
    bodyHtml += '<div class="manager-coupon-table-wrap" style="margin-top:14px;"><table class="manager-coupon-table">';
    bodyHtml += '<thead><tr><th>腿号</th><th>科目</th><th>账户域</th><th>账户类型</th><th>账户号</th><th>主体</th><th>方向</th><th>金额</th><th>业务角色</th><th>引用流水</th></tr></thead><tbody>';
    if (!selectedEvent.legs || selectedEvent.legs.length === 0) {
      bodyHtml += '<tr><td colspan="10" class="manager-coupon-empty-row">暂无资金腿</td></tr>';
    } else {
      bodyHtml += selectedEvent.legs.map(function (leg) {
        return '<tr>' +
          '<td>' + escapeHtml(leg.legNo || '-') + '</td>' +
          '<td>' + escapeHtml(buildSubjectDisplayText(leg.subjectHint, leg.subjectName)) + '</td>' +
          '<td>' + escapeHtml(leg.accountDomain || '-') + '</td>' +
          '<td>' + escapeHtml(formatAccountingAccountType(leg.accountType)) + '</td>' +
          '<td>' + escapeHtml(leg.accountNo || '-') + '</td>' +
          '<td>' + escapeHtml(formatAccountingOwnerType(leg.ownerType) + ' / ' + (leg.ownerId || '-')) + '</td>' +
          '<td>' + escapeHtml(formatAccountingDirection(leg.direction)) + '</td>' +
          '<td>' + escapeHtml(formatMoney(leg.amount)) + '</td>' +
          '<td>' + escapeHtml(leg.bizRole || '-') + '</td>' +
          '<td>' + escapeHtml(leg.referenceNo || '-') + '</td>' +
          '</tr>';
      }).join('');
    }
    bodyHtml += '</tbody></table></div>';
    bodyHtml += '<div style="margin-top:14px;">';
    bodyHtml += '<h4 class="manager-panel-title" style="font-size:13px;">事件载荷</h4>';
    bodyHtml += renderCodeBlock(selectedEvent.payload, '暂无事件载荷');
    bodyHtml += '</div>';
    return renderManagerModal({
      title: '会计事件详情',
      description: '详情查看通过弹窗完成，不再挤占事件列表页面。',
      closeAction: 'hide-accounting-event-detail',
      bodyHtml: bodyHtml,
    });
  }

  function renderAccountingVoucherDetailModal(selectedVoucher, visible) {
    if (!visible || !selectedVoucher) {
      return '';
    }
    var canReverseVoucher = selectedVoucher.voucherType !== 'REVERSE' && selectedVoucher.status !== 'REVERSED';
    var bodyHtml = '';
    bodyHtml += '<div class="manager-coupon-detail-grid">';
    bodyHtml += renderDetailItem('凭证号', selectedVoucher.voucherNo || '-');
    bodyHtml += renderDetailItem('凭证类型', formatAccountingVoucherType(selectedVoucher.voucherType));
    bodyHtml += renderDetailItem('事件号', selectedVoucher.eventId || '-');
    bodyHtml += renderDetailItem('状态', formatAccountingVoucherStatus(selectedVoucher.status));
    bodyHtml += renderDetailItem('来源业务', formatAccountingBizType(selectedVoucher.sourceBizType) + ' / ' + (selectedVoucher.sourceBizNo || '-'));
    bodyHtml += renderDetailItem('交易号', selectedVoucher.tradeOrderNo || '-');
    bodyHtml += renderDetailItem('支付号', selectedVoucher.payOrderNo || '-');
    bodyHtml += renderDetailItem('业务场景', formatBusinessSceneCode(selectedVoucher.businessSceneCode));
    bodyHtml += renderDetailItem('业务域', formatTradeBusinessDomain(selectedVoucher.businessDomainCode));
    bodyHtml += renderDetailItem('借方总额', formatMoney(selectedVoucher.totalDebitAmount));
    bodyHtml += renderDetailItem('贷方总额', formatMoney(selectedVoucher.totalCreditAmount));
    bodyHtml += renderDetailItem('会计日', selectedVoucher.postingDate || '-');
    bodyHtml += renderDetailItem('发生时间', formatDateTime(selectedVoucher.occurredAt));
    bodyHtml += renderDetailItem('被冲正凭证', selectedVoucher.reversedVoucherNo || '-');
    bodyHtml += renderDetailItem('创建时间', formatDateTime(selectedVoucher.createdAt));
    bodyHtml += renderDetailItem('更新时间', formatDateTime(selectedVoucher.updatedAt));
    bodyHtml += '</div>';
    bodyHtml += '<div class="manager-inline-actions" style="margin-top:14px;">';
    if (selectedVoucher.eventId) {
      bodyHtml += '<button class="manager-btn" type="button" data-action="load-accounting-event" data-event-id="' + escapeHtml(selectedVoucher.eventId) + '">查看关联事件</button>';
    }
    if (selectedVoucher.reversedVoucherNo) {
      bodyHtml += '<button class="manager-btn" type="button" data-action="load-accounting-voucher" data-voucher-no="' + escapeHtml(selectedVoucher.reversedVoucherNo) + '">查看冲正凭证</button>';
    }
    if (canReverseVoucher) {
      bodyHtml += '<button class="manager-btn" type="button" data-action="reverse-accounting-voucher" data-voucher-no="' + escapeHtml(selectedVoucher.voucherNo || '') + '">冲正该凭证</button>';
    }
    bodyHtml += '</div>';
    bodyHtml += '<div class="manager-coupon-table-wrap" style="margin-top:14px;"><table class="manager-coupon-table">';
    bodyHtml += '<thead><tr><th>行号</th><th>科目</th><th>借贷</th><th>金额</th><th>账户域</th><th>账户类型</th><th>账户号</th><th>主体</th><th>引用流水</th><th>摘要</th></tr></thead><tbody>';
    if (!selectedVoucher.entries || selectedVoucher.entries.length === 0) {
      bodyHtml += '<tr><td colspan="10" class="manager-coupon-empty-row">暂无分录</td></tr>';
    } else {
      bodyHtml += selectedVoucher.entries.map(function (entry) {
        return '<tr>' +
          '<td>' + escapeHtml(entry.lineNo || '-') + '</td>' +
          '<td>' + escapeHtml(buildSubjectDisplayText(entry.subjectCode, entry.subjectName)) + '</td>' +
          '<td>' + escapeHtml(formatAccountingDebitCreditFlag(entry.dcFlag)) + '</td>' +
          '<td>' + escapeHtml(formatMoney(entry.amount)) + '</td>' +
          '<td>' + escapeHtml(entry.accountDomain || '-') + '</td>' +
          '<td>' + escapeHtml(formatAccountingAccountType(entry.accountType)) + '</td>' +
          '<td>' + escapeHtml(entry.accountNo || '-') + '</td>' +
          '<td>' + escapeHtml(formatAccountingOwnerType(entry.ownerType) + ' / ' + (entry.ownerId || '-')) + '</td>' +
          '<td>' + escapeHtml(entry.referenceNo || '-') + '</td>' +
          '<td>' + escapeHtml(entry.entryMemo || '-') + '</td>' +
          '</tr>';
      }).join('');
    }
    bodyHtml += '</tbody></table></div>';
    return renderManagerModal({
      title: '会计凭证详情',
      description: '详情查看通过弹窗完成，不再挤占凭证列表页面。',
      closeAction: 'hide-accounting-voucher-detail',
      bodyHtml: bodyHtml,
    });
  }

  function renderAccountingSubjectEditorModal(accounting) {
    if (!accounting || !accounting.subjectEditorVisible) {
      return '';
    }
    var draft = accounting.subjectDraft || createEmptyAccountingSubjectDraft();
    var subjects = accounting.subjects || [];
    var selectedSubject = findAccountingSubjectByCode(accounting.selectedSubjectCode);
    var selectedChildCount = selectedSubject ? countAccountingSubjectChildren(selectedSubject.subjectCode, subjects) : 0;
    var codeReadonly = accounting.selectedSubjectCode ? ' readonly' : '';
    var bodyHtml = '';
    bodyHtml += '<div class="manager-accounting-subject-hint">';
    if (selectedSubject) {
      bodyHtml += '当前选中科目有 ' + escapeHtml(String(selectedChildCount)) + ' 个直接下级；存在下级时，不允许修改父级、层级、科目类型或余额方向。';
    } else {
      bodyHtml += '新建子科目时，填写父级科目后会自动带出层级、科目类型和余额方向。';
    }
    bodyHtml += '</div>';
    bodyHtml += '<div class="manager-coupon-form-grid compact" style="margin-top:12px;">';
    bodyHtml += renderField('科目编码', '<input id="accountingSubjectCode" class="form-input" value="' + escapeHtml(draft.subjectCode || '') + '" placeholder="如 200101"' + codeReadonly + '/>');
    bodyHtml += renderField('科目名称', '<input id="accountingSubjectName" class="form-input" value="' + escapeHtml(draft.subjectName || '') + '" placeholder="如 用户钱包可用余额"/>');
    bodyHtml += renderField('科目类型', '<select id="accountingSubjectType" class="form-input">' + renderAccountingSubjectTypeOptions(draft.subjectType || 'ASSET') + '</select>');
    bodyHtml += renderField('余额方向', '<select id="accountingSubjectBalanceDirection" class="form-input"><option value="DEBIT"' + (String(draft.balanceDirection || '') === 'DEBIT' ? ' selected' : '') + '>DEBIT</option><option value="CREDIT"' + (String(draft.balanceDirection || '') === 'CREDIT' ? ' selected' : '') + '>CREDIT</option></select>');
    bodyHtml += renderField('父级科目', '<input id="accountingSubjectParentCode" class="form-input" value="' + escapeHtml(draft.parentSubjectCode || '') + '" placeholder="可为空"/>');
    bodyHtml += renderField('层级', '<input id="accountingSubjectLevelNo" class="form-input" value="' + escapeHtml(draft.levelNo || '1') + '" placeholder="1"/>');
    bodyHtml += renderField('是否启用', '<select id="accountingSubjectEnabled" class="form-input"><option value="true"' + (String(draft.enabled || 'true') !== 'false' ? ' selected' : '') + '>true</option><option value="false"' + (String(draft.enabled || '') === 'false' ? ' selected' : '') + '>false</option></select>');
    bodyHtml += renderField('备注', '<input id="accountingSubjectRemark" class="form-input" value="' + escapeHtml(draft.remark || '') + '" placeholder="说明"/>');
    bodyHtml += '</div>';
    bodyHtml += '<div class="manager-rbac-actions">';
    bodyHtml += '<button class="manager-btn primary" type="button" data-action="save-accounting-subject">保存科目</button> ';
    bodyHtml += '<button class="manager-btn" type="button" data-action="clear-accounting-subject-form">新建科目</button> ';
    bodyHtml += '<button class="manager-btn" type="button" data-action="reload-accounting-subjects">刷新科目</button>';
    bodyHtml += '</div>';
    return renderManagerModal({
      title: accounting.selectedSubjectCode ? '编辑科目 - ' + accounting.selectedSubjectCode : '新建科目',
      description: '科目维护改为弹窗处理，主页面只保留科目树和列表。',
      closeAction: 'hide-accounting-subject-editor',
      bodyHtml: bodyHtml,
    });
  }

  function renderAccountingSubjectMetric(label, value, tip) {
    return (
      '<div class="manager-accounting-subject-metric">' +
      '<div class="manager-accounting-subject-metric-label">' + escapeHtml(label) + '</div>' +
      '<div class="manager-accounting-subject-metric-value">' + escapeHtml(String(value == null ? '-' : value)) + '</div>' +
      '<div class="manager-accounting-subject-metric-tip">' + escapeHtml(tip || '') + '</div>' +
      '</div>'
    );
  }

  function renderAccountingSubjectTree(nodes, selectedSubjectCode) {
    if (!nodes || nodes.length === 0) {
      return renderManagerBlockState(
        !!(state.accounting && state.accounting.subjectLoading) || (!!(state.accounting && state.accounting.loading) && !(state.accounting && state.accounting.loadedViews && state.accounting.loadedViews.subjects)),
        '会计科目加载中...',
        '暂无会计科目',
        'manager-rbac-empty'
      ).replace('class="manager-rbac-empty"', 'class="manager-rbac-empty" style="margin-top:12px;"');
    }
    return '<div class="manager-accounting-subject-tree">' + nodes.map(function (node) {
      return renderAccountingSubjectTreeNode(node, selectedSubjectCode);
    }).join('') + '</div>';
  }

  function renderAccountingSubjectTreeNode(node, selectedSubjectCode) {
    var activeClass = String(selectedSubjectCode || '') === String(node.subjectCode || '') ? ' active' : '';
    var html = '';
    html += '<div class="manager-accounting-subject-node' + activeClass + '">';
    html += '<button class="manager-accounting-subject-node-main" type="button" data-action="select-accounting-subject" data-subject-code="' + escapeHtml(node.subjectCode || '') + '">';
    html += '<div class="manager-accounting-subject-node-head">';
    html += '<div>';
    html += '<div class="manager-accounting-subject-node-code">' + escapeHtml(node.subjectCode || '-') + '</div>';
    html += '<div class="manager-accounting-subject-node-name">' + escapeHtml(node.subjectName || '-') + '</div>';
    html += '</div>';
    html += renderBooleanBadge(node.enabled, '启用', '停用');
    html += '</div>';
    html += '<div class="manager-accounting-subject-node-meta">';
    html += '<span class="manager-accounting-subject-tag">' + escapeHtml(formatAccountingSubjectType(node.subjectType)) + '</span>';
    html += '<span class="manager-accounting-subject-tag">Lv.' + escapeHtml(node.levelNo || '-') + '</span>';
    if (node.children && node.children.length > 0) {
      html += '<span class="manager-accounting-subject-tag">下级 ' + escapeHtml(String(node.children.length)) + '</span>';
    }
    html += '</div>';
    if (node.remark) {
      html += '<div class="manager-accounting-subject-node-remark">' + escapeHtml(node.remark) + '</div>';
    }
    html += '</button>';
    if (node.children && node.children.length > 0) {
      html += '<div class="manager-accounting-subject-node-children">';
      html += node.children.map(function (child) {
        return renderAccountingSubjectTreeNode(child, selectedSubjectCode);
      }).join('');
      html += '</div>';
    }
    html += '</div>';
    return html;
  }

  function renderAccountingEventStatusOptions(selected) {
    var options = ['', 'NEW', 'PROCESSING', 'POSTED', 'FAILED', 'REVERSED'];
    return options.map(function (value) {
      var current = String(selected || '').toUpperCase();
      var isSelected = current === String(value) ? ' selected' : '';
      var label = value ? formatAccountingEventStatus(value) : '全部';
      return '<option value=\"' + escapeHtml(value) + '\"' + isSelected + '>' + escapeHtml(label) + '</option>';
    }).join('');
  }

  function renderAccountingVoucherStatusOptions(selected) {
    var options = ['', 'CREATED', 'POSTED', 'REVERSED'];
    return options.map(function (value) {
      var current = String(selected || '').toUpperCase();
      var isSelected = current === String(value) ? ' selected' : '';
      var label = value ? formatAccountingVoucherStatus(value) : '全部';
      return '<option value=\"' + escapeHtml(value) + '\"' + isSelected + '>' + escapeHtml(label) + '</option>';
    }).join('');
  }

  function renderAccountingSubjectTypeOptions(selected) {
    var options = ['ASSET', 'LIABILITY', 'EQUITY', 'INCOME', 'EXPENSE', 'MEMO'];
    return options.map(function (value) {
      var isSelected = String(selected || '').toUpperCase() === value ? ' selected' : '';
      return '<option value=\"' + value + '\"' + isSelected + '>' + formatAccountingSubjectType(value) + '</option>';
    }).join('');
  }

  function renderDeliverPage(title, subtitle) {
    if (state.selectedPath === DELIVER_AUDIENCE_PATH) {
      return renderEmbeddedConsolePage(
        title || langText('人群管理', 'Audience Management'),
        subtitle || langText('按标签、分群和规则维护投放人群，并支持用户标签快照调试。', 'Maintain audience groups by tags, segments, and rules, with user-tag snapshot debugging support.'),
        'managerAudienceFrame',
        buildEmbeddedConsoleSrc('/manager/audience-console.html'),
        langText('人群管理台', 'Audience Console')
      );
    }
    return renderEmbeddedConsolePage(
      title || langText('投放中心', 'Deliver Center'),
      subtitle || langText('展位、素材、创意、单元、关系、疲劳度与定向规则统一管理。', 'Manage placements, materials, creatives, units, relations, frequency caps, and targeting rules in one place.'),
      'managerDeliverFrame',
      buildEmbeddedConsoleSrc('/manager/deliver-console.html'),
      langText('投放配置台', 'Deliver Console')
    );
  }

  function renderMessagePage(title, subtitle) {
    return renderEmbeddedConsolePage(
      title || langText('消息中心', 'Message Center'),
      subtitle || langText('会话、消息、红包、好友申请、好友关系与黑名单统一查看。', 'Unified view of conversations, messages, red packets, contact requests, friendships, and blacklist records.'),
      'managerMessageFrame',
      buildEmbeddedConsoleSrc('/manager/message-console.html'),
      langText('消息中心', 'Message Center')
    );
  }

  function renderFundPage(title, subtitle) {
    return renderEmbeddedConsolePage(
      title || langText('资金中心', 'Fund Center'),
      subtitle || langText('钱包、爱存、爱花、爱借、银行卡与收银台统一查看。', 'Unified view of wallet, AiCash, AiCredit, AiLoan, bank cards, and cashier.'),
      'managerFundFrame',
      resolveFundConsoleSrc(),
      langText('资金中心', 'Fund Center')
    );
  }

  function renderRiskPage(title, subtitle) {
    return renderEmbeddedConsolePage(
      title || langText('风控中心', 'Risk Control'),
      subtitle || langText('KYC、风险档案、双因子与黑名单统一排查与配置。', 'Unified triage and configuration for KYC, risk profiles, two-factor mode, and blacklist.'),
      'managerRiskFrame',
      buildEmbeddedConsoleSrc('/manager/risk-console.html'),
      langText('风控中心', 'Risk Control')
    );
  }

  function renderTradeOpsPage(title, subtitle) {
    return renderEmbeddedConsolePage(
      title || langText('出入金中心', 'Capital Flow Center'),
      subtitle || langText('按入金或出金链路查看订单状态、结果码与通道字段。', 'Inspect order status, result codes, and channel fields by inbound or outbound flow.'),
      'managerTradeOpsFrame',
      buildEmbeddedConsoleSrc('/manager/trade-ops-console.html'),
      langText('出入金中心', 'Capital Flow Center')
    );
  }

  function buildEmbeddedConsoleSrc(basePath) {
    var versionedPath = withAssetVersion(basePath);
    var separator = versionedPath.indexOf('?') >= 0 ? '&' : '?';
    return versionedPath + separator + 'path=' + encodeURIComponent(state.selectedPath || state.homePath || DASHBOARD_PATH);
  }

  function resolveFundConsoleSrc() {
    return buildEmbeddedConsoleSrc('/manager/fund-console.html');
  }

  function withAssetVersion(url) {
    var separator = url.indexOf('?') >= 0 ? '&' : '?';
    return url + separator + 'v=' + encodeURIComponent(MANAGER_ASSET_VERSION);
  }

  function renderEmbeddedConsolePage(title, subtitle, frameId, frameSrc, frameTitle) {
    var html = '';
    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle) + '</p>';
    html += '</div>';
    html += '</div>';
    html += '<section class="manager-panel manager-embedded-page">';
    html += '<iframe id="' + escapeHtml(frameId) + '" class="manager-embedded-frame" src="' + escapeHtml(frameSrc) + '" title="' + escapeHtml(frameTitle) + '" loading="lazy"></iframe>';
    html += '</section>';
    return html;
  }

  function renderHomePage(title, subtitle) {
    var menus = state.pageInit && Array.isArray(state.pageInit.menus) ? state.pageInit.menus : [];
    var rootModules = buildDisplayMenuTree(menus).filter(function (item) {
      return item.menuCode !== 'dashboard';
    });
    var quickCards = rootModules
      .map(function (menu, index) {
        var targetPath = resolveDefaultPathForMenuNode(menu);
        if (!targetPath) {
          return null;
        }
        var clsList = ['manager-home-a', 'manager-home-b', 'manager-home-c', 'manager-home-d', 'manager-home-e', 'manager-home-f'];
        var cls = clsList[index % clsList.length];
        return {
          title: localizeMenuName(menu.menuName, menu.path, menu.menuCode),
          summary: resolveHomeCardSummary(menu, targetPath),
          cls: cls,
          icon: menu.icon,
          path: targetPath,
        };
      })
      .filter(function (item) {
        return !!item;
      });

    var html = '';
    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    if (subtitle) {
      html += '<p class="manager-subtext">' + escapeHtml(subtitle) + '</p>';
    }
    html += '</div>';
    html += '</div>';

    if (quickCards.length > 0) {
      html += '<div class="manager-home-card-grid">';
      html += quickCards.map(renderHomeCard).join('');
      html += '</div>';
    } else {
      html += '<section class="manager-panel">';
      html += '<div class="manager-coupon-empty-row">' + escapeHtml(localizeText('暂无可访问模块')) + '</div>';
      html += '</section>';
    }

    return html;
  }

  function renderHomeCard(card) {
    var html = '';
    var actionAttrs = '';
    if (card.path) {
      actionAttrs =
        ' data-action="open-home-module" data-path="' +
        escapeHtml(card.path) +
        '" title="' +
        escapeHtml(localizeText('点击打开模块功能菜单')) +
        '"';
    }
    html += '<article class="manager-home-card ' + escapeHtml(card.cls) + '"' + actionAttrs + '>';
    html += '<div class="manager-home-card-inner">';
    html += '<div class="manager-home-card-icon">' + renderManagerIcon(card.icon, 'home') + '</div>';
    html += '<div class="manager-home-card-header">';
    html += '<p class="manager-home-card-title">' + escapeHtml(card.title) + '</p>';
    html += '</div>';
    if (card.summary) {
      html += '<div class="manager-home-card-summary-wrap">';
      html += '<p class="manager-home-card-summary">' + escapeHtml(card.summary) + '</p>';
      html += '</div>';
    }
    if (card.desc) {
      html += '<p class="manager-home-card-desc">' + escapeHtml(card.desc) + '</p>';
    }
    if (card.value) {
      html += '<div class="manager-home-card-value">' + escapeHtml(String(card.value)) + '</div>';
    }
    html += '</div>';
    html += '</article>';
    return html;
  }

  function resolveHomeCardSummary(menu, targetPath) {
    function normalizeOptionalText(raw) {
      var normalized = String(raw == null ? '' : raw).trim();
      return normalized || '';
    }
    var menuCode = normalizeOptionalText(menu && menu.menuCode).toLowerCase();
    var menuPath = normalizeOptionalText(menu && menu.path).toLowerCase();
    var resolvedPath = normalizeOptionalText(targetPath).toLowerCase();

    var summaryByCode = {
      'user-center': '用户账号、实名与安全信息',
      'coupon-center': '红包模板、发放与核销',
      'message-center': '会话消息、好友与红包记录',
      'fund-center': '钱包、基金、授信与借贷账户',
      'pricing-center': '费率规则与报价查询',
      'trade-center': '交易单查询与链路追踪',
      'inbound-center': '入金订单与通道状态',
      'deliver-center': '投放素材与投放位配置',
      'feedback-center': '反馈工单流转与处理',
      'accounting-center': '会计事件、凭证与分录',
      'outbound-center': '出金订单与通道状态',
      'app-center': '版本发布与提示开关',
      'risk-center': 'KYC审核与黑名单管理',
      'system-center': '权限角色与系统配置',
      'outbox-center': '消息投递中心（死信排查）',
    };

    if (menuCode && summaryByCode[menuCode]) {
      return localizeText(summaryByCode[menuCode]);
    }

    var summaryByPathPrefix = [
      { prefix: '/admin/users', summary: '用户账号、实名与安全信息' },
      { prefix: '/admin/coupons', summary: '红包模板、发放与核销' },
      { prefix: '/admin/messages', summary: '会话消息、好友与红包记录' },
      { prefix: '/admin/fund', summary: '钱包、基金、授信与借贷账户' },
      { prefix: '/admin/pricing', summary: '费率规则与报价查询' },
      { prefix: '/admin/trade', summary: '交易单查询与链路追踪' },
      { prefix: '/admin/inbound', summary: '入金订单与通道状态' },
      { prefix: '/admin/deliver', summary: '投放素材与投放位配置' },
      { prefix: '/admin/feedback', summary: '反馈工单流转与处理' },
      { prefix: '/admin/accounting', summary: '会计事件、凭证与分录' },
      { prefix: '/admin/outbound', summary: '出金订单与通道状态' },
      { prefix: '/admin/apps', summary: '版本发布与提示开关' },
      { prefix: '/admin/risk', summary: 'KYC审核与黑名单管理' },
      { prefix: '/admin/system', summary: '权限角色与系统配置' },
      { prefix: '/admin/outbox', summary: '消息投递中心（死信排查）' },
    ];

    var pathCandidates = [resolvedPath, menuPath];
    for (var pathIndex = 0; pathIndex < pathCandidates.length; pathIndex += 1) {
      var candidate = pathCandidates[pathIndex];
      if (!candidate) {
        continue;
      }
      for (var ruleIndex = 0; ruleIndex < summaryByPathPrefix.length; ruleIndex += 1) {
        var rule = summaryByPathPrefix[ruleIndex];
        if (candidate.indexOf(rule.prefix) === 0) {
          return localizeText(rule.summary);
        }
      }
    }

    return localizeText('模块功能入口');
  }

  function renderRbacPage(title, subtitle) {
    var rbac = state.rbac;
    var admins = rbac.admins || [];
    var roles = rbac.roles || [];
    var permissions = rbac.permissions || [];
    var modules = rbac.modules || [];
    var menus = rbac.menus || [];
    var rbacLoading = isManagerDataLoading(rbac.loaded, rbac.loading, rbac.notice);

    var selectedAdminId = normalizePositiveIdText(rbac.selectedAdminId);
    var selectedAdmin = admins.find(function (item) {
      return samePositiveId(item.adminId, selectedAdminId);
    });

    var selectedAuthorization = rbac.authorizationMap[selectedAdminId] || {
      adminId: selectedAdminId || null,
      roleCodes: selectedAdmin ? selectedAdmin.roleCodes || [] : [],
      permissionCodes: [],
      menuCodes: [],
    };

    var selectedRoleCode = rbac.selectedRoleCode;
    var selectedRole = roles.find(function (item) {
      return item.roleCode === selectedRoleCode;
    });
    var selectedRoleMenuCodes = selectedRole ? selectedRole.menuCodes || [] : [];
    var selectedRolePermissionCodes = selectedRole ? selectedRole.permissionCodes || [] : [];

    var roleOptionsHtml = roles
      .map(function (role) {
        var selected = role.roleCode === selectedRoleCode ? ' selected' : '';
        return (
          '<option value="' +
          escapeHtml(role.roleCode) +
          '"' +
          selected +
          '>' +
          escapeHtml(role.roleName + ' (' + role.roleCode + ')') +
          '</option>'
        );
      })
      .join('');

    var adminListHtml = admins
      .map(function (admin) {
        var active = samePositiveId(admin.adminId, selectedAdminId) ? ' active' : '';
        var rolesText = (admin.roleCodes || []).join(', ') || langText('无角色', 'No roles');
        return (
          '<button class="manager-rbac-admin-item' +
          active +
          '" type="button" data-action="select-admin" data-admin-id="' +
          escapeHtml(admin.adminId) +
          '">' +
          '<div class="manager-rbac-admin-name">' +
          escapeHtml(admin.displayName || admin.username || String(admin.adminId)) +
          '</div>' +
          '<div class="manager-rbac-admin-meta">ID: ' +
          escapeHtml(admin.adminId) +
          langText(' · 用户名: ', ' · Username: ') +
          escapeHtml(admin.username || '-') +
          '</div>' +
          '<div class="manager-rbac-badge-row"><span class="manager-rbac-badge">' +
          escapeHtml(formatAdminAccountStatus(admin.accountStatus)) +
          '</span><span class="manager-rbac-badge">' +
          escapeHtml(rolesText) +
          '</span></div>' +
          '</button>'
        );
      })
      .join('');

    var adminRoleChecklist = roles
      .map(function (role) {
        var checked = (selectedAuthorization.roleCodes || []).indexOf(role.roleCode) >= 0 ? ' checked' : '';
        return (
          '<label class="manager-rbac-check-item">' +
          '<input type="checkbox" name="admin-role-checkbox" value="' +
          escapeHtml(role.roleCode) +
          '"' +
          checked +
          '/>' +
          '<span>' +
          escapeHtml(role.roleName) +
          '</span>' +
          '</label>'
        );
      })
      .join('');

    var menuTreeHtml = renderMenuTreeForRole(menus, selectedRoleMenuCodes);
    var permissionTreeHtml = renderPermissionTreeForRole(modules, permissions, selectedRolePermissionCodes);

    var html = '';
    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title || langText('权限管理', 'Access Control')) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle || langText('管理员、角色、菜单与接口权限管理。', 'Manage admins, roles, menus, and API permissions.')) + '</p>';
    html += '</div>';
    html += '</div>';

    html += '<div class="manager-rbac-grid">';
    html += '<section class="manager-panel">';
    html += '<h3 class="manager-panel-title">管理员列表</h3>';
    html += '<div class="manager-rbac-hint">选择管理员后可分配角色并查看最终可见菜单。</div>';
    html += '<div class="manager-rbac-admin-list">' + (adminListHtml || renderManagerBlockState(rbacLoading, '管理员数据加载中...', '暂无管理员', 'manager-rbac-empty')) + '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="reload-rbac">刷新权限数据</button>';
    html += '<span class="manager-rbac-notice">' + escapeHtml(rbac.notice || '') + '</span>';
    html += '</div>';
    html += '</section>';

    html += '<section class="manager-panel">';
    html += '<h3 class="manager-panel-title">管理员角色分配</h3>';
    html +=
      '<div class="manager-rbac-hint">当前管理员：' +
      escapeHtml(selectedAdmin ? selectedAdmin.displayName + ' (' + selectedAdmin.adminId + ')' : '未选择') +
      '</div>';
    html +=
      '<div class="manager-rbac-hint">已生效菜单数：' +
      String((selectedAuthorization.menuCodes || []).length) +
      '，已生效权限数：' +
      String((selectedAuthorization.permissionCodes || []).length) +
      '</div>';
    html += '<div class="manager-rbac-checklist">' + (adminRoleChecklist || renderManagerBlockState(rbacLoading, '角色数据加载中...', '暂无角色', 'manager-rbac-empty')) + '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="save-admin-roles">保存管理员角色</button>';
    html += '</div>';
    html += '</section>';
    html += '</div>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<div class="manager-rbac-config-head">';
    html += '<h3 class="manager-panel-title">角色菜单与接口权限配置</h3>';
    html +=
      '<select id="rbacRoleSelect" class="manager-select">' +
      (roleOptionsHtml || '<option value="">' + escapeHtml(rbacLoading ? '角色数据加载中...' : '暂无角色') + '</option>') +
      '</select>';
    html += '</div>';

    html += '<div class="manager-rbac-config-grid">';
    html += '<div>';
    html += '<h4 class="manager-panel-title">菜单可见权限</h4>';
    html += menuTreeHtml;
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="save-role-menus">保存角色菜单</button>';
    html += '</div>';
    html += '</div>';

    html += '<div>';
    html += '<h4 class="manager-panel-title">接口权限</h4>';
    html += permissionTreeHtml;
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="save-role-permissions">保存角色权限</button>';
    html += '</div>';
    html += '</div>';
    html += '</div>';
    html += '</section>';

    return html;
  }

  function renderOutboxPage(title, subtitle) {
    var outbox = state.outbox || {};
    var overview = outbox.overview || {};
    var topics = Array.isArray(outbox.topics) ? outbox.topics : [];
    var messages = Array.isArray(outbox.messages) ? outbox.messages : [];
    var deadLetters = Array.isArray(outbox.deadLetters) ? outbox.deadLetters : [];
    var filter = outbox.filter || {};
    var deadFilter = outbox.deadFilter || {};
    var requeue = outbox.requeue || {};
    var outboxLoading = isManagerDataLoading(outbox.loaded, outbox.loading, outbox.notice);
    var messageLoading = !!outbox.messageLoading || outboxLoading;
    var deadLoading = !!outbox.deadLetterLoading || outboxLoading;
    var html = '';

    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle || langText('可靠异步消息投递监控，支持死信批量重放与链路排查。', 'Monitor reliable async message delivery with dead-letter batch replay and flow diagnostics.')) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="reload-outbox">刷新投递数据</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(outbox.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    html += '<p class="manager-user-summary-text">'
      + escapeHtml('总量：' + String(overview.totalCount || 0)
      + '，待处理：' + String(overview.pendingCount || 0)
      + '，处理中：' + String(overview.processingCount || 0)
      + '，成功：' + String(overview.succeededCount || 0)
      + '，死信：' + String(overview.deadCount || 0))
      + '</p>';
    html += '<p class="manager-user-summary-text">'
      + escapeHtml('待重试：' + String(overview.retryPendingCount || 0)
      + '，已重试：' + String(overview.retriedCount || 0)
      + '，可分发：' + String(overview.readyDispatchCount || 0)
      + '，超时处理中：' + String(overview.staleProcessingCount || 0))
      + '</p>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">主题分布</h3>';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>主题</th><th>总量</th><th>待处理</th><th>处理中</th><th>成功</th><th>死信</th><th>已重试</th></tr></thead><tbody>';
    if (topics.length === 0) {
      html += renderManagerTableStateRow(7, outboxLoading, '主题分布加载中...', '暂无主题分布数据');
    } else {
      html += topics.map(function (topicRow) {
        return '<tr>'
          + '<td>' + escapeHtml(topicRow.topic || '-') + '</td>'
          + '<td>' + escapeHtml(String(topicRow.totalCount == null ? 0 : topicRow.totalCount)) + '</td>'
          + '<td>' + escapeHtml(String(topicRow.pendingCount == null ? 0 : topicRow.pendingCount)) + '</td>'
          + '<td>' + escapeHtml(String(topicRow.processingCount == null ? 0 : topicRow.processingCount)) + '</td>'
          + '<td>' + escapeHtml(String(topicRow.succeededCount == null ? 0 : topicRow.succeededCount)) + '</td>'
          + '<td>' + escapeHtml(String(topicRow.deadCount == null ? 0 : topicRow.deadCount)) + '</td>'
          + '<td>' + escapeHtml(String(topicRow.retriedCount == null ? 0 : topicRow.retriedCount)) + '</td>'
          + '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">消息查询</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('主题', '<input id="outboxMessageTopicFilter" class="form-input" value="' + escapeHtml(filter.topic || '') + '" placeholder="可选 topic"/>');
    html += renderField('状态', '<select id="outboxMessageStatusFilter" class="form-select">' + renderOutboxStatusOptions(filter.status || '', true) + '</select>');
    html += renderField('关键字', '<input id="outboxMessageKeywordFilter" class="form-input" value="' + escapeHtml(filter.keyword || '') + '" placeholder="messageKey / error"/>');
    html += renderField('返回条数', '<input id="outboxMessageLimitFilter" class="form-input" value="' + escapeHtml(filter.limit || '20') + '" placeholder="20"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="query-outbox-messages">查询消息</button>';
    html += '<label class="manager-rbac-notice"><input id="outboxMessageOnlyRetried" type="checkbox"' + (filter.onlyRetried ? ' checked="checked"' : '') + '/>仅看已重试</label>';
    html += '<label class="manager-rbac-notice"><input id="outboxMessageIncludePayload" type="checkbox"' + (filter.includePayload ? ' checked="checked"' : '') + '/>显示载荷</label>';
    html += '</div>';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>ID</th><th>主题</th><th>消息键</th><th>状态</th><th>重试</th><th>下次重试</th><th>更新时间</th><th>最近错误</th><th>载荷预览</th></tr></thead><tbody>';
    if (messages.length === 0) {
      html += renderManagerTableStateRow(9, messageLoading, '消息列表加载中...', '暂无消息数据');
    } else {
      html += messages.map(function (message) {
        return '<tr>'
          + '<td>' + escapeHtml(String(message.id == null ? '-' : message.id)) + '</td>'
          + '<td>' + escapeHtml(message.topic || '-') + '</td>'
          + '<td>' + escapeHtml(message.messageKey || '-') + '</td>'
          + '<td>' + renderStatusBadge(message.status || '-', formatGenericStatusText(message.status)) + '</td>'
          + '<td>' + escapeHtml(String(message.retryCount == null ? 0 : message.retryCount) + '/' + String(message.maxRetryCount == null ? 0 : message.maxRetryCount)) + '</td>'
          + '<td>' + escapeHtml(formatDateTime(message.nextRetryAt)) + '</td>'
          + '<td>' + escapeHtml(formatDateTime(message.updatedAt)) + '</td>'
          + '<td>' + escapeHtml(formatOutboxPayloadPreview(message.lastError, 48)) + '</td>'
          + '<td>' + escapeHtml(formatOutboxPayloadPreview(message.payloadPreview || message.payload, 80)) + '</td>'
          + '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">死信消息与重放</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('死信主题', '<input id="outboxDeadTopicFilter" class="form-input" value="' + escapeHtml(deadFilter.topic || '') + '" placeholder="可选 topic"/>');
    html += renderField('关键字', '<input id="outboxDeadKeywordFilter" class="form-input" value="' + escapeHtml(deadFilter.keyword || '') + '" placeholder="messageKey / error"/>');
    html += renderField('返回条数', '<input id="outboxDeadLimitFilter" class="form-input" value="' + escapeHtml(deadFilter.limit || '20') + '" placeholder="20"/>');
    html += renderField('重放时间', '<input id="outboxRequeueNextRetryAt" class="form-input" type="datetime-local" value="' + escapeHtml(requeue.nextRetryAt || '') + '" placeholder="留空则立即重试"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="query-outbox-dead-letters">查询死信</button>';
    html += '<label class="manager-rbac-notice"><input id="outboxDeadIncludePayload" type="checkbox"' + (deadFilter.includePayload ? ' checked="checked"' : '') + '/>显示载荷</label>';
    html += '</div>';

    html += '<div class="manager-coupon-form-grid compact" style="margin-top:8px;">';
    html += renderField('批量主题', '<input id="outboxRequeueTopic" class="form-input" value="' + escapeHtml(requeue.topic || '') + '" placeholder="留空表示所有主题"/>');
    html += renderField('批量上限', '<input id="outboxRequeueLimit" class="form-input" value="' + escapeHtml(requeue.limit || '20') + '" placeholder="20"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="requeue-outbox-dead-letters">批量重放死信</button>';
    html += '</div>';

    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>ID</th><th>主题</th><th>消息键</th><th>状态</th><th>重试</th><th>最近错误</th><th>更新时间</th><th>操作</th></tr></thead><tbody>';
    if (deadLetters.length === 0) {
      html += renderManagerTableStateRow(8, deadLoading, '死信列表加载中...', '暂无死信消息');
    } else {
      html += deadLetters.map(function (message) {
        return '<tr>'
          + '<td>' + escapeHtml(String(message.id == null ? '-' : message.id)) + '</td>'
          + '<td>' + escapeHtml(message.topic || '-') + '</td>'
          + '<td>' + escapeHtml(message.messageKey || '-') + '</td>'
          + '<td>' + renderStatusBadge(message.status || '-', formatGenericStatusText(message.status)) + '</td>'
          + '<td>' + escapeHtml(String(message.retryCount == null ? 0 : message.retryCount) + '/' + String(message.maxRetryCount == null ? 0 : message.maxRetryCount)) + '</td>'
          + '<td>' + escapeHtml(formatOutboxPayloadPreview(message.lastError, 56)) + '</td>'
          + '<td>' + escapeHtml(formatDateTime(message.updatedAt)) + '</td>'
          + '<td><button class="manager-btn" type="button" data-action="requeue-outbox-dead-letter" data-message-id="' + escapeHtml(String(message.id == null ? '' : message.id)) + '">重放</button></td>'
          + '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderObservabilityPage(title, subtitle) {
    var observability = state.observability || {};
    var overview = observability.overview || {};
    var apiScenes = Array.isArray(observability.apiScenes) ? observability.apiScenes : [];
    var filter = observability.filter || {};
    var sceneLoading = !!observability.sceneLoading || isManagerDataLoading(observability.loaded, observability.loading, observability.notice);
    var html = '';

    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle || langText('汇总 API 指标、请求场景耗时与 Outbox 健康状态。', 'Aggregate API metrics, request-scene latency, and Outbox health status.')) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="reload-observability">刷新指标</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(observability.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    html += '<p class="manager-user-summary-text">'
      + escapeHtml('API总量：' + String(overview.apiRequestTotal || 0)
      + '，成功：' + String(overview.apiRequestSuccessTotal || 0)
      + '，失败：' + String(overview.apiRequestFailureTotal || 0)
      + '，平均耗时(ms)：' + formatDecimalText(overview.apiAvgLatencyMs, 3)
      + '，最大耗时(ms)：' + formatDecimalText(overview.apiMaxLatencyMs, 3))
      + '</p>';
    html += '<p class="manager-user-summary-text">'
      + escapeHtml('Outbox死信：' + String(overview.outboxDeadCount || 0)
      + '，Outbox待重试：' + String(overview.outboxRetryPendingCount || 0)
      + '，待对账支付：' + String(overview.reconPendingPayCount || 0)
      + '，今日失败交易：' + String(overview.tradeFailedTodayCount || 0)
      + '，生成时间：' + formatDateTime(overview.generatedAt))
      + '</p>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">API 场景指标</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('返回条数', '<input id="observabilitySceneLimit" class="form-input" value="' + escapeHtml(filter.limit || '20') + '" placeholder="20"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="query-observability-scenes">查询场景指标</button>';
    html += '</div>';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>场景</th><th>请求总量</th><th>成功量</th><th>失败量</th><th>平均耗时(ms)</th><th>最大耗时(ms)</th></tr></thead><tbody>';
    if (apiScenes.length === 0) {
      html += renderManagerTableStateRow(6, sceneLoading, '场景指标加载中...', '暂无场景指标');
    } else {
      html += apiScenes.map(function (row) {
        return '<tr>'
          + '<td>' + escapeHtml(row.scene || '-') + '</td>'
          + '<td>' + escapeHtml(String(row.total == null ? 0 : row.total)) + '</td>'
          + '<td>' + escapeHtml(String(row.success == null ? 0 : row.success)) + '</td>'
          + '<td>' + escapeHtml(String(row.failure == null ? 0 : row.failure)) + '</td>'
          + '<td>' + escapeHtml(formatDecimalText(row.avgLatencyMs, 3)) + '</td>'
          + '<td>' + escapeHtml(formatDecimalText(row.maxLatencyMs, 3)) + '</td>'
          + '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderAuditPage(title, subtitle) {
    var audit = state.audit || {};
    var rows = Array.isArray(audit.rows) ? audit.rows : [];
    var filter = audit.filter || {};
    var auditLoading = isManagerDataLoading(audit.loaded, audit.loading, audit.notice);
    var html = '';

    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle || langText('记录后台写操作审计日志，支持按管理员、路径、状态与时间范围筛选。', 'Audit backend write operations with filters by admin, path, status, and time range.')) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="reload-audit">刷新审计日志</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(audit.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">筛选条件</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('管理员ID', '<input id="auditFilterAdminId" class="form-input" value="' + escapeHtml(filter.adminId || '') + '" placeholder="可选"/>');
    html += renderField('请求方法', '<select id="auditFilterMethod" class="form-select">' + renderAuditMethodOptions(filter.requestMethod || '', true) + '</select>');
    html += renderField('请求路径', '<input id="auditFilterRequestPath" class="form-input" value="' + escapeHtml(filter.requestPath || '') + '" placeholder="/api/admin/..."/>');
    html += renderField('执行结果', '<select id="auditFilterResultStatus" class="form-select">' + renderAuditResultOptions(filter.resultStatus || '', true) + '</select>');
    html += renderField('开始时间', '<input id="auditFilterFrom" type="datetime-local" class="form-input" value="' + escapeHtml(filter.from || '') + '"/>');
    html += renderField('结束时间', '<input id="auditFilterTo" type="datetime-local" class="form-input" value="' + escapeHtml(filter.to || '') + '"/>');
    html += renderField('返回条数', '<input id="auditFilterLimit" class="form-input" value="' + escapeHtml(filter.limit || '20') + '" placeholder="20"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="query-audits">查询审计日志</button>';
    html += '</div>';
    html += '</section>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">审计日志列表</h3>';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>时间</th><th>管理员</th><th>请求</th><th>结果</th><th>耗时(ms)</th><th>客户端IP</th><th>TraceId</th><th>错误摘要</th></tr></thead><tbody>';
    if (rows.length === 0) {
      html += renderManagerTableStateRow(8, auditLoading, '审计日志加载中...', '暂无审计日志');
    } else {
      html += rows.map(function (row) {
        return '<tr>'
          + '<td>' + escapeHtml(formatDateTime(row.createdAt)) + '</td>'
          + '<td>' + escapeHtml(String(row.adminUsername || '-') + ' / ' + String(row.adminId == null ? '-' : row.adminId)) + '</td>'
          + '<td>' + escapeHtml(formatAuditRequestPath(row)) + '</td>'
          + '<td>' + renderStatusBadge(row.resultStatus || '-', formatGenericStatusText(row.resultStatus)) + '</td>'
          + '<td>' + escapeHtml(String(row.costMs == null ? '-' : row.costMs)) + '</td>'
          + '<td>' + escapeHtml(row.clientIp || '-') + '</td>'
          + '<td>' + escapeHtml(row.traceId || '-') + '</td>'
          + '<td>' + escapeHtml(formatOutboxPayloadPreview(row.errorMessage, 64)) + '</td>'
          + '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderCouponPage(title, subtitle) {
    if (isPathActive(COUPON_TEMPLATE_PATH)) {
      return renderCouponTemplatePage(title, subtitle);
    }
    if (isPathActive(COUPON_ISSUE_PATH)) {
      return renderCouponIssuePage(title, subtitle);
    }
    if (isPathActive(COUPON_REDEEM_PATH)) {
      return renderCouponRedeemPage(title, subtitle);
    }
    return renderCouponOverviewPage(title, subtitle);
  }

  function renderCouponOverviewPage(title, subtitle) {
    var coupon = state.coupon;
    var summary = coupon.summary || {};
    var issues = coupon.issues || [];
    var issueListLoading = !!coupon.issuesLoading || isManagerDataLoading(coupon.loaded, coupon.loading, coupon.notice);

    var cards = [
      {
        title: '活跃模板',
        desc: '可发放模板数量',
        value: summary.activeTemplateCount || 0,
        cls: 'manager-home-a',
        icon: '★',
      },
      {
        title: '暂停模板',
        desc: '已暂停模板数量',
        value: summary.pausedTemplateCount || 0,
        cls: 'manager-home-b',
        icon: 'Ⅱ',
      },
      {
        title: '草稿模板',
        desc: '待发布模板数量',
        value: summary.draftTemplateCount || 0,
        cls: 'manager-home-d',
        icon: '✎',
      },
      {
        title: '过期模板',
        desc: '已过期模板数量',
        value: summary.expiredTemplateCount || 0,
        cls: 'manager-home-e',
        icon: '⌛',
      },
      {
        title: '累计发放',
        desc: '红包发放总数',
        value: summary.totalIssuedCoupons || 0,
        cls: 'manager-home-c',
        icon: '↗',
      },
    ];

    var html = '';
    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="reload-coupon">刷新红包数据</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(coupon.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    html += '<div class="manager-home-card-grid">';
    html += cards.map(renderHomeCard).join('');
    html += '</div>';

    html += '<div style="margin-top:14px;">';
    html += '<section class="manager-panel">';
    html += '<h3 class="manager-panel-title">最近发放记录</h3>';
    html += '<div class="manager-coupon-table-wrap">';
    html += '<table class="manager-coupon-table">';
    html += '<thead><tr><th>券号</th><th>用户ID</th><th>金额</th><th>状态</th></tr></thead>';
    html += '<tbody>';
    if (issues.length === 0) {
      html += renderManagerTableStateRow(4, issueListLoading, '发放记录加载中...', '暂无发放记录');
    } else {
      html += issues
        .slice(0, 8)
        .map(function (issue) {
          return (
            '<tr>' +
            '<td>' + escapeHtml(issue.couponNo) + '</td>' +
            '<td>' + escapeHtml(issue.userId) + '</td>' +
            '<td>' + escapeHtml(formatMoney(issue.couponAmount)) + '</td>' +
            '<td>' + renderStatusBadge(issue.status, formatCouponIssueStatus(issue.status)) + '</td>' +
            '</tr>'
          );
        })
        .join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    html += '</div>';
    return html;
  }

  function renderCouponTemplatePage(title, subtitle) {
    var coupon = state.coupon;
    var filter = coupon.templateFilter || {};
    var templates = coupon.templates || [];
    var templateListLoading = !!coupon.templatesLoading || isManagerDataLoading(coupon.loaded, coupon.loading, coupon.notice);

    var html = '';
    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="open-coupon-template-create">新建模板</button>';
    html += '<button class="manager-btn" type="button" data-action="query-coupon-templates">查询模板</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(coupon.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    html += '<section class="manager-panel">';
    html += '<h3 class="manager-panel-title">模板筛选</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('场景类型', '<select id="couponTemplateSceneFilter" class="form-input">' + renderSceneOptions(filter.sceneType, true) + '</select>');
    html += renderField('模板状态', '<select id="couponTemplateStatusFilter" class="form-input">' + renderTemplateStatusOptions(filter.status, true) + '</select>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="query-coupon-templates">按条件查询</button>';
    html += '<button class="manager-btn" type="button" data-action="reload-coupon">刷新汇总</button>';
    html += '</div>';
    html += '</section>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">模板列表</h3>';
    html += '<div class="manager-coupon-table-wrap">';
    html += '<table class="manager-coupon-table">';
    html += '<thead><tr><th>ID</th><th>编码</th><th>名称</th><th>场景</th><th>面额</th><th>库存</th><th>状态</th><th>操作</th></tr></thead>';
    html += '<tbody>';
    if (templates.length === 0) {
      html += renderManagerTableStateRow(8, templateListLoading, '模板数据加载中...', '暂无模板数据');
    } else {
      html += templates
        .map(function (template) {
          return (
            '<tr>' +
            '<td>' + escapeHtml(template.templateId) + '</td>' +
            '<td>' + escapeHtml(template.templateCode) + '</td>' +
            '<td>' + escapeHtml(template.templateName) + '</td>' +
            '<td>' + escapeHtml(formatCouponSceneType(template.sceneType)) + '</td>' +
            '<td>' + escapeHtml(renderTemplateAmount(template)) + '</td>' +
            '<td>' + escapeHtml(String(template.claimedCount || 0) + '/' + String(template.totalStock || 0)) + '</td>' +
            '<td>' + renderStatusBadge(template.status, formatCouponTemplateStatus(template.status)) + '</td>' +
            '<td>' + renderTemplateActions(template) + '</td>' +
            '</tr>'
          );
        })
        .join('');
    }
    html += '</tbody>';
    html += '</table>';
    html += '</div>';
    html += '</section>';
    html += renderCouponTemplateCreateModal(coupon);
    return html;
  }

  function renderCouponTemplateCreateModal(coupon) {
    if (!coupon || !coupon.templateCreateVisible) {
      return '';
    }
    var createStart = buildDateTimeLocal(0);
    var createEnd = buildDateTimeLocal(90);
    var useEnd = buildDateTimeLocal(120);
    var bodyHtml = '';
    bodyHtml += '<div class="manager-coupon-form-grid">';
    bodyHtml += renderField('模板编码', '<input id="couponTemplateCode" class="form-input" placeholder="例如 GJ_PAY_2026Q2"/>');
    bodyHtml += renderField('模板名称', '<input id="couponTemplateName" class="form-input" placeholder="例如 顾郡支付补贴红包"/>');
    bodyHtml += renderField('场景类型', '<select id="couponTemplateSceneType" class="form-input">' + renderSceneOptions('PAYMENT_INCENTIVE', false) + '</select>');
    bodyHtml += renderField('面额类型', '<select id="couponTemplateValueType" class="form-input"><option value="FIXED" selected>固定金额</option><option value="RANDOM">随机金额</option></select>');
    bodyHtml += renderField('固定金额(元)', '<input id="couponTemplateAmount" class="form-input" placeholder="固定红包填，如 8.88"/>');
    bodyHtml += renderField('随机最小金额(元)', '<input id="couponTemplateMinAmount" class="form-input" placeholder="随机红包填，如 2.00"/>');
    bodyHtml += renderField('随机最大金额(元)', '<input id="couponTemplateMaxAmount" class="form-input" placeholder="随机红包填，如 12.00"/>');
    bodyHtml += renderField('门槛金额(元)', '<input id="couponTemplateThresholdAmount" class="form-input" placeholder="可空，默认 0"/>');
    bodyHtml += renderField('总预算(元)', '<input id="couponTemplateTotalBudget" class="form-input" placeholder="例如 10000.00"/>');
    bodyHtml += renderField('总库存', '<input id="couponTemplateTotalStock" class="form-input" placeholder="例如 1000"/>');
    bodyHtml += renderField('单用户限领', '<input id="couponTemplatePerUserLimit" class="form-input" value="1"/>');
    bodyHtml += renderField('资金来源', '<input id="couponTemplateFundingSource" class="form-input" value="PLATFORM_MARKETING"/>');
    bodyHtml += renderField('领取开始', '<input id="couponTemplateClaimStart" type="datetime-local" class="form-input" value="' + escapeHtml(createStart) + '"/>');
    bodyHtml += renderField('领取结束', '<input id="couponTemplateClaimEnd" type="datetime-local" class="form-input" value="' + escapeHtml(createEnd) + '"/>');
    bodyHtml += renderField('使用开始', '<input id="couponTemplateUseStart" type="datetime-local" class="form-input" value="' + escapeHtml(createStart) + '"/>');
    bodyHtml += renderField('使用结束', '<input id="couponTemplateUseEnd" type="datetime-local" class="form-input" value="' + escapeHtml(useEnd) + '"/>');
    bodyHtml += renderField('初始状态', '<select id="couponTemplateInitialStatus" class="form-input">' + renderTemplateStatusOptions('ACTIVE', false) + '</select>');
    bodyHtml += renderField('规则JSON', '<input id="couponTemplateRulePayload" class="form-input" placeholder=\'可空，例如 {"biz":"pay"}\'/>');
    bodyHtml += '</div>';
    bodyHtml += '<div class="manager-rbac-actions">';
    bodyHtml += '<button class="manager-btn primary" type="button" data-action="create-coupon-template">创建模板</button>';
    bodyHtml += '</div>';
    return renderManagerModal({
      title: '新建红包模板',
      description: '创建入口迁移到弹窗，列表页仅保留筛选和模板列表。',
      closeAction: 'hide-coupon-template-create',
      bodyHtml: bodyHtml,
    });
  }

  function renderCouponIssuePage(title, subtitle) {
    var coupon = state.coupon;
    var filter = coupon.issueFilter || {};
    var issues = coupon.issues || [];
    var templates = coupon.templates || [];
    var issueListLoading = !!coupon.issuesLoading || isManagerDataLoading(coupon.loaded, coupon.loading, coupon.notice);

    var html = '';
    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="open-coupon-issue-create">发放红包</button>';
    html += '<button class="manager-btn" type="button" data-action="query-coupon-issues">查询发放记录</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(coupon.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    html += '<section class="manager-panel">';
    html += '<h3 class="manager-panel-title">发放记录筛选</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('模板ID', '<input id="couponIssueFilterTemplateId" class="form-input" value="' + escapeHtml(filter.templateId || '') + '" placeholder="可空"/>');
    html += renderField('用户ID', '<input id="couponIssueFilterUserId" class="form-input" value="' + escapeHtml(filter.userId || '') + '" placeholder="可空"/>');
    html += renderField('状态', '<select id="couponIssueFilterStatus" class="form-input">' + renderIssueStatusOptions(filter.status, true) + '</select>');
    html += renderField('返回条数', '<input id="couponIssueFilterLimit" class="form-input" value="' + escapeHtml(filter.limit || '20') + '" placeholder="1-200"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="query-coupon-issues">按条件查询</button>';
    html += '<button class="manager-btn" type="button" data-action="reload-coupon">刷新模块数据</button>';
    html += '</div>';
    html += '</section>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">发放记录</h3>';
    html += '<div class="manager-coupon-table-wrap">';
    html += '<table class="manager-coupon-table">';
    html += '<thead><tr><th>券号</th><th>用户ID</th><th>模板ID</th><th>金额</th><th>状态</th><th>渠道</th><th>领取时间</th><th>过期时间</th><th>操作</th></tr></thead>';
    html += '<tbody>';
    if (issues.length === 0) {
      html += renderManagerTableStateRow(9, issueListLoading, '发放记录加载中...', '暂无发放记录');
    } else {
      html += issues
        .map(function (issue) {
          return (
            '<tr>' +
            '<td>' + escapeHtml(issue.couponNo) + '</td>' +
            '<td>' + escapeHtml(issue.userId) + '</td>' +
            '<td>' + escapeHtml(issue.templateId) + '</td>' +
            '<td>' + escapeHtml(formatMoney(issue.couponAmount)) + '</td>' +
            '<td>' + renderStatusBadge(issue.status, formatCouponIssueStatus(issue.status)) + '</td>' +
            '<td>' + escapeHtml(formatCouponClaimChannel(issue.claimChannel)) + '</td>' +
            '<td>' + escapeHtml(formatDateTime(issue.claimedAt)) + '</td>' +
            '<td>' + escapeHtml(formatDateTime(issue.expireAt)) + '</td>' +
            '<td><button class="manager-btn" type="button" data-action="load-coupon-issue" data-coupon-no="' + escapeHtml(issue.couponNo) + '">详情</button></td>' +
            '</tr>'
          );
        })
        .join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    html += renderCouponIssueCreateModal(coupon, templates);
    html += renderCouponIssueDetailModal(coupon);
    return html;
  }

  function renderCouponRedeemPage(title, subtitle) {
    var coupon = state.coupon;

    var html = '';
    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="open-coupon-redeem">核销红包</button>';
    html += '<button class="manager-btn" type="button" data-action="open-coupon-issue-detail">查询券详情</button>';
    html += '<button class="manager-btn" type="button" data-action="reload-coupon">刷新红包数据</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(coupon.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    html += '<section class="manager-panel">';
    html += '<h3 class="manager-panel-title">操作入口</h3>';
    html += '<div class="manager-inline-panels">';
    html += '<section class="manager-panel">';
    html += '<h4 class="manager-panel-title">红包核销</h4>';
    html += '<div class="manager-rbac-hint">核销动作改为弹窗处理，主页面不再展示整块操作表单。</div>';
    html += '<div class="manager-rbac-actions"><button class="manager-btn primary" type="button" data-action="open-coupon-redeem">去核销</button></div>';
    html += '</section>';
    html += '<section class="manager-panel">';
    html += '<h4 class="manager-panel-title">券详情查询</h4>';
    html += '<div class="manager-rbac-hint">详情查询改为弹窗处理，可输入券号查看领取、过期与核销状态。</div>';
    html += '<div class="manager-rbac-actions"><button class="manager-btn" type="button" data-action="open-coupon-issue-detail">查看券详情</button></div>';
    html += '</section>';
    html += '</div>';
    html += '</section>';
    html += renderCouponRedeemModal(coupon);
    html += renderCouponIssueDetailModal(coupon);
    return html;
  }

  function renderCouponIssueCreateModal(coupon, templates) {
    if (!coupon || !coupon.issueCreateVisible) {
      return '';
    }
    var bodyHtml = '';
    bodyHtml += '<div class="manager-coupon-form-grid compact">';
    bodyHtml += renderField('模板ID', '<input id="couponIssueTemplateId" class="form-input" placeholder="必填，模板ID"/>');
    bodyHtml += renderField('用户ID', '<input id="couponIssueUserId" class="form-input" value="880100068483692100" placeholder="必填"/>');
    bodyHtml += renderField('发放渠道', '<input id="couponIssueClaimChannel" class="form-input" value="COUPON_CENTER" placeholder="例如 APP_ACTIVITY"/>');
    bodyHtml += renderField('业务单号', '<input id="couponIssueBusinessNo" class="form-input" placeholder="可空"/>');
    bodyHtml += '</div>';
    if (coupon.templatesLoading) {
      bodyHtml += '<div class="manager-coupon-tip">模板数据加载中...</div>';
    } else if (templates.length > 0) {
      bodyHtml += '<div class="manager-coupon-tip">可用模板ID参考：' + escapeHtml(templates.map(function (item) { return item.templateId + '(' + item.templateCode + ')'; }).join('，')) + '</div>';
    } else {
      bodyHtml += '<div class="manager-coupon-tip">暂无模板数据</div>';
    }
    bodyHtml += '<div class="manager-rbac-actions">';
    bodyHtml += '<button class="manager-btn primary" type="button" data-action="issue-coupon">发放红包</button>';
    bodyHtml += '</div>';
    return renderManagerModal({
      title: '发放红包',
      description: '发放入口迁移到弹窗，主页面仅保留筛选与发放记录列表。',
      closeAction: 'hide-coupon-issue-create',
      bodyHtml: bodyHtml,
    });
  }

  function renderCouponRedeemModal(coupon) {
    if (!coupon || !coupon.redeemVisible) {
      return '';
    }
    var bodyHtml = '';
    bodyHtml += '<div class="manager-coupon-form-grid compact">';
    bodyHtml += renderField('券号', '<input id="couponRedeemCouponNo" class="form-input" placeholder="必填，例如 4010..."/>');
    bodyHtml += renderField('订单号', '<input id="couponRedeemOrderNo" class="form-input" placeholder="必填，32位订单号"/>');
    bodyHtml += '</div>';
    bodyHtml += '<div class="manager-rbac-actions">';
    bodyHtml += '<button class="manager-btn primary" type="button" data-action="redeem-coupon">核销红包</button>';
    bodyHtml += '</div>';
    return renderManagerModal({
      title: '红包核销',
      description: '核销入口迁移到弹窗，避免主页面直接摊开操作表单。',
      closeAction: 'hide-coupon-redeem',
      bodyHtml: bodyHtml,
    });
  }

  function renderCouponIssueDetailModal(coupon) {
    if (!coupon || !coupon.issueDetailVisible) {
      return '';
    }
    var selectedIssue = coupon.selectedIssue;
    var bodyHtml = '';
    bodyHtml += '<div class="manager-coupon-form-grid compact">';
    bodyHtml += renderField('券号', '<input id="couponIssueDetailNo" class="form-input" value="' + escapeHtml(selectedIssue ? selectedIssue.couponNo : '') + '" placeholder="输入券号后点击查询"/>');
    bodyHtml += '</div>';
    bodyHtml += '<div class="manager-rbac-actions">';
    bodyHtml += '<button class="manager-btn primary" type="button" data-action="load-coupon-issue" data-coupon-no="' + escapeHtml(selectedIssue ? selectedIssue.couponNo : '') + '">查询当前券</button>';
    bodyHtml += '</div>';
    if (selectedIssue) {
      bodyHtml += '<div class="manager-coupon-detail-grid">';
      bodyHtml += renderDetailItem('券号', selectedIssue.couponNo);
      bodyHtml += renderDetailItem('状态', formatCouponIssueStatus(selectedIssue.status));
      bodyHtml += renderDetailItem('用户ID', selectedIssue.userId);
      bodyHtml += renderDetailItem('模板ID', selectedIssue.templateId);
      bodyHtml += renderDetailItem('金额', formatMoney(selectedIssue.couponAmount));
      bodyHtml += renderDetailItem('渠道', formatCouponClaimChannel(selectedIssue.claimChannel));
      bodyHtml += renderDetailItem('业务单号', selectedIssue.businessNo || '-');
      bodyHtml += renderDetailItem('订单号', selectedIssue.orderNo || '-');
      bodyHtml += renderDetailItem('领取时间', formatDateTime(selectedIssue.claimedAt));
      bodyHtml += renderDetailItem('过期时间', formatDateTime(selectedIssue.expireAt));
      bodyHtml += renderDetailItem('使用时间', formatDateTime(selectedIssue.usedAt));
      bodyHtml += '</div>';
    } else {
      bodyHtml += '<div class="manager-coupon-empty-row">暂无券详情，请在发放记录页点击“详情”，或在此处输入券号查询。</div>';
    }
    return renderManagerModal({
      title: '券详情查询',
      description: '券详情查看迁移到弹窗，主页面不再展示整块详情表单。',
      closeAction: 'hide-coupon-issue-detail',
      bodyHtml: bodyHtml,
      panelClass: 'manager-user-detail-modal',
    });
  }

  function renderField(label, controlHtml) {
    var html = '';
    html += '<label class="manager-coupon-field">';
    html += '<span class="manager-coupon-label">' + escapeHtml(label) + '</span>';
    html += controlHtml;
    html += '</label>';
    return html;
  }

  function renderManagerModal(options) {
    if (!options) {
      return '';
    }
    var panelClass = options.panelClass ? ' ' + String(options.panelClass) : '';
    var html = '';
    html += '<div class="manager-modal-layer">';
    html += '<div class="manager-modal-mask" data-action="' + escapeHtml(options.closeAction || '') + '"></div>';
    html += '<section class="manager-modal-panel' + escapeHtml(panelClass) + '">';
    html += '<div class="manager-modal-head">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">' + escapeHtml(options.title || '') + '</h3>';
    if (options.description) {
      html += '<div class="manager-rbac-hint">' + escapeHtml(options.description) + '</div>';
    }
    html += '</div>';
    html += '<button class="manager-modal-close" type="button" data-action="' + escapeHtml(options.closeAction || '') + '" aria-label="关闭">×</button>';
    html += '</div>';
    html += options.bodyHtml || '';
    html += '</section>';
    html += '</div>';
    return html;
  }

  function renderDetailItem(label, value) {
    return (
      '<div class="manager-coupon-detail-item">' +
      '<span class="manager-coupon-detail-key">' + escapeHtml(label) + '</span>' +
      '<span class="manager-coupon-detail-value">' + escapeHtml(value) + '</span>' +
      '</div>'
    );
  }

  function renderDetailItemHtml(label, htmlValue) {
    return (
      '<div class="manager-coupon-detail-item">' +
      '<span class="manager-coupon-detail-key">' + escapeHtml(label) + '</span>' +
      '<span class="manager-coupon-detail-value">' + (htmlValue || '-') + '</span>' +
      '</div>'
    );
  }

  function formatJsonText(raw) {
    var normalized = String(raw == null ? '' : raw).trim();
    if (!normalized) {
      return '';
    }
    try {
      return JSON.stringify(JSON.parse(normalized), null, 2);
    } catch (error) {
      return normalized;
    }
  }

  function renderCodeBlock(raw, emptyText) {
    var content = formatJsonText(raw);
    if (!content) {
      return '<div class="manager-coupon-empty-row">' + escapeHtml(emptyText || langText('暂无内容', 'No content')) + '</div>';
    }
    return '<pre class="manager-code-block">' + escapeHtml(content) + '</pre>';
  }

  function isManagerDataLoading(loaded, loading, notice) {
    return !!loading || (!loaded && !notice);
  }

  function renderManagerTableStateRow(colspan, isLoading, loadingText, emptyText) {
    return '<tr><td colspan="' + String(colspan) + '" class="manager-coupon-empty-row">' +
      escapeHtml(isLoading ? loadingText : emptyText) +
      '</td></tr>';
  }

  function renderManagerBlockState(isLoading, loadingText, emptyText, className) {
    return '<div class="' + escapeHtml(className || 'manager-rbac-empty') + '">' +
      escapeHtml(isLoading ? loadingText : emptyText) +
      '</div>';
  }

  function buildSubjectDisplayText(subjectCode, subjectName) {
    var code = String(subjectCode || '').trim();
    var name = String(subjectName || '').trim();
    if (code && name) {
      return code + ' / ' + name;
    }
    return code || name || '-';
  }

  function renderSceneOptions(selected, withAll) {
    var options = [
      'SOCIAL_GIFT',
      'NEW_USER_ACQUISITION',
      'USER_ACTIVATION',
      'PAYMENT_INCENTIVE',
      'MERCHANT_MARKETING',
      'FESTIVAL_CAMPAIGN',
      'ENTERPRISE_GRANT',
      'SERVICE_COMPENSATION',
    ];
    var html = '';
    if (withAll) {
      html += '<option value="">全部</option>';
    }
    html += options
      .map(function (scene) {
        var isSelected = String(selected || '') === scene ? ' selected' : '';
        return '<option value="' + scene + '"' + isSelected + '>' + escapeHtml(formatCouponSceneType(scene)) + '</option>';
      })
      .join('');
    return html;
  }

  function formatCouponSceneType(sceneType) {
    var normalized = String(sceneType || '').trim().toUpperCase();
    if (normalized === 'SOCIAL_GIFT') {
      return localizeText('社交赠礼');
    }
    if (normalized === 'NEW_USER_ACQUISITION') {
      return localizeText('拉新激励');
    }
    if (normalized === 'USER_ACTIVATION') {
      return localizeText('用户活跃');
    }
    if (normalized === 'PAYMENT_INCENTIVE') {
      return localizeText('支付激励');
    }
    if (normalized === 'MERCHANT_MARKETING') {
      return localizeText('商家营销');
    }
    if (normalized === 'FESTIVAL_CAMPAIGN') {
      return localizeText('节日活动');
    }
    if (normalized === 'ENTERPRISE_GRANT') {
      return localizeText('企业发放');
    }
    if (normalized === 'SERVICE_COMPENSATION') {
      return localizeText('服务补偿');
    }
    return normalized || '-';
  }

  function normalizeEnumValue(value) {
    return String(value == null ? '' : value).trim().toUpperCase();
  }

  function formatEnumText(value, mapping) {
    var normalized = normalizeEnumValue(value);
    if (!normalized) {
      return '-';
    }
    return mapping[normalized] || normalized;
  }

  function formatGenericStatusText(status) {
    return formatEnumText(status, {
      DRAFT: langText('草稿', 'Draft'),
      ACTIVE: langText('启用', 'Active'),
      ENABLED: langText('启用', 'Enabled'),
      DISABLED: langText('停用', 'Disabled'),
      PAUSED: langText('暂停', 'Paused'),
      EXPIRED: langText('已过期', 'Expired'),
      ARCHIVED: langText('已归档', 'Archived'),
      UNUSED: langText('未使用', 'Unused'),
      USED: langText('已使用', 'Used'),
      FROZEN: langText('冻结', 'Frozen'),
      PROCESSING: langText('处理中', 'Processing'),
      POSTED: langText('已过账', 'Posted'),
      REVERSED: langText('已冲正', 'Reversed'),
      CREATED: langText('已创建', 'Created'),
      PUBLISHED: langText('已发布', 'Published'),
      OFFLINE: langText('已下线', 'Offline'),
      EDITING: langText('编辑中', 'Editing'),
      SUBMITTED: langText('已提交', 'Submitted'),
      REVIEWING: langText('审核中', 'Reviewing'),
      REJECTED: langText('已驳回', 'Rejected'),
      RUNNING: langText('执行中', 'Running'),
      SKIPPED: langText('已跳过', 'Skipped'),
      SUCCESS: langText('成功', 'Success'),
      SUCCEEDED: langText('成功', 'Succeeded'),
      FAILED: langText('失败', 'Failed'),
      CLOSED: langText('已关闭', 'Closed'),
      CANCELED: langText('已取消', 'Canceled'),
      INIT: langText('初始化', 'Init'),
      ACCEPTED: langText('已受理', 'Accepted'),
      SETTLING: langText('清算中', 'Settling'),
      SETTLED: langText('已清算', 'Settled'),
      INACTIVE: langText('停用', 'Inactive'),
      INSTALLED: langText('已安装', 'Installed'),
      UNINSTALLED: langText('已卸载', 'Uninstalled'),
    });
  }

  function formatCouponTemplateStatus(status) {
    return formatEnumText(status, {
      DRAFT: langText('草稿', 'Draft'),
      ACTIVE: langText('启用', 'Active'),
      PAUSED: langText('暂停', 'Paused'),
      EXPIRED: langText('已过期', 'Expired'),
    });
  }

  function formatCouponIssueStatus(status) {
    return formatEnumText(status, {
      UNUSED: langText('未使用', 'Unused'),
      USED: langText('已使用', 'Used'),
      EXPIRED: langText('已过期', 'Expired'),
      FROZEN: langText('已冻结', 'Frozen'),
    });
  }

  function formatCouponClaimChannel(channel) {
    return formatEnumText(channel, {
      COUPON_CENTER: langText('红包中心', 'Coupon Center'),
      APP_ACTIVITY: langText('活动投放', 'App Activity'),
      DELIVER: langText('投放系统', 'Deliver'),
      RED_PACKET: langText('红包消息', 'Red Packet'),
      MARKETING: langText('营销活动', 'Marketing'),
      SYSTEM: langText('系统发放', 'System'),
    });
  }

  function formatAppStatus(status) {
    return formatEnumText(status, {
      ENABLED: langText('启用', 'Enabled'),
      DISABLED: langText('停用', 'Disabled'),
    });
  }

  function formatAppVersionStatus(status) {
    return formatEnumText(status, {
      DRAFT: langText('草稿', 'Draft'),
      ENABLED: langText('启用', 'Enabled'),
      DISABLED: langText('停用', 'Disabled'),
      ARCHIVED: langText('已归档', 'Archived'),
    });
  }

  function formatAppUpdateType(updateType) {
    return formatEnumText(updateType, {
      OPTIONAL: langText('可选更新', 'Optional'),
      RECOMMENDED: langText('推荐更新', 'Recommended'),
      FORCE: langText('强制更新', 'Force'),
    });
  }

  function formatAppUpdatePromptFrequency(promptFrequency) {
    return formatEnumText(promptFrequency, {
      ALWAYS: langText('每次打开都提示', 'Always'),
      DAILY: langText('每天提示一次', 'Daily'),
      ONCE_PER_VERSION: langText('每个版本提示一次', 'Once per version'),
      SILENT: langText('静默不提示', 'Silent'),
    });
  }

  function formatAppReleaseStatus(status) {
    return formatEnumText(status, {
      DRAFT: langText('草稿', 'Draft'),
      REVIEWING: langText('审核中', 'Reviewing'),
      PUBLISHED: langText('已发布', 'Published'),
      REJECTED: langText('已驳回', 'Rejected'),
      OFFLINE: langText('已下线', 'Offline'),
    });
  }

  function formatAppDeviceStatus(status) {
    return formatEnumText(status, {
      INSTALLED: langText('已安装', 'Installed'),
      ACTIVE: langText('活跃', 'Active'),
      INACTIVE: langText('未活跃', 'Inactive'),
      UNINSTALLED: langText('已卸载', 'Uninstalled'),
    });
  }

  function formatNetworkType(networkType) {
    return formatEnumText(networkType, {
      WIFI: langText('无线网络', 'Wi-Fi'),
      CELLULAR: langText('蜂窝网络', 'Cellular'),
      MOBILE: langText('移动网络', 'Mobile'),
      '4G': langText('4G', '4G'),
      '5G': langText('5G', '5G'),
      ETHERNET: langText('有线网络', 'Ethernet'),
      UNKNOWN: langText('未知', 'Unknown'),
    });
  }

  function formatTradeBusinessDomain(code) {
    return formatEnumText(code, {
      TRADE: langText('统一交易', 'Trade'),
      WALLET: langText('余额', 'Wallet'),
      AICREDIT: langText('爱花', 'AiCredit'),
      AILOAN: langText('爱借', 'AiLoan'),
      AICREDIT: langText('爱花', 'AiCredit'),
      AILOAN: langText('爱借', 'AiLoan'),
      AICASH: langText('爱存', 'AiCash'),
      AICASH: langText('爱存', 'AiCash'),
      RED_PACKET: langText('红包', 'Red Packet'),
      INBOUND: langText('入金', 'Inbound'),
      OUTBOUND: langText('出金', 'Outbound'),
    });
  }

  function formatTradeType(type) {
    return formatEnumText(type, {
      DEPOSIT: langText('入金', 'Deposit'),
      WITHDRAW: langText('提现', 'Withdraw'),
      PAY: langText('支付', 'Pay'),
      PAYMENT: langText('支付', 'Payment'),
      TRANSFER: langText('转账', 'Transfer'),
      REFUND: langText('退款', 'Refund'),
    });
  }

  function formatBusinessSceneCode(code) {
    var normalized = normalizeEnumValue(code);
    if (!normalized) {
      return '-';
    }
    if (normalized === 'TRANSFER' || normalized.indexOf('TRANSFER') >= 0) {
      return langText('转账', 'Transfer');
    }
    if (normalized === 'PAY' || normalized === 'PAYMENT' || (normalized.indexOf('PAY') >= 0 && normalized.indexOf('REPAY') < 0)) {
      return langText('支付', 'Payment');
    }
    if (normalized.indexOf('REPAY') >= 0) {
      return langText('还款', 'Repayment');
    }
    if (normalized.indexOf('SUBSCRIBE') >= 0) {
      return langText('基金申购', 'Fund Subscribe');
    }
    if (normalized.indexOf('REDEEM') >= 0) {
      return langText('基金赎回', 'Fund Redeem');
    }
    if (normalized.indexOf('RED_PACKET') >= 0) {
      return langText('红包', 'Red Packet');
    }
    if (normalized.indexOf('INBOUND') >= 0 || normalized.indexOf('DEPOSIT') >= 0) {
      return langText('入金', 'Inbound');
    }
    if (normalized.indexOf('OUTBOUND') >= 0 || normalized.indexOf('WITHDRAW') >= 0) {
      return langText('出金', 'Outbound');
    }
    return normalized;
  }

  function formatTradeStatus(status) {
    return formatEnumText(status, {
      CREATED: langText('已创建', 'Created'),
      QUOTED: langText('已报价', 'Quoted'),
      PAY_SUBMITTED: langText('已提交支付', 'Pay Submitted'),
      PAY_PROCESSING: langText('支付处理中', 'Pay Processing'),
      PAY_PREPARING: langText('支付预处理中', 'Pay Preparing'),
      PAY_PREPARED: langText('支付预处理完成', 'Pay Prepared'),
      PAY_COMMITTING: langText('支付提交中', 'Pay Committing'),
      SUCCEEDED: langText('已成功', 'Succeeded'),
      PAY_ROLLING_BACK: langText('支付回滚中', 'Pay Rolling Back'),
      ROLLED_BACK: langText('已回滚', 'Rolled Back'),
      FAILED: langText('已失败', 'Failed'),
      RECON_PENDING: langText('待对账', 'Recon Pending'),
      SUCCESS: langText('已成功', 'Success'),
    });
  }

  function formatTradeStepStatus(status) {
    return formatEnumText(status, {
      RUNNING: langText('执行中', 'Running'),
      SUCCESS: langText('成功', 'Success'),
      FAILED: langText('失败', 'Failed'),
      SKIPPED: langText('已跳过', 'Skipped'),
    });
  }

  function formatPayStatus(status) {
    return formatEnumText(status, {
      CREATED: langText('已创建', 'Created'),
      SUBMITTED: langText('已提交', 'Submitted'),
      TRYING: langText('预处理执行中', 'Trying'),
      PREPARED: langText('预处理完成', 'Prepared'),
      COMMITTING: langText('提交中', 'Committing'),
      COMMITTED: langText('已成功', 'Committed'),
      RECON_PENDING: langText('待对账', 'Recon Pending'),
      ROLLING_BACK: langText('回滚中', 'Rolling Back'),
      ROLLED_BACK: langText('已回滚', 'Rolled Back'),
      FAILED: langText('已失败', 'Failed'),
    });
  }

  function formatPayParticipantType(type) {
    return formatEnumText(type, {
      COUPON: langText('红包券', 'Coupon'),
      WALLET_ACCOUNT: langText('钱包账户', 'Wallet Account'),
      FUND_ACCOUNT: langText('基金账户', 'Fund Account'),
      CREDIT_ACCOUNT: langText('信用账户', 'Credit Account'),
      INBOUND: langText('入金网关', 'Inbound'),
      OUTBOUND: langText('出金网关', 'Outbound'),
    });
  }

  function formatPayParticipantStatus(status) {
    return formatEnumText(status, {
      INIT: langText('待执行', 'Init'),
      TRY_OK: langText('预处理成功', 'Try OK'),
      TRY_FAILED: langText('预处理失败', 'Try Failed'),
      CONFIRM_OK: langText('确认成功', 'Confirm OK'),
      CANCEL_OK: langText('取消成功', 'Cancel OK'),
      SKIPPED: langText('已跳过', 'Skipped'),
    });
  }

  function formatPaymentMethod(method) {
    var normalized = normalizeEnumValue(method);
    if (!normalized) {
      return '-';
    }
    if (normalized === 'WALLET' || normalized === 'BALANCE' || normalized.indexOf('WALLET') >= 0 || normalized.indexOf('BALANCE') >= 0) {
      return langText('余额', 'Wallet');
    }
    if (normalized === 'BANK_CARD' || normalized === 'CARD' || normalized.indexOf('CARD') >= 0) {
      return langText('银行卡', 'Bank Card');
    }
    if (normalized.indexOf('AICASH') >= 0 || normalized.indexOf('AICASH') >= 0 || normalized.indexOf('FUND') >= 0) {
      return langText('爱存', 'AiCash');
    }
    if (normalized.indexOf('AICREDIT') >= 0) {
      return langText('爱花', 'AiCredit');
    }
    if (normalized.indexOf('CREDIT') >= 0) {
      return langText('爱花', 'AiCredit');
    }
    if (normalized.indexOf('AILOAN') >= 0 || normalized.indexOf('LOAN') >= 0) {
      return langText('爱借', 'AiLoan');
    }
    if (normalized.indexOf('RED_PACKET') >= 0) {
      return langText('红包', 'Red Packet');
    }
    if (normalized.indexOf('CASHIER') >= 0) {
      return langText('收银台', 'Cashier');
    }
    return normalized;
  }

  function formatAccountingEventStatus(status) {
    return formatEnumText(status, {
      NEW: langText('待处理', 'New'),
      PROCESSING: langText('处理中', 'Processing'),
      POSTED: langText('已过账', 'Posted'),
      FAILED: langText('失败', 'Failed'),
      REVERSED: langText('已冲正', 'Reversed'),
    });
  }

  function formatAccountingVoucherStatus(status) {
    return formatEnumText(status, {
      CREATED: langText('已创建', 'Created'),
      POSTED: langText('已过账', 'Posted'),
      REVERSED: langText('已冲正', 'Reversed'),
    });
  }

  function formatAccountingVoucherType(type) {
    return formatEnumText(type, {
      NORMAL: langText('普通凭证', 'Normal'),
      REVERSE: langText('冲正凭证', 'Reverse'),
      ADJUSTMENT: langText('调整凭证', 'Adjustment'),
    });
  }

  function formatAccountingBizType(type) {
    var normalized = normalizeEnumValue(type);
    if (!normalized) {
      return '-';
    }
    if (normalized.indexOf('INBOUND') >= 0) {
      return langText('入金订单', 'Inbound Order');
    }
    if (normalized.indexOf('OUTBOUND') >= 0) {
      return langText('出金订单', 'Outbound Order');
    }
    if (normalized.indexOf('RED_PACKET') >= 0) {
      return langText('红包', 'Red Packet');
    }
    if (normalized.indexOf('COUPON') >= 0) {
      return langText('红包券', 'Coupon');
    }
    if (normalized.indexOf('TRANSFER') >= 0) {
      return langText('转账', 'Transfer');
    }
    if (normalized.indexOf('PAY') >= 0) {
      return langText('支付', 'Payment');
    }
    if (normalized.indexOf('REPAY') >= 0) {
      return langText('还款', 'Repayment');
    }
    return normalized;
  }

  function formatAccountingSubjectType(type) {
    return formatEnumText(type, {
      ASSET: langText('资产', 'Asset'),
      LIABILITY: langText('负债', 'Liability'),
      EQUITY: langText('权益', 'Equity'),
      INCOME: langText('收入', 'Income'),
      EXPENSE: langText('费用', 'Expense'),
      MEMO: langText('备查', 'Memo'),
    });
  }

  function formatAccountingOwnerType(type) {
    var normalized = normalizeEnumValue(type);
    if (!normalized) {
      return '-';
    }
    if (normalized.indexOf('USER') >= 0) {
      return langText('用户', 'User');
    }
    if (normalized.indexOf('MERCHANT') >= 0) {
      return langText('商户', 'Merchant');
    }
    if (normalized.indexOf('PLATFORM') >= 0) {
      return langText('平台', 'Platform');
    }
    if (normalized.indexOf('CHANNEL') >= 0) {
      return langText('渠道', 'Channel');
    }
    if (normalized.indexOf('SYSTEM') >= 0) {
      return langText('系统', 'System');
    }
    return normalized;
  }

  function formatAccountingAccountType(type) {
    var normalized = normalizeEnumValue(type);
    if (!normalized) {
      return '-';
    }
    if (normalized.indexOf('WALLET') >= 0) {
      return langText('钱包账户', 'Wallet Account');
    }
    if (normalized.indexOf('FUND') >= 0) {
      return langText('基金账户', 'Fund Account');
    }
    if (normalized.indexOf('CREDIT') >= 0) {
      return langText('爱花账户', 'AiCredit Account');
    }
    if (normalized.indexOf('LOAN') >= 0) {
      return langText('爱借账户', 'AiLoan Account');
    }
    if (normalized.indexOf('MERCHANT') >= 0) {
      return langText('商户账户', 'Merchant Account');
    }
    if (normalized.indexOf('INTERNAL') >= 0) {
      return langText('内部账户', 'Internal Account');
    }
    return normalized;
  }

  function formatAccountingDirection(direction) {
    return formatEnumText(direction, {
      IN: langText('流入', 'In'),
      OUT: langText('流出', 'Out'),
      DEBIT: langText('借', 'Debit'),
      CREDIT: langText('贷', 'Credit'),
    });
  }

  function formatAccountingDebitCreditFlag(flag) {
    return formatEnumText(flag, {
      DEBIT: langText('借', 'Debit'),
      CREDIT: langText('贷', 'Credit'),
    });
  }

  function formatAccountingBalanceDirection(direction) {
    return formatEnumText(direction, {
      DEBIT: langText('借方', 'Debit'),
      CREDIT: langText('贷方', 'Credit'),
      IN: langText('流入', 'In'),
      OUT: langText('流出', 'Out'),
    });
  }

  function formatPricingStatus(status) {
    return formatEnumText(status, {
      DRAFT: langText('草稿', 'Draft'),
      ACTIVE: langText('启用', 'Active'),
      INACTIVE: langText('停用', 'Inactive'),
    });
  }

  function formatPricingFeeMode(mode) {
    return formatEnumText(mode, {
      RATE: langText('按费率', 'Rate'),
      FIXED: langText('固定金额', 'Fixed'),
      RATE_PLUS_FIXED: langText('费率+固定额', 'Rate + Fixed'),
    });
  }

  function formatPricingFeeBearer(bearer) {
    return formatEnumText(bearer, {
      PAYER: langText('付款方', 'Payer'),
      PAYEE: langText('收款方', 'Payee'),
      PLATFORM: langText('平台', 'Platform'),
    });
  }

  function formatUserKycLevel(level) {
    return formatEnumText(level, {
      L0: langText('L0 未实名', 'L0 Unverified'),
      L1: langText('L1 基础实名', 'L1 Basic KYC'),
      L2: langText('L2 增强实名', 'L2 Enhanced KYC'),
      L3: langText('L3 高级实名', 'L3 Advanced KYC'),
    });
  }

  function formatRiskLevel(level) {
    return formatEnumText(level, {
      LOW: langText('低风险', 'Low Risk'),
      MEDIUM: langText('中风险', 'Medium Risk'),
      HIGH: langText('高风险', 'High Risk'),
      SAFE: langText('安全', 'Safe'),
    });
  }

  function formatTwoFactorMode(mode) {
    return formatEnumText(mode, {
      NONE: langText('未开启', 'None'),
      SMS: langText('短信验证', 'SMS'),
      APP: langText('验证器', 'Authenticator'),
      BIOMETRIC: langText('生物识别', 'Biometric'),
    });
  }

  function formatAdminAccountStatus(status) {
    return formatEnumText(status, {
      ACTIVE: langText('正常', 'Active'),
      FROZEN: langText('冻结', 'Frozen'),
      CLOSED: langText('关闭', 'Closed'),
      DISABLED: langText('停用', 'Disabled'),
    });
  }

  function renderTemplateStatusOptions(selected, withAll) {
    var statuses = ['DRAFT', 'ACTIVE', 'PAUSED', 'EXPIRED'];
    var html = '';
    if (withAll) {
      html += '<option value="">全部</option>';
    }
    html += statuses
      .map(function (status) {
        var isSelected = String(selected || '') === status ? ' selected' : '';
        return '<option value="' + status + '"' + isSelected + '>' + formatCouponTemplateStatus(status) + '</option>';
      })
      .join('');
    return html;
  }

  function renderIssueStatusOptions(selected, withAll) {
    var statuses = ['UNUSED', 'USED', 'EXPIRED', 'FROZEN'];
    var html = '';
    if (withAll) {
      html += '<option value="">全部</option>';
    }
    html += statuses
      .map(function (status) {
        var isSelected = String(selected || '') === status ? ' selected' : '';
        return '<option value="' + status + '"' + isSelected + '>' + formatCouponIssueStatus(status) + '</option>';
      })
      .join('');
    return html;
  }

  function renderOutboxStatusOptions(selected, withAll) {
    var statuses = ['PENDING', 'PROCESSING', 'SUCCEEDED', 'DEAD'];
    var html = '';
    if (withAll) {
      html += '<option value="">全部</option>';
    }
    html += statuses
      .map(function (status) {
        var isSelected = String(selected || '').toUpperCase() === status ? ' selected' : '';
        return '<option value="' + status + '"' + isSelected + '>' + formatGenericStatusText(status) + '</option>';
      })
      .join('');
    return html;
  }

  function renderAuditMethodOptions(selected, withAll) {
    var methods = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'];
    var html = '';
    if (withAll) {
      html += '<option value="">全部</option>';
    }
    html += methods
      .map(function (method) {
        var isSelected = String(selected || '').toUpperCase() === method ? ' selected' : '';
        return '<option value="' + method + '"' + isSelected + '>' + method + '</option>';
      })
      .join('');
    return html;
  }

  function renderAuditResultOptions(selected, withAll) {
    var options = ['SUCCESS', 'FAILED'];
    var html = '';
    if (withAll) {
      html += '<option value="">全部</option>';
    }
    html += options
      .map(function (status) {
        var isSelected = String(selected || '').toUpperCase() === status ? ' selected' : '';
        return '<option value="' + status + '"' + isSelected + '>' + formatGenericStatusText(status) + '</option>';
      })
      .join('');
    return html;
  }

  function formatOutboxPayloadPreview(raw, maxLength) {
    var normalized = String(raw == null ? '' : raw).trim();
    if (!normalized) {
      return '-';
    }
    var limit = Number(maxLength);
    if (Number.isFinite(limit) && limit > 0 && normalized.length > limit) {
      return normalized.slice(0, limit) + '...';
    }
    return normalized;
  }

  function formatDecimalText(value, scale) {
    var number = Number(value);
    if (!Number.isFinite(number)) {
      return '-';
    }
    var digits = Number.isFinite(scale) ? Math.max(0, Math.min(6, Math.floor(scale))) : 2;
    return number.toFixed(digits);
  }

  function formatAuditRequestPath(row) {
    if (!row) {
      return '-';
    }
    var method = String(row.requestMethod || '').toUpperCase();
    var path = String(row.requestPath || '').trim();
    var query = String(row.requestQuery || '').trim();
    if (query) {
      path += query.charAt(0) === '?' ? query : ('?' + query);
    }
    if (!path) {
      path = '-';
    }
    if (!method) {
      return path;
    }
    return method + ' ' + path;
  }

  function renderStatusBadge(status, displayText) {
    var text = String(displayText == null ? formatGenericStatusText(status) : displayText);
    var statusCode = String(status || '-').toLowerCase();
    return '<span class="manager-coupon-status status-' + escapeHtml(statusCode) + '">' + escapeHtml(text) + '</span>';
  }

  function renderBooleanBadge(enabled, enabledText, disabledText) {
    return renderStatusBadge(
      enabled ? 'ENABLED' : 'DISABLED',
      enabled ? (enabledText || langText('开启', 'Enabled')) : (disabledText || langText('关闭', 'Disabled'))
    );
  }

  function renderTemplateAmount(template) {
    if (!template) {
      return '-';
    }
    if (template.valueType === 'RANDOM') {
      return formatMoney(template.minAmount) + ' ~ ' + formatMoney(template.maxAmount);
    }
    return formatMoney(template.amount);
  }

  function renderTemplateActions(template) {
    var actions = [];
    var templateId = template.templateId;
    var status = String(template.status || '');
    if (status !== 'ACTIVE') {
      actions.push(
        '<button class="manager-btn" type="button" data-action="change-coupon-template-status" data-template-id="' +
          escapeHtml(templateId) +
          '" data-target-status="ACTIVE">启用</button>'
      );
    }
    if (status !== 'PAUSED') {
      actions.push(
        '<button class="manager-btn" type="button" data-action="change-coupon-template-status" data-template-id="' +
          escapeHtml(templateId) +
          '" data-target-status="PAUSED">暂停</button>'
      );
    }
    if (status !== 'EXPIRED') {
      actions.push(
        '<button class="manager-btn" type="button" data-action="change-coupon-template-status" data-template-id="' +
          escapeHtml(templateId) +
          '" data-target-status="EXPIRED">过期</button>'
      );
    }
    return actions.join(' ');
  }

  async function ensureOutboxData(forceReload) {
    if (state.outbox.loading) {
      return;
    }
    if (!forceReload && state.outbox.loaded) {
      return;
    }

    state.outbox.loading = true;
    state.outbox.messageLoading = true;
    state.outbox.deadLetterLoading = true;
    try {
      setOutboxNotice('正在加载消息投递数据...');
      renderContent();
      await Promise.all([loadOutboxOverview(), loadOutboxTopics(), loadOutboxMessages(), loadOutboxDeadLetters()]);
      state.outbox.loaded = true;
      setOutboxNotice('消息投递数据已加载');
    } catch (error) {
      setOutboxNotice(error && error.message ? error.message : '消息投递数据加载失败');
    } finally {
      state.outbox.messageLoading = false;
      state.outbox.deadLetterLoading = false;
      state.outbox.loading = false;
    }
  }

  async function queryOutboxMessagesFromForm() {
    try {
      state.outbox.filter.topic = readInputValue('outboxMessageTopicFilter');
      state.outbox.filter.status = readInputValue('outboxMessageStatusFilter').toUpperCase();
      state.outbox.filter.keyword = readInputValue('outboxMessageKeywordFilter');
      state.outbox.filter.limit = readInputValue('outboxMessageLimitFilter') || '20';
      state.outbox.filter.onlyRetried = !!(document.getElementById('outboxMessageOnlyRetried') && document.getElementById('outboxMessageOnlyRetried').checked);
      state.outbox.filter.includePayload = !!(document.getElementById('outboxMessageIncludePayload') && document.getElementById('outboxMessageIncludePayload').checked);
      state.outbox.messageLoading = true;
      setOutboxNotice('正在查询消息...');
      renderContent();
      await Promise.all([loadOutboxMessages(), loadOutboxOverview(), loadOutboxTopics()]);
      setOutboxNotice('消息查询完成，共 ' + String((state.outbox.messages || []).length) + ' 条');
    } catch (error) {
      setOutboxNotice(error && error.message ? error.message : '消息查询失败');
    } finally {
      state.outbox.messageLoading = false;
    }
    renderContent();
  }

  async function queryOutboxDeadLettersFromForm() {
    try {
      state.outbox.deadFilter.topic = readInputValue('outboxDeadTopicFilter');
      state.outbox.deadFilter.keyword = readInputValue('outboxDeadKeywordFilter');
      state.outbox.deadFilter.limit = readInputValue('outboxDeadLimitFilter') || '20';
      state.outbox.deadFilter.includePayload = !!(document.getElementById('outboxDeadIncludePayload') && document.getElementById('outboxDeadIncludePayload').checked);
      state.outbox.deadLetterLoading = true;
      setOutboxNotice('正在查询死信...');
      renderContent();
      await Promise.all([loadOutboxDeadLetters(), loadOutboxOverview()]);
      setOutboxNotice('死信查询完成，共 ' + String((state.outbox.deadLetters || []).length) + ' 条');
    } catch (error) {
      setOutboxNotice(error && error.message ? error.message : '死信查询失败');
    } finally {
      state.outbox.deadLetterLoading = false;
    }
    renderContent();
  }

  async function requeueOutboxDeadLetter(messageId) {
    var normalizedMessageId = normalizePositiveIdText(messageId);
    if (!normalizedMessageId) {
      setOutboxNotice('消息ID不能为空');
      renderContent();
      return;
    }
    try {
      var nextRetryAtText = readInputValue('outboxRequeueNextRetryAt');
      state.outbox.requeue.nextRetryAt = nextRetryAtText;
      var nextRetryAt = normalizeDateTimeInputOptional(nextRetryAtText);
      setOutboxNotice('正在重放死信 #' + normalizedMessageId + ' ...');
      renderContent();
      var response = await requestJson('/api/admin/outbox/dead-letters/' + encodeURIComponent(normalizedMessageId) + '/requeue', {
        method: 'POST',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          nextRetryAt: nextRetryAt,
        }),
      });
      await Promise.all([loadOutboxOverview(), loadOutboxTopics(), loadOutboxMessages(), loadOutboxDeadLetters()]);
      var requeuedCount = response && response.requeuedCount != null ? response.requeuedCount : 0;
      setOutboxNotice('死信重放完成，成功 ' + String(requeuedCount) + ' 条');
    } catch (error) {
      setOutboxNotice(error && error.message ? error.message : '死信重放失败');
    }
    renderContent();
  }

  async function requeueOutboxDeadLettersBatch() {
    try {
      state.outbox.requeue.topic = readInputValue('outboxRequeueTopic');
      state.outbox.requeue.limit = readInputValue('outboxRequeueLimit') || '20';
      state.outbox.requeue.nextRetryAt = readInputValue('outboxRequeueNextRetryAt');
      var limit = parsePositiveInteger(String(state.outbox.requeue.limit || '20'), '批量上限');
      var nextRetryAt = normalizeDateTimeInputOptional(state.outbox.requeue.nextRetryAt);
      setOutboxNotice('正在批量重放死信...');
      renderContent();
      var response = await requestJson('/api/admin/outbox/dead-letters/requeue', {
        method: 'POST',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          topic: state.outbox.requeue.topic || null,
          limit: limit,
          nextRetryAt: nextRetryAt,
        }),
      });
      await Promise.all([loadOutboxOverview(), loadOutboxTopics(), loadOutboxMessages(), loadOutboxDeadLetters()]);
      var count = response && response.requeuedCount != null ? response.requeuedCount : 0;
      setOutboxNotice('批量重放完成，成功 ' + String(count) + ' 条');
    } catch (error) {
      setOutboxNotice(error && error.message ? error.message : '批量重放失败');
    }
    renderContent();
  }

  async function loadOutboxOverview() {
    state.outbox.overview = await requestJson('/api/admin/outbox/overview', {
      method: 'GET',
      headers: buildAdminHeaders(),
    });
  }

  async function loadOutboxTopics() {
    var limit = parsePositiveInteger(String((state.outbox.filter && state.outbox.filter.limit) || '20'), '返回条数');
    var query = buildQueryString({
      limit: String(limit),
    });
    state.outbox.topics = await requestJson('/api/admin/outbox/topics' + query, {
      method: 'GET',
      headers: buildAdminHeaders(),
    });
  }

  async function loadOutboxMessages() {
    var filter = state.outbox.filter || {};
    var limit = parsePositiveInteger(String(filter.limit || '20'), '返回条数');
    var query = buildQueryString({
      topic: filter.topic || '',
      status: (filter.status || '').toUpperCase(),
      keyword: filter.keyword || '',
      onlyRetried: filter.onlyRetried ? 'true' : '',
      limit: String(limit),
      includePayload: filter.includePayload ? 'true' : '',
    });
    try {
      state.outbox.messageLoading = true;
      state.outbox.messages = await requestJson('/api/admin/outbox/messages' + query, {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
    } finally {
      state.outbox.messageLoading = false;
    }
  }

  async function loadOutboxDeadLetters() {
    var deadFilter = state.outbox.deadFilter || {};
    var limit = parsePositiveInteger(String(deadFilter.limit || '20'), '返回条数');
    var query = buildQueryString({
      topic: deadFilter.topic || '',
      keyword: deadFilter.keyword || '',
      limit: String(limit),
      includePayload: deadFilter.includePayload ? 'true' : '',
    });
    try {
      state.outbox.deadLetterLoading = true;
      state.outbox.deadLetters = await requestJson('/api/admin/outbox/dead-letters' + query, {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
    } finally {
      state.outbox.deadLetterLoading = false;
    }
  }

  async function ensureObservabilityData(forceReload) {
    if (state.observability.loading) {
      return;
    }
    if (!forceReload && state.observability.loaded) {
      return;
    }

    state.observability.loading = true;
    state.observability.sceneLoading = true;
    try {
      setObservabilityNotice('正在加载可观测指标...');
      renderContent();
      await Promise.all([loadObservabilityOverview(), loadObservabilityScenes()]);
      state.observability.loaded = true;
      setObservabilityNotice('可观测指标已加载');
    } catch (error) {
      setObservabilityNotice(error && error.message ? error.message : '可观测指标加载失败');
    } finally {
      state.observability.sceneLoading = false;
      state.observability.loading = false;
    }
  }

  async function queryObservabilityScenesFromForm() {
    try {
      state.observability.filter.limit = readInputValue('observabilitySceneLimit') || '20';
      state.observability.sceneLoading = true;
      setObservabilityNotice('正在查询场景指标...');
      renderContent();
      await loadObservabilityScenes();
      setObservabilityNotice('场景指标查询完成，共 ' + String((state.observability.apiScenes || []).length) + ' 条');
    } catch (error) {
      setObservabilityNotice(error && error.message ? error.message : '场景指标查询失败');
    } finally {
      state.observability.sceneLoading = false;
    }
    renderContent();
  }

  async function loadObservabilityOverview() {
    state.observability.overview = await requestJson('/api/admin/observability/overview', {
      method: 'GET',
      headers: buildAdminHeaders(),
    });
  }

  async function loadObservabilityScenes() {
    var limit = parsePositiveInteger(String((state.observability.filter && state.observability.filter.limit) || '20'), '返回条数');
    var query = buildQueryString({
      limit: String(limit),
    });
    state.observability.apiScenes = await requestJson('/api/admin/observability/api-scenes' + query, {
      method: 'GET',
      headers: buildAdminHeaders(),
    });
  }

  async function ensureAuditData(forceReload) {
    if (state.audit.loading) {
      return;
    }
    if (!forceReload && state.audit.loaded) {
      return;
    }

    state.audit.loading = true;
    try {
      setAuditNotice('正在加载审计日志...');
      renderContent();
      await loadAudits();
      state.audit.loaded = true;
      setAuditNotice('审计日志已加载');
    } catch (error) {
      setAuditNotice(error && error.message ? error.message : '审计日志加载失败');
    } finally {
      state.audit.loading = false;
    }
  }

  async function queryAuditsFromForm() {
    try {
      state.audit.filter.adminId = readInputValue('auditFilterAdminId');
      state.audit.filter.requestMethod = readInputValue('auditFilterMethod').toUpperCase();
      state.audit.filter.requestPath = readInputValue('auditFilterRequestPath');
      state.audit.filter.resultStatus = readInputValue('auditFilterResultStatus').toUpperCase();
      state.audit.filter.from = readInputValue('auditFilterFrom');
      state.audit.filter.to = readInputValue('auditFilterTo');
      state.audit.filter.limit = readInputValue('auditFilterLimit') || '20';
      state.audit.loading = true;
      setAuditNotice('正在查询审计日志...');
      renderContent();
      await loadAudits();
      setAuditNotice('审计日志查询完成，共 ' + String((state.audit.rows || []).length) + ' 条');
    } catch (error) {
      setAuditNotice(error && error.message ? error.message : '审计日志查询失败');
    } finally {
      state.audit.loading = false;
    }
    renderContent();
  }

  async function loadAudits() {
    var filter = state.audit.filter || {};
    var limit = parsePositiveInteger(String(filter.limit || '20'), '返回条数');
    var query = buildQueryString({
      adminId: toPositiveDigitsOrEmpty(filter.adminId),
      requestMethod: (filter.requestMethod || '').toUpperCase(),
      requestPath: filter.requestPath || '',
      resultStatus: (filter.resultStatus || '').toUpperCase(),
      from: normalizeDateTimeInputOptional(filter.from),
      to: normalizeDateTimeInputOptional(filter.to),
      limit: String(limit),
    });
    state.audit.rows = await requestJson('/api/admin/audits' + query, {
      method: 'GET',
      headers: buildAdminHeaders(),
    });
  }

  async function ensureCouponData(forceReload) {
    if (state.coupon.loading) {
      return;
    }
    if (!forceReload && state.coupon.loaded) {
      return;
    }

    state.coupon.loading = true;
    state.coupon.templatesLoading = true;
    state.coupon.issuesLoading = true;
    try {
      setCouponNotice('正在加载红包数据...');
      renderContent();
      await Promise.all([loadCouponSummary(), loadCouponTemplates(), loadCouponIssues()]);
      state.coupon.loaded = true;
      setCouponNotice('红包数据已加载');
    } catch (error) {
      setCouponNotice(error && error.message ? error.message : '红包数据加载失败');
    } finally {
      state.coupon.templatesLoading = false;
      state.coupon.issuesLoading = false;
      state.coupon.loading = false;
    }
  }

  async function queryCouponTemplatesFromForm() {
    state.coupon.templateFilter.sceneType = readInputValue('couponTemplateSceneFilter').toUpperCase();
    state.coupon.templateFilter.status = readInputValue('couponTemplateStatusFilter').toUpperCase();
    try {
      state.coupon.templatesLoading = true;
      setCouponNotice('正在查询模板...');
      renderContent();
      await Promise.all([loadCouponTemplates(), loadCouponSummary()]);
      setCouponNotice('模板查询完成，共 ' + String((state.coupon.templates || []).length) + ' 条');
    } catch (error) {
      setCouponNotice(error && error.message ? error.message : '模板查询失败');
    } finally {
      state.coupon.templatesLoading = false;
    }
    renderContent();
  }

  async function createCouponTemplateFromForm() {
    try {
      var templateCode = requireText(readInputValue('couponTemplateCode'), '模板编码');
      var templateName = requireText(readInputValue('couponTemplateName'), '模板名称');
      var sceneType = requireText(readInputValue('couponTemplateSceneType').toUpperCase(), '场景类型');
      var valueType = requireText(readInputValue('couponTemplateValueType').toUpperCase(), '面额类型');
      var totalBudget = parseMoneyRequired(readInputValue('couponTemplateTotalBudget'), '总预算');
      var totalStock = parsePositiveInteger(readInputValue('couponTemplateTotalStock'), '总库存');
      var perUserLimit = parsePositiveInteger(readInputValue('couponTemplatePerUserLimit') || '1', '单用户限领');
      var claimStartTime = normalizeDateTimeInput(readInputValue('couponTemplateClaimStart'), '领取开始时间');
      var claimEndTime = normalizeDateTimeInput(readInputValue('couponTemplateClaimEnd'), '领取结束时间');
      var useStartTime = normalizeDateTimeInput(readInputValue('couponTemplateUseStart'), '使用开始时间');
      var useEndTime = normalizeDateTimeInput(readInputValue('couponTemplateUseEnd'), '使用结束时间');
      var fundingSource = requireText(readInputValue('couponTemplateFundingSource'), '资金来源');
      var initialStatus = readInputValue('couponTemplateInitialStatus').toUpperCase() || 'ACTIVE';
      var rulePayload = readInputValue('couponTemplateRulePayload');

      var amount = null;
      var minAmount = null;
      var maxAmount = null;
      if (valueType === 'FIXED') {
        amount = parseMoneyRequired(readInputValue('couponTemplateAmount'), '固定金额');
      } else {
        minAmount = parseMoneyRequired(readInputValue('couponTemplateMinAmount'), '随机最小金额');
        maxAmount = parseMoneyRequired(readInputValue('couponTemplateMaxAmount'), '随机最大金额');
      }
      var thresholdAmount = parseMoneyOptional(readInputValue('couponTemplateThresholdAmount'));

      setCouponNotice('正在创建模板...');
      renderContent();

      await requestJson('/api/admin/coupons/templates', {
        method: 'POST',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          templateCode: templateCode,
          templateName: templateName,
          sceneType: sceneType,
          valueType: valueType,
          amount: amount,
          minAmount: minAmount,
          maxAmount: maxAmount,
          thresholdAmount: thresholdAmount,
          totalBudget: totalBudget,
          totalStock: totalStock,
          perUserLimit: perUserLimit,
          claimStartTime: claimStartTime,
          claimEndTime: claimEndTime,
          useStartTime: useStartTime,
          useEndTime: useEndTime,
          fundingSource: fundingSource,
          rulePayload: rulePayload || null,
          initialStatus: initialStatus,
          operator: (state.session && state.session.username) || 'admin',
        }),
      });

      await Promise.all([loadCouponTemplates(), loadCouponSummary()]);
      state.coupon.templateCreateVisible = false;
      setCouponNotice('模板创建成功：' + templateCode);
    } catch (error) {
      setCouponNotice(error && error.message ? error.message : '模板创建失败');
    }
    renderContent();
  }

  async function changeCouponTemplateStatus(templateId, targetStatus) {
    try {
      setCouponNotice('正在变更模板状态...');
      renderContent();
      await requestJson('/api/admin/coupons/templates/' + templateId + '/status', {
        method: 'PUT',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          status: String(targetStatus || '').toUpperCase(),
          operator: (state.session && state.session.username) || 'admin',
        }),
      });
      await Promise.all([loadCouponTemplates(), loadCouponSummary()]);
      setCouponNotice('模板状态已更新为 ' + String(targetStatus).toUpperCase());
    } catch (error) {
      setCouponNotice(error && error.message ? error.message : '模板状态变更失败');
    }
    renderContent();
  }

  async function queryCouponIssuesFromForm() {
    state.coupon.issueFilter.templateId = readInputValue('couponIssueFilterTemplateId');
    state.coupon.issueFilter.userId = readInputValue('couponIssueFilterUserId');
    state.coupon.issueFilter.status = readInputValue('couponIssueFilterStatus').toUpperCase();
    state.coupon.issueFilter.limit = readInputValue('couponIssueFilterLimit') || '20';
    try {
      state.coupon.issuesLoading = true;
      setCouponNotice('正在查询发放记录...');
      renderContent();
      await loadCouponIssues();
      setCouponNotice('发放记录查询完成，共 ' + String((state.coupon.issues || []).length) + ' 条');
    } catch (error) {
      setCouponNotice(error && error.message ? error.message : '发放记录查询失败');
    } finally {
      state.coupon.issuesLoading = false;
    }
    renderContent();
  }

  async function issueCouponFromForm() {
    try {
      var templateId = parsePositiveInteger(readInputValue('couponIssueTemplateId'), '模板ID');
      var userId = parsePositiveLongText(readInputValue('couponIssueUserId'), '用户ID');
      var claimChannel = requireText(readInputValue('couponIssueClaimChannel'), '发放渠道');
      var businessNo = readInputValue('couponIssueBusinessNo');

      setCouponNotice('正在发放红包...');
      renderContent();
      var issue = await requestJson('/api/admin/coupons/issue', {
        method: 'POST',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          templateId: templateId,
          userId: userId,
          claimChannel: claimChannel,
          businessNo: businessNo || null,
          operator: (state.session && state.session.username) || 'admin',
        }),
      });
      state.coupon.selectedIssue = issue;
      state.coupon.issueCreateVisible = false;
      state.coupon.issueDetailVisible = true;
      await Promise.all([loadCouponIssues(), loadCouponTemplates(), loadCouponSummary()]);
      setCouponNotice('发放成功，券号：' + issue.couponNo);
    } catch (error) {
      setCouponNotice(error && error.message ? error.message : '发放红包失败');
    }
    renderContent();
  }

  async function redeemCouponFromForm() {
    try {
      var couponNo = requireText(readInputValue('couponRedeemCouponNo'), '券号');
      var orderNo = requireText(readInputValue('couponRedeemOrderNo'), '订单号');
      setCouponNotice('正在核销红包...');
      renderContent();
      var issue = await requestJson('/api/admin/coupons/redeem', {
        method: 'POST',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          couponNo: couponNo,
          orderNo: orderNo,
        }),
      });
      state.coupon.selectedIssue = issue;
      state.coupon.redeemVisible = false;
      state.coupon.issueDetailVisible = true;
      await loadCouponIssues();
      setCouponNotice('核销成功：' + couponNo);
    } catch (error) {
      setCouponNotice(error && error.message ? error.message : '核销失败');
    }
    renderContent();
  }

  async function loadCouponIssueDetail(couponNo, renderAfterLoad) {
    try {
      var normalizedCouponNo = requireText(String(couponNo || '').trim(), '券号');
      state.coupon.selectedIssue = await requestJson('/api/admin/coupons/issues/' + encodeURIComponent(normalizedCouponNo), {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
      state.coupon.issueDetailVisible = true;
      setCouponNotice('券详情加载成功：' + normalizedCouponNo);
    } catch (error) {
      setCouponNotice(error && error.message ? error.message : '券详情加载失败');
    }
    if (renderAfterLoad) {
      renderContent();
    }
  }

  async function loadCouponSummary() {
    state.coupon.summary = await requestJson('/api/admin/coupons/summary', {
      method: 'GET',
      headers: buildAdminHeaders(),
    });
  }

  async function loadCouponTemplates() {
    var filter = state.coupon.templateFilter || {};
    var query = buildQueryString({
      sceneType: filter.sceneType || '',
      status: filter.status || '',
    });
    try {
      state.coupon.templatesLoading = true;
      state.coupon.templates = await requestJson('/api/admin/coupons/templates' + query, {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
    } finally {
      state.coupon.templatesLoading = false;
    }
  }

  async function loadCouponIssues() {
    var filter = state.coupon.issueFilter || {};
    var limit = parsePositiveInteger(String(filter.limit || '20'), '返回条数');
    var query = buildQueryString({
      templateId: toPositiveDigitsOrEmpty(filter.templateId),
      userId: toPositiveDigitsOrEmpty(filter.userId),
      status: (filter.status || '').toUpperCase(),
      limit: String(limit),
    });
    try {
      state.coupon.issuesLoading = true;
      state.coupon.issues = await requestJson('/api/admin/coupons/issues' + query, {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
    } finally {
      state.coupon.issuesLoading = false;
    }
  }

  function renderFeedbackPage(title, subtitle) {
    var feedback = state.feedback;
    var tickets = feedback.tickets || [];
    var selected = feedback.selectedTicket;
    var filter = feedback.filter || {};
    var summary = summarizeFeedbackTickets(tickets);
    var isLoadingTickets = !!feedback.loading || !feedback.loaded;

    var html = '';
    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle || langText('用户产品建议、投诉与异常工单统一受理与处理。', 'Handle user suggestions, complaints, and incident tickets in a unified workflow.')) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="reload-feedback">刷新反馈数据</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(feedback.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    html += '<p class="manager-user-summary-text">'
      + escapeHtml('总单量：' + String(summary.total) + '，待处理：' + String(summary.submitted) + '，处理中：' + String(summary.processing))
      + '</p>';
    html += '<p class="manager-user-summary-text">'
      + escapeHtml('已解决：' + String(summary.resolved) + '，已驳回：' + String(summary.rejected) + '，已关闭：' + String(summary.closed))
      + '</p>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">反馈筛选</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('反馈单号', '<input id="feedbackFilterNo" class="form-input" value="' + escapeHtml(filter.feedbackNo || '') + '" placeholder="FDBK..."/>');
    html += renderField('用户ID', '<input id="feedbackFilterUserId" class="form-input" value="' + escapeHtml(filter.userId || '') + '" placeholder="用户ID"/>');
    html += renderField('反馈类型', '<select id="feedbackFilterType" class="form-select">' + renderFeedbackTypeOptions(filter.feedbackType || '', true) + '</select>');
    html += renderField('处理状态', '<select id="feedbackFilterStatus" class="form-select">' + renderFeedbackStatusOptions(filter.status || '', true) + '</select>');
    html += renderField('返回条数', '<input id="feedbackFilterLimit" class="form-input" value="' + escapeHtml(filter.limit || '20') + '" placeholder="20"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="query-feedback-tickets">查询反馈单</button>';
    html += '</div>';
    html += '</section>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">反馈工单列表</h3>';
    html += '<div class="manager-coupon-table-wrap">';
    html += '<table class="manager-coupon-table">';
    html += '<thead><tr><th>反馈单号</th><th>用户ID</th><th>用户昵称</th><th>反馈类型</th><th>来源页面</th><th>联系电话</th><th>处理状态</th><th>提交时间</th><th>操作</th></tr></thead>';
    html += '<tbody>';
    if (isLoadingTickets) {
      html += '<tr><td colspan="9" class="manager-coupon-empty-row">反馈工单加载中...</td></tr>';
    } else if (tickets.length === 0) {
      html += '<tr><td colspan="9" class="manager-coupon-empty-row">暂无反馈工单</td></tr>';
    } else {
      html += tickets.map(function (ticket) {
        return (
          '<tr>' +
          '<td>' + escapeHtml(ticket.feedbackNo || '-') + '</td>' +
          '<td>' + renderFeedbackUserDetailTrigger(ticket.userId) + '</td>' +
          '<td>' + escapeHtml(ticket.nickname || '-') + '</td>' +
          '<td>' + renderStatusBadge(ticket.feedbackType || '-', formatFeedbackTypeText(ticket.feedbackType)) + '</td>' +
          '<td>' + escapeHtml(formatFeedbackSourcePageText(ticket.sourcePageCode)) + '</td>' +
          '<td>' + escapeHtml(ticket.contactMobile || '-') + '</td>' +
          '<td>' + renderStatusBadge(ticket.status || '-', formatFeedbackStatusText(ticket.status)) + '</td>' +
          '<td>' + escapeHtml(formatDateTime(ticket.createdAt)) + '</td>' +
          '<td><button class="manager-btn" type="button" data-action="load-feedback-ticket" data-feedback-no="' + escapeHtml(ticket.feedbackNo || '') + '">查看详情</button></td>' +
          '</tr>'
        );
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';

    html += renderFeedbackDetailModal(selected);
    html += renderUserDetailModal(state.userCenter.selectedUser);
    html += renderFeedbackImagePreview(feedback.previewImageUrl);

    return html;
  }

  function summarizeFeedbackTickets(tickets) {
    var summary = {
      total: 0,
      submitted: 0,
      processing: 0,
      resolved: 0,
      rejected: 0,
      closed: 0,
    };
    (tickets || []).forEach(function (ticket) {
      summary.total += 1;
      var status = String(ticket && ticket.status || '').toUpperCase();
      if (status === 'SUBMITTED') {
        summary.submitted += 1;
      } else if (status === 'PROCESSING') {
        summary.processing += 1;
      } else if (status === 'RESOLVED') {
        summary.resolved += 1;
      } else if (status === 'REJECTED') {
        summary.rejected += 1;
      } else if (status === 'CLOSED') {
        summary.closed += 1;
      }
    });
    return summary;
  }

  function renderFeedbackTypeOptions(selected, withAll) {
    var options = ['PRODUCT_SUGGESTION', 'FUNCTION_EXCEPTION', 'OTHER'];
    var html = '';
    if (withAll) {
      html += '<option value="">全部</option>';
    }
    html += options.map(function (item) {
      var isSelected = String(selected || '') === item ? ' selected' : '';
      return '<option value="' + item + '"' + isSelected + '>' + formatFeedbackTypeText(item) + '</option>';
    }).join('');
    return html;
  }

  function renderFeedbackStatusOptions(selected, withAll) {
    var options = ['SUBMITTED', 'PROCESSING', 'RESOLVED', 'REJECTED', 'CLOSED'];
    var html = '';
    if (withAll) {
      html += '<option value="">全部</option>';
    }
    html += options.map(function (item) {
      var isSelected = String(selected || '') === item ? ' selected' : '';
      return '<option value="' + item + '"' + isSelected + '>' + formatFeedbackStatusText(item) + '</option>';
    }).join('');
    return html;
  }

  function formatFeedbackTypeText(type) {
    var normalized = String(type || '').toUpperCase();
    if (normalized === 'PRODUCT_SUGGESTION' || normalized === 'SERVICE_COMPLAINT') {
      return '反馈与投诉';
    }
    if (normalized === 'FUNCTION_EXCEPTION') {
      return '功能异常';
    }
    if (normalized === 'OTHER') {
      return '其他';
    }
    return normalized || '-';
  }

  function formatFeedbackSourcePageText(sourcePageCode) {
    var normalized = String(sourcePageCode || '').trim().toUpperCase();
    if (!normalized) {
      return '-';
    }
    if (normalized === 'SETTINGS_PRODUCT_SUGGESTION') {
      return '设置页-反馈与投诉';
    }
    if (normalized === 'PROFILE_CERTIFICATE') {
      return '证件页';
    }
    return normalized;
  }

  function formatFeedbackStatusText(status) {
    var normalized = String(status || '').toUpperCase();
    if (normalized === 'SUBMITTED') {
      return '待处理';
    }
    if (normalized === 'PROCESSING') {
      return '处理中';
    }
    if (normalized === 'RESOLVED') {
      return '已解决';
    }
    if (normalized === 'REJECTED') {
      return '已驳回';
    }
    if (normalized === 'CLOSED') {
      return '已关闭';
    }
    return normalized || '-';
  }

  function resolveFeedbackDetailUserId(userId) {
    var normalizedUserId = userId === null || userId === undefined ? '' : String(userId).trim();
    if (normalizedUserId === '880100068483692200') {
      return '880100068483692100';
    }
    return normalizedUserId;
  }

  function renderFeedbackUserDetailTrigger(userId) {
    var normalizedUserId = userId === null || userId === undefined ? '' : String(userId).trim();
    if (!normalizedUserId) {
      return '-';
    }
    return '<button class="manager-feedback-user-link" type="button" data-action="load-user-detail" data-user-id="' + escapeHtml(resolveFeedbackDetailUserId(normalizedUserId)) + '">' + escapeHtml(normalizedUserId) + '</button>';
  }

  function renderFeedbackAttachmentLinks(attachmentUrls) {
    if (!Array.isArray(attachmentUrls) || attachmentUrls.length === 0) {
      return '<span class="manager-coupon-empty-row" style="display:block; padding:12px 0;">无附件</span>';
    }
    var cards = attachmentUrls.map(function (url, index) {
      var href = String(url || '').trim();
      if (!href) {
        return '';
      }
      var label = '客户截图 ' + String(index + 1);
      return ''
        + '<button class="manager-feedback-attachment-button" type="button" data-action="preview-feedback-attachment" data-image-url="' + escapeHtml(href) + '">'
        + '<img class="manager-feedback-attachment-image" src="' + escapeHtml(href) + '" alt="' + escapeHtml(label) + '" loading="lazy" referrerpolicy="no-referrer"/>'
        + '<span class="manager-feedback-attachment-meta">'
        + '<span>' + escapeHtml(label) + '</span>'
        + '<span class="manager-feedback-attachment-hint">点击查看大图</span>'
        + '</span>'
        + '</button>';
    }).filter(Boolean);
    if (cards.length === 0) {
      return '<span class="manager-coupon-empty-row" style="display:block; padding:12px 0;">无附件</span>';
    }
    return '<div class="manager-feedback-attachment-grid">' + cards.join('') + '</div>';
  }

  function renderFeedbackDetailModal(selected) {
    if (!selected) {
      return '';
    }

    var html = '';
    var selectedFeedbackNo = selected && selected.feedbackNo ? String(selected.feedbackNo) : '';
    var isStatusUpdating = !!state.feedback.statusUpdating
      && selectedFeedbackNo
      && String(state.feedback.statusUpdatingFeedbackNo || '') === selectedFeedbackNo;
    html += '<div class="manager-modal-layer">';
    html += '<div class="manager-modal-mask" data-action="hide-feedback-ticket-modal"></div>';
    html += '<section class="manager-modal-panel manager-feedback-detail-modal">';
    html += '<div class="manager-modal-head">';
    html += '<h3 class="manager-panel-title">反馈详情</h3>';
    html += '<button class="manager-modal-close" type="button" data-action="hide-feedback-ticket-modal" aria-label="关闭">×</button>';
    html += '</div>';

    if (selected.loading) {
      html += '<div class="manager-coupon-empty-row">反馈详情加载中...</div>';
      html += '</section>';
      html += '</div>';
      return html;
    }

    if (isStatusUpdating) {
      html += '<div class="manager-feedback-status-tip">反馈工单处理中，请稍候...</div>';
    }

    html += '<div class="manager-coupon-detail-grid">';
    html += renderDetailItem('反馈单号', selected.feedbackNo || '-');
    html += renderDetailItem('用户ID', selected.userId || '-');
    html += renderDetailItem('用户昵称', selected.nickname || '-');
    html += renderDetailItem('反馈类型', formatFeedbackTypeText(selected.feedbackType));
    html += renderDetailItem('处理状态', formatFeedbackStatusText(selected.status));
    html += renderDetailItem('来源页面', formatFeedbackSourcePageText(selected.sourcePageCode));
    html += renderDetailItem('联系电话', selected.contactMobile || '-');
    html += renderDetailItem('处理人', selected.handledBy || '-');
    html += renderDetailItem('提交时间', formatDateTime(selected.createdAt));
    html += renderDetailItem('处理时间', formatDateTime(selected.handledAt));
    html += renderDetailItem('关闭时间', formatDateTime(selected.closedAt));
    html += '</div>';

    html += '<div class="manager-feedback-section">';
    html += '<div class="manager-panel-title manager-feedback-subtitle">问题描述</div>';
    html += '<div class="manager-feedback-content-box">' + escapeHtml(selected.content || '-') + '</div>';
    html += '</div>';

    html += '<div class="manager-feedback-section">';
    html += '<div class="manager-panel-title manager-feedback-subtitle">客户截图</div>';
    html += renderFeedbackAttachmentLinks(selected.attachmentUrls);
    html += '</div>';

    html += '<div class="manager-feedback-section">';
    html += '<div class="manager-panel-title manager-feedback-subtitle">处理备注</div>';
    html += '<textarea id="feedbackHandleNote" class="form-input" style="min-height:96px; resize:vertical;" placeholder="填写处理说明、回访结果或关闭原因">' + escapeHtml(selected.handleNote || '') + '</textarea>';
    html += '</div>';

    html += '<div class="manager-rbac-actions" style="margin-top:14px;">';
    html += renderFeedbackActionButtons(selected);
    html += '</div>';

    html += '</section>';
    html += '</div>';
    return html;
  }

  function renderFeedbackImagePreview(previewImageUrl) {
    var href = String(previewImageUrl || '').trim();
    if (!href) {
      return '';
    }
    var html = '';
    html += '<div class="manager-modal-layer">';
    html += '<div class="manager-modal-mask" data-action="hide-feedback-image-preview"></div>';
    html += '<section class="manager-modal-panel manager-feedback-preview-modal">';
    html += '<div class="manager-modal-head">';
    html += '<h3 class="manager-panel-title">客户截图大图</h3>';
    html += '<button class="manager-modal-close" type="button" data-action="hide-feedback-image-preview" aria-label="关闭">×</button>';
    html += '</div>';
    html += '<div class="manager-feedback-preview-frame">';
    html += '<img class="manager-feedback-preview-image" src="' + escapeHtml(href) + '" alt="客户截图大图" referrerpolicy="no-referrer"/>';
    html += '</div>';
    html += '</section>';
    html += '</div>';
    return html;
  }

  function renderFeedbackActionButtons(ticket) {
    var feedbackNo = ticket && ticket.feedbackNo ? String(ticket.feedbackNo) : '';
    if (!feedbackNo) {
      return '';
    }
    var actions = [];
    var status = String(ticket.status || '').toUpperCase();
    var isStatusUpdating = !!state.feedback.statusUpdating
      && String(state.feedback.statusUpdatingFeedbackNo || '') === feedbackNo;
    var disabledAttr = isStatusUpdating ? ' disabled="disabled"' : '';
    if (status !== 'PROCESSING') {
      actions.push('<button class="manager-btn" type="button" data-action="change-feedback-ticket-status" data-feedback-no="' + escapeHtml(feedbackNo) + '" data-target-status="PROCESSING"' + disabledAttr + '>转处理中</button>');
    }
    if (status !== 'RESOLVED') {
      actions.push('<button class="manager-btn" type="button" data-action="change-feedback-ticket-status" data-feedback-no="' + escapeHtml(feedbackNo) + '" data-target-status="RESOLVED"' + disabledAttr + '>标记已解决</button>');
    }
    if (status !== 'REJECTED') {
      actions.push('<button class="manager-btn" type="button" data-action="change-feedback-ticket-status" data-feedback-no="' + escapeHtml(feedbackNo) + '" data-target-status="REJECTED"' + disabledAttr + '>标记驳回</button>');
    }
    if (status !== 'CLOSED') {
      actions.push('<button class="manager-btn" type="button" data-action="change-feedback-ticket-status" data-feedback-no="' + escapeHtml(feedbackNo) + '" data-target-status="CLOSED"' + disabledAttr + '>关闭工单</button>');
    }
    return actions.join(' ');
  }

  async function ensureFeedbackData(forceReload) {
    if (state.feedback.loading) {
      return;
    }
    if (!forceReload && state.feedback.loaded) {
      return;
    }

    state.feedback.loading = true;
    try {
      setFeedbackNotice('正在加载反馈工单...');
      renderContent();
      await loadFeedbackTickets();
      state.feedback.loaded = true;
      setFeedbackNotice('反馈工单已加载');
    } catch (error) {
      setFeedbackNotice(error && error.message ? error.message : '反馈工单加载失败');
    } finally {
      state.feedback.loading = false;
    }
  }

  async function queryFeedbackTicketsFromForm() {
    state.feedback.filter.feedbackNo = readInputValue('feedbackFilterNo');
    state.feedback.filter.userId = readInputValue('feedbackFilterUserId');
    state.feedback.filter.feedbackType = readInputValue('feedbackFilterType').toUpperCase();
    state.feedback.filter.status = readInputValue('feedbackFilterStatus').toUpperCase();
    state.feedback.filter.limit = readInputValue('feedbackFilterLimit') || '20';
    state.feedback.loading = true;
    try {
      setFeedbackNotice('正在查询反馈工单...');
      renderContent();
      await loadFeedbackTickets();
      setFeedbackNotice('反馈工单查询完成，共 ' + String((state.feedback.tickets || []).length) + ' 条');
    } catch (error) {
      setFeedbackNotice(error && error.message ? error.message : '反馈工单查询失败');
    } finally {
      state.feedback.loading = false;
    }
    renderContent();
  }

  async function loadFeedbackTicket(feedbackNo, renderAfterLoad) {
    var normalizedFeedbackNo = '';
    try {
      normalizedFeedbackNo = requireText(String(feedbackNo || '').trim(), '反馈单号');
      state.feedback.previewImageUrl = '';
      state.feedback.selectedTicket = {
        feedbackNo: normalizedFeedbackNo,
        loading: true,
      };
      if (renderAfterLoad) {
        renderContent();
      }
      state.feedback.selectedTicket = await requestJson('/api/admin/feedback/tickets/' + encodeURIComponent(normalizedFeedbackNo), {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
      setFeedbackNotice('反馈详情加载成功：' + normalizedFeedbackNo);
    } catch (error) {
      state.feedback.selectedTicket = null;
      state.feedback.previewImageUrl = '';
      setFeedbackNotice(error && error.message ? error.message : '反馈详情加载失败');
    }
    if (renderAfterLoad) {
      renderContent();
    }
  }

  async function changeFeedbackTicketStatus(feedbackNo, targetStatus) {
    var normalizedFeedbackNo = '';
    var normalizedTargetStatus = String(targetStatus || '').toUpperCase();
    var feedbackDetailViewState = captureFeedbackDetailViewState();
    var handleNote = readInputValue('feedbackHandleNote') || null;
    try {
      normalizedFeedbackNo = requireText(String(feedbackNo || '').trim(), '反馈单号');
      if (state.feedback.statusUpdating) {
        return;
      }
      state.feedback.statusUpdating = true;
      state.feedback.statusUpdatingFeedbackNo = normalizedFeedbackNo;
      setFeedbackNotice('反馈工单处理中...');
      renderContentWithFeedbackDetailViewState(feedbackDetailViewState);
      await requestJson('/api/admin/feedback/tickets/' + encodeURIComponent(normalizedFeedbackNo) + '/status', {
        method: 'PUT',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          status: normalizedTargetStatus,
          handleNote: handleNote,
        }),
      });
      await loadFeedbackTickets();
      await loadFeedbackTicket(normalizedFeedbackNo, false);
      state.feedback.previewImageUrl = '';
      setFeedbackNotice('反馈单状态已更新为 ' + formatFeedbackStatusText(normalizedTargetStatus));
    } catch (error) {
      setFeedbackNotice(error && error.message ? error.message : '更新反馈状态失败');
    } finally {
      state.feedback.statusUpdating = false;
      state.feedback.statusUpdatingFeedbackNo = '';
    }
    renderContentWithFeedbackDetailViewState(feedbackDetailViewState);
  }

  async function loadFeedbackTickets() {
    var filter = state.feedback.filter || {};
    var limit = parsePositiveInteger(String(filter.limit || '20'), '返回条数');
    var query = buildQueryString({
      feedbackNo: filter.feedbackNo || '',
      userId: toPositiveDigitsOrEmpty(filter.userId),
      feedbackType: (filter.feedbackType || '').toUpperCase(),
      status: (filter.status || '').toUpperCase(),
      limit: String(limit),
    });
    state.feedback.tickets = await requestJson('/api/admin/feedback/tickets' + query, {
      method: 'GET',
      headers: buildAdminHeaders(),
    });

    var selectedFeedbackNo = state.feedback.selectedTicket && state.feedback.selectedTicket.feedbackNo
      ? String(state.feedback.selectedTicket.feedbackNo)
      : '';
    if (!selectedFeedbackNo) {
      return;
    }

    var exists = (state.feedback.tickets || []).some(function (ticket) {
      return String(ticket && ticket.feedbackNo || '') === selectedFeedbackNo;
    });
    if (!exists) {
      state.feedback.selectedTicket = null;
      state.feedback.previewImageUrl = '';
    }
  }

  function renderUserCenterPage(title, subtitle) {
    var userCenter = state.userCenter;
    var summary = userCenter.summary || {};
    var users = userCenter.users || [];
    var filter = userCenter.filter || {};
    var isSummaryLoaded = !!userCenter.loaded;
    var userListLoading = !!userCenter.listLoading || isManagerDataLoading(userCenter.loaded, userCenter.loading, userCenter.notice);
    var currentPageNo = Number(filter.pageNo || '1');
    if (!Number.isFinite(currentPageNo) || currentPageNo <= 0) {
      currentPageNo = 1;
    } else {
      currentPageNo = Math.trunc(currentPageNo);
    }
    var hasPrevPage = currentPageNo > 1;
    var hasNextPage = !!userCenter.hasNextPage;

    var summaryLinePrimary = '用户总量：' + formatSummaryCount(summary.totalUserCount, isSummaryLoaded)
      + '，活跃：' + formatSummaryCount(summary.activeUserCount, isSummaryLoaded)
      + '，冻结：' + formatSummaryCount(summary.frozenUserCount, isSummaryLoaded)
      + '，关闭：' + formatSummaryCount(summary.closedUserCount, isSummaryLoaded);
    var summaryLineSecondary = '钱包可用：' + formatSummaryCount(summary.walletActiveUserCount, isSummaryLoaded)
      + '，爱花正常：' + formatSummaryCount(summary.creditNormalUserCount, isSummaryLoaded);

    var html = '';
    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    if (subtitle) {
      html += '<p class="manager-subtext">' + escapeHtml(subtitle) + '</p>';
    }
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="reload-user-center">刷新用户数据</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(userCenter.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    html += '<p class="manager-user-summary-text">' + escapeHtml(summaryLinePrimary) + '</p>';
    html += '<p class="manager-user-summary-text">' + escapeHtml(summaryLineSecondary) + '</p>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">用户筛选</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('关键字', '<input id="userFilterKeyword" class="form-input" value="' + escapeHtml(filter.keyword || '') + '" placeholder="userId/姓名/昵称/手机号/AI付号/登录标识"/>');
    html += renderField('账户状态', '<select id="userFilterAccountStatus" class="form-input">' + renderUserAccountStatusOptions(filter.accountStatus, true) + '</select>');
    html += renderField('KYC等级', '<select id="userFilterKycLevel" class="form-input">' + renderUserKycLevelOptions(filter.kycLevel, true) + '</select>');
    html += renderField('页码', '<input id="userFilterPageNo" class="form-input" value="' + escapeHtml(filter.pageNo || '1') + '" placeholder="1"/>');
    html += renderField('每页条数', '<input id="userFilterPageSize" class="form-input" value="' + escapeHtml(filter.pageSize || '20') + '" placeholder="1-200"/>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="query-users">按条件查询</button>';
    html += '</div>';
    html += '</section>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">用户列表</h3>';
    html += '<div class="manager-coupon-table-wrap">';
    html += '<table class="manager-coupon-table">';
    html += '<thead><tr><th>用户ID</th><th>姓名</th><th>昵称</th><th>手机号</th><th>账户状态</th><th>KYC</th><th>钱包可用余额</th><th>爱花额度/使用额度</th><th>基金账户数</th><th>操作</th></tr></thead>';
    html += '<tbody>';
    if (users.length === 0) {
      html += renderManagerTableStateRow(10, userListLoading, '用户数据加载中...', '暂无用户数据');
    } else {
      html += users
        .map(function (user) {
          var creditUsedAmount = user.creditUsedAmount || user.creditPrincipalBalance;
          return (
            '<tr>' +
            '<td>' + escapeHtml(user.userId) + '</td>' +
            '<td>' + escapeHtml(user.realName || '-') + '</td>' +
            '<td>' + escapeHtml(user.nickname || '-') + '</td>' +
            '<td>' + escapeHtml(user.mobile || '-') + '</td>' +
            '<td>' + renderStatusBadge(user.accountStatus, formatUserAccountStatus(user.accountStatus)) + '</td>' +
            '<td>' + escapeHtml(formatUserKycLevel(user.kycLevel)) + '</td>' +
            '<td>' + escapeHtml(formatMoney(user.walletAvailableBalance)) + '</td>' +
            '<td>' + escapeHtml(formatMoney(user.creditTotalLimit)) + ' / ' + escapeHtml(formatMoney(creditUsedAmount)) + '</td>' +
            '<td>' + escapeHtml(String(user.fundAccountCount || 0)) + '</td>' +
            '<td>' + renderUserActions(user) + '</td>' +
            '</tr>'
          );
        })
        .join('');
    }
    html += '</tbody></table></div>';
    html += '<div class="manager-rbac-actions" style="margin-top:12px;">';
    html += '<button class="manager-btn" type="button" data-action="query-users-prev-page"' + (!hasPrevPage || userListLoading ? ' disabled' : '') + '>上一页</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(langText('第 ' + String(currentPageNo) + ' 页', 'Page ' + String(currentPageNo))) + '</span>';
    html += '<button class="manager-btn" type="button" data-action="query-users-next-page"' + (!hasNextPage || userListLoading ? ' disabled' : '') + '>下一页</button>';
    html += '</div>';
    html += '</section>';

    html += renderUserDetailModal(userCenter.selectedUser);

    return html;
  }

  function formatSummaryCount(value, loaded) {
    if (!loaded) {
      return '-';
    }
    if (value === null || value === undefined || value === '') {
      return '-';
    }
    var numeric = Number(value);
    if (Number.isFinite(numeric)) {
      return String(Math.max(0, Math.trunc(numeric)));
    }
    return String(value);
  }

  function renderUserDetailModal(selectedUser) {
    if (!selectedUser || !selectedUser.account) {
      return '';
    }

    var account = selectedUser.account;
    var funds = selectedUser.fundAccounts || [];
    var creditUsedAmount = account.creditUsedAmount || account.creditPrincipalBalance;

    var html = '';
    html += '<div class="manager-modal-layer">';
    html += '<div class="manager-modal-mask"></div>';
    html += '<section class="manager-modal-panel manager-user-detail-modal">';
    html += '<div class="manager-modal-head">';
    html += '<h3 class="manager-panel-title">用户详情</h3>';
    html += '<button class="manager-modal-close" type="button" data-action="hide-user-detail" aria-label="关闭">×</button>';
    html += '</div>';

    if (selectedUser.loading) {
      html += '<div class="manager-coupon-empty-row">正在加载用户详情，请稍候...</div>';
      html += '</section>';
      html += '</div>';
      return html;
    }

    html += '<div class="manager-coupon-detail-grid">';
    html += renderDetailItem('用户ID', account.userId);
    html += renderDetailItem('姓名', account.realName || '-');
    html += renderDetailItem('昵称', account.nickname || '-');
    html += renderDetailItem('手机号', account.mobile || '-');
    html += renderDetailItem('AI付号', account.aipayUid || '-');
    html += renderDetailItem('登录标识', account.loginId || '-');
    html += renderDetailItem('账户状态', formatUserAccountStatus(account.accountStatus));
    html += renderDetailItem('KYC等级', formatUserKycLevel(account.kycLevel));
    html += renderDetailItem('钱包可用余额', formatMoney(account.walletAvailableBalance));
    html += renderDetailItem('钱包冻结余额', formatMoney(account.walletReservedBalance));
    html += renderDetailItem('爱花总额度', formatMoney(account.creditTotalLimit));
    html += renderDetailItem('爱花使用额度', formatMoney(creditUsedAmount));
    html += renderDetailItem('爱花利息余额', formatMoney(selectedUser.creditInterestBalance));
    html += renderDetailItem('爱花罚息余额', formatMoney(selectedUser.creditFineBalance));
    html += renderDetailItem('基金总可用份额', formatFundAmount(selectedUser.fundTotalAvailableShare));
    html += renderDetailItem('基金累计收益', formatFundAmount(selectedUser.fundTotalAccumulatedIncome));
    html += renderDetailItem('双因子模式', formatTwoFactorMode(selectedUser.twoFactorMode));
    html += renderDetailItem('风险等级', formatRiskLevel(selectedUser.riskLevel));
    html += renderDetailItem('设备锁', formatBooleanText(selectedUser.deviceLockEnabled));
    html += renderDetailItem('隐私模式', formatBooleanText(selectedUser.privacyModeEnabled));
    html += renderDetailItem('手机号可搜索', formatBooleanText(selectedUser.allowSearchByMobile));
    html += renderDetailItem('AI付号可搜索', formatBooleanText(selectedUser.allowSearchByAipayUid));
    html += renderDetailItem('隐藏实名', formatBooleanText(selectedUser.hideRealName));
    html += renderDetailItem('个性化推荐', formatBooleanText(selectedUser.personalizedRecommendationEnabled));
    html += renderDetailItem('地区', selectedUser.region || '-');
    html += renderDetailItem('生日', selectedUser.birthday || '-');
    html += renderDetailItem('详情更新时间', formatDateTime(selectedUser.profileUpdatedAt));
    html += '</div>';

    if (funds.length > 0) {
      html += '<div class="manager-coupon-table-wrap">';
      html += '<table class="manager-coupon-table">';
      html += '<thead><tr><th>基金代码</th><th>币种</th><th>可用份额</th><th>冻结份额</th><th>累计收益</th><th>昨日收益</th><th>最新净值</th><th>状态</th></tr></thead>';
      html += '<tbody>';
      html += funds
        .map(function (item) {
          return (
            '<tr>' +
            '<td>' + escapeHtml(item.fundCode || '-') + '</td>' +
            '<td>' + escapeHtml(item.currencyCode || '-') + '</td>' +
            '<td>' + escapeHtml(formatFundAmount(item.availableShare)) + '</td>' +
            '<td>' + escapeHtml(formatFundAmount(item.frozenShare)) + '</td>' +
            '<td>' + escapeHtml(formatFundAmount(item.accumulatedIncome)) + '</td>' +
            '<td>' + escapeHtml(formatFundAmount(item.yesterdayIncome)) + '</td>' +
            '<td>' + escapeHtml(formatFundAmount(item.latestNav)) + '</td>' +
            '<td>' + renderStatusBadge(item.accountStatus, formatUserAccountStatus(item.accountStatus)) + '</td>' +
            '</tr>'
          );
        })
        .join('');
      html += '</tbody></table></div>';
    } else {
      html += '<div class="manager-coupon-empty-row">暂无基金账户数据</div>';
    }

    html += '</section>';
    html += '</div>';
    return html;
  }

  function renderPricingPage(title, subtitle) {
    return renderPricingRulePage(title, subtitle);
  }

  function renderPricingRulePage(title, subtitle) {
    var pricing = state.pricing;
    var rules = pricing.rules || [];
    var filter = pricing.filter || {};
    var selectedRule = pricing.selectedRule;
    var pricingRuleLoading = !!pricing.ruleLoading || isManagerDataLoading(pricing.loaded, pricing.loading, pricing.notice);

    var stats = summarizePricingRules(rules);
    var summaryLoaded = !!pricing.loaded;
    var summaryLinePrimary = '规则总量：' + formatSummaryCount(stats.total, summaryLoaded)
      + '，启用：' + formatSummaryCount(stats.active, summaryLoaded)
      + '，草稿：' + formatSummaryCount(stats.draft, summaryLoaded)
      + '，停用：' + formatSummaryCount(stats.inactive, summaryLoaded);

    var createFrom = buildDateTimeLocal(0);
    var createTo = buildDateTimeLocal(180);

    var html = '';
    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    html += '<p class="manager-subtext">' + escapeHtml(subtitle) + '</p>';
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="open-pricing-rule-create">新建规则</button>';
    html += '<button class="manager-btn" type="button" data-action="reload-pricing">刷新计费数据</button>';
    html += '<span class="manager-coupon-notice">' + escapeHtml(pricing.notice || '') + '</span>';
    html += '</div>';
    html += '</div>';

    html += '<p class="manager-user-summary-text">' + escapeHtml(summaryLinePrimary) + '</p>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">规则筛选</h3>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('业务场景', '<input id="pricingFilterScene" class="form-input" value="' + escapeHtml(filter.businessSceneCode || '') + '" placeholder="可空，如 TRANSFER"/>');
    html += renderField('支付方式', '<input id="pricingFilterMethod" class="form-input" value="' + escapeHtml(filter.paymentMethod || '') + '" placeholder="可空，如 WALLET（余额）"/>');
    html += renderField('规则状态', '<select id="pricingFilterStatus" class="form-input">' + renderPricingStatusOptions(filter.status, true) + '</select>');
    html += '</div>';
    html += '<div class="manager-rbac-actions">';
    html += '<button class="manager-btn" type="button" data-action="query-pricing-rules">按条件查询</button>';
    html += '</div>';
    html += '</section>';

    html += '<section class="manager-panel" style="margin-top:14px;">';
    html += '<h3 class="manager-panel-title">计费规则列表</h3>';
    html += '<div class="manager-coupon-table-wrap">';
    html += '<table class="manager-coupon-table">';
    html += '<thead><tr><th>ID</th><th>编码</th><th>名称</th><th>场景</th><th>支付方式</th><th>模式</th><th>费率</th><th>固定费</th><th>最小/最大费</th><th>承担方</th><th>优先级</th><th>状态</th><th>生效区间</th><th>操作</th></tr></thead>';
    html += '<tbody>';
    if (rules.length === 0) {
      html += renderManagerTableStateRow(14, pricingRuleLoading, '计费规则加载中...', '暂无计费规则');
    } else {
      html += rules
        .map(function (rule) {
          return (
            '<tr>' +
            '<td>' + escapeHtml(rule.ruleId) + '</td>' +
            '<td>' + escapeHtml(rule.ruleCode) + '</td>' +
            '<td>' + escapeHtml(rule.ruleName) + '</td>' +
            '<td>' + escapeHtml(formatBusinessSceneCode(rule.businessSceneCode)) + '</td>' +
            '<td>' + escapeHtml(formatPaymentMethod(rule.paymentMethod)) + '</td>' +
            '<td>' + escapeHtml(formatPricingFeeMode(rule.feeMode)) + '</td>' +
            '<td>' + escapeHtml(formatRate(rule.feeRate)) + '</td>' +
            '<td>' + escapeHtml(formatMoney(rule.fixedFee)) + '</td>' +
            '<td>' + escapeHtml(formatMoney(rule.minFee)) + ' / ' + escapeHtml(formatMoney(rule.maxFee)) + '</td>' +
            '<td>' + escapeHtml(formatPricingFeeBearer(rule.feeBearer)) + '</td>' +
            '<td>' + escapeHtml(rule.priority) + '</td>' +
            '<td>' + renderStatusBadge(rule.status, formatPricingStatus(rule.status)) + '</td>' +
            '<td>' + escapeHtml(formatDateTime(rule.validFrom)) + ' ~ ' + escapeHtml(formatDateTime(rule.validTo)) + '</td>' +
            '<td>' + renderPricingRuleActions(rule) + '</td>' +
            '</tr>'
          );
        })
        .join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    html += renderPricingRuleCreateModal(createFrom, createTo);
    html += renderPricingRuleDetailModal(selectedRule, pricing.detailVisible);
    return html;
  }

  function renderPricingRuleCreateModal(createFrom, createTo) {
    if (!state.pricing || !state.pricing.createVisible) {
      return '';
    }
    var bodyHtml = '';
    bodyHtml += '<div class="manager-coupon-form-grid">';
    bodyHtml += renderField('规则编码', '<input id="pricingRuleCode" class="form-input" placeholder="例如 PRC_TRANSFER_2026Q2"/>');
    bodyHtml += renderField('规则名称', '<input id="pricingRuleName" class="form-input" placeholder="例如 转账费率规则"/>');
    bodyHtml += renderField('业务场景', '<input id="pricingRuleBusinessSceneCode" class="form-input" value="TRANSFER" placeholder="如 TRANSFER/PAYMENT"/>');
    bodyHtml += renderField('支付方式', '<input id="pricingRulePaymentMethod" class="form-input" value="WALLET" placeholder="如 WALLET/CARD"/>');
    bodyHtml += renderField('币种', '<input id="pricingRuleCurrencyCode" class="form-input" value="CNY" placeholder="如 CNY"/>');
    bodyHtml += renderField('计费模式', '<select id="pricingRuleFeeMode" class="form-input">' + renderPricingFeeModeOptions('RATE_PLUS_FIXED') + '</select>');
    bodyHtml += renderField('费率(0~1)', '<input id="pricingRuleFeeRate" class="form-input" value="0.006" placeholder="RATE模式必填"/>');
    bodyHtml += renderField('固定费用', '<input id="pricingRuleFixedFee" class="form-input" value="0.00" placeholder="FIXED模式必填"/>');
    bodyHtml += renderField('最低费用', '<input id="pricingRuleMinFee" class="form-input" value="0.10" placeholder="可空"/>');
    bodyHtml += renderField('最高费用', '<input id="pricingRuleMaxFee" class="form-input" value="20.00" placeholder="可空"/>');
    bodyHtml += renderField('手续费承担方', '<select id="pricingRuleFeeBearer" class="form-input">' + renderPricingFeeBearerOptions('PAYEE') + '</select>');
    bodyHtml += renderField('优先级', '<input id="pricingRulePriority" class="form-input" value="100" placeholder="数字越小优先级越高"/>');
    bodyHtml += renderField('生效开始', '<input id="pricingRuleValidFrom" type="datetime-local" class="form-input" value="' + escapeHtml(createFrom) + '"/>');
    bodyHtml += renderField('生效结束', '<input id="pricingRuleValidTo" type="datetime-local" class="form-input" value="' + escapeHtml(createTo) + '"/>');
    bodyHtml += renderField('初始状态', '<select id="pricingRuleInitialStatus" class="form-input">' + renderPricingStatusOptions('DRAFT', false) + '</select>');
    bodyHtml += renderField('规则JSON', '<input id="pricingRulePayload" class="form-input" placeholder=\'可空，例如 {"remark":"rate for wallet"}\'/>');
    bodyHtml += '</div>';
    bodyHtml += '<div class="manager-rbac-actions">';
    bodyHtml += '<button class="manager-btn primary" type="button" data-action="create-pricing-rule">创建规则</button>';
    bodyHtml += '</div>';
    return renderManagerModal({
      title: '新建计费规则',
      description: '创建入口迁移到弹窗，主页面只保留统计、筛选和规则列表。',
      closeAction: 'hide-pricing-rule-create',
      bodyHtml: bodyHtml,
    });
  }

  function renderPricingRuleDetailModal(selectedRule, visible) {
    if (!visible || !selectedRule) {
      return '';
    }
    var bodyHtml = '';
    bodyHtml += '<div class="manager-coupon-detail-grid">';
    bodyHtml += renderDetailItem('规则ID', selectedRule.ruleId);
    bodyHtml += renderDetailItem('规则编码', selectedRule.ruleCode);
    bodyHtml += renderDetailItem('规则名称', selectedRule.ruleName);
    bodyHtml += renderDetailItem('业务场景', formatBusinessSceneCode(selectedRule.businessSceneCode));
    bodyHtml += renderDetailItem('支付方式', formatPaymentMethod(selectedRule.paymentMethod));
    bodyHtml += renderDetailItem('币种', selectedRule.currencyCode || '-');
    bodyHtml += renderDetailItem('计费模式', formatPricingFeeMode(selectedRule.feeMode));
    bodyHtml += renderDetailItem('费率', formatRate(selectedRule.feeRate));
    bodyHtml += renderDetailItem('固定费用', formatMoney(selectedRule.fixedFee));
    bodyHtml += renderDetailItem('最低费用', formatMoney(selectedRule.minFee));
    bodyHtml += renderDetailItem('最高费用', formatMoney(selectedRule.maxFee));
    bodyHtml += renderDetailItem('承担方', formatPricingFeeBearer(selectedRule.feeBearer));
    bodyHtml += renderDetailItem('优先级', selectedRule.priority);
    bodyHtml += renderDetailItem('状态', formatPricingStatus(selectedRule.status));
    bodyHtml += renderDetailItem('生效开始', formatDateTime(selectedRule.validFrom));
    bodyHtml += renderDetailItem('生效结束', formatDateTime(selectedRule.validTo));
    bodyHtml += renderDetailItem('创建人', selectedRule.createdBy || '-');
    bodyHtml += renderDetailItem('更新人', selectedRule.updatedBy || '-');
    bodyHtml += renderDetailItem('创建时间', formatDateTime(selectedRule.createdAt));
    bodyHtml += renderDetailItem('更新时间', formatDateTime(selectedRule.updatedAt));
    bodyHtml += renderDetailItem('规则JSON', selectedRule.rulePayload || '-');
    bodyHtml += '</div>';
    return renderManagerModal({
      title: '计费规则详情',
      description: '详情查看通过弹窗完成，不再占据列表页空间。',
      closeAction: 'hide-pricing-rule-detail',
      bodyHtml: bodyHtml,
    });
  }

  function renderUserActions(user) {
    var userId = user.userId;
    var status = String(user.accountStatus || '').toUpperCase();
    var actions = [];
    actions.push(
      '<button class="manager-btn" type="button" data-action="load-user-detail" data-user-id="' +
        escapeHtml(userId) +
        '">详情</button>'
    );
    if (status !== 'ACTIVE') {
      actions.push(
        '<button class="manager-btn" type="button" data-action="change-user-status" data-user-id="' +
          escapeHtml(userId) +
          '" data-target-status="ACTIVE">激活</button>'
      );
    }
    if (status !== 'FROZEN') {
      actions.push(
        '<button class="manager-btn" type="button" data-action="change-user-status" data-user-id="' +
          escapeHtml(userId) +
          '" data-target-status="FROZEN">冻结</button>'
      );
    }
    if (status !== 'CLOSED') {
      actions.push(
        '<button class="manager-btn" type="button" data-action="change-user-status" data-user-id="' +
          escapeHtml(userId) +
          '" data-target-status="CLOSED">关闭</button>'
      );
    }
    return actions.join(' ');
  }

  function renderPricingRuleActions(rule) {
    var actions = [];
    actions.push(
      '<button class="manager-btn" type="button" data-action="load-pricing-rule" data-rule-id="' +
        escapeHtml(rule.ruleId) +
        '">详情</button>'
    );
    if (String(rule.status || '').toUpperCase() !== 'ACTIVE') {
      actions.push(
        '<button class="manager-btn" type="button" data-action="change-pricing-rule-status" data-rule-id="' +
          escapeHtml(rule.ruleId) +
          '" data-target-status="ACTIVE">启用</button>'
      );
    }
    if (String(rule.status || '').toUpperCase() !== 'INACTIVE') {
      actions.push(
        '<button class="manager-btn" type="button" data-action="change-pricing-rule-status" data-rule-id="' +
          escapeHtml(rule.ruleId) +
          '" data-target-status="INACTIVE">停用</button>'
      );
    }
    return actions.join(' ');
  }

  function summarizePricingRules(rules) {
    var summary = {
      total: 0,
      active: 0,
      draft: 0,
      inactive: 0,
    };
    (rules || []).forEach(function (rule) {
      summary.total += 1;
      var status = String(rule.status || '').toUpperCase();
      if (status === 'ACTIVE') {
        summary.active += 1;
      } else if (status === 'DRAFT') {
        summary.draft += 1;
      } else if (status === 'INACTIVE') {
        summary.inactive += 1;
      }
    });
    return summary;
  }

  function renderUserAccountStatusOptions(selected, withAll) {
    var statuses = ['ACTIVE', 'FROZEN', 'CLOSED'];
    var html = '';
    if (withAll) {
      html += '<option value="">全部</option>';
    }
    html += statuses
      .map(function (status) {
        var isSelected = String(selected || '').toUpperCase() === status ? ' selected' : '';
        return '<option value="' + status + '"' + isSelected + '>' + formatUserAccountStatus(status) + '</option>';
      })
      .join('');
    return html;
  }

  function formatUserAccountStatus(status) {
    var normalized = String(status || '').toUpperCase();
    if (normalized === 'ACTIVE') {
      return langText('正常', 'Active');
    }
    if (normalized === 'FROZEN') {
      return langText('冻结', 'Frozen');
    }
    if (normalized === 'CLOSED') {
      return langText('关闭', 'Closed');
    }
    return normalized || '-';
  }

  function renderUserKycLevelOptions(selected, withAll) {
    var levels = ['L0', 'L1', 'L2', 'L3'];
    var html = '';
    if (withAll) {
      html += '<option value="">全部</option>';
    }
    html += levels
      .map(function (level) {
        var isSelected = String(selected || '').toUpperCase() === level ? ' selected' : '';
        return '<option value="' + level + '"' + isSelected + '>' + formatUserKycLevel(level) + '</option>';
      })
      .join('');
    return html;
  }

  function renderPricingStatusOptions(selected, withAll) {
    var statuses = ['DRAFT', 'ACTIVE', 'INACTIVE'];
    var html = '';
    if (withAll) {
      html += '<option value="">全部</option>';
    }
    html += statuses
      .map(function (status) {
        var isSelected = String(selected || '').toUpperCase() === status ? ' selected' : '';
        return '<option value="' + status + '"' + isSelected + '>' + formatPricingStatus(status) + '</option>';
      })
      .join('');
    return html;
  }

  function renderPricingFeeModeOptions(selected) {
    var modes = ['RATE', 'FIXED', 'RATE_PLUS_FIXED'];
    return modes
      .map(function (mode) {
        var isSelected = String(selected || '').toUpperCase() === mode ? ' selected' : '';
        return '<option value="' + mode + '"' + isSelected + '>' + formatPricingFeeMode(mode) + '</option>';
      })
      .join('');
  }

  function renderPricingFeeBearerOptions(selected) {
    var bearers = ['PAYER', 'PAYEE', 'PLATFORM'];
    return bearers
      .map(function (bearer) {
        var isSelected = String(selected || '').toUpperCase() === bearer ? ' selected' : '';
        return '<option value="' + bearer + '"' + isSelected + '>' + formatPricingFeeBearer(bearer) + '</option>';
      })
      .join('');
  }

  async function ensureUserCenterData(forceReload) {
    if (state.userCenter.loading) {
      return;
    }
    if (!forceReload && state.userCenter.loaded) {
      return;
    }

    state.userCenter.loading = true;
    state.userCenter.listLoading = true;
    try {
      setUserCenterNotice('正在加载用户中心数据...');
      renderContent();
      await Promise.all([loadUserCenterSummary(), loadUserList()]);
      state.userCenter.loaded = true;
      setUserCenterNotice('用户中心数据已加载');
    } catch (error) {
      setUserCenterNotice(error && error.message ? error.message : '用户中心数据加载失败');
    } finally {
      state.userCenter.listLoading = false;
      state.userCenter.loading = false;
    }
  }

  async function queryUsersFromForm() {
    state.userCenter.filter.keyword = readInputValue('userFilterKeyword');
    state.userCenter.filter.accountStatus = readInputValue('userFilterAccountStatus').toUpperCase();
    state.userCenter.filter.kycLevel = readInputValue('userFilterKycLevel').toUpperCase();
    state.userCenter.filter.pageNo = readInputValue('userFilterPageNo') || '1';
    state.userCenter.filter.pageSize = readInputValue('userFilterPageSize') || '20';
    await executeUserListQuery();
  }

  async function queryUsersByOffset(offset) {
    var delta = Number(offset);
    if (!Number.isFinite(delta) || delta === 0) {
      return;
    }
    var filter = state.userCenter.filter || {};
    var currentPageNo = parsePositiveInteger(String(filter.pageNo || '1'), '页码');
    var targetPageNo = currentPageNo + Math.trunc(delta);
    if (targetPageNo <= 0) {
      targetPageNo = 1;
    }
    if (delta > 0 && !state.userCenter.hasNextPage) {
      return;
    }
    if (targetPageNo === currentPageNo) {
      return;
    }
    state.userCenter.filter.pageNo = String(targetPageNo);
    await executeUserListQuery();
  }

  async function executeUserListQuery() {
    try {
      state.userCenter.listLoading = true;
      setUserCenterNotice('正在查询用户...');
      renderContent();
      await loadUserList();
      // Refresh summary in background to avoid blocking list query.
      loadUserCenterSummary()
        .then(function () {
          renderContent();
        })
        .catch(function () {
          // keep current summary if refresh fails
        });
      setUserCenterNotice('用户查询完成，共 ' + String((state.userCenter.users || []).length) + ' 条');
    } catch (error) {
      state.userCenter.hasNextPage = false;
      setUserCenterNotice(error && error.message ? error.message : '用户查询失败');
    } finally {
      state.userCenter.listLoading = false;
    }
    renderContent();
  }

  async function loadUserDetail(userId, renderAfterLoad) {
    try {
      var normalizedUserId = parsePositiveLongText(String(userId || '').trim(), '用户ID');
      state.userCenter.selectedUser = {
        loading: true,
        account: {
          userId: normalizedUserId,
        },
      };
      if (renderAfterLoad) {
        renderContent();
      }
      state.userCenter.selectedUser = await requestJson('/api/admin/users/' + encodeURIComponent(normalizedUserId), {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
    } catch (error) {
      state.userCenter.selectedUser = null;
      setUserCenterNotice(error && error.message ? error.message : '用户详情加载失败');
    }
    if (renderAfterLoad) {
      renderContent();
    }
  }

  async function changeUserStatus(userId, status) {
    try {
      var normalizedUserId = parsePositiveLongText(String(userId || '').trim(), '用户ID');
      var normalizedStatus = requireText(String(status || '').trim().toUpperCase(), '目标状态');
      setUserCenterNotice('正在变更用户状态...');
      renderContent();
      await requestJson('/api/admin/users/' + encodeURIComponent(normalizedUserId) + '/status', {
        method: 'PUT',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          status: normalizedStatus,
          operator: (state.session && state.session.username) || 'admin',
        }),
      });
      await Promise.all([loadUserList(), loadUserCenterSummary()]);
      if (state.userCenter.selectedUser && state.userCenter.selectedUser.account && state.userCenter.selectedUser.account.userId === normalizedUserId) {
        await loadUserDetail(normalizedUserId, false);
      }
      setUserCenterNotice('用户状态已更新为 ' + normalizedStatus);
    } catch (error) {
      setUserCenterNotice(error && error.message ? error.message : '变更用户状态失败');
    }
    renderContent();
  }

  async function loadUserCenterSummary() {
    state.userCenter.summary = await requestJson('/api/admin/users/summary', {
      method: 'GET',
      headers: buildAdminHeaders(),
    });
  }

  async function loadUserList() {
    var filter = state.userCenter.filter || {};
    var pageNo = parsePositiveInteger(String(filter.pageNo || '1'), '页码');
    var pageSize = parsePositiveInteger(String(filter.pageSize || '20'), '每页条数');
    pageSize = Math.min(pageSize, 200);
    state.userCenter.filter.pageNo = String(pageNo);
    state.userCenter.filter.pageSize = String(pageSize);
    var query = buildQueryString({
      keyword: filter.keyword || '',
      accountStatus: (filter.accountStatus || '').toUpperCase(),
      kycLevel: (filter.kycLevel || '').toUpperCase(),
      pageNo: String(pageNo),
      pageSize: String(pageSize),
    });
    var userRows = await requestJson('/api/admin/users' + query, {
      method: 'GET',
      headers: buildAdminHeaders(),
    });
    state.userCenter.users = Array.isArray(userRows) ? userRows : [];
    state.userCenter.hasNextPage = state.userCenter.users.length >= pageSize;
  }

  async function ensurePricingData(forceReload) {
    if (state.pricing.loading) {
      return;
    }
    if (!forceReload && state.pricing.loaded) {
      return;
    }

    state.pricing.loading = true;
    state.pricing.ruleLoading = true;
    try {
      setPricingNotice('正在加载计费规则...');
      renderContent();
      await loadPricingRules();
      state.pricing.loaded = true;
      setPricingNotice('计费规则已加载');
    } catch (error) {
      setPricingNotice(error && error.message ? error.message : '计费规则加载失败');
    } finally {
      state.pricing.ruleLoading = false;
      state.pricing.loading = false;
    }
  }

  async function queryPricingRulesFromForm() {
    state.pricing.filter.businessSceneCode = readInputValue('pricingFilterScene').toUpperCase();
    state.pricing.filter.paymentMethod = readInputValue('pricingFilterMethod').toUpperCase();
    state.pricing.filter.status = readInputValue('pricingFilterStatus').toUpperCase();
    try {
      state.pricing.ruleLoading = true;
      setPricingNotice('正在查询计费规则...');
      renderContent();
      await loadPricingRules();
      setPricingNotice('规则查询完成，共 ' + String((state.pricing.rules || []).length) + ' 条');
    } catch (error) {
      setPricingNotice(error && error.message ? error.message : '计费规则查询失败');
    } finally {
      state.pricing.ruleLoading = false;
    }
    renderContent();
  }

  async function createPricingRuleFromForm() {
    try {
      var ruleCode = requireText(readInputValue('pricingRuleCode').toUpperCase(), '规则编码');
      var ruleName = requireText(readInputValue('pricingRuleName'), '规则名称');
      var businessSceneCode = requireText(readInputValue('pricingRuleBusinessSceneCode').toUpperCase(), '业务场景');
      var paymentMethod = requireText(readInputValue('pricingRulePaymentMethod').toUpperCase(), '支付方式');
      var currencyCode = requireText(readInputValue('pricingRuleCurrencyCode').toUpperCase(), '币种');
      var feeMode = requireText(readInputValue('pricingRuleFeeMode').toUpperCase(), '计费模式');
      var feeBearer = requireText(readInputValue('pricingRuleFeeBearer').toUpperCase(), '手续费承担方');
      var priority = parseNonNegativeInteger(readInputValue('pricingRulePriority') || '100', '优先级');
      var validFrom = normalizeDateTimeInputOptional(readInputValue('pricingRuleValidFrom'));
      var validTo = normalizeDateTimeInputOptional(readInputValue('pricingRuleValidTo'));
      var initialStatus = readInputValue('pricingRuleInitialStatus').toUpperCase() || 'DRAFT';
      var rulePayload = readInputValue('pricingRulePayload');

      var feeRate = parseRateOptional(readInputValue('pricingRuleFeeRate'));
      var fixedFee = parseMoneyOptionalWithCurrency(readInputValue('pricingRuleFixedFee'), currencyCode);
      var minFee = parseMoneyOptionalWithCurrency(readInputValue('pricingRuleMinFee'), currencyCode);
      var maxFee = parseMoneyOptionalWithCurrency(readInputValue('pricingRuleMaxFee'), currencyCode);

      validatePricingExpression(feeMode, feeRate, fixedFee);

      setPricingNotice('正在创建计费规则...');
      renderContent();

      var created = await requestJson('/api/admin/pricing-rules', {
        method: 'POST',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          ruleCode: ruleCode,
          ruleName: ruleName,
          businessSceneCode: businessSceneCode,
          paymentMethod: paymentMethod,
          currencyCode: currencyCode,
          feeMode: feeMode,
          feeRate: feeRate,
          fixedFee: fixedFee,
          minFee: minFee,
          maxFee: maxFee,
          feeBearer: feeBearer,
          priority: priority,
          validFrom: validFrom,
          validTo: validTo,
          rulePayload: rulePayload || null,
          initialStatus: initialStatus,
          operator: (state.session && state.session.username) || 'admin',
        }),
      });

      state.pricing.selectedRule = created;
      state.pricing.createVisible = false;
      await loadPricingRules();
      setPricingNotice('计费规则创建成功：' + ruleCode);
    } catch (error) {
      setPricingNotice(error && error.message ? error.message : '创建计费规则失败');
    }
    renderContent();
  }

  async function changePricingRuleStatus(ruleId, targetStatus) {
    try {
      setPricingNotice('正在变更计费规则状态...');
      renderContent();
      await requestJson('/api/admin/pricing-rules/' + ruleId + '/status', {
        method: 'PUT',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          status: String(targetStatus || '').toUpperCase(),
          operator: (state.session && state.session.username) || 'admin',
        }),
      });
      await loadPricingRules();
      if (state.pricing.selectedRule && Number(state.pricing.selectedRule.ruleId) === Number(ruleId)) {
        await loadPricingRuleDetail(ruleId, false);
      }
      setPricingNotice('计费规则状态已更新为 ' + String(targetStatus || '').toUpperCase());
    } catch (error) {
      setPricingNotice(error && error.message ? error.message : '变更计费规则状态失败');
    }
    renderContent();
  }

  async function loadPricingRuleDetail(ruleId, renderAfterLoad) {
    try {
      var normalizedRuleId = parsePositiveInteger(String(ruleId || ''), '规则ID');
      state.pricing.selectedRule = await requestJson('/api/admin/pricing-rules/' + normalizedRuleId, {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
      state.pricing.detailVisible = true;
      setPricingNotice('规则详情加载成功：' + normalizedRuleId);
    } catch (error) {
      setPricingNotice(error && error.message ? error.message : '规则详情加载失败');
    }
    if (renderAfterLoad) {
      renderContent();
    }
  }

  async function loadPricingRules() {
    var filter = state.pricing.filter || {};
    var query = buildQueryString({
      businessSceneCode: filter.businessSceneCode || '',
      paymentMethod: filter.paymentMethod || '',
      status: filter.status || '',
    });
    state.pricing.rules = await requestJson('/api/admin/pricing-rules' + query, {
      method: 'GET',
      headers: buildAdminHeaders(),
    });
  }

  function renderMenuTreeForRole(allMenus, selectedMenuCodes) {
    if (!allMenus || allMenus.length === 0) {
      return renderManagerBlockState(isManagerDataLoading(state.rbac.loaded, state.rbac.loading, state.rbac.notice), '菜单数据加载中...', '暂无菜单', 'manager-rbac-empty');
    }

    var tree = buildMenuTree(allMenus);
    var selectedSet = toSet(selectedMenuCodes || []);

    return (
      '<div class="manager-rbac-tree">' +
      tree
        .map(function (root) {
          return renderMenuTreeNode(root, selectedSet);
        })
        .join('') +
      '</div>'
    );
  }

  function renderMenuTreeNode(node, selectedSet) {
    var checked = selectedSet.has(node.menuCode) ? ' checked' : '';
    var children = node.children || [];

    var html = '';
    html += '<div class="manager-rbac-tree-parent">';
    html += '<label class="manager-rbac-tree-parent-title">';
    html +=
      '<input type="checkbox" name="role-menu-checkbox" value="' +
      escapeHtml(node.menuCode) +
      '"' +
      checked +
      '/>';
    html +=
      '<span>' +
      escapeHtml(localizeMenuName(node.menuName, node.path, node.menuCode)) +
      ' <span style="color:#7489a8;">(' +
      escapeHtml(node.menuCode) +
      ')</span></span>';
    html += '</label>';

    if (children.length > 0) {
      html += '<div class="manager-rbac-tree-children">';
      html += children
        .map(function (child) {
          var childChecked = selectedSet.has(child.menuCode) ? ' checked' : '';
          return (
            '<label class="manager-rbac-check-item">' +
            '<input type="checkbox" name="role-menu-checkbox" value="' +
            escapeHtml(child.menuCode) +
            '"' +
            childChecked +
            '/>' +
            '<span>' +
            escapeHtml(localizeMenuName(child.menuName, child.path, child.menuCode)) +
            '</span>' +
            '</label>'
          );
        })
        .join('');
      html += '</div>';
    }

    html += '</div>';
    return html;
  }

  function renderPermissionTreeForRole(modules, permissions, selectedPermissionCodes) {
    if (!permissions || permissions.length === 0) {
      return renderManagerBlockState(isManagerDataLoading(state.rbac.loaded, state.rbac.loading, state.rbac.notice), '权限数据加载中...', '暂无权限', 'manager-rbac-empty');
    }

    var moduleMap = {};
    (modules || []).forEach(function (module) {
      moduleMap[module.moduleCode] = module.moduleName;
    });

    var grouped = {};
    permissions.forEach(function (permission) {
      var code = permission.moduleCode || 'UNSPECIFIED';
      if (!grouped[code]) {
        grouped[code] = [];
      }
      grouped[code].push(permission);
    });

    var selectedSet = toSet(selectedPermissionCodes || []);

    var html = '<div class="manager-rbac-perm-list">';
    Object.keys(grouped)
      .sort()
      .forEach(function (moduleCode) {
        var title = localizeModuleName(moduleMap[moduleCode] || moduleCode, moduleCode);
        html += '<div class="manager-rbac-perm-module">';
        html += '<div class="manager-rbac-perm-title">' + escapeHtml(title) + '</div>';
        html += '<div class="manager-rbac-perm-items">';
        html += grouped[moduleCode]
          .map(function (permission) {
            var checked = selectedSet.has(permission.permissionCode) ? ' checked' : '';
            return (
              '<label class="manager-rbac-check-item">' +
              '<input type="checkbox" name="role-permission-checkbox" value="' +
              escapeHtml(permission.permissionCode) +
              '"' +
              checked +
              '/>' +
              '<span title="' +
              escapeHtml(permission.permissionCode) +
              '">' +
              escapeHtml(permission.permissionName) +
              '</span>' +
              '</label>'
            );
          })
          .join('');
        html += '</div>';
        html += '</div>';
      });
    html += '</div>';

    return html;
  }

  async function saveAdminRoles() {
    if (!state.rbac.selectedAdminId) {
      setRbacNotice('请先选择管理员');
      renderContent();
      return;
    }

    var roleCodes = collectCheckedValues('admin-role-checkbox');

    try {
      setRbacNotice('正在保存管理员角色...');
      renderContent();

      await requestJson('/api/admin/rbac/admins/' + state.rbac.selectedAdminId + '/roles', {
        method: 'PUT',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          roleCodes: roleCodes,
          operator: (state.session && state.session.username) || 'system',
        }),
      });

      await ensureRbacData(true);
      await refreshPageInit();
      setRbacNotice('管理员角色保存成功');
      renderShell();
    } catch (error) {
      setRbacNotice(error && error.message ? error.message : '保存管理员角色失败');
      renderContent();
    }
  }

  async function saveRoleMenus() {
    var roleCode = state.rbac.selectedRoleCode;
    if (!roleCode) {
      setRbacNotice('请先选择角色');
      renderContent();
      return;
    }

    var menuCodes = collectCheckedValues('role-menu-checkbox');

    try {
      setRbacNotice('正在保存角色菜单...');
      renderContent();

      await requestJson('/api/admin/rbac/roles/' + encodeURIComponent(roleCode) + '/menus', {
        method: 'PUT',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          menuCodes: menuCodes,
          operator: (state.session && state.session.username) || 'system',
        }),
      });

      await ensureRbacData(true);
      await refreshPageInit();
      setRbacNotice('角色菜单保存成功');
      renderShell();
    } catch (error) {
      setRbacNotice(error && error.message ? error.message : '保存角色菜单失败');
      renderContent();
    }
  }

  async function saveRolePermissions() {
    var roleCode = state.rbac.selectedRoleCode;
    if (!roleCode) {
      setRbacNotice('请先选择角色');
      renderContent();
      return;
    }

    var permissionCodes = collectCheckedValues('role-permission-checkbox');

    try {
      setRbacNotice('正在保存角色权限...');
      renderContent();

      await requestJson('/api/admin/rbac/roles/' + encodeURIComponent(roleCode) + '/permissions', {
        method: 'PUT',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          permissionCodes: permissionCodes,
          operator: (state.session && state.session.username) || 'system',
        }),
      });

      await ensureRbacData(true);
      await refreshPageInit();
      setRbacNotice('角色权限保存成功');
      renderShell();
    } catch (error) {
      setRbacNotice(error && error.message ? error.message : '保存角色权限失败');
      renderContent();
    }
  }

  function collectCheckedValues(inputName) {
    return Array.prototype.slice
      .call(document.querySelectorAll('input[name="' + inputName + '"]:checked'))
      .map(function (input) {
        return input.value;
      })
      .filter(function (value) {
        return Boolean(value);
      });
  }

  async function queryTradeByTradeNoFromForm() {
    var tradeOrderNo = requireText(readInputValue('tradeQueryTradeNo'), '交易号');
    state.trade.query.tradeOrderNo = tradeOrderNo;
    setTradeNotice('正在查询交易...');
    try {
      state.trade.selectedOrder = await requestJson('/api/admin/trades/' + encodeURIComponent(tradeOrderNo), {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
      state.trade.detailVisible = true;
      setTradeNotice('交易详情加载成功：' + tradeOrderNo);
      renderContent();
    } catch (error) {
      state.trade.selectedOrder = null;
      setTradeNotice(error && error.message ? error.message : '查询交易失败');
      renderContent();
    }
  }

  async function queryTradeByRequestNoFromForm() {
    var requestNo = requireText(readInputValue('tradeQueryRequestNo'), '请求号');
    state.trade.query.requestNo = requestNo;
    setTradeNotice('正在按请求号查询交易...');
    try {
      state.trade.selectedOrder = await requestJson('/api/admin/trades/by-request/' + encodeURIComponent(requestNo), {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
      state.trade.detailVisible = true;
      setTradeNotice('请求号查询成功：' + requestNo);
      renderContent();
    } catch (error) {
      state.trade.selectedOrder = null;
      setTradeNotice(error && error.message ? error.message : '按请求号查询交易失败');
      renderContent();
    }
  }

  async function queryTradeByBusinessOrderFromForm() {
    var businessDomainCode = requireText(readInputValue('tradeQueryBusinessDomainCode').toUpperCase(), '业务域');
    var bizOrderNo = requireText(readInputValue('tradeQueryBizOrderNo'), '业务单号');
    state.trade.query.businessDomainCode = businessDomainCode;
    state.trade.query.bizOrderNo = bizOrderNo;
    setTradeNotice('正在按业务域查询交易...');
    try {
      state.trade.selectedOrder = await requestJson(
        '/api/admin/trades/by-business/' + encodeURIComponent(businessDomainCode) + '/' + encodeURIComponent(bizOrderNo),
        {
          method: 'GET',
          headers: buildAdminHeaders(),
        }
      );
      state.trade.detailVisible = true;
      setTradeNotice('业务单查询成功：' + businessDomainCode + ' / ' + bizOrderNo);
      renderContent();
    } catch (error) {
      state.trade.selectedOrder = null;
      setTradeNotice(error && error.message ? error.message : '按业务域查询交易失败');
      renderContent();
    }
  }

  async function ensureAccountingData(forceReload) {
    var activeAccountingView = getAccountingActiveView(state.selectedPath);
    if (state.accounting.loading) {
      return;
    }
    if (!forceReload && state.accounting.loaded && state.accounting.loadedViews && state.accounting.loadedViews[activeAccountingView]) {
      return;
    }
    state.accounting.loading = true;
    try {
      renderContent();
      if (isPathActive(ACCOUNTING_VOUCHER_PATH)) {
        await queryAccountingVouchers(false);
      } else if (isPathActive(ACCOUNTING_ENTRY_PATH)) {
        await queryAccountingEntries(false);
      } else if (isPathActive(ACCOUNTING_SUBJECT_PATH)) {
        await loadAccountingSubjects(false);
      } else {
        await queryAccountingEvents(false);
      }
      state.accounting.loaded = true;
      state.accounting.loadedViews[activeAccountingView] = true;
      if (!state.accounting.notice) {
        setAccountingNotice('会计中心数据已加载');
      }
    } catch (error) {
      setAccountingNotice(error && error.message ? error.message : '会计中心数据加载失败');
    } finally {
      state.accounting.loading = false;
    }
  }

  async function queryAccountingEventsFromForm() {
    state.accounting.eventFilter.eventId = readInputValue('accountingEventId');
    state.accounting.eventFilter.sourceBizNo = readInputValue('accountingEventSourceBizNo');
    state.accounting.eventFilter.tradeOrderNo = readInputValue('accountingEventTradeNo');
    state.accounting.eventFilter.payOrderNo = readInputValue('accountingEventPaymentId');
    state.accounting.eventFilter.status = readInputValue('accountingEventStatus');
    state.accounting.eventFilter.limit = readInputValue('accountingEventLimit') || '20';
    await queryAccountingEvents(true);
  }

  async function queryAccountingEvents(renderAfter) {
    setAccountingNotice('正在查询会计事件...');
    state.accounting.eventLoading = true;
    if (renderAfter !== false) {
      renderContent();
    }
    try {
      var currentSelectedEvent = state.accounting.selectedEvent;
      var currentEventId = state.accounting.selectedEvent && state.accounting.selectedEvent.eventId
        ? String(state.accounting.selectedEvent.eventId)
        : '';
      state.accounting.events = await requestJson(
        '/api/admin/accounting/events' + buildQueryString({
          eventId: state.accounting.eventFilter.eventId,
          sourceBizNo: state.accounting.eventFilter.sourceBizNo,
          tradeOrderNo: state.accounting.eventFilter.tradeOrderNo,
          payOrderNo: state.accounting.eventFilter.payOrderNo,
          status: state.accounting.eventFilter.status,
          limit: state.accounting.eventFilter.limit,
        }),
        {
          method: 'GET',
          headers: buildAdminHeaders(),
        }
      );
      if (state.accounting.events.length === 0) {
        state.accounting.selectedEvent = null;
      } else {
        var existsInList = state.accounting.events.some(function (item) {
          return String(item && item.eventId || '') === currentEventId;
        });
        state.accounting.selectedEvent = existsInList && currentSelectedEvent
          ? currentSelectedEvent
          : state.accounting.events[0];
      }
      state.accounting.loadedViews.events = true;
      setAccountingNotice('会计事件查询完成，共 ' + String((state.accounting.events || []).length) + ' 条');
    } catch (error) {
      state.accounting.events = [];
      state.accounting.selectedEvent = null;
      setAccountingNotice(error && error.message ? error.message : '查询会计事件失败');
    } finally {
      state.accounting.eventLoading = false;
    }
    if (renderAfter !== false) {
      renderContent();
    }
  }

  async function loadAccountingEvent(eventId, renderAfter) {
    setAccountingNotice('正在加载会计事件详情...');
    try {
      state.accounting.selectedEvent = await requestJson('/api/admin/accounting/events/' + encodeURIComponent(eventId), {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
      state.accounting.eventDetailVisible = true;
      state.accounting.voucherDetailVisible = false;
      setAccountingNotice('会计事件详情加载成功：' + eventId);
    } catch (error) {
      setAccountingNotice(error && error.message ? error.message : '加载会计事件详情失败');
    }
    if (renderAfter !== false) {
      renderContent();
    }
  }

  async function retryAccountingEvent(eventId) {
    setAccountingNotice('正在重试会计事件...');
    try {
      var voucher = await requestJson('/api/admin/accounting/events/' + encodeURIComponent(eventId) + '/retry', {
        method: 'POST',
        headers: buildAdminHeaders(),
      });
      setAccountingNotice('会计事件重试成功，生成凭证：' + (voucher.voucherNo || '-'));
      await queryAccountingEvents(false);
      await loadAccountingEvent(eventId, false);
      state.accounting.selectedVoucher = voucher;
    } catch (error) {
      setAccountingNotice(error && error.message ? error.message : '重试会计事件失败');
    }
    renderContent();
  }

  async function queryAccountingVouchersFromForm() {
    state.accounting.voucherFilter.voucherNo = readInputValue('accountingVoucherNo');
    state.accounting.voucherFilter.sourceBizNo = readInputValue('accountingVoucherSourceBizNo');
    state.accounting.voucherFilter.tradeOrderNo = readInputValue('accountingVoucherTradeNo');
    state.accounting.voucherFilter.payOrderNo = readInputValue('accountingVoucherPaymentId');
    state.accounting.voucherFilter.status = readInputValue('accountingVoucherStatus');
    state.accounting.voucherFilter.limit = readInputValue('accountingVoucherLimit') || '20';
    await queryAccountingVouchers(true);
  }

  async function queryAccountingVouchers(renderAfter) {
    setAccountingNotice('正在查询会计凭证...');
    state.accounting.voucherLoading = true;
    if (renderAfter !== false) {
      renderContent();
    }
    try {
      var currentSelectedVoucher = state.accounting.selectedVoucher;
      var currentVoucherNo = state.accounting.selectedVoucher && state.accounting.selectedVoucher.voucherNo
        ? String(state.accounting.selectedVoucher.voucherNo)
        : '';
      state.accounting.vouchers = await requestJson(
        '/api/admin/accounting/vouchers' + buildQueryString({
          voucherNo: state.accounting.voucherFilter.voucherNo,
          sourceBizNo: state.accounting.voucherFilter.sourceBizNo,
          tradeOrderNo: state.accounting.voucherFilter.tradeOrderNo,
          payOrderNo: state.accounting.voucherFilter.payOrderNo,
          status: state.accounting.voucherFilter.status,
          limit: state.accounting.voucherFilter.limit,
        }),
        {
          method: 'GET',
          headers: buildAdminHeaders(),
        }
      );
      if (state.accounting.vouchers.length === 0) {
        state.accounting.selectedVoucher = null;
      } else {
        var existsInList = state.accounting.vouchers.some(function (item) {
          return String(item && item.voucherNo || '') === currentVoucherNo;
        });
        state.accounting.selectedVoucher = existsInList && currentSelectedVoucher
          ? currentSelectedVoucher
          : state.accounting.vouchers[0];
      }
      state.accounting.loadedViews.vouchers = true;
      setAccountingNotice('会计凭证查询完成，共 ' + String((state.accounting.vouchers || []).length) + ' 条');
    } catch (error) {
      state.accounting.vouchers = [];
      state.accounting.selectedVoucher = null;
      setAccountingNotice(error && error.message ? error.message : '查询会计凭证失败');
    } finally {
      state.accounting.voucherLoading = false;
    }
    if (renderAfter !== false) {
      renderContent();
    }
  }

  async function loadAccountingVoucher(voucherNo, renderAfter) {
    setAccountingNotice('正在加载会计凭证详情...');
    try {
      state.accounting.selectedVoucher = await requestJson('/api/admin/accounting/vouchers/' + encodeURIComponent(voucherNo), {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
      state.accounting.voucherDetailVisible = true;
      state.accounting.eventDetailVisible = false;
      setAccountingNotice('会计凭证详情加载成功：' + voucherNo);
    } catch (error) {
      setAccountingNotice(error && error.message ? error.message : '加载会计凭证详情失败');
    }
    if (renderAfter !== false) {
      renderContent();
    }
  }

  async function reverseAccountingVoucher(voucherNo) {
    var reverseReason = window.prompt('请输入冲正原因', '后台人工冲正');
    if (reverseReason == null) {
      return;
    }
    reverseReason = String(reverseReason || '').trim();
    if (!reverseReason) {
      setAccountingNotice('冲正原因不能为空');
      renderContent();
      return;
    }
    setAccountingNotice('正在执行会计凭证冲正...');
    try {
      state.accounting.selectedVoucher = await requestJson('/api/admin/accounting/vouchers/' + encodeURIComponent(voucherNo) + '/reverse', {
        method: 'POST',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          reverseReason: reverseReason,
        }),
      });
      setAccountingNotice('会计凭证冲正成功：原凭证 ' + voucherNo + '，冲正凭证 ' + (state.accounting.selectedVoucher.voucherNo || '-'));
      await queryAccountingVouchers(false);
    } catch (error) {
      setAccountingNotice(error && error.message ? error.message : '会计凭证冲正失败');
    }
    renderContent();
  }

  async function queryAccountingEntriesFromForm() {
    state.accounting.entryFilter.voucherNo = readInputValue('accountingEntryVoucherNo');
    state.accounting.entryFilter.subjectCode = readInputValue('accountingEntrySubjectCode');
    state.accounting.entryFilter.ownerId = readInputValue('accountingEntryOwnerId');
    state.accounting.entryFilter.payOrderNo = readInputValue('accountingEntryPaymentId');
    state.accounting.entryFilter.limit = readInputValue('accountingEntryLimit') || '50';
    await queryAccountingEntries(true);
  }

  async function queryAccountingEntries(renderAfter) {
    setAccountingNotice('正在查询会计分录...');
    state.accounting.entryLoading = true;
    if (renderAfter !== false) {
      renderContent();
    }
    try {
      state.accounting.entries = await requestJson(
        '/api/admin/accounting/entries' + buildQueryString({
          voucherNo: state.accounting.entryFilter.voucherNo,
          subjectCode: state.accounting.entryFilter.subjectCode,
          ownerId: state.accounting.entryFilter.ownerId,
          payOrderNo: state.accounting.entryFilter.payOrderNo,
          limit: state.accounting.entryFilter.limit,
        }),
        {
          method: 'GET',
          headers: buildAdminHeaders(),
        }
      );
      state.accounting.loadedViews.entries = true;
      setAccountingNotice('会计分录查询完成，共 ' + String((state.accounting.entries || []).length) + ' 条');
    } catch (error) {
      state.accounting.entries = [];
      setAccountingNotice(error && error.message ? error.message : '查询会计分录失败');
    } finally {
      state.accounting.entryLoading = false;
    }
    if (renderAfter !== false) {
      renderContent();
    }
  }

  async function loadAccountingSubjects(renderAfter) {
    setAccountingNotice('正在加载会计科目...');
    state.accounting.subjectLoading = true;
    if (renderAfter !== false) {
      renderContent();
    }
    try {
      state.accounting.subjects = await requestJson('/api/admin/accounting/subjects' + buildQueryString({ limit: '200' }), {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
      if (state.accounting.selectedSubjectCode) {
        selectAccountingSubjectByCode(state.accounting.selectedSubjectCode);
      } else if (!state.accounting.subjectDraft || !state.accounting.subjectDraft.subjectCode) {
        clearAccountingSubjectDraft();
      }
      state.accounting.loadedViews.subjects = true;
      setAccountingNotice('会计科目已加载，共 ' + String((state.accounting.subjects || []).length) + ' 条');
    } catch (error) {
      state.accounting.subjects = [];
      clearAccountingSubjectDraft();
      setAccountingNotice(error && error.message ? error.message : '加载会计科目失败');
    } finally {
      state.accounting.subjectLoading = false;
    }
    if (renderAfter !== false) {
      renderContent();
    }
  }

  async function saveAccountingSubjectFromForm() {
    var subjectCode = requireText(readInputValue('accountingSubjectCode'), '科目编码');
    var subjectName = requireText(readInputValue('accountingSubjectName'), '科目名称');
    setAccountingNotice('正在保存会计科目...');
    try {
      var savedSubject = await requestJson('/api/admin/accounting/subjects/' + encodeURIComponent(subjectCode), {
        method: 'PUT',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          subjectName: subjectName,
          subjectType: requireText(readInputValue('accountingSubjectType'), '科目类型'),
          balanceDirection: requireText(readInputValue('accountingSubjectBalanceDirection'), '余额方向'),
          parentSubjectCode: readInputValue('accountingSubjectParentCode') || null,
          levelNo: Number(readInputValue('accountingSubjectLevelNo') || '1'),
          enabled: String(readInputValue('accountingSubjectEnabled')) !== 'false',
          remark: readInputValue('accountingSubjectRemark') || null,
        }),
      });
      selectAccountingSubject(savedSubject);
      setAccountingNotice('会计科目保存成功：' + subjectCode + ' / ' + (savedSubject.subjectName || '-'));
      await loadAccountingSubjects(false);
    } catch (error) {
      setAccountingNotice(error && error.message ? error.message : '保存会计科目失败');
    }
    renderContent();
  }

  async function changeAccountingSubjectStatus(subjectCode, enabled) {
    setAccountingNotice(enabled ? '正在启用会计科目...' : '正在停用会计科目...');
    try {
      var updatedSubject = await requestJson('/api/admin/accounting/subjects/' + encodeURIComponent(subjectCode) + '/status', {
        method: 'POST',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify({
          enabled: !!enabled,
        }),
      });
      state.accounting.selectedSubjectCode = updatedSubject.subjectCode || '';
      setAccountingNotice('会计科目状态更新成功：' + (updatedSubject.subjectCode || '-') + ' / ' + (updatedSubject.enabled ? '已启用' : '已停用'));
      await loadAccountingSubjects(false);
    } catch (error) {
      setAccountingNotice(error && error.message ? error.message : '更新会计科目状态失败');
    }
    renderContent();
  }

  async function initializeAccountingSubjects() {
    if (!window.confirm('初始化仅补齐缺失标准科目，不会覆盖现有科目配置。是否继续？')) {
      return;
    }
    setAccountingNotice('正在初始化标准会计科目...');
    try {
      var result = await requestJson('/api/admin/accounting/subjects/initialize-standard', {
        method: 'POST',
        headers: buildAdminHeaders(),
      });
      setAccountingNotice(result && result.message ? result.message : '标准会计科目初始化完成');
      await loadAccountingSubjects(false);
    } catch (error) {
      setAccountingNotice(error && error.message ? error.message : '初始化标准会计科目失败');
    }
    renderContent();
  }

  async function resetAccountingSubjectsToStandard() {
    if (!window.confirm('重置会恢复标准科目的名称、层级、启停状态，并停用非标准科目。是否继续？')) {
      return;
    }
    setAccountingNotice('正在重置标准会计科目...');
    try {
      var result = await requestJson('/api/admin/accounting/subjects/reset-standard', {
        method: 'POST',
        headers: buildAdminHeaders(),
      });
      setAccountingNotice(result && result.message ? result.message : '标准会计科目重置完成');
      await loadAccountingSubjects(false);
    } catch (error) {
      setAccountingNotice(error && error.message ? error.message : '重置标准会计科目失败');
    }
    renderContent();
  }

  async function ensureRbacData(forceReload) {
    if (state.rbac.loading) {
      return;
    }
    if (!forceReload && state.rbac.loaded) {
      return;
    }

    state.rbac.loading = true;
    try {
      renderContent();
      var headers = buildAdminHeaders();
      var result = await Promise.all([
        requestJson('/api/admin/rbac/admins', { method: 'GET', headers: headers }),
        requestJson('/api/admin/rbac/roles', { method: 'GET', headers: headers }),
        requestJson('/api/admin/rbac/permissions', { method: 'GET', headers: headers }),
        requestJson('/api/admin/rbac/modules', { method: 'GET', headers: headers }),
        requestJson('/api/admin/rbac/menus', { method: 'GET', headers: headers }),
      ]);

      state.rbac.admins = result[0] || [];
      state.rbac.roles = result[1] || [];
      state.rbac.permissions = result[2] || [];
      state.rbac.modules = result[3] || [];
      state.rbac.menus = result[4] || [];

      updateRbacDefaultAdmin();
      await loadAdminAuthorization(state.rbac.selectedAdminId, true);
      updateRbacDefaultRole();
      state.rbac.loaded = true;
      if (!state.rbac.notice) {
        state.rbac.notice = '权限数据已加载';
      }
    } catch (error) {
      state.rbac.notice = error && error.message ? error.message : '权限数据加载失败';
    } finally {
      state.rbac.loading = false;
    }
  }

  function updateRbacDefaultAdmin() {
    var admins = state.rbac.admins || [];
    if (admins.length === 0) {
      state.rbac.selectedAdminId = null;
      return;
    }

    var currentId = normalizePositiveIdText(state.session && state.session.adminId);
    var byCurrent = admins.find(function (item) {
      return samePositiveId(item.adminId, currentId);
    });

    if (byCurrent) {
      state.rbac.selectedAdminId = byCurrent.adminId;
      return;
    }

    var existingSelected = admins.find(function (item) {
      return samePositiveId(item.adminId, state.rbac.selectedAdminId);
    });
    if (existingSelected) {
      state.rbac.selectedAdminId = existingSelected.adminId;
      return;
    }

    state.rbac.selectedAdminId = admins[0].adminId;
  }

  function updateRbacDefaultRole() {
    var roles = state.rbac.roles || [];
    if (roles.length === 0) {
      state.rbac.selectedRoleCode = null;
      return;
    }

    var selectedAdminId = normalizePositiveIdText(state.rbac.selectedAdminId);
    var auth = state.rbac.authorizationMap[selectedAdminId];
    var authRoleCodes = auth ? auth.roleCodes || [] : [];

    var current = roles.find(function (role) {
      return role.roleCode === state.rbac.selectedRoleCode;
    });
    if (current) {
      return;
    }

    var roleFromAuth = roles.find(function (role) {
      return authRoleCodes.indexOf(role.roleCode) >= 0;
    });
    if (roleFromAuth) {
      state.rbac.selectedRoleCode = roleFromAuth.roleCode;
      return;
    }

    state.rbac.selectedRoleCode = roles[0].roleCode;
  }

  async function loadAdminAuthorization(adminId, forceReload) {
    var normalizedAdminId = normalizePositiveIdText(adminId);
    if (!normalizedAdminId) {
      return;
    }

    if (!forceReload && state.rbac.authorizationMap[normalizedAdminId]) {
      return;
    }

    var auth = await requestJson('/api/admin/rbac/admins/' + normalizedAdminId + '/authorizations', {
      method: 'GET',
      headers: buildAdminHeaders(),
    });
    if (auth) {
      auth.adminId = normalizePositiveIdText(auth.adminId) || normalizedAdminId;
    }
    state.rbac.authorizationMap[normalizedAdminId] = auth;
  }


  async function ensureAppCenterData(forceReload) {
    if (state.appCenter.loaded && !forceReload) {
      if (state.appCenter.selectedApp && (!state.appCenter.versions || state.appCenter.versions.length === 0)) {
        await selectAppCenterApp(state.appCenter.selectedApp.appCode, false);
      }
      if (
        isPathActive(APP_BEHAVIOR_PATH) &&
        state.appCenter.selectedApp &&
        state.appCenter.selectedApp.appCode &&
        !state.appCenter.behaviorLoading &&
        !state.appCenter.behaviorStatsLoading &&
        !state.appCenter.behaviorReportLoading
      ) {
        var hasEvents = Array.isArray(state.appCenter.behaviorEvents) && state.appCenter.behaviorEvents.length > 0;
        var hasStats = !!state.appCenter.behaviorStats;
        var hasReport = !!state.appCenter.behaviorReport;
        if (!hasEvents && !hasStats && !hasReport) {
          await loadAppBehaviorPanelData(state.appCenter.selectedApp.appCode, false);
        }
      }
      return;
    }
    state.appCenter.loading = true;
    renderContent();
    try {
      state.appCenter.apps = await requestJson('/api/admin/apps', {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
      state.appCenter.loaded = true;
      if (state.appCenter.selectedApp && state.appCenter.selectedApp.appCode) {
        await selectAppCenterApp(state.appCenter.selectedApp.appCode, false);
      } else if (state.appCenter.apps.length > 0) {
        await selectAppCenterApp(state.appCenter.apps[0].appCode, false);
      }
      setAppCenterNotice('应用版本数据已刷新');
    } catch (error) {
      setAppCenterNotice(error && error.message ? error.message : '应用版本数据加载失败');
    } finally {
      state.appCenter.loading = false;
    }
  }

  async function selectAppCenterApp(appCode, renderAfterLoad) {
    var normalizedAppCode = requireText(String(appCode || '').trim(), '应用Code');
    state.appCenter.detailLoading = true;
    state.appCenter.selectedApp = (state.appCenter.apps || []).find(function (item) {
      return String(item && item.appCode || '') === normalizedAppCode;
    }) || { appCode: normalizedAppCode };
    state.appCenter.versionFormMode = 'CREATE';
    state.appCenter.versionEditingCode = '';
    state.appCenter.versionDraft = null;
    state.appCenter.versionFormNotice = '';
    state.appCenter.versionSubmitting = false;
    state.appCenter.selectedDevice = null;
    state.appCenter.visitRecords = [];
    state.appCenter.behaviorEvents = [];
    state.appCenter.behaviorStats = null;
    state.appCenter.behaviorReport = null;
    if (renderAfterLoad) {
      renderContent();
    }
    try {
      var result = await Promise.all([
        requestJson('/api/admin/apps/' + encodeURIComponent(normalizedAppCode) + '/versions', {
          method: 'GET',
          headers: buildAdminHeaders(),
        }),
        requestJson('/api/admin/apps/' + encodeURIComponent(normalizedAppCode) + '/devices', {
          method: 'GET',
          headers: buildAdminHeaders(),
        }),
      ]);
      state.appCenter.versions = result[0] || [];
      state.appCenter.devices = result[1] || [];
      if (isPathActive(APP_BEHAVIOR_PATH)) {
        await loadAppBehaviorPanelData(normalizedAppCode, false);
      }
    } finally {
      state.appCenter.detailLoading = false;
      if (renderAfterLoad) {
        renderContent();
      }
    }
  }

  async function loadAppDeviceVisits(deviceId, renderAfterLoad) {
    var normalizedDeviceId = requireText(String(deviceId || '').trim(), '设备ID');
    state.appCenter.selectedDevice = (state.appCenter.devices || []).find(function (item) {
      return String(item && item.deviceId || '') === normalizedDeviceId;
    }) || { deviceId: normalizedDeviceId };
    state.appCenter.visitLoading = true;
    state.appCenter.visitRecords = [];
    if (renderAfterLoad) {
      renderContent();
    }
    try {
      state.appCenter.visitRecords = await requestJson('/api/admin/apps/devices/' + encodeURIComponent(normalizedDeviceId) + '/visit-records?limit=20', {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
    } finally {
      state.appCenter.visitLoading = false;
      if (renderAfterLoad) {
        renderContent();
      }
    }
  }

  async function loadAppBehaviorPanelData(appCode, renderAfterLoad) {
    await Promise.all([
      loadAppBehaviorEvents(renderAfterLoad, appCode),
      loadAppBehaviorStats(renderAfterLoad, appCode),
      loadAppBehaviorReport(renderAfterLoad, appCode),
    ]);
  }

  async function queryAppBehaviorEventsFromForm() {
    syncAppBehaviorFilterFromForm();
    await loadAppBehaviorPanelData(state.appCenter.selectedApp && state.appCenter.selectedApp.appCode, true);
    setAppCenterNotice('埋点查询条件已更新');
    renderContent();
  }

  function syncAppBehaviorFilterFromForm() {
    if (!state.appCenter.behaviorFilter) {
      state.appCenter.behaviorFilter = {};
    }
    state.appCenter.behaviorFilter.eventType = readInputValue('appBehaviorFilterEventType');
    state.appCenter.behaviorFilter.eventName = readInputValue('appBehaviorFilterEventName');
    state.appCenter.behaviorFilter.pageName = readInputValue('appBehaviorFilterPageName');
    state.appCenter.behaviorFilter.deviceId = readInputValue('appBehaviorFilterDeviceId');
    state.appCenter.behaviorFilter.userId = readInputValue('appBehaviorFilterUserId');
    state.appCenter.behaviorFilter.startAt = readInputValue('appBehaviorFilterStartAt');
    state.appCenter.behaviorFilter.endAt = readInputValue('appBehaviorFilterEndAt');
    state.appCenter.behaviorFilter.limit = readInputValue('appBehaviorFilterLimit') || '50';
    state.appCenter.behaviorFilter.reportLimit = readInputValue('appBehaviorFilterReportLimit') || '200';
  }

  function buildAppBehaviorFilterQuery(limitType) {
    var filter = state.appCenter.behaviorFilter || {};
    var query = {
      eventType: filter.eventType || '',
      eventName: filter.eventName || '',
      pageName: filter.pageName || '',
      deviceId: filter.deviceId || '',
      userId: toPositiveDigitsOrEmpty(filter.userId || ''),
      startAt: filter.startAt || '',
      endAt: filter.endAt || '',
    };
    if (limitType === 'report') {
      query.limit = toPositiveDigitsOrEmpty(filter.reportLimit || '');
    } else if (limitType === 'stats') {
      query.topLimit = '10';
    } else {
      query.limit = toPositiveDigitsOrEmpty(filter.limit || '');
    }
    return buildQueryString(query);
  }

  async function loadAppBehaviorEvents(renderAfterLoad, appCode) {
    var selectedAppCode = requireText(String(appCode || (state.appCenter.selectedApp && state.appCenter.selectedApp.appCode) || '').trim(), '应用Code');
    state.appCenter.behaviorLoading = true;
    state.appCenter.behaviorEvents = [];
    if (renderAfterLoad) {
      renderContent();
    }
    try {
      state.appCenter.behaviorEvents = await requestJson(
        '/api/admin/apps/' + encodeURIComponent(selectedAppCode) + '/behavior-events' + buildAppBehaviorFilterQuery('detail'),
        {
          method: 'GET',
          headers: buildAdminHeaders(),
        }
      );
    } finally {
      state.appCenter.behaviorLoading = false;
      if (renderAfterLoad) {
        renderContent();
      }
    }
  }

  async function loadAppBehaviorStats(renderAfterLoad, appCode) {
    var selectedAppCode = requireText(String(appCode || (state.appCenter.selectedApp && state.appCenter.selectedApp.appCode) || '').trim(), '应用Code');
    state.appCenter.behaviorStatsLoading = true;
    if (renderAfterLoad) {
      renderContent();
    }
    try {
      state.appCenter.behaviorStats = await requestJson(
        '/api/admin/apps/' + encodeURIComponent(selectedAppCode) + '/behavior-events/stats' + buildAppBehaviorFilterQuery('stats'),
        {
          method: 'GET',
          headers: buildAdminHeaders(),
        }
      );
    } finally {
      state.appCenter.behaviorStatsLoading = false;
      if (renderAfterLoad) {
        renderContent();
      }
    }
  }

  async function loadAppBehaviorReport(renderAfterLoad, appCode) {
    var selectedAppCode = requireText(String(appCode || (state.appCenter.selectedApp && state.appCenter.selectedApp.appCode) || '').trim(), '应用Code');
    state.appCenter.behaviorReportLoading = true;
    if (renderAfterLoad) {
      renderContent();
    }
    try {
      state.appCenter.behaviorReport = await requestJson(
        '/api/admin/apps/' + encodeURIComponent(selectedAppCode) + '/behavior-events/report' + buildAppBehaviorFilterQuery('report'),
        {
          method: 'GET',
          headers: buildAdminHeaders(),
        }
      );
    } finally {
      state.appCenter.behaviorReportLoading = false;
      if (renderAfterLoad) {
        renderContent();
      }
    }
  }

  async function exportAppBehaviorReportCsv() {
    syncAppBehaviorFilterFromForm();
    var selectedAppCode = requireText(String((state.appCenter.selectedApp && state.appCenter.selectedApp.appCode) || '').trim(), '应用Code');
    var response = await fetch(
      '/api/admin/apps/' + encodeURIComponent(selectedAppCode) + '/behavior-events/report.csv' + buildAppBehaviorFilterQuery('report'),
      {
        method: 'GET',
        headers: buildAdminHeaders(),
      }
    );
    if (!response.ok) {
      var payload = await safeJson(response);
      throw new Error(readErrorMessage(payload, response.status, '埋点报表导出失败'));
    }
    var blob = await response.blob();
    var downloadUrl = URL.createObjectURL(blob);
    var filename = extractDownloadFilename(response.headers.get('content-disposition')) || ('app_behavior_report_' + selectedAppCode + '.csv');
    try {
      var anchor = document.createElement('a');
      anchor.href = downloadUrl;
      anchor.download = filename;
      document.body.appendChild(anchor);
      anchor.click();
      document.body.removeChild(anchor);
    } finally {
      URL.revokeObjectURL(downloadUrl);
    }
    setAppCenterNotice('埋点报表导出完成');
    renderContent();
  }

  function selectAppDevice(deviceId, renderAfterLoad) {
    var normalizedDeviceId = requireText(String(deviceId || '').trim(), '设备ID');
    state.appCenter.selectedDevice = (state.appCenter.devices || []).find(function (item) {
      return String(item && item.deviceId || '') === normalizedDeviceId;
    }) || { deviceId: normalizedDeviceId };
    if (renderAfterLoad) {
      renderContent();
    }
  }

  function openAppVersionForm() {
    var selectedApp = state.appCenter && state.appCenter.selectedApp;
    if (!selectedApp || !selectedApp.appCode) {
      setAppCenterNotice('请先选择应用');
      renderContent();
      return;
    }
    state.appCenter.versionDraft = createDefaultAppVersionDraft(selectedApp, state.appCenter.versions || []);
    state.appCenter.versionFormMode = 'CREATE';
    state.appCenter.versionEditingCode = '';
    state.appCenter.versionFormNotice = '';
    state.appCenter.versionSubmitting = false;
    state.appCenter.versionFormVisible = true;
    renderContent();
  }

  function openAppVersionEditForm(versionCode) {
    var selectedApp = state.appCenter && state.appCenter.selectedApp;
    if (!selectedApp || !selectedApp.appCode) {
      setAppCenterNotice('请先选择应用');
      renderContent();
      return;
    }
    var normalizedVersionCode = requireText(String(versionCode || '').trim(), '版本Code');
    var version = (state.appCenter.versions || []).find(function (item) {
      return item && String(item.versionCode || '') === normalizedVersionCode;
    });
    if (!version) {
      setAppCenterNotice('未找到版本');
      renderContent();
      return;
    }
    var iosPackage = version.iosPackage || null;
    state.appCenter.versionDraft = {
      appVersionNo: normalizeOptionalAppVersionNo(version.appVersionNo) || '',
      versionCode: version.versionCode || '',
      iosCode: (iosPackage && iosPackage.iosCode) || buildAppIosCode(selectedApp.appCode, version.appVersionNo),
      updateType: normalizeEnumValue(version.updateType || 'OPTIONAL') || 'OPTIONAL',
      updatePromptFrequency: normalizeEnumValue(version.updatePromptFrequency || 'ONCE_PER_VERSION') || 'ONCE_PER_VERSION',
      minSupportedVersionNo: normalizeOptionalAppVersionNo(version.minSupportedVersionNo) || '',
      appStoreUrl: (iosPackage && iosPackage.appStoreUrl) || '',
      versionDescription: version.versionDescription || DEFAULT_VERSION_PROMPT_MESSAGE,
      publisherRemark: version.publisherRemark || '',
    };
    state.appCenter.versionFormMode = 'EDIT';
    state.appCenter.versionEditingCode = version.versionCode || '';
    state.appCenter.versionFormNotice = '';
    state.appCenter.versionSubmitting = false;
    state.appCenter.versionFormVisible = true;
    renderContent();
  }

  function closeAppVersionForm() {
    state.appCenter.versionFormVisible = false;
    state.appCenter.versionFormMode = 'CREATE';
    state.appCenter.versionEditingCode = '';
    state.appCenter.versionDraft = null;
    state.appCenter.versionFormNotice = '';
    state.appCenter.versionSubmitting = false;
    renderContent();
  }

  function createDefaultAppVersionDraft(selectedApp, versions) {
    var versionNo = resolveNextAppVersionNoByDate(versions, new Date());
    return {
      appVersionNo: versionNo,
      versionCode: buildAppVersionCode(selectedApp && selectedApp.appCode, versionNo),
      iosCode: buildAppIosCode(selectedApp && selectedApp.appCode, versionNo),
      updateType: 'OPTIONAL',
      updatePromptFrequency: 'ONCE_PER_VERSION',
      minSupportedVersionNo: '',
      appStoreUrl: '',
      versionDescription: DEFAULT_VERSION_PROMPT_MESSAGE,
      publisherRemark: '',
    };
  }

  function ensureAppVersionDraft() {
    if (!state.appCenter.versionDraft) {
      state.appCenter.versionDraft = createDefaultAppVersionDraft(
        state.appCenter.selectedApp || { appCode: 'OPENAIPAY_IOS' },
        state.appCenter.versions || []
      );
    }
    return state.appCenter.versionDraft;
  }

  function syncAppVersionDraftFromForm() {
    var draft = ensureAppVersionDraft();
    var appVersionNoValue = readInputValue('appVersionNo');
    draft.appVersionNo = normalizeOptionalAppVersionNo(appVersionNoValue) || '';
    var updateTypeValue = readInputValue('appVersionUpdateType');
    if (updateTypeValue) {
      draft.updateType = updateTypeValue;
    }
    var promptFrequencyValue = readInputValue('appVersionPromptFrequency');
    if (promptFrequencyValue) {
      draft.updatePromptFrequency = promptFrequencyValue;
    }
    draft.minSupportedVersionNo = normalizeOptionalAppVersionNo(readInputValue('appVersionMinSupported')) || '';
    draft.appStoreUrl = readInputValue('appIosStoreUrl') || '';
    draft.versionDescription = readInputValue('appVersionDescription') || DEFAULT_VERSION_PROMPT_MESSAGE;
    draft.publisherRemark = readInputValue('appVersionPublisherRemark') || '';
    draft.versionCode = buildAppVersionCode(state.appCenter.selectedApp && state.appCenter.selectedApp.appCode, draft.appVersionNo);
    draft.iosCode = buildAppIosCode(state.appCenter.selectedApp && state.appCenter.selectedApp.appCode, draft.appVersionNo);
    if (normalizeEnumValue(draft.updateType) === 'FORCE') {
      draft.updatePromptFrequency = 'ALWAYS';
    }
    return draft;
  }

  function updateAppVersionDraftByVersionNo(rawVersionNo) {
    var draft = ensureAppVersionDraft();
    var normalizedVersionNo = normalizeOptionalAppVersionNo(rawVersionNo) || '';
    draft.appVersionNo = normalizedVersionNo;
    draft.versionCode = buildAppVersionCode(state.appCenter.selectedApp && state.appCenter.selectedApp.appCode, normalizedVersionNo);
    draft.iosCode = buildAppIosCode(state.appCenter.selectedApp && state.appCenter.selectedApp.appCode, normalizedVersionNo);
    var versionCodeInput = document.getElementById('appVersionCode');
    if (versionCodeInput) {
      versionCodeInput.value = draft.versionCode || '';
    }
  }

  function resolveNextAppVersionNoByDate(versions, currentDate) {
    var prefix = buildAppVersionDatePrefix(currentDate);
    var targetPrefix = prefix + '.';
    var maxSequence = 0;
    (versions || []).forEach(function (item) {
      var appVersionNo = normalizeOptionalAppVersionNo(item && item.appVersionNo);
      if (!appVersionNo || appVersionNo.indexOf(targetPrefix) !== 0) {
        return;
      }
      var sequenceText = appVersionNo.slice(targetPrefix.length);
      if (!/^\d+$/.test(sequenceText)) {
        return;
      }
      var sequence = Number(sequenceText);
      if (Number.isFinite(sequence) && sequence > maxSequence) {
        maxSequence = sequence;
      }
    });
    return prefix + '.' + String(maxSequence + 1);
  }

  function buildAppVersionDatePrefix(currentDate) {
    var date = currentDate instanceof Date ? currentDate : new Date();
    var year = String(date.getFullYear()).slice(-2);
    var month = String(date.getMonth() + 1);
    var day = String(date.getDate()).padStart(2, '0');
    return year + '.' + month + day;
  }

  function normalizeOptionalAppVersionNo(raw) {
    if (raw == null) {
      return null;
    }
    var normalized = String(raw).trim();
    return normalized ? normalized : null;
  }

  function buildAppVersionCode(appCode, appVersionNo) {
    var normalizedVersionNo = normalizeOptionalAppVersionNo(appVersionNo);
    if (!normalizedVersionNo) {
      return '';
    }
    return normalizeAppCodePrefix(appCode) + '_VER_' + normalizedVersionNo.replace(/\./g, '_');
  }

  function buildAppIosCode(appCode, appVersionNo) {
    var normalizedVersionNo = normalizeOptionalAppVersionNo(appVersionNo);
    if (!normalizedVersionNo) {
      return '';
    }
    return normalizeAppCodePrefix(appCode) + '_PKG_' + normalizedVersionNo.replace(/\./g, '_');
  }

  function normalizeAppCodePrefix(appCode) {
    var normalized = String(appCode == null ? '' : appCode).trim().toUpperCase();
    return normalized || 'OPENAIPAY_IOS';
  }

  function parseAppVersionNo(rawVersionNo) {
    var normalized = normalizeOptionalAppVersionNo(rawVersionNo);
    if (!normalized) {
      return null;
    }
    var matched = normalized.match(/^(\d{2})\.(\d{3,4})\.(\d+)$/);
    if (!matched) {
      return null;
    }
    return {
      year: Number(matched[1]),
      monthDay: Number(matched[2]),
      sequence: Number(matched[3]),
    };
  }

  function compareAppVersionNoDesc(left, right) {
    var leftParsed = parseAppVersionNo(left);
    var rightParsed = parseAppVersionNo(right);
    if (!leftParsed || !rightParsed) {
      return String(right || '').localeCompare(String(left || ''));
    }
    if (leftParsed.year !== rightParsed.year) {
      return rightParsed.year - leftParsed.year;
    }
    if (leftParsed.monthDay !== rightParsed.monthDay) {
      return rightParsed.monthDay - leftParsed.monthDay;
    }
    return rightParsed.sequence - leftParsed.sequence;
  }

  function renderMinSupportedVersionOptions(selectedValue, versions) {
    var selected = normalizeOptionalAppVersionNo(selectedValue) || '';
    var seen = {};
    var values = [];
    (versions || []).forEach(function (item) {
      var appVersionNo = normalizeOptionalAppVersionNo(item && item.appVersionNo);
      if (!appVersionNo || seen[appVersionNo]) {
        return;
      }
      seen[appVersionNo] = true;
      values.push(appVersionNo);
    });
    if (selected && !seen[selected]) {
      values.push(selected);
    }
    values.sort(compareAppVersionNoDesc);
    var html = '<option value="">不限制</option>';
    html += values.map(function (versionNo) {
      var selectedAttr = versionNo === selected ? ' selected' : '';
      return '<option value="' + escapeHtml(versionNo) + '"' + selectedAttr + '>' + escapeHtml(versionNo) + '</option>';
    }).join('');
    return html;
  }

  function renderAppUpdatePromptFrequencyOptions(selectedValue) {
    var normalizedSelected = normalizeEnumValue(selectedValue || 'ONCE_PER_VERSION');
    var values = ['ONCE_PER_VERSION', 'ALWAYS', 'DAILY', 'SILENT'];
    return values.map(function (item) {
      var selectedAttr = item === normalizedSelected ? ' selected' : '';
      return '<option value="' + item + '"' + selectedAttr + '>' + escapeHtml(formatAppUpdatePromptFrequency(item)) + '</option>';
    }).join('');
  }

  async function createAppFromForm() {
    var appCode = requireText(readInputValue('appInfoCode'), '应用Code');
    var appName = requireText(readInputValue('appInfoName'), '应用名称');
    await requestJson('/api/admin/apps', {
      method: 'POST',
      headers: buildAdminHeaders(),
      body: JSON.stringify({
        appCode: appCode,
        appName: appName,
      }),
    });
    await ensureAppCenterData(true);
    await selectAppCenterApp(appCode, false);
    state.appCenter.appFormVisible = false;
    setAppCenterNotice('应用已保存');
    renderContent();
  }

  async function createAppVersionFromForm() {
    var selectedApp = state.appCenter.selectedApp;
    if (!selectedApp || !selectedApp.appCode) {
      throw new Error('请先选择应用');
    }
    if (state.appCenter.versionSubmitting) {
      return;
    }
    state.appCenter.versionSubmitting = true;
    state.appCenter.versionFormNotice = '';
    renderContent();
    try {
      var formMode = state.appCenter.versionFormMode === 'EDIT' ? 'EDIT' : 'CREATE';
      var draft = syncAppVersionDraftFromForm();
      var appVersionNo = requireText(draft.appVersionNo || readInputValue('appVersionNo'), '版本号');
      var updateType = draft.updateType || readInputValue('appVersionUpdateType') || 'OPTIONAL';
      var updatePromptFrequency = normalizeEnumValue(updateType) === 'FORCE'
        ? 'ALWAYS'
        : (draft.updatePromptFrequency || readInputValue('appVersionPromptFrequency') || 'ONCE_PER_VERSION');
      var resolvedVersionCode = formMode === 'EDIT'
        ? requireText(state.appCenter.versionEditingCode || draft.versionCode || buildAppVersionCode(selectedApp.appCode, appVersionNo), '版本Code')
        : requireText(buildAppVersionCode(selectedApp.appCode, appVersionNo), '版本Code');
      var resolvedIosCode = requireText(
        draft.iosCode || buildAppIosCode(selectedApp.appCode, appVersionNo),
        'iOS包Code'
      );
      var resolvedVersionDescription = draft.versionDescription
        || readInputValue('appVersionDescription')
        || DEFAULT_VERSION_PROMPT_MESSAGE;
      var resolvedPublisherRemark = draft.publisherRemark || readInputValue('appVersionPublisherRemark') || null;
      if (formMode === 'EDIT') {
        var updatedVersion = await requestJson('/api/admin/apps/versions/' + encodeURIComponent(resolvedVersionCode), {
          method: 'PUT',
          headers: buildAdminHeaders(),
          body: JSON.stringify({
            updateType: updateType,
            updatePromptFrequency: updatePromptFrequency,
            versionDescription: resolvedVersionDescription,
            publisherRemark: resolvedPublisherRemark,
            minSupportedVersionNo: normalizeOptionalAppVersionNo(draft.minSupportedVersionNo) || null,
            iosCode: resolvedIosCode,
            appStoreUrl: draft.appStoreUrl || readInputValue('appIosStoreUrl') || null,
            packageSizeBytes: null,
            md5: null,
          }),
        });
        upsertAppCenterVersion(updatedVersion);
        setAppCenterNotice('应用版本已更新');
      } else {
        var createdVersion = await requestJson('/api/admin/apps/' + encodeURIComponent(selectedApp.appCode) + '/versions', {
          method: 'POST',
          headers: buildAdminHeaders(),
          body: JSON.stringify({
            versionCode: resolvedVersionCode,
            appVersionNo: appVersionNo,
            updateType: updateType,
            updatePromptFrequency: updatePromptFrequency,
            versionDescription: resolvedVersionDescription,
            publisherRemark: resolvedPublisherRemark,
            releaseRegions: ['CN'],
            targetedRegions: ['CN'],
            minSupportedVersionNo: normalizeOptionalAppVersionNo(draft.minSupportedVersionNo) || null,
            iosCode: resolvedIosCode,
            appStoreUrl: draft.appStoreUrl || readInputValue('appIosStoreUrl') || null,
            packageSizeBytes: null,
            md5: null,
          }),
        });
        upsertAppCenterVersion(createdVersion);
        setAppCenterNotice('应用版本已创建');
      }
      state.appCenter.versionFormVisible = false;
      state.appCenter.versionFormMode = 'CREATE';
      state.appCenter.versionEditingCode = '';
      state.appCenter.versionDraft = null;
      state.appCenter.versionFormNotice = '';
    } catch (error) {
      state.appCenter.versionFormNotice = error && error.message ? error.message : '保存版本失败';
      throw error;
    } finally {
      state.appCenter.versionSubmitting = false;
      renderContent();
    }
  }

  async function updateAppSettingsFromForm() {
    var selectedApp = state.appCenter.selectedApp;
    if (!selectedApp || !selectedApp.appCode) {
      throw new Error('请先选择应用');
    }
    var versionPromptEnabledInput = document.getElementById('appVersionPromptEnabled');
    var demoAutoLoginEnabledInput = document.getElementById('appDemoAutoLoginEnabled');
    var loginDeviceBindingCheckEnabledInput = document.getElementById('appLoginDeviceBindingCheckEnabled');
    var demoTemplateLoginIdInput = document.getElementById('appDemoTemplateLoginId');
    var demoContactLoginIdInput = document.getElementById('appDemoContactLoginId');
    var demoLoginPasswordInput = document.getElementById('appDemoLoginPassword');
    var demoLoginPasswordValue = demoLoginPasswordInput ? String(demoLoginPasswordInput.value || '').trim() : '';
    await requestJson('/api/admin/apps/' + encodeURIComponent(selectedApp.appCode) + '/settings', {
      method: 'PUT',
      headers: buildAdminHeaders(),
      body: JSON.stringify({
        versionPromptEnabled: !!(versionPromptEnabledInput && String(versionPromptEnabledInput.value || '').toLowerCase() === 'true'),
        demoAutoLoginEnabled: !!(demoAutoLoginEnabledInput && String(demoAutoLoginEnabledInput.value || '').toLowerCase() === 'true'),
        loginDeviceBindingCheckEnabled: !(loginDeviceBindingCheckEnabledInput && String(loginDeviceBindingCheckEnabledInput.value || '').toLowerCase() === 'false'),
        demoTemplateLoginId: demoTemplateLoginIdInput ? String(demoTemplateLoginIdInput.value || '').trim() : null,
        demoContactLoginId: demoContactLoginIdInput ? String(demoContactLoginIdInput.value || '').trim() : null,
        demoLoginPassword: demoLoginPasswordValue || (selectedApp.demoLoginPasswordConfigured ? null : ''),
      }),
    });
    await ensureAppCenterData(true);
    await selectAppCenterApp(selectedApp.appCode, false);
    state.appCenter.settingsFormVisible = false;
    setAppCenterNotice('应用开关已更新');
    renderContent();
  }

  async function updateAppSettingInline(actionElement) {
    if (!actionElement) {
      return;
    }
    var appCode = requireText(String(actionElement.getAttribute('data-app-code') || '').trim(), '应用Code');
    var fieldName = String(actionElement.getAttribute('data-field-name') || '').trim();
    var targetValueText = String(actionElement.getAttribute('data-target-value') || '').trim().toLowerCase();
    if (fieldName !== 'versionPromptEnabled' && fieldName !== 'demoAutoLoginEnabled' && fieldName !== 'loginDeviceBindingCheckEnabled') {
      throw new Error('开关字段不合法');
    }
    if (targetValueText !== 'true' && targetValueText !== 'false') {
      throw new Error('开关值不合法');
    }
    var targetValue = targetValueText === 'true';
    var versionPromptEnabled = String(actionElement.getAttribute('data-version-prompt-enabled') || '').trim().toLowerCase() === 'true';
    var demoAutoLoginEnabled = String(actionElement.getAttribute('data-demo-auto-login-enabled') || '').trim().toLowerCase() === 'true';
    var loginDeviceBindingCheckEnabled = String(actionElement.getAttribute('data-login-device-binding-check-enabled') || '').trim().toLowerCase() !== 'false';
    if (fieldName === 'versionPromptEnabled') {
      versionPromptEnabled = targetValue;
    } else if (fieldName === 'demoAutoLoginEnabled') {
      demoAutoLoginEnabled = targetValue;
    } else {
      loginDeviceBindingCheckEnabled = targetValue;
    }

    var app = (state.appCenter.apps || []).find(function (item) {
      return item && item.appCode === appCode;
    });
    if (app) {
      app.versionPromptEnabled = versionPromptEnabled;
      app.demoAutoLoginEnabled = demoAutoLoginEnabled;
      app.loginDeviceBindingCheckEnabled = loginDeviceBindingCheckEnabled;
      app.updatedAt = new Date().toISOString();
      renderContent();
    }

    await requestJson('/api/admin/apps/' + encodeURIComponent(appCode) + '/settings', {
      method: 'PUT',
      headers: buildAdminHeaders(),
      body: JSON.stringify({
        versionPromptEnabled: versionPromptEnabled,
        demoAutoLoginEnabled: demoAutoLoginEnabled,
        loginDeviceBindingCheckEnabled: loginDeviceBindingCheckEnabled,
        demoTemplateLoginId: app && app.demoTemplateLoginId ? app.demoTemplateLoginId : null,
        demoContactLoginId: app && app.demoContactLoginId ? app.demoContactLoginId : null,
        demoLoginPassword: null,
      }),
    });
    await ensureAppCenterData(true);
    setAppCenterNotice('应用功能开关已更新');
    renderContent();
  }

  function applyAppSettingToggle(actionElement) {
    if (!actionElement) {
      return;
    }
    var fieldId = actionElement.getAttribute('data-field-id');
    var value = String(actionElement.getAttribute('data-value') || '').toLowerCase();
    if (!fieldId || (value !== 'true' && value !== 'false')) {
      return;
    }
    var hiddenInput = document.getElementById(fieldId);
    if (!hiddenInput) {
      return;
    }
    hiddenInput.value = value;

    var group = actionElement.closest('[data-app-setting-group]');
    if (!group) {
      return;
    }
    var buttons = group.querySelectorAll('[data-action="set-app-setting-toggle"]');
    buttons.forEach(function (button) {
      var buttonValue = String(button.getAttribute('data-value') || '').toLowerCase();
      if (buttonValue === value) {
        button.classList.add('primary');
      } else {
        button.classList.remove('primary');
      }
    });
  }

  async function changeAppVersionStatus(versionCode, targetStatus) {
    var updatedVersion = await requestJson('/api/admin/apps/versions/' + encodeURIComponent(versionCode) + '/status', {
      method: 'PUT',
      headers: buildAdminHeaders(),
      body: JSON.stringify({ status: targetStatus }),
    });
    upsertAppCenterVersion(updatedVersion);
    var normalizedStatus = normalizeEnumValue(targetStatus);
    var successNotice = normalizedStatus === 'ENABLED' ? '启用成功' : '停用成功';
    if (typeof window !== 'undefined' && typeof window.alert === 'function') {
      window.alert(localizeText(successNotice));
    }
    setAppCenterNotice(successNotice);
    renderContent();
  }

  function setAppCenterNotice(text) {
    state.appCenter.notice = localizeText(text || '');
  }

  function upsertAppCenterVersion(version) {
    if (!version || !version.versionCode) {
      return;
    }
    var versions = Array.isArray(state.appCenter.versions) ? state.appCenter.versions : [];
    var targetVersionCode = String(version.versionCode || '');
    var hitIndex = versions.findIndex(function (item) {
      return item && String(item.versionCode || '') === targetVersionCode;
    });
    if (hitIndex >= 0) {
      versions[hitIndex] = version;
      state.appCenter.versions = versions;
      return;
    }
    versions.unshift(version);
    state.appCenter.versions = versions;
  }

  function splitCsvInput(raw) {
    return String(raw || '')
      .split(',')
      .map(function (item) { return item.trim(); })
      .filter(function (item) { return item.length > 0; });
  }

  function readOptionalPositiveInteger(id) {
    var raw = readInputValue(id);
    if (!raw) {
      return null;
    }
    return parsePositiveInteger(raw, id);
  }

  function renderPlaceholderPage(title, subtitle) {
    var html = '';
    html += '<div class="manager-title-row">';
    html += '<div>';
    html += '<h1 class="manager-title">' + escapeHtml(title) + '</h1>';
    if (subtitle) {
      html += '<p class="manager-subtext">' + escapeHtml(subtitle) + '</p>';
    }
    html += '</div>';
    html += '</div>';
    html += '<div class="manager-placeholder">';
    html += '<h2 class="manager-placeholder-title">' + escapeHtml(title) + '</h2>';
    html += '<p class="manager-placeholder-tip">' + escapeHtml(langText('该模块页面框架已就绪，后续可继续接入具体查询与操作功能。', 'This module page scaffold is ready. You can continue wiring detailed query and operation features.')) + '</p>';
    html += '</div>';
    return html;
  }

  function resolveHomePath(menus) {
    var dashboard = menus.find(function (menu) {
      return menu.path === '/admin/dashboard';
    });
    if (dashboard) {
      return dashboard.path;
    }

    var firstRoot = menus
      .filter(function (menu) {
        return !menu.parentCode;
      })
      .sort(bySortNo)[0];
    if (firstRoot && firstRoot.path) {
      return firstRoot.path;
    }
    return '/admin/dashboard';
  }

  function buildMenuTree(menus) {
    var sorted = menus.slice().sort(bySortNo);
    var menuMap = {};
    sorted.forEach(function (menu) {
      menuMap[menu.menuCode] = {
        menuCode: menu.menuCode,
        parentCode: menu.parentCode,
        menuName: menu.menuName,
        path: menu.path,
        icon: menu.icon,
        sortNo: menu.sortNo,
        children: [],
      };
    });

    var roots = [];
    sorted.forEach(function (menu) {
      var current = menuMap[menu.menuCode];
      if (menu.parentCode && menuMap[menu.parentCode]) {
        menuMap[menu.parentCode].children.push(current);
      } else {
        roots.push(current);
      }
    });

    roots.forEach(function (node) {
      node.children.sort(bySortNo);
    });

    return roots;
  }

  function buildDisplayMenuTree(menus) {
    return wrapStandaloneRoots(injectRootAccessChildren(mergeCapitalFlowRoots(buildMenuTree(menus || []))));
  }

  function wrapStandaloneRoots(tree) {
    return wrapUserCenterRoot(tree || []);
  }

  function wrapUserCenterRoot(tree) {
    return (tree || []).map(function (node) {
      if (!node || node.menuCode !== 'user-center' || (node.children && node.children.length > 0)) {
        return node;
      }
      return {
        menuCode: node.menuCode,
        parentCode: node.parentCode,
        menuName: node.menuName,
        path: node.path,
        icon: node.icon,
        sortNo: node.sortNo,
        children: [
          {
            menuCode: 'user-account-list',
            parentCode: node.menuCode,
            menuName: '用户列表',
            path: node.path,
            icon: 'unordered-list',
            sortNo: 1,
            children: [],
          },
        ],
      };
    });
  }

  function injectRootAccessChildren(tree) {
    return (tree || []).map(function (node) {
      return ensureVirtualChildren(ensureRootAccessChild(node));
    });
  }

  function ensureVirtualChildren(node) {
    if (!node) {
      return node;
    }
    var withBehavior = ensureAppBehaviorChild(node);
    return withBehavior;
  }

  function ensureAppBehaviorChild(node) {
    if (!node || node.menuCode !== 'app-center') {
      return node;
    }
    var children = Array.isArray(node.children) ? node.children.slice() : [];
    var exists = children.some(function (child) {
      return child && String(child.path || '') === APP_BEHAVIOR_PATH;
    });
    if (exists) {
      return node;
    }
    children.push({
      menuCode: 'app-behavior',
      parentCode: node.menuCode,
      menuName: '埋点管理',
      path: APP_BEHAVIOR_PATH,
      icon: 'table',
      sortNo: 25,
      children: [],
    });
    children.sort(bySortNo);
    return {
      menuCode: node.menuCode,
      parentCode: node.parentCode,
      menuName: node.menuName,
      path: node.path,
      icon: node.icon,
      sortNo: node.sortNo,
      children: children,
    };
  }

  function ensureRootAccessChild(node) {
    if (!node || !node.path) {
      return node;
    }
    var config = getRootAccessChildConfig(node.menuCode);
    if (!config) {
      return node;
    }

    var children = Array.isArray(node.children) ? node.children.slice() : [];
    var hasSamePathChild = children.some(function (child) {
      return child && String(child.path || '') === String(node.path || '');
    });
    if (hasSamePathChild) {
      return node;
    }

    children.unshift({
      menuCode: config.menuCode,
      parentCode: node.menuCode,
      menuName: config.menuName,
      path: node.path,
      icon: config.icon || node.icon,
      sortNo: typeof config.sortNo === 'number' ? config.sortNo : 0,
      children: [],
    });
    children.sort(bySortNo);

    return {
      menuCode: node.menuCode,
      parentCode: node.parentCode,
      menuName: node.menuName,
      path: node.path,
      icon: node.icon,
      sortNo: node.sortNo,
      children: children,
    };
  }

  function getRootAccessChildConfig(menuCode) {
    if (menuCode === 'user-center') {
      return {
        menuCode: 'user-account-list',
        menuName: '用户列表',
        icon: 'unordered-list',
        sortNo: 0,
      };
    }
    if (menuCode === 'app-center') {
      return {
        menuCode: 'app-overview',
        menuName: '应用总览',
        icon: 'appstore',
        sortNo: 0,
      };
    }
    if (menuCode === 'coupon-center') {
      return {
        menuCode: 'coupon-overview',
        menuName: '红包总览',
        icon: 'dashboard',
        sortNo: 0,
      };
    }
    if (menuCode === 'feedback-center') {
      return {
        menuCode: 'feedback-overview',
        menuName: '反馈总览',
        icon: 'message',
        sortNo: 0,
      };
    }
    return null;
  }

  function mergeCapitalFlowRoots(tree) {
    var roots = Array.isArray(tree) ? tree.slice() : [];
    var inboundRoot = null;
    var outboundRoot = null;

    roots.forEach(function (node) {
      if (!node) {
        return;
      }
      if (node.menuCode === 'inbound-center') {
        inboundRoot = node;
      } else if (node.menuCode === 'outbound-center') {
        outboundRoot = node;
      }
    });

    if (!inboundRoot || !outboundRoot) {
      return roots;
    }

    roots = roots.filter(function (node) {
      return node && node.menuCode !== 'inbound-center' && node.menuCode !== 'outbound-center';
    });

    roots.push({
      menuCode: 'capital-flow-center',
      parentCode: '',
      menuName: '出入金中心',
      path: '',
      icon: 'swap',
      sortNo: Math.min(
        typeof inboundRoot.sortNo === 'number' ? inboundRoot.sortNo : 0,
        typeof outboundRoot.sortNo === 'number' ? outboundRoot.sortNo : 0
      ),
      children: [inboundRoot, outboundRoot].sort(bySortNo),
    });

    roots.sort(bySortNo);
    return roots;
  }

  function resolveDefaultPathForMenuNode(node) {
    if (!node) {
      return '';
    }
    if (node.children && node.children.length > 0) {
      return findFirstMenuPathInNodes(node.children) || node.path || '';
    }
    return node.path || '';
  }

  function findFirstMenuPathInNodes(nodes) {
    var list = Array.isArray(nodes) ? nodes : [];
    for (var i = 0; i < list.length; i += 1) {
      var current = list[i];
      if (!current) {
        continue;
      }
      if (current.path) {
        return current.path;
      }
      var nested = findFirstMenuPathInNodes(current.children || []);
      if (nested) {
        return nested;
      }
    }
    return '';
  }

  function syncExpandedToSelectedPath(menus) {
    var tree = Array.isArray(menus) ? buildDisplayMenuTree(menus) : buildDisplayMenuTree((state.pageInit && state.pageInit.menus) || []);
    var expandedMenuCode = findRootMenuCodeByPath(state.selectedPath, tree);
    state.expandedMap = buildExpandedMap(tree, expandedMenuCode);
  }

  function toggleExpandedRootMenu(menuCode) {
    var tree = buildDisplayMenuTree((state.pageInit && state.pageInit.menus) || []);
    var currentExpanded = !!state.expandedMap[menuCode];
    state.expandedMap = buildExpandedMap(tree, currentExpanded ? '' : menuCode);
  }

  function buildExpandedMap(tree, expandedMenuCode) {
    var map = {};
    (tree || []).forEach(function (node) {
      if (node.children && node.children.length > 0) {
        map[node.menuCode] = !!expandedMenuCode && node.menuCode === expandedMenuCode;
      }
    });
    return map;
  }

  function findRootMenuCodeByPath(path, tree) {
    var normalizedPath = String(path || '');
    var roots = Array.isArray(tree) ? tree : [];
    for (var i = 0; i < roots.length; i += 1) {
      if (menuNodeContainsPath(roots[i], normalizedPath)) {
        return roots[i].menuCode;
      }
    }
    return '';
  }

  function menuNodeContainsPath(node, path) {
    if (!node) {
      return false;
    }
    if (String(node.path || '') === String(path || '')) {
      return true;
    }
    return (node.children || []).some(function (child) {
      return menuNodeContainsPath(child, path);
    });
  }

  function isMenuNodeActive(node) {
    return menuNodeContainsPath(node, state.selectedPath);
  }

  function findMenuByPath(path) {
    var menus = state.pageInit && Array.isArray(state.pageInit.menus) ? state.pageInit.menus : [];
    var exact = menus.find(function (menu) {
      return menu.path === path;
    });
    if (exact) {
      return exact;
    }
    return findDisplayMenuByPath(path, buildDisplayMenuTree(menus));
  }

  function menuPathExists(path, menus) {
    var existsInRaw = (menus || []).some(function (menu) {
      return menu.path === path;
    });
    if (existsInRaw) {
      return true;
    }
    return !!findDisplayMenuByPath(path, buildDisplayMenuTree(menus || []));
  }

  function findDisplayMenuByPath(path, nodes) {
    var normalizedPath = String(path || '');
    var queue = Array.isArray(nodes) ? nodes.slice() : [];
    while (queue.length > 0) {
      var current = queue.shift();
      if (!current) {
        continue;
      }
      if (String(current.path || '') === normalizedPath) {
        return current;
      }
      (current.children || []).forEach(function (child) {
        queue.push(child);
      });
    }
    return null;
  }

  function bySortNo(a, b) {
    var left = typeof a.sortNo === 'number' ? a.sortNo : 0;
    var right = typeof b.sortNo === 'number' ? b.sortNo : 0;
    return left - right;
  }

  function renderManagerIcon(iconCode, variant) {
    var normalized = String(iconCode || '').trim();
    var icon = ICON_MAP[normalized];
    var variantClass = variant === 'home' ? ' is-home' : ' is-menu';
    if (icon && icon.svg) {
      return '<span class="manager-decor-icon tone-' + escapeHtml(icon.tone || 'steel') + variantClass + '">' + icon.svg + '</span>';
    }
    var glyph = normalized || '•';
    var glyphTone = GLYPH_ICON_TONE_MAP[glyph] || 'steel';
    return '<span class="manager-decor-icon tone-' + escapeHtml(glyphTone) + variantClass + '"><span class="manager-decor-icon-glyph">' + escapeHtml(glyph) + '</span></span>';
  }

  function isPathActive(path) {
    return String(path || '') === String(state.selectedPath || '');
  }

  function resolvePathFromUrl() {
    if (isLoginUrl()) {
      return DASHBOARD_PATH;
    }
    var normalizedPathname = normalizePathname(window.location.pathname);
    if (normalizedPathname === MANAGER_ROOT_PATH || normalizedPathname === MANAGER_ROOT_PATH + '/index.html') {
      return DASHBOARD_PATH;
    }
    var managerPrefix = MANAGER_ROOT_PATH + '/';
    if (normalizedPathname.indexOf(managerPrefix) === 0) {
      var managerRelativePath = normalizedPathname.slice(managerPrefix.length).replace(/^\/+/, '');
      if (!managerRelativePath || managerRelativePath === 'index.html') {
        return DASHBOARD_PATH;
      }
      if (managerRelativePath.indexOf('.') >= 0) {
        return DASHBOARD_PATH;
      }
      if (managerRelativePath === 'dashboard') {
        return DASHBOARD_PATH;
      }
      if (managerRelativePath === 'login') {
        return DASHBOARD_PATH;
      }
      if (managerRelativePath.indexOf('admin/') === 0) {
        return '/' + managerRelativePath;
      }
      return '/admin/' + managerRelativePath.replace(/^\/+/, '');
    }
    return DASHBOARD_PATH;
  }

  function syncPathToUrl(replace) {
    var path = String(state.selectedPath || state.homePath || '/admin/dashboard');
    var targetUrl = buildManagerPageUrl(path);
    var currentUrl = window.location.pathname + window.location.search;
    if (currentUrl === targetUrl) {
      return;
    }
    if (replace && window.history && window.history.replaceState) {
      window.history.replaceState(null, '', targetUrl);
      return;
    }
    if (window.history && window.history.pushState) {
      window.history.pushState(null, '', targetUrl);
      return;
    }
    window.location.href = targetUrl;
  }

  function syncLoginUrl(replace) {
    var targetUrl = MANAGER_LOGIN_PATH;
    var currentUrl = window.location.pathname + window.location.search;
    if (currentUrl === targetUrl) {
      return;
    }
    if (replace && window.history && window.history.replaceState) {
      window.history.replaceState(null, '', targetUrl);
      return;
    }
    if (window.history && window.history.pushState) {
      window.history.pushState(null, '', targetUrl);
      return;
    }
    window.location.href = targetUrl;
  }

  function buildManagerPageUrl(path) {
    var normalizedPath = String(path || DASHBOARD_PATH).trim();
    if (!normalizedPath || normalizedPath === '/admin' || normalizedPath === DASHBOARD_PATH) {
      return MANAGER_ROOT_PATH + '/dashboard';
    }
    if (normalizedPath.indexOf('/admin/') === 0) {
      return MANAGER_ROOT_PATH + '/' + normalizedPath.slice('/admin/'.length);
    }
    return MANAGER_ROOT_PATH + '/' + normalizedPath.replace(/^\/+/, '');
  }

  function normalizePathname(pathname) {
    var normalized = String(pathname || '').replace(/\/+$/, '');
    return normalized || '/';
  }

  function isLoginUrl() {
    return normalizePathname(window.location.pathname) === MANAGER_LOGIN_PATH;
  }

  function langText(zh, en) {
    return state.lang === 'en' ? en : zh;
  }

  function localizeText(text) {
    if (state.lang !== 'en') {
      return text;
    }
    var normalized = String(text == null ? '' : text);
    var mapped = EN_TEXT_MAP[normalized];
    if (mapped) {
      return mapped;
    }
    var localized = normalized;
    EN_TEXT_KEYS.forEach(function (key) {
      localized = localized.split(key).join(EN_TEXT_MAP[key]);
    });
    return localized;
  }

  function localizeHtml(html) {
    if (state.lang !== 'en') {
      return html;
    }
    return localizeText(html);
  }

  function localizeMenuName(menuName, path, menuCode) {
    if (state.lang !== 'en') {
      return menuName;
    }
    var codeMapped = EN_MENU_NAME_BY_CODE[String(menuCode || '')];
    if (codeMapped) {
      return codeMapped;
    }
    var mapped = EN_MENU_NAME_BY_PATH[String(path || '')];
    if (mapped) {
      return mapped;
    }
    return localizeText(String(menuName || ''));
  }

  function localizeModuleName(moduleName, moduleCode) {
    if (state.lang !== 'en') {
      return moduleName;
    }
    var codeMapped = EN_MENU_NAME_BY_CODE[String(moduleCode || '')];
    if (codeMapped) {
      return codeMapped;
    }
    return localizeText(String(moduleName || ''));
  }

  function applyLanguageToStaticViews() {
    document.title = langText('爱付管理后台', 'AiPay Admin Console');
    var loginBrand = document.querySelector('.manager-login-brand');
    if (loginBrand) {
      loginBrand.textContent = langText('爱付管理后台', 'AiPay Admin Console');
    }
    var loginSub = document.querySelector('.manager-login-sub');
    if (loginSub) {
      loginSub.textContent = langText('请输入管理员账号与密码登录', 'Please sign in with admin account and password');
    }
    var usernameLabel = document.querySelector('label[for="usernameInput"]');
    if (usernameLabel) {
      usernameLabel.textContent = langText('管理员账号', 'Admin Account');
    }
    var passwordLabel = document.querySelector('label[for="passwordInput"]');
    if (passwordLabel) {
      passwordLabel.textContent = langText('登录密码', 'Password');
    }
    if (loginSubmit && !loginSubmit.disabled) {
      loginSubmit.textContent = langText('登录后台', 'Sign In');
    }
    var loginDefaultTip = document.querySelector('.manager-login-tip');
    if (loginDefaultTip) {
      loginDefaultTip.textContent = langText('默认管理员账号：admin', 'Default admin account: admin');
    }
    var managerBrand = document.querySelector('.manager-brand');
    if (managerBrand) {
      managerBrand.textContent = langText('爱付管理后台', 'AiPay Admin Console');
    }
    if (languageButton) {
      languageButton.textContent = state.lang === 'en' ? '中文' : 'EN';
      languageButton.title = langText('切换语言', 'Switch language');
    }
    if (logoutButton) {
      logoutButton.textContent = langText('退出登录', 'Sign Out');
    }
    if (!state.session || !state.session.adminId) {
      setLoginTip(langText('请输入账号密码后登录', 'Enter credentials and sign in'));
    }
  }

  function pickDisplayName() {
    if (state.pageInit && state.pageInit.admin && state.pageInit.admin.displayName) {
      return state.pageInit.admin.displayName;
    }
    if (state.session && state.session.displayName) {
      return state.session.displayName;
    }
    if (state.session && state.session.username) {
      return state.session.username;
    }
    return langText('管理员', 'Admin');
  }

  function setRbacNotice(text) {
    state.rbac.notice = localizeText(text || '');
  }

  function setOutboxNotice(text) {
    state.outbox.notice = localizeText(text || '');
  }

  function setObservabilityNotice(text) {
    state.observability.notice = localizeText(text || '');
  }

  function setAuditNotice(text) {
    state.audit.notice = localizeText(text || '');
  }

  function setCouponNotice(text) {
    state.coupon.notice = localizeText(text || '');
  }

  function setFeedbackNotice(text) {
    state.feedback.notice = localizeText(text || '');
  }

  function setUserCenterNotice(text) {
    state.userCenter.notice = localizeText(text || '');
  }

  function setPricingNotice(text) {
    state.pricing.notice = localizeText(text || '');
  }

  function setTradeNotice(text) {
    state.trade.notice = localizeText(text || '');
  }

  function setAccountingNotice(text) {
    state.accounting.notice = localizeText(text || '');
  }

  function createEmptyAccountingSubjectDraft() {
    return {
      subjectCode: '',
      subjectName: '',
      subjectType: 'ASSET',
      balanceDirection: 'DEBIT',
      parentSubjectCode: '',
      levelNo: '1',
      enabled: 'true',
      remark: '',
    };
  }

  function cloneAccountingSubjectDraft(subject) {
    var draft = createEmptyAccountingSubjectDraft();
    if (!subject) {
      return draft;
    }
    draft.subjectCode = String(subject.subjectCode || '');
    draft.subjectName = String(subject.subjectName || '');
    draft.subjectType = String(subject.subjectType || 'ASSET');
    draft.balanceDirection = String(subject.balanceDirection || 'DEBIT');
    draft.parentSubjectCode = String(subject.parentSubjectCode || '');
    draft.levelNo = String(subject.levelNo == null ? '1' : subject.levelNo);
    draft.enabled = subject.enabled === false ? 'false' : 'true';
    draft.remark = String(subject.remark || '');
    return draft;
  }

  function buildAccountingSubjectStats(subjects) {
    var totalCount = (subjects || []).length;
    var enabledCount = (subjects || []).filter(function (item) {
      return item && item.enabled !== false;
    }).length;
    var rootCount = (subjects || []).filter(function (item) {
      return item && !item.parentSubjectCode;
    }).length;
    return {
      totalCount: totalCount,
      enabledCount: enabledCount,
      disabledCount: Math.max(totalCount - enabledCount, 0),
      rootCount: rootCount,
    };
  }

  function buildAccountingSubjectTree(subjects) {
    var nodeMap = {};
    var roots = [];
    (subjects || []).forEach(function (item) {
      if (!item || !item.subjectCode) {
        return;
      }
      nodeMap[item.subjectCode] = {
        subjectCode: item.subjectCode,
        subjectName: item.subjectName,
        subjectType: item.subjectType,
        balanceDirection: item.balanceDirection,
        parentSubjectCode: item.parentSubjectCode,
        levelNo: item.levelNo,
        enabled: item.enabled,
        remark: item.remark,
        children: [],
      };
    });
    Object.keys(nodeMap).forEach(function (subjectCode) {
      var node = nodeMap[subjectCode];
      if (node.parentSubjectCode && nodeMap[node.parentSubjectCode]) {
        nodeMap[node.parentSubjectCode].children.push(node);
      } else {
        roots.push(node);
      }
    });
    sortAccountingSubjectTreeNodes(roots);
    return roots;
  }

  function sortAccountingSubjectTreeNodes(nodes) {
    (nodes || []).sort(function (left, right) {
      return String(left && left.subjectCode || '').localeCompare(String(right && right.subjectCode || ''));
    });
    (nodes || []).forEach(function (node) {
      sortAccountingSubjectTreeNodes(node.children || []);
    });
  }

  function countAccountingSubjectChildren(subjectCode, subjects) {
    return (subjects || []).filter(function (item) {
      return String(item && item.parentSubjectCode || '') === String(subjectCode || '');
    }).length;
  }

  function findAccountingSubjectByCode(subjectCode) {
    var normalized = String(subjectCode || '').trim();
    if (!normalized) {
      return null;
    }
    var subjects = (state.accounting && state.accounting.subjects) || [];
    return subjects.find(function (item) {
      return String(item && item.subjectCode || '') === normalized;
    }) || null;
  }

  function syncAccountingSubjectParentHints(parentSubjectCode) {
    var normalizedParentCode = String(parentSubjectCode || '').trim();
    var parentSubject = findAccountingSubjectByCode(normalizedParentCode);
    var levelInput = document.getElementById('accountingSubjectLevelNo');
    var subjectTypeSelect = document.getElementById('accountingSubjectType');
    var balanceDirectionSelect = document.getElementById('accountingSubjectBalanceDirection');
    if (levelInput && !normalizedParentCode) {
      levelInput.value = '1';
      return;
    }
    if (!parentSubject) {
      return;
    }
    if (levelInput) {
      levelInput.value = String(Number(parentSubject.levelNo || 1) + 1);
    }
    if (subjectTypeSelect && parentSubject.subjectType) {
      subjectTypeSelect.value = String(parentSubject.subjectType);
    }
    if (balanceDirectionSelect && parentSubject.balanceDirection) {
      balanceDirectionSelect.value = String(parentSubject.balanceDirection);
    }
  }

  function selectAccountingSubject(subject) {
    state.accounting.selectedSubjectCode = subject && subject.subjectCode ? String(subject.subjectCode) : '';
    state.accounting.subjectDraft = cloneAccountingSubjectDraft(subject);
  }

  function clearAccountingSubjectDraft() {
    state.accounting.selectedSubjectCode = '';
    state.accounting.subjectDraft = createEmptyAccountingSubjectDraft();
  }

  function selectAccountingSubjectByCode(subjectCode) {
    var normalized = String(subjectCode || '').trim();
    var selected = findAccountingSubjectByCode(normalized);
    if (!selected) {
      setAccountingNotice('未找到会计科目：' + normalized);
      return;
    }
    selectAccountingSubject(selected);
    setAccountingNotice('科目已载入编辑器：' + normalized + ' / ' + (selected.subjectName || '-'));
  }

  function resetRbacState() {
    state.rbac = {
      loaded: false,
      loading: false,
      admins: [],
      roles: [],
      permissions: [],
      modules: [],
      menus: [],
      authorizationMap: {},
      selectedAdminId: null,
      selectedRoleCode: null,
      notice: '',
      previewImageUrl: '',
    };
  }

  function resetOutboxState() {
    state.outbox = {
      loaded: false,
      loading: false,
      overview: null,
      topics: [],
      messages: [],
      deadLetters: [],
      messageLoading: false,
      deadLetterLoading: false,
      filter: {
        topic: '',
        status: '',
        keyword: '',
        onlyRetried: false,
        includePayload: false,
        limit: '20',
      },
      deadFilter: {
        topic: '',
        keyword: '',
        includePayload: false,
        limit: '20',
      },
      requeue: {
        topic: '',
        limit: '20',
        nextRetryAt: '',
      },
      notice: '',
    };
  }

  function resetObservabilityState() {
    state.observability = {
      loaded: false,
      loading: false,
      sceneLoading: false,
      overview: null,
      apiScenes: [],
      filter: {
        limit: '20',
      },
      notice: '',
    };
  }

  function resetAuditState() {
    state.audit = {
      loaded: false,
      loading: false,
      rows: [],
      filter: {
        adminId: '',
        requestMethod: '',
        requestPath: '',
        resultStatus: '',
        from: '',
        to: '',
        limit: '20',
      },
      notice: '',
    };
  }

  function resetCouponState() {
    state.coupon = {
      loaded: false,
      loading: false,
      summary: null,
      templates: [],
      templatesLoading: false,
      issues: [],
      issuesLoading: false,
      selectedIssue: null,
      templateCreateVisible: false,
      issueCreateVisible: false,
      redeemVisible: false,
      issueDetailVisible: false,
      templateFilter: {
        sceneType: '',
        status: '',
      },
      issueFilter: {
        templateId: '',
        userId: '880100068483692100',
        status: '',
        limit: '20',
      },
      notice: '',
    };
  }

  function resetFeedbackState() {
    state.feedback = {
      loaded: false,
      loading: false,
      tickets: [],
      selectedTicket: null,
      filter: {
        feedbackNo: '',
        userId: '',
        feedbackType: '',
        status: '',
        limit: '20',
      },
      notice: '',
      previewImageUrl: '',
      statusUpdating: false,
      statusUpdatingFeedbackNo: '',
    };
  }


function resetAppCenterState() {
  state.appCenter = {
    loaded: false,
    loading: false,
    detailLoading: false,
    visitLoading: false,
    behaviorLoading: false,
    behaviorStatsLoading: false,
    behaviorReportLoading: false,
    apps: [],
    selectedApp: null,
    appFormVisible: false,
    versionFormVisible: false,
    versionFormMode: 'CREATE',
    versionEditingCode: '',
    versionDraft: null,
    versionFormNotice: '',
    versionSubmitting: false,
    settingsFormVisible: false,
    versions: [],
    devices: [],
    selectedDevice: null,
    visitRecords: [],
    behaviorEvents: [],
    behaviorStats: null,
    behaviorReport: null,
    behaviorFilter: {
      eventType: '',
      eventName: '',
      pageName: '',
      deviceId: '',
      userId: '',
      startAt: buildDateTimeLocal(-1),
      endAt: buildDateTimeLocal(0),
      limit: '50',
      reportLimit: '200',
    },
    notice: '',
  };
}

function resetUserCenterState() {
    state.userCenter = {
      loaded: false,
      loading: false,
      listLoading: false,
      hasNextPage: false,
      summary: null,
      users: [],
      selectedUser: null,
      filter: {
        keyword: '',
        accountStatus: '',
        kycLevel: '',
        pageNo: '1',
        pageSize: '20',
      },
      notice: '',
    };
  }

  function resetPricingState() {
    state.pricing = {
      loaded: false,
      loading: false,
      ruleLoading: false,
      rules: [],
      selectedRule: null,
      createVisible: false,
      detailVisible: false,
      filter: {
        businessSceneCode: '',
        paymentMethod: '',
        status: '',
      },
      notice: '',
    };
  }

  function resetTradeState() {
    state.trade = {
      query: {
        tradeOrderNo: '',
        requestNo: '',
        businessDomainCode: 'AICREDIT',
        bizOrderNo: '',
      },
      selectedOrder: null,
      detailVisible: false,
      notice: '',
    };
  }

  function resetAccountingState() {
    state.accounting = {
      loaded: false,
      loading: false,
      eventLoading: false,
      voucherLoading: false,
      entryLoading: false,
      subjectLoading: false,
      loadedViews: createEmptyAccountingLoadedViews(),
      events: [],
      vouchers: [],
      entries: [],
      subjects: [],
      selectedEvent: null,
      eventDetailVisible: false,
      selectedVoucher: null,
      voucherDetailVisible: false,
      selectedSubjectCode: '',
      subjectDraft: createEmptyAccountingSubjectDraft(),
      subjectEditorVisible: false,
      eventFilter: {
        eventId: '',
        sourceBizNo: '',
        tradeOrderNo: '',
        payOrderNo: '',
        status: '',
        limit: '20',
      },
      voucherFilter: {
        voucherNo: '',
        sourceBizNo: '',
        tradeOrderNo: '',
        payOrderNo: '',
        status: '',
        limit: '20',
      },
      entryFilter: {
        voucherNo: '',
        subjectCode: '',
        ownerId: '',
        payOrderNo: '',
        limit: '50',
      },
      notice: '',
    };
  }

  function createEmptyAccountingLoadedViews() {
    return {
      events: false,
      vouchers: false,
      entries: false,
      subjects: false,
    };
  }

  function getAccountingActiveView(path) {
    if (String(path || '') === ACCOUNTING_VOUCHER_PATH) {
      return 'vouchers';
    }
    if (String(path || '') === ACCOUNTING_ENTRY_PATH) {
      return 'entries';
    }
    if (String(path || '') === ACCOUNTING_SUBJECT_PATH) {
      return 'subjects';
    }
    return 'events';
  }


function isRbacPath(path) {
  var normalizedPath = String(path || '');
  return normalizedPath === SYSTEM_ROOT_PATH || normalizedPath === RBAC_PAGE_PATH;
}

function isSystemObservabilityPath(path) {
  return String(path || '') === SYSTEM_OBSERVABILITY_PATH;
}

function isSystemAuditPath(path) {
  return String(path || '') === SYSTEM_AUDIT_PATH;
}

function isOutboxPath(path) {
  var normalizedPath = String(path || '');
  return normalizedPath === OUTBOX_ROOT_PATH || normalizedPath.indexOf(OUTBOX_ROOT_PATH + '/') === 0;
}

function isAppPath(path) {
  var normalizedPath = String(path || '');
  return normalizedPath === APP_ROOT_PATH || normalizedPath.indexOf(APP_ROOT_PATH + '/') === 0;
}

function isMessagePath(path) {
  var normalizedPath = String(path || '');
  return normalizedPath === MESSAGE_ROOT_PATH || normalizedPath.indexOf(MESSAGE_ROOT_PATH + '/') === 0;
}

function isFundPath(path) {
  var normalizedPath = String(path || '');
  return normalizedPath === FUND_ROOT_PATH || normalizedPath.indexOf(FUND_ROOT_PATH + '/') === 0;
}

function isRiskPath(path) {
  var normalizedPath = String(path || '');
  return normalizedPath === RISK_ROOT_PATH || normalizedPath.indexOf(RISK_ROOT_PATH + '/') === 0;
}

function isUserPath(path) {
    return String(path || '') === USER_ROOT_PATH;
  }

  function isTradePath(path) {
    var normalizedPath = String(path || '');
    return normalizedPath === TRADE_ROOT_PATH || normalizedPath.indexOf(TRADE_ROOT_PATH + '/') === 0;
  }

  function isAccountingPath(path) {
    var normalizedPath = String(path || '');
    return normalizedPath === ACCOUNTING_ROOT_PATH || normalizedPath.indexOf(ACCOUNTING_ROOT_PATH + '/') === 0;
  }

  function isPricingPath(path) {
    var normalizedPath = String(path || '');
    return normalizedPath === PRICING_ROOT_PATH || normalizedPath.indexOf(PRICING_ROOT_PATH + '/') === 0;
  }

  function isCouponPath(path) {
    var normalizedPath = String(path || '');
    return normalizedPath === COUPON_ROOT_PATH || normalizedPath.indexOf(COUPON_ROOT_PATH + '/') === 0;
  }

  function isFeedbackPath(path) {
    var normalizedPath = String(path || '');
    return normalizedPath === FEEDBACK_ROOT_PATH || normalizedPath.indexOf(FEEDBACK_ROOT_PATH + '/') === 0;
  }

  function isDeliverPath(path) {
    var normalizedPath = String(path || '');
    return normalizedPath === DELIVER_ROOT_PATH || normalizedPath.indexOf(DELIVER_ROOT_PATH + '/') === 0;
  }

  function isInboundPath(path) {
    var normalizedPath = String(path || '');
    return normalizedPath === INBOUND_ROOT_PATH || normalizedPath.indexOf(INBOUND_ROOT_PATH + '/') === 0;
  }

  function isOutboundPath(path) {
    var normalizedPath = String(path || '');
    return normalizedPath === OUTBOUND_ROOT_PATH || normalizedPath.indexOf(OUTBOUND_ROOT_PATH + '/') === 0;
  }

  function readInputValue(id) {
    var element = document.getElementById(id);
    if (!element || typeof element.value !== 'string') {
      return '';
    }
    return element.value.trim();
  }

  function requireText(value, label) {
    var normalized = String(value || '').trim();
    if (!normalized) {
      throw new Error(label + '不能为空');
    }
    return normalized;
  }

  function parsePositiveInteger(raw, label) {
    var normalized = requireText(raw, label);
    if (!/^\d+$/.test(normalized)) {
      throw new Error(label + '必须为正整数');
    }
    var value = Number(normalized);
    if (!Number.isFinite(value) || value <= 0) {
      throw new Error(label + '必须大于0');
    }
    return value;
  }

  function parseNonNegativeInteger(raw, label) {
    var normalized = requireText(raw, label);
    if (!/^\d+$/.test(normalized)) {
      throw new Error(label + '必须为非负整数');
    }
    var value = Number(normalized);
    if (!Number.isFinite(value) || value < 0) {
      throw new Error(label + '必须大于等于0');
    }
    return value;
  }

  function normalizePositiveIdText(raw) {
    var normalized = String(raw == null ? '' : raw).trim();
    if (!/^\d+$/.test(normalized) || normalized === '0') {
      return '';
    }
    return normalized;
  }

  function samePositiveId(left, right) {
    var leftId = normalizePositiveIdText(left);
    var rightId = normalizePositiveIdText(right);
    return !!leftId && leftId === rightId;
  }

  function parsePositiveLongText(raw, label) {
    var normalized = normalizePositiveIdText(requireText(raw, label));
    if (!normalized) {
      throw new Error(label + '必须为正整数');
    }
    return normalized;
  }

  function parseMoneyOptional(raw) {
    return parseMoneyOptionalWithCurrency(raw, 'CNY');
  }

  function parseMoneyOptionalWithCurrency(raw, currencyCode) {
    var normalized = String(raw || '').trim();
    if (!normalized) {
      return null;
    }
    var value = Number(normalized);
    if (!Number.isFinite(value) || value < 0) {
      throw new Error('金额格式不正确');
    }
    return {
      currencyUnit: currencyCode || 'CNY',
      amount: Number(value.toFixed(2)),
    };
  }

  function parseMoneyRequired(raw, label) {
    var money = parseMoneyOptional(raw);
    if (!money || money.amount <= 0) {
      throw new Error(label + '必须大于0');
    }
    return money;
  }

  function normalizeDateTimeInput(raw, label) {
    var normalized = requireText(raw, label);
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(normalized)) {
      return normalized + ':00';
    }
    if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(normalized)) {
      return normalized.replace(' ', 'T');
    }
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/.test(normalized)) {
      return normalized;
    }
    throw new Error(label + '格式必须为 yyyy-MM-ddTHH:mm:ss');
  }

  function normalizeDateTimeInputOptional(raw) {
    var normalized = String(raw || '').trim();
    if (!normalized) {
      return null;
    }
    return normalizeDateTimeInput(normalized, '时间');
  }

  function parseRateOptional(raw) {
    var normalized = String(raw || '').trim();
    if (!normalized) {
      return null;
    }
    var value = Number(normalized);
    if (!Number.isFinite(value) || value < 0 || value > 1) {
      throw new Error('费率必须在 0 到 1 之间');
    }
    return Number(value.toFixed(6));
  }

  function validatePricingExpression(feeMode, feeRate, fixedFee) {
    var mode = String(feeMode || '').toUpperCase();
    var hasRate = typeof feeRate === 'number' && feeRate > 0;
    var hasFixed = fixedFee && typeof fixedFee.amount === 'number' && fixedFee.amount > 0;

    if (mode === 'RATE' && !hasRate) {
      throw new Error('RATE 模式下费率必须大于0');
    }
    if (mode === 'FIXED' && !hasFixed) {
      throw new Error('FIXED 模式下固定费用必须大于0');
    }
    if (mode === 'RATE_PLUS_FIXED' && !hasRate && !hasFixed) {
      throw new Error('RATE_PLUS_FIXED 模式下费率或固定费用至少一个大于0');
    }
  }

  function toPositiveDigitsOrEmpty(raw) {
    var normalized = String(raw || '').trim();
    if (!normalized) {
      return '';
    }
    if (!/^\d+$/.test(normalized) || normalized === '0') {
      throw new Error('查询参数必须为正整数');
    }
    return normalized;
  }

  function formatMoney(money) {
    if (!money) {
      return '-';
    }
    var amount = typeof money.amount === 'number' ? money.amount : Number(money.amount);
    if (!Number.isFinite(amount)) {
      return '-';
    }
    return '¥' + amount.toFixed(2);
  }

  function formatFundAmount(value) {
    if (value == null || value === '') {
      return '-';
    }
    var amount = typeof value === 'number' ? value : Number(value);
    if (!Number.isFinite(amount)) {
      return '-';
    }
    return amount.toFixed(4);
  }

  function formatRate(rate) {
    if (rate == null || rate === '') {
      return '-';
    }
    var value = typeof rate === 'number' ? rate : Number(rate);
    if (!Number.isFinite(value)) {
      return '-';
    }
    return value.toFixed(6);
  }

  function formatBooleanText(value) {
    if (value === true) {
      return '是';
    }
    if (value === false) {
      return '否';
    }
    return '-';
  }

  function formatDateTime(raw) {
    if (!raw) {
      return '-';
    }
    var text = String(raw);
    return text.replace('T', ' ');
  }

  function buildQueryString(params) {
    var pairs = [];
    Object.keys(params || {}).forEach(function (key) {
      var value = params[key];
      if (value == null) {
        return;
      }
      var normalized = String(value).trim();
      if (!normalized) {
        return;
      }
      pairs.push(encodeURIComponent(key) + '=' + encodeURIComponent(normalized));
    });
    return pairs.length > 0 ? '?' + pairs.join('&') : '';
  }

  function extractDownloadFilename(contentDisposition) {
    var text = String(contentDisposition || '').trim();
    if (!text) {
      return '';
    }
    var utf8Match = text.match(/filename\*=UTF-8''([^;]+)/i);
    if (utf8Match && utf8Match[1]) {
      try {
        return decodeURIComponent(utf8Match[1]);
      } catch (error) {
        return utf8Match[1];
      }
    }
    var quotedMatch = text.match(/filename=\"([^\"]+)\"/i);
    if (quotedMatch && quotedMatch[1]) {
      return quotedMatch[1];
    }
    var plainMatch = text.match(/filename=([^;]+)/i);
    if (plainMatch && plainMatch[1]) {
      return plainMatch[1].trim();
    }
    return '';
  }

  function pad2(value) {
    return value < 10 ? '0' + value : String(value);
  }

  function buildDateTimeLocal(offsetDays) {
    var base = new Date();
    base.setDate(base.getDate() + (offsetDays || 0));
    return (
      base.getFullYear() +
      '-' +
      pad2(base.getMonth() + 1) +
      '-' +
      pad2(base.getDate()) +
      'T' +
      pad2(base.getHours()) +
      ':' +
      pad2(base.getMinutes())
    );
  }

  function createFallbackPageInit() {
    var displayName = (state.session && state.session.displayName) || (state.session && state.session.username) || '管理员';
    return {
      admin: {
        adminId: state.session ? state.session.adminId : null,
        username: state.session ? state.session.username : '',
        displayName: displayName,
        accountStatus: 'ACTIVE',
        lastLoginAt: null,
      },
      menus: [],
      couponSummary: null,
      roles: [],
      permissions: [],
    };
  }

  function isAuthError(error) {
    var message = error && error.message ? String(error.message) : '';
    return message.indexOf('鉴权失败') >= 0 || message.indexOf('权限不足') >= 0 || message.indexOf('管理员不存在') >= 0;
  }

  function loadCachedPageInit() {
    try {
      var raw = localStorage.getItem(PAGE_INIT_CACHE_KEY);
      if (!raw) {
        return null;
      }
      var parsed = parseJsonWithLargeIntegerSupport(raw);
      if (!parsed || !Array.isArray(parsed.menus)) {
        return null;
      }
      return parsed;
    } catch (error) {
      return null;
    }
  }

  function saveCachedPageInit(pageInit) {
    if (!pageInit || !Array.isArray(pageInit.menus)) {
      return;
    }
    try {
      localStorage.setItem(PAGE_INIT_CACHE_KEY, JSON.stringify(pageInit));
    } catch (error) {
      // ignore quota or storage write failures
    }
  }

  function clearCachedPageInit() {
    try {
      localStorage.removeItem(PAGE_INIT_CACHE_KEY);
    } catch (error) {
      // ignore storage failures
    }
  }

  function loadSession() {
    try {
      var raw = localStorage.getItem(SESSION_KEY);
      if (!raw) {
        return null;
      }
      var parsed = parseJsonWithLargeIntegerSupport(raw);
      if (!parsed) {
        return null;
      }
      var adminId = normalizePositiveIdText(parsed.adminId);
      if (!adminId) {
        return null;
      }
      var accessToken = typeof parsed.accessToken === 'string' ? parsed.accessToken.trim() : '';
      if (!accessToken) {
        return null;
      }
      var tokenType = typeof parsed.tokenType === 'string' && parsed.tokenType.trim()
        ? parsed.tokenType.trim()
        : 'Bearer';
      parsed.adminId = adminId;
      parsed.accessToken = accessToken;
      parsed.tokenType = tokenType;
      return parsed;
    } catch (error) {
      return null;
    }
  }

  function loadLang() {
    try {
      var raw = localStorage.getItem(LANG_KEY);
      return raw === 'en' ? 'en' : 'zh';
    } catch (error) {
      return 'zh';
    }
  }

  function saveLang(lang) {
    try {
      localStorage.setItem(LANG_KEY, lang === 'en' ? 'en' : 'zh');
    } catch (error) {
      // ignore storage failures
    }
  }

  function saveSession(session) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  }

  function clearSession() {
    localStorage.removeItem(SESSION_KEY);
  }

  function buildAdminHeaders() {
    var headers = {
      'X-Admin-Id': String((state.session && state.session.adminId) || ''),
    };
    var accessToken = state.session && typeof state.session.accessToken === 'string'
      ? state.session.accessToken.trim()
      : '';
    if (accessToken) {
      var tokenType = state.session && typeof state.session.tokenType === 'string' && state.session.tokenType.trim()
        ? state.session.tokenType.trim()
        : 'Bearer';
      headers.Authorization = tokenType + ' ' + accessToken;
    }
    return headers;
  }

  function withJson(headers) {
    var merged = {};
    Object.keys(headers || {}).forEach(function (key) {
      merged[key] = headers[key];
    });
    merged['Content-Type'] = 'application/json';
    return merged;
  }

  function hasHeaderIgnoreCase(headers, targetName) {
    var normalizedTarget = String(targetName || '').trim().toLowerCase();
    if (!normalizedTarget) {
      return false;
    }
    return Object.keys(headers || {}).some(function (key) {
      return String(key || '').trim().toLowerCase() === normalizedTarget;
    });
  }

  function parseJsonWithLargeIntegerSupport(raw) {
    if (typeof raw !== 'string' || !raw) {
      return null;
    }
    return JSON.parse(quoteLargeIntegerLiterals(raw));
  }

  function quoteLargeIntegerLiterals(raw) {
    var result = '';
    var inString = false;
    var escaped = false;
    var index = 0;

    while (index < raw.length) {
      var char = raw.charAt(index);
      if (inString) {
        result += char;
        if (escaped) {
          escaped = false;
        } else if (char === '\\') {
          escaped = true;
        } else if (char === '"') {
          inString = false;
        }
        index += 1;
        continue;
      }

      if (char === '"') {
        inString = true;
        result += char;
        index += 1;
        continue;
      }

      if (char === '-' || (char >= '0' && char <= '9')) {
        var prevIndex = index - 1;
        while (prevIndex >= 0) {
          var prevChar = raw.charAt(prevIndex);
          if (prevChar === ' ' || prevChar === '\n' || prevChar === '\r' || prevChar === '\t') {
            prevIndex -= 1;
            continue;
          }
          break;
        }
        var prevSignificantChar = prevIndex >= 0 ? raw.charAt(prevIndex) : '';
        if (prevSignificantChar === ':' || prevSignificantChar === ',' || prevSignificantChar === '[') {
          var tokenEnd = index;
          if (raw.charAt(tokenEnd) === '-') {
            tokenEnd += 1;
          }
          var digitStart = tokenEnd;
          while (tokenEnd < raw.length) {
            var tokenChar = raw.charAt(tokenEnd);
            if (tokenChar >= '0' && tokenChar <= '9') {
              tokenEnd += 1;
              continue;
            }
            break;
          }
          var token = raw.slice(index, tokenEnd);
          var nextIndex = tokenEnd;
          while (nextIndex < raw.length) {
            var nextChar = raw.charAt(nextIndex);
            if (nextChar === ' ' || nextChar === '\n' || nextChar === '\r' || nextChar === '\t') {
              nextIndex += 1;
              continue;
            }
            break;
          }
          var nextSignificantChar = nextIndex < raw.length ? raw.charAt(nextIndex) : '';
          if ((nextSignificantChar === ',' || nextSignificantChar === '}' || nextSignificantChar === ']') && isUnsafeJsonIntegerToken(token)) {
            result += '"' + token + '"';
            index = tokenEnd;
            continue;
          }
        }
      }

      result += char;
      index += 1;
    }

    return result;
  }

  function isUnsafeJsonIntegerToken(token) {
    var normalized = String(token || '').trim();
    if (!/^[-]?\d+$/.test(normalized)) {
      return false;
    }

    var signless = normalized.charAt(0) === '-' ? normalized.slice(1) : normalized;
    signless = signless.replace(/^0+(?=\d)/, '');
    if (!signless) {
      signless = '0';
    }

    if (signless.length < 16) {
      return false;
    }

    if (typeof BigInt === 'function') {
      try {
        var value = BigInt(normalized);
        return value > BigInt(Number.MAX_SAFE_INTEGER) || value < BigInt(Number.MIN_SAFE_INTEGER);
      } catch (error) {
        // ignore and fall back to string comparison
      }
    }

    var boundary = normalized.charAt(0) === '-' ? '9007199254740991' : '9007199254740991';
    if (signless.length !== boundary.length) {
      return signless.length > boundary.length;
    }
    return signless > boundary;
  }

  async function requestJson(url, options) {
    var requestOptions = options || {};
    var mergedOptions = {};
    Object.keys(requestOptions).forEach(function (key) {
      mergedOptions[key] = requestOptions[key];
    });
    var mergedHeaders = {};
    Object.keys(requestOptions.headers || {}).forEach(function (key) {
      mergedHeaders[key] = requestOptions.headers[key];
    });
    if (
      requestOptions.body != null &&
      typeof requestOptions.body === 'string' &&
      !hasHeaderIgnoreCase(mergedHeaders, 'Content-Type')
    ) {
      mergedHeaders['Content-Type'] = 'application/json';
    }
    mergedOptions.headers = mergedHeaders;
    var response = await fetch(url, mergedOptions);
    var payload = await safeJson(response);

    if (!response.ok) {
      var httpErrorMessage = readErrorMessage(payload, response.status, '请求失败');
      if (response.status === 401) {
        forceReloginWhenSessionExpired(httpErrorMessage);
      }
      throw new Error(httpErrorMessage);
    }

    if (!payload || payload.success !== true) {
      var businessErrorMessage = readErrorMessage(payload, response.status, '请求返回失败');
      if (isSessionExpiredMessage(businessErrorMessage)) {
        forceReloginWhenSessionExpired(businessErrorMessage);
      }
      throw new Error(businessErrorMessage);
    }

    return payload.data;
  }

  async function safeJson(response) {
    try {
      var raw = await response.text();
      return parseJsonWithLargeIntegerSupport(raw);
    } catch (error) {
      return null;
    }
  }

  function readErrorMessage(payload, status, fallback) {
    if (payload && payload.error && typeof payload.error.message === 'string' && payload.error.message) {
      var localizedMessage = localizeText(payload.error.message);
      var errorCode = payload.error && typeof payload.error.code === 'string' ? payload.error.code : '';
      if (localizedMessage === '服务器开小差，请稍后再试' && errorCode) {
        return localizedMessage + '（' + errorCode + '）';
      }
      return localizedMessage;
    }
    if (status === 401) {
      return localizeText('后台鉴权失败，请重新登录');
    }
    if (status === 403) {
      return localizeText('权限不足，无法访问该页面');
    }
    return localizeText(fallback);
  }

  function isSessionExpiredMessage(message) {
    var normalized = String(message == null ? '' : message);
    if (!normalized) {
      return false;
    }
    return normalized.indexOf('鉴权失败') >= 0
      || normalized.indexOf('会话已过期') >= 0
      || normalized.indexOf('登录已过期') >= 0
      || normalized.indexOf('请重新登录') >= 0
      || normalized.indexOf('Authentication failed') >= 0
      || normalized.indexOf('sign in again') >= 0;
  }

  function forceReloginWhenSessionExpired(message) {
    if (!state.session || !state.session.adminId) {
      return;
    }
    onLogout();
    setLoginTip(message || localizeText('后台鉴权失败，请重新登录'));
  }

  function setLoginTip(text) {
    loginTip.textContent = localizeText(text);
  }

  function toSet(values) {
    var set = new Set();
    (values || []).forEach(function (value) {
      if (value) {
        set.add(value);
      }
    });
    return set;
  }

  function escapeHtml(text) {
    return String(text == null ? '' : text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/\"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }
})();
