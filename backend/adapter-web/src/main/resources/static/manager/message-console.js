(function () {
  var common = window.OpenAiPayAdminEmbedded;
  var root = document.getElementById('messageConsoleApp');
  var FRAME_ID = 'managerMessageFrame';
  var MESSAGE_ROOT_PATH = '/admin/messages';
  var INITIAL_SELECTED_PATH = common.resolveSelectedPath(MESSAGE_ROOT_PATH);
  var PATH_SECTION_MAP = {
    '/admin/messages': 'messageConversationSection',
    '/admin/messages/conversations': 'messageConversationSection',
    '/admin/messages/records': 'messageRecordSection',
    '/admin/messages/red-packets': 'messageRedPacketSection',
    '/admin/messages/contact-requests': 'messageContactRequestSection',
    '/admin/messages/friends': 'messageFriendshipSection',
    '/admin/messages/blacklist': 'messageBlacklistSection',
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
      conversationCount: 0,
      messageCount: 0,
      redPacketCount: 0,
      pendingContactRequestCount: 0,
      friendshipCount: 0,
      blacklistCount: 0,
    },
    filters: createDefaultFilters(),
    pagination: createDefaultPagination(),
    data: {
      conversations: [],
      records: [],
      redPackets: [],
      contactRequests: [],
      friendships: [],
      blacklist: [],
    },
    conversationDetail: null,
    selectedConversationNo: '',
    conversationDetailVisible: false,
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
      conversations: {
        keyword: '',
        userId: '',
        pageNo: '1',
        pageSize: '20',
      },
      records: {
        conversationNo: '',
        messageType: '',
        senderUserId: '',
        receiverUserId: '',
        pageNo: '1',
        pageSize: '20',
      },
      redPackets: {
        redPacketNo: '',
        senderUserId: '',
        receiverUserId: '',
        status: '',
        pageNo: '1',
        pageSize: '20',
      },
      contactRequests: {
        requestNo: '',
        requesterUserId: '',
        targetUserId: '',
        status: '',
        pageNo: '1',
        pageSize: '20',
      },
      friendships: {
        ownerUserId: '',
        friendUserId: '',
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
      conversations: { hasNextPage: false },
      records: { hasNextPage: false },
      redPackets: { hasNextPage: false },
      contactRequests: { hasNextPage: false },
      friendships: { hasNextPage: false },
      blacklist: { hasNextPage: false },
    };
  }

  function resolveFocusSection(path) {
    return PATH_SECTION_MAP[String(path || MESSAGE_ROOT_PATH)] || 'messageConversationSection';
  }

  function resolveActiveSection() {
    if (state.selectedPath === '/admin/messages/records') {
      return 'records';
    }
    if (state.selectedPath === '/admin/messages/red-packets') {
      return 'redPackets';
    }
    if (state.selectedPath === '/admin/messages/contact-requests') {
      return 'contactRequests';
    }
    if (state.selectedPath === '/admin/messages/friends') {
      return 'friendships';
    }
    if (state.selectedPath === '/admin/messages/blacklist') {
      return 'blacklist';
    }
    return 'conversations';
  }

  function buildPageMeta() {
    var section = resolveActiveSection();
    if (section === 'records') {
      return { section: section, title: '消息记录', loadingText: '正在加载消息记录...' };
    }
    if (section === 'redPackets') {
      return { section: section, title: '红包记录', loadingText: '正在加载红包记录...' };
    }
    if (section === 'contactRequests') {
      return { section: section, title: '好友申请', loadingText: '正在加载好友申请...' };
    }
    if (section === 'friendships') {
      return { section: section, title: '好友关系', loadingText: '正在加载好友关系...' };
    }
    if (section === 'blacklist') {
      return { section: section, title: '黑名单', loadingText: '正在加载黑名单记录...' };
    }
    return { section: 'conversations', title: '会话管理', loadingText: '正在加载会话数据...' };
  }

  async function loadAllData(showLoadedNotice) {
    state.loading = true;
    render();
    var sectionTasks = {
      conversations: loadConversations,
      records: loadRecords,
      redPackets: loadRedPackets,
      contactRequests: loadContactRequests,
      friendships: loadFriendships,
      blacklist: loadBlacklist,
    };
    var pageMeta = buildPageMeta();
    var activeTask = sectionTasks[pageMeta.section] || loadConversations;
    var results = await Promise.allSettled([loadOverview(), activeTask()]);
    state.loading = false;
    state.notice = buildNotice(results, showLoadedNotice ? pageMeta.title + '已加载' : pageMeta.title + '已刷新');
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

  async function loadOverview() {
    state.overview = await request('/api/admin/messages/overview');
  }

  async function loadConversations() {
    var pageSize = toPositiveIntOrDefault(state.filters.conversations.pageSize, 20);
    state.data.conversations = await request('/api/admin/messages/conversations', state.filters.conversations);
    state.pagination.conversations.hasNextPage = state.data.conversations.length >= pageSize;
    if (state.selectedConversationNo) {
      var stillExists = state.data.conversations.some(function (item) {
        return item.conversationNo === state.selectedConversationNo;
      });
      if (!stillExists) {
        state.selectedConversationNo = '';
        state.conversationDetail = null;
        state.conversationDetailVisible = false;
      }
    }
  }

  async function loadRecords() {
    var pageSize = toPositiveIntOrDefault(state.filters.records.pageSize, 20);
    state.data.records = await request('/api/admin/messages/records', state.filters.records);
    state.pagination.records.hasNextPage = state.data.records.length >= pageSize;
  }

  async function loadRedPackets() {
    var pageSize = toPositiveIntOrDefault(state.filters.redPackets.pageSize, 20);
    state.data.redPackets = await request('/api/admin/messages/red-packets', state.filters.redPackets);
    state.pagination.redPackets.hasNextPage = state.data.redPackets.length >= pageSize;
  }

  async function loadContactRequests() {
    var pageSize = toPositiveIntOrDefault(state.filters.contactRequests.pageSize, 20);
    state.data.contactRequests = await request('/api/admin/messages/contact-requests', state.filters.contactRequests);
    state.pagination.contactRequests.hasNextPage = state.data.contactRequests.length >= pageSize;
  }

  async function loadFriendships() {
    var pageSize = toPositiveIntOrDefault(state.filters.friendships.pageSize, 20);
    state.data.friendships = await request('/api/admin/messages/friends', state.filters.friendships);
    state.pagination.friendships.hasNextPage = state.data.friendships.length >= pageSize;
  }

  async function loadBlacklist() {
    var pageSize = toPositiveIntOrDefault(state.filters.blacklist.pageSize, 20);
    state.data.blacklist = await request('/api/admin/messages/blacklist', state.filters.blacklist);
    state.pagination.blacklist.hasNextPage = state.data.blacklist.length >= pageSize;
  }

  async function request(path, query) {
    var qs = common.serializeQuery(query);
    return common.requestJson(path + (qs ? '?' + qs : ''), {
      method: 'GET',
      headers: common.buildAdminHeaders(state.session),
    });
  }

  async function loadConversationDetail(conversationNo) {
    if (!conversationNo) {
      return;
    }
    state.notice = '正在加载会话详情...';
    render();
    try {
      state.conversationDetail = await common.requestJson('/api/admin/messages/conversations/' + encodeURIComponent(conversationNo), {
        method: 'GET',
        headers: common.buildAdminHeaders(state.session),
      });
      state.selectedConversationNo = conversationNo;
      state.conversationDetailVisible = true;
      state.notice = '会话详情已加载';
    } catch (error) {
      state.notice = error && error.message ? error.message : '会话详情加载失败';
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
      updateFilterState(section);
      querySingleSection(section);
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

    if (action === 'load-conversation-detail') {
      loadConversationDetail(actionElement.getAttribute('data-conversation-no'));
      return;
    }

    if (action === 'hide-conversation-detail') {
      state.conversationDetailVisible = false;
      render();
      return;
    }
  }

  async function querySingleSection(section) {
    var taskMap = {
      conversations: loadConversations,
      records: loadRecords,
      redPackets: loadRedPackets,
      contactRequests: loadContactRequests,
      friendships: loadFriendships,
      blacklist: loadBlacklist,
    };
    var task = taskMap[section];
    if (!task) {
      return;
    }
    state.notice = '正在查询...';
    normalizeSectionPagination(section);
    render();
    try {
      await task();
      state.notice = '查询完成';
    } catch (error) {
      if (state.pagination[section]) {
        state.pagination[section].hasNextPage = false;
      }
      state.notice = error && error.message ? error.message : '查询失败';
    }
    render();
    focusSectionByName(section);
  }

  function querySectionByOffset(section, offset) {
    if (!section || !state.filters[section]) {
      return;
    }
    updateFilterState(section);
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
    querySingleSection(section);
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
    if (section === 'conversations') {
      state.selectedConversationNo = '';
      state.conversationDetail = null;
      state.conversationDetailVisible = false;
    }
    render();
    querySingleSection(section);
  }

  function focusSectionByName(section) {
    var mapping = {
      conversations: 'messageConversationSection',
      records: 'messageRecordSection',
      redPackets: 'messageRedPacketSection',
      contactRequests: 'messageContactRequestSection',
      friendships: 'messageFriendshipSection',
      blacklist: 'messageBlacklistSection',
    };
    var targetId = mapping[section];
    if (targetId) {
      state.focusSectionId = targetId;
      state.pendingFocus = true;
      render();
    }
  }

  function updateFilterState(section) {
    if (section === 'conversations') {
      state.filters.conversations.keyword = readInputValue('messageConversationKeyword');
      state.filters.conversations.userId = readInputValue('messageConversationUserId');
      state.filters.conversations.pageNo = readInputValue('messageConversationPageNo') || '1';
      state.filters.conversations.pageSize = readInputValue('messageConversationPageSize') || '20';
      return;
    }
    if (section === 'records') {
      state.filters.records.conversationNo = readInputValue('messageRecordConversationNo');
      state.filters.records.messageType = readInputValue('messageRecordMessageType');
      state.filters.records.senderUserId = readInputValue('messageRecordSenderUserId');
      state.filters.records.receiverUserId = readInputValue('messageRecordReceiverUserId');
      state.filters.records.pageNo = readInputValue('messageRecordPageNo') || '1';
      state.filters.records.pageSize = readInputValue('messageRecordPageSize') || '20';
      return;
    }
    if (section === 'redPackets') {
      state.filters.redPackets.redPacketNo = readInputValue('messageRedPacketNo');
      state.filters.redPackets.senderUserId = readInputValue('messageRedPacketSenderUserId');
      state.filters.redPackets.receiverUserId = readInputValue('messageRedPacketReceiverUserId');
      state.filters.redPackets.status = readInputValue('messageRedPacketStatus');
      state.filters.redPackets.pageNo = readInputValue('messageRedPacketPageNo') || '1';
      state.filters.redPackets.pageSize = readInputValue('messageRedPacketPageSize') || '20';
      return;
    }
    if (section === 'contactRequests') {
      state.filters.contactRequests.requestNo = readInputValue('messageContactRequestNo');
      state.filters.contactRequests.requesterUserId = readInputValue('messageContactRequesterUserId');
      state.filters.contactRequests.targetUserId = readInputValue('messageContactTargetUserId');
      state.filters.contactRequests.status = readInputValue('messageContactRequestStatus');
      state.filters.contactRequests.pageNo = readInputValue('messageContactRequestPageNo') || '1';
      state.filters.contactRequests.pageSize = readInputValue('messageContactRequestPageSize') || '20';
      return;
    }
    if (section === 'friendships') {
      state.filters.friendships.ownerUserId = readInputValue('messageFriendshipOwnerUserId');
      state.filters.friendships.friendUserId = readInputValue('messageFriendshipFriendUserId');
      state.filters.friendships.pageNo = readInputValue('messageFriendshipPageNo') || '1';
      state.filters.friendships.pageSize = readInputValue('messageFriendshipPageSize') || '20';
      return;
    }
    if (section === 'blacklist') {
      state.filters.blacklist.ownerUserId = readInputValue('messageBlacklistOwnerUserId');
      state.filters.blacklist.blockedUserId = readInputValue('messageBlacklistBlockedUserId');
      state.filters.blacklist.pageNo = readInputValue('messageBlacklistPageNo') || '1';
      state.filters.blacklist.pageSize = readInputValue('messageBlacklistPageSize') || '20';
    }
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
    var pageMeta = buildPageMeta();
    common.setDocumentTitle(pageMeta.title);
    if (!state.session || !state.session.adminId) {
      root.innerHTML = common.localizeHtml(common.renderAuthRequired('请先登录管理后台', '当前消息中心无法读取管理员会话，请返回后台登录后重试。'));
      FRAME_NOTIFIER.schedule();
      return;
    }

    if (state.loading && !hasDataForActiveSection()) {
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

  function hasDataForActiveSection() {
    var section = resolveActiveSection();
    if (section === 'records') {
      return !!state.data.records.length;
    }
    if (section === 'redPackets') {
      return !!state.data.redPackets.length;
    }
    if (section === 'contactRequests') {
      return !!state.data.contactRequests.length;
    }
    if (section === 'friendships') {
      return !!state.data.friendships.length;
    }
    if (section === 'blacklist') {
      return !!state.data.blacklist.length;
    }
    return !!state.data.conversations.length;
  }

  function renderPage() {
    var pageMeta = buildPageMeta();
    var html = '';
    html += renderOverview();
    if (state.notice) {
      html += '<section class="manager-panel"><span class="admin-embedded-note">' + common.escapeHtml(state.notice) + '</span></section>';
    }
    if (pageMeta.section === 'records') {
      html += renderRecordSection();
    } else if (pageMeta.section === 'redPackets') {
      html += renderRedPacketSection();
    } else if (pageMeta.section === 'contactRequests') {
      html += renderContactRequestSection();
    } else if (pageMeta.section === 'friendships') {
      html += renderFriendshipSection();
    } else if (pageMeta.section === 'blacklist') {
      html += renderBlacklistSection();
    } else {
      html += renderConversationSection();
      html += renderConversationDetailModal();
    }
    return html;
  }

  function renderOverview() {
    var cards = [
      { label: '会话数', value: state.overview.conversationCount },
      { label: '消息数', value: state.overview.messageCount },
      { label: '红包单', value: state.overview.redPacketCount },
      { label: '待处理申请', value: state.overview.pendingContactRequestCount },
      { label: '好友关系', value: state.overview.friendshipCount },
      { label: '黑名单', value: state.overview.blacklistCount },
    ];
    return '<section class="manager-panel admin-embedded-summary-panel">' +
      '<div class="admin-embedded-summary-strip">' +
      '<div class="admin-embedded-summary-title">资源统计</div>' +
      '<div class="admin-embedded-summary-list">' +
      cards.map(function (card) {
        return '<div class="admin-embedded-summary-item"><span class="admin-embedded-summary-label">' + common.escapeHtml(card.label) + '</span><span class="admin-embedded-summary-value">' + common.escapeHtml(card.value) + '</span></div>';
      }).join('') +
      '</div>' +
      '</div>' +
      '</section>';
  }

  function renderConversationSection() {
    var rows = state.data.conversations || [];
    var filters = state.filters.conversations;
    var html = '';
    html += '<section id="messageConversationSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('会话管理', '支持按会话号、业务键、对端用户回溯消息链路。', rows.length);
    html += renderFilterGrid([
      renderField('关键字', '<input id="messageConversationKeyword" class="form-input" value="' + common.escapeHtml(filters.keyword) + '" placeholder="会话号 / bizKey / 消息摘要" />'),
      renderField('用户ID', '<input id="messageConversationUserId" class="form-input" value="' + common.escapeHtml(filters.userId) + '" placeholder="按会话成员筛选" />'),
      renderField('页码', '<input id="messageConversationPageNo" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />'),
      renderField('每页条数', '<input id="messageConversationPageSize" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />'),
    ], 'conversations');
    html += '<div class="admin-embedded-table-wrap">' + renderConversationTable(rows) + '</div>';
    html += '</section>';
    return html;
  }

  function renderConversationTable(rows) {
    var html = '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>会话号</th><th>类型</th><th>最近消息</th><th>最后活跃</th><th>成员数</th><th>对端</th><th>操作</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="7" class="manager-coupon-empty-row">暂无会话数据</td></tr>';
    } else {
      html += rows.map(function (item) {
        var isSelected = item.conversationNo === state.selectedConversationNo ? ' class="manager-table-row-selected"' : '';
        return '<tr' + isSelected + '>' +
          '<td><span class="admin-embedded-mono">' + common.escapeHtml(item.conversationNo) + '</span></td>' +
          '<td>' + common.escapeHtml(common.formatConversationType(item.conversationType)) + '</td>' +
          '<td>' + common.escapeHtml(item.lastMessagePreview || '-') + '</td>' +
          '<td>' + common.escapeHtml(common.formatDateTime(item.lastMessageAt)) + '</td>' +
          '<td>' + common.escapeHtml(item.memberCount) + ' / 未读 ' + common.escapeHtml(item.unreadCount == null ? '-' : item.unreadCount) + '</td>' +
          '<td>' + common.escapeHtml(buildPeerText(item.peerDisplayName, item.peerAipayUid)) + '</td>' +
          '<td class="admin-embedded-table-actions"><button class="manager-btn" type="button" data-action="load-conversation-detail" data-conversation-no="' + common.escapeHtml(item.conversationNo) + '">详情</button></td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function renderConversationDetail() {
    var detail = state.conversationDetail;
    var html = '';
    if (!detail || !detail.conversation) {
      return '<p class="manager-placeholder-tip">点击左侧会话可查看成员与最近消息。</p>';
    }
    html += common.renderDetailGrid([
      { label: '会话号', value: detail.conversation.conversationNo },
      { label: '会话类型', value: common.formatConversationType(detail.conversation.conversationType) },
      { label: '业务键', value: detail.conversation.bizKey },
      { label: '成员数', value: detail.conversation.memberCount },
      { label: '最近消息', value: detail.conversation.lastMessagePreview },
      { label: '最后活跃', value: common.formatDateTime(detail.conversation.lastMessageAt) },
    ]);
    html += '<div class="admin-embedded-section-head" style="margin-top:12px;"><div><h4 class="manager-panel-title">成员</h4></div></div>';
    html += renderMemberTable(detail.members || []);
    html += '<div class="admin-embedded-section-head" style="margin-top:12px;"><div><h4 class="manager-panel-title">最近消息</h4></div></div>';
    html += renderRecentMessageTable(detail.recentMessages || []);
    return html;
  }

  function renderConversationDetailModal() {
    if (!state.conversationDetailVisible) {
      return '';
    }
    var html = '';
    html += '<div class="manager-modal-layer">';
    html += '<div class="manager-modal-mask" data-action="hide-conversation-detail"></div>';
    html += '<section class="manager-modal-panel manager-user-detail-modal">';
    html += '<div class="manager-modal-head">';
    html += '<div><h3 class="manager-panel-title">会话详情</h3><div class="admin-embedded-section-desc">详情查看改为弹窗处理，主页面仅保留会话列表。</div></div>';
    html += '<button class="manager-modal-close" type="button" data-action="hide-conversation-detail" aria-label="关闭">×</button>';
    html += '</div>';
    html += renderConversationDetail();
    html += '</section>';
    html += '</div>';
    return html;
  }

  function renderMemberTable(rows) {
    var html = '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>用户</th><th>对端</th><th>未读</th><th>最近已读</th><th>开关</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="5" class="manager-coupon-empty-row">暂无成员数据</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td>' + common.escapeHtml(buildPeerText(item.userDisplayName, item.userAipayUid)) + '</td>' +
          '<td>' + common.escapeHtml(buildPeerText(item.peerDisplayName, item.peerAipayUid)) + '</td>' +
          '<td>' + common.escapeHtml(item.unreadCount == null ? '-' : item.unreadCount) + '</td>' +
          '<td>' + common.escapeHtml(item.lastReadMessageId || '-') + '<br /><span class="admin-embedded-muted">' + common.escapeHtml(common.formatDateTime(item.lastReadAt)) + '</span></td>' +
          '<td>' + common.escapeHtml('免打扰 ' + common.formatBooleanText(item.muteFlag) + ' / 置顶 ' + common.formatBooleanText(item.pinFlag)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function renderRecentMessageTable(rows) {
    var html = '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>消息ID</th><th>发送方</th><th>接收方</th><th>类型</th><th>内容</th><th>时间</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="6" class="manager-coupon-empty-row">暂无最近消息</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td><span class="admin-embedded-mono">' + common.escapeHtml(item.messageId) + '</span></td>' +
          '<td>' + common.escapeHtml(item.senderDisplayName || '-') + '</td>' +
          '<td>' + common.escapeHtml(item.receiverDisplayName || '-') + '</td>' +
          '<td>' + common.escapeHtml(common.formatMessageType(item.messageType)) + '</td>' +
          '<td>' + common.escapeHtml(item.contentText || item.extPayload || '-') + '</td>' +
          '<td>' + common.escapeHtml(common.formatDateTime(item.createdAt)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function renderRecordSection() {
    var rows = state.data.records || [];
    var filters = state.filters.records;
    var html = '';
    html += '<section id="messageRecordSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('消息记录', '支持按会话、消息类型、收发双方排查消息状态与交易挂载。', rows.length);
    html += renderFilterGrid([
      renderField('会话号', '<input id="messageRecordConversationNo" class="form-input" value="' + common.escapeHtml(filters.conversationNo) + '" placeholder="conversationNo" />'),
      renderField('消息类型', '<input id="messageRecordMessageType" class="form-input" value="' + common.escapeHtml(filters.messageType) + '" placeholder="如 TEXT（文本）/ TRANSFER（转账）/ RED_PACKET（红包）" />'),
      renderField('发送方', '<input id="messageRecordSenderUserId" class="form-input" value="' + common.escapeHtml(filters.senderUserId) + '" placeholder="senderUserId" />'),
      renderField('接收方', '<input id="messageRecordReceiverUserId" class="form-input" value="' + common.escapeHtml(filters.receiverUserId) + '" placeholder="receiverUserId" />'),
      renderField('页码', '<input id="messageRecordPageNo" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />'),
      renderField('每页条数', '<input id="messageRecordPageSize" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />'),
    ], 'records');
    html += renderRecordTable(rows);
    html += '</section>';
    return html;
  }

  function renderRecordTable(rows) {
    var html = '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>消息ID</th><th>会话号</th><th>收发双方</th><th>类型</th><th>金额/交易</th><th>状态</th><th>发送时间</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="7" class="manager-coupon-empty-row">暂无消息记录</td></tr>';
    } else {
      html += rows.map(function (item) {
        var statusHtml = common.buildStatusBadge(item.messageStatus || '-', common.formatMessageStatus(item.messageStatus));
        return '<tr>' +
          '<td><span class="admin-embedded-mono">' + common.escapeHtml(item.messageId) + '</span></td>' +
          '<td><span class="admin-embedded-mono">' + common.escapeHtml(item.conversationNo) + '</span></td>' +
          '<td>' + common.escapeHtml((item.senderDisplayName || '-') + ' → ' + (item.receiverDisplayName || '-')) + '</td>' +
          '<td>' + common.escapeHtml(common.formatMessageType(item.messageType)) + '<br /><span class="admin-embedded-muted">' + common.escapeHtml(item.contentText || '-') + '</span></td>' +
          '<td>' + common.escapeHtml(common.formatMoney(item.amount)) + '<br /><span class="admin-embedded-muted">' + common.escapeHtml(item.tradeOrderNo || '-') + '</span></td>' +
          '<td>' + statusHtml + '</td>' +
          '<td>' + common.escapeHtml(common.formatDateTime(item.createdAt)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function renderRedPacketSection() {
    var rows = state.data.redPackets || [];
    var filters = state.filters.redPackets;
    var html = '';
    html += '<section id="messageRedPacketSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('红包记录', '查看红包消息与资金单号挂载情况。', rows.length);
    html += renderFilterGrid([
      renderField('红包单号', '<input id="messageRedPacketNo" class="form-input" value="' + common.escapeHtml(filters.redPacketNo) + '" placeholder="redPacketNo" />'),
      renderField('发送方', '<input id="messageRedPacketSenderUserId" class="form-input" value="' + common.escapeHtml(filters.senderUserId) + '" placeholder="senderUserId" />'),
      renderField('接收方', '<input id="messageRedPacketReceiverUserId" class="form-input" value="' + common.escapeHtml(filters.receiverUserId) + '" placeholder="receiverUserId" />'),
      renderField('状态', '<input id="messageRedPacketStatus" class="form-input" value="' + common.escapeHtml(filters.status) + '" placeholder="如 PENDING_CLAIM（待领取）/ CLAIMED（已领取）" />'),
      renderField('页码', '<input id="messageRedPacketPageNo" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />'),
      renderField('每页条数', '<input id="messageRedPacketPageSize" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />'),
    ], 'redPackets');
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>红包单号</th><th>会话号</th><th>发送/接收</th><th>金额</th><th>支付方式</th><th>状态</th><th>资金链路</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="7" class="manager-coupon-empty-row">暂无红包记录</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td><span class="admin-embedded-mono">' + common.escapeHtml(item.redPacketNo) + '</span></td>' +
          '<td><span class="admin-embedded-mono">' + common.escapeHtml(item.conversationNo) + '</span></td>' +
          '<td>' + common.escapeHtml((item.senderDisplayName || '-') + ' → ' + (item.receiverDisplayName || '-')) + '</td>' +
          '<td>' + common.escapeHtml(common.formatMoney(item.amount)) + '</td>' +
          '<td>' + common.escapeHtml(common.formatPaymentMethod(item.paymentMethod)) + '</td>' +
          '<td>' + common.buildStatusBadge(item.status || '-', common.formatRedPacketStatus(item.status)) + '<br /><span class="admin-embedded-muted">' + common.escapeHtml(common.formatDateTime(item.claimedAt)) + '</span></td>' +
          '<td>' + common.escapeHtml(item.fundingTradeNo || '-') + '<br /><span class="admin-embedded-muted">' + common.escapeHtml(item.claimTradeNo || '-') + '</span></td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderContactRequestSection() {
    var rows = state.data.contactRequests || [];
    var filters = state.filters.contactRequests;
    var html = '';
    html += '<section id="messageContactRequestSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('好友申请', '查看好友申请状态与处理时点。', rows.length);
    html += renderFilterGrid([
      renderField('申请单号', '<input id="messageContactRequestNo" class="form-input" value="' + common.escapeHtml(filters.requestNo) + '" placeholder="requestNo" />'),
      renderField('申请人', '<input id="messageContactRequesterUserId" class="form-input" value="' + common.escapeHtml(filters.requesterUserId) + '" placeholder="requesterUserId" />'),
      renderField('目标用户', '<input id="messageContactTargetUserId" class="form-input" value="' + common.escapeHtml(filters.targetUserId) + '" placeholder="targetUserId" />'),
      renderField('状态', '<input id="messageContactRequestStatus" class="form-input" value="' + common.escapeHtml(filters.status) + '" placeholder="如 PENDING（待处理）/ ACCEPTED（已通过）" />'),
      renderField('页码', '<input id="messageContactRequestPageNo" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />'),
      renderField('每页条数', '<input id="messageContactRequestPageSize" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />'),
    ], 'contactRequests');
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>申请单号</th><th>申请人</th><th>目标用户</th><th>申请文案</th><th>状态</th><th>处理信息</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="6" class="manager-coupon-empty-row">暂无好友申请</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td><span class="admin-embedded-mono">' + common.escapeHtml(item.requestNo) + '</span></td>' +
          '<td>' + common.escapeHtml(item.requesterDisplayName || '-') + ' / ' + common.escapeHtml(item.requesterUserId || '-') + '</td>' +
          '<td>' + common.escapeHtml(item.targetDisplayName || '-') + ' / ' + common.escapeHtml(item.targetUserId || '-') + '</td>' +
          '<td>' + common.escapeHtml(item.applyMessage || '-') + '</td>' +
          '<td>' + common.buildStatusBadge(item.status || '-', common.formatContactRequestStatus(item.status)) + '</td>' +
          '<td>' + common.escapeHtml(item.handledByUserId || '-') + '<br /><span class="admin-embedded-muted">' + common.escapeHtml(common.formatDateTime(item.handledAt || item.createdAt)) + '</span></td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderFriendshipSection() {
    var rows = state.data.friendships || [];
    var filters = state.filters.friendships;
    var html = '';
    html += '<section id="messageFriendshipSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('好友关系', '用于核对双向好友是否正确落库。', rows.length);
    html += renderFilterGrid([
      renderField('主用户', '<input id="messageFriendshipOwnerUserId" class="form-input" value="' + common.escapeHtml(filters.ownerUserId) + '" placeholder="ownerUserId" />'),
      renderField('好友用户', '<input id="messageFriendshipFriendUserId" class="form-input" value="' + common.escapeHtml(filters.friendUserId) + '" placeholder="friendUserId" />'),
      renderField('页码', '<input id="messageFriendshipPageNo" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />'),
      renderField('每页条数', '<input id="messageFriendshipPageSize" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />'),
    ], 'friendships');
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>主用户</th><th>好友</th><th>备注</th><th>来源申请</th><th>更新时间</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="5" class="manager-coupon-empty-row">暂无好友关系</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td>' + common.escapeHtml(item.ownerDisplayName || '-') + ' / ' + common.escapeHtml(item.ownerUserId || '-') + '</td>' +
          '<td>' + common.escapeHtml(item.friendDisplayName || '-') + ' / ' + common.escapeHtml(item.friendUserId || '-') + '</td>' +
          '<td>' + common.escapeHtml(item.remark || '-') + '</td>' +
          '<td>' + common.escapeHtml(item.sourceRequestNo || '-') + '</td>' +
          '<td>' + common.escapeHtml(common.formatDateTime(item.updatedAt)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderBlacklistSection() {
    var rows = state.data.blacklist || [];
    var filters = state.filters.blacklist;
    var html = '';
    html += '<section id="messageBlacklistSection" class="manager-panel admin-embedded-section" tabindex="-1">';
    html += renderSectionHead('黑名单', '排查联系人黑名单与风控黑名单联动前的数据基础。', rows.length);
    html += renderFilterGrid([
      renderField('主用户', '<input id="messageBlacklistOwnerUserId" class="form-input" value="' + common.escapeHtml(filters.ownerUserId) + '" placeholder="ownerUserId" />'),
      renderField('被拉黑用户', '<input id="messageBlacklistBlockedUserId" class="form-input" value="' + common.escapeHtml(filters.blockedUserId) + '" placeholder="blockedUserId" />'),
      renderField('页码', '<input id="messageBlacklistPageNo" class="form-input" value="' + common.escapeHtml(filters.pageNo) + '" placeholder="1" />'),
      renderField('每页条数', '<input id="messageBlacklistPageSize" class="form-input" value="' + common.escapeHtml(filters.pageSize) + '" placeholder="20" />'),
    ], 'blacklist');
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>主用户</th><th>被拉黑用户</th><th>原因</th><th>更新时间</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="4" class="manager-coupon-empty-row">暂无黑名单记录</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td>' + common.escapeHtml(item.ownerDisplayName || '-') + ' / ' + common.escapeHtml(item.ownerUserId || '-') + '</td>' +
          '<td>' + common.escapeHtml(item.blockedDisplayName || '-') + ' / ' + common.escapeHtml(item.blockedUserId || '-') + '</td>' +
          '<td>' + common.escapeHtml(item.reason || '-') + '</td>' +
          '<td>' + common.escapeHtml(common.formatDateTime(item.updatedAt)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
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

  function buildPeerText(displayName, aipayUid) {
    if (displayName && aipayUid) {
      return displayName + ' / ' + aipayUid;
    }
    return displayName || aipayUid || '-';
  }
})();
