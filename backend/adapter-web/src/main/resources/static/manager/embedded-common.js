(function (global) {
  var SESSION_KEYS = ['openaipay.admin.session.v2', 'openaipay.admin.session.v1'];
  var LANG_KEY = 'openaipay.admin.lang.v1';
  var EN_TEXT_MAP = {
    '请先登录管理后台': 'Please sign in to the admin console',
    '当前页面无法读取管理员会话，请返回后台登录后重试。': 'This page cannot read the admin session. Please return to the admin console and sign in again.',
    '当前资金中心无法读取管理员会话，请返回后台登录后重试。': 'Fund Center cannot read the admin session. Please return to the admin console and sign in again.',
    '当前消息中心无法读取管理员会话，请返回后台登录后重试。': 'Message Center cannot read the admin session. Please return to the admin console and sign in again.',
    '当前风控中心无法读取管理员会话，请返回后台登录后重试。': 'Risk Control cannot read the admin session. Please return to the admin console and sign in again.',
    '当前出入金中心无法读取管理员会话，请返回后台登录后重试。': 'Capital Flow Center cannot read the admin session. Please return to the admin console and sign in again.',
    '当前投放配置页无法读取管理员会话，请返回后台登录后重试。': 'Deliver Console cannot read the admin session. Please return to the admin console and sign in again.',
    '返回登录': 'Back to Login',
    '查询': 'Query',
    '重置': 'Reset',
    '详情': 'Details',
    '修改': 'Edit',
    '操作': 'Actions',
    '查看': 'View',
    '取消': 'Cancel',
    '保存': 'Save',
    '状态': 'Status',
    '类型': 'Type',
    '说明': 'Description',
    '默认': 'Default',
    '工具': 'Tool',
    '是': 'Yes',
    '否': 'No',
    '预览': 'Preview',
    '暂无': 'N/A',
    '暂无数据': 'No data',
    '记录': 'Record',
    '备注': 'Remark',
    '原因': 'Reason',
    '请求失败': 'Request failed',
    '请求返回失败': 'Request failed',
    '后台鉴权失败，请重新登录': 'Authentication failed, please sign in again',
    '权限不足，无法访问当前页面': 'Permission denied for this page',
    '权限不足，无法访问投放配置': 'Permission denied for Deliver Console',
    '部分数据加载失败': 'Some data failed to load',
    '查询失败': 'Query failed',
    '查询完成': 'Query completed',
    '正在查询...': 'Querying...',
    '正在加载': 'Loading',
    '正在加载数据...': 'Loading data...',
    '已加载': ' loaded',
    '已刷新': ' refreshed',
    '加载失败': ' failed to load',
    '资金中心': 'Fund Center',
    '正在加载钱包、基金、授信、借贷、银行卡与收银台数据...': 'Loading wallet, fund, credit, loan, bank card, and cashier data...',
    '正在加载钱包账户数据...': 'Loading wallet account data...',
    '正在加载基金账户数据...': 'Loading fund account data...',
    '正在加载授信账户数据...': 'Loading credit account data...',
    '正在加载借贷账户数据...': 'Loading loan account data...',
    '正在加载银行卡数据...': 'Loading bank card data...',
    '正在加载收银台数据...': 'Loading cashier data...',
    '正在加载当前标签页数据...': 'Loading current tab data...',
    '消息中心': 'Message Center',
    '正在加载会话、消息、红包与联系人数据...': 'Loading conversations, messages, red packets, and contact data...',
    '风控中心': 'Risk Control',
    '正在加载 KYC、风险档案与黑名单数据...': 'Loading KYC, risk profiles, and blacklist data...',
    '统一查看 KYC、风险等级、双因子、设备锁与联系人黑名单。': 'View KYC, risk level, two-factor mode, device lock, and contact blacklist in one place.',
    '出入金中心': 'Capital Flow Center',
    '入金中心': 'Inbound Center',
    '出金中心': 'Outbound Center',
    '投放配置台': 'Deliver Console',
    '资金中心 - 钱包账户': 'Fund Center - Wallet Accounts',
    '资金中心 - 基金账户': 'Fund Center - Fund Accounts',
    '资金中心 - 授信账户': 'Fund Center - Credit Accounts',
    '资金中心 - 借贷账户': 'Fund Center - Loan Accounts',
    '资金中心 - 银行卡': 'Fund Center - Bank Cards',
    '资金中心 - 收银台': 'Fund Center - Cashier',
    '顶部切换独立 tab 页面，每个页面只承载一类资金能力。': 'Switch top tabs to separate standalone pages. Each page focuses on one fund capability.',
    '当前管理员：': 'Current Admin: ',
    '刷新当前页': 'Refresh Current Page',
    '刷新数据': 'Refresh Data',
    '资源统计': 'Resource Stats',
    '钱包账户': 'Wallet Accounts',
    '基金账户': 'Fund Accounts',
    '授信账户': 'Credit Accounts',
    '借贷账户': 'Loan Accounts',
    '银行卡': 'Bank Cards',
    '收银台': 'Cashier',
    '独立页面，仅展示钱包账户查询与结果。': 'Standalone page showing wallet account queries and results only.',
    '独立页面，仅展示基金账户、份额、收益与 NAV。': 'Standalone page showing fund accounts, shares, income, and NAV only.',
    '独立页面，仅展示授信额度账户。': 'Standalone page showing credit limit accounts only.',
    '独立页面，仅展示借贷账户与还款状态。': 'Standalone page showing loan accounts and repayment status only.',
    '独立页面，仅展示绑卡与限额配置。': 'Standalone page showing bank card bindings and limit settings only.',
    '独立页面，查询用户场景下的可用支付工具并做价格试算。': 'Standalone page for querying available payment tools and running pricing previews for a user scenario.',
    '当前场景下后端返回的可用支付工具列表。': 'Available payment tools returned by backend for the current scenario.',
    '可在右上输入金额与支付方式后进行试算。': 'Enter amount and payment method at top right to run a pricing preview.',
    '先输入用户ID并点击“查询收银台”。': 'Enter a user ID first, then click "Query Cashier".',
    '暂无场景配置返回。': 'No scene configuration returned.',
    '场景配置': 'Scene Configuration',
    '定价预览': 'Pricing Preview',
    '支付工具': 'Payment Tools',
    '试算详情': 'Pricing Details',
    '查询收银台': 'Query Cashier',
    '价格试算': 'Pricing Preview',
    '收银台筛选已重置': 'Cashier filters reset',
    '正在加载收银台视图...': 'Loading cashier view...',
    '正在试算价格...': 'Calculating price...',
    '收银台视图已加载': 'Cashier view loaded',
    '收银台试算已完成': 'Cashier preview completed',
    '收银台请求失败': 'Cashier request failed',
    '资金中心已加载': 'Fund Center loaded',
    '资金中心已刷新': 'Fund Center refreshed',
    '当前场景暂无可用支付工具': 'No payment tools available for this scenario',
    '余额': 'Balance',
    '爱存': 'AiCash',
    '爱花': 'AiCredit',
    '爱借': 'AiLoan',
    '用户ID': 'User ID',
    '账户状态': 'Account Status',
    '账号状态': 'Account Status',
    '卡状态': 'Card Status',
    '条数': 'Limit',
    '页码': 'Page',
    '每页条数': 'Page Size',
    '上一页': 'Prev',
    '下一页': 'Next',
    '第 ': 'Page ',
    ' 页': '',
    '基金代码': 'Fund Code',
    '银行编码': 'Bank Code',
    '支付方式': 'Payment Method',
    '金额': 'Amount',
    '币种': 'Currency',
    '场景编码': 'Scene Code',
    '用户': 'User',
    '基金': 'Fund',
    '可用份额': 'Available Share',
    '可用余额': 'Available Balance',
    '冻结/预留': 'Frozen / Reserved',
    '冻结/待处理': 'Frozen / Pending',
    '收益': 'Income',
    '更新时间': 'Updated At',
    '卡信息': 'Card Info',
    '银行卡信息': 'Bank Card Info',
    '银行': 'Bank',
    '限额': 'Limit',
    '账户号': 'Account No.',
    '额度': 'Limit Amount',
    '本金/逾期': 'Principal / Overdue',
    '利息/罚息': 'Interest / Penalty',
    '还款状态': 'Repayment Status',
    '原始金额': 'Original Amount',
    '手续费': 'Fee',
    '应付金额': 'Payable Amount',
    '清算金额': 'Settlement Amount',
    '费率': 'Fee Rate',
    '费用承担方': 'Fee Bearer',
    '付款方': 'Payer',
    '收款方': 'Payee',
    '平台': 'Platform',
    '储蓄卡': 'Debit Card',
    '信用卡': 'Credit Card',
    '报价单号': 'Quote No.',
    '规则编码': 'Rule Code',
    '规则名称': 'Rule Name',
    '定价场景': 'Pricing Scene',
    '默认卡 ': 'Default Card ',
    '尾号 ': 'Tail No. ',
    '日限额 ': 'Daily Limit ',
    '申购 ': 'Subscribe ',
    '赎回 ': 'Redeem ',
    '逾期 ': 'Overdue ',
    '可用 ': 'Available ',
    '昨日 ': 'Yesterday ',
    '渠道：': 'Channels: ',
    '未配置': 'Not configured',
    '，手续费 ': ', fee ',
    '，应付 ': ', payable ',
    '罚息 ': 'Penalty Interest ',
    '如 ACTIVE（正常）/ FROZEN（冻结）': 'For example: ACTIVE (Active) / FROZEN (Frozen)',
    '如 ACTIVE（正常）/ OVERDUE（逾期）': 'For example: ACTIVE (Active) / OVERDUE (Overdue)',
    '如 NORMAL（正常）/ OVERDUE（逾期）': 'For example: NORMAL (Active) / OVERDUE (Overdue)',
    '如 ACTIVE（正常）/ UNBOUND（已解绑）': 'For example: ACTIVE (Active) / UNBOUND (Unbound)',
    '如 WALLET（余额）/ BANK_CARD（银行卡）': 'For example: WALLET (Balance) / BANK_CARD (Bank Cards)',
    '；银行卡策略：': '; Bank Card Policy: ',
    '；空态文案：': '; Empty State: ',
    '/ 还款日 ': ' / Repay Day ',
    '消息中心': 'Message Center',
    '会话管理': 'Conversation Management',
    '消息记录': 'Message Records',
    '红包记录': 'Red Packet Records',
    '好友申请': 'Contact Requests',
    '好友关系': 'Friendships',
    '黑名单': 'Blacklist',
    '正在加载会话数据...': 'Loading conversation data...',
    '正在加载消息记录...': 'Loading message records...',
    '正在加载红包记录...': 'Loading red packet records...',
    '正在加载好友申请...': 'Loading contact requests...',
    '正在加载好友关系...': 'Loading friendships...',
    '正在加载黑名单记录...': 'Loading blacklist records...',
    '会话详情': 'Conversation Details',
    '会话类型': 'Conversation Type',
    '会话号': 'Conversation No.',
    '会话数': 'Conversations',
    '业务键': 'Business Key',
    '成员': 'Members',
    '成员数': 'Members',
    '未读': 'Unread',
    '消息数': 'Messages',
    '红包单': 'Red Packets',
    '待处理申请': 'Pending Requests',
    '最近消息': 'Latest Message',
    '最近已读': 'Last Read',
    '最近活跃': 'Last Active',
    '最后活跃': 'Last Active',
    '消息ID': 'Message ID',
    '消息类型': 'Message Type',
    '发送方': 'Sender',
    '接收方': 'Receiver',
    '发送时间': 'Sent At',
    '内容': 'Content',
    '收发双方': 'Sender / Receiver',
    '发送/接收': 'Sender / Receiver',
    '金额/交易': 'Amount / Trade',
    '红包单号': 'Red Packet No.',
    '结果码': 'Result Code',
    '结果说明': 'Result Description',
    '申请单号': 'Request No.',
    '申请人': 'Requester',
    '目标用户': 'Target User',
    '处理状态': 'Processing Status',
    '处理信息': 'Processing Info',
    '申请文案': 'Request Message',
    '来源申请': 'Source Request',
    '好友': 'Friend',
    '好友用户': 'Friend User',
    '主用户': 'Owner User',
    '对端': 'Peer',
    '开关': 'Switches',
    '关键字': 'Keyword',
    '会话号 / bizKey / 消息摘要': 'Conversation No. / bizKey / Message Preview',
    '按会话成员筛选': 'Filter by conversation member',
    '如 TEXT（文本）/ TRANSFER（转账）/ RED_PACKET（红包）': 'For example: TEXT (Text) / TRANSFER (Transfer) / RED_PACKET (Red Packet)',
    '如 PENDING_CLAIM（待领取）/ CLAIMED（已领取）': 'For example: PENDING_CLAIM (Pending Claim) / CLAIMED (Claimed)',
    '如 PENDING（待处理）/ ACCEPTED（已通过）': 'For example: PENDING (Pending) / ACCEPTED (Accepted)',
    '免打扰 ': 'Mute ',
    ' / 置顶 ': ' / Pinned ',
    '时间': 'Time',
    '资金链路': 'Capital Flow',
    '点击左侧会话可查看成员与最近消息。': 'Click a conversation on the left to view members and recent messages.',
    '详情查看改为弹窗处理，主页面仅保留会话列表。': 'Details are shown in a modal. The main page only keeps the conversation list.',
    '支持按会话、消息类型、收发双方排查消息状态与交易挂载。': 'Inspect message status and linked trades by conversation, message type, sender, and receiver.',
    '支持按会话号、业务键、对端用户回溯消息链路。': 'Supports tracing message flows by conversation no., business key, and peer user.',
    '查看红包消息与资金单号挂载情况。': 'Inspect red packet messages and linked capital order numbers.',
    '查看好友申请状态与处理时点。': 'Inspect contact request status and handling timestamps.',
    '用于核对双向好友是否正确落库。': 'Used to verify whether bidirectional friendships are stored correctly.',
    '排查联系人黑名单与风控黑名单联动前的数据基础。': 'Check source data before contact blacklist and risk blacklist linkage.',
    '暂无会话数据': 'No conversation data',
    '暂无消息记录': 'No message records',
    '暂无红包记录': 'No red packet records',
    '暂无好友申请': 'No contact requests',
    '暂无好友关系': 'No friendships',
    '暂无黑名单记录': 'No blacklist records',
    '暂无黑名单数据': 'No blacklist data',
    '暂无成员数据': 'No member data',
    '暂无最近消息': 'No recent messages',
    '正在加载会话详情...': 'Loading conversation details...',
    '会话详情已加载': 'Conversation details loaded',
    '会话详情加载失败': 'Failed to load conversation details',
    '风控中心': 'Risk Control',
    '风控概览': 'Risk Overview',
    '风险用户': 'Risk Users',
    '风险档案': 'Risk Profile',
    '风险档案详情': 'Risk Profile Details',
    '修改风险档案': 'Edit Risk Profile',
    '调整风险档案': 'Adjust Risk Profile',
    '总用户': 'Total Users',
    'KYC等级': 'KYC Level',
    '风险等级': 'Risk Level',
    '双因子': 'Two-Factor',
    '设备锁': 'Device Lock',
    '隐私模式': 'Privacy Mode',
    '手机号搜索': 'Mobile Search',
    'UID搜索': 'UID Search',
    '隐私开关': 'Privacy Switches',
    '隐藏实名': 'Hide Real Name',
    '个性化推荐': 'Personalized Recommendation',
    '被拉黑用户': 'Blocked User',
    '联系人黑名单': 'Contact Blacklist',
    '爱付号': 'AiPay ID',
    '登录号': 'Login ID',
    '展示名': 'Display Name',
    '手机号': 'Mobile No.',
    '点击左侧用户查看完整风险档案。': 'Click a user on the left to view the full risk profile.',
    '详情查看改为弹窗处理，主页面仅保留风险用户列表。': 'Details are shown in a modal. The main page only keeps the risk user list.',
    '查看 KYC、风险等级、双因子与隐私/设备策略。': 'View KYC, risk level, two-factor mode, and privacy / device policies.',
    '复用联系人域黑名单，辅助风控排查封禁关系。': 'Reuse the contact-domain blacklist to help risk control investigate blocking relations.',
    '如 低风险 / 中风险 / 高风险': 'For example: Low Risk / Medium Risk / High Risk',
    '如 L0 未实名 / L1 基础实名': 'For example: L0 Unverified / L1 Basic Verified',
    '如 L0 / L1 / L2 / L3': 'For example: L0 / L1 / L2 / L3',
    '如 LOW / MEDIUM / HIGH': 'For example: LOW / MEDIUM / HIGH',
    '如 NONE / SMS / APP / BIOMETRIC': 'For example: NONE / SMS / APP / BIOMETRIC',
    'userId / aipayUid / 登录号 / 昵称': 'userId / aipayUid / loginId / nickname',
    '风险 ': 'Risk ',
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
    '暂无风险用户数据': 'No risk user data',
    '风险用户列表已刷新': 'Risk user list refreshed',
    '黑名单列表已刷新': 'Blacklist refreshed',
    '未找到待编辑用户': 'Editable user not found',
    '正在保存风险档案...': 'Saving risk profile...',
    '风险档案已更新': 'Risk profile updated',
    '风险档案保存失败': 'Failed to save risk profile',
    '风控中心已加载': 'Risk Control loaded',
    '风控中心已刷新': 'Risk Control refreshed',
    '出入金中心': 'Capital Flow Center',
    '入金中心': 'Inbound Center',
    '出金中心': 'Outbound Center',
    '正在加载入金与出金订单数据...': 'Loading inbound and outbound orders...',
    '正在加载入金订单数据...': 'Loading inbound orders...',
    '正在加载出金订单数据...': 'Loading outbound orders...',
    '总量': 'Total',
    '成功': 'Success',
    '处理中': 'Processing',
    '失败': 'Failed',
    '入金订单': 'Inbound Orders',
    '出金订单': 'Outbound Orders',
    '入金详情': 'Inbound Details',
    '出金详情': 'Outbound Details',
    '入金单号': 'Inbound No.',
    '出金单号': 'Outbound No.',
    '业务请求号': 'Business Request No.',
    '支付单号': 'Pay Order No.',
    '处理金额': 'Processed Amount',
    '记账金额': 'Accounted Amount',
    '点击左侧入金订单查看完整链路字段。': 'Click an inbound order on the left to view full flow fields.',
    '点击左侧出金订单查看完整链路字段。': 'Click an outbound order on the left to view full flow fields.',
    '详情查看改为弹窗处理，列表页仅保留订单列表。': 'Details are shown in a modal. The list page only keeps the order list.',
    '查看入金订单状态、结果码与清算链路字段。': 'Inspect inbound order status, result codes, and settlement flow fields.',
    '查看出金订单状态、结果码与通道响应。': 'Inspect outbound order status, result codes, and channel responses.',
    '统一查看入金与出金订单状态、结果码与通道链路字段。': 'Inspect inbound and outbound order status, result codes, and channel flow fields in one place.',
    '对齐 C 端充值/入金链路，排查订单状态、结果码与通道链路字段。': 'Align with C-end top-up / inbound flows to inspect order status, result codes, and channel flow fields.',
    '对齐提现/转出链路，排查订单状态、结果码与通道响应。': 'Align with withdrawal / transfer-out flows to inspect order status, result codes, and channel responses.',
    '入金订单列表已刷新': 'Inbound order list refreshed',
    '出金订单列表已刷新': 'Outbound order list refreshed',
    '正在加载入金详情...': 'Loading inbound details...',
    '入金详情已加载': 'Inbound details loaded',
    '入金详情加载失败': 'Failed to load inbound details',
    '正在加载出金详情...': 'Loading outbound details...',
    '出金详情已加载': 'Outbound details loaded',
    '出金详情加载失败': 'Failed to load outbound details',
    '机构ID': 'Institution ID',
    '付款账号': 'Payer Account',
    '收款账号': 'Payee Account',
    '财务处理编码': 'Financial Processing Code',
    '支付渠道编码': 'Payment Channel Code',
    '提交时间': 'Submitted At',
    '响应时间': 'Responded At',
    '清算时间': 'Settled At',
    '如 SUBMITTED（已提交）/ ACCEPTED（已受理）/ SUCCEEDED（成功）': 'For example: SUBMITTED (Submitted) / ACCEPTED (Accepted) / SUCCEEDED (Success)',
    '如 CREATED（已创建）/ SUBMITTED（已提交）/ ACCEPTED（已受理）/ SUCCEEDED（成功）': 'For example: CREATED (Created) / SUBMITTED (Submitted) / ACCEPTED (Accepted) / SUCCEEDED (Success)',
    '清算 ': 'Settlement ',
    '单聊': 'Private Chat',
    '群聊': 'Group Chat',
    '系统会话': 'System Conversation',
    '服务会话': 'Service Conversation',
    '系统': 'System',
    '文本': 'Text',
    '图片': 'Image',
    '已发送': 'Sent',
    '已撤回': 'Recalled',
    '发送失败': 'Send Failed',
    '发送中': 'Sending',
    '正常': 'Active',
    '冻结': 'Frozen',
    '关闭': 'Closed',
    '逾期': 'Overdue',
    '已解绑': 'Unbound',
    '待处理': 'Pending',
    '已通过': 'Accepted',
    '已拒绝': 'Rejected',
    '已取消': 'Canceled',
    '待领取': 'Pending Claim',
    '已领取': 'Claimed',
    '已过期': 'Expired',
    '已退回': 'Refunded',
    '已创建': 'Created',
    '初始化': 'Initialized',
    '已提交': 'Submitted',
    '已受理': 'Accepted',
    '清算中': 'Settling',
    '已清算': 'Settled',
    '已安装': 'Installed',
    '未活跃': 'Inactive',
    '已卸载': 'Uninstalled',
    '草稿': 'Draft',
    '已发布': 'Published',
    '已下线': 'Offline',
    '编辑中': 'Editing',
    '已结清': 'Settled',
    '投放配置台': 'Deliver Console',
    '正在拉取展位、投放单元与素材配置...': 'Loading placements, ad units, and materials...',
    '正在加载投放管理数据...': 'Loading deliver management data...',
    '按“展位 / 投放单元 / 素材”分层管理，创意与规则在单元与创意详情内维护。': 'Manage by Placement / Ad Unit / Material. Creatives and rules are maintained in ad unit and creative details.',
    '展位': 'Placement',
    '投放单元': 'Ad Unit',
    '素材': 'Material',
    '创意': 'Creative',
    '展位列表': 'Placement List',
    '投放单元列表': 'Ad Unit List',
    '素材列表': 'Material List',
    '创意列表': 'Creative List',
    '关系列表': 'Relation List',
    '疲劳规则列表': 'Fatigue Rule List',
    '定向规则列表': 'Targeting Rule List',
    '展位详情': 'Placement Details',
    '投放单元详情': 'Ad Unit Details',
    '素材详情': 'Material Details',
    '创意详情': 'Creative Details',
    '关系详情': 'Relation Details',
    '疲劳规则详情': 'Fatigue Rule Details',
    '定向规则详情': 'Targeting Rule Details',
    '按展位管理关联投放单元，可在详情中增加或剔除单元。': 'Manage associated ad units by placement. Add or remove units in details.',
    '投放单元内维护素材绑定、创意生成及规则配置。': 'Maintain material binding, creative generation, and rule settings within ad units.',
    '维护素材内容，供投放单元关联后自动生成创意。': 'Maintain material content so that creatives can be generated automatically after ad unit binding.',
    '新建展位': 'New Placement',
    '新建投放单元': 'New Ad Unit',
    '新建素材': 'New Material',
    '新建创意': 'New Creative',
    '新建关系': 'New Relation',
    '新建疲劳规则': 'New Fatigue Rule',
    '新建定向规则': 'New Targeting Rule',
    '保存展位': 'Save Placement',
    '保存单元': 'Save Ad Unit',
    '保存素材': 'Save Material',
    '保存创意': 'Save Creative',
    '保存关系': 'Save Relation',
    '保存疲劳规则': 'Save Fatigue Rule',
    '保存定向规则': 'Save Targeting Rule',
    '取消新建': 'Cancel Create',
    '返回详情': 'Back to Details',
    '新建配置': 'Create Configuration',
    '编辑配置': 'Edit Configuration',
    '展位编码': 'Placement Code',
    '展位名称': 'Placement Name',
    '展位类型': 'Placement Type',
    '轮播秒数': 'Carousel Seconds',
    '排序方式': 'Sort Type',
    '排序规则': 'Sort Rule',
    '兜底开关': 'Fallback Switch',
    '发布时间': 'Published At',
    '单元编码': 'Unit Code',
    '单元名称': 'Unit Name',
    '素材编码': 'Material Code',
    '素材名称': 'Material Name',
    '素材类型': 'Material Type',
    '素材标题': 'Material Title',
    '图片地址': 'Image URL',
    '预览图': 'Preview Image',
    '无预览图': 'No Preview',
    '点击查看大图': 'Click to view full image',
    '缩略图': 'Thumbnail',
    '创意编码': 'Creative Code',
    '创意名称': 'Creative Name',
    '所属单元': 'Owning Unit',
    '绑定素材': 'Bound Material',
    '绑定关系': 'Binding Relation',
    '展示顺序': 'Display Order',
    '发布状态': 'Publish Status',
    '生效开始': 'Active From',
    '生效结束': 'Active To',
    '优先级': 'Priority',
    '权重': 'Weight',
    '兜底': 'Fallback',
    '启用': 'Enabled',
    '执行配置': 'Execution Settings',
    '投放配置': 'Deliver Settings',
    '投放链路': 'Deliver Flow',
    '规则': 'Rule',
    '频控条件': 'Frequency Cap Conditions',
    '条件': 'Conditions',
    '匹配': 'Match',
    '创意数量': 'Creative Count',
    '关系ID': 'Relation ID',
    '关联投放单元': 'Associate Ad Unit',
    '关联素材并生成创意': 'Bind Material and Generate Creative',
    '增加投放单元': 'Add Ad Unit',
    '查看单元': 'View Unit',
    '查看创意': 'View Creative',
    '进入修改页': 'Go to Edit',
    '修改单元': 'Edit Unit',
    '修改素材': 'Edit Material',
    '修改创意': 'Edit Creative',
    '修改关系': 'Edit Relation',
    '修改规则': 'Edit Rule',
    '新增规则': 'New Rule',
    '剔除': 'Remove',
    '新建单元': 'New Unit',
    '编辑投放单元 #': 'Edit Ad Unit #',
    '编辑素材 #': 'Edit Material #',
    '编辑创意 #': 'Edit Creative #',
    '编辑关系 #': 'Edit Relation #',
    '编辑疲劳规则 #': 'Edit Fatigue Rule #',
    '编辑定向规则 #': 'Edit Targeting Rule #',
    '下线创意': 'Offline Creative',
    '再上线': 'Bring Online Again',
    '删除创意': 'Delete Creative',
    '实体类型': 'Entity Type',
    '实体编码': 'Entity Code',
    '生效窗口': 'Active Window',
    '落地页': 'Landing Page',
    '事件类型': 'Event Type',
    '时间窗口(分钟)': 'Time Window (Minutes)',
    '时间窗口': 'Time Window',
    '次数上限': 'Max Count',
    '最大返回数': 'Max Return Count',
    '定向类型': 'Targeting Type',
    '操作符': 'Operator',
    '定向值': 'Targeting Value',
    '启用状态': 'Enabled',
    '作用对象': 'Target Object',
    '作用实体': 'Target Entity',
    '关联关系数': 'Relation Count',
    '匹配条件': 'Match Conditions',
    '内容配置': 'Content Settings',
    '基础信息': 'Basic Info',
    '规则信息': 'Rule Info',
    '链路信息': 'Flow Info',
    '关联资源': 'Associated Resources',
    '关联创意': 'Associated Creatives',
    '关联展位': 'Associated Placements',
    '关联链路': 'Association Flow',
    '投放单元管理': 'Ad Unit Management',
    '规则配置': 'Rule Settings',
    '兜底创意': 'Fallback Creative',
    '兜底关系': 'Fallback Relation',
    '展位详情以弹出页查看，点击右侧按钮进入修改页。': 'Placement details are shown in a modal. Click the action on the right to enter edit mode.',
    '展位新增以弹出页完成。': 'Create placements in a modal.',
    '展位修改以弹出页完成。': 'Edit placements in a modal.',
    '当前展位暂无关联投放单元，请先增加投放单元。': 'No ad units are associated with this placement yet. Please add an ad unit first.',
    '当前展位暂无投放关系。': 'There are no deliver relations under the current placement.',
    '当前单元暂无创意。关联素材后将自动生成创意实体。': 'This unit has no creatives yet. Bind a material and a creative entity will be generated automatically.',
    '主界面仅展示创意列表，详情与编辑以弹窗打开。': 'The main page shows only the creative list. Details and editing open in modals.',
    '主界面仅展示关系列表，详情与编辑以弹窗打开。': 'The main page shows only the relation list. Details and editing open in modals.',
    '主界面仅展示疲劳规则列表，详情与编辑以弹窗打开。': 'The main page shows only the fatigue rule list. Details and editing open in modals.',
    '主界面仅展示定向规则列表，详情与编辑以弹窗打开。': 'The main page shows only the targeting rule list. Details and editing open in modals.',
    '仅查看关联创意与疲劳度/定向规则。': 'View associated creatives and fatigue / targeting rules only.',
    '查看关联位置、单元、素材与规则。': 'View associated placements, units, materials, and rules.',
    '新建投放单元并配置执行策略。': 'Create an ad unit and configure execution strategy.',
    '可修改单元信息、关联创意及疲劳度/定向规则。': 'Edit unit info, associated creatives, and fatigue / targeting rules.',
    '查看内容配置与引用关系。': 'View content settings and reference relations.',
    '新建素材并维护内容配置。': 'Create a material and maintain its content settings.',
    '修改当前素材配置。': 'Edit current material settings.',
    '新建创意并绑定单元与素材。': 'Create a creative and bind a unit and material.',
    '修改当前创意配置。': 'Edit current creative settings.',
    '查看当前关系连接的资源。': 'View resources connected by the current relation.',
    '新建展位、单元与创意的链路关系。': 'Create relation flows for placements, units, and creatives.',
    '修改当前关系配置。': 'Edit current relation settings.',
    '查看作用对象与频控参数。': 'View target objects and frequency cap parameters.',
    '新建疲劳规则并限制触达次数。': 'Create a fatigue rule to limit touch frequency.',
    '修改当前疲劳规则配置。': 'Edit current fatigue rule settings.',
    '查看作用对象与匹配条件。': 'View target objects and match conditions.',
    '新建定向规则并限定投放条件。': 'Create a targeting rule to constrain delivery conditions.',
    '修改当前定向规则配置。': 'Edit current targeting rule settings.',
    '暂无展位配置': 'No placement configuration',
    '暂无投放单元配置': 'No ad unit configuration',
    '暂无素材配置': 'No material configuration',
    '暂无关系配置': 'No relation configuration',
    '暂无创意配置': 'No creative configuration',
    '暂无定向规则。': 'No targeting rules.',
    '暂无疲劳度规则。': 'No fatigue rules.',
    '暂无定向规则，可在弹窗中新建': 'No targeting rules. Create one in the modal.',
    '暂无疲劳规则，可在弹窗中新建': 'No fatigue rules. Create one in the modal.',
    '暂无可选实体': 'No selectable entities',
    '暂无可选项': 'No options available',
    '投放配置已加载': 'Deliver Console loaded',
    '投放配置加载失败': 'Failed to load Deliver Console',
    '请在展位修改页操作投放单元': 'Please manage ad units from the placement edit page',
    '请在投放单元修改页操作创意关联': 'Please manage creative associations from the ad unit edit page',
    '请在投放单元修改页操作规则配置': 'Please manage rule settings from the ad unit edit page',
    '正在关联投放单元...': 'Associating ad unit...',
    '投放单元关联成功，已同步创意关系': 'Ad unit associated successfully. Creative relations have been synced.',
    '该展位下该投放单元已处于剔除状态': 'This ad unit is already removed under the current placement',
    '正在剔除投放单元...': 'Removing ad unit...',
    '投放单元已剔除': 'Ad unit removed',
    '剔除投放单元失败': 'Failed to remove ad unit',
    '正在关联素材并生成创意...': 'Binding material and generating creative...',
    '素材关联成功，已生成创意并同步投放关系': 'Material bound successfully. Creative generated and deliver relations synced.',
    '素材关联成功，已更新创意并同步投放关系': 'Material bound successfully. Creative updated and deliver relations synced.',
    '关联素材失败': 'Failed to bind material',
    '关联投放单元失败': 'Failed to associate ad unit',
    '正在下线创意...': 'Offlining creative...',
    '创意已下线': 'Creative offlined',
    '下线创意失败': 'Failed to offline creative',
    '正在上线创意...': 'Bringing creative online...',
    '创意已重新上线': 'Creative brought online again',
    '上线创意失败': 'Failed to bring creative online',
    '正在删除创意...': 'Deleting creative...',
    '创意已删除': 'Creative deleted',
    '删除创意失败': 'Failed to delete creative',
    '未填写标题': 'Untitled',
    '素材预览': 'Material Preview',
    '创意预览': 'Creative Preview',
    '投放关系': 'Deliver Relation',
    '疲劳规则': 'Fatigue Rule',
    '疲劳度规则': 'Fatigue Rules',
    '定向规则': 'Targeting Rule',
    '可空': 'Optional',
    '可空，默认可与图片地址一致': 'Optional. Defaults to the image URL if empty.',
    '可空，如 hand-picked': 'Optional, e.g. hand-picked',
    '素材预览图 URL': 'Material preview image URL',
    '越小越优先': 'Smaller value means higher priority',
    '返回 ': 'Return ',
    '轮播 ': 'Carousel ',
    '排序 ': 'Sort ',
    '优先级 ': 'Priority ',
    '权重 ': 'Weight ',
    '兜底 ': 'Fallback ',
    '启用 ': 'Enabled ',
    '顺序 ': 'Order ',
    '事件 ': 'Event ',
    '窗口 ': 'Window ',
    '上限 ': 'Limit ',
    '例如 1': 'For example: 1',
    '例如 3': 'For example: 3',
    '例如 5': 'For example: 5',
    '例如 20': 'For example: 20',
    '例如 100': 'For example: 100',
    '例如 1440': 'For example: 1440',
    '例如 单用户日曝光上限': 'For example: Single-user daily impression cap',
    '例如 首页横幅-红包会场红': 'For example: Home banner - red packet campaign red',
    '例如 首页横幅-主推红包会场': 'For example: Home banner - featured red packet campaign',
    '例如 首页横幅单元': 'For example: Home banner unit',
    '例如 首页腰封横幅': 'For example: Home waist-banner',
    '例如 替换首页常用区块': 'For example: Replace home common block',
    '例如 周末红包会场': 'For example: Weekend red packet campaign',
    '例如 {': 'For example: {',
    '例如 aipay://home?feature=deliver-home-banner': 'For example: aipay://home?feature=deliver-home-banner',
    '例如 CRT_HOME_BANNER_RED': 'For example: CRT_HOME_BANNER_RED',
    '例如 FAT_HOME_BANNER_POSITION_DAILY': 'For example: FAT_HOME_BANNER_POSITION_DAILY',
    '例如 HOME_COMMON_BANNER': 'For example: HOME_COMMON_BANNER',
    '例如 IOS_APP 或 IOS,ANDROID': 'For example: IOS_APP or IOS,ANDROID',
    '例如 MAT_HOME_BANNER_RED': 'For example: MAT_HOME_BANNER_RED',
    '例如 TRG_HOME_BANNER_CLIENT_IOS': 'For example: TRG_HOME_BANNER_CLIENT_IOS',
    '例如 UNIT_HOME_BANNER': 'For example: UNIT_HOME_BANNER',
    '例如 {\"action\":\"stay-on-home\",\"campaign\":\"weekend-redpacket\"}': 'For example: {\"action\":\"stay-on-home\",\"campaign\":\"weekend-redpacket\"}',
    '无图片': 'No Image',
    '无落地页': 'No Landing Page',
    '无 Schema': 'No Schema',
    '单元策略说明': 'Unit Strategy Description',
    '自动创意-': 'Auto Creative-',
    '横幅': 'Banner',
    '弹窗': 'Popup',
    '通用展位': 'Common Placement',
    '人工顺序': 'Manual Order',
    '视频': 'Video',
    '曝光': 'Impression',
    '点击': 'Click',
    '转化': 'Conversion',
    '用户标签': 'User Tag',
    '渠道': 'Channel',
    '场景': 'Scene',
    '客户端': 'Client',
    '时间范围': 'Time Range',
    '包含': 'IN',
    '不包含': 'NOT IN',
    '等于': 'Equals',
    '不等于': 'Not Equals',
    '正在保存配置...': 'Saving configuration...',
    '展位保存成功': 'Placement saved successfully',
    '展位保存失败': 'Failed to save placement',
    '投放单元保存成功': 'Ad unit saved successfully',
    '投放单元保存失败': 'Failed to save ad unit',
    '素材保存成功': 'Material saved successfully',
    '素材保存失败': 'Failed to save material',
    '创意保存成功': 'Creative saved successfully',
    '创意保存失败': 'Failed to save creative',
    '关系保存成功': 'Relation saved successfully',
    '关系保存失败': 'Failed to save relation',
    '疲劳规则保存成功': 'Fatigue rule saved successfully',
    '疲劳规则保存失败': 'Failed to save fatigue rule',
    '定向规则保存成功': 'Targeting rule saved successfully',
    '定向规则保存失败': 'Failed to save targeting rule',
    '展位参数不合法': 'Invalid placement parameters',
    '剔除参数不合法': 'Invalid remove parameters',
    '投放单元参数不合法': 'Invalid ad unit parameters',
    '删除参数不合法': 'Invalid delete parameters',
    '上线参数不合法': 'Invalid online parameters',
    '下线参数不合法': 'Invalid offline parameters',
    '未找到展位或投放单元，请刷新后重试': 'Placement or ad unit not found. Please refresh and try again.',
    '未找到投放单元或素材，请刷新后重试': 'Ad unit or material not found. Please refresh and try again.',
    '未找到创意，请刷新后重试': 'Creative not found. Please refresh and try again.',
    '该投放单元尚未关联素材，请先在投放单元详情中关联素材生成创意': 'This ad unit has not bound a material yet. Please bind a material in ad unit details first to generate a creative.',
    '未找到该展位，请刷新后重试。': 'Placement not found. Please refresh and try again.',
    '未找到该投放单元，请刷新后重试。': 'Ad unit not found. Please refresh and try again.',
    '未找到该素材，请刷新后重试。': 'Material not found. Please refresh and try again.',
    '未找到该创意，请刷新后重试。': 'Creative not found. Please refresh and try again.',
    '未找到该关系，请刷新后重试。': 'Relation not found. Please refresh and try again.',
    '未找到该疲劳规则，请刷新后重试。': 'Fatigue rule not found. Please refresh and try again.',
    '未找到该定向规则，请刷新后重试。': 'Targeting rule not found. Please refresh and try again.',
    '未找到所选展位，请刷新后重试。': 'Selected placement not found. Please refresh and try again.',
    '未找到所选投放单元，请刷新后重试。': 'Selected ad unit not found. Please refresh and try again.',
    '未找到所选素材，请刷新后重试。': 'Selected material not found. Please refresh and try again.',
    '未找到所选创意，请刷新后重试。': 'Selected creative not found. Please refresh and try again.',
    '未找到所选关系，请刷新后重试。': 'Selected relation not found. Please refresh and try again.',
    '未找到所选疲劳规则，请刷新后重试。': 'Selected fatigue rule not found. Please refresh and try again.',
    '未找到所选定向规则，请刷新后重试。': 'Selected targeting rule not found. Please refresh and try again.',
    '不能为空': ' is required',
    '必须为正整数': ' must be a positive integer',
    '必须大于0': ' must be greater than 0',
    '布尔值配置不合法': 'Invalid boolean configuration',
    '格式必须为 yyyy-MM-ddTHH:mm:ss': ' format must be yyyy-MM-ddTHH:mm:ss'
  };
  var EN_TEXT_AUTO_MAP = {
    '上限': 'upper limit',
    '事件': 'event',
    '停用': 'deactivate',
    '区间': 'interval',
    '大于': 'greater than',
    '存在': 'exist',
    '小于': 'less than',
    '属于': 'belong',
    '布尔': 'Boolean',
    '排序': 'sort',
    '数值': 'numerical value',
    '来源': 'source',
    '枚举': 'enumerate',
    '窗口': 'window',
    '轮播': 'Carousel',
    '返回': 'return',
    '顺序': 'order',
    '风险': 'Risk',
    '不存在': 'does not exist',
    '不属于': 'does not belong',
    '免打扰': 'Mute',
    '标签值': 'tag value',
    '目标值': 'target value',
    '，应付': ', payable',
    '人群名称': 'Group name',
    '人群定义': 'crowd definition',
    '人群描述': 'crowd description',
    '人群编码': 'crowd coding',
    '人群规则': 'crowd rules',
    '取值范围': 'Value range',
    '命中条件': 'hit condition',
    '大于等于': 'Greater than or equal to',
    '小于等于': 'less than or equal to',
    '排除条件': 'Exclusion criteria',
    '标签名称': 'Tag name',
    '标签定义': 'Tag definition',
    '标签描述': 'Tag description',
    '标签类型': 'Tag type',
    '标签编码': 'Tag encoding',
    '规则归属': 'Rule ownership',
    '规则数量': 'Number of rules',
    '人群管理台': 'crowd management desk',
    '必须是正整数': 'Must be a positive integer',
    '用户标签调试': 'User Tag Debugging',
    '人群配置已加载': 'Crowd configuration has been loaded',
    '人群定义保存成功': 'Crowd definition saved successfully',
    '人群规则保存成功': 'Crowd rules saved successfully',
    '人群配置加载失败': 'Crowd configuration loading failed',
    '标签定义保存成功': 'Tag definition saved successfully',
    '规则数据加载失败': 'Rule data loading failed',
    '用户标签数据已刷新': 'User tag data has been refreshed',
    '用户标签数据加载失败': 'User tag data loading failed',
    '正在加载标签、人群和规则数据...': 'Loading tags, crowds, and rules data...',
    '当前人群管理页无法读取管理员会话，请返回后台登录后重试。': 'The current crowd management page cannot read the administrator session. Please return to the background and log in and try again.',
  };
  Object.keys(EN_TEXT_AUTO_MAP).forEach(function (key) {
    if (!EN_TEXT_MAP[key]) {
      EN_TEXT_MAP[key] = EN_TEXT_AUTO_MAP[key];
    }
  });

  var EN_TEXT_AUTO_PARTS_MAP = {
    '可选': 'Optional',
    '名称': 'Name',
    '归属': 'Ownership',
    '标签': 'Tag',
    '编码': 'Code',
    '命中人群': 'Matched Audience',
    '值更新时间': 'Value Updated At',
    '如 HOME': 'Such as HOME',
    '如 城市层级': 'Such as city level',
    '如 高价值用户': 'Such as high value users',
    '请输入用户ID': 'Please enter user ID',
    '标签 Token': 'Tag Token',
    '用户标签与命中调试': 'User Tags and Match Debugging',
    '如 RULE_001': 'Such as RULE_001',
    '如 T1,T2,T3': 'Such as T1, T2, T3',
    '如 T1 / 5000': 'Such as T1/5000',
    '如 city_tier': 'Such as city_tier',
    '如 MANUAL / ETL': 'Such as MANUAL/ETL',
    '例如 5000 或 A,B,C': 'For example 5000 or A,B,C',
    '分群可按场景配置为草稿、发布或下线。': 'Segments can be configured as draft, published, or offline by scenario.',
    '如 880100068483692100': 'Such as 880100068483692100',
    '如 AUD_SEG_HIGH_VALUE': 'Such as AUD_SEG_HIGH_VALUE',
    '配置投放可用的人群标签，支持枚举、数值、布尔和文本。': 'Configure audience tags for delivery, supporting enum, numeric, boolean, and text.',
    '支持按标签快照计算人群命中，并用于投放单元与创意定向。': 'Supports computing audience matches by tag snapshot for ad-unit and creative targeting.',
    '规则支持 INCLUDE / EXCLUDE，作用于标签值匹配。': 'Rules support INCLUDE / EXCLUDE and apply to tag value matching.',
    '输入用户ID可查看标签快照、标签token以及命中的已发布人群。': 'Enter user ID to view tag snapshots, tag tokens, and matched published audiences.',
  };
  Object.keys(EN_TEXT_AUTO_PARTS_MAP).forEach(function (key) {
    if (!EN_TEXT_MAP[key]) {
      EN_TEXT_MAP[key] = EN_TEXT_AUTO_PARTS_MAP[key];
    }
  });

  var EN_TEXT_KEYS = Object.keys(EN_TEXT_MAP).sort(function (a, b) {
    return b.length - a.length;
  });

  function loadLang() {
    try {
      var raw = localStorage.getItem(LANG_KEY);
      return raw === 'en' ? 'en' : 'zh';
    } catch (error) {
      return 'zh';
    }
  }

  function getLang() {
    return loadLang();
  }

  function langText(zh, en) {
    return getLang() === 'en' ? en : zh;
  }

  function localizeText(text) {
    var normalized = String(text == null ? '' : text);
    if (getLang() !== 'en') {
      return normalized;
    }
    if (EN_TEXT_MAP[normalized]) {
      return EN_TEXT_MAP[normalized];
    }
    var localized = normalized;
    EN_TEXT_KEYS.forEach(function (key) {
      localized = localized.split(key).join(EN_TEXT_MAP[key]);
    });
    return localized;
  }

  function localizeHtml(html) {
    if (getLang() !== 'en') {
      return html;
    }
    return localizeText(html);
  }

  function setDocumentTitle(title) {
    if (typeof document === 'undefined') {
      return;
    }
    document.title = localizeText(title || document.title || '');
    if (document.documentElement) {
      document.documentElement.lang = getLang() === 'en' ? 'en' : 'zh-CN';
    }
  }

  function applyStaticPageI18n() {
    if (typeof document === 'undefined') {
      return;
    }
    setDocumentTitle(document.title || '');
    var nodes = document.querySelectorAll('.manager-placeholder-title, .manager-placeholder-tip, .manager-btn, a.manager-btn, [title], [aria-label], input[placeholder], textarea[placeholder]');
    Array.prototype.forEach.call(nodes, function (node) {
      if (!node) {
        return;
      }
      if (node.textContent) {
        node.textContent = localizeText(node.textContent);
      }
      if (typeof node.getAttribute === 'function' && typeof node.setAttribute === 'function') {
        ['title', 'aria-label', 'placeholder'].forEach(function (attr) {
          var value = node.getAttribute(attr);
          if (value) {
            node.setAttribute(attr, localizeText(value));
          }
        });
      }
    });
  }

  function loadSession() {
    var index;
    for (index = 0; index < SESSION_KEYS.length; index += 1) {
      try {
        var raw = localStorage.getItem(SESSION_KEYS[index]);
        if (!raw) {
          continue;
        }
        var parsed = parseJsonWithLargeIntegerSupport(raw);
        if (!parsed) {
          continue;
        }
        var adminId = normalizePositiveIdText(parsed.adminId);
        if (!adminId) {
          continue;
        }
        var accessToken = typeof parsed.accessToken === 'string' ? parsed.accessToken.trim() : '';
        if (!accessToken) {
          continue;
        }
        var tokenType = typeof parsed.tokenType === 'string' && parsed.tokenType.trim()
          ? parsed.tokenType.trim()
          : 'Bearer';
        parsed.adminId = adminId;
        parsed.accessToken = accessToken;
        parsed.tokenType = tokenType;
        return parsed;
      } catch (error) {
        // ignore invalid cached payload
      }
    }
    return null;
  }

  function pickDisplayName(session) {
    if (!session) {
      return '-';
    }
    return session.displayName || session.username || String(session.adminId || '-');
  }

  function buildAdminHeaders(session) {
    var headers = {
      'X-Admin-Id': String((session && session.adminId) || ''),
    };
    var accessToken = session && typeof session.accessToken === 'string'
      ? session.accessToken.trim()
      : '';
    if (accessToken) {
      var tokenType = session && typeof session.tokenType === 'string' && session.tokenType.trim()
        ? session.tokenType.trim()
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

  async function requestJson(url, options) {
    var response = await fetch(url, options);
    var payload = await safeJson(response);

    if (!response.ok) {
      throw new Error(readErrorMessage(payload, response.status, '请求失败'));
    }

    if (!payload || payload.success !== true) {
      throw new Error(readErrorMessage(payload, response.status, '请求返回失败'));
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
    if (payload && payload.error && payload.error.message) {
      return localizeText(String(payload.error.message));
    }
    if (status === 401) {
      return localizeText('后台鉴权失败，请重新登录');
    }
    if (status === 403) {
      return localizeText('权限不足，无法访问当前页面');
    }
    return localizeText(fallback);
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
    var signless;
    var boundary = '9007199254740991';
    if (!/^[-]?\d+$/.test(normalized)) {
      return false;
    }

    signless = normalized.charAt(0) === '-' ? normalized.slice(1) : normalized;
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
        // ignore and fallback to length compare
      }
    }

    if (signless.length !== boundary.length) {
      return signless.length > boundary.length;
    }
    return signless > boundary;
  }

  function normalizePositiveIdText(raw) {
    var normalized = String(raw == null ? '' : raw).trim();
    if (!/^\d+$/.test(normalized) || normalized === '0') {
      return '';
    }
    return normalized;
  }

  function escapeHtml(text) {
    return String(text == null ? '' : text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function formatDateTime(raw) {
    if (!raw) {
      return '-';
    }
    return String(raw).replace('T', ' ');
  }

  function formatBooleanText(value) {
    if (value === true) {
      return langText('是', 'Yes');
    }
    if (value === false) {
      return langText('否', 'No');
    }
    return '-';
  }

  function formatMoney(value) {
    if (value == null || value === '') {
      return '-';
    }
    if (typeof value === 'number' || typeof value === 'string') {
      return String(value);
    }
    if (typeof value === 'object' && value.amount != null) {
      var currencyCode = value.currencyCode || (value.currencyUnit && value.currencyUnit.code) || '';
      return String(value.amount) + (currencyCode ? ' ' + currencyCode : '');
    }
    return safeJsonStringify(value);
  }

  function formatValue(value) {
    if (value == null || value === '') {
      return '-';
    }
    if (typeof value === 'boolean') {
      return formatBooleanText(value);
    }
    if (typeof value === 'number' || typeof value === 'string') {
      return String(value);
    }
    if (Array.isArray(value)) {
      return value.length ? value.join(getLang() === 'en' ? ', ' : '、') : '-';
    }
    if (typeof value === 'object' && value.amount != null) {
      return formatMoney(value);
    }
    return safeJsonStringify(value);
  }

  function safeJsonStringify(value) {
    try {
      return JSON.stringify(value, null, 2);
    } catch (error) {
      return String(value == null ? '' : value);
    }
  }

  function normalizeEnumValue(value) {
    return String(value == null ? '' : value).trim().toUpperCase();
  }

  function formatEnumText(value, mapping) {
    var normalized = normalizeEnumValue(value);
    if (!normalized) {
      return '-';
    }
    return localizeText(mapping[normalized] || normalized);
  }

  function formatGenericStatus(status) {
    return formatEnumText(status, {
      ACTIVE: '正常',
      FROZEN: '冻结',
      CLOSED: '关闭',
      OVERDUE: '逾期',
      UNBOUND: '已解绑',
      PENDING: '待处理',
      ACCEPTED: '已通过',
      REJECTED: '已拒绝',
      CANCELED: '已取消',
      SENT: '已发送',
      RECALLED: '已撤回',
      PENDING_CLAIM: '待领取',
      CLAIMED: '已领取',
      EXPIRED: '已过期',
      REFUNDED: '已退回',
      CREATED: '已创建',
      INIT: '初始化',
      SUBMITTED: '已提交',
      ACCEPTED_CHANNEL: '已受理',
      ACCEPTED_INST: '已受理',
      SUCCESS: '成功',
      SUCCEEDED: '成功',
      PROCESSING: '处理中',
      SETTLING: '清算中',
      SETTLED: '已清算',
      FAILED: '失败',
      INSTALLED: '已安装',
      INACTIVE: '未活跃',
      UNINSTALLED: '已卸载',
      DRAFT: '草稿',
      PUBLISHED: '已发布',
      OFFLINE: '已下线',
      EDITING: '编辑中',
    });
  }

  function formatAccountStatus(status) {
    return formatEnumText(status, {
      ACTIVE: '正常',
      FROZEN: '冻结',
      CLOSED: '关闭',
      OVERDUE: '逾期',
    });
  }

  function formatKycLevel(level) {
    return formatEnumText(level, {
      L0: 'L0 未实名',
      L1: 'L1 基础实名',
      L2: 'L2 增强实名',
      L3: 'L3 高级实名',
    });
  }

  function formatRiskLevel(level) {
    return formatEnumText(level, {
      LOW: '低风险',
      MEDIUM: '中风险',
      HIGH: '高风险',
      SAFE: '安全',
    });
  }

  function formatTwoFactorMode(mode) {
    return formatEnumText(mode, {
      NONE: '未开启',
      SMS: '短信验证',
      APP: '验证器',
      BIOMETRIC: '生物识别',
    });
  }

  function formatConversationType(type) {
    return formatEnumText(type, {
      PRIVATE: '单聊',
      GROUP: '群聊',
      SYSTEM: '系统会话',
      SERVICE: '服务会话',
    });
  }

  function formatMessageType(type) {
    return formatEnumText(type, {
      TEXT: '文本',
      IMAGE: '图片',
      TRANSFER: '转账',
      RED_PACKET: '红包',
      SYSTEM: '系统',
    });
  }

  function formatMessageStatus(status) {
    return formatEnumText(status, {
      SENT: '已发送',
      RECALLED: '已撤回',
      FAILED: '发送失败',
      PROCESSING: '发送中',
    });
  }

  function formatPaymentMethod(method) {
    var normalized = normalizeEnumValue(method);
    if (!normalized) {
      return '-';
    }
    if (normalized === 'WALLET' || normalized === 'BALANCE' || normalized.indexOf('WALLET') >= 0 || normalized.indexOf('BALANCE') >= 0) {
      return langText('余额', 'Balance');
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
    return localizeText(normalized);
  }

  function formatRedPacketStatus(status) {
    return formatEnumText(status, {
      PENDING_CLAIM: '待领取',
      CREATED: '待领取',
      CLAIMED: '已领取',
      EXPIRED: '已过期',
      REFUNDED: '已退回',
    });
  }

  function formatContactRequestStatus(status) {
    return formatEnumText(status, {
      PENDING: '待处理',
      ACCEPTED: '已通过',
      REJECTED: '已拒绝',
      CANCELED: '已取消',
    });
  }

  function formatCardStatus(status) {
    return formatEnumText(status, {
      ACTIVE: '正常',
      UNBOUND: '已解绑',
      FROZEN: '冻结',
      EXPIRED: '已过期',
    });
  }

  function formatCardType(type) {
    return formatEnumText(type, {
      DEBIT: '储蓄卡',
      CREDIT: '信用卡',
      DEBIT_CARD: '储蓄卡',
      CREDIT_CARD: '信用卡',
    });
  }

  function formatCreditPayStatus(status) {
    return formatEnumText(status, {
      NORMAL: '正常',
      OVERDUE: '已逾期',
      SETTLED: '已结清',
    });
  }

  function formatToolType(type) {
    var normalized = normalizeEnumValue(type);
    if (!normalized) {
      return '-';
    }
    if (normalized.indexOf('WALLET') >= 0 || normalized.indexOf('BALANCE') >= 0) {
      return langText('余额', 'Balance');
    }
    if (normalized.indexOf('FUND') >= 0 || normalized.indexOf('AICASH') >= 0 || normalized.indexOf('AICASH') >= 0) {
      return langText('爱存', 'AiCash');
    }
    if (normalized.indexOf('CREDIT') >= 0) {
      return langText('爱花', 'AiCredit');
    }
    if (normalized.indexOf('LOAN') >= 0) {
      return langText('爱借', 'AiLoan');
    }
    if (normalized.indexOf('CARD') >= 0) {
      return langText('银行卡', 'Bank Card');
    }
    return localizeText(normalized);
  }

  function formatOperationStatus(status) {
    return formatEnumText(status, {
      INIT: '初始化',
      CREATED: '已创建',
      SUBMITTED: '已提交',
      ACCEPTED: '已受理',
      PROCESSING: '处理中',
      SUCCESS: '成功',
      SUCCEEDED: '成功',
      SETTLING: '清算中',
      SETTLED: '已清算',
      FAILED: '失败',
      CANCELED: '已取消',
    });
  }

  function formatFeeBearer(bearer) {
    return formatEnumText(bearer, {
      PAYER: '付款方',
      PAYEE: '收款方',
      PLATFORM: '平台',
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
    if (normalized.indexOf('REPAY') >= 0) {
      return langText('还款', 'Repayment');
    }
    if (normalized === 'PAY' || normalized === 'PAYMENT' || (normalized.indexOf('PAY') >= 0 && normalized.indexOf('REPAY') < 0)) {
      return langText('支付', 'Payment');
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
    return localizeText(normalized);
  }

  function buildStatusBadge(status, displayText) {
    var code = String(status || '-').toLowerCase();
    var text = displayText == null ? formatGenericStatus(status) : displayText;
    return '<span class="manager-coupon-status status-' + escapeHtml(code) + '">' + escapeHtml(text || '-') + '</span>';
  }

  function serializeQuery(params) {
    return Object.keys(params || {})
      .filter(function (key) {
        var value = params[key];
        return value != null && String(value).trim() !== '';
      })
      .map(function (key) {
        return encodeURIComponent(key) + '=' + encodeURIComponent(String(params[key]).trim());
      })
      .join('&');
  }

  function renderDetailGrid(items) {
    return '<div class="manager-coupon-detail-grid">' + (items || []).map(function (item) {
      return '<div class="manager-coupon-detail-item">' +
        '<span class="manager-coupon-detail-key">' + escapeHtml(item.label || '-') + '</span>' +
        '<span class="manager-coupon-detail-value">' + escapeHtml(formatValue(item.value)) + '</span>' +
        '</div>';
    }).join('') + '</div>';
  }

  function renderJsonBlock(value) {
    return '<pre class="manager-code-block">' + escapeHtml(safeJsonStringify(value)) + '</pre>';
  }

  function renderAuthRequired(title, description) {
    var html = '';
    html += '<section class="manager-panel">';
    html += '<div class="manager-placeholder">';
    html += '<h2 class="manager-placeholder-title">' + escapeHtml(localizeText(title || '请先登录管理后台')) + '</h2>';
    html += '<p class="manager-placeholder-tip">' + escapeHtml(localizeText(description || '当前页面无法读取管理员会话，请返回后台登录后重试。')) + '</p>';
    html += '<div class="admin-embedded-filter-actions"><a class="manager-btn primary" href="/manager/login" target="_top">' + escapeHtml(localizeText('返回登录')) + '</a></div>';
    html += '</div>';
    html += '</section>';
    return html;
  }

  function renderLoading(title, description) {
    var html = '';
    html += '<section class="manager-panel">';
    html += '<div class="manager-placeholder">';
    html += '<h2 class="manager-placeholder-title">' + escapeHtml(localizeText(title || '正在加载')) + '</h2>';
    html += '<p class="manager-placeholder-tip">' + escapeHtml(localizeText(description || '正在加载数据...')) + '</p>';
    html += '</div>';
    html += '</section>';
    return html;
  }

  function resolveSelectedPath(defaultPath) {
    var params = new URLSearchParams(window.location.search || '');
    var path = params.get('path');
    if (!path) {
      return defaultPath;
    }
    return String(path || '').trim() || defaultPath;
  }

  function focusSection(id) {
    window.setTimeout(function () {
      var element = document.getElementById(id);
      if (!element) {
        return;
      }
      if (typeof element.focus === 'function') {
        try {
          element.focus({ preventScroll: true });
        } catch (error) {
          element.focus();
        }
      }
      if (typeof element.scrollIntoView === 'function') {
        element.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 24);
  }

  function createFrameHeightNotifier(frameId, minHeight) {
    var minimum = Number(minHeight);
    if (!Number.isFinite(minimum) || minimum <= 0) {
      minimum = 920;
    }

    function notify() {
      if (window.parent === window || !window.parent || typeof window.parent.postMessage !== 'function') {
        return;
      }
      var bodyHeight = document.body ? document.body.scrollHeight : 0;
      var documentHeight = document.documentElement ? document.documentElement.scrollHeight : 0;
      var height = Math.max(bodyHeight, documentHeight, minimum);
      window.parent.postMessage({
        type: 'openaipay-admin-frame-height',
        frameId: frameId,
        height: height,
      }, window.location.origin);
    }

    function schedule() {
      if (typeof window.requestAnimationFrame === 'function') {
        window.requestAnimationFrame(notify);
        return;
      }
      window.setTimeout(notify, 16);
    }

    return {
      notify: notify,
      schedule: schedule,
    };
  }

  global.OpenAiPayAdminEmbedded = {
    loadSession: loadSession,
    pickDisplayName: pickDisplayName,
    buildAdminHeaders: buildAdminHeaders,
    withJson: withJson,
    requestJson: requestJson,
    escapeHtml: escapeHtml,
    formatDateTime: formatDateTime,
    formatBooleanText: formatBooleanText,
    formatMoney: formatMoney,
    formatValue: formatValue,
    safeJsonStringify: safeJsonStringify,
    formatAccountStatus: formatAccountStatus,
    formatBusinessSceneCode: formatBusinessSceneCode,
    formatCardStatus: formatCardStatus,
    formatCardType: formatCardType,
    formatContactRequestStatus: formatContactRequestStatus,
    formatConversationType: formatConversationType,
    formatCreditPayStatus: formatCreditPayStatus,
    formatOperationStatus: formatOperationStatus,
    formatFeeBearer: formatFeeBearer,
    formatGenericStatus: formatGenericStatus,
    formatKycLevel: formatKycLevel,
    formatMessageStatus: formatMessageStatus,
    formatMessageType: formatMessageType,
    formatPaymentMethod: formatPaymentMethod,
    formatRedPacketStatus: formatRedPacketStatus,
    formatRiskLevel: formatRiskLevel,
    formatToolType: formatToolType,
    formatTwoFactorMode: formatTwoFactorMode,
    buildStatusBadge: buildStatusBadge,
    serializeQuery: serializeQuery,
    renderDetailGrid: renderDetailGrid,
    renderJsonBlock: renderJsonBlock,
    renderAuthRequired: renderAuthRequired,
    renderLoading: renderLoading,
    resolveSelectedPath: resolveSelectedPath,
    focusSection: focusSection,
    createFrameHeightNotifier: createFrameHeightNotifier,
    getLang: getLang,
    langText: langText,
    localizeText: localizeText,
    localizeHtml: localizeHtml,
    setDocumentTitle: setDocumentTitle,
    applyStaticPageI18n: applyStaticPageI18n,
  };

  applyStaticPageI18n();
})(window);
