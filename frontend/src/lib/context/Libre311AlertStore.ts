import { Queue } from '$lib/utils/Queue';
import type { Readable } from 'svelte/motion';
import { writable } from 'svelte/store';

export type AlertType = 'success' | 'error' | 'info' | 'warn';
export type Alert = {
	type: AlertType;
	title: string;
	description?: string;
	duration?: number | null; // ms to auto-dismiss, null to require manual close
};

// Default auto-dismiss duration for success/info alerts (in ms)
export const DEFAULT_AUTO_DISMISS_DURATION = 4000;

export type Libre311Alert = {
	alert(alert: Alert): void;
};

export type AlertStore = {
	currentAlert: Readable<Alert | undefined>;
	close: () => void;
} & Libre311Alert;

export const ALERT_ANIMATION_DURATION = 600;

export function createAlertStore(): AlertStore {
	const alertQueue = new Queue<Alert>();
	const currentAlert = writable<Alert | undefined>();
	let autoDismissTimeout: ReturnType<typeof setTimeout> | null = null;

	function getEffectiveDuration(alert: Alert): number | null {
		if (alert.duration !== undefined) {
			return alert.duration;
		}
		// Auto-dismiss success/info by default, require manual close for error/warn
		if (alert.type === 'success' || alert.type === 'info') {
			return DEFAULT_AUTO_DISMISS_DURATION;
		}
		return null;
	}

	function scheduleAutoDismiss(alert: Alert) {
		clearAutoDismiss();
		const duration = getEffectiveDuration(alert);
		if (duration !== null) {
			autoDismissTimeout = setTimeout(() => {
				close();
			}, duration);
		}
	}

	function clearAutoDismiss() {
		if (autoDismissTimeout !== null) {
			clearTimeout(autoDismissTimeout);
			autoDismissTimeout = null;
		}
	}

	function alert(alert: Alert) {
		alertQueue.enqueue(alert);
		const current = alertQueue.peek();
		currentAlert.set(current);
		if (current) {
			scheduleAutoDismiss(current);
		}
	}

	function close() {
		clearAutoDismiss();
		alertQueue.dequeue();
		if (!alertQueue.isEmpty()) {
			currentAlert.set(undefined);
			setTimeout(() => {
				const next = alertQueue.peek();
				currentAlert.set(next);
				if (next) {
					scheduleAutoDismiss(next);
				}
			}, ALERT_ANIMATION_DURATION);
		} else {
			currentAlert.set(alertQueue.peek());
		}
	}

	return {
		alert,
		close,
		currentAlert
	};
}
