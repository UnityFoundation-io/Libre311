import { type Libre311Service } from '$lib/services/Libre311/Libre311';
import { LinkResolver } from '$lib/services/LinkResolver';
import type { Mode } from '$lib/services/mode';
import { getContext, setContext } from 'svelte';

const libre311CtxKey = Symbol();

export type Libre311Context = {
	service: Libre311Service;
	linkResolver: LinkResolver;
	mode: Mode;
};

export type Libre311ContextProviderProps = {
	service: Libre311Service;
	mode: Mode;
	recaptchaKey: string;
};

export function createLibre311Context(props: Libre311ContextProviderProps) {
	const linkResolver = new LinkResolver();
	const ctx: Libre311Context = {
		...props,
		linkResolver
	};
	setContext(libre311CtxKey, ctx);
	return ctx;
}

export function useLibre311Context(): Libre311Context {
	return getContext<Libre311Context>(libre311CtxKey);
}

export function useLibre311Service(): Libre311Service {
	return getContext<Libre311Context>(libre311CtxKey).service;
}
