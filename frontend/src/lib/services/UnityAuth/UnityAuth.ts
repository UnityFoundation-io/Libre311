import type { AxiosInstance } from 'axios';
import axios from 'axios';
import { z } from 'zod';
import { BaseObservable } from '../EventBus/EventBus';
import Cookies from 'js-cookie';

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

export type UnityAuthEventMap = {
	login: UnityAuthLoginResponse;
	logout: void;
};
// Auth Interface
export type UnityAuthService = BaseObservable<UnityAuthEventMap> & {
	login(email: string, password: string): Promise<UnityAuthLoginResponse>;
	logout(): void;
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
		// call retrieveLoginDataFromCookie
	}

	public static create(props: UnityAuthServiceProps) {
		return new UnityAuthServiceImpl({ ...props });
	}

	async login(email: string, password: string): Promise<UnityAuthLoginResponse> {
		const res = await this.axiosInstance.post('/api/login', {
			username: email,
			password: password
		});

		const loginRes = UnityAuthLoginResponseSchema.parse(res.data);
		this.publish('login', loginRes);
		// Set loginRes to cookie
		return loginRes;
	}

	logout() {
		this.publish('logout', void 0);
	}

	// private void retrieveLoginDataFromCookie() {
	//   // Check for login info from Cookie
	//   // If it exists, publish login event with Login info (this.publish('login', loginRes);)
	// }
}

// Auth Factory
export function unityAuthServiceFactory(props: UnityAuthServiceProps): UnityAuthService {
	return UnityAuthServiceImpl.create(props);
}
