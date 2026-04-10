import { z } from 'zod';
import { EmailSchema, ServiceRequestSchema } from '../Libre311';

// Request Schema
export const UpdateSensitiveServiceRequestSchema = z
	.object({
		agency_email: EmailSchema.optional(),
		service_notice: z.string().nullish(),
		priority: z.string().nullish(),
		project_id: z.number().nullish(),
		attribute_validation: z.enum(['VALIDATED', 'NEEDS_REVIEW', 'APPROVED']).optional()
	})
	.merge(ServiceRequestSchema);

// Response Schema
export const UpdateSensitiveServiceRequestResponseSchema = z.object({
	service_request_id: z.number()
});

// Request Type
export type UpdateSensitiveServiceRequestRequest = z.infer<
	typeof UpdateSensitiveServiceRequestSchema
>;

// Response Type
export type UpdateSensitiveServiceRequestResponse = z.infer<
	typeof UpdateSensitiveServiceRequestResponseSchema
>;
