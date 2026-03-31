(function () {
  var common = window.OpenAiPayAdminEmbedded || null;
  var root = document.getElementById('audienceConsoleApp');
  var FRAME_ID = 'managerAudienceFrame';
  var AUDIENCE_ROOT_PATH = '/admin/deliver/audience';
  var FRAME_NOTIFIER = common ? common.createFrameHeightNotifier(FRAME_ID, 980) : null;

  if (!common || !root) {
    return;
  }

  var AUDIENCE_TAB_ITEMS = [
    { key: 'segment', label: '人群定义' },
    { key: 'rule', label: '人群规则' },
    { key: 'tag', label: '标签定义' },
    { key: 'user', label: '用户标签调试' },
  ];
  var TAG_TYPE_OPTIONS = ['ENUM', 'NUMBER', 'BOOLEAN', 'TEXT'];
  var TAG_TYPE_LABEL_MAP = {
    ENUM: '枚举',
    NUMBER: '数值',
    BOOLEAN: '布尔',
    TEXT: '文本',
  };
  var SEGMENT_STATUS_OPTIONS = ['DRAFT', 'PUBLISHED', 'OFFLINE'];
  var SEGMENT_STATUS_LABEL_MAP = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    OFFLINE: '已下线',
  };
  var RULE_OPERATOR_OPTIONS = ['EQ', 'NEQ', 'IN', 'NOT_IN', 'GT', 'GTE', 'LT', 'LTE', 'BETWEEN', 'EXISTS', 'NOT_EXISTS', 'CONTAINS'];
  var RULE_OPERATOR_LABEL_MAP = {
    EQ: '等于',
    NEQ: '不等于',
    IN: '属于',
    NOT_IN: '不属于',
    GT: '大于',
    GTE: '大于等于',
    LT: '小于',
    LTE: '小于等于',
    BETWEEN: '区间',
    EXISTS: '存在',
    NOT_EXISTS: '不存在',
    CONTAINS: '包含',
  };
  var RULE_RELATION_OPTIONS = ['INCLUDE', 'EXCLUDE'];
  var RULE_RELATION_LABEL_MAP = {
    INCLUDE: '命中条件',
    EXCLUDE: '排除条件',
  };

  var state = {
    session: common.loadSession(),
    loading: false,
    initialized: false,
    notice: '',
    selectedPath: common.resolveSelectedPath(AUDIENCE_ROOT_PATH),
    activeTab: 'segment',
    selectedSegmentCode: '',
    queryUserId: '',
    data: {
      tagDefinitions: [],
      segments: [],
      segmentRules: [],
      userTags: [],
      userTagTokens: [],
      matchedSegments: [],
    },
  };

  root.addEventListener('click', onClick);
  root.addEventListener('change', onChange);
  window.addEventListener('resize', scheduleHeightSync);
  start();

  async function start() {
    if (!state.session || !state.session.adminId) {
      render();
      return;
    }
    await loadAll(true);
  }

  async function loadAll(showNotice) {
    state.loading = true;
    render();
    try {
      var payloads = await Promise.all([
        requestGet('/api/admin/audience/tag-definitions'),
        requestGet('/api/admin/audience/segments'),
      ]);
      state.data.tagDefinitions = toArray(payloads[0]);
      state.data.segments = toArray(payloads[1]);
      ensureSelectedSegmentCode();
      await loadSegmentRules();
      if (state.queryUserId) {
        await loadUserContext(state.queryUserId);
      }
      if (showNotice) {
        state.notice = '人群配置已加载';
      }
    } catch (error) {
      state.notice = error && error.message ? error.message : '人群配置加载失败';
    } finally {
      state.loading = false;
      state.initialized = true;
      render();
    }
  }

  async function loadSegmentRules() {
    if (!state.selectedSegmentCode) {
      state.data.segmentRules = [];
      return;
    }
    state.data.segmentRules = toArray(await requestGet('/api/admin/audience/segment-rules', { segmentCode: state.selectedSegmentCode }));
  }

  async function loadUserContext(userId) {
    var normalizedUserId = normalizePositiveId(userId);
    if (!normalizedUserId) {
      state.data.userTags = [];
      state.data.userTagTokens = [];
      state.data.matchedSegments = [];
      return;
    }
    state.queryUserId = normalizedUserId;
    var payloads = await Promise.all([
      requestGet('/api/admin/audience/user-tags', { userId: normalizedUserId }),
      requestGet('/api/admin/audience/user-tag-tokens', { userId: normalizedUserId }),
      requestGet('/api/admin/audience/segment-matches', { userId: normalizedUserId }),
    ]);
    state.data.userTags = toArray(payloads[0]);
    state.data.userTagTokens = toArray(payloads[1]);
    state.data.matchedSegments = toArray(payloads[2]);
  }

  async function saveTagDefinition() {
    var payload = {
      tagCode: requireValue(readValue('audTagCode'), '标签编码'),
      tagName: requireValue(readValue('audTagName'), '标签名称'),
      tagType: readValue('audTagType').toUpperCase() || 'ENUM',
      valueScope: optionalValue(readValue('audTagValueScope')),
      description: optionalValue(readValue('audTagDescription')),
      enabled: parseOptionalBoolean(readValue('audTagEnabled'), true),
    };
    await requestPost('/api/admin/audience/tag-definitions', payload);
    state.notice = '标签定义保存成功';
    await loadAll(false);
  }

  async function saveSegment() {
    var payload = {
      segmentCode: requireValue(readValue('audSegmentCode'), '人群编码'),
      segmentName: requireValue(readValue('audSegmentName'), '人群名称'),
      description: optionalValue(readValue('audSegmentDescription')),
      sceneCode: optionalValue(readValue('audSegmentSceneCode')),
      status: readValue('audSegmentStatus').toUpperCase() || 'DRAFT',
    };
    await requestPost('/api/admin/audience/segments', payload);
    state.notice = '人群定义保存成功';
    await loadAll(false);
  }

  async function saveSegmentRule() {
    var segmentCode = requireValue(readValue('audRuleSegmentCode'), '人群编码');
    var payload = {
      ruleCode: requireValue(readValue('audRuleCode'), '规则编码'),
      segmentCode: segmentCode,
      tagCode: requireValue(readValue('audRuleTagCode'), '标签编码'),
      operator: readValue('audRuleOperator').toUpperCase() || 'EQ',
      targetValue: optionalValue(readValue('audRuleTargetValue')),
      relation: readValue('audRuleRelation').toUpperCase() || 'INCLUDE',
      enabled: parseOptionalBoolean(readValue('audRuleEnabled'), true),
    };
    await requestPost('/api/admin/audience/segment-rules', payload);
    state.selectedSegmentCode = segmentCode;
    state.notice = '人群规则保存成功';
    await loadSegmentRules();
    render();
  }

  async function saveUserTag() {
    var userId = requirePositiveId(readValue('audUserTagUserId'), '用户ID');
    var payload = {
      userId: Number(userId),
      tagCode: requireValue(readValue('audUserTagCode'), '标签编码'),
      tagValue: requireValue(readValue('audUserTagValue'), '标签值'),
      source: optionalValue(readValue('audUserTagSource')),
      valueUpdatedAt: null,
    };
    await requestPost('/api/admin/audience/user-tags', payload);
    state.notice = '用户标签保存成功';
    await loadUserContext(userId);
    render();
  }

  async function queryUserTags() {
    var userId = requirePositiveId(readValue('audQueryUserId'), '用户ID');
    state.loading = true;
    render();
    try {
      await loadUserContext(userId);
      state.notice = '用户标签数据已刷新';
    } catch (error) {
      state.notice = error && error.message ? error.message : '用户标签数据加载失败';
    } finally {
      state.loading = false;
      state.initialized = true;
      render();
    }
  }

  function onChange(event) {
    var target = event.target;
    if (!target) {
      return;
    }
    if (target.id === 'audRuleSegmentFilter') {
      state.selectedSegmentCode = String(target.value || '').trim();
      state.loading = true;
      render();
      loadSegmentRules().then(function () {
        state.loading = false;
        render();
      }).catch(function (error) {
        state.loading = false;
        state.notice = error && error.message ? error.message : '规则数据加载失败';
        render();
      });
      return;
    }
    if (target.id === 'audRuleSegmentCode') {
      state.selectedSegmentCode = String(target.value || '').trim();
      render();
    }
  }

  async function onClick(event) {
    var actionElement = event.target.closest('[data-action]');
    if (!actionElement) {
      return;
    }
    var action = String(actionElement.getAttribute('data-action') || '').trim();
    if (!action) {
      return;
    }
    try {
      if (action === 'switch-audience-tab') {
        var tabKey = String(actionElement.getAttribute('data-tab') || '').trim();
        if (AUDIENCE_TAB_ITEMS.some(function (item) { return item.key === tabKey; })) {
          state.activeTab = tabKey;
          render();
        }
        return;
      }
      if (action === 'reload-audience') {
        await loadAll(true);
        return;
      }
      if (action === 'save-tag-definition') {
        await saveTagDefinition();
        return;
      }
      if (action === 'save-segment') {
        await saveSegment();
        return;
      }
      if (action === 'save-segment-rule') {
        await saveSegmentRule();
        return;
      }
      if (action === 'query-user-tags') {
        await queryUserTags();
        return;
      }
      if (action === 'save-user-tag') {
        await saveUserTag();
        return;
      }
    } catch (error) {
      state.notice = error && error.message ? error.message : '操作失败';
      render();
    }
  }

  function render() {
    common.setDocumentTitle('人群管理台');
    if (!state.session || !state.session.adminId) {
      root.innerHTML = common.renderAuthRequired({
        title: '请先登录管理后台',
        description: '当前人群管理页无法读取管理员会话，请返回后台登录后重试。',
        loginUrl: '/manager/login',
      });
      scheduleHeightSync();
      return;
    }

    if (state.loading && !state.initialized) {
      root.innerHTML = common.renderLoading('人群管理台', '正在加载标签、人群和规则数据...');
      scheduleHeightSync();
      return;
    }

    root.innerHTML = common.localizeHtml(renderPage());
    scheduleHeightSync();
  }

  function renderPage() {
    var html = '';
    html += renderToolbar();
    html += renderOverview();
    if (state.notice) {
      html += '<section class="manager-panel"><span class="audience-notice">' + escapeHtml(state.notice) + '</span></section>';
    }
    html += renderTabs();
    html += renderActiveTabSection();
    return html;
  }

  function renderTabs() {
    var html = '';
    html += '<section class="manager-panel">';
    html += '<div class="audience-tabs">';
    html += AUDIENCE_TAB_ITEMS.map(function (item) {
      var activeClass = state.activeTab === item.key ? ' is-active' : '';
      return '<button class="audience-tab-btn' + activeClass + '" type="button" data-action="switch-audience-tab" data-tab="' + escapeHtml(item.key) + '">' +
        escapeHtml(item.label) +
      '</button>';
    }).join('');
    html += '</div>';
    html += '</section>';
    return html;
  }

  function renderActiveTabSection() {
    if (state.activeTab === 'segment') {
      return renderSegmentSection();
    }
    if (state.activeTab === 'rule') {
      return renderSegmentRuleSection();
    }
    if (state.activeTab === 'user') {
      return renderUserTagSection();
    }
    if (state.activeTab === 'tag') {
      return renderTagDefinitionSection();
    }
    return renderSegmentSection();
  }

  function renderToolbar() {
    var html = '';
    html += '<section class="manager-panel audience-toolbar">';
    html += '<div>';
    html += '<h1 class="manager-title">人群管理台</h1>';
    html += '<p class="manager-subtext">支持按标签快照计算人群命中，并用于投放单元与创意定向。</p>';
    html += '</div>';
    html += '<div class="audience-toolbar-actions">';
    html += '<span class="audience-toolbar-note">当前管理员：' + escapeHtml(common.pickDisplayName(state.session)) + '</span>';
    html += '<button class="manager-btn" type="button" data-action="reload-audience">刷新数据</button>';
    html += '</div>';
    html += '</section>';
    return html;
  }

  function renderOverview() {
    var html = '';
    html += '<section class="manager-panel audience-overview-panel">';
    html += '<div class="audience-overview-strip">';
    html += '<div class="audience-overview-strip-title">资源统计</div>';
    html += '<div class="audience-overview-list">';
    html += renderOverviewCard('标签定义', toArray(state.data.tagDefinitions).length);
    html += renderOverviewCard('人群定义', toArray(state.data.segments).length);
    html += renderOverviewCard('规则数量', toArray(state.data.segmentRules).length);
    html += renderOverviewCard('用户标签', toArray(state.data.userTags).length);
    html += '</div>';
    html += '</div>';
    html += '</section>';
    return html;
  }

  function renderOverviewCard(label, value) {
    return '<div class="audience-overview-item">' +
      '<span class="audience-overview-label">' + escapeHtml(label) + '</span>' +
      '<span class="audience-overview-value">' + escapeHtml(String(value || 0)) + '</span>' +
      '</div>';
  }

  function renderTagDefinitionSection() {
    var rows = toArray(state.data.tagDefinitions);
    var html = '';
    html += '<section class="manager-panel">';
    html += '<div class="audience-section-head">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">标签定义</h3>';
    html += '<div class="audience-section-desc">配置投放可用的人群标签，支持枚举、数值、布尔和文本。</div>';
    html += '</div>';
    html += '</div>';
    html += '<div class="audience-form-grid">';
    html += renderField('标签编码', '<input id="audTagCode" class="form-input" placeholder="如 city_tier" />');
    html += renderField('标签名称', '<input id="audTagName" class="form-input" placeholder="如 城市层级" />');
    html += renderField('标签类型', '<select id="audTagType" class="form-input">' + renderEnumOptions(TAG_TYPE_OPTIONS, 'ENUM', TAG_TYPE_LABEL_MAP) + '</select>');
    html += renderField('取值范围', '<input id="audTagValueScope" class="form-input" placeholder="如 T1,T2,T3" />');
    html += renderField('启用状态', '<select id="audTagEnabled" class="form-input">' + renderBooleanOptions(true) + '</select>');
    html += renderField('标签描述', '<input id="audTagDescription" class="form-input" placeholder="可选" />');
    html += '</div>';
    html += '<div style="margin-top:10px;"><button class="manager-btn primary" type="button" data-action="save-tag-definition">保存标签定义</button></div>';
    html += '<div class="audience-table-wrap" style="margin-top:12px;">';
    html += '<table class="audience-table"><thead><tr><th>编码</th><th>名称</th><th>类型</th><th>取值范围</th><th>启用</th><th>更新时间</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="6" class="manager-coupon-empty-row">暂无标签定义</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td><span class="audience-code">' + escapeHtml(item.tagCode || '-') + '</span></td>' +
          '<td>' + escapeHtml(item.tagName || '-') + '</td>' +
          '<td>' + escapeHtml(formatTagType(item.tagType)) + '</td>' +
          '<td>' + escapeHtml(item.valueScope || '-') + '</td>' +
          '<td>' + escapeHtml(common.formatBooleanText(item.enabled)) + '</td>' +
          '<td>' + escapeHtml(common.formatDateTime(item.updatedAt || item.createdAt)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderSegmentSection() {
    var rows = toArray(state.data.segments);
    var html = '';
    html += '<section class="manager-panel">';
    html += '<div class="audience-section-head">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">人群定义</h3>';
    html += '<div class="audience-section-desc">分群可按场景配置为草稿、发布或下线。</div>';
    html += '</div>';
    html += '</div>';
    html += '<div class="audience-form-grid">';
    html += renderField('人群编码', '<input id="audSegmentCode" class="form-input" placeholder="如 AUD_SEG_HIGH_VALUE" />');
    html += renderField('人群名称', '<input id="audSegmentName" class="form-input" placeholder="如 高价值用户" />');
    html += renderField('状态', '<select id="audSegmentStatus" class="form-input">' + renderEnumOptions(SEGMENT_STATUS_OPTIONS, 'DRAFT', SEGMENT_STATUS_LABEL_MAP) + '</select>');
    html += renderField('场景编码', '<input id="audSegmentSceneCode" class="form-input" placeholder="如 HOME" />');
    html += renderField('人群描述', '<input id="audSegmentDescription" class="form-input" placeholder="可选" />');
    html += '</div>';
    html += '<div style="margin-top:10px;"><button class="manager-btn primary" type="button" data-action="save-segment">保存人群定义</button></div>';
    html += '<div class="audience-table-wrap" style="margin-top:12px;">';
    html += '<table class="audience-table"><thead><tr><th>编码</th><th>名称</th><th>场景</th><th>状态</th><th>更新时间</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="5" class="manager-coupon-empty-row">暂无人群定义</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td><span class="audience-code">' + escapeHtml(item.segmentCode || '-') + '</span></td>' +
          '<td>' + escapeHtml(item.segmentName || '-') + '</td>' +
          '<td>' + escapeHtml(item.sceneCode || '-') + '</td>' +
          '<td>' + escapeHtml(formatSegmentStatus(item.status)) + '</td>' +
          '<td>' + escapeHtml(common.formatDateTime(item.updatedAt || item.createdAt)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderSegmentRuleSection() {
    var rows = toArray(state.data.segmentRules);
    var segmentOptions = renderSegmentOptions(state.selectedSegmentCode);
    var tagOptions = renderTagOptions('');
    var html = '';
    html += '<section class="manager-panel">';
    html += '<div class="audience-section-head">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">人群规则</h3>';
    html += '<div class="audience-section-desc">规则支持 INCLUDE / EXCLUDE，作用于标签值匹配。</div>';
    html += '</div>';
    html += '<div class="audience-inline-row">';
    html += '<select id="audRuleSegmentFilter" class="form-input">' + segmentOptions + '</select>';
    html += '</div>';
    html += '</div>';

    html += '<div class="audience-form-grid">';
    html += renderField('规则编码', '<input id="audRuleCode" class="form-input" placeholder="如 RULE_001" />');
    html += renderField('人群编码', '<select id="audRuleSegmentCode" class="form-input">' + segmentOptions + '</select>');
    html += renderField('标签编码', '<select id="audRuleTagCode" class="form-input">' + tagOptions + '</select>');
    html += renderField('操作符', '<select id="audRuleOperator" class="form-input">' + renderEnumOptions(RULE_OPERATOR_OPTIONS, 'EQ', RULE_OPERATOR_LABEL_MAP) + '</select>');
    html += renderField('目标值', '<input id="audRuleTargetValue" class="form-input" placeholder="例如 5000 或 A,B,C" />');
    html += renderField('规则归属', '<select id="audRuleRelation" class="form-input">' + renderEnumOptions(RULE_RELATION_OPTIONS, 'INCLUDE', RULE_RELATION_LABEL_MAP) + '</select>');
    html += renderField('启用状态', '<select id="audRuleEnabled" class="form-input">' + renderBooleanOptions(true) + '</select>');
    html += '</div>';
    html += '<div style="margin-top:10px;"><button class="manager-btn primary" type="button" data-action="save-segment-rule">保存人群规则</button></div>';

    html += '<div class="audience-table-wrap" style="margin-top:12px;">';
    html += '<table class="audience-table"><thead><tr><th>规则编码</th><th>标签</th><th>匹配条件</th><th>归属</th><th>启用</th><th>更新时间</th></tr></thead><tbody>';
    if (!rows.length) {
      html += '<tr><td colspan="6" class="manager-coupon-empty-row">暂无人群规则</td></tr>';
    } else {
      html += rows.map(function (item) {
        return '<tr>' +
          '<td><span class="audience-code">' + escapeHtml(item.ruleCode || '-') + '</span></td>' +
          '<td>' + escapeHtml(item.tagCode || '-') + '</td>' +
          '<td>' + escapeHtml(formatRuleOperator(item.operator) + ' / ' + (item.targetValue || '-')) + '</td>' +
          '<td>' + escapeHtml(formatRuleRelation(item.relation)) + '</td>' +
          '<td>' + escapeHtml(common.formatBooleanText(item.enabled)) + '</td>' +
          '<td>' + escapeHtml(common.formatDateTime(item.updatedAt || item.createdAt)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderUserTagSection() {
    var userTags = toArray(state.data.userTags);
    var tokens = toArray(state.data.userTagTokens);
    var matched = toArray(state.data.matchedSegments);
    var tagOptions = renderTagOptions('');
    var html = '';
    html += '<section class="manager-panel">';
    html += '<div class="audience-section-head">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">用户标签与命中调试</h3>';
    html += '<div class="audience-section-desc">输入用户ID可查看标签快照、标签token以及命中的已发布人群。</div>';
    html += '</div>';
    html += '</div>';
    html += '<div class="audience-inline-row">';
    html += '<input id="audQueryUserId" class="form-input" placeholder="请输入用户ID" value="' + escapeHtml(state.queryUserId || '') + '" />';
    html += '<button class="manager-btn" type="button" data-action="query-user-tags">查询用户标签</button>';
    html += '</div>';
    html += '<div class="audience-form-grid two" style="margin-top:10px;">';
    html += renderField('用户ID', '<input id="audUserTagUserId" class="form-input" placeholder="如 880100068483692100" value="' + escapeHtml(state.queryUserId || '') + '" />');
    html += renderField('标签编码', '<select id="audUserTagCode" class="form-input">' + tagOptions + '</select>');
    html += renderField('标签值', '<input id="audUserTagValue" class="form-input" placeholder="如 T1 / 5000" />');
    html += renderField('来源', '<input id="audUserTagSource" class="form-input" placeholder="如 MANUAL / ETL" />');
    html += '</div>';
    html += '<div style="margin-top:10px;"><button class="manager-btn primary" type="button" data-action="save-user-tag">保存用户标签</button></div>';

    html += '<div style="margin-top:12px;" class="audience-form-grid two">';
    html += '<div><div class="manager-panel-title" style="font-size:13px;">标签 Token</div>' + renderBadgeList(tokens) + '</div>';
    html += '<div><div class="manager-panel-title" style="font-size:13px;">命中人群</div>' + renderBadgeList(matched) + '</div>';
    html += '</div>';

    html += '<div class="audience-table-wrap" style="margin-top:12px;">';
    html += '<table class="audience-table"><thead><tr><th>用户ID</th><th>标签编码</th><th>标签值</th><th>来源</th><th>值更新时间</th></tr></thead><tbody>';
    if (!userTags.length) {
      html += '<tr><td colspan="5" class="manager-coupon-empty-row">暂无用户标签</td></tr>';
    } else {
      html += userTags.map(function (item) {
        return '<tr>' +
          '<td>' + escapeHtml(item.userId || '-') + '</td>' +
          '<td><span class="audience-code">' + escapeHtml(item.tagCode || '-') + '</span></td>' +
          '<td>' + escapeHtml(item.tagValue || '-') + '</td>' +
          '<td>' + escapeHtml(item.source || '-') + '</td>' +
          '<td>' + escapeHtml(common.formatDateTime(item.valueUpdatedAt || item.updatedAt || item.createdAt)) + '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    html += '</section>';
    return html;
  }

  function renderBadgeList(values) {
    var list = toArray(values);
    if (!list.length) {
      return '<div class="manager-coupon-tip">暂无</div>';
    }
    return '<div class="audience-badges">' + list.map(function (item) {
      return '<span class="audience-badge">' + escapeHtml(String(item)) + '</span>';
    }).join('') + '</div>';
  }

  function renderField(label, controlHtml) {
    return '<div class="audience-field"><label>' + escapeHtml(label) + '</label>' + controlHtml + '</div>';
  }

  function renderTagOptions(selectedValue) {
    var rows = toArray(state.data.tagDefinitions);
    if (!rows.length) {
      return '<option value="">暂无标签定义</option>';
    }
    return rows.map(function (item) {
      var code = String(item.tagCode || '');
      var selected = code === String(selectedValue || '') ? ' selected' : '';
      var label = (item.tagName ? item.tagName : code) + '（' + code + '）';
      return '<option value="' + escapeHtml(code) + '"' + selected + '>' + escapeHtml(label) + '</option>';
    }).join('');
  }

  function renderSegmentOptions(selectedValue) {
    var rows = toArray(state.data.segments);
    if (!rows.length) {
      return '<option value="">暂无人群定义</option>';
    }
    return rows.map(function (item) {
      var code = String(item.segmentCode || '');
      var selected = code === String(selectedValue || '') ? ' selected' : '';
      var label = (item.segmentName ? item.segmentName : code) + '（' + code + '）';
      return '<option value="' + escapeHtml(code) + '"' + selected + '>' + escapeHtml(label) + '</option>';
    }).join('');
  }

  function renderEnumOptions(options, selectedValue, labelMap) {
    return toArray(options).map(function (value) {
      var normalized = String(value || '');
      var selected = normalized === String(selectedValue || '') ? ' selected' : '';
      var label = labelMap && labelMap[normalized] ? labelMap[normalized] : normalized;
      return '<option value="' + escapeHtml(normalized) + '"' + selected + '>' + escapeHtml(label) + '</option>';
    }).join('');
  }

  function renderBooleanOptions(selectedValue) {
    return [
      { value: 'true', label: '启用', selected: selectedValue === true },
      { value: 'false', label: '停用', selected: selectedValue === false },
    ].map(function (item) {
      return '<option value="' + item.value + '"' + (item.selected ? ' selected' : '') + '>' + item.label + '</option>';
    }).join('');
  }

  function formatTagType(value) {
    return resolveEnumLabel(value, TAG_TYPE_LABEL_MAP);
  }

  function formatSegmentStatus(value) {
    return resolveEnumLabel(value, SEGMENT_STATUS_LABEL_MAP);
  }

  function formatRuleOperator(value) {
    return resolveEnumLabel(value, RULE_OPERATOR_LABEL_MAP);
  }

  function formatRuleRelation(value) {
    return resolveEnumLabel(value, RULE_RELATION_LABEL_MAP);
  }

  function resolveEnumLabel(value, labelMap) {
    var code = String(value || '').trim().toUpperCase();
    if (!code) {
      return '-';
    }
    return labelMap && labelMap[code] ? labelMap[code] : code;
  }

  function ensureSelectedSegmentCode() {
    if (!state.selectedSegmentCode && state.data.segments.length) {
      state.selectedSegmentCode = String(state.data.segments[0].segmentCode || '');
    }
    if (!state.selectedSegmentCode) {
      return;
    }
    var matched = state.data.segments.some(function (item) {
      return String(item.segmentCode || '') === state.selectedSegmentCode;
    });
    if (!matched) {
      state.selectedSegmentCode = state.data.segments.length ? String(state.data.segments[0].segmentCode || '') : '';
    }
  }

  function readValue(id) {
    var element = document.getElementById(id);
    if (!element || typeof element.value !== 'string') {
      return '';
    }
    return String(element.value || '').trim();
  }

  function requireValue(value, label) {
    var normalized = String(value || '').trim();
    if (!normalized) {
      throw new Error(label + '不能为空');
    }
    return normalized;
  }

  function optionalValue(value) {
    var normalized = String(value || '').trim();
    return normalized || null;
  }

  function parseOptionalBoolean(raw, defaultValue) {
    var normalized = String(raw || '').trim().toLowerCase();
    if (!normalized) {
      return defaultValue;
    }
    if (normalized === 'true') {
      return true;
    }
    if (normalized === 'false') {
      return false;
    }
    return defaultValue;
  }

  function normalizePositiveId(raw) {
    var normalized = String(raw == null ? '' : raw).trim();
    if (!/^\d+$/.test(normalized) || normalized === '0') {
      return '';
    }
    return normalized;
  }

  function requirePositiveId(raw, label) {
    var normalized = normalizePositiveId(raw);
    if (!normalized) {
      throw new Error(label + '必须是正整数');
    }
    return normalized;
  }

  async function requestGet(path, query) {
    var qs = common.serializeQuery(query);
    return common.requestJson(path + (qs ? '?' + qs : ''), {
      method: 'GET',
      headers: common.buildAdminHeaders(state.session),
    });
  }

  async function requestPost(path, payload) {
    return common.requestJson(path, {
      method: 'POST',
      headers: common.withJson(common.buildAdminHeaders(state.session)),
      body: JSON.stringify(payload || {}),
    });
  }

  function toArray(value) {
    if (!value) {
      return [];
    }
    if (Array.isArray(value)) {
      return value;
    }
    return [];
  }

  function scheduleHeightSync() {
    if (FRAME_NOTIFIER && typeof FRAME_NOTIFIER.schedule === 'function') {
      FRAME_NOTIFIER.schedule();
    }
  }

  function escapeHtml(value) {
    return common.escapeHtml(value == null ? '' : String(value));
  }
})();
