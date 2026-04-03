import {
	libre311Factory,
	type Libre311Service,
	type Libre311ServiceProps,
	type Project
} from '$lib/services/Libre311/Libre311';
import {
	unityAuthServiceFactory,
	type UnityAuthService,
	type UnityAuthServiceProps,
	type CompleteLoginResponse
} from '$lib/services/UnityAuth/UnityAuth';
import { LinkResolver } from '$lib/services/LinkResolver';
import type { Mode } from '$lib/services/mode';
import { getContext, setContext } from 'svelte';
import {
	recaptchaServiceFactory,
	type RecaptchaServiceProps
} from '$lib/services/RecaptchaService';
import { writable, type Readable, type Writable } from 'svelte/store';
import type { Libre311Alert } from './Libre311AlertStore';
import {
	checkHasMessage,
	extractFirstErrorMessage,
	isHateoasErrorResponse,
	isLibre311ServerErrorResponse
} from '$lib/services/Libre311/types/ServerErrors';
import { isAxiosError } from 'axios';
import { UserPermissionsResolverImpl } from '$lib/services/UserPermissionsResolver';
import { createNetworkStatus, type NetworkStatus } from '$lib/services/NetworkStatus';
import { createOfflineQueue, type OfflineQueue } from '$lib/services/OfflineQueue';
import { createBackgroundSync } from '$lib/services/BackgroundSync';
import { OfflineAwareLibre311Service } from '$lib/services/Libre311/OfflineAwareLibre311Service';
import { goto } from '$app/navigation';

const libre311CtxKey = Symbol();

export type UserInfo = CompleteLoginResponse | undefined;

export type Libre311Context = {
	service: Libre311Service;
	linkResolver: LinkResolver;
	unityAuthService: UnityAuthService;
	mode: Mode;
	user: Readable<UserInfo>;
	projects: Readable<Project[]>;
	fetchProjectsAdmin: () => Promise<void>;
	alertError: (unknown: unknown) => void;
	networkStatus: NetworkStatus;
	offlineQueue: OfflineQueue;
	syncSignal: Readable<number>;
	sessionExpired: Writable<boolean>;
} & Libre311Alert;

export type Libre311ContextProviderProps = {
	libreServiceProps: Omit<Libre311ServiceProps, 'recaptchaService'>;
	unityAuthServiceProps: Omit<UnityAuthServiceProps, 'userPermissionsResolver'>;
	recaptchaServiceProps: RecaptchaServiceProps;
	mode: Mode;
};

export function createLibre311Context(props: Libre311ContextProviderProps & Libre311Alert) {
	const linkResolver = new LinkResolver();
	const userPermissionsResolver = new UserPermissionsResolverImpl({
		libreBaseUrl: props.libreServiceProps.baseURL,
		jurisdictionId: props.libreServiceProps.jurisdictionConfig.jurisdiction_id
	});
	const unityAuthService = unityAuthServiceFactory({
		userPermissionsResolver,
		...props.unityAuthServiceProps
	});
	const recaptchaService = recaptchaServiceFactory(props.mode, props.recaptchaServiceProps);
	const baseLibre311Service = libre311Factory({
		...props.libreServiceProps,
		recaptchaService,
		onUnauthorized: (reason) => unityAuthService.logout(reason)
	});

	const networkStatus = createNetworkStatus();
	const offlineQueue = createOfflineQueue();
	const libre311Service = new OfflineAwareLibre311Service(
		baseLibre311Service,
		networkStatus,
		offlineQueue
	);

	const syncSignal = writable(0);
	const backgroundSync = createBackgroundSync(baseLibre311Service, offlineQueue, props, () =>
		syncSignal.update((n) => n + 1)
	);

	const projects = writable<Project[]>([]);

	async function fetchProjects() {
		try {
			const res = await libre311Service.getProjects();
			projects.set(res);
		} catch (e) {
			console.error('Failed to fetch projects', e);
		}
	}

	async function fetchProjectsAdmin() {
		try {
			const res = await libre311Service.getProjectsAdmin();
			projects.set(res);
		} catch (e) {
			console.error('Failed to fetch admin projects', e);
		}
	}

	fetchProjects();

	// Trigger sync when coming back online
	networkStatus.isOnline.subscribe((online) => {
		if (online) {
			backgroundSync.syncAll();
		} else {
			props.alert({
				type: 'warn',
				title: 'Offline',
				description:
					'You are offline. Requests will be queued and submitted when connectivity returns.'
			});
		}
	});

	libre311Service.setAuthInfo(unityAuthService.getLoginData());
	const sessionExpired = writable(false);
	const user: Writable<UserInfo> = writable(unityAuthService.getLoginData());

	let expirationTimer: ReturnType<typeof setTimeout> | undefined;

	function scheduleExpiration() {
		if (expirationTimer) clearTimeout(expirationTimer);
		const expiration = unityAuthService.getLoginDataExpiration();
		if (expiration) {
			const delay = expiration - Date.now() - 1000; // Fire 1s early to be proactive
			if (delay > 0) {
				expirationTimer = setTimeout(() => {
					unityAuthService.logout('expired');
				}, delay);
			} else {
				unityAuthService.logout('expired');
			}
		}
	}

	// Bootstrap check
	if (typeof window !== 'undefined') {
		const isProtectedRoute =
			window.location.pathname.startsWith('/admin') ||
			window.location.pathname.startsWith('/groups') ||
			window.location.pathname.startsWith('/projects');

		if (!unityAuthService.getLoginData() && isProtectedRoute) {
			goto('/');
		} else if (unityAuthService.getLoginData()) {
			scheduleExpiration();
		}
	}

	unityAuthService.subscribe('login', (args) => {
		user.set(args);
		libre311Service.setAuthInfo(args);
		scheduleExpiration();
		fetchProjects();
	});
	unityAuthService.subscribe('logout', (args) => {
		if (expirationTimer) {
			clearTimeout(expirationTimer);
			expirationTimer = undefined;
		}
		if (args?.reason === 'expired') {
			sessionExpired.set(true);
		} else {
			libre311Service.setAuthInfo(undefined);
			user.set(undefined);
			if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
				goto('/login');
			}
		}
		fetchProjects();
	});

	function alertError(unknown: unknown) {
		console.error(unknown);
		if (isAxiosError(unknown)) {
			if (unknown.response?.status === 401) {
				return;
			}
			if (isLibre311ServerErrorResponse(unknown.response?.data)) {
				const libre311ServerError = unknown.response.data;
				props.alert({
					type: 'error',
					title: libre311ServerError.message,
					description: `${extractFirstErrorMessage(libre311ServerError)} \n logref: ${libre311ServerError.logref}`
				});
				return;
			} else if (isHateoasErrorResponse(unknown.response?.data)) {
				const hateoasErrorResponse = unknown.response.data;
				props.alert({
					type: 'error',
					title: hateoasErrorResponse.message,
					description: extractFirstErrorMessage(hateoasErrorResponse)
				});
				return;
			}
		}

		if (checkHasMessage(unknown)) {
			props.alert({
				type: 'error',
				title: 'Error',
				description: unknown.message
			});
		} else {
			props.alert({
				type: 'error',
				title: 'Something unexpected happened',
				description: 'The complete error has been logged in the console'
			});
		}
	}

	const ctx: Libre311Context = {
		...props,
		service: libre311Service,
		linkResolver,
		unityAuthService,
		user,
		projects,
		fetchProjectsAdmin,
		alertError,
		networkStatus,
		offlineQueue,
		syncSignal,
		sessionExpired
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
