import { BadRequestException, Injectable } from '@nestjs/common';
import { BackendHttpService } from '../common/backend-http.service';
import { firstNonEmpty, normalizeAvatarUrl, normalizeFirstAvailableUserId, normalizeUserId } from '../common/data-normalizer';
import { BackendEnvelope, rethrowMappedUpstreamError } from '../common/upstream';
import { searchFeatureCatalog } from './feature-catalog';

type BackendContactSearch = {
  userId?: string | number;
  aipayUid?: string;
  nickname?: string;
  avatarUrl?: string;
  mobile?: string;
  maskedRealName?: string;
  friend?: boolean;
  blocked?: boolean;
  remark?: string;
};

type SearchSection = {
  type: 'contacts' | 'features';
  title: string;
  count: number;
  items: Record<string, unknown>[];
};

@Injectable()
export class SearchService {
  private static readonly DEFAULT_LIMIT = 8;
  private static readonly MAX_LIMIT = 20;
  private static readonly CONTACT_FETCH_MULTIPLIER = 5;
  private static readonly CONTACT_FETCH_MAX_LIMIT = 100;

  constructor(private readonly backendHttpService: BackendHttpService) {}

  async search(userId: string, keyword: string, limit?: number): Promise<Record<string, unknown>> {
    const normalizedUserId = this.normalizeUserIdInput(userId);
    const normalizedKeyword = this.normalizeKeyword(keyword);
    const safeLimit = this.normalizeLimit(limit);

    const [contacts, features] = await Promise.all([
      this.searchContacts(normalizedUserId, normalizedKeyword, safeLimit),
      Promise.resolve(this.searchFeatures(normalizedKeyword, safeLimit)),
    ]);

    const sections: SearchSection[] = [];
    if (contacts.length > 0) {
      sections.push({
        type: 'contacts',
        title: '联系人',
        count: contacts.length,
        items: contacts,
      });
    }
    if (features.length > 0) {
      sections.push({
        type: 'features',
        title: '功能',
        count: features.length,
        items: features,
      });
    }

    return {
      keyword: normalizedKeyword,
      sections,
      empty: sections.length === 0,
      meta: {
        userId: normalizedUserId,
        limit: safeLimit,
      },
    };
  }

  private async searchContacts(userId: string, keyword: string, limit: number): Promise<Record<string, unknown>[]> {
    const upstreamLimit = this.normalizeContactFetchLimit(limit);
    try {
      const response = await this.backendHttpService.get<BackendEnvelope<BackendContactSearch[]>>(
        `/api/contacts/search?ownerUserId=${encodeURIComponent(userId)}&keyword=${encodeURIComponent(keyword)}&limit=${upstreamLimit}`,
      );
      const items = Array.isArray(response.data?.data) ? response.data?.data ?? [] : [];
      return items
        .filter((item) => item && item.friend !== false)
        .map((item) => this.normalizeContactSearchItem(item, keyword))
        .slice(0, limit);
    } catch (error) {
      rethrowMappedUpstreamError(error, '搜索失败');
    }
  }

  private searchFeatures(keyword: string, limit: number): Record<string, unknown>[] {
    return searchFeatureCatalog(keyword, limit).map((item) => ({
      id: `feature_${item.featureKey}`,
      featureKey: item.featureKey,
      title: item.title,
      subtitle: item.subtitle,
      icon: item.icon,
      route: {
        type: 'page',
        page: item.page,
      },
    }));
  }

