import { describe, it, expect } from 'vitest';

describe('Env Mode is accurate', () => {
	it('mode is test', () => {
		const mode = import.meta.env.MODE;
		expect(mode).toBe('test');
	});
});
