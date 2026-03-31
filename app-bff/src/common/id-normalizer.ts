export const MAX_LONG_ID_DIGITS = 20;

type NormalizeOptions = {
  minDigits?: number;
  maxDigits?: number;
};

export function normalizePositiveLongId(raw: unknown, options?: NormalizeOptions): string {
  const minDigits = resolveDigits(options?.minDigits, 1);
  const maxDigits = resolveDigits(options?.maxDigits, MAX_LONG_ID_DIGITS);
  if (maxDigits < minDigits) {
    return '';
  }

  if (typeof raw === 'string') {
    const normalized = raw.trim();
    if (!normalized || normalized === '0') {
      return '';
    }
    const pattern = new RegExp(`^\\d{${minDigits},${maxDigits}}$`);
    return pattern.test(normalized) ? normalized : '';
  }

  if (typeof raw === 'number') {
    if (!Number.isFinite(raw) || raw <= 0 || !Number.isInteger(raw) || !Number.isSafeInteger(raw)) {
      return '';
    }
    const digits = String(Math.trunc(raw));
    if (digits.length < minDigits || digits.length > maxDigits) {
      return '';
    }
    return digits;
  }

  if (typeof raw === 'bigint') {
    if (raw <= 0n) {
      return '';
    }
    const digits = raw.toString();
    if (digits.length < minDigits || digits.length > maxDigits) {
      return '';
    }
    return digits;
  }

  return '';
}

export function isUnsafePositiveLongNumber(raw: unknown): boolean {
  return typeof raw === 'number'
    && Number.isFinite(raw)
    && raw > 0
    && Number.isInteger(raw)
    && !Number.isSafeInteger(raw);
}

function resolveDigits(raw: number | undefined, fallback: number): number {
  if (typeof raw !== 'number' || !Number.isInteger(raw) || raw <= 0) {
    return fallback;
  }
  return raw;
}
