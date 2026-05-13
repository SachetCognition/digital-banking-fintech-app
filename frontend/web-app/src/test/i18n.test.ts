import { describe, it, expect } from 'vitest';
import en from '../i18n/locales/en.json';
import ar from '../i18n/locales/ar.json';

function flattenKeys(obj: Record<string, unknown>, prefix = ''): string[] {
  const keys: string[] = [];
  for (const key of Object.keys(obj)) {
    const fullKey = prefix ? `${prefix}.${key}` : key;
    const value = obj[key];
    if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
      keys.push(...flattenKeys(value as Record<string, unknown>, fullKey));
    } else {
      keys.push(fullKey);
    }
  }
  return keys;
}

describe('TC-AR-001: i18n key coverage', () => {
  it('ar.json must have 100% key coverage of en.json', () => {
    const enKeys = flattenKeys(en).sort();
    const arKeys = flattenKeys(ar).sort();

    const missingInAr = enKeys.filter((k) => !arKeys.includes(k));
    expect(missingInAr, `Missing keys in ar.json: ${missingInAr.join(', ')}`).toEqual([]);
  });
});
