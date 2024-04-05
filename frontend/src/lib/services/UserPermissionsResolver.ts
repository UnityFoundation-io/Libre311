import { z } from 'zod';
import axios from 'axios';
import type { UnityAuthLoginResponse } from './UnityAuth/UnityAuth';
import { LibrePermissionsSchema } from './Libre311/Libre311';

export const UserPermissionsSuccessResponseSchema = z.object({
	permissions: z.array(LibrePermissionsSchema)
});

export type UserPermissionsSuccessResponse = z.infer<typeof UserPermissionsSuccessResponseSchema>;

export function isUserPermissionsSuccessResponse(
	unknown: unknown
): unknown is UserPermissionsSuccessResponse {
	return UserPermissionsSuccessResponseSchema.safeParse(unknown).success;
}

export const UserPermissionsFailureResponseSchema = z.object({
	errorMessage: z.string()
});

export type UserPermissionsFailureResponse = z.infer<typeof UserPermissionsFailureResponseSchema>;

export const UserPermissionsResponseSchema = z.union([
	UserPermissionsSuccessResponseSchema,
	UserPermissionsFailureResponseSchema
]);

export type UserPermissionsResponse = z.infer<typeof UserPermissionsResponseSchema>;

export type UserPermissionsResolver = {
	getUserPermissions(loginRes: UnityAuthLoginResponse): Promise<UserPermissionsResponse>;
};

export class UserPermissionsResolverImpl implements UserPermissionsResolver {
	constructor(private props: { libreBaseUrl: string; jurisdictionId: string }) {}

	async getUserPermissions(loginRes: UnityAuthLoginResponse): Promise<UserPermissionsResponse> {
		const res = await axios.get<unknown>(
			this.props.libreBaseUrl + `/${this.props.jurisdictionId}/principal/permissions`,
			{
				headers: {
					Authorization: `Bearer ${loginRes.access_token}`
				}
			}
		);

		return UserPermissionsResponseSchema.parse(res.data);
	}
}
