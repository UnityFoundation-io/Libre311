import {
	libre311Factory,
	type Libre311Service,
	type Libre311ServiceProps
} from '$lib/services/Libre311/Libre311';
import {
	unityAuthServiceFactory,
	type UnityAuthService,
	type UnityAuthServiceProps
} from '$lib/services/UnityAuth/UnityAuth';
import { LinkResolver } from '$lib/services/LinkResolver';
import type { Mode } from '$lib/services/mode';
import { getContext, setContext } from 'svelte';
import {
	recaptchaServiceFactory,
	type RecaptchaServiceProps
} from '$lib/services/RecaptchaService';
import { writable, type Writable } from 'svelte/store';

const libre311CtxKey = Symbol();

export type Libre311Context = {
	service: Libre311Service;
	linkResolver: LinkResolver;
	unityAuthService: UnityAuthService;
	mode: Mode;
	username: Writable<string>;
	setUsername: (username: string) => void;
	unsetUsername: () => void;
};

export type Libre311ContextProviderProps = {
	libreServiceProps: Omit<Libre311ServiceProps, 'recaptchaService'>;
	unityAuthServiceProps: UnityAuthServiceProps;
	recaptchaServiceProps: RecaptchaServiceProps;
	mode: Mode;
};

export function createLibre311Context(props: Libre311ContextProviderProps) {
	const linkResolver = new LinkResolver();
	const unityAuthService = unityAuthServiceFactory(props.unityAuthServiceProps);
	const recaptchaService = recaptchaServiceFactory(props.mode, props.recaptchaServiceProps);
	const libre311Service = libre311Factory({ ...props.libreServiceProps, recaptchaService });
	const username: Writable<string> = writable('');

	const setUsername = (newUsername: string) => {
		username.update((u: string) => (u = newUsername));
	};

	const unsetUsername = () => {
		username.update((u: string) => (u = ''));
	};

	unityAuthService.subscribe('login', (args) => libre311Service.setAuthInfo(args));
	unityAuthService.subscribe('logout', () => libre311Service.setAuthInfo(undefined));

	const ctx: Libre311Context = {
		mode: props.mode,
		service: libre311Service,
		linkResolver,
		unityAuthService,
		username,
		setUsername,
		unsetUsername
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