  private normalizeContactSearchItem(raw: BackendContactSearch, keyword: string): Record<string, unknown> {
    const userId = normalizeFirstAvailableUserId(raw.userId, raw.aipayUid) || normalizeUserId(raw.userId, '');
    const title = firstNonEmpty(raw.remark, raw.nickname, raw.maskedRealName, this.maskMobile(raw.mobile), raw.aipayUid, userId);
    const subtitleParts: string[] = [];

    if (raw.remark && raw.remark.trim().length > 0 && raw.remark.trim() !== title) {
      subtitleParts.push(`备注 ${raw.remark.trim()}`);
    }
    if (raw.nickname && raw.nickname.trim().length > 0 && raw.nickname.trim() !== title) {
      subtitleParts.push(`昵称 ${raw.nickname.trim()}`);
    }
    if (raw.maskedRealName && raw.maskedRealName.trim().length > 0 && raw.maskedRealName.trim() !== title) {
      subtitleParts.push(`实名 ${raw.maskedRealName.trim()}`);
    }
    if (raw.mobile && raw.mobile.trim().length > 0) {
      subtitleParts.push(`手机号 ${this.maskMobile(raw.mobile)}`);
    }

    return {
      id: `contact_${userId}`,
      userId,
      aipayUid: firstNonEmpty(raw.aipayUid, userId),
      title,
      subtitle: subtitleParts.slice(0, 2).join(' · '),
      avatarUrl: normalizeAvatarUrl(this.backendHttpService, raw.avatarUrl),
      matchedField: this.resolveMatchedField(raw, keyword),
      route: {
        type: 'contact',
        userId,
        page: 'messageContacts',
      },
    };
  }

  private resolveMatchedField(raw: BackendContactSearch, keyword: string): 'remark' | 'nickname' | 'real_name' | 'mobile' {
    const normalizedKeyword = this.normalizeComparableText(keyword);
    if (this.matchesMobile(raw.mobile, normalizedKeyword)) {
      return 'mobile';
    }
    if (this.matchesText(raw.remark, normalizedKeyword)) {
      return 'remark';
    }
    if (this.matchesText(raw.nickname, normalizedKeyword)) {
      return 'nickname';
    }
    if (this.matchesText(raw.maskedRealName, normalizedKeyword)) {
      return 'real_name';
    }
    return 'nickname';
  }

  private matchesText(source: string | undefined, normalizedKeyword: string): boolean {
    if (!source) {
      return false;
    }
    return this.normalizeComparableText(source).includes(normalizedKeyword);
  }

  private matchesMobile(source: string | undefined, normalizedKeyword: string): boolean {
    if (!source) {
      return false;
    }
    const normalizedDigitsKeyword = normalizedKeyword.replace(/\D+/g, '');
    if (normalizedDigitsKeyword.length === 0) {
      return false;
    }
    const normalizedMobile = source.replace(/\D+/g, '');
    return normalizedMobile.length > 0 && normalizedMobile.includes(normalizedDigitsKeyword);
  }

  private maskMobile(raw: string | undefined): string {
    const digits = (raw ?? '').replace(/\D+/g, '');
    if (digits.length < 7) {
      return raw?.trim() ?? '';
    }
    return `${digits.slice(0, 3)}****${digits.slice(-4)}`;
  }

  private normalizeUserIdInput(raw: string): string {
    const normalized = raw.trim();
    if (!/^\d{6,20}$/.test(normalized)) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'uid 参数格式不正确',
      });
    }
    return normalized;
  }

  private normalizeKeyword(raw: string): string {
    const normalized = raw.trim();
    if (normalized.length === 0) {
      throw new BadRequestException({
        code: 'INVALID_ARGUMENT',
        message: 'keyword 参数不能为空',
      });
    }
    return normalized;
  }

  private normalizeLimit(raw: number | undefined): number {
    if (raw === undefined || !Number.isFinite(raw) || raw <= 0) {
      return SearchService.DEFAULT_LIMIT;
    }
    return Math.min(Math.trunc(raw), SearchService.MAX_LIMIT);
  }

  private normalizeContactFetchLimit(limit: number): number {
    const expandedLimit = Math.trunc(limit) * SearchService.CONTACT_FETCH_MULTIPLIER;
    return Math.min(Math.max(limit, expandedLimit), SearchService.CONTACT_FETCH_MAX_LIMIT);
  }

  private normalizeComparableText(raw: string): string {
    return raw.trim().toLowerCase().replace(/[\s\-_/]+/g, '');
  }
}
