import { describe, it, expect, vi } from 'vitest';
import { toDatetimeLocal } from './functions';

describe('functions utility', () => {
	describe('toDatetimeLocal', () => {
		it('should return empty string for null/undefined', () => {
			expect(toDatetimeLocal(null)).toBe('');
			expect(toDatetimeLocal(undefined)).toBe('');
		});

		it('should format date string correctly ignoring timezone', () => {
			// Mocking getTimezoneOffset to ensure consistent test results
			const date = new Date('2023-05-20T10:30:00Z');
			const originalGetOffset = Date.prototype.getTimezoneOffset;
			Date.prototype.getTimezoneOffset = vi.fn(() => 0); // UTC

			try {
				const result = toDatetimeLocal(date);
				expect(result).toBe('2023-05-20T10:30');
			} finally {
				Date.prototype.getTimezoneOffset = originalGetOffset;
			}
		});

		it('should handle local timezone adjustment', () => {
			const date = new Date('2023-05-20T10:30:00Z');
			const originalGetOffset = Date.prototype.getTimezoneOffset;
			Date.prototype.getTimezoneOffset = vi.fn(() => 120); // 2 hours ahead of UTC (e.g. UTC-2)

			try {
				// 10:30 UTC - 2 hours = 08:30 local
				const result = toDatetimeLocal(date);
				expect(result).toBe('2023-05-20T08:30');
			} finally {
				Date.prototype.getTimezoneOffset = originalGetOffset;
			}
		});
	});
});
