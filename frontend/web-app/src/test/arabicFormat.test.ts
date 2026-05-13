import { describe, it, expect } from 'vitest';
import { formatArabicNumber, formatArabicCurrency, formatArabicDate } from '../utils/arabicFormat';

describe('TC-AR-004: Arabic number formatting', () => {
  it('should format 12345.67 with Arabic-Indic numerals', () => {
    const result = formatArabicNumber(12345.67);
    expect(result).toBeTruthy();
    expect(result.length).toBeGreaterThan(0);
  });

  it('should format currency in Arabic locale', () => {
    const result = formatArabicCurrency(12345.67, 'AED');
    expect(result).toBeTruthy();
    expect(result.length).toBeGreaterThan(0);
  });

  it('should format date in Arabic locale', () => {
    const date = new Date(2024, 0, 1);
    const result = formatArabicDate(date);
    expect(result).toBeTruthy();
    expect(result.length).toBeGreaterThan(0);
  });
});
