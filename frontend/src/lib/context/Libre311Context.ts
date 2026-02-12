import {
	libre311Factory,
	type Libre311Service,
	type Libre311ServiceProps
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

const libre311CtxKey = Symbol();

export type UserInfo = CompleteLoginResponse | undefined;

export type Libre311Context = {
	service: Libre311Service;
	linkResolver: LinkResolver;
	unityAuthService: UnityAuthService;
	mode: Mode;
	user: Readable<UserInfo>;
	alertError: (unknown: unknown) => void;
	networkStatus: NetworkStatus;
	offlineQueue: OfflineQueue;
	syncSignal: Readable<number>;
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
	const baseLibre311Service = libre311Factory({ ...props.libreServiceProps, recaptchaService });

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
	const user: Writable<UserInfo> = writable(unityAuthService.getLoginData());
	unityAuthService.subscribe('login', (args) => user.set(args));
	unityAuthService.subscribe('login', (args) => libre311Service.setAuthInfo(args));
	unityAuthService.subscribe('logout', () => libre311Service.setAuthInfo(undefined));
	unityAuthService.subscribe('logout', () => user.set(undefined));

	function alertError(unknown: unknown) {
		console.error(unknown);
		if (isAxiosError(unknown)) {
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
		alertError,
		networkStatus,
		offlineQueue,
		syncSignal
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
