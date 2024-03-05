import { writable } from 'svelte/store';
import { getContext, setContext } from 'svelte';
import type { ActiveComponentId, ActiveComponentIdContext } from './types';

const activeComponentId = writable<ActiveComponentId>(null);

export function setJackListOptions() {
	// Set Context
	setContext('active', activeComponentId);
}

export function getJackListOptions() {
	const activeComponentId = getContext<ActiveComponentIdContext>('active');

	return { activeComponentId };
}
