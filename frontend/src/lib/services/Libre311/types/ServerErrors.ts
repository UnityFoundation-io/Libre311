import { z } from 'zod';

export const HasMessageSchema = z.object({
	message: z.string()
});

export type HasMessage = z.infer<typeof HasMessageSchema>;

export function checkHasMessage(unknown: unknown): unknown is HasMessage {
	return HasMessageSchema.safeParse(unknown).success;
}

export const HateoasErrorResponseSchema = z
	.object({
		_embedded: z
			.object({
				errors: z.array(HasMessageSchema)
			})
			.optional()
	})
	.merge(HasMessageSchema);

export type HateoasErrorResponse = z.infer<typeof HateoasErrorResponseSchema>;

export function isHateoasErrorResponse(unknown: unknown): unknown is HateoasErrorResponse {
	return HateoasErrorResponseSchema.safeParse(unknown).success;
}

export const Libre311ServerErrorResponseSchema = z
	.object({
		logref: z.string()
	})
	.merge(HateoasErrorResponseSchema);

export type Libre311ServerErrorResponse = z.infer<typeof Libre311ServerErrorResponseSchema>;

export function isLibre311ServerErrorResponse(
	unknown: unknown
): unknown is Libre311ServerErrorResponse {
	return Libre311ServerErrorResponseSchema.safeParse(unknown).success;
}

export function extractFirstErrorMessage(err: HateoasErrorResponse) {
	return err._embedded?.errors[0].message;
}
