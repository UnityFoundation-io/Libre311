import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { createAutoSave, debounce, throttle, DEFAULT_DEBOUNCE_MS } from './autoSave';

/**
 * Unit tests for auto-save utility functions
 *
 * These tests verify:
 * 1. Debounce timing behavior
 * 2. Save function execution
 * 3. Success and error callbacks
 * 4. Cancellation behavior
 * 5. Immediate save functionality
 */

describe('createAutoSave', () => {
	beforeEach(() => {
		vi.useFakeTimers();
	});

	afterEach(() => {
		vi.useRealTimers();
		vi.restoreAllMocks();
	});

	describe('Debounce Timing', () => {
		it('should use default debounce of 500ms', () => {
			expect(DEFAULT_DEBOUNCE_MS).toBe(500);
		});

		it('should not call saveFn immediately when triggered', () => {
			const saveFn = vi.fn().mockResolvedValue('result');

			const { trigger } = createAutoSave({
				saveFn,
				showToast: false
			});

			trigger();

			expect(saveFn).not.toHaveBeenCalled();
		});

		it('should call saveFn after debounce delay', async () => {
			const saveFn = vi.fn().mockResolvedValue('result');

			const { trigger } = createAutoSave({
				saveFn,
				debounceMs: 500,
				showToast: false
			});

			trigger();

			// Advance timers past debounce delay
			vi.advanceTimersByTime(500);

			// Need to let promises resolve
			await vi.runAllTimersAsync();

			expect(saveFn).toHaveBeenCalledTimes(1);
		});

		it('should reset debounce timer on subsequent triggers', async () => {
			const saveFn = vi.fn().mockResolvedValue('result');

			const { trigger } = createAutoSave({
				saveFn,
				debounceMs: 500,
				showToast: false
			});

			trigger();
			vi.advanceTimersByTime(300); // 300ms
			trigger(); // Reset timer
			vi.advanceTimersByTime(300); // 600ms total, but only 300ms since last trigger
			trigger(); // Reset timer again
			vi.advanceTimersByTime(500); // Now 500ms since last trigger

			await vi.runAllTimersAsync();

			// Should only be called once, after the final debounce
			expect(saveFn).toHaveBeenCalledTimes(1);
		});

		it('should use custom debounce delay', async () => {
			const saveFn = vi.fn().mockResolvedValue('result');

			const { trigger } = createAutoSave({
				saveFn,
				debounceMs: 1000,
				showToast: false
			});

			trigger();

			vi.advanceTimersByTime(500);
			expect(saveFn).not.toHaveBeenCalled();

			vi.advanceTimersByTime(500);
			await vi.runAllTimersAsync();

			expect(saveFn).toHaveBeenCalledTimes(1);
		});
	});

	describe('Callbacks', () => {
		it('should call onSuccess callback with result', async () => {
			const saveFn = vi.fn().mockResolvedValue({ id: 1, name: 'test' });
			const onSuccess = vi.fn();

			const { trigger } = createAutoSave({
				saveFn,
				onSuccess,
				showToast: false
			});

			trigger();
			await vi.runAllTimersAsync();

			expect(onSuccess).toHaveBeenCalledWith({ id: 1, name: 'test' });
		});

		it('should call onError callback on failure', async () => {
			const error = new Error('Network error');
			const saveFn = vi.fn().mockRejectedValue(error);
			const onError = vi.fn();

			const { trigger } = createAutoSave({
				saveFn,
				onError,
				showToast: false
			});

			trigger();
			await vi.runAllTimersAsync();

			expect(onError).toHaveBeenCalledWith(error);
		});

		it('should convert non-Error exceptions to Error objects', async () => {
			const saveFn = vi.fn().mockRejectedValue('string error');
			const onError = vi.fn();

			const { trigger } = createAutoSave({
				saveFn,
				onError,
				showToast: false
			});

			trigger();
			await vi.runAllTimersAsync();

			expect(onError).toHaveBeenCalled();
			const calledError = onError.mock.calls[0][0];
			expect(calledError).toBeInstanceOf(Error);
		});
	});

	describe('Cancellation', () => {
		it('should cancel pending save when cancel is called', async () => {
			const saveFn = vi.fn().mockResolvedValue('result');

			const { trigger, cancel } = createAutoSave({
				saveFn,
				showToast: false
			});

			trigger();
			vi.advanceTimersByTime(200);
			cancel();
			vi.advanceTimersByTime(500);

			await vi.runAllTimersAsync();

			expect(saveFn).not.toHaveBeenCalled();
		});

		it('should report pending status correctly', () => {
			const saveFn = vi.fn().mockResolvedValue('result');

			const { trigger, isPending, cancel } = createAutoSave({
				saveFn,
				showToast: false
			});

			expect(isPending()).toBe(false);

			trigger();
			expect(isPending()).toBe(true);

			cancel();
			expect(isPending()).toBe(false);
		});
	});

	describe('Immediate Save', () => {
		it('should execute save immediately with saveNow', async () => {
			const saveFn = vi.fn().mockResolvedValue('result');

			const { saveNow } = createAutoSave({
				saveFn,
				showToast: false
			});

			const result = await saveNow();

			expect(saveFn).toHaveBeenCalledTimes(1);
			expect(result).toBe('result');
		});

		it('should cancel pending debounced save when saveNow is called', async () => {
			const saveFn = vi.fn().mockResolvedValue('result');

			const { trigger, saveNow } = createAutoSave({
				saveFn,
				debounceMs: 1000,
				showToast: false
			});

			trigger();
			vi.advanceTimersByTime(500);

			await saveNow();
			await vi.runAllTimersAsync();

			// Should only be called once (from saveNow)
			expect(saveFn).toHaveBeenCalledTimes(1);
		});

		it('should return undefined on error in saveNow', async () => {
			const saveFn = vi.fn().mockRejectedValue(new Error('Failed'));

			const { saveNow } = createAutoSave({
				saveFn,
				showToast: false
			});

			const result = await saveNow();

			expect(result).toBeUndefined();
		});
	});
});

