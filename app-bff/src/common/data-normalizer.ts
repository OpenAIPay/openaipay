import { BackendHttpService } from './backend-http.service';
import { normalizePositiveLongId } from './id-normalizer';

export function normalizeUserId(raw: string | number | undefined, fallback: string): string {
  const normalized = normalizePositiveLongId(raw, { minDigits: 6, maxDigits: 20 });
  if (normalized) {
    return normalized;
  }
  return fallback;
}

export function normalizeOptionalUserId(raw: string | number | undefined): string {
  return normalizePositiveLongId(raw, { minDigits: 1, maxDigits: 20 });
}

export function normalizeFirstAvailableUserId(...values: Array<string | number | undefined | null>): string {
  for (const value of values) {
    const normalized = normalizeOptionalUserId(value ?? undefined);
    if (normalized) {
      return normalized;
    }
  }
  return '';
}

export function normalizeAvatarUrl(backendHttpService: BackendHttpService, raw: string | undefined): string {
  const normalized = backendHttpService.resolveAbsoluteUrl(raw);
  if (normalized.includes('api.dicebear.com') && normalized.includes('/svg?')) {
    return normalized.replace('/svg?', '/png?');
  }
  return normalized;
}

export function firstNonEmpty(...values: Array<string | undefined | null>): string {
  for (const value of values) {
    if (value && value.trim().length > 0) {
      return value.trim();
    }
  }
  return '';
}
