(function () {
  var common = window.OpenAiPayAdminEmbedded;
  var root = document.getElementById('riskConsoleApp');
  var FRAME_ID = 'managerRiskFrame';
  var RISK_ROOT_PATH = '/admin/risk';
  var PATH_SECTION_MAP = {
    '/admin/risk': 'riskUserSection',
    '/admin/risk/users': 'riskUserSection',
    '/admin/risk/blacklist': 'riskBlacklistSection',
  };
  var FRAME_NOTIFIER = common.createFrameHeightNotifier(FRAME_ID, 920);

  var state = {
    session: common.loadSession(),
    loading: false,
    saving: false,
    notice: '',
    focusSectionId: resolveFocusSection(common.resolveSelectedPath(RISK_ROOT_PATH)),
    pendingFocus: false,
    overview: {
      totalUserCount: 0,
      l0Count: 0,
      l1Count: 0,
      l2Count: 0,
      l3Count: 0,
      lowRiskCount: 0,
      mediumRiskCount: 0,
      highRiskCount: 0,
      blacklistCount: 0,
    },
    filters: createDefaultFilters(),
    pagination: createDefaultPagination(),
    users: [],
    blacklist: [],
    selectedUserId: null,
    userDetailVisible: false,
    editingUserId: null,
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
      users: {
        keyword: '',
        kycLevel: '',
        riskLevel: '',
        pageNo: '1',
        pageSize: '20',
      },
      blacklist: {
        ownerUserId: '',
        blockedUserId: '',
        pageNo: '1',
        pageSize: '20',
      },
    };
  }

  function createDefaultPagination() {
    return {
      users: { hasNextPage: false },
      blacklist: { hasNextPage: false },
    };
  }

  function resolveFocusSection(path) {
    return PATH_SECTION_MAP[String(path || RISK_ROOT_PATH)] || 'riskUserSection';
  }

  async function loadAllData(showLoadedNotice) {
    state.loading = true;
    render();
    var results = await Promise.allSettled([
      loadOverview(),
      loadUsers(),
      loadBlacklist(),
    ]);
    state.loading = false;
    state.notice = buildNotice(results, showLoadedNotice ? '风控中心已加载' : '风控中心已刷新');
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

  async function request(path, options) {
    var requestOptions = options || {};
    requestOptions.headers = requestOptions.headers || common.buildAdminHeaders(state.session);
    return common.requestJson(path, requestOptions);
  }

  async function loadOverview() {
    state.overview = await request('/api/admin/risk/overview', {
      method: 'GET',
      headers: common.buildAdminHeaders(state.session),
    });
  }

  async function loadUsers() {
    var qs = common.serializeQuery(state.filters.users);
    state.users = await request('/api/admin/risk/users' + (qs ? '?' + qs : ''), {
      method: 'GET',
      headers: common.buildAdminHeaders(state.session),
    });
    state.pagination.users.hasNextPage = state.users.length >= toPositiveIntOrDefault(state.filters.users.pageSize, 20);
    if (state.selectedUserId != null && !findUserById(state.selectedUserId)) {
      state.selectedUserId = null;
      state.userDetailVisible = false;
    }
    if (state.editingUserId != null && !findUserById(state.editingUserId)) {
      state.editingUserId = null;
    }
  }

  async function loadBlacklist() {
    var qs = common.serializeQuery(state.filters.blacklist);
    state.blacklist = await request('/api/admin/risk/blacklist' + (qs ? '?' + qs : ''), {
      method: 'GET',
      headers: common.buildAdminHeaders(state.session),
    });
    state.pagination.blacklist.hasNextPage = state.blacklist.length >= toPositiveIntOrDefault(state.filters.blacklist.pageSize, 20);
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

    if (action === 'select-user') {
      state.selectedUserId = toPositiveIdText(actionElement.getAttribute('data-user-id'));
      state.userDetailVisible = true;
      render();
      return;
    }

    if (action === 'hide-user-detail') {
      state.userDetailVisible = false;
      render();
      return;
    }

    if (action === 'edit-user') {
      state.editingUserId = toPositiveIdText(actionElement.getAttribute('data-user-id'));
      render();
      return;
    }

    if (action === 'close-edit-user') {
      state.editingUserId = null;
      render();
      return;
    }

    if (action === 'save-edit-user') {
      saveRiskProfile();
    }
  }

  function updateSectionFilters(section) {
    if (section === 'users') {
      state.filters.users.keyword = readInputValue('riskUserKeyword');
      state.filters.users.kycLevel = readInputValue('riskUserKycLevel');
      state.filters.users.riskLevel = readInputValue('riskUserRiskLevel');
      state.filters.users.pageNo = readInputValue('riskUserPageNo') || '1';
      state.filters.users.pageSize = readInputValue('riskUserPageSize') || '20';
      return;
    }
    if (section === 'blacklist') {
      state.filters.blacklist.ownerUserId = readInputValue('riskBlacklistOwnerUserId');
      state.filters.blacklist.blockedUserId = readInputValue('riskBlacklistBlockedUserId');
      state.filters.blacklist.pageNo = readInputValue('riskBlacklistPageNo') || '1';
      state.filters.blacklist.pageSize = readInputValue('riskBlacklistPageSize') || '20';
    }
  }

  async function querySection(section) {
    state.notice = '正在查询...';
    normalizeSectionPagination(section);
    render();
    try {
      if (section === 'users') {
        await loadUsers();
        state.notice = '风险用户列表已刷新';
        focusSectionById('riskUserSection');
      } else if (section === 'blacklist') {
        await loadBlacklist();
        state.notice = '黑名单列表已刷新';
        focusSectionById('riskBlacklistSection');
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
    if (section === 'users') {
      state.selectedUserId = null;
      state.userDetailVisible = false;
      state.editingUserId = null;
    }
    render();
    querySection(section);
  }

  async function saveRiskProfile() {
    var editingUser = findUserById(state.editingUserId);
    var payload;
    if (!editingUser) {
      state.notice = '未找到待编辑用户';
      state.editingUserId = null;
      render();
      return;
    }
    payload = {
      kycLevel: readInputValue('riskEditKycLevel'),
      riskLevel: readInputValue('riskEditRiskLevel'),
      twoFactorMode: readInputValue('riskEditTwoFactorMode'),
      deviceLockEnabled: readBooleanValue('riskEditDeviceLockEnabled'),
      privacyModeEnabled: readBooleanValue('riskEditPrivacyModeEnabled'),
    };
    state.saving = true;
    state.notice = '正在保存风险档案...';
    render();
    try {
      await request('/api/admin/risk/users/' + encodeURIComponent(editingUser.userId) + '/risk-profile', {
        method: 'PUT',
        headers: common.withJson(common.buildAdminHeaders(state.session)),
        body: JSON.stringify(payload),
      });
      await Promise.all([loadOverview(), loadUsers()]);
      state.selectedUserId = editingUser.userId;
      state.userDetailVisible = true;
      state.editingUserId = null;
      state.notice = '风险档案已更新';
    } catch (error) {
      state.notice = error && error.message ? error.message : '风险档案保存失败';
    }
    state.saving = false;
    render();
  }

  function readBooleanValue(id) {
    var raw = readInputValue(id);
    return String(raw || '').toLowerCase() === 'true';
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

  function toPositiveIdText(raw) {
    var normalized = String(raw == null ? '' : raw).trim();
    if (!/^\d+$/.test(normalized) || normalized === '0') {
      return null;
    }
    return normalized;
  }

  function samePositiveId(left, right) {
    var leftId = toPositiveIdText(left);
    var rightId = toPositiveIdText(right);
    return !!leftId && leftId === rightId;
  }

  function findUserById(userId) {
    return (state.users || []).find(function (item) {
      return samePositiveId(item.userId, userId);
    }) || null;
  }

  function focusSectionById(id) {
    state.focusSectionId = id;
    state.pendingFocus = true;
    render();
  }

  function render() {
    common.setDocumentTitle('风控中心');
    if (!state.session || !state.session.adminId) {
      root.innerHTML = common.localizeHtml(common.renderAuthRequired('请先登录管理后台', '当前风控中心无法读取管理员会话，请返回后台登录后重试。'));
      FRAME_NOTIFIER.schedule();
      return;
    }

    if (state.loading && !state.users.length && !state.blacklist.length) {
      root.innerHTML = common.localizeHtml(common.renderLoading('风控中心', '正在加载 KYC、风险档案与黑名单数据...'));
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
    var html = '';
    html += '<div class="admin-embedded-toolbar">';
    html += '<div>';
    html += '<h1 class="manager-title">风控中心</h1>';
    html += '<p class="manager-subtext">统一查看 KYC、风险等级、双因子、设备锁与联系人黑名单。</p>';
    html += renderAnchorRow();
    html += '</div>';
    html += '<div class="admin-embedded-toolbar-actions">';
    html += '<span class="admin-embedded-toolbar-note">当前管理员：' + common.escapeHtml(common.pickDisplayName(state.session)) + '</span>';
    html += '<button class="manager-btn" type="button" data-action="refresh-all">刷新数据</button>';
    html += '</div>';
    html += '</div>';

    html += renderOverview();
    if (state.notice) {
      html += '<section class="manager-panel"><span class="admin-embedded-note">' + common.escapeHtml(state.notice) + '</span></section>';
    }
    html += renderUserSection();
    html += renderBlacklistSection();
    html += renderUserDetailModal(findUserById(state.selectedUserId));
    html += renderEditModal();
    return html;
  }

  function renderAnchorRow() {
    return '<div class="admin-embedded-anchor-row">' +
      renderAnchor('风险用户', 'riskUserSection') +
      renderAnchor('黑名单', 'riskBlacklistSection') +
      '</div>';
  }

  function renderAnchor(label, sectionId) {
    var activeClass = state.focusSectionId === sectionId ? ' active' : '';
    return '<button class="admin-embedded-anchor' + activeClass + '" type="button" data-action="focus-section" data-section="' + sectionId + '">' +
      common.escapeHtml(label) +
      '</button>';
  }

  function renderOverview() {
    var cards = [
      { label: '总用户', value: state.overview.totalUserCount },
      { label: 'L0', value: state.overview.l0Count },
      { label: 'L1', value: state.overview.l1Count },
      { label: 'L2', value: state.overview.l2Count },
      { label: 'L3', value: state.overview.l3Count },
      { label: '低风险', value: state.overview.lowRiskCount },
      { label: '中风险', value: state.overview.mediumRiskCount },
      { label: '高风险', value: state.overview.highRiskCount },
      { label: '黑名单', value: state.overview.blacklistCount },
    ];
    return '<section class="manager-panel admin-embedded-summary-panel">' +
      '<div class="admin-embedded-summary-strip">' +
      '<div class="admin-embedded-summary-title">风控概览</div>' +
      '<div class="admin-embedded-summary-list">' +
      cards.map(function (card) {
        return '<div class="admin-embedded-summary-item"><span class="admin-embedded-summary-label">' + common.escapeHtml(card.label) + '</span><span class="admin-embedded-summary-value">' + common.escapeHtml(card.value) + '</span></div>';
      }).join('') +
      '</div></div></section>';
  }

  function renderUserSection() {
    var filters = state.filters.users;
    var html = '';
    html += '<section id="riskUserSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('风险用户', '查看 KYC、风险等级、双因子与隐私/设备策略。', state.users.length);
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('关键字', '<input id="riskUserKeyword" class="form-input" value="' + common.escapeHtml(filters.keyword) + '" placeholder="userId / aipayUid / 登录号 / 昵称" />');
    html += renderField('KYC', '<input id="riskUserKycLevel" class="form-input" value="' + common.escapeHtml(filters.kycLevel) + '" placeholder="如 L0 未实名 / L1 基础实名" />');
    html += renderField('风险等级', '<input id="riskUserRiskLevel" class="form-input" value="' + common.escapeHtml(filters.riskLevel) + '" placeholder="如 低风险 / 中风险 / 高风险" />');
    html += renderField('页码', '<input id="riskUserPageNo" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />');
    html += renderField('每页条数', '<input id="riskUserPageSize" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />');
    html += '</div>';
    html += renderSectionActions('users');
    html += renderUserTable();
    html += '</section>';
    return html;
  }

  function renderUserTable() {
    var html = '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>用户</th><th>账号状态</th><th>KYC</th><th>风险档案</th><th>隐私开关</th><th>操作</th></tr></thead><tbody>';
    if (!state.users.length) {
      html += '<tr><td colspan="6" class="manager-coupon-empty-row">暂无风险用户数据</td></tr>';
    } else {
      html += state.users.map(function (item) {
        var isSelected = samePositiveId(item.userId, state.selectedUserId) ? ' class="manager-table-row-selected"' : '';
        return '<tr' + isSelected + '>' +
          '<td>' + common.escapeHtml(buildUserText(item)) + '</td>' +
          '<td>' + common.buildStatusBadge(item.accountStatus || '-', common.formatAccountStatus(item.accountStatus)) + '</td>' +
          '<td>' + common.escapeHtml(common.formatKycLevel(item.kycLevel)) + '</td>' +
          '<td>' + common.escapeHtml('风险 ' + common.formatRiskLevel(item.riskLevel) + ' / 2FA ' + common.formatTwoFactorMode(item.twoFactorMode)) + '<br /><span class="admin-embedded-muted">设备锁 ' + common.escapeHtml(common.formatBooleanText(item.deviceLockEnabled)) + ' / 隐私模式 ' + common.escapeHtml(common.formatBooleanText(item.privacyModeEnabled)) + '</span></td>' +
          '<td>' + common.escapeHtml('手机号搜索 ' + common.formatBooleanText(item.allowSearchByMobile)) + '<br /><span class="admin-embedded-muted">UID搜索 ' + common.escapeHtml(common.formatBooleanText(item.allowSearchByAipayUid)) + ' / 隐藏实名 ' + common.escapeHtml(common.formatBooleanText(item.hideRealName)) + '</span></td>' +
          '<td class="admin-embedded-table-actions"><button class="manager-btn" type="button" data-action="select-user" data-user-id="' + common.escapeHtml(item.userId) + '">详情</button><button class="manager-btn" type="button" data-action="edit-user" data-user-id="' + common.escapeHtml(item.userId) + '">修改</button></td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function renderUserDetail(user) {
    var html = '';
    if (!user) {
      return '<p class="manager-placeholder-tip">点击左侧用户查看完整风险档案。</p>';
    }
    html += common.renderDetailGrid([
      { label: '用户ID', value: user.userId },
      { label: '展示名', value: user.displayName },
      { label: '爱付号', value: user.aipayUid },
      { label: '登录号', value: user.loginId },
      { label: '手机号', value: user.mobile },
      { label: '账号状态', value: common.formatAccountStatus(user.accountStatus) },
      { label: 'KYC', value: common.formatKycLevel(user.kycLevel) },
      { label: '风险等级', value: common.formatRiskLevel(user.riskLevel) },
      { label: '双因子', value: common.formatTwoFactorMode(user.twoFactorMode) },
      { label: '设备锁', value: common.formatBooleanText(user.deviceLockEnabled) },
      { label: '隐私模式', value: common.formatBooleanText(user.privacyModeEnabled) },
      { label: '手机号搜索', value: common.formatBooleanText(user.allowSearchByMobile) },
      { label: 'UID搜索', value: common.formatBooleanText(user.allowSearchByAipayUid) },
      { label: '隐藏实名', value: common.formatBooleanText(user.hideRealName) },
      { label: '个性化推荐', value: common.formatBooleanText(user.personalizedRecommendationEnabled) },
      { label: '更新时间', value: common.formatDateTime(user.updatedAt) },
    ]);
    html += '<div class="admin-embedded-filter-actions"><button class="manager-btn primary" type="button" data-action="edit-user" data-user-id="' + common.escapeHtml(user.userId) + '">调整风险档案</button></div>';
    return html;
  }

  function renderUserDetailModal(user) {
    if (!state.userDetailVisible) {
      return '';
    }
    var html = '';
    html += '<div class="manager-modal-layer">';
    html += '<div class="manager-modal-mask" data-action="hide-user-detail"></div>';
    html += '<section class="manager-modal-panel manager-user-detail-modal">';
    html += '<div class="manager-modal-head">';
    html += '<div><h3 class="manager-panel-title">风险档案详情</h3><div class="admin-embedded-section-desc">详情查看改为弹窗处理，主页面仅保留风险用户列表。</div></div>';
    html += '<button class="manager-modal-close" type="button" data-action="hide-user-detail" aria-label="关闭">×</button>';
    html += '</div>';
    html += renderUserDetail(user);
    html += '</section>';
    html += '</div>';
    return html;
  }

  function renderBlacklistSection() {
    var filters = state.filters.blacklist;
    var html = '';
    html += '<section id="riskBlacklistSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('联系人黑名单', '复用联系人域黑名单，辅助风控排查封禁关系。', state.blacklist.length);
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('主用户', '<input id="riskBlacklistOwnerUserId" class="form-input" value="' + common.escapeHtml(filters.ownerUserId) + '" placeholder="ownerUserId" />');
    html += renderField('被拉黑用户', '<input id="riskBlacklistBlockedUserId" class="form-input" value="' + common.escapeHtml(filters.blockedUserId) + '" placeholder="blockedUserId" />');
    html += renderField('页码', '<input id="riskBlacklistPageNo" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />');
    html += renderField('每页条数', '<input id="riskBlacklistPageSize" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />');
    html += '</div>';
    html += renderSectionActions('blacklist');
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>主用户</th><th>被拉黑用户</th><th>原因</th><th>更新时间</th></tr></thead><tbody>';
    if (!state.blacklist.length) {
      html += '<tr><td colspan="4" class="manager-coupon-empty-row">暂无黑名单数据</td></tr>';
    } else {
      html += state.blacklist.map(function (item) {
        return '<tr>' +
          '<td>' + common.escapeHtml((item.ownerDisplayName || '-') + ' / ' + (item.ownerAipayUid || '-') + ' / ' + (item.ownerUserId || '-')) + '</td>' +
          '<td>' + common.escapeHtml((item.blockedDisplayName || '-') + ' / ' + (item.blockedAipayUid || '-') + ' / ' + (item.blockedUserId || '-')) + '</td>' +
          '<td>' + common.escapeHtml(item.reason || '-') + '</td>' +
          '<td>' + common.escapeHtml(common.formatDateTime(item.updatedAt || item.createdAt)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div></section>';
    return html;
  }

  function renderEditModal() {
    var user = findUserById(state.editingUserId);
    var html = '';
    if (!user) {
      return html;
    }
    html += '<div class="manager-modal-layer">';
    html += '<div class="manager-modal-mask" data-action="close-edit-user"></div>';
    html += '<section class="manager-modal-panel">';
    html += '<div class="manager-modal-head">';
    html += '<div><h3 class="manager-panel-title">修改风险档案</h3><div class="admin-embedded-section-desc">' + common.escapeHtml(buildUserText(user)) + '</div></div>';
    html += '<button class="manager-modal-close" type="button" data-action="close-edit-user" aria-label="关闭">×</button>';
    html += '</div>';
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('KYC', '<input id="riskEditKycLevel" class="form-input" value="' + common.escapeHtml(user.kycLevel || '') + '" placeholder="如 L0 / L1 / L2 / L3" />');
    html += renderField('风险等级', '<input id="riskEditRiskLevel" class="form-input" value="' + common.escapeHtml(user.riskLevel || '') + '" placeholder="如 LOW / MEDIUM / HIGH" />');
    html += renderField('双因子', '<input id="riskEditTwoFactorMode" class="form-input" value="' + common.escapeHtml(user.twoFactorMode || '') + '" placeholder="如 NONE / SMS / APP / BIOMETRIC" />');
    html += renderField('设备锁', '<select id="riskEditDeviceLockEnabled" class="form-input"><option value="true"' + (user.deviceLockEnabled === true ? ' selected' : '') + '>是</option><option value="false"' + (user.deviceLockEnabled === false ? ' selected' : '') + '>否</option></select>');
    html += renderField('隐私模式', '<select id="riskEditPrivacyModeEnabled" class="form-input"><option value="true"' + (user.privacyModeEnabled === true ? ' selected' : '') + '>是</option><option value="false"' + (user.privacyModeEnabled === false ? ' selected' : '') + '>否</option></select>');
    html += '</div>';
    html += '<div class="admin-embedded-filter-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="save-edit-user"' + (state.saving ? ' disabled' : '') + '>保存</button>';
    html += '<button class="manager-btn" type="button" data-action="close-edit-user">取消</button>';
    html += '</div>';
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

  function buildUserText(user) {
    return (user.displayName || user.aipayUid || '-') + ' / ' + (user.userId == null ? '-' : user.userId);
  }
})();
