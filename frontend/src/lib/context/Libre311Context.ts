import { type Libre311Service } from '$lib/services/Libre311/Libre311';
import { getContext, setContext } from 'svelte';

const libre311CtxKey = Symbol();

export type Libre311Context = {
	service: Libre311Service;
};

export type Libre311ContextProviderProps = {
	service: Libre311Service;
};

export function createLibre311Context(ctx: Libre311Context) {
	setContext(libre311CtxKey, ctx);
	return ctx;
}

export function useLibre311Service(): Libre311Service {
	return getContext<Libre311Context>(libre311CtxKey).service;
}
