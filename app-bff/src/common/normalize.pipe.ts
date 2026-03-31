import { Injectable, PipeTransform } from '@nestjs/common';

@Injectable()
export class NormalizePipe implements PipeTransform {
  transform(value: unknown): unknown {
    return this.normalize(value);
  }

  private normalize(value: unknown): unknown {
    if (value === null || value === undefined) {
      return value;
    }

    if (typeof value === 'string') {
      const v = value.trim();
      if (v === '') {
        return undefined;
      }
      if (v === 'null' || v === 'undefined') {
        return undefined;
      }
      if (v === 'true') {
        return true;
      }
      if (v === 'false') {
        return false;
      }
      if (/^-?\d+$/.test(v) && !this.hasLeadingZeroInteger(v)) {
        const parsed = Number.parseInt(v, 10);
        if (Number.isSafeInteger(parsed)) {
          return parsed;
        }
      }
      if (/^-?\d+\.\d+$/.test(v)) {
        const parsed = Number.parseFloat(v);
        if (Number.isFinite(parsed)) {
          return parsed;
        }
      }
      return v;
    }

    if (Array.isArray(value)) {
      return value.map((item) => this.normalize(item)).filter((item) => item !== undefined);
    }

    if (typeof value === 'object') {
      const record = value as Record<string, unknown>;
      const out: Record<string, unknown> = {};
      for (const [k, v] of Object.entries(record)) {
        const normalized = this.normalize(v);
        if (normalized !== undefined) {
          out[k] = normalized;
        }
      }
      return out;
    }

    return value;
  }

  private hasLeadingZeroInteger(value: string): boolean {
    const signless = value.startsWith('-') ? value.slice(1) : value;
    return signless.length > 1 && signless.startsWith('0');
  }
}
