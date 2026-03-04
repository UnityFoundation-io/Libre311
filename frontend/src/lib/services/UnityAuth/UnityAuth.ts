import type { AxiosInstance } from 'axios';
import axios from 'axios';
import { z } from 'zod';
import { BaseObservable } from '../EventBus/EventBus';

import {
	isUserPermissionsSuccessResponse,
	UserPermissionsSuccessResponseSchema,
	type UserPermissionsResolver
} from '../UserPermissionsResolver';

// Auth props schema
const UnityAuthServicePropsSchema = z.object({
	baseURL: z.string()
});

export type UnityAuthServiceProps = z.infer<typeof UnityAuthServicePropsSchema> & {
	userPermissionsResolver: UserPermissionsResolver;
};

// Auth Login Schema
const UnityAuthLoginResponseSchema = z.object({
	access_token: z.string(),
	token_type: z.string(),
	expires_in: z.number(),
	username: z.string()
});

export type UnityAuthLoginResponse = z.infer<typeof UnityAuthLoginResponseSchema>;

export type UnityAuthEventMap = {
	login: CompleteLoginResponse;
	logout: { reason?: 'expired' } | undefined;
};
const CompleteLoginResponseSchema = UnityAuthLoginResponseSchema.merge(
	UserPermissionsSuccessResponseSchema
);

export type CompleteLoginResponse = z.infer<typeof CompleteLoginResponseSchema>;
// Auth Interface
export type UnityAuthService = BaseObservable<UnityAuthEventMap> & {
	login(email: string, password: string): Promise<CompleteLoginResponse>;
	getLoginData(): CompleteLoginResponse | undefined;
	getLoginDataExpiration(): number | undefined;
	logout(reason?: 'expired'): void;
};

// Auth Implementation
export class UnityAuthServiceImpl
	extends BaseObservable<UnityAuthEventMap>
	implements UnityAuthService
{
	private loginDataKey: string = 'loginData';
	private axiosInstance: AxiosInstance;
	private userPermissionsResolver: UserPermissionsResolver;
	private loginData: CompleteLoginResponse | undefined;
	private constructor(props: UnityAuthServiceProps) {
		super();
		UnityAuthServicePropsSchema.parse(props);
		this.axiosInstance = axios.create({ baseURL: props.baseURL });

		this.axiosInstance.interceptors.response.use(
			(response) => response,
			(error) => {
				if (axios.isAxiosError(error) && error.response?.status === 401) {
					if (error.config?.headers?.['Authorization']) {
						this.logout('expired');
					}
				}
				return Promise.reject(error);
			}
		);

		this.loginData = this.retrieveLoginData();

		this.userPermissionsResolver = props.userPermissionsResolver;
	}

	public static create(props: UnityAuthServiceProps) {
		return new UnityAuthServiceImpl({ ...props });
	}

	getLoginData(): CompleteLoginResponse | undefined {
		return this.loginData;
	}

	getLoginDataExpiration(): number | undefined {
		if (this.loginData) {
			return this.getTokenExpiration(this.loginData.access_token);
		}
		return undefined;
	}

	async login(email: string, password: string): Promise<CompleteLoginResponse> {
		const res = await this.axiosInstance.post('/api/login', {
			username: email,
			password: password
		});

		const loginRes = UnityAuthLoginResponseSchema.parse(res.data);

		const permissionsRes = await this.userPermissionsResolver.getUserPermissions(loginRes);
		if (!isUserPermissionsSuccessResponse(permissionsRes)) {
			throw new Error(permissionsRes.errorMessage);
		}
		const completeLoginRes: CompleteLoginResponse = { ...loginRes, ...permissionsRes };

		this.loginData = completeLoginRes;
		this.publish('login', completeLoginRes);
		sessionStorage.setItem(this.loginDataKey, JSON.stringify(completeLoginRes));
		return completeLoginRes;
	}

	logout(reason?: 'expired') {
		sessionStorage.removeItem(this.loginDataKey);
		this.publish('logout', { reason });
	}

	private getTokenExpiration(token: string): number | undefined {
		try {
			const base64Url = token.split('.')[1];
			const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
			const jsonPayload = decodeURIComponent(
				atob(base64)
					.split('')
					.map(function (c) {
						return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
					})
					.join('')
			);

			const payload = JSON.parse(jsonPayload);
			return payload.exp ? payload.exp * 1000 : undefined;
		} catch (e) {
			return undefined;
		}
	}

	private isTokenExpired(token: string, offsetMs: number = 0): boolean {
		const expiration = this.getTokenExpiration(token);
		return expiration ? expiration < Date.now() + offsetMs : true;
	}

	private retrieveLoginData(): CompleteLoginResponse | undefined {
		const loginInfo = sessionStorage.getItem(this.loginDataKey);

		if (!loginInfo) {
			return;
		}

		try {
			const loginData = CompleteLoginResponseSchema.parse(JSON.parse(loginInfo));
			// Use a 10s grace period on bootstrap to avoid immediate session expiration modal
			if (this.isTokenExpired(loginData.access_token, 10000)) {
				sessionStorage.removeItem(this.loginDataKey);
				return;
			}
			return loginData;
		} catch (e) {
			sessionStorage.removeItem(this.loginDataKey);
			return;
		}
	}
}

// Auth Factory
export function unityAuthServiceFactory(props: UnityAuthServiceProps): UnityAuthService {
	return UnityAuthServiceImpl.create(props);
}
