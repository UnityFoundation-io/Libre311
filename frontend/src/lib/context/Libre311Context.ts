import { type Libre311Service } from '$lib/services/Libre311/Libre311';
import { unityAuthServiceFactory, type UnityAuthService, type UnityAuthServiceProps } from '$lib/services/UnityAuth/UnityAuth';
import { LinkResolver } from '$lib/services/LinkResolver';
import { getContext, setContext } from 'svelte';

const libre311CtxKey = Symbol();

export type Libre311Context = {
	service: Libre311Service;
	linkResolver: LinkResolver;
	unityAuthService: UnityAuthService;
};

export type Libre311ContextProviderProps = {
	service: Libre311Service;
	unityAuthServiceProps: UnityAuthServiceProps;
};

export function createLibre311Context(props: Libre311ContextProviderProps) {
	const linkResolver = new LinkResolver();
	const unityAuthService = unityAuthServiceFactory(props.unityAuthServiceProps);
	const ctx = {
		...props,
		linkResolver,
		unityAuthService
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

export function useUnityAuthService(): UnityAuthService {
	return getContext<Libre311Context>(libre311CtxKey).unityAuthService;
}