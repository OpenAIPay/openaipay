(function () {
  var common = window.OpenAiPayAdminEmbedded;
  var root = document.getElementById('tradeOpsConsoleApp');
  var FRAME_ID = 'managerTradeOpsFrame';
  var TRADE_OPS_ROOT_PATH = '/admin/inbound';
  var INITIAL_SELECTED_PATH = common.resolveSelectedPath(TRADE_OPS_ROOT_PATH);
  var PATH_SECTION_MAP = {
    '/admin/inbound': 'tradeOpsInboundSection',
    '/admin/inbound/orders': 'tradeOpsInboundSection',
    '/admin/outbound': 'tradeOpsOutboundSection',
    '/admin/outbound/orders': 'tradeOpsOutboundSection',
  };
  var FRAME_NOTIFIER = common.createFrameHeightNotifier(FRAME_ID, 920);

  var state = {
    session: common.loadSession(),
    selectedPath: INITIAL_SELECTED_PATH,
    loading: false,
    notice: '',
    focusSectionId: resolveFocusSection(INITIAL_SELECTED_PATH),
    pendingFocus: false,
    overview: {
      inbound: {
        totalCount: 0,
        successCount: 0,
        processingCount: 0,
        failedCount: 0,
      },
      outbound: {
        totalCount: 0,
        successCount: 0,
        processingCount: 0,
        failedCount: 0,
      },
    },
    filters: createDefaultFilters(),
    pagination: createDefaultPagination(),
    orders: {
      inbound: [],
      outbound: [],
    },
    detail: {
      inbound: null,
      outbound: null,
    },
    inboundDetailVisible: false,
    outboundDetailVisible: false,
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
    await loadAllData(true);
  }

  function createDefaultFilters() {
    return {
      inbound: {
        inboundId: '',
        requestBizNo: '',
        payOrderNo: '',
        inboundStatus: '',
        pageNo: '1',
        pageSize: '20',
      },
      outbound: {
        outboundId: '',
        requestBizNo: '',
        payOrderNo: '',
        outboundStatus: '',
        pageNo: '1',
        pageSize: '20',
      },
    };
  }

  function createDefaultPagination() {
    return {
      inbound: { hasNextPage: false },
      outbound: { hasNextPage: false },
    };
  }

  function resolveFocusSection(path) {
    return PATH_SECTION_MAP[String(path || TRADE_OPS_ROOT_PATH)] || 'tradeOpsInboundSection';
  }

  function isInboundPath(path) {
    var normalizedPath = String(path || '');
    return normalizedPath === '/admin/inbound' || normalizedPath.indexOf('/admin/inbound/') === 0;
  }

  function isOutboundPath(path) {
    var normalizedPath = String(path || '');
    return normalizedPath === '/admin/outbound' || normalizedPath.indexOf('/admin/outbound/') === 0;
  }

  function resolveActiveScope() {
    if (isOutboundPath(state.selectedPath)) {
      return 'outbound';
    }
    if (isInboundPath(state.selectedPath)) {
      return 'inbound';
    }
    return 'all';
  }

  function buildPageMeta() {
    var scope = resolveActiveScope();
    if (scope === 'outbound') {
      return {
        title: '出金中心',
        subtitle: '查看出金订单状态、结果码与通道响应。',
        loadingText: '正在加载出金订单数据...',
        showInbound: false,
        showOutbound: true,
      };
    }
    if (scope === 'inbound') {
      return {
        title: '入金中心',
        subtitle: '查看入金订单状态、结果码与清算链路字段。',
        loadingText: '正在加载入金订单数据...',
        showInbound: true,
        showOutbound: false,
      };
    }
    return {
      title: '出入金中心',
      subtitle: '统一查看入金与出金订单状态、结果码与通道链路字段。',
      loadingText: '正在加载入金与出金订单数据...',
      showInbound: true,
      showOutbound: true,
    };
  }

  async function loadAllData(showLoadedNotice) {
    state.loading = true;
    render();
    var results = await Promise.allSettled([
      loadInboundOverview(),
      loadInboundOrders(),
      loadOutboundOverview(),
      loadOutboundOrders(),
    ]);
    state.loading = false;
    state.notice = buildNotice(results, showLoadedNotice ? buildPageMeta().title + '已加载' : buildPageMeta().title + '已刷新');
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

  async function loadInboundOverview() {
    state.overview.inbound = await request('/api/admin/inbound/overview');
  }

  async function loadInboundOrders() {
    state.orders.inbound = await request('/api/admin/inbound/orders', state.filters.inbound);
    state.pagination.inbound.hasNextPage = state.orders.inbound.length >= toPositiveIntOrDefault(state.filters.inbound.pageSize, 20);
    if (state.detail.inbound && !findInboundOrder(state.detail.inbound.inboundId)) {
      state.detail.inbound = null;
      state.inboundDetailVisible = false;
    }
  }

  async function loadOutboundOverview() {
    state.overview.outbound = await request('/api/admin/outbound/overview');
  }

  async function loadOutboundOrders() {
    state.orders.outbound = await request('/api/admin/outbound/orders', state.filters.outbound);
    state.pagination.outbound.hasNextPage = state.orders.outbound.length >= toPositiveIntOrDefault(state.filters.outbound.pageSize, 20);
    if (state.detail.outbound && !findOutboundOrder(state.detail.outbound.outboundId)) {
      state.detail.outbound = null;
      state.outboundDetailVisible = false;
    }
  }

  async function loadInboundDetail(inboundId) {
    if (!inboundId) {
      return;
    }
    state.notice = '正在加载入金详情...';
    render();
    try {
      state.detail.inbound = await request('/api/admin/inbound/orders/' + encodeURIComponent(inboundId));
      state.inboundDetailVisible = true;
      state.notice = '入金详情已加载';
    } catch (error) {
      state.notice = error && error.message ? error.message : '入金详情加载失败';
    }
    render();
  }

  async function loadOutboundDetail(outboundId) {
    if (!outboundId) {
      return;
    }
    state.notice = '正在加载出金详情...';
    render();
    try {
      state.detail.outbound = await request('/api/admin/outbound/orders/' + encodeURIComponent(outboundId));
      state.outboundDetailVisible = true;
      state.notice = '出金详情已加载';
    } catch (error) {
      state.notice = error && error.message ? error.message : '出金详情加载失败';
    }
    render();
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
      loadAllData(false);
      return;
    }

    if (action === 'focus-section') {
      section = actionElement.getAttribute('data-section');
      if (section) {
        state.focusSectionId = section;
        state.pendingFocus = true;
        render();
      }
      return;
    }

    if (action === 'query-section') {
      section = actionElement.getAttribute('data-section');
      updateSectionFilters(section);
      querySection(section);
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

    if (action === 'reset-section') {
      section = actionElement.getAttribute('data-section');
      resetSection(section);
      return;
    }

    if (action === 'inbound-detail') {
      loadInboundDetail(actionElement.getAttribute('data-order-id'));
      return;
    }

    if (action === 'hide-inbound-detail') {
      state.inboundDetailVisible = false;
      render();
      return;
    }

    if (action === 'outbound-detail') {
      loadOutboundDetail(actionElement.getAttribute('data-order-id'));
      return;
    }

    if (action === 'hide-outbound-detail') {
      state.outboundDetailVisible = false;
      render();
      return;
    }
  }

  function updateSectionFilters(section) {
    if (section === 'inbound') {
      state.filters.inbound.inboundId = readInputValue('tradeOpsInboundId');
      state.filters.inbound.requestBizNo = readInputValue('tradeOpsInboundRequestBizNo');
      state.filters.inbound.payOrderNo = readInputValue('tradeOpsInboundPayOrderNo');
      state.filters.inbound.inboundStatus = readInputValue('tradeOpsInboundStatus');
      state.filters.inbound.pageNo = readInputValue('tradeOpsInboundPageNo') || '1';
      state.filters.inbound.pageSize = readInputValue('tradeOpsInboundPageSize') || '20';
      return;
    }
    if (section === 'outbound') {
      state.filters.outbound.outboundId = readInputValue('tradeOpsOutboundId');
      state.filters.outbound.requestBizNo = readInputValue('tradeOpsOutboundRequestBizNo');
      state.filters.outbound.payOrderNo = readInputValue('tradeOpsOutboundPayOrderNo');
      state.filters.outbound.outboundStatus = readInputValue('tradeOpsOutboundStatus');
      state.filters.outbound.pageNo = readInputValue('tradeOpsOutboundPageNo') || '1';
      state.filters.outbound.pageSize = readInputValue('tradeOpsOutboundPageSize') || '20';
    }
  }

  async function querySection(section) {
    state.notice = '正在查询...';
    normalizeSectionPagination(section);
    render();
    try {
      if (section === 'inbound') {
        await Promise.all([loadInboundOverview(), loadInboundOrders()]);
        state.notice = '入金订单列表已刷新';
        focusSectionById('tradeOpsInboundSection');
      } else if (section === 'outbound') {
        await Promise.all([loadOutboundOverview(), loadOutboundOrders()]);
        state.notice = '出金订单列表已刷新';
        focusSectionById('tradeOpsOutboundSection');
      }
    } catch (error) {
      if (state.pagination[section]) {
        state.pagination[section].hasNextPage = false;
      }
      state.notice = error && error.message ? error.message : '查询失败';
      render();
    }
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

  function resetSection(section) {
    var defaults = createDefaultFilters();
    var paginationDefaults = createDefaultPagination();
    if (defaults[section]) {
      state.filters[section] = defaults[section];
    }
    if (paginationDefaults[section]) {
      state.pagination[section] = paginationDefaults[section];
    }
    if (section === 'inbound') {
      state.detail.inbound = null;
      state.inboundDetailVisible = false;
    } else if (section === 'outbound') {
      state.detail.outbound = null;
      state.outboundDetailVisible = false;
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

  function focusSectionById(id) {
    state.focusSectionId = id;
    state.pendingFocus = true;
    render();
  }

  function findInboundOrder(inboundId) {
    return (state.orders.inbound || []).find(function (item) {
      return item.inboundId === inboundId;
    }) || null;
  }

  function findOutboundOrder(outboundId) {
    return (state.orders.outbound || []).find(function (item) {
      return item.outboundId === outboundId;
    }) || null;
  }

  function render() {
    var pageMeta = buildPageMeta();
    common.setDocumentTitle(pageMeta.title);
    if (!state.session || !state.session.adminId) {
      root.innerHTML = common.localizeHtml(common.renderAuthRequired('请先登录管理后台', '当前出入金中心无法读取管理员会话，请返回后台登录后重试。'));
      FRAME_NOTIFIER.schedule();
      return;
    }

    if (state.loading && !state.orders.inbound.length && !state.orders.outbound.length) {
      root.innerHTML = common.localizeHtml(common.renderLoading(pageMeta.title, pageMeta.loadingText));
      FRAME_NOTIFIER.schedule();
      return;
    }

    root.innerHTML = common.localizeHtml(renderPage());
    FRAME_NOTIFIER.schedule();
    if (state.pendingFocus && state.focusSectionId) {
      state.pendingFocus = false;
      common.focusSection(state.focusSectionId);
    }
  }

  function renderPage() {
    var pageMeta = buildPageMeta();
    var html = '';
    html += '<div class="admin-embedded-toolbar">';
    html += '<div>';
    html += '<h1 class="manager-title">' + common.escapeHtml(pageMeta.title) + '</h1>';
    html += '<p class="manager-subtext">' + common.escapeHtml(pageMeta.subtitle) + '</p>';
    if (pageMeta.showInbound && pageMeta.showOutbound) {
      html += renderAnchorRow();
    }
    html += '</div>';
    html += '<div class="admin-embedded-toolbar-actions">';
    html += '<span class="admin-embedded-toolbar-note">当前管理员：' + common.escapeHtml(common.pickDisplayName(state.session)) + '</span>';
    html += '<button class="manager-btn" type="button" data-action="refresh-all">刷新数据</button>';
    html += '</div>';
    html += '</div>';
    if (state.notice) {
      html += '<section class="manager-panel"><span class="admin-embedded-note">' + common.escapeHtml(state.notice) + '</span></section>';
    }
    if (pageMeta.showInbound) {
      html += renderInboundSection();
      html += renderInboundDetailModal();
    }
    if (pageMeta.showOutbound) {
      html += renderOutboundSection();
      html += renderOutboundDetailModal();
    }
    return html;
  }

  function renderAnchorRow() {
    return '<div class="admin-embedded-anchor-row">' +
      renderAnchor('入金订单', 'tradeOpsInboundSection') +
      renderAnchor('出金订单', 'tradeOpsOutboundSection') +
      '</div>';
  }

  function renderAnchor(label, sectionId) {
    var activeClass = state.focusSectionId === sectionId ? ' active' : '';
    return '<button class="admin-embedded-anchor' + activeClass + '" type="button" data-action="focus-section" data-section="' + sectionId + '">' +
      common.escapeHtml(label) +
      '</button>';
  }

  function renderInboundSection() {
    var filters = state.filters.inbound;
    var overview = state.overview.inbound;
    var rows = state.orders.inbound || [];
    var html = '';
    html += '<section id="tradeOpsInboundSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('入金订单', '对齐 C 端充值/入金链路，排查订单状态、结果码与通道链路字段。', rows.length);
    html += renderOverviewStrip(overview);
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('入金单号', '<input id="tradeOpsInboundId" class="form-input" value="' + common.escapeHtml(filters.inboundId) + '" placeholder="inboundId" />');
    html += renderField('业务请求号', '<input id="tradeOpsInboundRequestBizNo" class="form-input" value="' + common.escapeHtml(filters.requestBizNo) + '" placeholder="requestBizNo" />');
    html += renderField('支付单号', '<input id="tradeOpsInboundPayOrderNo" class="form-input" value="' + common.escapeHtml(filters.payOrderNo) + '" placeholder="payOrderNo" />');
    html += renderField('处理状态', '<input id="tradeOpsInboundStatus" class="form-input" value="' + common.escapeHtml(filters.inboundStatus) + '" placeholder="如 SUBMITTED（已提交）/ ACCEPTED（已受理）/ SUCCEEDED（成功）" />');
    html += renderField('页码', '<input id="tradeOpsInboundPageNo" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />');
    html += renderField('每页条数', '<input id="tradeOpsInboundPageSize" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />');
    html += '</div>';
    html += renderSectionActions('inbound');
    html += renderInboundTable(rows);
    html += '</section>';
    return html;
  }

  function renderOutboundSection() {
    var filters = state.filters.outbound;
    var overview = state.overview.outbound;
    var rows = state.orders.outbound || [];
    var html = '';
    html += '<section id="tradeOpsOutboundSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('出金订单', '对齐提现/转出链路，排查订单状态、结果码与通道响应。', rows.length);
    html += renderOverviewStrip(overview);
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('出金单号', '<input id="tradeOpsOutboundId" class="form-input" value="' + common.escapeHtml(filters.outboundId) + '" placeholder="outboundId" />');
    html += renderField('业务请求号', '<input id="tradeOpsOutboundRequestBizNo" class="form-input" value="' + common.escapeHtml(filters.requestBizNo) + '" placeholder="requestBizNo" />');
    html += renderField('支付单号', '<input id="tradeOpsOutboundPayOrderNo" class="form-input" value="' + common.escapeHtml(filters.payOrderNo) + '" placeholder="payOrderNo" />');
    html += renderField('处理状态', '<input id="tradeOpsOutboundStatus" class="form-input" value="' + common.escapeHtml(filters.outboundStatus) + '" placeholder="如 CREATED（已创建）/ SUBMITTED（已提交）/ ACCEPTED（已受理）/ SUCCEEDED（成功）" />');
    html += renderField('页码', '<input id="tradeOpsOutboundPageNo" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />');
    html += renderField('每页条数', '<input id="tradeOpsOutboundPageSize" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />');
    html += '</div>';
    html += renderSectionActions('outbound');
    html += renderOutboundTable(rows);
    html += '</section>';
    return html;
  }

  function renderOverviewStrip(overview) {
    return '<div class="admin-embedded-summary-list" style="margin:8px 0 0;">' +
      renderMiniStat('总量', overview.totalCount) +
      renderMiniStat('成功', overview.successCount) +
      renderMiniStat('处理中', overview.processingCount) +
      renderMiniStat('失败', overview.failedCount) +
      '</div>';
  }

  function renderMiniStat(label, value) {
    return '<div class="admin-embedded-summary-item"><span class="admin-embedded-summary-label">' + common.escapeHtml(label) + '</span><span class="admin-embedded-summary-value">' + common.escapeHtml(value) + '</span></div>';
  }

  function renderInboundTable(rows) {
    var selectedId = state.detail.inbound && state.detail.inbound.inboundId;
    var html = '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>入金单号</th><th>业务请求号</th><th>支付单号</th><th>金额</th><th>状态</th><th>结果码</th><th>操作</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="7" class="manager-coupon-empty-row">暂无入金订单</td></tr>';
    } else {
      html += rows.map(function (item) {
        var selectedClass = item.inboundId === selectedId ? ' class="manager-table-row-selected"' : '';
        return '<tr' + selectedClass + '>' +
          '<td><span class="admin-embedded-mono">' + common.escapeHtml(item.inboundId) + '</span><br /><span class="admin-embedded-muted">' + common.escapeHtml(item.instId || '-') + '</span></td>' +
          '<td>' + common.escapeHtml(item.requestBizNo || '-') + '</td>' +
          '<td>' + common.escapeHtml(item.payOrderNo || '-') + '</td>' +
          '<td>' + common.escapeHtml(common.formatMoney(item.inboundAmount)) + '<br /><span class="admin-embedded-muted">清算 ' + common.escapeHtml(common.formatMoney(item.settleAmount)) + '</span></td>' +
          '<td>' + common.buildStatusBadge(item.inboundStatus || '-', common.formatOperationStatus(item.inboundStatus)) + '</td>' +
          '<td>' + common.escapeHtml(item.resultCode || '-') + '<br /><span class="admin-embedded-muted">' + common.escapeHtml(common.formatDateTime(item.gmtSubmit)) + '</span></td>' +
          '<td class="admin-embedded-table-actions"><button class="manager-btn" type="button" data-action="inbound-detail" data-order-id="' + common.escapeHtml(item.inboundId) + '">详情</button></td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function renderOutboundTable(rows) {
    var selectedId = state.detail.outbound && state.detail.outbound.outboundId;
    var html = '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>出金单号</th><th>业务请求号</th><th>支付单号</th><th>金额</th><th>状态</th><th>结果码</th><th>操作</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="7" class="manager-coupon-empty-row">暂无出金订单</td></tr>';
    } else {
      html += rows.map(function (item) {
        var selectedClass = item.outboundId === selectedId ? ' class="manager-table-row-selected"' : '';
        return '<tr' + selectedClass + '>' +
          '<td><span class="admin-embedded-mono">' + common.escapeHtml(item.outboundId) + '</span><br /><span class="admin-embedded-muted">' + common.escapeHtml(item.instId || '-') + '</span></td>' +
          '<td>' + common.escapeHtml(item.requestBizNo || '-') + '</td>' +
          '<td>' + common.escapeHtml(item.payOrderNo || '-') + '</td>' +
          '<td>' + common.escapeHtml(common.formatMoney(item.outboundAmount)) + '</td>' +
          '<td>' + common.buildStatusBadge(item.outboundStatus || '-', common.formatOperationStatus(item.outboundStatus)) + '</td>' +
          '<td>' + common.escapeHtml(item.resultCode || '-') + '<br /><span class="admin-embedded-muted">' + common.escapeHtml(common.formatDateTime(item.gmtSubmit)) + '</span></td>' +
          '<td class="admin-embedded-table-actions"><button class="manager-btn" type="button" data-action="outbound-detail" data-order-id="' + common.escapeHtml(item.outboundId) + '">详情</button></td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function renderInboundDetail() {
    var item = state.detail.inbound;
    var html = '';
    if (!item) {
      return '<p class="manager-placeholder-tip">点击左侧入金订单查看完整链路字段。</p>';
    }
    html += common.renderDetailGrid([
      { label: '入金单号', value: item.inboundId },
      { label: '机构ID', value: item.instId },
      { label: '业务请求号', value: item.requestBizNo },
      { label: '支付单号', value: item.payOrderNo },
      { label: '付款账号', value: item.payerAccountNo },
      { label: '处理金额', value: common.formatMoney(item.inboundAmount) },
      { label: '记账金额', value: common.formatMoney(item.accountAmount) },
      { label: '清算金额', value: common.formatMoney(item.settleAmount) },
      { label: '处理状态', value: common.formatOperationStatus(item.inboundStatus) },
      { label: '结果码', value: item.resultCode },
      { label: '结果说明', value: item.resultDescription },
      { label: '财务处理编码', value: item.instChannelCode },
      { label: '入金单号', value: item.inboundOrderNo },
      { label: '支付渠道编码', value: item.payChannelCode },
      { label: '提交时间', value: common.formatDateTime(item.gmtSubmit) },
      { label: '响应时间', value: common.formatDateTime(item.gmtResp) },
      { label: '清算时间', value: common.formatDateTime(item.gmtSettle) },
    ]);
    html += '<div class="admin-embedded-raw">' + common.renderJsonBlock(item) + '</div>';
    return html;
  }

  function renderOutboundDetail() {
    var item = state.detail.outbound;
    var html = '';
    if (!item) {
      return '<p class="manager-placeholder-tip">点击左侧出金订单查看完整链路字段。</p>';
    }
    html += common.renderDetailGrid([
      { label: '出金单号', value: item.outboundId },
      { label: '机构ID', value: item.instId },
      { label: '业务请求号', value: item.requestBizNo },
      { label: '支付单号', value: item.payOrderNo },
      { label: '收款账号', value: item.payeeAccountNo },
      { label: '处理金额', value: common.formatMoney(item.outboundAmount) },
      { label: '处理状态', value: common.formatOperationStatus(item.outboundStatus) },
      { label: '结果码', value: item.resultCode },
      { label: '结果说明', value: item.resultDescription },
      { label: '财务处理编码', value: item.instChannelCode },
      { label: '出金单号', value: item.outboundOrderNo },
      { label: '支付渠道编码', value: item.payChannelCode },
      { label: '提交时间', value: common.formatDateTime(item.gmtSubmit) },
      { label: '响应时间', value: common.formatDateTime(item.gmtResp) },
      { label: '清算时间', value: common.formatDateTime(item.gmtSettle) },
    ]);
    html += '<div class="admin-embedded-raw">' + common.renderJsonBlock(item) + '</div>';
    return html;
  }

  function renderInboundDetailModal() {
    if (!state.inboundDetailVisible) {
      return '';
    }
    var html = '';
    html += '<div class="manager-modal-layer">';
    html += '<div class="manager-modal-mask" data-action="hide-inbound-detail"></div>';
    html += '<section class="manager-modal-panel manager-user-detail-modal">';
    html += '<div class="manager-modal-head">';
    html += '<div><h3 class="manager-panel-title">入金详情</h3><div class="admin-embedded-section-desc">详情查看改为弹窗处理，列表页仅保留订单列表。</div></div>';
    html += '<button class="manager-modal-close" type="button" data-action="hide-inbound-detail" aria-label="关闭">×</button>';
    html += '</div>';
    html += renderInboundDetail();
    html += '</section>';
    html += '</div>';
    return html;
  }

  function renderOutboundDetailModal() {
    if (!state.outboundDetailVisible) {
      return '';
    }
    var html = '';
    html += '<div class="manager-modal-layer">';
    html += '<div class="manager-modal-mask" data-action="hide-outbound-detail"></div>';
    html += '<section class="manager-modal-panel manager-user-detail-modal">';
    html += '<div class="manager-modal-head">';
    html += '<div><h3 class="manager-panel-title">出金详情</h3><div class="admin-embedded-section-desc">详情查看改为弹窗处理，列表页仅保留订单列表。</div></div>';
    html += '<button class="manager-modal-close" type="button" data-action="hide-outbound-detail" aria-label="关闭">×</button>';
    html += '</div>';
    html += renderOutboundDetail();
    html += '</section>';
    html += '</div>';
    return html;
  }

  function renderSectionHead(title, description, count) {
    return '<div class="admin-embedded-section-head">' +
      '<div><h3 class="manager-panel-title">' + common.escapeHtml(title) + '</h3><div class="admin-embedded-section-desc">' + common.escapeHtml(description) + '</div></div>' +
      '<div class="admin-embedded-section-meta">' + common.escapeHtml(count) + '</div>' +
      '</div>';
  }

  function renderSectionActions(section) {
    var filter = state.filters[section] || {};
    var pagination = state.pagination[section] || {};
    var currentPageNo = toPositiveIntOrDefault(filter.pageNo, 1);
    var hasNextPage = !!pagination.hasNextPage;
    return '<div class="admin-embedded-filter-actions">' +
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
})();
