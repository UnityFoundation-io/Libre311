import { Queue } from '$lib/utils/Queue';
import type { Readable } from 'svelte/motion';
import { writable } from 'svelte/store';

export type AlertType = 'success' | 'error' | 'info' | 'warn';
export type Alert = {
	type: AlertType;
	title: string;
	description?: string;
};

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

	function alert(alert: Alert) {
		alertQueue.enqueue(alert);
		currentAlert.set(alertQueue.peek());
	}

	function close() {
		alertQueue.dequeue();
		if (!alertQueue.isEmpty()) {
			currentAlert.set(undefined);
			setTimeout(() => {
				currentAlert.set(alertQueue.peek());
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
