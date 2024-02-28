import { z } from 'zod';
import { ServiceSchema } from '../Libre311';

// Request Schema
export const CreateServiceRequestSchema = z.object({
	service_code: z.string(),
	service_name: z.string(),
	description: z.string(),
	service_definition: z.string(),
	group_id: z.number()
});

// Response Schema
export const CreateServiceResponseSchema = z
	.object({
		id: z.number(),
		jurisdiction_id: z.string(),
		group_id: z.number()
	})
	.merge(ServiceSchema);

// Request Type
export type CreateServiceRequest = z.infer<typeof CreateServiceRequestSchema>;

// Response Type
export type CreateServiceResponse = z.infer<typeof CreateServiceResponseSchema>;
