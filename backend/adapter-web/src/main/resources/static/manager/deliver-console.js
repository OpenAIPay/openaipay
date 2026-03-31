(function () {
  var common = window.OpenAiPayAdminEmbedded || null;
  var SESSION_KEYS = ['openaipay.admin.session.v2', 'openaipay.admin.session.v1'];
  var ROOT_SELECTOR = 'deliverConsoleApp';
  var API_CONSOLE_PATH = '/api/admin/deliver/console';
  var root = document.getElementById(ROOT_SELECTOR);

  function langText(zh, en) {
    if (common && typeof common.langText === 'function') {
      return common.langText(zh, en);
    }
    return zh;
  }

  function localizeText(text) {
    if (common && typeof common.localizeText === 'function') {
      return common.localizeText(text);
    }
    return String(text == null ? '' : text);
  }

  function localizeHtml(html) {
    if (common && typeof common.localizeHtml === 'function') {
      return common.localizeHtml(html);
    }
    return html;
  }

  function setDocumentTitle(title) {
    if (common && typeof common.setDocumentTitle === 'function') {
      common.setDocumentTitle(title);
      return;
    }
    document.title = localizeText(title);
  }

  var state = {
    session: loadSession(),
    loading: false,
    notice: '',
    lastReportedHeight: 0,
    activeTab: 'position',
    scopedRuleDraft: {
      entityType: '',
      entityCode: '',
    },
    consoleData: createEmptyConsoleData(),
    positionPanel: {
      mode: 'hidden',
      positionId: null,
    },
    unitPanel: {
      mode: 'hidden',
      unitId: null,
    },
    materialPanel: {
      mode: 'hidden',
      materialId: null,
    },
    creativePanel: {
      mode: 'hidden',
      creativeId: null,
    },
    relationPanel: {
      mode: 'hidden',
      relationId: null,
    },
    fatiguePanel: {
      mode: 'hidden',
      fatigueRuleId: null,
    },
    targetingPanel: {
      mode: 'hidden',
      targetingRuleId: null,
    },
    editing: {
      positionId: null,
      unitId: null,
      materialId: null,
      creativeId: null,
      relationId: null,
      fatigueRuleId: null,
      targetingRuleId: null,
    },
  };

  bindEvents();
  window.addEventListener('resize', scheduleNotifyParentHeight);
  start();

  function bindEvents() {
    if (!root) {
      return;
    }
    root.addEventListener('click', onClick);
    root.addEventListener('change', onChange);
  }

  async function start() {
    if (!root) {
      return;
    }
    if (!state.session || !state.session.adminId) {
      render();
      return;
    }
    await loadConsole(true);
  }

  function createEmptyConsoleData() {
    return {
      overview: {
        positionCount: 0,
        unitCount: 0,
        materialCount: 0,
        creativeCount: 0,
        relationCount: 0,
        fatigueRuleCount: 0,
        targetingRuleCount: 0,
      },
      positions: [],
      units: [],
      materials: [],
      creatives: [],
      relations: [],
      fatigueRules: [],
      targetingRules: [],
    };
  }

  async function loadConsole(showLoadedNotice) {
    state.loading = true;
    render();
    try {
      state.consoleData = await requestJson(API_CONSOLE_PATH, {
        method: 'GET',
        headers: buildAdminHeaders(),
      });
      if (showLoadedNotice) {
        state.notice = '投放配置已加载';
      }
    } catch (error) {
      state.notice = error && error.message ? error.message : '投放配置加载失败';
    } finally {
      state.loading = false;
      render();
    }
  }

  function render() {
    if (!root) {
      return;
    }
    setDocumentTitle('投放配置台');
    if (!state.session || !state.session.adminId) {
      root.innerHTML = localizeHtml(renderAuthRequired());
      scheduleNotifyParentHeight();
      return;
    }
    if (state.loading && !hasDeliverData()) {
      root.innerHTML = localizeHtml(renderLoadingPage());
      scheduleNotifyParentHeight();
      return;
    }
    root.innerHTML = localizeHtml(renderPage());
    scheduleNotifyParentHeight();
  }

  function scheduleNotifyParentHeight() {
    if (typeof window.requestAnimationFrame === 'function') {
      window.requestAnimationFrame(notifyParentHeight);
      return;
    }
    window.setTimeout(notifyParentHeight, 16);
  }

  function notifyParentHeight() {
    if (window.parent === window || !window.parent || typeof window.parent.postMessage !== 'function') {
      return;
    }
    var rootHeight = root ? Math.max(root.scrollHeight || 0, root.offsetHeight || 0, root.clientHeight || 0) : 0;
    var bodyHeight = document.body ? Math.max(document.body.scrollHeight || 0, document.body.offsetHeight || 0, document.body.clientHeight || 0) : 0;
    var documentHeight = document.documentElement ? Math.max(document.documentElement.scrollHeight || 0, document.documentElement.offsetHeight || 0, document.documentElement.clientHeight || 0) : 0;
    var measuredHeight = Math.max(rootHeight, bodyHeight, documentHeight, 0);
    var height = Math.max(Math.ceil(measuredHeight), 240);
    if (state.lastReportedHeight > 0 && Math.abs(state.lastReportedHeight - height) < 2) {
      return;
    }
    state.lastReportedHeight = height;
    window.parent.postMessage({
      type: 'openaipay-deliver-frame-height',
      height: height,
    }, window.location.origin);
  }

  function renderAuthRequired() {
    var html = '';
    html += '<section class="manager-panel deliver-auth-empty">';
    html += '<div class="manager-placeholder">';
    html += '<h2 class="manager-placeholder-title">请先登录管理后台</h2>';
    html += '<p class="manager-placeholder-tip">当前投放配置页无法读取管理员会话，请返回后台登录后重试。</p>';
    html += '<div class="deliver-auth-actions"><a class="manager-btn primary" href="/manager/login" target="_top">返回登录</a></div>';
    html += '</div>';
    html += '</section>';
    return html;
  }

  function renderLoadingPage() {
    var html = '';
    html += '<section class="manager-panel">';
    html += '<div class="manager-placeholder">';
    html += '<h2 class="manager-placeholder-title">投放配置台</h2>';
    html += '<p class="manager-placeholder-tip">正在拉取展位、投放单元与素材配置...</p>';
    html += '</div>';
    html += '</section>';
    return html;
  }


  
function renderPage() {
  var html = '';
  html += '<div class="deliver-toolbar">';
  html += '<div>';
  html += '<h1 class="manager-title">投放配置台</h1>';
  html += '<p class="manager-subtext">按“展位 / 投放单元 / 素材”分层管理，创意与规则在单元与创意详情内维护。</p>';
  html += '</div>';
  html += '<div class="deliver-toolbar-actions">';
  html += '<span class="deliver-toolbar-note">当前管理员：' + escapeHtml(pickDisplayName()) + '</span>';
  html += '<button class="manager-btn" type="button" data-action="reload-deliver">刷新数据</button>';
  html += '</div>';
  html += '</div>';

  html += renderOverviewGrid();

  if (state.notice) {
    html += '<section class="manager-panel" style="margin-bottom:14px;">';
    html += '<span class="manager-coupon-notice">' + escapeHtml(state.notice) + '</span>';
    html += '</section>';
  }

  html += renderPrimaryTabs();
  html += renderActiveTabSection();
  html += renderPositionDetailSection();
  html += renderUnitDetailSection();
  html += renderMaterialDetailSection();
  html += renderCreativeDetailSection();
  html += renderRelationDetailSection();
  html += renderFatigueDetailSection();
  html += renderTargetingDetailSection();
  return html;
}

function renderPrimaryTabs() {
  var tabs = [
    { key: 'position', label: '展位', count: (state.consoleData.positions || []).length },
    { key: 'unit', label: '投放单元', count: (state.consoleData.units || []).length },
    { key: 'material', label: '素材', count: (state.consoleData.materials || []).length },
  ];
  var html = '';
  html += '<section class="manager-panel deliver-tabs-panel">';
  html += '<div class="deliver-tabs-row">';
  html += tabs.map(function (tab) {
    var activeClass = state.activeTab === tab.key ? ' is-active' : '';
    return '<button class="deliver-tab-btn' + activeClass + '" type="button" data-action="switch-deliver-tab" data-tab="' + escapeHtml(tab.key) + '">' +
      '<span class="deliver-tab-label">' + escapeHtml(tab.label) + '</span>' +
      '<span class="deliver-tab-count">' + escapeHtml(String(tab.count)) + '</span>' +
    '</button>';
  }).join('');
  html += '</div>';
  html += '</section>';
  return html;
}

function renderActiveTabSection() {
  if (state.activeTab === 'unit') {
    return renderUnitSection();
  }
  if (state.activeTab === 'material') {
    return renderMaterialSection();
  }
  return renderPositionSection();
}


function renderOverviewGrid() {
  var overview = state.consoleData.overview || {};
  var cards = [
    { title: '展位', value: overview.positionCount || 0 },
    { title: '投放单元', value: overview.unitCount || 0 },
    { title: '素材', value: overview.materialCount || 0 },
    { title: '创意', value: overview.creativeCount || 0 },
  ];
  var html = '';
  html += '<section class="manager-panel deliver-overview-panel">';
  html += '<div class="deliver-overview-strip">';
  html += '<div class="deliver-overview-strip-title">资源统计</div>';
  html += '<div class="deliver-overview-list">' + cards.map(renderOverviewCard).join('') + '</div>';
  html += '</div>';
  html += '</section>';
  return html;
}

function renderOverviewCard(card) {
  var html = '';
  html += '<div class="deliver-overview-item">';
  html += '<span class="deliver-overview-label">' + escapeHtml(card.title) + '</span>';
  html += '<span class="deliver-overview-value">' + escapeHtml(card.value) + '</span>';
  html += '</div>';
  return html;
}

  function renderPositionSection() {
    var items = state.consoleData.positions || [];
    var html = '';
    html += '<section id="deliverPositionSection" class="manager-panel deliver-section">';
    html += '<div class="deliver-section-head">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">展位列表</h3>';
    html += '<div class="deliver-section-desc">按展位管理关联投放单元，可在详情中增加或剔除单元。</div>';
    html += '</div>';
    html += '<div class="deliver-toolbar-actions">';
    html += '<span class="deliver-section-meta">' + escapeHtml(items.length) + '</span>';
    html += '<button class="manager-btn primary" type="button" data-action="create-position">新建展位</button>';
    html += '</div>';
    html += '</div>';
    html += renderPositionTable(items);
    html += '</section>';
    return html;
  }


function renderDeliverModal(options) {
  if (!options) {
    return '';
  }
  var panelClass = options.panelClass ? ' ' + options.panelClass : '';
  var html = '';
  html += '<div class="manager-modal-layer deliver-modal-layer">';
  html += '<div class="manager-modal-mask" data-action="' + escapeHtml(options.closeAction || '') + '"></div>';
  html += '<section id="' + escapeHtml(options.id || '') + '" class="manager-modal-panel deliver-modal-panel' + escapeHtml(panelClass) + '" tabindex="-1">';
  html += '<div class="manager-modal-head">';
  html += '<div class="deliver-modal-head-copy">';
  html += '<h3 class="manager-panel-title">' + (options.titleHtml || '') + '</h3>';
  if (options.descHtml) {
    html += '<div class="deliver-section-desc">' + options.descHtml + '</div>';
  }
  html += '</div>';
  html += '<div class="deliver-modal-head-actions">';
  if (options.actionsHtml) {
    html += options.actionsHtml;
  }
  html += '<button class="manager-modal-close" type="button" data-action="' + escapeHtml(options.closeAction || '') + '" aria-label="关闭">×</button>';
  html += '</div>';
  html += '</div>';
  html += '<div class="deliver-modal-body">';
  html += options.bodyHtml || '';
  html += '</div>';
  html += '</section>';
  html += '</div>';
  return html;
}


function renderPositionDetailSection() {
  var panel = state.positionPanel || { mode: 'hidden', positionId: null };
  if (!panel.mode || panel.mode === 'hidden') {
    return '';
  }
  var item = findItemById(state.consoleData.positions || [], panel.positionId);
  var isCreate = panel.mode === 'create';
  var isEdit = panel.mode === 'edit' || isCreate;
  var titleHtml = '';
  var descHtml = '';
  var actionsHtml = '';
  var bodyHtml = '';
  var panelClass = isEdit ? 'deliver-position-edit-modal' : 'deliver-position-view-modal';

  if (isCreate) {
    titleHtml = '新建展位';
    descHtml = '展位新增以弹出页完成。';
  } else if (isEdit && item) {
    titleHtml = '修改展位 #' + escapeHtml(item.id);
    descHtml = '展位修改以弹出页完成。';
  } else if (item) {
    titleHtml = '展位详情 #' + escapeHtml(item.id);
    descHtml = '展位详情以弹出页查看，点击右侧按钮进入修改页。';
    actionsHtml = '<button class="manager-btn" type="button" data-action="edit-position" data-id="' + escapeHtml(item.id) + '">进入修改页</button>';
  } else {
    titleHtml = '展位详情';
    descHtml = '未找到所选展位，请刷新后重试。';
  }

  if (!isCreate && !item) {
    bodyHtml = '<div class="manager-placeholder"><p class="manager-placeholder-tip">未找到该展位，请刷新后重试。</p></div>';
  } else if (isEdit) {
    bodyHtml = '<div class="deliver-position-edit-page">' + renderPositionForm(item) + '</div>';
  } else {
    bodyHtml = '<div class="deliver-position-view-page">' + renderPositionDetail(item) + '</div>';
  }

  return renderDeliverModal({
    id: 'deliverPositionDetailSection',
    titleHtml: titleHtml,
    descHtml: descHtml,
    closeAction: 'close-position-panel',
    actionsHtml: actionsHtml,
    bodyHtml: bodyHtml,
    panelClass: panelClass,
  });
}

  function renderPositionDetail(item) {
    var context = buildPositionDetailContext(item);
    var html = '';
    html += '<div class="deliver-position-detail-grid">';
    html += renderPositionDetailBlock('基础信息', renderPositionDetailItems([
      { label: '展位编码', valueHtml: '<span class="deliver-inline-code">' + escapeHtml(item.positionCode || '-') + '</span>' },
      { label: '展位名称', valueHtml: escapeHtml(item.positionName || '-') },
      { label: '展位类型', valueHtml: escapeHtml(formatDeliverEnumText(item.positionType)) },
      { label: '发布状态', valueHtml: renderStatusBadge(item.status) },
      { label: '轮播秒数', valueHtml: escapeHtml(item.slideInterval == null ? '-' : item.slideInterval + 's') },
      { label: '最大返回数', valueHtml: escapeHtml(item.maxDisplayCount == null ? '-' : item.maxDisplayCount) },
      { label: '排序方式', valueHtml: escapeHtml(formatDeliverEnumText(item.sortType)) },
      { label: '排序规则', valueHtml: escapeHtml(item.sortRule || '-') },
      { label: '兜底开关', valueHtml: escapeHtml(formatBooleanText(item.needFallback)) },
      { label: '发布时间', valueHtml: escapeHtml(formatDateTime(item.publishedAt)) },
      { label: '预览图', valueHtml: renderPreviewLink(item.previewImage) },
      { label: '备注', valueHtml: escapeHtml(item.memo || '-') }
    ]));
    html += renderPositionDetailBlock('关联资源', renderPositionDetailItems([
      { label: '投放单元', valueHtml: renderPositionTagList(context.units, 'unitCode', 'unitName') },
      { label: '创意', valueHtml: renderPositionTagList(context.creatives, 'creativeCode', 'creativeName') },
      { label: '素材', valueHtml: renderPlainTagList(context.materialCodes) },
      { label: '关联关系数', valueHtml: escapeHtml(String(context.relations.length)) }
    ]));
    html += '</div>';
    html += renderPositionRelationPreview(context);
    return html;
  }

  function buildPositionDetailContext(position) {
    var relationItems = (state.consoleData.relations || []).filter(function (item) {
      return Number(item.positionId) === Number(position.id);
    });
    var enabledRelations = relationItems.filter(function (item) {
      return item.enabled !== false;
    });
    var unitMap = createIdMap(state.consoleData.units || [], 'id');
    var creativeMap = createIdMap(state.consoleData.creatives || [], 'id');
    var seenUnit = {};
    var seenCreative = {};
    var seenMaterial = {};
    var units = [];
    var creatives = [];
    var materialCodes = [];

    enabledRelations.forEach(function (relation) {
      var unit = unitMap[relation.unitId];
      var creative = creativeMap[relation.creativeId];
      if (unit && !seenUnit[unit.id]) {
        seenUnit[unit.id] = true;
        units.push(unit);
      }
      if (creative && !seenCreative[creative.id]) {
        seenCreative[creative.id] = true;
        creatives.push(creative);
        if (creative.materialCode && !seenMaterial[creative.materialCode]) {
          seenMaterial[creative.materialCode] = true;
          materialCodes.push(creative.materialCode);
        }
      }
    });

    return {
      relations: enabledRelations,
      allRelations: relationItems,
      units: units,
      creatives: creatives,
      materialCodes: materialCodes,
    };
  }

  function renderPositionUnitManageBlock(position) {
    var context = buildPositionDetailContext(position);
    var attachableUnits = resolveAttachableUnitsForPosition(position.id);
    var html = '';
    html += '<div class="deliver-position-relations">';
    html += '<h4 class="manager-panel-title">投放单元管理</h4>';
    html += '<div class="deliver-inline-form-row">';
    html += '<select id="positionUnitAttachSelect" class="form-input deliver-inline-select">';
    html += renderIdOptions(attachableUnits, 'id', 'unitCode', 'unitName', '');
    html += '</select>';
    html += '<button class="manager-btn primary" type="button" data-action="attach-position-unit" data-id="' + escapeHtml(position.id) + '">增加投放单元</button>';
    html += '</div>';

    if (!context.units.length) {
      html += '<div class="manager-coupon-tip">当前展位暂无关联投放单元，请先增加投放单元。</div>';
      html += '</div>';
      return html;
    }

    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>投放单元</th><th>创意数量</th><th>操作</th></tr></thead><tbody>';
    html += context.units.map(function (unit) {
      return '<tr>' +
        '<td><span class="deliver-inline-code">' + escapeHtml(buildCodeLabel(unit.unitCode, unit.unitName, unit.id)) + '</span></td>' +
        '<td>' + escapeHtml(String(countActiveCreativesForUnitCode(unit.unitCode))) + '</td>' +
        '<td class="deliver-table-actions">' +
          '<button class="manager-btn" type="button" data-action="view-unit" data-id="' + escapeHtml(unit.id) + '">查看单元</button>' +
          '<button class="manager-btn" type="button" data-action="detach-position-unit" data-position-id="' + escapeHtml(position.id) + '" data-unit-id="' + escapeHtml(unit.id) + '">剔除</button>' +
        '</td>' +
      '</tr>';
    }).join('');
    html += '</tbody></table></div>';
    html += '</div>';
    return html;
  }

  function renderPositionDetailBlock(title, bodyHtml) {
    var html = '';
    html += '<section class="deliver-position-detail-block">';
    html += '<h4 class="deliver-position-detail-title">' + escapeHtml(title) + '</h4>';
    html += bodyHtml;
    html += '</section>';
    return html;
  }

  function renderPositionDetailItems(items) {
    if (!items || !items.length) {
      return '<div class="deliver-muted">暂无数据</div>';
    }
    return '<div class="deliver-position-detail-items">' + items.map(function (item) {
      return '<div class="deliver-position-detail-item">' +
        '<div class="deliver-position-detail-label">' + escapeHtml(item.label) + '</div>' +
        '<div class="deliver-position-detail-value">' + item.valueHtml + '</div>' +
      '</div>';
    }).join('') + '</div>';
  }

  function renderPositionTagList(items, codeKey, nameKey) {
    if (!items || !items.length) {
      return '<span class="deliver-muted">暂无</span>';
    }
    return '<div class="deliver-detail-tag-list">' + items.map(function (item) {
      return '<span class="deliver-inline-code">' + escapeHtml(buildCodeLabel(item[codeKey], item[nameKey], item.id)) + '</span>';
    }).join('') + '</div>';
  }

  function renderPlainTagList(values) {
    if (!values || !values.length) {
      return '<span class="deliver-muted">暂无</span>';
    }
    return '<div class="deliver-detail-tag-list">' + values.map(function (value) {
      return '<span class="deliver-inline-code">' + escapeHtml(value) + '</span>';
    }).join('') + '</div>';
  }

  function renderTableStack(titleHtml, subtitleHtml, metaHtml) {
    var html = '';
    html += '<div class="deliver-rich-cell">';
    if (titleHtml) {
      html += '<div class="deliver-cell-title">' + titleHtml + '</div>';
    }
    if (subtitleHtml) {
      html += '<div class="deliver-cell-subtext">' + subtitleHtml + '</div>';
    }
    if (metaHtml) {
      html += '<div class="deliver-cell-meta">' + metaHtml + '</div>';
    }
    html += '</div>';
    return html;
  }

  function renderTableMeta(parts) {
    return (parts || []).filter(function (part) {
      return !!part;
    }).join(' · ');
  }

  function renderPositionRelationPreview(context) {
    var unitMap = createIdMap(state.consoleData.units || [], 'id');
    var creativeMap = createIdMap(state.consoleData.creatives || [], 'id');
    var html = '';
    html += '<div class="deliver-position-relations">';
    html += '<h4 class="manager-panel-title">关联链路</h4>';
    if (!context.relations.length) {
      html += '<div class="manager-coupon-tip">当前展位暂无投放关系。</div>';
      html += '</div>';
      return html;
    }
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>关系ID</th><th>投放单元</th><th>创意</th><th>展示顺序</th><th>兜底</th><th>启用</th></tr></thead><tbody>';
    html += context.relations.map(function (relation) {
      var unit = unitMap[relation.unitId];
      var creative = creativeMap[relation.creativeId];
      return '<tr>' +
        '<td>' + escapeHtml(relation.id) + '</td>' +
        '<td>' + escapeHtml(unit ? buildCodeLabel(unit.unitCode, unit.unitName, unit.id) : relation.unitId) + '</td>' +
        '<td>' + escapeHtml(creative ? buildCodeLabel(creative.creativeCode, creative.creativeName, creative.id) : relation.creativeId) + '</td>' +
        '<td>' + escapeHtml(relation.displayOrder == null ? '-' : relation.displayOrder) + '</td>' +
        '<td>' + escapeHtml(formatBooleanText(relation.fallback)) + '</td>' +
        '<td>' + escapeHtml(formatBooleanText(relation.enabled)) + '</td>' +
      '</tr>';
    }).join('');
    html += '</tbody></table></div>';
    html += '</div>';
    return html;
  }


  function closeAllPanels() {
  state.positionPanel.mode = 'hidden';
  state.positionPanel.positionId = null;
  state.unitPanel.mode = 'hidden';
  state.unitPanel.unitId = null;
  state.materialPanel.mode = 'hidden';
  state.materialPanel.materialId = null;
  state.creativePanel.mode = 'hidden';
  state.creativePanel.creativeId = null;
  state.relationPanel.mode = 'hidden';
  state.relationPanel.relationId = null;
  state.fatiguePanel.mode = 'hidden';
  state.fatiguePanel.fatigueRuleId = null;
  state.targetingPanel.mode = 'hidden';
  state.targetingPanel.targetingRuleId = null;
  state.scopedRuleDraft.entityType = '';
  state.scopedRuleDraft.entityCode = '';
}

  function openPositionPanel(mode, positionId) {
  closeAllPanels();
  state.positionPanel.mode = mode;
  state.positionPanel.positionId = positionId == null ? null : Number(positionId);
}

  function closePositionPanel() {
    state.positionPanel.mode = 'hidden';
    state.positionPanel.positionId = null;
    state.editing.positionId = null;
  }

  function renderUnitSection() {
    var items = state.consoleData.units || [];
    var html = '';
    html += '<section id="deliverUnitSection" class="manager-panel deliver-section">';
    html += '<div class="deliver-section-head">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">投放单元列表</h3>';
    html += '<div class="deliver-section-desc">投放单元内维护素材绑定、创意生成及规则配置。</div>';
    html += '</div>';
    html += '<div class="deliver-toolbar-actions">';
    html += '<span class="deliver-section-meta">' + escapeHtml(items.length) + '</span>';
    html += '<button class="manager-btn primary" type="button" data-action="create-unit">新建单元</button>';
    html += '</div>';
    html += '</div>';
    html += renderUnitTable(items);
    html += '</section>';
    return html;
  }


function renderUnitDetailSection() {
  var panel = state.unitPanel || { mode: 'hidden', unitId: null };
  if (!panel.mode || panel.mode === 'hidden') {
    return '';
  }
  var item = findItemById(state.consoleData.units || [], panel.unitId);
  var isCreate = panel.mode === 'create';
  var isEdit = panel.mode === 'edit';
  var titleHtml = '';
  var descHtml = '';
  var actionsHtml = '';
  var bodyHtml = '';

  if (isCreate) {
    titleHtml = '新建投放单元';
    descHtml = '新建投放单元并配置执行策略。';
  } else if (isEdit && item) {
    titleHtml = '编辑投放单元 #' + escapeHtml(item.id);
    descHtml = '可修改单元信息、关联创意及疲劳度/定向规则。';
  } else if (item) {
    titleHtml = '投放单元详情 #' + escapeHtml(item.id);
    descHtml = '仅查看关联创意与疲劳度/定向规则。';
    actionsHtml = '<button class="manager-btn" type="button" data-action="edit-unit" data-id="' + escapeHtml(item.id) + '">修改单元</button>';
  } else {
    titleHtml = '投放单元详情';
    descHtml = '未找到所选投放单元，请刷新后重试。';
  }

  if (!isCreate && !item) {
    bodyHtml = '<div class="manager-placeholder"><p class="manager-placeholder-tip">未找到该投放单元，请刷新后重试。</p></div>';
  } else if (isCreate) {
    bodyHtml = renderUnitForm(item);
  } else if (isEdit) {
    bodyHtml = renderUnitEditWorkspace(item);
  } else {
    bodyHtml = renderUnitDetail(item);
  }

  return renderDeliverModal({
    id: 'deliverUnitDetailSection',
    titleHtml: titleHtml,
    descHtml: descHtml,
    closeAction: 'close-unit-panel',
    actionsHtml: actionsHtml,
    bodyHtml: bodyHtml,
  });
}

  function renderUnitDetail(item) {
    var context = buildUnitDetailContext(item);
    var html = '';
    html += renderUnitMaterialCreativeBlock(item, context, { editable: false });
    html += renderScopedRuleBlock('unit', item.unitCode, context.fatigueRules, context.targetingRules, { editable: false });
    return html;
  }

  function renderUnitEditWorkspace(item) {
    var context = buildUnitDetailContext(item);
    var html = '';
    html += '<div class="deliver-position-edit-page">';
    html += renderUnitForm(item);
    html += '</div>';
    html += renderUnitMaterialCreativeBlock(item, context, { editable: true });
    html += renderScopedRuleBlock('unit', item.unitCode, context.fatigueRules, context.targetingRules, { editable: true });
    return html;
  }

  function buildUnitDetailContext(unit) {
    var relations = (state.consoleData.relations || []).filter(function (item) {
      return Number(item.unitId) === Number(unit.id) && item.enabled !== false;
    });
    var positionMap = createIdMap(state.consoleData.positions || [], 'id');
    var creativeMap = createIdMap(state.consoleData.creatives || [], 'id');
    var materialMap = createCodeMap(state.consoleData.materials || [], 'materialCode');
    var positions = [];
    var creatives = [];
    var materials = [];
    var seenPosition = {};
    var seenCreative = {};
    var seenMaterial = {};
    relations.forEach(function (relation) {
      var position = positionMap[relation.positionId];
      var creative = creativeMap[relation.creativeId];
      if (position && !seenPosition[position.id]) {
        seenPosition[position.id] = true;
        positions.push(position);
      }
      if (creative && !seenCreative[creative.id]) {
        seenCreative[creative.id] = true;
        creatives.push(creative);
      }
      if (creative && creative.materialCode && !seenMaterial[creative.materialCode] && materialMap[creative.materialCode]) {
        seenMaterial[creative.materialCode] = true;
        materials.push(materialMap[creative.materialCode]);
      }
    });
    (state.consoleData.creatives || []).forEach(function (creative) {
      if (creative.unitCode !== unit.unitCode) {
        return;
      }
      if (!seenCreative[creative.id]) {
        seenCreative[creative.id] = true;
        creatives.push(creative);
      }
      if (creative.materialCode && !seenMaterial[creative.materialCode] && materialMap[creative.materialCode]) {
        seenMaterial[creative.materialCode] = true;
        materials.push(materialMap[creative.materialCode]);
      }
    });
    return {
      relations: relations,
      positions: positions,
      creatives: creatives,
      materials: materials,
      fatigueRules: (state.consoleData.fatigueRules || []).filter(function (rule) {
        return String(rule.entityType || '').toUpperCase() === 'UNIT' && rule.entityCode === unit.unitCode;
      }),
      targetingRules: (state.consoleData.targetingRules || []).filter(function (rule) {
        return String(rule.entityType || '').toUpperCase() === 'UNIT' && rule.entityCode === unit.unitCode;
      })
    };
  }

  function renderUnitMaterialCreativeBlock(unit, context, options) {
    var editable = !options || options.editable !== false;
    var materialOptions = state.consoleData.materials || [];
    var html = '';
    html += '<div class="deliver-position-relations">';
    html += '<h4 class="manager-panel-title">关联创意</h4>';
    if (editable) {
      html += '<div class="deliver-inline-form-row">';
      html += '<select id="unitMaterialBindSelect" class="form-input deliver-inline-select">';
      html += renderCodeOptions(materialOptions, 'materialCode', 'materialName', '');
      html += '</select>';
      html += '<button class="manager-btn primary" type="button" data-action="bind-unit-material" data-id="' + escapeHtml(unit.id) + '">关联素材并生成创意</button>';
      html += '</div>';
    }

    if (!context.creatives.length) {
      html += '<div class="manager-coupon-tip">当前单元暂无创意。关联素材后将自动生成创意实体。</div>';
      html += '</div>';
      return html;
    }

    var materialMap = createCodeMap(state.consoleData.materials || [], 'materialCode');
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>创意</th><th>缩略图</th><th>状态</th><th>操作</th></tr></thead><tbody>';
    html += context.creatives.map(function (creative) {
      var material = materialMap[creative.materialCode] || null;
      var previewUrl = resolveCreativePreviewUrl(creative, material);
      var actionCell = '<button class="manager-btn" type="button" data-action="view-creative" data-id="' + escapeHtml(creative.id) + '">查看创意</button>';
      if (editable) {
        var statusCode = String(creative.status || '').toUpperCase();
        if (statusCode === 'OFFLINE') {
          actionCell += '<button class="manager-btn" type="button" data-action="online-unit-creative" data-unit-id="' + escapeHtml(unit.id) + '" data-creative-id="' + escapeHtml(creative.id) + '">再上线</button>';
        } else {
          actionCell += '<button class="manager-btn" type="button" data-action="offline-unit-creative" data-unit-id="' + escapeHtml(unit.id) + '" data-creative-id="' + escapeHtml(creative.id) + '">下线创意</button>';
        }
        actionCell += '<button class="manager-btn" type="button" data-action="delete-unit-creative" data-unit-id="' + escapeHtml(unit.id) + '" data-creative-id="' + escapeHtml(creative.id) + '">删除创意</button>';
      }
      return '<tr>' +
        '<td><span class="deliver-inline-code">' + escapeHtml(buildCodeLabel(creative.creativeCode, creative.creativeName, creative.id)) + '</span></td>' +
        '<td>' + renderPreviewThumbnail(previewUrl, creative.creativeName || creative.creativeCode || '创意预览') + '</td>' +
        '<td>' + renderStatusBadge(creative.status) + '</td>' +
        '<td class="deliver-table-actions">' + actionCell + '</td>' +
      '</tr>';
    }).join('');
    html += '</tbody></table></div>';
    html += '</div>';
    return html;
  }

  function resolveCreativePreviewUrl(creative, material) {
    if (creative && creative.previewImage) {
      return creative.previewImage;
    }
    if (material && material.previewImage) {
      return material.previewImage;
    }
    if (material && material.imageUrl) {
      return material.imageUrl;
    }
    return '';
  }

  function renderScopedRuleBlock(scope, entityCode, fatigueRules, targetingRules, options) {
    var editable = !options || options.editable !== false;
    var isUnitScope = scope === 'unit';
    var fatigueCreateAction = isUnitScope ? 'create-unit-fatigue' : 'create-creative-fatigue';
    var targetingCreateAction = isUnitScope ? 'create-unit-targeting' : 'create-creative-targeting';
    var html = '';
    html += '<div class="deliver-position-relations">';
    html += '<h4 class="manager-panel-title">规则配置</h4>';

    html += '<div class="deliver-rule-scope-row">';
    html += '<span class="deliver-rule-scope-label">疲劳度规则</span>';
    if (editable) {
      html += '<button class="manager-btn" type="button" data-action="' + escapeHtml(fatigueCreateAction) + '" data-entity-code="' + escapeHtml(entityCode || '') + '">新增规则</button>';
    }
    html += '</div>';
    if (!fatigueRules || !fatigueRules.length) {
      html += '<div class="manager-coupon-tip">暂无疲劳度规则。</div>';
    } else {
      html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
      html += '<thead><tr><th>规则编码</th><th>规则名称</th><th>条件</th>' + (editable ? '<th>操作</th>' : '') + '</tr></thead><tbody>';
      html += fatigueRules.map(function (rule) {
        var operationCell = editable
          ? '<td class="deliver-table-actions"><button class="manager-btn" type="button" data-action="edit-fatigue" data-id="' + escapeHtml(rule.id) + '">修改</button></td>'
          : '';
        return '<tr>' +
          '<td><span class="deliver-inline-code">' + escapeHtml(rule.fatigueCode || '-') + '</span></td>' +
          '<td>' + escapeHtml(rule.ruleName || '-') + '</td>' +
          '<td>' + escapeHtml(formatDeliverEnumText(rule.eventType) + ' / ' + String(rule.timeWindowMinutes == null ? '-' : rule.timeWindowMinutes + 'min') + ' / 上限 ' + String(rule.maxCount == null ? '-' : rule.maxCount)) + '</td>' +
          operationCell +
        '</tr>';
      }).join('');
      html += '</tbody></table></div>';
    }

    html += '<div class="deliver-rule-scope-row" style="margin-top:10px;">';
    html += '<span class="deliver-rule-scope-label">定向规则</span>';
    if (editable) {
      html += '<button class="manager-btn" type="button" data-action="' + escapeHtml(targetingCreateAction) + '" data-entity-code="' + escapeHtml(entityCode || '') + '">新增规则</button>';
    }
    html += '</div>';
    if (!targetingRules || !targetingRules.length) {
      html += '<div class="manager-coupon-tip">暂无定向规则。</div>';
    } else {
      html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
      html += '<thead><tr><th>规则编码</th><th>类型</th><th>匹配</th>' + (editable ? '<th>操作</th>' : '') + '</tr></thead><tbody>';
      html += targetingRules.map(function (rule) {
        var operationCell = editable
          ? '<td class="deliver-table-actions"><button class="manager-btn" type="button" data-action="edit-targeting" data-id="' + escapeHtml(rule.id) + '">修改</button></td>'
          : '';
        return '<tr>' +
          '<td><span class="deliver-inline-code">' + escapeHtml(rule.ruleCode || '-') + '</span></td>' +
          '<td>' + escapeHtml(formatDeliverEnumText(rule.targetingType)) + '</td>' +
          '<td>' + escapeHtml(formatDeliverEnumText(rule.operator) + ' / ' + String(rule.targetingValue || '-')) + '</td>' +
          operationCell +
        '</tr>';
      }).join('');
      html += '</tbody></table></div>';
    }
    html += '</div>';
    return html;
  }

  function openUnitPanel(mode, unitId) {
    closeAllPanels();
    state.unitPanel.mode = mode;
    state.unitPanel.unitId = unitId == null ? null : Number(unitId);
  }

  function closeUnitPanel() {
    state.unitPanel.mode = 'hidden';
    state.unitPanel.unitId = null;
    state.editing.unitId = null;
  }

  function renderMaterialSection() {
    var items = state.consoleData.materials || [];
    var html = '';
    html += '<section id="deliverMaterialSection" class="manager-panel deliver-section">';
    html += '<div class="deliver-section-head">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">素材列表</h3>';
    html += '<div class="deliver-section-desc">维护素材内容，供投放单元关联后自动生成创意。</div>';
    html += '</div>';
    html += '<div class="deliver-toolbar-actions">';
    html += '<span class="deliver-section-meta">' + escapeHtml(items.length) + '</span>';
    html += '<button class="manager-btn primary" type="button" data-action="create-material">新建素材</button>';
    html += '</div>';
    html += '</div>';
    html += renderMaterialTable(items);
    html += '</section>';
    return html;
  }


function renderMaterialDetailSection() {
  var panel = state.materialPanel || { mode: 'hidden', materialId: null };
  if (!panel.mode || panel.mode === 'hidden') {
    return '';
  }
  var item = findItemById(state.consoleData.materials || [], panel.materialId);
  var isCreate = panel.mode === 'create';
  var isEdit = panel.mode === 'edit' || isCreate;
  var titleHtml = '';
  var descHtml = '';
  var actionsHtml = '';
  var bodyHtml = '';

  if (isCreate) {
    titleHtml = '新建素材';
    descHtml = '新建素材并维护内容配置。';
  } else if (isEdit && item) {
    titleHtml = '编辑素材 #' + escapeHtml(item.id);
    descHtml = '修改当前素材配置。';
  } else if (item) {
    titleHtml = '素材详情 #' + escapeHtml(item.id);
    descHtml = '查看内容配置与引用关系。';
    actionsHtml = '<button class="manager-btn" type="button" data-action="edit-material" data-id="' + escapeHtml(item.id) + '">修改素材</button>';
  } else {
    titleHtml = '素材详情';
    descHtml = '未找到所选素材，请刷新后重试。';
  }

  if (!isCreate && !item) {
    bodyHtml = '<div class="manager-placeholder"><p class="manager-placeholder-tip">未找到该素材，请刷新后重试。</p></div>';
  } else if (isEdit) {
    bodyHtml = renderMaterialForm(item);
  } else {
    bodyHtml = renderMaterialDetail(item);
  }

  return renderDeliverModal({
    id: 'deliverMaterialDetailSection',
    titleHtml: titleHtml,
    descHtml: descHtml,
    closeAction: 'close-material-panel',
    actionsHtml: actionsHtml,
    bodyHtml: bodyHtml,
  });
}

  function renderMaterialDetail(item) {
    var context = buildMaterialDetailContext(item);
    var html = '';
    html += '<div class="deliver-position-detail-grid">';
    html += renderPositionDetailBlock('基础信息', renderPositionDetailItems([
      { label: '素材编码', valueHtml: '<span class="deliver-inline-code">' + escapeHtml(item.materialCode || '-') + '</span>' },
      { label: '素材名称', valueHtml: escapeHtml(item.materialName || '-') },
      { label: '素材类型', valueHtml: escapeHtml(formatDeliverEnumText(item.materialType)) },
      { label: '素材标题', valueHtml: escapeHtml(item.title || '-') },
      { label: '状态', valueHtml: renderStatusBadge(item.status) },
      { label: '生效窗口', valueHtml: escapeHtml(formatWindow(item.activeFrom, item.activeTo)) }
    ]));
    html += renderPositionDetailBlock('内容配置', renderPositionDetailItems([
      { label: '图片地址', valueHtml: renderPreviewLink(item.imageUrl) },
      { label: '预览图', valueHtml: renderPreviewLink(item.previewImage || item.imageUrl) },
      { label: '落地页', valueHtml: '<span class="deliver-inline-code">' + escapeHtml(item.landingUrl || '-') + '</span>' },
      { label: 'Schema', valueHtml: '<span class="deliver-inline-code">' + escapeHtml(item.schemaJson || '-') + '</span>' }
    ]));
    html += renderPositionDetailBlock('关联资源', renderPositionDetailItems([
      { label: '关联创意', valueHtml: renderPositionTagList(context.creatives, 'creativeCode', 'creativeName') },
      { label: '投放单元', valueHtml: renderPositionTagList(context.units, 'unitCode', 'unitName') },
      { label: '关联展位', valueHtml: renderPositionTagList(context.positions, 'positionCode', 'positionName') }
    ]));
    html += '</div>';
    return html;
  }

  function buildMaterialDetailContext(material) {
    var creatives = (state.consoleData.creatives || []).filter(function (item) {
      return item.materialCode === material.materialCode;
    });
    var relations = state.consoleData.relations || [];
    var units = [];
    var positions = [];
    var unitMapByCode = {};
    var unitMapById = createIdMap(state.consoleData.units || [], 'id');
    var positionMap = createIdMap(state.consoleData.positions || [], 'id');
    (state.consoleData.units || []).forEach(function (item) {
      unitMapByCode[item.unitCode] = item;
    });
    var creativeIdSet = {};
    var seenUnit = {};
    var seenPosition = {};
    creatives.forEach(function (creative) {
      creativeIdSet[creative.id] = true;
      var unit = unitMapByCode[creative.unitCode];
      if (unit && !seenUnit[unit.id]) {
        seenUnit[unit.id] = true;
        units.push(unit);
      }
    });
    relations.forEach(function (relation) {
      if (!creativeIdSet[relation.creativeId]) {
        return;
      }
      var position = positionMap[relation.positionId];
      var unit = unitMapById[relation.unitId];
      if (position && !seenPosition[position.id]) {
        seenPosition[position.id] = true;
        positions.push(position);
      }
      if (unit && !seenUnit[unit.id]) {
        seenUnit[unit.id] = true;
        units.push(unit);
      }
    });
    return {
      creatives: creatives,
      units: units,
      positions: positions
    };
  }

  function openMaterialPanel(mode, materialId) {
    closeAllPanels();
    state.materialPanel.mode = mode;
    state.materialPanel.materialId = materialId == null ? null : Number(materialId);
  }

  function closeMaterialPanel() {
    state.materialPanel.mode = 'hidden';
    state.materialPanel.materialId = null;
    state.editing.materialId = null;
  }

  function renderCreativeSection() {
    var items = state.consoleData.creatives || [];
    var html = '';
    html += '<section id="deliverCreativeSection" class="manager-panel deliver-section">';
    html += '<div class="deliver-section-head">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">创意列表</h3>';
    html += '<div class="deliver-section-desc">主界面仅展示创意列表，详情与编辑以弹窗打开。</div>';
    html += '</div>';
    html += '<div class="deliver-toolbar-actions">';
    html += '<span class="deliver-section-meta">' + escapeHtml(items.length) + '</span>';
    html += '<button class="manager-btn primary" type="button" data-action="create-creative">新建创意</button>';
    html += '</div>';
    html += '</div>';
    html += renderCreativeTable(items);
    html += '</section>';
    return html;
  }


function renderCreativeDetailSection() {
  var panel = state.creativePanel || { mode: 'hidden', creativeId: null };
  if (!panel.mode || panel.mode === 'hidden') {
    return '';
  }
  var item = findItemById(state.consoleData.creatives || [], panel.creativeId);
  var isCreate = panel.mode === 'create';
  var isEdit = panel.mode === 'edit' || isCreate;
  var titleHtml = '';
  var descHtml = '';
  var actionsHtml = '';
  var bodyHtml = '';

  if (isCreate) {
    titleHtml = '新建创意';
    descHtml = '新建创意并绑定单元与素材。';
  } else if (isEdit && item) {
    titleHtml = '编辑创意 #' + escapeHtml(item.id);
    descHtml = '修改当前创意配置。';
  } else if (item) {
    titleHtml = '创意详情 #' + escapeHtml(item.id);
    descHtml = '查看关联位置、单元、素材与规则。';
    actionsHtml = '<button class="manager-btn" type="button" data-action="edit-creative" data-id="' + escapeHtml(item.id) + '">修改创意</button>';
  } else {
    titleHtml = '创意详情';
    descHtml = '未找到所选创意，请刷新后重试。';
  }

  if (!isCreate && !item) {
    bodyHtml = '<div class="manager-placeholder"><p class="manager-placeholder-tip">未找到该创意，请刷新后重试。</p></div>';
  } else if (isEdit) {
    bodyHtml = renderCreativeForm(item);
  } else {
    bodyHtml = renderCreativeDetail(item);
  }

  return renderDeliverModal({
    id: 'deliverCreativeDetailSection',
    titleHtml: titleHtml,
    descHtml: descHtml,
    closeAction: 'close-creative-panel',
    actionsHtml: actionsHtml,
    bodyHtml: bodyHtml,
  });
}

  function renderCreativeDetail(item) {
    var context = buildCreativeDetailContext(item);
    var html = '';
    html += '<div class="deliver-position-detail-grid">';
    html += renderPositionDetailBlock('基础信息', renderPositionDetailItems([
      { label: '创意编码', valueHtml: '<span class="deliver-inline-code">' + escapeHtml(item.creativeCode || '-') + '</span>' },
      { label: '创意名称', valueHtml: escapeHtml(item.creativeName || '-') },
      { label: '所属单元', valueHtml: context.unit ? '<span class="deliver-inline-code">' + escapeHtml(buildCodeLabel(context.unit.unitCode, context.unit.unitName, context.unit.id)) + '</span>' : escapeHtml(item.unitCode || '-') },
      { label: '绑定素材', valueHtml: context.material ? '<span class="deliver-inline-code">' + escapeHtml(buildCodeLabel(context.material.materialCode, context.material.materialName, context.material.id)) + '</span>' : escapeHtml(item.materialCode || '-') },
      { label: '优先级', valueHtml: escapeHtml(item.priority == null ? '-' : item.priority) },
      { label: '权重', valueHtml: escapeHtml(item.weight == null ? '-' : item.weight) },
      { label: '兜底', valueHtml: escapeHtml(formatBooleanText(item.fallback)) },
      { label: '状态', valueHtml: renderStatusBadge(item.status) },
      { label: '生效窗口', valueHtml: escapeHtml(formatWindow(item.activeFrom, item.activeTo)) }
    ]));
    html += renderPositionDetailBlock('投放配置', renderPositionDetailItems([
      { label: '落地页', valueHtml: '<span class="deliver-inline-code">' + escapeHtml(item.landingUrl || '-') + '</span>' },
      { label: 'Schema', valueHtml: '<span class="deliver-inline-code">' + escapeHtml(item.schemaJson || '-') + '</span>' },
      { label: '预览图', valueHtml: renderPreviewLink(item.previewImage) },
      { label: '关联展位', valueHtml: renderPositionTagList(context.positions, 'positionCode', 'positionName') }
    ]));
    html += '</div>';
    html += renderScopedRuleBlock('creative', item.creativeCode, context.fatigueRules, context.targetingRules);
    return html;
  }

  function buildCreativeDetailContext(creative) {
    var unit = null;
    var material = null;
    var positions = [];
    var seenPosition = {};
    (state.consoleData.units || []).forEach(function (item) {
      if (!unit && item.unitCode === creative.unitCode) {
        unit = item;
      }
    });
    (state.consoleData.materials || []).forEach(function (item) {
      if (!material && item.materialCode === creative.materialCode) {
        material = item;
      }
    });
    var positionMap = createIdMap(state.consoleData.positions || [], 'id');
    (state.consoleData.relations || []).forEach(function (relation) {
      if (Number(relation.creativeId) !== Number(creative.id)) {
        return;
      }
      var position = positionMap[relation.positionId];
      if (position && !seenPosition[position.id]) {
        seenPosition[position.id] = true;
        positions.push(position);
      }
    });
    return {
      unit: unit,
      material: material,
      positions: positions,
      fatigueRules: (state.consoleData.fatigueRules || []).filter(function (rule) {
        return String(rule.entityType || '').toUpperCase() === 'CREATIVE' && rule.entityCode === creative.creativeCode;
      }),
      targetingRules: (state.consoleData.targetingRules || []).filter(function (rule) {
        return String(rule.entityType || '').toUpperCase() === 'CREATIVE' && rule.entityCode === creative.creativeCode;
      })
    };
  }

  function openCreativePanel(mode, creativeId) {
    closeAllPanels();
    state.creativePanel.mode = mode;
    state.creativePanel.creativeId = creativeId == null ? null : Number(creativeId);
  }

  function closeCreativePanel() {
    state.creativePanel.mode = 'hidden';
    state.creativePanel.creativeId = null;
    state.editing.creativeId = null;
  }

  function renderRelationSection() {
    var items = state.consoleData.relations || [];
    var html = '';
    html += '<section id="deliverRelationSection" class="manager-panel deliver-section">';
    html += '<div class="deliver-section-head">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">关系列表</h3>';
    html += '<div class="deliver-section-desc">主界面仅展示关系列表，详情与编辑以弹窗打开。</div>';
    html += '</div>';
    html += '<div class="deliver-toolbar-actions">';
    html += '<span class="deliver-section-meta">' + escapeHtml(items.length) + '</span>';
    html += '<button class="manager-btn primary" type="button" data-action="create-relation">新建关系</button>';
    html += '</div>';
    html += '</div>';
    html += renderRelationTable(items);
    html += '</section>';
    return html;
  }


function renderRelationDetailSection() {
  var panel = state.relationPanel || { mode: 'hidden', relationId: null };
  if (!panel.mode || panel.mode === 'hidden') {
    return '';
  }
  var item = findItemById(state.consoleData.relations || [], panel.relationId);
  var isCreate = panel.mode === 'create';
  var isEdit = panel.mode === 'edit' || isCreate;
  var titleHtml = '';
  var descHtml = '';
  var actionsHtml = '';
  var bodyHtml = '';

  if (isCreate) {
    titleHtml = '新建关系';
    descHtml = '新建展位、单元与创意的链路关系。';
  } else if (isEdit && item) {
    titleHtml = '编辑关系 #' + escapeHtml(item.id);
    descHtml = '修改当前关系配置。';
  } else if (item) {
    titleHtml = '关系详情 #' + escapeHtml(item.id);
    descHtml = '查看当前关系连接的资源。';
    actionsHtml = '<button class="manager-btn" type="button" data-action="edit-relation" data-id="' + escapeHtml(item.id) + '">修改关系</button>';
  } else {
    titleHtml = '关系详情';
    descHtml = '未找到所选关系，请刷新后重试。';
  }

  if (!isCreate && !item) {
    bodyHtml = '<div class="manager-placeholder"><p class="manager-placeholder-tip">未找到该关系，请刷新后重试。</p></div>';
  } else if (isEdit) {
    bodyHtml = renderRelationForm(item);
  } else {
    bodyHtml = renderRelationDetail(item);
  }

  return renderDeliverModal({
    id: 'deliverRelationDetailSection',
    titleHtml: titleHtml,
    descHtml: descHtml,
    closeAction: 'close-relation-panel',
    actionsHtml: actionsHtml,
    bodyHtml: bodyHtml,
  });
}

  function renderRelationDetail(item) {
    var context = buildRelationDetailContext(item);
    var html = '';
    html += '<div class="deliver-position-detail-grid">';
    html += renderPositionDetailBlock('链路信息', renderPositionDetailItems([
      { label: '关系ID', valueHtml: escapeHtml(item.id) },
      { label: '展位', valueHtml: renderResolvedEntityValue('POSITION', context.position ? context.position.positionCode : null) },
      { label: '投放单元', valueHtml: renderResolvedEntityValue('UNIT', context.unit ? context.unit.unitCode : null) },
      { label: '创意', valueHtml: renderResolvedEntityValue('CREATIVE', context.creative ? context.creative.creativeCode : null) },
      { label: '素材', valueHtml: context.creative && context.creative.materialCode ? '<span class="deliver-inline-code">' + escapeHtml(context.creative.materialCode) + '</span>' : '<span class="deliver-muted">暂无</span>' }
    ]));
    html += renderPositionDetailBlock('投放配置', renderPositionDetailItems([
      { label: '展示顺序', valueHtml: escapeHtml(item.displayOrder == null ? '-' : item.displayOrder) },
      { label: '兜底', valueHtml: escapeHtml(formatBooleanText(item.fallback)) },
      { label: '启用', valueHtml: escapeHtml(formatBooleanText(item.enabled)) }
    ]));
    html += '</div>';
    return html;
  }

  function buildRelationDetailContext(relation) {
    var positionMap = createIdMap(state.consoleData.positions || [], 'id');
    var unitMap = createIdMap(state.consoleData.units || [], 'id');
    var creativeMap = createIdMap(state.consoleData.creatives || [], 'id');
    return {
      position: positionMap[relation.positionId] || null,
      unit: unitMap[relation.unitId] || null,
      creative: creativeMap[relation.creativeId] || null
    };
  }

  function openRelationPanel(mode, relationId) {
    closeAllPanels();
    state.relationPanel.mode = mode;
    state.relationPanel.relationId = relationId == null ? null : Number(relationId);
  }

  function closeRelationPanel() {
    state.relationPanel.mode = 'hidden';
    state.relationPanel.relationId = null;
    state.editing.relationId = null;
  }

  function renderFatigueSection() {
    var items = state.consoleData.fatigueRules || [];
    var html = '';
    html += '<section id="deliverFatigueSection" class="manager-panel deliver-section">';
    html += '<div class="deliver-section-head">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">疲劳规则列表</h3>';
    html += '<div class="deliver-section-desc">主界面仅展示疲劳规则列表，详情与编辑以弹窗打开。</div>';
    html += '</div>';
    html += '<div class="deliver-toolbar-actions">';
    html += '<span class="deliver-section-meta">' + escapeHtml(items.length) + '</span>';
    html += '<button class="manager-btn primary" type="button" data-action="create-fatigue">新建疲劳规则</button>';
    html += '</div>';
    html += '</div>';
    html += renderFatigueTable(items);
    html += '</section>';
    return html;
  }


function renderFatigueDetailSection() {
  var panel = state.fatiguePanel || { mode: 'hidden', fatigueRuleId: null };
  if (!panel.mode || panel.mode === 'hidden') {
    return '';
  }
  var item = findItemById(state.consoleData.fatigueRules || [], panel.fatigueRuleId);
  var isCreate = panel.mode === 'create';
  var isEdit = panel.mode === 'edit' || isCreate;
  var titleHtml = '';
  var descHtml = '';
  var actionsHtml = '';
  var bodyHtml = '';

  if (isCreate) {
    titleHtml = '新建疲劳规则';
    descHtml = '新建疲劳规则并限制触达次数。';
  } else if (isEdit && item) {
    titleHtml = '编辑疲劳规则 #' + escapeHtml(item.id);
    descHtml = '修改当前疲劳规则配置。';
  } else if (item) {
    titleHtml = '疲劳规则详情 #' + escapeHtml(item.id);
    descHtml = '查看作用对象与频控参数。';
    actionsHtml = '<button class="manager-btn" type="button" data-action="edit-fatigue" data-id="' + escapeHtml(item.id) + '">修改规则</button>';
  } else {
    titleHtml = '疲劳规则详情';
    descHtml = '未找到所选疲劳规则，请刷新后重试。';
  }

  if (!isCreate && !item) {
    bodyHtml = '<div class="manager-placeholder"><p class="manager-placeholder-tip">未找到该疲劳规则，请刷新后重试。</p></div>';
  } else if (isEdit) {
    bodyHtml = renderFatigueForm(item);
  } else {
    bodyHtml = renderFatigueDetail(item);
  }

  return renderDeliverModal({
    id: 'deliverFatigueDetailSection',
    titleHtml: titleHtml,
    descHtml: descHtml,
    closeAction: 'close-fatigue-panel',
    actionsHtml: actionsHtml,
    bodyHtml: bodyHtml,
  });
}

  function renderFatigueDetail(item) {
    var html = '';
    html += '<div class="deliver-position-detail-grid">';
    html += renderPositionDetailBlock('规则信息', renderPositionDetailItems([
      { label: '规则编码', valueHtml: '<span class="deliver-inline-code">' + escapeHtml(item.fatigueCode || '-') + '</span>' },
      { label: '规则名称', valueHtml: escapeHtml(item.ruleName || '-') },
      { label: '实体类型', valueHtml: escapeHtml(formatDeliverEnumText(item.entityType)) },
      { label: '作用实体', valueHtml: renderResolvedEntityValue(item.entityType, item.entityCode) },
      { label: '事件类型', valueHtml: escapeHtml(formatDeliverEnumText(item.eventType)) },
      { label: '时间窗口', valueHtml: escapeHtml(item.timeWindowMinutes == null ? '-' : item.timeWindowMinutes + ' min') },
      { label: '次数上限', valueHtml: escapeHtml(item.maxCount == null ? '-' : item.maxCount) },
      { label: '启用', valueHtml: escapeHtml(formatBooleanText(item.enabled)) }
    ]));
    html += '</div>';
    return html;
  }

  function openFatiguePanel(mode, fatigueRuleId) {
    closeAllPanels();
    state.fatiguePanel.mode = mode;
    state.fatiguePanel.fatigueRuleId = fatigueRuleId == null ? null : Number(fatigueRuleId);
  }

  function closeFatiguePanel() {
    state.fatiguePanel.mode = 'hidden';
    state.fatiguePanel.fatigueRuleId = null;
    state.editing.fatigueRuleId = null;
    state.scopedRuleDraft.entityType = '';
    state.scopedRuleDraft.entityCode = '';
  }

  function renderTargetingSection() {
    var items = state.consoleData.targetingRules || [];
    var html = '';
    html += '<section id="deliverTargetingSection" class="manager-panel deliver-section">';
    html += '<div class="deliver-section-head">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">定向规则列表</h3>';
    html += '<div class="deliver-section-desc">主界面仅展示定向规则列表，详情与编辑以弹窗打开。</div>';
    html += '</div>';
    html += '<div class="deliver-toolbar-actions">';
    html += '<span class="deliver-section-meta">' + escapeHtml(items.length) + '</span>';
    html += '<button class="manager-btn primary" type="button" data-action="create-targeting">新建定向规则</button>';
    html += '</div>';
    html += '</div>';
    html += renderTargetingTable(items);
    html += '</section>';
    return html;
  }


function renderTargetingDetailSection() {
  var panel = state.targetingPanel || { mode: 'hidden', targetingRuleId: null };
  if (!panel.mode || panel.mode === 'hidden') {
    return '';
  }
  var item = findItemById(state.consoleData.targetingRules || [], panel.targetingRuleId);
  var isCreate = panel.mode === 'create';
  var isEdit = panel.mode === 'edit' || isCreate;
  var titleHtml = '';
  var descHtml = '';
  var actionsHtml = '';
  var bodyHtml = '';

  if (isCreate) {
    titleHtml = '新建定向规则';
    descHtml = '新建定向规则并限定投放条件。';
  } else if (isEdit && item) {
    titleHtml = '编辑定向规则 #' + escapeHtml(item.id);
    descHtml = '修改当前定向规则配置。';
  } else if (item) {
    titleHtml = '定向规则详情 #' + escapeHtml(item.id);
    descHtml = '查看作用对象与匹配条件。';
    actionsHtml = '<button class="manager-btn" type="button" data-action="edit-targeting" data-id="' + escapeHtml(item.id) + '">修改规则</button>';
  } else {
    titleHtml = '定向规则详情';
    descHtml = '未找到所选定向规则，请刷新后重试。';
  }

  if (!isCreate && !item) {
    bodyHtml = '<div class="manager-placeholder"><p class="manager-placeholder-tip">未找到该定向规则，请刷新后重试。</p></div>';
  } else if (isEdit) {
    bodyHtml = renderTargetingForm(item);
  } else {
    bodyHtml = renderTargetingDetail(item);
  }

  return renderDeliverModal({
    id: 'deliverTargetingDetailSection',
    titleHtml: titleHtml,
    descHtml: descHtml,
    closeAction: 'close-targeting-panel',
    actionsHtml: actionsHtml,
    bodyHtml: bodyHtml,
  });
}

  function renderTargetingDetail(item) {
    var html = '';
    html += '<div class="deliver-position-detail-grid">';
    html += renderPositionDetailBlock('规则信息', renderPositionDetailItems([
      { label: '规则编码', valueHtml: '<span class="deliver-inline-code">' + escapeHtml(item.ruleCode || '-') + '</span>' },
      { label: '实体类型', valueHtml: escapeHtml(formatDeliverEnumText(item.entityType)) },
      { label: '作用实体', valueHtml: renderResolvedEntityValue(item.entityType, item.entityCode) },
      { label: '定向类型', valueHtml: escapeHtml(formatDeliverEnumText(item.targetingType)) },
      { label: '操作符', valueHtml: escapeHtml(formatDeliverEnumText(item.operator)) },
      { label: '定向值', valueHtml: '<span class="deliver-inline-code">' + escapeHtml(item.targetingValue || '-') + '</span>' },
      { label: '启用', valueHtml: escapeHtml(formatBooleanText(item.enabled)) }
    ]));
    html += '</div>';
    return html;
  }

  function openTargetingPanel(mode, targetingRuleId) {
    closeAllPanels();
    state.targetingPanel.mode = mode;
    state.targetingPanel.targetingRuleId = targetingRuleId == null ? null : Number(targetingRuleId);
  }

  function closeTargetingPanel() {
    state.targetingPanel.mode = 'hidden';
    state.targetingPanel.targetingRuleId = null;
    state.editing.targetingRuleId = null;
    state.scopedRuleDraft.entityType = '';
    state.scopedRuleDraft.entityCode = '';
  }

  function resolveEntityRecord(entityType, entityCode) {
    var normalizedType = String(entityType || '').toUpperCase();
    var normalizedCode = String(entityCode || '');
    if (!normalizedCode) {
      return null;
    }
    if (normalizedType === 'POSITION') {
      return (state.consoleData.positions || []).find(function (item) { return item.positionCode === normalizedCode; }) || null;
    }
    if (normalizedType === 'UNIT') {
      return (state.consoleData.units || []).find(function (item) { return item.unitCode === normalizedCode; }) || null;
    }
    if (normalizedType === 'CREATIVE') {
      return (state.consoleData.creatives || []).find(function (item) { return item.creativeCode === normalizedCode; }) || null;
    }
    return null;
  }

  function renderResolvedEntityValue(entityType, entityCode) {
    var normalizedCode = String(entityCode || '');
    if (!normalizedCode) {
      return '<span class="deliver-muted">暂无</span>';
    }
    var entity = resolveEntityRecord(entityType, entityCode);
    var normalizedType = String(entityType || '').toUpperCase();
    if (!entity) {
      return '<span class="deliver-inline-code">' + escapeHtml(normalizedCode) + '</span>';
    }
    if (normalizedType === 'POSITION') {
      return '<span class="deliver-inline-code">' + escapeHtml(buildCodeLabel(entity.positionCode, entity.positionName, entity.id)) + '</span>';
    }
    if (normalizedType === 'UNIT') {
      return '<span class="deliver-inline-code">' + escapeHtml(buildCodeLabel(entity.unitCode, entity.unitName, entity.id)) + '</span>';
    }
    if (normalizedType === 'CREATIVE') {
      return '<span class="deliver-inline-code">' + escapeHtml(buildCodeLabel(entity.creativeCode, entity.creativeName, entity.id)) + '</span>';
    }
    return '<span class="deliver-inline-code">' + escapeHtml(normalizedCode) + '</span>';
  }

  function renderSectionLayout(section) {
    var html = '';
    html += '<section id="' + escapeHtml(section.anchor) + '" class="manager-panel deliver-section">';
    html += '<div class="deliver-section-head">';
    html += '<div>';
    html += '<h3 class="manager-panel-title">' + escapeHtml(section.title) + '</h3>';
    html += '<div class="deliver-section-desc">' + escapeHtml(section.desc) + '</div>';
    html += '</div>';
    html += '<div class="deliver-section-meta">' + escapeHtml(section.count) + '</div>';
    html += '</div>';
    html += '<div class="deliver-section-grid">';
    html += '<section id="' + escapeHtml(section.formId) + '" class="manager-panel deliver-form-card">' + section.formHtml + '</section>';
    html += '<section class="manager-panel deliver-table-card">' + section.tableHtml + '</section>';
    html += '</div>';
    html += '</section>';
    return html;
  }

  function renderPositionForm(item) {
    var publishedAt = item ? toInputDateTime(item.publishedAt) : buildDateTimeLocal(0);
    var html = '';
    html += renderFormTitle('position');
    html += '<div class="manager-coupon-form-grid">';
    html += renderField('展位编码', '<input id="positionCode" class="form-input" value="' + escapeHtml(item ? item.positionCode : '') + '" placeholder="例如 HOME_COMMON_BANNER"/>');
    html += renderField('展位名称', '<input id="positionName" class="form-input" value="' + escapeHtml(item ? item.positionName : '') + '" placeholder="例如 首页腰封横幅"/>');
    html += renderField('展位类型', '<select id="positionType" class="form-input">' + renderOptions(['BANNER', 'POPUP', 'COMMON'], item ? item.positionType : 'BANNER') + '</select>');
    html += renderField('状态', '<select id="positionStatus" class="form-input">' + renderOptions(['EDITING', 'PUBLISHED', 'OFFLINE'], item ? item.status : 'PUBLISHED') + '</select>');
    html += renderField('轮播秒数', '<input id="positionSlideInterval" class="form-input" value="' + escapeHtml(item && item.slideInterval != null ? item.slideInterval : '5') + '" placeholder="例如 5"/>');
    html += renderField('最大返回数', '<input id="positionMaxDisplayCount" class="form-input" value="' + escapeHtml(item && item.maxDisplayCount != null ? item.maxDisplayCount : '3') + '" placeholder="例如 3"/>');
    html += renderField('排序方式', '<select id="positionSortType" class="form-input">' + renderOptions(['MANUAL', 'PRIORITY', 'WEIGHT'], item ? item.sortType : 'MANUAL') + '</select>');
    html += renderField('排序规则', '<input id="positionSortRule" class="form-input" value="' + escapeHtml(item ? item.sortRule : '') + '" placeholder="可空，如 hand-picked"/>');
    html += renderField('兜底开关', '<select id="positionNeedFallback" class="form-input">' + renderBooleanOptions(item ? item.needFallback : true) + '</select>');
    html += renderField('发布时间', '<input id="positionPublishedAt" type="datetime-local" class="form-input" value="' + escapeHtml(publishedAt) + '"/>');
    html += renderField('预览图', '<input id="positionPreviewImage" class="form-input" value="' + escapeHtml(item ? item.previewImage : '') + '" placeholder="素材预览图 URL"/>');
    html += renderField('备注', '<input id="positionMemo" class="form-input" value="' + escapeHtml(item ? item.memo : '') + '" placeholder="例如 替换首页常用区块"/>');
    html += '</div>';
    html += renderFormActions('save-position', 'reset-position', !!item, '保存展位', '新建展位');
    if (item && item.id != null) {
      html += renderPositionUnitManageBlock(item);
    }
    return html;
  }

  function renderUnitForm(item) {

    var activeFrom = item ? toInputDateTime(item.activeFrom) : buildDateTimeLocal(0);
    var activeTo = item ? toInputDateTime(item.activeTo) : buildDateTimeLocal(180);
    var html = '';
    html += renderFormTitle('unit');
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('单元编码', '<input id="unitCode" class="form-input" value="' + escapeHtml(item ? item.unitCode : '') + '" placeholder="例如 UNIT_HOME_BANNER"/>');
    html += renderField('单元名称', '<input id="unitName" class="form-input" value="' + escapeHtml(item ? item.unitName : '') + '" placeholder="例如 首页横幅单元"/>');
    html += renderField('优先级', '<input id="unitPriority" class="form-input" value="' + escapeHtml(item && item.priority != null ? item.priority : '1') + '" placeholder="越小越优先"/>');
    html += renderField('状态', '<select id="unitStatus" class="form-input">' + renderOptions(['EDITING', 'PUBLISHED', 'OFFLINE'], item ? item.status : 'PUBLISHED') + '</select>');
    html += renderField('生效开始', '<input id="unitActiveFrom" type="datetime-local" class="form-input" value="' + escapeHtml(activeFrom) + '"/>');
    html += renderField('生效结束', '<input id="unitActiveTo" type="datetime-local" class="form-input" value="' + escapeHtml(activeTo) + '"/>');
    html += renderField('备注', '<input id="unitMemo" class="form-input" value="' + escapeHtml(item ? item.memo : '') + '" placeholder="单元策略说明"/>');
    html += '</div>';
    html += renderFormActions('save-unit', 'reset-unit', !!item, '保存单元', '新建单元');
    return html;
  }

  function renderMaterialForm(item) {
    var activeFrom = item ? toInputDateTime(item.activeFrom) : buildDateTimeLocal(0);
    var activeTo = item ? toInputDateTime(item.activeTo) : buildDateTimeLocal(180);
    var html = '';
    html += renderFormTitle('material');
    html += '<div class="manager-coupon-form-grid">';
    html += renderField('素材编码', '<input id="materialCode" class="form-input" value="' + escapeHtml(item ? item.materialCode : '') + '" placeholder="例如 MAT_HOME_BANNER_RED"/>');
    html += renderField('素材名称', '<input id="materialName" class="form-input" value="' + escapeHtml(item ? item.materialName : '') + '" placeholder="例如 首页横幅-红包会场红"/>');
    html += renderField('素材类型', '<select id="materialType" class="form-input">' + renderOptions(['IMAGE', 'VIDEO', 'TEXT'], item ? item.materialType : 'IMAGE') + '</select>');
    html += renderField('状态', '<select id="materialStatus" class="form-input">' + renderOptions(['EDITING', 'PUBLISHED', 'OFFLINE'], item ? item.status : 'PUBLISHED') + '</select>');
    html += renderField('素材标题', '<input id="materialTitle" class="form-input" value="' + escapeHtml(item ? item.title : '') + '" placeholder="例如 周末红包会场"/>');
    html += renderField('图片地址', '<input id="materialImageUrl" class="form-input" value="' + escapeHtml(item ? item.imageUrl : '') + '" placeholder="http(s)://..."/>');
    html += renderField('落地页', '<input id="materialLandingUrl" class="form-input" value="' + escapeHtml(item ? item.landingUrl : '') + '" placeholder="aipay://home?feature=deliver-home-banner"/>');
    html += renderField('预览图', '<input id="materialPreviewImage" class="form-input" value="' + escapeHtml(item ? item.previewImage : '') + '" placeholder="可空，默认可与图片地址一致"/>');
    html += renderField('生效开始', '<input id="materialActiveFrom" type="datetime-local" class="form-input" value="' + escapeHtml(activeFrom) + '"/>');
    html += renderField('生效结束', '<input id="materialActiveTo" type="datetime-local" class="form-input" value="' + escapeHtml(activeTo) + '"/>');
    html += '</div>';
    html += renderField('Schema JSON', '<textarea id="materialSchemaJson" class="deliver-textarea" placeholder="例如 {\"action\":\"stay-on-home\",\"campaign\":\"weekend-redpacket\"}">' + escapeHtml(item ? item.schemaJson : '') + '</textarea>');
    html += renderFormActions('save-material', 'reset-material', !!item, '保存素材', '新建素材');
    return html;
  }

  function renderCreativeForm(item) {
    var activeFrom = item ? toInputDateTime(item.activeFrom) : buildDateTimeLocal(0);
    var activeTo = item ? toInputDateTime(item.activeTo) : buildDateTimeLocal(180);
    var html = '';
    html += renderFormTitle('creative');
    html += '<div class="manager-coupon-form-grid">';
    html += renderField('创意编码', '<input id="creativeCode" class="form-input" value="' + escapeHtml(item ? item.creativeCode : '') + '" placeholder="例如 CRT_HOME_BANNER_RED"/>');
    html += renderField('创意名称', '<input id="creativeName" class="form-input" value="' + escapeHtml(item ? item.creativeName : '') + '" placeholder="例如 首页横幅-主推红包会场"/>');
    html += renderField('所属单元', '<select id="creativeUnitCode" class="form-input">' + renderCodeOptions(state.consoleData.units || [], 'unitCode', 'unitName', item ? item.unitCode : '') + '</select>');
    html += renderField('绑定素材', '<select id="creativeMaterialCode" class="form-input">' + renderCodeOptions(state.consoleData.materials || [], 'materialCode', 'materialName', item ? item.materialCode : '') + '</select>');
    html += renderField('优先级', '<input id="creativePriority" class="form-input" value="' + escapeHtml(item && item.priority != null ? item.priority : '1') + '" placeholder="越小越优先"/>');
    html += renderField('权重', '<input id="creativeWeight" class="form-input" value="' + escapeHtml(item && item.weight != null ? item.weight : '100') + '" placeholder="例如 100"/>');
    html += renderField('兜底创意', '<select id="creativeFallback" class="form-input">' + renderBooleanOptions(item ? item.fallback : false) + '</select>');
    html += renderField('状态', '<select id="creativeStatus" class="form-input">' + renderOptions(['EDITING', 'PUBLISHED', 'OFFLINE'], item ? item.status : 'PUBLISHED') + '</select>');
    html += renderField('落地页', '<input id="creativeLandingUrl" class="form-input" value="' + escapeHtml(item ? item.landingUrl : '') + '" placeholder="例如 aipay://home?feature=deliver-home-banner"/>');
    html += renderField('预览图', '<input id="creativePreviewImage" class="form-input" value="' + escapeHtml(item ? item.previewImage : '') + '" placeholder="可空"/>');
    html += renderField('生效开始', '<input id="creativeActiveFrom" type="datetime-local" class="form-input" value="' + escapeHtml(activeFrom) + '"/>');
    html += renderField('生效结束', '<input id="creativeActiveTo" type="datetime-local" class="form-input" value="' + escapeHtml(activeTo) + '"/>');
    html += '</div>';
    html += renderField('Schema JSON', '<textarea id="creativeSchemaJson" class="deliver-textarea" placeholder="例如 {\"action\":\"stay-on-home\",\"campaign\":\"weekend-redpacket\"}">' + escapeHtml(item ? item.schemaJson : '') + '</textarea>');
    html += renderFormActions('save-creative', 'reset-creative', !!item, '保存创意', '新建创意');
    return html;
  }

  function renderRelationForm(item) {
    var html = '';
    html += renderFormTitle('relation');
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('展位', '<select id="relationPositionId" class="form-input">' + renderIdOptions(state.consoleData.positions || [], 'id', 'positionCode', 'positionName', item ? item.positionId : '') + '</select>');
    html += renderField('投放单元', '<select id="relationUnitId" class="form-input">' + renderIdOptions(state.consoleData.units || [], 'id', 'unitCode', 'unitName', item ? item.unitId : '') + '</select>');
    html += renderField('创意', '<select id="relationCreativeId" class="form-input">' + renderIdOptions(state.consoleData.creatives || [], 'id', 'creativeCode', 'creativeName', item ? item.creativeId : '') + '</select>');
    html += renderField('展示顺序', '<input id="relationDisplayOrder" class="form-input" value="' + escapeHtml(item && item.displayOrder != null ? item.displayOrder : '1') + '" placeholder="例如 1"/>');
    html += renderField('兜底关系', '<select id="relationFallback" class="form-input">' + renderBooleanOptions(item ? item.fallback : false) + '</select>');
    html += renderField('启用状态', '<select id="relationEnabled" class="form-input">' + renderBooleanOptions(item ? item.enabled : true) + '</select>');
    html += '</div>';
    html += renderFormActions('save-relation', 'reset-relation', !!item, '保存关系', '新建关系');
    return html;
  }

  function renderFatigueForm(item) {
    var presetType = state.scopedRuleDraft && state.scopedRuleDraft.entityType ? state.scopedRuleDraft.entityType : 'UNIT';
    var presetCode = state.scopedRuleDraft && state.scopedRuleDraft.entityCode ? state.scopedRuleDraft.entityCode : '';
    var entityType = item ? item.entityType : presetType;
    var html = '';
    html += renderFormTitle('fatigue');
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('规则编码', '<input id="fatigueCode" class="form-input" value="' + escapeHtml(item ? item.fatigueCode : '') + '" placeholder="例如 FAT_HOME_BANNER_POSITION_DAILY"/>');
    html += renderField('规则名称', '<input id="fatigueRuleName" class="form-input" value="' + escapeHtml(item ? item.ruleName : '') + '" placeholder="例如 单用户日曝光上限"/>');
    html += renderField('实体类型', '<select id="fatigueEntityType" class="form-input">' + renderOptions(['POSITION', 'UNIT', 'CREATIVE'], entityType) + '</select>');
    html += renderField('实体编码', '<select id="fatigueEntityCode" class="form-input">' + renderEntityCodeOptions(entityType, item ? item.entityCode : presetCode) + '</select>');
    html += renderField('事件类型', '<select id="fatigueEventType" class="form-input">' + renderOptions(['DISPLAY', 'CLICK', 'CONVERSION'], item ? item.eventType : 'DISPLAY') + '</select>');
    html += renderField('时间窗口(分钟)', '<input id="fatigueTimeWindowMinutes" class="form-input" value="' + escapeHtml(item && item.timeWindowMinutes != null ? item.timeWindowMinutes : '1440') + '" placeholder="例如 1440"/>');
    html += renderField('次数上限', '<input id="fatigueMaxCount" class="form-input" value="' + escapeHtml(item && item.maxCount != null ? item.maxCount : '20') + '" placeholder="例如 20"/>');
    html += renderField('启用状态', '<select id="fatigueEnabled" class="form-input">' + renderBooleanOptions(item ? item.enabled : true) + '</select>');
    html += '</div>';
    html += renderFormActions('save-fatigue', 'reset-fatigue', !!item, '保存疲劳规则', '新建疲劳规则');
    return html;
  }

  function renderTargetingForm(item) {
    var presetType = state.scopedRuleDraft && state.scopedRuleDraft.entityType ? state.scopedRuleDraft.entityType : 'UNIT';
    var presetCode = state.scopedRuleDraft && state.scopedRuleDraft.entityCode ? state.scopedRuleDraft.entityCode : '';
    var entityType = item ? item.entityType : presetType;
    var html = '';
    html += renderFormTitle('targeting');
    html += '<div class="manager-coupon-form-grid compact">';
    html += renderField('规则编码', '<input id="targetingRuleCode" class="form-input" value="' + escapeHtml(item ? item.ruleCode : '') + '" placeholder="例如 TRG_HOME_BANNER_CLIENT_IOS"/>');
    html += renderField('实体类型', '<select id="targetingEntityType" class="form-input">' + renderOptions(['POSITION', 'UNIT', 'CREATIVE'], entityType) + '</select>');
    html += renderField('实体编码', '<select id="targetingEntityCode" class="form-input">' + renderEntityCodeOptions(entityType, item ? item.entityCode : presetCode) + '</select>');
    html += renderField('定向类型', '<select id="targetingType" class="form-input">' + renderOptions(['CLIENT', 'CHANNEL', 'SCENE', 'USER_TAG', 'TIME_RANGE'], item ? item.targetingType : 'CLIENT') + '</select>');
    html += renderField('操作符', '<select id="targetingOperator" class="form-input">' + renderOptions(['EQUALS', 'IN', 'NOT_EQUALS', 'NOT_IN'], item ? item.operator : 'EQUALS') + '</select>');
    html += renderField('定向值', '<input id="targetingValue" class="form-input" value="' + escapeHtml(item ? item.targetingValue : '') + '" placeholder="例如 IOS_APP 或 IOS,ANDROID"/>');
    html += renderField('启用状态', '<select id="targetingEnabled" class="form-input">' + renderBooleanOptions(item ? item.enabled : true) + '</select>');
    html += '</div>';
    html += renderFormActions('save-targeting', 'reset-targeting', !!item, '保存定向规则', '新建定向规则');
    return html;
  }

  function renderFormTitle(kind) {
    var idMap = {
      position: state.editing.positionId,
      unit: state.editing.unitId,
      material: state.editing.materialId,
      creative: state.editing.creativeId,
      relation: state.editing.relationId,
      fatigue: state.editing.fatigueRuleId,
      targeting: state.editing.targetingRuleId,
    };
    var labelMap = {
      position: '展位',
      unit: '投放单元',
      material: '素材',
      creative: '创意',
      relation: '投放关系',
      fatigue: '疲劳规则',
      targeting: '定向规则',
    };
    var currentId = idMap[kind];
    var entityLabel = labelMap[kind] || '记录';
    var title = currentId ? '编辑配置' : '新建配置';
    var caption = currentId ? entityLabel + ' #' + currentId : entityLabel;
    var html = '';
    html += '<div class="deliver-form-title-row">';
    html += '<h4 class="manager-panel-title">' + escapeHtml(title) + '</h4>';
    html += '<span class="deliver-form-caption">' + escapeHtml(caption) + '</span>';
    html += '</div>';
    return html;
  }

  function renderFormActions(saveAction, resetAction, editing, saveText, createText) {
    var html = '';
    html += '<div class="deliver-form-actions">';
    html += '<button class="manager-btn primary" type="button" data-action="' + escapeHtml(saveAction) + '">' + escapeHtml(editing ? saveText : createText) + '</button>';
    html += '<button class="manager-btn" type="button" data-action="' + escapeHtml(resetAction) + '">' + escapeHtml(editing ? '返回详情' : '取消新建') + '</button>';
    html += '</div>';
    return html;
  }

  function renderPositionTable(items) {
    var html = '';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>ID</th><th>展位</th><th>投放配置</th><th>状态</th><th>操作</th></tr></thead><tbody>';
    if (!items.length) {
      html += '<tr><td colspan="5" class="manager-coupon-empty-row">暂无展位配置</td></tr>';
    } else {
      html += items.map(function (item) {
        var positionCell = renderTableStack(
          '<span class="deliver-inline-code">' + escapeHtml(item.positionCode) + '</span>',
          escapeHtml(item.positionName || '-'),
          escapeHtml(formatDeliverEnumText(item.positionType))
        );
        var configCell = renderTableStack(
          escapeHtml(renderTableMeta([
            '轮播 ' + (item.slideInterval == null ? '-' : item.slideInterval + 's'),
            '返回 ' + (item.maxDisplayCount == null ? '-' : item.maxDisplayCount)
          ])),
          escapeHtml(renderTableMeta([
            '排序 ' + formatDeliverEnumText(item.sortType),
            item.sortRule || ''
          ])),
          escapeHtml('兜底 ' + formatBooleanText(item.needFallback))
        );
        var statusCell = renderTableStack(
          renderStatusBadge(item.status),
          item.previewImage ? renderPreviewLink(item.previewImage) : '<span class="deliver-muted">无预览图</span>',
          item.memo ? escapeHtml(item.memo) : ''
        );
        return '<tr>' +
          '<td>' + escapeHtml(item.id) + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + positionCell + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + configCell + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + statusCell + '</td>' +
          '<td class="deliver-table-actions">' +
            '<button class="manager-btn" type="button" data-action="view-position" data-id="' + escapeHtml(item.id) + '">详情</button>' +
            '<button class="manager-btn" type="button" data-action="edit-position" data-id="' + escapeHtml(item.id) + '">修改</button>' +
          '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function renderUnitTable(items) {
    var html = '';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>ID</th><th>投放单元</th><th>执行配置</th><th>状态</th><th>操作</th></tr></thead><tbody>';
    if (!items.length) {
      html += '<tr><td colspan="5" class="manager-coupon-empty-row">暂无投放单元配置</td></tr>';
    } else {
      html += items.map(function (item) {
        var unitCell = renderTableStack(
          '<span class="deliver-inline-code">' + escapeHtml(item.unitCode) + '</span>',
          escapeHtml(item.unitName || '-'),
          item.memo ? escapeHtml(item.memo) : ''
        );
        var configCell = renderTableStack(
          escapeHtml('优先级 ' + (item.priority == null ? '-' : item.priority)),
          escapeHtml(formatWindow(item.activeFrom, item.activeTo)),
          ''
        );
        var statusCell = renderTableStack(renderStatusBadge(item.status), '', '');
        return '<tr>' +
          '<td>' + escapeHtml(item.id) + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + unitCell + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + configCell + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + statusCell + '</td>' +
          '<td class="deliver-table-actions">' +
            '<button class="manager-btn" type="button" data-action="view-unit" data-id="' + escapeHtml(item.id) + '">详情</button>' +
            '<button class="manager-btn" type="button" data-action="edit-unit" data-id="' + escapeHtml(item.id) + '">修改</button>' +
          '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function renderMaterialTable(items) {
    var html = '';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>ID</th><th>素材</th><th>内容配置</th><th>状态</th><th>操作</th></tr></thead><tbody>';
    if (!items.length) {
      html += '<tr><td colspan="5" class="manager-coupon-empty-row">暂无素材配置</td></tr>';
    } else {
      html += items.map(function (item) {
        var materialCell = renderTableStack(
          '<span class="deliver-inline-code">' + escapeHtml(item.materialCode) + '</span>',
          escapeHtml(item.materialName || '-'),
          escapeHtml(renderTableMeta([formatDeliverEnumText(item.materialType), item.title || '未填写标题']))
        );
        var materialPreviewUrl = item.previewImage || item.imageUrl || '';
        var contentCell = renderTableStack(
          materialPreviewUrl ? renderPreviewThumbnail(materialPreviewUrl, item.materialName || item.materialCode || '素材预览') : '<span class="deliver-muted">无图片</span>',
          item.landingUrl ? '<span class="deliver-inline-code">' + escapeHtml(item.landingUrl) + '</span>' : '<span class="deliver-muted">无落地页</span>',
          item.schemaJson ? '<span class="deliver-inline-code">' + escapeHtml(item.schemaJson) + '</span>' : '<span class="deliver-muted">无 Schema</span>'
        );
        var statusCell = renderTableStack(
          renderStatusBadge(item.status),
          escapeHtml(formatWindow(item.activeFrom, item.activeTo)),
          ''
        );
        return '<tr>' +
          '<td>' + escapeHtml(item.id) + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + materialCell + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + contentCell + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + statusCell + '</td>' +
          '<td class="deliver-table-actions">' +
            '<button class="manager-btn" type="button" data-action="view-material" data-id="' + escapeHtml(item.id) + '">详情</button>' +
            '<button class="manager-btn" type="button" data-action="edit-material" data-id="' + escapeHtml(item.id) + '">修改</button>' +
          '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function renderCreativeTable(items) {
    var html = '';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>ID</th><th>创意</th><th>绑定关系</th><th>投放配置</th><th>操作</th></tr></thead><tbody>';
    if (!items.length) {
      html += '<tr><td colspan="5" class="manager-coupon-empty-row">暂无创意配置</td></tr>';
    } else {
      html += items.map(function (item) {
        var creativeCell = renderTableStack(
          '<span class="deliver-inline-code">' + escapeHtml(item.creativeCode) + '</span>',
          escapeHtml(item.creativeName || '-'),
          renderStatusBadge(item.status)
        );
        var bindingCell = renderTableStack(
          '<span class="deliver-inline-code">' + escapeHtml(item.unitCode || '-') + '</span>',
          '<span class="deliver-inline-code">' + escapeHtml(item.materialCode || '-') + '</span>',
          ''
        );
        var configCell = renderTableStack(
          escapeHtml(renderTableMeta([
            '优先级 ' + (item.priority == null ? '-' : item.priority),
            '权重 ' + (item.weight == null ? '-' : item.weight)
          ])),
          escapeHtml('兜底 ' + formatBooleanText(item.fallback)),
          item.landingUrl ? '<span class="deliver-inline-code">' + escapeHtml(item.landingUrl) + '</span>' : '<span class="deliver-muted">无落地页</span>'
        );
        return '<tr>' +
          '<td>' + escapeHtml(item.id) + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + creativeCell + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + bindingCell + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + configCell + '</td>' +
          '<td class="deliver-table-actions">' +
            '<button class="manager-btn" type="button" data-action="view-creative" data-id="' + escapeHtml(item.id) + '">详情</button>' +
            '<button class="manager-btn" type="button" data-action="edit-creative" data-id="' + escapeHtml(item.id) + '">修改</button>' +
          '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function renderRelationTable(items) {
    var positionMap = createIdMap(state.consoleData.positions || [], 'id');
    var unitMap = createIdMap(state.consoleData.units || [], 'id');
    var creativeMap = createIdMap(state.consoleData.creatives || [], 'id');
    var html = '';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>ID</th><th>投放链路</th><th>投放配置</th><th>操作</th></tr></thead><tbody>';
    if (!items.length) {
      html += '<tr><td colspan="4" class="manager-coupon-empty-row">暂无关系配置</td></tr>';
    } else {
      html += items.map(function (item) {
        var position = positionMap[item.positionId];
        var unit = unitMap[item.unitId];
        var creative = creativeMap[item.creativeId];
        var chainCell = renderTableStack(
          position ? '<span class="deliver-inline-code">' + escapeHtml(position.positionCode) + '</span>' : '<span class="deliver-inline-code">' + escapeHtml(item.positionId) + '</span>',
          unit ? '<span class="deliver-inline-code">' + escapeHtml(unit.unitCode) + '</span>' : '<span class="deliver-inline-code">' + escapeHtml(item.unitId) + '</span>',
          creative ? '<span class="deliver-inline-code">' + escapeHtml(creative.creativeCode) + '</span>' : '<span class="deliver-inline-code">' + escapeHtml(item.creativeId) + '</span>'
        );
        var configCell = renderTableStack(
          escapeHtml('顺序 ' + (item.displayOrder == null ? '-' : item.displayOrder)),
          escapeHtml(renderTableMeta([
            '兜底 ' + formatBooleanText(item.fallback),
            '启用 ' + formatBooleanText(item.enabled)
          ])),
          ''
        );
        return '<tr>' +
          '<td>' + escapeHtml(item.id) + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + chainCell + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + configCell + '</td>' +
          '<td class="deliver-table-actions">' +
            '<button class="manager-btn" type="button" data-action="view-relation" data-id="' + escapeHtml(item.id) + '">详情</button>' +
            '<button class="manager-btn" type="button" data-action="edit-relation" data-id="' + escapeHtml(item.id) + '">修改</button>' +
          '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function renderFatigueTable(items) {
    var html = '';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>ID</th><th>规则</th><th>作用对象</th><th>频控条件</th><th>操作</th></tr></thead><tbody>';
    if (!items.length) {
      html += '<tr><td colspan="5" class="manager-coupon-empty-row">暂无疲劳规则，可在弹窗中新建</td></tr>';
    } else {
      html += items.map(function (item) {
        var ruleCell = renderTableStack(
          '<span class="deliver-inline-code">' + escapeHtml(item.fatigueCode) + '</span>',
          escapeHtml(item.ruleName || '-'),
          ''
        );
        var entityCell = renderTableStack(
          escapeHtml(formatDeliverEnumText(item.entityType)),
          renderResolvedEntityValue(item.entityType, item.entityCode),
          ''
        );
        var configCell = renderTableStack(
          escapeHtml(renderTableMeta([
            '事件 ' + formatDeliverEnumText(item.eventType),
            '窗口 ' + (item.timeWindowMinutes == null ? '-' : item.timeWindowMinutes + ' min')
          ])),
          escapeHtml('上限 ' + (item.maxCount == null ? '-' : item.maxCount)),
          escapeHtml('启用 ' + formatBooleanText(item.enabled))
        );
        return '<tr>' +
          '<td>' + escapeHtml(item.id) + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + ruleCell + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + entityCell + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + configCell + '</td>' +
          '<td class="deliver-table-actions">' +
            '<button class="manager-btn" type="button" data-action="view-fatigue" data-id="' + escapeHtml(item.id) + '">详情</button>' +
            '<button class="manager-btn" type="button" data-action="edit-fatigue" data-id="' + escapeHtml(item.id) + '">修改</button>' +
          '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  function renderTargetingTable(items) {
    var html = '';
    html += '<div class="manager-coupon-table-wrap"><table class="manager-coupon-table">';
    html += '<thead><tr><th>ID</th><th>规则</th><th>作用对象</th><th>匹配条件</th><th>操作</th></tr></thead><tbody>';
    if (!items.length) {
      html += '<tr><td colspan="5" class="manager-coupon-empty-row">暂无定向规则，可在弹窗中新建</td></tr>';
    } else {
      html += items.map(function (item) {
        var ruleCell = renderTableStack(
          '<span class="deliver-inline-code">' + escapeHtml(item.ruleCode) + '</span>',
          escapeHtml(formatDeliverEnumText(item.targetingType)),
          ''
        );
        var entityCell = renderTableStack(
          escapeHtml(formatDeliverEnumText(item.entityType)),
          renderResolvedEntityValue(item.entityType, item.entityCode),
          ''
        );
        var matchCell = renderTableStack(
          escapeHtml(renderTableMeta([
            '操作符 ' + formatDeliverEnumText(item.operator),
            '启用 ' + formatBooleanText(item.enabled)
          ])),
          '<span class="deliver-inline-code">' + escapeHtml(item.targetingValue || '-') + '</span>',
          ''
        );
        return '<tr>' +
          '<td>' + escapeHtml(item.id) + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + ruleCell + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + entityCell + '</td>' +
          '<td class="deliver-rich-cell-wrap">' + matchCell + '</td>' +
          '<td class="deliver-table-actions">' +
            '<button class="manager-btn" type="button" data-action="view-targeting" data-id="' + escapeHtml(item.id) + '">详情</button>' +
            '<button class="manager-btn" type="button" data-action="edit-targeting" data-id="' + escapeHtml(item.id) + '">修改</button>' +
          '</td>' +
          '</tr>';
      }).join('');
    }
    html += '</tbody></table></div>';
    return html;
  }

  async function onClick(event) {
    var actionElement = event.target.closest('[data-action]');
    if (!actionElement) {
      return;
    }
    var action = actionElement.getAttribute('data-action');
    if (!action) {
      return;
    }

    if (action === 'reload-deliver') {
      await loadConsole(true);
      return;
    }

    if (action === 'switch-deliver-tab') {
      var nextTab = String(actionElement.getAttribute('data-tab') || '').trim();
      if (nextTab === 'position' || nextTab === 'unit' || nextTab === 'material') {
        state.activeTab = nextTab;
      } else {
        state.activeTab = 'position';
      }
      render();
      return;
    }

    if (action === 'attach-position-unit') {
      if (state.positionPanel.mode !== 'edit') {
        state.notice = '请在展位修改页操作投放单元';
        render();
        return;
      }
      await attachPositionUnit(readActionId(actionElement));
      return;
    }
    if (action === 'detach-position-unit') {
      if (state.positionPanel.mode !== 'edit') {
        state.notice = '请在展位修改页操作投放单元';
        render();
        return;
      }
      await detachPositionUnit(readActionIdFromAttr(actionElement, 'data-position-id'), readActionIdFromAttr(actionElement, 'data-unit-id'));
      return;
    }

    if (action === 'bind-unit-material') {
      if (state.unitPanel.mode !== 'edit') {
        state.notice = '请在投放单元修改页操作创意关联';
        render();
        return;
      }
      await bindUnitMaterial(readActionId(actionElement));
      return;
    }
    if (action === 'offline-unit-creative') {
      if (state.unitPanel.mode !== 'edit') {
        state.notice = '请在投放单元修改页操作创意关联';
        render();
        return;
      }
      await offlineUnitCreative(readActionIdFromAttr(actionElement, 'data-unit-id'), readActionIdFromAttr(actionElement, 'data-creative-id'));
      return;
    }
    if (action === 'online-unit-creative') {
      if (state.unitPanel.mode !== 'edit') {
        state.notice = '请在投放单元修改页操作创意关联';
        render();
        return;
      }
      await onlineUnitCreative(readActionIdFromAttr(actionElement, 'data-unit-id'), readActionIdFromAttr(actionElement, 'data-creative-id'));
      return;
    }
    if (action === 'delete-unit-creative') {
      if (state.unitPanel.mode !== 'edit') {
        state.notice = '请在投放单元修改页操作创意关联';
        render();
        return;
      }
      await deleteUnitCreative(readActionIdFromAttr(actionElement, 'data-unit-id'), readActionIdFromAttr(actionElement, 'data-creative-id'));
      return;
    }

    if (action === 'create-unit-fatigue') {
      if (state.unitPanel.mode !== 'edit') {
        state.notice = '请在投放单元修改页操作规则配置';
        render();
        return;
      }
      openScopedFatiguePanel('UNIT', readActionText(actionElement, 'data-entity-code'));
      render();
      focusSection('deliverFatigueDetailSection');
      return;
    }
    if (action === 'create-unit-targeting') {
      if (state.unitPanel.mode !== 'edit') {
        state.notice = '请在投放单元修改页操作规则配置';
        render();
        return;
      }
      openScopedTargetingPanel('UNIT', readActionText(actionElement, 'data-entity-code'));
      render();
      focusSection('deliverTargetingDetailSection');
      return;
    }
    if (action === 'create-creative-fatigue') {
      openScopedFatiguePanel('CREATIVE', readActionText(actionElement, 'data-entity-code'));
      render();
      focusSection('deliverFatigueDetailSection');
      return;
    }
    if (action === 'create-creative-targeting') {
      openScopedTargetingPanel('CREATIVE', readActionText(actionElement, 'data-entity-code'));
      render();
      focusSection('deliverTargetingDetailSection');
      return;
    }

    if (action === 'create-position') {
      state.editing.positionId = null;
      openPositionPanel('create', null);
      render();
      focusSection('deliverPositionDetailSection');
      return;
    }
    if (action === 'view-position') {
      state.editing.positionId = null;
      openPositionPanel('view', readActionId(actionElement));
      render();
      focusSection('deliverPositionDetailSection');
      return;
    }
    if (action === 'edit-position') {
      state.editing.positionId = readActionId(actionElement);
      openPositionPanel('edit', state.editing.positionId);
      render();
      focusSection('deliverPositionDetailSection');
      return;
    }
    if (action === 'close-position-panel') {
      closePositionPanel();
      render();
      return;
    }

    if (action === 'create-unit') {
      state.editing.unitId = null;
      openUnitPanel('create', null);
      render();
      focusSection('deliverUnitDetailSection');
      return;
    }
    if (action === 'view-unit') {
      state.editing.unitId = null;
      openUnitPanel('view', readActionId(actionElement));
      render();
      focusSection('deliverUnitDetailSection');
      return;
    }
    if (action === 'edit-unit') {
      state.editing.unitId = readActionId(actionElement);
      openUnitPanel('edit', state.editing.unitId);
      render();
      focusSection('deliverUnitDetailSection');
      return;
    }
    if (action === 'close-unit-panel') {
      closeUnitPanel();
      render();
      return;
    }

    if (action === 'create-material') {
      state.editing.materialId = null;
      openMaterialPanel('create', null);
      render();
      focusSection('deliverMaterialDetailSection');
      return;
    }
    if (action === 'view-material') {
      state.editing.materialId = null;
      openMaterialPanel('view', readActionId(actionElement));
      render();
      focusSection('deliverMaterialDetailSection');
      return;
    }
    if (action === 'edit-material') {
      state.editing.materialId = readActionId(actionElement);
      openMaterialPanel('edit', state.editing.materialId);
      render();
      focusSection('deliverMaterialDetailSection');
      return;
    }
    if (action === 'close-material-panel') {
      closeMaterialPanel();
      render();
      return;
    }

    if (action === 'create-creative') {
      state.editing.creativeId = null;
      openCreativePanel('create', null);
      render();
      focusSection('deliverCreativeDetailSection');
      return;
    }
    if (action === 'view-creative') {
      state.editing.creativeId = null;
      openCreativePanel('view', readActionId(actionElement));
      render();
      focusSection('deliverCreativeDetailSection');
      return;
    }
    if (action === 'edit-creative') {
      state.editing.creativeId = readActionId(actionElement);
      openCreativePanel('edit', state.editing.creativeId);
      render();
      focusSection('deliverCreativeDetailSection');
      return;
    }
    if (action === 'close-creative-panel') {
      closeCreativePanel();
      render();
      return;
    }

    if (action === 'create-relation') {
      state.editing.relationId = null;
      openRelationPanel('create', null);
      render();
      focusSection('deliverRelationDetailSection');
      return;
    }
    if (action === 'view-relation') {
      state.editing.relationId = null;
      openRelationPanel('view', readActionId(actionElement));
      render();
      focusSection('deliverRelationDetailSection');
      return;
    }
    if (action === 'edit-relation') {
      state.editing.relationId = readActionId(actionElement);
      openRelationPanel('edit', state.editing.relationId);
      render();
      focusSection('deliverRelationDetailSection');
      return;
    }
    if (action === 'close-relation-panel') {
      closeRelationPanel();
      render();
      return;
    }

    if (action === 'create-fatigue') {
      state.editing.fatigueRuleId = null;
      openFatiguePanel('create', null);
      render();
      focusSection('deliverFatigueDetailSection');
      return;
    }
    if (action === 'view-fatigue') {
      state.editing.fatigueRuleId = null;
      openFatiguePanel('view', readActionId(actionElement));
      render();
      focusSection('deliverFatigueDetailSection');
      return;
    }
    if (action === 'edit-fatigue') {
      state.editing.fatigueRuleId = readActionId(actionElement);
      openFatiguePanel('edit', state.editing.fatigueRuleId);
      render();
      focusSection('deliverFatigueDetailSection');
      return;
    }
    if (action === 'close-fatigue-panel') {
      closeFatiguePanel();
      render();
      return;
    }

    if (action === 'create-targeting') {
      state.editing.targetingRuleId = null;
      openTargetingPanel('create', null);
      render();
      focusSection('deliverTargetingDetailSection');
      return;
    }
    if (action === 'view-targeting') {
      state.editing.targetingRuleId = null;
      openTargetingPanel('view', readActionId(actionElement));
      render();
      focusSection('deliverTargetingDetailSection');
      return;
    }
    if (action === 'edit-targeting') {
      state.editing.targetingRuleId = readActionId(actionElement);
      openTargetingPanel('edit', state.editing.targetingRuleId);
      render();
      focusSection('deliverTargetingDetailSection');
      return;
    }
    if (action === 'close-targeting-panel') {
      closeTargetingPanel();
      render();
      return;
    }

    if (action === 'reset-position') {
      if (state.positionPanel.mode === 'edit' && state.positionPanel.positionId) {
        state.editing.positionId = null;
        openPositionPanel('view', state.positionPanel.positionId);
      } else {
        closePositionPanel();
      }
      render();
      return;
    }
    if (action === 'reset-unit') {
      if (state.unitPanel.mode === 'edit' && state.unitPanel.unitId) {
        state.editing.unitId = null;
        openUnitPanel('view', state.unitPanel.unitId);
      } else {
        closeUnitPanel();
      }
      render();
      return;
    }
    if (action === 'reset-material') {
      if (state.materialPanel.mode === 'edit' && state.materialPanel.materialId) {
        state.editing.materialId = null;
        openMaterialPanel('view', state.materialPanel.materialId);
      } else {
        closeMaterialPanel();
      }
      render();
      return;
    }
    if (action === 'reset-creative') {
      if (state.creativePanel.mode === 'edit' && state.creativePanel.creativeId) {
        state.editing.creativeId = null;
        openCreativePanel('view', state.creativePanel.creativeId);
      } else {
        closeCreativePanel();
      }
      render();
      return;
    }
    if (action === 'reset-relation') {
      if (state.relationPanel.mode === 'edit' && state.relationPanel.relationId) {
        state.editing.relationId = null;
        openRelationPanel('view', state.relationPanel.relationId);
      } else {
        closeRelationPanel();
      }
      render();
      return;
    }
    if (action === 'reset-fatigue') {
      if (state.fatiguePanel.mode === 'edit' && state.fatiguePanel.fatigueRuleId) {
        state.editing.fatigueRuleId = null;
        openFatiguePanel('view', state.fatiguePanel.fatigueRuleId);
      } else {
        closeFatiguePanel();
      }
      render();
      return;
    }
    if (action === 'reset-targeting') {
      if (state.targetingPanel.mode === 'edit' && state.targetingPanel.targetingRuleId) {
        state.editing.targetingRuleId = null;
        openTargetingPanel('view', state.targetingPanel.targetingRuleId);
      } else {
        closeTargetingPanel();
      }
      render();
      return;
    }

    if (action === 'save-position') {
      await savePosition();
      return;
    }
    if (action === 'save-unit') {
      await saveUnit();
      return;
    }
    if (action === 'save-material') {
      await saveMaterial();
      return;
    }
    if (action === 'save-creative') {
      await saveCreative();
      return;
    }
    if (action === 'save-relation') {
      await saveRelation();
      return;
    }
    if (action === 'save-fatigue') {
      await saveFatigueRule();
      return;
    }
    if (action === 'save-targeting') {
      await saveTargetingRule();
      return;
    }
  }

  function onChange(event) {

    var target = event.target;
    if (!target) {
      return;
    }
    if (target.id === 'fatigueEntityType') {
      syncEntityCodeSelect('fatigueEntityType', 'fatigueEntityCode');
      return;
    }
    if (target.id === 'targetingEntityType') {
      syncEntityCodeSelect('targetingEntityType', 'targetingEntityCode');
    }
  }

  function openScopedFatiguePanel(entityType, entityCode) {
    state.editing.fatigueRuleId = null;
    openFatiguePanel('create', null);
    state.scopedRuleDraft.entityType = String(entityType || '').toUpperCase();
    state.scopedRuleDraft.entityCode = String(entityCode || '').trim();
  }

  function openScopedTargetingPanel(entityType, entityCode) {
    state.editing.targetingRuleId = null;
    openTargetingPanel('create', null);
    state.scopedRuleDraft.entityType = String(entityType || '').toUpperCase();
    state.scopedRuleDraft.entityCode = String(entityCode || '').trim();
  }

  async function savePosition() {
    try {
      var publishedAtValue = readInputValue('positionPublishedAt');
      var payload = {
        positionCode: requireText(readInputValue('positionCode'), '展位编码'),
        positionName: requireText(readInputValue('positionName'), '展位名称'),
        positionType: requireText(readInputValue('positionType'), '展位类型').toUpperCase(),
        previewImage: readOptionalValue('positionPreviewImage'),
        slideInterval: parsePositiveInteger(readInputValue('positionSlideInterval'), '轮播秒数'),
        maxDisplayCount: parsePositiveInteger(readInputValue('positionMaxDisplayCount'), '最大返回数'),
        sortType: requireText(readInputValue('positionSortType'), '排序方式').toUpperCase(),
        sortRule: readOptionalValue('positionSortRule'),
        needFallback: parseBooleanValue(readInputValue('positionNeedFallback'), true),
        status: requireText(readInputValue('positionStatus'), '状态').toUpperCase(),
        memo: readOptionalValue('positionMemo'),
        publishedAt: publishedAtValue ? normalizeDateTimeInput(publishedAtValue, '发布时间') : null,
        activeFrom: null,
        activeTo: null,
      };
      await upsertResource('/api/admin/deliver/positions', state.editing.positionId, payload, '展位保存成功');
      closePositionPanel();
      render();
    } catch (error) {
      state.notice = error && error.message ? error.message : '展位保存失败';
      render();
    }
  }

  async function saveUnit() {
    try {
      var payload = {
        unitCode: requireText(readInputValue('unitCode'), '单元编码'),
        unitName: requireText(readInputValue('unitName'), '单元名称'),
        priority: parsePositiveInteger(readInputValue('unitPriority'), '优先级'),
        status: requireText(readInputValue('unitStatus'), '状态').toUpperCase(),
        memo: readOptionalValue('unitMemo'),
        activeFrom: normalizeDateTimeInput(readInputValue('unitActiveFrom'), '生效开始'),
        activeTo: normalizeDateTimeInput(readInputValue('unitActiveTo'), '生效结束'),
      };
      await upsertResource('/api/admin/deliver/units', state.editing.unitId, payload, '投放单元保存成功');
      closeUnitPanel();
      render();
    } catch (error) {
      state.notice = error && error.message ? error.message : '投放单元保存失败';
      render();
    }
  }

  async function saveMaterial() {
    try {
      var payload = {
        materialCode: requireText(readInputValue('materialCode'), '素材编码'),
        materialName: requireText(readInputValue('materialName'), '素材名称'),
        materialType: requireText(readInputValue('materialType'), '素材类型').toUpperCase(),
        title: readOptionalValue('materialTitle'),
        imageUrl: requireText(readInputValue('materialImageUrl'), '图片地址'),
        landingUrl: readOptionalValue('materialLandingUrl'),
        schemaJson: readOptionalValue('materialSchemaJson'),
        previewImage: readOptionalValue('materialPreviewImage'),
        status: requireText(readInputValue('materialStatus'), '状态').toUpperCase(),
        activeFrom: normalizeDateTimeInput(readInputValue('materialActiveFrom'), '生效开始'),
        activeTo: normalizeDateTimeInput(readInputValue('materialActiveTo'), '生效结束'),
      };
      await upsertResource('/api/admin/deliver/materials', state.editing.materialId, payload, '素材保存成功');
      closeMaterialPanel();
      render();
    } catch (error) {
      state.notice = error && error.message ? error.message : '素材保存失败';
      render();
    }
  }

  async function saveCreative() {
    try {
      var payload = {
        creativeCode: requireText(readInputValue('creativeCode'), '创意编码'),
        creativeName: requireText(readInputValue('creativeName'), '创意名称'),
        unitCode: requireText(readInputValue('creativeUnitCode'), '所属单元'),
        materialCode: requireText(readInputValue('creativeMaterialCode'), '绑定素材'),
        landingUrl: readOptionalValue('creativeLandingUrl'),
        schemaJson: readOptionalValue('creativeSchemaJson'),
        priority: parsePositiveInteger(readInputValue('creativePriority'), '优先级'),
        weight: parsePositiveInteger(readInputValue('creativeWeight'), '权重'),
        fallback: parseBooleanValue(readInputValue('creativeFallback'), false),
        previewImage: readOptionalValue('creativePreviewImage'),
        status: requireText(readInputValue('creativeStatus'), '状态').toUpperCase(),
        activeFrom: normalizeDateTimeInput(readInputValue('creativeActiveFrom'), '生效开始'),
        activeTo: normalizeDateTimeInput(readInputValue('creativeActiveTo'), '生效结束'),
      };
      await upsertResource('/api/admin/deliver/creatives', state.editing.creativeId, payload, '创意保存成功');
      closeCreativePanel();
      render();
    } catch (error) {
      state.notice = error && error.message ? error.message : '创意保存失败';
      render();
    }
  }

  async function saveRelation() {
    try {
      var payload = {
        positionId: parsePositiveInteger(readInputValue('relationPositionId'), '展位'),
        unitId: parsePositiveInteger(readInputValue('relationUnitId'), '投放单元'),
        creativeId: parsePositiveInteger(readInputValue('relationCreativeId'), '创意'),
        displayOrder: parsePositiveInteger(readInputValue('relationDisplayOrder'), '展示顺序'),
        fallback: parseBooleanValue(readInputValue('relationFallback'), false),
        enabled: parseBooleanValue(readInputValue('relationEnabled'), true),
      };
      await upsertResource('/api/admin/deliver/relations', state.editing.relationId, payload, '关系保存成功');
      closeRelationPanel();
      render();
    } catch (error) {
      state.notice = error && error.message ? error.message : '关系保存失败';
      render();
    }
  }

  async function saveFatigueRule() {
    try {
      var payload = {
        fatigueCode: requireText(readInputValue('fatigueCode'), '规则编码'),
        ruleName: requireText(readInputValue('fatigueRuleName'), '规则名称'),
        entityType: requireText(readInputValue('fatigueEntityType'), '实体类型').toUpperCase(),
        entityCode: requireText(readInputValue('fatigueEntityCode'), '实体编码'),
        eventType: requireText(readInputValue('fatigueEventType'), '事件类型').toUpperCase(),
        timeWindowMinutes: parsePositiveInteger(readInputValue('fatigueTimeWindowMinutes'), '时间窗口'),
        maxCount: parsePositiveInteger(readInputValue('fatigueMaxCount'), '次数上限'),
        enabled: parseBooleanValue(readInputValue('fatigueEnabled'), true),
      };
      await upsertResource('/api/admin/deliver/fatigue-rules', state.editing.fatigueRuleId, payload, '疲劳规则保存成功');
      closeFatiguePanel();
      render();
    } catch (error) {
      state.notice = error && error.message ? error.message : '疲劳规则保存失败';
      render();
    }
  }

  async function saveTargetingRule() {
    try {
      var payload = {
        ruleCode: requireText(readInputValue('targetingRuleCode'), '规则编码'),
        entityType: requireText(readInputValue('targetingEntityType'), '实体类型').toUpperCase(),
        entityCode: requireText(readInputValue('targetingEntityCode'), '实体编码'),
        targetingType: requireText(readInputValue('targetingType'), '定向类型').toUpperCase(),
        operator: requireText(readInputValue('targetingOperator'), '操作符').toUpperCase(),
        targetingValue: requireText(readInputValue('targetingValue'), '定向值'),
        enabled: parseBooleanValue(readInputValue('targetingEnabled'), true),
      };
      await upsertResource('/api/admin/deliver/targeting-rules', state.editing.targetingRuleId, payload, '定向规则保存成功');
      closeTargetingPanel();
      render();
    } catch (error) {
      state.notice = error && error.message ? error.message : '定向规则保存失败';
      render();
    }
  }

  async function attachPositionUnit(positionId) {
    try {
      var targetPositionId = Number(positionId);
      if (!Number.isFinite(targetPositionId) || targetPositionId <= 0) {
        throw new Error('展位参数不合法');
      }
      var selectedUnitId = parsePositiveInteger(readInputValue('positionUnitAttachSelect'), '投放单元');
      var position = findItemById(state.consoleData.positions || [], targetPositionId);
      var unit = findItemById(state.consoleData.units || [], selectedUnitId);
      if (!position || !unit) {
        throw new Error('未找到展位或投放单元，请刷新后重试');
      }
      var unitCreatives = (state.consoleData.creatives || []).filter(function (creative) {
        return creative.unitCode === unit.unitCode && String(creative.status || '').toUpperCase() !== 'OFFLINE';
      });
      if (!unitCreatives.length) {
        throw new Error('该投放单元尚未关联素材，请先在投放单元详情中关联素材生成创意');
      }
      state.notice = '正在关联投放单元...';
      render();
      for (var index = 0; index < unitCreatives.length; index += 1) {
        await ensureRelationEnabled(targetPositionId, selectedUnitId, unitCreatives[index].id);
      }
      await loadConsole(false);
      openPositionPanel('view', targetPositionId);
      state.notice = '投放单元关联成功，已同步创意关系';
      render();
    } catch (error) {
      state.notice = error && error.message ? error.message : '关联投放单元失败';
      render();
    }
  }

  async function detachPositionUnit(positionId, unitId) {
    try {
      var targetPositionId = Number(positionId);
      var targetUnitId = Number(unitId);
      if (!Number.isFinite(targetPositionId) || targetPositionId <= 0 || !Number.isFinite(targetUnitId) || targetUnitId <= 0) {
        throw new Error('剔除参数不合法');
      }
      var relations = (state.consoleData.relations || []).filter(function (relation) {
        return Number(relation.positionId) === targetPositionId
          && Number(relation.unitId) === targetUnitId
          && relation.enabled !== false;
      });
      if (!relations.length) {
        state.notice = '该展位下该投放单元已处于剔除状态';
        render();
        return;
      }
      state.notice = '正在剔除投放单元...';
      render();
      for (var index = 0; index < relations.length; index += 1) {
        var relation = relations[index];
        await requestJson('/api/admin/deliver/relations/' + encodeURIComponent(relation.id), {
          method: 'PUT',
          headers: withJson(buildAdminHeaders()),
          body: JSON.stringify(buildRelationPayload(relation, { enabled: false })),
        });
      }
      await loadConsole(false);
      openPositionPanel('view', targetPositionId);
      state.notice = '投放单元已剔除';
      render();
    } catch (error) {
      state.notice = error && error.message ? error.message : '剔除投放单元失败';
      render();
    }
  }

  async function bindUnitMaterial(unitId) {
    try {
      var targetUnitId = Number(unitId);
      if (!Number.isFinite(targetUnitId) || targetUnitId <= 0) {
        throw new Error('投放单元参数不合法');
      }
      var materialCode = requireText(readInputValue('unitMaterialBindSelect'), '素材');
      var unit = findItemById(state.consoleData.units || [], targetUnitId);
      var material = findByCode(state.consoleData.materials || [], 'materialCode', materialCode);
      if (!unit || !material) {
        throw new Error('未找到投放单元或素材，请刷新后重试');
      }
      state.notice = '正在关联素材并生成创意...';
      render();

      var existingCreative = findCreativeByUnitAndMaterial(unit.unitCode, material.materialCode);
      var savedCreative = null;
      if (existingCreative) {
        savedCreative = await requestJson('/api/admin/deliver/creatives/' + encodeURIComponent(existingCreative.id), {
          method: 'PUT',
          headers: withJson(buildAdminHeaders()),
          body: JSON.stringify(buildAutoCreativePayload(unit, material, existingCreative)),
        });
      } else {
        savedCreative = await requestJson('/api/admin/deliver/creatives', {
          method: 'POST',
          headers: withJson(buildAdminHeaders()),
          body: JSON.stringify(buildAutoCreativePayload(unit, material, null)),
        });
      }

      await ensureCreativeRelationsUnderUnit(unit, savedCreative);
      await loadConsole(false);
      state.editing.unitId = targetUnitId;
      openUnitPanel('edit', targetUnitId);
      state.notice = existingCreative ? '素材关联成功，已更新创意并同步投放关系' : '素材关联成功，已生成创意并同步投放关系';
      render();
    } catch (error) {
      state.notice = error && error.message ? error.message : '关联素材失败';
      render();
    }
  }

  async function offlineUnitCreative(unitId, creativeId) {
    try {
      var targetUnitId = Number(unitId);
      var targetCreativeId = Number(creativeId);
      if (!Number.isFinite(targetUnitId) || targetUnitId <= 0 || !Number.isFinite(targetCreativeId) || targetCreativeId <= 0) {
        throw new Error('下线参数不合法');
      }
      var creative = findItemById(state.consoleData.creatives || [], targetCreativeId);
      if (!creative) {
        throw new Error('未找到创意，请刷新后重试');
      }
      state.notice = '正在下线创意...';
      render();
      await requestJson('/api/admin/deliver/creatives/' + encodeURIComponent(targetCreativeId), {
        method: 'PUT',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify(buildCreativeOfflinePayload(creative)),
      });
      var activeRelations = (state.consoleData.relations || []).filter(function (relation) {
        return Number(relation.creativeId) === targetCreativeId && relation.enabled !== false;
      });
      for (var relationIndex = 0; relationIndex < activeRelations.length; relationIndex += 1) {
        var activeRelation = activeRelations[relationIndex];
        await requestJson('/api/admin/deliver/relations/' + encodeURIComponent(activeRelation.id), {
          method: 'PUT',
          headers: withJson(buildAdminHeaders()),
          body: JSON.stringify(buildRelationPayload(activeRelation, { enabled: false })),
        });
      }
      await loadConsole(false);
      state.editing.unitId = targetUnitId;
      openUnitPanel('edit', targetUnitId);
      state.notice = '创意已下线';
      render();
    } catch (error) {
      state.notice = error && error.message ? error.message : '下线创意失败';
      render();
    }
  }

  async function onlineUnitCreative(unitId, creativeId) {
    try {
      var targetUnitId = Number(unitId);
      var targetCreativeId = Number(creativeId);
      if (!Number.isFinite(targetUnitId) || targetUnitId <= 0 || !Number.isFinite(targetCreativeId) || targetCreativeId <= 0) {
        throw new Error('上线参数不合法');
      }
      var creative = findItemById(state.consoleData.creatives || [], targetCreativeId);
      if (!creative) {
        throw new Error('未找到创意，请刷新后重试');
      }
      state.notice = '正在上线创意...';
      render();
      await requestJson('/api/admin/deliver/creatives/' + encodeURIComponent(targetCreativeId), {
        method: 'PUT',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify(buildCreativeOnlinePayload(creative)),
      });
      var unitRelationPositionIds = {};
      (state.consoleData.relations || []).forEach(function (relation) {
        if (Number(relation.unitId) !== targetUnitId) {
          return;
        }
        unitRelationPositionIds[String(relation.positionId)] = true;
      });
      var positionIds = Object.keys(unitRelationPositionIds);
      for (var positionIndex = 0; positionIndex < positionIds.length; positionIndex += 1) {
        var positionId = Number(positionIds[positionIndex]);
        if (!Number.isFinite(positionId) || positionId <= 0) {
          continue;
        }
        await ensureRelationEnabled(positionId, targetUnitId, targetCreativeId);
      }
      await loadConsole(false);
      state.editing.unitId = targetUnitId;
      openUnitPanel('edit', targetUnitId);
      state.notice = '创意已重新上线';
      render();
    } catch (error) {
      state.notice = error && error.message ? error.message : '上线创意失败';
      render();
    }
  }

  async function deleteUnitCreative(unitId, creativeId) {
    try {
      var targetUnitId = Number(unitId);
      var targetCreativeId = Number(creativeId);
      if (!Number.isFinite(targetUnitId) || targetUnitId <= 0 || !Number.isFinite(targetCreativeId) || targetCreativeId <= 0) {
        throw new Error('删除参数不合法');
      }
      state.notice = '正在删除创意...';
      render();
      await requestJson('/api/admin/deliver/creatives/' + encodeURIComponent(targetCreativeId), {
        method: 'DELETE',
        headers: buildAdminHeaders(),
      });
      await loadConsole(false);
      state.editing.unitId = targetUnitId;
      openUnitPanel('edit', targetUnitId);
      state.notice = '创意已删除';
      render();
    } catch (error) {
      state.notice = error && error.message ? error.message : '删除创意失败';
      render();
    }
  }

  async function ensureCreativeRelationsUnderUnit(unit, creative) {
    var creativeId = Number(creative && creative.id);
    if (!Number.isFinite(creativeId) || creativeId <= 0) {
      return;
    }
    var linkedPositionIds = {};
    (state.consoleData.relations || []).forEach(function (relation) {
      if (Number(relation.unitId) === Number(unit.id) && relation.enabled !== false) {
        linkedPositionIds[relation.positionId] = true;
      }
    });
    var positionIds = Object.keys(linkedPositionIds);
    for (var index = 0; index < positionIds.length; index += 1) {
      var positionId = Number(positionIds[index]);
      if (!Number.isFinite(positionId) || positionId <= 0) {
        continue;
      }
      await ensureRelationEnabled(positionId, Number(unit.id), creativeId);
    }
  }

  async function ensureRelationEnabled(positionId, unitId, creativeId) {
    var existing = (state.consoleData.relations || []).find(function (relation) {
      return Number(relation.positionId) === Number(positionId)
        && Number(relation.unitId) === Number(unitId)
        && Number(relation.creativeId) === Number(creativeId);
    }) || null;
    if (existing) {
      if (existing.enabled !== false) {
        return;
      }
      await requestJson('/api/admin/deliver/relations/' + encodeURIComponent(existing.id), {
        method: 'PUT',
        headers: withJson(buildAdminHeaders()),
        body: JSON.stringify(buildRelationPayload(existing, { enabled: true })),
      });
      return;
    }
    await requestJson('/api/admin/deliver/relations', {
      method: 'POST',
      headers: withJson(buildAdminHeaders()),
      body: JSON.stringify({
        positionId: Number(positionId),
        unitId: Number(unitId),
        creativeId: Number(creativeId),
        displayOrder: resolveNextDisplayOrder(positionId),
        fallback: false,
        enabled: true,
      }),
    });
  }

  function resolveNextDisplayOrder(positionId) {
    var maxOrder = 0;
    (state.consoleData.relations || []).forEach(function (relation) {
      if (Number(relation.positionId) !== Number(positionId)) {
        return;
      }
      var currentOrder = Number(relation.displayOrder);
      if (Number.isFinite(currentOrder) && currentOrder > maxOrder) {
        maxOrder = currentOrder;
      }
    });
    return maxOrder + 1;
  }

  function buildRelationPayload(relation, overrides) {
    var next = Object.assign({}, relation || {}, overrides || {});
    return {
      positionId: Number(next.positionId),
      unitId: Number(next.unitId),
      creativeId: Number(next.creativeId),
      displayOrder: Number(next.displayOrder == null ? 1 : next.displayOrder),
      fallback: !!next.fallback,
      enabled: next.enabled !== false,
    };
  }

  function buildAutoCreativePayload(unit, material, existingCreative) {
    var nextCode = existingCreative && existingCreative.creativeCode
      ? existingCreative.creativeCode
      : generateCreativeCode(unit.unitCode, material.materialCode);
    var nextName = existingCreative && existingCreative.creativeName
      ? existingCreative.creativeName
      : buildAutoCreativeName(unit, material);
    return {
      creativeCode: nextCode,
      creativeName: nextName,
      unitCode: unit.unitCode,
      materialCode: material.materialCode,
      landingUrl: material.landingUrl || (existingCreative ? existingCreative.landingUrl : null),
      schemaJson: material.schemaJson || (existingCreative ? existingCreative.schemaJson : null),
      priority: existingCreative && existingCreative.priority != null ? existingCreative.priority : 1,
      weight: existingCreative && existingCreative.weight != null ? existingCreative.weight : 100,
      fallback: existingCreative ? !!existingCreative.fallback : false,
      previewImage: material.previewImage || material.imageUrl || (existingCreative ? existingCreative.previewImage : null),
      status: 'PUBLISHED',
      activeFrom: normalizeApiDateTimeValue(unit.activeFrom),
      activeTo: normalizeApiDateTimeValue(unit.activeTo),
    };
  }

  function buildCreativeOfflinePayload(creative) {
    return {
      creativeCode: creative.creativeCode,
      creativeName: creative.creativeName,
      unitCode: creative.unitCode,
      materialCode: creative.materialCode,
      landingUrl: creative.landingUrl || null,
      schemaJson: creative.schemaJson || null,
      priority: creative.priority == null ? 1 : Number(creative.priority),
      weight: creative.weight == null ? 100 : Number(creative.weight),
      fallback: !!creative.fallback,
      previewImage: creative.previewImage || null,
      status: 'OFFLINE',
      activeFrom: normalizeApiDateTimeValue(creative.activeFrom),
      activeTo: normalizeApiDateTimeValue(creative.activeTo),
    };
  }

  function buildCreativeOnlinePayload(creative) {
    return {
      creativeCode: creative.creativeCode,
      creativeName: creative.creativeName,
      unitCode: creative.unitCode,
      materialCode: creative.materialCode,
      landingUrl: creative.landingUrl || null,
      schemaJson: creative.schemaJson || null,
      priority: creative.priority == null ? 1 : Number(creative.priority),
      weight: creative.weight == null ? 100 : Number(creative.weight),
      fallback: !!creative.fallback,
      previewImage: creative.previewImage || null,
      status: 'PUBLISHED',
      activeFrom: normalizeApiDateTimeValue(creative.activeFrom),
      activeTo: normalizeApiDateTimeValue(creative.activeTo),
    };
  }

  function normalizeApiDateTimeValue(raw) {
    var normalized = String(raw == null ? '' : raw).trim();
    if (!normalized) {
      return null;
    }
    if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(normalized)) {
      return normalized.replace(' ', 'T');
    }
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(normalized)) {
      return normalized + ':00';
    }
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/.test(normalized)) {
      return normalized;
    }
    return null;
  }

  function findCreativeByUnitAndMaterial(unitCode, materialCode) {
    return (state.consoleData.creatives || []).find(function (item) {
      return item.unitCode === unitCode && item.materialCode === materialCode;
    }) || null;
  }

  function buildAutoCreativeName(unit, material) {
    var raw = '自动创意-' + String(unit.unitName || unit.unitCode || '投放单元') + '-' + String(material.materialName || material.materialCode || '素材');
    return raw.length > 120 ? raw.slice(0, 120) : raw;
  }

  function generateCreativeCode(unitCode, materialCode) {
    var segmentUnit = sanitizeCodeSegment(unitCode, 18);
    var segmentMaterial = sanitizeCodeSegment(materialCode, 18);
    var baseCode = 'CRT_AUTO_' + segmentUnit + '_' + segmentMaterial;
    var existingCodeSet = {};
    (state.consoleData.creatives || []).forEach(function (item) {
      existingCodeSet[String(item.creativeCode || '').toUpperCase()] = true;
    });
    var normalizedBase = baseCode.slice(0, 64).toUpperCase();
    if (!existingCodeSet[normalizedBase]) {
      return normalizedBase;
    }
    for (var index = 2; index < 1000; index += 1) {
      var suffix = '_' + index;
      var candidate = normalizedBase.slice(0, 64 - suffix.length) + suffix;
      if (!existingCodeSet[candidate]) {
        return candidate;
      }
    }
    return normalizedBase.slice(0, 60) + '_NEW';
  }

  function sanitizeCodeSegment(raw, limit) {
    var normalized = String(raw == null ? '' : raw)
      .toUpperCase()
      .replace(/[^A-Z0-9]+/g, '_')
      .replace(/^_+|_+$/g, '');
    if (!normalized) {
      normalized = 'X';
    }
    return normalized.slice(0, limit || 20);
  }

  async function upsertResource(basePath, editingId, payload, successMessage) {
    state.notice = '正在保存配置...';
    render();
    await requestJson(editingId ? basePath + '/' + editingId : basePath, {
      method: editingId ? 'PUT' : 'POST',
      headers: withJson(buildAdminHeaders()),
      body: JSON.stringify(payload),
    });
    state.notice = successMessage;
    await loadConsole(false);
  }

  function syncEntityCodeSelect(typeSelectId, codeSelectId) {
    var typeSelect = document.getElementById(typeSelectId);
    var codeSelect = document.getElementById(codeSelectId);
    if (!typeSelect || !codeSelect) {
      return;
    }
    codeSelect.innerHTML = renderEntityCodeOptions(typeSelect.value, '');
  }

  function renderEntityCodeOptions(entityType, selected) {
    var options = resolveEntityOptions(entityType);
    if (!options.length) {
      return '<option value="">暂无可选实体</option>';
    }
    return options
      .map(function (option) {
        var current = String(option.value);
        var isSelected = String(selected || '') === current ? ' selected' : '';
        return '<option value="' + escapeHtml(current) + '"' + isSelected + '>' + escapeHtml(option.label) + '</option>';
      })
      .join('');
  }

  function resolveEntityOptions(entityType) {
    var normalized = String(entityType || 'POSITION').toUpperCase();
    if (normalized === 'UNIT') {
      return (state.consoleData.units || []).map(function (item) {
        return { value: item.unitCode, label: buildCodeLabel(item.unitCode, item.unitName, item.id) };
      });
    }
    if (normalized === 'CREATIVE') {
      return (state.consoleData.creatives || []).map(function (item) {
        return { value: item.creativeCode, label: buildCodeLabel(item.creativeCode, item.creativeName, item.id) };
      });
    }
    return (state.consoleData.positions || []).map(function (item) {
      return { value: item.positionCode, label: buildCodeLabel(item.positionCode, item.positionName, item.id) };
    });
  }

  function buildCodeLabel(code, name, id) {
    return String(code || '-') + ' · ' + String(name || '-') + ' (#' + String(id || '-') + ')';
  }

  function renderCodeOptions(items, codeKey, nameKey, selected) {
    if (!items.length) {
      return '<option value="">暂无可选项</option>';
    }
    return items.map(function (item) {
      var value = item[codeKey];
      var isSelected = String(selected || '') === String(value || '') ? ' selected' : '';
      return '<option value="' + escapeHtml(value) + '"' + isSelected + '>' + escapeHtml(buildCodeLabel(value, item[nameKey], item.id)) + '</option>';
    }).join('');
  }

  function createCodeMap(items, codeKey) {
    var map = {};
    (items || []).forEach(function (item) {
      var key = String(item && item[codeKey] != null ? item[codeKey] : '').trim();
      if (key) {
        map[key] = item;
      }
    });
    return map;
  }

  function findByCode(items, codeKey, codeValue) {
    var target = String(codeValue == null ? '' : codeValue).trim();
    if (!target) {
      return null;
    }
    return (items || []).find(function (item) {
      return String(item && item[codeKey] != null ? item[codeKey] : '').trim() === target;
    }) || null;
  }

  function readActionIdFromAttr(actionElement, attrName) {
    var value = Number(actionElement && actionElement.getAttribute(attrName));
    if (!Number.isFinite(value) || value <= 0) {
      return null;
    }
    return value;
  }

  function readActionText(actionElement, attrName) {
    return String(actionElement && actionElement.getAttribute(attrName) || '').trim();
  }

  function resolveAttachableUnitsForPosition(positionId) {
    var boundUnitIds = {};
    (state.consoleData.relations || []).forEach(function (relation) {
      if (Number(relation.positionId) === Number(positionId) && relation.enabled !== false) {
        boundUnitIds[String(relation.unitId)] = true;
      }
    });
    return (state.consoleData.units || []).filter(function (unit) {
      return !boundUnitIds[String(unit.id)];
    });
  }

  function countActiveCreativesForUnitCode(unitCode) {
    var count = 0;
    (state.consoleData.creatives || []).forEach(function (creative) {
      if (creative.unitCode === unitCode && String(creative.status || '').toUpperCase() !== 'OFFLINE') {
        count += 1;
      }
    });
    return count;
  }

  function renderIdOptions(items, idKey, codeKey, nameKey, selected) {
    if (!items.length) {
      return '<option value="">暂无可选项</option>';
    }
    return items.map(function (item) {
      var value = item[idKey];
      var isSelected = String(selected || '') === String(value || '') ? ' selected' : '';
      return '<option value="' + escapeHtml(value) + '"' + isSelected + '>' + escapeHtml(buildCodeLabel(item[codeKey], item[nameKey], value)) + '</option>';
    }).join('');
  }

  function renderOptions(values, selected) {
    return (values || []).map(function (value) {
      var normalized = String(value || '');
      var isSelected = String(selected || '') === normalized ? ' selected' : '';
      return '<option value="' + escapeHtml(normalized) + '"' + isSelected + '>' + escapeHtml(formatDeliverEnumText(normalized)) + '</option>';
    }).join('');
  }

  function formatDeliverEnumText(value) {
    var normalized = String(value == null ? '' : value).trim().toUpperCase();
    if (!normalized) {
      return '-';
    }
    if (normalized === 'BANNER') {
      return langText('横幅', 'Banner');
    }
    if (normalized === 'POPUP') {
      return langText('弹窗', 'Popup');
    }
    if (normalized === 'COMMON') {
      return langText('通用展位', 'Common Placement');
    }
    if (normalized === 'EDITING') {
      return langText('编辑中', 'Editing');
    }
    if (normalized === 'PUBLISHED') {
      return langText('已发布', 'Published');
    }
    if (normalized === 'OFFLINE') {
      return langText('已下线', 'Offline');
    }
    if (normalized === 'MANUAL') {
      return langText('人工顺序', 'Manual Order');
    }
    if (normalized === 'PRIORITY') {
      return langText('优先级', 'Priority');
    }
    if (normalized === 'WEIGHT') {
      return langText('权重', 'Weight');
    }
    if (normalized === 'IMAGE') {
      return langText('图片', 'Image');
    }
    if (normalized === 'VIDEO') {
      return langText('视频', 'Video');
    }
    if (normalized === 'TEXT') {
      return langText('文本', 'Text');
    }
    if (normalized === 'POSITION') {
      return langText('展位', 'Placement');
    }
    if (normalized === 'UNIT') {
      return langText('投放单元', 'Ad Unit');
    }
    if (normalized === 'CREATIVE') {
      return langText('创意', 'Creative');
    }
    if (normalized === 'DISPLAY') {
      return langText('曝光', 'Impression');
    }
    if (normalized === 'CLICK') {
      return langText('点击', 'Click');
    }
    if (normalized === 'CONVERSION') {
      return langText('转化', 'Conversion');
    }
    if (normalized === 'USER_TAG') {
      return langText('用户标签', 'User Tag');
    }
    if (normalized === 'CHANNEL') {
      return langText('渠道', 'Channel');
    }
    if (normalized === 'SCENE') {
      return langText('场景', 'Scene');
    }
    if (normalized === 'CLIENT') {
      return langText('客户端', 'Client');
    }
    if (normalized === 'TIME_RANGE') {
      return langText('时间范围', 'Time Range');
    }
    if (normalized === 'IN') {
      return langText('包含', 'IN');
    }
    if (normalized === 'NOT_IN') {
      return langText('不包含', 'NOT IN');
    }
    if (normalized === 'EQUALS') {
      return langText('等于', 'Equals');
    }
    if (normalized === 'NOT_EQUALS') {
      return langText('不等于', 'Not Equals');
    }
    return localizeText(normalized);
  }

  function renderBooleanOptions(selected) {
    return [
      '<option value="true"' + (selected === true ? ' selected' : '') + '>' + escapeHtml(langText('是', 'Yes')) + '</option>',
      '<option value="false"' + (selected === false ? ' selected' : '') + '>' + escapeHtml(langText('否', 'No')) + '</option>',
    ].join('');
  }

  function renderField(label, controlHtml) {
    var html = '';
    html += '<label class="manager-coupon-field">';
    html += '<span class="manager-coupon-label">' + escapeHtml(label) + '</span>';
    html += controlHtml;
    html += '</label>';
    return html;
  }

  function renderStatusBadge(status) {
    var code = String(status || '-').toLowerCase();
    return '<span class="manager-coupon-status status-' + escapeHtml(code) + '">' + escapeHtml(formatDeliverEnumText(status)) + '</span>';
  }

  function renderPreviewLink(url) {
    if (!url) {
      return '<span class="deliver-muted">-</span>';
    }
    return '<a class="deliver-preview-link" href="' + escapeHtml(url) + '" target="_blank" rel="noreferrer">' + escapeHtml(langText('查看', 'View')) + '</a>';
  }

  function renderPreviewThumbnail(url, altText) {
    if (!url) {
      return '<span class="deliver-muted">-</span>';
    }
    var safeAlt = String(localizeText(altText || '预览'));
    return '<a class="deliver-preview-thumb-link" href="' + escapeHtml(url) + '" target="_blank" rel="noreferrer" title="' + escapeHtml(langText('点击查看大图', 'Click to view full image')) + '">' +
      '<img class="deliver-preview-thumb" src="' + escapeHtml(url) + '" alt="' + escapeHtml(safeAlt) + '"/>' +
    '</a>';
  }

  function formatWindow(activeFrom, activeTo) {
    return formatDateTime(activeFrom) + ' ~ ' + formatDateTime(activeTo);
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

  function toInputDateTime(raw) {
    if (!raw) {
      return '';
    }
    return String(raw).replace(' ', 'T').slice(0, 16);
  }

  function buildDateTimeLocal(offsetDays) {
    var date = new Date();
    date.setDate(date.getDate() + (offsetDays || 0));
    return [
      date.getFullYear(),
      '-',
      pad2(date.getMonth() + 1),
      '-',
      pad2(date.getDate()),
      'T',
      pad2(date.getHours()),
      ':',
      pad2(date.getMinutes()),
    ].join('');
  }

  function pad2(value) {
    return value < 10 ? '0' + value : String(value);
  }

  function readInputValue(id) {
    var element = document.getElementById(id);
    if (!element || typeof element.value !== 'string') {
      return '';
    }
    return element.value.trim();
  }

  function readOptionalValue(id) {
    var value = readInputValue(id);
    return value || null;
  }

  function requireText(value, label) {
    var normalized = String(value || '').trim();
    if (!normalized) {
      throw new Error(localizeText(label + '不能为空'));
    }
    return normalized;
  }

  function parsePositiveInteger(raw, label) {
    var normalized = requireText(raw, label);
    if (!/^\d+$/.test(normalized)) {
      throw new Error(localizeText(label + '必须为正整数'));
    }
    var value = Number(normalized);
    if (!Number.isFinite(value) || value <= 0) {
      throw new Error(localizeText(label + '必须大于0'));
    }
    return value;
  }

  function parseBooleanValue(raw, defaultValue) {
    var normalized = String(raw == null ? '' : raw).trim().toLowerCase();
    if (!normalized) {
      return !!defaultValue;
    }
    if (normalized === 'true') {
      return true;
    }
    if (normalized === 'false') {
      return false;
    }
    throw new Error(localizeText('布尔值配置不合法'));
  }

  function normalizeDateTimeInput(raw, label) {
    var normalized = requireText(raw, label);
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(normalized)) {
      return normalized + ':00';
    }
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/.test(normalized)) {
      return normalized;
    }
    throw new Error(localizeText(label + '格式必须为 yyyy-MM-ddTHH:mm:ss'));
  }

  function readActionId(actionElement) {
    var value = Number(actionElement.getAttribute('data-id'));
    if (!Number.isFinite(value) || value <= 0) {
      return null;
    }
    return value;
  }

  function findItemById(items, id) {
    var targetId = Number(id);
    if (!Number.isFinite(targetId) || targetId <= 0) {
      return null;
    }
    return (items || []).find(function (item) {
      return Number(item.id) === targetId;
    }) || null;
  }

  function createIdMap(items, idKey) {
    var map = {};
    (items || []).forEach(function (item) {
      map[item[idKey]] = item;
    });
    return map;
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
    if (element.classList && element.classList.contains('manager-modal-panel')) {
      return;
    }
    if (typeof element.scrollIntoView === 'function') {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }, 16);
}

  function hasDeliverData() {
    var data = state.consoleData || {};
    return !!((data.positions && data.positions.length) || (data.materials && data.materials.length) || (data.creatives && data.creatives.length));
  }

  function pickDisplayName() {
    if (!state.session) {
      return '-';
    }
    return state.session.displayName || state.session.username || String(state.session.adminId || '-');
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

  function normalizePositiveIdText(raw) {
    var normalized = String(raw == null ? '' : raw).trim();
    if (!/^\d+$/.test(normalized) || normalized === '0') {
      return '';
    }
    return normalized;
  }

  function withJson(headers) {
    var merged = {};
    Object.keys(headers || {}).forEach(function (key) {
      merged[key] = headers[key];
    });
    merged['Content-Type'] = 'application/json';
    return merged;
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
      return localizeText('权限不足，无法访问投放配置');
    }
    return localizeText(fallback);
  }

  function escapeHtml(text) {
    return String(text == null ? '' : text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }
})();
