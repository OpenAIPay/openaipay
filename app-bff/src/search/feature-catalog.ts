export type SearchFeatureCatalogItem = {
  featureKey: string;
  title: string;
  subtitle: string;
  page: string;
  icon: string;
  aliases: string[];
  keywords: string[];
};

type RankedFeature = {
  item: SearchFeatureCatalogItem;
  score: number;
};

export const FEATURE_CATALOG: SearchFeatureCatalogItem[] = [
  {
    featureKey: 'home',
    title: '首页',
    subtitle: '查看推荐服务与常用入口',
    page: 'home',
    icon: 'home',
    aliases: ['主页', 'home'],
    keywords: ['推荐', '常用', '服务'],
  },
  {
    featureKey: 'transfer',
    title: '转账',
    subtitle: '给联系人转账',
    page: 'transferList',
    icon: 'transfer',
    aliases: ['付款', 'pay', 'transfer'],
    keywords: ['收付款', '给好友转钱', '打款'],
  },
  {
    featureKey: 'contacts',
    title: '联系人',
    subtitle: '查看好友与联系人目录',
    page: 'messageContacts',
    icon: 'contacts',
    aliases: ['好友', '通讯录', 'contacts'],
    keywords: ['朋友', '联系人列表', '好友列表'],
  },
  {
    featureKey: 'message',
    title: '消息',
    subtitle: '进入消息会话首页',
    page: 'message',
    icon: 'message',
    aliases: ['聊天', '对话', 'chat'],
    keywords: ['会话', '消息列表'],
  },
  {
    featureKey: 'red-packet',
    title: '红包',
    subtitle: '发红包给好友',
    page: 'redPacket',
    icon: 'red-packet',
    aliases: ['发红包', 'hongbao'],
    keywords: ['拜年红包', '红包会场'],
  },
  {
    featureKey: 'red-packet-history',
    title: '红包记录',
    subtitle: '查看红包收发记录',
    page: 'redPacketHistory',
    icon: 'history',
    aliases: ['红包历史', 'hongbao-history'],
    keywords: ['收红包', '发红包记录'],
  },
  {
    featureKey: 'balance',
    title: '余额',
    subtitle: '查看钱包余额',
    page: 'balance',
    icon: 'wallet',
    aliases: ['钱包', 'wallet'],
    keywords: ['账户余额', '可用余额'],
  },
  {
    featureKey: 'balance-detail',
    title: '余额明细',
    subtitle: '查看余额流水与账单',
    page: 'balanceChangeDetail',
    icon: 'bill',
    aliases: ['账单', '流水', '明细'],
    keywords: ['余额流水', '交易明细'],
  },
  {
    featureKey: 'aicash',
    title: '爱存',
    subtitle: '查看爱存首页',
    page: 'aicash',
    icon: 'fund',
    aliases: ['理财', '基金', 'fund', 'aicash'],
    keywords: ['爱存收益', '货币基金'],
  },
  {
    featureKey: 'aicash-deposit',
    title: '爱存转入',
    subtitle: '把钱转入爱存',
    page: 'aicashDeposit',
    icon: 'deposit',
    aliases: ['转入', '存入', 'deposit', 'aicash-deposit'],
    keywords: ['充值爱存', '申购'],
  },
  {
    featureKey: 'aicash-withdraw',
    title: '爱存转出',
    subtitle: '从爱存转出到余额',
    page: 'aicashWithdraw',
    icon: 'withdraw',
    aliases: ['转出', '取出', 'withdraw', 'aicash-withdraw'],
    keywords: ['赎回', '爱存提现'],
  },
  {
    featureKey: 'bank-cards',
    title: '银行卡',
    subtitle: '管理已绑定银行卡',
    page: 'bankCards',
    icon: 'bank-card',
    aliases: ['卡包', 'bankcard'],
    keywords: ['绑定卡', '储蓄卡', '信用卡'],
  },
  {
    featureKey: 'settings',
    title: '设置',
    subtitle: '查看通用设置与安全偏好',
    page: 'settings',
    icon: 'settings',
    aliases: ['偏好设置', 'security', 'preferences'],
    keywords: ['通用设置', '隐私设置'],
  },
  {
    featureKey: 'profile',
    title: '个人信息',
    subtitle: '查看个人资料',
    page: 'personalInfo',
    icon: 'profile',
    aliases: ['个人资料', 'profile'],
    keywords: ['实名信息', '账号资料'],
  },
  {
    featureKey: 'credit-home',
    title: '爱花',
    subtitle: '查看信用账户首页',
    page: 'creditHome',
    icon: 'credit',
    aliases: ['信用', 'credit'],
    keywords: ['信用额度', '账单'],
  },
  {
    featureKey: 'credit-repay',
    title: '爱花还款',
    subtitle: '进入还款首页',
    page: 'creditRepay',
    icon: 'repay',
    aliases: ['还款', 'repay'],
    keywords: ['信用还款', '账单还款'],
  },
  {
    featureKey: 'feedback',
    title: '产品建议',
    subtitle: '提交意见反馈',
    page: 'productSuggestion',
    icon: 'feedback',
    aliases: ['反馈', '意见反馈', 'feedback'],
    keywords: ['功能建议', '问题反馈'],
  },
];

export function searchFeatureCatalog(keyword: string, limit: number): SearchFeatureCatalogItem[] {
  const normalizedKeyword = normalizeComparableText(keyword);
  if (!normalizedKeyword) {
    return [];
  }

  return FEATURE_CATALOG.map((item) => ({ item, score: resolveFeatureScore(item, normalizedKeyword) }))
    .filter((candidate) => candidate.score > 0)
    .sort((left, right) => {
      if (left.score !== right.score) {
        return right.score - left.score;
      }
      return left.item.title.localeCompare(right.item.title, 'zh-Hans-CN');
    })
    .slice(0, Math.max(1, limit))
    .map((candidate) => candidate.item);
}

function resolveFeatureScore(item: SearchFeatureCatalogItem, normalizedKeyword: string): number {
  let bestScore = scoreText(item.title, normalizedKeyword, 1000, 920, 820);
  item.aliases.forEach((alias) => {
    bestScore = Math.max(bestScore, scoreText(alias, normalizedKeyword, 960, 900, 800));
  });
  item.keywords.forEach((entry) => {
    bestScore = Math.max(bestScore, scoreText(entry, normalizedKeyword, 900, 860, 760));
  });
  return bestScore;
}

function scoreText(source: string, normalizedKeyword: string, exactScore: number, prefixScore: number, containsScore: number): number {
  const normalizedSource = normalizeComparableText(source);
  if (!normalizedSource) {
    return 0;
  }
  if (normalizedSource === normalizedKeyword) {
    return exactScore;
  }
  if (normalizedSource.startsWith(normalizedKeyword)) {
    return prefixScore;
  }
  if (normalizedSource.includes(normalizedKeyword)) {
    return containsScore;
  }
  return 0;
}

function normalizeComparableText(raw: string): string {
  return raw.trim().toLowerCase().replace(/[\s\-_/]+/g, '');
}
