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
	logout: void;
};
const CompleteLoginResponseSchema = UnityAuthLoginResponseSchema.merge(
	UserPermissionsSuccessResponseSchema
);

export type CompleteLoginResponse = z.infer<typeof CompleteLoginResponseSchema>;
// Auth Interface
export type UnityAuthService = BaseObservable<UnityAuthEventMap> & {
	login(email: string, password: string): Promise<CompleteLoginResponse>;
	getLoginData(): CompleteLoginResponse | undefined;
	logout(): void;
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

		this.loginData = this.retrieveLoginData();
		this.publish('logout', undefined);

		this.userPermissionsResolver = props.userPermissionsResolver;
	}

	public static create(props: UnityAuthServiceProps) {
		return new UnityAuthServiceImpl({ ...props });
	}

	getLoginData(): CompleteLoginResponse | undefined {
		return this.loginData;
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

		this.publish('login', completeLoginRes);
		sessionStorage.setItem(this.loginDataKey, JSON.stringify(completeLoginRes));
		return completeLoginRes;
	}

	logout() {
		sessionStorage.removeItem(this.loginDataKey);
		this.publish('logout', undefined);
	}

	private retrieveLoginData(): CompleteLoginResponse | undefined {
		const loginInfo = sessionStorage.getItem(this.loginDataKey);

		if (!loginInfo) {
			return;
		}

		return CompleteLoginResponseSchema.parse(JSON.parse(loginInfo));
	}
}

// Auth Factory
export function unityAuthServiceFactory(props: UnityAuthServiceProps): UnityAuthService {
	return UnityAuthServiceImpl.create(props);
}
