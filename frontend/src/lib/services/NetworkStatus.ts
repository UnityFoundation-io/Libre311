import { readable, type Readable } from 'svelte/store';

export type NetworkStatus = {
	isOnline: Readable<boolean>;
	get online(): boolean;
};

export function createNetworkStatus(): NetworkStatus {
	let currentValue = typeof navigator !== 'undefined' ? navigator.onLine : true;

	const isOnline = readable<boolean>(currentValue, (set) => {
		if (typeof window === 'undefined') return;

		function handleOnline() {
			currentValue = true;
			set(true);
		}

		function handleOffline() {
			currentValue = false;
			set(false);
		}

		window.addEventListener('online', handleOnline);
		window.addEventListener('offline', handleOffline);

		return () => {
			window.removeEventListener('online', handleOnline);
			window.removeEventListener('offline', handleOffline);
		};
	});

	return {
		isOnline,
		get online() {
			return currentValue;
		}
	};
}
