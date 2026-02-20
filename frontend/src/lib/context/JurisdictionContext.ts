import { getContext, setContext } from 'svelte';
import { readable, type Readable } from 'svelte/store';
import type { JurisdictionConfig } from '$lib/services/Libre311/Libre311';
import { useLibre311Service } from './Libre311Context';

const jurisdictionCtxKey = Symbol();

export function createJurisdictionContext(): Readable<JurisdictionConfig> {
	const libre311 = useLibre311Service();
	const jurisdictionStore = readable<JurisdictionConfig>(libre311.getJurisdictionConfig());
	setContext(jurisdictionCtxKey, jurisdictionStore);
	return jurisdictionStore;
}

export function useJurisdiction(): Readable<JurisdictionConfig> {
	return getContext<Readable<JurisdictionConfig>>(jurisdictionCtxKey);
}
