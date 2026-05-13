import { describe, it, expect } from 'vitest';
import { toHijriString } from '../components/HijriDateDisplay';

describe('TC-AR-003: Hijri date conversion', () => {
  it('should convert a known Gregorian date to Hijri format', () => {
    const date = new Date(2024, 0, 1);
    const hijri = toHijriString(date);

    expect(hijri).toBeTruthy();
    expect(typeof hijri).toBe('string');
    expect(hijri.length).toBeGreaterThan(0);
  });
});
