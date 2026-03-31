(function () {
  var common = window.OpenAiPayAdminEmbedded;
  var root = document.getElementById('fundConsoleApp');
  var FRAME_ID = 'managerFundFrame';
  var FUND_ROOT_PATH = '/admin/fund';
  var FUND_WALLET_PATH = '/admin/fund/wallet';
  var FUND_PRODUCT_PATH = '/admin/fund/funds';
  var FUND_CREDIT_PATH = '/admin/fund/credit';
  var FUND_LOAN_PATH = '/admin/fund/loan';
  var FUND_BANKCARD_PATH = '/admin/fund/bankcards';
  var FUND_CASHIER_PATH = '/admin/fund/cashier';
  var FRAME_NOTIFIER = common.createFrameHeightNotifier(FRAME_ID, 920);
  var TAB_ITEMS = [
    { path: FUND_WALLET_PATH, label: '钱包账户', section: 'walletAccounts' },
    { path: FUND_PRODUCT_PATH, label: '基金账户', section: 'fundAccounts' },
    { path: FUND_CREDIT_PATH, label: '授信账户', section: 'creditAccounts' },
    { path: FUND_LOAN_PATH, label: '借贷账户', section: 'loanAccounts' },
    { path: FUND_BANKCARD_PATH, label: '银行卡', section: 'bankCards' },
    { path: FUND_CASHIER_PATH, label: '收银台', section: 'cashier' },
  ];
  var FIXED_ACTIVE_PATH = normalizeFundPath(
    window.OPENAIPAY_FUND_PATH ||
    (document.body && document.body.getAttribute('data-fund-path')) ||
    common.resolveSelectedPath(FUND_ROOT_PATH)
  );

  var state = {
    session: common.loadSession(),
    loading: false,
    initialized: false,
    notice: '',
    activePath: FIXED_ACTIVE_PATH,
    overview: {
      walletAccountCount: 0,
      fundAccountCount: 0,
      creditAccountCount: 0,
      loanAccountCount: 0,
      bankCardCount: 0,
    },
    filters: createDefaultFilters(),
    pagination: createDefaultPagination(),
    data: {
      walletAccounts: [],
      fundAccounts: [],
      creditAccounts: [],
      loanAccounts: [],
      bankCards: [],
      cashierView: null,
      pricingPreview: null,
    },
  };

  if (!root || !common) {
    return;
  }

  root.addEventListener('click', onClick);
  window.addEventListener('resize', FRAME_NOTIFIER.schedule);
  start();

  async function start() {
    if (!state.session || !state.session.adminId) {
      render();
      return;
    }
    await loadPage(true);
  }

  function createDefaultFilters() {
    return {
      walletAccounts: {
        userId: '',
        accountStatus: '',
        pageNo: '1',
        pageSize: '20',
      },
      fundAccounts: {
        userId: '',
        fundCode: '',
        accountStatus: '',
        pageNo: '1',
        pageSize: '20',
      },
      creditAccounts: {
        userId: '',
        accountStatus: '',
        payStatus: '',
        pageNo: '1',
        pageSize: '20',
      },
      loanAccounts: {
        userId: '',
        accountStatus: '',
        payStatus: '',
        pageNo: '1',
        pageSize: '20',
      },
      bankCards: {
        userId: '',
        cardStatus: '',
        bankCode: '',
        pageNo: '1',
        pageSize: '20',
      },
      cashier: {
        userId: '',
        sceneCode: '',
        paymentMethod: '',
        amount: '',
        currencyCode: 'CNY',
      },
    };
  }

  function createDefaultPagination() {
    return {
      walletAccounts: {
        hasNextPage: false,
      },
      fundAccounts: {
        hasNextPage: false,
      },
      creditAccounts: {
        hasNextPage: false,
      },
      loanAccounts: {
        hasNextPage: false,
      },
      bankCards: {
        hasNextPage: false,
      },
    };
  }

  function normalizeFundPath(path) {
    var normalized = String(path || '').trim();
    var matched = TAB_ITEMS.find(function (item) {
      return item.path === normalized;
    });
    return matched ? matched.path : FUND_WALLET_PATH;
  }

  function getActiveSection() {
    var matched = TAB_ITEMS.find(function (item) {
      return item.path === state.activePath;
    });
    return matched ? matched.section : 'walletAccounts';
  }

  async function loadPage(showLoadedNotice) {
    state.loading = true;
    render();
    var results = await Promise.allSettled([
      loadOverview(),
      loadActiveSectionData(),
    ]);
    state.loading = false;
    state.initialized = true;
    state.notice = buildNotice(results, showLoadedNotice ? '资金中心已加载' : '资金中心已刷新');
    render();
  }

  function buildNotice(results, successText) {
    var failures = (results || []).filter(function (item) {
      return item.status === 'rejected';
    });
    if (!failures.length) {
      return successText;
    }
    var firstError = failures[0].reason;
    return firstError && firstError.message ? firstError.message : '部分数据加载失败';
  }

  async function request(path, query) {
    var qs = common.serializeQuery(query);
    return common.requestJson(path + (qs ? '?' + qs : ''), {
      method: 'GET',
      headers: common.buildAdminHeaders(state.session),
    });
  }

  async function loadOverview() {
    state.overview = await request('/api/admin/fund/overview');
  }

  async function loadActiveSectionData() {
    var section = getActiveSection();
    if (section === 'walletAccounts') {
      await loadWalletAccounts();
      return;
    }
    if (section === 'fundAccounts') {
      await loadFundAccounts();
      return;
    }
    if (section === 'creditAccounts') {
      await loadCreditAccounts();
      return;
    }
    if (section === 'loanAccounts') {
      await loadLoanAccounts();
      return;
    }
    if (section === 'bankCards') {
      await loadBankCards();
      return;
    }
  }

  async function loadWalletAccounts() {
    var pageSize = toPositiveIntOrDefault(state.filters.walletAccounts.pageSize, 20);
    var rows = await request('/api/admin/fund/wallet-accounts', state.filters.walletAccounts);
    state.data.walletAccounts = Array.isArray(rows) ? rows : [];
    state.pagination.walletAccounts.hasNextPage = state.data.walletAccounts.length >= pageSize;
  }

  async function loadFundAccounts() {
    var pageSize = toPositiveIntOrDefault(state.filters.fundAccounts.pageSize, 20);
    var rows = await request('/api/admin/fund/fund-accounts', state.filters.fundAccounts);
    state.data.fundAccounts = Array.isArray(rows) ? rows : [];
    state.pagination.fundAccounts.hasNextPage = state.data.fundAccounts.length >= pageSize;
  }

  async function loadCreditAccounts() {
    var pageSize = toPositiveIntOrDefault(state.filters.creditAccounts.pageSize, 20);
    var rows = await request('/api/admin/fund/credit-accounts', state.filters.creditAccounts);
    state.data.creditAccounts = Array.isArray(rows) ? rows : [];
    state.pagination.creditAccounts.hasNextPage = state.data.creditAccounts.length >= pageSize;
  }

  async function loadLoanAccounts() {
    var pageSize = toPositiveIntOrDefault(state.filters.loanAccounts.pageSize, 20);
    var rows = await request('/api/admin/fund/loan-accounts', state.filters.loanAccounts);
    state.data.loanAccounts = Array.isArray(rows) ? rows : [];
    state.pagination.loanAccounts.hasNextPage = state.data.loanAccounts.length >= pageSize;
  }

  async function loadBankCards() {
    var pageSize = toPositiveIntOrDefault(state.filters.bankCards.pageSize, 20);
    var rows = await request('/api/admin/fund/bank-cards', state.filters.bankCards);
    state.data.bankCards = Array.isArray(rows) ? rows : [];
    state.pagination.bankCards.hasNextPage = state.data.bankCards.length >= pageSize;
  }

  async function loadCashierView() {
    var filters = state.filters.cashier;
    requireValue(filters.userId, '用户ID');
    state.data.cashierView = await request('/api/admin/fund/cashier/view', {
      userId: filters.userId,
      sceneCode: filters.sceneCode,
    });
  }

  async function loadPricingPreview() {
    var filters = state.filters.cashier;
    requireValue(filters.userId, '用户ID');
    requireValue(filters.amount, '金额');
    state.data.pricingPreview = await request('/api/admin/fund/cashier/pricing-preview', {
      userId: filters.userId,
      sceneCode: filters.sceneCode,
      paymentMethod: filters.paymentMethod,
      amount: filters.amount,
      currencyCode: filters.currencyCode || 'CNY',
    });
  }

  function requireValue(value, label) {
    if (!String(value || '').trim()) {
      throw new Error(label + '不能为空');
    }
  }

  function onClick(event) {
    var actionElement = event.target.closest('[data-action]');
    var action;
    var section;
    if (!actionElement) {
      return;
    }
    action = actionElement.getAttribute('data-action');
    if (!action) {
      return;
    }

    if (action === 'refresh-all') {
      loadPage(false);
      return;
    }

    if (action === 'query-section') {
      section = actionElement.getAttribute('data-section');
      updateSectionFilters(section);
      querySection(section);
      return;
    }

    if (action === 'reset-section') {
      section = actionElement.getAttribute('data-section');
      resetSection(section);
      return;
    }

    if (action === 'query-section-prev-page') {
      section = actionElement.getAttribute('data-section');
      querySectionByOffset(section, -1);
      return;
    }

    if (action === 'query-section-next-page') {
      section = actionElement.getAttribute('data-section');
      querySectionByOffset(section, 1);
      return;
    }

    if (action === 'query-cashier-view') {
      updateCashierFilters();
      queryCashier('view');
      return;
    }

    if (action === 'query-cashier-preview') {
      updateCashierFilters();
      queryCashier('preview');
      return;
    }

    if (action === 'reset-cashier') {
      state.filters.cashier = createDefaultFilters().cashier;
      state.data.cashierView = null;
      state.data.pricingPreview = null;
      state.notice = '收银台筛选已重置';
      render();
    }
  }

  function updateSectionFilters(section) {
    if (section === 'walletAccounts') {
      state.filters.walletAccounts.userId = readInputValue('fundWalletUserId');
      state.filters.walletAccounts.accountStatus = readInputValue('fundWalletStatus');
      state.filters.walletAccounts.pageNo = readInputValue('fundWalletPageNo') || '1';
      state.filters.walletAccounts.pageSize = readInputValue('fundWalletPageSize') || '20';
      return;
    }
    if (section === 'fundAccounts') {
      state.filters.fundAccounts.userId = readInputValue('fundProductUserId');
      state.filters.fundAccounts.fundCode = readInputValue('fundProductCode');
      state.filters.fundAccounts.accountStatus = readInputValue('fundProductStatus');
      state.filters.fundAccounts.pageNo = readInputValue('fundProductPageNo') || '1';
      state.filters.fundAccounts.pageSize = readInputValue('fundProductPageSize') || '20';
      return;
    }
    if (section === 'creditAccounts') {
      state.filters.creditAccounts.userId = readInputValue('fundCreditUserId');
      state.filters.creditAccounts.accountStatus = readInputValue('fundCreditStatus');
      state.filters.creditAccounts.payStatus = readInputValue('fundCreditPayStatus');
      state.filters.creditAccounts.pageNo = readInputValue('fundCreditPageNo') || '1';
      state.filters.creditAccounts.pageSize = readInputValue('fundCreditPageSize') || '20';
      return;
    }
    if (section === 'loanAccounts') {
      state.filters.loanAccounts.userId = readInputValue('fundLoanUserId');
      state.filters.loanAccounts.accountStatus = readInputValue('fundLoanStatus');
      state.filters.loanAccounts.payStatus = readInputValue('fundLoanPayStatus');
      state.filters.loanAccounts.pageNo = readInputValue('fundLoanPageNo') || '1';
      state.filters.loanAccounts.pageSize = readInputValue('fundLoanPageSize') || '20';
      return;
    }
    if (section === 'bankCards') {
      state.filters.bankCards.userId = readInputValue('fundBankCardUserId');
      state.filters.bankCards.cardStatus = readInputValue('fundBankCardStatus');
      state.filters.bankCards.bankCode = readInputValue('fundBankCardCode');
      state.filters.bankCards.pageNo = readInputValue('fundBankCardPageNo') || '1';
      state.filters.bankCards.pageSize = readInputValue('fundBankCardPageSize') || '20';
    }
  }

  function updateCashierFilters() {
    state.filters.cashier.userId = readInputValue('fundCashierUserId');
    state.filters.cashier.sceneCode = readInputValue('fundCashierSceneCode');
    state.filters.cashier.paymentMethod = readInputValue('fundCashierPaymentMethod');
    state.filters.cashier.amount = readInputValue('fundCashierAmount');
    state.filters.cashier.currencyCode = readInputValue('fundCashierCurrencyCode') || 'CNY';
  }

  async function querySection(section) {
    state.notice = '正在查询...';
    normalizeSectionPagination(section);
    render();
    try {
      if (section === 'walletAccounts') {
        await loadWalletAccounts();
      } else if (section === 'fundAccounts') {
        await loadFundAccounts();
      } else if (section === 'creditAccounts') {
        await loadCreditAccounts();
      } else if (section === 'loanAccounts') {
        await loadLoanAccounts();
      } else if (section === 'bankCards') {
        await loadBankCards();
      }
      state.notice = '查询完成';
    } catch (error) {
      if (state.pagination[section]) {
        state.pagination[section].hasNextPage = false;
      }
      state.notice = error && error.message ? error.message : '查询失败';
    }
    render();
  }

  function querySectionByOffset(section, offset) {
    if (!section || !state.filters[section]) {
      return;
    }
    updateSectionFilters(section);
    normalizeSectionPagination(section);
    var pageNo = toPositiveIntOrDefault(state.filters[section].pageNo, 1);
    var targetPageNo = pageNo + Number(offset || 0);
    if (targetPageNo <= 0) {
      targetPageNo = 1;
    }
    if (offset > 0 && !(state.pagination[section] && state.pagination[section].hasNextPage)) {
      return;
    }
    if (targetPageNo === pageNo) {
      return;
    }
    state.filters[section].pageNo = String(targetPageNo);
    querySection(section);
  }

  async function queryCashier(kind) {
    state.notice = kind === 'view' ? '正在加载收银台视图...' : '正在试算价格...';
    render();
    try {
      if (kind === 'view') {
        await loadCashierView();
        state.notice = '收银台视图已加载';
      } else {
        await loadPricingPreview();
        state.notice = '收银台试算已完成';
      }
    } catch (error) {
      state.notice = error && error.message ? error.message : '收银台请求失败';
    }
    render();
  }

  function resetSection(section) {
    var defaults = createDefaultFilters();
    var paginationDefaults = createDefaultPagination();
    if (section === 'walletAccounts') {
      state.filters.walletAccounts = defaults.walletAccounts;
      state.pagination.walletAccounts = paginationDefaults.walletAccounts;
    } else if (section === 'fundAccounts') {
      state.filters.fundAccounts = defaults.fundAccounts;
      state.pagination.fundAccounts = paginationDefaults.fundAccounts;
    } else if (section === 'creditAccounts') {
      state.filters.creditAccounts = defaults.creditAccounts;
      state.pagination.creditAccounts = paginationDefaults.creditAccounts;
    } else if (section === 'loanAccounts') {
      state.filters.loanAccounts = defaults.loanAccounts;
      state.pagination.loanAccounts = paginationDefaults.loanAccounts;
    } else if (section === 'bankCards') {
      state.filters.bankCards = defaults.bankCards;
      state.pagination.bankCards = paginationDefaults.bankCards;
    }
    render();
    querySection(section);
  }

  function readInputValue(id) {
    var element = document.getElementById(id);
    if (!element || typeof element.value !== 'string') {
      return '';
    }
    return element.value.trim();
  }

  function toPositiveIntOrDefault(raw, fallback) {
    var value = Number(String(raw == null ? '' : raw).trim());
    if (!Number.isFinite(value) || value <= 0) {
      return fallback;
    }
    return Math.trunc(value);
  }

  function normalizeSectionPagination(section) {
    var filter = state.filters[section];
    if (!filter) {
      return;
    }
    filter.pageNo = String(toPositiveIntOrDefault(filter.pageNo, 1));
    filter.pageSize = String(Math.min(toPositiveIntOrDefault(filter.pageSize, 20), 200));
  }

  function render() {
    common.setDocumentTitle('资金中心');
    if (!state.session || !state.session.adminId) {
      root.innerHTML = common.localizeHtml(common.renderAuthRequired('请先登录管理后台', '当前资金中心无法读取管理员会话，请返回后台登录后重试。'));
      FRAME_NOTIFIER.schedule();
      return;
    }

    if (state.loading && !state.initialized) {
      root.innerHTML = common.localizeHtml(common.renderLoading('资金中心', '正在加载当前标签页数据...'));
      FRAME_NOTIFIER.schedule();
      return;
    }

    root.innerHTML = common.localizeHtml(renderPage());
    FRAME_NOTIFIER.schedule();
  }

  function renderPage() {
    var html = '';
    html += '<div class="admin-embedded-toolbar">';
    html += '<div>';
    html += '<h1 class="manager-title">资金中心</h1>';
    html += '<p class="manager-subtext">顶部切换独立 tab 页面，每个页面只承载一类资金能力。</p>';
    html += renderTabBar();
    html += '</div>';
    html += '<div class="admin-embedded-toolbar-actions">';
    html += '<span class="admin-embedded-toolbar-note">当前管理员：' + common.escapeHtml(common.pickDisplayName(state.session)) + '</span>';
    html += '<button class="manager-btn" type="button" data-action="refresh-all">刷新当前页</button>';
    html += '</div>';
    html += '</div>';

    html += renderOverview();
    if (state.notice) {
      html += '<section class="manager-panel"><span class="admin-embedded-note">' + common.escapeHtml(state.notice) + '</span></section>';
    }
    html += renderActiveSection();
    return html;
  }

  function renderTabBar() {
    return '<div class="admin-embedded-tabbar">' + TAB_ITEMS.map(function (item) {
      var activeClass = item.path === state.activePath ? ' active' : '';
      return '<a class="admin-embedded-tab' + activeClass + '" href="' + common.escapeHtml(buildManagerUrl(item.path)) + '" target="_top">' + common.escapeHtml(item.label) + '</a>';
    }).join('') + '</div>';
  }

  function buildManagerUrl(path) {
    var normalized = String(path || '').trim();
    if (!normalized || normalized === '/admin' || normalized === '/admin/dashboard') {
      return '/manager/dashboard';
    }
    if (normalized.indexOf('/admin/') === 0) {
      return '/manager/' + normalized.slice('/admin/'.length);
    }
    return '/manager/' + normalized.replace(/^\/+/, '');
  }

  function renderOverview() {
    var cards = [
      { label: '钱包账户', value: state.overview.walletAccountCount },
      { label: '基金账户', value: state.overview.fundAccountCount },
      { label: '授信账户', value: state.overview.creditAccountCount },
      { label: '借贷账户', value: state.overview.loanAccountCount },
      { label: '银行卡', value: state.overview.bankCardCount },
    ];
    return '<section class="manager-panel admin-embedded-summary-panel">' +
      '<div class="admin-embedded-summary-strip">' +
      '<div class="admin-embedded-summary-title">资源统计</div>' +
      '<div class="admin-embedded-summary-list">' +
      cards.map(function (card) {
        return '<div class="admin-embedded-summary-item"><span class="admin-embedded-summary-label">' + common.escapeHtml(card.label) + '</span><span class="admin-embedded-summary-value">' + common.escapeHtml(card.value) + '</span></div>';
      }).join('') +
      '</div></div></section>';
  }

  function renderActiveSection() {
    var section = getActiveSection();
    if (section === 'fundAccounts') {
      return renderFundProductSection();
    }
    if (section === 'creditAccounts') {
      return renderCreditSection();
    }
    if (section === 'loanAccounts') {
      return renderLoanSection();
    }
    if (section === 'bankCards') {
      return renderBankCardSection();
    }
    if (section === 'cashier') {
      return renderCashierSection();
    }
    return renderWalletSection();
  }

  function renderWalletSection() {
    var rows = state.data.walletAccounts || [];
    var filters = state.filters.walletAccounts;
    var html = '';
    html += '<section id="fundWalletSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('钱包账户', '独立页面，仅展示钱包账户查询与结果。', rows.length);
    html += renderFilterGrid([
      renderField('用户ID', '<input id="fundWalletUserId" class="form-input" value="' + common.escapeHtml(filters.userId) + '" placeholder="userId" />'),
      renderField('账户状态', '<input id="fundWalletStatus" class="form-input" value="' + common.escapeHtml(filters.accountStatus) + '" placeholder="如 ACTIVE（正常）/ FROZEN（冻结）" />'),
      renderField('页码', '<input id="fundWalletPageNo" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />'),
      renderField('每页条数', '<input id="fundWalletPageSize" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />'),
    ], 'walletAccounts');
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>用户</th><th>币种</th><th>可用余额</th><th>冻结/预留</th><th>状态</th><th>更新时间</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="6" class="manager-coupon-empty-row">暂无钱包账户</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td>' + common.escapeHtml(buildUserText(item.userDisplayName, item.aipayUid, item.userId)) + '</td>' +
          '<td>' + common.escapeHtml(item.currencyCode || '-') + '</td>' +
          '<td>' + common.escapeHtml(common.formatMoney(item.availableBalance)) + '</td>' +
          '<td>' + common.escapeHtml(common.formatMoney(item.reservedBalance)) + '</td>' +
          '<td>' + common.buildStatusBadge(item.accountStatus || '-', common.formatAccountStatus(item.accountStatus)) + '</td>' +
          '<td>' + common.escapeHtml(common.formatDateTime(item.updatedAt || item.createdAt)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div></section>';
    return html;
  }

  function renderFundProductSection() {
    var rows = state.data.fundAccounts || [];
    var filters = state.filters.fundAccounts;
    var html = '';
    html += '<section id="fundProductSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('基金账户', '独立页面，仅展示基金账户、份额、收益与 NAV。', rows.length);
    html += renderFilterGrid([
      renderField('用户ID', '<input id="fundProductUserId" class="form-input" value="' + common.escapeHtml(filters.userId) + '" placeholder="userId" />'),
      renderField('基金代码', '<input id="fundProductCode" class="form-input" value="' + common.escapeHtml(filters.fundCode) + '" placeholder="fundCode" />'),
      renderField('账户状态', '<input id="fundProductStatus" class="form-input" value="' + common.escapeHtml(filters.accountStatus) + '" placeholder="如 ACTIVE（正常）/ FROZEN（冻结）" />'),
      renderField('页码', '<input id="fundProductPageNo" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />'),
      renderField('每页条数', '<input id="fundProductPageSize" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />'),
    ], 'fundAccounts');
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>用户</th><th>基金</th><th>可用份额</th><th>冻结/待处理</th><th>收益</th><th>状态</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="6" class="manager-coupon-empty-row">暂无基金账户</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td>' + common.escapeHtml(buildUserText(item.userDisplayName, item.aipayUid, item.userId)) + '</td>' +
          '<td>' + common.escapeHtml(item.fundCode || '-') + ' / ' + common.escapeHtml(item.currencyCode || '-') + '</td>' +
          '<td>' + common.escapeHtml(common.formatValue(item.availableShare)) + '</td>' +
          '<td>' + common.escapeHtml(common.formatValue(item.frozenShare)) + '<br /><span class="admin-embedded-muted">申购 ' + common.escapeHtml(common.formatValue(item.pendingSubscribeAmount)) + ' / 赎回 ' + common.escapeHtml(common.formatValue(item.pendingRedeemShare)) + '</span></td>' +
          '<td>' + common.escapeHtml(common.formatValue(item.accumulatedIncome)) + '<br /><span class="admin-embedded-muted">昨日 ' + common.escapeHtml(common.formatValue(item.yesterdayIncome)) + ' / NAV ' + common.escapeHtml(common.formatValue(item.latestNav)) + '</span></td>' +
          '<td>' + common.buildStatusBadge(item.accountStatus || '-', common.formatAccountStatus(item.accountStatus)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div></section>';
    return html;
  }

  function renderCreditSection() {
    return renderCreditLikeSection(
      'fundCreditSection',
      '授信账户',
      '独立页面，仅展示授信额度账户。',
      state.data.creditAccounts || [],
      state.filters.creditAccounts,
      {
        userId: 'fundCreditUserId',
        status: 'fundCreditStatus',
        payStatus: 'fundCreditPayStatus',
        pageNo: 'fundCreditPageNo',
        pageSize: 'fundCreditPageSize',
      },
      'creditAccounts'
    );
  }

  function renderLoanSection() {
    return renderCreditLikeSection(
      'fundLoanSection',
      '借贷账户',
      '独立页面，仅展示借贷账户与还款状态。',
      state.data.loanAccounts || [],
      state.filters.loanAccounts,
      {
        userId: 'fundLoanUserId',
        status: 'fundLoanStatus',
        payStatus: 'fundLoanPayStatus',
        pageNo: 'fundLoanPageNo',
        pageSize: 'fundLoanPageSize',
      },
      'loanAccounts'
    );
  }

  function renderCreditLikeSection(sectionId, title, description, rows, filters, inputIds, actionSection) {
    var html = '';
    html += '<section id="' + sectionId + '" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead(title, description, rows.length);
    html += renderFilterGrid([
      renderField('用户ID', '<input id="' + inputIds.userId + '" class="form-input" value="' + common.escapeHtml(filters.userId) + '" placeholder="userId" />'),
      renderField('账户状态', '<input id="' + inputIds.status + '" class="form-input" value="' + common.escapeHtml(filters.accountStatus) + '" placeholder="如 ACTIVE（正常）/ OVERDUE（逾期）" />'),
      renderField('还款状态', '<input id="' + inputIds.payStatus + '" class="form-input" value="' + common.escapeHtml(filters.payStatus) + '" placeholder="如 NORMAL（正常）/ OVERDUE（逾期）" />'),
      renderField('页码', '<input id="' + inputIds.pageNo + '" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />'),
      renderField('每页条数', '<input id="' + inputIds.pageSize + '" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />'),
    ], actionSection);
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>用户</th><th>账户号</th><th>额度</th><th>本金/逾期</th><th>利息/罚息</th><th>状态</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="6" class="manager-coupon-empty-row">暂无' + common.escapeHtml(title) + '</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td>' + common.escapeHtml(buildUserText(item.userDisplayName, item.aipayUid, item.userId)) + '</td>' +
          '<td><span class="admin-embedded-mono">' + common.escapeHtml(item.accountNo) + '</span><br /><span class="admin-embedded-muted">' + common.escapeHtml(item.productCode || '-') + '</span></td>' +
          '<td>' + common.escapeHtml(common.formatMoney(item.totalLimit)) + '<br /><span class="admin-embedded-muted">可用 ' + common.escapeHtml(common.formatMoney(item.availableLimit)) + '</span></td>' +
          '<td>' + common.escapeHtml(common.formatMoney(item.principalBalance)) + '<br /><span class="admin-embedded-muted">逾期 ' + common.escapeHtml(common.formatMoney(item.overduePrincipalBalance)) + '</span></td>' +
          '<td>' + common.escapeHtml(common.formatMoney(item.interestBalance)) + '<br /><span class="admin-embedded-muted">罚息 ' + common.escapeHtml(common.formatMoney(item.fineBalance)) + '</span></td>' +
          '<td>' + common.buildStatusBadge(item.accountStatus || '-', common.formatAccountStatus(item.accountStatus)) + '<br /><span class="admin-embedded-muted">' + common.escapeHtml(common.formatCreditPayStatus(item.payStatus)) + ' / 还款日 ' + common.escapeHtml(item.repayDayOfMonth == null ? '-' : item.repayDayOfMonth) + '</span></td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div></section>';
    return html;
  }

  function renderBankCardSection() {
    var rows = state.data.bankCards || [];
    var filters = state.filters.bankCards;
    var html = '';
    html += '<section id="fundBankCardSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('银行卡', '独立页面，仅展示绑卡与限额配置。', rows.length);
    html += renderFilterGrid([
      renderField('用户ID', '<input id="fundBankCardUserId" class="form-input" value="' + common.escapeHtml(filters.userId) + '" placeholder="userId" />'),
      renderField('卡状态', '<input id="fundBankCardStatus" class="form-input" value="' + common.escapeHtml(filters.cardStatus) + '" placeholder="如 ACTIVE（正常）/ UNBOUND（已解绑）" />'),
      renderField('银行编码', '<input id="fundBankCardCode" class="form-input" value="' + common.escapeHtml(filters.bankCode) + '" placeholder="bankCode" />'),
      renderField('页码', '<input id="fundBankCardPageNo" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />'),
      renderField('每页条数', '<input id="fundBankCardPageSize" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />'),
    ], 'bankCards');
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>用户</th><th>卡信息</th><th>银行</th><th>限额</th><th>状态</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="5" class="manager-coupon-empty-row">暂无银行卡</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td>' + common.escapeHtml(buildUserText(item.userDisplayName, item.aipayUid, item.userId)) + '</td>' +
          '<td><span class="admin-embedded-mono">' + common.escapeHtml(item.cardNo || '-') + '</span><br /><span class="admin-embedded-muted">' + common.escapeHtml(item.cardHolderName || '-') + ' / 尾号 ' + common.escapeHtml(item.phoneTailNo || '-') + '</span></td>' +
          '<td>' + common.escapeHtml(item.bankName || item.bankCode || '-') + '<br /><span class="admin-embedded-muted">' + common.escapeHtml(common.formatCardType(item.cardType)) + '</span></td>' +
          '<td>' + common.escapeHtml(common.formatMoney(item.singleLimit)) + '<br /><span class="admin-embedded-muted">日限额 ' + common.escapeHtml(common.formatMoney(item.dailyLimit)) + '</span></td>' +
          '<td>' + common.buildStatusBadge(item.cardStatus || '-', common.formatCardStatus(item.cardStatus)) + '<br /><span class="admin-embedded-muted">默认卡 ' + common.escapeHtml(common.formatBooleanText(item.isDefault)) + '</span></td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div></section>';
    return html;
  }

  function renderCashierSection() {
    var filters = state.filters.cashier;
    var view = state.data.cashierView;
    var preview = state.data.pricingPreview;
    var html = '';
    html += '<section id="fundCashierSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('收银台', '独立页面，查询用户场景下的可用支付工具并做价格试算。', (view && view.payTools ? view.payTools.length : 0));
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('用户ID', '<input id="fundCashierUserId" class="form-input" value="' + common.escapeHtml(filters.userId) + '" placeholder="userId" />');
    html += renderField('场景编码', '<input id="fundCashierSceneCode" class="form-input" value="' + common.escapeHtml(filters.sceneCode) + '" placeholder="sceneCode" />');
    html += renderField('支付方式', '<input id="fundCashierPaymentMethod" class="form-input" value="' + common.escapeHtml(filters.paymentMethod) + '" placeholder="如 WALLET（余额）/ BANK_CARD（银行卡）" />');
    html += renderField('金额', '<input id="fundCashierAmount" class="form-input" value="' + common.escapeHtml(filters.amount) + '" placeholder="100.00" />');
    html += renderField('币种', '<input id="fundCashierCurrencyCode" class="form-input" value="' + common.escapeHtml(filters.currencyCode) + '" placeholder="CNY" />');
    html += '</div>';
    html += '<div class="admin-embedded-filter-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="query-cashier-view">查询收银台</button>';
    html += '<button class="manager-btn" type="button" data-action="query-cashier-preview">价格试算</button>';
    html += '<button class="manager-btn" type="button" data-action="reset-cashier">重置</button>';
    html += '</div>';

    html += '<div class="admin-embedded-card-grid" style="margin-top:12px;">';
    html += renderCashierCard('场景配置', view ? buildSceneConfigCopy(view.sceneConfig) : '先输入用户ID并点击“查询收银台”。');
    html += renderCashierCard('定价预览', preview ? buildPricingCopy(preview) : '可在右上输入金额与支付方式后进行试算。');
    html += '</div>';

    if (view) {
      html += '<div class="admin-embedded-section-head" style="margin-top:12px;"><div><h3 class="manager-panel-title">支付工具</h3><div class="admin-embedded-section-desc">当前场景下后端返回的可用支付工具列表。</div></div></div>';
      html += renderPayToolTable(view.payTools || []);
    }

    if (preview) {
      html += '<div class="admin-embedded-section-head" style="margin-top:12px;"><div><h3 class="manager-panel-title">试算详情</h3></div></div>';
      html += common.renderDetailGrid([
        { label: '用户ID', value: preview.userId },
        { label: '场景编码', value: common.formatBusinessSceneCode(preview.sceneCode) },
        { label: '定价场景', value: common.formatBusinessSceneCode(preview.pricingSceneCode) },
        { label: '支付方式', value: common.formatPaymentMethod(preview.paymentMethod) },
        { label: '报价单号', value: preview.quoteNo },
        { label: '规则编码', value: preview.ruleCode },
        { label: '规则名称', value: preview.ruleName },
        { label: '原始金额', value: common.formatMoney(preview.originalAmount) },
        { label: '手续费', value: common.formatMoney(preview.feeAmount) },
        { label: '应付金额', value: common.formatMoney(preview.payableAmount) },
        { label: '清算金额', value: common.formatMoney(preview.settleAmount) },
        { label: '费率', value: preview.feeRate },
        { label: '费用承担方', value: common.formatFeeBearer(preview.feeBearer) },
      ]);
    }
    html += '</section>';
    return html;
  }

  function renderCashierCard(title, copy) {
    return '<div class="admin-embedded-card"><h4 class="admin-embedded-card-title">' + common.escapeHtml(title) + '</h4><p class="admin-embedded-card-copy">' + common.escapeHtml(copy) + '</p></div>';
  }

  function renderPayToolTable(rows) {
    var html = '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>工具</th><th>说明</th><th>限额</th><th>银行卡信息</th><th>默认</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="5" class="manager-coupon-empty-row">当前场景暂无可用支付工具</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td>' + common.escapeHtml(item.toolName || item.toolCode || '-') + '<br /><span class="admin-embedded-muted">' + common.escapeHtml(common.formatToolType(item.toolType)) + ' / ' + common.escapeHtml(item.toolCode || '-') + '</span></td>' +
          '<td>' + common.escapeHtml(item.toolDescription || '-') + '</td>' +
          '<td>' + common.escapeHtml(common.formatMoney(item.singleLimit)) + '<br /><span class="admin-embedded-muted">日限额 ' + common.escapeHtml(common.formatMoney(item.dailyLimit)) + '</span></td>' +
          '<td>' + common.escapeHtml(item.bankCode || '-') + '<br /><span class="admin-embedded-muted">' + common.escapeHtml(common.formatCardType(item.cardType)) + ' / 尾号 ' + common.escapeHtml(item.phoneTailNo || '-') + '</span></td>' +
          '<td>' + common.escapeHtml(common.formatBooleanText(item.defaultSelected)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function buildSceneConfigCopy(sceneConfig) {
    if (!sceneConfig) {
      return '暂无场景配置返回。';
    }
    return '渠道：' + (sceneConfig.supportedChannels && sceneConfig.supportedChannels.length ? sceneConfig.supportedChannels.join('、') : '未配置') +
      '；银行卡策略：' + (sceneConfig.bankCardPolicy || '-') +
      '；空态文案：' + (sceneConfig.emptyBankCardText || '-');
  }

  function buildPricingCopy(preview) {
    return '原始金额 ' + common.formatMoney(preview.originalAmount) +
      '，手续费 ' + common.formatMoney(preview.feeAmount) +
      '，应付 ' + common.formatMoney(preview.payableAmount) +
      '。';
  }

  function renderSectionHead(title, description, count) {
    return '<div class="admin-embedded-section-head">' +
      '<div><h3 class="manager-panel-title">' + common.escapeHtml(title) + '</h3><div class="admin-embedded-section-desc">' + common.escapeHtml(description) + '</div></div>' +
      '<div class="admin-embedded-section-meta">' + common.escapeHtml(count) + '</div>' +
      '</div>';
  }

  function renderFilterGrid(fields, section) {
    var filter = state.filters[section] || {};
    var pagination = state.pagination[section] || {};
    var currentPageNo = toPositiveIntOrDefault(filter.pageNo, 1);
    var hasNextPage = !!pagination.hasNextPage;
    return '<div class="manager-coupon-form-grid compact">' + fields.join('') + '</div>' +
      '<div class="admin-embedded-filter-actions">' +
      '<button class="manager-btn primary" type="button" data-action="query-section" data-section="' + section + '">查询</button>' +
      '<button class="manager-btn" type="button" data-action="reset-section" data-section="' + section + '">重置</button>' +
      '<button class="manager-btn" type="button" data-action="query-section-prev-page" data-section="' + section + '"' + (currentPageNo <= 1 ? ' disabled' : '') + '>上一页</button>' +
      '<span class="admin-embedded-note">第 ' + common.escapeHtml(currentPageNo) + ' 页</span>' +
      '<button class="manager-btn" type="button" data-action="query-section-next-page" data-section="' + section + '"' + (!hasNextPage ? ' disabled' : '') + '>下一页</button>' +
      '</div>';
  }

  function renderField(label, controlHtml) {
    return '<label class="manager-coupon-field"><span class="manager-coupon-label">' + common.escapeHtml(label) + '</span>' + controlHtml + '</label>';
  }

  function buildUserText(displayName, aipayUid, userId) {
    var prefix = displayName || aipayUid || '-';
    return prefix + ' / ' + (userId == null ? '-' : userId);
  }
})();