describe('debounce utility', () => {
	beforeEach(() => {
		vi.useFakeTimers();
	});

	afterEach(() => {
		vi.useRealTimers();
	});

	it('should debounce function calls', () => {
		const fn = vi.fn();
		const debouncedFn = debounce(fn, 100);

		debouncedFn();
		debouncedFn();
		debouncedFn();

		expect(fn).not.toHaveBeenCalled();

		vi.advanceTimersByTime(100);

		expect(fn).toHaveBeenCalledTimes(1);
	});

	it('should pass arguments to debounced function', () => {
		const fn = vi.fn();
		const debouncedFn = debounce(fn, 100);

		debouncedFn('arg1', 'arg2');

		vi.advanceTimersByTime(100);

		expect(fn).toHaveBeenCalledWith('arg1', 'arg2');
	});

	it('should use latest arguments on multiple calls', () => {
		const fn = vi.fn();
		const debouncedFn = debounce(fn, 100);

		debouncedFn('first');
		debouncedFn('second');
		debouncedFn('third');

		vi.advanceTimersByTime(100);

		expect(fn).toHaveBeenCalledTimes(1);
		expect(fn).toHaveBeenCalledWith('third');
	});
});

describe('throttle utility', () => {
	beforeEach(() => {
		vi.useFakeTimers();
	});

	afterEach(() => {
		vi.useRealTimers();
	});

	it('should execute immediately on first call', () => {
		const fn = vi.fn();
		const throttledFn = throttle(fn, 100);

		throttledFn();

		expect(fn).toHaveBeenCalledTimes(1);
	});

	it('should throttle subsequent calls', () => {
		const fn = vi.fn();
		const throttledFn = throttle(fn, 100);

		throttledFn(); // Immediate
		throttledFn(); // Throttled
		throttledFn(); // Throttled

		expect(fn).toHaveBeenCalledTimes(1);

		vi.advanceTimersByTime(100);

		expect(fn).toHaveBeenCalledTimes(2);
	});

	it('should allow execution after interval passes', () => {
		const fn = vi.fn();
		const throttledFn = throttle(fn, 100);

		throttledFn();
		vi.advanceTimersByTime(100);
		throttledFn();

		expect(fn).toHaveBeenCalledTimes(2);
	});

	it('should pass arguments to throttled function', () => {
		const fn = vi.fn();
		const throttledFn = throttle(fn, 100);

		throttledFn('arg1', 'arg2');

		expect(fn).toHaveBeenCalledWith('arg1', 'arg2');
	});
});
