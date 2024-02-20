import type { AxiosInstance } from 'axios';
import axios from 'axios';
import { z } from 'zod';
import { BaseObservable } from '../EventBus/EventBus';

// Auth props schema
const UnityAuthServicePropsSchema = z.object({
	baseURL: z.string()
});

export type UnityAuthServiceProps = z.infer<typeof UnityAuthServicePropsSchema>;

// Auth Login Schema
const UnityAuthLoginResponseSchema = z.object({
	access_token: z.string(),
	token_type: z.string(),
	expires_in: z.number(),
	username: z.string()
});

export type UnityAuthLoginResponse = z.infer<typeof UnityAuthLoginResponseSchema>;
export type UnityAuthLogoutResponse = z.infer<typeof UnityAuthLoginResponseSchema>;

export type UnityAuthEventMap = {
	login: UnityAuthLoginResponse;
	logout: UnityAuthLogoutResponse;
};
// Auth Interface
export type UnityAuthService = BaseObservable<UnityAuthEventMap> & {
	login(email: string, password: string): Promise<UnityAuthLoginResponse>;
	logout(): UnityAuthLogoutResponse;
};

// Auth Implementation
export class UnityAuthServiceImpl
	extends BaseObservable<UnityAuthEventMap>
	implements UnityAuthService
{
	private axiosInstance: AxiosInstance;

	private constructor(props: UnityAuthServiceProps) {
		super();
		UnityAuthServicePropsSchema.parse(props);
		this.axiosInstance = axios.create({ baseURL: props.baseURL });
	}

	public static create(props: UnityAuthServiceProps) {
		return new UnityAuthServiceImpl({ ...props });
	}

	async login(email: string, password: string): Promise<UnityAuthLoginResponse> {
		try {
			const res = await this.axiosInstance.post('/login', {
				username: email,
				password: password
			});

			const loginRes = UnityAuthLoginResponseSchema.parse(res.data);
			this.publish('login', loginRes);
			return loginRes;
		} catch (e) {
			throw new Error(e?.toString());
		}
	}

	logout() {
		const response = {
			access_token: '',
			token_type: '',
			expires_in: 0,
			username: ''
		};
		this.publish('logout', response);

		return response;
	}
}

// Auth Factory
export function unityAuthServiceFactory(props: UnityAuthServiceProps): UnityAuthService {
	return UnityAuthServiceImpl.create(props);
}
