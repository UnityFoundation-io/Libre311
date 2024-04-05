import type { AxiosInstance } from 'axios';
import axios from 'axios';
import { z } from 'zod';
import { BaseObservable } from '../EventBus/EventBus';
import {
	isUserPermissionsSuccessResponse,
	type UserPermissionsResolver,
	type UserPermissionsSuccessResponse
} from '../Libre311/UserPermissionsResolver';

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

export type CompleteLoginResponse = UnityAuthLoginResponse & UserPermissionsSuccessResponse;

// Auth Interface
export type UnityAuthService = BaseObservable<UnityAuthEventMap> & {
	login(email: string, password: string): Promise<CompleteLoginResponse>;
	logout(): void;
};

// Auth Implementation
export class UnityAuthServiceImpl
	extends BaseObservable<UnityAuthEventMap>
	implements UnityAuthService
{
	private axiosInstance: AxiosInstance;
	private userPermissionsResolver: UserPermissionsResolver;
	private constructor(props: UnityAuthServiceProps) {
		super();
		UnityAuthServicePropsSchema.parse(props);
		this.axiosInstance = axios.create({ baseURL: props.baseURL });
		this.userPermissionsResolver = props.userPermissionsResolver;
	}

	public static create(props: UnityAuthServiceProps) {
		return new UnityAuthServiceImpl({ ...props });
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
		return completeLoginRes;
	}

	logout() {
		this.publish('logout', undefined);
	}
}

// Auth Factory
export function unityAuthServiceFactory(props: UnityAuthServiceProps): UnityAuthService {
	return UnityAuthServiceImpl.create(props);
}
