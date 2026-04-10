import { getContext, setContext } from 'svelte';
import { writable, type Writable } from 'svelte/store';
import type { JurisdictionConfig } from '$lib/services/Libre311/Libre311';
import { useLibre311Service } from './Libre311Context';

const jurisdictionCtxKey = Symbol();

export function createJurisdictionContext(): Writable<JurisdictionConfig> {
	const libre311 = useLibre311Service();
	const jurisdictionStore = writable<JurisdictionConfig>(libre311.getJurisdictionConfig());
	setContext(jurisdictionCtxKey, jurisdictionStore);
	return jurisdictionStore;
}

export function useJurisdiction(): Writable<JurisdictionConfig> {
	return getContext<Writable<JurisdictionConfig>>(jurisdictionCtxKey);
}
